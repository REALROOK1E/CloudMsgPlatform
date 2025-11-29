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
@Table("message_template")
public class MessageTemplate {

    @Id
    private Long id;
    private String name;
    private Integer auditStatus; // 0: pending, 1: approved, 2: rejected
    private Integer msgStatus; // 0: disabled, 1: enabled
    private Integer idType; // 10: userId, 20: did, 30: phone, 40: openId, 50: email
    private Integer sendChannel; // 10: IM, 20: Push, 30: SMS, 40: Email
    private Integer templateType; // 10: operation, 20: tech, 30: notification
    private Integer msgType; // 10: notification, 20: marketing, 30: verification
    private String expectPushTime; // cron expression
    private String msgContent;
    private String dedupeKeyExpression; // SpEL expression for deduplication key
    private Integer sendAccount;
    private String creator;
    private String updator;
    private Integer isDeleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
