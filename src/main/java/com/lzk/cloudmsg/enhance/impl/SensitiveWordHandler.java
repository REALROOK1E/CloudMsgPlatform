package com.lzk.cloudmsg.enhance.impl;

import com.lzk.cloudmsg.common.SensitiveWordTrie;
import com.lzk.cloudmsg.domain.MessageRecord;
import com.lzk.cloudmsg.enhance.EnhanceChain;
import com.lzk.cloudmsg.enhance.EnhanceHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Order(2)
@Slf4j
@RequiredArgsConstructor
public class SensitiveWordHandler implements EnhanceHandler {

    private final SensitiveWordTrie sensitiveWordTrie;

    @Override
    public Mono<Void> handle(MessageRecord messageRecord, EnhanceChain chain) {
        if (sensitiveWordTrie.contains(messageRecord.getContent())) {
            log.warn("Sensitive word detected in message: {}", messageRecord.getId());
            // Stop chain, maybe mark as failed or rejected
            messageRecord.setStatus(2); // Fail
            messageRecord.setFailReason("Sensitive word detected");
            return Mono.empty(); 
        }
        return chain.next(messageRecord);
    }
}
