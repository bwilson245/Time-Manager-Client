package com.tmc.service;

import lombok.Data;

import javax.inject.Inject;
import javax.inject.Singleton;

@Data
@Singleton
public class Service {
    private ServiceManager serviceManager;

    @Inject
    public Service(ServiceManager serviceManager) {
        this.serviceManager = serviceManager;
    }
}
