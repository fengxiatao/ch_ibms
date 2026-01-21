-- =====================================================
-- IoT设备表结构重构 - 门禁设备迁移回滚脚本
-- 功能: 回滚门禁设备数据迁移,恢复到迁移前状态
-- 版本: V1.0
-- 日期: 2024-12-11
-- =====================================================

-- 警告: 
-- 1. 本脚本将删除统一表中的门禁设备数据
-- 2. 执行前请确保已备份数据
-- 3. 遗留表(iot_access_device)的数据不会被删除
-- 4. 建议在测试环境先验证回滚脚本

-- =====================================================
-- 回滚前检查
-- =====================================================

SELECT '========== 回滚前数据检查 ==========' as section;

-- 检查将要删除的数据量
SELECT 
    '即将删除的门禁设备数量' as description,
    COUNT(*) as count,
    '请确认是否继续' as warning
FROM iot_device
WHERE device_type = 1;

-- 检查遗留表数据是否完整
SELECT 
    '遗留表数据检查' as description,
    COUNT(*) as access_device_count,
    '遗留表数据将被保留' as note
FROM iot_access_device
WHERE deleted = 0;

-- =====================================================
-- 方案1: 删除迁移的数据(推荐)
-- =====================================================

-- 说明: 此方案删除统一表中的门禁设备数据,保留遗留表数据
-- 优点: 简单直接,遗留表数据完整
-- 缺点: 如果迁移后有新增或修改,这些变更会丢失

SELECT '========== 执行回滚方案1: 删除迁移数据 ==========' as section;

-- 开始事务
START TRANSACTION;

-- 记录回滚前的数据量
SELECT 
    '回滚前统计' as stage,
    (SELECT COUNT(*) FROM iot_device WHERE device_type = 1) as unified_table_count,
    (SELECT COUNT(*) FROM iot_access_device WHERE deleted = 0) as legacy_table_count,
    NOW() as timestamp;

-- 删除统一表中的门禁设备数据
DELETE FROM iot_device
WHERE device_type = 1
  AND id IN (
      SELECT id FROM iot_access_device
  );

-- 记录回滚后的数据量
SELECT 
    '回滚后统计' as stage,
    (SELECT COUNT(*) FROM iot_device WHERE device_type = 1) as unified_table_count,
    (SELECT COUNT(*) FROM iot_access_device WHERE deleted = 0) as legacy_table_count,
    NOW() as timestamp;

-- 验证回滚结果
SELECT 
    '回滚验证' as check_type,
    CASE 
        WHEN (SELECT COUNT(*) FROM iot_device WHERE device_type = 1) = 0
        THEN '✓ 回滚成功'
        ELSE '✗ 回滚失败,仍有门禁设备数据'
    END as result,
    (SELECT COUNT(*) FROM iot_device WHERE device_type = 1) as remaining_count;

-- 如果验证通过,提交事务;否则需要手动检查
-- COMMIT;  -- 取消注释以提交
-- ROLLBACK;  -- 如果需要撤销,使用此命令

-- =====================================================
-- 方案2: 从备份表恢复(可选)
-- =====================================================

-- 说明: 如果在迁移前创建了备份表,可以使用此方案恢复
-- 前提: 需要先执行备份脚本创建 iot_device_backup 表

/*
-- 检查备份表是否存在
SELECT 
    '备份表检查' as description,
    CASE 
        WHEN EXISTS (
            SELECT 1 FROM information_schema.tables 
            WHERE table_schema = DATABASE() 
            AND table_name = 'iot_device_backup'
        )
        THEN '✓ 备份表存在'
        ELSE '✗ 备份表不存在'
    END as result;

-- 从备份表恢复(如果备份表存在)
START TRANSACTION;

-- 删除当前统一表中的门禁设备数据
DELETE FROM iot_device WHERE device_type = 1;

-- 从备份表恢复数据
INSERT INTO iot_device
SELECT * FROM iot_device_backup
WHERE device_type = 1;

-- 验证恢复结果
SELECT 
    '从备份恢复验证' as check_type,
    (SELECT COUNT(*) FROM iot_device WHERE device_type = 1) as restored_count,
    (SELECT COUNT(*) FROM iot_device_backup WHERE device_type = 1) as backup_count,
    CASE 
        WHEN (SELECT COUNT(*) FROM iot_device WHERE device_type = 1) = 
             (SELECT COUNT(*) FROM iot_device_backup WHERE device_type = 1)
        THEN '✓ 恢复成功'
        ELSE '✗ 恢复失败'
    END as result;

-- COMMIT;  -- 取消注释以提交
*/

-- =====================================================
-- 回滚后验证
-- =====================================================

SELECT '========== 回滚后验证 ==========' as section;

-- 验证统一表中是否还有门禁设备
SELECT 
    '统一表门禁设备检查' as check_type,
    COUNT(*) as remaining_count,
    CASE 
        WHEN COUNT(*) = 0 THEN '✓ 已清理干净'
        ELSE '✗ 仍有残留数据'
    END as result
FROM iot_device
WHERE device_type = 1;

-- 验证遗留表数据是否完整
SELECT 
    '遗留表数据完整性' as check_type,
    COUNT(*) as count,
    '遗留表数据应保持不变' as note
FROM iot_access_device
WHERE deleted = 0;

-- 对比迁移前后的数据量(需要有迁移前的记录)
SELECT 
    '数据量对比' as check_type,
    (SELECT COUNT(*) FROM iot_access_device WHERE deleted = 0) as legacy_count,
    (SELECT COUNT(*) FROM iot_device WHERE device_type = 1) as unified_count,
    '统一表应为0,遗留表应保持原数量' as expected;

-- =====================================================
-- 回滚完成提示
-- =====================================================

SELECT 
    '门禁设备迁移回滚完成' as status,
    NOW() as completion_time,
    '请验证遗留表数据完整性' as next_step,
    '如需重新迁移,请执行迁移脚本' as note;

-- =====================================================
-- 附加: 创建备份表的脚本(在迁移前执行)
-- =====================================================

/*
-- 在执行迁移前,先创建备份表
CREATE TABLE iot_device_backup LIKE iot_device;

-- 备份现有数据
INSERT INTO iot_device_backup SELECT * FROM iot_device;

-- 验证备份
SELECT 
    '备份验证' as check_type,
    (SELECT COUNT(*) FROM iot_device) as original_count,
    (SELECT COUNT(*) FROM iot_device_backup) as backup_count,
    CASE 
        WHEN (SELECT COUNT(*) FROM iot_device) = (SELECT COUNT(*) FROM iot_device_backup)
        THEN '✓ 备份成功'
        ELSE '✗ 备份失败'
    END as result;
*/
