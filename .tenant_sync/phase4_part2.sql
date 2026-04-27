-- ============================================================================
-- Phase 4 — 智慧通行 数据同步  PART 2
-- 范围(part2): 中层依赖表 (16 张)
--   iot_access_person, iot_parking_lane/gate/monthly_vehicle/monthly_recharge/
--   blacklist/free_vehicle/pass_rule/charge_rule_apply/system_config/wechat_user/
--   present_vehicle/record/refund_record,
--   iot_visitor_abnormal_event, iot_device_channel, iot_device_event_log
-- ============================================================================
USE ch_ibms;
SET SQL_SAFE_UPDATES = 0;

-- ============================================================================
-- 14) iot_access_person (t1=15): dept_id → dept map
-- ============================================================================
INSERT INTO iot_access_person
  (person_code, person_name, person_type, dept_id, id_card, phone, email,
   face_url, valid_start, valid_end, status, remark,
   tenant_id, creator, create_time, updater, update_time, deleted)
SELECT t.person_code, t.person_name, t.person_type,
       COALESCE(dm.new_id, t.dept_id),
       t.id_card, t.phone, t.email, t.face_url, t.valid_start, t.valid_end, t.status, t.remark,
       162, t.creator, t.create_time, t.updater, t.update_time, t.deleted
FROM iot_access_person t
LEFT JOIN _tmp_map_dept dm ON dm.old_id = t.dept_id
WHERE t.tenant_id=1 ORDER BY t.id;

DROP TABLE IF EXISTS _tmp_map_person;
CREATE TABLE _tmp_map_person (old_id BIGINT PRIMARY KEY, new_id BIGINT NOT NULL) ENGINE=InnoDB;
INSERT INTO _tmp_map_person (old_id, new_id)
SELECT a.old_id, b.new_id FROM
  (SELECT id AS old_id, ROW_NUMBER() OVER (ORDER BY id) rn FROM iot_access_person WHERE tenant_id=1) a
  JOIN
  (SELECT id AS new_id, ROW_NUMBER() OVER (ORDER BY id) rn FROM iot_access_person WHERE tenant_id=162) b
  ON a.rn = b.rn;

SELECT '[14] iot_access_person' tag,
  (SELECT COUNT(*) FROM iot_access_person WHERE tenant_id=1) t1,
  (SELECT COUNT(*) FROM iot_access_person WHERE tenant_id=162) t162,
  (SELECT COUNT(*) FROM _tmp_map_person) map;

-- ============================================================================
-- 15) iot_parking_lane (t1=5): lot_id, main/aux camera/screen device_ids → 对应 map
-- ============================================================================
INSERT INTO iot_parking_lane
  (lane_name, lane_code, lot_id, direction, main_camera_id, main_screen_id,
   aux_camera_id, aux_screen_id, status, remark,
   creator, create_time, updater, update_time, deleted, tenant_id)
SELECT t.lane_name, t.lane_code,
       COALESCE(lm.new_id, t.lot_id), t.direction,
       COALESCE(dm1.new_id, t.main_camera_id),
       COALESCE(dm2.new_id, t.main_screen_id),
       COALESCE(dm3.new_id, t.aux_camera_id),
       COALESCE(dm4.new_id, t.aux_screen_id),
       t.status, t.remark,
       t.creator, t.create_time, t.updater, t.update_time, t.deleted, 162
FROM iot_parking_lane t
LEFT JOIN _tmp_map_lot lm ON lm.old_id = t.lot_id
LEFT JOIN _tmp_map_device dm1 ON dm1.old_id = t.main_camera_id
LEFT JOIN _tmp_map_device dm2 ON dm2.old_id = t.main_screen_id
LEFT JOIN _tmp_map_device dm3 ON dm3.old_id = t.aux_camera_id
LEFT JOIN _tmp_map_device dm4 ON dm4.old_id = t.aux_screen_id
WHERE t.tenant_id=1 ORDER BY t.id;

