-- ----------------------------
-- IoT 发现设备表
-- 用于记录通过自动发现功能找到的设备
-- ----------------------------
CREATE TABLE IF NOT EXISTS `iot_discovered_device` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `ip` VARCHAR(50) NOT NULL COMMENT 'IP地址',
    `mac` VARCHAR(50) NULL DEFAULT NULL COMMENT 'MAC地址',
    `vendor` VARCHAR(100) NULL DEFAULT NULL COMMENT '厂商',
    `model` VARCHAR(100) NULL DEFAULT NULL COMMENT '型号',
    `serial_number` VARCHAR(100) NULL DEFAULT NULL COMMENT '序列号',
    `device_type` VARCHAR(50) NULL DEFAULT NULL COMMENT '设备类型（camera、sensor、gateway等）',
    `firmware_version` VARCHAR(50) NULL DEFAULT NULL COMMENT '固件版本',
    `discovery_method` VARCHAR(20) NOT NULL COMMENT '发现方式（ONVIF、SSDP、ARP、MDNS等）',
    `discovery_time` DATETIME NOT NULL COMMENT '发现时间',
    
    -- 设备注册状态
    `added` BIT(1) NOT NULL DEFAULT b'0' COMMENT '是否已添加到平台',
    `device_id` BIGINT(20) NULL DEFAULT NULL COMMENT '平台设备ID（关联 iot_device 表）',
    `status` TINYINT(2) NOT NULL DEFAULT 1 COMMENT '状态: 1=已发现, 2=已通知, 3=已忽略, 4=待处理, 5=已注册',
    
    -- 通知相关
    `notified_count` INT(11) NOT NULL DEFAULT 0 COMMENT '通知次数',
    `last_notified_time` DATETIME NULL DEFAULT NULL COMMENT '最后通知时间',
    
    -- 忽略相关
    `ignored_by` BIGINT(20) NULL DEFAULT NULL COMMENT '忽略操作人ID',
    `ignored_time` DATETIME NULL DEFAULT NULL COMMENT '忽略时间',
    `ignore_reason` VARCHAR(255) NULL DEFAULT NULL COMMENT '忽略原因',
    `ignore_until` DATETIME NULL DEFAULT NULL COMMENT '忽略截止时间（NULL表示永久忽略）',
    
    -- BaseDO 标准字段
    `creator` VARCHAR(64) NULL DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` VARCHAR(64) NULL DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` BIT(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id` BIGINT(20) NOT NULL DEFAULT 0 COMMENT '租户编号',
    
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_ip_discovery_time` (`ip`, `discovery_time`, `deleted`) COMMENT 'IP和发现时间唯一索引',
    KEY `idx_ip` (`ip`) COMMENT 'IP地址索引',
    KEY `idx_status` (`status`) COMMENT '状态索引',
    KEY `idx_discovery_time` (`discovery_time`) COMMENT '发现时间索引',
    KEY `idx_device_id` (`device_id`) COMMENT '平台设备ID索引',
    KEY `idx_ignored_by` (`ignored_by`) COMMENT '忽略操作人索引',
    KEY `idx_create_time` (`create_time`) COMMENT '创建时间索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='IoT 发现设备表';

-- ----------------------------
-- 示例数据（可选，用于测试）
-- ----------------------------
-- INSERT INTO `iot_discovered_device` (`ip`, `vendor`, `device_type`, `discovery_method`, `discovery_time`, `status`) VALUES
-- ('192.168.1.100', 'Hikvision', 'camera', 'ONVIF', NOW(), 1),
-- ('192.168.1.101', 'Dahua', 'camera', 'ONVIF', NOW(), 1);














