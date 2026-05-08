package org.example.aichatservice.controller;
import org.example.aichatservice.model.ChatRequest;
import org.example.aichatservice.model.ChatResponse;
import org.example.aichatservice.service.ChatService;
import org.junit.jupiter.api.*;
import org.springframework.http.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


class ChatControllerTest {

    private final ChatService chatService = mock(ChatService.class);
    private final ChatController chatController = new ChatController(chatService);

    @Test
    void shouldReturnChatResponse() {
        when(chatService.processMessage(any(ChatRequest.class)))
                .thenReturn(new ChatResponse("Hello! How can I help you?", "helper"));

        ChatRequest request = new ChatRequest();
        request.setPersonality("helper");
        request.setMessage("Hello");
        request.setSessionId("test-123");

        ResponseEntity<ChatResponse> response = chatController.chat(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Hello! How can I help you?", response.getBody().getReply());
    }

    @Test
    void shouldReturnErrorOnServiceFailure() {
        when(chatService.processMessage(any(ChatRequest.class)))
                .thenThrow(new RuntimeException("AI-service is unavailable"));

        ChatRequest request = new ChatRequest();
        request.setPersonality("helper");
        request.setMessage("Hello");
        request.setSessionId("test-123");

        RuntimeException ex = assertThrows(RuntimeException.class, () -> chatController.chat(request));
        assertEquals("AI-service is unavailable", ex.getMessage());
    }

    @Test
    void shouldSucceedOnSubsequentCallAfterPriorFailure() {
        when(chatService.processMessage(any(ChatRequest.class)))
                .thenThrow(new RuntimeException("503 Service Unavailable"))
                .thenReturn(new ChatResponse("Response after retry!", "helper"));

        ChatRequest request = new ChatRequest();
        request.setPersonality("helper");
        request.setMessage("Test");
        request.setSessionId("retry-test");

        assertThrows(RuntimeException.class, () -> chatController.chat(request));

        ResponseEntity<ChatResponse> response = chatController.chat(request);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Response after retry!", response.getBody().getReply());
    }
}