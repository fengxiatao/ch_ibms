-- =============================================
-- 完整数据迁移脚本：从科鼎(Keding)表迁移到长辉(Changhui)表
-- 版本: V6.2
-- 
-- 功能说明:
-- 1. 检查源表是否存在，不存在则跳过
-- 2. 支持增量迁移（不会重复插入已存在的数据）
-- 3. 迁移所有相关表：设备、报警、控制日志、固件、升级任务
-- 4. 提供迁移前后的数据统计
-- 5. 支持回滚（通过备份表）
--
-- 执行前提:
-- 1. 已执行 V6.0__create_changhui_tables.sql 创建目标表
-- 2. 数据库用户具有 CREATE PROCEDURE, INSERT, SELECT 权限
--
-- 使用方法:
-- 1. 直接执行此脚本进行迁移
-- 2. 如需回滚，执行 V6.2__rollback_changhui_to_keding.sql
-- =============================================

-- 设置字符集
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 迁移前统计
-- ----------------------------
SELECT '=== 迁移前数据统计 ===' AS info;
SELECT NOW() AS migration_start_time;

-- 统计源表数据量（如果存在）
SELECT 
    'iot_keding_device' AS table_name,
    IFNULL((SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'iot_keding_device'), 0) AS table_exists,
    IFNULL((SELECT COUNT(*) FROM iot_keding_device), 0) AS record_count
UNION ALL
SELECT 
    'iot_keding_alarm',
    IFNULL((SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'iot_keding_alarm'), 0),
    IFNULL((SELECT COUNT(*) FROM iot_keding_alarm), 0)
UNION ALL
SELECT 
    'iot_keding_control_log',
    IFNULL((SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'iot_keding_control_log'), 0),
    IFNULL((SELECT COUNT(*) FROM iot_keding_control_log), 0)
UNION ALL
SELECT 
    'iot_keding_firmware',
    IFNULL((SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'iot_keding_firmware'), 0),
    IFNULL((SELECT COUNT(*) FROM iot_keding_firmware), 0)
UNION ALL
SELECT 
    'iot_keding_upgrade_task',
    IFNULL((SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'iot_keding_upgrade_task'), 0),
    IFNULL((SELECT COUNT(*) FROM iot_keding_upgrade_task), 0);

-- ----------------------------
-- 创建迁移存储过程
-- ----------------------------
DELIMITER //

DROP PROCEDURE IF EXISTS sp_migrate_keding_to_changhui//

