package com.tmc.service;

import com.tmc.model.Company;
import com.tmc.model.TypeEnum;
import com.tmc.service.dao.DynamoDbDao;
import com.tmc.service.inter.ServiceDao;
import lombok.Data;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

@Data
@Singleton
public class CompanyService implements ServiceDao<Company> {
    private DynamoDbDao dao;
    private CacheManager cacheManager;

    @Inject
    public CompanyService(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
        this.dao = cacheManager.getDao();
    }

    @Override
    public Company create(Company request) {
        Company company = new Company(request);

        cacheManager.getCompanyCache().getCache().put(company.getId(), company);
        return dao.saveCompany(company);
    }

    @Override
    public Company edit(Company request) {
        Company company = new Company(request, cacheManager.getCompanyCache().getCache().getUnchecked(request.getId()));

        cacheManager.getCompanyCache().getCache().put(company.getId(), company);
        return dao.saveCompany(company);
    }

    public Company deactivate(String id) {
        Company company = new Company(cacheManager.getCompanyCache().getCache().getUnchecked(id));
        company.setIsActive(false);
        return dao.saveCompany(company);
    }

    @Override
    public List<Company> search(TypeEnum type, String id, String name, String email, Boolean isActive) {
        return null;
    }
    @Override
    public List<Company> search(TypeEnum type, String id, String workType, String department, String orderNum, Long before, Long after, Boolean complete, Boolean validated) {
        return null;
    }
}
