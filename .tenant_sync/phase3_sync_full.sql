-- ============================================================================
-- Phase 3 — 智慧安防 数据同步 (tenant_id=1 → tenant_id=162)
-- 实例: 127.0.0.1:3306 / ch_ibms
-- 日期: 20260426
-- ----------------------------------------------------------------------------
-- 范围: 28 张表(去除 t1=0 的 5 张及全部 security_*)
-- 后缀字段(无 tenant_id 的 UK):
--   iot_cloud_defense_area.area_code, mode.mode_code, point.point_code
-- 标记字段策略:
--   父表插入时把 t1.id 暂存到 marker 字段(如 remark/description),
--   插入后用 SELECT 回查建立 (old_id, new_id) 映射, 最后清除标记
-- ============================================================================

USE ch_ibms;
SET SQL_SAFE_UPDATES = 0;
SET @D = '20260426';

-- ============================================================================
-- 1) 重建 Phase 2 实体的 ID 映射(device/channel/space)
-- ============================================================================
DROP TABLE IF EXISTS _tmp_map_device;
CREATE TABLE _tmp_map_device (old_id BIGINT PRIMARY KEY, new_id BIGINT NOT NULL) ENGINE=InnoDB;
INSERT INTO _tmp_map_device (old_id, new_id)
SELECT t1.id, t162.id FROM ibms_device t1
JOIN ibms_device t162 ON t1.device_code = t162.device_code AND t162.tenant_id = 162
WHERE t1.tenant_id = 1;

DROP TABLE IF EXISTS _tmp_map_channel;
CREATE TABLE _tmp_map_channel (old_id BIGINT PRIMARY KEY, new_id BIGINT NOT NULL) ENGINE=InnoDB;
INSERT INTO _tmp_map_channel (old_id, new_id)
SELECT t1.id, t162.id FROM ibms_channel t1
JOIN ibms_channel t162 ON t1.code = t162.code AND t162.tenant_id = 162
WHERE t1.tenant_id = 1;

DROP TABLE IF EXISTS _tmp_map_space;
CREATE TABLE _tmp_map_space (old_id BIGINT PRIMARY KEY, new_id BIGINT NOT NULL) ENGINE=InnoDB;
INSERT INTO _tmp_map_space (old_id, new_id)
SELECT t1.id, t162.id FROM ibms_space t1
JOIN ibms_space t162 ON t1.space_code = t162.space_code AND t162.tenant_id = 162
WHERE t1.tenant_id = 1;

SELECT '[map]' tag, (SELECT COUNT(*) FROM _tmp_map_device) device,
       (SELECT COUNT(*) FROM _tmp_map_channel) channel,
       (SELECT COUNT(*) FROM _tmp_map_space) space;

-- ============================================================================
-- 2) 备份 t162 现有数据 (28 张表; 大部分为空)
-- ============================================================================
DROP TABLE IF EXISTS bak_iot_alarm_host_t162_20260426;
CREATE TABLE bak_iot_alarm_host_t162_20260426 AS SELECT * FROM iot_alarm_host WHERE tenant_id=162;
DROP TABLE IF EXISTS bak_iot_alarm_partition_t162_20260426;
CREATE TABLE bak_iot_alarm_partition_t162_20260426 AS SELECT * FROM iot_alarm_partition WHERE tenant_id=162;
DROP TABLE IF EXISTS bak_iot_alarm_zone_t162_20260426;
CREATE TABLE bak_iot_alarm_zone_t162_20260426 AS SELECT * FROM iot_alarm_zone WHERE tenant_id=162;
DROP TABLE IF EXISTS bak_iot_alarm_event_t162_20260426;
CREATE TABLE bak_iot_alarm_event_t162_20260426 AS SELECT * FROM iot_alarm_event WHERE tenant_id=162;
DROP TABLE IF EXISTS bak_iot_alarm_operation_log_t162_20260426;
CREATE TABLE bak_iot_alarm_operation_log_t162_20260426 AS SELECT * FROM iot_alarm_operation_log WHERE tenant_id=162;
DROP TABLE IF EXISTS bak_iot_camera_cruise_t162_20260426;
CREATE TABLE bak_iot_camera_cruise_t162_20260426 AS SELECT * FROM iot_camera_cruise WHERE tenant_id=162;
DROP TABLE IF EXISTS bak_iot_camera_cruise_point_t162_20260426;
CREATE TABLE bak_iot_camera_cruise_point_t162_20260426 AS SELECT * FROM iot_camera_cruise_point WHERE tenant_id=162;
DROP TABLE IF EXISTS bak_iot_camera_preset_t162_20260426;
CREATE TABLE bak_iot_camera_preset_t162_20260426 AS SELECT * FROM iot_camera_preset WHERE tenant_id=162;
DROP TABLE IF EXISTS bak_iot_camera_recording_t162_20260426;
CREATE TABLE bak_iot_camera_recording_t162_20260426 AS SELECT * FROM iot_camera_recording WHERE tenant_id=162;
DROP TABLE IF EXISTS bak_iot_camera_snapshot_t162_20260426;
CREATE TABLE bak_iot_camera_snapshot_t162_20260426 AS SELECT * FROM iot_camera_snapshot WHERE tenant_id=162;
DROP TABLE IF EXISTS bak_iot_cloud_defense_area_t162_20260426;
CREATE TABLE bak_iot_cloud_defense_area_t162_20260426 AS SELECT * FROM iot_cloud_defense_area WHERE tenant_id=162;
DROP TABLE IF EXISTS bak_iot_cloud_defense_area_device_rel_t162_20260426;
CREATE TABLE bak_iot_cloud_defense_area_device_rel_t162_20260426 AS SELECT * FROM iot_cloud_defense_area_device_rel WHERE tenant_id=162;
DROP TABLE IF EXISTS bak_iot_cloud_defense_mode_t162_20260426;
CREATE TABLE bak_iot_cloud_defense_mode_t162_20260426 AS SELECT * FROM iot_cloud_defense_mode WHERE tenant_id=162;
DROP TABLE IF EXISTS bak_iot_cloud_defense_point_t162_20260426;
CREATE TABLE bak_iot_cloud_defense_point_t162_20260426 AS SELECT * FROM iot_cloud_defense_point WHERE tenant_id=162;
DROP TABLE IF EXISTS bak_iot_cloud_defense_score_log_t162_20260426;
CREATE TABLE bak_iot_cloud_defense_score_log_t162_20260426 AS SELECT * FROM iot_cloud_defense_score_log WHERE tenant_id=162;
DROP TABLE IF EXISTS bak_iot_video_view_group_t162_20260426;
CREATE TABLE bak_iot_video_view_group_t162_20260426 AS SELECT * FROM iot_video_view_group WHERE tenant_id=162;
DROP TABLE IF EXISTS bak_iot_video_view_t162_20260426;
CREATE TABLE bak_iot_video_view_t162_20260426 AS SELECT * FROM iot_video_view WHERE tenant_id=162;
DROP TABLE IF EXISTS bak_iot_video_view_pane_t162_20260426;
CREATE TABLE bak_iot_video_view_pane_t162_20260426 AS SELECT * FROM iot_video_view_pane WHERE tenant_id=162;
DROP TABLE IF EXISTS bak_iot_video_patrol_plan_t162_20260426;
CREATE TABLE bak_iot_video_patrol_plan_t162_20260426 AS SELECT * FROM iot_video_patrol_plan WHERE tenant_id=162;
DROP TABLE IF EXISTS bak_iot_video_patrol_task_t162_20260426;
CREATE TABLE bak_iot_video_patrol_task_t162_20260426 AS SELECT * FROM iot_video_patrol_task WHERE tenant_id=162;
DROP TABLE IF EXISTS bak_iot_video_patrol_scene_t162_20260426;
CREATE TABLE bak_iot_video_patrol_scene_t162_20260426 AS SELECT * FROM iot_video_patrol_scene WHERE tenant_id=162;
DROP TABLE IF EXISTS bak_iot_video_patrol_scene_channel_t162_20260426;
CREATE TABLE bak_iot_video_patrol_scene_channel_t162_20260426 AS SELECT * FROM iot_video_patrol_scene_channel WHERE tenant_id=162;
DROP TABLE IF EXISTS bak_iot_video_patrol_schedule_t162_20260426;
CREATE TABLE bak_iot_video_patrol_schedule_t162_20260426 AS SELECT * FROM iot_video_patrol_schedule WHERE tenant_id=162;
DROP TABLE IF EXISTS bak_iot_video_inspection_task_t162_20260426;
CREATE TABLE bak_iot_video_inspection_task_t162_20260426 AS SELECT * FROM iot_video_inspection_task WHERE tenant_id=162;
DROP TABLE IF EXISTS bak_iot_epatrol_person_t162_20260426;
CREATE TABLE bak_iot_epatrol_person_t162_20260426 AS SELECT * FROM iot_epatrol_person WHERE tenant_id=162;
DROP TABLE IF EXISTS bak_iot_epatrol_route_t162_20260426;
CREATE TABLE bak_iot_epatrol_route_t162_20260426 AS SELECT * FROM iot_epatrol_route WHERE tenant_id=162;
DROP TABLE IF EXISTS bak_iot_epatrol_route_point_t162_20260426;
CREATE TABLE bak_iot_epatrol_route_point_t162_20260426 AS SELECT * FROM iot_epatrol_route_point WHERE tenant_id=162;
DROP TABLE IF EXISTS bak_iot_epatrol_point_t162_20260426;
CREATE TABLE bak_iot_epatrol_point_t162_20260426 AS SELECT * FROM iot_epatrol_point WHERE tenant_id=162;
DROP TABLE IF EXISTS bak_iot_epatrol_plan_t162_20260426;
CREATE TABLE bak_iot_epatrol_plan_t162_20260426 AS SELECT * FROM iot_epatrol_plan WHERE tenant_id=162;
DROP TABLE IF EXISTS bak_iot_epatrol_plan_period_t162_20260426;
CREATE TABLE bak_iot_epatrol_plan_period_t162_20260426 AS SELECT * FROM iot_epatrol_plan_period WHERE tenant_id=162;
DROP TABLE IF EXISTS bak_iot_epatrol_task_t162_20260426;
CREATE TABLE bak_iot_epatrol_task_t162_20260426 AS SELECT * FROM iot_epatrol_task WHERE tenant_id=162;
DROP TABLE IF EXISTS bak_iot_epatrol_task_record_t162_20260426;
CREATE TABLE bak_iot_epatrol_task_record_t162_20260426 AS SELECT * FROM iot_epatrol_task_record WHERE tenant_id=162;

