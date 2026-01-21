-- =====================================================
-- 德通设备数据迁移验证脚本
-- 版本: V1.1
-- 描述: 验证 iot_detong_device 到 iot_device 的数据迁移完整性
-- 作者: IoT Platform Team
-- 日期: 2024-12-11
-- =====================================================

SELECT '========================================' as separator;
SELECT '德通设备数据迁移验证报告' as title;
SELECT DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s') as validation_time;
SELECT '========================================' as separator;

-- =====================================================
-- 验证1: 数据数量一致性
-- =====================================================

SELECT '' as blank_line;
SELECT '【验证1】数据数量一致性检查' as validation_section;
SELECT '----------------------------------------' as separator;

SELECT 
    'iot_detong_device' as source_table,
    COUNT(*) as record_count
FROM iot_detong_device
WHERE deleted = 0;

SELECT 
    'iot_device (device_type=2)' as target_table,
    COUNT(*) as record_count
FROM iot_device
WHERE device_type = 2 AND deleted = 0;

SELECT 
    CASE 
        WHEN (SELECT COUNT(*) FROM iot_detong_device WHERE deleted = 0) = 
             (SELECT COUNT(*) FROM iot_device WHERE device_type = 2 AND deleted = 0)
        THEN '✓ 通过: 数据数量一致'
        ELSE CONCAT('✗ 失败: 数量不一致 (源表:', 
                   (SELECT COUNT(*) FROM iot_detong_device WHERE deleted = 0),
                   ', 目标表:',
                   (SELECT COUNT(*) FROM iot_device WHERE device_type = 2 AND deleted = 0),
                   ')')
    END as validation_result;

-- =====================================================
-- 验证2: 通用字段一致性
-- =====================================================

SELECT '' as blank_line;
SELECT '【验证2】通用字段一致性检查' as validation_section;
SELECT '----------------------------------------' as separator;

SELECT 
    'device_name' as field_name,
    COUNT(*) as total_count,
    SUM(CASE 
        WHEN dd.device_name = d.device_name 
        THEN 1 ELSE 0 
    END) as matched_count,
    CONCAT(ROUND(SUM(CASE 
        WHEN dd.device_name = d.device_name 
        THEN 1 ELSE 0 
    END) * 100.0 / COUNT(*), 2), '%') as match_rate,
    CASE 
        WHEN COUNT(*) = SUM(CASE 
            WHEN dd.device_name = d.device_name 
            THEN 1 ELSE 0 
        END)
        THEN '✓ 通过'
        ELSE '✗ 失败'
    END as result
FROM iot_detong_device dd
JOIN iot_device d ON dd.id = d.id
WHERE dd.deleted = 0 AND d.deleted = 0

UNION ALL

SELECT 
    'serial_number (station_code)' as field_name,
    COUNT(*) as total_count,
    SUM(CASE 
        WHEN dd.station_code = d.serial_number 
        THEN 1 ELSE 0 
    END) as matched_count,
    CONCAT(ROUND(SUM(CASE 
        WHEN dd.station_code = d.serial_number 
        THEN 1 ELSE 0 
    END) * 100.0 / COUNT(*), 2), '%') as match_rate,
    CASE 
        WHEN COUNT(*) = SUM(CASE 
            WHEN dd.station_code = d.serial_number 
            THEN 1 ELSE 0 
        END)
        THEN '✓ 通过'
        ELSE '✗ 失败'
    END as result
FROM iot_detong_device dd
JOIN iot_device d ON dd.id = d.id
WHERE dd.deleted = 0 AND d.deleted = 0

UNION ALL

SELECT 
    'tenant_id' as field_name,
    COUNT(*) as total_count,
    SUM(CASE 
        WHEN dd.tenant_id = d.tenant_id 
        THEN 1 ELSE 0 
    END) as matched_count,
    CONCAT(ROUND(SUM(CASE 
        WHEN dd.tenant_id = d.tenant_id 
        THEN 1 ELSE 0 
    END) * 100.0 / COUNT(*), 2), '%') as match_rate,
    CASE 
        WHEN COUNT(*) = SUM(CASE 
            WHEN dd.tenant_id = d.tenant_id 
            THEN 1 ELSE 0 
        END)
        THEN '✓ 通过'
        ELSE '✗ 失败'
    END as result
FROM iot_detong_device dd
JOIN iot_device d ON dd.id = d.id
WHERE dd.deleted = 0 AND d.deleted = 0

UNION ALL

