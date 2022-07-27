package com.tmc.controller;

import com.amazonaws.services.dynamodbv2.datamodeling.QueryResultPage;
import com.tmc.dependency.DaggerServiceComponent;
import com.tmc.dependency.ServiceComponent;
import com.tmc.model.Customer;
import com.tmc.model.Timesheet;
import com.tmc.model.request.SearchCustomerRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class CustomerController {
    @GetMapping("/customer")
    public ResponseEntity<Customer> getCustomer(@RequestBody String id) {
        ServiceComponent dagger = DaggerServiceComponent.create();
        return new ResponseEntity<>(dagger.provideCustomerService().get(id), HttpStatus.OK);
    }

    @GetMapping("/customers")
    public ResponseEntity<List<Customer>> getCustomers(@RequestBody List<String> ids) {
        ServiceComponent dagger = DaggerServiceComponent.create();
        return new ResponseEntity<>(dagger.provideCustomerService().get(ids), HttpStatus.OK);
    }

    @GetMapping("/customers/search")
    public ResponseEntity<QueryResultPage<Customer>> searchCustomers(@RequestBody SearchCustomerRequest request) {
        ServiceComponent dagger = DaggerServiceComponent.create();
        return new ResponseEntity<>(dagger.provideCustomerService().search(request), HttpStatus.OK);
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

    @DeleteMapping("/customer")
    public ResponseEntity<Timesheet> deleteCustomer(@RequestBody String id) {
        ServiceComponent dagger = DaggerServiceComponent.create();
        dagger.provideCustomerService().deleteCustomer(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
