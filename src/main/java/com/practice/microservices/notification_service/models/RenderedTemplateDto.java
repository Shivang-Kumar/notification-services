package com.practice.microservices.notification_service.models;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RenderedTemplateDto {
		 
	private String subject;
	private String body;
	
}
