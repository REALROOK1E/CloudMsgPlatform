package com.lzk.cloudmsg.send.impl;

import com.lzk.cloudmsg.domain.MessageRecord;
import com.lzk.cloudmsg.send.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class TencentSmsChannel implements Channel {

    @Override
    public Mono<Void> send(MessageRecord messageRecord) {
        // Mock sending SMS via Tencent
        return Mono.fromRunnable(() -> {
            log.info("Sending SMS via Tencent to {}: {}", messageRecord.getReceiver(), messageRecord.getContent());
        });
    }

    @Override
    public Integer getChannelType() {
        return 30;
    }

    @Override
    public String getName() {
        return "TencentSMS";
    }

    @Override
    public double getWeight() {
        return 3.0;
    }
}
