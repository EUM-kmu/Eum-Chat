package com.eum.haetsal.chat.domain.client;


import com.eum.haetsal.chat.domain.dto.request.HaetsalRequestDto;
import com.eum.haetsal.chat.domain.dto.response.HaetsalResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

//@FeignClient(name = "haetsal-service", url = "http://223.130.146.39:8000") 운영서버
@FeignClient(name = "haetsal-service", url = "http://175.45.203.201:8000")
public interface HaetsalClient {

    @PostMapping ("/haetsal-service/api/v2/chat/posts")
    List<HaetsalResponseDto.PostInfo> getChatPost(@RequestBody HaetsalRequestDto.PostIdList postIdList);

    @PostMapping ("/haetsal-service/api/v2/chat/users")
    List<HaetsalResponseDto.UserInfo> getChatUser(@RequestBody HaetsalRequestDto.UserIdList userIdList) ;
}
