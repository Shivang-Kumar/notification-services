package com.practice.microservices.notification_service.ai.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LlmResponse {

    private String to;
    private String subject;
    private String body;
    private List<String> missing_fields;
    private String action;
}