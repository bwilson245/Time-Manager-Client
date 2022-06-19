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
public class Company {

    @DynamoDBHashKey(attributeName = "id")
    private String id;

    @DynamoDBAttribute(attributeName = "name")
    private String name;

    @DynamoDBAttribute(attributeName = "location")
    private Location location;

    @Builder.Default
    @DynamoDBAttribute(attributeName = "customerIds")
    private List<String> customerIds = new ArrayList<>();

    @Builder.Default
    @DynamoDBAttribute(attributeName = "employeeIds")
    private List<String> employeeIds = new ArrayList<>();

    @Builder.Default
    @DynamoDBAttribute(attributeName = "timesheetIds")
    private List<String> timesheetIds = new ArrayList<>();

    @Builder.Default
    @DynamoDBAttribute(attributeName = "isActive")
    private Boolean isActive = true;

    @Builder.Default
    @DynamoDBTypeConvertedEnum
    @DynamoDBAttribute(attributeName = "type")
    private TypeEnum type = TypeEnum.COMPANY;
}
