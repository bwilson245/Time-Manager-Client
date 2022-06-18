package com.tmc.dao;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.googlejavaformat.Op;
import com.tmc.exception.TimesheetNotFoundException;
import com.tmc.model.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.inject.Inject;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class TimesheetCachingDao {
    public DynamoDbDao dao;
    private final LoadingCache<String, Timesheet> cache;


    @Inject
    public TimesheetCachingDao(DynamoDbDao dao) {
        this.dao = dao;
        this.cache = CacheBuilder.newBuilder()
                .expireAfterWrite(5, TimeUnit.DAYS)
                .maximumSize(10000)
                .build(CacheLoader.from(dao::getTimesheet));
    }

    public Timesheet getTimesheet(String id) {
        return cache.getUnchecked(id);
    }

    public List<Timesheet> getTimesheets(List<String> ids) {
        List<String> notCached = new ArrayList<>();
        List<Timesheet> cached = new ArrayList<>();
        for (String s : ids) {
            if (cache.getIfPresent(s) == null) {
                notCached.add(s);
            } else {
                cached.add(cache.getUnchecked(s));
            }
        }
        cached.addAll(dao.getTimesheets(notCached));
        return cached;
    }

    public void createTimesheet(Timesheet timesheet) {
        String id = UUID.randomUUID().toString();
        while (cache.getIfPresent(id) != null) {
            id = UUID.randomUUID().toString();
        }
        timesheet.setId(id);
        cache.put(timesheet.getId(), timesheet);
        dao.saveTimesheet(timesheet);
    }

    public void deleteTimesheet(String id) {
        dao.deleteTimesheet(cache.getUnchecked(id));
        cache.invalidate(id);
    }

    //        List<EmployeeInstance> employeeInstances = new ArrayList<>();
    //        employeeInstances.add(new EmployeeInstance("23456", "Ben Wilson", 8.0, 0.0, 0.0, 3.0));
    //        employeeInstances.add(new EmployeeInstance("6784756", "Trevor Hunt", 3.0, 5.0, 0.0, 3.0));
    //        employeeInstances.add(new EmployeeInstance("634563", "Mike Slack", 8.0, 4.0, 9.5, 3.0));
    //        Location location = Location.builder()
    //                .address1("938 Urbana Rd")
    //                .city("El Dorado")
    //                .state("Arkansas")
    //                .zip("71730")
    //                .build();
    //        Customer customer = Customer.builder()
    //                .id("qw32345")
    //                .name("Pilgrims Pride")
    //                .location(location)
    //                .build();
    //        Timesheet timesheet = Timesheet.builder()
    //                .id("12345")
    //                .date(new Date().getTime())
    //                .department("sewer")
    //                .description("This is a test description. did some bullshit. some more shit. some more shit. some more shit. some more shit. some more shit. some more shit. some more shit. some more shit. some more shit. some more shit. some more shit. some more shit. some more shit.")
    //                .isComplete(false)
    //                .type(TypeEnum.INSTALLATION)
    //                .workOrderNumber("5908739475")
    //                .customer(customer)
    //                .employeeInstances(employeeInstances)
    //                .build();
    //        return timesheet;
}
