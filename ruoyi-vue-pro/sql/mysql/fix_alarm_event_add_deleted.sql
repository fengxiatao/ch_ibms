-- 修复 iot_alarm_event 表，添加 deleted 字段
-- 如果表不存在则创建，如果字段不存在则添加

-- 检查并添加 deleted 字段
ALTER TABLE `iot_alarm_event` 
ADD COLUMN IF NOT EXISTS `deleted` BIT(1) NOT NULL DEFAULT b'0' COMMENT '是否删除' AFTER `update_time`;

-- 如果表不存在，则创建完整的表
CREATE TABLE IF NOT EXISTS `iot_alarm_event` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '事件ID',
    `host_id` BIGINT NOT NULL COMMENT '报警主机ID',
    `event_code` VARCHAR(10) NOT NULL COMMENT '事件码（如1130、3401等）',
    `event_type` VARCHAR(50) NOT NULL COMMENT '事件类型：ALARM-报警, ARM-布防, DISARM-撤防, FAULT-故障, BYPASS-旁路',
    `event_level` VARCHAR(20) DEFAULT 'INFO' COMMENT '事件级别：INFO-信息, WARNING-警告, ERROR-错误, CRITICAL-严重',
    `area_no` INT DEFAULT 0 COMMENT '分区号',
    `zone_no` INT DEFAULT 0 COMMENT '防区号',
    `user_no` INT DEFAULT 0 COMMENT '用户号',
    `sequence` VARCHAR(20) COMMENT '序列号',
    `event_desc` VARCHAR(500) COMMENT '事件描述',
    `raw_data` VARCHAR(1000) COMMENT '原始数据',
    `is_new_event` TINYINT(1) DEFAULT 1 COMMENT '是否新事件：1-新事件（千位1），0-恢复事件（千位3）',
    `is_handled` TINYINT(1) DEFAULT 0 COMMENT '是否已处理：0-未处理，1-已处理',
    `handled_by` VARCHAR(64) COMMENT '处理人',
    `handled_time` DATETIME COMMENT '处理时间',
    `handle_remark` VARCHAR(500) COMMENT '处理备注',
    `creator` VARCHAR(64) DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` VARCHAR(64) DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` BIT(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`),
    INDEX `idx_host_id` (`host_id`),
    INDEX `idx_event_code` (`event_code`),
    INDEX `idx_event_type` (`event_type`),
    INDEX `idx_create_time` (`create_time`),
    INDEX `idx_is_handled` (`is_handled`),
    INDEX `idx_zone_no` (`host_id`, `zone_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='报警主机事件记录表';
