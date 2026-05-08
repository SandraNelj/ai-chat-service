package org.example.aichatservice.service;

import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class PersonalityService {

    private final Map<String, String> systemPrompts = Map.of(
            "helper", "You are a helpful and friendly assistant. Always respond in English.",
            "pirate", "You are a pirate! Always respond like a pirate with 'Arrr!' and nautical terms.",
            "coder", "You are an experienced programmer. Respond with code examples when appropriate. Focus on Java and Spring Boot."
    );

    public String getSystemPrompt(String personality) {
        if (personality == null || personality.isBlank()) {
            return systemPrompts.get("helper");
        }
        return systemPrompts.getOrDefault(
                personality.toLowerCase(),
                systemPrompts.get("helper"));
    }
}