-- ============================================================================
-- 3) 清空 t162 (倒序: 子表先清, 父表后清)
-- ============================================================================
DELETE FROM iot_epatrol_task_record WHERE tenant_id=162;
DELETE FROM iot_epatrol_task WHERE tenant_id=162;
DELETE FROM iot_epatrol_plan_period WHERE tenant_id=162;
DELETE FROM iot_epatrol_plan WHERE tenant_id=162;
DELETE FROM iot_epatrol_route_point WHERE tenant_id=162;
DELETE FROM iot_epatrol_route WHERE tenant_id=162;
DELETE FROM iot_epatrol_point WHERE tenant_id=162;
DELETE FROM iot_epatrol_person WHERE tenant_id=162;
DELETE FROM iot_video_inspection_task WHERE tenant_id=162;
DELETE FROM iot_video_patrol_schedule WHERE tenant_id=162;
DELETE FROM iot_video_patrol_scene_channel WHERE tenant_id=162;
DELETE FROM iot_video_patrol_scene WHERE tenant_id=162;
DELETE FROM iot_video_patrol_task WHERE tenant_id=162;
DELETE FROM iot_video_patrol_plan WHERE tenant_id=162;
DELETE FROM iot_video_view_pane WHERE tenant_id=162;
DELETE FROM iot_video_view WHERE tenant_id=162;
DELETE FROM iot_video_view_group WHERE tenant_id=162;
DELETE FROM iot_cloud_defense_score_log WHERE tenant_id=162;
DELETE FROM iot_cloud_defense_point WHERE tenant_id=162;
DELETE FROM iot_cloud_defense_area_device_rel WHERE tenant_id=162;
DELETE FROM iot_cloud_defense_mode WHERE tenant_id=162;
DELETE FROM iot_cloud_defense_area WHERE tenant_id=162;
DELETE FROM iot_camera_snapshot WHERE tenant_id=162;
DELETE FROM iot_camera_recording WHERE tenant_id=162;
DELETE FROM iot_camera_cruise_point WHERE tenant_id=162;
DELETE FROM iot_camera_cruise WHERE tenant_id=162;
DELETE FROM iot_camera_preset WHERE tenant_id=162;
DELETE FROM iot_alarm_operation_log WHERE tenant_id=162;
DELETE FROM iot_alarm_event WHERE tenant_id=162;
DELETE FROM iot_alarm_zone WHERE tenant_id=162;
DELETE FROM iot_alarm_partition WHERE tenant_id=162;
DELETE FROM iot_alarm_host WHERE tenant_id=162;

SELECT '[clear] done' tag;
 
-- ============================================================================
-- A) 告警 (5 张)
-- ============================================================================

-- A1) iot_alarm_host (t1=2): device_id 通过 _tmp_map_device 重写; host_sn/ip_address 加后缀
INSERT INTO iot_alarm_host
  (tenant_id, device_id, host_name, host_model, host_sn, zone_count, online_status, arm_status,
   alarm_status, system_status, last_query_time, location,
   remark, creator, create_time, updater, update_time, deleted, ip_address, port, account, password)
