-- =============================================
-- 安全数据迁移脚本：从科鼎(Keding)表迁移到长辉(Changhui)表
-- 此脚本会检查源表是否存在，如果不存在则跳过迁移
-- 执行前请确保已创建changhui相关表（V6.0__create_changhui_tables.sql）
-- =============================================

DELIMITER //

-- 创建迁移存储过程
DROP PROCEDURE IF EXISTS migrate_keding_to_changhui//

CREATE PROCEDURE migrate_keding_to_changhui()
BEGIN
    DECLARE table_exists INT DEFAULT 0;
    DECLARE migrated_count INT DEFAULT 0;
    
    -- 检查并迁移设备数据
    SELECT COUNT(*) INTO table_exists 
    FROM information_schema.tables 
    WHERE table_schema = DATABASE() AND table_name = 'iot_keding_device';
    
    IF table_exists > 0 THEN
        INSERT INTO `changhui_device` (
            `id`, `station_code`, `device_name`, `device_type`, 
            `province_code`, `management_code`, `station_code_part`,
            `pile_front`, `pile_back`, `manufacturer`, `sequence_no`,
            `tea_key`, `password`, `status`, `last_heartbeat`,
            `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`
        )
        SELECT 
            `id`, `station_code`, `device_name`, `device_type`,
            `province_code`, `management_code`, `station_code_part`,
            `pile_front`, `pile_back`, `manufacturer`, `sequence_no`,
            `tea_key`, `password`, `status`, `last_heartbeat`,
            `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`
        FROM `iot_keding_device` kd
        WHERE NOT EXISTS (
            SELECT 1 FROM `changhui_device` cd 
            WHERE cd.station_code = kd.station_code
        );
        
        SET migrated_count = ROW_COUNT();
        SELECT CONCAT('设备数据迁移完成，迁移记录数: ', migrated_count) AS migration_result;
    ELSE
        SELECT '源表 iot_keding_device 不存在，跳过设备数据迁移' AS migration_result;
    END IF;
    
    -- 检查并迁移报警数据
    SELECT COUNT(*) INTO table_exists 
    FROM information_schema.tables 
    WHERE table_schema = DATABASE() AND table_name = 'iot_keding_alarm';
    
    IF table_exists > 0 THEN
        INSERT INTO `changhui_alarm` (
            `id`, `device_id`, `station_code`, `alarm_type`, `alarm_value`,
            `alarm_time`, `status`, `ack_time`, `ack_user`,
            `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`
        )
        SELECT 
            `id`, `device_id`, `station_code`, `alarm_type`, `alarm_value`,
            `alarm_time`, `status`, `ack_time`, `ack_user`,
            `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`
        FROM `iot_keding_alarm` ka
        WHERE NOT EXISTS (
            SELECT 1 FROM `changhui_alarm` ca 
            WHERE ca.id = ka.id
        );
        
        SET migrated_count = ROW_COUNT();
        SELECT CONCAT('报警数据迁移完成，迁移记录数: ', migrated_count) AS migration_result;
    ELSE
        SELECT '源表 iot_keding_alarm 不存在，跳过报警数据迁移' AS migration_result;
    END IF;
    
    -- 检查并迁移控制日志数据
    SELECT COUNT(*) INTO table_exists 
    FROM information_schema.tables 
    WHERE table_schema = DATABASE() AND table_name = 'iot_keding_control_log';
    
    IF table_exists > 0 THEN
        INSERT INTO `changhui_control_log` (
            `id`, `device_id`, `station_code`, `control_type`, `control_params`,
            `result`, `error_message`, `operator`, `operate_time`,
            `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`
        )
        SELECT 
            `id`, `device_id`, `station_code`, `control_type`, `control_params`,
            `result`, `error_message`, `operator`, `operate_time`,
            `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`
        FROM `iot_keding_control_log` kcl
        WHERE NOT EXISTS (
            SELECT 1 FROM `changhui_control_log` ccl 
            WHERE ccl.id = kcl.id
        );
        
        SET migrated_count = ROW_COUNT();
        SELECT CONCAT('控制日志数据迁移完成，迁移记录数: ', migrated_count) AS migration_result;
    ELSE
        SELECT '源表 iot_keding_control_log 不存在，跳过控制日志数据迁移' AS migration_result;
    END IF;
    
    -- 检查并迁移固件数据
    SELECT COUNT(*) INTO table_exists 
    FROM information_schema.tables 
    WHERE table_schema = DATABASE() AND table_name = 'iot_keding_firmware';
    
    IF table_exists > 0 THEN
        INSERT INTO `changhui_firmware` (
            `id`, `name`, `version`, `device_type`, `file_path`,
            `file_size`, `file_md5`, `description`,
            `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`
        )
        SELECT 
            `id`, `name`, `version`, `device_type`, `file_path`,
            `file_size`, `file_md5`, `description`,
            `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`
        FROM `iot_keding_firmware` kf
        WHERE NOT EXISTS (
            SELECT 1 FROM `changhui_firmware` cf 
            WHERE cf.id = kf.id
        );
        
        SET migrated_count = ROW_COUNT();
        SELECT CONCAT('固件数据迁移完成，迁移记录数: ', migrated_count) AS migration_result;
    ELSE
        SELECT '源表 iot_keding_firmware 不存在，跳过固件数据迁移' AS migration_result;
    END IF;
    
    -- 检查并迁移升级任务数据
    SELECT COUNT(*) INTO table_exists 
    FROM information_schema.tables 
    WHERE table_schema = DATABASE() AND table_name = 'iot_keding_upgrade_task';
    
    IF table_exists > 0 THEN
        INSERT INTO `changhui_upgrade_task` (
            `id`, `device_id`, `station_code`, `firmware_id`, `firmware_version`,
            `upgrade_mode`, `firmware_url`, `status`, `progress`,
            `total_frames`, `sent_frames`, `retry_count`,
            `start_time`, `end_time`, `error_message`,
            `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`
        )
        SELECT 
            `id`, `device_id`, `station_code`, `firmware_id`, `firmware_version`,
            0 AS `upgrade_mode`,
            NULL AS `firmware_url`,
            `status`, `progress`,
            `total_frames`, `sent_frames`, `retry_count`,
            `start_time`, `end_time`, `error_message`,
            `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`
        FROM `iot_keding_upgrade_task` kut
        WHERE NOT EXISTS (
            SELECT 1 FROM `changhui_upgrade_task` cut 
            WHERE cut.id = kut.id
        );
        
        SET migrated_count = ROW_COUNT();
        SELECT CONCAT('升级任务数据迁移完成，迁移记录数: ', migrated_count) AS migration_result;
    ELSE
        SELECT '源表 iot_keding_upgrade_task 不存在，跳过升级任务数据迁移' AS migration_result;
    END IF;
    
    -- 输出迁移统计
    SELECT '=== 迁移完成统计 ===' AS info;
    SELECT 'changhui_device' AS table_name, COUNT(*) AS record_count FROM `changhui_device`
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
    
END//

DELIMITER ;

-- 执行迁移
CALL migrate_keding_to_changhui();

-- 清理存储过程
DROP PROCEDURE IF EXISTS migrate_keding_to_changhui;
