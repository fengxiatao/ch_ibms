-- =====================================================
-- 修复 iot_access_operation_log 表添加 channel_name 字段
-- 用于显示操作对象的门通道名称
-- =====================================================

-- 1. 添加 channel_name 字段（如果不存在）
SET @exist := (SELECT COUNT(*) FROM information_schema.COLUMNS 
               WHERE TABLE_SCHEMA = DATABASE() 
               AND TABLE_NAME = 'iot_access_operation_log' 
               AND COLUMN_NAME = 'channel_name');
SET @sql := IF(@exist = 0, 
    'ALTER TABLE `iot_access_operation_log` ADD COLUMN `channel_name` varchar(128) DEFAULT NULL COMMENT ''通道名称（门通道名称）'' AFTER `channel_id`',
    'SELECT ''Column channel_name already exists''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2. 更新已有记录的 channel_name（从 iot_device_channel 表关联查询）
UPDATE `iot_access_operation_log` ol
LEFT JOIN `iot_device_channel` dc ON ol.channel_id = dc.id
SET ol.channel_name = dc.channel_name
WHERE ol.channel_id IS NOT NULL 
  AND ol.channel_name IS NULL
  AND dc.channel_name IS NOT NULL;

-- 3. 验证修复结果
SELECT 
    COUNT(*) AS '总记录数',
    SUM(CASE WHEN channel_name IS NOT NULL THEN 1 ELSE 0 END) AS '有通道名称的记录数',
    SUM(CASE WHEN channel_id IS NOT NULL AND channel_name IS NULL THEN 1 ELSE 0 END) AS '有通道ID但无名称的记录数'
FROM `iot_access_operation_log`;

-- 4. 显示表结构确认
SHOW COLUMNS FROM `iot_access_operation_log` LIKE 'channel%';
