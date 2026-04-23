-- 边界测试数据（停车 + 门禁）
-- 目标：更贴合页面筛选条件；幂等（可重复执行不会重复插入）
-- 说明：
-- - 默认 tenant_id = 1
-- - 停车默认 lot_id = 2（上一批 seed 的测试车场）
-- - 门禁默认 device_id = 114（总裁办公室门禁）
-- - 若你环境 lot_id/device_id 不同，可先用 SELECT 找到对应 id 后替换
--
-- ⚠️ 若通过 MCP 执行：请按“单条 SQL”逐条执行（不要多语句一起提交）

-- =========================================================
-- 0) 快速自检（可选）
-- =========================================================
-- SELECT id, lot_code, lot_name FROM iot_parking_lot WHERE tenant_id=1 AND deleted=b'0' ORDER BY id DESC;
-- SELECT id, name, subsystem_code FROM ibms_device WHERE deleted=b'0' AND id IN (114) OR name LIKE '%门禁%';

-- =========================================================
-- A) 停车：月卡过期/停用 + 续费记录
-- =========================================================
-- A1. 过期边界：status=0（正常）但 valid_end 已过期（用于测试“过期按临停”/异常状态）
INSERT INTO iot_parking_monthly_vehicle
 (plate_number, owner_name, owner_phone, vehicle_type, lot_id,
  valid_start, valid_end, monthly_fee, last_charge_time, last_charge_months,
  status, remark, tenant_id, creator, create_time, updater, update_time, deleted)
SELECT
  '测M00001', '月卡-已过期但状态正常', '13900000001', 1, 2,
  DATE_SUB(NOW(), INTERVAL 45 DAY),
  DATE_SUB(NOW(), INTERVAL 1 DAY),
  300.00, DATE_SUB(NOW(), INTERVAL 32 DAY), 1,
  0, 'seed:edge_v1 monthly expired but status=0', 1, 'seed', NOW(), 'seed', NOW(), b'0'
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM iot_parking_monthly_vehicle
  WHERE tenant_id=1 AND deleted=b'0' AND plate_number='测M00001'
);

-- A2. 停用边界：status=1（停用），valid_end 仍有效
INSERT INTO iot_parking_monthly_vehicle
 (plate_number, owner_name, owner_phone, vehicle_type, lot_id,
  valid_start, valid_end, monthly_fee, last_charge_time, last_charge_months,
  status, remark, tenant_id, creator, create_time, updater, update_time, deleted)
SELECT
  '测M00002', '月卡-停用', '13900000002', 3, 2,
  DATE_SUB(NOW(), INTERVAL 10 DAY),
  DATE_ADD(NOW(), INTERVAL 20 DAY),
  380.00, DATE_SUB(NOW(), INTERVAL 10 DAY), 1,
  1, 'seed:edge_v1 monthly disabled', 1, 'seed', NOW(), 'seed', NOW(), b'0'
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM iot_parking_monthly_vehicle
  WHERE tenant_id=1 AND deleted=b'0' AND plate_number='测M00002'
);

-- A3. 月卡续费记录：给 测M00002 补一条已支付续费记录（便于在“充值记录”页筛选）
INSERT INTO iot_parking_monthly_recharge
 (monthly_vehicle_id, plate_number, owner_name, owner_phone,
  recharge_months, valid_start, valid_end,
  charge_amount, paid_amount, payment_method, payment_time, payment_status,
  operator, remark, tenant_id, creator, create_time, updater, update_time, deleted)
SELECT
  mv.id, mv.plate_number, mv.owner_name, mv.owner_phone,
  1, mv.valid_start, mv.valid_end,
  mv.monthly_fee, mv.monthly_fee, 'wechat', DATE_SUB(NOW(), INTERVAL 10 DAY), 1,
  'seed', 'seed:edge_v1 monthly recharge record', 1, 'seed', NOW(), 'seed', NOW(), b'0'
FROM iot_parking_monthly_vehicle mv
WHERE mv.tenant_id=1 AND mv.deleted=b'0' AND mv.plate_number='测M00002'
  AND NOT EXISTS (
    SELECT 1 FROM iot_parking_monthly_recharge r
    WHERE r.tenant_id=1 AND r.deleted=b'0' AND r.plate_number='测M00002'
      AND r.remark='seed:edge_v1 monthly recharge record'
  );

