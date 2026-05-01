package org.example.aichatservice.repository;

import org.example.aichatservice.model.Message;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class ConversationRepository {

    private Map<String, List<Message>> sessionHistory = new ConcurrentHashMap<>();
    private static final int MAX_HISTORY = 10;

    public void addMessage(String sessionId, Message message) {
        sessionHistory.computeIfAbsent(sessionId,k -> new ArrayList<>()).add(message);

        List <Message> history = sessionHistory.get(sessionId);
        if (history.size() > MAX_HISTORY) {
            history.remove(0);
        }
    }

    public List<Message> getHistory(String sessionId) {
        return sessionHistory.getOrDefault(sessionId, new ArrayList<>());
    }

    public void clearSession(String sessionId) {
        sessionHistory.remove(sessionId);
    }
}


