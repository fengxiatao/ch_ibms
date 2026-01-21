-- 报警主机分区表
CREATE TABLE IF NOT EXISTS `iot_alarm_partition` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '分区ID',
    `host_id` BIGINT NOT NULL COMMENT '主机ID',
    `partition_no` INT NOT NULL COMMENT '分区号',
    `partition_name` VARCHAR(100) DEFAULT NULL COMMENT '分区名称',
    `status` TINYINT DEFAULT 0 COMMENT '状态：0=撤防，1=布防，2=居家布防',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '分区描述',
    `creator` VARCHAR(64) DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` VARCHAR(64) DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` BIT(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_host_partition` (`host_id`, `partition_no`, `deleted`),
    KEY `idx_host_id` (`host_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='报警主机分区表';

-- 报警主机防区表
CREATE TABLE IF NOT EXISTS `iot_alarm_zone` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '防区ID',
    `host_id` BIGINT NOT NULL COMMENT '主机ID',
    `partition_id` BIGINT DEFAULT NULL COMMENT '分区ID',
    `zone_no` INT NOT NULL COMMENT '防区号',
    `zone_name` VARCHAR(100) DEFAULT NULL COMMENT '防区名称',
    `zone_type` VARCHAR(50) DEFAULT NULL COMMENT '防区类型：DOOR-门磁, PIR-红外, SMOKE-烟感等',
    `zone_status` VARCHAR(20) DEFAULT NULL COMMENT '防区状态：ARM-布防, DISARM-撤防, BYPASS-旁路',
    `area_location` VARCHAR(100) DEFAULT NULL COMMENT '区域位置',
    `status` CHAR(1) DEFAULT NULL COMMENT '状态字符：a/b/A/B/C/D/E/F/G/H/I（协议原始状态）',
    `status_name` VARCHAR(50) DEFAULT NULL COMMENT '状态名称',
    `is_armed` TINYINT(1) DEFAULT 0 COMMENT '是否布防：0=否，1=是',
    `is_alarming` TINYINT(1) DEFAULT 0 COMMENT '是否报警：0=否，1=是',
    `online_status` INT DEFAULT 1 COMMENT '在线状态：0=离线, 1=在线',
    `is_important` TINYINT(1) DEFAULT 0 COMMENT '是否重要防区：0=否, 1=是',
    `is_24h` TINYINT(1) DEFAULT 0 COMMENT '是否24小时防区：0=否, 1=是',
    `alarm_count` INT DEFAULT 0 COMMENT '报警次数统计',
    `last_alarm_time` DATETIME DEFAULT NULL COMMENT '最后报警时间',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `creator` VARCHAR(64) DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` VARCHAR(64) DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` BIT(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_host_zone` (`host_id`, `zone_no`, `deleted`),
    KEY `idx_host_id` (`host_id`),
    KEY `idx_partition_id` (`partition_id`),
    KEY `idx_is_alarming` (`is_alarming`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='报警主机防区表';

-- 为报警主机表添加系统状态字段（如果不存在）
ALTER TABLE `iot_alarm_host` 
ADD COLUMN IF NOT EXISTS `system_status` TINYINT DEFAULT 0 COMMENT '系统状态：0=撤防，1=布防，2=居家布防' AFTER `status`,
ADD COLUMN IF NOT EXISTS `last_query_time` DATETIME DEFAULT NULL COMMENT '最后查询时间' AFTER `system_status`;

-- 插入示例数据（可选）
-- 假设已有主机ID为1的报警主机

-- 插入默认分区
INSERT INTO `iot_alarm_partition` (`host_id`, `partition_no`, `partition_name`, `status`, `tenant_id`)
VALUES (1, 1, '默认分区', 0, 1)
ON DUPLICATE KEY UPDATE `partition_name` = VALUES(`partition_name`);

-- 插入防区示例数据
INSERT INTO `iot_alarm_zone` (`host_id`, `partition_id`, `zone_no`, `zone_name`, `zone_type`, `status`, `status_name`, `is_armed`, `is_alarming`, `tenant_id`)
VALUES 
(1, 1, 1, '前门', 'DOOR', 'b', '防区旁路', 0, 0, 1),
(1, 1, 2, '后门', 'DOOR', 'b', '防区旁路', 0, 0, 1),
(1, 1, 3, '客厅红外', 'PIR', 'b', '防区旁路', 0, 0, 1),
(1, 1, 4, '卧室红外', 'PIR', 'b', '防区旁路', 0, 0, 1),
(1, 1, 5, '厨房烟感', 'SMOKE', 'b', '防区旁路', 0, 0, 1),
(1, 1, 6, '阳台红外', 'PIR', 'b', '防区旁路', 0, 0, 1),
(1, 1, 7, '车库门磁', 'DOOR', 'b', '防区旁路', 0, 0, 1),
(1, 1, 8, '窗户', 'DOOR', 'B', '防区布防+正在报警', 1, 1, 1)
ON DUPLICATE KEY UPDATE 
    `zone_name` = VALUES(`zone_name`),
    `zone_type` = VALUES(`zone_type`),
    `status` = VALUES(`status`),
    `status_name` = VALUES(`status_name`),
    `is_armed` = VALUES(`is_armed`),
    `is_alarming` = VALUES(`is_alarming`);