-- =========================================================
-- B) 停车：免费车过期（valid_end 过期但仍 status=0）
-- =========================================================
INSERT INTO iot_parking_free_vehicle
 (plate_number, owner_name, owner_phone, vehicle_type, special_type,
  valid_start, valid_end, lot_ids, status, remark,
  tenant_id, creator, create_time, updater, update_time, deleted)
SELECT
  '测F00001', '免费车-已过期', '13900000003', 1, '警车',
  DATE_SUB(NOW(), INTERVAL 20 DAY),
  DATE_SUB(NOW(), INTERVAL 2 DAY),
  JSON_ARRAY(2), 0, 'seed:edge_v1 free expired but status=0',
  1, 'seed', NOW(), 'seed', NOW(), b'0'
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM iot_parking_free_vehicle
  WHERE tenant_id=1 AND deleted=b'0' AND plate_number='测F00001'
);

-- =========================================================
-- C) 停车：黑名单（生效中/已解除）
-- =========================================================
-- C1. 生效中：lot_id=2 且 end_time 在未来
INSERT INTO iot_parking_blacklist
 (plate_number, reason, end_time, lot_id, status, remark,
  tenant_id, creator, create_time, updater, update_time, deleted)
SELECT
  '测BL0001', '恶意逃费', DATE_ADD(NOW(), INTERVAL 30 DAY), 2, 0,
  'seed:edge_v1 blacklist active',
  1, 'seed', NOW(), 'seed', NOW(), b'0'
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM iot_parking_blacklist
  WHERE tenant_id=1 AND deleted=b'0' AND plate_number='测BL0001'
);

-- C2. 已解除：status=1
INSERT INTO iot_parking_blacklist
 (plate_number, reason, end_time, lot_id, status, remark,
  tenant_id, creator, create_time, updater, update_time, deleted)
SELECT
  '测BL0002', '测试用-已解除', DATE_ADD(NOW(), INTERVAL 10 DAY), 2, 1,
  'seed:edge_v1 blacklist released',
  1, 'seed', NOW(), 'seed', NOW(), b'0'
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM iot_parking_blacklist
  WHERE tenant_id=1 AND deleted=b'0' AND plate_number='测BL0002'
);

-- =========================================================
-- D) 停车：长停在场车辆（longTermFlag=2）+ 未支付在场记录
-- =========================================================
-- D1. 长停在场：入场时间 100 天前，longTermFlag=2
INSERT INTO iot_parking_present_vehicle
 (plate_number, vehicle_type, vehicle_category, lot_id,
  entry_lane_id, entry_gate_id, entry_time, entry_photo_url, entry_operator,
  parking_duration, long_term_flag, status, remark,
  tenant_id, creator, create_time, updater, update_time, deleted)
SELECT
  '测LT0001', 1, 'temporary', 2,
  (SELECT id FROM iot_parking_lane WHERE tenant_id=1 AND deleted=b'0' AND lot_id=2 ORDER BY id ASC LIMIT 1),
  (SELECT id FROM iot_parking_gate WHERE tenant_id=1 AND deleted=b'0' AND lot_id=2 ORDER BY id ASC LIMIT 1),
  DATE_SUB(NOW(), INTERVAL 100 DAY),
  'https://example.com/parking/entry/lt0001.jpg',
  'seed',
  100*24*60, 2, 0, 'seed:edge_v1 present longTermFlag=2',
  1, 'seed', NOW(), 'seed', NOW(), b'0'
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM iot_parking_present_vehicle
  WHERE tenant_id=1 AND deleted=b'0' AND plate_number='测LT0001' AND lot_id=2
);

-- D2. 在场未支付记录：使用已存在在场车辆（上一批 seed 的 测C34567），补一条 record_status=1, payment_status=0 的记录
INSERT INTO iot_parking_record
 (plate_number, vehicle_type, vehicle_category, lot_id,
  entry_lane_id, entry_gate_id, entry_time, entry_photo_url, entry_operator,
  parking_duration, charge_amount, paid_amount, payment_method, payment_time, payment_status,
  exit_type, charge_rule_id, pass_rule_id, record_status, remark,
  tenant_id, creator, create_time, updater, update_time, deleted)