DROP TABLE IF EXISTS _tmp_map_lane;
CREATE TABLE _tmp_map_lane (old_id BIGINT PRIMARY KEY, new_id BIGINT NOT NULL) ENGINE=InnoDB;
INSERT INTO _tmp_map_lane (old_id, new_id)
SELECT a.old_id, b.new_id FROM
  (SELECT id AS old_id, ROW_NUMBER() OVER (ORDER BY id) rn FROM iot_parking_lane WHERE tenant_id=1) a
  JOIN
  (SELECT id AS new_id, ROW_NUMBER() OVER (ORDER BY id) rn FROM iot_parking_lane WHERE tenant_id=162) b
  ON a.rn = b.rn;

SELECT '[15] iot_parking_lane' tag,
  (SELECT COUNT(*) FROM iot_parking_lane WHERE tenant_id=1) t1,
  (SELECT COUNT(*) FROM iot_parking_lane WHERE tenant_id=162) t162,
  (SELECT COUNT(*) FROM _tmp_map_lane) map;

-- ============================================================================
-- 16) iot_parking_gate (t1=4): lot_id, lane_id, device_id → 各 map
-- ============================================================================
INSERT INTO iot_parking_gate
  (gate_name, gate_code, lot_id, lane_id, device_id, ip_address, port,
   username, password, manufacturer, model, gate_type, direction,
   online_status, last_heartbeat, status, remark,
   creator, create_time, updater, update_time, deleted, tenant_id)
SELECT t.gate_name, t.gate_code,
       COALESCE(lm.new_id, t.lot_id),
       COALESCE(lam.new_id, t.lane_id),
       COALESCE(dm.new_id, t.device_id),
       t.ip_address, t.port, t.username, t.password, t.manufacturer, t.model,
       t.gate_type, t.direction, t.online_status, t.last_heartbeat, t.status, t.remark,
       t.creator, t.create_time, t.updater, t.update_time, t.deleted, 162
FROM iot_parking_gate t
LEFT JOIN _tmp_map_lot lm ON lm.old_id = t.lot_id
LEFT JOIN _tmp_map_lane lam ON lam.old_id = t.lane_id
LEFT JOIN _tmp_map_device dm ON dm.old_id = t.device_id
WHERE t.tenant_id=1 ORDER BY t.id;

DROP TABLE IF EXISTS _tmp_map_gate;
CREATE TABLE _tmp_map_gate (old_id BIGINT PRIMARY KEY, new_id BIGINT NOT NULL) ENGINE=InnoDB;
INSERT INTO _tmp_map_gate (old_id, new_id)
SELECT a.old_id, b.new_id FROM
  (SELECT id AS old_id, ROW_NUMBER() OVER (ORDER BY id) rn FROM iot_parking_gate WHERE tenant_id=1) a
  JOIN
  (SELECT id AS new_id, ROW_NUMBER() OVER (ORDER BY id) rn FROM iot_parking_gate WHERE tenant_id=162) b
  ON a.rn = b.rn;

SELECT '[16] iot_parking_gate' tag,
  (SELECT COUNT(*) FROM iot_parking_gate WHERE tenant_id=1) t1,
  (SELECT COUNT(*) FROM iot_parking_gate WHERE tenant_id=162) t162,
  (SELECT COUNT(*) FROM _tmp_map_gate) map;

-- ============================================================================
-- 17) iot_parking_monthly_vehicle (t1=8): lot_id → map
-- ============================================================================
INSERT INTO iot_parking_monthly_vehicle
  (plate_number, owner_name, owner_phone, vehicle_type, lot_id,
   valid_start, valid_end, monthly_fee, last_charge_time, last_charge_months,
   status, remark, creator, create_time, updater, update_time, deleted, tenant_id)
SELECT t.plate_number, t.owner_name, t.owner_phone, t.vehicle_type,
       COALESCE(lm.new_id, t.lot_id),
       t.valid_start, t.valid_end, t.monthly_fee, t.last_charge_time, t.last_charge_months,
       t.status, t.remark, t.creator, t.create_time, t.updater, t.update_time, t.deleted, 162
