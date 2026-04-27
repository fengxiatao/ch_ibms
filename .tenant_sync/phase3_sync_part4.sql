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
