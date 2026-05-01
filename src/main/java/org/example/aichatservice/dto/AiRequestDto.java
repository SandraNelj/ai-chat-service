package org.example.aichatservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class AiRequestDto {
    private String model;
    private List<MessageDto> messages;
}


