-- =====================================================
-- 通用设备数据迁移验证脚本
-- 版本: V1.2
-- 描述: 验证所有设备类型的数据迁移完整性
-- 作者: IoT Platform Team
-- 日期: 2024-12-11
-- =====================================================

SELECT '========================================' as separator;
SELECT '设备数据迁移完整性验证报告' as title;
SELECT DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s') as validation_time;
SELECT '========================================' as separator;

-- =====================================================
-- 验证1: 整体数据数量统计
-- =====================================================

SELECT '' as blank_line;
SELECT '【验证1】整体数据数量统计' as validation_section;
SELECT '----------------------------------------' as separator;

-- 统计各设备类型数量
SELECT 
    '统一表设备类型分布' as table_name,
    device_type,
    CASE device_type
        WHEN 1 THEN '门禁设备(ACCESS)'
        WHEN 2 THEN '德通设备(DETONG)'
        ELSE CONCAT('其他类型(', device_type, ')')
    END as device_type_name,
    COUNT(*) as record_count,
    CONCAT(ROUND(COUNT(*) * 100.0 / (SELECT COUNT(*) FROM iot_device WHERE deleted = 0), 2), '%') as percentage
FROM iot_device
WHERE deleted = 0
GROUP BY device_type
ORDER BY device_type;

-- 遗留表数量对比
SELECT 
    '遗留表数量对比' as comparison_type,
    'iot_access_device' as legacy_table,
    (SELECT COUNT(*) FROM iot_access_device WHERE deleted = 0) as legacy_count,
    (SELECT COUNT(*) FROM iot_device WHERE device_type = 1 AND deleted = 0) as unified_count,
    (SELECT COUNT(*) FROM iot_access_device WHERE deleted = 0) - 
    (SELECT COUNT(*) FROM iot_device WHERE device_type = 1 AND deleted = 0) as difference,
    CASE 
        WHEN (SELECT COUNT(*) FROM iot_access_device WHERE deleted = 0) = 
             (SELECT COUNT(*) FROM iot_device WHERE device_type = 1 AND deleted = 0)
        THEN '✓ 一致'
        ELSE '✗ 不一致'
    END as status

UNION ALL

SELECT 
    '遗留表数量对比' as comparison_type,
    'iot_detong_device' as legacy_table,
    (SELECT COUNT(*) FROM iot_detong_device WHERE deleted = 0) as legacy_count,
    (SELECT COUNT(*) FROM iot_device WHERE device_type = 2 AND deleted = 0) as unified_count,
    (SELECT COUNT(*) FROM iot_detong_device WHERE deleted = 0) - 
    (SELECT COUNT(*) FROM iot_device WHERE device_type = 2 AND deleted = 0) as difference,
    CASE 
        WHEN (SELECT COUNT(*) FROM iot_detong_device WHERE deleted = 0) = 
             (SELECT COUNT(*) FROM iot_device WHERE device_type = 2 AND deleted = 0)
        THEN '✓ 一致'
        ELSE '✗ 不一致'
    END as status;

-- =====================================================
-- 验证2: 租户数据隔离验证
-- =====================================================

SELECT '' as blank_line;
SELECT '【验证2】租户数据隔离验证' as validation_section;
SELECT '----------------------------------------' as separator;

-- 按租户统计设备数量
SELECT 
    tenant_id,
    device_type,
    CASE device_type
        WHEN 1 THEN '门禁设备'
        WHEN 2 THEN '德通设备'
        ELSE '其他类型'
    END as device_type_name,
    COUNT(*) as device_count
FROM iot_device
WHERE deleted = 0
GROUP BY tenant_id, device_type
ORDER BY tenant_id, device_type;

-- 验证租户ID一致性
SELECT 
    '租户ID一致性验证' as validation_type,
    COUNT(*) as total_records,
    SUM(CASE 
        WHEN ad.tenant_id = d.tenant_id 
        THEN 1 ELSE 0 
    END) as matched_records,
    CASE 
        WHEN COUNT(*) = SUM(CASE 
            WHEN ad.tenant_id = d.tenant_id 
            THEN 1 ELSE 0 
        END)
        THEN '✓ 通过'
        ELSE '✗ 失败'
    END as result
