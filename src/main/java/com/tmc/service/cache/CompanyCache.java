package com.tmc.service.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.tmc.model.Company;
import com.tmc.service.dao.DynamoDbDao;
import com.tmc.service.inter.CachingDao;
import lombok.Data;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Data
@Singleton
public class CompanyCache implements CachingDao<Company> {
    private LoadingCache<String, Company> cache;
    private DynamoDbDao dao;

    @Inject
    public CompanyCache(DynamoDbDao dao) {
        this.dao = dao;
        this.cache = CacheBuilder.newBuilder()
                .expireAfterWrite(1, TimeUnit.DAYS)
                .maximumSize(1000)
                .build(CacheLoader.from(this.dao::getCompany));
    }
    @Override
    public Company get(String id) {
        return cache.getUnchecked(id);
    }
    @Override
    public List<Company> get(List<String> ids) {
        return null;
    }
}
