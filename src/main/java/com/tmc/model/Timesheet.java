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
public class Timesheet {

    @DynamoDBHashKey(attributeName = "id")
    private String id;

    @DynamoDBAttribute(attributeName = "customer")
    private Customer customer;

    @DynamoDBAttribute(attributeName = "_date")
    private Long date;

    @DynamoDBAttribute(attributeName = "employeeInstances")
    private List<EmployeeInstance> employeeInstances;

    @DynamoDBAttribute(attributeName = "isComplete")
    private Boolean isComplete;

    @DynamoDBAttribute(attributeName = "workOrderNumber")
    private String workOrderNumber;

    @DynamoDBAttribute(attributeName = "department")
    private String department;

    @DynamoDBAttribute(attributeName = "description")
    private String description;

    @DynamoDBTypeConvertedEnum
    @DynamoDBAttribute(attributeName = "type")
    private TypeEnum type;
}
