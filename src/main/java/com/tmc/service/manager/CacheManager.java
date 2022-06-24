package com.tmc.service.manager;

import com.google.common.cache.LoadingCache;
import com.tmc.model.Company;
import com.tmc.model.Customer;
import com.tmc.model.Employee;
import com.tmc.model.Timesheet;
import com.tmc.service.dao.DynamoDbDao;
import lombok.Data;

import javax.inject.Inject;
import javax.inject.Singleton;

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
    public CacheManager(DynamoDbDao dao, LoadingCache<String, Timesheet> timesheetCache, LoadingCache<String, Customer> customerCache, LoadingCache<String, Employee> employeeCache, LoadingCache<String, Company> companyCache) {
        if (uniqueInstance == null) {
            this.dao = dao;
            this.timesheetCache = timesheetCache;
            this.customerCache = customerCache;
            this.employeeCache = employeeCache;
            this.companyCache = companyCache;
            uniqueInstance = this;
        } else {
            this.timesheetCache = uniqueInstance.getTimesheetCache();
            this.customerCache = uniqueInstance.getCustomerCache();
            this.employeeCache = uniqueInstance.getEmployeeCache();
            this.companyCache = uniqueInstance.getCompanyCache();
        }

    }
}
