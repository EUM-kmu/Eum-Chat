package com.eum.haetsal.chat.domain.controller.dto.request;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatUserRequestDto {
    private int userId;
    private int profileId;
    private String profileImage;
    private String nickName;
    private String accountNumber;
}
