package com.lzk.cloudmsg.send;

import com.lzk.cloudmsg.domain.MessageRecord;
import reactor.core.publisher.Mono;

public interface SendRouter {
    Mono<Void> routeAndSend(MessageRecord messageRecord);
}
