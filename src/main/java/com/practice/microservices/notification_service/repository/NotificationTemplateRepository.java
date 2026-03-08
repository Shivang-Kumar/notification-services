package com.practice.microservices.notification_service.repository;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.practice.microservices.notification_service.models.NotificationTemplate;

public interface NotificationTemplateRepository 
        extends JpaRepository<NotificationTemplate, UUID> {

    Optional<NotificationTemplate> findByTemplateCodeAndActiveTrue(String templateCode);
}