package com.lzk.cloudmsg.enhance.impl;

import com.lzk.cloudmsg.domain.MessageRecord;
import com.lzk.cloudmsg.enhance.EnhanceChain;
import com.lzk.cloudmsg.enhance.EnhanceHandler;
import com.lzk.cloudmsg.infrastructure.MessageTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
@Order(1) // After TemplateHandler (Order 0)
@Slf4j
@RequiredArgsConstructor
public class DedupeHandler implements EnhanceHandler {

    private final ReactiveStringRedisTemplate redisTemplate;
    private final MessageTemplateRepository templateRepository;
    private final ExpressionParser parser = new SpelExpressionParser();

    @Override
    public Mono<Void> handle(MessageRecord messageRecord, EnhanceChain chain) {
        return templateRepository.findById(messageRecord.getTemplateId())
                .flatMap(template -> {
                    String key;
                    if (template.getDedupeKeyExpression() != null && !template.getDedupeKeyExpression().isEmpty()) {
                        try {
                            StandardEvaluationContext context = new StandardEvaluationContext(messageRecord);
                            key = "dedupe:" + parser.parseExpression(template.getDedupeKeyExpression()).getValue(context, String.class);
                        } catch (Exception e) {
                            log.error("Failed to parse dedupe expression, fallback to default", e);
                            key = "dedupe:" + messageRecord.getTemplateId() + ":" + messageRecord.getReceiver();
                        }
                    } else {
                        key = "dedupe:" + messageRecord.getTemplateId() + ":" + messageRecord.getReceiver();
                    }

                    String finalKey = key;
                    return redisTemplate.opsForValue().setIfAbsent(finalKey, "1", Duration.ofMinutes(5))
                            .flatMap(success -> {
                                if (Boolean.TRUE.equals(success)) {
                                    return chain.next(messageRecord);
                                } else {
                                    log.warn("Duplicate message detected: {}", finalKey);
                                    messageRecord.setStatus(2);
                                    messageRecord.setFailReason("Duplicate message");
                                    return Mono.empty();
                                }
                            });
                });
    }
}