FROM iot_access_device ad
JOIN iot_device d ON ad.id = d.id
WHERE ad.deleted = 0 AND d.deleted = 0

UNION ALL

SELECT 
    '租户ID一致性验证' as validation_type,
    COUNT(*) as total_records,
    SUM(CASE 
        WHEN dd.tenant_id = d.tenant_id 
        THEN 1 ELSE 0 
    END) as matched_records,
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
WHERE dd.deleted = 0 AND d.deleted = 0;

-- =====================================================
-- 验证3: JSON配置格式验证
-- =====================================================

SELECT '' as blank_line;
SELECT '【验证3】JSON配置格式验证' as validation_section;
SELECT '----------------------------------------' as separator;

-- 验证所有设备的JSON格式
SELECT 
    device_type,
    CASE device_type
        WHEN 1 THEN '门禁设备'
        WHEN 2 THEN '德通设备'
        ELSE '其他类型'
    END as device_type_name,
    COUNT(*) as total_count,
    SUM(CASE WHEN JSON_VALID(config) = 1 THEN 1 ELSE 0 END) as valid_count,
    SUM(CASE WHEN JSON_VALID(config) = 0 OR config IS NULL THEN 1 ELSE 0 END) as invalid_count,
    CONCAT(ROUND(SUM(CASE WHEN JSON_VALID(config) = 1 THEN 1 ELSE 0 END) * 100.0 / COUNT(*), 2), '%') as valid_rate,
    CASE 
        WHEN COUNT(*) = SUM(CASE WHEN JSON_VALID(config) = 1 THEN 1 ELSE 0 END)
        THEN '✓ 通过'
        ELSE '✗ 失败'
    END as result
FROM iot_device
WHERE deleted = 0
GROUP BY device_type
ORDER BY device_type;

-- 验证deviceType字段
SELECT 
    'deviceType字段验证' as validation_type,
    device_type,
    CASE device_type
        WHEN 1 THEN 'ACCESS'
        WHEN 2 THEN 'DETONG'
        ELSE 'UNKNOWN'
    END as expected_device_type,
    COUNT(*) as total_count,
    SUM(CASE 
        WHEN JSON_UNQUOTE(JSON_EXTRACT(config, '$.deviceType')) = 
             CASE device_type
                 WHEN 1 THEN 'ACCESS'
                 WHEN 2 THEN 'DETONG'
                 ELSE 'UNKNOWN'
             END
        THEN 1 ELSE 0 
    END) as matched_count,
    CASE 
        WHEN COUNT(*) = SUM(CASE 
            WHEN JSON_UNQUOTE(JSON_EXTRACT(config, '$.deviceType')) = 
                 CASE device_type
                     WHEN 1 THEN 'ACCESS'
                     WHEN 2 THEN 'DETONG'
                     ELSE 'UNKNOWN'
                 END
            THEN 1 ELSE 0 
        END)
        THEN '✓ 通过'
        ELSE '✗ 失败'
    END as result
FROM iot_device
WHERE deleted = 0
GROUP BY device_type
ORDER BY device_type;

-- =====================================================
-- 验证4: 设备状态一致性
-- =====================================================

SELECT '' as blank_line;
SELECT '【验证4】设备状态一致性验证' as validation_section;
SELECT '----------------------------------------' as separator;

-- 门禁设备状态验证
SELECT 
    '门禁设备状态' as device_category,
    ad.status as source_status,
    d.state as target_state,
    COUNT(*) as record_count,
    CASE 
        WHEN (ad.status = 'ONLINE' AND d.state = 1) OR 
             (ad.status = 'OFFLINE' AND d.state = 0)
        THEN '✓ 匹配'
        ELSE '✗ 不匹配'
    END as status
