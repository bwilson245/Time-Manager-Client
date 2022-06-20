package com.tmc.model;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.tmc.model.instance.CustomerInstance;
import com.tmc.model.request.EditTimesheetRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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

    public Customer(Customer request) {
        this.location = new Location(request.getLocation());
        this.id = Optional.ofNullable(request.getId())
                .orElse("").toUpperCase();
        this.companyId = Optional.ofNullable(request.getName())
                .orElse("").toUpperCase();
        this.name = Optional.ofNullable(request.getName())
                .orElse("").toUpperCase();
        this.timesheetIds = Optional.ofNullable(request.getTimesheetIds())
                .orElse(new ArrayList<>());
        this.employeeIds = Optional.ofNullable(request.getEmployeeIds())
                .orElse(new ArrayList<>());
        this.isActive = Optional.ofNullable(request.getIsActive())
                .orElse(false);
        this.type = TypeEnum.CUSTOMER;
    }

    public Customer(Customer request, Customer original) {
        this.location = new Location(request.getLocation(), original.getLocation());
        this.id = Optional.ofNullable(request.getId())
                .orElse(original.getId()).toUpperCase();
        this.companyId = Optional.ofNullable(request.getName())
                .orElse(original.getName()).toUpperCase();
        this.name = Optional.ofNullable(request.getName())
                .orElse(original.getName()).toUpperCase();
        this.timesheetIds = Optional.ofNullable(request.getTimesheetIds())
                .orElse(original.getTimesheetIds());
        this.employeeIds = Optional.ofNullable(request.getEmployeeIds())
                .orElse(original.getEmployeeIds());
        this.isActive = Optional.ofNullable(request.getIsActive())
                .orElse(original.getIsActive());
        this.type = TypeEnum.CUSTOMER;
    }
}
