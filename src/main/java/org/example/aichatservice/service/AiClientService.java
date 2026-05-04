package org.example.aichatservice.service;

import org.example.aichatservice.dto.AiRequestDto;
import org.example.aichatservice.dto.AiResponseDto;
import org.example.aichatservice.dto.MessageDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;

@Service
public class AiClientService {

    private final WebClient aiWebClient;

    @Value("${openai.api.model}")
    private String model;

    public AiClientService(WebClient aiWebClient) {
        this.aiWebClient = aiWebClient;
    }

    @Retryable(
    retryFor = {WebClientResponseException.class},
            noRetryFor = {
                    WebClientResponseException.BadRequest.class,
                    WebClientResponseException.Unauthorized.class,
                    WebClientResponseException.Forbidden.class,
                    WebClientResponseException.NotFound.class
            },
            maxAttempts = 3,
            backoff = @Backoff(delay=15000, multiplier = 2)
    )

    public AiResponseDto callAiApi(AiRequestDto aiRequestDto) {
        return aiWebClient.post()
                .uri("/chat/completions")
                .bodyValue(aiRequestDto)
                .retrieve()
                .bodyToMono(AiResponseDto.class)
                .block();
    }

    @Recover
    public AiResponseDto recoverFromAiFail(WebClientResponseException e, AiRequestDto aiRequestDto) {
        AiResponseDto fallbackResponse = new AiResponseDto();
        AiResponseDto.Choice choice = new AiResponseDto.Choice();
        MessageDto message = new MessageDto("Assistant", "AI-tjänsten är just nu överbelastad. Försök igen om 1 minut!");
        choice.setMessage(message);
        fallbackResponse.setChoices(List.of(choice));
        return fallbackResponse;
    }
}

