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

    public Customer(EditTimesheetRequest request, Timesheet original) {
        this.location = Location.builder()
                .address1(Optional.ofNullable(request.getCustomer().getLocation().getAddress1())
                        .orElse(original.getCustomer().getLocation().getAddress1()).toUpperCase())
                .address2(Optional.ofNullable(request.getCustomer().getLocation().getAddress2())
                        .orElse(original.getCustomer().getLocation().getAddress2()).toUpperCase())
                .city(Optional.ofNullable(request.getCustomer().getLocation().getCity())
                        .orElse(original.getCustomer().getLocation().getCity()).toUpperCase())
                .state(Optional.ofNullable(request.getCustomer().getLocation().getState())
                        .orElse(original.getCustomer().getLocation().getState()).toUpperCase())
                .zip(Optional.ofNullable(request.getCustomer().getLocation().getZip())
                        .orElse(original.getCustomer().getLocation().getZip()).toUpperCase())
                .build();
        this.id = Optional.ofNullable(request.getCustomer().getId())
                .orElse(original.getCustomer().getId()).toUpperCase();
        this.companyId = Optional.ofNullable(request.getCustomer().getName())
                .orElse(original.getCustomer().getName()).toUpperCase();
        this.name = Optional.ofNullable(request.getCustomer().getName())
                .orElse(original.getCustomer().getName()).toUpperCase();
        this.timesheetIds = Optional.ofNullable(request.getCustomer().getTimesheetIds())
                .orElse(original.getCustomer().getTimesheetIds());
        this.employeeIds = Optional.ofNullable(request.getCustomer().getEmployeeIds())
                .orElse(original.getCustomer().getEmployeeIds());
        this.isActive = Optional.ofNullable(request.getCustomer().getIsActive())
                .orElse(original.getCustomer().getIsActive());
        this.type = TypeEnum.CUSTOMER;
    }
}
