-- Phase 6 行数对齐终验证：t1 vs t162
-- 按 phase 分组，输出 (table, t1, t162, diff)；diff=0 即对齐
USE ch_ibms;

DROP TEMPORARY TABLE IF EXISTS _tmp_verify;
CREATE TEMPORARY TABLE _tmp_verify (
  phase TINYINT,
  tbl VARCHAR(100),
  t1_cnt INT,
  t162_cnt INT,
  diff INT,
  note VARCHAR(200)
);

-- Phase 1
INSERT INTO _tmp_verify SELECT 1,'iot_subsystem',(SELECT COUNT(*) FROM iot_subsystem WHERE tenant_id=1),(SELECT COUNT(*) FROM iot_subsystem WHERE tenant_id=162),0,'';
INSERT INTO _tmp_verify SELECT 1,'iot_job_type_definition',(SELECT COUNT(*) FROM iot_job_type_definition WHERE tenant_id=1),(SELECT COUNT(*) FROM iot_job_type_definition WHERE tenant_id=162),0,'';
INSERT INTO _tmp_verify SELECT 1,'iot_thing_model',(SELECT COUNT(*) FROM iot_thing_model WHERE tenant_id=1),(SELECT COUNT(*) FROM iot_thing_model WHERE tenant_id=162),0,'';
INSERT INTO _tmp_verify SELECT 1,'iot_data_sink',(SELECT COUNT(*) FROM iot_data_sink WHERE tenant_id=1),(SELECT COUNT(*) FROM iot_data_sink WHERE tenant_id=162),0,'';
INSERT INTO _tmp_verify SELECT 1,'iot_data_rule',(SELECT COUNT(*) FROM iot_data_rule WHERE tenant_id=1),(SELECT COUNT(*) FROM iot_data_rule WHERE tenant_id=162),0,'';
INSERT INTO _tmp_verify SELECT 1,'iot_product_category',(SELECT COUNT(*) FROM iot_product_category WHERE tenant_id=1),(SELECT COUNT(*) FROM iot_product_category WHERE tenant_id=162),0,'';
INSERT INTO _tmp_verify SELECT 1,'iot_scheduled_task_config',(SELECT COUNT(*) FROM iot_scheduled_task_config WHERE tenant_id=1),(SELECT COUNT(*) FROM iot_scheduled_task_config WHERE tenant_id=162),0,'';
INSERT INTO _tmp_verify SELECT 1,'ibms_product',(SELECT COUNT(*) FROM ibms_product WHERE tenant_id=1),(SELECT COUNT(*) FROM ibms_product WHERE tenant_id=162),0,'';
INSERT INTO _tmp_verify SELECT 1,'ibms_product_property',(SELECT COUNT(*) FROM ibms_product_property WHERE tenant_id=1),(SELECT COUNT(*) FROM ibms_product_property WHERE tenant_id=162),0,'';
INSERT INTO _tmp_verify SELECT 1,'ibms_product_point_type',(SELECT COUNT(*) FROM ibms_product_point_type WHERE tenant_id=1),(SELECT COUNT(*) FROM ibms_product_point_type WHERE tenant_id=162),0,'';

