-- =============================================
-- 回滚脚本：从长辉(Changhui)表回滚到科鼎(Keding)表
-- 版本: V6.2
-- 
-- 功能说明:
-- 1. 清空changhui表中从keding迁移过来的数据
-- 2. 保留changhui表中新增的数据（如果有的话）
-- 3. 不会删除changhui表结构
--
-- 注意事项:
-- 1. 此脚本会删除changhui表中的数据，请谨慎执行
-- 2. 建议在执行前备份数据
-- 3. 如果changhui表中有新增数据，这些数据也会被删除
-- =============================================

-- 设置字符集
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 回滚前统计
-- ----------------------------
SELECT '=== 回滚前数据统计 ===' AS info;
SELECT NOW() AS rollback_start_time;

SELECT 
    'changhui_device' AS table_name, COUNT(*) AS record_count FROM `changhui_device`
UNION ALL
SELECT 'changhui_data', COUNT(*) FROM `changhui_data`
UNION ALL
SELECT 'changhui_alarm', COUNT(*) FROM `changhui_alarm`
UNION ALL
SELECT 'changhui_control_log', COUNT(*) FROM `changhui_control_log`
UNION ALL
SELECT 'changhui_firmware', COUNT(*) FROM `changhui_firmware`
UNION ALL
SELECT 'changhui_upgrade_task', COUNT(*) FROM `changhui_upgrade_task`;

-- ----------------------------
-- 创建回滚存储过程
-- ----------------------------
DELIMITER //

DROP PROCEDURE IF EXISTS sp_rollback_changhui_to_keding//

