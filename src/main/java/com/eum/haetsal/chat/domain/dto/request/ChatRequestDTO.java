package com.eum.haetsal.chat.domain.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

public class ChatRequestDTO {
    @Getter
    @Setter
    @AllArgsConstructor
    public static class PostIdList{
        private List<Integer> postIdList;
    }
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserIdList{
        private List<String> userIdList;
    }
}
