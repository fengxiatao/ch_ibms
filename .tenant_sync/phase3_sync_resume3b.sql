USE ch_ibms;
SET SQL_SAFE_UPDATES=0;

-- 重建 plan_period 映射: 用 ROW_NUMBER 配对(insert 已保持 t1.id 顺序)
DROP TABLE IF EXISTS _tmp_map_eperiod;
CREATE TABLE _tmp_map_eperiod (old_id BIGINT PRIMARY KEY, new_id BIGINT NOT NULL) ENGINE=InnoDB;

INSERT INTO _tmp_map_eperiod (old_id, new_id)
SELECT t1.old_id, t162.new_id FROM
  (SELECT id AS old_id, ROW_NUMBER() OVER (ORDER BY id) rn FROM iot_epatrol_plan_period WHERE tenant_id=1) t1
JOIN
  (SELECT id AS new_id, ROW_NUMBER() OVER (ORDER BY id) rn FROM iot_epatrol_plan_period WHERE tenant_id=162) t162
ON t1.rn = t162.rn;

SELECT 'period_map' tag, COUNT(*) c FROM _tmp_map_eperiod;

-- task
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
