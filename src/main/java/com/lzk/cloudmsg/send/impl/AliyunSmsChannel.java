package com.lzk.cloudmsg.send.impl;

import com.lzk.cloudmsg.domain.MessageRecord;
import com.lzk.cloudmsg.send.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class AliyunSmsChannel implements Channel {

    @Override
    public Mono<Void> send(MessageRecord messageRecord) {
        // Mock sending SMS via Aliyun
        return Mono.fromRunnable(() -> {
            log.info("Sending SMS via Aliyun to {}: {}", messageRecord.getReceiver(), messageRecord.getContent());
        });
    }

    @Override
    public Integer getChannelType() {
        return 30;
    }

    @Override
    public String getName() {
        return "AliyunSMS";
    }

    @Override
    public double getWeight() {
        return 5.0;
    }
}
