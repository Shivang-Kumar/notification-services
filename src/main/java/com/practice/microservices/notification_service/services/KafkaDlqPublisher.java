package com.practice.microservices.notification_service.services;


import java.time.Instant;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.practice.microservices.notification_service.models.DlqPublisherEvent;
import com.practice.microservices.notification_service.models.Notification;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class KafkaDlqPublisher {

	private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publish(Notification notification)
    {
        DlqPublisherEvent dlqEvent =
        		DlqPublisherEvent.builder()
                        .originalEvent(notification)
                        .failedAt(Instant.now())
                        .build();

        kafkaTemplate.send(
                "notifications.dlq",
                dlqEvent
        );
    }
}
