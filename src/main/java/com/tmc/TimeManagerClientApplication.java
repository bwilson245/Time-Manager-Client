package com.tmc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


import javax.inject.Singleton;


@SpringBootApplication
@Singleton
public class TimeManagerClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(TimeManagerClientApplication.class, args);
	}
}
