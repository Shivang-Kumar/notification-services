package com.practice.microservices.notification_service.services;

import com.practice.microservices.notification_service.models.Notification;
import com.practice.microservices.notification_service.models.NotificationChannel;

public interface NotificationSender {
	

	void send(Notification notification, String subject, String body) throws Exception;
	NotificationChannel getChannel();
}
