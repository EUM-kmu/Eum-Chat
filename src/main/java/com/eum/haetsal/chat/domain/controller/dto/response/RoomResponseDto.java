package com.eum.haetsal.chat.domain.controller.dto.response;

import com.eum.haetsal.chat.domain.model.ChatRoom;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoomResponseDto {
    private String roomId;
    private int postId;
    private int memberCount;

    public RoomResponseDto(ChatRoom chatRoom) {
        this.roomId = chatRoom.getId();
        this.postId = chatRoom.getPostId();
        this.memberCount = chatRoom.getMembers().size();
    }
}
