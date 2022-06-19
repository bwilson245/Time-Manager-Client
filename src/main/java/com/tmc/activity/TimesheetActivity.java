package com.tmc.activity;

import com.tmc.dao.TimesheetCachingDao;
import com.tmc.model.Timesheet;
import com.tmc.model.request.CreateTimesheetRequest;
import com.tmc.model.request.EditTimesheetRequest;

import javax.inject.Inject;
import java.util.List;

public class TimesheetActivity {
    private TimesheetCachingDao cachingDao;

    @Inject
    public TimesheetActivity(TimesheetCachingDao cachingDao) {
        this.cachingDao = cachingDao;
    }

    public Timesheet getTimesheet(String id) {
        return cachingDao.getTimesheet(id);
    }

    public List<Timesheet> getTimesheetsForCompany(String id, String type, String department, String orderNum,
                                                   Long before, Long after, Boolean complete, Boolean validated) {
        return cachingDao.getTimesheetsForCompany(id, type, department, orderNum, before, after, complete, validated);
    }

    public List<Timesheet> getTimesheetsForCustomer(String id, String type, String department, String orderNum,
                                                   Long before, Long after, Boolean complete, Boolean validated) {
        return cachingDao.getTimesheetsForCustomer(id, type, department, orderNum, before, after, complete, validated);
    }

    public List<Timesheet> getTimesheetsForEmployee(String id, String type, String department, String orderNum,
                                                    Long before, Long after, Boolean complete, Boolean validated) {
        return cachingDao.getTimesheetsForEmployee(id, type, department, orderNum, before, after, complete, validated);
    }

    public Timesheet createTimesheet(CreateTimesheetRequest request) {
        return cachingDao.createTimesheet(request);
    }

    public Timesheet editTimesheet(String id, EditTimesheetRequest request) {
        return cachingDao.editTimesheet(id, request);
    }
}
