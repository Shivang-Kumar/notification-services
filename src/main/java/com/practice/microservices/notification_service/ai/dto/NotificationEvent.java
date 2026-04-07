package com.practice.microservices.notification_service.ai.dto;

import java.time.Instant;
import java.util.Map;

import com.practice.microservices.notification_service.models.NotificationChannel;

import lombok.AllArgsConstructor;
import jakarta.persistence.Entity;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder;
import lombok.Getter;


@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class NotificationEvent {
	private String eventId;
	private String traceId;
	private String eventType;
	private String channel;
	private String recipient;
	private String templateId;
	private  Map<String, Object> payload;
	private Instant createdAt;
	private  String version;
}
