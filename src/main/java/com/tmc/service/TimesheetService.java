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
public class TimesheetService implements ServiceDao<Timesheet, CreateTimesheetRequest, EditTimesheetRequest> {
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
    public Timesheet create(CreateTimesheetRequest request) {

        //****** Build a Location Object *******//
        Location location = Location.builder()
                .address1(request.getLocation().getAddress1().toUpperCase())
                .address2(request.getLocation().getAddress2().toUpperCase())
                .city(request.getLocation().getCity().toUpperCase())
                .state(request.getLocation().getState().toUpperCase())
                .zip(request.getLocation().getZip())
                .build();

        //****** Retrieve the Company object *******//
        Company company = cacheManager.getCompanyCache().get(request.getCompanyId());

        //****** Save the companyId and name to each EmployeeInstance *******//
        for (EmployeeInstance instance : request.getEmployeeInstances()) {
            instance.setCompanyId(company.getId());
            instance.setName(instance.getName().toUpperCase());
        }

        //****** Build a Timesheet object *******//
        Timesheet timesheet = Timesheet.builder()
                .id(UUID.randomUUID().toString())
                .companyId(request.getCompanyId())
                .customerId("")
                .location(location)
                .customer(request.getCustomer())
                .date(request.getDate())
                .employeeInstances(request.getEmployeeInstances())
                .isComplete(request.getIsComplete())
                .workOrderNumber(request.getWorkOrderNumber())
                .department(request.getDepartment().toUpperCase())
                .description(request.getDescription())
                .workType(request.getType().toUpperCase())
                .build();

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
     * @param id - The id associated with the original timesheet.
     * @param request - The request object containing the variables to assign to the timesheet.
     * @return - returns the modified Timesheet object.
     */
    @Override
    public Timesheet edit(String id, EditTimesheetRequest request) {
        Timesheet timesheet = cacheManager.getTimesheetCache().get(id);

        //****** Store original Lists *******//
        Set<String> originalEmployeeIds = new HashSet<>(timesheet.getEmployeeIds());

        Set<String> originalEmployeeInstanceIds = new HashSet<>();
        for (EmployeeInstance instance : timesheet.getEmployeeInstances()) {
            if (instance.getId() != null) {
                originalEmployeeInstanceIds.add(instance.getId());
            }
        }

        //****** Build Location object *******//
        Location location = Location.builder()
                .address1(Optional.ofNullable(request.getLocation().getAddress1())
                        .orElse(Optional.ofNullable(timesheet.getLocation().getAddress1())
                                .orElse("")).toUpperCase())
                .address2(Optional.ofNullable(request.getLocation().getAddress2())
                        .orElse(Optional.ofNullable(timesheet.getLocation().getAddress2())
                                .orElse("")).toUpperCase())
                .city(Optional.ofNullable(request.getLocation().getCity())
                        .orElse(Optional.ofNullable(timesheet.getLocation().getCity())
                                .orElse("")).toUpperCase())
                .state(Optional.ofNullable(request.getLocation().getState())
                        .orElse(Optional.ofNullable(timesheet.getLocation().getState())
                                .orElse("")).toUpperCase())
                .zip(Optional.ofNullable(request.getLocation().getZip())
                        .orElse(Optional.ofNullable(timesheet.getLocation().getZip())
                                .orElse("")))
                .build();



        //****** Modify Customer object *******//
        Customer customer = Optional.ofNullable(request.getCustomer()).orElse(timesheet.getCustomer());
        customer.getLocation().setAddress1(Optional.ofNullable(request.getCustomer().getLocation().getAddress1())
                .orElse(Optional.ofNullable(timesheet.getCustomer().getLocation().getAddress1())
                        .orElse("")).toUpperCase());
        customer.getLocation().setAddress2(Optional.ofNullable(request.getCustomer().getLocation().getAddress2())
                .orElse(Optional.ofNullable(timesheet.getCustomer().getLocation().getAddress2())
                        .orElse("")).toUpperCase());
        customer.getLocation().setCity(Optional.ofNullable(request.getCustomer().getLocation().getCity())
                .orElse(Optional.ofNullable(timesheet.getCustomer().getLocation().getCity())
                        .orElse("")).toUpperCase());
        customer.getLocation().setState(Optional.ofNullable(request.getCustomer().getLocation().getState())
                .orElse(Optional.ofNullable(timesheet.getCustomer().getLocation().getState())
                        .orElse("")).toUpperCase());
        customer.getLocation().setZip(Optional.ofNullable(request.getCustomer().getLocation().getZip())
                .orElse(Optional.ofNullable(timesheet.getCustomer().getLocation().getZip())
                        .orElse("")).toUpperCase());

        //****** Modify Timesheet object *******//
        timesheet.setLocation(location);
        timesheet.setCustomer(Optional.ofNullable(request.getCustomer())
                .orElse(timesheet.getCustomer()));
        timesheet.setDate(Optional.ofNullable(request.getDate())
                .orElse(timesheet.getDate()));
        timesheet.setEmployeeInstances(Optional.ofNullable(request.getEmployeeInstances())
                .orElse(timesheet.getEmployeeInstances()));
        timesheet.setIsComplete(Optional.ofNullable(request.getIsComplete())
                .orElse(timesheet.getIsComplete()));
        timesheet.setWorkOrderNumber(Optional.ofNullable(request.getWorkOrderNumber())
                .orElse(timesheet.getWorkOrderNumber()));
        timesheet.setDepartment(Optional.ofNullable(request.getDepartment())
                .orElse(timesheet.getDepartment()));
        timesheet.setDescription(Optional.ofNullable(request.getDescription())
                .orElse(timesheet.getDescription()));
        timesheet.setWorkType(Optional.ofNullable(request.getWorkType())
                .orElse(timesheet.getWorkType()));
        timesheet.setIsValidated(Optional.ofNullable(request.getIsValidated())
                .orElse(timesheet.getIsValidated()));



        //****** Build a list of ids that were removed *******//
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
        if (difference.size() > 0) {
            Set<Employee> diffEmployees = new HashSet<>(cacheManager.getEmployeeCache().get(new ArrayList<>(difference)));
            for (Employee employee : diffEmployees) {
                employee.getTimesheetIds().remove(timesheet.getId());
            }
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


        //****** Build a list of customer's employeeIds and adds the new employees ids *******//
        Set<String> originalCustomerEmployeeIds = new HashSet<>(timesheet.getCustomer().getEmployeeIds());
        originalCustomerEmployeeIds.addAll(newEmployeeIds);
        customer.setEmployeeIds(new ArrayList<>(originalCustomerEmployeeIds));

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
