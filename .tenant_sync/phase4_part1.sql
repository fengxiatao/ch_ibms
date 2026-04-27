-- ============================================================================
-- Phase 4 — 智慧通行 数据同步 (tenant_id=1 → tenant_id=162)  PART 1
-- 实例: 127.0.0.1:3306 / ch_ibms ; 日期: 20260427
-- 范围(part1): maps 重建 + 38 张表备份 + 独立基础表插入
--   iot_keding_device, iot_keding_firmware, iot_ota_firmware,
--   iot_access_department(+self FK update), iot_access_permission_group,
--   iot_parking_lot, iot_parking_charge_rule, iot_visitor_appointment,
--   iot_device_group, iot_device_display_config
-- 后缀字段: iot_keding_device.station_code, iot_parking_wechat_user.openid/username
-- ============================================================================
USE ch_ibms;
SET SQL_SAFE_UPDATES = 0;
SET @D = '20260427';

-- ============================================================================
-- 1) 重建 Phase 1/2 实体的 ID 映射 (基于已存在 t162 数据)
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

-- area/floor/building 无自然 UK 且 Phase 4 实际不需要（iot_device_channel 字段为 NULL）

DROP TABLE IF EXISTS _tmp_map_product;
CREATE TABLE _tmp_map_product (old_id BIGINT PRIMARY KEY, new_id BIGINT NOT NULL) ENGINE=InnoDB;
INSERT INTO _tmp_map_product (old_id, new_id)
SELECT t1.id, t162.id FROM ibms_product t1
JOIN ibms_product t162 ON t162.product_code = CONCAT(t1.product_code, '-ibms')
WHERE t1.tenant_id = 1 AND t162.tenant_id = 162;

SELECT '[map_rebuild]' tag,
  (SELECT COUNT(*) FROM _tmp_map_device) device,
  (SELECT COUNT(*) FROM _tmp_map_channel) channel,
  (SELECT COUNT(*) FROM _tmp_map_space) space,
  (SELECT COUNT(*) FROM _tmp_map_product) product;

