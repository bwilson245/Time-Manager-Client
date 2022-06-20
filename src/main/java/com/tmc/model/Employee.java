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
@DynamoDBTable(tableName = "Time-Manager")
public class Employee {
    public static final String EMAIL_INDEX = "email-index";

    @DynamoDBHashKey(attributeName = "id")
    private String id;

    @DynamoDBIndexHashKey(attributeName = "companyId", globalSecondaryIndexName = "companyId-type-index")
    @DynamoDBAttribute(attributeName = "companyId")
    private String companyId;

    @DynamoDBAttribute(attributeName = "name")
    private String name;

    @DynamoDBIndexHashKey(attributeName = "email", globalSecondaryIndexName = EMAIL_INDEX)
    @DynamoDBAttribute(attributeName = "email")
    private String email;

    @DynamoDBAttribute(attributeName = "password")
    private String password;

    @Builder.Default
    @DynamoDBAttribute(attributeName = "customerIds")
    private List<String> customerIds = new ArrayList<>();

    @Builder.Default
    @DynamoDBAttribute(attributeName = "timesheetIds")
    private List<String> timesheetIds = new ArrayList<>();

    @Builder.Default
    @DynamoDBAttribute(attributeName = "isActive")
    private Boolean isActive = true;

    @Builder.Default
    @DynamoDBTypeConvertedEnum
    @DynamoDBIndexRangeKey(attributeName = "type", globalSecondaryIndexNames = {"companyId-type-index"})
    @DynamoDBAttribute(attributeName = "type")
    private TypeEnum type = TypeEnum.EMPLOYEE;

    public Employee(Employee request) {
        this.id = Optional.ofNullable(request.getId()).orElse(UUID.randomUUID().toString());
        this.companyId = Optional.ofNullable(request.getCompanyId()).orElse("");
        this.name = Optional.ofNullable(request.getName()).orElse("").toUpperCase();
        this.email = Optional.ofNullable(request.getEmail()).orElse("");
        this.password = Optional.ofNullable(request.getPassword()).orElse("");
        this.customerIds = Optional.ofNullable(request.getCustomerIds()).orElse(new ArrayList<>());
        this.timesheetIds = Optional.ofNullable(request.getTimesheetIds()).orElse(new ArrayList<>());
        this.isActive = Optional.ofNullable(request.getIsActive()).orElse(false);
        this.type = TypeEnum.EMPLOYEE;
    }

    public Employee(Employee request, Employee original) {
        this.id = Optional.ofNullable(request.getId()).orElse(original.getId());
        this.companyId = Optional.ofNullable(request.getCompanyId()).orElse(original.getCompanyId());
        this.name = Optional.ofNullable(request.getName()).orElse(original.getName()).toUpperCase();
        this.email = Optional.ofNullable(request.getEmail()).orElse(original.getEmail());
        this.password = Optional.ofNullable(request.getPassword()).orElse(original.getPassword());
        this.customerIds = Optional.ofNullable(request.getCustomerIds()).orElse(original.getCustomerIds());
        this.timesheetIds = Optional.ofNullable(request.getTimesheetIds()).orElse(original.getTimesheetIds());
        this.isActive = Optional.ofNullable(request.getIsActive()).orElse(original.isActive);
        this.type = TypeEnum.EMPLOYEE;
    }
}
