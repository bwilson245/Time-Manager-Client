package com.tmc.dao;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.tmc.model.Timesheet;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
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

    public void saveTimesheet(Timesheet timesheet) {
        cache.put(timesheet.getId(), timesheet);
        dao.saveTimesheet(timesheet);
    }

    public void deleteTimesheet(String id) {
        dao.deleteTimesheet(cache.getUnchecked(id));
        cache.invalidate(id);
    }
}