SELECT 
    'state (status)' as field_name,
    COUNT(*) as total_count,
    SUM(CASE 
        WHEN (dd.status = 1 AND d.state = 1) OR (dd.status = 0 AND d.state = 0)
        THEN 1 ELSE 0 
    END) as matched_count,
    CONCAT(ROUND(SUM(CASE 
        WHEN (dd.status = 1 AND d.state = 1) OR (dd.status = 0 AND d.state = 0)
        THEN 1 ELSE 0 
    END) * 100.0 / COUNT(*), 2), '%') as match_rate,
    CASE 
        WHEN COUNT(*) = SUM(CASE 
            WHEN (dd.status = 1 AND d.state = 1) OR (dd.status = 0 AND d.state = 0)
            THEN 1 ELSE 0 
        END)
        THEN '✓ 通过'
        ELSE '✗ 失败'
    END as result
FROM iot_detong_device dd
JOIN iot_device d ON dd.id = d.id
WHERE dd.deleted = 0 AND d.deleted = 0;

-- =====================================================
-- 验证3: JSON配置字段完整性
-- =====================================================

SELECT '' as blank_line;
SELECT '【验证3】JSON配置字段完整性检查' as validation_section;
SELECT '----------------------------------------' as separator;

SELECT 
    'stationCode' as config_field,
    COUNT(*) as total_count,
    SUM(CASE 
        WHEN dd.station_code = JSON_UNQUOTE(JSON_EXTRACT(d.config, '$.stationCode'))
        OR (dd.station_code IS NULL AND JSON_UNQUOTE(JSON_EXTRACT(d.config, '$.stationCode')) = '')
        THEN 1 ELSE 0 
    END) as matched_count,
    CONCAT(ROUND(SUM(CASE 
        WHEN dd.station_code = JSON_UNQUOTE(JSON_EXTRACT(d.config, '$.stationCode'))
        OR (dd.station_code IS NULL AND JSON_UNQUOTE(JSON_EXTRACT(d.config, '$.stationCode')) = '')
        THEN 1 ELSE 0 
    END) * 100.0 / COUNT(*), 2), '%') as match_rate,
    CASE 
        WHEN COUNT(*) = SUM(CASE 
            WHEN dd.station_code = JSON_UNQUOTE(JSON_EXTRACT(d.config, '$.stationCode'))
            OR (dd.station_code IS NULL AND JSON_UNQUOTE(JSON_EXTRACT(d.config, '$.stationCode')) = '')
            THEN 1 ELSE 0 
        END)
        THEN '✓ 通过'
        ELSE '✗ 失败'
    END as result
FROM iot_detong_device dd
JOIN iot_device d ON dd.id = d.id
WHERE dd.deleted = 0 AND d.deleted = 0

UNION ALL

SELECT 
    'deviceTypeCode' as config_field,
    COUNT(*) as total_count,
    SUM(CASE 
        WHEN dd.device_type = JSON_EXTRACT(d.config, '$.deviceTypeCode')
        OR (dd.device_type IS NULL AND JSON_EXTRACT(d.config, '$.deviceTypeCode') = 0)
        THEN 1 ELSE 0 
    END) as matched_count,
    CONCAT(ROUND(SUM(CASE 
        WHEN dd.device_type = JSON_EXTRACT(d.config, '$.deviceTypeCode')
        OR (dd.device_type IS NULL AND JSON_EXTRACT(d.config, '$.deviceTypeCode') = 0)
        THEN 1 ELSE 0 
    END) * 100.0 / COUNT(*), 2), '%') as match_rate,
    CASE 
        WHEN COUNT(*) = SUM(CASE 
            WHEN dd.device_type = JSON_EXTRACT(d.config, '$.deviceTypeCode')
            OR (dd.device_type IS NULL AND JSON_EXTRACT(d.config, '$.deviceTypeCode') = 0)
            THEN 1 ELSE 0 
        END)
        THEN '✓ 通过'
        ELSE '✗ 失败'
    END as result
FROM iot_detong_device dd
JOIN iot_device d ON dd.id = d.id
WHERE dd.deleted = 0 AND d.deleted = 0

UNION ALL

SELECT 
    'provinceCode' as config_field,
    COUNT(*) as total_count,
    SUM(CASE 
        WHEN dd.province_code = JSON_UNQUOTE(JSON_EXTRACT(d.config, '$.provinceCode'))
        OR (dd.province_code IS NULL AND JSON_UNQUOTE(JSON_EXTRACT(d.config, '$.provinceCode')) = '')
        THEN 1 ELSE 0 
    END) as matched_count,
    CONCAT(ROUND(SUM(CASE 
        WHEN dd.province_code = JSON_UNQUOTE(JSON_EXTRACT(d.config, '$.provinceCode'))
        OR (dd.province_code IS NULL AND JSON_UNQUOTE(JSON_EXTRACT(d.config, '$.provinceCode')) = '')
        THEN 1 ELSE 0 
    END) * 100.0 / COUNT(*), 2), '%') as match_rate,
    CASE 
        WHEN COUNT(*) = SUM(CASE 
            WHEN dd.province_code = JSON_UNQUOTE(JSON_EXTRACT(d.config, '$.provinceCode'))
            OR (dd.province_code IS NULL AND JSON_UNQUOTE(JSON_EXTRACT(d.config, '$.provinceCode')) = '')
            THEN 1 ELSE 0 
        END)
        THEN '✓ 通过'
        ELSE '✗ 失败'
    END as result
