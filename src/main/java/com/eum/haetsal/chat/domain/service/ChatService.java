package com.eum.haetsal.chat.domain.service;

import com.eum.haetsal.chat.domain.client.HaetsalClient;
import com.eum.haetsal.chat.domain.controller.dto.request.HaetsalRequestDto;
import com.eum.haetsal.chat.domain.controller.dto.response.*;
import com.eum.haetsal.chat.domain.model.ChatRoom;
import com.eum.haetsal.chat.domain.model.Message;
import com.eum.haetsal.chat.domain.base.BaseResponseEntity;
import com.eum.haetsal.chat.domain.repository.ChatRepository;
import com.eum.haetsal.chat.domain.repository.ChatRoomRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

        Message message = Message.from(chatRoomId,userId, Message.MessageType.CHAT, content);

        try {
            chatRepository.save(message);
            broadcastService.broadcastMessage(message, chatRoomId);

            return new BaseResponseEntity<>(HttpStatus.OK);
        } catch (Exception e){
            return new BaseResponseEntity<>(e);
        }

    }

    public BaseResponseEntity<?> getMessagesAndUserInfo(String chatRoomId, int pageNumber, int pageSize) {
        try {

            ChatRoom chatRoom = chatRoomRepository.findChatRoomById(chatRoomId);
            Long creatorId = Long.parseLong(chatRoom.getCreatorId());

            HaetsalResponseDto.PostInfo postInfo = haetsalClient.getChatPost(
                    new HaetsalRequestDto.PostIdList(Collections.singletonList(chatRoom.getPostId()))
                    ).get(0);

            List<HaetsalResponseDto.UserInfo> userInfos = haetsalClient.getChatUser(
                    new HaetsalRequestDto.UserIdList(chatRoom.getMembersHistory())
            );
            userInfos.forEach(userInfo -> {
                userInfo.setCreator(userInfo.getUserId().equals(creatorId));
            });

            // userId를 키로 하고 UserInfo를 값으로 하는 맵 생성
            Map<Long, MessageResponseDTO.SenderInfo> userInfoMap = userInfos.stream()
                    .collect(Collectors.toMap(HaetsalResponseDto.UserInfo::getUserId,
                            userInfo -> new MessageResponseDTO.SenderInfo( // 값 매퍼: UserInfo를 SenderInfo로 변환
                                    userInfo.getUserId(),
                                    userInfo.getProfileImage(),
                                    userInfo.getNickName(),
                                    userInfo.getUserId().equals(creatorId),
                                    userInfo.isDeleted()
                            )));

            Queue<Message> messages = null;
                Pageable pageable = PageRequest.of(pageNumber, pageSize);
                messages = chatRepository.findAllByChatRoomIdOrderByCreatedAtDesc(chatRoomId, pageable);

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

    void broadcastStatusMessages(List<String> removed, Message.MessageType type, String x, String chatRoomId) {

        HaetsalRequestDto.UserIdList userIdList = new HaetsalRequestDto.UserIdList(removed);
        List<HaetsalResponseDto.UserInfo> userInfos = haetsalClient.getChatUser(userIdList);

        userInfos.forEach(userInfo -> {

            Message message = Message.from(chatRoomId, null, type, userInfo.getNickName() + x);

            chatRepository.save(message);
            broadcastService.broadcastMessage(message, chatRoomId);
        });
    }

}
