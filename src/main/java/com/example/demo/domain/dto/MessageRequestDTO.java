package com.example.demo.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MessageRequestDTO {

    @Schema(description = "채팅방 id" , example = "1")
    private String chatRoomId;

    @Schema(description = "보내는 사람 닉네임" , example = "홍길동")
    private String senderName;

    @Schema(description = "보내는 사람 uuid" , example = "1234")
    private String senderUuid; // TODO: uuid type?

    @Schema(description = "채팅 메시지 내용" , example = "안녕하세요~~")
    private String message;
}
