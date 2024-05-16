package com.eum.haetsal.chat.domain.service;

import com.eum.haetsal.chat.domain.base.BaseResponseEntity;
import com.eum.haetsal.chat.domain.client.HaetsalClient;
import com.eum.haetsal.chat.domain.controller.dto.request.HaetsalRequestDto;
import com.eum.haetsal.chat.domain.controller.dto.request.RoomRequestDto;
import com.eum.haetsal.chat.domain.controller.dto.response.ChatPostResponseDto;
import com.eum.haetsal.chat.domain.controller.dto.response.HaetsalResponseDto;
import com.eum.haetsal.chat.domain.controller.dto.response.MemberIdsResponseDto;
import com.eum.haetsal.chat.domain.controller.dto.response.RoomResponseDto;
import com.eum.haetsal.chat.domain.model.ChatRoom;
import com.eum.haetsal.chat.domain.model.Message;
import com.eum.haetsal.chat.domain.repository.ChatRoomRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;

    private final HaetsalClient haetsalClient;
    private final ChatService chatService;


    public BaseResponseEntity<?> createChatRoom(RoomRequestDto dto, String userId) {

        ChatRoom chatRoom = new ChatRoom(dto, userId);
        chatRoom.getMembers().add(0,userId);
        chatRoom.setMembersHistory(chatRoom.getMembers());

        try{
            chatRoomRepository.save(chatRoom);
            return new BaseResponseEntity<>(HttpStatus.OK, new RoomResponseDto.Room(chatRoom));

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
        chatRoom.getMembersHistory().addAll(Added);

        try{
            chatRoomRepository.save(chatRoom);

            try {
                chatService.broadcastStatusMessages(removed, Message.MessageType.LEAVE, " 님이 퇴장했습니다.", chatRoomId);
                chatService.broadcastStatusMessages(Added, Message.MessageType.JOIN, " 님이 입장했습니다.", chatRoomId);

            } catch (FeignException.FeignClientException fe){
                return new BaseResponseEntity<>(HttpStatus.BAD_GATEWAY, "채팅 외부 서비스(햇살 서버의 /chat/users)를 불러오는 데 실패했습니다.: " + fe.getMessage());
            }

            return new BaseResponseEntity<>(HttpStatus.OK, new RoomResponseDto.Room(chatRoom));
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

    public BaseResponseEntity getRoomIds(int postId) {
        List<RoomResponseDto.RoomId> chatRoomIds = chatRoomRepository.findAllChatRoomIdByPostId(postId);
        return new BaseResponseEntity<>(HttpStatus.OK, new RoomResponseDto.RoomIds(chatRoomIds.stream()
                .map(RoomResponseDto.RoomId::getId)
                .collect(Collectors.toList())));
    }
}
