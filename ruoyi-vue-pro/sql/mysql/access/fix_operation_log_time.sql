-- =====================================================
-- 修复 iot_access_operation_log 表中 operation_time 为空的数据
-- 将 operation_time 设置为 create_time
-- =====================================================

-- 更新 operation_time 为空的记录，使用 create_time 作为默认值
UPDATE `iot_access_operation_log` 
SET `operation_time` = `create_time` 
WHERE `operation_time` IS NULL;

-- 验证修复结果
SELECT COUNT(*) AS '修复后仍为空的记录数' 
FROM `iot_access_operation_log` 
WHERE `operation_time` IS NULL;
