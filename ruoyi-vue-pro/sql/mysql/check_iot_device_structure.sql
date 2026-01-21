-- ========================================
-- IoT 设备表结构诊断脚本
-- 用于检查 iot_device 表是否缺少字段
-- ========================================

USE ch_ibms;

-- 显示当前表结构
SELECT '=== 当前 iot_device 表结构 ===' AS info;

SELECT 
    ORDINAL_POSITION AS '序号',
    COLUMN_NAME AS '字段名',
    COLUMN_TYPE AS '数据类型',
    IS_NULLABLE AS '允许空',
    COLUMN_DEFAULT AS '默认值',
    COLUMN_COMMENT AS '注释'
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = 'ch_ibms'
  AND TABLE_NAME = 'iot_device'
ORDER BY ORDINAL_POSITION;

-- 检查必需字段是否存在
SELECT '=== 缺失字段检查 ===' AS info;

SELECT 
    CASE 
        WHEN COUNT(*) > 0 THEN '✅ 存在'
        ELSE '❌ 缺失'
    END AS 'nickname字段'
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = 'ch_ibms'
  AND TABLE_NAME = 'iot_device'
  AND COLUMN_NAME = 'nickname'

UNION ALL

SELECT 
    CASE 
        WHEN COUNT(*) > 0 THEN '✅ 存在'
        ELSE '❌ 缺失'
    END AS 'serial_number字段'
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = 'ch_ibms'
  AND TABLE_NAME = 'iot_device'
  AND COLUMN_NAME = 'serial_number'

UNION ALL

SELECT 
    CASE 
        WHEN COUNT(*) > 0 THEN '✅ 存在'
        ELSE '❌ 缺失'
    END AS 'pic_url字段'
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = 'ch_ibms'
  AND TABLE_NAME = 'iot_device'
  AND COLUMN_NAME = 'pic_url'

UNION ALL

SELECT 
    CASE 
        WHEN COUNT(*) > 0 THEN '✅ 存在'
        ELSE '❌ 缺失'
    END AS 'group_ids字段'
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = 'ch_ibms'
  AND TABLE_NAME = 'iot_device'
  AND COLUMN_NAME = 'group_ids'

UNION ALL

SELECT 
    CASE 
        WHEN COUNT(*) > 0 THEN '✅ 存在'
        ELSE '❌ 缺失'
    END AS 'product_key字段'
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = 'ch_ibms'
  AND TABLE_NAME = 'iot_device'
  AND COLUMN_NAME = 'product_key'

UNION ALL

SELECT 
    CASE 
        WHEN COUNT(*) > 0 THEN '✅ 存在'
        ELSE '❌ 缺失'
    END AS 'device_type字段'
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = 'ch_ibms'
  AND TABLE_NAME = 'iot_device'
  AND COLUMN_NAME = 'device_type'

UNION ALL

SELECT 
    CASE 
        WHEN COUNT(*) > 0 THEN '✅ 存在 (state)'
        WHEN (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = 'ch_ibms' AND TABLE_NAME = 'iot_device' AND COLUMN_NAME = 'status') > 0 
        THEN '⚠️ 使用了旧字段名 status，需要重命名为 state'
        ELSE '❌ 缺失'
    END AS 'state字段'
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = 'ch_ibms'
  AND TABLE_NAME = 'iot_device'
  AND COLUMN_NAME = 'state';

-- 检查索引
SELECT '=== 索引检查 ===' AS info;

SELECT 
    INDEX_NAME AS '索引名',
    NON_UNIQUE AS '非唯一',
    GROUP_CONCAT(COLUMN_NAME ORDER BY SEQ_IN_INDEX) AS '包含字段'
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = 'ch_ibms'
  AND TABLE_NAME = 'iot_device'
GROUP BY INDEX_NAME, NON_UNIQUE;

-- 统计设备数量
SELECT '=== 数据统计 ===' AS info;

SELECT 
    COUNT(*) AS '总设备数',
    COUNT(CASE WHEN deleted = 0 THEN 1 END) AS '未删除设备数',
    COUNT(CASE WHEN deleted = 1 THEN 1 END) AS '已删除设备数'
FROM iot_device;





