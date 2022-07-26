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
public class Employee {

    @DynamoDBIndexRangeKey(attributeName = "_id", globalSecondaryIndexName = Const.COMPANY_ID_ID_INDEX_GSI)
    @DynamoDBHashKey(attributeName = "_id")
    private String id;

    @DynamoDBIndexHashKey(attributeName = "_companyId", globalSecondaryIndexName = Const.COMPANY_ID_ID_INDEX_GSI)
    @DynamoDBAttribute(attributeName = "_companyId")
    private String companyId;

    @DynamoDBAttribute(attributeName = "_name")
    private String name;

    @DynamoDBAttribute(attributeName = "_email")
    private String email;

    @DynamoDBAttribute(attributeName = "_password")
    private String password;

    @Builder.Default
    @DynamoDBAttribute(attributeName = "_customerIds")
    private List<String> customerIds = new ArrayList<>();

    @Builder.Default
    @DynamoDBAttribute(attributeName = "_timesheetIds")
    private List<String> timesheetIds = new ArrayList<>();

    @Builder.Default
    @DynamoDBTyped(DynamoDBMapperFieldModel.DynamoDBAttributeType.BOOL)
    @DynamoDBAttribute(attributeName = "_isActive")
    private Boolean isActive = true;


    public Employee(Employee request) {
        this.id = "employee." + UUID.randomUUID();
        this.companyId = Optional.ofNullable(request.getCompanyId()).orElse("*");
        this.name = Optional.ofNullable(request.getName()).orElse("*").toUpperCase();
        this.email = Optional.ofNullable(request.getEmail()).orElse("*").toLowerCase();
        this.password = Optional.ofNullable(request.getPassword()).orElse("*");
        this.customerIds = Optional.ofNullable(request.getCustomerIds()).orElse(new ArrayList<>());
        this.timesheetIds = Optional.ofNullable(request.getTimesheetIds()).orElse(new ArrayList<>());
        this.isActive = Optional.ofNullable(request.getIsActive()).orElse(false);
    }

    public Employee(Employee request, Employee original) {
        this.id = Optional.ofNullable(request.getId()).orElse(original.getId());
        this.companyId = Optional.ofNullable(request.getCompanyId()).orElse(original.getCompanyId());
        this.name = Optional.ofNullable(request.getName()).orElse(original.getName()).toUpperCase();
        this.email = Optional.ofNullable(request.getEmail()).orElse(original.getEmail()).toLowerCase();
        this.password = Optional.ofNullable(request.getPassword()).orElse(original.getPassword());
        this.customerIds = Optional.ofNullable(request.getCustomerIds()).orElse(original.getCustomerIds());
        this.timesheetIds = Optional.ofNullable(request.getTimesheetIds()).orElse(original.getTimesheetIds());
        this.isActive = Optional.ofNullable(request.getIsActive()).orElse(original.getIsActive());
    }
}
