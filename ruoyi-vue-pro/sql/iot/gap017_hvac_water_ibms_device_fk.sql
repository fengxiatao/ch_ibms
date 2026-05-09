-- =====================================================================
-- GAP-017：ibms_hvac_device / ibms_water_device 接入 IBMS 单源
-- 主计划：docs/ibms-unified-data-source-plan.md §1.2 单源数据
-- 缺口  ：docs/ibms-bidirectional-gap.md GAP-017
-- 日期  ：2026-05-09  |  会话：v36 后续
-- =====================================================================
-- 现状  ：
--   ibms_hvac_device  36 条（tenant_id=1: 18 / tenant_id=162: 18）
--   ibms_water_device 16 条（tenant_id=1:  8 / tenant_id=162:  8）
--   两表均无 ibms_device_id 外键，且对应 device_code 在 ibms_device 中
--   不存在 → 完全脱离单源。
-- 迁移目标：
--   1) 两表加 ibms_device_id BIGINT NULL（+索引）；
--   2) 在 ibms_device 中按 (device_code, tenant_id) 创建主设备记录
--      （hvac → system_code='HV' / device_type_code='HVAC'；
--        water → system_code='WS' / device_type_code='WTR'）；
--   3) UPDATE 回填业务表的 ibms_device_id；
--   4) 验证孤儿数 = 0。
-- 风险/回滚：
--   ALTER ADD COLUMN 与 INSERT INTO ibms_device 都是可逆的，回滚见文末。
-- =====================================================================

-- ---------------------------------------------------------------------
-- Step 1 - DDL：加 ibms_device_id 列 + 索引
-- ---------------------------------------------------------------------
ALTER TABLE `ibms_hvac_device`
    ADD COLUMN `ibms_device_id` BIGINT NULL COMMENT 'IBMS 设备中台主设备 ID（ibms_device.id）' AFTER `id`,
    ADD INDEX `idx_ibms_device_id` (`ibms_device_id`);

ALTER TABLE `ibms_water_device`
    ADD COLUMN `ibms_device_id` BIGINT NULL COMMENT 'IBMS 设备中台主设备 ID（ibms_device.id）' AFTER `id`,
    ADD INDEX `idx_ibms_device_id` (`ibms_device_id`);

-- ---------------------------------------------------------------------
-- Step 2 - 在 ibms_device 中创建对应主设备记录（HVAC）
--   仅插入 (device_code, tenant_id) 在 ibms_device 中尚不存在的行
-- ---------------------------------------------------------------------
INSERT INTO `ibms_device`
    (`tenant_id`, `device_code`, `name`, `nickname`,
     `system_code`, `device_type_code`,
     `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT h.`tenant_id`, h.`device_code`, h.`device_name`, h.`device_name`,
       'HV', 'HVAC',
       'gap017-migration', NOW(), 'gap017-migration', NOW(), 0
FROM `ibms_hvac_device` h
WHERE h.`deleted` = 0
  AND NOT EXISTS (
      SELECT 1 FROM `ibms_device` d
      WHERE d.`deleted` = 0
        AND d.`device_code` = h.`device_code`
        AND d.`tenant_id`   = h.`tenant_id`
  );

-- ---------------------------------------------------------------------
-- Step 2 - 在 ibms_device 中创建对应主设备记录（WATER）
-- ---------------------------------------------------------------------
INSERT INTO `ibms_device`
    (`tenant_id`, `device_code`, `name`, `nickname`,
     `system_code`, `device_type_code`,
     `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT w.`tenant_id`, w.`device_code`, w.`device_name`, w.`device_name`,
       'WS', 'WTR',
       'gap017-migration', NOW(), 'gap017-migration', NOW(), 0
FROM `ibms_water_device` w
WHERE w.`deleted` = 0
  AND NOT EXISTS (
      SELECT 1 FROM `ibms_device` d
      WHERE d.`deleted` = 0
        AND d.`device_code` = w.`device_code`
        AND d.`tenant_id`   = w.`tenant_id`
  );

-- ---------------------------------------------------------------------
-- Step 3 - 回填业务表 ibms_device_id（按 device_code + tenant_id）
-- ---------------------------------------------------------------------
UPDATE `ibms_hvac_device` h
JOIN   `ibms_device` d
       ON  d.`device_code` = h.`device_code`
       AND d.`tenant_id`   = h.`tenant_id`
       AND d.`deleted`     = 0
SET    h.`ibms_device_id` = d.`id`
WHERE  h.`deleted` = 0
  AND  h.`ibms_device_id` IS NULL;

UPDATE `ibms_water_device` w
JOIN   `ibms_device` d
       ON  d.`device_code` = w.`device_code`
       AND d.`tenant_id`   = w.`tenant_id`
       AND d.`deleted`     = 0
SET    w.`ibms_device_id` = d.`id`
WHERE  w.`deleted` = 0
  AND  w.`ibms_device_id` IS NULL;

-- ---------------------------------------------------------------------
-- Step 4 - 验证：期望 orphan_hvac = 0，orphan_water = 0
-- ---------------------------------------------------------------------
SELECT 'orphan_hvac'  AS metric,
       COUNT(*)        AS cnt
FROM   `ibms_hvac_device`
WHERE  `deleted` = 0 AND `ibms_device_id` IS NULL
UNION ALL
SELECT 'orphan_water',
       COUNT(*)
FROM   `ibms_water_device`
WHERE  `deleted` = 0 AND `ibms_device_id` IS NULL
UNION ALL
SELECT 'hvac_total',  COUNT(*) FROM `ibms_hvac_device`  WHERE `deleted` = 0
UNION ALL
SELECT 'water_total', COUNT(*) FROM `ibms_water_device` WHERE `deleted` = 0
UNION ALL
SELECT 'ibms_device_HV_inserted', COUNT(*)
FROM   `ibms_device`
WHERE  `deleted` = 0 AND `system_code` = 'HV' AND `creator` = 'gap017-migration'
UNION ALL
SELECT 'ibms_device_WS_inserted', COUNT(*)
FROM   `ibms_device`
WHERE  `deleted` = 0 AND `system_code` = 'WS' AND `creator` = 'gap017-migration';

-- =====================================================================
-- 回滚（仅本地灾备使用，谨慎执行）：
--   UPDATE ibms_device SET deleted = 1
--    WHERE creator = 'gap017-migration' AND system_code IN ('HV','WS');
--   ALTER TABLE ibms_hvac_device  DROP INDEX idx_ibms_device_id, DROP COLUMN ibms_device_id;
--   ALTER TABLE ibms_water_device DROP INDEX idx_ibms_device_id, DROP COLUMN ibms_device_id;
-- =====================================================================
