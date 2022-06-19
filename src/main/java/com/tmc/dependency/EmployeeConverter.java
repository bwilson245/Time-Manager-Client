package com.tmc.dependency;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tmc.model.instance.EmployeeInstance;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EmployeeConverter implements DynamoDBTypeConverter<List<String>, List<EmployeeInstance>> {
    private ObjectMapper mapper;

    public EmployeeConverter() {
        this.mapper = new ObjectMapper();
    }

    @Override
    public List<String> convert(List<EmployeeInstance> object) {
        List<String> result = new ArrayList<>();
        try {
            for (EmployeeInstance instance : object) {
                String s = mapper.writeValueAsString(instance);
                result.add(s);
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return result;
    }
    @Override
    public List<EmployeeInstance> unconvert(List<String> object) {
        List<EmployeeInstance> result = new ArrayList<>();
        try {
            for (String str : object) {
                EmployeeInstance instance = mapper.readValue(str, EmployeeInstance.class);
                result.add(instance);
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return result;
    }
}
