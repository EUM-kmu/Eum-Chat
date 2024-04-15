package com.eum.haetsal.chat.domain.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class MessageResponseDTO {

    @Schema(description = "보내는 사람 정보" , example = "1234")
    private SenderInfo senderInfo;

    @Schema(description = "채팅 메시지 내용" , example = "안녕하세요~~")
    private String message;
    private LocalDateTime createdAt;

    public MessageResponseDTO(SenderInfo senderInfo, String message, LocalDateTime createdAt) {
        this.senderInfo = senderInfo;
        this.message = message;
        this.createdAt = createdAt;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class SenderInfo {
        private Long userId;
        private String profileImage;
        private String nickName;
    }
}
