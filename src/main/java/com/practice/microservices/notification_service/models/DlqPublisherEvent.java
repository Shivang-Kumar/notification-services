package com.practice.microservices.notification_service.models;


import java.time.Instant;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.AllArgsConstructor;
@Getter
@Setter 
@Builder
public class DlqPublisherEvent {


	    private Notification originalEvent;
	    private Instant failedAt;
	    
	}

