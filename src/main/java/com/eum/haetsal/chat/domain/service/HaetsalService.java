package com.eum.haetsal.chat.domain.service;

import com.eum.haetsal.chat.domain.base.BaseResponseEntity;
import com.eum.haetsal.chat.domain.dto.response.OneToOneChatRoomsResponseDto;
import com.eum.haetsal.chat.domain.model.ChatRoom;
import com.eum.haetsal.chat.domain.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class HaetsalService {

    private final ChatRoomRepository chatRoomRepository;

    public BaseResponseEntity getOneToOneChatRooms(String myId, String theOtherId) {

        // 유저 id 제대로 입력했는지 확인 -> 다른 서버에 요청

        List<ChatRoom> OneToOneChatRooms = chatRoomRepository.findOneToOneChatRoomByExactTwoMembers(myId, theOtherId);

        if(OneToOneChatRooms.isEmpty()){
            return new BaseResponseEntity<>(HttpStatus.OK, "두 사람의 1:1 채팅방이 존재하지 않습니다.");
        }

        return new BaseResponseEntity<>(HttpStatus.OK, OneToOneChatRooms.stream().map(OneToOneChatRoomsResponseDto::new));
    }
}
