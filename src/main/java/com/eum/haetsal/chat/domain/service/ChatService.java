package com.eum.haetsal.chat.domain.service;

import com.eum.haetsal.chat.domain.client.HaetsalClient;
import com.eum.haetsal.chat.domain.dto.request.ChatPostRequestDto;
import com.eum.haetsal.chat.domain.dto.request.ChatRequestDTO;
import com.eum.haetsal.chat.domain.dto.request.ChatUserRequestDto;
import com.eum.haetsal.chat.domain.dto.response.*;
import com.eum.haetsal.chat.domain.model.ChatMessage;
import com.eum.haetsal.chat.domain.model.ChatRoom;
import com.eum.haetsal.chat.domain.model.Message;
import com.eum.haetsal.chat.domain.base.BaseResponseEntity;
import com.eum.haetsal.chat.domain.dto.request.RoomRequestDto;
import com.eum.haetsal.chat.domain.repository.ChatRepository;
import com.eum.haetsal.chat.domain.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ChatService {

    private final ChatRepository chatRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final SimpMessagingTemplate messagingTemplate;

    private final HaetsalClient haetsalClient;

    public BaseResponseEntity<?> saveMessage(String content, String userId, String chatRoomId) {

        BaseResponseEntity<String> BAD_REQUEST = isValidChatRoomId(chatRoomId);
        if (BAD_REQUEST != null) return BAD_REQUEST;

        Message message = new Message(chatRoomId, userId,content, LocalDateTime.now());

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

    private static BaseResponseEntity<String> isValidChatRoomId(String chatRoomId) {
        if(chatRoomId.length() != 24){
            return new BaseResponseEntity<>(HttpStatus.BAD_REQUEST, "chatRoomId를 확인해주세요.");
        }
        return null;
    }

    private ChatMessage convertToChatMessage(Message message) {
        return new ChatMessage(message.getUserId(),ChatMessage.MessageType.CHAT, message.getMessage(), message.getCreatedAt() );
    }

    private void broadcastMessage(ChatMessage chatMessage, String chatRoomId) {
        // 특정 채팅방 구독자들에게 메시지를 브로드캐스트
        messagingTemplate.convertAndSend("/sub/room/" + chatRoomId, chatMessage);
    }

    public BaseResponseEntity<?> getMessagesAndUserInfo(String chatRoomId, String userId) {

        BaseResponseEntity<String> BAD_REQUEST = isValidChatRoomId(chatRoomId);
        if (BAD_REQUEST != null) return BAD_REQUEST;

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

        // haetsalClient 로부터 userInfo 받아오기
        ChatRequestDTO.UserIdList userIdList = new ChatRequestDTO.UserIdList();
        userIdList.setUserIdList(chatRoom.getMembers());
        List<ChatResponseDTO.UserInfo> userInfo = haetsalClient.getChatUser(userIdList);

        List<MessageResponseDTO> messages = chatRepository.findByChatRoomId(chatRoomId).stream()
                .map(form -> new MessageResponseDTO(
                        form.getUserId(),
                        form.getMessage(),
                        form.getCreatedAt()
                )).collect(Collectors.toList());

        return new BaseResponseEntity<>(HttpStatus.OK, new ChatUserResponseDto(userInfo, messages));
    }

    public BaseResponseEntity<?> createChatRoom(RoomRequestDto dto, String userId) {

        ChatRoom chatRoom = new ChatRoom(dto.getPostId(), dto.getMemberIds());
        chatRoom.getMembers().add(0,userId);

        try{
            chatRoomRepository.save(chatRoom);
            return new BaseResponseEntity<>(HttpStatus.OK, new RoomResponseDto(chatRoom));
        }catch (Exception e){
            return new BaseResponseEntity<>(e);
        }
    }

    public BaseResponseEntity<?> getChatRooms(String userId) {

        List<ChatRoom> myRooms = chatRoomRepository.findAllById(userId);

        if(myRooms.isEmpty()){
            return new BaseResponseEntity<>(HttpStatus.BAD_REQUEST, "유저가 속한 채팅방이 없습니다.");

        }else {
            ChatRequestDTO.PostIdList postIdList = new ChatRequestDTO.PostIdList();
            postIdList.setPostIdList(myRooms.stream()
                    .map(chatRoom -> String.valueOf(chatRoom.getPostId()))
                    .collect(Collectors.toList()));
            List<ChatResponseDTO.PostInfo> postInfo = haetsalClient.getChatPost(postIdList);

            return new BaseResponseEntity<>(HttpStatus.OK, createChatPostResponseDtoList(postInfo, myRooms));
        }
    }

    private List<ChatPostResponseDto> createChatPostResponseDtoList(List<ChatResponseDTO.PostInfo> requests, List<ChatRoom> rooms) {
        List<ChatPostResponseDto> data = new ArrayList<>();

        for (int i = 0; i < rooms.size(); i++) {
            ChatResponseDTO.PostInfo request = requests.get(i);
            ChatRoom room = rooms.get(i);
            ChatPostResponseDto responseDto = new ChatPostResponseDto(request, room);
            data.add(responseDto);
        }

        return data;
    }

    public BaseResponseEntity getMembers(String chatRoomId, String userId) {

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

        return new BaseResponseEntity<>(HttpStatus.OK, new MemberIdsResponseDto(chatRoom.getMembers()));
    }
}
