package com.tmc.activity;

import com.tmc.dao.EmployeeCachingDao;
import com.tmc.model.Employee;

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

    public Employee saveEmployee(Employee employee) {
        cachingDao.saveEmployee(employee);
        return null;
    }

    public Employee editEmployee(String id, String name, String email, String password) {
        cachingDao.editEmployee(id, name, email, password);
        return null;
    }

    public Employee deleteEmployee(String id) {
        cachingDao.deleteEmployee(id);
        return null;
    }
}
