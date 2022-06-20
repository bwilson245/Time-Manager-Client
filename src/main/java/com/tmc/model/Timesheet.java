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

    public Timesheet(CreateTimesheetRequest request) {
        //****** Retrieve the Company object *******//
        Location location = Location.builder()
                .address1(Optional.ofNullable(request.getLocation().getAddress1()).orElse(""))
                .address2(Optional.ofNullable(request.getLocation().getAddress2()).orElse(""))
                .city(Optional.ofNullable(request.getLocation().getCity()).orElse(""))
                .state(Optional.ofNullable(request.getLocation().getState()).orElse(""))
                .zip(Optional.ofNullable(request.getLocation().getZip()).orElse(""))
                .build();
        this.id = UUID.randomUUID().toString();
        this.companyId = request.getCompanyId();
        this.customer = Optional.ofNullable(request.getCustomer()).orElse(new Customer());
        this.customerId = Optional.ofNullable(request.getCustomer().getId()).orElse("");
        this.location = location;
        this.date = Optional.ofNullable(request.getDate()).orElse(new Date().getTime());
        this.employeeInstances = Optional.ofNullable(request.getEmployeeInstances()).orElse(new ArrayList<>());
        this.isComplete = Optional.ofNullable(request.getIsComplete()).orElse(false);
        this.workOrderNumber = Optional.ofNullable(request.getWorkOrderNumber()).orElse("");
        this.department = Optional.ofNullable(request.getDepartment()).orElse("");
        this.description = Optional.ofNullable(request.getDescription()).orElse("");
        this.workType = Optional.ofNullable(request.getWorkType()).orElse("");
        this.type = TypeEnum.TIMESHEET;
    }

    public Timesheet(EditTimesheetRequest request, Timesheet original) {
        //****** Build Location object *******//
        Location location = Location.builder()
                .address1(Optional.ofNullable(request.getLocation().getAddress1()).orElse("").toUpperCase())
                .address2(Optional.ofNullable(request.getLocation().getAddress2()).orElse("").toUpperCase())
                .city(Optional.ofNullable(request.getLocation().getCity()).orElse("").toUpperCase())
                .state(Optional.ofNullable(request.getLocation().getState()).orElse("").toUpperCase())
                .zip(Optional.ofNullable(request.getLocation().getZip()).orElse("").toUpperCase())
                .build();
        this.location = location;
        this.customer = new Customer(request, original);
        this.date = Optional.ofNullable(request.getDate()).orElse(original.getDate());
        this.employeeInstances = Optional.ofNullable(request.getEmployeeInstances()).orElse(original.getEmployeeInstances());
        this.isComplete = Optional.ofNullable(request.getIsComplete()).orElse(original.getIsComplete());
        this.workOrderNumber = Optional.ofNullable(request.getWorkOrderNumber()).orElse(original.getWorkOrderNumber()).toUpperCase();
        this.department = Optional.ofNullable(request.getDepartment()).orElse(original.getDepartment()).toUpperCase();
        this.description = Optional.ofNullable(request.getDescription()).orElse(original.getDescription());
        this.workType = Optional.ofNullable(request.getWorkType()).orElse(original.getWorkType()).toUpperCase();
        this.isValidated = Optional.ofNullable(request.getIsValidated()).orElse(original.getIsValidated());
        this.type = TypeEnum.TIMESHEET;
    }
}
