-- 修复 iot_alarm_zone 表字段问题
-- 问题：数据库有 is_24h（带下划线），但实体类使用 is24h（驼峰命名）
-- 解决：删除错误的 is_24h 字段，保留正确的 is24h 字段

-- 1. 检查当前字段情况
SELECT COLUMN_NAME, DATA_TYPE, COLUMN_COMMENT
FROM information_schema.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE() 
  AND TABLE_NAME = 'iot_alarm_zone' 
  AND COLUMN_NAME IN ('is24h', 'is_24h');

-- 2. 删除错误的 is_24h 字段（带下划线的）
ALTER TABLE `iot_alarm_zone` DROP COLUMN `is_24h`;

-- 3. 确认 is24h 字段存在，如果不存在则添加
-- （根据截图，is24h 已存在，这一步可能不需要）
-- ALTER TABLE `iot_alarm_zone` 
-- ADD COLUMN `is24h` tinyint(1) DEFAULT 0 COMMENT '是否24小时防区：0-否, 1-是' 
-- AFTER `is_important`;

-- 4. 验证修复结果
SELECT COLUMN_NAME, DATA_TYPE, COLUMN_COMMENT
FROM information_schema.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE() 
  AND TABLE_NAME = 'iot_alarm_zone' 
  AND COLUMN_NAME LIKE '%24h%';
