package com.tmc.model;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.tmc.model.instance.CustomerInstance;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@DynamoDBTable(tableName = "Time-Manager")
public class Customer {

    @DynamoDBHashKey(attributeName = "id")
    private String id;

    @DynamoDBIndexHashKey(attributeName = "companyId", globalSecondaryIndexName = "companyId-type-index")
    @DynamoDBAttribute(attributeName = "companyId")
    private String companyId;

    @DynamoDBAttribute(attributeName = "name")
    private String name;

    @DynamoDBAttribute(attributeName = "location")
    private Location location;

    @Builder.Default
    @DynamoDBAttribute(attributeName = "timesheetIds")
    private List<String> timesheetIds = new ArrayList<>();

    @Builder.Default
    @DynamoDBAttribute(attributeName = "employeeIds")
    private List<String> employeeIds = new ArrayList<>();

    @Builder.Default
    @DynamoDBAttribute(attributeName = "isActive")
    private Boolean isActive = true;

    @Builder.Default
    @DynamoDBTypeConvertedEnum
    @DynamoDBIndexRangeKey(attributeName = "type", globalSecondaryIndexNames = {"companyId-type-index"})
    @DynamoDBAttribute(attributeName = "type")
    private TypeEnum type = TypeEnum.CUSTOMER;
}
