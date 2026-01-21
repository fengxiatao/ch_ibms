-- =====================================================
-- IP地址列删除脚本
-- 功能: 删除 iot_device 表的 ip_address 列
-- 版本: V3.1
-- 日期: 2024-12-18
-- 需求: Requirements 1.4
-- =====================================================

-- 说明:
-- 1. 本脚本在 V3.0 迁移脚本执行成功后执行
-- 2. 删除前会进行最终验证，确保所有数据已迁移到 config
-- 3. 删除操作不可逆，请确保已备份数据

-- =====================================================
-- 第一步: 删除前最终验证
-- =====================================================

-- 检查是否还有未迁移的数据
SELECT 
    '未迁移数据检查' as description,
    COUNT(*) as unmigrated_count,
    CASE 
        WHEN COUNT(*) = 0 THEN '✓ 可以安全删除列'
        ELSE '✗ 存在未迁移数据，请先执行 V3.0 迁移脚本'
    END as status
FROM iot_device
WHERE ip_address IS NOT NULL 
  AND ip_address != ''
  AND (config IS NULL 
       OR JSON_EXTRACT(config, '$.ipAddress') IS NULL
       OR JSON_UNQUOTE(JSON_EXTRACT(config, '$.ipAddress')) = '')
  AND deleted = 0;

-- 检查备份表是否存在
SELECT 
    '备份表检查' as description,
    CASE 
        WHEN COUNT(*) > 0 THEN '✓ 备份表存在'
        ELSE '⚠ 备份表不存在或为空，建议先执行 V3.0 脚本创建备份'
    END as status
FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'iot_device_ip_backup';

-- 显示将要删除的列信息
SELECT 
    COLUMN_NAME,
    DATA_TYPE,
    COLUMN_TYPE,
    IS_NULLABLE,
    COLUMN_DEFAULT,
    COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'iot_device'
  AND COLUMN_NAME = 'ip_address';

-- =====================================================
-- 第二步: 检查是否有依赖该列的索引
-- =====================================================

SELECT 
    INDEX_NAME,
    COLUMN_NAME,
    SEQ_IN_INDEX
FROM INFORMATION_SCHEMA.STATISTICS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'iot_device'
  AND COLUMN_NAME = 'ip_address';

-- =====================================================
-- 第三步: 删除 ip_address 列
-- =====================================================

-- 注意: 以下语句会永久删除列，请确保已完成验证和备份

-- 如果存在索引，先删除索引（根据上面查询结果调整）
-- ALTER TABLE iot_device DROP INDEX idx_ip_address;

-- 删除 ip_address 列
ALTER TABLE iot_device DROP COLUMN ip_address;

-- =====================================================
-- 第四步: 删除后验证
-- =====================================================

-- 确认列已删除
SELECT 
    '列删除验证' as description,
    CASE 
        WHEN COUNT(*) = 0 THEN '✓ ip_address 列已成功删除'
        ELSE '✗ ip_address 列仍然存在'
    END as status
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'iot_device'
  AND COLUMN_NAME = 'ip_address';

-- 显示当前表结构（验证）
SELECT 
    COLUMN_NAME,
    DATA_TYPE,
    IS_NULLABLE
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'iot_device'
ORDER BY ORDINAL_POSITION;

-- 验证 config 字段中的 IP 数据仍然可访问
SELECT 
    'config中IP数据验证' as description,
    COUNT(*) as devices_with_ip
FROM iot_device
WHERE config IS NOT NULL
  AND JSON_EXTRACT(config, '$.ipAddress') IS NOT NULL
  AND JSON_UNQUOTE(JSON_EXTRACT(config, '$.ipAddress')) != ''
  AND deleted = 0;

-- =====================================================
-- 删除完成提示
-- =====================================================

SELECT 
    'ip_address 列删除完成' as status,
    NOW() as completion_time,
    '所有IP地址数据现在统一存储在 config JSON 字段中' as note,
    '如需回滚，请执行 V3.2__rollback_ip_address_column.sql' as rollback_info;