FROM iot_access_device ad
JOIN iot_device d ON ad.id = d.id
WHERE ad.deleted = 0 AND d.deleted = 0
GROUP BY ad.status, d.state
ORDER BY ad.status, d.state;

-- 德通设备状态验证
SELECT 
    '德通设备状态' as device_category,
    dd.status as source_status,
    d.state as target_state,
    COUNT(*) as record_count,
    CASE 
        WHEN (dd.status = 1 AND d.state = 1) OR 
             (dd.status = 0 AND d.state = 0)
        THEN '✓ 匹配'
        ELSE '✗ 不匹配'
    END as status
FROM iot_detong_device dd
JOIN iot_device d ON dd.id = d.id
WHERE dd.deleted = 0 AND d.deleted = 0
GROUP BY dd.status, d.state
ORDER BY dd.status, d.state;

-- =====================================================
-- 验证5: 审计字段完整性
-- =====================================================

SELECT '' as blank_line;
SELECT '【验证5】审计字段完整性验证' as validation_section;
SELECT '----------------------------------------' as separator;

-- 验证创建时间
SELECT 
    '创建时间验证' as validation_type,
    device_type,
    COUNT(*) as total_count,
    SUM(CASE WHEN create_time IS NOT NULL THEN 1 ELSE 0 END) as has_create_time,
    SUM(CASE WHEN create_time IS NULL THEN 1 ELSE 0 END) as missing_create_time,
    CASE 
        WHEN COUNT(*) = SUM(CASE WHEN create_time IS NOT NULL THEN 1 ELSE 0 END)
        THEN '✓ 通过'
        ELSE '✗ 失败'
    END as result
FROM iot_device
WHERE deleted = 0
GROUP BY device_type
ORDER BY device_type;

-- 验证更新时间
SELECT 
    '更新时间验证' as validation_type,
    device_type,
    COUNT(*) as total_count,
    SUM(CASE WHEN update_time IS NOT NULL THEN 1 ELSE 0 END) as has_update_time,
    SUM(CASE WHEN update_time IS NULL THEN 1 ELSE 0 END) as missing_update_time,
    CASE 
        WHEN COUNT(*) = SUM(CASE WHEN update_time IS NOT NULL THEN 1 ELSE 0 END)
        THEN '✓ 通过'
        ELSE '✗ 失败'
    END as result
FROM iot_device
WHERE deleted = 0
GROUP BY device_type
ORDER BY device_type;

-- =====================================================
-- 验证6: 数据完整性检查
-- =====================================================

SELECT '' as blank_line;
SELECT '【验证6】数据完整性检查' as validation_section;
SELECT '----------------------------------------' as separator;

-- 检查必填字段
SELECT 
    '必填字段检查' as validation_type,
    device_type,
    COUNT(*) as total_count,
    SUM(CASE WHEN device_name IS NULL OR device_name = '' THEN 1 ELSE 0 END) as missing_device_name,
    SUM(CASE WHEN serial_number IS NULL OR serial_number = '' THEN 1 ELSE 0 END) as missing_serial_number,
    SUM(CASE WHEN device_key IS NULL OR device_key = '' THEN 1 ELSE 0 END) as missing_device_key,
    SUM(CASE WHEN config IS NULL THEN 1 ELSE 0 END) as missing_config,
    CASE 
        WHEN SUM(CASE WHEN device_name IS NULL OR device_name = '' THEN 1 ELSE 0 END) = 0
         AND SUM(CASE WHEN serial_number IS NULL OR serial_number = '' THEN 1 ELSE 0 END) = 0
         AND SUM(CASE WHEN device_key IS NULL OR device_key = '' THEN 1 ELSE 0 END) = 0
         AND SUM(CASE WHEN config IS NULL THEN 1 ELSE 0 END) = 0
        THEN '✓ 通过'
        ELSE '✗ 失败'
    END as result
FROM iot_device
WHERE deleted = 0
GROUP BY device_type
ORDER BY device_type;

