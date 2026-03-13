package com.practice.microservices.notification_service.services;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;

import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.practice.microservices.notification_service.models.Notification;
import com.practice.microservices.notification_service.models.NotificationDto;
import com.practice.microservices.notification_service.models.NotificationStatus;
import com.practice.microservices.notification_service.repository.NotificationRepository;


@Service
@AllArgsConstructor
public class NotificationWorker {
	

    private final NotificationRepository notificationRepository;
    private final ExecutorService executor;
    private final NotificationProcessor processor;
    private final KafkaDlqPublisher dlqPublisher;

    
    private static final int MAX_RETRIES = 3;

    
    
    @Scheduled(fixedDelay= 5000)
    public void pollNotification()
    {
    	List<Notification> notifications=notificationRepository.claimPendingNotifications();
    	
    	for(Notification notification: notifications)
    	{
    		executor.execute(()->{
    			processNotification(notification);
    		});
    	}
    }
    
    private void processNotification(Notification notification)
    {
        System.out.println("Processing Notification: " + notification.getId());

        try {
            processor.process(notification);

            notification.setStatus(NotificationStatus.SENT);
            notification.setProcessedAt(Instant.now());

            notificationRepository.save(notification);
        }
        catch(Exception ex)
        {
            handleFailure(notification,ex);
        }
    }
    
  
    
    
    private void handleFailure(Notification notification,Exception ex)
    {
    	int retryCount=notification.getRetryCount()+1;
    	notification.setRetryCount(retryCount);
    	
    	if(retryCount>=MAX_RETRIES)
    	{
    		notification.setStatus(NotificationStatus.DEAD);
    		dlqPublisher.publish(notification);
    	}
    	else {
    		
    		notification.setStatus(NotificationStatus.PENDING);
    		long delaySeconds = 60; // fixed retry
    		notification.setNextRetryAt(Instant.now().plusSeconds(delaySeconds));
    		notification.setErrorMessage(ex.getMessage());
    	}
    	notificationRepository.save(notification);
    }
    
}