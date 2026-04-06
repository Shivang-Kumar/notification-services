package com.practice.microservices.notification_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableKafka
@EnableScheduling
@ConfigurationPropertiesScan
public class NotificationServiceApplication {

	public static void main(String[] args) {
		System.out.println("PROJECT ID = " + 
			    System.getProperty("spring.ai.vertex.ai.project-id"));
		SpringApplication.run(NotificationServiceApplication.class, args);
		
	}

}
