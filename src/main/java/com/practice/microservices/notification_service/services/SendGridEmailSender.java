package com.practice.microservices.notification_service.services;


import com.practice.microservices.notification_service.models.Notification;
import com.practice.microservices.notification_service.models.NotificationChannel;
import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("prod")
@RequiredArgsConstructor
public class SendGridEmailSender implements NotificationSender {

    @Value("${sendgrid.api-key}")
    private String apiKey;

    @Value("${sendgrid.from-email}")
    private String fromEmail;

    @Override
    public void send(Notification notification, String subject, String body) throws Exception {

        Email from = new Email(fromEmail);
        Email to = new Email(notification.getRecipient());

        Content content = new Content("text/html", body);

        Mail mail = new Mail(from, subject, to, content);

        SendGrid sg = new SendGrid(apiKey);
        Request request = new Request();

        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sg.api(request);

            if(response.getStatusCode() >= 400) {
                throw new RuntimeException("SendGrid failed: " + response.getBody());
            }

        } catch (Exception ex) {
            throw new RuntimeException("SendGrid error", ex);
        }
    }

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.EMAIL;
    }
}