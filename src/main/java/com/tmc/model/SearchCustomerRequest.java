package com.tmc.model;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class SearchCustomerRequest {
    private String name;
    private String address;
    private String city;
    private String state;
    private String zip;
    private Boolean isActive;
    private Map<String, AttributeValue> startKey;
    private Integer limit;
}
