package com.example.demo.domain.service;

import com.example.demo.domain.ChatMessage;
import com.example.demo.domain.Message;
import com.example.demo.domain.dto.MessageRequestDTO;
import com.example.demo.domain.dto.MessageResponseDTO;
import com.example.demo.domain.repository.ChatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ChatService {

    private final ChatRepository chatRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public MessageResponseDTO saveMessage(MessageRequestDTO requestDTO) {
        Message message = new Message();
        message.setRoomIdx(requestDTO.getRoomIdx());
        message.setSenderName(requestDTO.getSenderName());
        message.setSenderUuid(requestDTO.getSenderUuid());
        message.setMessage(requestDTO.getMessage());
        message.setCreatedAt(LocalDateTime.now());
        message = chatRepository.save(message);
        // 저장된 메시지를 바탕으로 ChatMessage 객체 생성 및 브로드캐스트
        ChatMessage chatMessage = convertToChatMessage(message);
        broadcastMessage(chatMessage, chatMessage.getRoomIdx());

        return new MessageResponseDTO(
                message.getId(),
                message.getRoomIdx(),
                message.getSenderName(),
                message.getSenderUuid(),
                message.getMessage(),
                message.getCreatedAt()
        );
    }

    private ChatMessage convertToChatMessage(Message message) {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setRoomIdx(message.getRoomIdx());
        chatMessage.setSenderName(message.getSenderName());
        chatMessage.setMessage(message.getMessage());
        chatMessage.setType(ChatMessage.MessageType.CHAT); // 메시지 타입 설정.. TODO: join, leave 는 아직 안함
        chatMessage.setCreatedAt(message.getCreatedAt());
        return chatMessage;
    }

    private void broadcastMessage(ChatMessage chatMessage, Long roomIdx) {
        // 특정 채팅방 구독자들에게 메시지를 브로드캐스트
        messagingTemplate.convertAndSend("/sub/room/" + roomIdx, chatMessage);
    }

    public List<MessageResponseDTO> getAllMessagesByRoomIdx(Long roomIdx) {
        return chatRepository.findByRoomIdx(roomIdx).stream()
                .map(message -> new MessageResponseDTO(
                        message.getId(),
                        message.getRoomIdx(),
                        message.getSenderName(),
                        message.getSenderUuid(),
                        message.getMessage(),
                        message.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }
}
