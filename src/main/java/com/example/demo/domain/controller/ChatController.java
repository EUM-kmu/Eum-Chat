package com.example.demo.domain.controller;

import com.example.demo.domain.Message;
import com.example.demo.domain.dto.MessageRequestDTO;
import com.example.demo.domain.dto.MessageResponseDTO;
import com.example.demo.domain.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @PostMapping
    public MessageResponseDTO createMessage(@RequestBody MessageRequestDTO requestDTO) {
        return chatService.saveMessage(requestDTO);
    }

    @GetMapping
    public List<MessageResponseDTO> getAllMessages() {
        return chatService.getAllMessages();
    }


/*
    @MessageMapping("/chat/{roomNo}")
    @SendTo("/sub/chat/{roomNo}")
    public ChatResponse broadcasting(final ChatRequest request,
                                     @DestinationVariable(value = "roomNo") final Long chatRoomNo) {

        log.info("{roomNo : {}, request : {}}", chatRoomNo, request);
        return chatFacade.doChat(request, chatRoomNo);
    }

    private final SimpMessagingTemplate template;

    @MessageMapping("/chat/{roomId}/entered")
    @SendTo("/sub/chat/{roomNo}")
    public void entered(@DestinationVariable(value = "roomId") String roomId, Message message){
        log.info("# roomId = {}", roomId);
        log.info("# message = {}", message);
        final String payload = message.getSenderName() + "님이 입장하셨습니다.";
        template.convertAndSend("/sub/room/" + roomId, payload);
    }

    @MessageMapping("/chat/{roomId}")
    @SendTo("/sub/chat/{roomNo}")
    public void sendMessage(@DestinationVariable(value = "roomId") String roomId, Message message) {
        log.info("# roomId = {}", roomId);
        log.info("# message = {}", message);

        template.convertAndSend("/sub/room/" + roomId, message.getMessage());
    }
 */

}
