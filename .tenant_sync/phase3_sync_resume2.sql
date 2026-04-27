USE ch_ibms;
SET SQL_SAFE_UPDATES=0;

-- ============================================================================
-- D) 视频视图
-- ============================================================================
INSERT INTO iot_video_view_group
  (name, icon, sort, creator, create_time, updater, update_time, deleted, tenant_id)
SELECT t.name, CONCAT('__OLDID:', t.id, '|', IFNULL(t.icon,'')),
       t.sort, t.creator, t.create_time, t.updater, t.update_time, t.deleted, 162
FROM iot_video_view_group t WHERE t.tenant_id=1;

DROP TABLE IF EXISTS _tmp_map_view_group;
CREATE TABLE _tmp_map_view_group (old_id BIGINT PRIMARY KEY, new_id BIGINT NOT NULL) ENGINE=InnoDB;
INSERT INTO _tmp_map_view_group SELECT CAST(SUBSTRING_INDEX(SUBSTRING(icon,9),'|',1) AS UNSIGNED), id
FROM iot_video_view_group WHERE tenant_id=162 AND icon LIKE '__OLDID:%';
UPDATE iot_video_view_group SET icon=NULLIF(SUBSTRING(icon, LOCATE('|',icon)+1),'')
WHERE tenant_id=162 AND icon LIKE '__OLDID:%';

INSERT INTO iot_video_view
  (name, group_ids, grid_layout, description, is_default, sort,
   creator, create_time, updater, update_time, deleted, tenant_id)
SELECT t.name, t.group_ids, t.grid_layout,
       CONCAT('__OLDID:', t.id, '|', IFNULL(t.description,'')),
       t.is_default, t.sort, t.creator, t.create_time, t.updater, t.update_time, t.deleted, 162
FROM iot_video_view t WHERE t.tenant_id=1;

DROP TABLE IF EXISTS _tmp_map_view;
CREATE TABLE _tmp_map_view (old_id BIGINT PRIMARY KEY, new_id BIGINT NOT NULL) ENGINE=InnoDB;
INSERT INTO _tmp_map_view SELECT CAST(SUBSTRING_INDEX(SUBSTRING(description,9),'|',1) AS UNSIGNED), id
FROM iot_video_view WHERE tenant_id=162 AND description LIKE '__OLDID:%';
UPDATE iot_video_view SET description=NULLIF(SUBSTRING(description, LOCATE('|',description)+1),'')
WHERE tenant_id=162 AND description LIKE '__OLDID:%';

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
       GROUP_CONCAT(COALESCE(gm.new_id, CAST(p.old_gid AS UNSIGNED)) ORDER BY p.n SEPARATOR ';')
FROM parts p
LEFT JOIN _tmp_map_view_group gm ON gm.old_id = CAST(p.old_gid AS UNSIGNED)
GROUP BY p.view_id;

UPDATE iot_video_view v
JOIN _tmp_view_groupids_rewrite r ON r.view_id = v.id
SET v.group_ids = r.new_group_ids
WHERE v.tenant_id = 162;

DROP TABLE _tmp_view_groupids_rewrite;

INSERT INTO iot_video_view_pane
  (view_id, pane_index, channel_id, device_id, channel_no, channel_name, target_ip, target_channel_no,
   stream_url_main, stream_url_sub, config,
   creator, create_time, updater, update_time, tenant_id, deleted)
SELECT vm.new_id, t.pane_index, COALESCE(cm.new_id, t.channel_id), COALESCE(dm.new_id, t.device_id),
       t.channel_no, t.channel_name, t.target_ip, t.target_channel_no,
       t.stream_url_main, t.stream_url_sub, t.config,
       t.creator, t.create_time, t.updater, t.update_time, 162, t.deleted
FROM iot_video_view_pane t
JOIN _tmp_map_view vm ON vm.old_id = t.view_id
LEFT JOIN _tmp_map_channel cm ON cm.old_id = t.channel_id
LEFT JOIN _tmp_map_device dm ON dm.old_id = t.device_id
WHERE t.tenant_id=1;

SELECT '[D]' tag,
  (SELECT COUNT(*) FROM iot_video_view_group WHERE tenant_id=162) g,
  (SELECT COUNT(*) FROM iot_video_view WHERE tenant_id=162) v,
  (SELECT COUNT(*) FROM iot_video_view_pane WHERE tenant_id=162) p;

-- ============================================================================
-- E) 视频巡更
-- ============================================================================
INSERT INTO iot_video_patrol_plan
  (plan_name, plan_code, description, status, running_status, loop_mode, executor, executor_name,
   start_date, end_date, sort, creator, create_time, updater, update_time, deleted, tenant_id)
SELECT t.plan_name, t.plan_code,
       CONCAT('__OLDID:', t.id, '|', IFNULL(t.description,'')),
       t.status, t.running_status, t.loop_mode, t.executor, t.executor_name,
       t.start_date, t.end_date, t.sort,
       t.creator, t.create_time, t.updater, t.update_time, t.deleted, 162
FROM iot_video_patrol_plan t WHERE t.tenant_id=1;

