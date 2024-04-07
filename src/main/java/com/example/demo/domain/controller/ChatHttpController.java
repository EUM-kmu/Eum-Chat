package com.example.demo.domain.controller;

import com.example.demo.domain.base.BaseResponseEntity;
import com.example.demo.domain.dto.MessageRequestDTO;
import com.example.demo.domain.dto.MessageResponseDTO;
import com.example.demo.domain.dto.RoomRequestDto;
import com.example.demo.domain.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

// Http 통신을 위한 controller

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chats")
public class ChatHttpController {

    private final ChatService chatService;

    @PostMapping("")
    @Operation(summary = "채팅방을 생성합니다.")
    public BaseResponseEntity createChatRoom(@RequestBody RoomRequestDto dto, @RequestHeader String userId) {
        BaseResponseEntity response = chatService.createChatRoom(dto, userId);
        return response;
    }

    @GetMapping("")
    @Operation(summary = "특정 유저가 속한 채팅방 목록을 불러옵니다.")
    public BaseResponseEntity getMyChatRooms(@RequestHeader String userId) {
        BaseResponseEntity response = chatService.getChatRoomsById(userId);
        return response;
    }


    @PostMapping("/{roomIdx}/message")
    @Operation(summary = "채팅을 보냅니다.")
    public MessageResponseDTO createMessage(@RequestBody MessageRequestDTO requestDTO) {
        return chatService.saveMessage(requestDTO);
    }

    @GetMapping("/{roomIdx}")
    @Operation(summary = "특정 채팅방의 전체 채팅 내역을 확인합니다.")
    public List<MessageResponseDTO> getMessagesByRoomIdx(
            @Parameter(description = "채팅 내역을 확인할 room 의 id", example = "1", required = true)
            @PathVariable String chatRoomId
    ) {
        return chatService.getAllMessagesByRoomIdx(chatRoomId);
    }

}
