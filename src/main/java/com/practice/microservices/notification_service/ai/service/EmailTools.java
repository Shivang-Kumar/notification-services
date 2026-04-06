package com.practice.microservices.notification_service.ai.service;

import org.springframework.stereotype.Component;

@Component
public class EmailTools {

    public String sendEmail(String to, String subject, String body) {
        return "EMAIL_REQUEST_ACCEPTED";
    }
}