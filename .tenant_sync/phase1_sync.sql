-- =============================================================
-- Phase 1 — 共享/字典 同步脚本（v2）
-- 源 tenant_id=1（长辉信息）→ 目标 tenant_id=162（长辉IBMS）
-- 备份后缀：_t162_20260426
-- 唯一外部标识统一加 -ibms 后缀
-- 自增主键不指定，依赖 ID 通过自然键或临时标记回查映射
-- 执行顺序：父表先（建映射），子表后（用映射）
-- =============================================================

SET NAMES utf8mb4;
SET @from_tid := 1;
SET @to_tid   := 162;
SET @suffix   := '-ibms';
SET @creator  := '162';

-- -------------------------------------------------------------
-- 0) 备份 t162 现有数据
-- -------------------------------------------------------------
DROP TABLE IF EXISTS `bak_iot_subsystem_t162_20260426`;
CREATE TABLE `bak_iot_subsystem_t162_20260426`             AS SELECT * FROM iot_subsystem             WHERE tenant_id = 162;
DROP TABLE IF EXISTS `bak_iot_job_type_definition_t162_20260426`;
CREATE TABLE `bak_iot_job_type_definition_t162_20260426`   AS SELECT * FROM iot_job_type_definition   WHERE tenant_id = 162;
DROP TABLE IF EXISTS `bak_iot_scheduled_task_config_t162_20260426`;
CREATE TABLE `bak_iot_scheduled_task_config_t162_20260426` AS SELECT * FROM iot_scheduled_task_config WHERE tenant_id = 162;
DROP TABLE IF EXISTS `bak_iot_thing_model_t162_20260426`;
CREATE TABLE `bak_iot_thing_model_t162_20260426`           AS SELECT * FROM iot_thing_model           WHERE tenant_id = 162;
DROP TABLE IF EXISTS `bak_iot_data_sink_t162_20260426`;
CREATE TABLE `bak_iot_data_sink_t162_20260426`             AS SELECT * FROM iot_data_sink             WHERE tenant_id = 162;
DROP TABLE IF EXISTS `bak_iot_data_rule_t162_20260426`;
CREATE TABLE `bak_iot_data_rule_t162_20260426`             AS SELECT * FROM iot_data_rule             WHERE tenant_id = 162;
DROP TABLE IF EXISTS `bak_iot_product_category_t162_20260426`;
CREATE TABLE `bak_iot_product_category_t162_20260426`      AS SELECT * FROM iot_product_category      WHERE tenant_id = 162;
DROP TABLE IF EXISTS `bak_ibms_product_t162_20260426`;
CREATE TABLE `bak_ibms_product_t162_20260426`              AS SELECT * FROM ibms_product              WHERE tenant_id = 162;
DROP TABLE IF EXISTS `bak_ibms_product_property_t162_20260426`;
CREATE TABLE `bak_ibms_product_property_t162_20260426`     AS SELECT * FROM ibms_product_property     WHERE tenant_id = 162;
DROP TABLE IF EXISTS `bak_ibms_product_point_type_t162_20260426`;
CREATE TABLE `bak_ibms_product_point_type_t162_20260426`   AS SELECT * FROM ibms_product_point_type   WHERE tenant_id = 162;

-- -------------------------------------------------------------
-- 1) 清空 t162（幂等）
-- -------------------------------------------------------------
DELETE FROM ibms_product_property    WHERE tenant_id = 162;
DELETE FROM ibms_product_point_type  WHERE tenant_id = 162;
DELETE FROM ibms_product             WHERE tenant_id = 162;
DELETE FROM iot_data_rule            WHERE tenant_id = 162;
DELETE FROM iot_data_sink            WHERE tenant_id = 162;
DELETE FROM iot_product_category     WHERE tenant_id = 162;
DELETE FROM iot_scheduled_task_config WHERE tenant_id = 162;
DELETE FROM iot_thing_model          WHERE tenant_id = 162;
DELETE FROM iot_job_type_definition  WHERE tenant_id = 162;
DELETE FROM iot_subsystem            WHERE tenant_id = 162;

-- =============================================================
-- 2) 父表 / 无依赖表
-- =============================================================

-- 2.1 iot_subsystem (code+'-ibms'；parent_code 用 code 引用同步加后缀)
INSERT INTO iot_subsystem
    (code, name, parent_code, level, menu_id, menu_path, icon, description, sort, enabled,
     tenant_id, create_time, update_time, creator, updater, deleted)
