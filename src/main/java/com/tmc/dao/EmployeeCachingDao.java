package com.tmc.dao;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.tmc.exception.EmployeeNotFoundException;
import com.tmc.model.Employee;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class EmployeeCachingDao {
    private final DynamoDbDao dao;
    private final LoadingCache<String, Employee> cache;

    @Inject
    public EmployeeCachingDao(DynamoDbDao dao) {
        this.dao = dao;
        this.cache = CacheBuilder.newBuilder()
                .expireAfterWrite(1, TimeUnit.DAYS)
                .maximumSize(1000)
                .build(CacheLoader.from(dao::getEmployee));
    }

    public Employee getEmployee(String id) {
        return cache.getUnchecked(id);
    }

    public List<Employee> getEmployees(List<String> ids) {
        List<String> notCached = new ArrayList<>();
        List<Employee> cached = new ArrayList<>();
        for (String s : ids) {
            if (cache.getIfPresent(s) == null) {
                notCached.add(s);
            } else {
                cached.add(cache.getUnchecked(s));
            }
        }
        cached.addAll(dao.getEmployees(notCached));
        for (Employee e : cached) {
            e.setPassword("*****");
        }
        return cached;
    }

    public void saveEmployee(Employee employee) {
        cache.put(employee.getId(), employee);
        dao.saveEmployee(employee);
    }

    public void editEmployee(String id, String name, String email, String password) {
        Employee employee = cache.getUnchecked(id);
        if (employee == null) {
            throw new EmployeeNotFoundException("Could not find employee with id: " + id);
        }
        employee.setName(Optional.ofNullable(name).orElse(employee.getName()));
        employee.setEmail(Optional.ofNullable(email).orElse(employee.getEmail()));
        employee.setPassword(Optional.ofNullable(password).orElse(employee.getPassword()));
        cache.put(employee.getId(), employee);
        dao.saveEmployee(employee);
    }

    public void deleteEmployee(String id) {
        dao.deleteEmployee(cache.getUnchecked(id));
        cache.invalidate(id);
    }
}
