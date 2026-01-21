-- ============================================================================
-- IoT 消息发件箱表（本地消息表）
-- 
-- 功能：
-- 1. 确保消息可靠投递（本地事务+消息补偿）
-- 2. 防止消息丢失（RocketMQ 宕机时保存到本地）
-- 3. 自动重试机制（定时任务扫描失败消息）
-- 4. 消息追踪和审计
--
-- 创建日期：2025-10-26
-- 作者：长辉信息科技有限公司
-- ============================================================================

CREATE TABLE IF NOT EXISTS `iot_message_outbox` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    `topic` VARCHAR(100) NOT NULL COMMENT '消息主题',
    `message_key` VARCHAR(100) COMMENT '消息Key（用于去重）',
    `content` TEXT NOT NULL COMMENT '消息内容（JSON格式）',
    `status` VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态: PENDING=待发送 SENT=已发送 FAILED=失败',
    `retry_count` INT DEFAULT 0 COMMENT '重试次数',
    `max_retry` INT DEFAULT 3 COMMENT '最大重试次数',
    `error_message` VARCHAR(500) COMMENT '错误信息',
    `sent_time` DATETIME COMMENT '成功发送时间',
    `creator` VARCHAR(64) DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` VARCHAR(64) DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` BIT(1) DEFAULT b'0' COMMENT '是否删除',
    `tenant_id` BIGINT DEFAULT 0 COMMENT '租户编号',
    INDEX `idx_status_time` (`status`, `create_time`) COMMENT '状态+时间索引',
    INDEX `idx_message_key` (`message_key`) COMMENT '消息Key索引',
    INDEX `idx_topic` (`topic`) COMMENT '主题索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='IoT 消息发件箱（本地消息表）';

-- ============================================================================
-- 消息幂等性检查表
-- ============================================================================

CREATE TABLE IF NOT EXISTS `iot_message_idempotent` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    `message_id` VARCHAR(100) NOT NULL COMMENT '消息ID（业务生成）',
    `topic` VARCHAR(100) NOT NULL COMMENT '消息主题',
    `status` VARCHAR(20) DEFAULT 'PROCESSING' COMMENT '状态: PROCESSING=处理中 SUCCESS=成功 FAILED=失败',
    `processed_time` DATETIME COMMENT '处理完成时间',
    `error_message` VARCHAR(500) COMMENT '错误信息',
    `creator` VARCHAR(64) DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` VARCHAR(64) DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` BIT(1) DEFAULT b'0' COMMENT '是否删除',
    `tenant_id` BIGINT DEFAULT 0 COMMENT '租户编号',
    UNIQUE INDEX `uk_message_id` (`message_id`) COMMENT '消息ID唯一索引',
    INDEX `idx_status_time` (`status`, `create_time`) COMMENT '状态+时间索引',
    INDEX `idx_topic` (`topic`) COMMENT '主题索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='IoT 消息幂等性检查表';

-- ============================================================================
-- 说明：
-- 
-- 本地消息表模式（Transactional Outbox Pattern）：
-- 1. 业务操作和消息保存在同一个本地事务中
-- 2. 定时任务扫描待发送消息，异步发送到 RocketMQ
-- 3. 发送失败会自动重试（指数退避）
-- 4. 超过最大重试次数标记为 FAILED，需要人工介入
--
-- 消息幂等性检查：
-- 1. 消费者收到消息后，先检查是否已处理（message_id 唯一约束）
-- 2. 如果已处理，直接返回成功，避免重复消费
-- 3. 如果未处理，标记为 PROCESSING，开始处理
-- 4. 处理完成后更新为 SUCCESS
-- 
-- 消息清理策略：
-- 1. SENT 状态的消息保留 7 天后删除
-- 2. FAILED 状态的消息保留 30 天后删除
-- 3. SUCCESS 状态的幂等记录保留 24 小时后删除
-- ============================================================================


