package com.tmc.service;

import com.amazonaws.services.dynamodbv2.datamodeling.QueryResultPage;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.google.common.cache.LoadingCache;
import com.tmc.dependency.DaggerServiceComponent;
import com.tmc.dependency.ServiceComponent;
import com.tmc.model.*;
import com.tmc.model.EmployeeInstance;
import com.tmc.service.dao.DynamoDbDao;
import com.tmc.service.manager.CacheManager;
import lombok.Data;


import javax.inject.Inject;
import javax.inject.Singleton;
import java.security.InvalidParameterException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Data
@Singleton
public class TimesheetService {
    private CacheManager cacheManager;
    private DynamoDbDao dao;
    private LoadingCache<String, Timesheet> timesheetCache;
    private LoadingCache<String, Customer> customerCache;
    private LoadingCache<String, Employee> employeeCache;
    private LoadingCache<String, Company> companyCache;

    @Inject
    public TimesheetService(CacheManager cacheManager, DynamoDbDao dao) {
        this.cacheManager = cacheManager;
        this.dao = dao;
        this.timesheetCache = cacheManager.getTimesheetCache();
        this.employeeCache = cacheManager.getEmployeeCache();
        this.customerCache = cacheManager.getCustomerCache();
        this.companyCache = cacheManager.getCompanyCache();
    }


    public Timesheet get(String id) {
        if (id == null) {
            throw new InvalidParameterException("Missing ID.");
        }
        return timesheetCache.getUnchecked(id);
    }

    public List<Timesheet> get(List<String> ids) {
        if (ids == null) {
            throw new InvalidParameterException("Missing ID.");
        }
        List<Timesheet> cached = new ArrayList<>();
        List<String> notCached = new ArrayList<>();
        for (String id : ids) {
            Timesheet timesheet = timesheetCache.getIfPresent(id);
            if (timesheet == null) {
                notCached.add(id);
            } else {
                cached.add(timesheet);
            }
        }
        cached.addAll(dao.getTimesheets(notCached));
        for (Timesheet timesheet : cached) {
            timesheetCache.put(timesheet.getId(), timesheet);
        }
        return cached;
    }



    public QueryResultPage<Timesheet> search(String id, String workType, String department, String orderNum, Long before, Long after, Boolean complete, Boolean validated, Map<String, AttributeValue> startKey, Integer limit) {
        if (id == null) {
            throw new InvalidParameterException("Missing ID.");
        }
        return dao.searchTimesheets(id, workType, department, orderNum, before, after, complete, validated, startKey, limit);
    }


    /**
     * Creates a new Timesheet object. Only requires basic information about the job. isValidated is set to false
     * because it is considered an "unapproved" Timesheet object and therefore should not be added to the employees or
     * customer's list of timesheetIds. An administrator is required to "validate" the timesheet.
     * Retrieved Lists are converted to Set for the purpose of avoiding duplication of Ids.
     * @param request - The request object containing all the information required to build a new Timesheet.
     * @return returns the newly created Timesheet with isValidated = false.
     */


