-- =============================================
-- 验证脚本：验证从科鼎(Keding)到长辉(Changhui)的数据迁移
-- 版本: V6.2
-- 
-- 功能说明:
-- 1. 检查源表是否存在
-- 2. 验证数据迁移完整性
-- 3. 检查数据一致性
-- 4. 输出详细的验证报告
-- =============================================

-- 设置字符集
SET NAMES utf8mb4;

SELECT '========================================' AS separator;
SELECT '  科鼎到长辉数据迁移验证报告' AS title;
SELECT '========================================' AS separator;
SELECT NOW() AS validation_time;

-- ----------------------------
-- 1. 检查源表存在性
-- ----------------------------
SELECT '' AS blank;
SELECT '=== 1. 源表存在性检查 ===' AS section;

SELECT 
    table_name,
    CASE WHEN COUNT(*) > 0 THEN '✓ 存在' ELSE '✗ 不存在' END AS status
FROM (
    SELECT 'iot_keding_device' AS table_name
    UNION ALL SELECT 'iot_keding_alarm'
    UNION ALL SELECT 'iot_keding_control_log'
    UNION ALL SELECT 'iot_keding_firmware'
    UNION ALL SELECT 'iot_keding_upgrade_task'
) t
LEFT JOIN information_schema.tables ist 
    ON ist.table_schema = DATABASE() AND ist.table_name = t.table_name
GROUP BY t.table_name;

-- ----------------------------
-- 2. 检查目标表存在性
-- ----------------------------
SELECT '' AS blank;
SELECT '=== 2. 目标表存在性检查 ===' AS section;

SELECT 
    table_name,
    CASE WHEN COUNT(*) > 0 THEN '✓ 存在' ELSE '✗ 不存在' END AS status
FROM (
    SELECT 'changhui_device' AS table_name
    UNION ALL SELECT 'changhui_data'
    UNION ALL SELECT 'changhui_alarm'
    UNION ALL SELECT 'changhui_control_log'
    UNION ALL SELECT 'changhui_firmware'
    UNION ALL SELECT 'changhui_upgrade_task'
) t
LEFT JOIN information_schema.tables ist 
    ON ist.table_schema = DATABASE() AND ist.table_name = t.table_name
GROUP BY t.table_name;

-- ----------------------------
-- 3. 数据量对比
-- ----------------------------
SELECT '' AS blank;
SELECT '=== 3. 数据量对比 ===' AS section;

-- 创建临时存储过程进行数据量对比
DELIMITER //

DROP PROCEDURE IF EXISTS sp_validate_migration_counts//

CREATE PROCEDURE sp_validate_migration_counts()
BEGIN
    DECLARE v_keding_device_count INT DEFAULT 0;
    DECLARE v_changhui_device_count INT DEFAULT 0;
    DECLARE v_keding_alarm_count INT DEFAULT 0;
    DECLARE v_changhui_alarm_count INT DEFAULT 0;
    DECLARE v_keding_control_count INT DEFAULT 0;
    DECLARE v_changhui_control_count INT DEFAULT 0;
    DECLARE v_keding_firmware_count INT DEFAULT 0;
    DECLARE v_changhui_firmware_count INT DEFAULT 0;
    DECLARE v_keding_upgrade_count INT DEFAULT 0;
    DECLARE v_changhui_upgrade_count INT DEFAULT 0;
    DECLARE v_table_exists INT DEFAULT 0;
    
    -- 设备数据对比
    SELECT COUNT(*) INTO v_table_exists 
    FROM information_schema.tables 
    WHERE table_schema = DATABASE() AND table_name = 'iot_keding_device';
    
    IF v_table_exists > 0 THEN
        SELECT COUNT(*) INTO v_keding_device_count FROM iot_keding_device;
    END IF;
    SELECT COUNT(*) INTO v_changhui_device_count FROM changhui_device;
    
    -- 报警数据对比
    SELECT COUNT(*) INTO v_table_exists 
    FROM information_schema.tables 
    WHERE table_schema = DATABASE() AND table_name = 'iot_keding_alarm';
    
    IF v_table_exists > 0 THEN
        SELECT COUNT(*) INTO v_keding_alarm_count FROM iot_keding_alarm;
    END IF;
    SELECT COUNT(*) INTO v_changhui_alarm_count FROM changhui_alarm;
    
    -- 控制日志数据对比
    SELECT COUNT(*) INTO v_table_exists 
    FROM information_schema.tables 
    WHERE table_schema = DATABASE() AND table_name = 'iot_keding_control_log';
    
    IF v_table_exists > 0 THEN
        SELECT COUNT(*) INTO v_keding_control_count FROM iot_keding_control_log;
    END IF;
    SELECT COUNT(*) INTO v_changhui_control_count FROM changhui_control_log;
    
    -- 固件数据对比
    SELECT COUNT(*) INTO v_table_exists 
    FROM information_schema.tables 
    WHERE table_schema = DATABASE() AND table_name = 'iot_keding_firmware';
    
    IF v_table_exists > 0 THEN
        SELECT COUNT(*) INTO v_keding_firmware_count FROM iot_keding_firmware;
    END IF;
    SELECT COUNT(*) INTO v_changhui_firmware_count FROM changhui_firmware;
    
    -- 升级任务数据对比
    SELECT COUNT(*) INTO v_table_exists 
    FROM information_schema.tables 
    WHERE table_schema = DATABASE() AND table_name = 'iot_keding_upgrade_task';
    
    IF v_table_exists > 0 THEN
        SELECT COUNT(*) INTO v_keding_upgrade_count FROM iot_keding_upgrade_task;
    END IF;
    SELECT COUNT(*) INTO v_changhui_upgrade_count FROM changhui_upgrade_task;
    
    -- 输出对比结果
    SELECT 
        '设备' AS data_type,
        v_keding_device_count AS keding_count,
        v_changhui_device_count AS changhui_count,
        CASE 
            WHEN v_keding_device_count <= v_changhui_device_count THEN '✓ 通过'
            ELSE '✗ 失败'
        END AS status
    UNION ALL
    SELECT 
        '报警',
        v_keding_alarm_count,
        v_changhui_alarm_count,
        CASE 
            WHEN v_keding_alarm_count <= v_changhui_alarm_count THEN '✓ 通过'
            ELSE '✗ 失败'
        END
    UNION ALL
    SELECT 
        '控制日志',
        v_keding_control_count,
        v_changhui_control_count,
        CASE 
            WHEN v_keding_control_count <= v_changhui_control_count THEN '✓ 通过'
            ELSE '✗ 失败'
        END
    UNION ALL
    SELECT 
        '固件',
        v_keding_firmware_count,
        v_changhui_firmware_count,
        CASE 
            WHEN v_keding_firmware_count <= v_changhui_firmware_count THEN '✓ 通过'
            ELSE '✗ 失败'
        END
    UNION ALL
    SELECT 
        '升级任务',
        v_keding_upgrade_count,
        v_changhui_upgrade_count,
        CASE 
            WHEN v_keding_upgrade_count <= v_changhui_upgrade_count THEN '✓ 通过'
            ELSE '✗ 失败'
        END;
        
