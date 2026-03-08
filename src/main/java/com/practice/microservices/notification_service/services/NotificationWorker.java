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
    	System.out.println("Processing Notification: Lalalalalalala   lallaaaaaa.........    "+notification.getId());
    	
    	try {
    		//simulating sending notification
        	sendNotification(notification);
        	
        	notification.setStatus(NotificationStatus.SENT);
        	notificationRepository.save(notification);
    	}
    	catch(Exception ex)
    	{
    		handleFailure(notification);
    	}
    }
    
    private void sendNotification(Notification notification) throws Exception
    {
    	//simulating random failure
    	if(Math.random()<0.3)
    	{
    		throw new RuntimeException("Simulation Failure");
    	}
    	
    	System.out.println("Notification sent   successfully: "+notification.getId());
    	
    }
    
    
    private void handleFailure(Notification notification)
    {
    	int retryCount=notification.getRetryCount()+1;
    	notification.setRetryCount(retryCount);
    	
    	if(retryCount>4)
    	{
    		notification.setStatus(NotificationStatus.DEAD);
    	}
    	else {
    		notification.setStatus(NotificationStatus.PENDING);
    		long delaySeconds=(long)Math.pow(2,retryCount)*30;
    		notification.setNextRetryAt(notification.getNextRetryAt().plusSeconds(delaySeconds));
    	}
    	notificationRepository.save(notification);
    }
    
}