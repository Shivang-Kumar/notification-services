package com.practice.microservices.notification_service.ai.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.practice.microservices.notification_service.ai.entity.EmailDraft;
import com.practice.microservices.notification_service.ai.entity.Message;

public interface EmailDraftRepository extends JpaRepository<EmailDraft,UUID>{

}
