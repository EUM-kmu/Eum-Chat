package com.eum.haetsal.chat.domain.model;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.time.LocalDateTime;

@Getter
@Setter
@Document(collection = "message")
@Builder
@AllArgsConstructor
public class Message {

    @Id
    @Field(value = "_id", targetType = FieldType.OBJECT_ID)
    private String id;

    @Field("chat_room_id")
    private String chatRoomId;

    @Field("user_id")
    private String userId;

    @Field("message_type")
    private MessageType type;

    @Field("message")
    private String message;

    @Field("created_at")
    @CreatedDate
    private LocalDateTime createdAt;

    public enum MessageType {
        CHAT,
        JOIN,
        LEAVE
    }

    public static Message from(String chatRoomId, String userId, MessageType type, String message){
        return Message.builder()
                .chatRoomId(chatRoomId)
                .userId(userId)
                .type(type)
                .message(message)
                .createdAt(LocalDateTime.now())
                .build();

    }
}
