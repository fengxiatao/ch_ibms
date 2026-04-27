-- ============================================================================
-- Phase 3 续跑: 修复 NOT NULL FK 孤儿问题(orphan device/channel),
-- 跳过备份+清空(已完�?; 仅做 INSERT
-- ============================================================================
USE ch_ibms;
SET SQL_SAFE_UPDATES=0;

-- 确保映射存在(idempotent rebuild)
DROP TABLE IF EXISTS _tmp_map_device;
CREATE TABLE _tmp_map_device (old_id BIGINT PRIMARY KEY, new_id BIGINT NOT NULL) ENGINE=InnoDB;
INSERT INTO _tmp_map_device SELECT t1.id, t162.id FROM ibms_device t1
JOIN ibms_device t162 ON t1.device_code=t162.device_code AND t162.tenant_id=162 WHERE t1.tenant_id=1;

DROP TABLE IF EXISTS _tmp_map_channel;
CREATE TABLE _tmp_map_channel (old_id BIGINT PRIMARY KEY, new_id BIGINT NOT NULL) ENGINE=InnoDB;
INSERT INTO _tmp_map_channel SELECT t1.id, t162.id FROM ibms_channel t1
JOIN ibms_channel t162 ON t1.code=t162.code AND t162.tenant_id=162 WHERE t1.tenant_id=1;

DROP TABLE IF EXISTS _tmp_map_space;
CREATE TABLE _tmp_map_space (old_id BIGINT PRIMARY KEY, new_id BIGINT NOT NULL) ENGINE=InnoDB;
INSERT INTO _tmp_map_space SELECT t1.id, t162.id FROM ibms_space t1
JOIN ibms_space t162 ON t1.space_code=t162.space_code AND t162.tenant_id=162 WHERE t1.tenant_id=1;

-- ============================================================================
-- A) 告警
-- ============================================================================
INSERT INTO iot_alarm_host
  (tenant_id, device_id, host_name, host_model, host_sn, zone_count, online_status, arm_status,
   alarm_status, system_status, last_query_time, location,
   remark, creator, create_time, updater, update_time, deleted, ip_address, port, account, password)
SELECT 162, COALESCE(dm.new_id, t.device_id), t.host_name, t.host_model,
       CASE WHEN t.host_sn IS NOT NULL AND t.host_sn<>'' THEN CONCAT(t.host_sn,'-ibms') ELSE t.host_sn END,
       t.zone_count, t.online_status, t.arm_status, t.alarm_status, t.system_status,
       t.last_query_time, t.location,
       CONCAT('__OLDID:', t.id, '|', IFNULL(t.remark,'')),
       t.creator, t.create_time, t.updater, t.update_time, t.deleted,
       CASE WHEN t.ip_address IS NOT NULL AND t.ip_address<>'' THEN CONCAT(t.ip_address,'-ibms') ELSE t.ip_address END,
       t.port, t.account, t.password
FROM iot_alarm_host t
LEFT JOIN _tmp_map_device dm ON dm.old_id = t.device_id
WHERE t.tenant_id=1;

DROP TABLE IF EXISTS _tmp_map_alarm_host;
CREATE TABLE _tmp_map_alarm_host (old_id BIGINT PRIMARY KEY, new_id BIGINT NOT NULL) ENGINE=InnoDB;
INSERT INTO _tmp_map_alarm_host SELECT CAST(SUBSTRING_INDEX(SUBSTRING(remark,9),'|',1) AS UNSIGNED), id
FROM iot_alarm_host WHERE tenant_id=162 AND remark LIKE '__OLDID:%';
UPDATE iot_alarm_host SET remark = NULLIF(SUBSTRING(remark, LOCATE('|',remark)+1),'')
WHERE tenant_id=162 AND remark LIKE '__OLDID:%';

INSERT INTO iot_alarm_partition
  (host_id, partition_no, partition_name, status, description, creator, create_time, updater, update_time, deleted, tenant_id)
