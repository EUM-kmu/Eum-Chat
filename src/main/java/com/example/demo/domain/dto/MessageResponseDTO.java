package com.example.demo.domain.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class MessageResponseDTO {
    private String id;
    private Long roomIdx;
    private String senderName;
    private String senderUuid;
    private String message;
    private LocalDateTime createdAt;

    public MessageResponseDTO(String id, Long roomIdx, String senderName, String senderUuid, String message, LocalDateTime createdAt) {
        this.id = id;
        this.roomIdx = roomIdx;
        this.senderName = senderName;
        this.senderUuid = senderUuid;
        this.message = message;
        this.createdAt = createdAt;
    }
}
