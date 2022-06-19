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

    /**
     * Returns a Timesheet object from cache. If it is not in the cache, retrieves it from the database.
     * @param id - the id of the timesheet.
     * @return - returns the timesheet.
     */
    public Timesheet getTimesheet(String id) {
        return cache.getUnchecked(id);
    }

    public List<Timesheet> getTimesheetsSearch(TypeEnum type, String id, String workType, String department, String orderNum,
                                               Long before, Long after, Boolean complete, Boolean validated) {
        Object obj;
        String table;
        switch (type) {
            case CUSTOMER:
                obj = customerCachingDao.getCustomer(id);
                table = "customerId-type-index";
                break;
            case EMPLOYEE:
                obj = employeeCachingDao.getEmployee(id);
                table = "companyId-type-index";
                break;
            default:
                obj = companyCachingDao.getCompany(id);
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
                .department(request.getDepartment().toUpperCase())
                .description(request.getDescription())
                .workType(request.getType().toUpperCase())
                .build();

        Set<String> timesheetIds = new HashSet<>(company.getTimesheetIds());
        timesheetIds.add(timesheet.getId());
        company.setTimesheetIds(new ArrayList<>(timesheetIds));

        dao.saveCompany(company);
        cache.put(timesheet.getId(), timesheet);
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
    public Timesheet editTimesheet(String id, EditTimesheetRequest request) {
        Timesheet timesheet = cache.getUnchecked(id);

        Set<String> originalEmployeeInstanceIds = new HashSet<>();
        for (EmployeeInstance instance : timesheet.getEmployeeInstances()) {
            originalEmployeeInstanceIds.add(instance.getId());
        }

        timesheet.setLocation(Optional.ofNullable(request.getLocation()).orElse(timesheet.getLocation()));
        timesheet.setCustomer(Optional.ofNullable(request.getCustomer()).orElse(timesheet.getCustomer()));
        timesheet.setDate(Optional.ofNullable(request.getDate()).orElse(timesheet.getDate()));
        timesheet.setEmployeeInstances(Optional.ofNullable(request.getEmployeeInstances()).orElse(timesheet.getEmployeeInstances()));
        timesheet.setIsComplete(Optional.ofNullable(request.getIsComplete()).orElse(timesheet.getIsComplete()));
        timesheet.setWorkOrderNumber(Optional.ofNullable(request.getWorkOrderNumber()).orElse(timesheet.getWorkOrderNumber()));
        timesheet.setDepartment(Optional.ofNullable(request.getDepartment()).orElse(timesheet.getDepartment()));
        timesheet.setDescription(Optional.ofNullable(request.getDescription()).orElse(timesheet.getDescription()));
        timesheet.setWorkType(Optional.ofNullable(request.getWorkType()).orElse(timesheet.getWorkType()));
        timesheet.setIsValidated(Optional.ofNullable(request.getIsValidated()).orElse(timesheet.getIsValidated()));

        Set<String> newEmployeeIds = new HashSet<>();
        for (EmployeeInstance instance : timesheet.getEmployeeInstances()) {
            newEmployeeIds.add(instance.getId());
        }

        Set<String> newDiffIds = new HashSet<>();
        for (String originId : originalEmployeeInstanceIds) {
            if (!newEmployeeIds.contains(originId)) {
                newDiffIds.add(originId);
            }
        }
        if (newDiffIds.size() > 0) {
            Set<Employee> diffEmployees = new HashSet<>(employeeCachingDao.getEmployees(new ArrayList<>(newDiffIds)));
            for (Employee employee : diffEmployees) {
                employee.getTimesheetIds().remove(timesheet.getId());
            }
            dao.batchSaveEmployees(new ArrayList<>(diffEmployees));
        }

        Set<String> employeeIds = new HashSet<>(timesheet.getEmployeeIds());
        Set<Employee> employees = new HashSet<>(employeeCachingDao.getEmployees(new ArrayList<>(newEmployeeIds)));
        for (Employee employee : employees) {
            employeeIds.add(employee.getId());
            Set<String> timesheetIds = new HashSet<>(employee.getTimesheetIds());
            Set<String> customerIds = new HashSet<>(employee.getCustomerIds());
            timesheetIds.add(timesheet.getId());
            customerIds.add(timesheet.getCustomer().getId());
            employee.setTimesheetIds(new ArrayList<>(timesheetIds));
            employee.setCustomerIds(new ArrayList<>(customerIds));
        }
        timesheet.setEmployeeIds(new ArrayList<>(employeeIds));

        Customer customer = customerCachingDao.getCustomer(timesheet.getCustomer().getId());
        Set<String> timesheetIds = new HashSet<>(customer.getTimesheetIds());
        timesheetIds.add(timesheet.getId());
        customer.setTimesheetIds(new ArrayList<>(timesheetIds));
        timesheet.setCustomerId(customer.getId());

        dao.saveCustomer(customer);
        dao.batchSaveEmployees(new ArrayList<>(employees));
        return dao.saveTimesheet(timesheet);
    }
}
