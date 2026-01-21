-- 长辉设备升级日志表
-- 用于追踪固件升级过程，便于4G网络问题排查

CREATE TABLE IF NOT EXISTS `changhui_upgrade_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    `task_id` BIGINT NULL COMMENT '升级任务ID',
    `device_id` BIGINT NULL COMMENT '设备ID',
    `station_code` VARCHAR(32) NULL COMMENT '测站编码',
    `event_type` VARCHAR(50) NOT NULL COMMENT '事件类型',
    `event_description` VARCHAR(255) NULL COMMENT '事件描述',
    `progress` INT NULL COMMENT '当前进度(0-100)',
    `event_detail` TEXT NULL COMMENT '事件详情(JSON格式)',
    `network_info` TEXT NULL COMMENT '网络信息(JSON格式)',
    `event_time` DATETIME NOT NULL COMMENT '事件时间',
    `creator` VARCHAR(64) DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` VARCHAR(64) DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` BIT(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`),
    INDEX `idx_task_id` (`task_id`),
    INDEX `idx_device_id` (`device_id`),
    INDEX `idx_station_code` (`station_code`),
    INDEX `idx_event_type` (`event_type`),
    INDEX `idx_event_time` (`event_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='长辉设备升级日志';
