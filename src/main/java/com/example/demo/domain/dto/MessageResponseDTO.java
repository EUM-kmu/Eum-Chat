package com.example.demo.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class MessageResponseDTO {

    @Schema(description = "메시지 id" , example = "1")
    private String id;

    @Schema(description = "채팅방 id" , example = "1")
    private String chatRoomId;

    @Schema(description = "보내는 사람 닉네임" , example = "홍길동")
    private String senderName;

    @Schema(description = "보내는 사람 uuid" , example = "1234")
    private String senderUuid;

    @Schema(description = "채팅 메시지 내용" , example = "안녕하세요~~")
    private String message;
    private LocalDateTime createdAt;

    public MessageResponseDTO(String id, String chatRoomId, String senderName, String senderUuid, String message, LocalDateTime createdAt) {
        this.id = id;
        this.chatRoomId = chatRoomId;
        this.senderName = senderName;
        this.senderUuid = senderUuid;
        this.message = message;
        this.createdAt = createdAt;
    }
}
