package com.tmc.service;

import com.tmc.model.Company;
import com.tmc.model.Location;
import com.tmc.model.TypeEnum;
import com.tmc.model.request.CreateCompanyRequest;
import com.tmc.model.request.EditCompanyRequest;
import com.tmc.service.dao.DynamoDbDao;
import com.tmc.service.inter.ServiceDao;
import lombok.Data;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Data
@Singleton
public class CompanyService implements ServiceDao<Company, CreateCompanyRequest, EditCompanyRequest> {
    private DynamoDbDao dao;
    private CacheManager cacheManager;

    @Inject
    public CompanyService(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
        this.dao = cacheManager.getDao();
    }

    @Override
    public Company create(CreateCompanyRequest request) {
        Company company = Company.builder()
                .id(UUID.randomUUID().toString())
                .name(request.getName().toUpperCase())
                .location(Location.builder()
                        .address1(request.getAddress1().toUpperCase())
                        .address2(request.getAddress2().toUpperCase())
                        .city(request.getCity().toUpperCase())
                        .state(request.getState().toUpperCase())
                        .zip(request.getZip())
                        .build())
                .build();

        cacheManager.getCompanyCache().getCache().put(company.getId(), company);
        return dao.saveCompany(company);
    }

    @Override
    public Company edit(String id, EditCompanyRequest request) {
        Company company = cacheManager.getCompanyCache().getCache().getUnchecked(id);
        Location location = company.getLocation();

        location.setAddress1(Optional.ofNullable(request.getAddress1()).orElse(company.getLocation().getAddress1()).toUpperCase());
        location.setAddress2(Optional.ofNullable(request.getAddress2()).orElse(company.getLocation().getAddress2()).toUpperCase());
        location.setCity(Optional.ofNullable(request.getCity()).orElse(company.getLocation().getCity()).toUpperCase());
        location.setState(Optional.ofNullable(request.getState()).orElse(company.getLocation().getState()).toUpperCase());
        location.setZip(Optional.ofNullable(request.getZip()).orElse(company.getLocation().getZip()));

        company.setName(Optional.ofNullable(request.getName()).orElse(company.getName()).toUpperCase());
        company.setLocation(location);

        cacheManager.getCompanyCache().getCache().put(company.getId(), company);
        return dao.saveCompany(company);
    }

    public Company deactivate(String id) {
        Company company = cacheManager.getCompanyCache().getCache().getUnchecked(id);
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
