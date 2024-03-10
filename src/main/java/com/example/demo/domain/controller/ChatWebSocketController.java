package com.example.demo.domain.controller;

import com.example.demo.domain.ChatMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

// WebSocket 통신을 위한 controller
@Slf4j
@Controller
public class ChatWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;

    public ChatWebSocketController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /*
    TODO: 사용자 입장 시 알림.. 아직까진 새로운 사람이 들어올 경우가 없음(v1.0)
    @MessageMapping("/chat.join")
    public void joinChatRoom(@Payload ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) {

        String roomIdx = headerAccessor.getSessionAttributes().get("roomIdx").toString();
        chatMessage.setType(ChatMessage.MessageType.JOIN);
        chatMessage.setMessage(chatMessage.getSenderName() + "님이 입장하셨습니다.");
        broadcastUserStatus(chatMessage, roomIdx);
    }

     */

    @MessageMapping("/chat.leave")
    public void leaveChatRoom(@Payload ChatMessage chatMessage) {
        Long roomIdx = chatMessage.getRoomIdx();
        chatMessage.setType(ChatMessage.MessageType.LEAVE);
        chatMessage.setMessage(chatMessage.getSenderName() + "님이 퇴장하셨습니다.");
        chatMessage.setCreatedAt(LocalDateTime.now());
        broadcastUserStatus(chatMessage, roomIdx);
    }

    private void broadcastUserStatus(ChatMessage chatMessage, Long roomIdx) {
        messagingTemplate.convertAndSend("/sub/room/" + roomIdx, chatMessage);
    }
}
