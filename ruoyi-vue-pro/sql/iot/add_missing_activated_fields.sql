-- ============================================================================
-- 添加 iot_discovered_device 表缺失的 activated 相关字段
-- 
-- 问题：Java 代码中定义了 activated 相关字段，但数据库表中缺失这些字段
-- 导致 MyBatis 尝试插入不存在的字段，引发数据截断错误
--
-- 错误信息：Data too long for column 'added' at row 1
-- 实际原因：activated 字段不存在，MyBatis 可能将数据写入了错误的字段
-- ============================================================================

-- 1. 检查当前表结构
SELECT 
    COLUMN_NAME,
    DATA_TYPE,
    CHARACTER_MAXIMUM_LENGTH,
    IS_NULLABLE,
    COLUMN_DEFAULT,
    COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE() 
  AND TABLE_NAME = 'iot_discovered_device' 
ORDER BY ORDINAL_POSITION;

-- 2. 添加缺失的 activated 相关字段
-- 这些字段在 Java 代码 IotDiscoveredDeviceDO 中定义，但数据库表中缺失

-- 2.1 添加 activated 字段（是否已激活）
ALTER TABLE `iot_discovered_device` 
ADD COLUMN `activated` BIT(1) NOT NULL DEFAULT b'0' COMMENT '是否已激活' AFTER `added`;

-- 2.2 添加 activated_device_id 字段（激活后的设备ID）
ALTER TABLE `iot_discovered_device` 
ADD COLUMN `activated_device_id` BIGINT(20) NULL DEFAULT NULL COMMENT '激活后的设备ID（关联 iot_device.id）' AFTER `activated`;

-- 2.3 添加 activated_time 字段（激活时间）
ALTER TABLE `iot_discovered_device` 
ADD COLUMN `activated_time` DATETIME NULL DEFAULT NULL COMMENT '激活时间' AFTER `activated_device_id`;

-- 2.4 添加 activated_by 字段（激活操作人ID）
ALTER TABLE `iot_discovered_device` 
ADD COLUMN `activated_by` BIGINT(20) NULL DEFAULT NULL COMMENT '激活操作人ID' AFTER `activated_time`;

-- 3. 添加相关索引
CREATE INDEX `idx_activated` ON `iot_discovered_device`(`activated`) COMMENT '激活状态索引';
CREATE INDEX `idx_activated_device_id` ON `iot_discovered_device`(`activated_device_id`) COMMENT '激活设备ID索引';
CREATE INDEX `idx_activated_by` ON `iot_discovered_device`(`activated_by`) COMMENT '激活操作人索引';

-- 4. 更新现有数据
-- 根据 device_id 字段判断是否已激活（如果有关联的设备ID，说明已激活）
UPDATE `iot_discovered_device` 
SET 
    `activated` = CASE 
        WHEN `device_id` IS NOT NULL THEN b'1'
        ELSE b'0'
    END,
    `activated_device_id` = `device_id`,
    `activated_time` = CASE 
        WHEN `device_id` IS NOT NULL THEN `update_time`
        ELSE NULL
    END;

-- 5. 验证修复结果
SELECT 
    COLUMN_NAME,
    DATA_TYPE,
    CHARACTER_MAXIMUM_LENGTH,
    IS_NULLABLE,
    COLUMN_DEFAULT,
    COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE() 
  AND TABLE_NAME = 'iot_discovered_device' 
  AND COLUMN_NAME IN ('added', 'activated', 'activated_device_id', 'activated_time', 'activated_by')
ORDER BY ORDINAL_POSITION;

-- 6. 检查数据样本
SELECT 
    id, 
    ip, 
    vendor,
    added, 
    activated, 
    activated_device_id,
    activated_time,
    device_id,
    status, 
    discovery_time 
FROM `iot_discovered_device` 
ORDER BY id DESC 
LIMIT 10;

-- 7. 统计数据
SELECT 
    COUNT(*) AS '总记录数',
    SUM(CASE WHEN added = b'1' THEN 1 ELSE 0 END) AS '已添加数',
    SUM(CASE WHEN activated = b'1' THEN 1 ELSE 0 END) AS '已激活数',
    COUNT(DISTINCT ip) AS '唯一IP数'
FROM `iot_discovered_device`;

-- ============================================================================
-- 执行说明：
-- 1. 先执行查询语句检查当前表结构
-- 2. 逐步执行 ALTER TABLE 语句添加缺失字段
-- 3. 执行数据更新语句
-- 4. 验证修复结果
-- 
-- 注意：如果字段已存在，ALTER TABLE 语句会报错，这是正常的
-- ============================================================================
