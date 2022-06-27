package com.tmc.model.converter;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tmc.model.Timesheet;

public class ObjectConverter implements DynamoDBTypeConverter<String, Object> {
    private ObjectMapper mapper;

    public ObjectConverter() {
        this.mapper = new ObjectMapper();
    }

    @Override
    public String convert(Object obj) {
        String result = "";
        try {
            result = mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return result;
    }
    @Override
    public Object unconvert(String str) {
        Object result = null;
        try {
            result = mapper.readValue(str, Object.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return result;
    }
}
