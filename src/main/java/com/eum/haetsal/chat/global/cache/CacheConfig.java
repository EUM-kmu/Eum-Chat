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

    @Bean
    public Cache<String, ConcurrentLinkedQueue<Message>> chatCache() {

        return Caffeine.newBuilder()
                .initialCapacity(200) // 초기 크기 설정
                .softValues() // Soft references to values, reclaimed in response to memory demand
                .maximumSize(1000) // 최대 크기 설정
                .build();
    }

}