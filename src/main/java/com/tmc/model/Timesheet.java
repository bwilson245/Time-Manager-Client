package com.tmc.model;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@DynamoDBTable(tableName = Const.PRIMARY_TABLE)
public class Timesheet {

    @DynamoDBHashKey(attributeName = "_id")
    private String id;

    @DynamoDBIndexHashKey(attributeName = "_companyId", globalSecondaryIndexName = Const.COMPANY_ID_INDEX_GSI)
    @DynamoDBAttribute(attributeName = "_companyId")
    private String companyId;

    @DynamoDBAttribute(attributeName = "_customerId")
    private String customerId;

    @DynamoDBAttribute(attributeName = "_location")
    private Location location;

    @DynamoDBTyped(DynamoDBMapperFieldModel.DynamoDBAttributeType.M)
    @DynamoDBAttribute(attributeName = "_customer")
    private Customer customer;

    @DynamoDBAttribute(attributeName = "_date")
    private Long date;

    @Builder.Default
    @DynamoDBAttribute(attributeName = "_employeeInstances")
    private List<EmployeeInstance> employeeInstances = new ArrayList<>();

    @Builder.Default
    @DynamoDBAttribute(attributeName = "_employeeIds")
    private List<String> employeeIds = new ArrayList<>();

    @Builder.Default
    @DynamoDBAttribute(attributeName = "_isComplete")
    private Boolean isComplete = false;

    @DynamoDBAttribute(attributeName = "_workOrderNumber")
    private String workOrderNumber;

    @DynamoDBAttribute(attributeName = "_department")
    private String department;

    @DynamoDBAttribute(attributeName = "_description")
    private String description;

    @DynamoDBAttribute(attributeName = "_workType")
    private String workType;

    @Builder.Default
    @DynamoDBAttribute(attributeName = "_validated")
    private Boolean isValidated = false;

    @Builder.Default
    @DynamoDBAttribute(attributeName = "_type")
    private String type = Const.TIMESHEET;

    public Timesheet(Timesheet request) {
        //****** Build Timesheet object *******//
        this.location = Optional.ofNullable(request.getLocation()).orElse(new Location());
        this.customer = new Customer(request.getCustomer());
        this.id = "timesheet." + UUID.randomUUID();
        this.companyId = request.getCompanyId();
        this.customerId = Optional.ofNullable(customer.getId()).orElse("");
        this.date = Optional.ofNullable(request.getDate()).orElse(new Date().getTime());
        this.employeeInstances = Optional.ofNullable(request.getEmployeeInstances()).orElse(new ArrayList<>());
        this.employeeIds = Optional.of(request.getEmployeeIds()).orElse(new ArrayList<>());
        this.isComplete = Optional.ofNullable(request.getIsComplete()).orElse(false);
        this.workOrderNumber = Optional.ofNullable(request.getWorkOrderNumber()).orElse("").toUpperCase();
        this.department = Optional.ofNullable(request.getDepartment()).orElse("").toUpperCase();
        this.description = Optional.ofNullable(request.getDescription()).orElse("");
        this.workType = Optional.ofNullable(request.getWorkType()).orElse("").toUpperCase();
        this.isValidated = Optional.ofNullable(request.getIsValidated()).orElse(false);
        this.type = Const.TIMESHEET;
    }

    public Timesheet(Timesheet request, Timesheet original) {
        //****** Build Timesheet object *******//
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
        this.type = Const.TIMESHEET;
    }
}
