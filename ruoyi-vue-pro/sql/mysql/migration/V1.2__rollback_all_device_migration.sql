-- =====================================================
-- 通用设备数据迁移回滚脚本
-- 版本: V1.2
-- 描述: 回滚所有设备类型的数据迁移
-- 作者: IoT Platform Team
-- 日期: 2024-12-11
-- =====================================================

-- 设置事务隔离级别
SET SESSION TRANSACTION ISOLATION LEVEL READ COMMITTED;

-- 开始事务
START TRANSACTION;

SELECT '========================================' as separator;
SELECT '设备数据迁移回滚' as title;
SELECT DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s') as rollback_time;
SELECT '========================================' as separator;

-- =====================================================
-- 回滚前检查
-- =====================================================

SELECT '' as blank_line;
SELECT '【步骤1】回滚前数据检查' as step;
SELECT '----------------------------------------' as separator;

-- 统计要回滚的数据
SELECT 
    '统一表设备统计' as check_item,
    device_type,
    CASE device_type
        WHEN 1 THEN '门禁设备(ACCESS)'
        WHEN 2 THEN '德通设备(DETONG)'
        ELSE CONCAT('其他类型(', device_type, ')')
    END as device_type_name,
    COUNT(*) as record_count
FROM iot_device
WHERE deleted = 0
GROUP BY device_type
ORDER BY device_type;

-- 检查遗留表数据
SELECT 
    '遗留表数据检查' as check_item,
    'iot_access_device' as table_name,
    COUNT(*) as record_count
FROM iot_access_device
WHERE deleted = 0

UNION ALL

SELECT 
    '遗留表数据检查' as check_item,
    'iot_detong_device' as table_name,
    COUNT(*) as record_count
FROM iot_detong_device
WHERE deleted = 0;

-- 确认回滚范围
SELECT 
    '回滚范围确认' as check_item,
    device_type,
    CASE device_type
        WHEN 1 THEN '门禁设备'
        WHEN 2 THEN '德通设备'
        ELSE '其他类型'
    END as device_type_name,
    COUNT(*) as will_be_deleted
FROM iot_device d
WHERE d.deleted = 0
AND (
    (d.device_type = 1 AND EXISTS (SELECT 1 FROM iot_access_device ad WHERE ad.id = d.id AND ad.deleted = 0))
    OR
    (d.device_type = 2 AND EXISTS (SELECT 1 FROM iot_detong_device dd WHERE dd.id = d.id AND dd.deleted = 0))
)
GROUP BY device_type
ORDER BY device_type;

-- =====================================================
-- 回滚方案选择
-- =====================================================

SELECT '' as blank_line;
SELECT '【步骤2】选择回滚方案' as step;
SELECT '----------------------------------------' as separator;
SELECT '方案1: 删除迁移的数据(推荐)' as option1;
SELECT '  - 优点: 简单直接,不需要备份表' as option1_pro;
SELECT '  - 缺点: 迁移后的修改会丢失' as option1_con;
SELECT '  - 适用: 迁移后立即发现问题' as option1_use;
SELECT '' as blank_line;
SELECT '方案2: 从备份表恢复(可选)' as option2;
SELECT '  - 优点: 完整恢复到迁移前状态' as option2_pro;
SELECT '  - 缺点: 需要预先创建备份表' as option2_con;
SELECT '  - 适用: 需要保留迁移后的变更' as option2_use;
SELECT '' as blank_line;

-- =====================================================
-- 方案1: 删除迁移的数据(推荐)
-- =====================================================

SELECT '执行方案1: 删除统一表中的迁移数据...' as action;

-- 删除门禁设备数据
DELETE FROM iot_device
WHERE device_type = 1
AND id IN (
    SELECT id FROM iot_access_device WHERE deleted = 0
);

SELECT '门禁设备删除完成!' as status;
SELECT ROW_COUNT() as access_deleted_count;

-- 删除德通设备数据
DELETE FROM iot_device
WHERE device_type = 2
AND id IN (
    SELECT id FROM iot_detong_device WHERE deleted = 0
);

SELECT '德通设备删除完成!' as status;
SELECT ROW_COUNT() as detong_deleted_count;

-- =====================================================
-- 方案2: 从备份表恢复(可选)
-- =====================================================