CREATE PROCEDURE sp_rollback_changhui_to_keding()
BEGIN
    DECLARE v_table_exists INT DEFAULT 0;
    DECLARE v_deleted_count INT DEFAULT 0;
    DECLARE v_error_message VARCHAR(500);
    
    -- 错误处理
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        GET DIAGNOSTICS CONDITION 1 v_error_message = MESSAGE_TEXT;
        SELECT CONCAT('回滚失败: ', v_error_message) AS error;
        ROLLBACK;
    END;
    
    START TRANSACTION;
    
    -- ========================================
    -- 1. 删除从keding迁移的升级任务数据
    -- ========================================
    SELECT COUNT(*) INTO v_table_exists 
    FROM information_schema.tables 
    WHERE table_schema = DATABASE() AND table_name = 'iot_keding_upgrade_task';
    
    IF v_table_exists > 0 THEN
        DELETE cut FROM `changhui_upgrade_task` cut
        INNER JOIN `iot_keding_upgrade_task` kut 
            ON cut.station_code = kut.station_code 
            AND cut.firmware_version = kut.firmware_version
            AND cut.create_time = kut.create_time;
        
        SET v_deleted_count = ROW_COUNT();
        SELECT CONCAT('✓ 升级任务数据回滚完成，删除记录数: ', v_deleted_count) AS rollback_result;
    ELSE
        SELECT '○ 源表 iot_keding_upgrade_task 不存在，跳过升级任务数据回滚' AS rollback_result;
    END IF;
    
    -- ========================================
    -- 2. 删除从keding迁移的固件数据
    -- ========================================
    SELECT COUNT(*) INTO v_table_exists 
    FROM information_schema.tables 
    WHERE table_schema = DATABASE() AND table_name = 'iot_keding_firmware';
    
    IF v_table_exists > 0 THEN
        DELETE cf FROM `changhui_firmware` cf
        INNER JOIN `iot_keding_firmware` kf 
            ON cf.name = kf.name 
            AND cf.version = kf.version 
            AND cf.deleted = kf.deleted;
        
        SET v_deleted_count = ROW_COUNT();
        SELECT CONCAT('✓ 固件数据回滚完成，删除记录数: ', v_deleted_count) AS rollback_result;
    ELSE
        SELECT '○ 源表 iot_keding_firmware 不存在，跳过固件数据回滚' AS rollback_result;
    END IF;
    
    -- ========================================
    -- 3. 删除从keding迁移的控制日志数据
    -- ========================================
    SELECT COUNT(*) INTO v_table_exists 
    FROM information_schema.tables 
    WHERE table_schema = DATABASE() AND table_name = 'iot_keding_control_log';
    
    IF v_table_exists > 0 THEN
        DELETE ccl FROM `changhui_control_log` ccl
        INNER JOIN `iot_keding_control_log` kcl 
            ON ccl.station_code = kcl.station_code 
            AND ccl.control_type = kcl.control_type 
            AND ccl.operate_time = kcl.operate_time;
        
        SET v_deleted_count = ROW_COUNT();
        SELECT CONCAT('✓ 控制日志数据回滚完成，删除记录数: ', v_deleted_count) AS rollback_result;
    ELSE
        SELECT '○ 源表 iot_keding_control_log 不存在，跳过控制日志数据回滚' AS rollback_result;
    END IF;
    
    -- ========================================
    -- 4. 删除从keding迁移的报警数据
    -- ========================================
    SELECT COUNT(*) INTO v_table_exists 
    FROM information_schema.tables 
    WHERE table_schema = DATABASE() AND table_name = 'iot_keding_alarm';
    
    IF v_table_exists > 0 THEN
        DELETE ca FROM `changhui_alarm` ca
        INNER JOIN `iot_keding_alarm` ka 
            ON ca.station_code = ka.station_code 
            AND ca.alarm_type = ka.alarm_type 
            AND ca.alarm_time = ka.alarm_time;
        
        SET v_deleted_count = ROW_COUNT();
        SELECT CONCAT('✓ 报警数据回滚完成，删除记录数: ', v_deleted_count) AS rollback_result;
    ELSE
        SELECT '○ 源表 iot_keding_alarm 不存在，跳过报警数据回滚' AS rollback_result;
    END IF;
    
    -- ========================================
    -- 5. 删除从keding迁移的设备数据
    -- ========================================
    SELECT COUNT(*) INTO v_table_exists 
    FROM information_schema.tables 
    WHERE table_schema = DATABASE() AND table_name = 'iot_keding_device';
    
    IF v_table_exists > 0 THEN
        DELETE cd FROM `changhui_device` cd
        INNER JOIN `iot_keding_device` kd 
            ON cd.station_code = kd.station_code 
            AND cd.deleted = kd.deleted;
        
        SET v_deleted_count = ROW_COUNT();
        SELECT CONCAT('✓ 设备数据回滚完成，删除记录数: ', v_deleted_count) AS rollback_result;
    ELSE
        SELECT '○ 源表 iot_keding_device 不存在，跳过设备数据回滚' AS rollback_result;
    END IF;
    
    COMMIT;
    
    SELECT '=== 回滚完成 ===' AS summary;
    
END//

DELIMITER ;

-- ----------------------------
-- 执行回滚
-- ----------------------------
CALL sp_rollback_changhui_to_keding();

-- ----------------------------
-- 回滚后统计
-- ----------------------------
SELECT '=== 回滚后数据统计 ===' AS info;
SELECT NOW() AS rollback_end_time;

SELECT 
    'changhui_device' AS table_name, COUNT(*) AS record_count FROM `changhui_device`
UNION ALL
SELECT 'changhui_data', COUNT(*) FROM `changhui_data`
UNION ALL
SELECT 'changhui_alarm', COUNT(*) FROM `changhui_alarm`
UNION ALL
SELECT 'changhui_control_log', COUNT(*) FROM `changhui_control_log`
UNION ALL
SELECT 'changhui_firmware', COUNT(*) FROM `changhui_firmware`
UNION ALL
SELECT 'changhui_upgrade_task', COUNT(*) FROM `changhui_upgrade_task`;

-- ----------------------------
-- 清理存储过程
-- ----------------------------
DROP PROCEDURE IF EXISTS sp_rollback_changhui_to_keding;

SET FOREIGN_KEY_CHECKS = 1;

SELECT '=== 回滚脚本执行完成 ===' AS info;