SELECT
  pv.plate_number, pv.vehicle_type, pv.vehicle_category, pv.lot_id,
  pv.entry_lane_id, pv.entry_gate_id, pv.entry_time, pv.entry_photo_url, pv.entry_operator,
  pv.parking_duration,
  12.00, 0.00, NULL, NULL, 0,
  'normal',
  (SELECT id FROM iot_parking_charge_rule WHERE tenant_id=1 AND deleted=b'0' AND lot_id=2 ORDER BY id ASC LIMIT 1),
  (SELECT id FROM iot_parking_pass_rule WHERE tenant_id=1 AND deleted=b'0' AND lot_id=2 ORDER BY id ASC LIMIT 1),
  1, 'seed:edge_v1 unpaid in-lot record for existing present',
  1, 'seed', NOW(), 'seed', NOW(), b'0'
FROM iot_parking_present_vehicle pv
WHERE pv.tenant_id=1 AND pv.deleted=b'0' AND pv.lot_id=2 AND pv.plate_number='测C34567'
  AND NOT EXISTS (
    SELECT 1 FROM iot_parking_record r
    WHERE r.tenant_id=1 AND r.deleted=b'0' AND r.lot_id=2 AND r.plate_number='测C34567'
      AND r.record_status=1 AND r.payment_status=0
      AND r.remark='seed:edge_v1 unpaid in-lot record for existing present'
  );

-- D3. 新增一个“在场未支付”的临时车：同时插 present_vehicle + record（便于在页内筛选）
INSERT INTO iot_parking_present_vehicle
 (plate_number, vehicle_type, vehicle_category, lot_id,
  entry_lane_id, entry_gate_id, entry_time, entry_photo_url, entry_operator,
  parking_duration, long_term_flag, status, remark,
  tenant_id, creator, create_time, updater, update_time, deleted)
SELECT
  '测U00001', 2, 'temporary', 2,
  (SELECT id FROM iot_parking_lane WHERE tenant_id=1 AND deleted=b'0' AND lot_id=2 ORDER BY id DESC LIMIT 1),
  (SELECT id FROM iot_parking_gate WHERE tenant_id=1 AND deleted=b'0' AND lot_id=2 ORDER BY id DESC LIMIT 1),
  DATE_SUB(NOW(), INTERVAL 3 HOUR),
  'https://example.com/parking/entry/u00001.jpg',
  'seed',
  180, 0, 0, 'seed:edge_v1 present unpaid temp',
  1, 'seed', NOW(), 'seed', NOW(), b'0'
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM iot_parking_present_vehicle
  WHERE tenant_id=1 AND deleted=b'0' AND plate_number='测U00001' AND lot_id=2
);

INSERT INTO iot_parking_record
 (plate_number, vehicle_type, vehicle_category, lot_id,
  entry_lane_id, entry_gate_id, entry_time, entry_photo_url, entry_operator,
  parking_duration, charge_amount, paid_amount, payment_method, payment_time, payment_status,
  exit_type, charge_rule_id, pass_rule_id, record_status, remark,
  tenant_id, creator, create_time, updater, update_time, deleted)
SELECT
  pv.plate_number, pv.vehicle_type, pv.vehicle_category, pv.lot_id,
  pv.entry_lane_id, pv.entry_gate_id, pv.entry_time, pv.entry_photo_url, pv.entry_operator,
  pv.parking_duration,
  8.00, 0.00, NULL, NULL, 0,
  'normal',
  (SELECT id FROM iot_parking_charge_rule WHERE tenant_id=1 AND deleted=b'0' AND lot_id=2 ORDER BY id ASC LIMIT 1),
  (SELECT id FROM iot_parking_pass_rule WHERE tenant_id=1 AND deleted=b'0' AND lot_id=2 ORDER BY id ASC LIMIT 1),
  1, 'seed:edge_v1 unpaid in-lot record new present',
  1, 'seed', NOW(), 'seed', NOW(), b'0'
FROM iot_parking_present_vehicle pv
WHERE pv.tenant_id=1 AND pv.deleted=b'0' AND pv.lot_id=2 AND pv.plate_number='测U00001'
  AND NOT EXISTS (
    SELECT 1 FROM iot_parking_record r
    WHERE r.tenant_id=1 AND r.deleted=b'0' AND r.lot_id=2 AND r.plate_number='测U00001'
      AND r.record_status=1 AND r.payment_status=0
      AND r.remark='seed:edge_v1 unpaid in-lot record new present'
  );

