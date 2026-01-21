-- =============================================
-- 修复 iot_access_person_device_auth 表缺少 version 字段
-- 版本: V1.6
-- 说明: 添加乐观锁版本号字段，用于并发更新时的冲突检测
-- =============================================

-- 添加 version 字段（乐观锁）
SET @exist_version := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'iot_access_person_device_auth' AND COLUMN_NAME = 'version');
SET @sql_add_version := IF(@exist_version = 0, 
    'ALTER TABLE `iot_access_person_device_auth` ADD COLUMN `version` int NOT NULL DEFAULT 0 COMMENT ''乐观锁版本号'' AFTER `credential_hash`',
    'SELECT ''version column already exists''');
PREPARE stmt FROM @sql_add_version;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