FROM iot_parking_monthly_vehicle t
LEFT JOIN _tmp_map_lot lm ON lm.old_id = t.lot_id
WHERE t.tenant_id=1 ORDER BY t.id;

DROP TABLE IF EXISTS _tmp_map_monthly_v;
CREATE TABLE _tmp_map_monthly_v (old_id BIGINT PRIMARY KEY, new_id BIGINT NOT NULL) ENGINE=InnoDB;
INSERT INTO _tmp_map_monthly_v (old_id, new_id)
SELECT a.old_id, b.new_id FROM
  (SELECT id AS old_id, ROW_NUMBER() OVER (ORDER BY id) rn FROM iot_parking_monthly_vehicle WHERE tenant_id=1) a
  JOIN
  (SELECT id AS new_id, ROW_NUMBER() OVER (ORDER BY id) rn FROM iot_parking_monthly_vehicle WHERE tenant_id=162) b
  ON a.rn = b.rn;

SELECT '[17] iot_parking_monthly_vehicle' tag,
  (SELECT COUNT(*) FROM iot_parking_monthly_vehicle WHERE tenant_id=1) t1,
  (SELECT COUNT(*) FROM iot_parking_monthly_vehicle WHERE tenant_id=162) t162,
  (SELECT COUNT(*) FROM _tmp_map_monthly_v) map;

-- ============================================================================
-- 18) iot_parking_monthly_recharge (t1=2): monthly_vehicle_id → map
-- ============================================================================
INSERT INTO iot_parking_monthly_recharge
  (monthly_vehicle_id, plate_number, owner_name, owner_phone, recharge_months,
   valid_start, valid_end, charge_amount, paid_amount, payment_method,
   payment_time, payment_status, operator, remark,
   creator, create_time, updater, update_time, deleted, tenant_id)
SELECT COALESCE(mvm.new_id, t.monthly_vehicle_id),
       t.plate_number, t.owner_name, t.owner_phone, t.recharge_months,
       t.valid_start, t.valid_end, t.charge_amount, t.paid_amount, t.payment_method,
       t.payment_time, t.payment_status, t.operator, t.remark,
       t.creator, t.create_time, t.updater, t.update_time, t.deleted, 162
FROM iot_parking_monthly_recharge t
LEFT JOIN _tmp_map_monthly_v mvm ON mvm.old_id = t.monthly_vehicle_id
WHERE t.tenant_id=1 ORDER BY t.id;

SELECT '[18] iot_parking_monthly_recharge' tag,
  (SELECT COUNT(*) FROM iot_parking_monthly_recharge WHERE tenant_id=1) t1,
  (SELECT COUNT(*) FROM iot_parking_monthly_recharge WHERE tenant_id=162) t162;

-- ============================================================================
-- 19) iot_parking_blacklist (t1=4): lot_id → map
-- ============================================================================
INSERT INTO iot_parking_blacklist
  (plate_number, reason, end_time, lot_id, status, remark,
   tenant_id, creator, create_time, updater, update_time, deleted)
SELECT t.plate_number, t.reason, t.end_time,
       COALESCE(lm.new_id, t.lot_id), t.status, t.remark,
       162, t.creator, t.create_time, t.updater, t.update_time, t.deleted
FROM iot_parking_blacklist t
LEFT JOIN _tmp_map_lot lm ON lm.old_id = t.lot_id
WHERE t.tenant_id=1 ORDER BY t.id;

SELECT '[19] iot_parking_blacklist' tag,
  (SELECT COUNT(*) FROM iot_parking_blacklist WHERE tenant_id=1) t1,
  (SELECT COUNT(*) FROM iot_parking_blacklist WHERE tenant_id=162) t162;