/*
-- 如果需要使用备份表恢复,请取消注释以下代码:

SELECT '执行方案2: 从备份表恢复...' as action;

-- 检查备份表是否存在
SELECT 
    CASE 
        WHEN EXISTS (
            SELECT 1 FROM information_schema.tables 
            WHERE table_schema = DATABASE() 
            AND table_name = 'iot_device_backup'
        )
        THEN '✓ 备份表存在'
        ELSE '✗ 备份表不存在,无法使用方案2'
    END as backup_check;

-- 删除当前迁移的数据
DELETE FROM iot_device 
WHERE device_type IN (1, 2);

SELECT '当前数据删除完成!' as status;
SELECT ROW_COUNT() as deleted_count;

-- 从备份表恢复
INSERT INTO iot_device
SELECT * FROM iot_device_backup
WHERE device_type IN (1, 2);

SELECT '数据恢复完成!' as status;
SELECT ROW_COUNT() as restored_count;
*/

-- =====================================================
-- 回滚后验证
-- =====================================================

SELECT '' as blank_line;
SELECT '【步骤3】回滚后数据验证' as step;
SELECT '----------------------------------------' as separator;

-- 验证统一表中的设备已删除
SELECT 
    '统一表设备清理验证' as validation_item,
    device_type,
    CASE device_type
        WHEN 1 THEN '门禁设备'
        WHEN 2 THEN '德通设备'
        ELSE '其他类型'
    END as device_type_name,
    COUNT(*) as remaining_count,
    CASE 
        WHEN COUNT(*) = 0 
        THEN '✓ 通过: 已全部删除'
        ELSE CONCAT('✗ 警告: 仍有 ', COUNT(*), ' 条记录')
    END as result
FROM iot_device
WHERE deleted = 0
AND device_type IN (1, 2)
GROUP BY device_type

UNION ALL

SELECT 
    '统一表设备清理验证' as validation_item,
    0 as device_type,
    '总计' as device_type_name,
    COUNT(*) as remaining_count,
    CASE 
        WHEN COUNT(*) = 0 
        THEN '✓ 通过: 所有迁移数据已删除'
        ELSE CONCAT('✗ 警告: 仍有 ', COUNT(*), ' 条记录')
    END as result
FROM iot_device
WHERE deleted = 0
AND device_type IN (1, 2);

-- 验证遗留表数据完整性
SELECT 
    '遗留表数据完整性' as validation_item,
    'iot_access_device' as table_name,
    COUNT(*) as record_count,
    CASE 
        WHEN COUNT(*) > 0 
        THEN '✓ 通过: 遗留表数据完整'
        ELSE '✗ 警告: 遗留表无数据'
    END as result
FROM iot_access_device
WHERE deleted = 0

UNION ALL

SELECT 
    '遗留表数据完整性' as validation_item,
    'iot_detong_device' as table_name,
    COUNT(*) as record_count,
    CASE 
        WHEN COUNT(*) > 0 
        THEN '✓ 通过: 遗留表数据完整'
        ELSE '✗ 警告: 遗留表无数据'
    END as result
FROM iot_detong_device
WHERE deleted = 0;

-- 检查是否有孤立记录
SELECT 
    '孤立记录检查' as validation_item,
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
    '孤立记录检查' as validation_item,
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

-- 统计剩余设备类型
SELECT 
    '剩余设备类型统计' as validation_item,
    device_type,
    CASE device_type
        WHEN 1 THEN '门禁设备'
        WHEN 2 THEN '德通设备'
        ELSE CONCAT('其他类型(', device_type, ')')
    END as device_type_name,
    COUNT(*) as record_count
FROM iot_device
WHERE deleted = 0
GROUP BY device_type
ORDER BY device_type;

-- =====================================================
-- 回滚总结
-- =====================================================

SELECT '' as blank_line;
SELECT '========================================' as separator;
SELECT '回滚操作总结' as summary_section;
SELECT '========================================' as separator;