-- =========================================================
-- E) 门禁：报警/异常/正常事件 + 进出方向（方向字段在表里可能不存在时可忽略）
-- 说明：事件类型需与后端枚举/筛选一致（NORMAL/ALARM/ABNORMAL）
-- =========================================================
-- E1. 正常：刷卡成功
INSERT INTO iot_access_event_log
 (device_id, channel_id, event_type, event_time,
  person_id, person_name, card_no, verify_mode, verify_result, fail_reason,
  snapshot_url, temperature, mask_status,
  tenant_id, creator, create_time, updater, update_time, deleted)
SELECT
  114, NULL, 'CARD_SWIPE', DATE_SUB(NOW(), INTERVAL 20 MINUTE),
  (SELECT id FROM iot_access_person WHERE tenant_id=1 AND deleted=b'0' AND person_code='TST_EMP001' LIMIT 1),
  '测试员工-001', 'CARD_TST_0001', 'CARD', 1, NULL,
  'https://example.com/access/capture/card_ok.jpg', 36.6, 1,
  1, 'seed', NOW(), 'seed', NOW(), b'0'
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM iot_access_event_log
  WHERE tenant_id=1 AND deleted=b'0' AND device_id=114 AND event_type='CARD_SWIPE'
    AND snapshot_url='https://example.com/access/capture/card_ok.jpg'
);

-- E2. 异常：VERIFY_FAILED（验证失败）
INSERT INTO iot_access_event_log
 (device_id, channel_id, event_type, event_time,
  person_id, person_name, card_no, verify_mode, verify_result, fail_reason,
  snapshot_url, temperature, mask_status,
  tenant_id, creator, create_time, updater, update_time, deleted)
SELECT
  114, NULL, 'VERIFY_FAILED', DATE_SUB(NOW(), INTERVAL 15 MINUTE),
  NULL, NULL, NULL, 'FACE', 0, '人脸不匹配',
  'https://example.com/access/capture/face_fail.jpg', 36.8, 0,
  1, 'seed', NOW(), 'seed', NOW(), b'0'
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM iot_access_event_log
  WHERE tenant_id=1 AND deleted=b'0' AND device_id=114 AND event_type='VERIFY_FAILED'
    AND snapshot_url='https://example.com/access/capture/face_fail.jpg'
);

-- E3. 报警：FORCED_OPEN（强行开门）
INSERT INTO iot_access_event_log
 (device_id, channel_id, event_type, event_time,
  person_id, person_name, card_no, verify_mode, verify_result, fail_reason,
  snapshot_url, temperature, mask_status,
  tenant_id, creator, create_time, updater, update_time, deleted)
SELECT
  114, NULL, 'FORCED_OPEN', DATE_SUB(NOW(), INTERVAL 5 MINUTE),
  NULL, NULL, NULL, NULL, 0, '门磁检测到强行开门',
  'https://example.com/access/capture/forced_open.jpg', NULL, NULL,
  1, 'seed', NOW(), 'seed', NOW(), b'0'
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM iot_access_event_log
  WHERE tenant_id=1 AND deleted=b'0' AND device_id=114 AND event_type='FORCED_OPEN'
    AND snapshot_url='https://example.com/access/capture/forced_open.jpg'
);

-- =========================================================
-- F) 门禁：门控操作日志（open/close/always_open/always_closed/cancel_always）
-- =========================================================
INSERT INTO iot_access_operation_log
 (device_id, channel_id, operation_type, operation_time,
  operator_id, operator_name, result, error_message, request_params,
  tenant_id, creator, create_time, updater, update_time, deleted)
SELECT
  114, NULL, 'open_door', DATE_SUB(NOW(), INTERVAL 25 MINUTE),
  1, '管理员', 1, NULL, JSON_OBJECT('source','seed','reason','demo'),
  1, 'seed', NOW(), 'seed', NOW(), b'0'
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM iot_access_operation_log
  WHERE tenant_id=1 AND deleted=b'0' AND device_id=114 AND operation_type='open_door'
    AND operator_name='管理员' AND error_message IS NULL
    AND JSON_EXTRACT(request_params,'$.source')='seed'
);