-- ============================================================================
-- 20) iot_parking_free_vehicle (t1=5): lot_ids JSON 数组 → 重写
-- ============================================================================
INSERT INTO iot_parking_free_vehicle
  (plate_number, owner_name, owner_phone, vehicle_type, special_type,
   valid_start, valid_end, lot_ids, status, remark,
   creator, create_time, updater, update_time, deleted, tenant_id)
SELECT t.plate_number, t.owner_name, t.owner_phone, t.vehicle_type, t.special_type,
       t.valid_start, t.valid_end,
       COALESCE(
         (SELECT JSON_ARRAYAGG(COALESCE(lm.new_id, jt.v))
          FROM JSON_TABLE(t.lot_ids, '$[*]' COLUMNS(v BIGINT PATH '$')) jt
          LEFT JOIN _tmp_map_lot lm ON lm.old_id = jt.v),
         t.lot_ids
       ),
       t.status, t.remark, t.creator, t.create_time, t.updater, t.update_time, t.deleted, 162
FROM iot_parking_free_vehicle t
WHERE t.tenant_id=1 ORDER BY t.id;

SELECT '[20] iot_parking_free_vehicle' tag,
  (SELECT COUNT(*) FROM iot_parking_free_vehicle WHERE tenant_id=1) t1,
  (SELECT COUNT(*) FROM iot_parking_free_vehicle WHERE tenant_id=162) t162;

-- ============================================================================
-- 21) iot_parking_pass_rule (t1=3): lot_ids, lane_ids JSON 数组 → 重写
-- ============================================================================
INSERT INTO iot_parking_pass_rule
  (rule_name, lot_ids, special_vehicle_types, vehicle_categories, charge_vehicle_types,
   entry_confirm_rule, exit_confirm_rule, lane_ids, priority, status, remark,
   creator, create_time, updater, update_time, deleted, tenant_id)
SELECT t.rule_name,
       COALESCE(
         (SELECT JSON_ARRAYAGG(COALESCE(lm.new_id, jt.v))
          FROM JSON_TABLE(t.lot_ids, '$[*]' COLUMNS(v BIGINT PATH '$')) jt
          LEFT JOIN _tmp_map_lot lm ON lm.old_id = jt.v),
         t.lot_ids),
       t.special_vehicle_types, t.vehicle_categories, t.charge_vehicle_types,
       t.entry_confirm_rule, t.exit_confirm_rule,
       COALESCE(
         (SELECT JSON_ARRAYAGG(COALESCE(lam.new_id, jt.v))
          FROM JSON_TABLE(t.lane_ids, '$[*]' COLUMNS(v BIGINT PATH '$')) jt
          LEFT JOIN _tmp_map_lane lam ON lam.old_id = jt.v),
         t.lane_ids),
       t.priority, t.status, t.remark,
       t.creator, t.create_time, t.updater, t.update_time, t.deleted, 162
FROM iot_parking_pass_rule t
WHERE t.tenant_id=1 ORDER BY t.id;

DROP TABLE IF EXISTS _tmp_map_pass_rule;
CREATE TABLE _tmp_map_pass_rule (old_id BIGINT PRIMARY KEY, new_id BIGINT NOT NULL) ENGINE=InnoDB;
INSERT INTO _tmp_map_pass_rule (old_id, new_id)
SELECT a.old_id, b.new_id FROM
  (SELECT id AS old_id, ROW_NUMBER() OVER (ORDER BY id) rn FROM iot_parking_pass_rule WHERE tenant_id=1) a
  JOIN
  (SELECT id AS new_id, ROW_NUMBER() OVER (ORDER BY id) rn FROM iot_parking_pass_rule WHERE tenant_id=162) b
  ON a.rn = b.rn;

SELECT '[21] iot_parking_pass_rule' tag,
  (SELECT COUNT(*) FROM iot_parking_pass_rule WHERE tenant_id=1) t1,
  (SELECT COUNT(*) FROM iot_parking_pass_rule WHERE tenant_id=162) t162,
  (SELECT COUNT(*) FROM _tmp_map_pass_rule) map;

