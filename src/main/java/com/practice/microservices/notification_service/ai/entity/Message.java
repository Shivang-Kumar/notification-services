package com.practice.microservices.notification_service.ai.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import com.google.auto.value.AutoValue.Builder;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
