package com.eum.haetsal.chat.domain.model;

import com.eum.haetsal.chat.domain.controller.dto.request.RoomRequestDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.util.List;

@Getter
@Setter
@Document("chatRoom")
@NoArgsConstructor
public class ChatRoom {

    @Id
    @Field(value = "_id", targetType = FieldType.OBJECT_ID)
    private String id;

    private int postId;

    private String creatorId;

    private List<String> members;

    private List<String> membersHistory;

    public ChatRoom(RoomRequestDto dto, String userId) {
        this.postId = dto.getPostId();
        this.creatorId = userId;
        this.members = dto.getMemberIds();
    }
}
