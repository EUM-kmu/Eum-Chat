package com.example.demo.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class MessageResponseDTO {

    @Schema(description = "보내는 사람 uuid" , example = "1234")
    private String senderUuid;

    @Schema(description = "채팅 메시지 내용" , example = "안녕하세요~~")
    private String message;
    private LocalDateTime createdAt;

    public MessageResponseDTO(String senderUuid, String message, LocalDateTime createdAt) {
        this.senderUuid = senderUuid;
        this.message = message;
        this.createdAt = createdAt;
    }
}
