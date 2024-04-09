package com.eum.haetsal.chat.domain.repository;

import com.eum.haetsal.chat.domain.model.Message;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRepository extends MongoRepository<Message, String>{
    List<Message> findByChatRoomId(String chatRoomId);
}
