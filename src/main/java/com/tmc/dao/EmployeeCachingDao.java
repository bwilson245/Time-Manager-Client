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

    /**
     * Gets the employee associated with the id.
     * @param id - the id of the employee.
     * @return - returns an Employee object.
     */
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

    public List<Employee> getEmployeesForCompany(String id, String name, String email, Boolean isActive) {
        Company company = companyCachingDao.getCompany(id);
        return dao.getEmployees("companyId-index", company, name, email, isActive);
    }

    /**
     * Creates a new employee and adds it to the company.
     * @param request - the values to assign to the new Employee object.
     * @return - the new Employee object.
     */
    public Employee createEmployee(CreateEmployeeRequest request) {
        Company company = companyCachingDao.getCompany(request.getCompanyId());

        Employee employee = Employee.builder()
                .companyId(company.getId())
                .id(UUID.randomUUID().toString())
                .name(request.getName().toUpperCase())
                .email(request.getEmail())
                .password(request.getPassword())
                .build();

        Set<String> employeeIds = new HashSet<>(company.getEmployeeIds());
        employeeIds.add(employee.getId());
        company.setEmployeeIds(new ArrayList<>(employeeIds));

        cache.put(employee.getId(), employee);
        dao.saveCompany(company);
        return dao.saveEmployee(employee);
    }

    /**
     * Modifies an Employee object.
     * @param id - (required) - the id of the employee to modify.
     * @param request - (optional) - the values to assign to the employee. The values are optional, however the request
     *                object itself is required to pass into the method.
     * @return - returns the modified employee.
     */
    public Employee editEmployee(String id, EditEmployeeRequest request) {
        Employee employee = cache.getUnchecked(id);

        employee.setName(Optional.ofNullable(request.getName()).orElse(employee.getName()));
        employee.setEmail(Optional.ofNullable(request.getEmail()).orElse(employee.getEmail()));
        employee.setPassword(Optional.ofNullable(request.getPassword()).orElse(employee.getPassword()));
        employee.setName(employee.getName().toUpperCase());

        cache.put(employee.getId(), employee);
        return dao.saveEmployee(employee);
    }

    /**
     * Sets an employee's isActive to false. Employee can still be looked up for historical purposes, but employee
     * can no longer submit Timesheet objects to the company.
     * @param id - id of the employee to deactivate.
     * @return returns the deactivated employee.
     */
    public Employee deactivateEmployee(String id) {
        Employee employee = cache.getUnchecked(id);
        employee.setIsActive(false);
        return dao.saveEmployee(employee);
    }
}
