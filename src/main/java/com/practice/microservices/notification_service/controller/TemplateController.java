package com.practice.microservices.notification_service.controller;


import org.springframework.web.bind.annotation.*;

import com.practice.microservices.notification_service.models.NotificationTemplate;
import com.practice.microservices.notification_service.models.NotificationTemplateDto;
import com.practice.microservices.notification_service.services.TemplateService;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/templates")
@AllArgsConstructor
public class TemplateController {

    private final TemplateService templateService;

    @PostMapping
    public NotificationTemplate createTemplate(@RequestBody NotificationTemplateDto request) {

        return templateService.createTemplate(request);

    }
}