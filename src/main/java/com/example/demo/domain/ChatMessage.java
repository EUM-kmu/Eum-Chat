package com.example.demo.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatMessage {
    private MessageType type; // 메시지 유형 (예: CHAT, JOIN, LEAVE)
    private String message;
    private String senderName;

    public enum MessageType {
        CHAT,
        JOIN,
        LEAVE
    }
}
