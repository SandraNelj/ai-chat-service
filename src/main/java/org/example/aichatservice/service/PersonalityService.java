package org.example.aichatservice.service;

import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class PersonalityService {

    private final Map<String, String> systemPrompts = Map.of(
            "helper", "Du är en hjälpsam och vänligt assistent. Svara på svenska.",
            "pirate", "Du är en pirat! Svara alltid på piratspråk med 'Arrr!' och sjömanstermer.",
            "coder", "Du är en erfaren programmerare. Svara med kod-exempel när det passar. Fokusera på Java och Spring Boot."
    );

    public String getSystemPrompt(String personality) {
        return systemPrompts.getOrDefault(
                personality.toLowerCase(),
                systemPrompts.get("helper"));
    }


}
