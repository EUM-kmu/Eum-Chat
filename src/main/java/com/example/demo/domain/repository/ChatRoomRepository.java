package com.example.demo.domain.repository;

import com.example.demo.domain.model.ChatRoom;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRoomRepository extends MongoRepository<ChatRoom, String> {

    @Query("{'members': ?0}")
    List<ChatRoom> findAllById(String id);

    ChatRoom findMembersById(String chatRoomId);

}
