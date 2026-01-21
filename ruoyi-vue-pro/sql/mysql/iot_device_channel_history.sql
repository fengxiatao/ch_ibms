-- =============================================
-- IoT 设备通道历史记录表
-- 用于记录通道的增删改操作历史
-- =============================================

CREATE TABLE IF NOT EXISTS `iot_device_channel_history` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '历史记录ID',
    `channel_id` BIGINT NOT NULL COMMENT '通道ID',
    `device_id` BIGINT NOT NULL COMMENT '所属设备ID',
    `channel_no` INT DEFAULT NULL COMMENT '通道号',
    `operation` VARCHAR(32) NOT NULL COMMENT '操作类型（INSERT、UPDATE、DELETE、SOFT_DELETE）',
    `operation_desc` VARCHAR(255) DEFAULT NULL COMMENT '操作描述',
    `channel_data` TEXT COMMENT '通道数据快照（JSON格式）',
    `changed_fields` TEXT COMMENT '变更字段（JSON格式）',
    `old_values` TEXT COMMENT '旧值（JSON格式）',
    `new_values` TEXT COMMENT '新值（JSON格式）',
    `operator` VARCHAR(64) DEFAULT NULL COMMENT '操作人',
    `operator_id` BIGINT DEFAULT NULL COMMENT '操作人ID',
    `operate_time` DATETIME NOT NULL COMMENT '操作时间',
    `operate_ip` VARCHAR(64) DEFAULT NULL COMMENT '操作IP',
    `sync_source` VARCHAR(32) DEFAULT NULL COMMENT '同步来源',
    `sync_batch_id` VARCHAR(64) DEFAULT NULL COMMENT '同步批次ID',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    KEY `idx_channel_id` (`channel_id`),
    KEY `idx_device_id` (`device_id`),
    KEY `idx_operation` (`operation`),
    KEY `idx_operate_time` (`operate_time`),
    KEY `idx_sync_batch_id` (`sync_batch_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='IoT 设备通道历史记录表';
