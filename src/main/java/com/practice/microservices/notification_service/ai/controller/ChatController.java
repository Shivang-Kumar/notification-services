package com.practice.microservices.notification_service.ai.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.practice.microservices.notification_service.ai.dto.ChatApiResponse;
import com.practice.microservices.notification_service.ai.dto.ChatRequest;
import com.practice.microservices.notification_service.ai.service.ChatService;

@RestController
@RequestMapping("/ai")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/chat")
    public ChatApiResponse chat(@RequestBody ChatRequest request) {
        return chatService.chat(request);
    }
    
    @GetMapping("/test-ai")
    public String test() {
        return chatService.test();
    }
}