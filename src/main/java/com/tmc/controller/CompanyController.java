package com.tmc.controller;

import com.tmc.dependency.DaggerServiceComponent;
import com.tmc.dependency.ServiceComponent;
import com.tmc.model.Company;
import com.tmc.model.Timesheet;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class CompanyController {

    @GetMapping("/company")
    public ResponseEntity<Company> getCompany(@RequestParam String id) {
        ServiceComponent dagger = DaggerServiceComponent.create();
        return new ResponseEntity<>(dagger.provideCompanyService().get(id), HttpStatus.OK);
    }

    @GetMapping("/companies")
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

    @DeleteMapping("/company")
    public ResponseEntity<Timesheet> deleteCompany(@RequestBody String id) {
        ServiceComponent dagger = DaggerServiceComponent.create();
        dagger.provideCompanyService().delete(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
