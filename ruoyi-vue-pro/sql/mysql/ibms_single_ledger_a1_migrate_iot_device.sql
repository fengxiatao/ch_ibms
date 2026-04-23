-- =============================================
-- WP-A1：将仍仅在 iot_device 的有效台账迁入 ibms_device + ibms_device_runtime（幂等）
-- 主键 id 与 iot_device 一致；device_code = LEG-IOT-{id}（租户内唯一）
-- 执行前：确认已存在表 ibms_device、ibms_device_runtime；建议先备份
-- =============================================

SET NAMES utf8mb4;

INSERT INTO ibms_device (
  id, tenant_id, device_code, name, product_key, group_ids, extra,
  creator, create_time, updater, update_time, deleted,
  point_count, points_online, points_alarm
)
SELECT
  d.id,
  d.tenant_id,
  CONCAT('LEG-IOT-', d.id),
  d.device_name,
  NULLIF(NULLIF(TRIM(d.product_key), ''), 'None'),
  d.group_ids,
  LEFT(JSON_OBJECT(
    'migratedFrom', 'iot_device',
    'legacyProductId', d.product_id,
    'deviceSecret', d.device_secret
  ), 1024),
  d.creator, d.create_time, d.updater, d.update_time, d.deleted,
  0, 0, 0
FROM iot_device d
WHERE d.deleted = 0
AND NOT EXISTS (SELECT 1 FROM ibms_device b WHERE b.id = d.id AND b.deleted = 0);

-- 迁入行 extra 补充 MQTT/网关用 deviceKey（与 iot_device.device_key 一致）
UPDATE ibms_device b
INNER JOIN iot_device d ON d.id = b.id AND d.deleted = 0
SET b.extra = JSON_MERGE_PATCH(CAST(b.extra AS JSON), JSON_OBJECT('deviceKey', d.device_key))
WHERE b.deleted = 0 AND b.device_code LIKE 'LEG-IOT-%';

INSERT INTO ibms_device_runtime (
  device_id, tenant_id, state, online_time, offline_time, active_time,
  firmware_id, gateway_id, location_type, latitude, longitude,
  area_id, address, campus_id, building_id, floor_id, room_id,
  local_x, local_y, local_z, install_location, install_height_type,
  config, job_config, creator, create_time, updater, update_time, deleted
)
SELECT
  d.id, d.tenant_id, d.state, d.online_time, d.offline_time, d.active_time,
  d.firmware_id, d.gateway_id, d.location_type, d.latitude, d.longitude,
  d.area_id, d.address, d.campus_id, d.building_id, d.floor_id, d.room_id,
  d.local_x, d.local_y, d.local_z, d.install_location, d.install_height_type,
  IF(JSON_VALID(d.config), CAST(d.config AS JSON), JSON_OBJECT('_invalidConfig', TRUE)),
  LEFT(d.job_config, 2048),
  d.creator, d.create_time, d.updater, d.update_time, d.deleted
FROM iot_device d
WHERE d.deleted = 0
AND NOT EXISTS (SELECT 1 FROM ibms_device_runtime r WHERE r.device_id = d.id AND r.deleted = 0);
