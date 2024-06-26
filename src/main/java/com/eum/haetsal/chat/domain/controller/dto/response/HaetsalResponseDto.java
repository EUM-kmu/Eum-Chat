package com.eum.haetsal.chat.domain.controller.dto.response;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

public class HaetsalResponseDto {

    @Getter
    @Setter
    @AllArgsConstructor
    public static class PostInfo{
        private Long postId;
        private String title;
        private String content;
        private String location;
        private String startDate;
        private String  createdDate;
        private String status;
        private Long dealId;
        private Long pay;
        private boolean deleted;
        private UserInfo userInfo;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class UserInfo{
        private Long userId;
        private Long profileId;
        private String profileImage;
        private String nickName;
        private String address;
        private String accountNumber;
        private boolean deleted;

        // 추가
        private boolean isCreator;
    }

}
