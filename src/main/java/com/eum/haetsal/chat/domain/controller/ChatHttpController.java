package com.eum.haetsal.chat.domain.controller;

import com.eum.haetsal.chat.domain.dto.request.RoomRequestDto;
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
@Tag(name = "Chat" ,description = "chat api")
public class ChatHttpController {

    private final ValidationService validationService;
    private final ChatService chatService;

    @PostMapping("")
    @Operation(summary = "채팅방을 생성합니다.")
    public BaseResponseEntity<?> createChatRoom(
            @RequestBody RoomRequestDto dto,
            @RequestHeader String userId
    ) {
        BaseResponseEntity response = chatService.createChatRoom(dto, userId);
        return response;
    }

    @GetMapping("")
    @Operation(summary = "특정 유저가 속한 채팅방 목록을 불러옵니다.")
    public BaseResponseEntity<?> getMyChatRooms(@RequestHeader String userId) {
        BaseResponseEntity response = chatService.getChatRooms(userId);
        return response;
    }

    @GetMapping("/{chatRoomId}")
    @Operation(summary = "채팅방 내부. 유저가 속한 특정 채팅방의 전체 채팅 내역을 확인합니다.")
    public BaseResponseEntity<?> getMessagesAndUserInfo(
            @PathVariable String chatRoomId,
            @RequestHeader String userId,
            @RequestParam(value = "pagingIndex", defaultValue = "0") int pageNumber,
            @RequestParam(value = "pagingSize", defaultValue = "30") int pageSize
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

    @GetMapping("/{chatRoomId}/members")
    @Operation(summary = "유저가 속한 특정 채팅방의 멤버 목록을 불러옵니다.")
    public BaseResponseEntity<?> getMembers(
            @PathVariable String chatRoomId,
            @RequestHeader String userId
    ) {

        BaseResponseEntity<String> validateResponse = validationService.validateChatRoomAccess(chatRoomId, userId);
        if(validateResponse != null){
            return validateResponse;
        }

        BaseResponseEntity response =  chatService.getMembers(chatRoomId);
        return response;
    }
    

    @PatchMapping("/{chatRoomId}/members")
    @Operation(summary = "특정 채팅방에 새로운 유저를 추가/삭제합니다.")
    public BaseResponseEntity<?> updateMembers(
            @PathVariable String chatRoomId,
            @RequestBody RoomRequestDto dto,
            @RequestHeader String userId
    ) {

        BaseResponseEntity<String> validateResponse = validationService.validateChatRoomAccess(chatRoomId, userId);
        if(validateResponse != null){
            return validateResponse;
        }

        BaseResponseEntity response = chatService.updateMembers(chatRoomId, dto, userId);
        return response;
    }

}
