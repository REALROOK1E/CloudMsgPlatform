package com.lzk.cloudmsg.access;

import com.lzk.cloudmsg.access.dto.MessageRequest;
import reactor.core.publisher.Mono;

public interface SendService {
    Mono<String> send(MessageRequest request);
}
