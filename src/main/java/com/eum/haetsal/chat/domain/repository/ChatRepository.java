package com.eum.haetsal.chat.domain.repository;

import com.eum.haetsal.chat.domain.model.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.LinkedList;

@Repository
public interface ChatRepository extends MongoRepository<Message, String>{
    LinkedList<Message> findAllByChatRoomIdOrderByCreatedAtDesc(String chatRoomId, Pageable pageable);
}
