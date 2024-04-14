package com.eum.haetsal.chat.domain.service;

import com.eum.haetsal.chat.domain.client.ChatPostClient;
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

    private final ChatPostClient chatPostClient;

    public BaseResponseEntity<?> saveMessage(String content, String userId, String chatRoomId) {

        BaseResponseEntity<String> BAD_REQUEST = isValidChatRoomId(chatRoomId);
        if (BAD_REQUEST != null) return BAD_REQUEST;

        Message message = new Message();
        message.setChatRoomId(chatRoomId);
        message.setUserId(userId);
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

    private static BaseResponseEntity<String> isValidChatRoomId(String chatRoomId) {
        if(chatRoomId.length() != 24){
            return new BaseResponseEntity<>(HttpStatus.BAD_REQUEST, "chatRoomId를 확인해주세요.");
        }
        return null;
    }

    private ChatMessage convertToChatMessage(Message message) {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setUserId(message.getUserId());
        chatMessage.setMessage(message.getMessage());
        chatMessage.setType(ChatMessage.MessageType.CHAT); // 메시지 타입 설정.. TODO: join, leave 는 아직 안함
        chatMessage.setCreatedAt(message.getCreatedAt());
        return chatMessage;
    }

    private void broadcastMessage(ChatMessage chatMessage, String chatRoomId) {
        // 특정 채팅방 구독자들에게 메시지를 브로드캐스트
        messagingTemplate.convertAndSend("/sub/room/" + chatRoomId, chatMessage);
    }

    public BaseResponseEntity<?> getAllMessagesByChatRoomId(String chatRoomId, String userId) {

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


        ChatRequestDTO.UserIdList userIdList = new ChatRequestDTO.UserIdList();
        userIdList.setUserIdList(chatRoom.getMembers());
        List<ChatUserRequestDto> userProfiles = chatPostClient.getChatUser(userIdList);

        List<MessageResponseDTO> messages = chatRepository.findByChatRoomId(chatRoomId).stream()
                .map(form -> new MessageResponseDTO(
                        form.getUserId(),
                        form.getMessage(),
                        form.getCreatedAt()
                )).collect(Collectors.toList());

        ChatUserResponseDto data = new ChatUserResponseDto(userProfiles, messages);

        return new BaseResponseEntity<>(HttpStatus.OK, data);
    }

    public BaseResponseEntity<?> createChatRoom(RoomRequestDto dto, String userId) {

        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setPostId(dto.getPostId());
        chatRoom.setMembers(dto.getMemberIds());
        chatRoom.getMembers().add(0,userId);

        try{
            chatRoomRepository.save(chatRoom);
            return new BaseResponseEntity<>(HttpStatus.OK, new RoomResponseDto(chatRoom));
        }catch (Exception e){
            return new BaseResponseEntity<>(e);
        }
    }

    public BaseResponseEntity<?> getChatRoomsById(String userId) {
        List<ChatRoom> myRooms = chatRoomRepository.findAllById(userId);

        if(myRooms.isEmpty()){
            return new BaseResponseEntity<>(HttpStatus.BAD_REQUEST, "유저가 속한 채팅방이 없습니다.");
        }else {

            ChatRequestDTO.PostIdList postIdList = new ChatRequestDTO.PostIdList();
            postIdList.setPostIdList(myRooms.stream()
                    .map(chatRoom -> String.valueOf(chatRoom.getPostId()))
                    .collect(Collectors.toList()));

            List<ChatPostRequestDto> req = chatPostClient.getChatPost(postIdList);

            List<ChatPostResponseDto> data = createChatPostResponseDtoList(myRooms, req);

            return new BaseResponseEntity<>(HttpStatus.OK, data);
        }
    }

    private List<ChatPostResponseDto> createChatPostResponseDtoList(List<ChatRoom> rooms, List<ChatPostRequestDto> requests) {
        List<ChatPostResponseDto> data = new ArrayList<>();

        for (int i = 0; i < rooms.size(); i++) {
            ChatRoom room = rooms.get(i);
            ChatPostRequestDto request = requests.get(i);
            ChatPostResponseDto responseDto = new ChatPostResponseDto(request, room);
            data.add(responseDto);
        }

        return data;
    }

    public BaseResponseEntity getAllMembersByChatRoomId(String chatRoomId, String userId) {

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

    public BaseResponseEntity getOneToOneChatRooms(String myId, String theOtherId) {

        // 유저 id 제대로 입력했는지 확인 -> 다른 서버에 요청

        List<ChatRoom> OneToOneChatRooms = chatRoomRepository.findOneToOneChatRoomByExactTwoMembers(myId, theOtherId);

        if(OneToOneChatRooms.isEmpty()){
            return new BaseResponseEntity<>(HttpStatus.OK, "두 사람의 1:1 채팅방이 존재하지 않습니다.");
        }

        return new BaseResponseEntity<>(HttpStatus.OK, OneToOneChatRooms.stream().map(OneToOneChatRoomsResponseDto::new));
    }
}
