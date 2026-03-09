package com.practice.microservices.notification_service.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotificationTemplateDto {
	 	private String templateCode;
	    private NotificationChannel channel;
	    private String subject;
	    private String body;
	    private boolean active;
	    private String version;
}
