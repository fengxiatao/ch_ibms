-- ================================================
-- IoT 设备表 - 添加 device_key 字段
-- ================================================
-- 说明：修复激活设备时 device_key 字段缺失的问题
-- 日期：2025-10-27
-- ================================================

USE `ruoyi-vue-pro`;

-- 添加 device_key 字段
ALTER TABLE `iot_device`
ADD COLUMN `device_key` varchar(255) NULL COMMENT '设备唯一标识（格式：{productKey}_{serialNumber} 或 {productKey}_{UUID}）'
AFTER `product_key`;

-- 为已存在的设备生成 device_key
-- 规则：product_key + "_" + device_name（或UUID）
UPDATE `iot_device`
SET `device_key` = CONCAT(product_key, '_', COALESCE(device_name, UUID()))
WHERE `device_key` IS NULL OR `device_key` = '';

-- 添加唯一索引（设备key在全表唯一）
ALTER TABLE `iot_device`
ADD UNIQUE INDEX `uk_device_key` (`device_key`, `deleted`) USING BTREE COMMENT '设备唯一标识';

-- 验证数据
SELECT 
    id,
    device_name,
    product_key,
    device_key,
    device_secret,
    state
FROM `iot_device`
WHERE device_key IS NOT NULL
LIMIT 10;

-- ================================================
-- 执行完成提示
-- ================================================
SELECT '✅ device_key 字段添加成功！' AS message;