CREATE PROCEDURE sp_migrate_keding_to_changhui()
BEGIN
    DECLARE v_table_exists INT DEFAULT 0;
    DECLARE v_migrated_count INT DEFAULT 0;
    DECLARE v_total_migrated INT DEFAULT 0;
    DECLARE v_error_message VARCHAR(500);
    
    -- 错误处理
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        GET DIAGNOSTICS CONDITION 1 v_error_message = MESSAGE_TEXT;
        SELECT CONCAT('迁移失败: ', v_error_message) AS error;
        ROLLBACK;
    END;
    
    START TRANSACTION;
    
    -- ========================================
    -- 1. 迁移设备数据
    -- ========================================
    SELECT COUNT(*) INTO v_table_exists 
    FROM information_schema.tables 
    WHERE table_schema = DATABASE() AND table_name = 'iot_keding_device';
    
    IF v_table_exists > 0 THEN
        -- 插入不存在的设备记录
        INSERT INTO `changhui_device` (
            `station_code`, `device_name`, `device_type`, 
            `province_code`, `management_code`, `station_code_part`,
            `pile_front`, `pile_back`, `manufacturer`, `sequence_no`,
            `tea_key`, `password`, `status`, `last_heartbeat`,
            `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`
        )
        SELECT 
            kd.`station_code`, kd.`device_name`, kd.`device_type`,
            kd.`province_code`, kd.`management_code`, kd.`station_code_part`,
            kd.`pile_front`, kd.`pile_back`, kd.`manufacturer`, kd.`sequence_no`,
            kd.`tea_key`, kd.`password`, kd.`status`, kd.`last_heartbeat`,
            kd.`creator`, kd.`create_time`, kd.`updater`, kd.`update_time`, kd.`deleted`, kd.`tenant_id`
        FROM `iot_keding_device` kd
        WHERE NOT EXISTS (
            SELECT 1 FROM `changhui_device` cd 
            WHERE cd.station_code = kd.station_code AND cd.deleted = kd.deleted
        );
        
        SET v_migrated_count = ROW_COUNT();
        SET v_total_migrated = v_total_migrated + v_migrated_count;
        SELECT CONCAT('✓ 设备数据迁移完成，新增记录数: ', v_migrated_count) AS migration_result;
    ELSE
        SELECT '○ 源表 iot_keding_device 不存在，跳过设备数据迁移' AS migration_result;
    END IF;
    
    -- ========================================
    -- 2. 迁移报警数据
    -- ========================================
    SELECT COUNT(*) INTO v_table_exists 
    FROM information_schema.tables 
    WHERE table_schema = DATABASE() AND table_name = 'iot_keding_alarm';
    
    IF v_table_exists > 0 THEN
        INSERT INTO `changhui_alarm` (
            `device_id`, `station_code`, `alarm_type`, `alarm_value`,
            `alarm_time`, `status`, `ack_time`, `ack_user`,
            `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`
        )
        SELECT 
            -- 查找对应的changhui_device ID
            (SELECT cd.id FROM changhui_device cd WHERE cd.station_code = ka.station_code LIMIT 1) AS device_id,
            ka.`station_code`, ka.`alarm_type`, ka.`alarm_value`,
            ka.`alarm_time`, ka.`status`, ka.`ack_time`, ka.`ack_user`,
            ka.`creator`, ka.`create_time`, ka.`updater`, ka.`update_time`, ka.`deleted`, ka.`tenant_id`
        FROM `iot_keding_alarm` ka
        WHERE NOT EXISTS (
            SELECT 1 FROM `changhui_alarm` ca 
            WHERE ca.station_code = ka.station_code 
              AND ca.alarm_type = ka.alarm_type 
              AND ca.alarm_time = ka.alarm_time
        );
        
        SET v_migrated_count = ROW_COUNT();
        SET v_total_migrated = v_total_migrated + v_migrated_count;
        SELECT CONCAT('✓ 报警数据迁移完成，新增记录数: ', v_migrated_count) AS migration_result;
    ELSE
        SELECT '○ 源表 iot_keding_alarm 不存在，跳过报警数据迁移' AS migration_result;
    END IF;
    
    -- ========================================
    -- 3. 迁移控制日志数据
    -- ========================================
    SELECT COUNT(*) INTO v_table_exists 
    FROM information_schema.tables 
    WHERE table_schema = DATABASE() AND table_name = 'iot_keding_control_log';
    
    IF v_table_exists > 0 THEN
        INSERT INTO `changhui_control_log` (
            `device_id`, `station_code`, `control_type`, `control_params`,
            `result`, `error_message`, `operator`, `operate_time`,
            `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`
        )
        SELECT 
            (SELECT cd.id FROM changhui_device cd WHERE cd.station_code = kcl.station_code LIMIT 1) AS device_id,
            kcl.`station_code`, kcl.`control_type`, kcl.`control_params`,
            kcl.`result`, kcl.`error_message`, kcl.`operator`, kcl.`operate_time`,
            kcl.`creator`, kcl.`create_time`, kcl.`updater`, kcl.`update_time`, kcl.`deleted`, kcl.`tenant_id`
        FROM `iot_keding_control_log` kcl
        WHERE NOT EXISTS (
            SELECT 1 FROM `changhui_control_log` ccl 
            WHERE ccl.station_code = kcl.station_code 
              AND ccl.control_type = kcl.control_type 
              AND ccl.operate_time = kcl.operate_time
        );
        
        SET v_migrated_count = ROW_COUNT();
        SET v_total_migrated = v_total_migrated + v_migrated_count;
        SELECT CONCAT('✓ 控制日志数据迁移完成，新增记录数: ', v_migrated_count) AS migration_result;
    ELSE
        SELECT '○ 源表 iot_keding_control_log 不存在，跳过控制日志数据迁移' AS migration_result;
    END IF;
    
    -- ========================================
    -- 4. 迁移固件数据
    -- ========================================
    SELECT COUNT(*) INTO v_table_exists 
    FROM information_schema.tables 
    WHERE table_schema = DATABASE() AND table_name = 'iot_keding_firmware';
    
    IF v_table_exists > 0 THEN
        INSERT INTO `changhui_firmware` (
            `name`, `version`, `device_type`, `file_path`,
            `file_size`, `file_md5`, `description`,
            `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`
        )
        SELECT 
            kf.`name`, kf.`version`, kf.`device_type`, kf.`file_path`,
            kf.`file_size`, kf.`file_md5`, kf.`description`,
            kf.`creator`, kf.`create_time`, kf.`updater`, kf.`update_time`, kf.`deleted`, kf.`tenant_id`
        FROM `iot_keding_firmware` kf
        WHERE NOT EXISTS (
            SELECT 1 FROM `changhui_firmware` cf 
            WHERE cf.name = kf.name AND cf.version = kf.version AND cf.deleted = kf.deleted
        );
        
        SET v_migrated_count = ROW_COUNT();
        SET v_total_migrated = v_total_migrated + v_migrated_count;
        SELECT CONCAT('✓ 固件数据迁移完成，新增记录数: ', v_migrated_count) AS migration_result;
    ELSE
        SELECT '○ 源表 iot_keding_firmware 不存在，跳过固件数据迁移' AS migration_result;
    END IF;
    
    -- ========================================
    -- 5. 迁移升级任务数据
    -- ========================================
    SELECT COUNT(*) INTO v_table_exists 
    FROM information_schema.tables 
    WHERE table_schema = DATABASE() AND table_name = 'iot_keding_upgrade_task';
    
    IF v_table_exists > 0 THEN
        INSERT INTO `changhui_upgrade_task` (
            `device_id`, `station_code`, `firmware_id`, `firmware_version`,
            `upgrade_mode`, `firmware_url`, `status`, `progress`,
            `total_frames`, `sent_frames`, `retry_count`,
            `start_time`, `end_time`, `error_message`,
            `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`
        )
        SELECT 
            (SELECT cd.id FROM changhui_device cd WHERE cd.station_code = kut.station_code LIMIT 1) AS device_id,
            kut.`station_code`, 
            -- 查找对应的changhui_firmware ID
            (SELECT cf.id FROM changhui_firmware cf 
             WHERE cf.version = kut.firmware_version 
             ORDER BY cf.id DESC LIMIT 1) AS firmware_id,
            kut.`firmware_version`,
            0 AS `upgrade_mode`, -- 默认TCP帧传输模式
            NULL AS `firmware_url`,
            kut.`status`, kut.`progress`,
            kut.`total_frames`, kut.`sent_frames`, kut.`retry_count`,
            kut.`start_time`, kut.`end_time`, kut.`error_message`,
            kut.`creator`, kut.`create_time`, kut.`updater`, kut.`update_time`, kut.`deleted`, kut.`tenant_id`
        FROM `iot_keding_upgrade_task` kut
        WHERE NOT EXISTS (
            SELECT 1 FROM `changhui_upgrade_task` cut 
            WHERE cut.station_code = kut.station_code 
              AND cut.firmware_version = kut.firmware_version
              AND cut.create_time = kut.create_time
        );
        
        SET v_migrated_count = ROW_COUNT();
        SET v_total_migrated = v_total_migrated + v_migrated_count;
        SELECT CONCAT('✓ 升级任务数据迁移完成，新增记录数: ', v_migrated_count) AS migration_result;
    ELSE
        SELECT '○ 源表 iot_keding_upgrade_task 不存在，跳过升级任务数据迁移' AS migration_result;
    END IF;
    
    COMMIT;
    
    -- 输出总计
    SELECT CONCAT('=== 迁移完成，总计新增记录数: ', v_total_migrated, ' ===') AS summary;
    
END//

DELIMITER ;

-- ----------------------------
-- 执行迁移
-- ----------------------------
CALL sp_migrate_keding_to_changhui();

-- ----------------------------
-- 迁移后统计
-- ----------------------------
SELECT '=== 迁移后数据统计 ===' AS info;
SELECT NOW() AS migration_end_time;

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
DROP PROCEDURE IF EXISTS sp_migrate_keding_to_changhui;

SET FOREIGN_KEY_CHECKS = 1;

SELECT '=== 迁移脚本执行完成 ===' AS info;
