package com.eum.haetsal.chat.domain.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MemberIdsResponseDto {
    @Schema(description = "참여자 id List" , example = "")
    private List<String> userIds;

    public MemberIdsResponseDto(List<String> members) {
        this.userIds = members;
    }
}
