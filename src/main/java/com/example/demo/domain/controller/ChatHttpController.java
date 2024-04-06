package com.example.demo.domain.controller;

import com.example.demo.domain.base.BaseResponseEntity;
import com.example.demo.domain.dto.MessageRequestDTO;
import com.example.demo.domain.dto.MessageResponseDTO;
import com.example.demo.domain.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Http 통신을 위한 controller

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chats")
public class ChatHttpController {

    private final ChatService chatService;

    @PostMapping("/{roomIdx}/message")
    @Operation(summary = "채팅을 보냅니다.")
    public MessageResponseDTO createMessage(@RequestBody MessageRequestDTO requestDTO) {
        return chatService.saveMessage(requestDTO);
    }

    @GetMapping("/{roomIdx}")
    @Operation(summary = "특정 채팅방의 전체 채팅 내역을 확인합니다.")
    public List<MessageResponseDTO> getMessagesByRoomIdx(
            @Parameter(description = "채팅 내역을 확인할 room 의 id", example = "1", required = true)
            @PathVariable Long roomIdx
    ) {
        return chatService.getAllMessagesByRoomIdx(roomIdx);
    }

}
