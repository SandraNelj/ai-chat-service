package org.example.aichatservice.service;

import org.example.aichatservice.dto.AiRequestDto;
import org.example.aichatservice.dto.AiResponseDto;
import org.example.aichatservice.dto.MessageDto;
import org.example.aichatservice.model.ChatRequest;
import org.example.aichatservice.model.ChatResponse;
import org.example.aichatservice.model.Message;
import org.example.aichatservice.repository.ConversationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


import java.util.ArrayList;
import java.util.List;

@Service
public class ChatService {

    private final ConversationRepository conversationRepository;
    private final PersonalityService personalityService;
    private final AiClientService aiClientService;

    @Value("${openrouter.api.model}")
    private String model;

    public ChatService(ConversationRepository conversationRepository, PersonalityService personalityService,AiClientService aiClientService) {
        this.conversationRepository = conversationRepository;
        this.personalityService = personalityService;
        this.aiClientService = aiClientService;
    }

    public ChatResponse processMessage(ChatRequest request) {
        String sessionId = request.getSessionId() != null && !request.getSessionId().isBlank()
                ? request.getSessionId()
                : "anon-" + java.util.UUID.randomUUID();

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

        if (aiResponse == null
        || aiResponse.getChoices() == null
        || aiResponse.getChoices().isEmpty()
        || aiResponse.getChoices().get(0).getMessage() == null
        || aiResponse.getChoices().get(0).getMessage().getContent() == null) {
            throw new IllegalStateException("AI response is empty. Please try again!");
        }

        String aiReply = aiResponse.getChoices().get(0).getMessage().getContent();
        conversationRepository.addMessage(sessionId, new Message("assistant", aiReply));

        return new ChatResponse(aiReply, request.getPersonality());
    }
}
