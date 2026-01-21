-- 为 iot_alarm_zone 表添加缺失的字段
-- 执行时间：2025-12-03

USE `ruoyi-vue-pro`;

-- 添加协议原始状态字段
ALTER TABLE `iot_alarm_zone` 
ADD COLUMN `status` VARCHAR(10) DEFAULT NULL COMMENT '状态字符：a/b/A/B/C/D/E/F/G/H/I（协议原始状态）' AFTER `zone_status`;

-- 添加状态名称字段
ALTER TABLE `iot_alarm_zone` 
ADD COLUMN `status_name` VARCHAR(50) DEFAULT NULL COMMENT '状态名称' AFTER `status`;

-- 添加是否布防字段
ALTER TABLE `iot_alarm_zone` 
ADD COLUMN `is_armed` TINYINT NOT NULL DEFAULT 0 COMMENT '是否布防：0-否, 1-是' AFTER `status_name`;

-- 添加是否报警字段
ALTER TABLE `iot_alarm_zone` 
ADD COLUMN `is_alarming` TINYINT NOT NULL DEFAULT 0 COMMENT '是否报警：0-否, 1-是' AFTER `is_armed`;

-- 添加所属分区ID字段
ALTER TABLE `iot_alarm_zone` 
ADD COLUMN `partition_id` BIGINT DEFAULT NULL COMMENT '所属分区ID' AFTER `online_status`;

-- 添加分区ID索引
ALTER TABLE `iot_alarm_zone` 
ADD INDEX `idx_partition_id` (`partition_id`);

-- 查看表结构
DESC `iot_alarm_zone`;
