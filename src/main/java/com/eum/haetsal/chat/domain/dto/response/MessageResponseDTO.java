package com.eum.haetsal.chat.domain.dto.response;

import com.eum.haetsal.chat.domain.model.Message;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class MessageResponseDTO {

    @Schema(description = "보내는 사람 정보" , example = "1234")
    private SenderInfo senderInfo;

    @Schema(description = "채팅 메시지 타입" , example = "CHAT/JOIN/LEAVE")
    private Message.MessageType type;

    @Schema(description = "채팅 메시지 내용" , example = "안녕하세요~~")
    private String message;
    private LocalDateTime createdAt;

    @Getter
    @Setter
    @AllArgsConstructor
    public static class SenderInfo {
        private Long userId;
        private String profileImage;
        private String nickName;
        private boolean isDeleted;
    }
}
