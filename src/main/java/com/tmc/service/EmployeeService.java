package com.tmc.service;

import com.tmc.model.Company;
import com.tmc.model.Employee;
import com.tmc.model.TypeEnum;
import com.tmc.model.request.CreateEmployeeRequest;
import com.tmc.model.request.EditEmployeeRequest;
import com.tmc.service.dao.DynamoDbDao;
import com.tmc.service.inter.*;
import lombok.Data;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;

@Data
@Singleton
public class EmployeeService implements ServiceDao<Employee> {

    private DynamoDbDao dao;
    private CacheManager cacheManager;

    @Inject
    public EmployeeService(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
        this.dao = cacheManager.getDao();
    }
    /**
     * Gets the employee associated with the id.
     * @param id - the id of the employee.
     * @return - returns an Employee object.
     */

    @Override
    public List<Employee> search(TypeEnum type, String id, String name, String email, Boolean isActive) {
        Object obj;
        String table;
        if (type == null) {
            type = TypeEnum.COMPANY;
        }
        switch (type) {
            case CUSTOMER:
                obj = cacheManager.getCustomerCache().get(id);
                table = "customerId-index";
                break;
            case TIMESHEET:
                obj = cacheManager.getTimesheetCache().get(id);
                table = "";
                break;
            case COMPANY:
                obj = cacheManager.getCompanyCache().get(id);
                table = "companyId-index";
                break;
            default:
                return null;
        }
        return dao.getEmployees(table, obj, name, email, isActive);
    }



    /**
     * Creates a new employee and adds it to the company.
     * @param request - the values to assign to the new Employee object.
     * @return - the new Employee object.
     */
    @Override
    public Employee create(Employee request) {
        Company company = cacheManager.getCompanyCache().get(request.getCompanyId());

        Employee employee = new Employee(request);

        Set<String> employeeIds = new HashSet<>(company.getEmployeeIds());
        employeeIds.add(employee.getId());
        company.setEmployeeIds(new ArrayList<>(employeeIds));

        cacheManager.getEmployeeCache().getCache().put(employee.getId(), employee);
        dao.saveCompany(company);
        return dao.saveEmployee(employee);
    }


    /**
     * Modifies an Employee object.
     * @param request - (optional) - the values to assign to the employee. The values are optional, however the request
     *                object itself is required to pass into the method.
     * @return - returns the modified employee.
     */
    @Override
    public Employee edit(Employee request) {
        Employee employee = new Employee(cacheManager.getEmployeeCache().get(request.getId()));

        cacheManager.getEmployeeCache().getCache().put(employee.getId(), employee);
        return dao.saveEmployee(employee);
    }

    /**
     *
     * @param id
     * @return
     */
    @Override
    public Employee deactivate(String id) {
        Employee employee = new Employee(cacheManager.getEmployeeCache().get(id));
        employee.setIsActive(false);
        return dao.saveEmployee(employee);
    }


    @Override
    public List<Employee> search(TypeEnum type, String id, String workType, String department, String orderNum, Long before, Long after, Boolean complete, Boolean validated) {
        return null;
    }
}
