package com.eum.haetsal.chat.domain.model;

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

    private int postId; // 채팅과 연결된 게시글 id

    private List<String> members;
}