SELECT hm.new_id, t.partition_no, t.partition_name, t.status,
       CONCAT('__OLDID:', t.id, '|', IFNULL(t.description,'')),
       t.creator, t.create_time, t.updater, t.update_time, t.deleted, 162
FROM iot_alarm_partition t
JOIN _tmp_map_alarm_host hm ON hm.old_id = t.host_id
WHERE t.tenant_id=1;

DROP TABLE IF EXISTS _tmp_map_alarm_partition;
CREATE TABLE _tmp_map_alarm_partition (old_id BIGINT PRIMARY KEY, new_id BIGINT NOT NULL) ENGINE=InnoDB;
INSERT INTO _tmp_map_alarm_partition SELECT CAST(SUBSTRING_INDEX(SUBSTRING(description,9),'|',1) AS UNSIGNED), id
FROM iot_alarm_partition WHERE tenant_id=162 AND description LIKE '__OLDID:%';
UPDATE iot_alarm_partition SET description = NULLIF(SUBSTRING(description, LOCATE('|',description)+1),'')
WHERE tenant_id=162 AND description LIKE '__OLDID:%';

INSERT INTO iot_alarm_zone
  (tenant_id, host_id, zone_no, zone_name, zone_type, area_location, zone_status, status, status_name,
   arm_status, alarm_status, is_armed, is_alarming, online_status, partition_id, is_important, is24h,
   alarm_count, last_alarm_time, remark, creator, create_time, updater, update_time, deleted)
SELECT 162, hm.new_id, t.zone_no, t.zone_name, t.zone_type, t.area_location, t.zone_status, t.status, t.status_name,
       t.arm_status, t.alarm_status, t.is_armed, t.is_alarming, t.online_status,
       pm.new_id, t.is_important, t.is24h, t.alarm_count, t.last_alarm_time,
       CONCAT('__OLDID:', t.id, '|', IFNULL(t.remark,'')),
       t.creator, t.create_time, t.updater, t.update_time, t.deleted
FROM iot_alarm_zone t
JOIN _tmp_map_alarm_host hm ON hm.old_id = t.host_id
LEFT JOIN _tmp_map_alarm_partition pm ON pm.old_id = t.partition_id
WHERE t.tenant_id=1;

DROP TABLE IF EXISTS _tmp_map_alarm_zone;
CREATE TABLE _tmp_map_alarm_zone (old_id BIGINT PRIMARY KEY, new_id BIGINT NOT NULL) ENGINE=InnoDB;
INSERT INTO _tmp_map_alarm_zone SELECT CAST(SUBSTRING_INDEX(SUBSTRING(remark,9),'|',1) AS UNSIGNED), id
FROM iot_alarm_zone WHERE tenant_id=162 AND remark LIKE '__OLDID:%';
UPDATE iot_alarm_zone SET remark = NULLIF(SUBSTRING(remark, LOCATE('|',remark)+1),'')
WHERE tenant_id=162 AND remark LIKE '__OLDID:%';

INSERT INTO iot_alarm_event
  (host_id, event_code, event_type, event_level, area_no, zone_no, user_no, sequence, event_desc, raw_data,
   is_new_event, is_handled, status, handled_by, handled_time, handle_remark,
   creator, create_time, updater, update_time, deleted, tenant_id)
SELECT hm.new_id, t.event_code, t.event_type, t.event_level, t.area_no, t.zone_no, t.user_no, t.sequence,
       t.event_desc, t.raw_data, t.is_new_event, t.is_handled, t.status, t.handled_by, t.handled_time, t.handle_remark,
       t.creator, t.create_time, t.updater, t.update_time, t.deleted, 162
FROM iot_alarm_event t
JOIN _tmp_map_alarm_host hm ON hm.old_id = t.host_id
WHERE t.tenant_id=1;