-- Phase 2
INSERT INTO _tmp_verify SELECT 2,'campus',(SELECT COUNT(*) FROM campus WHERE tenant_id=1),(SELECT COUNT(*) FROM campus WHERE tenant_id=162),0,'';
INSERT INTO _tmp_verify SELECT 2,'building',(SELECT COUNT(*) FROM building WHERE tenant_id=1),(SELECT COUNT(*) FROM building WHERE tenant_id=162),0,'';
INSERT INTO _tmp_verify SELECT 2,'floor',(SELECT COUNT(*) FROM floor WHERE tenant_id=1),(SELECT COUNT(*) FROM floor WHERE tenant_id=162),0,'';
INSERT INTO _tmp_verify SELECT 2,'area',(SELECT COUNT(*) FROM area WHERE tenant_id=1),(SELECT COUNT(*) FROM area WHERE tenant_id=162),0,'';
INSERT INTO _tmp_verify SELECT 2,'ibms_space',(SELECT COUNT(*) FROM ibms_space WHERE tenant_id=1),(SELECT COUNT(*) FROM ibms_space WHERE tenant_id=162),0,'';
INSERT INTO _tmp_verify SELECT 2,'ibms_device',(SELECT COUNT(*) FROM ibms_device WHERE tenant_id=1),(SELECT COUNT(*) FROM ibms_device WHERE tenant_id=162),0,'';
INSERT INTO _tmp_verify SELECT 2,'ibms_channel',(SELECT COUNT(*) FROM ibms_channel WHERE tenant_id=1),(SELECT COUNT(*) FROM ibms_channel WHERE tenant_id=162),0,'';
INSERT INTO _tmp_verify SELECT 2,'ibms_device_runtime',(SELECT COUNT(*) FROM ibms_device_runtime WHERE tenant_id=1),(SELECT COUNT(*) FROM ibms_device_runtime WHERE tenant_id=162),0,'';
INSERT INTO _tmp_verify SELECT 2,'ibms_hvac_device',(SELECT COUNT(*) FROM ibms_hvac_device WHERE tenant_id=1),(SELECT COUNT(*) FROM ibms_hvac_device WHERE tenant_id=162),0,'';
INSERT INTO _tmp_verify SELECT 2,'ibms_water_device',(SELECT COUNT(*) FROM ibms_water_device WHERE tenant_id=1),(SELECT COUNT(*) FROM ibms_water_device WHERE tenant_id=162),0,'';
INSERT INTO _tmp_verify SELECT 2,'ibms_lighting_gateway',(SELECT COUNT(*) FROM ibms_lighting_gateway WHERE tenant_id=1),(SELECT COUNT(*) FROM ibms_lighting_gateway WHERE tenant_id=162),0,'';
INSERT INTO _tmp_verify SELECT 2,'ibms_lighting_controller',(SELECT COUNT(*) FROM ibms_lighting_controller WHERE tenant_id=1),(SELECT COUNT(*) FROM ibms_lighting_controller WHERE tenant_id=162),0,'';
INSERT INTO _tmp_verify SELECT 2,'ibms_lighting_circuit',(SELECT COUNT(*) FROM ibms_lighting_circuit WHERE tenant_id=1),(SELECT COUNT(*) FROM ibms_lighting_circuit WHERE tenant_id=162),0,'';
INSERT INTO _tmp_verify SELECT 2,'ibms_lighting_scene',(SELECT COUNT(*) FROM ibms_lighting_scene WHERE tenant_id=1),(SELECT COUNT(*) FROM ibms_lighting_scene WHERE tenant_id=162),0,'';
INSERT INTO _tmp_verify SELECT 2,'ibms_lighting_schedule',(SELECT COUNT(*) FROM ibms_lighting_schedule WHERE tenant_id=1),(SELECT COUNT(*) FROM ibms_lighting_schedule WHERE tenant_id=162),0,'';
INSERT INTO _tmp_verify SELECT 2,'ibms_lighting_alarm',(SELECT COUNT(*) FROM ibms_lighting_alarm WHERE tenant_id=1),(SELECT COUNT(*) FROM ibms_lighting_alarm WHERE tenant_id=162),0,'';
INSERT INTO _tmp_verify SELECT 2,'ibms_lighting_operation_log',(SELECT COUNT(*) FROM ibms_lighting_operation_log WHERE tenant_id=1),(SELECT COUNT(*) FROM ibms_lighting_operation_log WHERE tenant_id=162),0,'';
INSERT INTO _tmp_verify SELECT 2,'ibms_env_data_record',(SELECT COUNT(*) FROM ibms_env_data_record WHERE tenant_id=1),(SELECT COUNT(*) FROM ibms_env_data_record WHERE tenant_id=162),0,'';
INSERT INTO _tmp_verify SELECT 2,'ibms_env_alarm',(SELECT COUNT(*) FROM ibms_env_alarm WHERE tenant_id=1),(SELECT COUNT(*) FROM ibms_env_alarm WHERE tenant_id=162),0,'';
INSERT INTO _tmp_verify SELECT 2,'ibms_bac_alarm',(SELECT COUNT(*) FROM ibms_bac_alarm WHERE tenant_id=1),(SELECT COUNT(*) FROM ibms_bac_alarm WHERE tenant_id=162),0,'';
INSERT INTO _tmp_verify SELECT 2,'ibms_bac_system_log',(SELECT COUNT(*) FROM ibms_bac_system_log WHERE tenant_id=1),(SELECT COUNT(*) FROM ibms_bac_system_log WHERE tenant_id=162),0,'';
INSERT INTO _tmp_verify SELECT 2,'ibms_energy_rate',(SELECT COUNT(*) FROM ibms_energy_rate WHERE tenant_id=1),(SELECT COUNT(*) FROM ibms_energy_rate WHERE tenant_id=162),0,'';
INSERT INTO _tmp_verify SELECT 2,'ibms_energy_meter',(SELECT COUNT(*) FROM ibms_energy_meter WHERE tenant_id=1),(SELECT COUNT(*) FROM ibms_energy_meter WHERE tenant_id=162),0,'';
INSERT INTO _tmp_verify SELECT 2,'ibms_energy_record',(SELECT COUNT(*) FROM ibms_energy_record WHERE tenant_id=1),(SELECT COUNT(*) FROM ibms_energy_record WHERE tenant_id=162),0,'';
INSERT INTO _tmp_verify SELECT 2,'ibms_energy_statistics_daily',(SELECT COUNT(*) FROM ibms_energy_statistics_daily WHERE tenant_id=1),(SELECT COUNT(*) FROM ibms_energy_statistics_daily WHERE tenant_id=162),0,'';
INSERT INTO _tmp_verify SELECT 2,'ibms_energy_alarm',(SELECT COUNT(*) FROM ibms_energy_alarm WHERE tenant_id=1),(SELECT COUNT(*) FROM ibms_energy_alarm WHERE tenant_id=162),0,'5 行源孤儿丢弃';
INSERT INTO _tmp_verify SELECT 2,'ibms_energy_manual_reading',(SELECT COUNT(*) FROM ibms_energy_manual_reading WHERE tenant_id=1),(SELECT COUNT(*) FROM ibms_energy_manual_reading WHERE tenant_id=162),0,'';

