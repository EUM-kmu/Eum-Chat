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

    public List<OneToOneChatRoomsResponseDto> getOneToOneChatRooms(String myId, String theOtherId) {

        List<ChatRoom> OneToOneChatRooms = chatRoomRepository.findOneToOneChatRoomByExactTwoMembers(myId, theOtherId);
        System.out.println(OneToOneChatRooms);

        return OneToOneChatRooms.stream().map(OneToOneChatRoomsResponseDto::new).toList();
    }
}