SELECT 162, dm.new_id, t.host_name, t.host_model,
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
INSERT INTO _tmp_map_alarm_host (old_id, new_id)
SELECT CAST(SUBSTRING_INDEX(SUBSTRING(remark, 10), '|', 1) AS UNSIGNED), id
FROM iot_alarm_host WHERE tenant_id=162 AND remark LIKE '__OLDID:%';

UPDATE iot_alarm_host SET remark = NULLIF(SUBSTRING(remark, LOCATE('|', remark)+1), '')
WHERE tenant_id=162 AND remark LIKE '__OLDID:%';

-- A2) iot_alarm_partition (t1=1): host_id 重写
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
INSERT INTO _tmp_map_alarm_partition (old_id, new_id)
SELECT CAST(SUBSTRING_INDEX(SUBSTRING(description, 10), '|', 1) AS UNSIGNED), id
FROM iot_alarm_partition WHERE tenant_id=162 AND description LIKE '__OLDID:%';

UPDATE iot_alarm_partition SET description = NULLIF(SUBSTRING(description, LOCATE('|', description)+1), '')
WHERE tenant_id=162 AND description LIKE '__OLDID:%';

-- A3) iot_alarm_zone (t1=13): host_id, partition_id 重写
INSERT INTO iot_alarm_zone
  (tenant_id, host_id, zone_no, zone_name, zone_type, area_location, zone_status, status, status_name,
   arm_status, alarm_status, is_armed, is_alarming, online_status, partition_id, is_important, is24h,
   alarm_count, last_alarm_time, remark, creator, create_time, updater, update_time, deleted)
SELECT 162, hm.new_id, t.zone_no, t.zone_name, t.zone_type, t.area_location, t.zone_status, t.status, t.status_name,
       t.arm_status, t.alarm_status, t.is_armed, t.is_alarming, t.online_status,
       pm.new_id, t.is_important, t.is24h,
       t.alarm_count, t.last_alarm_time,
       CONCAT('__OLDID:', t.id, '|', IFNULL(t.remark,'')),
       t.creator, t.create_time, t.updater, t.update_time, t.deleted
FROM iot_alarm_zone t
JOIN _tmp_map_alarm_host hm ON hm.old_id = t.host_id
LEFT JOIN _tmp_map_alarm_partition pm ON pm.old_id = t.partition_id
WHERE t.tenant_id=1;

DROP TABLE IF EXISTS _tmp_map_alarm_zone;
CREATE TABLE _tmp_map_alarm_zone (old_id BIGINT PRIMARY KEY, new_id BIGINT NOT NULL) ENGINE=InnoDB;
INSERT INTO _tmp_map_alarm_zone (old_id, new_id)
SELECT CAST(SUBSTRING_INDEX(SUBSTRING(remark, 10), '|', 1) AS UNSIGNED), id
FROM iot_alarm_zone WHERE tenant_id=162 AND remark LIKE '__OLDID:%';

UPDATE iot_alarm_zone SET remark = NULLIF(SUBSTRING(remark, LOCATE('|', remark)+1), '')
WHERE tenant_id=162 AND remark LIKE '__OLDID:%';

-- A4) iot_alarm_event (t1=2468): host_id 重写; 大表但单语句可处理
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

-- A5) iot_alarm_operation_log (t1=103): host_id, partition_id, zone_id 重写
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

SELECT '[A] alarm done' tag,
  (SELECT COUNT(*) FROM iot_alarm_host WHERE tenant_id=162) host,
  (SELECT COUNT(*) FROM iot_alarm_partition WHERE tenant_id=162) partition_,
  (SELECT COUNT(*) FROM iot_alarm_zone WHERE tenant_id=162) zone_,
  (SELECT COUNT(*) FROM iot_alarm_event WHERE tenant_id=162) event,
  (SELECT COUNT(*) FROM iot_alarm_operation_log WHERE tenant_id=162) op_log;

-- ============================================================================
-- B) 摄像机 (5 张)
-- ============================================================================

-- B1) iot_camera_preset (t1=4): channel_id 重写
INSERT INTO iot_camera_preset
  (channel_id, preset_no, preset_name, description, pan, tilt, zoom,
   creator, create_time, updater, update_time, deleted, tenant_id)
SELECT cm.new_id, t.preset_no, t.preset_name,
       CONCAT('__OLDID:', t.id, '|', IFNULL(t.description,'')),
       t.pan, t.tilt, t.zoom,
       t.creator, t.create_time, t.updater, t.update_time, t.deleted, 162
FROM iot_camera_preset t
JOIN _tmp_map_channel cm ON cm.old_id = t.channel_id
WHERE t.tenant_id=1;

DROP TABLE IF EXISTS _tmp_map_preset;
CREATE TABLE _tmp_map_preset (old_id BIGINT PRIMARY KEY, new_id BIGINT NOT NULL) ENGINE=InnoDB;
INSERT INTO _tmp_map_preset (old_id, new_id)
SELECT CAST(SUBSTRING_INDEX(SUBSTRING(description,10),'|',1) AS UNSIGNED), id
FROM iot_camera_preset WHERE tenant_id=162 AND description LIKE '__OLDID:%';

UPDATE iot_camera_preset SET description = NULLIF(SUBSTRING(description, LOCATE('|', description)+1), '')
WHERE tenant_id=162 AND description LIKE '__OLDID:%';

-- B2) iot_camera_cruise (t1=13): channel_id 重写
INSERT INTO iot_camera_cruise
  (channel_id, cruise_name, description, status, dwell_time, loop_enabled,
   creator, create_time, updater, update_time, deleted, tenant_id)
SELECT cm.new_id, t.cruise_name,
       CONCAT('__OLDID:', t.id, '|', IFNULL(t.description,'')),
       t.status, t.dwell_time, t.loop_enabled,
       t.creator, t.create_time, t.updater, t.update_time, t.deleted, 162
FROM iot_camera_cruise t
JOIN _tmp_map_channel cm ON cm.old_id = t.channel_id
WHERE t.tenant_id=1;

DROP TABLE IF EXISTS _tmp_map_cruise;
CREATE TABLE _tmp_map_cruise (old_id BIGINT PRIMARY KEY, new_id BIGINT NOT NULL) ENGINE=InnoDB;
INSERT INTO _tmp_map_cruise (old_id, new_id)
SELECT CAST(SUBSTRING_INDEX(SUBSTRING(description,10),'|',1) AS UNSIGNED), id
FROM iot_camera_cruise WHERE tenant_id=162 AND description LIKE '__OLDID:%';

UPDATE iot_camera_cruise SET description = NULLIF(SUBSTRING(description, LOCATE('|', description)+1), '')
WHERE tenant_id=162 AND description LIKE '__OLDID:%';

