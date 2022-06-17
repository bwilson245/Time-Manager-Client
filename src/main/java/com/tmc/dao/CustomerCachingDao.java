package com.tmc.dao;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.tmc.exception.CustomerNotFoundException;
import com.tmc.exception.EmployeeNotFoundException;
import com.tmc.model.Customer;
import com.tmc.model.Employee;
import com.tmc.model.Location;
import com.tmc.model.Timesheet;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class CustomerCachingDao {
    private final DynamoDbDao dao;
    private final LoadingCache<String, Customer> cache;

    @Inject
    public CustomerCachingDao(DynamoDbDao dao) {
        this.dao = dao;
        this.cache = CacheBuilder.newBuilder()
                .expireAfterWrite(1, TimeUnit.DAYS)
                .maximumSize(1000)
                .build(CacheLoader.from(dao::getCustomer));
    }

    public Customer getCustomer(String id) {
        return cache.getUnchecked(id);
    }

    public List<Customer> getCustomers(List<String> ids) {
        List<String> notCached = new ArrayList<>();
        List<Customer> cached = new ArrayList<>();
        for (String s : ids) {
            if (cache.getIfPresent(s) == null) {
                notCached.add(s);
            } else {
                cached.add(cache.getUnchecked(s));
            }
        }
        cached.addAll(dao.getCustomers(notCached));
        return cached;
    }

    public void saveCustomer(Customer customer) {
        cache.put(customer.getId(), customer);
        dao.saveCustomer(customer);
    }

    public void editCustomer(String id, String name, Location location) {
        Customer customer = cache.getUnchecked(id);
        if (customer == null) {
            throw new CustomerNotFoundException("Could not find customer with id: " + id);
        }
        customer.setName(Optional.ofNullable(name).orElse(customer.getName()));
        customer.setLocation(Optional.ofNullable(location).orElse(customer.getLocation()));

        cache.put(customer.getId(), customer);
        dao.saveCustomer(customer);
    }

    public void deleteCustomer(String id) {
        dao.deleteCustomer(cache.getUnchecked(id));
        cache.invalidate(id);
    }
}
