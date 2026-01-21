-- =====================================================
-- IP地址列回滚脚本
-- 功能: 恢复 iot_device 表的 ip_address 列并从 config 或备份恢复数据
-- 版本: V3.2
-- 日期: 2024-12-18
-- 需求: Requirements 1.3 (回滚支持)
-- =====================================================

-- 说明:
-- 1. 本脚本用于回滚 V3.0 和 V3.1 的迁移操作
-- 2. 优先从备份表恢复数据，如果备份表不存在则从 config JSON 提取
-- 3. 回滚后需要同步更新应用代码以使用 ip_address 列

-- =====================================================
-- 第一步: 检查当前状态
-- =====================================================

-- 检查 ip_address 列是否存在
SELECT 
    '当前状态检查' as description,
    CASE 
        WHEN COUNT(*) > 0 THEN 'ip_address 列已存在，无需回滚'
        ELSE 'ip_address 列不存在，需要回滚'
    END as status
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'iot_device'
  AND COLUMN_NAME = 'ip_address';

-- 检查备份表是否存在
SELECT 
    '备份表检查' as description,
    CASE 
        WHEN COUNT(*) > 0 THEN '✓ 备份表存在，将从备份恢复'
        ELSE '⚠ 备份表不存在，将从 config JSON 提取'
    END as status,
    (SELECT COUNT(*) FROM iot_device_ip_backup) as backup_count
FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'iot_device_ip_backup';

-- =====================================================
-- 第二步: 重新添加 ip_address 列
-- =====================================================

-- 添加 ip_address 列（如果不存在）
-- 注意: 如果列已存在，此语句会报错，可以忽略
ALTER TABLE iot_device 
ADD COLUMN ip_address VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'IP地址' 
AFTER device_type;

-- 验证列已添加
SELECT 
    '列添加验证' as description,
    CASE 
        WHEN COUNT(*) > 0 THEN '✓ ip_address 列已添加'
        ELSE '✗ ip_address 列添加失败'
    END as status
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'iot_device'
  AND COLUMN_NAME = 'ip_address';

-- =====================================================
-- 第三步: 从备份表恢复数据（优先）
-- =====================================================

-- 开始事务
START TRANSACTION;

-- 方案A: 从备份表恢复（如果备份表存在且有数据）
UPDATE iot_device d
INNER JOIN iot_device_ip_backup b ON d.id = b.id
SET d.ip_address = b.ip_address
WHERE b.ip_address IS NOT NULL 
  AND b.ip_address != '';

-- 检查从备份恢复的记录数
SELECT 
    '从备份恢复' as description,
    ROW_COUNT() as restored_count;

-- 方案B: 从 config JSON 提取（补充备份中没有的数据）
UPDATE iot_device
SET ip_address = JSON_UNQUOTE(JSON_EXTRACT(config, '$.ipAddress'))
WHERE ip_address IS NULL
  AND config IS NOT NULL
  AND JSON_EXTRACT(config, '$.ipAddress') IS NOT NULL
  AND JSON_UNQUOTE(JSON_EXTRACT(config, '$.ipAddress')) != ''
  AND deleted = 0;

-- 检查从 config 恢复的记录数
SELECT 
    '从config提取' as description,
    ROW_COUNT() as extracted_count;

-- 提交事务
COMMIT;

-- =====================================================
-- 第四步: 回滚后验证
-- =====================================================

-- 验证数据恢复情况
SELECT 
    '数据恢复验证' as description,
    COUNT(*) as total_devices,
    SUM(CASE WHEN ip_address IS NOT NULL AND ip_address != '' THEN 1 ELSE 0 END) as devices_with_ip,
    SUM(CASE 
        WHEN config IS NOT NULL 
             AND JSON_EXTRACT(config, '$.ipAddress') IS NOT NULL
             AND JSON_UNQUOTE(JSON_EXTRACT(config, '$.ipAddress')) != ''
        THEN 1 ELSE 0 
    END) as devices_with_config_ip
FROM iot_device
WHERE deleted = 0;

-- 验证 ip_address 列与 config 中的 ipAddress 一致性
SELECT 
    '数据一致性验证' as description,
    COUNT(*) as total_with_both,
    SUM(CASE 
        WHEN ip_address = JSON_UNQUOTE(JSON_EXTRACT(config, '$.ipAddress'))
        THEN 1 ELSE 0 
    END) as consistent_count,
    SUM(CASE 
        WHEN ip_address != JSON_UNQUOTE(JSON_EXTRACT(config, '$.ipAddress'))
        THEN 1 ELSE 0 
    END) as inconsistent_count
FROM iot_device
WHERE ip_address IS NOT NULL 
  AND ip_address != ''
  AND config IS NOT NULL
  AND JSON_EXTRACT(config, '$.ipAddress') IS NOT NULL
  AND deleted = 0;

-- 抽样检查恢复结果（前10条）
SELECT 
    id,
    device_name,
    ip_address as restored_ip,
    JSON_UNQUOTE(JSON_EXTRACT(config, '$.ipAddress')) as config_ip,
    CASE 
        WHEN ip_address = JSON_UNQUOTE(JSON_EXTRACT(config, '$.ipAddress'))
        THEN '✓ 一致'
        WHEN ip_address IS NULL AND JSON_EXTRACT(config, '$.ipAddress') IS NULL
        THEN '- 均为空'
        ELSE '✗ 不一致'
    END as validation_result
FROM iot_device
WHERE deleted = 0
LIMIT 10;

-- =====================================================
-- 第五步: 可选 - 重建索引
-- =====================================================

-- 如果需要，可以为 ip_address 列添加索引
-- ALTER TABLE iot_device ADD INDEX idx_ip_address (ip_address);

-- =====================================================
-- 回滚完成提示
-- =====================================================

SELECT 
    'ip_address 列回滚完成' as status,
    NOW() as completion_time,
    '请同步更新应用代码以使用 ip_address 列' as note,
    '建议: 回滚后检查应用是否正常运行' as suggestion;

-- =====================================================
-- 清理备份表（可选，谨慎执行）
-- =====================================================

-- 如果确认回滚成功且不再需要备份，可以删除备份表
-- DROP TABLE IF EXISTS iot_device_ip_backup;
