package com.tmc.service;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.tmc.model.Company;
import com.tmc.model.Customer;
import com.tmc.model.Location;
import com.tmc.model.TypeEnum;
import com.tmc.model.request.CreateCustomerRequest;
import com.tmc.model.request.EditCustomerRequest;
import com.tmc.service.dao.DynamoDbDao;

import com.tmc.service.inter.ServiceDao;
import lombok.Data;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;

@Data
@Singleton
public class CustomerService implements ServiceDao<Customer> {
    private DynamoDbDao dao;
    private CacheManager cacheManager;

    @Inject
    public CustomerService(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
        this.dao = cacheManager.getDao();
    }


    @Override
    public Customer create(Customer request) {
        Company company = cacheManager.getCompanyCache().get(request.getCompanyId());

        Customer customer = new Customer(request);

        Set<String> customerIds = new HashSet<>(company.getCustomerIds());
        customerIds.add(customer.getId());
        company.setCustomerIds(new ArrayList<>(customerIds));

        cacheManager.getCustomerCache().getCache().put(customer.getId(), customer);
        dao.saveCompany(company);
        return dao.saveCustomer(customer);
    }

    @Override
    public Customer edit(Customer request) {
        Customer customer = new Customer(request, cacheManager.getCustomerCache().getCache().getUnchecked(request.getId()));

        cacheManager.getCustomerCache().getCache().put(customer.getId(), customer);
        return dao.saveCustomer(customer);
    }

    @Override
    public Customer deactivate(String id) {
        Customer customer = new Customer(cacheManager.getCustomerCache().getCache().getUnchecked(id));
        customer.setIsActive(false);
        return dao.saveCustomer(customer);
    }

    @Override
    public List<Customer> search(TypeEnum type, String id, String name, String email, Boolean isActive) {
        return null;
    }
    @Override
    public List<Customer> search(TypeEnum type, String id, String workType, String department, String orderNum, Long before, Long after, Boolean complete, Boolean validated) {
        return null;
    }
}