-- B3) iot_camera_cruise_point (t1=66): cruise_id, preset_id 重写
INSERT INTO iot_camera_cruise_point
  (cruise_id, preset_id, sort_order, dwell_time,
   creator, create_time, updater, update_time, deleted, tenant_id)
SELECT crm.new_id, pm.new_id, t.sort_order, t.dwell_time,
       t.creator, t.create_time, t.updater, t.update_time, t.deleted, 162
FROM iot_camera_cruise_point t
JOIN _tmp_map_cruise crm ON crm.old_id = t.cruise_id
LEFT JOIN _tmp_map_preset pm ON pm.old_id = t.preset_id
WHERE t.tenant_id=1;

-- B4) iot_camera_recording (t1=3): channel_id 重写
INSERT INTO iot_camera_recording
  (channel_id, channel_name, recording_url, file_size, duration, recording_type, start_time, end_time,
   status, error_msg, creator, create_time, updater, update_time, deleted, tenant_id)
SELECT cm.new_id, t.channel_name, t.recording_url, t.file_size, t.duration, t.recording_type,
       t.start_time, t.end_time, t.status, t.error_msg,
       t.creator, t.create_time, t.updater, t.update_time, t.deleted, 162
FROM iot_camera_recording t
JOIN _tmp_map_channel cm ON cm.old_id = t.channel_id
WHERE t.tenant_id=1;

-- B5) iot_camera_snapshot (t1=14): channel_id, device_id 重写
INSERT INTO iot_camera_snapshot
  (channel_id, device_id, channel_name, snapshot_url, snapshot_path, file_size, width, height,
   capture_time, snapshot_type, trigger_event, event_type, description, is_processed, processor,
   process_time, process_remark, creator, create_time, updater, update_time, deleted, tenant_id)
SELECT cm.new_id, dm.new_id, t.channel_name, t.snapshot_url, t.snapshot_path, t.file_size, t.width, t.height,
       t.capture_time, t.snapshot_type, t.trigger_event, t.event_type, t.description, t.is_processed, t.processor,
       t.process_time, t.process_remark, t.creator, t.create_time, t.updater, t.update_time, t.deleted, 162
FROM iot_camera_snapshot t
LEFT JOIN _tmp_map_channel cm ON cm.old_id = t.channel_id
LEFT JOIN _tmp_map_device  dm ON dm.old_id = t.device_id
WHERE t.tenant_id=1;

SELECT '[B] camera done' tag,
  (SELECT COUNT(*) FROM iot_camera_preset WHERE tenant_id=162) preset,
  (SELECT COUNT(*) FROM iot_camera_cruise WHERE tenant_id=162) cruise,
  (SELECT COUNT(*) FROM iot_camera_cruise_point WHERE tenant_id=162) cruise_point,
  (SELECT COUNT(*) FROM iot_camera_recording WHERE tenant_id=162) recording,
  (SELECT COUNT(*) FROM iot_camera_snapshot WHERE tenant_id=162) snapshot_;
-- ============================================================================
-- C) 云防 (5 张)
-- ============================================================================

-- C1) iot_cloud_defense_mode (t1=6): mode_code 加 -ibms
INSERT INTO iot_cloud_defense_mode
  (tenant_id, mode_code, mode_name, icon, status_text, sort, enabled,
   creator, create_time, updater, update_time, deleted)
SELECT 162, CONCAT(t.mode_code,'-ibms'), t.mode_name, t.icon, t.status_text, t.sort, t.enabled,
       t.creator, t.create_time, t.updater, t.update_time, t.deleted
FROM iot_cloud_defense_mode t WHERE t.tenant_id=1;

-- C2) iot_cloud_defense_area (t1=5): area_code 加 -ibms; space_id 多义保留
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
INSERT INTO _tmp_map_cd_area (old_id, new_id)
SELECT CAST(SUBSTRING_INDEX(SUBSTRING(detail_text,10),'|',1) AS UNSIGNED), id
FROM iot_cloud_defense_area WHERE tenant_id=162 AND detail_text LIKE '__OLDID:%';

UPDATE iot_cloud_defense_area SET detail_text = NULLIF(SUBSTRING(detail_text, LOCATE('|', detail_text)+1), '')
WHERE tenant_id=162 AND detail_text LIKE '__OLDID:%';

-- C3) iot_cloud_defense_area_device_rel (t1=6): area_id, device_id, channel_id 重写
INSERT INTO iot_cloud_defense_area_device_rel
  (tenant_id, area_id, device_id, channel_id, sort,
   creator, create_time, updater, update_time, deleted)
SELECT 162, am.new_id, dm.new_id, cm.new_id, t.sort,
       t.creator, t.create_time, t.updater, t.update_time, t.deleted
FROM iot_cloud_defense_area_device_rel t
JOIN _tmp_map_cd_area am ON am.old_id = t.area_id
LEFT JOIN _tmp_map_device dm ON dm.old_id = t.device_id
LEFT JOIN _tmp_map_channel cm ON cm.old_id = t.channel_id
WHERE t.tenant_id=1;

-- C4) iot_cloud_defense_point (t1=6): area_id, device_id, channel_id 重写; point_code 加 -ibms
INSERT INTO iot_cloud_defense_point
  (tenant_id, area_id, device_id, channel_id, point_code, point_name, point_type,
   layout_x, layout_y, armed_status, alarm_status, online_status, sort, enabled,
   creator, create_time, updater, update_time, deleted)
SELECT 162, am.new_id, dm.new_id, cm.new_id, CONCAT(t.point_code,'-ibms'),
       t.point_name, t.point_type, t.layout_x, t.layout_y, t.armed_status, t.alarm_status, t.online_status,
       t.sort, t.enabled, t.creator, t.create_time, t.updater, t.update_time, t.deleted
FROM iot_cloud_defense_point t
JOIN _tmp_map_cd_area am ON am.old_id = t.area_id
LEFT JOIN _tmp_map_device dm ON dm.old_id = t.device_id
LEFT JOIN _tmp_map_channel cm ON cm.old_id = t.channel_id
WHERE t.tenant_id=1;

-- C5) iot_cloud_defense_score_log (t1=1): 无 FK
INSERT INTO iot_cloud_defense_score_log
  (tenant_id, score, score_level, score_time, remark,
   creator, create_time, updater, update_time, deleted)
SELECT 162, score, score_level, score_time, remark,
       creator, create_time, updater, update_time, deleted
FROM iot_cloud_defense_score_log WHERE tenant_id=1;

SELECT '[C] cloud_defense done' tag,
  (SELECT COUNT(*) FROM iot_cloud_defense_mode WHERE tenant_id=162) mode_,
  (SELECT COUNT(*) FROM iot_cloud_defense_area WHERE tenant_id=162) area,
  (SELECT COUNT(*) FROM iot_cloud_defense_area_device_rel WHERE tenant_id=162) rel,
  (SELECT COUNT(*) FROM iot_cloud_defense_point WHERE tenant_id=162) point_,
  (SELECT COUNT(*) FROM iot_cloud_defense_score_log WHERE tenant_id=162) score_log;

