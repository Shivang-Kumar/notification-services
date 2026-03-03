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
@Table(name = "notification_templates",
       indexes = {
           @Index(name = "idx_template_code", columnList = "templateCode", unique = true)
       })
public class NotificationTemplate {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String templateCode;

    @Enumerated(EnumType.STRING)
    private NotificationChannel channel;

    private String subject;

    @Column(columnDefinition = "TEXT")
    private String body;

    private boolean active;

    private String version;

    private Instant createdAt;

    @PrePersist
    public void prePersist() {
        this.id = UUID.randomUUID();
        this.createdAt = Instant.now();
    }
}