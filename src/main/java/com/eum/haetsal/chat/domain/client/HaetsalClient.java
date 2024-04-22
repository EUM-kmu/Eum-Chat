package com.eum.haetsal.chat.domain.client;


import com.eum.haetsal.chat.domain.dto.request.ChatPostRequestDto;
import com.eum.haetsal.chat.domain.dto.request.ChatRequestDTO;
import com.eum.haetsal.chat.domain.dto.request.ChatUserRequestDto;
import com.eum.haetsal.chat.domain.dto.response.ChatResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "haetsal-service", url = "http://${HAETSAL_IP}:${HAETSAL_PORT}")
public interface HaetsalClient {

    @PostMapping ("/haetsal-service/api/v2/chat/posts")
    List<ChatResponseDTO.PostInfo> getChatPost(@RequestBody ChatRequestDTO.PostIdList postIdList);

    @PostMapping ("/haetsal-service/api/v2/chat/users")
    List<ChatResponseDTO.UserInfo> getChatUser(@RequestBody ChatRequestDTO.UserIdList userIdList) ;
}
