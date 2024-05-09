package com.eum.haetsal.chat.domain.service;


import com.eum.haetsal.chat.domain.client.HaetsalClient;
import com.eum.haetsal.chat.domain.controller.dto.request.HaetsalRequestDto;
import com.eum.haetsal.chat.domain.controller.dto.response.HaetsalResponseDto;
import com.eum.haetsal.chat.domain.model.Message;
import com.eum.haetsal.chat.domain.repository.ChatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class BroadcastService {

    private final ChatRepository chatRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final HaetsalClient haetsalClient;

    void broadcastMessage(Message message, String chatRoomId) {
        // 특정 채팅방 구독자들에게 메시지를 브로드캐스트
        messagingTemplate.convertAndSend("/sub/room/" + chatRoomId, message);
    }

     void broadcastStatusMessages(List<String> removed, Message.MessageType type, String x, String chatRoomId) {

        HaetsalRequestDto.UserIdList userIdList = new HaetsalRequestDto.UserIdList(removed);
        List<HaetsalResponseDto.UserInfo> userInfos = haetsalClient.getChatUser(userIdList);

        userInfos.forEach(userInfo -> {
            Message message = chatRepository.save(Message.from(chatRoomId, null, type, userInfo.getNickName() + x));
            broadcastMessage(message, chatRoomId);
        });
    }
}
