package com.lzk.cloudmsg.enhance;

import com.lzk.cloudmsg.domain.MessageRecord;
import reactor.core.publisher.Mono;

public interface EnhanceHandler {
    
    /**
     * Handle the message enhancement/check.
     * @param messageRecord The message context
     * @param chain The chain to proceed
     * @return Mono<Void>
     */
    Mono<Void> handle(MessageRecord messageRecord, EnhanceChain chain);
}
