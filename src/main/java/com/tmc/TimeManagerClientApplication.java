package com.tmc;

import com.tmc.dependency.DaggerServiceComponent;
import com.tmc.dependency.ServiceComponent;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


import javax.inject.Singleton;


@SpringBootApplication
@Singleton
public class TimeManagerClientApplication {
	public static ServiceComponent dagger;

	public static void main(String[] args) {
		dagger = DaggerServiceComponent.create();
		SpringApplication.run(TimeManagerClientApplication.class, args);
	}
}
