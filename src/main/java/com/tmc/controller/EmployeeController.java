package com.tmc.controller;

import com.amazonaws.services.dynamodbv2.datamodeling.QueryResultPage;
import com.tmc.dependency.DaggerServiceComponent;
import com.tmc.dependency.ServiceComponent;
import com.tmc.model.Employee;
import com.tmc.model.request.SearchEmployeeRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class EmployeeController {
    @GetMapping("/employee")
    public ResponseEntity<Employee> getEmployee(@RequestBody String id) {
        ServiceComponent dagger = DaggerServiceComponent.create();
        return new ResponseEntity<>(dagger.provideEmployeeService().get(id), HttpStatus.OK);
    }

    @GetMapping("/employees")
    public ResponseEntity<List<Employee>> getEmployees(@RequestBody List<String> ids) {
        ServiceComponent dagger = DaggerServiceComponent.create();
        return new ResponseEntity<>(dagger.provideEmployeeService().get(ids), HttpStatus.OK);
    }

    @GetMapping("/employees/search")
    public ResponseEntity<QueryResultPage<Employee>> getEmployeesSearch(@RequestBody SearchEmployeeRequest request) {
        ServiceComponent dagger = DaggerServiceComponent.create();
        return new ResponseEntity<>(dagger.provideEmployeeService().search(request), HttpStatus.OK);
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

    @DeleteMapping("/employee")
    public ResponseEntity<Employee> deleteEmployee(@RequestBody String id) {
        ServiceComponent dagger = DaggerServiceComponent.create();
        dagger.provideEmployeeService().deleteEmployee(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
