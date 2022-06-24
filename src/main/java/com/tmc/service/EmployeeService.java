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
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.lang.reflect.Array;
import java.security.InvalidParameterException;
import java.util.*;
import java.util.stream.Collectors;

@Data
@Singleton
public class EmployeeService {
    private CacheManager cacheManager;
    private DynamoDbDao dao;
    private LoadingCache<String, Timesheet> timesheetCache;
    private LoadingCache<String, Customer> customerCache;
    private LoadingCache<String, Employee> employeeCache;
    private LoadingCache<String, Company> companyCache;

    @Inject
    public EmployeeService(CacheManager cacheManager, DynamoDbDao dao) {
        this.cacheManager = cacheManager;
        this.dao = dao;
        this.timesheetCache = cacheManager.getTimesheetCache();
        this.employeeCache = cacheManager.getEmployeeCache();
        this.customerCache = cacheManager.getCustomerCache();
        this.companyCache = cacheManager.getCompanyCache();
    }


    /**
     * Gets the employee associated with the id.
     * @param id - the id of the employee.
     * @return - returns an Employee object.
     */
    public Employee get(String id) {
        if (id == null) {
            throw new InvalidParameterException("Missing ID.");
        }
        return employeeCache.getUnchecked(id);
    }

    public List<Employee> get(List<String> ids) {
        if (ids == null) {
            throw new InvalidParameterException("Missing ID.");
        }

        List<Employee> cached = new ArrayList<>();
        List<String> notCached = new ArrayList<>();

        for (String id : ids) {
            Employee employee = employeeCache.getIfPresent(id);
            if (employee == null) {
                notCached.add(id);
            } else {
                cached.add(employee);
            }
        }
        cached.addAll(dao.getEmployees(notCached));
        for (Employee employee : cached) {
            employeeCache.put(employee.getId(), employee);
        }
        return cached;
    }


    public QueryResultPage<Employee> search(String id, String name, String email, Boolean isActive, Map<String, AttributeValue> startKey, Integer limit) {
        if (id == null) {
            throw new InvalidParameterException("Missing Company ID.");
        }
        return dao.searchEmployees(id, name, email, isActive, startKey, limit);
    }



    /**
     * Creates a new employee and adds it to the company.
     * @param request - the values to assign to the new Employee object.
     * @return - the new Employee object.
     */

    public Employee create(Employee request) {
        if (request.getCompanyId() == null) {
            throw new InvalidParameterException("Missing ID.");
        }
        Company company = cacheManager.getCompanyCache().getUnchecked(request.getCompanyId());

        Employee employee = new Employee(request);

        Set<String> employeeIds = new HashSet<>(company.getEmployeeIds());
        employeeIds.add(employee.getId());
        company.setEmployeeIds(new ArrayList<>(employeeIds));

        employeeCache.put(employee.getId(), employee);
        dao.saveCompany(company);
        return dao.saveEmployee(employee);
    }


    /**
     * Modifies an Employee object based on the values in the request parameter
     * @param request - (optional) - the values to assign to the employee. The values are optional, however the request
     *                object itself is required to pass into the method.
     * @return - returns the modified employee.
     */

    public Employee edit(Employee request) {
        if (request.getId() == null) {
            throw new InvalidParameterException("Missing ID.");
        }
        Employee employee = new Employee(cacheManager.getEmployeeCache().getUnchecked(request.getId()));

        employeeCache.put(employee.getId(), employee);
        return dao.saveEmployee(employee);
    }

    /**
     * Sets an employees isActive to false;
     * @param id
     * @return
     */

    public Employee deactivate(String id) {
        if (id == null) {
            throw new InvalidParameterException("Missing ID.");
        }
        Employee employee = new Employee(cacheManager.getEmployeeCache().getUnchecked(id));
        employee.setIsActive(false);
        return dao.saveEmployee(employee);
    }

    public static void main(String[] args) throws InterruptedException {
        ServiceComponent dagger = DaggerServiceComponent.create();
        EmployeeService service = dagger.provideEmployeeService();
        service.generateEmployees();
    }

    public void generateEmployees() throws InterruptedException {
        int numEmployees = 3;
        for (int i = 0; i < numEmployees; i++) {
            Employee employee = Employee.builder()
                    .companyId("75b1a9c7-6887-4b75-8c2c-7a7219962f62")
                    .name("John Doe " + i)
                    .email("testEmail@test.com")
                    .password("testPassword")
                    .customerIds(new ArrayList<>())
                    .timesheetIds(new ArrayList<>())
                    .type(Const.EMPLOYEE)
                    .isActive(true)
                    .build();
            create(employee);
            Thread.sleep(1000);
        }
    }

}
