package com.example.demo.domain.controller;

import com.example.demo.domain.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class BrokerController {

    private final SimpMessagingTemplate template;

    @MessageMapping("/room/{roomId}/entered")
    public void entered(@DestinationVariable(value = "roomId") String roomId, Message message){
        log.info("# roomId = {}", roomId);
        log.info("# message = {}", message);
        final String payload = message.getSenderName() + "님이 입장하셨습니다.";
        template.convertAndSend("/sub/room/" + roomId, payload);
    }

    @MessageMapping("/room/{roomId}")
    public void sendMessage(@DestinationVariable(value = "roomId") String roomId, Message message) {
        log.info("# roomId = {}", roomId);
        log.info("# message = {}", message);

        template.convertAndSend("/sub/room/" + roomId, message.getMessage());
    }
}
