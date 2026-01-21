-- 摄像头预设点表
CREATE TABLE IF NOT EXISTS `iot_camera_preset` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '预设点ID',
    `channel_id` BIGINT(20) NOT NULL COMMENT '通道ID',
    `preset_no` INT(11) NOT NULL COMMENT '预设点编号（1-255）',
    `preset_name` VARCHAR(100) NOT NULL COMMENT '预设点名称',
    `description` VARCHAR(500) NULL DEFAULT NULL COMMENT '预设点描述',
    `pan` DECIMAL(10, 6) NULL DEFAULT NULL COMMENT '水平角度（Pan）',
    `tilt` DECIMAL(10, 6) NULL DEFAULT NULL COMMENT '垂直角度（Tilt）',
    `zoom` DECIMAL(10, 6) NULL DEFAULT NULL COMMENT '变焦值（Zoom）',
    `creator` VARCHAR(64) NULL DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` VARCHAR(64) NULL DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` BIT(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id` BIGINT(20) NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_channel_preset` (`channel_id`, `preset_no`, `deleted`, `tenant_id`) COMMENT '同一通道下预设点编号唯一',
    INDEX `idx_channel_id` (`channel_id`) USING BTREE,
    INDEX `idx_tenant_id` (`tenant_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='摄像头预设点表';
