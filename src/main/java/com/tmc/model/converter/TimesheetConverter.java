package com.tmc.model.converter;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tmc.model.Company;
import com.tmc.model.Timesheet;

public class TimesheetConverter implements DynamoDBTypeConverter<String, Timesheet> {
    private ObjectMapper mapper;

    public TimesheetConverter() {
        this.mapper = new ObjectMapper();
    }

    @Override
    public String convert(Timesheet timesheet) {
        String result = "";
        try {
            result = mapper.writeValueAsString(timesheet);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return result;
    }
    @Override
    public Timesheet unconvert(String str) {
        Timesheet result = null;
        try {
            result = mapper.readValue(str, Timesheet.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return result;
    }
}
