package com.tmc.controller;

import com.tmc.dependency.DaggerServiceComponent;
import com.tmc.dependency.ServiceComponent;
import com.tmc.model.*;
import com.tmc.model.request.*;
import lombok.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Data
@RestController
@RequestMapping("/time-manager")
public class Controller {

    /*
     ********************** HEALTH CHECK ****************
     */
    @GetMapping("/")
    public String healthCheck() {
        return "Welcome to Time Manager!";
    }

    /*
     ********************** TIMESHEET ****************
     */
    @GetMapping("/timesheet/{id}")
    public ResponseEntity<Timesheet> getTimesheet(@PathVariable String id) {
        ServiceComponent dagger = DaggerServiceComponent.create();
        return new ResponseEntity<>(dagger.provideServiceManager().getTimesheet(id), HttpStatus.OK);
    }

    @GetMapping("/timesheet")
    public ResponseEntity<List<Timesheet>> getTimesheets(@RequestBody List<String> ids) {
        ServiceComponent dagger = DaggerServiceComponent.create();
        return new ResponseEntity<>(dagger.provideServiceManager().getTimesheets(ids), HttpStatus.OK);
    }

    @GetMapping("/timesheet/search/{id}")
    public ResponseEntity<List<Timesheet>> getTimesheetsSearch(@PathVariable String id,
                                                               @RequestParam (required = false) TypeEnum type,
                                                               @RequestParam (required = false) String workType,
                                                               @RequestParam (required = false) Long before,
                                                               @RequestParam (required = false) Long after,
                                                               @RequestParam (required = false) String department,
                                                               @RequestParam (required = false) Boolean complete,
                                                               @RequestParam (required = false) Boolean validated,
                                                               @RequestParam (required = false) String orderNum) {
        ServiceComponent dagger = DaggerServiceComponent.create();
        return new ResponseEntity<>(dagger.provideServiceManager().searchTimesheets(type, id, workType, department,
                                                        orderNum, before, after, complete, validated), HttpStatus.OK);
    }

    @PostMapping("/timesheet")
    public ResponseEntity<Timesheet> createTimesheet(@RequestBody CreateTimesheetRequest request) {
        ServiceComponent dagger = DaggerServiceComponent.create();
        return new ResponseEntity<>(dagger.provideServiceManager().createTimesheet(request), HttpStatus.OK);
    }

    @PutMapping("/timesheet/{id}")
    public ResponseEntity<Timesheet> editTimesheet(@PathVariable String id,
                                                 @RequestBody EditTimesheetRequest request) {
        ServiceComponent dagger = DaggerServiceComponent.create();
        return new ResponseEntity<>(dagger.provideServiceManager().editTimesheet(id, request), HttpStatus.OK);
    }

    /*
     ********************** EMPLOYEE ****************
     */
    @GetMapping("/employee/{id}")
    public ResponseEntity<Employee> getEmployee(@PathVariable String id) {
        ServiceComponent dagger = DaggerServiceComponent.create();
        return new ResponseEntity<>(dagger.provideServiceManager().getEmployee(id), HttpStatus.OK);
    }

    @GetMapping("/employee")
    public ResponseEntity<List<Employee>> getEmployees(@RequestBody List<String> ids) {
        ServiceComponent dagger = DaggerServiceComponent.create();
        return new ResponseEntity<>(dagger.provideServiceManager().getEmployees(ids), HttpStatus.OK);
    }

    @GetMapping("/employee/search/{id}")
    public ResponseEntity<List<Employee>> getEmployeesSearch(@PathVariable String id,
                                                             @RequestParam (required = false) TypeEnum type,
                                                             @RequestParam (required = false) String name,
                                                             @RequestParam (required = false) String email,
                                                             @RequestParam (required = false) Boolean isActive) {
        ServiceComponent dagger = DaggerServiceComponent.create();
        return new ResponseEntity<>(dagger.provideServiceManager().searchEmployees(type, id, name, email, isActive), HttpStatus.OK);
    }

    @PostMapping("/employee")
    public ResponseEntity<Employee> createEmployee(@RequestBody CreateEmployeeRequest request) {
        ServiceComponent dagger = DaggerServiceComponent.create();
        return new ResponseEntity<>(dagger.provideServiceManager().createEmployee(request), HttpStatus.OK);
    }

    @PutMapping("/employee/{id}")
    public ResponseEntity<Employee> editEmployee(@PathVariable String id,
                                                 @RequestBody EditEmployeeRequest request) {
        ServiceComponent dagger = DaggerServiceComponent.create();
        return new ResponseEntity<>(dagger.provideServiceManager().editEmployee(id, request), HttpStatus.OK);
    }

    /*
    ********************** CUSTOMER ****************
     */
    @GetMapping("/customer/{id}")
    public ResponseEntity<Customer> getCustomer(@PathVariable String id) {
        ServiceComponent dagger = DaggerServiceComponent.create();
        return new ResponseEntity<>(dagger.provideServiceManager().getCustomer(id), HttpStatus.OK);
    }

    @GetMapping("/customer")
    public ResponseEntity<List<Customer>> getCustomers(@RequestBody List<String> ids) {
        ServiceComponent dagger = DaggerServiceComponent.create();
        return new ResponseEntity<>(dagger.provideServiceManager().getCustomers(ids), HttpStatus.OK);
    }

    @PostMapping("/customer")
    public ResponseEntity<Customer> createCustomer(@RequestBody CreateCustomerRequest request) {
        ServiceComponent dagger = DaggerServiceComponent.create();
        return new ResponseEntity<>(dagger.provideServiceManager().createCustomer(request), HttpStatus.OK);
    }

    @PutMapping("/customer/{id}")
    public ResponseEntity<Customer> editCustomer(@PathVariable String id,
                                                 @RequestBody EditCustomerRequest request) {
        ServiceComponent dagger = DaggerServiceComponent.create();
        return new ResponseEntity<>(dagger.provideServiceManager().editCustomer(id, request), HttpStatus.OK);
    }

    /*
     ********************** COMPANY ****************
     */
    @GetMapping("/company/{id}")
    public ResponseEntity<Company> getCompany(@PathVariable String id) {
        ServiceComponent dagger = DaggerServiceComponent.create();
        return new ResponseEntity<>(dagger.provideServiceManager().getCompany(id), HttpStatus.OK);
    }

    @PostMapping("/company")
    public ResponseEntity<Company> createCompany(@RequestBody CreateCompanyRequest request) {
        ServiceComponent dagger = DaggerServiceComponent.create();
        return new ResponseEntity<>(dagger.provideServiceManager().createCompany(request), HttpStatus.OK);
    }

    @PutMapping("/company/{id}")
    public ResponseEntity<Company> editCompany(@PathVariable String id,
                                                 @RequestBody EditCompanyRequest request) {
        ServiceComponent dagger = DaggerServiceComponent.create();
        return new ResponseEntity<>(dagger.provideServiceManager().editCompany(id, request), HttpStatus.OK);
    }
}
