package com.eum.haetsal.chat.domain.controller;

import com.eum.haetsal.chat.domain.service.ChatService;
import com.eum.haetsal.chat.domain.base.BaseResponseEntity;
import com.eum.haetsal.chat.domain.service.ValidationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/chat-service/api/chats")
@Tag(name = "채팅" ,description = "채팅 api")
public class ChatController {

    private final ValidationService validationService;
    private final ChatService chatService;

    @GetMapping("/{chatRoomId}")
    @Operation(summary = "채팅방 내부. 유저가 속한 특정 채팅방의 전체 채팅 내역을 확인합니다.")
    public BaseResponseEntity<?> getMessagesAndUserInfo(
            @PathVariable String chatRoomId,
            @RequestHeader String userId,
            @RequestParam(value = "pagingIndex", defaultValue = "0") int pageNumber,
            @RequestParam(value = "pagingSize", defaultValue = "20") int pageSize
    ) {

        BaseResponseEntity<String> validateResponse = validationService.validateChatRoomAccess(chatRoomId, userId);
        if(validateResponse != null){
            return validateResponse;
        }

        BaseResponseEntity response =  chatService.getMessagesAndUserInfo(chatRoomId, pageNumber, pageSize);
        return response;
    }


    @PostMapping("/{chatRoomId}/message")
    @Operation(summary = "채팅을 보냅니다.")
    public BaseResponseEntity<?> createMessage(
            @RequestBody String message,
            @RequestHeader String userId,
            @PathVariable String chatRoomId
    ) {

        BaseResponseEntity<String> validateResponse = validationService.validateChatRoomAccess(chatRoomId, userId);
        if(validateResponse != null){
            return validateResponse;
        }

        BaseResponseEntity response = chatService.saveMessage(message, userId, chatRoomId);
        return response;
    }

}
