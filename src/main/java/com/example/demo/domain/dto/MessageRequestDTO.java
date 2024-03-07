package com.example.demo.domain.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MessageRequestDTO {
    private Long roomIdx;
    private String senderName;
    private String senderUuid; // TODO: uuid type?
    private String message;
}
