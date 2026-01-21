-- =============================================
-- 数据迁移脚本：从科鼎(Keding)表迁移到长辉(Changhui)表
-- 执行前请确保已创建changhui相关表（V6.0__create_changhui_tables.sql）
-- =============================================

-- ----------------------------
-- 1. 迁移设备数据
-- ----------------------------
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
FROM `iot_keding_device`
WHERE NOT EXISTS (
    SELECT 1 FROM `changhui_device` cd 
    WHERE cd.station_code = `iot_keding_device`.station_code
);

-- ----------------------------
-- 2. 迁移报警数据
-- ----------------------------
INSERT INTO `changhui_alarm` (
    `id`, `device_id`, `station_code`, `alarm_type`, `alarm_value`,
    `alarm_time`, `status`, `ack_time`, `ack_user`,
    `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`
)
SELECT 
    `id`, `device_id`, `station_code`, `alarm_type`, `alarm_value`,
    `alarm_time`, `status`, `ack_time`, `ack_user`,
    `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`
FROM `iot_keding_alarm`
WHERE NOT EXISTS (
    SELECT 1 FROM `changhui_alarm` ca 
    WHERE ca.id = `iot_keding_alarm`.id
);

-- ----------------------------
-- 3. 迁移控制日志数据
-- ----------------------------
INSERT INTO `changhui_control_log` (
    `id`, `device_id`, `station_code`, `control_type`, `control_params`,
    `result`, `error_message`, `operator`, `operate_time`,
    `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`
)
SELECT 
    `id`, `device_id`, `station_code`, `control_type`, `control_params`,
    `result`, `error_message`, `operator`, `operate_time`,
    `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`
FROM `iot_keding_control_log`
WHERE NOT EXISTS (
    SELECT 1 FROM `changhui_control_log` ccl 
    WHERE ccl.id = `iot_keding_control_log`.id
);

-- ----------------------------
-- 4. 迁移固件数据
-- ----------------------------
INSERT INTO `changhui_firmware` (
    `id`, `name`, `version`, `device_type`, `file_path`,
    `file_size`, `file_md5`, `description`,
    `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`
)
SELECT 
    `id`, `name`, `version`, `device_type`, `file_path`,
    `file_size`, `file_md5`, `description`,
    `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`
FROM `iot_keding_firmware`
WHERE NOT EXISTS (
    SELECT 1 FROM `changhui_firmware` cf 
    WHERE cf.id = `iot_keding_firmware`.id
);

-- ----------------------------
-- 5. 迁移升级任务数据
-- ----------------------------
INSERT INTO `changhui_upgrade_task` (
    `id`, `device_id`, `station_code`, `firmware_id`, `firmware_version`,
    `upgrade_mode`, `firmware_url`, `status`, `progress`,
    `total_frames`, `sent_frames`, `retry_count`,
    `start_time`, `end_time`, `error_message`,
    `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`
)
SELECT 
    `id`, `device_id`, `station_code`, `firmware_id`, `firmware_version`,
    0 AS `upgrade_mode`, -- 默认TCP帧传输模式
    NULL AS `firmware_url`, -- 旧表没有此字段
    `status`, `progress`,
    `total_frames`, `sent_frames`, `retry_count`,
    `start_time`, `end_time`, `error_message`,
    `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`
FROM `iot_keding_upgrade_task`
WHERE NOT EXISTS (
    SELECT 1 FROM `changhui_upgrade_task` cut 
    WHERE cut.id = `iot_keding_upgrade_task`.id
);

-- ----------------------------
-- 迁移完成后的统计信息
-- ----------------------------
SELECT '迁移统计' AS info;
SELECT 'changhui_device' AS table_name, COUNT(*) AS record_count FROM `changhui_device`
UNION ALL
SELECT 'changhui_alarm', COUNT(*) FROM `changhui_alarm`
UNION ALL
SELECT 'changhui_control_log', COUNT(*) FROM `changhui_control_log`
UNION ALL
SELECT 'changhui_firmware', COUNT(*) FROM `changhui_firmware`
UNION ALL
SELECT 'changhui_upgrade_task', COUNT(*) FROM `changhui_upgrade_task`;
