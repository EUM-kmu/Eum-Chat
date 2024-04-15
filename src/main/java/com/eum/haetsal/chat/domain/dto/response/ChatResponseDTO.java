package com.eum.haetsal.chat.domain.dto.response;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

public class ChatResponseDTO {

    @Getter
    @Setter
    @AllArgsConstructor
    public static class PostInfo{
        private Long postId;
        private String status;
        private String title;
        private Long pay;
        private Long dealId;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class UserInfo{
        private Long userId;
        private Long profileId;
        private String profileImage;
        private String nickName;
        private String accountNumber;
    }

}
