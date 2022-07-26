package com.tmc.model.request;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class SearchTimesheetRequest {
    private String companyId;
    private String workType;
    private String department;
    private String workOrderNumber;
    private String description;
    private String address1;
    private String address2;
    private String city;
    private String state;
    private String zip;
    private Long before;
    private Long after;
    @Builder.Default
    private Boolean isValidated = false;
    @Builder.Default
    private Boolean isComplete = false;
    private Map<String, AttributeValue> startKey;
    @Builder.Default
    private Integer limit = 10;
}
