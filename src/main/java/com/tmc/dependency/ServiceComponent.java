package com.tmc.dependency;

import com.tmc.service.CompanyService;
import com.tmc.service.CustomerService;
import com.tmc.service.EmployeeService;
import com.tmc.service.TimesheetService;
import dagger.Component;

import javax.inject.Singleton;

@Component (modules = BuildModule.class)
@Singleton
public interface ServiceComponent {
    TimesheetService provideTimesheetService();
    CompanyService provideCompanyService();
    CustomerService provideCustomerService();
    EmployeeService provideEmployeeService();
}