SELECT 
    CASE 
        WHEN (
            -- 统一表中的迁移设备已删除
            (SELECT COUNT(*) FROM iot_device 
             WHERE device_type IN (1, 2) AND deleted = 0) = 0
            AND
            -- 遗留表数据完整
            (SELECT COUNT(*) FROM iot_access_device WHERE deleted = 0) > 0
            AND
            (SELECT COUNT(*) FROM iot_detong_device WHERE deleted = 0) > 0
        )
        THEN '✓✓✓ 回滚成功! 数据已恢复到迁移前状态 ✓✓✓'
        WHEN (
            (SELECT COUNT(*) FROM iot_device 
             WHERE device_type IN (1, 2) AND deleted = 0) = 0
        )
        THEN '✓ 回滚部分成功: 迁移数据已删除,但请检查遗留表'
        ELSE '✗✗✗ 回滚可能不完整,请检查上述验证结果 ✗✗✗'
    END as rollback_result;

-- 提供详细统计
SELECT 
    '回滚统计' as summary_type,
    (SELECT COUNT(*) FROM iot_device WHERE device_type IN (1, 2) AND deleted = 0) as remaining_in_unified,
    (SELECT COUNT(*) FROM iot_access_device WHERE deleted = 0) as access_in_legacy,
    (SELECT COUNT(*) FROM iot_detong_device WHERE deleted = 0) as detong_in_legacy;

SELECT 
    '下一步操作' as next_step,
    CASE 
        WHEN (SELECT COUNT(*) FROM iot_device WHERE device_type IN (1, 2) AND deleted = 0) = 0
        THEN '回滚成功,可以提交事务: COMMIT;'
        ELSE '回滚可能失败,建议回滚事务: ROLLBACK; 并检查问题'
    END as recommendation;

-- =====================================================
-- 提交或回滚事务
-- =====================================================

SELECT '' as blank_line;
SELECT '【步骤4】提交或回滚事务' as step;
SELECT '----------------------------------------' as separator;
SELECT '如果验证通过,执行: COMMIT;' as action;
SELECT '如果验证失败,执行: ROLLBACK;' as action;

-- 提交事务(需要手动执行)
-- COMMIT;

SELECT '========================================' as separator;
SELECT '回滚完成时间:' as label, DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s') as completion_time;
SELECT '========================================' as separator;

-- =====================================================
-- 回滚说明和注意事项
-- =====================================================

/*
回滚脚本说明:

本脚本提供全面的数据迁移回滚功能,包括:
1. 回滚前数据检查
2. 两种回滚方案选择
3. 回滚后数据验证
4. 详细的回滚统计

回滚方案对比:

方案1: 删除迁移数据(推荐)
- 删除统一表中的门禁设备(device_type=1)
- 删除统一表中的德通设备(device_type=2)
- 保留遗留表数据不变
- 适用于迁移后立即发现问题的场景

方案2: 从备份表恢复
- 需要预先创建iot_device_backup表
- 完整恢复到迁移前状态
- 适用于需要保留迁移后变更的场景

回滚后的数据状态:
- iot_device表中不再有device_type=1和2的记录
- iot_access_device表保持不变
- iot_detong_device表保持不变
- 应用可以继续使用遗留表访问设备

注意事项:
1. 回滚使用事务保护,确保数据一致性
2. 回滚前会检查数据状态
3. 回滚后会验证数据完整性
4. 需要手动执行COMMIT或ROLLBACK
5. 建议在测试环境先验证回滚流程
6. 回滚会丢失迁移后的所有修改

如果需要重新迁移:
1. 确认回滚成功
2. 检查并修复迁移脚本中的问题
3. 重新执行迁移脚本
4. 再次验证迁移结果

常见问题:

Q: 回滚后可以重新迁移吗?
A: 可以。回滚后遗留表数据完整,可以重新执行迁移脚本。

Q: 回滚会影响其他设备类型吗?
A: 不会。回滚只删除device_type=1和2的记录,其他类型不受影响。

Q: 回滚需要多长时间?
A: 取决于数据量,通常比迁移快,因为只是删除操作。

Q: 回滚失败怎么办?
A: 执行ROLLBACK回滚事务,检查错误日志,修复问题后重试。

Q: 可以只回滚某一种设备类型吗?
A: 可以。修改DELETE语句,只删除特定device_type的记录。

联系支持:
如果遇到问题,请联系:
- 技术支持: support@example.com
- DBA团队: dba@example.com
*/
