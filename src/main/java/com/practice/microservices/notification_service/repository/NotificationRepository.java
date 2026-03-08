package com.practice.microservices.notification_service.repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.practice.microservices.notification_service.models.Notification;
import com.practice.microservices.notification_service.models.NotificationStatus;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

	@Query(value = """
			UPDATE notifications
			SET status = 'PROCESSING'
			WHERE id IN (
			    SELECT id
			    FROM notifications
			    WHERE status = 'PENDING'
			    AND next_retry_at <= now()
			    LIMIT 30
			    FOR UPDATE SKIP LOCKED
			)
			RETURNING *;
			""", nativeQuery = true)
	List<Notification> claimPendingNotifications();
}