-- Phase 3
INSERT INTO _tmp_verify SELECT 3,'iot_alarm_host',(SELECT COUNT(*) FROM iot_alarm_host WHERE tenant_id=1),(SELECT COUNT(*) FROM iot_alarm_host WHERE tenant_id=162),0,'';
INSERT INTO _tmp_verify SELECT 3,'iot_alarm_partition',(SELECT COUNT(*) FROM iot_alarm_partition WHERE tenant_id=1),(SELECT COUNT(*) FROM iot_alarm_partition WHERE tenant_id=162),0,'';
INSERT INTO _tmp_verify SELECT 3,'iot_alarm_zone',(SELECT COUNT(*) FROM iot_alarm_zone WHERE tenant_id=1),(SELECT COUNT(*) FROM iot_alarm_zone WHERE tenant_id=162),0,'5 行源孤儿丢弃';
INSERT INTO _tmp_verify SELECT 3,'iot_alarm_event',(SELECT COUNT(*) FROM iot_alarm_event WHERE tenant_id=1),(SELECT COUNT(*) FROM iot_alarm_event WHERE tenant_id=162),0,'';
INSERT INTO _tmp_verify SELECT 3,'iot_alarm_operation_log',(SELECT COUNT(*) FROM iot_alarm_operation_log WHERE tenant_id=1),(SELECT COUNT(*) FROM iot_alarm_operation_log WHERE tenant_id=162),0,'';
INSERT INTO _tmp_verify SELECT 3,'iot_camera_preset',(SELECT COUNT(*) FROM iot_camera_preset WHERE tenant_id=1),(SELECT COUNT(*) FROM iot_camera_preset WHERE tenant_id=162),0,'';
INSERT INTO _tmp_verify SELECT 3,'iot_camera_cruise',(SELECT COUNT(*) FROM iot_camera_cruise WHERE tenant_id=1),(SELECT COUNT(*) FROM iot_camera_cruise WHERE tenant_id=162),0,'';
INSERT INTO _tmp_verify SELECT 3,'iot_camera_cruise_point',(SELECT COUNT(*) FROM iot_camera_cruise_point WHERE tenant_id=1),(SELECT COUNT(*) FROM iot_camera_cruise_point WHERE tenant_id=162),0,'';
INSERT INTO _tmp_verify SELECT 3,'iot_camera_recording',(SELECT COUNT(*) FROM iot_camera_recording WHERE tenant_id=1),(SELECT COUNT(*) FROM iot_camera_recording WHERE tenant_id=162),0,'';
INSERT INTO _tmp_verify SELECT 3,'iot_camera_snapshot',(SELECT COUNT(*) FROM iot_camera_snapshot WHERE tenant_id=1),(SELECT COUNT(*) FROM iot_camera_snapshot WHERE tenant_id=162),0,'';
INSERT INTO _tmp_verify SELECT 3,'iot_cloud_defense_mode',(SELECT COUNT(*) FROM iot_cloud_defense_mode WHERE tenant_id=1),(SELECT COUNT(*) FROM iot_cloud_defense_mode WHERE tenant_id=162),0,'';
INSERT INTO _tmp_verify SELECT 3,'iot_cloud_defense_area',(SELECT COUNT(*) FROM iot_cloud_defense_area WHERE tenant_id=1),(SELECT COUNT(*) FROM iot_cloud_defense_area WHERE tenant_id=162),0,'';
INSERT INTO _tmp_verify SELECT 3,'iot_cloud_defense_area_device_rel',(SELECT COUNT(*) FROM iot_cloud_defense_area_device_rel WHERE tenant_id=1),(SELECT COUNT(*) FROM iot_cloud_defense_area_device_rel WHERE tenant_id=162),0,'';
INSERT INTO _tmp_verify SELECT 3,'iot_cloud_defense_point',(SELECT COUNT(*) FROM iot_cloud_defense_point WHERE tenant_id=1),(SELECT COUNT(*) FROM iot_cloud_defense_point WHERE tenant_id=162),0,'';
INSERT INTO _tmp_verify SELECT 3,'iot_cloud_defense_score_log',(SELECT COUNT(*) FROM iot_cloud_defense_score_log WHERE tenant_id=1),(SELECT COUNT(*) FROM iot_cloud_defense_score_log WHERE tenant_id=162),0,'';
INSERT INTO _tmp_verify SELECT 3,'iot_video_view_group',(SELECT COUNT(*) FROM iot_video_view_group WHERE tenant_id=1),(SELECT COUNT(*) FROM iot_video_view_group WHERE tenant_id=162),0,'';
INSERT INTO _tmp_verify SELECT 3,'iot_video_view',(SELECT COUNT(*) FROM iot_video_view WHERE tenant_id=1),(SELECT COUNT(*) FROM iot_video_view WHERE tenant_id=162),0,'';
INSERT INTO _tmp_verify SELECT 3,'iot_video_view_pane',(SELECT COUNT(*) FROM iot_video_view_pane WHERE tenant_id=1),(SELECT COUNT(*) FROM iot_video_view_pane WHERE tenant_id=162),0,'3 行 view_id 孤儿丢弃';
INSERT INTO _tmp_verify SELECT 3,'iot_video_patrol_plan',(SELECT COUNT(*) FROM iot_video_patrol_plan WHERE tenant_id=1),(SELECT COUNT(*) FROM iot_video_patrol_plan WHERE tenant_id=162),0,'';
INSERT INTO _tmp_verify SELECT 3,'iot_video_patrol_task',(SELECT COUNT(*) FROM iot_video_patrol_task WHERE tenant_id=1),(SELECT COUNT(*) FROM iot_video_patrol_task WHERE tenant_id=162),0,'';
INSERT INTO _tmp_verify SELECT 3,'iot_video_patrol_scene',(SELECT COUNT(*) FROM iot_video_patrol_scene WHERE tenant_id=1),(SELECT COUNT(*) FROM iot_video_patrol_scene WHERE tenant_id=162),0,'';
INSERT INTO _tmp_verify SELECT 3,'iot_video_patrol_scene_channel',(SELECT COUNT(*) FROM iot_video_patrol_scene_channel WHERE tenant_id=1),(SELECT COUNT(*) FROM iot_video_patrol_scene_channel WHERE tenant_id=162),0,'';
INSERT INTO _tmp_verify SELECT 3,'iot_video_patrol_schedule',(SELECT COUNT(*) FROM iot_video_patrol_schedule WHERE tenant_id=1),(SELECT COUNT(*) FROM iot_video_patrol_schedule WHERE tenant_id=162),0,'';
INSERT INTO _tmp_verify SELECT 3,'iot_video_inspection_task',(SELECT COUNT(*) FROM iot_video_inspection_task WHERE tenant_id=1),(SELECT COUNT(*) FROM iot_video_inspection_task WHERE tenant_id=162),0,'';
INSERT INTO _tmp_verify SELECT 3,'iot_epatrol_person',(SELECT COUNT(*) FROM iot_epatrol_person WHERE tenant_id=1),(SELECT COUNT(*) FROM iot_epatrol_person WHERE tenant_id=162),0,'';
INSERT INTO _tmp_verify SELECT 3,'iot_epatrol_point',(SELECT COUNT(*) FROM iot_epatrol_point WHERE tenant_id=1),(SELECT COUNT(*) FROM iot_epatrol_point WHERE tenant_id=162),0,'';
INSERT INTO _tmp_verify SELECT 3,'iot_epatrol_route',(SELECT COUNT(*) FROM iot_epatrol_route WHERE tenant_id=1),(SELECT COUNT(*) FROM iot_epatrol_route WHERE tenant_id=162),0,'';
INSERT INTO _tmp_verify SELECT 3,'iot_epatrol_route_point',(SELECT COUNT(*) FROM iot_epatrol_route_point WHERE tenant_id=1),(SELECT COUNT(*) FROM iot_epatrol_route_point WHERE tenant_id=162),0,'';
INSERT INTO _tmp_verify SELECT 3,'iot_epatrol_plan',(SELECT COUNT(*) FROM iot_epatrol_plan WHERE tenant_id=1),(SELECT COUNT(*) FROM iot_epatrol_plan WHERE tenant_id=162),0,'';
INSERT INTO _tmp_verify SELECT 3,'iot_epatrol_plan_period',(SELECT COUNT(*) FROM iot_epatrol_plan_period WHERE tenant_id=1),(SELECT COUNT(*) FROM iot_epatrol_plan_period WHERE tenant_id=162),0,'';
INSERT INTO _tmp_verify SELECT 3,'iot_epatrol_task',(SELECT COUNT(*) FROM iot_epatrol_task WHERE tenant_id=1),(SELECT COUNT(*) FROM iot_epatrol_task WHERE tenant_id=162),0,'';
INSERT INTO _tmp_verify SELECT 3,'iot_epatrol_task_record',(SELECT COUNT(*) FROM iot_epatrol_task_record WHERE tenant_id=1),(SELECT COUNT(*) FROM iot_epatrol_task_record WHERE tenant_id=162),0,'';

