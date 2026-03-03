package com.practice.microservices.notification_service.models;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name="notifications",
			indexes= {
					@Index(name="idx_status_retry",columnList="status,nextRetryAt"),
					@Index(name="idx_event_id", columnList="eventId", unique=true)
			})


public class Notification {
		
	@Id
	private UUID id;
	
	@Column(nullable=false,unique=true)
	private String eventId;
	
	private String traceId;
	
	@Enumerated(EnumType.STRING)
	private NotificationChannel channel;
	
	private String recipient;
	
	private String templateId;
	
	@Column(columnDefinition="TEXT")
	private String payload;
	
	@Enumerated(EnumType.STRING)
	private NotificationStatus status;
	
	private int retryCount;
	private Instant nextRetryAt;
	private String errorMessage;
	private Instant createdAt;
	private Instant processedAt;
	private String version;
	
	 @PrePersist
	    public void prePersist() {
	        this.id = UUID.randomUUID();
	        this.createdAt = Instant.now();
	    }
}
