package com.eum.haetsal.chat.domain.controller;

import com.eum.haetsal.chat.domain.dto.request.RoomRequestDto;
import com.eum.haetsal.chat.domain.service.ChatService;
import com.eum.haetsal.chat.domain.base.BaseResponseEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

// Http 통신을 위한 controller

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chats")
@Tag(name = "Chat" ,description = "chat api")
public class ChatHttpController {

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
            @Parameter(description = "채팅 내역을 확인할 room 의 id", example = "1", required = true)
            @PathVariable String chatRoomId,
            @RequestHeader String userId
    ) {
        BaseResponseEntity response =  chatService.getMessagesAndUserInfo(chatRoomId, userId);
        return response;
    }


    @PostMapping("/{chatRoomId}/message")
    @Operation(summary = "채팅을 보냅니다.")
    public BaseResponseEntity<?> createMessage(
            @RequestBody String message,
            @RequestHeader String userId,
            @PathVariable String chatRoomId
    ) {
        BaseResponseEntity response = chatService.saveMessage(message, userId, chatRoomId);
        return response;
    }

    @GetMapping("/{chatRoomId}/members")
    @Operation(summary = "유저가 속한 특정 채팅방의 멤버 목록을 불러옵니다.")
    public BaseResponseEntity<?> getMembers(
            @PathVariable String chatRoomId,
            @RequestHeader String userId
    ) {
        BaseResponseEntity response =  chatService.getMembers(chatRoomId, userId);
        return response;
    }

    @PostMapping("/{chatRoomId}/add")
    @Operation(summary = "특정 채팅방에 새로운 유저를 초대/추가합니다.")
    public BaseResponseEntity<?> addUser(
            @PathVariable String chatRoomId,
            @RequestBody RoomRequestDto dto,
            @RequestHeader String userId
    ) {
        BaseResponseEntity response = chatService.addUser(chatRoomId, dto, userId);
        return response;
    }

}
