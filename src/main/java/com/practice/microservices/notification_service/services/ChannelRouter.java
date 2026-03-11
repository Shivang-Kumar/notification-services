package com.practice.microservices.notification_service.services;


import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.practice.microservices.notification_service.models.Notification;
import com.practice.microservices.notification_service.models.NotificationChannel;

import lombok.RequiredArgsConstructor;

@Service
public class ChannelRouter {

    private final Map<NotificationChannel, NotificationSender> senderMap;
    
    public ChannelRouter(List<NotificationSender> senders) {

        this.senderMap = senders.stream()
                .collect(Collectors.toMap(
                        NotificationSender::getChannel,
                        Function.identity()
                ));
    }
    public void route(Notification notification, String subject, String body) throws Exception {

        NotificationSender sender = senderMap.get(notification.getChannel());

        if (sender == null) {
            throw new RuntimeException("Unsupported notification channel: " + notification.getChannel());
        }

        sender.send(notification, subject, body);
    }
}