SELECT
    CONCAT(code, @suffix), name,
    CASE WHEN parent_code IS NULL OR parent_code='' THEN parent_code ELSE CONCAT(parent_code, @suffix) END,
    level, menu_id, menu_path, icon, description, sort, enabled,
    @to_tid, NOW(), NOW(), @creator, @creator, deleted
FROM iot_subsystem
WHERE tenant_id = @from_tid AND deleted = 0;

-- 2.2 iot_job_type_definition (code+'-ibms')
INSERT INTO iot_job_type_definition
    (name, code, description, business_type, applicable_entities, default_config_template, status,
     creator, create_time, updater, update_time, deleted, tenant_id)
SELECT
    name, CONCAT(code, @suffix), description, business_type, applicable_entities, default_config_template, status,
    @creator, NOW(), @creator, NOW(), deleted, @to_tid
FROM iot_job_type_definition
WHERE tenant_id = @from_tid AND deleted = 0;

-- 2.3 iot_thing_model (product_id 引用旧 iot_product 表已不存在；保留原值)
INSERT INTO iot_thing_model
    (identifier, name, description, product_id, product_key, type, property, event, service,
     creator, create_time, updater, update_time, deleted, tenant_id)
SELECT
    identifier, name, description, product_id, product_key, type, property, event, service,
    @creator, NOW(), @creator, NOW(), deleted, @to_tid
FROM iot_thing_model
WHERE tenant_id = @from_tid AND deleted = 0;

-- 2.4 iot_data_sink（在 description 临时记录 old_id 以便建映射）
INSERT INTO iot_data_sink
    (name, description, status, type, config, creator, create_time, updater, update_time, deleted, tenant_id)
SELECT
    name,
    CONCAT(IFNULL(description,''), '__MIG_OLD_', id, '__'),
    status, type, config, @creator, NOW(), @creator, NOW(), deleted, @to_tid
FROM iot_data_sink
WHERE tenant_id = @from_tid AND deleted = 0;

DROP TEMPORARY TABLE IF EXISTS _tmp_map_sink;
CREATE TEMPORARY TABLE _tmp_map_sink (old_id BIGINT PRIMARY KEY, new_id BIGINT NOT NULL);
INSERT INTO _tmp_map_sink (old_id, new_id)
SELECT
    CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(description, '__MIG_OLD_', -1), '__', 1) AS UNSIGNED),
    id
FROM iot_data_sink
WHERE tenant_id = @to_tid AND description LIKE '%__MIG_OLD_%';

UPDATE iot_data_sink
SET description = TRIM(BOTH ' ' FROM REGEXP_REPLACE(description, '__MIG_OLD_[0-9]+__', ''))
WHERE tenant_id = @to_tid AND description LIKE '%__MIG_OLD_%';

-- 2.5 iot_product_category（自引用 parent_id；同样用 description 标记建映射）
INSERT INTO iot_product_category
    (name, parent_id, level, status, icon, module_code, description, sort,
     creator, create_time, updater, update_time, deleted, tenant_id)
SELECT
    name, parent_id, level, status, icon, module_code,
    CONCAT(IFNULL(description,''), '__MIG_OLD_', id, '__'),
    sort, @creator, NOW(), @creator, NOW(), deleted, @to_tid
FROM iot_product_category
WHERE tenant_id = @from_tid AND deleted = 0;

DROP TEMPORARY TABLE IF EXISTS _tmp_map_category;
CREATE TEMPORARY TABLE _tmp_map_category (old_id BIGINT PRIMARY KEY, new_id BIGINT NOT NULL);
INSERT INTO _tmp_map_category (old_id, new_id)
SELECT
    CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(description, '__MIG_OLD_', -1), '__', 1) AS UNSIGNED),
    id
FROM iot_product_category
WHERE tenant_id = @to_tid AND description LIKE '%__MIG_OLD_%';

UPDATE iot_product_category c
JOIN _tmp_map_category m ON m.old_id = c.parent_id
SET c.parent_id = m.new_id
WHERE c.tenant_id = @to_tid AND c.parent_id <> 0;

UPDATE iot_product_category
SET description = TRIM(BOTH ' ' FROM REGEXP_REPLACE(description, '__MIG_OLD_[0-9]+__', ''))
WHERE tenant_id = @to_tid AND description LIKE '%__MIG_OLD_%';

