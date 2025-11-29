package com.lzk.cloudmsg.send.impl;

import com.lzk.cloudmsg.domain.MessageRecord;
import com.lzk.cloudmsg.send.Channel;
import com.lzk.cloudmsg.send.LoadBalancer;
import com.lzk.cloudmsg.send.SendRouter;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SendRouterImpl implements SendRouter {

    private final List<Channel> channels;
    private final LoadBalancer loadBalancer;
    private final ReactiveCircuitBreakerFactory circuitBreakerFactory;

    @PostConstruct
    public void init() {
        loadBalancer.init(channels);
    }

    @Override
    public Mono<Void> routeAndSend(MessageRecord messageRecord) {
        // Default to SMS (30) if not specified
        Integer channelType = messageRecord.getSendChannel() != null ? messageRecord.getSendChannel() : 30;
        
        Channel channel = loadBalancer.selectChannel(channelType);
        
        if (channel != null) {
            messageRecord.setChannelName(channel.getName());
            
            // Wrap with Circuit Breaker
            ReactiveCircuitBreaker rcb = circuitBreakerFactory.create(channel.getName());
            
            return rcb.run(channel.send(messageRecord), throwable -> {
                // Fallback logic
                return Mono.error(new RuntimeException("Channel " + channel.getName() + " is unavailable or timed out: " + throwable.getMessage()));
            });
        } else {
            return Mono.error(new RuntimeException("No channel found for type: " + channelType));
        }
    }
}