-- ============================================================================
-- 2) 备份 38 张目标表 (tenant_id=162; 全部为空, 仍创建备份表保持流程一致)
-- ============================================================================
DROP TABLE IF EXISTS bak_iot_keding_device_t162_20260427;
CREATE TABLE bak_iot_keding_device_t162_20260427 AS SELECT * FROM iot_keding_device WHERE tenant_id=162;
DROP TABLE IF EXISTS bak_iot_keding_firmware_t162_20260427;
CREATE TABLE bak_iot_keding_firmware_t162_20260427 AS SELECT * FROM iot_keding_firmware WHERE tenant_id=162;
DROP TABLE IF EXISTS bak_iot_access_person_t162_20260427;
CREATE TABLE bak_iot_access_person_t162_20260427 AS SELECT * FROM iot_access_person WHERE tenant_id=162;
DROP TABLE IF EXISTS bak_iot_access_person_credential_t162_20260427;
CREATE TABLE bak_iot_access_person_credential_t162_20260427 AS SELECT * FROM iot_access_person_credential WHERE tenant_id=162;
DROP TABLE IF EXISTS bak_iot_access_person_device_auth_t162_20260427;
CREATE TABLE bak_iot_access_person_device_auth_t162_20260427 AS SELECT * FROM iot_access_person_device_auth WHERE tenant_id=162;
DROP TABLE IF EXISTS bak_iot_access_department_t162_20260427;
CREATE TABLE bak_iot_access_department_t162_20260427 AS SELECT * FROM iot_access_department WHERE tenant_id=162;
DROP TABLE IF EXISTS bak_iot_access_permission_group_t162_20260427;
CREATE TABLE bak_iot_access_permission_group_t162_20260427 AS SELECT * FROM iot_access_permission_group WHERE tenant_id=162;
DROP TABLE IF EXISTS bak_iot_access_permission_group_device_t162_20260427;
CREATE TABLE bak_iot_access_permission_group_device_t162_20260427 AS SELECT * FROM iot_access_permission_group_device WHERE tenant_id=162;
DROP TABLE IF EXISTS bak_iot_access_permission_group_person_t162_20260427;
CREATE TABLE bak_iot_access_permission_group_person_t162_20260427 AS SELECT * FROM iot_access_permission_group_person WHERE tenant_id=162;
DROP TABLE IF EXISTS bak_iot_access_auth_task_t162_20260427;
CREATE TABLE bak_iot_access_auth_task_t162_20260427 AS SELECT * FROM iot_access_auth_task WHERE tenant_id=162;
DROP TABLE IF EXISTS bak_iot_access_auth_task_detail_t162_20260427;
CREATE TABLE bak_iot_access_auth_task_detail_t162_20260427 AS SELECT * FROM iot_access_auth_task_detail WHERE tenant_id=162;
DROP TABLE IF EXISTS bak_iot_access_device_capability_t162_20260427;
CREATE TABLE bak_iot_access_device_capability_t162_20260427 AS SELECT * FROM iot_access_device_capability WHERE tenant_id=162;
DROP TABLE IF EXISTS bak_iot_access_event_log_t162_20260427;
CREATE TABLE bak_iot_access_event_log_t162_20260427 AS SELECT * FROM iot_access_event_log WHERE tenant_id=162;
DROP TABLE IF EXISTS bak_iot_access_operation_log_t162_20260427;
CREATE TABLE bak_iot_access_operation_log_t162_20260427 AS SELECT * FROM iot_access_operation_log WHERE tenant_id=162;
DROP TABLE IF EXISTS bak_iot_visitor_appointment_t162_20260427;
CREATE TABLE bak_iot_visitor_appointment_t162_20260427 AS SELECT * FROM iot_visitor_appointment WHERE tenant_id=162;
DROP TABLE IF EXISTS bak_iot_visitor_abnormal_event_t162_20260427;
CREATE TABLE bak_iot_visitor_abnormal_event_t162_20260427 AS SELECT * FROM iot_visitor_abnormal_event WHERE tenant_id=162;
DROP TABLE IF EXISTS bak_iot_parking_lot_t162_20260427;
CREATE TABLE bak_iot_parking_lot_t162_20260427 AS SELECT * FROM iot_parking_lot WHERE tenant_id=162;
DROP TABLE IF EXISTS bak_iot_parking_gate_t162_20260427;
CREATE TABLE bak_iot_parking_gate_t162_20260427 AS SELECT * FROM iot_parking_gate WHERE tenant_id=162;
DROP TABLE IF EXISTS bak_iot_parking_lane_t162_20260427;
CREATE TABLE bak_iot_parking_lane_t162_20260427 AS SELECT * FROM iot_parking_lane WHERE tenant_id=162;
DROP TABLE IF EXISTS bak_iot_parking_charge_rule_t162_20260427;
CREATE TABLE bak_iot_parking_charge_rule_t162_20260427 AS SELECT * FROM iot_parking_charge_rule WHERE tenant_id=162;
DROP TABLE IF EXISTS bak_iot_parking_charge_rule_apply_t162_20260427;
CREATE TABLE bak_iot_parking_charge_rule_apply_t162_20260427 AS SELECT * FROM iot_parking_charge_rule_apply WHERE tenant_id=162;
DROP TABLE IF EXISTS bak_iot_parking_blacklist_t162_20260427;
CREATE TABLE bak_iot_parking_blacklist_t162_20260427 AS SELECT * FROM iot_parking_blacklist WHERE tenant_id=162;
DROP TABLE IF EXISTS bak_iot_parking_free_vehicle_t162_20260427;
CREATE TABLE bak_iot_parking_free_vehicle_t162_20260427 AS SELECT * FROM iot_parking_free_vehicle WHERE tenant_id=162;
DROP TABLE IF EXISTS bak_iot_parking_monthly_vehicle_t162_20260427;
CREATE TABLE bak_iot_parking_monthly_vehicle_t162_20260427 AS SELECT * FROM iot_parking_monthly_vehicle WHERE tenant_id=162;
DROP TABLE IF EXISTS bak_iot_parking_monthly_recharge_t162_20260427;
CREATE TABLE bak_iot_parking_monthly_recharge_t162_20260427 AS SELECT * FROM iot_parking_monthly_recharge WHERE tenant_id=162;
DROP TABLE IF EXISTS bak_iot_parking_pass_rule_t162_20260427;
CREATE TABLE bak_iot_parking_pass_rule_t162_20260427 AS SELECT * FROM iot_parking_pass_rule WHERE tenant_id=162;
DROP TABLE IF EXISTS bak_iot_parking_present_vehicle_t162_20260427;
CREATE TABLE bak_iot_parking_present_vehicle_t162_20260427 AS SELECT * FROM iot_parking_present_vehicle WHERE tenant_id=162;
DROP TABLE IF EXISTS bak_iot_parking_record_t162_20260427;
CREATE TABLE bak_iot_parking_record_t162_20260427 AS SELECT * FROM iot_parking_record WHERE tenant_id=162;
DROP TABLE IF EXISTS bak_iot_parking_refund_record_t162_20260427;
CREATE TABLE bak_iot_parking_refund_record_t162_20260427 AS SELECT * FROM iot_parking_refund_record WHERE tenant_id=162;
DROP TABLE IF EXISTS bak_iot_parking_system_config_t162_20260427;
CREATE TABLE bak_iot_parking_system_config_t162_20260427 AS SELECT * FROM iot_parking_system_config WHERE tenant_id=162;
DROP TABLE IF EXISTS bak_iot_parking_wechat_user_t162_20260427;
CREATE TABLE bak_iot_parking_wechat_user_t162_20260427 AS SELECT * FROM iot_parking_wechat_user WHERE tenant_id=162;
DROP TABLE IF EXISTS bak_iot_device_channel_t162_20260427;
CREATE TABLE bak_iot_device_channel_t162_20260427 AS SELECT * FROM iot_device_channel WHERE tenant_id=162;
DROP TABLE IF EXISTS bak_iot_device_event_log_t162_20260427;
CREATE TABLE bak_iot_device_event_log_t162_20260427 AS SELECT * FROM iot_device_event_log WHERE tenant_id=162;
DROP TABLE IF EXISTS bak_iot_device_display_config_t162_20260427;
CREATE TABLE bak_iot_device_display_config_t162_20260427 AS SELECT * FROM iot_device_display_config WHERE tenant_id=162;
DROP TABLE IF EXISTS bak_iot_device_group_t162_20260427;
CREATE TABLE bak_iot_device_group_t162_20260427 AS SELECT * FROM iot_device_group WHERE tenant_id=162;
DROP TABLE IF EXISTS bak_iot_ota_firmware_t162_20260427;
CREATE TABLE bak_iot_ota_firmware_t162_20260427 AS SELECT * FROM iot_ota_firmware WHERE tenant_id=162;
DROP TABLE IF EXISTS bak_iot_ota_task_t162_20260427;
CREATE TABLE bak_iot_ota_task_t162_20260427 AS SELECT * FROM iot_ota_task WHERE tenant_id=162;
DROP TABLE IF EXISTS bak_iot_ota_task_record_t162_20260427;
CREATE TABLE bak_iot_ota_task_record_t162_20260427 AS SELECT * FROM iot_ota_task_record WHERE tenant_id=162;

