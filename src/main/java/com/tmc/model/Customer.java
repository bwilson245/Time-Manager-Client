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
public class Customer {

    @DynamoDBIndexRangeKey(attributeName = "_id", globalSecondaryIndexName = Const.COMPANY_ID_ID_INDEX_GSI)
    @DynamoDBHashKey(attributeName = "_id")
    private String id;

    @DynamoDBIndexHashKey(attributeName = "_companyId", globalSecondaryIndexName = Const.COMPANY_ID_ID_INDEX_GSI)
    @DynamoDBAttribute(attributeName = "_companyId")
    private String companyId;

    @DynamoDBAttribute(attributeName = "_name")
    private String name;

    @DynamoDBAttribute(attributeName = "_location")
    private Location location;

    @Builder.Default
    @DynamoDBAttribute(attributeName = "_timesheetIds")
    private List<String> timesheetIds = new ArrayList<>();

    @Builder.Default
    @DynamoDBAttribute(attributeName = "_employeeIds")
    private List<String> employeeIds = new ArrayList<>();

    @Builder.Default
    @DynamoDBTyped(DynamoDBMapperFieldModel.DynamoDBAttributeType.BOOL)
    @DynamoDBAttribute(attributeName = "_isActive")
    private Boolean isActive = true;


    public Customer(Customer request) {
        this.location = new Location(Optional.ofNullable(request.getLocation()).orElse(new Location()));
        this.id = Optional.ofNullable(request.getId()).orElse("customer." + UUID.randomUUID());
        this.companyId = Optional.ofNullable(request.getCompanyId()).orElse("*");
        this.name = Optional.ofNullable(request.getName()).orElse("*").toUpperCase();
        this.timesheetIds = Optional.ofNullable(request.getTimesheetIds()).orElse(new ArrayList<>());
        this.employeeIds = Optional.ofNullable(request.getEmployeeIds()).orElse(new ArrayList<>());
        this.isActive = Optional.ofNullable(request.getIsActive()).orElse(false);
    }

    public Customer(Customer request, Customer original) {
        this.location = new Location(request.getLocation(), original.getLocation());
        this.id = Optional.ofNullable(request.getId()).orElse(original.getId());
        this.companyId = Optional.ofNullable(request.getName()).orElse(original.getName());
        this.name = Optional.ofNullable(request.getName()).orElse(original.getName()).toUpperCase();
        this.timesheetIds = Optional.ofNullable(request.getTimesheetIds()).orElse(original.getTimesheetIds());
        this.employeeIds = Optional.ofNullable(request.getEmployeeIds()).orElse(original.getEmployeeIds());
        this.isActive = Optional.ofNullable(request.getIsActive()).orElse(original.getIsActive());
    }
}
