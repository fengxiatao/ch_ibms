-- =====================================================
-- IoT 设备事件日志租户字段迁移
-- 为 `iot_device_event_log` 增加 `tenant_id`（幂等），并根据 `ibms_device` 回填历史数据
-- =====================================================

-- 第一步：新增列（默认 0，保证兼容历史数据；幂等）
SET @tenant_id_col_exists :=
    (SELECT COUNT(*)
     FROM information_schema.columns
     WHERE table_schema = DATABASE()
       AND table_name = 'iot_device_event_log'
       AND column_name = 'tenant_id');

SET @sql :=
    IF(@tenant_id_col_exists = 0,
       'ALTER TABLE `iot_device_event_log` ADD COLUMN `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT ''租户编号'' AFTER `device_id`',
       'SELECT 1');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 第二步：补充索引，提升租户过滤查询性能（幂等）
SET @idx_exists :=
    (SELECT COUNT(*)
     FROM information_schema.statistics
     WHERE table_schema = DATABASE()
       AND table_name = 'iot_device_event_log'
       AND index_name = 'idx_tenant_id');

SET @sql :=
    IF(@idx_exists = 0,
       'CREATE INDEX `idx_tenant_id` ON `iot_device_event_log`(`tenant_id`) USING BTREE',
       'SELECT 1');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 第三步：回填 tenant_id（从设备台账推导）
UPDATE `iot_device_event_log` el
        JOIN `ibms_device` d ON el.device_id = d.id
SET el.tenant_id = d.tenant_id
WHERE el.deleted = 0
  AND d.deleted = 0;

