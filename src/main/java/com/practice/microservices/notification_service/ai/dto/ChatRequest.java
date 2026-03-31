package com.practice.microservices.notification_service.ai.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ChatRequest {
    private UUID conversationId;
    private String message;
}