-- ============================================================================
-- 3) iot_keding_device (t1=12): station_code 加 -ibms; 无 FK
-- ============================================================================
INSERT INTO iot_keding_device
  (station_code, device_name, device_type, province_code, management_code,
   station_code_part, pile_front, pile_back, manufacturer, sequence_no,
   tea_key, password, status, last_heartbeat,
   creator, create_time, updater, update_time, deleted, tenant_id)
SELECT CONCAT(t.station_code, '-ibms'), t.device_name, t.device_type, t.province_code, t.management_code,
       t.station_code_part, t.pile_front, t.pile_back, t.manufacturer, t.sequence_no,
       t.tea_key, t.password, t.status, t.last_heartbeat,
       t.creator, t.create_time, t.updater, t.update_time, t.deleted, 162
FROM iot_keding_device t WHERE t.tenant_id=1 ORDER BY t.id;

DROP TABLE IF EXISTS _tmp_map_keding_device;
CREATE TABLE _tmp_map_keding_device (old_id BIGINT PRIMARY KEY, new_id BIGINT NOT NULL) ENGINE=InnoDB;
INSERT INTO _tmp_map_keding_device (old_id, new_id)
SELECT t1.id, t162.id FROM iot_keding_device t1
JOIN iot_keding_device t162 ON t162.station_code = CONCAT(t1.station_code, '-ibms')
WHERE t1.tenant_id=1 AND t162.tenant_id=162;

