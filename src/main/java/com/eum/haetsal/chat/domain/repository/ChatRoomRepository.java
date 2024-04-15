package com.eum.haetsal.chat.domain.repository;

import com.eum.haetsal.chat.domain.model.ChatRoom;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRoomRepository extends MongoRepository<ChatRoom, String> {

    @Query("{'members': ?0}")
    List<ChatRoom> findAllById(String id);

    ChatRoom findChatRoomById(String chatRoomId);

    // 두 사용자가 모두 포함되어 있고, members 배열의 크기가 2인 chatRoom을 찾는 메서드
    @Query("{'members': {$size: 2, $all: [?0, ?1]}}")
    List<ChatRoom> findOneToOneChatRoomByExactTwoMembers(String myId, String theOtherId);
}