END//

DELIMITER ;

CALL sp_validate_migration_counts();
DROP PROCEDURE IF EXISTS sp_validate_migration_counts;

-- ----------------------------
-- 4. 检查遗漏的设备
-- ----------------------------
SELECT '' AS blank;
SELECT '=== 4. 遗漏数据检查 ===' AS section;

-- 创建临时存储过程检查遗漏数据
DELIMITER //

DROP PROCEDURE IF EXISTS sp_check_missing_data//

CREATE PROCEDURE sp_check_missing_data()
BEGIN
    DECLARE v_table_exists INT DEFAULT 0;
    DECLARE v_missing_count INT DEFAULT 0;
    
    -- 检查遗漏的设备
    SELECT COUNT(*) INTO v_table_exists 
    FROM information_schema.tables 
    WHERE table_schema = DATABASE() AND table_name = 'iot_keding_device';
    
    IF v_table_exists > 0 THEN
        SELECT COUNT(*) INTO v_missing_count
        FROM iot_keding_device kd
        WHERE NOT EXISTS (
            SELECT 1 FROM changhui_device cd 
            WHERE cd.station_code = kd.station_code
        );
        
        SELECT 
            '设备' AS data_type,
            v_missing_count AS missing_count,
            CASE 
                WHEN v_missing_count = 0 THEN '✓ 无遗漏'
                ELSE CONCAT('✗ 遗漏 ', v_missing_count, ' 条')
            END AS status;
    ELSE
        SELECT '设备' AS data_type, 0 AS missing_count, '○ 源表不存在' AS status;
    END IF;
    
    -- 检查遗漏的报警
    SELECT COUNT(*) INTO v_table_exists 
    FROM information_schema.tables 
    WHERE table_schema = DATABASE() AND table_name = 'iot_keding_alarm';
    
    IF v_table_exists > 0 THEN
        SELECT COUNT(*) INTO v_missing_count
        FROM iot_keding_alarm ka
        WHERE NOT EXISTS (
            SELECT 1 FROM changhui_alarm ca 
            WHERE ca.station_code = ka.station_code 
              AND ca.alarm_type = ka.alarm_type 
              AND ca.alarm_time = ka.alarm_time
        );
        
        SELECT 
            '报警' AS data_type,
            v_missing_count AS missing_count,
            CASE 
                WHEN v_missing_count = 0 THEN '✓ 无遗漏'
                ELSE CONCAT('✗ 遗漏 ', v_missing_count, ' 条')
            END AS status;
    ELSE
        SELECT '报警' AS data_type, 0 AS missing_count, '○ 源表不存在' AS status;
    END IF;
    
    -- 检查遗漏的固件
    SELECT COUNT(*) INTO v_table_exists 
    FROM information_schema.tables 
    WHERE table_schema = DATABASE() AND table_name = 'iot_keding_firmware';
    
    IF v_table_exists > 0 THEN
        SELECT COUNT(*) INTO v_missing_count
        FROM iot_keding_firmware kf
        WHERE NOT EXISTS (
            SELECT 1 FROM changhui_firmware cf 
            WHERE cf.name = kf.name AND cf.version = kf.version
        );
        
        SELECT 
            '固件' AS data_type,
            v_missing_count AS missing_count,
            CASE 
                WHEN v_missing_count = 0 THEN '✓ 无遗漏'
                ELSE CONCAT('✗ 遗漏 ', v_missing_count, ' 条')
            END AS status;
    ELSE
        SELECT '固件' AS data_type, 0 AS missing_count, '○ 源表不存在' AS status;
    END IF;
    
END//

DELIMITER ;

CALL sp_check_missing_data();
DROP PROCEDURE IF EXISTS sp_check_missing_data;

-- ----------------------------
-- 5. 总体验证结果
-- ----------------------------
SELECT '' AS blank;
SELECT '=== 5. 总体验证结果 ===' AS section;

SELECT 
    '迁移验证' AS check_type,
    CASE 
        WHEN (
            SELECT COUNT(*) FROM information_schema.tables 
            WHERE table_schema = DATABASE() AND table_name = 'changhui_device'
        ) > 0 THEN '✓ 目标表已创建'
        ELSE '✗ 目标表未创建'
    END AS status;

SELECT '========================================' AS separator;
SELECT '  验证完成' AS title;
SELECT '========================================' AS separator;
