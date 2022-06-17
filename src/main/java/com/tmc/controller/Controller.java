package com.tmc.controller;

import com.tmc.dependency.DaggerServiceComponent;
import com.tmc.dependency.ServiceComponent;
import com.tmc.model.Customer;
import com.tmc.model.Employee;
import com.tmc.model.Location;
import com.tmc.model.Timesheet;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class Controller {

    /*
     ********************** TIMESHEET ****************
     */
    @GetMapping("/timesheets/{id}")
    public ResponseEntity<Timesheet> getTimesheet(@PathVariable String id) {
        ServiceComponent dagger = DaggerServiceComponent.create();
        return new ResponseEntity<>(dagger.provideTimesheetActivity().getTimesheet(id), HttpStatus.OK);
    }

    @GetMapping("/timesheets")
    public ResponseEntity<List<Timesheet>> getTimesheets(@RequestBody List<String> ids) {
        ServiceComponent dagger = DaggerServiceComponent.create();
        return new ResponseEntity<>(dagger.provideTimesheetActivity().getTimesheets(ids), HttpStatus.OK);
    }

    @PostMapping("/timesheets")
    public ResponseEntity<Timesheet> saveTimesheet(@RequestBody Timesheet timesheet) {
        ServiceComponent dagger = DaggerServiceComponent.create();
        return new ResponseEntity<>(dagger.provideTimesheetActivity().saveTimesheet(timesheet), HttpStatus.OK);
    }

    @DeleteMapping("/timesheets/{id}")
    public ResponseEntity<Timesheet> deleteTimesheet(@PathVariable String id) {
        ServiceComponent dagger = DaggerServiceComponent.create();
        return new ResponseEntity<>(dagger.provideTimesheetActivity().deleteTimesheet(id), HttpStatus.OK);
    }

    /*
     ********************** EMPLOYEE ****************
     */
    @GetMapping("/employees/{id}")
    public ResponseEntity<Employee> getEmployee(@PathVariable String id) {
        ServiceComponent dagger = DaggerServiceComponent.create();
        return new ResponseEntity<>(dagger.provideEmployeeActivity().getEmployee(id), HttpStatus.OK);
    }

    @GetMapping("/employees")
    public ResponseEntity<List<Employee>> getEmployees(@RequestBody List<String> ids) {
        ServiceComponent dagger = DaggerServiceComponent.create();
        return new ResponseEntity<>(dagger.provideEmployeeActivity().getEmployees(ids), HttpStatus.OK);
    }

    @PostMapping("/employees")
    public ResponseEntity<Employee> saveEmployee(@RequestBody Employee employee) {
        ServiceComponent dagger = DaggerServiceComponent.create();
        return new ResponseEntity<>(dagger.provideEmployeeActivity().saveEmployee(employee), HttpStatus.OK);
    }

    @PutMapping("/employees/{id}")
    public ResponseEntity<Employee> editEmployee(@PathVariable String id,
                                                 @RequestParam String name,
                                                 @RequestParam String email,
                                                 @RequestParam String password) {
        ServiceComponent dagger = DaggerServiceComponent.create();
        return new ResponseEntity<>(dagger.provideEmployeeActivity()
                .editEmployee(id, name, email, password), HttpStatus.OK);
    }


    @DeleteMapping("/employees/{id}")
    public ResponseEntity<Employee> deleteEmployee(@PathVariable String id) {
        ServiceComponent dagger = DaggerServiceComponent.create();
        return new ResponseEntity<>(dagger.provideEmployeeActivity().deleteEmployee(id), HttpStatus.OK);
    }

    /*
    ********************** CUSTOMER ****************
     */
    @GetMapping("/customers/{id}")
    public ResponseEntity<Customer> getCustomer(@PathVariable String id) {
        ServiceComponent dagger = DaggerServiceComponent.create();
        return new ResponseEntity<>(dagger.provideCustomerActivity().getCustomer(id), HttpStatus.OK);
    }

    @GetMapping("/customers")
    public ResponseEntity<List<Customer>> getCustomers(@RequestBody List<String> ids) {
        ServiceComponent dagger = DaggerServiceComponent.create();
        return new ResponseEntity<>(dagger.provideCustomerActivity().getCustomers(ids), HttpStatus.OK);
    }

    @PostMapping("/customers")
    public ResponseEntity<Customer> saveCustomer(@RequestBody Customer customer) {
        ServiceComponent dagger = DaggerServiceComponent.create();
        return new ResponseEntity<>(dagger.provideCustomerActivity().saveCustomer(customer), HttpStatus.OK);
    }

    @PutMapping("/customers/{id}")
    public ResponseEntity<Customer> editCustomer(@PathVariable String id,
                                                 @RequestParam String name,
                                                 @RequestBody Location location) {
        ServiceComponent dagger = DaggerServiceComponent.create();
        return new ResponseEntity<>(dagger.provideCustomerActivity()
                .editCustomer(id, name, location), HttpStatus.OK);
    }

    @DeleteMapping("/customers/{id}")
    public ResponseEntity<Customer> deleteCustomer(@PathVariable String id) {
        ServiceComponent dagger = DaggerServiceComponent.create();
        return new ResponseEntity<>(dagger.provideCustomerActivity().deleteCustomer(id), HttpStatus.OK);
    }
}