INSERT INTO iot_alarm_operation_log
  (host_id, partition_id, zone_id, operation_type, operation_time, operator_id, operator_name,
   result, error_message, request_id, tenant_id, creator, create_time, updater, update_time, deleted)
SELECT hm.new_id, pm.new_id, zm.new_id, t.operation_type, t.operation_time, t.operator_id, t.operator_name,
       t.result, t.error_message, t.request_id, 162, t.creator, t.create_time, t.updater, t.update_time, t.deleted
FROM iot_alarm_operation_log t
LEFT JOIN _tmp_map_alarm_host hm ON hm.old_id = t.host_id
LEFT JOIN _tmp_map_alarm_partition pm ON pm.old_id = t.partition_id
LEFT JOIN _tmp_map_alarm_zone zm ON zm.old_id = t.zone_id
WHERE t.tenant_id=1;

SELECT '[A]' tag,
  (SELECT COUNT(*) FROM iot_alarm_host WHERE tenant_id=162) host,
  (SELECT COUNT(*) FROM iot_alarm_partition WHERE tenant_id=162) part,
  (SELECT COUNT(*) FROM iot_alarm_zone WHERE tenant_id=162) zone,
  (SELECT COUNT(*) FROM iot_alarm_event WHERE tenant_id=162) event,
  (SELECT COUNT(*) FROM iot_alarm_operation_log WHERE tenant_id=162) op_log;

-- ============================================================================
-- B) 摄像�?(channel/device �?COALESCE)
-- ============================================================================
INSERT INTO iot_camera_preset
  (channel_id, preset_no, preset_name, description, pan, tilt, zoom,
   creator, create_time, updater, update_time, deleted, tenant_id)
SELECT COALESCE(cm.new_id, t.channel_id), t.preset_no, t.preset_name,
       CONCAT('__OLDID:', t.id, '|', IFNULL(t.description,'')),
       t.pan, t.tilt, t.zoom, t.creator, t.create_time, t.updater, t.update_time, t.deleted, 162
FROM iot_camera_preset t
LEFT JOIN _tmp_map_channel cm ON cm.old_id = t.channel_id
WHERE t.tenant_id=1;

DROP TABLE IF EXISTS _tmp_map_preset;
CREATE TABLE _tmp_map_preset (old_id BIGINT PRIMARY KEY, new_id BIGINT NOT NULL) ENGINE=InnoDB;
INSERT INTO _tmp_map_preset SELECT CAST(SUBSTRING_INDEX(SUBSTRING(description,9),'|',1) AS UNSIGNED), id
FROM iot_camera_preset WHERE tenant_id=162 AND description LIKE '__OLDID:%';
UPDATE iot_camera_preset SET description=NULLIF(SUBSTRING(description, LOCATE('|',description)+1),'')
WHERE tenant_id=162 AND description LIKE '__OLDID:%';

INSERT INTO iot_camera_cruise
  (channel_id, cruise_name, description, status, dwell_time, loop_enabled,
   creator, create_time, updater, update_time, deleted, tenant_id)
SELECT COALESCE(cm.new_id, t.channel_id), t.cruise_name,
       CONCAT('__OLDID:', t.id, '|', IFNULL(t.description,'')),
       t.status, t.dwell_time, t.loop_enabled,
       t.creator, t.create_time, t.updater, t.update_time, t.deleted, 162
FROM iot_camera_cruise t
LEFT JOIN _tmp_map_channel cm ON cm.old_id = t.channel_id
WHERE t.tenant_id=1;

DROP TABLE IF EXISTS _tmp_map_cruise;
CREATE TABLE _tmp_map_cruise (old_id BIGINT PRIMARY KEY, new_id BIGINT NOT NULL) ENGINE=InnoDB;
INSERT INTO _tmp_map_cruise SELECT CAST(SUBSTRING_INDEX(SUBSTRING(description,9),'|',1) AS UNSIGNED), id
FROM iot_camera_cruise WHERE tenant_id=162 AND description LIKE '__OLDID:%';
UPDATE iot_camera_cruise SET description=NULLIF(SUBSTRING(description, LOCATE('|',description)+1),'')
WHERE tenant_id=162 AND description LIKE '__OLDID:%';