SELECT '[3] iot_keding_device' tag,
  (SELECT COUNT(*) FROM iot_keding_device WHERE tenant_id=1) t1,
  (SELECT COUNT(*) FROM iot_keding_device WHERE tenant_id=162) t162,
  (SELECT COUNT(*) FROM _tmp_map_keding_device) map;

-- ============================================================================
-- 4) iot_keding_firmware (t1=5): 无 FK
-- ============================================================================
INSERT INTO iot_keding_firmware
  (name, version, device_type, file_path, file_size, file_md5, description,
   creator, create_time, updater, update_time, deleted, tenant_id)
SELECT t.name, t.version, t.device_type, t.file_path, t.file_size, t.file_md5, t.description,
       t.creator, t.create_time, t.updater, t.update_time, t.deleted, 162
FROM iot_keding_firmware t WHERE t.tenant_id=1 ORDER BY t.id;

SELECT '[4] iot_keding_firmware' tag,
  (SELECT COUNT(*) FROM iot_keding_firmware WHERE tenant_id=1) t1,
  (SELECT COUNT(*) FROM iot_keding_firmware WHERE tenant_id=162) t162;

-- ============================================================================
-- 5) iot_ota_firmware (t1=2): product_id → product map; 用 ROW_NUMBER 配对建 map
-- ============================================================================
INSERT INTO iot_ota_firmware
  (name, description, version, product_id, file_url, file_size,
   file_digest_algorithm, file_digest_value,
   creator, create_time, updater, update_time, deleted, tenant_id)
SELECT t.name, t.description, t.version,
       COALESCE(pm.new_id, t.product_id),
       t.file_url, t.file_size, t.file_digest_algorithm, t.file_digest_value,
       t.creator, t.create_time, t.updater, t.update_time, t.deleted, 162
FROM iot_ota_firmware t
LEFT JOIN _tmp_map_product pm ON pm.old_id = t.product_id
WHERE t.tenant_id=1 ORDER BY t.id;

DROP TABLE IF EXISTS _tmp_map_ota_fw;
CREATE TABLE _tmp_map_ota_fw (old_id BIGINT PRIMARY KEY, new_id BIGINT NOT NULL) ENGINE=InnoDB;
INSERT INTO _tmp_map_ota_fw (old_id, new_id)
SELECT a.old_id, b.new_id FROM
  (SELECT id AS old_id, ROW_NUMBER() OVER (ORDER BY id) rn FROM iot_ota_firmware WHERE tenant_id=1) a
  JOIN
  (SELECT id AS new_id, ROW_NUMBER() OVER (ORDER BY id) rn FROM iot_ota_firmware WHERE tenant_id=162) b
  ON a.rn = b.rn;

