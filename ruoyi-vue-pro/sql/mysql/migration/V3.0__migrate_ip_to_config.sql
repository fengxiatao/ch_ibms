-- =====================================================
-- IP地址字段迁移脚本
-- 功能: 将 iot_device.ip_address 列数据迁移到 config JSON 字段
-- 版本: V3.0
-- 日期: 2024-12-18
-- 需求: Requirements 1.3
-- =====================================================

-- 说明:
-- 1. 本脚本将 ip_address 列的数据合并到 config JSON 字段中
-- 2. 只处理 config 中尚未包含 ipAddress 的记录
-- 3. 使用事务保护，确保数据一致性
-- 4. 迁移完成后，ip_address 列数据将冗余存在，需执行 V3.1 脚本删除列

-- =====================================================
-- 第一步: 迁移前检查
-- =====================================================

-- 检查 ip_address 列是否存在
SELECT 
    COLUMN_NAME, 
    DATA_TYPE, 
    COLUMN_TYPE,
    IS_NULLABLE
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'iot_device'
  AND COLUMN_NAME = 'ip_address';

-- 统计需要迁移的记录数
SELECT 
    '需要迁移的记录数' as description,
    COUNT(*) as count
FROM iot_device
WHERE ip_address IS NOT NULL 
  AND ip_address != ''
  AND deleted = 0;

-- 统计 config 中已有 ipAddress 的记录数
SELECT 
    'config中已有ipAddress的记录数' as description,
    COUNT(*) as count
FROM iot_device
WHERE config IS NOT NULL
  AND JSON_EXTRACT(config, '$.ipAddress') IS NOT NULL
  AND JSON_UNQUOTE(JSON_EXTRACT(config, '$.ipAddress')) != ''
  AND deleted = 0;

-- 统计需要从 ip_address 列迁移到 config 的记录数
SELECT 
    '需要从ip_address迁移到config的记录数' as description,
    COUNT(*) as count
FROM iot_device
WHERE ip_address IS NOT NULL 
  AND ip_address != ''
  AND (config IS NULL 
       OR JSON_EXTRACT(config, '$.ipAddress') IS NULL 
       OR JSON_UNQUOTE(JSON_EXTRACT(config, '$.ipAddress')) = '')
  AND deleted = 0;

-- =====================================================
-- 第二步: 备份数据（建议在生产环境执行前先备份）
-- =====================================================

-- 创建备份表（如果不存在）
CREATE TABLE IF NOT EXISTS iot_device_ip_backup (
    id BIGINT PRIMARY KEY,
    ip_address VARCHAR(50),
    config JSON,
    backup_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 备份将要迁移的数据
INSERT INTO iot_device_ip_backup (id, ip_address, config)
SELECT id, ip_address, config
FROM iot_device
WHERE ip_address IS NOT NULL 
  AND ip_address != ''
  AND deleted = 0
ON DUPLICATE KEY UPDATE 
    ip_address = VALUES(ip_address),
    config = VALUES(config),
    backup_time = CURRENT_TIMESTAMP;

SELECT 
    '已备份记录数' as description,
    COUNT(*) as count
FROM iot_device_ip_backup;

-- =====================================================
-- 第三步: 执行数据迁移
-- =====================================================

-- 开始事务
START TRANSACTION;

-- 场景1: config 为 NULL，需要创建新的 JSON 对象
UPDATE iot_device
SET config = JSON_OBJECT('ipAddress', ip_address)
WHERE ip_address IS NOT NULL 
  AND ip_address != ''
  AND config IS NULL
  AND deleted = 0;

-- 场景2: config 存在但没有 ipAddress 字段，需要添加
UPDATE iot_device
SET config = JSON_SET(config, '$.ipAddress', ip_address)
WHERE ip_address IS NOT NULL 
  AND ip_address != ''
  AND config IS NOT NULL
  AND (JSON_EXTRACT(config, '$.ipAddress') IS NULL 
       OR JSON_UNQUOTE(JSON_EXTRACT(config, '$.ipAddress')) = '')
  AND deleted = 0;

-- 场景3: config 中的 ipAddress 与 ip_address 列不一致，以 ip_address 列为准更新
-- （这种情况理论上不应该发生，但为了数据一致性还是处理一下）
UPDATE iot_device
SET config = JSON_SET(config, '$.ipAddress', ip_address)
WHERE ip_address IS NOT NULL 
  AND ip_address != ''
  AND config IS NOT NULL
  AND JSON_EXTRACT(config, '$.ipAddress') IS NOT NULL
  AND JSON_UNQUOTE(JSON_EXTRACT(config, '$.ipAddress')) != ip_address
  AND deleted = 0;

-- 提交事务
COMMIT;

-- =====================================================
-- 第四步: 迁移后验证
-- =====================================================

-- 验证迁移结果：检查所有有 ip_address 的记录是否都已迁移到 config
SELECT 
    '迁移验证' as description,
    COUNT(*) as total_with_ip,
    SUM(CASE 
        WHEN JSON_UNQUOTE(JSON_EXTRACT(config, '$.ipAddress')) = ip_address 
        THEN 1 ELSE 0 
    END) as migrated_correctly,
    SUM(CASE 
        WHEN JSON_UNQUOTE(JSON_EXTRACT(config, '$.ipAddress')) != ip_address 
             OR JSON_EXTRACT(config, '$.ipAddress') IS NULL
        THEN 1 ELSE 0 
    END) as migration_failed
FROM iot_device
WHERE ip_address IS NOT NULL 
  AND ip_address != ''
  AND deleted = 0;

-- 抽样检查迁移结果（前20条）
SELECT 
    id,
    device_name,
    ip_address as column_ip,
    JSON_UNQUOTE(JSON_EXTRACT(config, '$.ipAddress')) as config_ip,
    CASE 
        WHEN ip_address = JSON_UNQUOTE(JSON_EXTRACT(config, '$.ipAddress'))
        THEN '✓ 一致'
        ELSE '✗ 不一致'
    END as validation_result
FROM iot_device
WHERE ip_address IS NOT NULL 
  AND ip_address != ''
  AND deleted = 0
LIMIT 20;

-- 检查是否有迁移失败的记录
SELECT 
    '迁移失败的记录' as description,
    COUNT(*) as count
FROM iot_device
WHERE ip_address IS NOT NULL 
  AND ip_address != ''
  AND (config IS NULL 
       OR JSON_EXTRACT(config, '$.ipAddress') IS NULL
       OR JSON_UNQUOTE(JSON_EXTRACT(config, '$.ipAddress')) != ip_address)
  AND deleted = 0;

-- =====================================================
-- 迁移完成提示
-- =====================================================

SELECT 
    'IP地址数据迁移完成' as status,
    NOW() as completion_time,
    '请检查验证结果，确认无误后执行 V3.1__drop_ip_address_column.sql 删除列' as next_step;
