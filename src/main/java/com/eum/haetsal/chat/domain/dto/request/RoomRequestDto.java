package com.eum.haetsal.chat.domain.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RoomRequestDto {

    @Schema(description = "채팅에 참가하는 사람들 id(글쓴이 포함)" , example = "")
    private List<String> memberIds;
}
