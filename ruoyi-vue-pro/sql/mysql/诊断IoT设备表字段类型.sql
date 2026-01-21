-- ================================================
-- IoT 设备表 - 字段类型诊断
-- ================================================
-- 说明：检查所有字段的实际类型，定位问题
-- 日期：2025-10-27
-- ================================================

USE `ruoyi-vue-pro`;

-- 查看完整的表结构
SELECT 
    COLUMN_NAME AS '字段名',
    DATA_TYPE AS '数据类型',
    COLUMN_TYPE AS '完整类型',
    IS_NULLABLE AS '允许NULL',
    COLUMN_DEFAULT AS '默认值',
    COLUMN_COMMENT AS '注释'
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'ruoyi-vue-pro'
  AND TABLE_NAME = 'iot_device'
ORDER BY ORDINAL_POSITION;

-- 特别检查 Boolean 相关字段
SELECT 
    '=== Boolean 字段诊断 ===' AS '诊断项';

SELECT 
    COLUMN_NAME,
    DATA_TYPE,
    COLUMN_TYPE,
    COLUMN_DEFAULT,
    CASE 
        WHEN DATA_TYPE = 'bit' AND COLUMN_TYPE = 'bit(1)' THEN '✅ 正确 (BIT)'
        WHEN DATA_TYPE = 'tinyint' AND COLUMN_TYPE = 'tinyint(1)' THEN '⚠️  TINYINT (可用但不标准)'
        WHEN DATA_TYPE IN ('varchar', 'char', 'text') THEN '❌ 错误 (字符串类型)'
        WHEN DATA_TYPE IN ('int', 'bigint') THEN '❌ 错误 (整数类型过大)'
        ELSE '❓ 未知类型'
    END AS '状态'
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'ruoyi-vue-pro'
  AND TABLE_NAME = 'iot_device'
  AND COLUMN_NAME IN ('subsystem_override', 'menu_override', 'deleted');

-- 检查是否存在问题数据
SELECT 
    '=== 检查现有数据 ===' AS '诊断项';

SELECT 
    id,
    device_name,
    subsystem_override,
    menu_override,
    CASE 
        WHEN subsystem_override IS NULL THEN 'NULL'
        WHEN subsystem_override = 0 THEN '0 (false)'
        WHEN subsystem_override = 1 THEN '1 (true)'
        ELSE CONCAT('异常值: ', subsystem_override)
    END AS 'subsystem_override_状态',
    CASE 
        WHEN menu_override IS NULL THEN 'NULL'
        WHEN menu_override = 0 THEN '0 (false)'
        WHEN menu_override = 1 THEN '1 (true)'
        ELSE CONCAT('异常值: ', menu_override)
    END AS 'menu_override_状态'
FROM iot_device
LIMIT 10;

-- 检查建表语句
SHOW CREATE TABLE iot_device;














