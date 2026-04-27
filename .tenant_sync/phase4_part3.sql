-- ============================================================================
-- Phase 4 — 智慧通行 数据同步  PART 3
-- 范围: 终末 FK 表 (11 张)
--   iot_access_person_credential, iot_access_person_device_auth,
--   iot_access_device_capability, iot_access_permission_group_device/person,
--   iot_access_auth_task(+detail), iot_access_event_log(12566), iot_access_operation_log,
--   iot_ota_task, iot_ota_task_record
-- ============================================================================
USE ch_ibms;
SET SQL_SAFE_UPDATES = 0;

-- ============================================================================
-- 31) iot_access_person_credential (t1=18): person_id → person map
-- ============================================================================
INSERT INTO iot_access_person_credential
  (person_id, credential_type, credential_data, card_no, issue_time, replace_time,
   card_status, old_card_no, finger_index, finger_name, device_synced, sync_time, status,
   tenant_id, creator, create_time, updater, update_time, deleted)
SELECT COALESCE(pm.new_id, t.person_id),
       t.credential_type, t.credential_data, t.card_no, t.issue_time, t.replace_time,
       t.card_status, t.old_card_no, t.finger_index, t.finger_name, t.device_synced, t.sync_time, t.status,
       162, t.creator, t.create_time, t.updater, t.update_time, t.deleted
FROM iot_access_person_credential t
LEFT JOIN _tmp_map_person pm ON pm.old_id = t.person_id
WHERE t.tenant_id=1 ORDER BY t.id;

SELECT '[31] iot_access_person_credential' tag,
  (SELECT COUNT(*) FROM iot_access_person_credential WHERE tenant_id=1) t1,
  (SELECT COUNT(*) FROM iot_access_person_credential WHERE tenant_id=162) t162;

-- ============================================================================
-- 32) iot_access_person_device_auth (t1=25): person_id, device_id, channel_id → maps
-- ============================================================================
INSERT INTO iot_access_person_device_auth
  (person_id, device_id, channel_id, auth_status, last_dispatch_time, last_dispatch_result,
   credential_hash, version,
   tenant_id, creator, create_time, updater, update_time, deleted)
SELECT COALESCE(pm.new_id, t.person_id),
       COALESCE(dm.new_id, t.device_id),
       COALESCE(cm.new_id, t.channel_id),
       t.auth_status, t.last_dispatch_time, t.last_dispatch_result,
       t.credential_hash, t.version,
       162, t.creator, t.create_time, t.updater, t.update_time, t.deleted
FROM iot_access_person_device_auth t
LEFT JOIN _tmp_map_person pm ON pm.old_id = t.person_id
LEFT JOIN _tmp_map_device dm ON dm.old_id = t.device_id
LEFT JOIN _tmp_map_channel cm ON cm.old_id = t.channel_id
WHERE t.tenant_id=1 ORDER BY t.id;

SELECT '[32] iot_access_person_device_auth' tag,
  (SELECT COUNT(*) FROM iot_access_person_device_auth WHERE tenant_id=1) t1,
  (SELECT COUNT(*) FROM iot_access_person_device_auth WHERE tenant_id=162) t162;

-- ============================================================================
-- 33) iot_access_device_capability (t1=2): device_id → device map
-- ============================================================================
INSERT INTO iot_access_device_capability
  (device_id, device_generation, max_users, max_cards, max_faces, max_fingerprints,
   current_users, current_cards, current_faces, current_fingerprints,
   sup_face_service, sup_fingerprint_service, sup_card_service, sup_holiday_plan,
   max_insert_rate_user, max_insert_rate_card, max_insert_rate_face, max_insert_rate_fingerprint,
   channels, max_cards_per_user, max_fingerprints_per_user, max_face_image_size,
   capability_json, last_query_time, cache_expire_time,
   tenant_id, creator, create_time, updater, update_time, deleted)
SELECT COALESCE(dm.new_id, t.device_id),
       t.device_generation, t.max_users, t.max_cards, t.max_faces, t.max_fingerprints,
       t.current_users, t.current_cards, t.current_faces, t.current_fingerprints,
       t.sup_face_service, t.sup_fingerprint_service, t.sup_card_service, t.sup_holiday_plan,
       t.max_insert_rate_user, t.max_insert_rate_card, t.max_insert_rate_face, t.max_insert_rate_fingerprint,
       t.channels, t.max_cards_per_user, t.max_fingerprints_per_user, t.max_face_image_size,
       t.capability_json, t.last_query_time, t.cache_expire_time,
       162, t.creator, t.create_time, t.updater, t.update_time, t.deleted
