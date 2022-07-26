package com.tmc.model;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@DynamoDBTable(tableName = Const.PRIMARY_TABLE)
public class Company {

    @DynamoDBHashKey(attributeName = "_id")
    private String id;

    @DynamoDBAttribute(attributeName = "_name")
    private String name;

    @DynamoDBAttribute(attributeName = "_location")
    private Location location;

    @Builder.Default
    @DynamoDBAttribute(attributeName = "_customerIds")
    private List<String> customerIds = new ArrayList<>();

    @Builder.Default
    @DynamoDBAttribute(attributeName = "_employeeIds")
    private List<String> employeeIds = new ArrayList<>();

    @Builder.Default
    @DynamoDBAttribute(attributeName = "_timesheetIds")
    private List<String> timesheetIds = new ArrayList<>();

    @Builder.Default
    @DynamoDBTyped(DynamoDBMapperFieldModel.DynamoDBAttributeType.BOOL)
    @DynamoDBAttribute(attributeName = "_isActive")
    private Boolean isActive = true;

    public Company(Company request) {
        this.id = Const.COMPANY_PREFIX + UUID.randomUUID();
        this.name = Optional.ofNullable(request.getName()).orElse("*").toUpperCase();
        this.location = new Location(request.getLocation());
        this.customerIds = Optional.ofNullable(request.getCustomerIds()).orElse(new ArrayList<>());
        this.employeeIds = Optional.ofNullable(request.getEmployeeIds()).orElse(new ArrayList<>());
        this.timesheetIds = Optional.ofNullable(request.getTimesheetIds()).orElse(new ArrayList<>());
        this.isActive = Optional.ofNullable(request.getIsActive()).orElse(false);
    }

    public Company(Company request, Company original) {
        this.id = original.getId();
        this.name = Optional.ofNullable(request.getName()).orElse(original.getName()).toUpperCase();
        this.location = new Location(request.getLocation(), original.getLocation());
        this.customerIds = Optional.ofNullable(request.getCustomerIds()).orElse(original.getCustomerIds());
        this.employeeIds = Optional.ofNullable(request.getEmployeeIds()).orElse(original.getEmployeeIds());
        this.timesheetIds = Optional.ofNullable(request.getTimesheetIds()).orElse(original.getTimesheetIds());
        this.isActive = Optional.ofNullable(request.getIsActive()).orElse(original.getIsActive());
    }
}
