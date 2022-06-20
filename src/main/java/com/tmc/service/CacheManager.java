package com.tmc.service;

import com.tmc.model.Timesheet;
import com.tmc.service.cache.CompanyCache;
import com.tmc.service.cache.CustomerCache;
import com.tmc.service.cache.EmployeeCache;
import com.tmc.service.cache.TimesheetCache;
import com.tmc.service.dao.DynamoDbDao;
import lombok.Data;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

@Data
@Singleton
public class CacheManager {
    private CompanyCache companyCache;
    private CustomerCache customerCache;
    private EmployeeCache employeeCache;
    private TimesheetCache timesheetCache;
    private DynamoDbDao dao;

    @Inject
    public CacheManager(CompanyCache companyCache, CustomerCache customerCache, EmployeeCache employeeCache, TimesheetCache timesheetCache, DynamoDbDao dao) {
        this.companyCache = companyCache;
        this.customerCache = customerCache;
        this.employeeCache = employeeCache;
        this.timesheetCache = timesheetCache;
        this.dao = dao;
    }

}
