package com.tmc.model.converter;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tmc.model.Company;
import com.tmc.model.Customer;

public class CustomerConverter implements DynamoDBTypeConverter<String, Customer> {
    private ObjectMapper mapper;

    public CustomerConverter() {
        this.mapper = new ObjectMapper();
    }

    @Override
    public String convert(Customer customer) {
        String result = "";
        try {
            result = mapper.writeValueAsString(customer);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return result;
    }
    @Override
    public Customer unconvert(String str) {
        Customer result = null;
        try {
            result = mapper.readValue(str, Customer.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return result;
    }
}