FROM iot_detong_device dd
JOIN iot_device d ON dd.id = d.id
WHERE dd.deleted = 0 AND d.deleted = 0

UNION ALL

SELECT 
    'teaKey' as config_field,
    COUNT(*) as total_count,
    SUM(CASE 
        WHEN dd.tea_key = JSON_UNQUOTE(JSON_EXTRACT(d.config, '$.teaKey'))
        OR (dd.tea_key IS NULL AND JSON_UNQUOTE(JSON_EXTRACT(d.config, '$.teaKey')) = '')
        THEN 1 ELSE 0 
    END) as matched_count,
    CONCAT(ROUND(SUM(CASE 
        WHEN dd.tea_key = JSON_UNQUOTE(JSON_EXTRACT(d.config, '$.teaKey'))
        OR (dd.tea_key IS NULL AND JSON_UNQUOTE(JSON_EXTRACT(d.config, '$.teaKey')) = '')
        THEN 1 ELSE 0 
    END) * 100.0 / COUNT(*), 2), '%') as match_rate,
    CASE 
        WHEN COUNT(*) = SUM(CASE 
            WHEN dd.tea_key = JSON_UNQUOTE(JSON_EXTRACT(d.config, '$.teaKey'))
            OR (dd.tea_key IS NULL AND JSON_UNQUOTE(JSON_EXTRACT(d.config, '$.teaKey')) = '')
            THEN 1 ELSE 0 
        END)
        THEN '✓ 通过'
        ELSE '✗ 失败'
    END as result
FROM iot_detong_device dd
JOIN iot_device d ON dd.id = d.id
WHERE dd.deleted = 0 AND d.deleted = 0

UNION ALL

SELECT 
    'manufacturer' as config_field,
    COUNT(*) as total_count,
    SUM(CASE 
        WHEN dd.manufacturer = JSON_UNQUOTE(JSON_EXTRACT(d.config, '$.manufacturer'))
        OR (dd.manufacturer IS NULL AND JSON_UNQUOTE(JSON_EXTRACT(d.config, '$.manufacturer')) = '')
        THEN 1 ELSE 0 
    END) as matched_count,
    CONCAT(ROUND(SUM(CASE 
        WHEN dd.manufacturer = JSON_UNQUOTE(JSON_EXTRACT(d.config, '$.manufacturer'))
        OR (dd.manufacturer IS NULL AND JSON_UNQUOTE(JSON_EXTRACT(d.config, '$.manufacturer')) = '')
        THEN 1 ELSE 0 
    END) * 100.0 / COUNT(*), 2), '%') as match_rate,
    CASE 
        WHEN COUNT(*) = SUM(CASE 
            WHEN dd.manufacturer = JSON_UNQUOTE(JSON_EXTRACT(d.config, '$.manufacturer'))
            OR (dd.manufacturer IS NULL AND JSON_UNQUOTE(JSON_EXTRACT(d.config, '$.manufacturer')) = '')
            THEN 1 ELSE 0 
        END)
        THEN '✓ 通过'
        ELSE '✗ 失败'
    END as result
FROM iot_detong_device dd
JOIN iot_device d ON dd.id = d.id
WHERE dd.deleted = 0 AND d.deleted = 0;

-- =====================================================
-- 验证4: JSON格式有效性
-- =====================================================

SELECT '' as blank_line;
SELECT '【验证4】JSON格式有效性检查' as validation_section;
SELECT '----------------------------------------' as separator;

SELECT 
    'JSON格式验证' as validation_type,
    COUNT(*) as total_count,
    SUM(CASE 
        WHEN JSON_VALID(d.config) = 1 
        THEN 1 ELSE 0 
    END) as valid_count,
    SUM(CASE 
        WHEN JSON_VALID(d.config) = 0 OR d.config IS NULL
        THEN 1 ELSE 0 
    END) as invalid_count,
    CASE 
        WHEN COUNT(*) = SUM(CASE 
            WHEN JSON_VALID(d.config) = 1 
            THEN 1 ELSE 0 
        END)
        THEN '✓ 通过: 所有config字段都是有效的JSON'
        ELSE CONCAT('✗ 失败: 发现 ', 
                   SUM(CASE WHEN JSON_VALID(d.config) = 0 OR d.config IS NULL THEN 1 ELSE 0 END),
                   ' 条无效JSON记录')
    END as result
