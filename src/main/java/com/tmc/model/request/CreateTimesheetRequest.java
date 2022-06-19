package com.tmc.model.request;

import com.tmc.model.Customer;
import com.tmc.model.Location;
import com.tmc.model.instance.EmployeeInstance;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateTimesheetRequest {
    private String companyId;
    private Location location;
    private Customer customer;
    private Long date;
    private List<EmployeeInstance> employeeInstances;
    private Boolean isComplete;
    private String workOrderNumber;
    private String department;
    private String description;
    private String type;
}
