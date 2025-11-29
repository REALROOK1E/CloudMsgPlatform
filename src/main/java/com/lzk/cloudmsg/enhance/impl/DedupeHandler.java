package com.lzk.cloudmsg.enhance.impl;

import com.lzk.cloudmsg.domain.MessageRecord;
import com.lzk.cloudmsg.enhance.EnhanceChain;
import com.lzk.cloudmsg.enhance.EnhanceHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
@Order(1) // First in chain
@Slf4j
@RequiredArgsConstructor
public class DedupeHandler implements EnhanceHandler {

    private final ReactiveStringRedisTemplate redisTemplate;

    @Override
    public Mono<Void> handle(MessageRecord messageRecord, EnhanceChain chain) {
        // Simple Dedupe Key: templateId:receiver
        String key = "dedupe:" + messageRecord.getTemplateId() + ":" + messageRecord.getReceiver();
        
        return redisTemplate.opsForValue().setIfAbsent(key, "1", Duration.ofMinutes(5))
                .flatMap(success -> {
                    if (Boolean.TRUE.equals(success)) {
                        return chain.next(messageRecord);
                    } else {
                        log.warn("Duplicate message detected: {}", key);
                        // Stop the chain
                        return Mono.empty();
                    }
                });
    }
}
