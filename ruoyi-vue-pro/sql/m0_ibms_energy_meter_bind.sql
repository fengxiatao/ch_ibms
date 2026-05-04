-- M0 业务关联试点：智慧能源 → IBMS 台账
-- 1) ibms_energy_meter 加 ibms_device_id 外键列 + 索引
-- 2) ibms_channel 补 business / system_type 索引（便于业务模块按 business 查询）
-- 执行后请保留 server-mvn.log 验证通过。

SET NAMES utf8mb4;
USE ch_ibms;

-- ---------- 1. ibms_energy_meter 加外键列 ----------
SET @col_exists := (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ibms_energy_meter' AND COLUMN_NAME = 'ibms_device_id'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE ibms_energy_meter ADD COLUMN ibms_device_id BIGINT NULL COMMENT ''IBMS 设备台账外键（ibms_device.id）'' AFTER id',
  'SELECT ''ibms_energy_meter.ibms_device_id already exists'' AS msg');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @idx_exists := (
  SELECT COUNT(*) FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ibms_energy_meter' AND INDEX_NAME = 'idx_ibms_device_id'
);
SET @sql := IF(@idx_exists = 0,
  'ALTER TABLE ibms_energy_meter ADD INDEX idx_ibms_device_id (ibms_device_id)',
  'SELECT ''ibms_energy_meter.idx_ibms_device_id already exists'' AS msg');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ---------- 2. ibms_channel 业务查询索引 ----------
SET @idx_exists := (
  SELECT COUNT(*) FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ibms_channel' AND INDEX_NAME = 'idx_business'
);
SET @sql := IF(@idx_exists = 0,
  'ALTER TABLE ibms_channel ADD INDEX idx_business (business)',
  'SELECT ''ibms_channel.idx_business already exists'' AS msg');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @idx_exists := (
  SELECT COUNT(*) FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ibms_channel' AND INDEX_NAME = 'idx_system_type'
);
SET @sql := IF(@idx_exists = 0,
  'ALTER TABLE ibms_channel ADD INDEX idx_system_type (system_type)',
  'SELECT ''ibms_channel.idx_system_type already exists'' AS msg');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SELECT 'M0 DDL done' AS result;
