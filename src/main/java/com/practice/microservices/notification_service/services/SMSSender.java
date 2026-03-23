package com.practice.microservices.notification_service.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.practice.microservices.notification_service.models.Notification;
import com.practice.microservices.notification_service.models.NotificationChannel;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SMSSender implements NotificationSender {

	@Override
	public void send(Notification notification, String subject, String body) {

		// Simulate SMS sending
		System.out.println("Sending SMS");
		System.out.println("To: " + notification.getRecipient());
		System.out.println("Message: " + body);
	}

	@Override
	public NotificationChannel getChannel() {
		return NotificationChannel.SMS;
	}
}