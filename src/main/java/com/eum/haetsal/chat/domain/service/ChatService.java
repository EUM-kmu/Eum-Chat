package com.eum.haetsal.chat.domain.service;

import com.eum.haetsal.chat.domain.client.HaetsalClient;
import com.eum.haetsal.chat.domain.dto.request.ChatRequestDTO;
import com.eum.haetsal.chat.domain.dto.response.*;
import com.eum.haetsal.chat.domain.model.ChatRoom;
import com.eum.haetsal.chat.domain.model.Message;
import com.eum.haetsal.chat.domain.base.BaseResponseEntity;
import com.eum.haetsal.chat.domain.dto.request.RoomRequestDto;
import com.eum.haetsal.chat.domain.repository.ChatRepository;
import com.eum.haetsal.chat.domain.repository.ChatRoomRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ChatService {

    private final ChatRepository chatRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final BroadcastService broadcastService;

    private final HaetsalClient haetsalClient;

    public BaseResponseEntity<?> saveMessage(String content, String userId, String chatRoomId) {

        BaseResponseEntity<String> BAD_REQUEST = isValidChatRoomId(chatRoomId);
        if (BAD_REQUEST != null) return BAD_REQUEST;

        try {
            Message message = Message.from(chatRoomId,userId, Message.MessageType.CHAT, content);
            chatRepository.save(message);

            broadcastService.broadcastMessage(message, chatRoomId);

            return new BaseResponseEntity<>(HttpStatus.OK);

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

    public BaseResponseEntity<?> getMessagesAndUserInfo(String chatRoomId, String userId) {

        BaseResponseEntity<String> BAD_REQUEST = isValidChatRoomId(chatRoomId);
        if (BAD_REQUEST != null) return BAD_REQUEST;

        ChatRoom chatRoom = chatRoomRepository.findChatRoomById(chatRoomId);

        // 채팅방 있는지 확인
        if(chatRoom == null){
            return new BaseResponseEntity<>(HttpStatus.BAD_REQUEST, "해당 채팅방이 없습니다.");
        }

        // 해당 채팅방에 유저가 속해 있는지 확인
        boolean isContained = chatRoom.getMembers().contains(userId);
        if(!isContained){
            return new BaseResponseEntity<>(HttpStatus.FORBIDDEN, "해당 채팅방의 참여자가 아닙니다.");
        }
        try {

            ChatRequestDTO.PostIdList postIdList = new ChatRequestDTO.PostIdList(Collections.singletonList(chatRoom.getPostId()));
            ChatResponseDTO.PostInfo postInfo = haetsalClient.getChatPost(postIdList).get(0);

            // 채팅 메시지들을 조회
            List<Message> messages = chatRepository.findMessageByChatRoomId(chatRoomId);

            // userId 목록을 추출
            List<String> userIds = messages.stream()
                    .map(Message::getUserId)
                    .distinct()
                    .collect(Collectors.toList());

            // haetsalClient 로부터 userInfo 받아오기
            ChatRequestDTO.UserIdList userIdList = new ChatRequestDTO.UserIdList(userIds);
            List<ChatResponseDTO.UserInfo> userInfos = haetsalClient.getChatUser(userIdList);

            // userId를 키로 하고 UserInfo를 값으로 하는 맵 생성
            Map<Long, MessageResponseDTO.SenderInfo> userInfoMap = userInfos.stream()
                    .collect(Collectors.toMap(ChatResponseDTO.UserInfo::getUserId,
                            userInfo -> new MessageResponseDTO.SenderInfo( // 값 매퍼: UserInfo를 SenderInfo로 변환
                                    userInfo.getUserId(),
                                    userInfo.getProfileImage(),
                                    userInfo.getNickName()
                            )));

            // 메시지와 사용자 정보를 결합하여 MessageResponseDTO 리스트 생성
            List<MessageResponseDTO> messageWithUserInfo = messages.stream()
                    .map(message -> new MessageResponseDTO(
                            userInfoMap.get(Long.parseLong(message.getUserId())), // userId에 해당하는 UserInfo 객체
                            message.getMessage(),
                            message.getCreatedAt()))
                    .collect(Collectors.toList());

            return new BaseResponseEntity<>(HttpStatus.OK, new ChatUserResponseDto(userInfos, postInfo, messageWithUserInfo));
        }catch (FeignException e) {
            return new BaseResponseEntity<>(HttpStatus.BAD_GATEWAY, "채팅 외부 서비스(햇살 서버의 /chat/users 혹은 /chat/posts)를 불러오는 데 실패했습니다:  " + e.getMessage());
        } catch (Exception e) {
            return new BaseResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR, "채팅 서버 에러: " + e.getMessage());
        }
    }

    public BaseResponseEntity<?> createChatRoom(RoomRequestDto dto, String userId) {

        ChatRoom chatRoom = new ChatRoom(dto, userId);
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
            ChatRequestDTO.PostIdList postIdList = new ChatRequestDTO.PostIdList(myRooms.stream().map(ChatRoom::getPostId).collect(Collectors.toList()));
            try {
                List<ChatResponseDTO.PostInfo> postInfo = haetsalClient.getChatPost(postIdList);

                return new BaseResponseEntity<>(HttpStatus.OK, createChatPostResponseDtoList(postInfo, myRooms));
            }catch (FeignException e) {
                return new BaseResponseEntity<>(HttpStatus.BAD_GATEWAY, "채팅 외부 서비스(햇살 서버의 /chat/posts)를 불러오는 데 실패했습니다:  " + e.getMessage());
            }
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

        ChatRoom chatRoom = chatRoomRepository.findChatRoomById(chatRoomId);

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

    public BaseResponseEntity updateMembers(String chatRoomId, RoomRequestDto dto, String userId) {

        if(chatRoomId.length() != 24){
            return new BaseResponseEntity<>(HttpStatus.BAD_REQUEST, "chatRoomId를 확인해주세요.");
        }

        ChatRoom chatRoom = chatRoomRepository.findChatRoomById(chatRoomId);

        // 채팅방 있는지 확인
        if(chatRoom == null){
            return new BaseResponseEntity<>(HttpStatus.BAD_REQUEST, "해당 채팅방이 없습니다.");
        }

        // 해당 채팅방에 유저가 속해 있는지 확인
        boolean isContained = chatRoom.getMembers().get(0).equals(userId);
        if(!isContained){
            return new BaseResponseEntity<>(HttpStatus.FORBIDDEN, "해당 채팅방의 작성자가 아닙니다. 유저 추가 권한이 없습니다.");
        }

        List<String> updatedList = dto.getMemberIds();
        updatedList.add(0,userId);
        List<String> existingList = chatRoom.getMembers();

        List<String> Added = findAddedMembers(updatedList, existingList);
        List<String> removed = findRemovedMembers(updatedList, existingList);

        chatRoom.setMembers(updatedList);

        try{
            chatRoomRepository.save(chatRoom);

            try {
                broadcastService.broadcastStatusMessages(removed, Message.MessageType.LEAVE, " 님이 퇴장했습니다.", chatRoomId);
                broadcastService.broadcastStatusMessages(Added, Message.MessageType.JOIN, " 님이 입장했습니다.", chatRoomId);

            } catch (FeignException.FeignClientException fe){
                return new BaseResponseEntity<>(HttpStatus.BAD_GATEWAY, "채팅 외부 서비스(햇살 서버의 /chat/users)를 불러오는 데 실패했습니다.: " + fe.getMessage());
            }

            return new BaseResponseEntity<>(HttpStatus.OK, new RoomResponseDto(chatRoom));
        }catch (Exception e){
            return new BaseResponseEntity<>(e);
        }
    }

    public List<String> findAddedMembers(List<String> updatedList, List<String> existingList) {
        return updatedList.stream()
                .filter(member -> !existingList.contains(member))
                .collect(Collectors.toList());
    }

    public List<String> findRemovedMembers(List<String> updatedList, List<String> existingList) {
        return existingList.stream()
                .filter(member -> !updatedList.contains(member))
                .collect(Collectors.toList());
    }
}
