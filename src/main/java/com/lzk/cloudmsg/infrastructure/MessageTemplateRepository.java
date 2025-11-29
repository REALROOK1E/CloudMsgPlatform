package com.lzk.cloudmsg.infrastructure;

import com.lzk.cloudmsg.domain.MessageTemplate;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageTemplateRepository extends R2dbcRepository<MessageTemplate, Long> {
}
