package com.practice.microservices.notification_service.ai.service;


import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.practice.microservices.notification_service.ai.dto.ChatApiResponse;
import com.practice.microservices.notification_service.ai.dto.ChatRequest;
import com.practice.microservices.notification_service.ai.dto.LlmResponse;
import com.practice.microservices.notification_service.ai.dto.NotificationEvent;
import com.practice.microservices.notification_service.ai.entity.Conversation;
import com.practice.microservices.notification_service.ai.entity.EmailDraft;
import com.practice.microservices.notification_service.ai.entity.Message;
import com.practice.microservices.notification_service.ai.repositories.ConversationRepository;
import com.practice.microservices.notification_service.ai.repositories.EmailDraftRepository;
import com.practice.microservices.notification_service.ai.repositories.MessageRepository;
import com.practice.microservices.notification_service.models.Notification;
import com.practice.microservices.notification_service.services.SendGridEmailSender;

@Service
public class ChatService {

    private final ChatClient chatClient;
    private final MessageRepository messageRepo;
    private final EmailDraftRepository draftRepo;
    private final ConversationRepository conversationRepository;
    private final ObjectMapper objectMapper;
    private final SendGridEmailSender sendGridEmailSender;
    private final NotificationEventPublisher eventPublisher;
    

    @Value("classpath:prompts/system-prompt.txt")
    private Resource resource;

    public ChatService(ChatClient.Builder builder,
                       MessageRepository messageRepo,
                       EmailDraftRepository draftRepo,
                       ConversationRepository conversationRepository,
                       SendGridEmailSender sendGridEmailSender,
                       NotificationEventPublisher eventPublisher) {

        this.chatClient = builder.build();
        this.messageRepo = messageRepo;
        this.draftRepo = draftRepo;
        this.conversationRepository = conversationRepository;
        this.objectMapper = new ObjectMapper();
        this.sendGridEmailSender=sendGridEmailSender;
        this.eventPublisher=eventPublisher;
    }

    public ChatApiResponse chat(ChatRequest request) throws Exception {

        UUID conversationId = request.getConversationId();

        // 1. Save user message
        saveMessage(conversationId, "USER", request.getMessage());

        // 2. Fetch history + draft
        List<Message> history =
                messageRepo.findByConversationIdOrderByCreatedAtAsc(conversationId);

        EmailDraft draft = draftRepo.findById(conversationId)
                .orElse(new EmailDraft(conversationId));

        // 3. Build prompt
        String prompt = buildPrompt(history, draft);

        // 4. Call LLM
        String rawResponse = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

        // 5. Parse safely
        LlmResponse llm = parseLlmResponse(rawResponse);

        // 6. Update draft
        updateDraft(draft, llm, conversationId);

        // 7. Save assistant reply
        saveMessage(conversationId, "ASSISTANT", llm.getReply());

        // 8. Handle SEND action
        if ("SEND".equalsIgnoreCase(llm.getAction())) {
            handleSend(draft, request.getMessage(), conversationId);
        }

        // 9. Return clean response
        return new ChatApiResponse(llm.getReply());
    }

    // ---------------- PROMPT ----------------
    


    public String getPrompt() throws Exception {
        return new String(
                resource.getInputStream().readAllBytes(),
                StandardCharsets.UTF_8
        );
    }

    private String buildPrompt(List<Message> history, EmailDraft draft) throws Exception {

        StringBuilder sb = new StringBuilder();
        String prompt=getPrompt();

        sb.append(prompt);

        sb.append("\n\nCurrent Draft:\n");
        sb.append("To: ").append(nullSafe(draft.getToEmail())).append("\n");
        sb.append("Subject: ").append(nullSafe(draft.getSubject())).append("\n");
        sb.append("Body: ").append(nullSafe(draft.getBody())).append("\n");

        sb.append("\n\nConversation:\n");

        for (Message msg : history) {
            sb.append(msg.getRole()).append(": ")
              .append(msg.getContent()).append("\n");
        }

        return sb.toString();
    }

    // ---------------- PARSER ----------------

    private LlmResponse parseLlmResponse(String response) {

        try {
            String cleaned = response.trim();

            // Handle cases where LLM adds extra text
            if (!cleaned.startsWith("{")) {
                cleaned = cleaned.substring(cleaned.indexOf("{"));
            }

            return objectMapper.readValue(cleaned, LlmResponse.class);

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse LLM response: " + response, e);
        }
    }

    // ---------------- DRAFT UPDATE ----------------

    private void updateDraft(EmailDraft draft, LlmResponse llm, UUID conversationId) {

        if (draft.getConversationId() == null) {
            draft.setConversationId(conversationId);
        }

        if (llm.getTo() != null) {
            draft.setToEmail(llm.getTo());
        }

        if (llm.getSubject() != null) {
            draft.setSubject(llm.getSubject());
        }

        if (llm.getBody() != null) {
            draft.setBody(llm.getBody());
        }

        if (llm.getMissing_fields() != null) {
            draft.setMissingFields(String.join(",", llm.getMissing_fields()));
        }

        if (llm.getAction() != null) {
            draft.setStatus(llm.getAction());
        }

        draft.setUpdatedAt(LocalDateTime.now());

        draftRepo.save(draft);
    }

    // ---------------- SEND HANDLER ----------------

    private void handleSend(EmailDraft draft, String userMessage, UUID conversationId) throws Exception {

        // Guardrail 1: confirmation
        if (!userMessage.toLowerCase().contains("send")) {
            throw new RuntimeException("User did not explicitly confirm sending.");
        }

        // Guardrail 2: validation
        if (draft.getToEmail() == null ||
            draft.getSubject() == null ||
            draft.getBody() == null) {
            throw new RuntimeException("Email is incomplete.");
        }

        // 🔥 TODO: Replace with your notification service call
        NotificationEvent mynotify=createNotificationEvent(draft,conversationId);
        eventPublisher.publish(mynotify);
        
        
        // Save TOOL message
        saveMessage(conversationId, "TOOL",
                "Email sent to " + draft.getToEmail());
    }

    // ---------------- MESSAGE SAVE ----------------

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

        if (!conversationRepository.existsById(conversationId)) {

            Conversation conversation = new Conversation();
            conversation.setId(conversationId);
            conversation.setStatus("COLLECTING");
            conversation.setCreatedAt(LocalDateTime.now());
            conversation.setUpdateAt(LocalDateTime.now());

            conversationRepository.save(conversation);
        }
    }
    
    //Creating event for kafka
    private  NotificationEvent createNotificationEvent(EmailDraft email,UUID conversationId)
    {
    	
    	HashMap<String,Object> details=new HashMap<>();
    	details.put("subject", email.getSubject());
    	details.put("body", email.getBody());
    	
    	NotificationEvent event=NotificationEvent.builder()
    			.eventId(UUID.randomUUID().toString())
    			.traceId(conversationId.toString())
    			.eventType("AI EMAIL")
    			.channel("EMAIL")
    			.recipient(email.getToEmail())
    			.payload(details)
    			.templateId("GEMINI_EMAIL")
    			.createdAt(Instant.now())
    			.build();
    	
    	return event;
    }

    // ---------------- UTILS ----------------

    private String nullSafe(String value) {
        return value == null ? "" : value;
    }
}