SELECT '[5] iot_ota_firmware' tag,
  (SELECT COUNT(*) FROM iot_ota_firmware WHERE tenant_id=1) t1,
  (SELECT COUNT(*) FROM iot_ota_firmware WHERE tenant_id=162) t162,
  (SELECT COUNT(*) FROM _tmp_map_ota_fw) map;

-- ============================================================================
-- 6) iot_access_department (t1=28): self-FK parent_id; 先全部 INSERT, 再 UPDATE parent_id
-- ============================================================================
INSERT INTO iot_access_department
  (parent_id, dept_name, dept_code, sort, leader, phone, email, status,
   tenant_id, creator, create_time, updater, update_time, deleted)
SELECT t.parent_id, t.dept_name, t.dept_code, t.sort, t.leader, t.phone, t.email, t.status,
       162, t.creator, t.create_time, t.updater, t.update_time, t.deleted
FROM iot_access_department t WHERE t.tenant_id=1 ORDER BY t.id;

DROP TABLE IF EXISTS _tmp_map_dept;
CREATE TABLE _tmp_map_dept (old_id BIGINT PRIMARY KEY, new_id BIGINT NOT NULL) ENGINE=InnoDB;
INSERT INTO _tmp_map_dept (old_id, new_id)
SELECT a.old_id, b.new_id FROM
  (SELECT id AS old_id, ROW_NUMBER() OVER (ORDER BY id) rn FROM iot_access_department WHERE tenant_id=1) a
  JOIN
  (SELECT id AS new_id, ROW_NUMBER() OVER (ORDER BY id) rn FROM iot_access_department WHERE tenant_id=162) b
  ON a.rn = b.rn;

UPDATE iot_access_department d
JOIN _tmp_map_dept m ON m.old_id = d.parent_id
SET d.parent_id = m.new_id
WHERE d.tenant_id=162 AND d.parent_id <> 0;

SELECT '[6] iot_access_department' tag,
  (SELECT COUNT(*) FROM iot_access_department WHERE tenant_id=1) t1,
  (SELECT COUNT(*) FROM iot_access_department WHERE tenant_id=162) t162,
  (SELECT COUNT(*) FROM _tmp_map_dept) map,
  (SELECT COUNT(*) FROM iot_access_department WHERE tenant_id=162 AND parent_id<>0
     AND parent_id NOT IN (SELECT id FROM iot_access_department WHERE tenant_id=162)) orphan_parent;

-- ============================================================================
-- 7) iot_access_permission_group (t1=7): 无 FK
-- ============================================================================
INSERT INTO iot_access_permission_group
  (group_name, time_template_id, auth_mode, description, status,
   tenant_id, creator, create_time, updater, update_time, deleted)
SELECT t.group_name, t.time_template_id, t.auth_mode, t.description, t.status,
       162, t.creator, t.create_time, t.updater, t.update_time, t.deleted
FROM iot_access_permission_group t WHERE t.tenant_id=1 ORDER BY t.id;

DROP TABLE IF EXISTS _tmp_map_perm_grp;
CREATE TABLE _tmp_map_perm_grp (old_id BIGINT PRIMARY KEY, new_id BIGINT NOT NULL) ENGINE=InnoDB;
INSERT INTO _tmp_map_perm_grp (old_id, new_id)
SELECT a.old_id, b.new_id FROM
  (SELECT id AS old_id, ROW_NUMBER() OVER (ORDER BY id) rn FROM iot_access_permission_group WHERE tenant_id=1) a
  JOIN
  (SELECT id AS new_id, ROW_NUMBER() OVER (ORDER BY id) rn FROM iot_access_permission_group WHERE tenant_id=162) b
  ON a.rn = b.rn;

SELECT '[7] iot_access_permission_group' tag,
  (SELECT COUNT(*) FROM iot_access_permission_group WHERE tenant_id=1) t1,
  (SELECT COUNT(*) FROM iot_access_permission_group WHERE tenant_id=162) t162,
  (SELECT COUNT(*) FROM _tmp_map_perm_grp) map;

