package com.tmc.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@DynamoDBTable(tableName = "Time-Manager")
public class Company {

    @DynamoDBHashKey(attributeName = "id")
    private String id;

    @DynamoDBAttribute(attributeName = "name")
    private String name;

    @DynamoDBAttribute(attributeName = "location")
    private Location location;

    @DynamoDBAttribute(attributeName = "customerIds")
    private List<String> customerIds;

    @DynamoDBAttribute(attributeName = "employeeIds")
    private List<String> employeeIds;

    @DynamoDBAttribute(attributeName = "timesheetIds")
    private List<String> timesheetIds;

    @DynamoDBAttribute(attributeName = "isActive")
    private Boolean isActive;
}
