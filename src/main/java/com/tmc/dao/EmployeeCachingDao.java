package com.tmc.dao;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.googlejavaformat.Op;
import com.tmc.model.Company;
import com.tmc.model.Employee;
import com.tmc.model.request.CreateEmployeeRequest;
import com.tmc.model.request.EditEmployeeRequest;

import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class EmployeeCachingDao {
    private final DynamoDbDao dao;
    private final CompanyCachingDao companyCachingDao;
    private final LoadingCache<String, Employee> cache;

    @Inject
    public EmployeeCachingDao(DynamoDbDao dao, CompanyCachingDao companyCachingDao) {
        this.dao = dao;
        this.companyCachingDao = companyCachingDao;
        this.cache = CacheBuilder.newBuilder()
                .expireAfterWrite(1, TimeUnit.DAYS)
                .maximumSize(1000)
                .build(CacheLoader.from(dao::getEmployee));
    }

    public Employee getEmployee(String id) {
        return cache.getUnchecked(id);
    }

    public List<Employee> getEmployees(List<String> ids) {
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

    public Employee createEmployee(CreateEmployeeRequest request) {
        Company company = companyCachingDao.getCompany(request.getCompanyId());

        Employee employee = Employee.builder()
                .companyId(company.getId())
                .id(UUID.randomUUID().toString())
                .name(request.getName())
                .email(request.getEmail())
                .password(request.getPassword())
                .timesheetIds(new ArrayList<>())
                .customerIds(new ArrayList<>())
                .isActive(true)
                .build();

        Set<String> employeeIds = new HashSet<>(company.getEmployeeIds());
        employeeIds.add(employee.getId());
        company.setEmployeeIds(new ArrayList<>(employeeIds));

        cache.put(employee.getId(), employee);
        dao.saveCompany(company);
        return dao.saveEmployee(employee);
    }

    public Employee editEmployee(String id, EditEmployeeRequest request) {
        Employee employee = cache.getUnchecked(id);

        employee.setName(Optional.ofNullable(request.getName()).orElse(employee.getName()));
        employee.setEmail(Optional.ofNullable(request.getEmail()).orElse(employee.getEmail()));
        employee.setPassword(Optional.ofNullable(request.getPassword()).orElse(employee.getPassword()));

        cache.put(employee.getId(), employee);
        return dao.saveEmployee(employee);
    }

    public Employee deactivateEmployee(String id) {
        Employee employee = cache.getUnchecked(id);
        employee.setIsActive(false);
        return dao.saveEmployee(employee);
    }
}
