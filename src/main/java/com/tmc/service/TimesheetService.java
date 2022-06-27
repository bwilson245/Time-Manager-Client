package com.tmc.service;

import com.amazonaws.services.dynamodbv2.datamodeling.QueryResultPage;
import com.google.common.cache.LoadingCache;
import com.tmc.dependency.DaggerServiceComponent;
import com.tmc.dependency.ServiceComponent;
import com.tmc.model.*;
import com.tmc.model.EmployeeInstance;
import com.tmc.model.request.SearchTimesheetRequest;
import com.tmc.service.dao.DynamoDbDao;
import com.tmc.service.manager.CacheManager;
import lombok.Data;


import javax.inject.Inject;
import javax.inject.Singleton;
import java.security.InvalidParameterException;
import java.util.*;
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



    public QueryResultPage<Timesheet> search(String companyId, SearchTimesheetRequest request) {
        if (companyId == null) {
            throw new InvalidParameterException("Missing companyId.");
        }
        return dao.search(companyId, request);
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

        //****** Define original and new timesheet *******//
        Timesheet originalTimesheet = timesheetCache.getUnchecked(request.getId());
        Timesheet timesheet = new Timesheet(request, originalTimesheet);

        //****** Build a list of ids that were removed *******//
        Set<String> originalEmployeeInstanceIds = originalTimesheet.getEmployeeInstances().stream().map(EmployeeInstance::getId).collect(Collectors.toSet());
        Set<String> newEmployeeIds = timesheet.getEmployeeInstances().stream().map(EmployeeInstance::getId).collect(Collectors.toSet());
        Set<String> removed = new HashSet<>();
        originalEmployeeInstanceIds.forEach(id -> {
            if (!newEmployeeIds.contains(id)) {
                removed.add(id);
            }
        });


        //****** Remove this timesheet from removed employees timesheetIds *******//
        removed.forEach(id -> {
            Employee employee = employeeCache.getUnchecked(id);
            List<String> timesheetIds = employee.getTimesheetIds();
            timesheetIds.remove(timesheet.getId());
            employee.setTimesheetIds(new ArrayList<>(timesheetIds));
            dao.saveEmployee(employee);
        });

        //****** set the new employeeIds to the timesheet  *******//
        timesheet.setEmployeeIds(new ArrayList<>(newEmployeeIds));

        //****** Add this timesheetId and customerId to each employee  *******//
        Set<Employee> employees = new HashSet<>(new ArrayList<>(employeeService.get(new ArrayList<>(newEmployeeIds))));
        employees.forEach(employee -> {
            Set<String> timesheetIds = new HashSet<>(employee.getTimesheetIds());
            timesheetIds.add(timesheet.getId());
            employee.setTimesheetIds(new ArrayList<>(timesheetIds));
        });

        //****** Retrieve the customer object and modify it according to the timesheet *******//
        if (!timesheet.getCustomerId().equals("*")) {
            Customer customer = customerCache.getUnchecked(timesheet.getCustomerId());

            //****** If original customer and new customer are different, remove this timesheetId from the original and save *******//
            if (!timesheet.getCustomerId().equals(originalTimesheet.getCustomerId()) && originalTimesheet.getCustomerId() != null) {
                Customer originalCustomer = customerCache.getUnchecked(originalTimesheet.getCustomerId());
                originalCustomer.getTimesheetIds().remove(timesheet.getId());
                originalCustomer.setTimesheetIds(new ArrayList<>(originalCustomer.getTimesheetIds()));
                dao.saveCustomer(originalCustomer);
            }

            //****** Add the timesheetId to this customer *******//
            Set<String> timesheetIds = new HashSet<>(customer.getTimesheetIds());
            timesheetIds.add(timesheet.getId());
            customer.setTimesheetIds(new ArrayList<>(timesheetIds));


            //****** Add the customerId to these employees *******//
            employees.forEach(employee -> {
                Set<String> customerIds = new HashSet<>(employee.getCustomerIds());
                customerIds.add(customer.getId());
                employee.setCustomerIds(new ArrayList<>(customerIds));

                //****** Add the employeeIds to this customer *******//
                Set<String> employeeIds = new HashSet<>(customer.getEmployeeIds());
                employeeIds.add(employee.getId());
                customer.setEmployeeIds(new ArrayList<>(employeeIds));
            });

            dao.saveCustomer(customer);
        }

        //****** Save final values *******//
        dao.batchSaveEmployees(new ArrayList<>(employees));
        dao.saveTimesheet(timesheet);
        return timesheet;
    }

    public void deleteTimesheet(String id) {
        Timesheet timesheet = timesheetCache.getUnchecked(id);
        Company company = companyCache.getUnchecked(timesheet.getCompanyId());
        List<Employee> employees = timesheet.getEmployeeIds().stream().map(employeeCache::getUnchecked).collect(Collectors.toList());
        Customer customer = customerCache.getUnchecked(timesheet.getCustomerId());

        company.getTimesheetIds().remove(id);
        customer.getTimesheetIds().remove(id);
        employees.forEach(e -> e.getTimesheetIds().remove(id));

        dao.saveCompany(company);
        dao.saveCustomer(customer);
        dao.batchSaveEmployees(employees);
    }


    // Reserved for creation of new objects for testing


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

        Company company = companyService.get("company.07ae01de-a9df-465c-990c-d28217d89baf");
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
                    .customerName("test customerName")
                    .customerLoc(customer.getLocation())
                    .location(customer.getLocation())
                    .companyId(company.getId())
                    .customerId(customer.getId())
                    .date(randomDate)
                    .complete(false)
                    .description("test description")
                    .workOrderNumber(String.valueOf(random.nextInt()))
                    .department(department)
                    .workType(workType)
                    .build();

            create(new Timesheet(timesheet));
            Thread.sleep(1000);

        }
    }

}
