package com.eum.haetsal.chat.domain.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;


@Getter
@Setter
public class ChatUserResponseDto {

    List<HaetsalResponseDto.UserInfo> userInfos;

    HaetsalResponseDto.PostInfo postInfo;

    List<MessageResponseDTO> messages;

    public ChatUserResponseDto(List<HaetsalResponseDto.UserInfo> userInfos,
                               HaetsalResponseDto.PostInfo postInfo,
                               List<MessageResponseDTO> messages) {
        this.userInfos = userInfos;
        this.postInfo = postInfo;
        this.messages = messages;
    }
}