INSERT INTO iot_access_operation_log
 (device_id, channel_id, operation_type, operation_time,
  operator_id, operator_name, result, error_message, request_params,
  tenant_id, creator, create_time, updater, update_time, deleted)
SELECT
  114, NULL, 'always_open', DATE_SUB(NOW(), INTERVAL 22 MINUTE),
  1, '管理员', 1, NULL, JSON_OBJECT('source','seed','durationMinutes',60),
  1, 'seed', NOW(), 'seed', NOW(), b'0'
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM iot_access_operation_log
  WHERE tenant_id=1 AND deleted=b'0' AND device_id=114 AND operation_type='always_open'
    AND JSON_EXTRACT(request_params,'$.source')='seed'
);

INSERT INTO iot_access_operation_log
 (device_id, channel_id, operation_type, operation_time,
  operator_id, operator_name, result, error_message, request_params,
  tenant_id, creator, create_time, updater, update_time, deleted)
SELECT
  114, NULL, 'always_closed', DATE_SUB(NOW(), INTERVAL 18 MINUTE),
  1, '管理员', 0, '设备离线', JSON_OBJECT('source','seed','note','simulate fail'),
  1, 'seed', NOW(), 'seed', NOW(), b'0'
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM iot_access_operation_log
  WHERE tenant_id=1 AND deleted=b'0' AND device_id=114 AND operation_type='always_closed'
    AND error_message='设备离线'
);

INSERT INTO iot_access_operation_log
 (device_id, channel_id, operation_type, operation_time,
  operator_id, operator_name, result, error_message, request_params,
  tenant_id, creator, create_time, updater, update_time, deleted)
SELECT
  114, NULL, 'cancel_always', DATE_SUB(NOW(), INTERVAL 12 MINUTE),
  1, '管理员', 1, NULL, JSON_OBJECT('source','seed'),
  1, 'seed', NOW(), 'seed', NOW(), b'0'
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM iot_access_operation_log
  WHERE tenant_id=1 AND deleted=b'0' AND device_id=114 AND operation_type='cancel_always'
    AND JSON_EXTRACT(request_params,'$.source')='seed'
);

INSERT INTO iot_access_operation_log
 (device_id, channel_id, operation_type, operation_time,
  operator_id, operator_name, result, error_message, request_params,
  tenant_id, creator, create_time, updater, update_time, deleted)
SELECT
  114, NULL, 'close_door', DATE_SUB(NOW(), INTERVAL 8 MINUTE),
  1, '管理员', 1, NULL, JSON_OBJECT('source','seed'),
  1, 'seed', NOW(), 'seed', NOW(), b'0'
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM iot_access_operation_log
  WHERE tenant_id=1 AND deleted=b'0' AND device_id=114 AND operation_type='close_door'
    AND JSON_EXTRACT(request_params,'$.source')='seed'
);

-- =========================================================
-- G) 验证查询（可选）
-- =========================================================
-- SELECT plate_number, status, valid_end, remark FROM iot_parking_monthly_vehicle WHERE tenant_id=1 AND deleted=b'0' AND plate_number IN ('测M00001','测M00002');
-- SELECT plate_number, status, valid_end, remark FROM iot_parking_free_vehicle WHERE tenant_id=1 AND deleted=b'0' AND plate_number='测F00001';
-- SELECT plate_number, status, end_time, lot_id, remark FROM iot_parking_blacklist WHERE tenant_id=1 AND deleted=b'0' AND plate_number IN ('测BL0001','测BL0002');
-- SELECT plate_number, long_term_flag, entry_time, remark FROM iot_parking_present_vehicle WHERE tenant_id=1 AND deleted=b'0' AND plate_number IN ('测LT0001','测U00001');
-- SELECT plate_number, record_status, payment_status, remark FROM iot_parking_record WHERE tenant_id=1 AND deleted=b'0' AND plate_number IN ('测C34567','测U00001') ORDER BY id DESC;
-- SELECT device_id, event_type, verify_result, snapshot_url, event_time FROM iot_access_event_log WHERE tenant_id=1 AND deleted=b'0' AND device_id=114 ORDER BY id DESC LIMIT 20;
-- SELECT device_id, operation_type, result, error_message, operation_time FROM iot_access_operation_log WHERE tenant_id=1 AND deleted=b'0' AND device_id=114 ORDER BY id DESC LIMIT 20;

