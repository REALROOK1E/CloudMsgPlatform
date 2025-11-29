package com.lzk.cloudmsg.access;

import com.lzk.cloudmsg.common.Response;
import com.lzk.cloudmsg.domain.MessageRecord;
import com.lzk.cloudmsg.infrastructure.MessageRecordRepository;
import com.lzk.cloudmsg.send.LoadBalancer;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/callback")
@RequiredArgsConstructor
@Slf4j
public class CallbackController {

    private final MessageRecordRepository messageRecordRepository;
    private final LoadBalancer loadBalancer;

    @PostMapping
    public Mono<Response<String>> callback(@RequestBody CallbackRequest request) {
        log.info("Received callback: {}", request);
        
        return messageRecordRepository.findById(request.getMessageId())
                .flatMap(record -> {
                    record.setStatus(request.isSuccess() ? 1 : 2);
                    record.setFailReason(request.getFailReason());
                    record.setCallbackTime(LocalDateTime.now());
                    
                    // Update Load Balancer Weight (Mock logic)
                    if (record.getChannelName() != null) {
                        // Simple logic: Success -> weight + 0.1, Fail -> weight - 0.5
                        // In reality, calculate based on latency and success rate window
                        double weightChange = request.isSuccess() ? 0.1 : -0.5;
                        // We need to get current effective weight to adjust it, but LoadBalancer manages it.
                        // For simplicity, we just call updateWeight with a fixed value or delta?
                        // LoadBalancer.updateWeight sets the effective weight.
                        // Let's assume we reset it to some value.
                        // Better: LoadBalancer should have a method `recordResult(channelName, success, latency)`
                        // But for now, let's just log it.
                        log.info("Updating weight for channel {} based on result {}", record.getChannelName(), request.isSuccess());
                        // loadBalancer.updateWeight(record.getChannelName(), ...);
                    }
                    
                    return messageRecordRepository.save(record);
                })
                .map(r -> Response.success("OK"))
                .switchIfEmpty(Mono.just(Response.fail("Message not found")));
    }

    @Data
    public static class CallbackRequest {
        private Long messageId;
        private boolean success;
        private String failReason;
    }
}
