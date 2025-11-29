package com.lzk.cloudmsg.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("message_record")
public class MessageRecord {
    @Id
    private Long id;
    private Long templateId;
    private String businessId;
    private String receiver;
    private String content;
    private String variables; // JSON string of variables
    private Integer status; // 0: pending, 1: success, 2: fail, -1: cannot_retry
    private String failReason;
    private Integer retryCount;
    private Integer sendChannel;
    private String channelName; // Specific channel instance name (e.g. AliyunSMS)
    private String traceId;
    private LocalDateTime callbackTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