-- ============================================================================
-- 22) iot_parking_charge_rule_apply (t1=2): lot_ids JSON, rule_id → charge_rule map
-- ============================================================================
INSERT INTO iot_parking_charge_rule_apply
  (apply_name, lot_ids, vehicle_category, charge_vehicle_types, rule_id,
   priority, enabled, status, remark,
   creator, create_time, updater, update_time, deleted, tenant_id)
SELECT t.apply_name,
       COALESCE(
         (SELECT JSON_ARRAYAGG(COALESCE(lm.new_id, jt.v))
          FROM JSON_TABLE(t.lot_ids, '$[*]' COLUMNS(v BIGINT PATH '$')) jt
          LEFT JOIN _tmp_map_lot lm ON lm.old_id = jt.v),
         t.lot_ids),
       t.vehicle_category, t.charge_vehicle_types,
       COALESCE(crm.new_id, t.rule_id),
       t.priority, t.enabled, t.status, t.remark,
       t.creator, t.create_time, t.updater, t.update_time, t.deleted, 162
FROM iot_parking_charge_rule_apply t
LEFT JOIN _tmp_map_charge_rule crm ON crm.old_id = t.rule_id
WHERE t.tenant_id=1 ORDER BY t.id;

SELECT '[22] iot_parking_charge_rule_apply' tag,
  (SELECT COUNT(*) FROM iot_parking_charge_rule_apply WHERE tenant_id=1) t1,
  (SELECT COUNT(*) FROM iot_parking_charge_rule_apply WHERE tenant_id=162) t162;

-- ============================================================================
-- 23) iot_parking_system_config (t1=2): lot_id → map
-- ============================================================================
INSERT INTO iot_parking_system_config
  (lot_id, parking_name, address, phone, total_spaces, business_hours,
   parking_type, remark, tenant_id, create_time, update_time, creator, updater, deleted)
SELECT COALESCE(lm.new_id, t.lot_id),
       t.parking_name, t.address, t.phone, t.total_spaces, t.business_hours,
       t.parking_type, t.remark, 162, t.create_time, t.update_time, t.creator, t.updater, t.deleted
FROM iot_parking_system_config t
LEFT JOIN _tmp_map_lot lm ON lm.old_id = t.lot_id
WHERE t.tenant_id=1 ORDER BY t.id;

SELECT '[23] iot_parking_system_config' tag,
  (SELECT COUNT(*) FROM iot_parking_system_config WHERE tenant_id=1) t1,
  (SELECT COUNT(*) FROM iot_parking_system_config WHERE tenant_id=162) t162;

-- ============================================================================
-- 24) iot_parking_wechat_user (t1=2): openid + username 加 -ibms 后缀
-- ============================================================================
INSERT INTO iot_parking_wechat_user
  (openid, unionid, session_key, username, nickname, avatar, mobile, status,
   last_login_time, last_login_ip,
   tenant_id, creator, create_time, updater, update_time, deleted)
SELECT CONCAT(t.openid, '-ibms'), t.unionid, t.session_key,
       CASE WHEN t.username IS NOT NULL THEN CONCAT(t.username, '-ibms') ELSE t.username END,
       t.nickname, t.avatar, t.mobile, t.status,
       t.last_login_time, t.last_login_ip,
       162, t.creator, t.create_time, t.updater, t.update_time, t.deleted
FROM iot_parking_wechat_user t WHERE t.tenant_id=1 ORDER BY t.id;

SELECT '[24] iot_parking_wechat_user' tag,
  (SELECT COUNT(*) FROM iot_parking_wechat_user WHERE tenant_id=1) t1,
  (SELECT COUNT(*) FROM iot_parking_wechat_user WHERE tenant_id=162) t162;

-- ============================================================================
-- 25) iot_parking_present_vehicle (t1=20): lot_id, entry_lane_id, entry_gate_id → map
-- ============================================================================
INSERT INTO iot_parking_present_vehicle
  (plate_number, vehicle_type, vehicle_category, lot_id, entry_lane_id, entry_gate_id,
   entry_time, entry_photo_url, entry_operator, parking_duration, long_term_flag,
   status, remark, creator, create_time, updater, update_time, deleted, tenant_id)