-- Phase 4
INSERT INTO _tmp_verify SELECT 4,'iot_keding_device',(SELECT COUNT(*) FROM iot_keding_device WHERE tenant_id=1),(SELECT COUNT(*) FROM iot_keding_device WHERE tenant_id=162),0,'';
INSERT INTO _tmp_verify SELECT 4,'iot_keding_firmware',(SELECT COUNT(*) FROM iot_keding_firmware WHERE tenant_id=1),(SELECT COUNT(*) FROM iot_keding_firmware WHERE tenant_id=162),0,'';
INSERT INTO _tmp_verify SELECT 4,'iot_ota_firmware',(SELECT COUNT(*) FROM iot_ota_firmware WHERE tenant_id=1),(SELECT COUNT(*) FROM iot_ota_firmware WHERE tenant_id=162),0,'';
INSERT INTO _tmp_verify SELECT 4,'iot_access_department',(SELECT COUNT(*) FROM iot_access_department WHERE tenant_id=1),(SELECT COUNT(*) FROM iot_access_department WHERE tenant_id=162),0,'';
INSERT INTO _tmp_verify SELECT 4,'iot_access_permission_group',(SELECT COUNT(*) FROM iot_access_permission_group WHERE tenant_id=1),(SELECT COUNT(*) FROM iot_access_permission_group WHERE tenant_id=162),0,'';
INSERT INTO _tmp_verify SELECT 4,'iot_parking_lot',(SELECT COUNT(*) FROM iot_parking_lot WHERE tenant_id=1),(SELECT COUNT(*) FROM iot_parking_lot WHERE tenant_id=162),0,'';
INSERT INTO _tmp_verify SELECT 4,'iot_parking_charge_rule',(SELECT COUNT(*) FROM iot_parking_charge_rule WHERE tenant_id=1),(SELECT COUNT(*) FROM iot_parking_charge_rule WHERE tenant_id=162),0,'';
INSERT INTO _tmp_verify SELECT 4,'iot_visitor_appointment',(SELECT COUNT(*) FROM iot_visitor_appointment WHERE tenant_id=1),(SELECT COUNT(*) FROM iot_visitor_appointment WHERE tenant_id=162),0,'';
INSERT INTO _tmp_verify SELECT 4,'iot_device_group',(SELECT COUNT(*) FROM iot_device_group WHERE tenant_id=1),(SELECT COUNT(*) FROM iot_device_group WHERE tenant_id=162),0,'';
INSERT INTO _tmp_verify SELECT 4,'iot_device_display_config',(SELECT COUNT(*) FROM iot_device_display_config WHERE tenant_id=1),(SELECT COUNT(*) FROM iot_device_display_config WHERE tenant_id=162),0,'';
INSERT INTO _tmp_verify SELECT 4,'iot_access_person',(SELECT COUNT(*) FROM iot_access_person WHERE tenant_id=1),(SELECT COUNT(*) FROM iot_access_person WHERE tenant_id=162),0,'';
INSERT INTO _tmp_verify SELECT 4,'iot_parking_lane',(SELECT COUNT(*) FROM iot_parking_lane WHERE tenant_id=1),(SELECT COUNT(*) FROM iot_parking_lane WHERE tenant_id=162),0,'';
INSERT INTO _tmp_verify SELECT 4,'iot_parking_gate',(SELECT COUNT(*) FROM iot_parking_gate WHERE tenant_id=1),(SELECT COUNT(*) FROM iot_parking_gate WHERE tenant_id=162),0,'';
INSERT INTO _tmp_verify SELECT 4,'iot_parking_monthly_vehicle',(SELECT COUNT(*) FROM iot_parking_monthly_vehicle WHERE tenant_id=1),(SELECT COUNT(*) FROM iot_parking_monthly_vehicle WHERE tenant_id=162),0,'';
INSERT INTO _tmp_verify SELECT 4,'iot_parking_monthly_recharge',(SELECT COUNT(*) FROM iot_parking_monthly_recharge WHERE tenant_id=1),(SELECT COUNT(*) FROM iot_parking_monthly_recharge WHERE tenant_id=162),0,'';
INSERT INTO _tmp_verify SELECT 4,'iot_parking_blacklist',(SELECT COUNT(*) FROM iot_parking_blacklist WHERE tenant_id=1),(SELECT COUNT(*) FROM iot_parking_blacklist WHERE tenant_id=162),0,'';
INSERT INTO _tmp_verify SELECT 4,'iot_parking_free_vehicle',(SELECT COUNT(*) FROM iot_parking_free_vehicle WHERE tenant_id=1),(SELECT COUNT(*) FROM iot_parking_free_vehicle WHERE tenant_id=162),0,'';
INSERT INTO _tmp_verify SELECT 4,'iot_parking_pass_rule',(SELECT COUNT(*) FROM iot_parking_pass_rule WHERE tenant_id=1),(SELECT COUNT(*) FROM iot_parking_pass_rule WHERE tenant_id=162),0,'';
INSERT INTO _tmp_verify SELECT 4,'iot_parking_charge_rule_apply',(SELECT COUNT(*) FROM iot_parking_charge_rule_apply WHERE tenant_id=1),(SELECT COUNT(*) FROM iot_parking_charge_rule_apply WHERE tenant_id=162),0,'';
INSERT INTO _tmp_verify SELECT 4,'iot_parking_system_config',(SELECT COUNT(*) FROM iot_parking_system_config WHERE tenant_id=1),(SELECT COUNT(*) FROM iot_parking_system_config WHERE tenant_id=162),0,'';
INSERT INTO _tmp_verify SELECT 4,'iot_parking_wechat_user',(SELECT COUNT(*) FROM iot_parking_wechat_user WHERE tenant_id=1),(SELECT COUNT(*) FROM iot_parking_wechat_user WHERE tenant_id=162),0,'';
INSERT INTO _tmp_verify SELECT 4,'iot_parking_present_vehicle',(SELECT COUNT(*) FROM iot_parking_present_vehicle WHERE tenant_id=1),(SELECT COUNT(*) FROM iot_parking_present_vehicle WHERE tenant_id=162),0,'';
INSERT INTO _tmp_verify SELECT 4,'iot_parking_record',(SELECT COUNT(*) FROM iot_parking_record WHERE tenant_id=1),(SELECT COUNT(*) FROM iot_parking_record WHERE tenant_id=162),0,'';
INSERT INTO _tmp_verify SELECT 4,'iot_parking_refund_record',(SELECT COUNT(*) FROM iot_parking_refund_record WHERE tenant_id=1),(SELECT COUNT(*) FROM iot_parking_refund_record WHERE tenant_id=162),0,'';
INSERT INTO _tmp_verify SELECT 4,'iot_visitor_abnormal_event',(SELECT COUNT(*) FROM iot_visitor_abnormal_event WHERE tenant_id=1),(SELECT COUNT(*) FROM iot_visitor_abnormal_event WHERE tenant_id=162),0,'';
INSERT INTO _tmp_verify SELECT 4,'iot_device_channel',(SELECT COUNT(*) FROM iot_device_channel WHERE tenant_id=1),(SELECT COUNT(*) FROM iot_device_channel WHERE tenant_id=162),0,'';
INSERT INTO _tmp_verify SELECT 4,'iot_device_event_log',(SELECT COUNT(*) FROM iot_device_event_log WHERE tenant_id=1),(SELECT COUNT(*) FROM iot_device_event_log WHERE tenant_id=162),0,'';
INSERT INTO _tmp_verify SELECT 4,'iot_access_person_credential',(SELECT COUNT(*) FROM iot_access_person_credential WHERE tenant_id=1),(SELECT COUNT(*) FROM iot_access_person_credential WHERE tenant_id=162),0,'';
INSERT INTO _tmp_verify SELECT 4,'iot_access_person_device_auth',(SELECT COUNT(*) FROM iot_access_person_device_auth WHERE tenant_id=1),(SELECT COUNT(*) FROM iot_access_person_device_auth WHERE tenant_id=162),0,'';
INSERT INTO _tmp_verify SELECT 4,'iot_access_device_capability',(SELECT COUNT(*) FROM iot_access_device_capability WHERE tenant_id=1),(SELECT COUNT(*) FROM iot_access_device_capability WHERE tenant_id=162),0,'';
INSERT INTO _tmp_verify SELECT 4,'iot_access_permission_group_device',(SELECT COUNT(*) FROM iot_access_permission_group_device WHERE tenant_id=1),(SELECT COUNT(*) FROM iot_access_permission_group_device WHERE tenant_id=162),0,'';
INSERT INTO _tmp_verify SELECT 4,'iot_access_permission_group_person',(SELECT COUNT(*) FROM iot_access_permission_group_person WHERE tenant_id=1),(SELECT COUNT(*) FROM iot_access_permission_group_person WHERE tenant_id=162),0,'';
INSERT INTO _tmp_verify SELECT 4,'iot_access_auth_task',(SELECT COUNT(*) FROM iot_access_auth_task WHERE tenant_id=1),(SELECT COUNT(*) FROM iot_access_auth_task WHERE tenant_id=162),0,'';
INSERT INTO _tmp_verify SELECT 4,'iot_access_auth_task_detail',(SELECT COUNT(*) FROM iot_access_auth_task_detail WHERE tenant_id=1),(SELECT COUNT(*) FROM iot_access_auth_task_detail WHERE tenant_id=162),0,'';
INSERT INTO _tmp_verify SELECT 4,'iot_access_event_log',(SELECT COUNT(*) FROM iot_access_event_log WHERE tenant_id=1),(SELECT COUNT(*) FROM iot_access_event_log WHERE tenant_id=162),0,'';
INSERT INTO _tmp_verify SELECT 4,'iot_access_operation_log',(SELECT COUNT(*) FROM iot_access_operation_log WHERE tenant_id=1),(SELECT COUNT(*) FROM iot_access_operation_log WHERE tenant_id=162),0,'';
INSERT INTO _tmp_verify SELECT 4,'iot_ota_task',(SELECT COUNT(*) FROM iot_ota_task WHERE tenant_id=1),(SELECT COUNT(*) FROM iot_ota_task WHERE tenant_id=162),0,'';
INSERT INTO _tmp_verify SELECT 4,'iot_ota_task_record',(SELECT COUNT(*) FROM iot_ota_task_record WHERE tenant_id=1),(SELECT COUNT(*) FROM iot_ota_task_record WHERE tenant_id=162),0,'';

