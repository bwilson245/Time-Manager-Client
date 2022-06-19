package com.tmc.activity;

import com.tmc.dao.CustomerCachingDao;
import com.tmc.model.Customer;
import com.tmc.model.Location;
import com.tmc.model.request.CreateCustomerRequest;
import com.tmc.model.request.EditCustomerRequest;

import javax.inject.Inject;
import java.util.List;

public class CustomerActivity {
    private final CustomerCachingDao cachingDao;

    @Inject
    public CustomerActivity(CustomerCachingDao cachingDao) {
        this.cachingDao = cachingDao;
    }

    public Customer getCustomer(String id) {
        return cachingDao.getCustomer(id);
    }

    public List<Customer> getCustomers(List<String> ids) {
        return cachingDao.getCustomers(ids);
    }

    public Customer createCustomer(CreateCustomerRequest request) {
        return cachingDao.createCustomer(request);
    }

    public Customer editCustomer(String id, EditCustomerRequest request) {
        return cachingDao.editCustomer(id, request);
    }

    public Customer deactivateCustomer(String id) {
        return cachingDao.deactivateCustomer(id);
    }
}
