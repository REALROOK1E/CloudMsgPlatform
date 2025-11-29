package com.lzk.cloudmsg.task;

import com.lzk.cloudmsg.infrastructure.MessageRecordRepository;
import com.lzk.cloudmsg.send.SendRouter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Slf4j
@RequiredArgsConstructor
public class RetryTask {

    private final MessageRecordRepository messageRecordRepository;
    private final SendRouter sendRouter;

    @Scheduled(fixedRate = 60000) // Every minute
    public void retryFailedMessages() {
        log.info("Starting retry task...");
        messageRecordRepository.findByStatusAndRetryCountLessThan(2, 3)
                .flatMap(record -> {
                    log.info("Retrying message: {}", record.getId());
                    record.setRetryCount(record.getRetryCount() + 1);
                    
                    return sendRouter.routeAndSend(record)
                            .then(Mono.defer(() -> {
                                record.setStatus(1); // Assume success if no error
                                return messageRecordRepository.save(record);
                            }))
                            .onErrorResume(e -> {
                                log.error("Retry failed for message {}", record.getId(), e);
                                // Status remains 2, retry count increased
                                if (record.getRetryCount() >= 3) {
                                    record.setStatus(-1); // Dead letter
                                }
                                return messageRecordRepository.save(record).then();
                            });
                })
                .subscribe();
    }
}
