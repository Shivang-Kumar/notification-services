package com.practice.microservices.notification_service.models;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class NotificationDto {
		
	private UUID id;
	
	private String eventId;
	
	private String traceId;

	private NotificationChannel channel;
	
	private String recipient;
	
	private String templateId;

	private Map<String,Object> payload;
	
	private NotificationStatus status;
	
	private int retryCount;
	private Instant nextRetryAt;
	private String errorMessage;
	private Instant createdAt;
	private Instant processedAt;
	private String version;

}