-- 检查唯一性约束
SELECT 
    '唯一性约束检查' as validation_type,
    'device_key' as field_name,
    COUNT(*) as total_count,
    COUNT(DISTINCT device_key) as unique_count,
    COUNT(*) - COUNT(DISTINCT device_key) as duplicate_count,
    CASE 
        WHEN COUNT(*) = COUNT(DISTINCT device_key)
        THEN '✓ 通过: 无重复'
        ELSE CONCAT('✗ 失败: 发现 ', COUNT(*) - COUNT(DISTINCT device_key), ' 个重复')
    END as result
FROM iot_device
WHERE deleted = 0;

-- =====================================================
-- 验证7: 孤立记录检查
-- =====================================================

SELECT '' as blank_line;
SELECT '【验证7】孤立记录检查' as validation_section;
SELECT '----------------------------------------' as separator;

-- 检查统一表中的孤立记录(在统一表但不在遗留表)
SELECT 
    '孤立记录检查' as validation_type,
    device_type,
    CASE device_type
        WHEN 1 THEN '门禁设备'
        WHEN 2 THEN '德通设备'
        ELSE '其他类型'
    END as device_type_name,
    COUNT(*) as orphan_count,
    CASE 
        WHEN COUNT(*) = 0
        THEN '✓ 通过: 无孤立记录'
        ELSE CONCAT('✗ 警告: 发现 ', COUNT(*), ' 条孤立记录')
    END as result
FROM iot_device d
WHERE d.deleted = 0
AND d.device_type = 1
AND NOT EXISTS (
    SELECT 1 FROM iot_access_device ad 
    WHERE ad.id = d.id AND ad.deleted = 0
)

UNION ALL

SELECT 
    '孤立记录检查' as validation_type,
    device_type,
    CASE device_type
        WHEN 1 THEN '门禁设备'
        WHEN 2 THEN '德通设备'
        ELSE '其他类型'
    END as device_type_name,
    COUNT(*) as orphan_count,
    CASE 
        WHEN COUNT(*) = 0
        THEN '✓ 通过: 无孤立记录'
        ELSE CONCAT('✗ 警告: 发现 ', COUNT(*), ' 条孤立记录')
    END as result
FROM iot_device d
WHERE d.deleted = 0
AND d.device_type = 2
AND NOT EXISTS (
    SELECT 1 FROM iot_detong_device dd 
    WHERE dd.id = d.id AND dd.deleted = 0
);

-- 检查遗留表中未迁移的记录
SELECT 
    '未迁移记录检查' as validation_type,
    'iot_access_device' as legacy_table,
    COUNT(*) as unmigrated_count,
    CASE 
        WHEN COUNT(*) = 0
        THEN '✓ 通过: 所有记录已迁移'
        ELSE CONCAT('✗ 警告: 发现 ', COUNT(*), ' 条未迁移记录')
    END as result
FROM iot_access_device ad
WHERE ad.deleted = 0
AND NOT EXISTS (
    SELECT 1 FROM iot_device d 
    WHERE d.id = ad.id AND d.deleted = 0
)

UNION ALL

SELECT 
    '未迁移记录检查' as validation_type,
    'iot_detong_device' as legacy_table,
    COUNT(*) as unmigrated_count,
    CASE 
        WHEN COUNT(*) = 0
        THEN '✓ 通过: 所有记录已迁移'
        ELSE CONCAT('✗ 警告: 发现 ', COUNT(*), ' 条未迁移记录')
    END as result
FROM iot_detong_device dd
WHERE dd.deleted = 0
AND NOT EXISTS (
    SELECT 1 FROM iot_device d 
    WHERE d.id = dd.id AND d.deleted = 0
);

-- =====================================================
-- 验证8: 性能指标检查
-- =====================================================

SELECT '' as blank_line;
SELECT '【验证8】性能指标检查' as validation_section;
SELECT '----------------------------------------' as separator;

-- 检查索引使用情况
SELECT 
    '索引检查' as validation_type,
    table_name,
    index_name,
    column_name,
    CASE 
        WHEN non_unique = 0 THEN '唯一索引'
        ELSE '普通索引'
    END as index_type
