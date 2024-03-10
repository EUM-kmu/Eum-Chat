package com.example.demo.domain.repository;

import com.example.demo.domain.Message;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRepository extends MongoRepository<Message, String>{
    List<Message> findByRoomIdx(Long roomIdx);
}