SELECT t.plate_number, t.vehicle_type, t.vehicle_category,
       COALESCE(lm.new_id, t.lot_id),
       COALESCE(lam.new_id, t.entry_lane_id),
       COALESCE(gm.new_id, t.entry_gate_id),
       t.entry_time, t.entry_photo_url, t.entry_operator, t.parking_duration, t.long_term_flag,
       t.status, t.remark, t.creator, t.create_time, t.updater, t.update_time, t.deleted, 162
FROM iot_parking_present_vehicle t
LEFT JOIN _tmp_map_lot lm ON lm.old_id = t.lot_id
LEFT JOIN _tmp_map_lane lam ON lam.old_id = t.entry_lane_id
LEFT JOIN _tmp_map_gate gm ON gm.old_id = t.entry_gate_id
WHERE t.tenant_id=1 ORDER BY t.id;

SELECT '[25] iot_parking_present_vehicle' tag,
  (SELECT COUNT(*) FROM iot_parking_present_vehicle WHERE tenant_id=1) t1,
  (SELECT COUNT(*) FROM iot_parking_present_vehicle WHERE tenant_id=162) t162;

-- ============================================================================
-- 26) iot_parking_record (t1=24): lot_id, lanes/gates, charge_rule_id, pass_rule_id → maps
-- ============================================================================
INSERT INTO iot_parking_record
  (plate_number, vehicle_type, vehicle_category, lot_id, entry_lane_id, entry_gate_id,
   entry_time, entry_photo_url, entry_operator, exit_lane_id, exit_gate_id,
   exit_time, exit_photo_url, exit_operator, parking_duration,
   charge_amount, paid_amount, payment_method, payment_time, payment_status,
   exit_type, charge_rule_id, pass_rule_id, record_status, remark,
   creator, create_time, updater, update_time, deleted, tenant_id)
SELECT t.plate_number, t.vehicle_type, t.vehicle_category,
       COALESCE(lm.new_id, t.lot_id),
       COALESCE(lam1.new_id, t.entry_lane_id),
       COALESCE(gm1.new_id, t.entry_gate_id),
       t.entry_time, t.entry_photo_url, t.entry_operator,
       COALESCE(lam2.new_id, t.exit_lane_id),
       COALESCE(gm2.new_id, t.exit_gate_id),
       t.exit_time, t.exit_photo_url, t.exit_operator, t.parking_duration,
       t.charge_amount, t.paid_amount, t.payment_method, t.payment_time, t.payment_status,
       t.exit_type,
       COALESCE(crm.new_id, t.charge_rule_id),
       COALESCE(prm.new_id, t.pass_rule_id),
       t.record_status, t.remark,
       t.creator, t.create_time, t.updater, t.update_time, t.deleted, 162
FROM iot_parking_record t
LEFT JOIN _tmp_map_lot lm ON lm.old_id = t.lot_id
LEFT JOIN _tmp_map_lane lam1 ON lam1.old_id = t.entry_lane_id
LEFT JOIN _tmp_map_lane lam2 ON lam2.old_id = t.exit_lane_id
LEFT JOIN _tmp_map_gate gm1 ON gm1.old_id = t.entry_gate_id
LEFT JOIN _tmp_map_gate gm2 ON gm2.old_id = t.exit_gate_id
LEFT JOIN _tmp_map_charge_rule crm ON crm.old_id = t.charge_rule_id
LEFT JOIN _tmp_map_pass_rule prm ON prm.old_id = t.pass_rule_id
WHERE t.tenant_id=1 ORDER BY t.id;

