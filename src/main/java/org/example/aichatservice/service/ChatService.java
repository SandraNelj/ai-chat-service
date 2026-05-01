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
    private final WebClient aiWebClient;

    @Value("${openai.api.model}")
    private String model;

    public ChatService(ConversationRepository conversationRepository, PersonalityService personalityService,WebClient aiWebClient) {
        this.conversationRepository = conversationRepository;
        this.personalityService = personalityService;
        this.aiWebClient = aiWebClient;
    }

    @Retryable(
            retryFor = {WebClientResponseException.class},
            noRetryFor = {WebClientResponseException.BadRequest.class,
                WebClientResponseException.Unauthorized.class,
                WebClientResponseException.Forbidden.class,
                WebClientResponseException.NotFound.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 15000, multiplier = 2)
    )

    @Recover
    public ChatResponse recoverFromAiFailure(WebClientResponseException exception, AiRequestDto aiRequest) {
        return new ChatResponse("AI-tjänsten är just nu överbelastad. Försök igen om en minut!", "error");
    }

    public AiResponseDto callAiApi(AiRequestDto aiRequest) {
        return aiWebClient.post()
                .uri("/chat/completions")
                .bodyValue(aiRequest)
                .retrieve()
                .bodyToMono(AiResponseDto.class)
                .block();
    }

    public ChatResponse processMessage(ChatRequest request) {
        String sessionId = request.getSessionId() != null
                ? request.getSessionId()
                : "default-session";

        String systemPrompt = personalityService.getSystemPrompt(request.getPersonality());

        List <MessageDto> messages = new ArrayList<>();
        messages.add(new MessageDto("system", systemPrompt));

        List <Message> history = conversationRepository.getHistory(sessionId);
        for (Message message : history) {
            messages.add(new MessageDto(message.getRole(), message.getContent()));
        }

        conversationRepository.addMessage(sessionId, new Message("user", request.getMessage()));

        AiRequestDto aiRequest = new AiRequestDto(model, messages);

        AiResponseDto aiResponse = callAiApi(aiRequest);

        String aiReply = aiResponse.getChoices().get(0).getMessage().getContent();
        conversationRepository.addMessage(sessionId, new Message("assistant", aiReply));

        return new ChatResponse(aiReply, request.getPersonality());

    }
}
