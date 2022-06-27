package com.tmc.service;

import com.amazonaws.services.dynamodbv2.datamodeling.QueryResultPage;
import com.google.common.cache.LoadingCache;
import com.tmc.dependency.DaggerServiceComponent;
import com.tmc.dependency.ServiceComponent;
import com.tmc.model.*;
import com.tmc.model.request.SearchCustomerRequest;
import com.tmc.service.dao.DynamoDbDao;

import com.tmc.service.manager.CacheManager;
import lombok.Data;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.lang.reflect.Field;
import java.security.InvalidParameterException;
import java.util.*;
import java.util.stream.Collectors;

@Data
@Singleton
public class CustomerService {
    private DynamoDbDao dao;
    private LoadingCache<String, Timesheet> timesheetCache;
    private LoadingCache<String, Customer> customerCache;
    private LoadingCache<String, Employee> employeeCache;
    private LoadingCache<String, Company> companyCache;
    private CacheManager cacheManager;

    @Inject
    public CustomerService(DynamoDbDao dao, CacheManager cacheManager) {
        this.cacheManager = cacheManager;
        this.dao = dao;
        this.timesheetCache = cacheManager.getTimesheetCache();
        this.employeeCache = cacheManager.getEmployeeCache();
        this.customerCache = cacheManager.getCustomerCache();
        this.companyCache = cacheManager.getCompanyCache();
    }

    /**
     * Returns a customer from the provided id.
     * @param id - The id of the desired customer.
     * @return - returns a customer object
     */
    public Customer get(String id) {
        if (id == null) {
            throw new InvalidParameterException("Missing ID.");
        }
        return customerCache.getUnchecked(id);
    }

    /**
     * Retrieves a list of customers from a list of customerIds.
     * This method checks the cache to see if it exists. If not, it will add the id to a list of notCached ids.
     * The dao then uses a batch load to retrieve all the uncached customers and they are added to the cached list.
     * @param ids - The list of customerIds desired.
     * @return - Returns a list of customer objects.
     */
    public List<Customer> get(List<String> ids) {
        if (ids == null) {
            throw new InvalidParameterException("Missing ID.");
        }
        List<Customer> cached = new ArrayList<>();
        List<String> notCached = new ArrayList<>();
        for (String id : ids) {
            Customer customer = customerCache.getIfPresent(id);
            if (customer == null) {
                notCached.add(id);
            } else {
                cached.add(customer);
            }
        }
        cached.addAll(dao.getCustomers(notCached));
        for (Customer customer : cached) {
            customerCache.put(customer.getId(), customer);
        }
        return cached;
    }

    /**
     * Searches a company for its customers based on optional parameters. The only required parameter is the id of the company.
     * @param companyId - The companyId associated with company containing the customers.
     * @param request - The SearchCustomerRequest object containing the information.
     * @return - returns a QueryResultPage containing a list of Customers and if available, a lastEvaluatedKey
     */
    public QueryResultPage<Customer> search(String companyId, SearchCustomerRequest request) {
        if (companyId == null) {
            throw new InvalidParameterException("Missing companyId.");
        }
        return dao.search(companyId, request);
    }

    /**
     * Creates a new customer object to be stored in the database.
     * The only required information is the companyId that is associated with this customer.
     * Generates a new id and any null fields are set to default values.
     * default string = *
     * default boolean = false
     * default list = new ArrayList()
     * @param request - The customer object desired to be created.
     * @return - returns the newly created customer object.
     */
    public Customer create(Customer request) {
        if (request.getCompanyId() == null) {
            throw new InvalidParameterException("Missing ID.");
        }
        Company company = cacheManager.getCompanyCache().getUnchecked(request.getCompanyId());

        Customer customer = new Customer(request);

        company.getCustomerIds().add(customer.getId());
        company.setCustomerIds(new ArrayList<>(company.getCustomerIds()));

        customerCache.put(customer.getId(), customer);
        dao.saveCompany(company);
        dao.saveCustomer(customer);
        return customer;
    }

    /**
     * Edits a customer to reflect the information passed into the request and saves it to the database.
     * Any null values will reflect the original customers information.
     * The only required value inside the request is the original customerId.
     * @param request - A customer object containing a valid customerId and the fields desired to be changed.
     * @return - returns A new customer Object.
     */
    public Customer edit(Customer request) {
        if (request.getId() == null) {
            throw new InvalidParameterException("Missing ID.");
        }
        Customer customer = new Customer(request, customerCache.getUnchecked(request.getId()));

        customerCache.put(customer.getId(), customer);
        dao.saveCustomer(customer);
        return customer;
    }

    /**
     * Removes a customer from the database.
     * Removes the customers ID from the company list of customerIds.
     * Removes the customers ID from the employees list of customerIds for every employee associated with this customer.
     * Appends "REMOVED." to the front of the customerId in all timesheets associated with this customer.
     * @param id - The id of the customer to be removed.
     */
    public void deleteCustomer(String id) {
        Customer customer = customerCache.getUnchecked(id);
        Company company = companyCache.getUnchecked(customer.getCompanyId());


        //************* remove customerId from employees customerIds ***********************//
        List<Employee> employees = customer.getEmployeeIds().stream().map(employeeCache::getUnchecked).collect(Collectors.toList());
        employees.forEach(e -> {
            List<String> customerIds = e.getCustomerIds();
            customerIds.remove(id);
            e.setCustomerIds(customerIds);
        });


        //************* remove customerId from company customerIds ***********************//
        List<String> customerIds = company.getCustomerIds();
        customerIds.remove(id);
        company.setCustomerIds(customerIds);


        //************* remove customerId from timesheets customerIds ***********************//
        List<Timesheet> timesheets = customer.getTimesheetIds().stream().map(timesheetCache::getUnchecked).collect(Collectors.toList());
        timesheets.forEach(t -> t.setCustomerId("REMOVED." + t.getCustomerId()));


        //************* save information ***********************//
        dao.batchSaveTimesheets(timesheets);
        dao.saveCompany(company);
        dao.batchSaveEmployees(employees);
        dao.deleteCustomer(customer);
    }

    public static void main(String[] args) throws InterruptedException {
        ServiceComponent dagger = DaggerServiceComponent.create();
        CustomerService service = dagger.provideCustomerService();
        service.generateCustomers();
    }

    public void generateCustomers() throws InterruptedException {
        int numCustomers = 1;
        for (int i = 0; i < numCustomers; i++) {
            Customer customer = Customer.builder()
                    .companyId("company.07ae01de-a9df-465c-990c-d28217d89baf")
                    .name("Customer " + i)
                    .location(Location.builder()
                            .address1("test address")
                            .city("test city")
                            .state("test state")
                            .zip("12345")
                            .build())
                    .timesheetIds(new ArrayList<>())
                    .employeeIds(new ArrayList<>())
                    .isActive(true)
                    .build();
            create(new Customer(customer));
            Thread.sleep(1000);
        }
    }

}
