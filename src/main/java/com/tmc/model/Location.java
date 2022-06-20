package com.tmc.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument;
import com.tmc.model.request.EditTimesheetRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Optional;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@DynamoDBDocument
public class Location {
    private String address1;
    private String address2;
    private String city;
    private String state;
    private String zip;

    public Location(Location request) {
        this.address1 = Optional.ofNullable(request.getAddress1()).orElse("").toUpperCase();
        this.address2 = Optional.ofNullable(request.getAddress2()).orElse("").toUpperCase();
        this.city = Optional.ofNullable(request.getCity()).orElse("").toUpperCase();
        this.state = Optional.ofNullable(request.getState()).orElse("").toUpperCase();
        this.zip = Optional.ofNullable(request.getZip()).orElse("").toUpperCase();
    }

    public Location(Location request, Location original) {
        this.address1 = Optional.ofNullable(request.getAddress1()).orElse(original.getAddress1()).toUpperCase();
        this.address2 = Optional.ofNullable(request.getAddress2()).orElse(original.getAddress2()).toUpperCase();
        this.city = Optional.ofNullable(request.getCity()).orElse(original.getCity()).toUpperCase();
        this.state = Optional.ofNullable(request.getState()).orElse(original.getState()).toUpperCase();
        this.zip = Optional.ofNullable(request.getZip()).orElse(original.getZip()).toUpperCase();
    }
}