    public Timesheet create(Timesheet request) {
        if (request.getCompanyId() == null) {
            throw new InvalidParameterException("Missing ID.");
        }

        //****** Retrieve the Company object *******//
        //****** throws CompanyNotFoundException if not in cache or dynamodb *******//
        Company company = cacheManager.getCompanyCache().getUnchecked(request.getCompanyId());

        //****** Builds a new Timesheet object from the request *******//
        Timesheet timesheet = new Timesheet(request);

        //****** Save the companyId and name to each EmployeeInstance *******//
        request.getEmployeeInstances().forEach(i -> {
            i.setCompanyId(company.getId());
            i.setName(i.getName().toUpperCase());
        });

        //****** Add the timesheetId to the company's timesheetIds *******//
        Set<String> timesheetIds = new HashSet<>(company.getTimesheetIds());
        timesheetIds.add(timesheet.getId());
        company.setTimesheetIds(new ArrayList<>(timesheetIds));

        //****** Save final values *******//
        dao.saveCompany(company);
        timesheetCache.put(timesheet.getId(), timesheet);
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

    public Timesheet edit(Timesheet request, EmployeeService employeeService) {
        if (request.getId() == null) {
            throw new InvalidParameterException("Missing ID.");
        }

        //****** Define original and new timesheet and customer *******//
        Timesheet originalTimesheet = cacheManager.getTimesheetCache().getUnchecked(request.getId());
        Timesheet timesheet = new Timesheet(request, originalTimesheet);
        Customer customer = timesheet.getCustomer();


        //****** Build a list of ids that were removed *******//
        Set<String> originalEmployeeInstanceIds = originalTimesheet.getEmployeeInstances().stream().map(EmployeeInstance::getId).collect(Collectors.toSet());
        Set<String> newEmployeeIds = timesheet.getEmployeeInstances().stream().map(EmployeeInstance::getId).collect(Collectors.toSet());
        Set<String> removed = originalEmployeeInstanceIds.stream().filter(id -> !newEmployeeIds.contains(id)).collect(Collectors.toSet());


        //****** Remove this timesheet from removed employees timesheetIds *******//
        //****** and remove each employee id from the customer employeeIds *******//
        Set<Employee> tmp = removed.stream().map((id) -> {
            Employee employee = employeeService.get(id);
            List<String> timesheetIds = employee.getTimesheetIds().stream().filter(i -> !i.equals(id)).collect(Collectors.toList());
            List<String> employeeIds = customer.getEmployeeIds().stream().filter(i -> !i.equals(id)).collect(Collectors.toList());
            employee.setTimesheetIds(new ArrayList<>(timesheetIds));
            customer.setEmployeeIds(new ArrayList<>(employeeIds));
            return employee;
        }).collect(Collectors.toSet());


        //****** set the new employeeIds to the timesheet and customer  *******//
        timesheet.setEmployeeIds(new ArrayList<>(newEmployeeIds));
        Set<String> customerEmployeeIds = new HashSet<>(customer.getEmployeeIds());
        customerEmployeeIds.addAll(timesheet.getEmployeeIds());
        customer.setEmployeeIds(new ArrayList<>(customerEmployeeIds));

        //****** Add this timesheetId and customerId to each employee  *******//
        Set<Employee> employees = new HashSet<>(new ArrayList<>(employeeService.get(new ArrayList<>(newEmployeeIds))));
        employees.forEach(employee -> {
            Set<String> timesheetIds = new HashSet<>(employee.getTimesheetIds());
            Set<String> customerIds = new HashSet<>(employee.getCustomerIds());
            timesheetIds.add(timesheet.getId());
            customerIds.add(timesheet.getCustomer().getId());
            employee.setTimesheetIds(new ArrayList<>(timesheetIds));
            employee.setCustomerIds(new ArrayList<>(customerIds));
        });


        //****** Build a list of customer's timesheetIds and adds the timesheetId *******//
        Set<String> timesheetIds = new HashSet<>(customer.getTimesheetIds());
        timesheetIds.add(timesheet.getId());
        customer.setTimesheetIds(new ArrayList<>(timesheetIds));

        //****** Build a list of employee's timesheetIds and adds the timesheetId *******//

        //****** If some employees were removed, add them to employees list before batch save *******//
        if (tmp.size() > 0) {
            employees.addAll(tmp);
        }

        //****** Save final values *******//
        dao.saveCustomer(customer);
        dao.batchSaveEmployees(new ArrayList<>(employees));
        return dao.saveTimesheet(timesheet);
    }

    public void toggleValidated(String id) {
        if (id == null) {
            throw new InvalidParameterException("Missing ID.");
        }
        Timesheet timesheet = timesheetCache.getUnchecked(id);
        timesheet.setIsValidated(!timesheet.getIsValidated());
        dao.saveTimesheet(timesheet);
    }

    public static void main(String[] args) throws InterruptedException {
        ServiceComponent dagger = DaggerServiceComponent.create();
        TimesheetService service = dagger.provideTimesheetService();
        service.generateTimesheets();
    }

    public void generateTimesheets() throws InterruptedException {
        ServiceComponent dagger = DaggerServiceComponent.create();
        EmployeeService employeeService = dagger.provideEmployeeService();
        CustomerService customerService = dagger.provideCustomerService();
        CompanyService companyService = dagger.provideCompanyService();
        int numTimesheets = 1;

        Company company = companyService.get("75b1a9c7-6887-4b75-8c2c-7a7219962f62");
        List<Employee> employees = employeeService.get(company.getEmployeeIds());
        List<Customer> customers = customerService.get(company.getCustomerIds());
        Random random = new Random();

        for (int i = 0; i < numTimesheets; i++) {
            int customerIndex = random.nextInt(customers.size());
            int employeesPerTimesheet = 5;
            long date = new Date().getTime();
            long randomDate = random.nextLong();

            Customer customer = customers.get(customerIndex);
            List<EmployeeInstance> instances = new ArrayList<>();
            String[] departments = new String[] {"chicken", "sewer", "water", "residential", "wastewater", "evis", "processing"};
            String[] workTypes = new String[] {"installation", "repairs"};
            String[] names = new String[] {"John Doe", "Jane Doe", "Ben Wilson", "Brad Edgerton", "Mike Slack", "Trevor Hunt", "Joey Howel", "Adam Nolan", "Zack Taute", "Jenny Harville", "Caleb Lester", "Ken Batey", "Terry Edderington", "Johnny Nolan"};

            for (int j = 0; j < company.getEmployeeIds().size(); j++) {


                Employee employee = employees.get(j);
                EmployeeInstance instance = EmployeeInstance.builder()
                        .name(employee.getName())
                        .id(employee.getId())
                        .companyId(employee.getCompanyId())
                        .straightTime(8.0)
                        .overTime(2.0)
                        .doubleTime(3.5)
                        .driveTime(4.5)
                        .build();
                instances.add(instance);
            }
            String name = names[random.nextInt(names.length)];
            String department = departments[random.nextInt(departments.length)];
            String workType = workTypes[random.nextInt(workTypes.length)];

            Timesheet timesheet = Timesheet.builder()
                    .employeeInstances(instances)
                    .customer(customer)
                    .location(customer.getLocation())
                    .companyId(company.getId())
                    .customerId(customer.getId())
                    .date(randomDate)
                    .isComplete(false)
                    .description("test description")
                    .workOrderNumber(String.valueOf(random.nextInt()))
                    .department(department)
                    .workType(workType)
                    .type(Const.TIMESHEET)
                    .build();

            create(timesheet);
            Thread.sleep(1000);

        }
    }

}