-- ============================================================================
-- D) 视频视图 (3 张): group → view → pane
-- ============================================================================

-- D1) iot_video_view_group (t1=4): icon 用作标记
INSERT INTO iot_video_view_group
  (name, icon, sort, creator, create_time, updater, update_time, deleted, tenant_id)
SELECT t.name, CONCAT('__OLDID:', t.id, '|', IFNULL(t.icon,'')),
       t.sort, t.creator, t.create_time, t.updater, t.update_time, t.deleted, 162
FROM iot_video_view_group t WHERE t.tenant_id=1;

DROP TABLE IF EXISTS _tmp_map_view_group;
CREATE TABLE _tmp_map_view_group (old_id BIGINT PRIMARY KEY, new_id BIGINT NOT NULL) ENGINE=InnoDB;
INSERT INTO _tmp_map_view_group (old_id, new_id)
SELECT CAST(SUBSTRING_INDEX(SUBSTRING(icon,10),'|',1) AS UNSIGNED), id
FROM iot_video_view_group WHERE tenant_id=162 AND icon LIKE '__OLDID:%';

UPDATE iot_video_view_group SET icon = NULLIF(SUBSTRING(icon, LOCATE('|', icon)+1), '')
WHERE tenant_id=162 AND icon LIKE '__OLDID:%';

-- D2) iot_video_view (t1=17): group_ids 形如 "1;2" 需重写为 t162 ID
INSERT INTO iot_video_view
  (name, group_ids, grid_layout, description, is_default, sort,
   creator, create_time, updater, update_time, deleted, tenant_id)
SELECT t.name, t.group_ids, t.grid_layout,
       CONCAT('__OLDID:', t.id, '|', IFNULL(t.description,'')),
       t.is_default, t.sort,
       t.creator, t.create_time, t.updater, t.update_time, t.deleted, 162
FROM iot_video_view t WHERE t.tenant_id=1;

DROP TABLE IF EXISTS _tmp_map_view;
CREATE TABLE _tmp_map_view (old_id BIGINT PRIMARY KEY, new_id BIGINT NOT NULL) ENGINE=InnoDB;
INSERT INTO _tmp_map_view (old_id, new_id)
SELECT CAST(SUBSTRING_INDEX(SUBSTRING(description,10),'|',1) AS UNSIGNED), id
FROM iot_video_view WHERE tenant_id=162 AND description LIKE '__OLDID:%';

UPDATE iot_video_view SET description = NULLIF(SUBSTRING(description, LOCATE('|', description)+1), '')
WHERE tenant_id=162 AND description LIKE '__OLDID:%';

-- 重写 group_ids: "1;2" → "10000xxx;10000yyy"
-- 用 numbers 表展开后回组装
DROP TABLE IF EXISTS _tmp_view_groupids_rewrite;
CREATE TABLE _tmp_view_groupids_rewrite (view_id BIGINT PRIMARY KEY, new_group_ids VARCHAR(255)) ENGINE=InnoDB;

INSERT INTO _tmp_view_groupids_rewrite (view_id, new_group_ids)
WITH RECURSIVE nums AS (
  SELECT 1 AS n UNION ALL SELECT n+1 FROM nums WHERE n < 20
),
parts AS (
  SELECT v.id AS view_id, n.n,
         SUBSTRING_INDEX(SUBSTRING_INDEX(v.group_ids, ';', n.n), ';', -1) AS old_gid
  FROM iot_video_view v
  JOIN nums n ON n.n <= 1 + (CHAR_LENGTH(v.group_ids) - CHAR_LENGTH(REPLACE(v.group_ids, ';', '')))
  WHERE v.tenant_id = 162 AND v.group_ids IS NOT NULL AND v.group_ids <> ''
)
SELECT p.view_id,
       GROUP_CONCAT(gm.new_id ORDER BY p.n SEPARATOR ';')
FROM parts p
LEFT JOIN _tmp_map_view_group gm ON gm.old_id = CAST(p.old_gid AS UNSIGNED)
GROUP BY p.view_id;

UPDATE iot_video_view v
JOIN _tmp_view_groupids_rewrite r ON r.view_id = v.id
SET v.group_ids = r.new_group_ids
WHERE v.tenant_id = 162;

DROP TABLE _tmp_view_groupids_rewrite;

-- D3) iot_video_view_pane (t1=107): view_id, channel_id, device_id 重写
INSERT INTO iot_video_view_pane
  (view_id, pane_index, channel_id, device_id, channel_no, channel_name, target_ip, target_channel_no,
   stream_url_main, stream_url_sub, config,
   creator, create_time, updater, update_time, tenant_id, deleted)
SELECT vm.new_id, t.pane_index, cm.new_id, dm.new_id,
       t.channel_no, t.channel_name, t.target_ip, t.target_channel_no,
       t.stream_url_main, t.stream_url_sub, t.config,
       t.creator, t.create_time, t.updater, t.update_time, 162, t.deleted
FROM iot_video_view_pane t
JOIN _tmp_map_view vm ON vm.old_id = t.view_id
LEFT JOIN _tmp_map_channel cm ON cm.old_id = t.channel_id
LEFT JOIN _tmp_map_device dm ON dm.old_id = t.device_id
WHERE t.tenant_id=1;

SELECT '[D] video_view done' tag,
  (SELECT COUNT(*) FROM iot_video_view_group WHERE tenant_id=162) groups_,
  (SELECT COUNT(*) FROM iot_video_view WHERE tenant_id=162) views_,
  (SELECT COUNT(*) FROM iot_video_view_pane WHERE tenant_id=162) panes;
-- ============================================================================
-- E) 视频巡更 (5 张): plan → task → scene → scene_channel; schedule
-- ============================================================================

-- E1) iot_video_patrol_plan (t1=7): description 用作标记
INSERT INTO iot_video_patrol_plan
  (plan_name, plan_code, description, status, running_status, loop_mode, executor, executor_name,
   start_date, end_date, sort,
   creator, create_time, updater, update_time, deleted, tenant_id)
SELECT t.plan_name, t.plan_code,
       CONCAT('__OLDID:', t.id, '|', IFNULL(t.description,'')),
       t.status, t.running_status, t.loop_mode, t.executor, t.executor_name,
       t.start_date, t.end_date, t.sort,
       t.creator, t.create_time, t.updater, t.update_time, t.deleted, 162
FROM iot_video_patrol_plan t WHERE t.tenant_id=1;

DROP TABLE IF EXISTS _tmp_map_vpplan;
CREATE TABLE _tmp_map_vpplan (old_id BIGINT PRIMARY KEY, new_id BIGINT NOT NULL) ENGINE=InnoDB;
INSERT INTO _tmp_map_vpplan (old_id, new_id)
SELECT CAST(SUBSTRING_INDEX(SUBSTRING(description,10),'|',1) AS UNSIGNED), id
FROM iot_video_patrol_plan WHERE tenant_id=162 AND description LIKE '__OLDID:%';

