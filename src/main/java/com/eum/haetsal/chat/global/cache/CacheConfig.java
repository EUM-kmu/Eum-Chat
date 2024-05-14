package com.eum.haetsal.chat.global.cache;

import com.eum.haetsal.chat.domain.model.Message;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ConcurrentLinkedQueue;

@Configuration
public class CacheConfig {

    @Bean
    public Cache<String, ConcurrentLinkedQueue<Message>> chatCache() {

        return Caffeine.newBuilder()
                .initialCapacity(200)
                .maximumSize(1000)
                .build();
    }

}