package com.eum.haetsal.chat.domain.service;

import com.eum.haetsal.chat.domain.client.HaetsalClient;
import com.eum.haetsal.chat.domain.dto.request.HaetsalRequestDto;
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
import org.springframework.beans.factory.DisposableBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ChatService implements DisposableBean {

    private final ChatRepository chatRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final BroadcastService broadcastService;

    private final HaetsalClient haetsalClient;

    private static final ConcurrentHashMap<String, ConcurrentLinkedQueue<Message>> messageMap = new ConcurrentHashMap<>();
    private static final int transactionMessageSize = 2;
    private static final int messagePageableSize = 3;

    @Override
    public void destroy() {
        System.out.println("서버가 종료되고 있습니다. 모든 메시지 큐를 처리합니다...");
        messageMap.forEach((roomId, messageQueue) -> commitMessageQueue(messageQueue));
    }

    public BaseResponseEntity<?> saveMessage(String content, String userId, String chatRoomId) {

        Message message = Message.from(chatRoomId,userId, Message.MessageType.CHAT, content);

        try {
            saveInCacheOrDB(chatRoomId, message);
            broadcastService.broadcastMessage(message, chatRoomId);

            return new BaseResponseEntity<>(HttpStatus.OK);
        } catch (Exception e){
            return new BaseResponseEntity<>(e);
        }

    }

    private void saveInCacheOrDB(String chatRoomId, Message message) {

        ConcurrentLinkedQueue<Message> messageQueue = messageMap.get(chatRoomId);

        if(messageMap.get(chatRoomId) == null){
            messageQueue = new ConcurrentLinkedQueue<>();
        }
        messageQueue.add(message);

        if(messageQueue.size() > transactionMessageSize + messagePageableSize){
            ConcurrentLinkedQueue<Message> q = new ConcurrentLinkedQueue<>();

            for(int i =0; i< transactionMessageSize; i++){
                q.add(messageQueue.poll());
            }
            commitMessageQueue(q);
        }

        messageMap.put(chatRoomId, messageQueue);
    }

    private void commitMessageQueue(Queue<Message> messageQueue) {
        int size = messageQueue.size();
        for (int i = 0; i < size; i++) {
            Message message = messageQueue.poll();
            chatRepository.save(message);
        }
    }

    public BaseResponseEntity<?> getMessagesAndUserInfo(String chatRoomId, int pageNumber, int pageSize) {
        try {

            ChatRoom chatRoom = chatRoomRepository.findChatRoomById(chatRoomId);

            HaetsalResponseDto.PostInfo postInfo = haetsalClient.getChatPost(
                    new HaetsalRequestDto.PostIdList(Collections.singletonList(chatRoom.getPostId()))
                    ).get(0);

            List<HaetsalResponseDto.UserInfo> userInfos = haetsalClient.getChatUser(
                    new HaetsalRequestDto.UserIdList(chatRoom.getMembers())
            );

            // userId를 키로 하고 UserInfo를 값으로 하는 맵 생성
            Map<Long, MessageResponseDTO.SenderInfo> userInfoMap = userInfos.stream()
                    .collect(Collectors.toMap(HaetsalResponseDto.UserInfo::getUserId,
                            userInfo -> new MessageResponseDTO.SenderInfo( // 값 매퍼: UserInfo를 SenderInfo로 변환
                                    userInfo.getUserId(),
                                    userInfo.getProfileImage(),
                                    userInfo.getNickName(),
                                    userInfo.isDeleted()
                            )));

            ConcurrentLinkedQueue<Message> messageQueue = messageMap.get(chatRoomId);

            Queue<Message> messages = null;

            if(messageQueue != null && pageNumber == 0){
                //Cache Hit
                LinkedList<Message> reversedQueue = new LinkedList<>();
                for (Message message : messageQueue) {
                    reversedQueue.addFirst(message);
                }
                messages =reversedQueue;
            }else{
                if(messageQueue != null){
                    pageNumber--;
                }
                Pageable pageable = PageRequest.of(pageNumber, pageSize);
                //Cache Miss
                messages = chatRepository.findAllByChatRoomIdOrderByCreatedAtDesc(chatRoomId, pageable);
            }

            // 메시지와 사용자 정보를 결합하여 MessageResponseDTO 리스트 생성
            List<MessageResponseDTO> messageWithUserInfo = messages.stream()
                    .map(message -> {
                        Long userId = Optional.ofNullable(message.getUserId())  // message.getUserId()가 null일 수 있음
                                .map(Long::parseLong)  // null이 아니면 Long으로 파싱
                                .orElse(null);  // null이면 null 반환

                        MessageResponseDTO.SenderInfo senderInfo = userId != null ? userInfoMap.get(userId) : null;  // userId가 null이 아니면 맵에서 정보 검색, null이면 null

                        return new MessageResponseDTO(
                                senderInfo,  // userInfoMap에서 검색된 SenderInfo 객체 또는 null
                                message.getType(),
                                message.getMessage(),
                                message.getCreatedAt());
                    })
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
            return new BaseResponseEntity<>(HttpStatus.OK, myRooms);
        }else {
            HaetsalRequestDto.PostIdList postIdList = new HaetsalRequestDto.PostIdList(myRooms.stream().map(ChatRoom::getPostId).collect(Collectors.toList()));
            try {
                List<HaetsalResponseDto.PostInfo> postInfo = haetsalClient.getChatPost(postIdList);

                return new BaseResponseEntity<>(HttpStatus.OK, createChatPostResponseDtoList(postInfo, myRooms));
            }catch (FeignException e) {
                return new BaseResponseEntity<>(HttpStatus.BAD_GATEWAY, "채팅 외부 서비스(햇살 서버의 /chat/posts)를 불러오는 데 실패했습니다:  " + e.getMessage());
            }
        }
    }

    private List<ChatPostResponseDto> createChatPostResponseDtoList(List<HaetsalResponseDto.PostInfo> requests, List<ChatRoom> rooms) {

        List<ChatPostResponseDto> data = new ArrayList<>();

        for (int i = 0; i < rooms.size(); i++) {
            HaetsalResponseDto.PostInfo request = requests.get(i);
            ChatRoom room = rooms.get(i);

            boolean isBlockedRoom = false;
            // 일대일 채팅의 경우
            if(room.getMembers().size() == 2){
                isBlockedRoom = isBlockedRoom(room.getMembers().get(1), isBlockedRoom);

            }else{ // 일대다 채팅의 경우
                isBlockedRoom = isBlockedRoom(room.getCreatorId(), isBlockedRoom);

            }
            ChatPostResponseDto responseDto = new ChatPostResponseDto(request, room, isBlockedRoom);
            data.add(responseDto);
        }

        return data;
    }

    private boolean isBlockedRoom(String memberId, boolean isBlockedRoom) {
        List<String> list = new ArrayList<>();
        list.add(memberId);

        HaetsalRequestDto.UserIdList userIdList = new HaetsalRequestDto.UserIdList(list);
        List<HaetsalResponseDto.UserInfo> userInfos = haetsalClient.getChatUser(userIdList);
        if(userInfos.get(0).isDeleted()){
            isBlockedRoom = true;
        }
        return isBlockedRoom;
    }

    public BaseResponseEntity getMembers(String chatRoomId) {

        ChatRoom chatRoom = chatRoomRepository.findChatRoomById(chatRoomId);

        return new BaseResponseEntity<>(HttpStatus.OK, new MemberIdsResponseDto(chatRoom.getMembers()));
    }

    public BaseResponseEntity updateMembers(String chatRoomId, RoomRequestDto dto, String userId) {

        ChatRoom chatRoom = chatRoomRepository.findChatRoomById(chatRoomId);

        if(!chatRoom.getCreatorId().equals(userId)){
            return new BaseResponseEntity<>(HttpStatus.UNAUTHORIZED, "해당 게시글의 작성자가 아니므로 수정권한이 없습니다.");
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
