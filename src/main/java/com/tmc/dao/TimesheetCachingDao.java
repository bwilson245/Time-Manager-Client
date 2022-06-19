package com.tmc.dao;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.tmc.model.*;
import com.tmc.model.instance.EmployeeInstance;
import com.tmc.model.request.CreateTimesheetRequest;
import com.tmc.model.request.EditTimesheetRequest;

import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class TimesheetCachingDao {
    private final DynamoDbDao dao;
    private final LoadingCache<String, Timesheet> cache;
    private final CompanyCachingDao companyCachingDao;
    private final CustomerCachingDao customerCachingDao;
    private final EmployeeCachingDao employeeCachingDao;

    @Inject
    public TimesheetCachingDao(DynamoDbDao dao, CompanyCachingDao companyCachingDao, CustomerCachingDao customerCachingDao, EmployeeCachingDao employeeCachingDao) {
        this.dao = dao;
        this.companyCachingDao = companyCachingDao;
        this.customerCachingDao = customerCachingDao;
        this.employeeCachingDao = employeeCachingDao;
        this.cache = CacheBuilder.newBuilder()
                .expireAfterWrite(5, TimeUnit.DAYS)
                .maximumSize(10000)
                .build(CacheLoader.from(dao::getTimesheet));

    }

    public Timesheet getTimesheet(String id) {
        return cache.getUnchecked(id);
    }

    public List<Timesheet> getTimesheetsForCompany(String id, String type, String department, String orderNum,
                                         Long before, Long after, Boolean complete, Boolean validated) {
        Company company = companyCachingDao.getCompany(id);
        return dao.getTimesheets("companyId-index", company, type, department, orderNum, before, after, complete, validated);
    }

    public List<Timesheet> getTimesheetsForCustomer(String id, String type, String department, String orderNum,
                                                   Long before, Long after, Boolean complete, Boolean validated) {
        Customer customer = customerCachingDao.getCustomer(id);
        return dao.getTimesheets("customerId-index", customer, type, department, orderNum, before, after, complete, validated);
    }

    public List<Timesheet> getTimesheetsForEmployee(String id, String type, String department, String orderNum,
                                                    Long before, Long after, Boolean complete, Boolean validated) {
        Employee employee = employeeCachingDao.getEmployee(id);
        return dao.getTimesheets("companyId-index", employee, type, department, orderNum, before, after, complete, validated);
    }

    /**
     * Creates a new Timesheet object. Only requires basic information about the job. isValidated is set to false
     * because it is considered an "unapproved" Timesheet object and therefore should not be added to the employees or
     * customer's list of timesheetIds. An administrator is required to edit the timesheet and during the process
     * "validate" the Timesheet.
     * Retrieved Lists are converted to Set for the purpose of avoiding duplication of Ids.
     * @param request - The request object containing all the information required to build a new Timesheet.
     * @return returns the newly created Timesheet with isValidated = false.
     */
    public Timesheet createTimesheet(CreateTimesheetRequest request) {

        Company company = companyCachingDao.getCompany(request.getCompanyId());

        Timesheet timesheet = Timesheet.builder()
                .id(UUID.randomUUID().toString())
                .companyId(request.getCompanyId())
                .customerId("")
                .location(request.getLocation())
                .customer(request.getCustomer())
                .date(request.getDate())
                .employeeInstances(request.getEmployeeInstances())
                .isComplete(request.getIsComplete())
                .workOrderNumber(request.getWorkOrderNumber())
                .department(request.getDepartment())
                .description(request.getDescription())
                .type(request.getType().toUpperCase())
                .isValidated(false)
                .build();

        Set<String> timesheetIds = new HashSet<>(company.getTimesheetIds());
        timesheetIds.add(timesheet.getId());
        company.setTimesheetIds(new ArrayList<>(timesheetIds));

        dao.saveCompany(company);
        cache.put(timesheet.getId(), timesheet);
        return dao.saveTimesheet(timesheet);
    }

    /**
     * Edits an existing timesheet. Since this operation is performed by an administrator, isValidated is set to true.
     * Retrieved Lists are converted to Set for the purpose of avoiding duplication of Ids.
     * In the event that employees are removed from a timesheet, the removed employees will have this timesheet id
     * removed from their list of timesheetIds.
     * @param id - The id associated with the original timesheet.
     * @param request - The request object containing the variables to assign to the timesheet.
     * @return - returns the modified Timesheet object.
     */
    public Timesheet editTimesheet(String id, EditTimesheetRequest request) {
        Timesheet timesheet = cache.getUnchecked(id);

        List<String> originalEmployeeInstanceIds = new ArrayList<>();
        for (EmployeeInstance instance : timesheet.getEmployeeInstances()) {
            originalEmployeeInstanceIds.add(instance.getId());
        }

        timesheet.setLocation(request.getLocation());
        timesheet.setCustomer(request.getCustomer());
        timesheet.setDate(request.getDate());
        timesheet.setEmployeeInstances(request.getEmployeeInstances());
        timesheet.setIsComplete(request.getIsComplete());
        timesheet.setWorkOrderNumber(request.getWorkOrderNumber());
        timesheet.setDepartment(request.getDepartment());
        timesheet.setDescription(request.getDescription());
        timesheet.setType(request.getType().toUpperCase());
        timesheet.setIsValidated(true);

        Set<String> newEmployeeIds = new HashSet<>();
        for (EmployeeInstance instance : request.getEmployeeInstances()) {
            newEmployeeIds.add(instance.getId());
        }

        List<String> newDiffIds = new ArrayList<>();
        for (String originId : originalEmployeeInstanceIds) {
            if (!newEmployeeIds.contains(originId)) {
                newDiffIds.add(originId);
            }
        }
        if (newDiffIds.size() > 0) {
            List<Employee> diffEmployees = employeeCachingDao.getEmployees(newDiffIds);
            for (Employee employee : diffEmployees) {
                employee.getTimesheetIds().remove(timesheet.getId());
            }
            dao.batchSaveEmployees(diffEmployees);
        }

        List<Employee> employees = employeeCachingDao.getEmployees(new ArrayList<>(newEmployeeIds));
        for (Employee employee : employees) {
            Set<String> timesheetIds = new HashSet<>(employee.getTimesheetIds());
            Set<String> customerIds = new HashSet<>(employee.getCustomerIds());
            timesheetIds.add(timesheet.getId());
            customerIds.add(timesheet.getCustomer().getId());
            employee.setTimesheetIds(new ArrayList<>(timesheetIds));
            employee.setCustomerIds(new ArrayList<>(customerIds));
        }

        Customer customer = customerCachingDao.getCustomer(timesheet.getCustomer().getId());
        Set<String> timesheetIds = new HashSet<>(customer.getTimesheetIds());
        timesheetIds.add(timesheet.getId());
        customer.setTimesheetIds(new ArrayList<>(timesheetIds));
        timesheet.setCustomerId(customer.getId());

        dao.saveCustomer(customer);
        dao.batchSaveEmployees(employees);
        return dao.saveTimesheet(timesheet);
    }
}