DROP TABLE IF EXISTS _tmp_map_park_record;
CREATE TABLE _tmp_map_park_record (old_id BIGINT PRIMARY KEY, new_id BIGINT NOT NULL) ENGINE=InnoDB;
INSERT INTO _tmp_map_park_record (old_id, new_id)
SELECT a.old_id, b.new_id FROM
  (SELECT id AS old_id, ROW_NUMBER() OVER (ORDER BY id) rn FROM iot_parking_record WHERE tenant_id=1) a
  JOIN
  (SELECT id AS new_id, ROW_NUMBER() OVER (ORDER BY id) rn FROM iot_parking_record WHERE tenant_id=162) b
  ON a.rn = b.rn;

SELECT '[26] iot_parking_record' tag,
  (SELECT COUNT(*) FROM iot_parking_record WHERE tenant_id=1) t1,
  (SELECT COUNT(*) FROM iot_parking_record WHERE tenant_id=162) t162,
  (SELECT COUNT(*) FROM _tmp_map_park_record) map;

-- ============================================================================
-- 27) iot_parking_refund_record (t1=3): record_id → park_record map
-- ============================================================================
INSERT INTO iot_parking_refund_record
  (record_id, plate_number, out_trade_no, transaction_id, out_refund_no, refund_id,
   total_fee, refund_fee, refund_reason, refund_status, apply_time, refund_time,
   apply_user, audit_user, fail_reason, remark,
   creator, create_time, updater, update_time, deleted, tenant_id)
SELECT COALESCE(prm.new_id, t.record_id),
       t.plate_number, t.out_trade_no, t.transaction_id, t.out_refund_no, t.refund_id,
       t.total_fee, t.refund_fee, t.refund_reason, t.refund_status, t.apply_time, t.refund_time,
       t.apply_user, t.audit_user, t.fail_reason, t.remark,
       t.creator, t.create_time, t.updater, t.update_time, t.deleted, 162
FROM iot_parking_refund_record t
LEFT JOIN _tmp_map_park_record prm ON prm.old_id = t.record_id
WHERE t.tenant_id=1 ORDER BY t.id;

SELECT '[27] iot_parking_refund_record' tag,
  (SELECT COUNT(*) FROM iot_parking_refund_record WHERE tenant_id=1) t1,
  (SELECT COUNT(*) FROM iot_parking_refund_record WHERE tenant_id=162) t162;

-- ============================================================================
-- 28) iot_visitor_abnormal_event (t1=11): appointment_id → visitor map
-- ============================================================================
INSERT INTO iot_visitor_abnormal_event
  (appointment_id, visitor_name, visitor_phone, abnormal_type, risk_level,
   details, event_time, current_status, handled, handler_id, handle_time, handle_result,
   tenant_id, creator, create_time, updater, update_time, deleted)
SELECT COALESCE(vm.new_id, t.appointment_id),
       t.visitor_name, t.visitor_phone, t.abnormal_type, t.risk_level,
       t.details, t.event_time, t.current_status, t.handled, t.handler_id, t.handle_time, t.handle_result,
       162, t.creator, t.create_time, t.updater, t.update_time, t.deleted
FROM iot_visitor_abnormal_event t
LEFT JOIN _tmp_map_visitor vm ON vm.old_id = t.appointment_id
WHERE t.tenant_id=1 ORDER BY t.id;

SELECT '[28] iot_visitor_abnormal_event' tag,
  (SELECT COUNT(*) FROM iot_visitor_abnormal_event WHERE tenant_id=1) t1,
  (SELECT COUNT(*) FROM iot_visitor_abnormal_event WHERE tenant_id=162) t162;

-- ============================================================================
-- 29) iot_device_channel (t1=2): device_id 多义保留 (900001 不在 ibms_device 也不在 keding_device)
--     product/building/floor/area/space 全为 NULL, 跳过 map
-- ============================================================================
INSERT INTO iot_device_channel
  (device_id, device_type, product_id, channel_no, channel_name, channel_code,
   channel_type, channel_sub_type, location, building_id, floor_id, area_id, space_id,
   target_device_id, target_ip, target_port, target_channel_no, protocol,
   username, password, stream_url_main, stream_url_sub, snapshot_url,
   ptz_support, audio_support, resolution, frame_rate, bit_rate,
   door_name, door_direction, card_reader_type, lock_type, detector_type, alarm_level,
   meter_type, circuit_name, measurement_unit, capabilities,
   online_status, enable_status, alarm_status, last_online_time, last_sync_time,
   is_recording, is_patrol, is_monitor, patrol_duration, monitor_position, config,
   description, sort, tags, creator, create_time, updater, update_time, deleted, tenant_id)