-- ============================================================================
-- 8) iot_parking_lot (t1=3): 无 FK
-- ============================================================================
INSERT INTO iot_parking_lot
  (lot_name, lot_code, lot_type, total_spaces, monthly_fee, free_duration,
   address, contact_person, contact_phone, free_exit_minutes, longitude, latitude,
   status, remark, creator, create_time, updater, update_time, deleted, tenant_id)
SELECT t.lot_name, t.lot_code, t.lot_type, t.total_spaces, t.monthly_fee, t.free_duration,
       t.address, t.contact_person, t.contact_phone, t.free_exit_minutes, t.longitude, t.latitude,
       t.status, t.remark, t.creator, t.create_time, t.updater, t.update_time, t.deleted, 162
FROM iot_parking_lot t WHERE t.tenant_id=1 ORDER BY t.id;

DROP TABLE IF EXISTS _tmp_map_lot;
CREATE TABLE _tmp_map_lot (old_id BIGINT PRIMARY KEY, new_id BIGINT NOT NULL) ENGINE=InnoDB;
INSERT INTO _tmp_map_lot (old_id, new_id)
SELECT a.old_id, b.new_id FROM
  (SELECT id AS old_id, ROW_NUMBER() OVER (ORDER BY id) rn FROM iot_parking_lot WHERE tenant_id=1) a
  JOIN
  (SELECT id AS new_id, ROW_NUMBER() OVER (ORDER BY id) rn FROM iot_parking_lot WHERE tenant_id=162) b
  ON a.rn = b.rn;

SELECT '[8] iot_parking_lot' tag,
  (SELECT COUNT(*) FROM iot_parking_lot WHERE tenant_id=1) t1,
  (SELECT COUNT(*) FROM iot_parking_lot WHERE tenant_id=162) t162,
  (SELECT COUNT(*) FROM _tmp_map_lot) map;

-- ============================================================================
-- 9) iot_parking_charge_rule (t1=3): 无 FK
-- ============================================================================
INSERT INTO iot_parking_charge_rule
  (rule_name, charge_mode, per_time_fee, free_minutes, first_hour_fee,
   extra_hour_fee, max_daily_fee, cycle_type, night_discount,
   night_start_time, night_end_time, rule_config, status, remark,
   creator, create_time, updater, update_time, deleted, tenant_id)
SELECT t.rule_name, t.charge_mode, t.per_time_fee, t.free_minutes, t.first_hour_fee,
       t.extra_hour_fee, t.max_daily_fee, t.cycle_type, t.night_discount,
       t.night_start_time, t.night_end_time, t.rule_config, t.status, t.remark,
       t.creator, t.create_time, t.updater, t.update_time, t.deleted, 162
FROM iot_parking_charge_rule t WHERE t.tenant_id=1 ORDER BY t.id;

DROP TABLE IF EXISTS _tmp_map_charge_rule;
CREATE TABLE _tmp_map_charge_rule (old_id BIGINT PRIMARY KEY, new_id BIGINT NOT NULL) ENGINE=InnoDB;
INSERT INTO _tmp_map_charge_rule (old_id, new_id)
SELECT a.old_id, b.new_id FROM
  (SELECT id AS old_id, ROW_NUMBER() OVER (ORDER BY id) rn FROM iot_parking_charge_rule WHERE tenant_id=1) a
  JOIN
  (SELECT id AS new_id, ROW_NUMBER() OVER (ORDER BY id) rn FROM iot_parking_charge_rule WHERE tenant_id=162) b
  ON a.rn = b.rn;

SELECT '[9] iot_parking_charge_rule' tag,
  (SELECT COUNT(*) FROM iot_parking_charge_rule WHERE tenant_id=1) t1,
  (SELECT COUNT(*) FROM iot_parking_charge_rule WHERE tenant_id=162) t162,
  (SELECT COUNT(*) FROM _tmp_map_charge_rule) map;

