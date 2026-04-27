USE ch_ibms;
SET SQL_SAFE_UPDATES=0;

-- ============================================================================
-- G) 电子巡更
-- ============================================================================
INSERT INTO iot_epatrol_person
  (name, phone, patrol_stick_no, person_card_no, status, remark,
   creator, create_time, updater, update_time, deleted, tenant_id)
SELECT t.name, t.phone,
       CASE WHEN t.patrol_stick_no IS NOT NULL AND t.patrol_stick_no<>'' THEN CONCAT(t.patrol_stick_no,'-ibms') ELSE t.patrol_stick_no END,
       CASE WHEN t.person_card_no IS NOT NULL AND t.person_card_no<>'' THEN CONCAT(t.person_card_no,'-ibms') ELSE t.person_card_no END,
       t.status, CONCAT('__OLDID:', t.id, '|', IFNULL(t.remark,'')),
       t.creator, t.create_time, t.updater, t.update_time, t.deleted, 162
FROM iot_epatrol_person t WHERE t.tenant_id=1;

DROP TABLE IF EXISTS _tmp_map_eperson;
CREATE TABLE _tmp_map_eperson (old_id BIGINT PRIMARY KEY, new_id BIGINT NOT NULL) ENGINE=InnoDB;
INSERT INTO _tmp_map_eperson SELECT CAST(SUBSTRING_INDEX(SUBSTRING(remark,9),'|',1) AS UNSIGNED), id
FROM iot_epatrol_person WHERE tenant_id=162 AND remark LIKE '__OLDID:%';
UPDATE iot_epatrol_person SET remark=NULLIF(SUBSTRING(remark, LOCATE('|',remark)+1),'')
WHERE tenant_id=162 AND remark LIKE '__OLDID:%';

INSERT INTO iot_epatrol_point
  (point_no, point_name, point_location, status, remark,
   creator, create_time, updater, update_time, deleted, tenant_id)
SELECT t.point_no, t.point_name, t.point_location, t.status,
       CONCAT('__OLDID:', t.id, '|', IFNULL(t.remark,'')),
       t.creator, t.create_time, t.updater, t.update_time, t.deleted, 162
FROM iot_epatrol_point t WHERE t.tenant_id=1;

DROP TABLE IF EXISTS _tmp_map_epoint;
CREATE TABLE _tmp_map_epoint (old_id BIGINT PRIMARY KEY, new_id BIGINT NOT NULL) ENGINE=InnoDB;
INSERT INTO _tmp_map_epoint SELECT CAST(SUBSTRING_INDEX(SUBSTRING(remark,9),'|',1) AS UNSIGNED), id
FROM iot_epatrol_point WHERE tenant_id=162 AND remark LIKE '__OLDID:%';
UPDATE iot_epatrol_point SET remark=NULLIF(SUBSTRING(remark, LOCATE('|',remark)+1),'')
WHERE tenant_id=162 AND remark LIKE '__OLDID:%';

INSERT INTO iot_epatrol_route
  (route_name, point_count, total_duration, status, remark,
   creator, create_time, updater, update_time, deleted, tenant_id)
SELECT t.route_name, t.point_count, t.total_duration, t.status,
       CONCAT('__OLDID:', t.id, '|', IFNULL(t.remark,'')),
       t.creator, t.create_time, t.updater, t.update_time, t.deleted, 162
FROM iot_epatrol_route t WHERE t.tenant_id=1;

DROP TABLE IF EXISTS _tmp_map_eroute;
CREATE TABLE _tmp_map_eroute (old_id BIGINT PRIMARY KEY, new_id BIGINT NOT NULL) ENGINE=InnoDB;
INSERT INTO _tmp_map_eroute SELECT CAST(SUBSTRING_INDEX(SUBSTRING(remark,9),'|',1) AS UNSIGNED), id
FROM iot_epatrol_route WHERE tenant_id=162 AND remark LIKE '__OLDID:%';
UPDATE iot_epatrol_route SET remark=NULLIF(SUBSTRING(remark, LOCATE('|',remark)+1),'')
WHERE tenant_id=162 AND remark LIKE '__OLDID:%';

INSERT INTO iot_epatrol_route_point
  (route_id, point_id, sort, interval_minutes,
   creator, create_time, updater, update_time, deleted, tenant_id)
SELECT rm.new_id, pm.new_id, t.sort, t.interval_minutes,
       t.creator, t.create_time, t.updater, t.update_time, t.deleted, 162
FROM iot_epatrol_route_point t
JOIN _tmp_map_eroute rm ON rm.old_id = t.route_id
JOIN _tmp_map_epoint pm ON pm.old_id = t.point_id
WHERE t.tenant_id=1;

INSERT INTO iot_epatrol_plan
  (plan_code, plan_name, route_id, start_date, end_date, weekdays, status, remark,
   creator, create_time, updater, update_time, deleted, tenant_id)
SELECT t.plan_code, t.plan_name, COALESCE(rm.new_id, t.route_id),
       t.start_date, t.end_date, t.weekdays, t.status,
       CONCAT('__OLDID:', t.id, '|', IFNULL(t.remark,'')),
       t.creator, t.create_time, t.updater, t.update_time, t.deleted, 162
FROM iot_epatrol_plan t
LEFT JOIN _tmp_map_eroute rm ON rm.old_id = t.route_id
WHERE t.tenant_id=1;

