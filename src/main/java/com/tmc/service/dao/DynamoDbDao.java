package com.tmc.service.dao;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.QueryResultPage;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.tmc.exception.CompanyNotFoundException;
import com.tmc.exception.CustomerNotFoundException;
import com.tmc.exception.EmployeeNotFoundException;
import com.tmc.exception.TimesheetNotFoundException;
import com.tmc.model.*;
import com.tmc.model.request.SearchCustomerRequest;
import com.tmc.model.request.SearchEmployeeRequest;
import com.tmc.model.request.SearchTimesheetRequest;
import lombok.Data;

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

    public QueryResultPage<Timesheet> search(String companyId, SearchTimesheetRequest request) {

        if (request.getLimit() == null || request.getLimit() < 1) {
            request.setLimit(10);
        }

        Map<String, String> nameMap = new HashMap<>();
        Map<String, AttributeValue> valueMap = new HashMap<>();

        nameMap.put("#_companyId", "_companyId");
        nameMap.put("#_id", "_id");
        valueMap.put(":_companyId", new AttributeValue().withS(companyId));
        valueMap.put(":_search", new AttributeValue().withS("timesheet"));


        String table = Const.COMPANY_ID_ID_INDEX_GSI;
        String keyExpression = "#_companyId = :_companyId and begins_with(#_id, :_search)";
        String filterExpression = "";



        if (request.getWorkType() != null) {
            if (filterExpression.length() > 0) {
                filterExpression = filterExpression.concat(" and ");
            }
            filterExpression = filterExpression.concat("contains(#_workType, :_workType");
            nameMap.put("#_workType", "_workType");
            valueMap.put(":_workType", new AttributeValue().withS(request.getWorkType()));
        }

        if (request.getDepartment() != null) {
            if (filterExpression.length() > 0) {
                filterExpression = filterExpression.concat(" and ");
            }
            filterExpression = filterExpression.concat("contains(#_department, :_department");
            nameMap.put("#_department", "_department");
            valueMap.put(":_department", new AttributeValue().withS(request.getDepartment()));
        }

        if (request.getWorkOrderNumber() != null) {
            if (filterExpression.length() > 0) {
                filterExpression = filterExpression.concat(" and ");
            }
            filterExpression = filterExpression.concat("contains(#_workOrderNumber), :_workOrderNumber");
            nameMap.put("#_workOrderNumber", "_workOrderNumber");
            valueMap.put(":_workOrderNumber", new AttributeValue().withS(request.getWorkOrderNumber()));
        }

        if (request.getIsComplete() != null) {
            if (filterExpression.length() > 0) {
                filterExpression = filterExpression.concat(" and ");
            }
            filterExpression = filterExpression.concat("#_isComplete = :_isComplete");
            nameMap.put("#_isComplete", "_isComplete");
            valueMap.put(":_isComplete", new AttributeValue().withBOOL(request.getIsComplete()));
        }

        if (request.getAddress1() != null || request.getAddress2() != null || request.getCity() != null || request.getState() != null || request.getZip() != null) {
            nameMap.put("#_location", "_location");
            if (request.getAddress1() != null) {
                if (filterExpression.length() > 0) {
                    filterExpression = filterExpression.concat(" and ");
                }
                valueMap.put(":_address1", new AttributeValue().withS(request.getAddress1()));
                filterExpression = filterExpression.concat("contains(#_location.address1, :_address1");
            }
            if (request.getAddress2() != null) {
                if (filterExpression.length() > 0) {
                    filterExpression = filterExpression.concat(" and ");
                }
                valueMap.put(":_address2", new AttributeValue().withS(request.getAddress2()));
                filterExpression = filterExpression.concat("contains(#_location.address2, :_address2");
            }
            if (request.getCity() != null) {
                if (filterExpression.length() > 0) {
                    filterExpression = filterExpression.concat(" and ");
                }
                valueMap.put(":_city", new AttributeValue().withS(request.getCity()));
                filterExpression = filterExpression.concat("contains(#_location.city, :_city");
            }
            if (request.getState() != null) {
                if (filterExpression.length() > 0) {
                    filterExpression = filterExpression.concat(" and ");
                }
                valueMap.put(":_state", new AttributeValue().withS(request.getState()));
                filterExpression = filterExpression.concat("contains(#_location.state, :_state");
            }
            if (request.getZip() != null) {
                if (filterExpression.length() > 0) {
                    filterExpression = filterExpression.concat(" and ");
                }
                valueMap.put(":_zip", new AttributeValue().withS(request.getZip()));
                filterExpression = filterExpression.concat("contains(#_location.zip, :_zip");
            }
        }

        if (request.getIsValidated() != null) {
            if (filterExpression.length() > 0) {
                filterExpression = filterExpression.concat(" and ");
            }
            filterExpression = filterExpression.concat("#_isValidated = :_isValidated");
            nameMap.put("#_isValidated", "_isValidated");
            valueMap.put(":_isValidated", new AttributeValue().withBOOL(request.getIsValidated()));
        }

        if (request.getBefore() != null || request.getAfter() != null) {
            if (filterExpression.length() > 0) {
                filterExpression = filterExpression.concat(" and ");
            }
            if (request.getBefore() != null && request.getAfter() != null) {
                nameMap.put("#_date", "_date");
                valueMap.put(":_before", new AttributeValue().withN(String.valueOf(request.getBefore())));
                valueMap.put(":_after", new AttributeValue().withN(String.valueOf(request.getAfter())));
                filterExpression = filterExpression.concat("#_date between :_before and :_after");
            } else if (request.getBefore() != null) {
                nameMap.put("#_date", "_date");
                valueMap.put(":_before", new AttributeValue().withN(String.valueOf(request.getBefore())));
                filterExpression = filterExpression.concat("#_date <= :_before");
            } else {
                nameMap.put("#_date", "_date");
                valueMap.put(":_after", new AttributeValue().withN(String.valueOf(request.getAfter())));
                filterExpression = filterExpression.concat("#_date >= :_after");
            }
        }

        DynamoDBQueryExpression<Timesheet> expression = new DynamoDBQueryExpression<Timesheet>()
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


    public QueryResultPage<Customer> search(String companyId, SearchCustomerRequest request) {
        if (request.getLimit() == null || request.getLimit() < 1) {
            request.setLimit(10);
        }

        Map<String, String> nameMap = new HashMap<>();
        Map<String, AttributeValue> valueMap = new HashMap<>();

        nameMap.put("#_companyId", "_companyId");
        nameMap.put("#_id", "_id");
        valueMap.put(":_companyId", new AttributeValue().withS(companyId));
        valueMap.put(":_search", new AttributeValue().withS("customer"));


        String table = Const.COMPANY_ID_ID_INDEX_GSI;
        String keyExpression = "#_companyId = :_companyId and begins_with(#_id, :_search)";
        String filterExpression = "";


        if (request.getName() != null) {
            if (filterExpression.length() > 0) {
                filterExpression = filterExpression.concat(" and ");
            }
            nameMap.put("#_name", "_name");
            valueMap.put(":_name", new AttributeValue().withS(request.getName()));
            filterExpression = filterExpression.concat("contains (#_name, :_name)");

        }

        if (request.getAddress1() != null || request.getAddress2() != null || request.getCity() != null || request.getState() != null || request.getZip() != null) {
            nameMap.put("#_location", "_location");
            if (request.getAddress1() != null) {
                if (filterExpression.length() > 0) {
                    filterExpression = filterExpression.concat(" and ");
                }
                valueMap.put(":_address1", new AttributeValue().withS(request.getAddress1()));
                filterExpression = filterExpression.concat("contains(#_location.address1, :_address1");
            }
            if (request.getAddress2() != null) {
                if (filterExpression.length() > 0) {
                    filterExpression = filterExpression.concat(" and ");
                }
                valueMap.put(":_address2", new AttributeValue().withS(request.getAddress2()));
                filterExpression = filterExpression.concat("contains(#_location.address2, :_address2");
            }
            if (request.getCity() != null) {
                if (filterExpression.length() > 0) {
                    filterExpression = filterExpression.concat(" and ");
                }
                valueMap.put(":_city", new AttributeValue().withS(request.getCity()));
                filterExpression = filterExpression.concat("contains(#_location.city, :_city");
            }
            if (request.getState() != null) {
                if (filterExpression.length() > 0) {
                    filterExpression = filterExpression.concat(" and ");
                }
                valueMap.put(":_state", new AttributeValue().withS(request.getState()));
                filterExpression = filterExpression.concat("contains(#_location.state, :_state");
            }
            if (request.getZip() != null) {
                if (filterExpression.length() > 0) {
                    filterExpression = filterExpression.concat(" and ");
                }
                valueMap.put(":_zip", new AttributeValue().withS(request.getZip()));
                filterExpression = filterExpression.concat("contains(#_location.zip, :_zip");
            }
        }

        if (request.getIsActive() != null) {
            nameMap.put("#_isActive", "_isActive");
            valueMap.put(":_isActive", new AttributeValue().withBOOL(request.getIsActive()));
            if (filterExpression.length() > 0) {
                filterExpression = filterExpression.concat(" and ");
            }
            filterExpression = filterExpression.concat("#_isActive = :_isActive");
        }

        DynamoDBQueryExpression<Customer> expression = new DynamoDBQueryExpression<Customer>()
                .withIndexName(table)
                .withKeyConditionExpression(keyExpression)
                .withFilterExpression(filterExpression)
                .withExpressionAttributeNames(nameMap)
                .withExpressionAttributeValues(valueMap)
                .withLimit(request.getLimit())
                .withConsistentRead(false);

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

    public QueryResultPage<Employee> search(String companyId, SearchEmployeeRequest request) {
        if (request.getLimit() == null || request.getLimit() < 1) {
            request.setLimit(10);
        }

        System.out.println(request);

        Map<String, String> nameMap = new HashMap<>();
        Map<String, AttributeValue> valueMap = new HashMap<>();

        nameMap.put("#_companyId", "_companyId");
        nameMap.put("#_id", "_id");
        valueMap.put(":_companyId", new AttributeValue().withS(companyId));
        valueMap.put(":_search", new AttributeValue().withS("employee"));


        String table = Const.COMPANY_ID_ID_INDEX_GSI;
        String keyExpression = "#_companyId = :_companyId and begins_with(#_id, :_search)";
        String filterExpression = "";


        if (request.getName() != null) {
            request.setName(request.getName().toUpperCase());
            nameMap.put("#_name", "_name");
            valueMap.put(":_name", new AttributeValue().withS(request.getName()));
            if (filterExpression.length() > 0) {
                filterExpression = filterExpression.concat(" and ");
            }
            filterExpression = filterExpression.concat("contains(#_name, :_name)");
        }

        if (request.getEmail() != null) {
            request.setEmail(request.getEmail().toLowerCase());
            nameMap.put("#_email", "_email");
            valueMap.put(":_email", new AttributeValue().withS(request.getEmail()));
            if (filterExpression.length() > 0) {
                filterExpression = filterExpression.concat(" and ");
            }
            filterExpression = filterExpression.concat("contains(#_email, :_email)");
        }


        if (request.getIsActive() != null) {
            nameMap.put("#_isActive", "_isActive");
            valueMap.put(":_isActive", new AttributeValue().withBOOL(request.getIsActive()));
            if (filterExpression.length() > 0) {
                filterExpression = filterExpression.concat(" and ");
            }
            filterExpression = filterExpression.concat("#_isActive = :_isActive");
        }

        DynamoDBQueryExpression<Employee> expression = new DynamoDBQueryExpression<Employee>()
                .withIndexName(table)
                .withKeyConditionExpression(keyExpression)
                .withExpressionAttributeNames(nameMap)
                .withExpressionAttributeValues(valueMap)
                .withLimit(request.getLimit())
                .withConsistentRead(false);

        if (request.getStartKey() != null) {
            expression.setExclusiveStartKey(request.getStartKey());
        }
        if (filterExpression.length() > 0) {
            expression.setFilterExpression(filterExpression);
        }

        long now = System.currentTimeMillis();
        System.out.println("Request to database: " + now);
        QueryResultPage<Employee> page = mapper.queryPage(Employee.class, expression);

        List<Employee> results = new ArrayList<>(page.getResults());
        int count = page.getCount();

        if (count < request.getLimit()) {

            while (count < request.getLimit() && page.getLastEvaluatedKey() != null) {
                expression = expression.withLimit(request.getLimit() - page.getCount());
                expression = expression.withExclusiveStartKey(page.getLastEvaluatedKey());
                page = mapper.queryPage(Employee.class, expression);
                results.addAll(page.getResults());
                count += page.getCount();
            }
            page.setResults(new ArrayList<>(results));
        }
        System.out.println("Response from database: " + System.currentTimeMillis());
        System.out.println("Total time: " + (System.currentTimeMillis() - now));
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
