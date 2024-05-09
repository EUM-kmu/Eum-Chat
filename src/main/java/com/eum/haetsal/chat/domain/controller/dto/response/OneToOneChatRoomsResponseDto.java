package com.eum.haetsal.chat.domain.controller.dto.response;

import com.eum.haetsal.chat.domain.model.ChatRoom;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OneToOneChatRoomsResponseDto {
    private String roomId;
    private int postId;

    public OneToOneChatRoomsResponseDto(ChatRoom chatRoom) {
        this.roomId = chatRoom.getId();
        this.postId = chatRoom.getPostId();
    }
}
