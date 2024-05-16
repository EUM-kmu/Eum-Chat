package com.eum.haetsal.chat.global.cache;

import com.eum.haetsal.chat.domain.model.Message;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.github.benmanes.caffeine.cache.RemovalListener;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ConcurrentLinkedQueue;

@Slf4j
@Configuration
public class CacheConfig {

    @Bean
    public Cache<String, ConcurrentLinkedQueue<Message>> chatCache() {

        RemovalListener<String, ConcurrentLinkedQueue<Message>> listener = (String key, ConcurrentLinkedQueue<Message> queue, RemovalCause cause) -> {
            if (cause.wasEvicted()) {
                log.info("캐시 삭제, removalListener: {}", cause);
            }
        };

        return Caffeine.newBuilder()
                .initialCapacity(200)
                .maximumSize(1000)
                .removalListener(listener)
                .build();
    }

}