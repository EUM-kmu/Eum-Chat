package com.eum.haetsal.chat.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class ChatMessage {
    private String userId;
    private MessageType type;
    private String message;
    private LocalDateTime createdAt;

    public enum MessageType {
        CHAT,
        JOIN,
        LEAVE
    }
}
