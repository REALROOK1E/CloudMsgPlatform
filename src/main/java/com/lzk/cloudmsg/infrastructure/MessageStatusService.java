package com.lzk.cloudmsg.infrastructure;

import com.lzk.cloudmsg.domain.MessageRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class MessageStatusService {

    private final MessageRecordRepository repository;
    
    // Sink to buffer status updates
    private final Sinks.Many<MessageRecord> sink = Sinks.many().multicast().onBackpressureBuffer();

    @PostConstruct
    public void init() {
        // Consume the sink in batches
        sink.asFlux()
                .bufferTimeout(100, Duration.ofSeconds(1)) // Batch size 100 or max wait 1s
                .flatMap(this::saveBatch)
                .subscribe();
    }

    public void updateStatus(MessageRecord record) {
        // Emit to sink instead of saving directly
        sink.tryEmitNext(record);
    }

    private Mono<Void> saveBatch(List<MessageRecord> records) {
        if (records.isEmpty()) return Mono.empty();
        
        log.debug("Saving batch of {} status updates", records.size());
        return repository.saveAll(records).then();
    }
}
