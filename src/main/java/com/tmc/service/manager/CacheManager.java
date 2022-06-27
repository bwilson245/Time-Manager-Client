package com.tmc.service.manager;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.tmc.model.Company;
import com.tmc.model.Customer;
import com.tmc.model.Employee;
import com.tmc.model.Timesheet;
import com.tmc.service.dao.DynamoDbDao;
import lombok.Data;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.TimeUnit;

@Data
@Singleton
public class CacheManager {
    private static CacheManager uniqueInstance;
    private DynamoDbDao dao;
    private LoadingCache<String, Timesheet> timesheetCache;
    private LoadingCache<String, Customer> customerCache;
    private LoadingCache<String, Employee> employeeCache;
    private LoadingCache<String, Company> companyCache;

    @Inject
    public CacheManager(DynamoDbDao dao) {
        if (uniqueInstance == null) {
            this.dao = dao;
            this.timesheetCache = CacheBuilder.newBuilder()
                    .expireAfterWrite(1, TimeUnit.DAYS)
                    .maximumSize(1000)
                    .build(CacheLoader.from(dao::getTimesheet));
            this.customerCache = CacheBuilder.newBuilder()
                    .expireAfterWrite(1, TimeUnit.DAYS)
                    .maximumSize(1000)
                    .build(CacheLoader.from(dao::getCustomer));;
            this.employeeCache = CacheBuilder.newBuilder()
                    .expireAfterWrite(1, TimeUnit.DAYS)
                    .maximumSize(1000)
                    .build(CacheLoader.from(dao::getEmployee));
            this.companyCache = CacheBuilder.newBuilder()
                    .expireAfterWrite(1, TimeUnit.DAYS)
                    .maximumSize(1000)
                    .build(CacheLoader.from(dao::getCompany));
            uniqueInstance = this;
        } else {
            this.dao = uniqueInstance.getDao();
            this.timesheetCache = uniqueInstance.getTimesheetCache();
            this.customerCache = uniqueInstance.getCustomerCache();
            this.employeeCache = uniqueInstance.getEmployeeCache();
            this.companyCache = uniqueInstance.getCompanyCache();
        }

    }
}