DROP TABLE IF EXISTS _tmp_map_vpplan;
CREATE TABLE _tmp_map_vpplan (old_id BIGINT PRIMARY KEY, new_id BIGINT NOT NULL) ENGINE=InnoDB;
INSERT INTO _tmp_map_vpplan SELECT CAST(SUBSTRING_INDEX(SUBSTRING(description,9),'|',1) AS UNSIGNED), id
FROM iot_video_patrol_plan WHERE tenant_id=162 AND description LIKE '__OLDID:%';
UPDATE iot_video_patrol_plan SET description=NULLIF(SUBSTRING(description, LOCATE('|',description)+1),'')
WHERE tenant_id=162 AND description LIKE '__OLDID:%';

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
       NULL, t.last_run_time, t.sort,
       t.creator, t.create_time, t.updater, t.update_time, t.deleted, 162
FROM iot_video_patrol_task t
LEFT JOIN _tmp_map_vpplan pm ON pm.old_id = t.plan_id
WHERE t.tenant_id=1;

DROP TABLE IF EXISTS _tmp_map_vptask;
CREATE TABLE _tmp_map_vptask (old_id BIGINT PRIMARY KEY, new_id BIGINT NOT NULL) ENGINE=InnoDB;
INSERT INTO _tmp_map_vptask SELECT CAST(SUBSTRING_INDEX(SUBSTRING(description,9),'|',1) AS UNSIGNED), id
FROM iot_video_patrol_task WHERE tenant_id=162 AND description LIKE '__OLDID:%';
UPDATE iot_video_patrol_task SET description=NULLIF(SUBSTRING(description, LOCATE('|',description)+1),'')
WHERE tenant_id=162 AND description LIKE '__OLDID:%';

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
INSERT INTO _tmp_map_vpscene SELECT CAST(SUBSTRING_INDEX(SUBSTRING(description,9),'|',1) AS UNSIGNED), id
FROM iot_video_patrol_scene WHERE tenant_id=162 AND description LIKE '__OLDID:%';
UPDATE iot_video_patrol_scene SET description=NULLIF(SUBSTRING(description, LOCATE('|',description)+1),'')
WHERE tenant_id=162 AND description LIKE '__OLDID:%';

UPDATE iot_video_patrol_task t162
JOIN _tmp_map_vptask tm ON tm.new_id = t162.id
JOIN iot_video_patrol_task t1 ON t1.id = tm.old_id AND t1.tenant_id = 1
JOIN _tmp_map_vpscene sm ON sm.old_id = t1.current_scene_id
SET t162.current_scene_id = sm.new_id
WHERE t162.tenant_id = 162;

INSERT INTO iot_video_patrol_scene_channel
  (scene_id, grid_position, duration, channel_id, device_id, channel_no, channel_name,
   target_ip, target_channel_no, stream_url_main, stream_url_sub, wvp_play_id, config,
   creator, create_time, updater, update_time, deleted, tenant_id)
SELECT sm.new_id, t.grid_position, t.duration, COALESCE(cm.new_id, t.channel_id), COALESCE(dm.new_id, t.device_id),
       t.channel_no, t.channel_name, t.target_ip, t.target_channel_no,
       t.stream_url_main, t.stream_url_sub, t.wvp_play_id, t.config,
       t.creator, t.create_time, t.updater, t.update_time, t.deleted, 162
FROM iot_video_patrol_scene_channel t
JOIN _tmp_map_vpscene sm ON sm.old_id = t.scene_id
LEFT JOIN _tmp_map_channel cm ON cm.old_id = t.channel_id
LEFT JOIN _tmp_map_device dm ON dm.old_id = t.device_id
WHERE t.tenant_id=1;

INSERT INTO iot_video_patrol_schedule
  (name, patrol_plan_id, schedule_type, start_time, end_time, week_days, status, remark,
   creator, create_time, updater, update_time, deleted, tenant_id)
SELECT t.name, pm.new_id, t.schedule_type, t.start_time, t.end_time, t.week_days, t.status, t.remark,
       t.creator, t.create_time, t.updater, t.update_time, t.deleted, 162
FROM iot_video_patrol_schedule t
LEFT JOIN _tmp_map_vpplan pm ON pm.old_id = t.patrol_plan_id
WHERE t.tenant_id=1;

SELECT '[E]' tag,
  (SELECT COUNT(*) FROM iot_video_patrol_plan WHERE tenant_id=162) plan_,
  (SELECT COUNT(*) FROM iot_video_patrol_task WHERE tenant_id=162) task_,
  (SELECT COUNT(*) FROM iot_video_patrol_scene WHERE tenant_id=162) scene_,
  (SELECT COUNT(*) FROM iot_video_patrol_scene_channel WHERE tenant_id=162) sc,
  (SELECT COUNT(*) FROM iot_video_patrol_schedule WHERE tenant_id=162) sched;

-- F)
INSERT INTO iot_video_inspection_task
  (tenant_id, task_name, layout, scenes, status, creator, create_time, updater, update_time, deleted)
SELECT 162, task_name, layout, scenes, status, creator, create_time, updater, update_time, deleted
FROM iot_video_inspection_task WHERE tenant_id=1;

SELECT '[F]' tag, (SELECT COUNT(*) FROM iot_video_inspection_task WHERE tenant_id=162) c;
