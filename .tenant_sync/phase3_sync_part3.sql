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
