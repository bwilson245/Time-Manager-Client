package com.tmc.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EditCompanyRequest {
    private String name;
    private String address1;
    private String address2;
    private String city;
    private String state;
    private String zip;
}
