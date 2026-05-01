package org.example.aichatservice.controller;

import org.example.aichatservice.model.ChatRequest;
import org.example.aichatservice.model.ChatResponse;
import org.example.aichatservice.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class ChatController {

    private ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        ChatResponse response = chatService.processMessage(request);
        return ResponseEntity.ok(response);
    }
}
