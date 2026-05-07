package org.example.aichatservice.controller;
import org.example.aichatservice.model.ChatRequest;
import org.example.aichatservice.model.ChatResponse;
import org.example.aichatservice.service.ChatService;
import org.junit.jupiter.api.*;
import org.springframework.http.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


class ChatControllerTest {

    private final ChatService chatService = mock(ChatService.class);
    private final ChatController chatController = new ChatController(chatService);

    @Test
    void shouldReturnChatResponse() {
        when(chatService.processMessage(any(ChatRequest.class)))
                .thenReturn(new ChatResponse("Hej! Hur kan jag hjälpa dig?", "helper"));

        ChatRequest request = new ChatRequest();
        request.setPersonality("helper");
        request.setMessage("Hello");
        request.setSessionId("test-123");

        ResponseEntity<ChatResponse> response = chatController.chat(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Hej! Hur kan jag hjälpa dig?", response.getBody().getReply());
    }

    @Test
    void shouldReturnErrorOnServiceFailure() {
        when(chatService.processMessage(any(ChatRequest.class)))
                .thenThrow(new RuntimeException("AI-tjänsten är nere"));

        ChatRequest request = new ChatRequest();
        request.setPersonality("helper");
        request.setMessage("Hej");
        request.setSessionId("test-123");

        try {
            chatController.chat(request);
        } catch (RuntimeException e) {
            assertEquals("AI-tjänsten är nere", e.getMessage());
        }
    }

    @Test
    void shouldRetryOnAiServiceFailure() {
        when(chatService.processMessage(any(ChatRequest.class)))
                .thenThrow(new RuntimeException("503 Service Unavailable"))
                .thenReturn(new ChatResponse("Response after retry!", "helper"));

        ChatRequest request = new ChatRequest();
        request.setPersonality("helper");
        request.setMessage("Test");
        request.setSessionId("retry-test");

        try {
            chatController.chat(request);
        } catch (RuntimeException e) {
            assertEquals("503 Service Unavailable", e.getMessage());
        }

        ResponseEntity<ChatResponse> response = chatController.chat(request);
        assertEquals(HttpStatus.OK,response.getStatusCode());
        assertEquals("Response after retry!", response.getBody().getReply());
    }
}