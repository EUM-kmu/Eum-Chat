package com.example.demo.domain.model;

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
@NoArgsConstructor
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

}
