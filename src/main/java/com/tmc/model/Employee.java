package com.tmc.model;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    @DynamoDBAttribute(attributeName = "companyId")
    private String companyId;

    @DynamoDBAttribute(attributeName = "name")
    private String name;

    @DynamoDBIndexHashKey(attributeName = "email", globalSecondaryIndexName = EMAIL_INDEX)
    @DynamoDBAttribute(attributeName = "email")
    private String email;

    @DynamoDBAttribute(attributeName = "password")
    private String password;

    @DynamoDBAttribute(attributeName = "customerIds")
    private List<String> customerIds;
}
