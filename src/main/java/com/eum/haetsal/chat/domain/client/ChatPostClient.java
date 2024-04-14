package com.eum.haetsal.chat.domain.client;


import com.eum.haetsal.chat.domain.dto.request.ChatPostRequestDto;
import com.eum.haetsal.chat.domain.dto.request.ChatRequestDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "haetsal-service", url = "http://223.130.146.39/")
public interface ChatPostClient {

    @PostMapping ("/haetsal-service/api/v2/chat/posts")
    List<ChatPostRequestDto> getChatPost(@RequestBody ChatRequestDTO.PostIdList postIdList) ;
}
