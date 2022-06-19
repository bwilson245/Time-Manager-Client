package com.tmc.dao;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.Condition;
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
        return mapper.load(Timesheet.class, id);
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

    public List<Timesheet> getTimesheets(String table, Object obj, String workType, String department, String orderNum,
                                         Long before, Long after, Boolean complete, Boolean validated) {

        Map<String, String> nameMap = new HashMap<>();
        Map<String, AttributeValue> valueMap = new HashMap<>();
        String filterExpression = "";

        if (obj instanceof Company) {
            nameMap.put("#id", "companyId");
            nameMap.put("#type", "type");
            valueMap.put(":id", new AttributeValue().withS(((Company) obj).getId()));
            valueMap.put(":type", new AttributeValue().withS(TypeEnum.TIMESHEET.toString()));
        } else if (obj instanceof Customer) {
            nameMap.put("#id", "customerId");
            nameMap.put("#type", "type");
            valueMap.put(":id", new AttributeValue().withS(((Customer) obj).getId()));
            valueMap.put(":type", new AttributeValue().withS(TypeEnum.TIMESHEET.toString()));
        } else if (obj instanceof Employee) {
            nameMap.put("#employeeIds", "employeeIds");
            nameMap.put("#id", "companyId");
            nameMap.put("#type", "type");
            valueMap.put(":id", new AttributeValue().withS(((Employee) obj).getCompanyId()));
            valueMap.put(":employeeId", new AttributeValue().withS(((Employee) obj).getId()));
            valueMap.put(":type", new AttributeValue().withS(TypeEnum.TIMESHEET.toString()));
            filterExpression = filterExpression.concat("contains(#employeeIds, :employeeId)");
        }

        String keyExpression = "#id = :id and #type = :type";

        if (workType != null) {
            nameMap.put("#workType", "workType");
            valueMap.put(":workType", new AttributeValue().withS(workType.toUpperCase()));
            if (filterExpression.length() == 0) {
                filterExpression = filterExpression.concat("contains(#workType, :workType)");
            } else {
                filterExpression = filterExpression.concat(" and contains(#workType, :workType)");
            }
        }

        if (department != null) {
            nameMap.put("#department", "department");
            valueMap.put(":department", new AttributeValue().withS(department.toUpperCase()));
            if (filterExpression.length() == 0) {
                filterExpression = filterExpression.concat("contains(#department, :department)");
            } else {
                filterExpression = filterExpression.concat(" and contains(#department, :department)");
            }
        }

        if (orderNum != null) {
            nameMap.put("#orderNum", "workOrderNumber");
            valueMap.put(":orderNum", new AttributeValue().withS(orderNum));
            if (filterExpression.length() == 0) {
                filterExpression = filterExpression.concat("contains(#orderNum, :orderNum)");
            } else {
                filterExpression = filterExpression.concat(" and contains(#orderNum, :orderNum)");
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
                .withKeyConditionExpression(keyExpression)
                .withExpressionAttributeNames(nameMap)
                .withExpressionAttributeValues(valueMap)
                .withConsistentRead(false);

        if (filterExpression.length() > 0) {
            expression.withFilterExpression(filterExpression);
        }

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

    public List<Employee> getEmployees(String table, Object obj, String name, String email, Boolean isActive) {
        Map<String, String> nameMap = new HashMap<>();
        Map<String, AttributeValue> valueMap = new HashMap<>();

        String filterExpression = "";

        if (obj instanceof Company) {
            nameMap.put("#id", "companyId");
            valueMap.put(":id", new AttributeValue().withS(((Company) obj).getId()));
        }

        if (name != null) {
            nameMap.put("#name", "name");
            valueMap.put(":name", new AttributeValue().withS(name.toUpperCase()));
            if (filterExpression.length() == 0) {
                filterExpression = filterExpression.concat("contains(#name, :name)");
            } else {
                filterExpression = filterExpression.concat(" and contains(#name, :name)");
            }
        }

        if (email != null) {
            nameMap.put("#email", "email");
            valueMap.put(":email", new AttributeValue().withS(email));
            if (filterExpression.length() == 0) {
                filterExpression = filterExpression.concat("contains(#email, :email)");
            } else {
                filterExpression = filterExpression.concat(" and contains(#email, :email)");
            }
        }

        if (isActive != null) {
            nameMap.put("#isActive", "isActive");
            valueMap.put(":isActive", new AttributeValue().withBOOL(isActive));
            if (filterExpression.length() == 0) {
                filterExpression = filterExpression.concat("#isActive = :isActive");
            } else {
                filterExpression = filterExpression.concat(" and #isActive = :isActive");
            }
        }

        DynamoDBQueryExpression<Employee> expression = new DynamoDBQueryExpression<Employee>()
                .withIndexName(table)
                .withKeyConditionExpression("#id = :id")
                .withExpressionAttributeNames(nameMap)
                .withExpressionAttributeValues(valueMap)
                .withConsistentRead(false);

        if (name != null || email != null || isActive != null) {
            expression.withFilterExpression(filterExpression);
        }
        return mapper.query(Employee.class, expression);
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
