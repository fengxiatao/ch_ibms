-- ============================================================
-- 长辉IBMS 租户 (tenant_id=162) 一键回滚脚本
-- 适用：撤销 2026-04-26 ~ 2026-04-27 期间从 tenant_id=1 复制过来的全量同步
-- 操作：
--   1) 关闭 FK 检查
--   2) 按"反向"顺序 DELETE tenant_id=162（主表先删依赖表，再删被依赖表）
--   3) 从 bak_*_t162_* 备份表 INSERT 还原同步前 t162 端原有数据（共 7 张非空备份）
--   4) 备份表本身不删除，删除请手工执行末尾 DROP 段或 phase6_drop_backups.sql
-- 实例：127.0.0.1:3306（ch_ibms）
-- 使用：mysql -h127.0.0.1 -uroot -p123456 ch_ibms < rollback_tenant_162.sql
-- ============================================================

USE ch_ibms;
SET FOREIGN_KEY_CHECKS = 0;
SET UNIQUE_CHECKS = 0;

-- ===== Phase 4 反向（38 张） =====
DELETE FROM iot_ota_task_record               WHERE tenant_id=162;
DELETE FROM iot_ota_task                      WHERE tenant_id=162;
DELETE FROM iot_access_operation_log          WHERE tenant_id=162;
DELETE FROM iot_access_event_log              WHERE tenant_id=162;
DELETE FROM iot_access_auth_task_detail       WHERE tenant_id=162;
DELETE FROM iot_access_auth_task              WHERE tenant_id=162;
DELETE FROM iot_access_permission_group_person WHERE tenant_id=162;
DELETE FROM iot_access_permission_group_device WHERE tenant_id=162;
DELETE FROM iot_access_device_capability      WHERE tenant_id=162;
DELETE FROM iot_access_person_device_auth     WHERE tenant_id=162;
DELETE FROM iot_access_person_credential      WHERE tenant_id=162;
DELETE FROM iot_device_event_log              WHERE tenant_id=162;
DELETE FROM iot_device_channel                WHERE tenant_id=162;
DELETE FROM iot_visitor_abnormal_event        WHERE tenant_id=162;
DELETE FROM iot_parking_refund_record         WHERE tenant_id=162;
DELETE FROM iot_parking_record                WHERE tenant_id=162;
DELETE FROM iot_parking_present_vehicle       WHERE tenant_id=162;
DELETE FROM iot_parking_wechat_user           WHERE tenant_id=162;
DELETE FROM iot_parking_system_config         WHERE tenant_id=162;
DELETE FROM iot_parking_charge_rule_apply     WHERE tenant_id=162;
DELETE FROM iot_parking_pass_rule             WHERE tenant_id=162;
DELETE FROM iot_parking_free_vehicle          WHERE tenant_id=162;
DELETE FROM iot_parking_blacklist             WHERE tenant_id=162;
DELETE FROM iot_parking_monthly_recharge      WHERE tenant_id=162;
DELETE FROM iot_parking_monthly_vehicle       WHERE tenant_id=162;
DELETE FROM iot_parking_gate                  WHERE tenant_id=162;
DELETE FROM iot_parking_lane                  WHERE tenant_id=162;
DELETE FROM iot_access_person                 WHERE tenant_id=162;
DELETE FROM iot_device_display_config         WHERE tenant_id=162;
DELETE FROM iot_device_group                  WHERE tenant_id=162;
DELETE FROM iot_visitor_appointment           WHERE tenant_id=162;
DELETE FROM iot_parking_charge_rule           WHERE tenant_id=162;
DELETE FROM iot_parking_lot                   WHERE tenant_id=162;
DELETE FROM iot_access_permission_group       WHERE tenant_id=162;
DELETE FROM iot_access_department             WHERE tenant_id=162;
DELETE FROM iot_ota_firmware                  WHERE tenant_id=162;
DELETE FROM iot_keding_firmware               WHERE tenant_id=162;
DELETE FROM iot_keding_device                 WHERE tenant_id=162;

