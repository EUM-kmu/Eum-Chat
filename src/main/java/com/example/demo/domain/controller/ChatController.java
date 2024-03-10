package com.example.demo.domain.controller;

import com.example.demo.domain.dto.MessageRequestDTO;
import com.example.demo.domain.dto.MessageResponseDTO;
import com.example.demo.domain.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Http 통신을 위한 controller

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/chat")
public class ChatController {

    private final ChatService chatService;

    @PostMapping
    public MessageResponseDTO createMessage(@RequestBody MessageRequestDTO requestDTO) {
        return chatService.saveMessage(requestDTO);
    }

    @GetMapping
    public List<MessageResponseDTO> getAllMessages() {
        return chatService.getAllMessages();
    }

}
