-- ----------------------------
-- IoT 消息幂等性检查表
-- 用于确保消息只被处理一次，防止重复消费
-- ----------------------------
CREATE TABLE IF NOT EXISTS `iot_message_idempotent` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `message_id` VARCHAR(255) NOT NULL COMMENT '消息ID（业务生成，如：设备IP_时间戳）',
    `topic` VARCHAR(255) NOT NULL COMMENT '消息主题（RocketMQ Topic）',
    `status` VARCHAR(20) NOT NULL DEFAULT 'PROCESSING' COMMENT '状态: PROCESSING=处理中, SUCCESS=成功, FAILED=失败',
    `processed_time` DATETIME NULL DEFAULT NULL COMMENT '处理完成时间',
    `error_message` TEXT NULL COMMENT '错误信息',
    
    -- BaseDO 标准字段
    `creator` VARCHAR(64) NULL DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` VARCHAR(64) NULL DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` BIT(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id` BIGINT(20) NOT NULL DEFAULT 0 COMMENT '租户编号',
    
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_message_id_topic` (`message_id`, `topic`, `deleted`) COMMENT '消息ID和主题唯一索引',
    KEY `idx_topic_status` (`topic`, `status`) COMMENT '主题和状态索引',
    KEY `idx_create_time` (`create_time`) COMMENT '创建时间索引，用于清理过期数据'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='IoT 消息幂等性检查表';

-- ----------------------------
-- 示例数据（可选，用于测试）
-- ----------------------------
-- INSERT INTO `iot_message_idempotent` (`message_id`, `topic`, `status`, `processed_time`) VALUES
-- ('192.168.1.100_2025-10-27T10:00:00', 'iot_device_discovered', 'SUCCESS', NOW());














