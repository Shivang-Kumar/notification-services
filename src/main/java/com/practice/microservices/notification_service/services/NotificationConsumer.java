package com.practice.microservices.notification_service.services;

import java.time.Instant;
import jakarta.annotation.PostConstruct;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.practice.microservices.notification_service.models.Notification;
import com.practice.microservices.notification_service.models.NotificationDto;
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
    public void consume(ConsumerRecord<String, NotificationDto> record ,
                        Acknowledgment acknowledgment) {

        try {
            
        	NotificationDto event =record.value();
        	
            Notification notification = new Notification();

            notification.setEventId(event.getEventId());
            notification.setRecipient(event.getRecipient());
            notification.setTemplateId(event.getTemplateId());

            // Convert Map to JSON string
            String payloadJson = objectMapper.writeValueAsString(event.getPayload());
            notification.setPayload(payloadJson);
            notification.setChannel(event.getChannel());
            notification.setNextRetryAt(Instant.now());
            notification.setStatus(NotificationStatus.PENDING);
            notification.setRetryCount(0);
            notification.setCreatedAt(Instant.now());

            repository.save(notification);

            //Commit offset ONLY after successful DB insert
            acknowledgment.acknowledge();

        } 
        catch (JsonProcessingException e) {
            System.out.println("Error while deserializing notification event");
            acknowledgment.acknowledge(); // skipping it to process next items
        }
        catch (DataIntegrityViolationException e) {

            // Duplicate event → idempotent case
            acknowledgment.acknowledge();

        } catch (Exception e) {

        	acknowledgment.acknowledge();
            throw new RuntimeException(e);
        }

}
}