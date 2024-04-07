package com.example.demo.domain.service;

import com.example.demo.domain.model.ChatMessage;
import com.example.demo.domain.model.ChatRoom;
import com.example.demo.domain.model.Message;
import com.example.demo.domain.base.BaseResponseEntity;
import com.example.demo.domain.dto.MessageRequestDTO;
import com.example.demo.domain.dto.MessageResponseDTO;
import com.example.demo.domain.dto.RoomRequestDto;
import com.example.demo.domain.dto.RoomResponseDto;
import com.example.demo.domain.repository.ChatRepository;
import com.example.demo.domain.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ChatService {

    private final ChatRepository chatRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public BaseResponseEntity<?> saveMessage(String content, String userId, String chatRoomId) {

        Message message = new Message();
        message.setChatRoomId(chatRoomId);
//        message.setSenderName(dto.getSenderName());
        message.setSenderUuid(userId);
        message.setMessage(content);
        message.setCreatedAt(LocalDateTime.now());

        try {
            message = chatRepository.save(message);

            try{
                // 저장된 메시지를 바탕으로 ChatMessage 객체 생성 및 브로드캐스트
                ChatMessage chatMessage = convertToChatMessage(message);
                broadcastMessage(chatMessage, chatRoomId);

                return new BaseResponseEntity<>(HttpStatus.OK);

            }catch (Exception e){
                return new BaseResponseEntity<>(e);
            }

        } catch (Exception e){
            return new BaseResponseEntity<>(e);
        }

    }

    private ChatMessage convertToChatMessage(Message message) {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setSenderId(message.getSenderUuid());
        chatMessage.setMessage(message.getMessage());
        chatMessage.setType(ChatMessage.MessageType.CHAT); // 메시지 타입 설정.. TODO: join, leave 는 아직 안함
        chatMessage.setCreatedAt(message.getCreatedAt());
        return chatMessage;
    }

    private void broadcastMessage(ChatMessage chatMessage, String chatRoomId) {
        // 특정 채팅방 구독자들에게 메시지를 브로드캐스트
        messagingTemplate.convertAndSend("/sub/room/" + chatRoomId, chatMessage);
    }

    public BaseResponseEntity<?> getAllMessagesByRoomIdx(String chatRoomId, String userId) {

        if(chatRoomId.length() != 24){
            return new BaseResponseEntity<>(HttpStatus.BAD_REQUEST, "chatRoomId를 확인해주세요.");
        }

        ChatRoom chatRoom = chatRoomRepository.findMembersById(chatRoomId);

        // 채팅방 있는지 확인
        if(chatRoom == null){
            return new BaseResponseEntity<>(HttpStatus.BAD_REQUEST, "해당 채팅방이 없습니다.");
        }

        // 해당 채팅방에 유저가 속해 있는지 확인
        boolean isContained = chatRoom.getMembers().contains(userId);
        if(!isContained){
            return new BaseResponseEntity<>(HttpStatus.FORBIDDEN, "해당 채팅방의 참여자가 아닙니다.");
        }

        List<MessageResponseDTO> messages;
        messages = chatRepository.findByChatRoomId(chatRoomId).stream()
                .map(form -> new MessageResponseDTO(
                        form.getSenderUuid(),
                        form.getMessage(),
                        form.getCreatedAt()
                )).collect(Collectors.toList());
        return new BaseResponseEntity<>(HttpStatus.OK, messages);
    }

    public BaseResponseEntity<?> createChatRoom(RoomRequestDto dto, String userId) {

        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setMembers(dto.getMemberIds());
        chatRoom.getMembers().add(0,userId);

        try{
            chatRoomRepository.save(chatRoom);
            return new BaseResponseEntity<>(HttpStatus.OK, new RoomResponseDto(chatRoom));
        }catch (Exception e){
            return new BaseResponseEntity<>(e);
        }
    }

    public BaseResponseEntity<Stream<RoomResponseDto>> getChatRoomsById(String userId) {
        List<ChatRoom> myRooms;
        myRooms = chatRoomRepository.findAllById(userId);
        return new BaseResponseEntity<>(HttpStatus.OK, myRooms.stream().map(RoomResponseDto::new));
    }
}
