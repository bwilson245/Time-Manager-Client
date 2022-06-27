package com.tmc.controller;

import com.amazonaws.services.dynamodbv2.datamodeling.QueryResultPage;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.tmc.dependency.DaggerServiceComponent;
import com.tmc.dependency.ServiceComponent;
import com.tmc.model.*;

import com.tmc.model.request.SearchCustomerRequest;
import com.tmc.model.request.SearchEmployeeRequest;
import com.tmc.model.request.SearchTimesheetRequest;
import lombok.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Singleton;
import java.util.*;

@Data
@RestController
@Singleton
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
        return new ResponseEntity<>(dagger.provideTimesheetService().get(id), HttpStatus.OK);
    }

    @GetMapping("/timesheet")
    public ResponseEntity<List<Timesheet>> getTimesheets(@RequestBody List<String> ids) {
        ServiceComponent dagger = DaggerServiceComponent.create();
        return new ResponseEntity<>(dagger.provideTimesheetService().get(ids), HttpStatus.OK);
    }

    @GetMapping("/timesheet/search/{id}")
    public ResponseEntity<QueryResultPage<Timesheet>> getTimesheetsSearch(@PathVariable String id,
                                                                          @RequestBody SearchTimesheetRequest request) {
        ServiceComponent dagger = DaggerServiceComponent.create();
        return new ResponseEntity<>(dagger.provideTimesheetService().search(id, request), HttpStatus.OK);
    }

    @PostMapping("/timesheet")
    public ResponseEntity<Timesheet> createTimesheet(@RequestBody Timesheet request) {
        ServiceComponent dagger = DaggerServiceComponent.create();
        return new ResponseEntity<>(dagger.provideTimesheetService().create(request), HttpStatus.OK);
    }

    @PutMapping("/timesheet")
    public ResponseEntity<Timesheet> editTimesheet(@RequestBody Timesheet request) {
        ServiceComponent dagger = DaggerServiceComponent.create();
        return new ResponseEntity<>(dagger.provideTimesheetService().edit(request, dagger.provideEmployeeService()), HttpStatus.OK);
    }

    @DeleteMapping("/timesheet/{id}")
    public ResponseEntity<Timesheet> deleteTimesheet(@PathVariable String id) {
        ServiceComponent dagger = DaggerServiceComponent.create();
        dagger.provideTimesheetService().deleteTimesheet(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /*
     ********************** EMPLOYEE ****************
     */
    @GetMapping("/employee/{id}")
    public ResponseEntity<Employee> getEmployee(@PathVariable String id) {
        ServiceComponent dagger = DaggerServiceComponent.create();
        return new ResponseEntity<>(dagger.provideEmployeeService().get(id), HttpStatus.OK);
    }

    @GetMapping("/employee")
    public ResponseEntity<List<Employee>> getEmployees(@RequestBody List<String> ids) {
        ServiceComponent dagger = DaggerServiceComponent.create();
        return new ResponseEntity<>(dagger.provideEmployeeService().get(ids), HttpStatus.OK);
    }

    @GetMapping("/employee/search/{id}")
    public ResponseEntity<QueryResultPage<Employee>> getEmployeesSearch(@PathVariable String id,
                                                                        @RequestBody SearchEmployeeRequest request) {
        ServiceComponent dagger = DaggerServiceComponent.create();
        return new ResponseEntity<>(dagger.provideEmployeeService().search(id, request), HttpStatus.OK);
    }

    @PostMapping("/employee")
    public ResponseEntity<Employee> createEmployee(@RequestBody Employee request) {
        ServiceComponent dagger = DaggerServiceComponent.create();
        return new ResponseEntity<>(dagger.provideEmployeeService().create(request), HttpStatus.OK);
    }

    @PutMapping("/employee")
    public ResponseEntity<Employee> editEmployee(@RequestBody Employee request) {
        ServiceComponent dagger = DaggerServiceComponent.create();
        return new ResponseEntity<>(dagger.provideEmployeeService().edit(request), HttpStatus.OK);
    }

    @DeleteMapping("/employee/{id}")
    public ResponseEntity<Employee> deleteEmployee(@PathVariable String id) {
        ServiceComponent dagger = DaggerServiceComponent.create();
        dagger.provideEmployeeService().deleteEmployee(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /*
    ********************** CUSTOMER ****************
     */
    @GetMapping("/customer/{id}")
    public ResponseEntity<Customer> getCustomer(@PathVariable String id) {
        ServiceComponent dagger = DaggerServiceComponent.create();
        return new ResponseEntity<>(dagger.provideCustomerService().get(id), HttpStatus.OK);
    }

    @GetMapping("/customer")
    public ResponseEntity<List<Customer>> getCustomers(@RequestBody List<String> ids) {
        ServiceComponent dagger = DaggerServiceComponent.create();
        return new ResponseEntity<>(dagger.provideCustomerService().get(ids), HttpStatus.OK);
    }

    @GetMapping("/customer/search/{companyId}")
    public ResponseEntity<QueryResultPage<Customer>> searchCustomers(@PathVariable String id,
                                                                     @RequestBody SearchCustomerRequest request) {
        ServiceComponent dagger = DaggerServiceComponent.create();
        return new ResponseEntity<>(dagger.provideCustomerService().search(id, request), HttpStatus.OK);
    }

    @PostMapping("/customer")
    public ResponseEntity<Customer> createCustomer(@RequestBody Customer request) {
        ServiceComponent dagger = DaggerServiceComponent.create();
        return new ResponseEntity<>(dagger.provideCustomerService().create(request), HttpStatus.OK);
    }

    @PutMapping("/customer")
    public ResponseEntity<Customer> editCustomer(@RequestBody Customer request) {
        ServiceComponent dagger = DaggerServiceComponent.create();
        return new ResponseEntity<>(dagger.provideCustomerService().edit(request), HttpStatus.OK);
    }

    @DeleteMapping("/customer/{id}")
    public ResponseEntity<Timesheet> deleteCustomer(@PathVariable String id) {
        ServiceComponent dagger = DaggerServiceComponent.create();
        dagger.provideCustomerService().deleteCustomer(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /*
     ********************** COMPANY ****************
     */
    @GetMapping("/company/{id}")
    public ResponseEntity<Company> getCompany(@PathVariable String id) {
        ServiceComponent dagger = DaggerServiceComponent.create();
        return new ResponseEntity<>(dagger.provideCompanyService().get(id), HttpStatus.OK);
    }

    @GetMapping("/company")
    public ResponseEntity<List<Company>> getCompanies(@RequestBody List<String> ids) {
        ServiceComponent dagger = DaggerServiceComponent.create();
        return new ResponseEntity<>(dagger.provideCompanyService().get(ids), HttpStatus.OK);
    }

    @PostMapping("/company")
    public ResponseEntity<Company> createCompany(@RequestBody Company request) {
        ServiceComponent dagger = DaggerServiceComponent.create();
        return new ResponseEntity<>(dagger.provideCompanyService().create(request), HttpStatus.OK);
    }

    @PutMapping("/company")
    public ResponseEntity<Company> editCompany(@RequestBody Company request) {
        ServiceComponent dagger = DaggerServiceComponent.create();
        return new ResponseEntity<>(dagger.provideCompanyService().edit(request), HttpStatus.OK);
    }

    @DeleteMapping("/company/{id}")
    public ResponseEntity<Timesheet> deleteCompany(@PathVariable String id) {
        ServiceComponent dagger = DaggerServiceComponent.create();
        dagger.provideCompanyService().deleteCompany(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
