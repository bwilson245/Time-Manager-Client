package com.tmc.service;

import com.google.common.cache.LoadingCache;
import com.tmc.dependency.DaggerServiceComponent;
import com.tmc.dependency.ServiceComponent;
import com.tmc.model.*;
import com.tmc.service.dao.DynamoDbDao;
import com.tmc.service.manager.CacheManager;
import lombok.Data;

import org.checkerframework.checker.units.qual.C;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Singleton
public class CompanyService {
    private CacheManager cacheManager;
    private DynamoDbDao dao;
    private LoadingCache<String, Timesheet> timesheetCache;
    private LoadingCache<String, Customer> customerCache;
    private LoadingCache<String, Employee> employeeCache;
    private LoadingCache<String, Company> companyCache;

    @Inject
    public CompanyService(CacheManager cacheManager, DynamoDbDao dao) {
        this.cacheManager = cacheManager;
        this.dao = dao;
        this.timesheetCache = cacheManager.getTimesheetCache();
        this.employeeCache = cacheManager.getEmployeeCache();
        this.customerCache = cacheManager.getCustomerCache();
        this.companyCache = cacheManager.getCompanyCache();
    }

    /**
     * Returns a company from the provided id.
     * @param id - The id of the desired company.
     * @return - returns a company object
     */
    public Company get(String id) {
        if (id == null) {
            throw new InvalidParameterException("Missing ID.");
        }
        System.out.println("IN COMPANY SERVICE");
        return companyCache.getUnchecked(id);
    }

    /**
     * Retrieves a list of companies from a list of companyIds.
     * This method checks the cache to see if it exists. If not, it will add the id to a list of notCached ids.
     * The dao then uses a batch load to retrieve all the uncached companies and they are added to the cached list.
     * @param ids - The list of companyIds desired.
     * @return - Returns a list of company objects.
     */
    public List<Company> get(List<String> ids) {
        if (ids == null) {
            throw new InvalidParameterException("Missing ID.");
        }
        List<Company> cached = new ArrayList<>();
        List<String> notCached = new ArrayList<>();
        for (String id : ids) {
            Company company = companyCache.getIfPresent(id);
            if (company == null) {
                notCached.add(id);
            } else {
                cached.add(company);
            }
        }
        cached.addAll(dao.getCompanies(notCached));
        for (Company company : cached) {
            companyCache.put(company.getId(), company);
        }
        return cached;
    }


    /**
     * Creates a new company object to be stored in the database.
     * Generates a new id and any null fields are set to default values.
     * default string = *
     * default boolean = false
     * default list = new ArrayList()
     * @param request - The company object desired to be created.
     * @return - returns the newly created company object.
     */
    public Company create(Company request) {
        Company company = new Company(request);

        companyCache.put(company.getId(), company);
        dao.saveCompany(company);
        return company;
    }


    /**
     * Edits a company to reflect the information passed into the request and saves it to the database.
     * Any null values will reflect the original company's information.
     * The only required value inside the request is the original companyId.
     * @param request - A company object containing a valid companyId and the fields desired to be changed.
     * @return - returns A new company Object.
     */
    public Company edit(Company request) {
        if (request.getId() == null) {
            throw new InvalidParameterException("Missing ID.");
        }
        Company company = new Company(request, companyCache.getUnchecked(request.getId()));

        companyCache.put(company.getId(), company);
        dao.saveCompany(company);
        return company;
    }

    /**
     * Removes a company object AND ALL ASSOCIATED VALUES WITH IT including customers, employees, and timesheets.
     * NOT REVERSABLE. PEFORM AT YOUR OWN RISK.
     * @param id - The id of the company to be removed.
     */
    public void deleteCompany(String id) {
        Company company = companyCache.getUnchecked(id);
        List<Customer> customers = company.getCustomerIds().stream().map(customerCache::getUnchecked).collect(Collectors.toList());
        List<Employee> employees = company.getEmployeeIds().stream().map(employeeCache::getUnchecked).collect(Collectors.toList());
        List<Timesheet> timesheets = company.getTimesheetIds().stream().map(timesheetCache::getUnchecked).collect(Collectors.toList());

        dao.batchDeleteCustomers(customers);
        dao.batchDeleteEmployee(employees);
        dao.batchDeleteTimesheets(timesheets);
        dao.deleteCompany(company);
    }

    public static void main(String[] args) {
        ServiceComponent dagger = DaggerServiceComponent.create();
        CompanyService service = dagger.provideCompanyService();
        Company company = Company.builder()
                .name("Test Company")
                .location(Location.builder()
                        .address1("Test address 1")
                        .city("test city")
                        .state("test state")
                        .zip("test zip")
                        .build())
                .build();
        service.create(company);

    }

}
