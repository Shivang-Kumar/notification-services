package com.practice.microservices.notification_service.repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.practice.microservices.notification_service.models.Notification;
import com.practice.microservices.notification_service.models.NotificationStatus;

public interface NotificationRepository 
extends JpaRepository<Notification, UUID> {

boolean existsByEventId(String eventId);

List<Notification> findTop50ByStatusAndNextRetryAtBefore(
    NotificationStatus status,
    Instant now);
}