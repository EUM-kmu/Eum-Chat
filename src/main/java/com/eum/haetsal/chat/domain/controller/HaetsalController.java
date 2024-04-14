package com.eum.haetsal.chat.domain.controller;

import com.eum.haetsal.chat.domain.base.BaseResponseEntity;
import com.eum.haetsal.chat.domain.service.ChatService;
import com.eum.haetsal.chat.domain.service.HaetsalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/haetsal")
@Tag(name = "Haetsal" ,description = "햇살 서버와 통신하기 위한 api")
public class HaetsalController {

    private final HaetsalService haetsalService;

    @GetMapping("/OneToOneChat")
    @Operation(summary = "두 유저가 1:1 채팅 중인 채팅방의 id(s)를 불러옵니다.")
    public BaseResponseEntity<?> getOneToOneChatRooms(
            @RequestParam String myId,
            @RequestParam String theOtherId
    ) {
        BaseResponseEntity response =  haetsalService.getOneToOneChatRooms(myId, theOtherId);
        return response;
    }
}
