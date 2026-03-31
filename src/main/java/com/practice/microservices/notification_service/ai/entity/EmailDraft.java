package com.practice.microservices.notification_service.ai.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class EmailDraft {

    public EmailDraft(UUID conversationId) {
    	this.conversationId=conversationId;
	}

	@Id
    private UUID conversationId;

    private String toEmail;
    private String subject;

    @Column(columnDefinition = "TEXT")
    private String body;

    private String status; // COLLECTING, READY, SENT

    @Column(columnDefinition = "TEXT")
    private String missingFields; // JSON or comma-separated

    private LocalDateTime updatedAt;
}