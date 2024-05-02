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
    private String creatorNickname;
    private String startDate;
    private String location;
    private int memberCount;

    public ChatPostResponseDto(ChatResponseDTO.PostInfo dto, ChatRoom chatRoom) {
        this.postId = dto.getPostId();
        this.status = dto.getStatus();
        this.title = dto.getTitle();
        this.roomId = chatRoom.getId();
        this.creatorId = chatRoom.getCreatorId();
        this.creatorNickname = dto.getUserInfo().getNickName();
        this.startDate = dto.getStartDate();
        this.location = dto.getLocation();
        this.memberCount = chatRoom.getMembers().size();
    }
}
