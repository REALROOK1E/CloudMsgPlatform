CREATE TABLE IF NOT EXISTS message_template (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    audit_status TINYINT DEFAULT 0 COMMENT '0: pending, 1: approved, 2: rejected',
    msg_status TINYINT DEFAULT 0 COMMENT '0: disabled, 1: enabled',
    id_type TINYINT DEFAULT 0 COMMENT '10: userId, 20: did, 30: phone, 40: openId, 50: email',
    send_channel TINYINT DEFAULT 0 COMMENT '10: IM, 20: Push, 30: SMS, 40: Email',
    template_type TINYINT DEFAULT 0 COMMENT '10: operation, 20: tech, 30: notification',
    msg_type TINYINT DEFAULT 0 COMMENT '10: notification, 20: marketing, 30: verification',
    expect_push_time VARCHAR(100) COMMENT 'cron expression',
    msg_content VARCHAR(1024) NOT NULL,
    dedupe_key_expression VARCHAR(255),
    send_account INT DEFAULT 0,
    creator VARCHAR(50),
    updator VARCHAR(50),
    is_deleted TINYINT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS message_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    template_id BIGINT NOT NULL,
    business_id VARCHAR(64) NOT NULL,
    receiver VARCHAR(100) NOT NULL,
    content TEXT,
    variables TEXT,
    status TINYINT DEFAULT 0 COMMENT '0: pending, 1: success, 2: fail, -1: cannot_retry',
    fail_reason VARCHAR(255),
    retry_count INT DEFAULT 0,
    send_channel INT,
    channel_name VARCHAR(50),
    trace_id VARCHAR(64),
    callback_time DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_trace_id (trace_id),
    INDEX idx_business_id (business_id)
);

CREATE TABLE IF NOT EXISTS crowd_pack (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    crowd_key VARCHAR(64) NOT NULL UNIQUE,
    crowd_name VARCHAR(100),
    user_count INT DEFAULT 0,
    file_path VARCHAR(255),
    status TINYINT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);
