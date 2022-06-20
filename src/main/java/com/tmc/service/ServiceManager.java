package com.tmc.service;

import com.tmc.model.*;
import com.tmc.model.request.*;
import com.tmc.service.cache.TimesheetCache;

import javax.inject.Inject;
import java.util.List;

public class ServiceManager {
    private CompanyService companyService;
    private CustomerService customerService;
    private EmployeeService employeeService;
    private TimesheetService timesheetService;

    @Inject
    public ServiceManager(CompanyService companyService, CustomerService customerService, EmployeeService employeeService, TimesheetService timesheetService) {
        this.companyService = companyService;
        this.customerService = customerService;
        this.employeeService = employeeService;
        this.timesheetService = timesheetService;
    }

    public Timesheet getTimesheet(String id) {
        return timesheetService.getCacheManager().getTimesheetCache().get(id);
    }

    public List<Timesheet> getTimesheets(List<String> ids) {
        return timesheetService.getCacheManager().getTimesheetCache().get(ids);
    }

    public List<Timesheet> searchTimesheets(TypeEnum type, String id, String workType, String department, String orderNum,
                                            Long before, Long after, Boolean isComplete, Boolean isValidated) {
        return timesheetService.search(type, id, workType, department, orderNum, before, after, isComplete, isValidated);
    }

    public Timesheet createTimesheet(CreateTimesheetRequest request) {
        return timesheetService.create(request);
    }

    public Timesheet editTimesheet(String id, EditTimesheetRequest request) {
        return timesheetService.edit(id, request);
    }

    //*****************************************************************************************************************
    //*****************************************************************************************************************
    //*****************************************************************************************************************

    public Employee getEmployee(String id) {
        return employeeService.getCacheManager().getEmployeeCache().get(id);
    }

    public List<Employee> getEmployees(List<String> ids) {
        return employeeService.getCacheManager().getEmployeeCache().get(ids);
    }

    public List<Employee> searchEmployees(TypeEnum type, String id, String name, String email, Boolean isActive) {
        return employeeService.search(type, id, name, email, isActive);
    }

    public Employee createEmployee(CreateEmployeeRequest request) {
        return employeeService.create(request);
    }

    public Employee editEmployee(String id, EditEmployeeRequest request) {
        return employeeService.edit(id, request);
    }

    //*****************************************************************************************************************
    //*****************************************************************************************************************
    //*****************************************************************************************************************

    public Customer getCustomer(String id) {
        return customerService.getCacheManager().getCustomerCache().get(id);
    }

    public List<Customer> getCustomers(List<String> ids) {
        return customerService.getCacheManager().getCustomerCache().get(ids);
    }

    public List<Customer> searchCustomer() {
        return null;
    }

    public Customer createCustomer(CreateCustomerRequest request) {
        return customerService.create(request);
    }

    public Customer editCustomer(String id, EditCustomerRequest request) {
        return customerService.edit(id, request);
    }

    //*****************************************************************************************************************
    //*****************************************************************************************************************
    //*****************************************************************************************************************

    public Company getCompany(String id) {
        return companyService.getCacheManager().getCompanyCache().get(id);
    }

    public Company createCompany(CreateCompanyRequest request) {
        return companyService.create(request);
    }

    public Company editCompany(String id, EditCompanyRequest request) {
        return companyService.edit(id, request);
    }
}
