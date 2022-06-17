package com.tmc.dao;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.tmc.model.*;
import kotlin.sequences.USequencesKt;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class DynamoDbDao {
    private DynamoDBMapper mapper;

    @Inject
    public DynamoDbDao(DynamoDBMapper mapper) {
        this.mapper = mapper;
    }

    /*
    **************** TIMESHEET **************
     */

    public Timesheet getTimesheet(String id) {
        return mapper.load(Timesheet.class, id);
//        List<EmployeeInstance> employeeInstances = new ArrayList<>();
//        employeeInstances.add(new EmployeeInstance("23456", "Ben Wilson", 8.0, 0.0, 0.0, 3.0));
//        employeeInstances.add(new EmployeeInstance("6784756", "Trevor Hunt", 3.0, 5.0, 0.0, 3.0));
//        employeeInstances.add(new EmployeeInstance("634563", "Mike Slack", 8.0, 4.0, 9.5, 3.0));
//        Location location = Location.builder()
//                .address1("938 Urbana Rd")
//                .city("El Dorado")
//                .state("Arkansas")
//                .zip("71730")
//                .build();
//        Customer customer = Customer.builder()
//                .id("qw32345")
//                .name("Pilgrims Pride")
//                .location(location)
//                .build();
//        Timesheet timesheet = Timesheet.builder()
//                .id("12345")
//                .date(new Date().getTime())
//                .department("sewer")
//                .description("This is a test description. did some bullshit. some more shit. some more shit. some more shit. some more shit. some more shit. some more shit. some more shit. some more shit. some more shit. some more shit. some more shit. some more shit. some more shit.")
//                .isComplete(false)
//                .type(TypeEnum.INSTALLATION)
//                .workOrderNumber("5908739475")
//                .customer(customer)
//                .employeeInstances(employeeInstances)
//                .build();
//        return timesheet;
    }

    public List<Timesheet> getTimesheets(List<String> ids) {
        List<Timesheet> timesheets = new ArrayList<>();
        for (String s : ids) {
            Timesheet timesheet = Timesheet.builder().id(s).build();
            timesheets.add(timesheet);
        }

        Map<String, List<Object>> loadResult = mapper.batchLoad(timesheets);
        timesheets.clear();
        for (Map.Entry<String, List<Object>> entry : loadResult.entrySet()) {
            for (Object o : entry.getValue()) {
                if (o instanceof Timesheet) {
                    timesheets.add((Timesheet) o);
                }
            }
        }
        return timesheets;
    }

    public void saveTimesheet(Timesheet timesheet) {
        mapper.save(timesheet);
    }

    public void deleteTimesheet(Timesheet timesheet) {
        mapper.delete(timesheet);
    }

    /*
     **************** CUSTOMER **************
     */
    public Customer getCustomer(String id) {
        return mapper.load(Customer.class, id);
    }

    public List<Customer> getCustomers(List<String> ids) {
        List<Customer> customers = new ArrayList<>();
        for (String s : ids) {
            Customer customer = Customer.builder().id(s).build();
            customers.add(customer);
        }

        Map<String, List<Object>> loadResult = mapper.batchLoad(customers);
        customers.clear();
        for (Map.Entry<String, List<Object>> entry : loadResult.entrySet()) {
            for (Object o : entry.getValue()) {
                if (o instanceof Customer) {
                    customers.add((Customer) o);
                }
            }
        }
        return customers;
    }

    public void saveCustomer(Customer customer) {
        mapper.save(customer);
    }

    public void deleteCustomer(Customer customer) {
        mapper.delete(customer);
    }

    /*
     **************** EMPLOYEE **************
     */
    public Employee getEmployee(String id) {
        return mapper.load(Employee.class, id);
    }

    public List<Employee> getEmployees(List<String> ids) {
        List<Employee> employees = new ArrayList<>();
        for (String s : ids) {
            Employee employee = Employee.builder().id(s).build();
            employees.add(employee);
        }

        Map<String, List<Object>> loadResult = mapper.batchLoad(employees);
        employees.clear();
        for (Map.Entry<String, List<Object>> entry : loadResult.entrySet()) {
            for (Object o : entry.getValue()) {
                if (o instanceof Employee) {
                    employees.add((Employee) o);
                }
            }
        }
        return employees;
    }

    public Employee getEmployeeByEmail(String email) {
        Employee employee = Employee.builder().email(email).build();

        DynamoDBQueryExpression<Employee> expression = new DynamoDBQueryExpression<Employee>()
                .withHashKeyValues(employee)
                .withConsistentRead(false);

        List<Employee> result = mapper.query(Employee.class, expression);
        if (result.size() > 0) {
            return result.get(0);
        }
        return null;
    }

    public void saveEmployee(Employee employee) {
        mapper.save(employee);
    }

    public void deleteEmployee(Employee employee) {
        mapper.delete(employee);
    }
}
