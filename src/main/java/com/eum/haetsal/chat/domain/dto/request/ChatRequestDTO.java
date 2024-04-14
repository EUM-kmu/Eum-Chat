package com.eum.haetsal.chat.domain.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class ChatRequestDTO {
    @Getter
    @Setter
    public static class PostIdList{
        private List<String> postIdList;
    }
    @Getter
    @Setter
    public static class UserIdList{
        private List<String> userIdList;
    }
}
