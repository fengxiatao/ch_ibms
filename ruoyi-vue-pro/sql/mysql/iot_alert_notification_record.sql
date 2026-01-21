-- 告警通知记录表
CREATE TABLE IF NOT EXISTS `iot_alert_notification_record` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '通知记录ID',
    `alert_id` BIGINT(20) NOT NULL COMMENT '告警ID',
    `alert_config_id` BIGINT(20) NOT NULL COMMENT '告警配置ID',
    `notification_type` VARCHAR(50) NOT NULL COMMENT '通知类型：email-邮件, sms-短信, websocket-推送, system-系统消息',
    `recipient` VARCHAR(500) NOT NULL COMMENT '接收人（邮箱/手机号/用户ID）',
    `title` VARCHAR(200) NOT NULL COMMENT '通知标题',
    `content` TEXT NOT NULL COMMENT '通知内容',
    `send_status` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '发送状态：0-待发送, 1-发送中, 2-发送成功, 3-发送失败',
    `send_time` DATETIME NULL COMMENT '发送时间',
    `fail_reason` VARCHAR(500) NULL COMMENT '失败原因',
    `retry_count` INT(11) NOT NULL DEFAULT 0 COMMENT '重试次数',
    `template_code` VARCHAR(100) NULL COMMENT '短信模板代码',
    `template_params` VARCHAR(1000) NULL COMMENT '模板参数（JSON）',
    
    -- 租户和审计字段
    `tenant_id` BIGINT(20) NOT NULL DEFAULT 0 COMMENT '租户编号',
    `creator` VARCHAR(64) DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` VARCHAR(64) DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` BIT(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    
    PRIMARY KEY (`id`),
    INDEX `idx_alert_id` (`alert_id`),
    INDEX `idx_alert_config_id` (`alert_config_id`),
    INDEX `idx_notification_type` (`notification_type`),
    INDEX `idx_send_status` (`send_status`),
    INDEX `idx_send_time` (`send_time`),
    INDEX `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='IoT告警通知记录表';

-- 告警通知规则表
CREATE TABLE IF NOT EXISTS `iot_alert_notification_rule` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '规则ID',
    `alert_config_id` BIGINT(20) NOT NULL COMMENT '告警配置ID',
    `rule_name` VARCHAR(100) NOT NULL COMMENT '规则名称',
    
    -- 通知渠道配置
    `email_enabled` BIT(1) NOT NULL DEFAULT b'0' COMMENT '是否启用邮件通知',
    `email_recipients` VARCHAR(1000) NULL COMMENT '邮件接收人列表（逗号分隔）',
    `email_template_code` VARCHAR(100) NULL COMMENT '邮件模板代码',
    
    `sms_enabled` BIT(1) NOT NULL DEFAULT b'0' COMMENT '是否启用短信通知',
    `sms_recipients` VARCHAR(1000) NULL COMMENT '短信接收人列表（逗号分隔）',
    `sms_template_code` VARCHAR(100) NULL COMMENT '短信模板代码',
    
    `websocket_enabled` BIT(1) NOT NULL DEFAULT b'1' COMMENT '是否启用WebSocket推送',
    `websocket_user_ids` VARCHAR(1000) NULL COMMENT '推送用户ID列表（逗号分隔）',
    
    `system_enabled` BIT(1) NOT NULL DEFAULT b'1' COMMENT '是否启用系统消息',
    `system_user_ids` VARCHAR(1000) NULL COMMENT '系统消息用户ID列表（逗号分隔）',
    
    -- 通知频率控制
    `notify_interval` INT(11) NOT NULL DEFAULT 0 COMMENT '通知间隔（秒），0表示每次都通知',
    `max_notify_count` INT(11) NOT NULL DEFAULT 0 COMMENT '最大通知次数，0表示不限制',
    
    -- 通知时段
    `notify_time_start` TIME NULL COMMENT '通知开始时间',
    `notify_time_end` TIME NULL COMMENT '通知结束时间',
    
    `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态：0-禁用, 1-启用',
    
    -- 租户和审计字段
    `tenant_id` BIGINT(20) NOT NULL DEFAULT 0 COMMENT '租户编号',
    `creator` VARCHAR(64) DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` VARCHAR(64) DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` BIT(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_alert_config_id` (`alert_config_id`, `deleted`),
    INDEX `idx_status` (`status`),
    INDEX `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='IoT告警通知规则表';












