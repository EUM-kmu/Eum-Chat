package com.eum.haetsal.chat.domain.client;


import com.eum.haetsal.chat.domain.controller.dto.request.HaetsalRequestDto;
import com.eum.haetsal.chat.domain.controller.dto.response.HaetsalResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "haetsal-service", url = "${haetsal-url}")
public interface HaetsalClient {

    @PostMapping ("/haetsal-service/api/v2/chat/posts")
    List<HaetsalResponseDto.PostInfo> getChatPost(@RequestBody HaetsalRequestDto.PostIdList postIdList);

    @PostMapping ("/haetsal-service/api/v2/chat/users")
    List<HaetsalResponseDto.UserInfo> getChatUser(@RequestBody HaetsalRequestDto.UserIdList userIdList) ;
}
