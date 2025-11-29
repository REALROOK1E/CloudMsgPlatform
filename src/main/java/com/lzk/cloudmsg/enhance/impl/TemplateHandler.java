package com.lzk.cloudmsg.enhance.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.lzk.cloudmsg.domain.MessageRecord;
import com.lzk.cloudmsg.enhance.EnhanceChain;
import com.lzk.cloudmsg.enhance.EnhanceHandler;
import com.lzk.cloudmsg.infrastructure.MessageTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
@Order(0)
@Slf4j
@RequiredArgsConstructor
public class TemplateHandler implements EnhanceHandler {

    private final MessageTemplateRepository templateRepository;

    @Override
    public Mono<Void> handle(MessageRecord messageRecord, EnhanceChain chain) {
        return templateRepository.findById(messageRecord.getTemplateId())
                .flatMap(template -> {
                    String content = template.getMsgContent();
                    Map<String, String> variables = JSON.parseObject(messageRecord.getVariables(), new TypeReference<Map<String, String>>() {});
                    
                    if (variables != null) {
                        for (Map.Entry<String, String> entry : variables.entrySet()) {
                            content = StringUtils.replace(content, "{{" + entry.getKey() + "}}", entry.getValue());
                        }
                    }
                    
                    messageRecord.setContent(content);
                    messageRecord.setSendChannel(template.getSendChannel());
                    
                    return chain.next(messageRecord);
                })
                .switchIfEmpty(Mono.error(new RuntimeException("Template not found: " + messageRecord.getTemplateId())));
    }
}
