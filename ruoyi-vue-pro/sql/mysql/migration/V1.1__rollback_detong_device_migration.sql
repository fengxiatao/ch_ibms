-- =====================================================
-- 德通设备数据迁移回滚脚本
-- 版本: V1.1
-- 描述: 回滚 iot_detong_device 到 iot_device 的数据迁移
-- 作者: IoT Platform Team
-- 日期: 2024-12-11
-- =====================================================

-- 设置事务隔离级别
SET SESSION TRANSACTION ISOLATION LEVEL READ COMMITTED;

-- 开始事务
START TRANSACTION;

SELECT '========================================' as separator;
SELECT '德通设备数据迁移回滚' as title;
SELECT DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s') as rollback_time;
SELECT '========================================' as separator;

-- =====================================================
-- 回滚前检查
-- =====================================================

SELECT '' as blank_line;
SELECT '【步骤1】回滚前数据检查' as step;
SELECT '----------------------------------------' as separator;

-- 检查要回滚的数据数量
SELECT 
    '统一表中的德通设备数量' as check_item,
    COUNT(*) as record_count
FROM iot_device
WHERE device_type = 2 AND deleted = 0;

-- 检查遗留表数据是否完整
SELECT 
    '遗留表中的德通设备数量' as check_item,
    COUNT(*) as record_count
FROM iot_detong_device
WHERE deleted = 0;

-- 确认回滚范围
SELECT 
    '将要删除的记录数' as check_item,
    COUNT(*) as record_count
FROM iot_device d
WHERE d.device_type = 2
AND d.deleted = 0
AND EXISTS (
    SELECT 1 FROM iot_detong_device dd 
    WHERE dd.id = d.id AND dd.deleted = 0
);

-- =====================================================
-- 回滚方案选择
-- =====================================================

SELECT '' as blank_line;
SELECT '【步骤2】选择回滚方案' as step;
SELECT '----------------------------------------' as separator;
SELECT '方案1: 删除迁移的数据(推荐)' as option1;
SELECT '方案2: 从备份表恢复(需要预先创建备份)' as option2;
SELECT '' as blank_line;

-- =====================================================
-- 方案1: 删除迁移的数据(推荐)
-- =====================================================

SELECT '执行方案1: 删除统一表中的德通设备数据...' as action;

-- 删除迁移到统一表的德通设备数据
DELETE FROM iot_device
WHERE device_type = 2
AND id IN (
    SELECT id FROM iot_detong_device WHERE deleted = 0
);

SELECT '删除完成!' as status;
SELECT ROW_COUNT() as deleted_count;

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

-- 删除当前数据
DELETE FROM iot_device WHERE device_type = 2;

-- 从备份表恢复
INSERT INTO iot_device
SELECT * FROM iot_device_backup
WHERE device_type = 2;

SELECT '恢复完成!' as status;
SELECT ROW_COUNT() as restored_count;
*/

-- =====================================================
-- 回滚后验证
-- =====================================================

SELECT '' as blank_line;
SELECT '【步骤3】回滚后数据验证' as step;
SELECT '----------------------------------------' as separator;

-- 验证统一表中的德通设备已删除
SELECT 
    '统一表中的德通设备数量' as validation_item,
    COUNT(*) as record_count,
    CASE 
        WHEN COUNT(*) = 0 
        THEN '✓ 通过: 已全部删除'
        ELSE CONCAT('✗ 警告: 仍有 ', COUNT(*), ' 条记录')
    END as result
FROM iot_device
WHERE device_type = 2 AND deleted = 0;

-- 验证遗留表数据完整性
SELECT 
    '遗留表数据完整性' as validation_item,
    COUNT(*) as record_count,
    CASE 
        WHEN COUNT(*) > 0 
        THEN '✓ 通过: 遗留表数据完整'
        ELSE '✗ 警告: 遗留表无数据'
    END as result
FROM iot_detong_device
WHERE deleted = 0;

-- 检查是否有孤立的德通设备记录
SELECT 
    '孤立记录检查' as validation_item,
    COUNT(*) as orphan_count,
    CASE 
        WHEN COUNT(*) = 0 
        THEN '✓ 通过: 无孤立记录'
        ELSE CONCAT('✗ 警告: 发现 ', COUNT(*), ' 条孤立记录')
    END as result
FROM iot_device d
WHERE d.device_type = 2
AND d.deleted = 0
AND NOT EXISTS (
    SELECT 1 FROM iot_detong_device dd 
    WHERE dd.id = d.id AND dd.deleted = 0
);

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
            -- 统一表中的德通设备已删除
            (SELECT COUNT(*) FROM iot_device 
             WHERE device_type = 2 AND deleted = 0) = 0
            AND
            -- 遗留表数据完整
            (SELECT COUNT(*) FROM iot_detong_device 
             WHERE deleted = 0) > 0
        )
        THEN '✓✓✓ 回滚成功! 数据已恢复到迁移前状态 ✓✓✓'
        ELSE '✗✗✗ 回滚可能不完整,请检查上述验证结果 ✗✗✗'
    END as rollback_result;

SELECT 
    '下一步操作' as next_step,
    CASE 
        WHEN (SELECT COUNT(*) FROM iot_device WHERE device_type = 2 AND deleted = 0) = 0
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
-- 回滚说明
-- =====================================================

/*
回滚方案说明:

方案1: 删除迁移数据(推荐)
- 优点: 简单直接,不需要备份表
- 缺点: 如果迁移后有新的修改,这些修改会丢失
- 适用场景: 迁移后立即发现问题需要回滚

方案2: 从备份表恢复
- 优点: 完整恢复到迁移前状态
- 缺点: 需要预先创建备份表
- 适用场景: 需要保留迁移后的变更

回滚后的数据状态:
- iot_device表中不再有device_type=2的记录
- iot_detong_device表保持不变
- 应用可以继续使用遗留表访问德通设备

注意事项:
1. 回滚使用事务保护,确保数据一致性
2. 回滚前会检查数据状态
3. 回滚后会验证数据完整性
4. 需要手动执行COMMIT或ROLLBACK
5. 建议在测试环境先验证回滚流程

如果需要重新迁移:
1. 确认回滚成功
2. 检查并修复迁移脚本中的问题
3. 重新执行迁移脚本
4. 再次验证迁移结果
*/
