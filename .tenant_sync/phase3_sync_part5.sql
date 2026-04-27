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
