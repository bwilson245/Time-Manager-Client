package com.tmc.model.request;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class SearchCustomerRequest {
    private String companyId;
    private String name;
    private String address1;
    private String address2;
    private String city;
    private String state;
    private String zip;
    @Builder.Default
    private Boolean isActive = true;
    private Map<String, AttributeValue> startKey;
    @Builder.Default
    private Integer limit = 10;
}