-- 2.6 ibms_product (product_code+'-ibms'；用 UK 回查映射)
INSERT INTO ibms_product
    (tenant_id, product_code, product_name, group_code, system_code, model_code,
     device_type_code, manufacturer, model_number, protocol, icon, color, description, extra,
     creator, create_time, updater, update_time, deleted)
SELECT
    @to_tid, CONCAT(product_code, @suffix), product_name, group_code, system_code, model_code,
    device_type_code, manufacturer, model_number, protocol, icon, color, description, extra,
    @creator, NOW(), @creator, NOW(), deleted
FROM ibms_product
WHERE tenant_id = @from_tid AND deleted = 0;

DROP TEMPORARY TABLE IF EXISTS _tmp_map_product;
CREATE TEMPORARY TABLE _tmp_map_product (old_id BIGINT PRIMARY KEY, new_id BIGINT NOT NULL);
INSERT INTO _tmp_map_product (old_id, new_id)
SELECT s.id, t.id
FROM ibms_product s
JOIN ibms_product t
  ON t.product_code = CONCAT(s.product_code, @suffix)
 AND t.tenant_id = @to_tid
WHERE s.tenant_id = @from_tid AND s.deleted = 0;

-- =============================================================
-- 3) 子表（依赖父表映射）
-- =============================================================

-- 3.1 iot_data_rule (sink_ids 字符串重写)
INSERT INTO iot_data_rule
    (name, description, status, source_configs, sink_ids,
     creator, create_time, updater, update_time, deleted, tenant_id)
SELECT
    r.name, r.description, r.status, r.source_configs,
    (
      SELECT GROUP_CONCAT(m.new_id ORDER BY ord SEPARATOR ',')
      FROM (
        SELECT 1 ord, NULLIF(SUBSTRING_INDEX(SUBSTRING_INDEX(r.sink_ids, ',', 1), ',', -1),'') sid
        UNION ALL SELECT 2, NULLIF(SUBSTRING_INDEX(SUBSTRING_INDEX(r.sink_ids, ',', 2), ',', -1),'')
        UNION ALL SELECT 3, NULLIF(SUBSTRING_INDEX(SUBSTRING_INDEX(r.sink_ids, ',', 3), ',', -1),'')
        UNION ALL SELECT 4, NULLIF(SUBSTRING_INDEX(SUBSTRING_INDEX(r.sink_ids, ',', 4), ',', -1),'')
        UNION ALL SELECT 5, NULLIF(SUBSTRING_INDEX(SUBSTRING_INDEX(r.sink_ids, ',', 5), ',', -1),'')
      ) parts
      JOIN _tmp_map_sink m ON m.old_id = CAST(parts.sid AS UNSIGNED)
      WHERE parts.sid IS NOT NULL
        AND parts.ord <= (LENGTH(r.sink_ids) - LENGTH(REPLACE(r.sink_ids, ',', '')) + 1)
    ) AS new_sink_ids,
    @creator, NOW(), @creator, NOW(), r.deleted, @to_tid
FROM iot_data_rule r
WHERE r.tenant_id = @from_tid AND r.deleted = 0;

-- 3.2 iot_scheduled_task_config
--     entity_type='PRODUCT' → entity_id 通过 _tmp_map_product 映射
--     entity_type='DEVICE'  → 暂保留原值（device 在 Phase 2 复制后可补刀）
--     job_type 加 -ibms 与 job_type_definition.code 保持一致
INSERT INTO iot_scheduled_task_config
    (entity_type, entity_id, entity_name,
     job_type, job_name, description, enabled, status, cron_expression, interval_seconds,
     job_config, priority, conflict_strategy, timeout_seconds, retry_count, alert_on_failure,
     from_product, product_id,
     last_execution_time, last_execution_status, last_execution_message, next_execution_time,
     execution_count, success_count, fail_count, avg_duration_ms,
     creator, create_time, updater, update_time, deleted, tenant_id)
SELECT
    s.entity_type,
    CASE WHEN s.entity_type = 'PRODUCT'
         THEN COALESCE(mp.new_id, s.entity_id)
         ELSE s.entity_id
    END,
    s.entity_name,
    CONCAT(s.job_type, @suffix), s.job_name, s.description, s.enabled, s.status,
    s.cron_expression, s.interval_seconds,
    s.job_config, s.priority, s.conflict_strategy, s.timeout_seconds, s.retry_count, s.alert_on_failure,
    s.from_product, s.product_id,
    NULL, NULL, NULL, NULL,
    0, 0, 0, NULL,
    @creator, NOW(), @creator, NOW(), s.deleted, @to_tid
