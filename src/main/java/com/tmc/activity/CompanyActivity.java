package com.tmc.activity;

import com.tmc.dao.CompanyCachingDao;
import com.tmc.model.Company;
import com.tmc.model.request.CreateCompanyRequest;
import com.tmc.model.request.EditCompanyRequest;

import javax.inject.Inject;

public class CompanyActivity {
    private final CompanyCachingDao cachingDao;

    @Inject
    public CompanyActivity(CompanyCachingDao cachingDao) {
        this.cachingDao = cachingDao;
    }

    public Company getCompany(String id) {
        return cachingDao.getCompany(id);
    }

    public Company createCompany(CreateCompanyRequest request) {
        return cachingDao.createCompany(request);
    }

    public Company editCompany(String id, EditCompanyRequest request) {
        return cachingDao.editCompany(id, request);
    }

    public Company deactivateCompany(String id) {
        return cachingDao.deactivateCompany(id);
    }
}
