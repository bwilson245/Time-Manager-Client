package com.tmc.service;

import com.amazonaws.services.dynamodbv2.datamodeling.QueryResultPage;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.google.common.cache.LoadingCache;
import com.tmc.dependency.DaggerServiceComponent;
import com.tmc.dependency.ServiceComponent;
import com.tmc.model.*;
import com.tmc.model.request.SearchEmployeeRequest;
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
     * Returns an employee from the provided id.
     * @param id - The id of the desired employee.
     * @return - returns a employee object
     */
    public Employee get(String id) {
        if (id == null) {
            throw new InvalidParameterException("Missing ID.");
        }
        return employeeCache.getUnchecked(id);
    }



    /**
     * Retrieves a list of employees from a list of employeeIds.
     * This method checks the cache to see if it exists. If not, it will add the id to a list of notCached ids.
     * The dao then uses a batch load to retrieve all the uncached employees, and they are added to the cached list.
     * @param ids - The list of employeeIds desired.
     * @return - Returns a list of employee objects.
     */
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

    /**
     * Searches a company for its employees based on optional parameters. The only required parameter is the id of the company.
     * @param request - The SearchEmployeeRequest object containing the desired search criteria.
     * @return - returns a QueryResultPage containing a list of employees and if available, a lastEvaluatedKey.
     */
    public QueryResultPage<Employee> search(SearchEmployeeRequest request) {
        if (request.getCompanyId() == null) {
            throw new InvalidParameterException("Missing companyId.");
        }
        return dao.search(request);
    }



    /**
     * Creates a new employee object to be stored in the database.
     * The only required information is the companyId that is associated with this employee.
     * Generates a new id and any null fields are set to default values.
     * default string = *
     * default boolean = false
     * default list = new ArrayList()
     * @param request - The employee object desired to be created.
     * @return - returns the newly created employee object.
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
     * Edits an employee to reflect the information passed into the request and saves it to the database.
     * Any null values will reflect the original employee's information.
     * The only required value inside the request is the original employeeId.
     * @param request - An employee object containing a valid employeeId and the fields desired to be changed.
     * @return - returns a new customer Object.
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
     * Removes an employee from the database and removes the id from all associated customers, timesheets, and companies.
     * @param id - The id of the employee to be removed.
     */
    public void deleteEmployee(String id) {
        Employee employee = employeeCache.getUnchecked(id);
        Company company = companyCache.getUnchecked(employee.getCompanyId());
        List<Timesheet> timesheets = employee.getTimesheetIds().stream().map(timesheetCache::getUnchecked).collect(Collectors.toList());
        List<Customer> customers = employee.getCustomerIds().stream().map(customerCache::getUnchecked).collect(Collectors.toList());

        company.getEmployeeIds().remove(id);
        timesheets.forEach(t -> t.getEmployeeIds().remove(id));
        customers.forEach(c -> c.getEmployeeIds().remove(id));

        dao.saveCompany(company);
        dao.batchSaveTimesheets(timesheets);
        dao.batchSaveCustomers(customers);
        dao.deleteEmployee(employee);
    }


    // Reserved for creation of new objects for testing

    public static void main(String[] args) throws InterruptedException {
        ServiceComponent dagger = DaggerServiceComponent.create();
        EmployeeService service = dagger.provideEmployeeService();
        service.generateEmployees();
    }

    public void generateEmployees() throws InterruptedException {
        int numEmployees = 30;
        for (int i = 0; i < numEmployees; i++) {
            Employee employee = Employee.builder()
                    .companyId("company.07ae01de-a9df-465c-990c-d28217d89baf")
                    .name("John Doe " + i)
                    .email("testEmail@test.com")
                    .password("testPassword")
                    .customerIds(new ArrayList<>())
                    .timesheetIds(new ArrayList<>())
                    .isActive(true)
                    .build();
            create(new Employee(employee));
            Thread.sleep(1100);
        }
    }

}
