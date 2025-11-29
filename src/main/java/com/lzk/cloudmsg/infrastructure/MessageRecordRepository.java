package com.lzk.cloudmsg.infrastructure;

import com.lzk.cloudmsg.domain.MessageRecord;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface MessageRecordRepository extends R2dbcRepository<MessageRecord, Long> {
    Flux<MessageRecord> findByStatusAndRetryCountLessThan(Integer status, Integer retryCount);
}