-- ===== Phase 3 反向（28 张同步 + 3 张 video_view 系列已在 t162 原存在） =====
DELETE FROM iot_epatrol_task_record           WHERE tenant_id=162;
DELETE FROM iot_epatrol_task                  WHERE tenant_id=162;
DELETE FROM iot_epatrol_plan_period           WHERE tenant_id=162;
DELETE FROM iot_epatrol_plan                  WHERE tenant_id=162;
DELETE FROM iot_epatrol_route_point           WHERE tenant_id=162;
DELETE FROM iot_epatrol_route                 WHERE tenant_id=162;
DELETE FROM iot_epatrol_point                 WHERE tenant_id=162;
DELETE FROM iot_epatrol_person                WHERE tenant_id=162;
DELETE FROM iot_video_inspection_task         WHERE tenant_id=162;
DELETE FROM iot_video_patrol_schedule         WHERE tenant_id=162;
DELETE FROM iot_video_patrol_scene_channel    WHERE tenant_id=162;
DELETE FROM iot_video_patrol_scene            WHERE tenant_id=162;
DELETE FROM iot_video_patrol_task             WHERE tenant_id=162;
DELETE FROM iot_video_patrol_plan             WHERE tenant_id=162;
DELETE FROM iot_video_view_pane               WHERE tenant_id=162;
DELETE FROM iot_video_view                    WHERE tenant_id=162;
DELETE FROM iot_video_view_group              WHERE tenant_id=162;
DELETE FROM iot_cloud_defense_score_log       WHERE tenant_id=162;
DELETE FROM iot_cloud_defense_point           WHERE tenant_id=162;
DELETE FROM iot_cloud_defense_area_device_rel WHERE tenant_id=162;
DELETE FROM iot_cloud_defense_area            WHERE tenant_id=162;
DELETE FROM iot_cloud_defense_mode            WHERE tenant_id=162;
DELETE FROM iot_camera_snapshot               WHERE tenant_id=162;
DELETE FROM iot_camera_recording              WHERE tenant_id=162;
DELETE FROM iot_camera_cruise_point           WHERE tenant_id=162;
DELETE FROM iot_camera_cruise                 WHERE tenant_id=162;
DELETE FROM iot_camera_preset                 WHERE tenant_id=162;
DELETE FROM iot_alarm_operation_log           WHERE tenant_id=162;
DELETE FROM iot_alarm_event                   WHERE tenant_id=162;
DELETE FROM iot_alarm_zone                    WHERE tenant_id=162;
DELETE FROM iot_alarm_partition               WHERE tenant_id=162;
DELETE FROM iot_alarm_host                    WHERE tenant_id=162;

-- ===== Phase 2 反向（27 张） =====
DELETE FROM ibms_energy_manual_reading        WHERE tenant_id=162;
DELETE FROM ibms_energy_alarm                 WHERE tenant_id=162;
DELETE FROM ibms_energy_statistics_daily      WHERE tenant_id=162;
DELETE FROM ibms_energy_record                WHERE tenant_id=162;
DELETE FROM ibms_energy_meter                 WHERE tenant_id=162;
DELETE FROM ibms_energy_rate                  WHERE tenant_id=162;
DELETE FROM ibms_bac_system_log               WHERE tenant_id=162;
DELETE FROM ibms_bac_alarm                    WHERE tenant_id=162;
DELETE FROM ibms_env_alarm                    WHERE tenant_id=162;
DELETE FROM ibms_env_data_record              WHERE tenant_id=162;
DELETE FROM ibms_lighting_operation_log       WHERE tenant_id=162;
DELETE FROM ibms_lighting_alarm               WHERE tenant_id=162;
DELETE FROM ibms_lighting_schedule            WHERE tenant_id=162;
DELETE FROM ibms_lighting_scene               WHERE tenant_id=162;
DELETE FROM ibms_lighting_circuit             WHERE tenant_id=162;
DELETE FROM ibms_lighting_controller          WHERE tenant_id=162;
DELETE FROM ibms_lighting_gateway             WHERE tenant_id=162;
DELETE FROM ibms_water_device                 WHERE tenant_id=162;
DELETE FROM ibms_hvac_device                  WHERE tenant_id=162;
DELETE FROM ibms_device_runtime               WHERE tenant_id=162;
DELETE FROM ibms_channel                      WHERE tenant_id=162;
DELETE FROM ibms_device                       WHERE tenant_id=162;
DELETE FROM ibms_space                        WHERE tenant_id=162;
DELETE FROM area                              WHERE tenant_id=162;
DELETE FROM floor                             WHERE tenant_id=162;
DELETE FROM building                          WHERE tenant_id=162;
DELETE FROM campus                            WHERE tenant_id=162;

