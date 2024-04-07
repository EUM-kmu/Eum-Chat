package com.example.demo.domain.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
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

    @DBRef(db="chatRoom", lazy = true)
    @Field("chat_room_id")
    private String chatRoomId;

    @Field("sender_name")
    private String senderName;

    @Field("sender_uuid")
    private String senderUuid;

    @Field("message")
    private String message;

    @Field("created_at")
    @CreatedDate
    private LocalDateTime createdAt;

//    @Field("img_url")
//    private String imgUrl;

//    @Field("updated_at")
//    @LastModifiedDate
//    private LocalDateTime updatedAt;

}
