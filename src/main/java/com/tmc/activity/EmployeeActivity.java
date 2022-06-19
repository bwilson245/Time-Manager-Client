package com.tmc.activity;

import com.tmc.dao.EmployeeCachingDao;
import com.tmc.model.Employee;
import com.tmc.model.request.CreateEmployeeRequest;
import com.tmc.model.request.EditEmployeeRequest;

import javax.inject.Inject;
import java.util.List;

public class EmployeeActivity {
    private final EmployeeCachingDao cachingDao;

    @Inject
    public EmployeeActivity(EmployeeCachingDao cachingDao) {
        this.cachingDao = cachingDao;
    }

    public Employee getEmployee(String id) {
        return cachingDao.getEmployee(id);
    }

    public List<Employee> getEmployees(List<String> ids) {
        return cachingDao.getEmployees(ids);
    }

    public List<Employee> getEmployeesForCompany(String id, String name, String email, Boolean isActive) {
        return cachingDao.getEmployeesForCompany(id, name, email, isActive);
    }

    public Employee createEmployee(CreateEmployeeRequest request) {
        return cachingDao.createEmployee(request);
    }

    public Employee editEmployee(String id, EditEmployeeRequest request) {
        return cachingDao.editEmployee(id, request);
    }

    public Employee deactivateEmployee(String id) {
        return cachingDao.deactivateEmployee(id);
    }
}
