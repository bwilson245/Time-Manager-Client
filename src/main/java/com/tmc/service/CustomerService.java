package com.tmc.service;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.tmc.model.Company;
import com.tmc.model.Customer;
import com.tmc.model.Location;
import com.tmc.model.TypeEnum;
import com.tmc.model.request.CreateCustomerRequest;
import com.tmc.model.request.EditCustomerRequest;
import com.tmc.service.dao.DynamoDbDao;

import com.tmc.service.inter.ServiceDao;
import lombok.Data;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Data
@Singleton
public class CustomerService implements ServiceDao<Customer, CreateCustomerRequest, EditCustomerRequest> {
    private DynamoDbDao dao;
    private CacheManager cacheManager;

    @Inject
    public CustomerService(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
        this.dao = cacheManager.getDao();
    }


    @Override
    public Customer create(CreateCustomerRequest request) {
        Company company = cacheManager.getCompanyCache().get(request.getCompanyId());

        Location location = Location.builder()
                .address1(request.getAddress1().toUpperCase())
                .address2(request.getAddress2().toUpperCase())
                .city(request.getCity().toUpperCase())
                .state(request.getState().toUpperCase())
                .zip(request.getZip())
                .build();

        Customer customer = Customer.builder()
                .id(UUID.randomUUID().toString())
                .companyId(company.getId())
                .name(request.getName().toUpperCase())
                .location(location)
                .build();

        List<String> customerIds = new ArrayList<>(company.getCustomerIds());
        customerIds.add(customer.getId());
        company.setCustomerIds(customerIds);

        cacheManager.getCustomerCache().getCache().put(customer.getId(), customer);
        dao.saveCompany(company);
        return dao.saveCustomer(customer);
    }

    @Override
    public Customer edit(String id, EditCustomerRequest request) {
        Customer customer = cacheManager.getCustomerCache().getCache().getUnchecked(id);

        Location location = Location.builder()
                .address1(Optional.ofNullable(request.getAddress1()).orElse(customer.getLocation().getAddress1()).toUpperCase())
                .address2(Optional.ofNullable(request.getAddress2()).orElse(customer.getLocation().getAddress2()).toUpperCase())
                .city(Optional.ofNullable(request.getCity()).orElse(customer.getLocation().getCity()).toUpperCase())
                .state(Optional.ofNullable(request.getState()).orElse(customer.getLocation().getState()).toUpperCase())
                .zip(Optional.ofNullable(request.getZip()).orElse(customer.getLocation().getZip()))
                .build();

        customer.setLocation(location);
        customer.setName(Optional.ofNullable(request.getName()).orElse(customer.getName()).toUpperCase());

        cacheManager.getCustomerCache().getCache().put(customer.getId(), customer);
        return dao.saveCustomer(customer);
    }

    @Override
    public Customer deactivate(String id) {
        Customer customer = cacheManager.getCustomerCache().getCache().getUnchecked(id);
        customer.setIsActive(false);
        return dao.saveCustomer(customer);
    }

    @Override
    public List<Customer> search(TypeEnum type, String id, String name, String email, Boolean isActive) {
        return null;
    }
    @Override
    public List<Customer> search(TypeEnum type, String id, String workType, String department, String orderNum, Long before, Long after, Boolean complete, Boolean validated) {
        return null;
    }
}
