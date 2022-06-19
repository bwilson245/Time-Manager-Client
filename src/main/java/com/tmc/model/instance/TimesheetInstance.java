package com.tmc.model.instance;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument;
import com.tmc.model.Location;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TimesheetInstance {
    private String customerName;
    private Location customerLocation;
    private Location workLocation;
    private Long date;
    private List<EmployeeInstance> employeeInstances;
    private Boolean isComplete;
    private String workOrderNumber;
    private String department;
    private String description;
    private String type;
}
