package com.practice.microservices.notification_service.ai.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.practice.microservices.notification_service.ai.dto.ChatApiResponse;
import com.practice.microservices.notification_service.ai.dto.ChatRequest;
import com.practice.microservices.notification_service.ai.dto.LlmResponse;
import com.practice.microservices.notification_service.ai.entity.Conversation;
import com.practice.microservices.notification_service.ai.entity.EmailDraft;
import com.practice.microservices.notification_service.ai.entity.Message;
import com.practice.microservices.notification_service.ai.repositories.ConversationRepository;
import com.practice.microservices.notification_service.ai.repositories.EmailDraftRepository;
import com.practice.microservices.notification_service.ai.repositories.MessageRepository;

@Service
public class ChatService {

	private final ChatClient chatClient;
	private final MessageRepository messageRepo;
	private final EmailDraftRepository draftRepo;
	private final ConversationRepository conversationRepository;

	public String test() {
		return chatClient.prompt().user("Say hello").call().content();
	}

	public ChatService(ChatClient.Builder builder, MessageRepository messageRepo, EmailDraftRepository draftRepo,
			ConversationRepository conversationRepository) {

		this.chatClient = builder.build();
		this.messageRepo = messageRepo;
		this.draftRepo = draftRepo;
		this.conversationRepository = conversationRepository;
	}

	public ChatApiResponse chat(ChatRequest request) {

		// Step 1: Save user message
		saveMessage(request.getConversationId(), "USER", request.getMessage());

		// Step 2: Fetch history + draft
		List<Message> history = messageRepo.findByConversationIdOrderByCreatedAtAsc(request.getConversationId());
		EmailDraft draft = draftRepo.findById(request.getConversationId())
				.orElse(new EmailDraft(request.getConversationId()));

		// Step 3: Build prompt
		String prompt = buildPrompt(history, draft);

		// Step 4: Call LLM
		String response = chatClient.prompt().user(prompt).call().content();

		// Step 5: Save assistant message
		saveMessage(request.getConversationId(), "ASSISTANT", response);

		// Step 6: Parse & update draft
		updateDraftFromResponse(draft, response, request.getConversationId());

		return new ChatApiResponse(response);
	}

	private String buildPrompt(List<Message> history, EmailDraft draft) {

		StringBuilder sb = new StringBuilder();

		sb.append("""
				You are an email assistant.

				Rules:
				- Help user draft an email
				- Collect: to, subject, body
				- If missing fields → ask user
				- NEVER assume email address
				- Only prepare email, DO NOT send

				Return JSON ONLY:
				{
				  "to": "...",
				  "subject": "...",
				  "body": "...",
				  "missing_fields": [],
				  "action": "COLLECT" | "READY"
				}
				""");

		sb.append("\n\nCurrent Draft:\n");
		sb.append("To: ").append(draft.getToEmail()).append("\n");
		sb.append("Subject: ").append(draft.getSubject()).append("\n");
		sb.append("Body: ").append(draft.getBody()).append("\n");

		sb.append("\n\nConversation:\n");

		for (Message msg : history) {
			sb.append(msg.getRole()).append(": ").append(msg.getContent()).append("\n");
		}

		return sb.toString();
	}

	private void saveMessage(UUID conversationId, String role, String content) {

		ensureConversationExists(conversationId);

		Message message = new Message();
		message.setConversationId(conversationId);
		message.setRole(role);
		message.setContent(content);
		message.setCreatedAt(LocalDateTime.now());

		messageRepo.save(message);
	}

	private void ensureConversationExists(UUID conversationId) {

		boolean exists = conversationRepository.existsById(conversationId);

		if (!exists) {
			Conversation conversation = new Conversation();
			conversation.setId(conversationId);
			conversation.setStatus("COLLECTING");
			conversation.setCreatedAt(LocalDateTime.now());
			conversation.setUpdateAt(LocalDateTime.now());

			conversationRepository.save(conversation);
		}
	}

	private void updateDraftFromResponse(EmailDraft draft, String response, UUID conversationId) {

		try {
			ObjectMapper mapper = new ObjectMapper();

			// Convert JSON string → Java object
			LlmResponse llm = mapper.readValue(response, LlmResponse.class);

			// If draft is new, attach conversationId
			if (draft.getConversationId() == null) {
				draft.setConversationId(conversationId);
			}

			// Update only if values are present (avoid overwriting with null)
			if (llm.getTo() != null) {
				draft.setToEmail(llm.getTo());
			}

			if (llm.getSubject() != null) {
				draft.setSubject(llm.getSubject());
			}

			if (llm.getBody() != null) {
				draft.setBody(llm.getBody());
			}

			// Missing fields
			if (llm.getMissing_fields() != null) {
				draft.setMissingFields(String.join(",", llm.getMissing_fields()));
			}

			// Status (COLLECTING / READY)
			if (llm.getAction() != null) {
				draft.setStatus(llm.getAction());
			}

			draft.setUpdatedAt(LocalDateTime.now());

			draftRepo.save(draft);

		} catch (Exception e) {
			throw new RuntimeException("Failed to parse LLM response: " + response, e);
		}
	}
}