FROM iot_device d
WHERE d.device_type = 2 AND d.deleted = 0;

-- 检查deviceType字段
SELECT 
    'deviceType字段验证' as validation_type,
    COUNT(*) as total_count,
    SUM(CASE 
        WHEN JSON_UNQUOTE(JSON_EXTRACT(d.config, '$.deviceType')) = 'DETONG'
        THEN 1 ELSE 0 
    END) as correct_count,
    CASE 
        WHEN COUNT(*) = SUM(CASE 
            WHEN JSON_UNQUOTE(JSON_EXTRACT(d.config, '$.deviceType')) = 'DETONG'
            THEN 1 ELSE 0 
        END)
        THEN '✓ 通过: 所有记录的deviceType都是DETONG'
        ELSE '✗ 失败: 存在deviceType不正确的记录'
    END as result
FROM iot_device d
WHERE d.device_type = 2 AND d.deleted = 0;

-- =====================================================
-- 验证5: 不一致记录详情
-- =====================================================

SELECT '' as blank_line;
SELECT '【验证5】不一致记录详情' as validation_section;
SELECT '----------------------------------------' as separator;

-- 查找device_name不一致的记录
SELECT 
    'device_name不一致' as issue_type,
    dd.id,
    dd.device_name as source_value,
    d.device_name as target_value
FROM iot_detong_device dd
JOIN iot_device d ON dd.id = d.id
WHERE dd.deleted = 0 AND d.deleted = 0
AND dd.device_name != d.device_name
LIMIT 10;

-- 查找station_code不一致的记录
SELECT 
    'station_code不一致' as issue_type,
    dd.id,
    dd.station_code as source_value,
    d.serial_number as target_value
FROM iot_detong_device dd
JOIN iot_device d ON dd.id = d.id
WHERE dd.deleted = 0 AND d.deleted = 0
AND dd.station_code != d.serial_number
LIMIT 10;

-- 查找JSON配置字段不一致的记录
SELECT 
    'JSON配置不一致' as issue_type,
    dd.id,
    dd.station_code as source_station_code,
    JSON_UNQUOTE(JSON_EXTRACT(d.config, '$.stationCode')) as target_station_code,
    dd.device_type as source_device_type,
    JSON_EXTRACT(d.config, '$.deviceTypeCode') as target_device_type
FROM iot_detong_device dd
JOIN iot_device d ON dd.id = d.id
WHERE dd.deleted = 0 AND d.deleted = 0
AND (
    dd.station_code != JSON_UNQUOTE(JSON_EXTRACT(d.config, '$.stationCode'))
    OR dd.device_type != JSON_EXTRACT(d.config, '$.deviceTypeCode')
)
LIMIT 10;

-- =====================================================
-- 验证总结
-- =====================================================

SELECT '' as blank_line;
SELECT '========================================' as separator;
SELECT '验证报告总结' as summary_section;
SELECT '========================================' as separator;

SELECT 
    CASE 
        WHEN (
            -- 数量一致性
            (SELECT COUNT(*) FROM iot_detong_device WHERE deleted = 0) = 
            (SELECT COUNT(*) FROM iot_device WHERE device_type = 2 AND deleted = 0)
            AND
            -- 通用字段一致性
            (SELECT COUNT(*) FROM iot_detong_device dd
             JOIN iot_device d ON dd.id = d.id
             WHERE dd.deleted = 0 AND d.deleted = 0
             AND dd.device_name = d.device_name
             AND dd.station_code = d.serial_number
             AND dd.tenant_id = d.tenant_id) = 
            (SELECT COUNT(*) FROM iot_detong_device WHERE deleted = 0)
            AND
            -- JSON格式有效性
            (SELECT COUNT(*) FROM iot_device d
             WHERE d.device_type = 2 AND d.deleted = 0
             AND JSON_VALID(d.config) = 1) = 
            (SELECT COUNT(*) FROM iot_device WHERE device_type = 2 AND deleted = 0)
        )
        THEN '✓✓✓ 所有验证通过! 数据迁移成功! ✓✓✓'
        ELSE '✗✗✗ 存在验证失败项,请检查上述详细报告 ✗✗✗'
    END as final_result;

SELECT 
    '建议操作' as recommendation,
    CASE 
        WHEN (
            (SELECT COUNT(*) FROM iot_detong_device WHERE deleted = 0) = 
            (SELECT COUNT(*) FROM iot_device WHERE device_type = 2 AND deleted = 0)
        )
        THEN '验证通过,可以提交事务: COMMIT;'
        ELSE '验证失败,建议回滚事务: ROLLBACK; 并检查迁移脚本'
    END as action;

SELECT '========================================' as separator;
SELECT '验证完成时间:' as label, DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s') as completion_time;
SELECT '========================================' as separator;
