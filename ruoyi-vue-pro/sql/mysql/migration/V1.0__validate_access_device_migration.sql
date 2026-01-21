-- =====================================================
-- IoT设备表结构重构 - 门禁设备迁移验证脚本
-- 功能: 验证门禁设备数据迁移的完整性和正确性
-- 版本: V1.0
-- 日期: 2024-12-11
-- =====================================================

-- =====================================================
-- 验证1: 数据数量一致性检查
-- =====================================================

SELECT '========== 数据数量验证 ==========' as section;

SELECT 
    'iot_access_device' as source_table,
    COUNT(*) as total_count,
    SUM(CASE WHEN deleted = 0 THEN 1 ELSE 0 END) as active_count,
    SUM(CASE WHEN deleted = 1 THEN 1 ELSE 0 END) as deleted_count
FROM iot_access_device

UNION ALL

SELECT 
    'iot_device (门禁)' as source_table,
    COUNT(*) as total_count,
    SUM(CASE WHEN deleted = 0 THEN 1 ELSE 0 END) as active_count,
    SUM(CASE WHEN deleted = 1 THEN 1 ELSE 0 END) as deleted_count
FROM iot_device
WHERE device_type = 1;

-- 数量差异检查
SELECT 
    '数量差异检查' as check_type,
    (SELECT COUNT(*) FROM iot_access_device WHERE deleted = 0) as source_count,
    (SELECT COUNT(*) FROM iot_device WHERE device_type = 1 AND deleted = 0) as target_count,
    (SELECT COUNT(*) FROM iot_access_device WHERE deleted = 0) - 
    (SELECT COUNT(*) FROM iot_device WHERE device_type = 1 AND deleted = 0) as difference,
    CASE 
        WHEN (SELECT COUNT(*) FROM iot_access_device WHERE deleted = 0) = 
             (SELECT COUNT(*) FROM iot_device WHERE device_type = 1 AND deleted = 0)
        THEN '✓ 通过'
        ELSE '✗ 失败'
    END as result;

-- =====================================================
-- 验证2: 通用字段值一致性检查
-- =====================================================

SELECT '========== 通用字段验证 ==========' as section;

-- 检查设备名称一致性
SELECT 
    '设备名称一致性' as check_type,
    COUNT(*) as total_records,
    SUM(CASE WHEN ad.device_name = d.device_name THEN 1 ELSE 0 END) as matched_count,
    SUM(CASE WHEN ad.device_name != d.device_name THEN 1 ELSE 0 END) as mismatched_count,
    CASE 
        WHEN SUM(CASE WHEN ad.device_name != d.device_name THEN 1 ELSE 0 END) = 0
        THEN '✓ 通过'
        ELSE '✗ 失败'
    END as result
FROM iot_access_device ad
JOIN iot_device d ON ad.id = d.id
WHERE ad.deleted = 0;

-- 检查产品ID一致性
SELECT 
    '产品ID一致性' as check_type,
    COUNT(*) as total_records,
    SUM(CASE WHEN ad.product_id = d.product_id THEN 1 ELSE 0 END) as matched_count,
    SUM(CASE WHEN ad.product_id != d.product_id THEN 1 ELSE 0 END) as mismatched_count,
    CASE 
        WHEN SUM(CASE WHEN ad.product_id != d.product_id THEN 1 ELSE 0 END) = 0
        THEN '✓ 通过'
        ELSE '✗ 失败'
    END as result
FROM iot_access_device ad
JOIN iot_device d ON ad.id = d.id
WHERE ad.deleted = 0;

-- 检查租户ID一致性
SELECT 
    '租户ID一致性' as check_type,
    COUNT(*) as total_records,
    SUM(CASE WHEN ad.tenant_id = d.tenant_id THEN 1 ELSE 0 END) as matched_count,
    SUM(CASE WHEN ad.tenant_id != d.tenant_id THEN 1 ELSE 0 END) as mismatched_count,
    CASE 
        WHEN SUM(CASE WHEN ad.tenant_id != d.tenant_id THEN 1 ELSE 0 END) = 0
        THEN '✓ 通过'
        ELSE '✗ 失败'
    END as result
FROM iot_access_device ad
JOIN iot_device d ON ad.id = d.id
WHERE ad.deleted = 0;

-- =====================================================
-- 验证3: JSON配置字段完整性检查
-- =====================================================

SELECT '========== JSON配置字段验证 ==========' as section;

