package com.tmc.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CustomerInstance {
    private String id;
    private String companyId;
    private String name;
    private Location location;
    private String type;
}