FROM iot_access_device_capability t
LEFT JOIN _tmp_map_device dm ON dm.old_id = t.device_id
WHERE t.tenant_id=1 ORDER BY t.id;

SELECT '[33] iot_access_device_capability' tag,
  (SELECT COUNT(*) FROM iot_access_device_capability WHERE tenant_id=1) t1,
  (SELECT COUNT(*) FROM iot_access_device_capability WHERE tenant_id=162) t162;

-- ============================================================================
-- 34) iot_access_permission_group_device (t1=27): group_id, device_id, channel_id
-- ============================================================================
INSERT INTO iot_access_permission_group_device
  (group_id, device_id, channel_id, tenant_id, creator, create_time, updater, update_time, deleted)
SELECT COALESCE(pgm.new_id, t.group_id),
       COALESCE(dm.new_id, t.device_id),
       COALESCE(cm.new_id, t.channel_id),
       162, t.creator, t.create_time, t.updater, t.update_time, t.deleted
FROM iot_access_permission_group_device t
LEFT JOIN _tmp_map_perm_grp pgm ON pgm.old_id = t.group_id
LEFT JOIN _tmp_map_device dm ON dm.old_id = t.device_id
LEFT JOIN _tmp_map_channel cm ON cm.old_id = t.channel_id
WHERE t.tenant_id=1 ORDER BY t.id;

SELECT '[34] iot_access_permission_group_device' tag,
  (SELECT COUNT(*) FROM iot_access_permission_group_device WHERE tenant_id=1) t1,
  (SELECT COUNT(*) FROM iot_access_permission_group_device WHERE tenant_id=162) t162;

-- ============================================================================
-- 35) iot_access_permission_group_person (t1=23): group_id, person_id
-- ============================================================================
INSERT INTO iot_access_permission_group_person
  (group_id, person_id, tenant_id, creator, create_time, updater, update_time, deleted)
SELECT COALESCE(pgm.new_id, t.group_id),
       COALESCE(pm.new_id, t.person_id),
       162, t.creator, t.create_time, t.updater, t.update_time, t.deleted
FROM iot_access_permission_group_person t
LEFT JOIN _tmp_map_perm_grp pgm ON pgm.old_id = t.group_id
LEFT JOIN _tmp_map_person pm ON pm.old_id = t.person_id
WHERE t.tenant_id=1 ORDER BY t.id;

SELECT '[35] iot_access_permission_group_person' tag,
  (SELECT COUNT(*) FROM iot_access_permission_group_person WHERE tenant_id=1) t1,
  (SELECT COUNT(*) FROM iot_access_permission_group_person WHERE tenant_id=162) t162;

-- ============================================================================
-- 36) iot_access_auth_task (t1=208): group_id, person_id, device_id → maps
-- ============================================================================
INSERT INTO iot_access_auth_task
  (task_type, group_id, person_id, device_id, total_count, success_count, fail_count,
   status, start_time, end_time, error_message,
   tenant_id, creator, create_time, updater, update_time, deleted)
SELECT t.task_type,
       COALESCE(pgm.new_id, t.group_id),
       COALESCE(pm.new_id, t.person_id),
       COALESCE(dm.new_id, t.device_id),
       t.total_count, t.success_count, t.fail_count,
       t.status, t.start_time, t.end_time, t.error_message,
       162, t.creator, t.create_time, t.updater, t.update_time, t.deleted
FROM iot_access_auth_task t
LEFT JOIN _tmp_map_perm_grp pgm ON pgm.old_id = t.group_id
LEFT JOIN _tmp_map_person pm ON pm.old_id = t.person_id
LEFT JOIN _tmp_map_device dm ON dm.old_id = t.device_id
WHERE t.tenant_id=1 ORDER BY t.id;

DROP TABLE IF EXISTS _tmp_map_auth_task;
CREATE TABLE _tmp_map_auth_task (old_id BIGINT PRIMARY KEY, new_id BIGINT NOT NULL) ENGINE=InnoDB;
INSERT INTO _tmp_map_auth_task (old_id, new_id)
SELECT a.old_id, b.new_id FROM
  (SELECT id AS old_id, ROW_NUMBER() OVER (ORDER BY id) rn FROM iot_access_auth_task WHERE tenant_id=1) a
  JOIN
  (SELECT id AS new_id, ROW_NUMBER() OVER (ORDER BY id) rn FROM iot_access_auth_task WHERE tenant_id=162) b
  ON a.rn = b.rn;

SELECT '[36] iot_access_auth_task' tag,
  (SELECT COUNT(*) FROM iot_access_auth_task WHERE tenant_id=1) t1,
  (SELECT COUNT(*) FROM iot_access_auth_task WHERE tenant_id=162) t162,
  (SELECT COUNT(*) FROM _tmp_map_auth_task) map;

