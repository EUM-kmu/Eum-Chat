package com.eum.haetsal.chat.domain.dto.response;

import com.eum.haetsal.chat.domain.dto.request.ChatPostRequestDto;
import com.eum.haetsal.chat.domain.model.ChatRoom;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatPostResponseDto {
    private Long postId;
    private String status;
    private String title;
    private String roomId;
    private String creatorId;
    private int memberCount;

    public ChatPostResponseDto(ChatResponseDTO.PostInfo dto, ChatRoom chatRoom) {
        this.postId = dto.getPostId();
        this.status = dto.getStatus();
        this.title = dto.getTitle();
        this.roomId = chatRoom.getId();
        this.creatorId = chatRoom.getCreatorId();
        this.memberCount = chatRoom.getMembers().size();
    }
}
