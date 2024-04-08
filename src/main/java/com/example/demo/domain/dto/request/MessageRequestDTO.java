package com.example.demo.domain.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MessageRequestDTO {

    @Schema(description = "채팅 메시지 내용" , example = "안녕하세요~~")
    private String message;
}
