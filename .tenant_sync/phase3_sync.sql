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
 
