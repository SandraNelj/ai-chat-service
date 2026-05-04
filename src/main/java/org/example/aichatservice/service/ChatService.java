package org.example.aichatservice.service;

import org.example.aichatservice.dto.AiRequestDto;
import org.example.aichatservice.dto.AiResponseDto;
import org.example.aichatservice.dto.MessageDto;
import org.example.aichatservice.model.ChatRequest;
import org.example.aichatservice.model.ChatResponse;
import org.example.aichatservice.model.Message;
import org.example.aichatservice.repository.ConversationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.ArrayList;
import java.util.List;

@Service
public class ChatService {

    private final ConversationRepository conversationRepository;
    private final PersonalityService personalityService;
    private final AiClientService aiClientService;

    @Value("${openai.api.model}")
    private String model;

    public ChatService(ConversationRepository conversationRepository, PersonalityService personalityService,AiClientService aiClientService) {
        this.conversationRepository = conversationRepository;
        this.personalityService = personalityService;
        this.aiClientService = aiClientService;
    }

    public ChatResponse processMessage(ChatRequest request) {
        String sessionId = request.getSessionId() != null && !request.getSessionId().isBlank()
                ? request.getSessionId()
                : "anon-" + java.util.UUID.randomUUID().toString().substring(0, 8);

        String systemPrompt = personalityService.getSystemPrompt(request.getPersonality());

        List <MessageDto> messages = new ArrayList<>();
        messages.add(new MessageDto("system", systemPrompt));

        List <Message> history = conversationRepository.getHistory(sessionId);
        for (Message message : history) {
            messages.add(new MessageDto(message.getRole(), message.getContent()));
        }

        messages.add(new MessageDto("user", request.getMessage()));

        conversationRepository.addMessage(sessionId, new Message("user", request.getMessage()));

        AiRequestDto aiRequest = new AiRequestDto(model, messages);

        AiResponseDto aiResponse = aiClientService.callAiApi(aiRequest);

        String aiReply = aiResponse.getChoices().get(0).getMessage().getContent();
        conversationRepository.addMessage(sessionId, new Message("assistant", aiReply));

        return new ChatResponse(aiReply, request.getPersonality());

    }
}
