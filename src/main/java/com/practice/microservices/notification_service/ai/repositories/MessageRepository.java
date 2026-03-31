package com.practice.microservices.notification_service.ai.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.practice.microservices.notification_service.ai.entity.Message;

public interface MessageRepository extends JpaRepository<Message,UUID>{

	List<Message> findByConversationIdOrderByCreatedAtAsc(UUID conversationId);
}