FROM information_schema.statistics
WHERE table_schema = DATABASE()
AND table_name = 'iot_device'
AND index_name != 'PRIMARY'
ORDER BY table_name, index_name, seq_in_index;

-- 统计表大小
SELECT 
    '表大小统计' as validation_type,
    table_name,
    ROUND(((data_length + index_length) / 1024 / 1024), 2) as size_mb,
    table_rows as estimated_rows
FROM information_schema.tables
WHERE table_schema = DATABASE()
AND table_name IN ('iot_device', 'iot_access_device', 'iot_detong_device')
ORDER BY table_name;

-- =====================================================
-- 验证总结
-- =====================================================

SELECT '' as blank_line;
SELECT '========================================' as separator;
SELECT '验证报告总结' as summary_section;
SELECT '========================================' as separator;

-- 计算总体通过率
SELECT 
    '总体验证结果' as summary_type,
    CASE 
        WHEN (
            -- 数量一致性
            (SELECT COUNT(*) FROM iot_access_device WHERE deleted = 0) = 
            (SELECT COUNT(*) FROM iot_device WHERE device_type = 1 AND deleted = 0)
            AND
            (SELECT COUNT(*) FROM iot_detong_device WHERE deleted = 0) = 
            (SELECT COUNT(*) FROM iot_device WHERE device_type = 2 AND deleted = 0)
            AND
            -- JSON格式有效性
            (SELECT COUNT(*) FROM iot_device WHERE deleted = 0 AND JSON_VALID(config) = 0) = 0
            AND
            -- 必填字段完整性
            (SELECT COUNT(*) FROM iot_device 
             WHERE deleted = 0 
             AND (device_name IS NULL OR device_name = '' 
                  OR serial_number IS NULL OR serial_number = ''
                  OR config IS NULL)) = 0
        )
        THEN '✓✓✓ 所有关键验证通过! 数据迁移质量良好! ✓✓✓'
        ELSE '✗✗✗ 存在验证失败项,请检查上述详细报告 ✗✗✗'
    END as final_result;

-- 提供建议
SELECT 
    '建议操作' as recommendation_type,
    CASE 
        WHEN (
            (SELECT COUNT(*) FROM iot_access_device WHERE deleted = 0) = 
            (SELECT COUNT(*) FROM iot_device WHERE device_type = 1 AND deleted = 0)
            AND
            (SELECT COUNT(*) FROM iot_detong_device WHERE deleted = 0) = 
            (SELECT COUNT(*) FROM iot_device WHERE device_type = 2 AND deleted = 0)
        )
        THEN '1. 数据迁移成功,可以继续使用统一表'
        ELSE '1. 数据迁移存在问题,建议检查并修复'
    END as action_1,
    CASE 
        WHEN (SELECT COUNT(*) FROM iot_device WHERE deleted = 0 AND JSON_VALID(config) = 0) = 0
        THEN '2. JSON配置格式正确,可以正常访问'
        ELSE '2. 存在无效JSON配置,需要修复'
    END as action_2,
    '3. 建议定期运行此验证脚本,监控数据质量' as action_3;

SELECT '========================================' as separator;
SELECT '验证完成时间:' as label, DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s') as completion_time;
SELECT '========================================' as separator;

-- =====================================================
-- 附加信息
-- =====================================================

/*
验证脚本说明:

本脚本提供全面的数据迁移验证,包括:
1. 数据数量统计和对比
2. 租户数据隔离验证
3. JSON配置格式验证
4. 设备状态一致性验证
5. 审计字段完整性验证
6. 数据完整性检查
7. 孤立记录检查
8. 性能指标检查

使用建议:
- 在每次迁移后运行此脚本
- 定期运行以监控数据质量
- 将验证结果保存为报告
- 根据验证结果采取相应措施

注意事项:
- 此脚本为只读操作,不会修改数据
- 可以安全地在生产环境运行
- 建议在低峰时段运行以减少性能影响
- 验证结果应该保存并归档
*/
