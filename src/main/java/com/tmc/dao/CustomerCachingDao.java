package com.tmc.dao;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.tmc.model.Company;
import com.tmc.model.Customer;
import com.tmc.model.Location;
import com.tmc.model.request.CreateCustomerRequest;
import com.tmc.model.request.EditCustomerRequest;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class CustomerCachingDao {
    private final DynamoDbDao dao;
    private final CompanyCachingDao companyCachingDao;
    private final LoadingCache<String, Customer> cache;

    @Inject
    public CustomerCachingDao(DynamoDbDao dao, CompanyCachingDao companyCachingDao) {
        this.dao = dao;
        this.companyCachingDao = companyCachingDao;
        this.cache = CacheBuilder.newBuilder()
                .expireAfterWrite(1, TimeUnit.DAYS)
                .maximumSize(1000)
                .build(CacheLoader.from(dao::getCustomer));
    }

    public Customer getCustomer(String id) {
        return cache.getUnchecked(id);
    }

    public List<Customer> getCustomers(List<String> ids) {
        List<Customer> cached = new ArrayList<>();
        List<String> notCached = new ArrayList<>();
        for (String id : ids) {
            Customer customer = cache.getIfPresent(id);
            if (customer == null) {
                notCached.add(id);
            } else {
                cached.add(customer);
            }
        }
        cached.addAll(dao.getCustomers(notCached));
        for (Customer customer : cached) {
            cache.put(customer.getId(), customer);
        }
        return cached;
    }

    public Customer createCustomer(CreateCustomerRequest request) {
        Company company = companyCachingDao.getCompany(request.getCompanyId());

        Location location = Location.builder()
                .address1(request.getAddress1())
                .address2(request.getAddress2())
                .city(request.getCity())
                .state(request.getState())
                .zip(request.getZip())
                .build();

        Customer customer = Customer.builder()
                .id(UUID.randomUUID().toString())
                .companyId(company.getId())
                .name(request.getName())
                .location(location)
                .timesheetIds(new ArrayList<>())
                .isActive(true)
                .build();

        List<String> customerIds = new ArrayList<>(company.getCustomerIds());
        customerIds.add(customer.getId());
        company.setCustomerIds(customerIds);

        cache.put(customer.getId(), customer);
        dao.saveCompany(company);
        return dao.saveCustomer(customer);
    }

    public Customer editCustomer(String id, EditCustomerRequest request) {
        Customer customer = cache.getUnchecked(id);

        Location location = Location.builder()
                .address1(Optional.ofNullable(request.getAddress1()).orElse(customer.getLocation().getAddress1()))
                .address2(Optional.ofNullable(request.getAddress2()).orElse(customer.getLocation().getAddress2()))
                .city(Optional.ofNullable(request.getCity()).orElse(customer.getLocation().getCity()))
                .state(Optional.ofNullable(request.getState()).orElse(customer.getLocation().getState()))
                .zip(Optional.ofNullable(request.getZip()).orElse(customer.getLocation().getZip()))
                .build();

        customer.setLocation(location);
        customer.setName(Optional.ofNullable(request.getName()).orElse(customer.getName()));

        cache.put(customer.getId(), customer);
        return dao.saveCustomer(customer);
    }

    public Customer deactivateCustomer(String id) {
        Customer customer = cache.getUnchecked(id);
        customer.setIsActive(false);
        return dao.saveCustomer(customer);
    }

}