INSERT INTO iot_camera_cruise_point
  (cruise_id, preset_id, sort_order, dwell_time,
   creator, create_time, updater, update_time, deleted, tenant_id)
SELECT crm.new_id, COALESCE(pm.new_id, t.preset_id), t.sort_order, t.dwell_time,
       t.creator, t.create_time, t.updater, t.update_time, t.deleted, 162
FROM iot_camera_cruise_point t
JOIN _tmp_map_cruise crm ON crm.old_id = t.cruise_id
LEFT JOIN _tmp_map_preset pm ON pm.old_id = t.preset_id
WHERE t.tenant_id=1;

INSERT INTO iot_camera_recording
  (channel_id, channel_name, recording_url, file_size, duration, recording_type, start_time, end_time,
   status, error_msg, creator, create_time, updater, update_time, deleted, tenant_id)
SELECT COALESCE(cm.new_id, t.channel_id), t.channel_name, t.recording_url, t.file_size, t.duration, t.recording_type,
       t.start_time, t.end_time, t.status, t.error_msg,
       t.creator, t.create_time, t.updater, t.update_time, t.deleted, 162
FROM iot_camera_recording t
LEFT JOIN _tmp_map_channel cm ON cm.old_id = t.channel_id
WHERE t.tenant_id=1;

INSERT INTO iot_camera_snapshot
  (channel_id, device_id, channel_name, snapshot_url, snapshot_path, file_size, width, height,
   capture_time, snapshot_type, trigger_event, event_type, description, is_processed, processor,
   process_time, process_remark, creator, create_time, updater, update_time, deleted, tenant_id)
SELECT COALESCE(cm.new_id, t.channel_id), COALESCE(dm.new_id, t.device_id), t.channel_name, t.snapshot_url, t.snapshot_path,
       t.file_size, t.width, t.height, t.capture_time, t.snapshot_type, t.trigger_event, t.event_type, t.description,
       t.is_processed, t.processor, t.process_time, t.process_remark,
       t.creator, t.create_time, t.updater, t.update_time, t.deleted, 162
FROM iot_camera_snapshot t
LEFT JOIN _tmp_map_channel cm ON cm.old_id = t.channel_id
LEFT JOIN _tmp_map_device  dm ON dm.old_id = t.device_id
WHERE t.tenant_id=1;

SELECT '[B]' tag,
  (SELECT COUNT(*) FROM iot_camera_preset WHERE tenant_id=162) preset,
  (SELECT COUNT(*) FROM iot_camera_cruise WHERE tenant_id=162) cruise,
  (SELECT COUNT(*) FROM iot_camera_cruise_point WHERE tenant_id=162) cp,
  (SELECT COUNT(*) FROM iot_camera_recording WHERE tenant_id=162) rec,
  (SELECT COUNT(*) FROM iot_camera_snapshot WHERE tenant_id=162) snap;

-- ============================================================================
-- C) 云防
-- ============================================================================
INSERT INTO iot_cloud_defense_mode
  (tenant_id, mode_code, mode_name, icon, status_text, sort, enabled,
   creator, create_time, updater, update_time, deleted)
SELECT 162, CONCAT(t.mode_code,'-ibms'), t.mode_name, t.icon, t.status_text, t.sort, t.enabled,
       t.creator, t.create_time, t.updater, t.update_time, t.deleted
FROM iot_cloud_defense_mode t WHERE t.tenant_id=1;

INSERT INTO iot_cloud_defense_area
  (tenant_id, area_code, area_name, area_type, space_id, layout_x, layout_y, layout_width, layout_height,
   detail_text, sort, enabled, creator, create_time, updater, update_time, deleted)
