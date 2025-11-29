package com.lzk.cloudmsg.send;

import com.lzk.cloudmsg.domain.MessageRecord;
import reactor.core.publisher.Mono;

public interface Channel {
    Mono<Void> send(MessageRecord messageRecord);
    Integer getChannelType(); // 10: IM, 20: Push, 30: SMS, 40: Email
    String getName();
    double getWeight(); // Static weight
}
