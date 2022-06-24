package com.tmc.service.dao;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList;
import com.amazonaws.services.dynamodbv2.datamodeling.QueryResultPage;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.tmc.exception.CompanyNotFoundException;
import com.tmc.exception.CustomerNotFoundException;
import com.tmc.exception.EmployeeNotFoundException;
import com.tmc.exception.TimesheetNotFoundException;
import com.tmc.model.*;
import lombok.Data;
import org.checkerframework.checker.units.qual.C;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.stream.Collectors;

@Data
@Singleton
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
            throw new TimesheetNotFoundException(id);
        }
        return timesheet;
    }

    public List<Timesheet> getTimesheets(List<String> ids) {
//        List<Timesheet> timesheets = new ArrayList<>();
//        for (String s : ids) {
//            Timesheet timesheet = Timesheet.builder().id(s).build();
//            timesheets.add(timesheet);
//        }
        List<Timesheet> timesheets = ids.stream().map(id -> Timesheet.builder().id(id).build()).collect(Collectors.toList());


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

    public QueryResultPage<Timesheet> searchTimesheets(String id, String workType, String department, String orderNum, Long before,
                                                    Long after, Boolean complete, Boolean validated,
                                                    Map<String, AttributeValue> startKey, Integer limit) {

        if (limit == null || limit < 1) {
            limit = 10;
        }

        Map<String, String> nameMap = new HashMap<>();
        Map<String, AttributeValue> valueMap = new HashMap<>();

        String table = "companyId-index";
        String keyExpression = "#_companyId = :_companyId";
        String filterExpression = "";


        nameMap.put("#_companyId", "_companyId");
        valueMap.put(":_companyId", new AttributeValue().withS(id));



        if (workType != null) {
            if (filterExpression.length() > 0) {
                filterExpression = filterExpression.concat(" and ");
            }
            filterExpression = filterExpression.concat("contains(#_workType, :_workType");
            nameMap.put("#_workType", "_workType");
            valueMap.put(":_workType", new AttributeValue().withS(workType));
        }

        if (department != null) {
            if (filterExpression.length() > 0) {
                filterExpression = filterExpression.concat(" and ");
            }
            filterExpression = filterExpression.concat("contains(#_department, :_department");
            nameMap.put("#_department", "_department");
            valueMap.put(":_department", new AttributeValue().withS(department));
        }

        if (orderNum != null) {
            if (filterExpression.length() > 0) {
                filterExpression = filterExpression.concat(" and ");
            }
            filterExpression = filterExpression.concat("contains(#_orderNum, :_orderNum");
            nameMap.put("#_orderNum", "_orderNum");
            valueMap.put(":_orderNum", new AttributeValue().withS(orderNum));
        }

        if (complete != null) {
            if (filterExpression.length() > 0) {
                filterExpression = filterExpression.concat(" and ");
            }
            filterExpression = filterExpression.concat("#_complete = :_complete");
            nameMap.put("#_complete", "_isComplete");
            valueMap.put(":_complete", new AttributeValue().withBOOL(complete));
        }

        if (validated != null) {
            if (filterExpression.length() > 0) {
                filterExpression = filterExpression.concat(" and ");
            }
            filterExpression = filterExpression.concat("#_validated = :_validated");
            nameMap.put("#_validated", "_validated");
            valueMap.put(":_validated", new AttributeValue().withBOOL(validated));
        }

        if (before != null && after != null) {
            if (filterExpression.length() > 0) {
                filterExpression = filterExpression.concat(" and ");
            }
            filterExpression = filterExpression.concat("#_date between :_before and :_after");
            nameMap.put("#_date", "_date");
            valueMap.put(":_before", new AttributeValue().withN(String.valueOf(before)));
            valueMap.put(":_after", new AttributeValue().withN(String.valueOf(after)));
        } else if (before != null) {
            if (filterExpression.length() > 0) {
                filterExpression = filterExpression.concat(" and ");
            }
            filterExpression = filterExpression.concat("#_date <= :_before");
            nameMap.put("#_date", "_date");
            valueMap.put(":_before", new AttributeValue().withN(String.valueOf(before)));
        } else if (after != null) {
            if (filterExpression.length() > 0) {
                filterExpression = filterExpression.concat(" and ");
            }
            filterExpression = filterExpression.concat("#_date >= :_after");
            nameMap.put("#_date", "_date");
            valueMap.put(":_after", new AttributeValue().withN(String.valueOf(after)));
        }

        DynamoDBQueryExpression<Timesheet> expression = new DynamoDBQueryExpression<Timesheet>()
                .withIndexName(table)
                .withKeyConditionExpression(keyExpression)
                .withExpressionAttributeNames(nameMap)
                .withExpressionAttributeValues(valueMap)
                .withLimit(limit)
                .withConsistentRead(false);

        if (filterExpression.length() > 0) {
            expression.setFilterExpression(filterExpression);
        }
        if (startKey != null) {
            expression.setExclusiveStartKey(startKey);
        }

        QueryResultPage<Timesheet> page = mapper.queryPage(Timesheet.class, expression);
        return page;
    }

    public Timesheet saveTimesheet(Timesheet timesheet) {
        mapper.save(timesheet);
        return timesheet;
    }

    public void batchSaveTimesheets(List<Timesheet> timesheets) {
        mapper.batchSave(timesheets);
    }

    public void batchDeleteTimesheets(List<Timesheet> timesheets) {
        mapper.batchDelete(timesheets);
    }

    /*
     **************** CUSTOMER **************
     */
    public Customer getCustomer(String id) {
        Customer customer = mapper.load(Customer.class, id);
        if (customer == null) {
            throw new CustomerNotFoundException(id);
        }
        return customer;
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


    public QueryResultPage<Customer> searchCustomers(String id, SearchCustomerRequest request) {

        if (request.getLimit() == null || request.getLimit() < 1) {
            request.setLimit(10);
        }

        Map<String, String> nameMap = new HashMap<>();
        Map<String, AttributeValue> valueMap = new HashMap<>();

        String table = Const.COMPANY_ID_ID_INDEX_GSI;
        String keyExpression = "#_companyId = :_companyId and #_id begins_with :_id";
        String filterExpression = "";


        nameMap.put("#_companyId", "_companyId");
        nameMap.put("#_id", "_:id");
        valueMap.put(":_companyId", new AttributeValue().withS(id));
        valueMap.put(":_id", new AttributeValue().withS("customer."));

        if (request.getName() != null) {
            if (filterExpression.length() > 0) {
                filterExpression = filterExpression.concat(" and ");
            }
            filterExpression = filterExpression.concat("contains(#_name, :_name");
            nameMap.put("#_name", "_name");
            valueMap.put(":_name", new AttributeValue().withS(request.getName()));
        }



        if (request.getIsActive() != null) {
            if (filterExpression.length() > 0) {
                filterExpression = filterExpression.concat(" and ");
            }
            filterExpression = filterExpression.concat("#_isActive = :_isActive");
            nameMap.put("#_isActive", "_isActive");
            valueMap.put(":_isActive", new AttributeValue().withBOOL(request.getIsActive()));
        }

        DynamoDBQueryExpression<Customer> expression = new DynamoDBQueryExpression<Customer>()
                .withIndexName(table)
                .withKeyConditionExpression(keyExpression)
                .withExpressionAttributeNames(nameMap)
                .withExpressionAttributeValues(valueMap)
                .withLimit(request.getLimit())
                .withConsistentRead(false);

        if (filterExpression.length() > 0) {
            expression.setFilterExpression(filterExpression);
        }
        if (request.getStartKey() != null) {
            expression.setExclusiveStartKey(request.getStartKey());
        }

        QueryResultPage<Customer> page = mapper.queryPage(Customer.class, expression);
        List<Customer> results = new ArrayList<>(page.getResults());
        int count = page.getCount();

        if (count < request.getLimit()) {

            while (count < request.getLimit() && page.getLastEvaluatedKey() != null) {
                expression = expression.withLimit(request.getLimit() - page.getCount());
                expression = expression.withExclusiveStartKey(page.getLastEvaluatedKey());
                page = mapper.queryPage(Customer.class, expression);
                results.addAll(page.getResults());
                count += page.getCount();
            }
            page.setResults(new ArrayList<>(results));
        }
        return page;
    }

    public void saveCustomer(Customer customer) {
        mapper.save(customer);
    }

    public void batchSaveCustomers(List<Customer> customers) {
        mapper.batchSave(customers);
    }

    public void deleteCustomer(Customer customer) {
        mapper.delete(customer);
    }

    public void batchDeleteCustomers(List<Customer> customers) {
        mapper.batchDelete(customers);
    }

    /*
     **************** EMPLOYEE **************
     */
    public Employee getEmployee(String id) {
        Employee employee = mapper.load(Employee.class, id);
        if (employee == null) {
            throw new EmployeeNotFoundException(id);
        }
        return employee;
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

    public QueryResultPage<Employee> searchEmployees(String id, String name, String email, Boolean isActive, Map<String, AttributeValue> startKey, Integer limit) {
        if (limit == null || limit < 1) {
            limit = 10;
        }

        Map<String, String> nameMap = new HashMap<>();
        Map<String, AttributeValue> valueMap = new HashMap<>();

        String table = Const.COMPANY_ID_ID_INDEX_GSI;
        String keyExpression = "#_companyId = :_companyId";



        nameMap.put("#_companyId", "_companyId");
        nameMap.put("#_type", "_type");
        valueMap.put(":_companyId", new AttributeValue().withS(id));
        valueMap.put(":_type", new AttributeValue().withS(Const.EMPLOYEE));
        String filterExpression = "#_type = :_type";

        if (name != null) {
            filterExpression = filterExpression.concat(" and contains (#_name, :_name)");
            nameMap.put("#_name", "_name");
            valueMap.put(":_name", new AttributeValue().withS(name));
        }

        if (email != null) {
            filterExpression = filterExpression.concat(" and contains (#_email, :_email)");
            nameMap.put("#_email", "_email");
            valueMap.put(":_email", new AttributeValue().withS(email));
        }

        if (isActive != null) {
            filterExpression = filterExpression.concat(" and #_isActive = :_isActive)");
            nameMap.put("#_isActive", "_isActive");
            valueMap.put(":_isActive", new AttributeValue().withBOOL(isActive));
        }

        DynamoDBQueryExpression<Employee> expression = new DynamoDBQueryExpression<Employee>()
                .withIndexName(table)
                .withKeyConditionExpression(keyExpression)
                .withFilterExpression(filterExpression)
                .withExpressionAttributeNames(nameMap)
                .withExpressionAttributeValues(valueMap)
                .withLimit(limit)
                .withConsistentRead(false);

        if (startKey.size() != 0) {
            expression.setExclusiveStartKey(startKey);
        }

        QueryResultPage<Employee> page = mapper.queryPage(Employee.class, expression);
        List<Employee> results = new ArrayList<>(page.getResults());
        int count = page.getCount();

        if (count < limit) {

            while (count < limit && page.getLastEvaluatedKey() != null) {
                expression = expression.withLimit(limit - page.getCount());
                expression = expression.withExclusiveStartKey(page.getLastEvaluatedKey());
                page = mapper.queryPage(Employee.class, expression);
                results.addAll(page.getResults());
                count += page.getCount();
                System.out.println(limit);
                System.out.println(count);
            }
            page.setResults(new ArrayList<>(results));
        }



        return page;
    }

    public List<Employee> batchSaveEmployees(List<Employee> employees) {
        mapper.batchSave(employees);
        return employees;
    }

    public Employee saveEmployee(Employee employee) {
        mapper.save(employee);
        return employee;
    }

    public void deleteEmployee(Employee employee) {
        mapper.delete(employee);
    }

    public void batchDeleteEmployee(List<Employee> employees) {
        mapper.batchDelete(employees);
    }

    /*
     **************** COMPANY **************
     */
    public Company getCompany(String id) {
        Company company = mapper.load(Company.class, id);
        System.out.println("IN DAO");
        if (company == null) {
            throw new CompanyNotFoundException(id);
        }
        return company;
    }

    public List<Company> getCompanies(List<String> ids) {
        List<Company> companies = new ArrayList<>();
        for (String s : ids) {
            Company timesheet = Company.builder().id(s).build();
            companies.add(timesheet);
        }

        Map<String, List<Object>> loadResult = mapper.batchLoad(companies);
        companies.clear();
        for (Map.Entry<String, List<Object>> entry : loadResult.entrySet()) {
            for (Object o : entry.getValue()) {
                if (o instanceof Company) {
                    companies.add((Company) o);
                }
            }
        }
        return companies;
    }

    public void saveCompany(Company company) {
        mapper.save(company);
    }

    public void deleteCompany(Company company) {
        mapper.delete(company);
    }
}
