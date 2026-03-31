package com.practice.microservices.notification_service.ai.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Message {
	
	@Id
	@GeneratedValue(strategy=GenerationType.UUID)
	private UUID id;
	
	private UUID conversationId;
	private  String role; //USER , ASSISTANT, TOOL
	
	@Column(columnDefinition="TEXT")
	private String content;
	private LocalDateTime createdAt;

}