-- ===== Phase 1 反向（10 张） =====
DELETE FROM ibms_product_point_type           WHERE tenant_id=162;
DELETE FROM ibms_product_property             WHERE tenant_id=162;
DELETE FROM ibms_product                      WHERE tenant_id=162;
DELETE FROM iot_scheduled_task_config         WHERE tenant_id=162;
DELETE FROM iot_product_category              WHERE tenant_id=162;
DELETE FROM iot_data_rule                     WHERE tenant_id=162;
DELETE FROM iot_data_sink                     WHERE tenant_id=162;
DELETE FROM iot_thing_model                   WHERE tenant_id=162;
DELETE FROM iot_job_type_definition           WHERE tenant_id=162;
DELETE FROM iot_subsystem                     WHERE tenant_id=162;

-- ============================================================
-- 还原备份：t162 端在同步前已存在的预置数据（共 7 张非空备份）
-- ============================================================

INSERT INTO ibms_space               SELECT * FROM bak_ibms_space_t162_20260426;                  -- 4
INSERT INTO ibms_device              SELECT * FROM bak_ibms_device_t162_20260426;                 -- 19
INSERT INTO ibms_channel             SELECT * FROM bak_ibms_channel_t162_20260426;                -- 39
INSERT INTO ibms_device_runtime      SELECT * FROM bak_ibms_device_runtime_t162_20260426;         -- 4
INSERT INTO iot_video_view_group     SELECT * FROM bak_iot_video_view_group_t162_20260426;        -- 3
INSERT INTO iot_video_view           SELECT * FROM bak_iot_video_view_t162_20260426;              -- 6
INSERT INTO iot_video_view_pane      SELECT * FROM bak_iot_video_view_pane_t162_20260426;         -- 20

SET FOREIGN_KEY_CHECKS = 1;
SET UNIQUE_CHECKS = 1;

-- 校验：t162 行数应仅剩还原的 7 张备份对应行
SELECT '=== 回滚后 t162 残留行数（应为 0 或仅备份还原值） ===' AS msg;
SELECT 'ibms_space' tbl, COUNT(*) cnt FROM ibms_space WHERE tenant_id=162
UNION ALL SELECT 'ibms_device', COUNT(*) FROM ibms_device WHERE tenant_id=162
UNION ALL SELECT 'ibms_channel', COUNT(*) FROM ibms_channel WHERE tenant_id=162
UNION ALL SELECT 'ibms_device_runtime', COUNT(*) FROM ibms_device_runtime WHERE tenant_id=162
UNION ALL SELECT 'iot_video_view_group', COUNT(*) FROM iot_video_view_group WHERE tenant_id=162
UNION ALL SELECT 'iot_video_view', COUNT(*) FROM iot_video_view WHERE tenant_id=162
UNION ALL SELECT 'iot_video_view_pane', COUNT(*) FROM iot_video_view_pane WHERE tenant_id=162
UNION ALL SELECT 'campus', COUNT(*) FROM campus WHERE tenant_id=162
UNION ALL SELECT 'building', COUNT(*) FROM building WHERE tenant_id=162
UNION ALL SELECT 'area', COUNT(*) FROM area WHERE tenant_id=162
UNION ALL SELECT 'iot_alarm_event', COUNT(*) FROM iot_alarm_event WHERE tenant_id=162
UNION ALL SELECT 'iot_access_event_log', COUNT(*) FROM iot_access_event_log WHERE tenant_id=162;

-- ============================================================
-- 备份表清单（不在本脚本删除；如确认无需保留，单独执行 phase6_drop_backups.sql）
-- ============================================================
