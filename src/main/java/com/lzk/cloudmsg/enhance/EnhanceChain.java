package com.lzk.cloudmsg.enhance;

import com.lzk.cloudmsg.domain.MessageRecord;
import reactor.core.publisher.Mono;

public interface EnhanceChain {
    Mono<Void> next(MessageRecord messageRecord);
}
