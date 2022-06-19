package com.tmc.model;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

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
}
