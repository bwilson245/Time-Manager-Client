package com.tmc.dependency;

import com.tmc.activity.CustomerActivity;
import com.tmc.activity.EmployeeActivity;
import com.tmc.activity.TimesheetActivity;
import dagger.Component;

import javax.inject.Singleton;

@Component(modules = BuildModule.class)
@Singleton
public interface ServiceComponent {
    TimesheetActivity provideTimesheetActivity();
    CustomerActivity provideCustomerActivity();
    EmployeeActivity provideEmployeeActivity();
}
