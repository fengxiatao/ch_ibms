-- ========================================
-- IoT 设备表结构升级脚本
-- 用于修复 iot_device 表字段缺失问题
-- ========================================
-- 问题：Unknown column 'nickname' in 'field list'
-- 日期：2025-10-22
-- ========================================

USE ch_ibms;

-- 备份提示
SELECT '⚠️ 建议先备份 iot_device 表数据！' AS warning;

-- 检查表是否存在
SELECT 
    CASE 
        WHEN COUNT(*) > 0 THEN '✅ iot_device 表存在，准备升级...'
        ELSE '❌ iot_device 表不存在，请先执行 iot_module.sql 创建表'
    END AS check_result
FROM information_schema.tables 
WHERE table_schema = 'ch_ibms' 
  AND table_name = 'iot_device';

-- ========================================
-- 步骤1: 添加缺失的字段
-- ========================================

-- 添加 nickname 字段（设备备注名称）
ALTER TABLE `iot_device` 
ADD COLUMN `nickname` VARCHAR(64) NULL COMMENT '设备备注名称' AFTER `device_name`;

-- 添加 serial_number 字段（设备序列号）
ALTER TABLE `iot_device` 
ADD COLUMN `serial_number` VARCHAR(64) NULL COMMENT '设备序列号' AFTER `nickname`;

-- 添加 pic_url 字段（设备图片）
ALTER TABLE `iot_device` 
ADD COLUMN `pic_url` VARCHAR(255) NULL COMMENT '设备图片' AFTER `serial_number`;

-- 添加 group_ids 字段（设备分组编号集合，逗号分隔）
ALTER TABLE `iot_device` 
ADD COLUMN `group_ids` VARCHAR(500) NULL COMMENT '设备分组编号集合（逗号分隔）' AFTER `pic_url`;

-- 添加 product_key 字段（产品标识）
ALTER TABLE `iot_device` 
ADD COLUMN `product_key` VARCHAR(64) NULL COMMENT '产品标识' AFTER `product_id`;

-- 添加 device_type 字段（设备类型）
ALTER TABLE `iot_device` 
ADD COLUMN `device_type` TINYINT NULL COMMENT '设备类型：0-直连设备 1-网关设备 2-网关子设备' AFTER `product_key`;

-- ========================================
-- 步骤2: 修改现有字段以匹配实体类
-- ========================================

-- 将 status 字段重命名为 state（与实体类一致）
ALTER TABLE `iot_device` 
CHANGE COLUMN `status` `state` TINYINT NOT NULL DEFAULT 0 COMMENT '设备状态：0-未激活 1-在线 2-离线';

-- 将 last_online_time 重命名为 online_time
ALTER TABLE `iot_device` 
CHANGE COLUMN `last_online_time` `online_time` DATETIME NULL COMMENT '最后上线时间';

-- 添加 offline_time 字段（最后离线时间）
ALTER TABLE `iot_device` 
ADD COLUMN `offline_time` DATETIME NULL COMMENT '最后离线时间' AFTER `online_time`;

-- 将 firmware_version 改为 firmware_id（关联固件表）
ALTER TABLE `iot_device` 
DROP COLUMN `firmware_version`;

ALTER TABLE `iot_device` 
ADD COLUMN `firmware_id` BIGINT NULL COMMENT '固件编号' AFTER `ip`;

-- ========================================
-- 步骤3: 添加认证和定位相关字段
-- ========================================

-- 添加 auth_type 字段（认证类型）
ALTER TABLE `iot_device` 
ADD COLUMN `auth_type` VARCHAR(32) NULL COMMENT '认证类型（如一机一密、动态注册）' AFTER `device_secret`;

-- 添加 location_type 字段（定位方式）
ALTER TABLE `iot_device` 
ADD COLUMN `location_type` TINYINT NULL COMMENT '定位方式：1-GPS 2-北斗 3-WiFi 4-基站 5-其他' AFTER `ip`;

-- 删除旧的 location 字段
ALTER TABLE `iot_device` 
DROP COLUMN `location`;

-- 添加 latitude 字段（纬度）
ALTER TABLE `iot_device` 
ADD COLUMN `latitude` DECIMAL(10, 6) NULL COMMENT '设备位置的纬度' AFTER `location_type`;

-- 添加 longitude 字段（经度）
ALTER TABLE `iot_device` 
ADD COLUMN `longitude` DECIMAL(10, 6) NULL COMMENT '设备位置的经度' AFTER `latitude`;

-- 添加 area_id 字段（地区编码）
ALTER TABLE `iot_device` 
ADD COLUMN `area_id` INT NULL COMMENT '地区编码' AFTER `longitude`;

-- 添加 address 字段（设备详细地址）
ALTER TABLE `iot_device` 
ADD COLUMN `address` VARCHAR(255) NULL COMMENT '设备详细地址' AFTER `area_id`;

-- ========================================
-- 步骤4: 添加配置字段
-- ========================================

-- 添加 config 字段（设备配置，JSON格式）
ALTER TABLE `iot_device` 
ADD COLUMN `config` TEXT NULL COMMENT '设备配置（JSON格式）' AFTER `address`;

-- job_config 字段已存在，确保在 config 之后
-- （如果顺序不对，可以调整）

-- ========================================
-- 步骤5: 删除不需要的字段
-- ========================================

-- 删除 description 字段（实体类中没有）
ALTER TABLE `iot_device` 
DROP COLUMN `description`;

-- 删除 device_key 字段（实体类中用 device_name 标识）
ALTER TABLE `iot_device` 
DROP COLUMN `device_key`;

-- ========================================
-- 步骤6: 调整索引
-- ========================================

-- 删除旧的唯一索引
ALTER TABLE `iot_device` 
DROP INDEX `uk_device_key`;

-- 添加新的唯一索引（product_id + device_name）
ALTER TABLE `iot_device` 
ADD UNIQUE KEY `uk_product_device` (`product_id`, `device_name`, `tenant_id`) USING BTREE COMMENT '同产品下设备名称唯一';

-- 删除旧的 status 索引
ALTER TABLE `iot_device` 
DROP INDEX `idx_status`;

-- 添加新的 state 索引
ALTER TABLE `iot_device` 
ADD KEY `idx_state` (`state`) USING BTREE;

-- ========================================
-- 完成提示
-- ========================================

SELECT '✅ iot_device 表结构升级完成！' AS result;

SELECT 
    COLUMN_NAME,
    COLUMN_TYPE,
    IS_NULLABLE,
    COLUMN_COMMENT
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = 'ch_ibms'
  AND TABLE_NAME = 'iot_device'
ORDER BY ORDINAL_POSITION;





