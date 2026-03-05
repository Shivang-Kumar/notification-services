package com.practice.microservices.notification_service.services;

import java.time.Instant;
import jakarta.annotation.PostConstruct;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.practice.microservices.notification_service.models.Notification;
import com.practice.microservices.notification_service.models.NotificationStatus;
import com.practice.microservices.notification_service.repository.NotificationRepository;


@Service
public class NotificationConsumer {
	

    private final NotificationRepository repository;
    private ObjectMapper objectMapper;

    public NotificationConsumer(NotificationRepository repository,
                                ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }
    @PostConstruct
    public void init() {
        System.out.println("Consumer Bean Created");
    }

    @KafkaListener(topics = "notification.events")
    public void consume(Notification event,
                        Acknowledgment acknowledgment) {

        try {

            Notification notification = new Notification();

            notification.setEventId(event.getEventId());
            notification.setRecipient(event.getRecipient());
            notification.setTemplateId(event.getTemplateId());
            notification.setPayload(event.getPayload());

            notification.setStatus(NotificationStatus.PENDING);
            notification.setRetryCount(0);
            notification.setCreatedAt(Instant.now());

            repository.save(notification);

            //Commit offset ONLY after successful DB insert
            acknowledgment.acknowledge();

        } catch (DataIntegrityViolationException e) {

            // Duplicate event → idempotent case
            acknowledgment.acknowledge();

        } catch (Exception e) {

            // DO NOT acknowledge
            // Kafka will retry automatically
            throw new RuntimeException(e);
        }

}
}