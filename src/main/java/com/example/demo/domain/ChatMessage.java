package com.example.demo.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ChatMessage {
    private MessageType type;
    private Long roomIdx; // TODO: roomIdx type?
    private String message;
    private String senderName;
    private LocalDateTime createdAt;

    public enum MessageType {
        CHAT,
        JOIN,
        LEAVE
    }
}
