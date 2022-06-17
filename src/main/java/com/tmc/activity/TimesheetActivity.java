package com.tmc.activity;

import com.tmc.dao.TimesheetCachingDao;
import com.tmc.model.Employee;
import com.tmc.model.Timesheet;

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

    public List<Timesheet> getTimesheets(List<String> ids) {
        return cachingDao.getTimesheets(ids);
    }

    public Timesheet saveTimesheet(Timesheet timesheet) {
        cachingDao.saveTimesheet(timesheet);
        return null;
    }

    public Timesheet deleteTimesheet(String id) {
        cachingDao.deleteTimesheet(id);
        return null;
    }

}
