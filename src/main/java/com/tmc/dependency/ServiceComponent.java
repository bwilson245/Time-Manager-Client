package com.tmc.dependency;

import com.tmc.service.CompanyService;
import com.tmc.service.CustomerService;
import com.tmc.service.EmployeeService;
import com.tmc.service.TimesheetService;
import com.tmc.service.manager.CacheManager;
import dagger.Component;

import javax.inject.Singleton;

@Component (modules = BuildModule.class)
@Singleton
public interface ServiceComponent {
    CacheManager provideCacheManager();
    TimesheetService provideTimesheetService();
    CompanyService provideCompanyService();
    CustomerService provideCustomerService();
    EmployeeService provideEmployeeService();
}