UPDATE iot_video_patrol_plan SET description = NULLIF(SUBSTRING(description, LOCATE('|', description)+1), '')
WHERE tenant_id=162 AND description LIKE '__OLDID:%';

-- E2) iot_video_patrol_task (t1=21): plan_id 重写; current_scene_id 暂置 NULL,后回填
INSERT INTO iot_video_patrol_task
  (plan_id, task_name, task_code, description, task_order, duration, schedule_type, schedule_config, time_slots,
   loop_mode, interval_minutes, auto_snapshot, auto_recording, recording_duration, ai_analysis,
   alert_on_abnormal, alert_user_ids, status, running_status, current_scene_id, last_run_time, sort,
   creator, create_time, updater, update_time, deleted, tenant_id)
SELECT pm.new_id, t.task_name, t.task_code,
       CONCAT('__OLDID:', t.id, '|', IFNULL(t.description,'')),
       t.task_order, t.duration, t.schedule_type, t.schedule_config, t.time_slots,
       t.loop_mode, t.interval_minutes, t.auto_snapshot, t.auto_recording, t.recording_duration, t.ai_analysis,
       t.alert_on_abnormal, t.alert_user_ids, t.status, t.running_status,
       NULL,  -- current_scene_id 后回填
       t.last_run_time, t.sort,
       t.creator, t.create_time, t.updater, t.update_time, t.deleted, 162
FROM iot_video_patrol_task t
LEFT JOIN _tmp_map_vpplan pm ON pm.old_id = t.plan_id
WHERE t.tenant_id=1;

DROP TABLE IF EXISTS _tmp_map_vptask;
CREATE TABLE _tmp_map_vptask (old_id BIGINT PRIMARY KEY, new_id BIGINT NOT NULL) ENGINE=InnoDB;
INSERT INTO _tmp_map_vptask (old_id, new_id)
SELECT CAST(SUBSTRING_INDEX(SUBSTRING(description,10),'|',1) AS UNSIGNED), id
FROM iot_video_patrol_task WHERE tenant_id=162 AND description LIKE '__OLDID:%';

UPDATE iot_video_patrol_task SET description = NULLIF(SUBSTRING(description, LOCATE('|', description)+1), '')
WHERE tenant_id=162 AND description LIKE '__OLDID:%';

-- E3) iot_video_patrol_scene (t1=21): task_id 重写
INSERT INTO iot_video_patrol_scene
  (task_id, scene_name, scene_order, duration, grid_layout, grid_count, description, status,
   creator, create_time, updater, update_time, deleted, tenant_id)
SELECT tm.new_id, t.scene_name, t.scene_order, t.duration, t.grid_layout, t.grid_count,
       CONCAT('__OLDID:', t.id, '|', IFNULL(t.description,'')),
       t.status, t.creator, t.create_time, t.updater, t.update_time, t.deleted, 162
FROM iot_video_patrol_scene t
JOIN _tmp_map_vptask tm ON tm.old_id = t.task_id
WHERE t.tenant_id=1;

DROP TABLE IF EXISTS _tmp_map_vpscene;
CREATE TABLE _tmp_map_vpscene (old_id BIGINT PRIMARY KEY, new_id BIGINT NOT NULL) ENGINE=InnoDB;
INSERT INTO _tmp_map_vpscene (old_id, new_id)
SELECT CAST(SUBSTRING_INDEX(SUBSTRING(description,10),'|',1) AS UNSIGNED), id
FROM iot_video_patrol_scene WHERE tenant_id=162 AND description LIKE '__OLDID:%';

UPDATE iot_video_patrol_scene SET description = NULLIF(SUBSTRING(description, LOCATE('|', description)+1), '')
WHERE tenant_id=162 AND description LIKE '__OLDID:%';

-- E4) 回填 iot_video_patrol_task.current_scene_id (用 _tmp_map_vpscene)
UPDATE iot_video_patrol_task t162
JOIN _tmp_map_vptask tm ON tm.new_id = t162.id
JOIN iot_video_patrol_task t1 ON t1.id = tm.old_id AND t1.tenant_id = 1
JOIN _tmp_map_vpscene sm ON sm.old_id = t1.current_scene_id
SET t162.current_scene_id = sm.new_id
WHERE t162.tenant_id = 162;

-- E5) iot_video_patrol_scene_channel (t1=188): scene_id, channel_id, device_id 重写
INSERT INTO iot_video_patrol_scene_channel
  (scene_id, grid_position, duration, channel_id, device_id, channel_no, channel_name,
   target_ip, target_channel_no, stream_url_main, stream_url_sub, wvp_play_id, config,
   creator, create_time, updater, update_time, deleted, tenant_id)
SELECT sm.new_id, t.grid_position, t.duration, cm.new_id, dm.new_id,
       t.channel_no, t.channel_name, t.target_ip, t.target_channel_no,
       t.stream_url_main, t.stream_url_sub, t.wvp_play_id, t.config,
       t.creator, t.create_time, t.updater, t.update_time, t.deleted, 162
FROM iot_video_patrol_scene_channel t
JOIN _tmp_map_vpscene sm ON sm.old_id = t.scene_id
LEFT JOIN _tmp_map_channel cm ON cm.old_id = t.channel_id
LEFT JOIN _tmp_map_device dm ON dm.old_id = t.device_id
WHERE t.tenant_id=1;

-- E6) iot_video_patrol_schedule (t1=2): patrol_plan_id 重写
INSERT INTO iot_video_patrol_schedule
  (name, patrol_plan_id, schedule_type, start_time, end_time, week_days, status, remark,
   creator, create_time, updater, update_time, deleted, tenant_id)
SELECT t.name, pm.new_id, t.schedule_type, t.start_time, t.end_time, t.week_days, t.status, t.remark,
       t.creator, t.create_time, t.updater, t.update_time, t.deleted, 162
FROM iot_video_patrol_schedule t
LEFT JOIN _tmp_map_vpplan pm ON pm.old_id = t.patrol_plan_id
WHERE t.tenant_id=1;

SELECT '[E] video_patrol done' tag,
  (SELECT COUNT(*) FROM iot_video_patrol_plan WHERE tenant_id=162) plan_,
  (SELECT COUNT(*) FROM iot_video_patrol_task WHERE tenant_id=162) task_,
  (SELECT COUNT(*) FROM iot_video_patrol_scene WHERE tenant_id=162) scene_,
  (SELECT COUNT(*) FROM iot_video_patrol_scene_channel WHERE tenant_id=162) scene_ch,
  (SELECT COUNT(*) FROM iot_video_patrol_schedule WHERE tenant_id=162) sched;