-- ============================================================================
-- 37) iot_access_auth_task_detail (t1=305): task_id, person_id, device_id, channel_id
-- ============================================================================
INSERT INTO iot_access_auth_task_detail
  (task_id, person_id, person_code, person_name, device_id, device_name, channel_id,
   status, retry_count, last_error, error_message, credential_types, execute_time,
   tenant_id, creator, create_time, updater, update_time, deleted)
SELECT COALESCE(atm.new_id, t.task_id),
       COALESCE(pm.new_id, t.person_id),
       t.person_code, t.person_name,
       COALESCE(dm.new_id, t.device_id),
       t.device_name,
       COALESCE(cm.new_id, t.channel_id),
       t.status, t.retry_count, t.last_error, t.error_message, t.credential_types, t.execute_time,
       162, t.creator, t.create_time, t.updater, t.update_time, t.deleted
FROM iot_access_auth_task_detail t
LEFT JOIN _tmp_map_auth_task atm ON atm.old_id = t.task_id
LEFT JOIN _tmp_map_person pm ON pm.old_id = t.person_id
LEFT JOIN _tmp_map_device dm ON dm.old_id = t.device_id
LEFT JOIN _tmp_map_channel cm ON cm.old_id = t.channel_id
WHERE t.tenant_id=1 ORDER BY t.id;

SELECT '[37] iot_access_auth_task_detail' tag,
  (SELECT COUNT(*) FROM iot_access_auth_task_detail WHERE tenant_id=1) t1,
  (SELECT COUNT(*) FROM iot_access_auth_task_detail WHERE tenant_id=162) t162;

-- ============================================================================
-- 38) iot_access_event_log (t1=12566): device_id, channel_id, person_id (channel_id=0 保留)
-- ============================================================================
INSERT INTO iot_access_event_log
  (device_id, channel_id, event_type, event_time, person_id, person_name, id_card, card_no,
   verify_mode, verify_result, direction, fail_reason, snapshot_url, capture_url,
   temperature, mask_status,
   tenant_id, creator, create_time, updater, update_time, deleted,
   device_name, channel_name, person_code, verify_result_desc, event_desc,
   credential_type, credential_data, success)
SELECT COALESCE(dm.new_id, t.device_id),
       CASE WHEN t.channel_id IS NULL OR t.channel_id = 0 THEN t.channel_id
            ELSE COALESCE(cm.new_id, t.channel_id) END,
       t.event_type, t.event_time,
       COALESCE(pm.new_id, t.person_id),
       t.person_name, t.id_card, t.card_no,
       t.verify_mode, t.verify_result, t.direction, t.fail_reason, t.snapshot_url, t.capture_url,
       t.temperature, t.mask_status,
       162, t.creator, t.create_time, t.updater, t.update_time, t.deleted,
       t.device_name, t.channel_name, t.person_code, t.verify_result_desc, t.event_desc,
       t.credential_type, t.credential_data, t.success
FROM iot_access_event_log t
LEFT JOIN _tmp_map_device dm ON dm.old_id = t.device_id
LEFT JOIN _tmp_map_channel cm ON cm.old_id = t.channel_id
LEFT JOIN _tmp_map_person pm ON pm.old_id = t.person_id
WHERE t.tenant_id=1;

SELECT '[38] iot_access_event_log' tag,
  (SELECT COUNT(*) FROM iot_access_event_log WHERE tenant_id=1) t1,
  (SELECT COUNT(*) FROM iot_access_event_log WHERE tenant_id=162) t162;

-- ============================================================================
-- 39) iot_access_operation_log (t1=254): device_id, channel_id, target_person_id,
--     permission_group_id, auth_task_id; operator_id 是 system user, 保留
-- ============================================================================
INSERT INTO iot_access_operation_log
  (device_id, device_name, channel_id, channel_name,
   operation_type, operation_time, operator_id, operator_name,
   result, result_desc, error_message, request_params,
   target_person_id, target_person_code, target_person_name,
   permission_group_id, permission_group_name, auth_task_id, credential_types,
   success_credential_count, failed_credential_count, sdk_error_code,
   tenant_id, creator, create_time, updater, update_time, deleted)
SELECT COALESCE(dm.new_id, t.device_id), t.device_name,
       COALESCE(cm.new_id, t.channel_id), t.channel_name,
       t.operation_type, t.operation_time, t.operator_id, t.operator_name,
       t.result, t.result_desc, t.error_message, t.request_params,
       COALESCE(pm.new_id, t.target_person_id), t.target_person_code, t.target_person_name,
       COALESCE(pgm.new_id, t.permission_group_id), t.permission_group_name,
       COALESCE(atm.new_id, t.auth_task_id), t.credential_types,
       t.success_credential_count, t.failed_credential_count, t.sdk_error_code,
       162, t.creator, t.create_time, t.updater, t.update_time, t.deleted
