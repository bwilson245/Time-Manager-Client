package com.tmc.service.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.tmc.model.Company;
import com.tmc.model.Employee;
import com.tmc.model.Timesheet;
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
public class EmployeeCache implements CachingDao<Employee> {
    private LoadingCache<String, Employee> cache;
    private DynamoDbDao dao;

    @Inject
    public EmployeeCache(DynamoDbDao dao) {
        this.dao = dao;
        this.cache = CacheBuilder.newBuilder()
                .expireAfterWrite(1, TimeUnit.DAYS)
                .maximumSize(1000)
                .build(CacheLoader.from(this.dao::getEmployee));
    }
    @Override
    public Employee get(String id) {
        return cache.getUnchecked(id);
    }
    @Override
    public List<Employee> get(List<String> ids) {
        List<Employee> cached = new ArrayList<>();
        List<String> notCached = new ArrayList<>();
        for (String id : ids) {
            Employee employee = cache.getIfPresent(id);
            if (employee == null) {
                notCached.add(id);
            } else {
                cached.add(employee);
            }
        }
        cached.addAll(dao.getEmployees(notCached));
        for (Employee employee : cached) {
            cache.put(employee.getId(), employee);
        }
        return cached;
    }
}
