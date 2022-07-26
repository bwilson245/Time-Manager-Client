package com.tmc.model.request;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class SearchEmployeeRequest {
    private String companyId;
    private String name;
    private String email;
    @Builder.Default
    private Boolean isActive = true;
    private Map<String, AttributeValue> startKey;
    @Builder.Default
    private Integer limit = 10;
}
