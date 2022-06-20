package com.tmc.service.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.tmc.model.Company;
import com.tmc.model.Timesheet;
import com.tmc.service.dao.DynamoDbDao;
import com.tmc.service.inter.CachingDao;
import lombok.Data;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Data
@Singleton
public class TimesheetCache implements CachingDao<Timesheet> {
    private LoadingCache<String, Timesheet> cache;
    private DynamoDbDao dao;

    @Inject
    public TimesheetCache(DynamoDbDao dao) {
        this.dao = dao;
        this.cache = CacheBuilder.newBuilder()
                .expireAfterWrite(1, TimeUnit.DAYS)
                .maximumSize(1000)
                .build(CacheLoader.from(this.dao::getTimesheet));
    }
    @Override
    public Timesheet get(String id) {
        return cache.getUnchecked(id);
    }
    @Override
    public List<Timesheet> get(List<String> ids) {
        List<Timesheet> cached = new ArrayList<>();
        List<String> notCached = new ArrayList<>();
        for (String id : ids) {
            Timesheet timesheet = cache.getIfPresent(id);
            if (timesheet == null) {
                notCached.add(id);
            } else {
                cached.add(timesheet);
            }
        }
        cached.addAll(dao.getTimesheets(notCached));
        for (Timesheet timesheet : cached) {
            cache.put(timesheet.getId(), timesheet);
        }
        return cached;
    }
}