UPDATE _tmp_verify SET diff = t1_cnt - t162_cnt;

-- 输出：先看不对齐（且非"合理丢弃"标记）的
SELECT '=== MISMATCH (excluding documented orphans) ===' AS section;
SELECT phase,tbl,t1_cnt,t162_cnt,diff,note FROM _tmp_verify WHERE diff <> 0 AND note = '' ORDER BY phase,tbl;

SELECT '=== DOCUMENTED ORPHAN DROPS ===' AS section;
SELECT phase,tbl,t1_cnt,t162_cnt,diff,note FROM _tmp_verify WHERE note <> '' ORDER BY phase,tbl;

SELECT '=== TOTAL ROW COUNT BY PHASE ===' AS section;
SELECT phase, SUM(t1_cnt) AS t1_total, SUM(t162_cnt) AS t162_total, SUM(t1_cnt)-SUM(t162_cnt) AS diff_total, COUNT(*) AS tables FROM _tmp_verify GROUP BY phase ORDER BY phase;

SELECT '=== GRAND TOTAL ===' AS section;
SELECT SUM(t1_cnt) AS t1_grand, SUM(t162_cnt) AS t162_grand, SUM(t1_cnt)-SUM(t162_cnt) AS diff_grand, COUNT(*) AS total_tables FROM _tmp_verify;
