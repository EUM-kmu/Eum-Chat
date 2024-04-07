package com.example.demo.domain.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ChatMessage {
    private String senderId;
    private MessageType type;
    private String message;
    private LocalDateTime createdAt;

    public enum MessageType {
        CHAT,
        JOIN,
        LEAVE
    }
}
