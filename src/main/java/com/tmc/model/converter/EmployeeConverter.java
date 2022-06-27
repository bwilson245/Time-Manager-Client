package com.tmc.model.converter;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tmc.model.Employee;

public class EmployeeConverter  implements DynamoDBTypeConverter<String, Employee> {
    private ObjectMapper mapper;

    public EmployeeConverter() {
        this.mapper = new ObjectMapper();
    }

    @Override
    public String convert(Employee employee) {
        String result = "";
        try {
            result = mapper.writeValueAsString(employee);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return result;
    }
    @Override
    public Employee unconvert(String str) {
        Employee result = null;
        try {
            result = mapper.readValue(str, Employee.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return result;
    }
}
