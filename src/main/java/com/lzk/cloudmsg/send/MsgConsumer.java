package com.lzk.cloudmsg.send;

import com.alibaba.fastjson2.JSON;
import com.lzk.cloudmsg.access.dto.MessageRequest;
import com.lzk.cloudmsg.domain.MessageRecord;
import com.lzk.cloudmsg.enhance.ChainManager;
import com.lzk.cloudmsg.infrastructure.MessageRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Slf4j
@RequiredArgsConstructor
public class MsgConsumer {

    private final ChainManager chainManager;
    private final SendRouter sendRouter;
    private final MessageRecordRepository messageRecordRepository;

    @KafkaListener(topics = "cloud-msg-access", groupId = "cloud-msg-group")
    public void consume(String message) {
        try {
            MessageRequest request = JSON.parseObject(message, MessageRequest.class);
            log.info("Received request: {}", request.getBusinessId());

            Flux.fromIterable(request.getReceivers())
                    .flatMap(receiver -> {
                        MessageRecord record = MessageRecord.builder()
                                .templateId(request.getTemplateId())
                                .businessId(request.getBusinessId())
                                .receiver(receiver)
                                .variables(JSON.toJSONString(request.getVariables()))
                                .status(0) // Pending
                                .build();
                        
                        // Save -> Enhance -> Send -> Update Status
                        return messageRecordRepository.save(record)
                                .flatMap(savedRecord -> 
                                    chainManager.execute(savedRecord, sendRouter.routeAndSend(savedRecord))
                                        .then(Mono.defer(() -> {
                                            savedRecord.setStatus(1); // Success
                                            return messageRecordRepository.save(savedRecord);
                                        }))
                                        .onErrorResume(e -> {
                                            log.error("Failed to send message to {}", savedRecord.getReceiver(), e);
                                            savedRecord.setStatus(2); // Fail
                                            savedRecord.setFailReason(e.getMessage());
                                            return messageRecordRepository.save(savedRecord).then();
                                        })
                                );
                    })
                    .subscribe(
                            null,
                            e -> log.error("Error processing message batch", e),
                            () -> log.debug("Finished processing batch")
                    );

        } catch (Exception e) {
            log.error("Failed to parse message", e);
        }
    }
}
