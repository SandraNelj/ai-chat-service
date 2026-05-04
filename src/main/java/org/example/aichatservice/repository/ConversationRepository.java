package org.example.aichatservice.repository;

import org.example.aichatservice.model.Message;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class ConversationRepository {

    private Map<String, List<Message>> sessionHistory = new ConcurrentHashMap<>();
    private static final int MAX_HISTORY = 10;

    public void addMessage(String sessionId, Message message) {
        sessionHistory.compute(sessionId, (key, history) -> {
            if (history == null) {
                history = Collections.synchronizedList(new ArrayList<>());
            }
            history.add(message);

            if (history.size() > MAX_HISTORY) {
                history.remove(0);
            }
            return history;
        });
    }

    public List<Message> getHistory(String sessionId) {
        List<Message> history = sessionHistory.get(sessionId);
        if (history == null) {
            return new ArrayList<>();
        }
        synchronized (history) {
            return new ArrayList<>(history);
        }
    }

    public void clearSession(String sessionId) {
        sessionHistory.remove(sessionId);
    }
}