FROM iot_scheduled_task_config s
LEFT JOIN _tmp_map_product mp ON s.entity_type='PRODUCT' AND mp.old_id = s.entity_id
WHERE s.tenant_id = @from_tid AND s.deleted = 0;

-- 3.3 ibms_product_property (product_id 用映射)
INSERT INTO ibms_product_property
    (tenant_id, product_id, prop_name, label, type, options, default_value, unit, remark,
     creator, create_time, updater, update_time, deleted)
SELECT
    @to_tid, m.new_id, p.prop_name, p.label, p.type, p.options, p.default_value, p.unit, p.remark,
    @creator, NOW(), @creator, NOW(), p.deleted
FROM ibms_product_property p
JOIN _tmp_map_product m ON m.old_id = p.product_id
WHERE p.tenant_id = @from_tid AND p.deleted = 0;

-- 3.4 ibms_product_point_type (product_id 用映射)
INSERT INTO ibms_product_point_type
    (tenant_id, product_id, point_type_code, name, count, data_type, remark,
     creator, create_time, updater, update_time, deleted)
SELECT
    @to_tid, m.new_id, p.point_type_code, p.name, p.count, p.data_type, p.remark,
    @creator, NOW(), @creator, NOW(), p.deleted
FROM ibms_product_point_type p
JOIN _tmp_map_product m ON m.old_id = p.product_id
WHERE p.tenant_id = @from_tid AND p.deleted = 0;

-- =============================================================
-- 4) 校验
-- =============================================================
SELECT 'iot_subsystem' AS tbl,
       (SELECT COUNT(*) FROM iot_subsystem WHERE tenant_id=1   AND deleted=0) AS t1,
       (SELECT COUNT(*) FROM iot_subsystem WHERE tenant_id=162 AND deleted=0) AS t162
UNION ALL SELECT 'iot_job_type_definition',
       (SELECT COUNT(*) FROM iot_job_type_definition WHERE tenant_id=1   AND deleted=0),
       (SELECT COUNT(*) FROM iot_job_type_definition WHERE tenant_id=162 AND deleted=0)
UNION ALL SELECT 'iot_thing_model',
       (SELECT COUNT(*) FROM iot_thing_model WHERE tenant_id=1   AND deleted=0),
       (SELECT COUNT(*) FROM iot_thing_model WHERE tenant_id=162 AND deleted=0)
UNION ALL SELECT 'iot_data_sink',
       (SELECT COUNT(*) FROM iot_data_sink WHERE tenant_id=1   AND deleted=0),
       (SELECT COUNT(*) FROM iot_data_sink WHERE tenant_id=162 AND deleted=0)
UNION ALL SELECT 'iot_data_rule',
       (SELECT COUNT(*) FROM iot_data_rule WHERE tenant_id=1   AND deleted=0),
       (SELECT COUNT(*) FROM iot_data_rule WHERE tenant_id=162 AND deleted=0)
UNION ALL SELECT 'iot_product_category',
       (SELECT COUNT(*) FROM iot_product_category WHERE tenant_id=1   AND deleted=0),
       (SELECT COUNT(*) FROM iot_product_category WHERE tenant_id=162 AND deleted=0)
UNION ALL SELECT 'iot_scheduled_task_config',
       (SELECT COUNT(*) FROM iot_scheduled_task_config WHERE tenant_id=1   AND deleted=0),
       (SELECT COUNT(*) FROM iot_scheduled_task_config WHERE tenant_id=162 AND deleted=0)
UNION ALL SELECT 'ibms_product',
       (SELECT COUNT(*) FROM ibms_product WHERE tenant_id=1   AND deleted=0),
       (SELECT COUNT(*) FROM ibms_product WHERE tenant_id=162 AND deleted=0)
UNION ALL SELECT 'ibms_product_property',
       (SELECT COUNT(*) FROM ibms_product_property WHERE tenant_id=1   AND deleted=0),
       (SELECT COUNT(*) FROM ibms_product_property WHERE tenant_id=162 AND deleted=0)
UNION ALL SELECT 'ibms_product_point_type',
       (SELECT COUNT(*) FROM ibms_product_point_type WHERE tenant_id=1   AND deleted=0),
       (SELECT COUNT(*) FROM ibms_product_point_type WHERE tenant_id=162 AND deleted=0);
