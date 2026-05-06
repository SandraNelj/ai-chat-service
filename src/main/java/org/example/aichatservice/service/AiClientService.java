package org.example.aichatservice.service;

import org.example.aichatservice.dto.AiRequestDto;
import org.example.aichatservice.dto.AiResponseDto;
import org.example.aichatservice.dto.MessageDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.*;
import java.util.List;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.HttpClientErrorException;

@Service
public class AiClientService {

    @Value("${openai.api.key}")
    private String apiKey;

    private final RestClient aiRestClient;

    @Value("${openai.api.model}")
    private String model;

    public AiClientService(RestClient aiRestClient) {
        this.aiRestClient = aiRestClient;
    }


    @Retryable(
    retryFor = {HttpServerErrorException.class, ResourceAccessException.class},
            noRetryFor =  {HttpClientErrorException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay=5000, multiplier = 2)
    )

    public AiResponseDto callAiApi(AiRequestDto aiRequest) {
        return aiRestClient.post()
                .uri("/chat/completions")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .body(aiRequest)
                .retrieve()
                .body(AiResponseDto.class);
    }

    @Recover
    public AiResponseDto recoverFromAiFailure(Exception e, AiRequestDto aiRequest) {
        AiResponseDto fallbackResponse = new AiResponseDto();
        AiResponseDto.Choice choice = new AiResponseDto.Choice();
        MessageDto message = new MessageDto("assistant",
                "AI-tjänsten är just nu otillgänglig. Försök igen om en minut!");
        choice.setMessage(message);
        fallbackResponse.setChoices(List.of(choice));
        return fallbackResponse;
    }
}