-- ============================================================================
-- F) 视频巡检 (1 张): scenes JSON 内嵌 channelId/deviceId,保留原值(多义)
-- ============================================================================
INSERT INTO iot_video_inspection_task
  (tenant_id, task_name, layout, scenes, status,
   creator, create_time, updater, update_time, deleted)
SELECT 162, task_name, layout, scenes, status,
       creator, create_time, updater, update_time, deleted
FROM iot_video_inspection_task WHERE tenant_id=1;

SELECT '[F] inspection done' tag,
  (SELECT COUNT(*) FROM iot_video_inspection_task WHERE tenant_id=162) task_;
-- ============================================================================
-- G) 电子巡更 (8 张): person, point, route, route_point, plan, plan_period, task, task_record
-- ============================================================================

-- G1) iot_epatrol_person (t1=3): patrol_stick_no/person_card_no 加后缀(可能含外部唯一标识)
-- 注: 表无 unique index 限制,但仍按规则加后缀以保持隔离
INSERT INTO iot_epatrol_person
  (name, phone, patrol_stick_no, person_card_no, status, remark,
   creator, create_time, updater, update_time, deleted, tenant_id)
SELECT t.name, t.phone,
       CASE WHEN t.patrol_stick_no IS NOT NULL AND t.patrol_stick_no<>'' THEN CONCAT(t.patrol_stick_no,'-ibms') ELSE t.patrol_stick_no END,
       CASE WHEN t.person_card_no IS NOT NULL AND t.person_card_no<>'' THEN CONCAT(t.person_card_no,'-ibms') ELSE t.person_card_no END,
       t.status,
       CONCAT('__OLDID:', t.id, '|', IFNULL(t.remark,'')),
       t.creator, t.create_time, t.updater, t.update_time, t.deleted, 162
FROM iot_epatrol_person t WHERE t.tenant_id=1;

DROP TABLE IF EXISTS _tmp_map_eperson;
CREATE TABLE _tmp_map_eperson (old_id BIGINT PRIMARY KEY, new_id BIGINT NOT NULL) ENGINE=InnoDB;
INSERT INTO _tmp_map_eperson (old_id, new_id)
SELECT CAST(SUBSTRING_INDEX(SUBSTRING(remark,10),'|',1) AS UNSIGNED), id
FROM iot_epatrol_person WHERE tenant_id=162 AND remark LIKE '__OLDID:%';

UPDATE iot_epatrol_person SET remark = NULLIF(SUBSTRING(remark, LOCATE('|', remark)+1), '')
WHERE tenant_id=162 AND remark LIKE '__OLDID:%';

-- G2) iot_epatrol_point (t1=15): UK(point_no, tenant_id) 安全
INSERT INTO iot_epatrol_point
  (point_no, point_name, point_location, status, remark,
   creator, create_time, updater, update_time, deleted, tenant_id)
SELECT t.point_no, t.point_name, t.point_location, t.status,
       CONCAT('__OLDID:', t.id, '|', IFNULL(t.remark,'')),
       t.creator, t.create_time, t.updater, t.update_time, t.deleted, 162
FROM iot_epatrol_point t WHERE t.tenant_id=1;

DROP TABLE IF EXISTS _tmp_map_epoint;
CREATE TABLE _tmp_map_epoint (old_id BIGINT PRIMARY KEY, new_id BIGINT NOT NULL) ENGINE=InnoDB;
INSERT INTO _tmp_map_epoint (old_id, new_id)
SELECT CAST(SUBSTRING_INDEX(SUBSTRING(remark,10),'|',1) AS UNSIGNED), id
FROM iot_epatrol_point WHERE tenant_id=162 AND remark LIKE '__OLDID:%';

UPDATE iot_epatrol_point SET remark = NULLIF(SUBSTRING(remark, LOCATE('|', remark)+1), '')
WHERE tenant_id=162 AND remark LIKE '__OLDID:%';

-- G3) iot_epatrol_route (t1=4): 无 UK
INSERT INTO iot_epatrol_route
  (route_name, point_count, total_duration, status, remark,
   creator, create_time, updater, update_time, deleted, tenant_id)
SELECT t.route_name, t.point_count, t.total_duration, t.status,
       CONCAT('__OLDID:', t.id, '|', IFNULL(t.remark,'')),
       t.creator, t.create_time, t.updater, t.update_time, t.deleted, 162
FROM iot_epatrol_route t WHERE t.tenant_id=1;

DROP TABLE IF EXISTS _tmp_map_eroute;
CREATE TABLE _tmp_map_eroute (old_id BIGINT PRIMARY KEY, new_id BIGINT NOT NULL) ENGINE=InnoDB;
INSERT INTO _tmp_map_eroute (old_id, new_id)
SELECT CAST(SUBSTRING_INDEX(SUBSTRING(remark,10),'|',1) AS UNSIGNED), id
FROM iot_epatrol_route WHERE tenant_id=162 AND remark LIKE '__OLDID:%';

UPDATE iot_epatrol_route SET remark = NULLIF(SUBSTRING(remark, LOCATE('|', remark)+1), '')
WHERE tenant_id=162 AND remark LIKE '__OLDID:%';

-- G4) iot_epatrol_route_point (t1=34): route_id, point_id 重写
INSERT INTO iot_epatrol_route_point
  (route_id, point_id, sort, interval_minutes,
   creator, create_time, updater, update_time, deleted, tenant_id)
SELECT rm.new_id, pm.new_id, t.sort, t.interval_minutes,
       t.creator, t.create_time, t.updater, t.update_time, t.deleted, 162
FROM iot_epatrol_route_point t
JOIN _tmp_map_eroute rm ON rm.old_id = t.route_id
JOIN _tmp_map_epoint pm ON pm.old_id = t.point_id
WHERE t.tenant_id=1;

-- G5) iot_epatrol_plan (t1=4): route_id 重写; UK(plan_code, tenant_id) 安全
INSERT INTO iot_epatrol_plan
  (plan_code, plan_name, route_id, start_date, end_date, weekdays, status, remark,
   creator, create_time, updater, update_time, deleted, tenant_id)
SELECT t.plan_code, t.plan_name, rm.new_id, t.start_date, t.end_date, t.weekdays, t.status,
       CONCAT('__OLDID:', t.id, '|', IFNULL(t.remark,'')),
       t.creator, t.create_time, t.updater, t.update_time, t.deleted, 162
FROM iot_epatrol_plan t
LEFT JOIN _tmp_map_eroute rm ON rm.old_id = t.route_id
WHERE t.tenant_id=1;

DROP TABLE IF EXISTS _tmp_map_eplan;
CREATE TABLE _tmp_map_eplan (old_id BIGINT PRIMARY KEY, new_id BIGINT NOT NULL) ENGINE=InnoDB;
INSERT INTO _tmp_map_eplan (old_id, new_id)
SELECT CAST(SUBSTRING_INDEX(SUBSTRING(remark,10),'|',1) AS UNSIGNED), id
FROM iot_epatrol_plan WHERE tenant_id=162 AND remark LIKE '__OLDID:%';