-- 检查IP地址字段
SELECT 
    'IP地址字段' as check_type,
    COUNT(*) as total_records,
    SUM(CASE 
        WHEN ad.ip_address = JSON_UNQUOTE(JSON_EXTRACT(d.config, '$.ipAddress'))
        OR (ad.ip_address IS NULL AND JSON_UNQUOTE(JSON_EXTRACT(d.config, '$.ipAddress')) = '')
        THEN 1 ELSE 0 
    END) as matched_count,
    SUM(CASE 
        WHEN ad.ip_address != JSON_UNQUOTE(JSON_EXTRACT(d.config, '$.ipAddress'))
        AND NOT (ad.ip_address IS NULL AND JSON_UNQUOTE(JSON_EXTRACT(d.config, '$.ipAddress')) = '')
        THEN 1 ELSE 0 
    END) as mismatched_count,
    CASE 
        WHEN SUM(CASE 
            WHEN ad.ip_address != JSON_UNQUOTE(JSON_EXTRACT(d.config, '$.ipAddress'))
            AND NOT (ad.ip_address IS NULL AND JSON_UNQUOTE(JSON_EXTRACT(d.config, '$.ipAddress')) = '')
            THEN 1 ELSE 0 
        END) = 0
        THEN '✓ 通过'
        ELSE '✗ 失败'
    END as result
FROM iot_access_device ad
JOIN iot_device d ON ad.id = d.id
WHERE ad.deleted = 0 AND d.device_type = 1;

-- 检查端口字段
SELECT 
    '端口字段' as check_type,
    COUNT(*) as total_records,
    SUM(CASE 
        WHEN ad.port = JSON_EXTRACT(d.config, '$.port')
        OR (ad.port IS NULL AND JSON_EXTRACT(d.config, '$.port') = 37777)
        THEN 1 ELSE 0 
    END) as matched_count,
    SUM(CASE 
        WHEN ad.port != JSON_EXTRACT(d.config, '$.port')
        AND NOT (ad.port IS NULL AND JSON_EXTRACT(d.config, '$.port') = 37777)
        THEN 1 ELSE 0 
    END) as mismatched_count,
    CASE 
        WHEN SUM(CASE 
            WHEN ad.port != JSON_EXTRACT(d.config, '$.port')
            AND NOT (ad.port IS NULL AND JSON_EXTRACT(d.config, '$.port') = 37777)
            THEN 1 ELSE 0 
        END) = 0
        THEN '✓ 通过'
        ELSE '✗ 失败'
    END as result
FROM iot_access_device ad
JOIN iot_device d ON ad.id = d.id
WHERE ad.deleted = 0 AND d.device_type = 1;

-- 检查用户名字段
SELECT 
    '用户名字段' as check_type,
    COUNT(*) as total_records,
    SUM(CASE 
        WHEN ad.username = JSON_UNQUOTE(JSON_EXTRACT(d.config, '$.username'))
        OR (ad.username IS NULL AND JSON_UNQUOTE(JSON_EXTRACT(d.config, '$.username')) = '')
        THEN 1 ELSE 0 
    END) as matched_count,
    SUM(CASE 
        WHEN ad.username != JSON_UNQUOTE(JSON_EXTRACT(d.config, '$.username'))
        AND NOT (ad.username IS NULL AND JSON_UNQUOTE(JSON_EXTRACT(d.config, '$.username')) = '')
        THEN 1 ELSE 0 
    END) as mismatched_count,
    CASE 
        WHEN SUM(CASE 
            WHEN ad.username != JSON_UNQUOTE(JSON_EXTRACT(d.config, '$.username'))
            AND NOT (ad.username IS NULL AND JSON_UNQUOTE(JSON_EXTRACT(d.config, '$.username')) = '')
            THEN 1 ELSE 0 
        END) = 0
        THEN '✓ 通过'
        ELSE '✗ 失败'
    END as result
FROM iot_access_device ad
JOIN iot_device d ON ad.id = d.id
WHERE ad.deleted = 0 AND d.device_type = 1;

-- 检查固件版本字段
SELECT 
    '固件版本字段' as check_type,
    COUNT(*) as total_records,
    SUM(CASE 
        WHEN ad.firmware_version = JSON_UNQUOTE(JSON_EXTRACT(d.config, '$.firmwareVersion'))
        OR (ad.firmware_version IS NULL AND JSON_UNQUOTE(JSON_EXTRACT(d.config, '$.firmwareVersion')) = '')
        THEN 1 ELSE 0 
    END) as matched_count,
    SUM(CASE 
        WHEN ad.firmware_version != JSON_UNQUOTE(JSON_EXTRACT(d.config, '$.firmwareVersion'))
        AND NOT (ad.firmware_version IS NULL AND JSON_UNQUOTE(JSON_EXTRACT(d.config, '$.firmwareVersion')) = '')
        THEN 1 ELSE 0 
    END) as mismatched_count,
    CASE 
        WHEN SUM(CASE 
            WHEN ad.firmware_version != JSON_UNQUOTE(JSON_EXTRACT(d.config, '$.firmwareVersion'))
            AND NOT (ad.firmware_version IS NULL AND JSON_UNQUOTE(JSON_EXTRACT(d.config, '$.firmwareVersion')) = '')
            THEN 1 ELSE 0 
        END) = 0
        THEN '✓ 通过'
        ELSE '✗ 失败'
    END as result
