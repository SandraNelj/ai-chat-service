package org.example.aichatservice.dto;

import lombok.Data;
import java.util.List;

@Data
public class AiResponseDto {
    private List<Choice> choices;

    @Data
    public static class Choice {
        private MessageDto message;
    }
}
