package com.eum.haetsal.chat.global.cache;

import com.eum.haetsal.chat.domain.model.Message;
import com.eum.haetsal.chat.domain.service.ChatService;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import com.github.benmanes.caffeine.cache.RemovalListener;
import org.springframework.data.mongodb.core.MongoTemplate;

@Configuration
public class CacheConfig {

    private final MongoTemplate mongoTemplate;

    public CacheConfig(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Bean
    public Cache<String, ConcurrentLinkedQueue<Message>> chatCache() {

        RemovalListener<String, ConcurrentLinkedQueue<Message>> listener = (String key, ConcurrentLinkedQueue<Message> queue, RemovalCause cause) -> {
            if (cause.wasEvicted()) {
                commitMessageQueue(queue);
            }
        };

        return Caffeine.newBuilder()
                .initialCapacity(200) // 초기 크기 설정
                .softValues() // Soft references to values, reclaimed in response to memory demand
                .maximumSize(1000) // 최대 크기 설정
                .removalListener(listener)
                .build();
    }

    public void commitMessageQueue(Queue<Message> messageQueue) {
        int size = messageQueue.size();
        List<Message> messages = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            messages.add(messageQueue.poll());
        }
        bulkInsertMessages(messages);
    }

    public void bulkInsertMessages(List<Message> messages) {
        mongoTemplate.insertAll(messages);
    }

}