FROM iot_access_device ad
JOIN iot_device d ON ad.id = d.id
WHERE ad.deleted = 0 AND d.device_type = 1;

-- =====================================================
-- 验证4: JSON格式有效性检查
-- =====================================================

SELECT '========== JSON格式验证 ==========' as section;

-- 检查config字段是否为有效JSON
SELECT 
    'JSON格式有效性' as check_type,
    COUNT(*) as total_records,
    SUM(CASE WHEN JSON_VALID(d.config) THEN 1 ELSE 0 END) as valid_count,
    SUM(CASE WHEN NOT JSON_VALID(d.config) THEN 1 ELSE 0 END) as invalid_count,
    CASE 
        WHEN SUM(CASE WHEN NOT JSON_VALID(d.config) THEN 1 ELSE 0 END) = 0
        THEN '✓ 通过'
        ELSE '✗ 失败'
    END as result
FROM iot_device d
WHERE d.device_type = 1 AND d.deleted = 0;

-- 检查deviceType字段是否正确
SELECT 
    'deviceType字段' as check_type,
    COUNT(*) as total_records,
    SUM(CASE WHEN JSON_UNQUOTE(JSON_EXTRACT(d.config, '$.deviceType')) = 'ACCESS' THEN 1 ELSE 0 END) as correct_count,
    SUM(CASE WHEN JSON_UNQUOTE(JSON_EXTRACT(d.config, '$.deviceType')) != 'ACCESS' THEN 1 ELSE 0 END) as incorrect_count,
    CASE 
        WHEN SUM(CASE WHEN JSON_UNQUOTE(JSON_EXTRACT(d.config, '$.deviceType')) != 'ACCESS' THEN 1 ELSE 0 END) = 0
        THEN '✓ 通过'
        ELSE '✗ 失败'
    END as result
FROM iot_device d
WHERE d.device_type = 1 AND d.deleted = 0;

-- =====================================================
-- 验证5: 详细不一致记录查询
-- =====================================================

SELECT '========== 不一致记录详情 ==========' as section;

-- 查询字段不一致的记录(如果有)
SELECT 
    ad.id,
    ad.device_name,
    'IP地址不一致' as issue_type,
    ad.ip_address as source_value,
    JSON_UNQUOTE(JSON_EXTRACT(d.config, '$.ipAddress')) as target_value
FROM iot_access_device ad
JOIN iot_device d ON ad.id = d.id
WHERE ad.deleted = 0 
  AND d.device_type = 1
  AND ad.ip_address != JSON_UNQUOTE(JSON_EXTRACT(d.config, '$.ipAddress'))
  AND NOT (ad.ip_address IS NULL AND JSON_UNQUOTE(JSON_EXTRACT(d.config, '$.ipAddress')) = '')

UNION ALL

SELECT 
    ad.id,
    ad.device_name,
    '端口不一致' as issue_type,
    CAST(ad.port AS CHAR) as source_value,
    CAST(JSON_EXTRACT(d.config, '$.port') AS CHAR) as target_value
FROM iot_access_device ad
JOIN iot_device d ON ad.id = d.id
WHERE ad.deleted = 0 
  AND d.device_type = 1
  AND ad.port != JSON_EXTRACT(d.config, '$.port')
  AND NOT (ad.port IS NULL AND JSON_EXTRACT(d.config, '$.port') = 37777)

LIMIT 20;

-- =====================================================
-- 验证总结
-- =====================================================

SELECT '========== 验证总结 ==========' as section;

SELECT 
    '门禁设备迁移验证' as validation_type,
    NOW() as validation_time,
    CASE 
        WHEN (SELECT COUNT(*) FROM iot_access_device WHERE deleted = 0) = 
             (SELECT COUNT(*) FROM iot_device WHERE device_type = 1 AND deleted = 0)
        THEN '✓ 数据迁移完整'
        ELSE '✗ 数据迁移不完整'
    END as data_completeness,
    '请检查上述验证结果' as recommendation;
