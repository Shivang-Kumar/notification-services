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
public class EmailSender implements NotificationSender {

    private final JavaMailSender mailSender;

    @Value("${notification.email.from}")
    private String fromEmail;

    @Override
    public void send(Notification notification, String subject, String body) throws Exception {

        MimeMessage message = mailSender.createMimeMessage();

        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setFrom(fromEmail);
        helper.setTo(notification.getRecipient());
        helper.setSubject(subject);
        helper.setText(body, true); // true → HTML support

        mailSender.send(message);
    }

	@Override
	public NotificationChannel getChannel() {
		// TODO Auto-generated method stub
		return NotificationChannel.EMAIL;
	}
}