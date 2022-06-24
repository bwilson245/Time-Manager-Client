package com.tmc.service;

import com.amazonaws.services.dynamodbv2.datamodeling.QueryResultPage;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.google.common.cache.LoadingCache;
import com.tmc.dependency.DaggerServiceComponent;
import com.tmc.dependency.ServiceComponent;
import com.tmc.model.*;
import com.tmc.service.dao.DynamoDbDao;

import com.tmc.service.manager.CacheManager;
import lombok.Data;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.security.InvalidParameterException;
import java.util.*;

@Data
@Singleton
public class CustomerService {
    private DynamoDbDao dao;
    private LoadingCache<String, Timesheet> timesheetCache;
    private LoadingCache<String, Customer> customerCache;
    private LoadingCache<String, Employee> employeeCache;
    private LoadingCache<String, Company> companyCache;
    private CacheManager cacheManager;

    @Inject
    public CustomerService(DynamoDbDao dao, CacheManager cacheManager) {
        this.cacheManager = cacheManager;
        this.dao = dao;
        this.timesheetCache = cacheManager.getTimesheetCache();
        this.employeeCache = cacheManager.getEmployeeCache();
        this.customerCache = cacheManager.getCustomerCache();
        this.companyCache = cacheManager.getCompanyCache();
    }

    public Customer get(String id) {
        if (id == null) {
            throw new InvalidParameterException("Missing ID.");
        }
        return customerCache.getUnchecked(id);
    }

    public List<Customer> get(List<String> ids) {
        if (ids == null) {
            throw new InvalidParameterException("Missing ID.");
        }
        List<Customer> cached = new ArrayList<>();
        List<String> notCached = new ArrayList<>();
        for (String id : ids) {
            Customer customer = customerCache.getIfPresent(id);
            if (customer == null) {
                notCached.add(id);
            } else {
                cached.add(customer);
            }
        }
        cached.addAll(dao.getCustomers(notCached));
        for (Customer customer : cached) {
            customerCache.put(customer.getId(), customer);
        }
        return cached;
    }


    public QueryResultPage<Customer> search(String id, String name, Location location, Boolean isActive, Map<String, AttributeValue> startKey, Integer limit) {
        if (id == null) {
            throw new InvalidParameterException("Missing ID.");
        }
        return dao.searchCustomers(id, name, location, isActive, startKey, limit);
    }


    public Customer create(Customer request) {
        if (request.getCompanyId() == null) {
            throw new InvalidParameterException("Missing ID.");
        }
        Company company = cacheManager.getCompanyCache().getUnchecked(request.getCompanyId());

        Customer customer = new Customer(request);

        company.getCustomerIds().add(customer.getId());
        company.setCustomerIds(new ArrayList<>(company.getCustomerIds()));

        customerCache.put(customer.getId(), customer);
        dao.saveCompany(company);
        dao.saveCustomer(customer);
        return customer;
    }


    public Customer edit(Customer request) {
        if (request.getId() == null) {
            throw new InvalidParameterException("Missing ID.");
        }
        Customer customer = new Customer(request, customerCache.getUnchecked(request.getId()));

        customerCache.put(customer.getId(), customer);
        dao.saveCustomer(customer);
        return customer;
    }


    public void deleteCustomer(String id) {
        Customer customer = customerCache.getUnchecked(id);
        dao.deleteCustomer(customer);
    }

    public static void main(String[] args) throws InterruptedException {
        ServiceComponent dagger = DaggerServiceComponent.create();
        CustomerService service = dagger.provideCustomerService();
        service.generateCustomers();
    }

    public void generateCustomers() throws InterruptedException {
        int numCustomers = 1;
        for (int i = 0; i < numCustomers; i++) {
            Customer customer = Customer.builder()
                    .companyId("75b1a9c7-6887-4b75-8c2c-7a7219962f62")
                    .id(UUID.randomUUID().toString())
                    .name("Customer " + i)
                    .location(Location.builder()
                            .address1("test address")
                            .city("test city")
                            .state("test state")
                            .zip("12345")
                            .build())
                    .timesheetIds(new ArrayList<>())
                    .employeeIds(new ArrayList<>())
                    .type(Const.CUSTOMER)
                    .isActive(true)
                    .build();
            create(customer);
            Thread.sleep(1000);
        }
    }

}
