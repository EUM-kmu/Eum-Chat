package com.eum.haetsal.chat.domain.dto.request;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatUserRequestDto {
    private int profileId;
    private String nickname;
    private String profileImage;
    private String address;
    private String gender;
    private int ageRange;
}
