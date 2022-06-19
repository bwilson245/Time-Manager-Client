package com.tmc.dao;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.tmc.model.Company;
import com.tmc.model.Location;
import com.tmc.model.request.CreateCompanyRequest;
import com.tmc.model.request.EditCompanyRequest;
import org.checkerframework.checker.units.qual.C;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

public class CompanyCachingDao {
    private final DynamoDbDao dao;
    private final LoadingCache<String, Company> cache;

    @Inject
    public CompanyCachingDao(DynamoDbDao dao) {
        this.dao = dao;
        this.cache = CacheBuilder.newBuilder()
                .build(CacheLoader.from(dao::getCompany));
    }

    public Company getCompany(String id) {
        return cache.getUnchecked(id);
    }

    public Company createCompany(CreateCompanyRequest request) {
        Company company = Company.builder()
                .id(UUID.randomUUID().toString())
                .name(request.getName())
                .location(Location.builder()
                        .address1(request.getAddress1())
                        .address2(request.getAddress2())
                        .city(request.getCity())
                        .state(request.getState())
                        .zip(request.getZip())
                        .build())
                .employeeIds(new ArrayList<>())
                .customerIds(new ArrayList<>())
                .timesheetIds(new ArrayList<>())
                .isActive(true)
                .build();

        cache.put(company.getId(), company);
        return dao.saveCompany(company);
    }

    public Company editCompany(String id, EditCompanyRequest request) {
        Company company = cache.getUnchecked(id);
        Location location = company.getLocation();

        location.setAddress1(Optional.ofNullable(request.getAddress1()).orElse(company.getLocation().getAddress1()));
        location.setAddress2(Optional.ofNullable(request.getAddress2()).orElse(company.getLocation().getAddress2()));
        location.setCity(Optional.ofNullable(request.getCity()).orElse(company.getLocation().getCity()));
        location.setState(Optional.ofNullable(request.getState()).orElse(company.getLocation().getState()));
        location.setZip(Optional.ofNullable(request.getZip()).orElse(company.getLocation().getZip()));

        company.setName(Optional.ofNullable(request.getName()).orElse(company.getName()));
        company.setLocation(location);

        cache.put(company.getId(), company);
        return dao.saveCompany(company);
    }

    public Company deactivateCompany(String id) {
        Company company = cache.getUnchecked(id);
        company.setIsActive(false);
        return dao.saveCompany(company);
    }
}
