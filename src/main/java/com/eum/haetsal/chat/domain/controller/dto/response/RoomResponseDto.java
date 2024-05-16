package com.eum.haetsal.chat.domain.controller.dto.response;

import com.eum.haetsal.chat.domain.model.ChatRoom;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;

import java.util.List;

public class RoomResponseDto {

    @Getter
    @Setter
    public static class Room{
        private String roomId;
        private int postId;
        private int memberCount;

        public Room(ChatRoom chatRoom) {
            this.roomId = chatRoom.getId();
            this.postId = chatRoom.getPostId();
            this.memberCount = chatRoom.getMembers().size();
        }
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class RoomIds{
        private List<String> ids;
    }

    @Getter
    @Setter
    public static class RoomId{
        private ObjectId id;

        public String getId() {
            return id.toHexString();  // ObjectId를 문자열로 변환
        }
    }
}
