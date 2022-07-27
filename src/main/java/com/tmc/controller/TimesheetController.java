package com.tmc.controller;

import com.amazonaws.services.dynamodbv2.datamodeling.QueryResultPage;
import com.tmc.dependency.DaggerServiceComponent;
import com.tmc.dependency.ServiceComponent;
import com.tmc.model.Timesheet;
import com.tmc.model.request.SearchTimesheetRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class TimesheetController {
    @GetMapping("/timesheet/{id}")
    public ResponseEntity<Timesheet> getTimesheet(@RequestParam String id) {
        ServiceComponent dagger = DaggerServiceComponent.create();
        return new ResponseEntity<>(dagger.provideTimesheetService().get(id), HttpStatus.OK);
    }

    @GetMapping("/timesheets")
    public ResponseEntity<List<Timesheet>> getTimesheets(@RequestBody List<String> ids) {
        ServiceComponent dagger = DaggerServiceComponent.create();
        return new ResponseEntity<>(dagger.provideTimesheetService().get(ids), HttpStatus.OK);
    }

    @GetMapping("/timesheets/search")
    public ResponseEntity<QueryResultPage<Timesheet>> getTimesheetsSearch(@RequestBody SearchTimesheetRequest request) {
        ServiceComponent dagger = DaggerServiceComponent.create();
        return new ResponseEntity<>(dagger.provideTimesheetService().search(request), HttpStatus.OK);
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

    @DeleteMapping("/timesheet")
    public ResponseEntity<Timesheet> deleteTimesheet(@RequestBody String id) {
        ServiceComponent dagger = DaggerServiceComponent.create();
        dagger.provideTimesheetService().deleteTimesheet(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
