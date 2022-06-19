package com.tmc.dao;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMappingException;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.tmc.exception.TimesheetNotFoundException;
import com.tmc.model.*;

import javax.inject.Inject;
import java.util.*;

public class DynamoDbDao {
    private DynamoDBMapper mapper;
    private DynamoDB dynamoDB;

    @Inject
    public DynamoDbDao(DynamoDBMapper mapper, DynamoDB dynamoDB) {
        this.mapper = mapper;
        this.dynamoDB = dynamoDB;
    }

    /*
    **************** TIMESHEET **************
     */

    public Timesheet getTimesheet(String id) {
        Timesheet timesheet = mapper.load(Timesheet.class, id);
        if (timesheet == null) {
            throw new TimesheetNotFoundException("Could not find Timesheet with id: " + id);
        }
        return timesheet;
    }

    public List<Timesheet> getTimesheets(String table, Object obj, String type, String department, String orderNum,
                                         Long before, Long after, Boolean complete, Boolean validated) {

        Map<String, String> nameMap = new HashMap<>();
        Map<String, AttributeValue> valueMap = new HashMap<>();
        String filterExpression = "";

        if (obj instanceof Company) {
            nameMap.put("#id", "companyId");
            valueMap.put(":id", new AttributeValue().withS(((Company) obj).getId()));
        } else if (obj instanceof Customer) {
            nameMap.put("#id", "customerId");
            valueMap.put(":id", new AttributeValue().withS(((Customer) obj).getId()));
        } else if (obj instanceof Employee) {
            nameMap.put("#employees", "employeeInstances");
            nameMap.put("#id", "companyId");
            valueMap.put(":id", new AttributeValue().withS(((Employee) obj).getCompanyId()));
            valueMap.put(":instanceId", new AttributeValue().withS(((Employee) obj).getId()));
            filterExpression = filterExpression.concat("contains(employeeInstances, :instanceId");
        }

        if (type != null) {
            nameMap.put("#type", "type");
            valueMap.put(":type", new AttributeValue().withS(type));
            if (filterExpression.length() == 0) {
                filterExpression = filterExpression.concat("#type = :type");
            } else {
                filterExpression = filterExpression.concat(" and #type = :type");
            }
        }

        if (department != null) {
            nameMap.put("#department", "department");
            valueMap.put(":department", new AttributeValue().withS(department));
            if (filterExpression.length() == 0) {
                filterExpression = filterExpression.concat("#department = :department");
            } else {
                filterExpression = filterExpression.concat(" and #department = :department");
            }
        }

        if (orderNum != null) {
            nameMap.put("#orderNum", "workOrderNumber");
            valueMap.put(":orderNum", new AttributeValue().withS(orderNum));
            if (filterExpression.length() == 0) {
                filterExpression = filterExpression.concat("#orderNum = :orderNum");
            } else {
                filterExpression = filterExpression.concat(" and #orderNum = :orderNum");
            }
        }

        if (complete != null) {
            nameMap.put("#complete", "isComplete");
            valueMap.put(":complete", new AttributeValue().withBOOL(complete));
            if (filterExpression.length() == 0) {
                filterExpression = filterExpression.concat("#complete = :complete");
            } else {
                filterExpression = filterExpression.concat(" and #complete = :complete");
            }
        }

        if (validated != null) {
            nameMap.put("#validated", "isValidated");
            valueMap.put(":validated", new AttributeValue().withBOOL(validated));
            if (filterExpression.length() == 0) {
                filterExpression = filterExpression.concat("#validated = :validated");
            } else {
                filterExpression = filterExpression.concat(" and #validated = :validated");
            }
        }

        if (before != null) {
            nameMap.put("#date", "_date");
            valueMap.put(":before", new AttributeValue().withN(before.toString()));
            if (filterExpression.length() == 0) {
                filterExpression = filterExpression.concat("#date <= :before");
            } else {
                filterExpression = filterExpression.concat(" and #date <= :before");
            }
        }

        if (after != null) {
            nameMap.put("#date", "_date");
            valueMap.put(":after", new AttributeValue().withN(after.toString()));
            if (filterExpression.length() == 0) {
                filterExpression = filterExpression.concat("#date >= :after");
            } else {
                filterExpression = filterExpression.concat(" and #date >= :after");
            }
        }

        DynamoDBQueryExpression<Timesheet> expression = new DynamoDBQueryExpression<Timesheet>()
                .withIndexName(table)
                .withKeyConditionExpression("#id = :id")
                .withFilterExpression(filterExpression)
                .withExpressionAttributeNames(nameMap)
                .withExpressionAttributeValues(valueMap)
                .withConsistentRead(false);

        return mapper.query(Timesheet.class, expression);
    }

    public Timesheet saveTimesheet(Timesheet timesheet) {
        mapper.save(timesheet);
        return timesheet;
    }

    public Timesheet deleteTimesheet(Timesheet timesheet) {
        mapper.delete(timesheet);
        return timesheet;
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

    public Customer saveCustomer(Customer customer) {
        mapper.save(customer);
        return customer;
    }

    public Customer deleteCustomer(Customer customer) {
        mapper.delete(customer);
        return customer;
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

    public List<Employee> batchSaveEmployees(List<Employee> employees) {
        mapper.batchSave(employees);
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

    public Employee saveEmployee(Employee employee) {
        mapper.save(employee);
        return employee;
    }

    public Employee deleteEmployee(Employee employee) {
        mapper.delete(employee);
        return employee;
    }

    /*
     **************** COMPANY **************
     */
    public Company getCompany(String id) {
        return mapper.load(Company.class, id);
    }

    public Company saveCompany(Company company) {
        mapper.save(company);
        return company;
    }

    public Company deleteCompany(Company company) {
        mapper.delete(company);
        return company;
    }
}
