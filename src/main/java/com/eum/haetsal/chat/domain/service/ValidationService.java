package com.eum.haetsal.chat.domain.service;

import com.eum.haetsal.chat.domain.base.BaseResponseEntity;
import com.eum.haetsal.chat.domain.model.ChatRoom;
import com.eum.haetsal.chat.domain.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ValidationService {

    private final ChatRoomRepository chatRoomRepository;

    public BaseResponseEntity<String> validateChatRoomAccess(String chatRoomId, String userId) {

        if (chatRoomId.length() != 24) {
            return new BaseResponseEntity<>(HttpStatus.BAD_REQUEST, "chatRoomId를 확인해주세요.");
        }

        ChatRoom chatRoom = chatRoomRepository.findChatRoomById(chatRoomId);
        if (chatRoom == null) {
            return new BaseResponseEntity<>(HttpStatus.BAD_REQUEST, "해당 채팅방이 존재하지 않습니다.");
        }

        if (!chatRoom.getMembers().contains(userId)) {
            return new BaseResponseEntity<>(HttpStatus.FORBIDDEN, "해당 채팅방에 권한이 없습니다.");
        }

        return null;
    }

}