FROM iot_access_operation_log t
LEFT JOIN _tmp_map_device dm ON dm.old_id = t.device_id
LEFT JOIN _tmp_map_channel cm ON cm.old_id = t.channel_id
LEFT JOIN _tmp_map_person pm ON pm.old_id = t.target_person_id
LEFT JOIN _tmp_map_perm_grp pgm ON pgm.old_id = t.permission_group_id
LEFT JOIN _tmp_map_auth_task atm ON atm.old_id = t.auth_task_id
WHERE t.tenant_id=1 ORDER BY t.id;

SELECT '[39] iot_access_operation_log' tag,
  (SELECT COUNT(*) FROM iot_access_operation_log WHERE tenant_id=1) t1,
  (SELECT COUNT(*) FROM iot_access_operation_log WHERE tenant_id=162) t162;

-- ============================================================================
-- 40) iot_ota_task (t1=7): firmware_id → ota_fw map
-- ============================================================================
INSERT INTO iot_ota_task
  (name, description, firmware_id, status, device_scope, device_total_count, device_success_count,
   creator, create_time, updater, update_time, deleted, tenant_id)
SELECT t.name, t.description,
       COALESCE(fwm.new_id, t.firmware_id),
       t.status, t.device_scope, t.device_total_count, t.device_success_count,
       t.creator, t.create_time, t.updater, t.update_time, t.deleted, 162
FROM iot_ota_task t
LEFT JOIN _tmp_map_ota_fw fwm ON fwm.old_id = t.firmware_id
WHERE t.tenant_id=1 ORDER BY t.id;

DROP TABLE IF EXISTS _tmp_map_ota_task;
CREATE TABLE _tmp_map_ota_task (old_id BIGINT PRIMARY KEY, new_id BIGINT NOT NULL) ENGINE=InnoDB;
INSERT INTO _tmp_map_ota_task (old_id, new_id)
SELECT a.old_id, b.new_id FROM
  (SELECT id AS old_id, ROW_NUMBER() OVER (ORDER BY id) rn FROM iot_ota_task WHERE tenant_id=1) a
  JOIN
  (SELECT id AS new_id, ROW_NUMBER() OVER (ORDER BY id) rn FROM iot_ota_task WHERE tenant_id=162) b
  ON a.rn = b.rn;

SELECT '[40] iot_ota_task' tag,
  (SELECT COUNT(*) FROM iot_ota_task WHERE tenant_id=1) t1,
  (SELECT COUNT(*) FROM iot_ota_task WHERE tenant_id=162) t162,
  (SELECT COUNT(*) FROM _tmp_map_ota_task) map;

-- ============================================================================
-- 41) iot_ota_task_record (t1=20): firmware_id, task_id, device_id, from_firmware_id
--     device_id 可能引用 ibms_device 或 iot_keding_device
-- ============================================================================
INSERT INTO iot_ota_task_record
  (firmware_id, task_id, device_id, from_firmware_id, status, progress, description,
   creator, create_time, updater, update_time, deleted, tenant_id)
SELECT COALESCE(fwm.new_id, t.firmware_id),
       COALESCE(tm.new_id, t.task_id),
       COALESCE(dm.new_id, kdm.new_id, t.device_id),
       COALESCE(fwm2.new_id, t.from_firmware_id),
       t.status, t.progress, t.description,
       t.creator, t.create_time, t.updater, t.update_time, t.deleted, 162
FROM iot_ota_task_record t
LEFT JOIN _tmp_map_ota_fw fwm ON fwm.old_id = t.firmware_id
LEFT JOIN _tmp_map_ota_fw fwm2 ON fwm2.old_id = t.from_firmware_id
LEFT JOIN _tmp_map_ota_task tm ON tm.old_id = t.task_id
LEFT JOIN _tmp_map_device dm ON dm.old_id = t.device_id
LEFT JOIN _tmp_map_keding_device kdm ON kdm.old_id = t.device_id
WHERE t.tenant_id=1 ORDER BY t.id;

SELECT '[41] iot_ota_task_record' tag,
  (SELECT COUNT(*) FROM iot_ota_task_record WHERE tenant_id=1) t1,
  (SELECT COUNT(*) FROM iot_ota_task_record WHERE tenant_id=162) t162;

SELECT '=== PART3 DONE ===' tag;