SELECT COALESCE(dm.new_id, t.device_id), t.device_type,
       COALESCE(pm.new_id, t.product_id),
       t.channel_no, t.channel_name, t.channel_code,
       t.channel_type, t.channel_sub_type, t.location,
       t.building_id, t.floor_id, t.area_id,
       COALESCE(sm.new_id, t.space_id),
       COALESCE(tdm.new_id, t.target_device_id),
       t.target_ip, t.target_port, t.target_channel_no, t.protocol,
       t.username, t.password, t.stream_url_main, t.stream_url_sub, t.snapshot_url,
       t.ptz_support, t.audio_support, t.resolution, t.frame_rate, t.bit_rate,
       t.door_name, t.door_direction, t.card_reader_type, t.lock_type, t.detector_type, t.alarm_level,
       t.meter_type, t.circuit_name, t.measurement_unit, t.capabilities,
       t.online_status, t.enable_status, t.alarm_status, t.last_online_time, t.last_sync_time,
       t.is_recording, t.is_patrol, t.is_monitor, t.patrol_duration, t.monitor_position, t.config,
       t.description, t.sort, t.tags, t.creator, t.create_time, t.updater, t.update_time, t.deleted, 162
FROM iot_device_channel t
LEFT JOIN _tmp_map_device dm ON dm.old_id = t.device_id
LEFT JOIN _tmp_map_product pm ON pm.old_id = t.product_id
LEFT JOIN _tmp_map_space sm ON sm.old_id = t.space_id
LEFT JOIN _tmp_map_device tdm ON tdm.old_id = t.target_device_id
WHERE t.tenant_id=1 ORDER BY t.id;

SELECT '[29] iot_device_channel' tag,
  (SELECT COUNT(*) FROM iot_device_channel WHERE tenant_id=1) t1,
  (SELECT COUNT(*) FROM iot_device_channel WHERE tenant_id=162) t162;

-- ============================================================================
-- 30) iot_device_event_log (t1=979): device_id (混合 ibms/keding/未知), product_id 多 NULL
-- ============================================================================
INSERT INTO iot_device_event_log
  (device_id, tenant_id, product_id, product_key, device_name,
   event_identifier, event_name, event_type, event_data, event_time,
   onvif_topic, processed, triggered_scene_rule_ids, generated_alert_record_ids,
   creator, create_time, updater, update_time, deleted)
SELECT COALESCE(dm.new_id, kdm.new_id, t.device_id),
       162,
       COALESCE(pm.new_id, t.product_id),
       t.product_key, t.device_name,
       t.event_identifier, t.event_name, t.event_type, t.event_data, t.event_time,
       t.onvif_topic, t.processed, t.triggered_scene_rule_ids, t.generated_alert_record_ids,
       t.creator, t.create_time, t.updater, t.update_time, t.deleted
FROM iot_device_event_log t
LEFT JOIN _tmp_map_device dm ON dm.old_id = t.device_id
LEFT JOIN _tmp_map_keding_device kdm ON kdm.old_id = t.device_id
LEFT JOIN _tmp_map_product pm ON pm.old_id = t.product_id
WHERE t.tenant_id=1 ORDER BY t.id;

SELECT '[30] iot_device_event_log' tag,
  (SELECT COUNT(*) FROM iot_device_event_log WHERE tenant_id=1) t1,
  (SELECT COUNT(*) FROM iot_device_event_log WHERE tenant_id=162) t162;

SELECT '=== PART2 DONE ===' tag;
