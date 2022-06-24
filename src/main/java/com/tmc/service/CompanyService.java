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


    public Company get(String id) {
        if (id == null) {
            throw new InvalidParameterException("Missing ID.");
        }
        System.out.println("IN COMPANY SERVICE");
        return companyCache.getUnchecked(id);
    }

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


    public Company create(Company request) {
        Company company = new Company(request);

        companyCache.put(company.getId(), company);
        return dao.saveCompany(company);
    }


    public Company edit(Company request) {
        if (request.getId() == null) {
            throw new InvalidParameterException("Missing ID.");
        }
        Company company = new Company(request, companyCache.getUnchecked(request.getId()));

        companyCache.put(company.getId(), company);
        return dao.saveCompany(company);
    }

    public Company deactivate(String id) {
        if (id == null) {
            throw new InvalidParameterException("Missing ID.");
        }
        Company company = new Company(companyCache.getUnchecked(id));
        company.setIsActive(false);
        return dao.saveCompany(company);
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
