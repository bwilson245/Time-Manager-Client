package com.tmc.model.converter;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tmc.model.Company;
import com.tmc.model.Employee;

public class CompanyConverter implements DynamoDBTypeConverter<String, Company> {
    private ObjectMapper mapper;

    public CompanyConverter() {
        this.mapper = new ObjectMapper();
    }

    @Override
    public String convert(Company company) {
        String result = "";
        try {
            result = mapper.writeValueAsString(company);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return result;
    }
    @Override
    public Company unconvert(String str) {
        Company result = null;
        try {
            result = mapper.readValue(str, Company.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return result;
    }
}