DROP TABLE IF EXISTS _tmp_map_eplan;
CREATE TABLE _tmp_map_eplan (old_id BIGINT PRIMARY KEY, new_id BIGINT NOT NULL) ENGINE=InnoDB;
INSERT INTO _tmp_map_eplan SELECT CAST(SUBSTRING_INDEX(SUBSTRING(remark,9),'|',1) AS UNSIGNED), id
FROM iot_epatrol_plan WHERE tenant_id=162 AND remark LIKE '__OLDID:%';
UPDATE iot_epatrol_plan SET remark=NULLIF(SUBSTRING(remark, LOCATE('|',remark)+1),'')
WHERE tenant_id=162 AND remark LIKE '__OLDID:%';

INSERT INTO iot_epatrol_plan_period
  (plan_id, route_id, start_time, duration_minutes, person_ids,
   creator, create_time, updater, update_time, deleted, tenant_id)
SELECT pm.new_id, COALESCE(rm.new_id, t.route_id), t.start_time, t.duration_minutes,
       (SELECT CAST(CONCAT('[', IFNULL(GROUP_CONCAT(COALESCE(epm.new_id, j.old_pid) ORDER BY j.idx), ''), ']') AS JSON)
        FROM JSON_TABLE(t.person_ids, '$[*]' COLUMNS (idx FOR ORDINALITY, old_pid BIGINT PATH '$')) j
        LEFT JOIN _tmp_map_eperson epm ON epm.old_id = j.old_pid),
       t.creator, t.create_time, t.updater, t.update_time, t.deleted, 162
FROM iot_epatrol_plan_period t
JOIN _tmp_map_eplan pm ON pm.old_id = t.plan_id
LEFT JOIN _tmp_map_eroute rm ON rm.old_id = t.route_id
WHERE t.tenant_id=1;

DROP TABLE IF EXISTS _tmp_map_eperiod;
CREATE TABLE _tmp_map_eperiod (old_id BIGINT PRIMARY KEY, new_id BIGINT NOT NULL) ENGINE=InnoDB;
INSERT INTO _tmp_map_eperiod
SELECT t1.id, t162.id
FROM iot_epatrol_plan_period t1
JOIN _tmp_map_eplan plm ON plm.old_id = t1.plan_id
JOIN iot_epatrol_plan_period t162
   ON t162.tenant_id = 162
  AND t162.plan_id = plm.new_id
  AND t162.start_time <=> t1.start_time
  AND t162.duration_minutes <=> t1.duration_minutes
WHERE t1.tenant_id = 1;

INSERT INTO iot_epatrol_task
  (task_code, plan_id, period_id, route_id, task_date, planned_start_time, planned_end_time, person_ids,
   status, submit_time, submitter_id, remark,
   creator, create_time, updater, update_time, deleted, tenant_id)
SELECT t.task_code, pm.new_id, prm.new_id, COALESCE(rm.new_id, t.route_id),
       t.task_date, t.planned_start_time, t.planned_end_time,
       (SELECT CAST(CONCAT('[', IFNULL(GROUP_CONCAT(COALESCE(epm.new_id, j.old_pid) ORDER BY j.idx), ''), ']') AS JSON)
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
INSERT INTO _tmp_map_etask SELECT CAST(SUBSTRING_INDEX(SUBSTRING(remark,9),'|',1) AS UNSIGNED), id
FROM iot_epatrol_task WHERE tenant_id=162 AND remark LIKE '__OLDID:%';
UPDATE iot_epatrol_task SET remark=NULLIF(SUBSTRING(remark, LOCATE('|',remark)+1),'')
WHERE tenant_id=162 AND remark LIKE '__OLDID:%';

INSERT INTO iot_epatrol_task_record
  (task_id, point_id, point_no, point_name, person_id, person_name,
   expected_sort, actual_sort, planned_time, actual_time, patrol_status, time_diff_seconds, remark,
   creator, create_time, updater, update_time, deleted, tenant_id)
SELECT tm.new_id, COALESCE(pm.new_id, t.point_id), t.point_no, t.point_name,
       COALESCE(prm.new_id, t.person_id), t.person_name,
       t.expected_sort, t.actual_sort, t.planned_time, t.actual_time, t.patrol_status, t.time_diff_seconds, t.remark,
       t.creator, t.create_time, t.updater, t.update_time, t.deleted, 162
FROM iot_epatrol_task_record t
JOIN _tmp_map_etask tm ON tm.old_id = t.task_id
LEFT JOIN _tmp_map_epoint pm ON pm.old_id = t.point_id
LEFT JOIN _tmp_map_eperson prm ON prm.old_id = t.person_id
WHERE t.tenant_id=1;

SELECT '[G]' tag,
  (SELECT COUNT(*) FROM iot_epatrol_person WHERE tenant_id=162) person_,
  (SELECT COUNT(*) FROM iot_epatrol_point WHERE tenant_id=162) point_,
  (SELECT COUNT(*) FROM iot_epatrol_route WHERE tenant_id=162) route_,
  (SELECT COUNT(*) FROM iot_epatrol_route_point WHERE tenant_id=162) rp,
  (SELECT COUNT(*) FROM iot_epatrol_plan WHERE tenant_id=162) plan_,
  (SELECT COUNT(*) FROM iot_epatrol_plan_period WHERE tenant_id=162) period_,
  (SELECT COUNT(*) FROM iot_epatrol_task WHERE tenant_id=162) task_,
  (SELECT COUNT(*) FROM iot_epatrol_task_record WHERE tenant_id=162) tr;

SELECT '[Phase 3] ALL DONE' tag, NOW() ts;
