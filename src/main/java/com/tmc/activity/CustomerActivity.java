package com.tmc.activity;

import com.tmc.dao.CustomerCachingDao;
import com.tmc.model.Customer;
import com.tmc.model.Location;

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

    public Customer createCustomer(Customer customer) {
        cachingDao.createCustomer(customer);
        return null;
    }

    public Customer editCustomer(String id, String name, Location location) {
        cachingDao.editCustomer(id, name, location);
        return null;
    }

    public Customer deleteCustomer(String id) {
        cachingDao.deleteCustomer(id);
        return null;
    }
}
