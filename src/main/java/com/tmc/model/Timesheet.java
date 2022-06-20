package com.tmc.model;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.google.googlejavaformat.Op;
import com.tmc.model.instance.EmployeeInstance;
import com.tmc.model.request.CreateTimesheetRequest;
import com.tmc.model.request.EditTimesheetRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@DynamoDBTable(tableName = "Time-Manager")
public class Timesheet {

    @DynamoDBHashKey(attributeName = "id")
    private String id;

    @DynamoDBIndexHashKey(attributeName = "companyId", globalSecondaryIndexName = "companyId-type-index")
    @DynamoDBAttribute(attributeName = "companyId")
    private String companyId;

    @DynamoDBIndexHashKey(attributeName = "customerId", globalSecondaryIndexName = "customerId-type-index")
    @DynamoDBAttribute(attributeName = "customerId")
    private String customerId;

    @DynamoDBAttribute(attributeName = "location")
    private Location location;

    @DynamoDBTyped(DynamoDBMapperFieldModel.DynamoDBAttributeType.M)
    @DynamoDBAttribute(attributeName = "customer")
    private Customer customer;

    @DynamoDBAttribute(attributeName = "_date")
    private Long date;

    @Builder.Default
    @DynamoDBAttribute(attributeName = "employeeInstances")
    private List<EmployeeInstance> employeeInstances = new ArrayList<>();

    @Builder.Default
    @DynamoDBAttribute(attributeName = "employeeIds")
    private List<String> employeeIds = new ArrayList<>();

    @Builder.Default
    @DynamoDBAttribute(attributeName = "isComplete")
    private Boolean isComplete = false;

    @DynamoDBAttribute(attributeName = "workOrderNumber")
    private String workOrderNumber;

    @DynamoDBAttribute(attributeName = "department")
    private String department;

    @DynamoDBAttribute(attributeName = "description")
    private String description;

    @DynamoDBAttribute(attributeName = "workType")
    private String workType;

    @Builder.Default
    @DynamoDBAttribute(attributeName = "validated")
    private Boolean isValidated = false;

    @Builder.Default
    @DynamoDBTypeConvertedEnum
    @DynamoDBIndexRangeKey(attributeName = "type", globalSecondaryIndexNames = {"companyId-type-index", "customerId-type-index"})
    @DynamoDBAttribute(attributeName = "type")
    private TypeEnum type = TypeEnum.TIMESHEET;

    public Timesheet(Timesheet request) {
        //****** Build Location object *******//
        this.location = new Location(request.getLocation());
        this.customer = new Customer(request.getCustomer());
        this.id = UUID.randomUUID().toString();
        this.companyId = request.getCompanyId();
        this.customerId = Optional.ofNullable(customer.getId()).orElse("");
        this.date = Optional.ofNullable(request.getDate()).orElse(new Date().getTime());
        this.employeeInstances = Optional.ofNullable(request.getEmployeeInstances()).orElse(new ArrayList<>());
        this.employeeIds = Optional.of(request.getEmployeeIds()).orElse(new ArrayList<>());
        this.isComplete = Optional.ofNullable(request.getIsComplete()).orElse(false);
        this.workOrderNumber = Optional.ofNullable(request.getWorkOrderNumber()).orElse("");
        this.department = Optional.ofNullable(request.getDepartment()).orElse("");
        this.description = Optional.ofNullable(request.getDescription()).orElse("");
        this.workType = Optional.ofNullable(request.getWorkType()).orElse("");
        this.isValidated = Optional.ofNullable(request.getIsValidated()).orElse(false);
        this.type = TypeEnum.TIMESHEET;
    }

    public Timesheet(Timesheet request, Timesheet original) {
        //****** Build Location object *******//
        this.location = new Location(request.getLocation(), original.getLocation());
        this.customer = new Customer(request.getCustomer(), original.getCustomer());
        this.id = request.getId();
        this.companyId = request.companyId;
        this.customerId = Optional.ofNullable(customer.getId()).orElse(original.getCustomerId());
        this.date = Optional.ofNullable(request.getDate()).orElse(original.getDate());
        this.employeeInstances = Optional.ofNullable(request.getEmployeeInstances()).orElse(original.getEmployeeInstances());
        this.employeeIds = Optional.ofNullable(request.getEmployeeIds()).orElse(original.getEmployeeIds());
        this.isComplete = Optional.ofNullable(request.getIsComplete()).orElse(original.getIsComplete());
        this.workOrderNumber = Optional.ofNullable(request.getWorkOrderNumber()).orElse(original.getWorkOrderNumber()).toUpperCase();
        this.department = Optional.ofNullable(request.getDepartment()).orElse(original.getDepartment()).toUpperCase();
        this.description = Optional.ofNullable(request.getDescription()).orElse(original.getDescription());
        this.workType = Optional.ofNullable(request.getWorkType()).orElse(original.getWorkType()).toUpperCase();
        this.isValidated = Optional.ofNullable(request.getIsValidated()).orElse(original.getIsValidated());
        this.type = TypeEnum.TIMESHEET;
    }
}
