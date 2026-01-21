-- =============================================
-- 验证脚本：验证从科鼎(Keding)到长辉(Changhui)的数据迁移
-- =============================================

-- ----------------------------
-- 1. 验证设备数据迁移
-- ----------------------------
SELECT '=== 设备数据迁移验证 ===' AS validation_step;

SELECT 
    'iot_keding_device' AS source_table,
    COUNT(*) AS source_count,
    (SELECT COUNT(*) FROM changhui_device) AS target_count,
    CASE 
        WHEN COUNT(*) = (SELECT COUNT(*) FROM changhui_device) THEN '✓ 通过'
        ELSE '✗ 失败'
    END AS status
FROM iot_keding_device;

-- 检查是否有遗漏的设备
SELECT 
    '遗漏的设备' AS check_type,
    COUNT(*) AS missing_count
FROM iot_keding_device kd
WHERE NOT EXISTS (
    SELECT 1 FROM changhui_device cd 
    WHERE cd.station_code = kd.station_code
);

-- ----------------------------
-- 2. 验证报警数据迁移
-- ----------------------------
SELECT '=== 报警数据迁移验证 ===' AS validation_step;

SELECT 
    'iot_keding_alarm' AS source_table,
    COUNT(*) AS source_count,
    (SELECT COUNT(*) FROM changhui_alarm) AS target_count,
    CASE 
        WHEN COUNT(*) = (SELECT COUNT(*) FROM changhui_alarm) THEN '✓ 通过'
        ELSE '✗ 失败'
    END AS status
FROM iot_keding_alarm;

-- ----------------------------
-- 3. 验证控制日志数据迁移
-- ----------------------------
SELECT '=== 控制日志数据迁移验证 ===' AS validation_step;

SELECT 
    'iot_keding_control_log' AS source_table,
    COUNT(*) AS source_count,
    (SELECT COUNT(*) FROM changhui_control_log) AS target_count,
    CASE 
        WHEN COUNT(*) = (SELECT COUNT(*) FROM changhui_control_log) THEN '✓ 通过'
        ELSE '✗ 失败'
    END AS status
FROM iot_keding_control_log;

-- ----------------------------
-- 4. 验证固件数据迁移
-- ----------------------------
SELECT '=== 固件数据迁移验证 ===' AS validation_step;

SELECT 
    'iot_keding_firmware' AS source_table,
    COUNT(*) AS source_count,
    (SELECT COUNT(*) FROM changhui_firmware) AS target_count,
    CASE 
        WHEN COUNT(*) = (SELECT COUNT(*) FROM changhui_firmware) THEN '✓ 通过'
        ELSE '✗ 失败'
    END AS status
FROM iot_keding_firmware;

-- ----------------------------
-- 5. 验证升级任务数据迁移
-- ----------------------------
SELECT '=== 升级任务数据迁移验证 ===' AS validation_step;

SELECT 
    'iot_keding_upgrade_task' AS source_table,
    COUNT(*) AS source_count,
    (SELECT COUNT(*) FROM changhui_upgrade_task) AS target_count,
    CASE 
        WHEN COUNT(*) = (SELECT COUNT(*) FROM changhui_upgrade_task) THEN '✓ 通过'
        ELSE '✗ 失败'
    END AS status
FROM iot_keding_upgrade_task;

-- ----------------------------
-- 6. 总体验证结果
-- ----------------------------
SELECT '=== 总体验证结果 ===' AS validation_step;

SELECT 
    '总体迁移状态' AS check_type,
    CASE 
        WHEN (SELECT COUNT(*) FROM iot_keding_device) = (SELECT COUNT(*) FROM changhui_device)
         AND (SELECT COUNT(*) FROM iot_keding_alarm) = (SELECT COUNT(*) FROM changhui_alarm)
         AND (SELECT COUNT(*) FROM iot_keding_control_log) = (SELECT COUNT(*) FROM changhui_control_log)
         AND (SELECT COUNT(*) FROM iot_keding_firmware) = (SELECT COUNT(*) FROM changhui_firmware)
         AND (SELECT COUNT(*) FROM iot_keding_upgrade_task) = (SELECT COUNT(*) FROM changhui_upgrade_task)
        THEN '✓ 所有数据迁移成功'
        ELSE '✗ 存在数据迁移问题，请检查'
    END AS status;