SELECT 162, CONCAT(t.area_code,'-ibms'), t.area_name, t.area_type, t.space_id, t.layout_x, t.layout_y,
       t.layout_width, t.layout_height,
       CONCAT('__OLDID:', t.id, '|', IFNULL(t.detail_text,'')),
       t.sort, t.enabled, t.creator, t.create_time, t.updater, t.update_time, t.deleted
FROM iot_cloud_defense_area t WHERE t.tenant_id=1;

DROP TABLE IF EXISTS _tmp_map_cd_area;
CREATE TABLE _tmp_map_cd_area (old_id BIGINT PRIMARY KEY, new_id BIGINT NOT NULL) ENGINE=InnoDB;
INSERT INTO _tmp_map_cd_area SELECT CAST(SUBSTRING_INDEX(SUBSTRING(detail_text,9),'|',1) AS UNSIGNED), id
FROM iot_cloud_defense_area WHERE tenant_id=162 AND detail_text LIKE '__OLDID:%';
UPDATE iot_cloud_defense_area SET detail_text=NULLIF(SUBSTRING(detail_text, LOCATE('|',detail_text)+1),'')
WHERE tenant_id=162 AND detail_text LIKE '__OLDID:%';

INSERT INTO iot_cloud_defense_area_device_rel
  (tenant_id, area_id, device_id, channel_id, sort, creator, create_time, updater, update_time, deleted)
SELECT 162, am.new_id, COALESCE(dm.new_id, t.device_id), COALESCE(cm.new_id, t.channel_id), t.sort,
       t.creator, t.create_time, t.updater, t.update_time, t.deleted
FROM iot_cloud_defense_area_device_rel t
JOIN _tmp_map_cd_area am ON am.old_id = t.area_id
LEFT JOIN _tmp_map_device dm ON dm.old_id = t.device_id
LEFT JOIN _tmp_map_channel cm ON cm.old_id = t.channel_id
WHERE t.tenant_id=1;

INSERT INTO iot_cloud_defense_point
  (tenant_id, area_id, device_id, channel_id, point_code, point_name, point_type,
   layout_x, layout_y, armed_status, alarm_status, online_status, sort, enabled,
   creator, create_time, updater, update_time, deleted)
SELECT 162, am.new_id, COALESCE(dm.new_id, t.device_id), COALESCE(cm.new_id, t.channel_id),
       CONCAT(t.point_code,'-ibms'), t.point_name, t.point_type, t.layout_x, t.layout_y,
       t.armed_status, t.alarm_status, t.online_status, t.sort, t.enabled,
       t.creator, t.create_time, t.updater, t.update_time, t.deleted
FROM iot_cloud_defense_point t
JOIN _tmp_map_cd_area am ON am.old_id = t.area_id
LEFT JOIN _tmp_map_device dm ON dm.old_id = t.device_id
LEFT JOIN _tmp_map_channel cm ON cm.old_id = t.channel_id
WHERE t.tenant_id=1;

INSERT INTO iot_cloud_defense_score_log
  (tenant_id, score, score_level, score_time, remark, creator, create_time, updater, update_time, deleted)
SELECT 162, score, score_level, score_time, remark, creator, create_time, updater, update_time, deleted
FROM iot_cloud_defense_score_log WHERE tenant_id=1;

SELECT '[C]' tag,
  (SELECT COUNT(*) FROM iot_cloud_defense_mode WHERE tenant_id=162) mode_,
  (SELECT COUNT(*) FROM iot_cloud_defense_area WHERE tenant_id=162) area,
  (SELECT COUNT(*) FROM iot_cloud_defense_area_device_rel WHERE tenant_id=162) rel,
  (SELECT COUNT(*) FROM iot_cloud_defense_point WHERE tenant_id=162) point_,
  (SELECT COUNT(*) FROM iot_cloud_defense_score_log WHERE tenant_id=162) sc;
