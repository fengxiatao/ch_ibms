-- =====================================================
-- 修复 iot_access_operation_log 表缺失字段
-- 执行此脚本前请确保已备份数据库
-- =====================================================

-- 添加 device_name 字段（如果不存在）
SET @exist := (SELECT COUNT(*) FROM information_schema.COLUMNS 
               WHERE TABLE_SCHEMA = DATABASE() 
               AND TABLE_NAME = 'iot_access_operation_log' 
               AND COLUMN_NAME = 'device_name');
SET @sql := IF(@exist = 0, 
    'ALTER TABLE `iot_access_operation_log` ADD COLUMN `device_name` varchar(128) DEFAULT NULL COMMENT ''设备名称'' AFTER `device_id`',
    'SELECT ''Column device_name already exists''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加 result_desc 字段（如果不存在）
SET @exist := (SELECT COUNT(*) FROM information_schema.COLUMNS 
               WHERE TABLE_SCHEMA = DATABASE() 
               AND TABLE_NAME = 'iot_access_operation_log' 
               AND COLUMN_NAME = 'result_desc');
SET @sql := IF(@exist = 0, 
    'ALTER TABLE `iot_access_operation_log` ADD COLUMN `result_desc` varchar(500) DEFAULT NULL COMMENT ''结果描述'' AFTER `result`',
    'SELECT ''Column result_desc already exists''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加授权操作扩展字段（如果不存在）
SET @exist := (SELECT COUNT(*) FROM information_schema.COLUMNS 
               WHERE TABLE_SCHEMA = DATABASE() 
               AND TABLE_NAME = 'iot_access_operation_log' 
               AND COLUMN_NAME = 'target_person_id');
SET @sql := IF(@exist = 0, 
    'ALTER TABLE `iot_access_operation_log` ADD COLUMN `target_person_id` bigint DEFAULT NULL COMMENT ''目标人员ID（授权操作时使用）'' AFTER `request_params`',
    'SELECT ''Column target_person_id already exists''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @exist := (SELECT COUNT(*) FROM information_schema.COLUMNS 
               WHERE TABLE_SCHEMA = DATABASE() 
               AND TABLE_NAME = 'iot_access_operation_log' 
               AND COLUMN_NAME = 'target_person_code');
SET @sql := IF(@exist = 0, 
    'ALTER TABLE `iot_access_operation_log` ADD COLUMN `target_person_code` varchar(64) DEFAULT NULL COMMENT ''目标人员编号'' AFTER `target_person_id`',
    'SELECT ''Column target_person_code already exists''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @exist := (SELECT COUNT(*) FROM information_schema.COLUMNS 
               WHERE TABLE_SCHEMA = DATABASE() 
               AND TABLE_NAME = 'iot_access_operation_log' 
               AND COLUMN_NAME = 'target_person_name');
SET @sql := IF(@exist = 0, 
    'ALTER TABLE `iot_access_operation_log` ADD COLUMN `target_person_name` varchar(100) DEFAULT NULL COMMENT ''目标人员姓名'' AFTER `target_person_code`',
    'SELECT ''Column target_person_name already exists''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @exist := (SELECT COUNT(*) FROM information_schema.COLUMNS 
               WHERE TABLE_SCHEMA = DATABASE() 
               AND TABLE_NAME = 'iot_access_operation_log' 
               AND COLUMN_NAME = 'permission_group_id');
SET @sql := IF(@exist = 0, 
    'ALTER TABLE `iot_access_operation_log` ADD COLUMN `permission_group_id` bigint DEFAULT NULL COMMENT ''权限组ID'' AFTER `target_person_name`',
    'SELECT ''Column permission_group_id already exists''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @exist := (SELECT COUNT(*) FROM information_schema.COLUMNS 
               WHERE TABLE_SCHEMA = DATABASE() 
               AND TABLE_NAME = 'iot_access_operation_log' 
               AND COLUMN_NAME = 'permission_group_name');
SET @sql := IF(@exist = 0, 
    'ALTER TABLE `iot_access_operation_log` ADD COLUMN `permission_group_name` varchar(100) DEFAULT NULL COMMENT ''权限组名称'' AFTER `permission_group_id`',
    'SELECT ''Column permission_group_name already exists''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @exist := (SELECT COUNT(*) FROM information_schema.COLUMNS 
               WHERE TABLE_SCHEMA = DATABASE() 
               AND TABLE_NAME = 'iot_access_operation_log' 
               AND COLUMN_NAME = 'auth_task_id');
SET @sql := IF(@exist = 0, 
    'ALTER TABLE `iot_access_operation_log` ADD COLUMN `auth_task_id` bigint DEFAULT NULL COMMENT ''授权任务ID'' AFTER `permission_group_name`',
    'SELECT ''Column auth_task_id already exists''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @exist := (SELECT COUNT(*) FROM information_schema.COLUMNS 
               WHERE TABLE_SCHEMA = DATABASE() 
               AND TABLE_NAME = 'iot_access_operation_log' 
               AND COLUMN_NAME = 'credential_types');
SET @sql := IF(@exist = 0, 
    'ALTER TABLE `iot_access_operation_log` ADD COLUMN `credential_types` varchar(200) DEFAULT NULL COMMENT ''凭证类型列表（逗号分隔）'' AFTER `auth_task_id`',
    'SELECT ''Column credential_types already exists''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @exist := (SELECT COUNT(*) FROM information_schema.COLUMNS 
               WHERE TABLE_SCHEMA = DATABASE() 
               AND TABLE_NAME = 'iot_access_operation_log' 
               AND COLUMN_NAME = 'success_credential_count');
SET @sql := IF(@exist = 0, 
    'ALTER TABLE `iot_access_operation_log` ADD COLUMN `success_credential_count` int DEFAULT NULL COMMENT ''成功的凭证类型数量'' AFTER `credential_types`',
    'SELECT ''Column success_credential_count already exists''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @exist := (SELECT COUNT(*) FROM information_schema.COLUMNS 
               WHERE TABLE_SCHEMA = DATABASE() 
               AND TABLE_NAME = 'iot_access_operation_log' 
               AND COLUMN_NAME = 'failed_credential_count');
SET @sql := IF(@exist = 0, 
    'ALTER TABLE `iot_access_operation_log` ADD COLUMN `failed_credential_count` int DEFAULT NULL COMMENT ''失败的凭证类型数量'' AFTER `success_credential_count`',
    'SELECT ''Column failed_credential_count already exists''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @exist := (SELECT COUNT(*) FROM information_schema.COLUMNS 
               WHERE TABLE_SCHEMA = DATABASE() 
               AND TABLE_NAME = 'iot_access_operation_log' 
               AND COLUMN_NAME = 'sdk_error_code');
SET @sql := IF(@exist = 0, 
    'ALTER TABLE `iot_access_operation_log` ADD COLUMN `sdk_error_code` int DEFAULT NULL COMMENT ''SDK错误码'' AFTER `failed_credential_count`',
    'SELECT ''Column sdk_error_code already exists''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加索引（如果不存在）
SET @exist := (SELECT COUNT(*) FROM information_schema.STATISTICS 
               WHERE TABLE_SCHEMA = DATABASE() 
               AND TABLE_NAME = 'iot_access_operation_log' 
               AND INDEX_NAME = 'idx_target_person_id');
SET @sql := IF(@exist = 0, 
    'ALTER TABLE `iot_access_operation_log` ADD INDEX `idx_target_person_id` (`target_person_id`)',
    'SELECT ''Index idx_target_person_id already exists''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @exist := (SELECT COUNT(*) FROM information_schema.STATISTICS 
               WHERE TABLE_SCHEMA = DATABASE() 
               AND TABLE_NAME = 'iot_access_operation_log' 
               AND INDEX_NAME = 'idx_auth_task_id');
SET @sql := IF(@exist = 0, 
    'ALTER TABLE `iot_access_operation_log` ADD INDEX `idx_auth_task_id` (`auth_task_id`)',
    'SELECT ''Index idx_auth_task_id already exists''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @exist := (SELECT COUNT(*) FROM information_schema.STATISTICS 
               WHERE TABLE_SCHEMA = DATABASE() 
               AND TABLE_NAME = 'iot_access_operation_log' 
               AND INDEX_NAME = 'idx_permission_group_id');
SET @sql := IF(@exist = 0, 
    'ALTER TABLE `iot_access_operation_log` ADD INDEX `idx_permission_group_id` (`permission_group_id`)',
    'SELECT ''Index idx_permission_group_id already exists''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 验证字段是否添加成功
SELECT COLUMN_NAME, DATA_TYPE, COLUMN_COMMENT 
FROM information_schema.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE() 
AND TABLE_NAME = 'iot_access_operation_log'
ORDER BY ORDINAL_POSITION;
