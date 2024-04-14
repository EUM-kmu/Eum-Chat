package com.eum.haetsal.chat.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.time.LocalDateTime;

@Getter
@Setter
@Document(collection = "message")
public class Message {

    @Id
    @Field(value = "_id", targetType = FieldType.OBJECT_ID)
    private String id;

    @Field("chat_room_id")
    private String chatRoomId;

    @Field("user_id")
    private String userId;

    @Field("message")
    private String message;

    @Field("created_at")
    @CreatedDate
    private LocalDateTime createdAt;

    public Message(String chatRoomId, String userId, String message, LocalDateTime createdAt) {
        this.chatRoomId = chatRoomId;
        this.userId = userId;
        this.message = message;
        this.createdAt = createdAt;
    }
}
