-- =============================================
-- WP-A2：iot_product → ibms_product；iot_device_channel → ibms_channel（幂等）
-- 前置：ibms_product 对 legacy 行使用占位编码段（SA/VI/DS/CAM/LEG），详情在 extra JSON
-- 通道 code 固定 LEG-IOT-CH-{id}，避免与 uk_code_tenant 既有数据冲突
-- 执行后建议跑 ibms_single_ledger_a3_reconcile.sql 对账
-- =============================================

SET NAMES utf8mb4;

-- 1) 产品：按 id 对齐迁入（与 iot_product.id 一致，便于 ibms_device / 外键逻辑沿用 product_id）
INSERT INTO ibms_product (
  id, tenant_id, product_code, product_name,
  group_code, system_code, model_code, device_type_code,
  manufacturer, model_number, protocol, icon, color, description, extra,
  creator, create_time, updater, update_time, deleted
)
SELECT
  p.id,
  p.tenant_id,
  CONCAT('LEG-IOT-P-', p.id),
  p.name,
  'SA',
  'VI',
  'DS',
  'CAM',
  'LEG',
  LEFT(CONCAT('PK-', IFNULL(p.product_key, '')), 64),
  NULL,
  LEFT(IFNULL(p.icon, ''), 64),
  NULL,
  IFNULL(p.description, ''),
  JSON_OBJECT(
    'migratedFrom', 'iot_product',
    'productKey', p.product_key,
    'legacyCategoryId', p.category_id,
    'picUrl', p.pic_url,
    'status', p.status,
    'deviceType', p.device_type,
    'netType', p.net_type,
    'locationType', p.location_type,
    'codecType', p.codec_type,
    'jobConfig', LEFT(IFNULL(p.job_config, ''), 2000)
  ),
  p.creator, p.create_time, p.updater, p.update_time, p.deleted
FROM iot_product p
WHERE p.deleted = 0
  -- 主键 id 已被占用（含 deleted=1）则跳过，避免 Duplicate entry
  AND NOT EXISTS (SELECT 1 FROM ibms_product b WHERE b.id = p.id);

-- 1b) 已存在 ibms 行但 deleted=1、iot 仍有效：按 iot 复活并覆盖为 LEGACY 占位编码（幂等可重复）
UPDATE ibms_product b
INNER JOIN iot_product p ON p.id = b.id AND p.deleted = 0
SET b.tenant_id = p.tenant_id,
    b.product_code = CONCAT('LEG-IOT-P-', p.id),
    b.product_name = LEFT(p.name, 128),
    b.group_code = 'SA',
    b.system_code = 'VI',
    b.model_code = 'DS',
    b.device_type_code = 'CAM',
    b.manufacturer = 'LEG',
    b.model_number = LEFT(CONCAT('PK-', IFNULL(p.product_key, '')), 64),
    b.protocol = NULL,
    b.icon = LEFT(IFNULL(p.icon, ''), 64),
    b.color = NULL,
    b.description = IFNULL(p.description, ''),
    b.extra = JSON_OBJECT(
      'migratedFrom', 'iot_product',
      'productKey', p.product_key,
      'legacyCategoryId', p.category_id,
      'picUrl', p.pic_url,
      'status', p.status,
      'deviceType', p.device_type,
      'netType', p.net_type,
      'locationType', p.location_type,
      'codecType', p.codec_type,
      'jobConfig', LEFT(IFNULL(p.job_config, ''), 2000)
    ),
    b.updater = p.updater,
    b.update_time = p.update_time,
    b.deleted = p.deleted
WHERE CAST(b.deleted AS UNSIGNED) = 1;

-- 2) 自增兜底：避免后续 INSERT 与已迁入的大 id 冲突（若 PREPARE 受限，可手工 SELECT MAX(id)+1 后 ALTER）
SET @max_pid = (SELECT IFNULL(MAX(id), 1) FROM ibms_product);
SET @sql = CONCAT('ALTER TABLE ibms_product AUTO_INCREMENT = ', @max_pid + 1);
PREPARE a2_ai FROM @sql;
EXECUTE a2_ai;
DEALLOCATE PREPARE a2_ai;

