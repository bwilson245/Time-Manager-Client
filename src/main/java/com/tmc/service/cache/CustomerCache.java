package com.tmc.service.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.tmc.model.Customer;
import com.tmc.service.dao.DynamoDbDao;
import com.tmc.service.inter.CachingDao;
import lombok.Data;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Data
@Singleton
public class CustomerCache implements CachingDao<Customer> {
    private LoadingCache<String, Customer> cache;
    private DynamoDbDao dao;

    @Inject
    public CustomerCache(DynamoDbDao dao) {
        this.dao = dao;
        this.cache = CacheBuilder.newBuilder()
                .expireAfterWrite(1, TimeUnit.DAYS)
                .maximumSize(1000)
                .build(CacheLoader.from(this.dao::getCustomer));
    }
    @Override
    public Customer get(String id) {
        return cache.getUnchecked(id);
    }
    @Override
    public List<Customer> get(List<String> ids) {
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
}
