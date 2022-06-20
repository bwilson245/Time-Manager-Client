package com.tmc.service;

import com.tmc.model.*;
import com.tmc.model.instance.EmployeeInstance;
import com.tmc.model.request.CreateTimesheetRequest;
import com.tmc.model.request.EditTimesheetRequest;
import com.tmc.service.dao.DynamoDbDao;
import com.tmc.service.inter.*;
import lombok.Data;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;

@Data
@Singleton
public class TimesheetService implements ServiceDao<Timesheet> {
    private DynamoDbDao dao;
    private CacheManager cacheManager;

    @Inject
    public TimesheetService(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
        this.dao = cacheManager.getDao();
    }


    @Override
    public List<Timesheet> search(TypeEnum type, String id, String workType, String department, String orderNum, Long before, Long after, Boolean complete, Boolean validated) {
        Object obj;
        String table;
        if (type == null) {
            type = TypeEnum.COMPANY;
        }
        switch (type) {
            case CUSTOMER:
                obj = cacheManager.getCustomerCache().get(id);
                table = "customerId-type-index";
                break;
            case EMPLOYEE:
                obj = cacheManager.getEmployeeCache().get(id);
                table = "companyId-type-index";
                break;
            default:
                obj = cacheManager.getCompanyCache().get(id);
                table = "companyId-type-index";
        }
        return dao.getTimesheets(table, obj, workType, department, orderNum, before, after, complete, validated);
    }


    /**
     * Creates a new Timesheet object. Only requires basic information about the job. isValidated is set to false
     * because it is considered an "unapproved" Timesheet object and therefore should not be added to the employees or
     * customer's list of timesheetIds. An administrator is required to "validate" the timesheet.
     * Retrieved Lists are converted to Set for the purpose of avoiding duplication of Ids.
     * @param request - The request object containing all the information required to build a new Timesheet.
     * @return returns the newly created Timesheet with isValidated = false.
     */

    @Override
    public Timesheet create(Timesheet request) {

        //****** Builds a new Timesheet object from the request *******//
        Timesheet timesheet = new Timesheet(request);

        //****** Retrieve the Company object *******//
        Company company = cacheManager.getCompanyCache().get(request.getCompanyId());

        //****** Save the companyId and name to each EmployeeInstance *******//
        for (EmployeeInstance instance : request.getEmployeeInstances()) {
            instance.setCompanyId(company.getId());
            instance.setName(instance.getName().toUpperCase());
        }

        //****** Add the timesheetId to the company's timesheetIds *******//
        Set<String> timesheetIds = new HashSet<>(company.getTimesheetIds());
        timesheetIds.add(timesheet.getId());
        company.setTimesheetIds(new ArrayList<>(timesheetIds));

        //****** Save final values *******//
        dao.saveCompany(company);
        cacheManager.getTimesheetCache().getCache().put(timesheet.getId(), timesheet);
        return dao.saveTimesheet(timesheet);
    }


    /**
     * Edits an existing timesheet.
     * Retrieved Lists are converted to Set for the purpose of avoiding duplication of Ids.
     * In the event that employees are removed from a timesheet, the removed employees will have this timesheet id
     * removed from their list of timesheetIds.
     * @param request - The request object containing the variables to assign to the timesheet.
     * @return - returns the modified Timesheet object.
     */
    @Override
    public Timesheet edit(Timesheet request) {

        //****** Define original and new timesheet and customer *******//
        Timesheet originalTimesheet = cacheManager.getTimesheetCache().get(request.getId());
        Timesheet timesheet = new Timesheet(request, originalTimesheet);
        Customer originalCustomer = originalTimesheet.getCustomer();
        Customer customer = timesheet.getCustomer();


        //****** Build a list of ids that were removed *******//
        Set<String> originalEmployeeInstanceIds = new HashSet<>();
        for (EmployeeInstance instance : originalTimesheet.getEmployeeInstances()) {
            if (instance.getId() != null) {
                originalEmployeeInstanceIds.add(instance.getId());
            }
        }
        Set<String> newEmployeeIds = new HashSet<>();
        for (EmployeeInstance instance : timesheet.getEmployeeInstances()) {
            newEmployeeIds.add(instance.getId());
        }
        Set<String> difference = new HashSet<>();
        for (String originId : originalEmployeeInstanceIds) {
            if (!newEmployeeIds.contains(originId)) {
                difference.add(originId);
            }
        }

        //****** Remove this timesheet from removed employees timesheetIds *******//
        //****** and remove each employee id from the customer employeeIds *******//
        if (difference.size() > 0) {
            Set<Employee> diffEmployees = new HashSet<>(cacheManager.getEmployeeCache().get(new ArrayList<>(difference)));
            Set<String> customerEmployeeIds = new HashSet<>(customer.getEmployeeIds());
            for (Employee employee : diffEmployees) {
                employee.getTimesheetIds().remove(timesheet.getId());
                customerEmployeeIds.remove(employee.getId());
            }
            customer.setEmployeeIds(new ArrayList<>(customerEmployeeIds));
            dao.batchSaveEmployees(new ArrayList<>(diffEmployees));
        }


        //****** Add this timesheetId and customerId to each employee  *******//
        Set<Employee> employees = new HashSet<>(cacheManager.getEmployeeCache().get(new ArrayList<>(newEmployeeIds)));
        for (Employee employee : employees) {
            Set<String> timesheetIds = new HashSet<>(employee.getTimesheetIds());
            Set<String> customerIds = new HashSet<>(employee.getCustomerIds());
            timesheetIds.add(timesheet.getId());
            customerIds.add(timesheet.getCustomer().getId());
            employee.setTimesheetIds(new ArrayList<>(timesheetIds));
            employee.setCustomerIds(new ArrayList<>(customerIds));
        }


        //****** Build a list of customer's timesheetIds and adds the timesheetId *******//
        Set<String> timesheetIds = new HashSet<>(customer.getTimesheetIds());
        timesheetIds.add(timesheet.getId());
        customer.setTimesheetIds(new ArrayList<>(timesheetIds));


        //****** Save final values *******//
        timesheet.setCustomerId(customer.getId());
        dao.saveCustomer(customer);
        dao.batchSaveEmployees(new ArrayList<>(employees));
        return dao.saveTimesheet(timesheet);
    }



    @Override
    public Timesheet deactivate(String id) {
        return null;
    }


    @Override
    public List<Timesheet> search(TypeEnum type, String id, String name, String email, Boolean isActive) {
        return null;
    }
}
