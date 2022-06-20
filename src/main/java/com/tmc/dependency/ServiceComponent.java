package com.tmc.dependency;

import com.tmc.service.*;
import dagger.Component;

import javax.inject.Singleton;

@Component (modules = BuildModule.class)
@Singleton
public interface ServiceComponent {
    ServiceManager provideServiceManager();
}