-- 3) 设备台账：补齐 ibms_product_id（与 iot_device.product_id 一致，且目标产品已在 ibms_product）
UPDATE ibms_device d
INNER JOIN iot_device i ON i.id = d.id AND i.deleted = 0 AND d.deleted = 0
INNER JOIN ibms_product pr ON pr.id = i.product_id AND pr.deleted = 0
SET d.ibms_product_id = i.product_id
WHERE d.ibms_product_id IS NULL OR d.ibms_product_id <> i.product_id;

-- 3b) 设备展示/认证列：从 iot_device 回填至 ibms_device（仅补缺，不覆盖已有非空）
UPDATE ibms_device d
INNER JOIN iot_device i ON i.id = d.id AND i.deleted = 0 AND d.deleted = 0
SET d.nickname = COALESCE(d.nickname, i.nickname),
    d.device_key = COALESCE(d.device_key, i.device_key),
    d.device_secret = COALESCE(d.device_secret, i.device_secret);

-- 4) 通道：id 与 iot_device_channel 对齐；code 使用 LEG-IOT-CH-{id}
INSERT INTO ibms_channel (
  id, tenant_id, space_id, device_id,
  code, channel_no, name,
  business, type_code, category, system_type, data_source,
  ip, mac, device_sn, device_name, space,
  current_value, status, extra,
  creator, create_time, updater, update_time, deleted
)
SELECT
  c.id,
  c.tenant_id,
  c.space_id,
  c.device_id,
  CONCAT('LEG-IOT-CH-', c.id),
  c.channel_no,
  LEFT(c.channel_name, 128),
  CASE c.device_type
    WHEN 'NVR' THEN 'security'
    WHEN 'DVR' THEN 'security'
    WHEN 'ACCESS_CONTROLLER' THEN 'access'
    WHEN 'FIRE_PANEL' THEN 'alarm'
    WHEN 'METER' THEN 'energy'
    WHEN 'BROADCAST' THEN 'building'
    ELSE 'security'
  END,
  CASE
    WHEN c.channel_type = 'ACCESS' OR c.device_type = 'ACCESS_CONTROLLER' THEN 'DR'
    WHEN c.channel_type = 'FIRE' OR c.device_type = 'FIRE_PANEL' THEN 'DI'
    WHEN c.channel_type = 'ENERGY' OR c.device_type = 'METER' THEN 'PM'
    ELSE 'VT'
  END,
  c.channel_type,
  NULL,
  c.protocol,
  c.target_ip,
  NULL,
  NULL,
  NULL,
  c.location,
  CASE c.online_status WHEN 1 THEN '在线' ELSE '离线' END,
  CASE c.online_status WHEN 1 THEN 'online' WHEN 2 THEN 'warning' ELSE 'offline' END,
  JSON_OBJECT(
    'migratedFrom', 'iot_device_channel',
    'deviceType', c.device_type,
    'channelCode', c.channel_code,
    'channelSubType', c.channel_sub_type,
    'targetDeviceId', c.target_device_id,
    'targetPort', c.target_port,
    'targetChannelNo', c.target_channel_no,
    'streamUrlMain', LEFT(IFNULL(c.stream_url_main, ''), 400),
    'streamUrlSub', LEFT(IFNULL(c.stream_url_sub, ''), 400),
    'snapshotUrl', LEFT(IFNULL(c.snapshot_url, ''), 400),
    'doorName', c.door_name,
    'config', LEFT(IFNULL(c.config, ''), 400)
  ),
  c.creator, c.create_time, c.updater, c.update_time, c.deleted
FROM iot_device_channel c
WHERE c.deleted = 0
  AND NOT EXISTS (SELECT 1 FROM ibms_channel b WHERE b.id = c.id AND b.deleted = 0);

SET @max_cid = (SELECT IFNULL(MAX(id), 1) FROM ibms_channel);
SET @sql2 = CONCAT('ALTER TABLE ibms_channel AUTO_INCREMENT = ', @max_cid + 1);
PREPARE a2_ai2 FROM @sql2;
EXECUTE a2_ai2;
DEALLOCATE PREPARE a2_ai2;
