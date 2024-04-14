package com.eum.haetsal.chat.domain.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatPostRequestDto {
    private int postId;
    private String status;
    private String title;
}