UPDATE iot_epatrol_plan SET remark = NULLIF(SUBSTRING(remark, LOCATE('|', remark)+1), '')
WHERE tenant_id=162 AND remark LIKE '__OLDID:%';

-- G6) iot_epatrol_plan_period (t1=16): plan_id, route_id 重写; person_ids JSON 重写
-- 用临时桥接表处理 JSON 的 person_ids 重写
INSERT INTO iot_epatrol_plan_period
  (plan_id, route_id, start_time, duration_minutes, person_ids,
   creator, create_time, updater, update_time, deleted, tenant_id)
SELECT pm.new_id, rm.new_id, t.start_time, t.duration_minutes,
       -- person_ids JSON 重写: 旧 t1 ID array → 新 t162 ID array
       (SELECT CAST(CONCAT('[', IFNULL(GROUP_CONCAT(epm.new_id ORDER BY j.idx), ''), ']') AS JSON)
        FROM JSON_TABLE(t.person_ids, '$[*]' COLUMNS (idx FOR ORDINALITY, old_pid BIGINT PATH '$')) j
        LEFT JOIN _tmp_map_eperson epm ON epm.old_id = j.old_pid),
       t.creator, t.create_time, t.updater, t.update_time, t.deleted, 162
FROM iot_epatrol_plan_period t
JOIN _tmp_map_eplan pm ON pm.old_id = t.plan_id
LEFT JOIN _tmp_map_eroute rm ON rm.old_id = t.route_id
WHERE t.tenant_id=1;

-- 建立 plan_period 映射(period 无标记字段, 用 (plan_id, start_time, duration_minutes) 自然唯一性)
DROP TABLE IF EXISTS _tmp_map_eperiod;
CREATE TABLE _tmp_map_eperiod (old_id BIGINT PRIMARY KEY, new_id BIGINT NOT NULL) ENGINE=InnoDB;
INSERT INTO _tmp_map_eperiod (old_id, new_id)
SELECT t1.id, t162.id
FROM iot_epatrol_plan_period t1
JOIN _tmp_map_eplan plm ON plm.old_id = t1.plan_id
JOIN iot_epatrol_plan_period t162
   ON t162.tenant_id = 162
  AND t162.plan_id = plm.new_id
  AND t162.start_time <=> t1.start_time
  AND t162.duration_minutes <=> t1.duration_minutes
WHERE t1.tenant_id = 1;

-- G7) iot_epatrol_task (t1=10): plan_id, period_id, route_id 重写; person_ids JSON 重写
INSERT INTO iot_epatrol_task
  (task_code, plan_id, period_id, route_id, task_date, planned_start_time, planned_end_time, person_ids,
   status, submit_time, submitter_id, remark,
   creator, create_time, updater, update_time, deleted, tenant_id)
SELECT t.task_code, pm.new_id, prm.new_id, rm.new_id, t.task_date, t.planned_start_time, t.planned_end_time,
       (SELECT CAST(CONCAT('[', IFNULL(GROUP_CONCAT(epm.new_id ORDER BY j.idx), ''), ']') AS JSON)
        FROM JSON_TABLE(t.person_ids, '$[*]' COLUMNS (idx FOR ORDINALITY, old_pid BIGINT PATH '$')) j
        LEFT JOIN _tmp_map_eperson epm ON epm.old_id = j.old_pid),
       t.status, t.submit_time, t.submitter_id,
       CONCAT('__OLDID:', t.id, '|', IFNULL(t.remark,'')),
       t.creator, t.create_time, t.updater, t.update_time, t.deleted, 162
FROM iot_epatrol_task t
LEFT JOIN _tmp_map_eplan pm ON pm.old_id = t.plan_id
LEFT JOIN _tmp_map_eperiod prm ON prm.old_id = t.period_id
LEFT JOIN _tmp_map_eroute rm ON rm.old_id = t.route_id
WHERE t.tenant_id=1;

DROP TABLE IF EXISTS _tmp_map_etask;
CREATE TABLE _tmp_map_etask (old_id BIGINT PRIMARY KEY, new_id BIGINT NOT NULL) ENGINE=InnoDB;
INSERT INTO _tmp_map_etask (old_id, new_id)
SELECT CAST(SUBSTRING_INDEX(SUBSTRING(remark,10),'|',1) AS UNSIGNED), id
FROM iot_epatrol_task WHERE tenant_id=162 AND remark LIKE '__OLDID:%';

UPDATE iot_epatrol_task SET remark = NULLIF(SUBSTRING(remark, LOCATE('|', remark)+1), '')
WHERE tenant_id=162 AND remark LIKE '__OLDID:%';

-- G8) iot_epatrol_task_record (t1=16): task_id, point_id, person_id 重写
INSERT INTO iot_epatrol_task_record
  (task_id, point_id, point_no, point_name, person_id, person_name,
   expected_sort, actual_sort, planned_time, actual_time, patrol_status, time_diff_seconds, remark,
   creator, create_time, updater, update_time, deleted, tenant_id)
SELECT tm.new_id, pm.new_id, t.point_no, t.point_name, prm.new_id, t.person_name,
       t.expected_sort, t.actual_sort, t.planned_time, t.actual_time, t.patrol_status, t.time_diff_seconds, t.remark,
       t.creator, t.create_time, t.updater, t.update_time, t.deleted, 162
FROM iot_epatrol_task_record t
JOIN _tmp_map_etask tm ON tm.old_id = t.task_id
LEFT JOIN _tmp_map_epoint pm ON pm.old_id = t.point_id
LEFT JOIN _tmp_map_eperson prm ON prm.old_id = t.person_id
WHERE t.tenant_id=1;

SELECT '[G] epatrol done' tag,
  (SELECT COUNT(*) FROM iot_epatrol_person WHERE tenant_id=162) person,
  (SELECT COUNT(*) FROM iot_epatrol_point WHERE tenant_id=162) point_,
  (SELECT COUNT(*) FROM iot_epatrol_route WHERE tenant_id=162) route,
  (SELECT COUNT(*) FROM iot_epatrol_route_point WHERE tenant_id=162) rp,
  (SELECT COUNT(*) FROM iot_epatrol_plan WHERE tenant_id=162) plan_,
  (SELECT COUNT(*) FROM iot_epatrol_plan_period WHERE tenant_id=162) period,
  (SELECT COUNT(*) FROM iot_epatrol_task WHERE tenant_id=162) task_,
  (SELECT COUNT(*) FROM iot_epatrol_task_record WHERE tenant_id=162) tr;

-- ============================================================================
-- 完成: 保留映射表供回溯; 后续 Phase 不依赖,可在最终 Phase 6 统一 DROP _tmp_*
-- ============================================================================
SELECT '[Phase 3] all done' tag, NOW() ts;