-- ============================================================================
-- 10) iot_visitor_appointment (t1=43): 无 FK (areas JSON 内为 location 标签字符串, 非 ID)
-- ============================================================================
INSERT INTO iot_visitor_appointment
  (name, phone, type, company, host, host_dept, visit_time, reason, areas,
   id_card, car_no, remark, status, approval_comment, approval_time, approver_id,
   sign_in_time, sign_out_time, current_location, rating,
   tenant_id, creator, create_time, updater, update_time, deleted)
SELECT t.name, t.phone, t.type, t.company, t.host, t.host_dept, t.visit_time, t.reason, t.areas,
       t.id_card, t.car_no, t.remark, t.status, t.approval_comment, t.approval_time, t.approver_id,
       t.sign_in_time, t.sign_out_time, t.current_location, t.rating,
       162, t.creator, t.create_time, t.updater, t.update_time, t.deleted
FROM iot_visitor_appointment t WHERE t.tenant_id=1 ORDER BY t.id;

DROP TABLE IF EXISTS _tmp_map_visitor;
CREATE TABLE _tmp_map_visitor (old_id BIGINT PRIMARY KEY, new_id BIGINT NOT NULL) ENGINE=InnoDB;
INSERT INTO _tmp_map_visitor (old_id, new_id)
SELECT a.old_id, b.new_id FROM
  (SELECT id AS old_id, ROW_NUMBER() OVER (ORDER BY id) rn FROM iot_visitor_appointment WHERE tenant_id=1) a
  JOIN
  (SELECT id AS new_id, ROW_NUMBER() OVER (ORDER BY id) rn FROM iot_visitor_appointment WHERE tenant_id=162) b
  ON a.rn = b.rn;

SELECT '[10] iot_visitor_appointment' tag,
  (SELECT COUNT(*) FROM iot_visitor_appointment WHERE tenant_id=1) t1,
  (SELECT COUNT(*) FROM iot_visitor_appointment WHERE tenant_id=162) t162,
  (SELECT COUNT(*) FROM _tmp_map_visitor) map;

-- ============================================================================
-- 11) iot_device_group (t1=2): 无 FK
-- ============================================================================
INSERT INTO iot_device_group
  (name, status, description, creator, create_time, updater, update_time, deleted, tenant_id)
SELECT t.name, t.status, t.description, t.creator, t.create_time, t.updater, t.update_time, t.deleted, 162
FROM iot_device_group t WHERE t.tenant_id=1 ORDER BY t.id;

SELECT '[11] iot_device_group' tag,
  (SELECT COUNT(*) FROM iot_device_group WHERE tenant_id=1) t1,
  (SELECT COUNT(*) FROM iot_device_group WHERE tenant_id=162) t162;

-- ============================================================================
-- 12) iot_device_display_config (t1=8): product_id → product map
-- ============================================================================
INSERT INTO iot_device_display_config
  (product_id, module_code, module_name, page_path, component_type, component_config,
   sort, status, creator, create_time, updater, update_time, deleted, tenant_id)
SELECT COALESCE(pm.new_id, t.product_id),
       t.module_code, t.module_name, t.page_path, t.component_type, t.component_config,
       t.sort, t.status, t.creator, t.create_time, t.updater, t.update_time, t.deleted, 162
FROM iot_device_display_config t
LEFT JOIN _tmp_map_product pm ON pm.old_id = t.product_id
WHERE t.tenant_id=1 ORDER BY t.id;

SELECT '[12] iot_device_display_config' tag,
  (SELECT COUNT(*) FROM iot_device_display_config WHERE tenant_id=1) t1,
  (SELECT COUNT(*) FROM iot_device_display_config WHERE tenant_id=162) t162;

SELECT '=== PART1 DONE ===' tag;
