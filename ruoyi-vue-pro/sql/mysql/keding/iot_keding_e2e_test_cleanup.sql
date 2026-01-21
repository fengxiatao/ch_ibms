-- =============================================
-- 科鼎固件升级端到端测试数据清理脚本
-- 用于清理 E2E 测试创建的所有数据
-- =============================================

-- 设置租户ID（默认租户）
SET @tenant_id = 1;

-- =============================================
-- 1. 清理升级日志（先清理，因为有外键依赖）
-- =============================================
SELECT '=== 清理升级日志 ===' AS info;

-- 删除 E2E 测试设备相关的升级日志
DELETE FROM iot_keding_upgrade_log 
WHERE task_id IN (
    SELECT id FROM iot_keding_upgrade_task 
    WHERE station_code IN ('TEST001', 'TEST002', 'TEST003')
);

SELECT ROW_COUNT() AS '已删除升级日志记录数';

-- =============================================
-- 2. 清理升级任务
-- =============================================
SELECT '=== 清理升级任务 ===' AS info;

-- 删除 E2E 测试设备相关的升级任务
DELETE FROM iot_keding_upgrade_task 
WHERE station_code IN ('TEST001', 'TEST002', 'TEST003');

SELECT ROW_COUNT() AS '已删除升级任务记录数';

-- =============================================
-- 3. 清理测试固件
-- =============================================
SELECT '=== 清理测试固件 ===' AS info;

-- 删除 E2E 测试固件
DELETE FROM iot_keding_firmware 
WHERE version = '1.0.0-test';

SELECT ROW_COUNT() AS '已删除固件记录数';

-- =============================================
-- 4. 清理测试设备
-- =============================================
SELECT '=== 清理测试设备 ===' AS info;

-- 删除 E2E 测试设备
DELETE FROM iot_keding_device 
WHERE station_code IN ('TEST001', 'TEST002', 'TEST003');

SELECT ROW_COUNT() AS '已删除设备记录数';

-- =============================================
-- 5. 验证清理结果
-- =============================================
SELECT '=== 验证清理结果 ===' AS info;

SELECT 'E2E测试设备' AS '数据类型', COUNT(*) AS '剩余记录数'
FROM iot_keding_device 
WHERE station_code IN ('TEST001', 'TEST002', 'TEST003')

UNION ALL

SELECT 'E2E测试固件' AS '数据类型', COUNT(*) AS '剩余记录数'
FROM iot_keding_firmware 
WHERE version = '1.0.0-test'

UNION ALL

SELECT 'E2E升级任务' AS '数据类型', COUNT(*) AS '剩余记录数'
FROM iot_keding_upgrade_task 
WHERE station_code IN ('TEST001', 'TEST002', 'TEST003')

UNION ALL

SELECT 'E2E升级日志' AS '数据类型', COUNT(*) AS '剩余记录数'
FROM iot_keding_upgrade_log 
WHERE task_id IN (
    SELECT id FROM iot_keding_upgrade_task 
    WHERE station_code IN ('TEST001', 'TEST002', 'TEST003')
);

SELECT '=== E2E 测试数据清理完成 ===' AS info;

-- =============================================
-- 清理说明
-- =============================================
/*
清理脚本说明：

1. 清理顺序（按依赖关系）：
   - 升级日志 (iot_keding_upgrade_log) - 依赖升级任务
   - 升级任务 (iot_keding_upgrade_task) - 依赖设备和固件
   - 测试固件 (iot_keding_firmware)
   - 测试设备 (iot_keding_device)

2. 清理范围：
   - 设备: station_code IN ('TEST001', 'TEST002', 'TEST003')
   - 固件: version = '1.0.0-test'
   - 任务: 关联上述设备的所有任务
   - 日志: 关联上述任务的所有日志

3. 安全说明：
   - 此脚本仅清理 E2E 测试数据
   - 不会影响生产数据或其他测试数据
   - 执行前请确认数据库连接正确

4. 使用方法：
   mysql -u root -p your_database < iot_keding_e2e_test_cleanup.sql
*/
