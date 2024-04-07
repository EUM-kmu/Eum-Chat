package com.example.demo.domain.dto;

import com.example.demo.domain.model.ChatRoom;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoomResponseDto {
    private String roomId;
    private int memberCount;

    public RoomResponseDto(ChatRoom chatRoom) {
        this.roomId = chatRoom.getId();
        this.memberCount = chatRoom.getMembers().size();
    }
}
