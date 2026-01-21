-- =============================================
-- 德通(Detong)表清理脚本
-- 清空数据并删除所有 iot_detong_* 表
-- =============================================

-- 设置外键检查为0，避免删除时的外键约束问题
SET FOREIGN_KEY_CHECKS = 0;

-- =============================================
-- Step 1: 清空德通表数据 (TRUNCATE)
-- =============================================
SELECT '=== Step 1: 清空德通表数据 ===' AS info;

-- 按依赖顺序清空（先清空子表）
TRUNCATE TABLE IF EXISTS `iot_detong_upgrade_log`;
TRUNCATE TABLE IF EXISTS `iot_detong_upgrade_task`;
TRUNCATE TABLE IF EXISTS `iot_detong_control_log`;
TRUNCATE TABLE IF EXISTS `iot_detong_alarm`;
TRUNCATE TABLE IF EXISTS `iot_detong_firmware`;
TRUNCATE TABLE IF EXISTS `iot_detong_device`;

SELECT '德通表数据已清空' AS result;

-- =============================================
-- Step 2: 删除德通表结构 (DROP TABLE)
-- =============================================
SELECT '=== Step 2: 删除德通表结构 ===' AS info;

-- 按依赖顺序删除（先删除子表）
DROP TABLE IF EXISTS `iot_detong_upgrade_log`;
DROP TABLE IF EXISTS `iot_detong_upgrade_task`;
DROP TABLE IF EXISTS `iot_detong_control_log`;
DROP TABLE IF EXISTS `iot_detong_alarm`;
DROP TABLE IF EXISTS `iot_detong_firmware`;
DROP TABLE IF EXISTS `iot_detong_device`;

SELECT '德通表结构已删除' AS result;

-- 恢复外键检查
SET FOREIGN_KEY_CHECKS = 1;

-- =============================================
-- Step 3: 验证清理结果
-- =============================================
SELECT '=== Step 3: 验证清理结果 ===' AS info;

SELECT 
    CASE 
        WHEN COUNT(*) = 0 THEN '✓ 德通表已全部删除'
        ELSE CONCAT('✗ 仍存在 ', COUNT(*), ' 个德通表')
    END AS verification_result
FROM information_schema.tables 
WHERE table_schema = DATABASE() 
AND table_name LIKE 'iot_detong_%';
