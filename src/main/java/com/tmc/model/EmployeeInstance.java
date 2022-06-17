package com.tmc.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@DynamoDBDocument
public class EmployeeInstance {
    private String id;
    private String name;
    private Double straightTime;
    private Double overTime;
    private Double doubleTime;
    private Double driveTime;
}
