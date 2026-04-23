-- =========================================================
-- IoT 停车场管理：可信测试数据（幂等）
-- 适用：yudao-module-iot 停车场管理相关表（iot_parking_*）
-- 说明：
-- - 默认 tenant_id = 1（如需其它租户，修改 @tenant_id）
-- - 通过“业务编码/车牌号”做 NOT EXISTS 幂等判断
-- - 使用 MySQL 变量串起外键（lotId/laneId/gateId/ruleId）
-- =========================================================
SET NAMES utf8mb4;

SET @tenant_id := 1;
SET @creator := 'seed';

-- ----------------------------
-- 1) 车场（iot_parking_lot）
-- ----------------------------
INSERT INTO iot_parking_lot
  (lot_name, lot_code, lot_type, total_spaces, monthly_fee, address, contact_person, contact_phone,
   free_exit_minutes, longitude, latitude, status, remark, tenant_id, creator, create_time, updater, update_time, deleted)
SELECT
  '示例停车场A', 'LOT_A', 1, 200, 300.00, '示例市示例区示例路 88 号', '张三', '13800000001',
  15, 116.397128, 39.916527, 0, '联调用测试车场（A）', @tenant_id, @creator, NOW(), @creator, NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1 FROM iot_parking_lot
   WHERE lot_code = 'LOT_A' AND tenant_id = @tenant_id AND deleted = b'0'
)
LIMIT 1;

SET @lotA_id := (
  SELECT id FROM iot_parking_lot
   WHERE lot_code = 'LOT_A' AND tenant_id = @tenant_id AND deleted = b'0'
   ORDER BY id DESC LIMIT 1
);

-- ----------------------------
-- 2) 车道（iot_parking_lane）
-- ----------------------------
INSERT INTO iot_parking_lane
  (lane_name, lane_code, lot_id, direction, main_camera_id, main_screen_id, aux_camera_id, aux_screen_id,
   status, remark, tenant_id, creator, create_time, updater, update_time, deleted)
SELECT
  'A入口车道', 'LANE_A_IN', @lotA_id, 1, NULL, NULL, NULL, NULL,
  0, '测试入口车道', @tenant_id, @creator, NOW(), @creator, NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1 FROM iot_parking_lane
   WHERE lane_code = 'LANE_A_IN' AND tenant_id = @tenant_id AND deleted = b'0'
)
LIMIT 1;

INSERT INTO iot_parking_lane
  (lane_name, lane_code, lot_id, direction, main_camera_id, main_screen_id, aux_camera_id, aux_screen_id,
   status, remark, tenant_id, creator, create_time, updater, update_time, deleted)
SELECT
  'A出口车道', 'LANE_A_OUT', @lotA_id, 2, NULL, NULL, NULL, NULL,
  0, '测试出口车道', @tenant_id, @creator, NOW(), @creator, NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1 FROM iot_parking_lane
   WHERE lane_code = 'LANE_A_OUT' AND tenant_id = @tenant_id AND deleted = b'0'
)
LIMIT 1;

SET @laneA_in_id := (
  SELECT id FROM iot_parking_lane
   WHERE lane_code = 'LANE_A_IN' AND tenant_id = @tenant_id AND deleted = b'0'
   ORDER BY id DESC LIMIT 1
);
SET @laneA_out_id := (
  SELECT id FROM iot_parking_lane
   WHERE lane_code = 'LANE_A_OUT' AND tenant_id = @tenant_id AND deleted = b'0'
   ORDER BY id DESC LIMIT 1
);

-- ----------------------------
-- 3) 道闸（iot_parking_gate）
-- ----------------------------
INSERT INTO iot_parking_gate
  (gate_name, gate_code, lot_id, lane_id, device_id, ip_address, port, username, password, manufacturer, model,
   gate_type, direction, online_status, last_heartbeat, status, remark, tenant_id, creator, create_time, updater, update_time, deleted)
SELECT
  'A入口道闸', 'GATE_A_IN', @lotA_id, @laneA_in_id, NULL, '192.168.10.11', 80, 'admin', 'admin123', 'DEMO', 'GATE-1000',
  1, 1, 1, NOW(), 0, '测试入口道闸', @tenant_id, @creator, NOW(), @creator, NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1 FROM iot_parking_gate
   WHERE gate_code = 'GATE_A_IN' AND tenant_id = @tenant_id AND deleted = b'0'
)
LIMIT 1;

INSERT INTO iot_parking_gate
  (gate_name, gate_code, lot_id, lane_id, device_id, ip_address, port, username, password, manufacturer, model,
   gate_type, direction, online_status, last_heartbeat, status, remark, tenant_id, creator, create_time, updater, update_time, deleted)
SELECT
  'A出口道闸', 'GATE_A_OUT', @lotA_id, @laneA_out_id, NULL, '192.168.10.12', 80, 'admin', 'admin123', 'DEMO', 'GATE-1000',
  1, 2, 1, NOW(), 0, '测试出口道闸', @tenant_id, @creator, NOW(), @creator, NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1 FROM iot_parking_gate
   WHERE gate_code = 'GATE_A_OUT' AND tenant_id = @tenant_id AND deleted = b'0'
)
LIMIT 1;

SET @gateA_in_id := (
  SELECT id FROM iot_parking_gate
   WHERE gate_code = 'GATE_A_IN' AND tenant_id = @tenant_id AND deleted = b'0'
   ORDER BY id DESC LIMIT 1
);
SET @gateA_out_id := (
  SELECT id FROM iot_parking_gate
   WHERE gate_code = 'GATE_A_OUT' AND tenant_id = @tenant_id AND deleted = b'0'
   ORDER BY id DESC LIMIT 1
);

-- ----------------------------
-- 4) 收费规则（iot_parking_charge_rule）
-- ----------------------------
INSERT INTO iot_parking_charge_rule
  (rule_name, charge_mode, per_time_fee, free_minutes, first_hour_fee, extra_hour_fee, max_daily_fee, cycle_type,
   night_discount, night_start_time, night_end_time, rule_config, status, remark,
   tenant_id, creator, create_time, updater, update_time, deleted)
SELECT
  '临停按时收费(示例)', 2, NULL, 15, 5.00, 3.00, 50.00, 1,
  0.80, '22:00:00', '06:00:00',
  '{\"freeMinutes\":15,\"firstHourFee\":5.00,\"extraHourFee\":3.00,\"maxDailyFee\":50.00,\"nightDiscount\":0.80}',
  0, '联调用收费规则', @tenant_id, @creator, NOW(), @creator, NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1 FROM iot_parking_charge_rule
   WHERE rule_name = '临停按时收费(示例)' AND tenant_id = @tenant_id AND deleted = b'0'
)
LIMIT 1;

SET @charge_rule_id := (
  SELECT id FROM iot_parking_charge_rule
   WHERE rule_name = '临停按时收费(示例)' AND tenant_id = @tenant_id AND deleted = b'0'
   ORDER BY id DESC LIMIT 1
);

-- ----------------------------
-- 5) 放行规则（iot_parking_pass_rule）
-- ----------------------------
SET @pass_rule_lot_ids := CONCAT('[', @lotA_id, ']');
SET @pass_rule_lane_ids := CONCAT('[', @laneA_in_id, ',', @laneA_out_id, ']');

INSERT INTO iot_parking_pass_rule
  (rule_name, lot_ids, special_vehicle_types, vehicle_categories, charge_vehicle_types,
   entry_confirm_rule, exit_confirm_rule, lane_ids, priority, status, remark,
   tenant_id, creator, create_time, updater, update_time, deleted)
SELECT
  '默认放行规则(示例)', @pass_rule_lot_ids, '[]', '[\"temporary\",\"monthly\",\"free\"]', '[1,2,3,4,5]',
  1, 1, @pass_rule_lane_ids, 100, 0, '联调用放行规则',
  @tenant_id, @creator, NOW(), @creator, NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1 FROM iot_parking_pass_rule
   WHERE rule_name = '默认放行规则(示例)' AND tenant_id = @tenant_id AND deleted = b'0'
)
LIMIT 1;

SET @pass_rule_id := (
  SELECT id FROM iot_parking_pass_rule
   WHERE rule_name = '默认放行规则(示例)' AND tenant_id = @tenant_id AND deleted = b'0'
   ORDER BY id DESC LIMIT 1
);

-- ----------------------------
-- 6) 系统配置（iot_parking_system_config）
-- ----------------------------
INSERT INTO iot_parking_system_config
  (lot_id, parking_name, address, phone, total_spaces, business_hours, parking_type, remark,
   tenant_id, creator, create_time, updater, update_time, deleted)
SELECT
  @lotA_id, '示例停车场A', '示例市示例区示例路 88 号', '13800000001', 200, '00:00-24:00', '商业停车场', '联调用系统配置',
  @tenant_id, @creator, NOW(), @creator, NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1 FROM iot_parking_system_config
   WHERE lot_id = @lotA_id AND tenant_id = @tenant_id AND deleted = b'0'
)
LIMIT 1;

-- ----------------------------
-- 7) 月租车（iot_parking_monthly_vehicle）
-- ----------------------------
INSERT INTO iot_parking_monthly_vehicle
  (plate_number, owner_name, owner_phone, vehicle_type, lot_id, valid_start, valid_end, monthly_fee,
   last_charge_time, last_charge_months, status, remark,
   tenant_id, creator, create_time, updater, update_time, deleted)
SELECT
  '京A88888', '李四', '13800000002', 1, @lotA_id,
  DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_ADD(NOW(), INTERVAL 20 DAY), 300.00,
  DATE_SUB(NOW(), INTERVAL 10 DAY), 1, 0, '示例月租车（有效）',
  @tenant_id, @creator, NOW(), @creator, NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1 FROM iot_parking_monthly_vehicle
   WHERE plate_number = '京A88888' AND tenant_id = @tenant_id AND deleted = b'0'
)
LIMIT 1;

-- ----------------------------
-- 8) 黑名单（iot_parking_blacklist）
-- ----------------------------
INSERT INTO iot_parking_blacklist
  (plate_number, reason, end_time, lot_id, status, remark,
   tenant_id, creator, create_time, updater, update_time, deleted)
SELECT
  '京A00000', '测试：欠费/异常车辆', DATE_ADD(NOW(), INTERVAL 7 DAY), @lotA_id, 0, '联调用黑名单',
  @tenant_id, @creator, NOW(), @creator, NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1 FROM iot_parking_blacklist
   WHERE plate_number = '京A00000' AND tenant_id = @tenant_id AND deleted = b'0'
)
LIMIT 1;

-- ----------------------------
-- 9) 在场车辆（iot_parking_present_vehicle）
-- ----------------------------
INSERT INTO iot_parking_present_vehicle
  (plate_number, vehicle_type, vehicle_category, lot_id, entry_lane_id, entry_gate_id, entry_time, entry_photo_url,
   entry_operator, parking_duration, long_term_flag, status, remark,
   tenant_id, creator, create_time, updater, update_time, deleted)
SELECT
  '京A12345', 1, 'temporary', @lotA_id, @laneA_in_id, @gateA_in_id, DATE_SUB(NOW(), INTERVAL 2 HOUR),
  'https://example.invalid/parking/entry/京A12345.jpg', '系统', 120, 0, 0, '示例在场临停',
  @tenant_id, @creator, NOW(), @creator, NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1 FROM iot_parking_present_vehicle
   WHERE plate_number = '京A12345' AND tenant_id = @tenant_id AND deleted = b'0'
)
LIMIT 1;

-- ----------------------------
-- 10) 进出记录（iot_parking_record）
--   - 一条“已出场 + 已支付”
-- ----------------------------
INSERT INTO iot_parking_record
  (plate_number, vehicle_type, vehicle_category, lot_id,
   entry_lane_id, entry_gate_id, entry_time, entry_photo_url, entry_operator,
   exit_lane_id, exit_gate_id, exit_time, exit_photo_url, exit_operator,
   parking_duration, charge_amount, paid_amount, payment_method, payment_time, payment_status,
   exit_type, charge_rule_id, pass_rule_id, record_status, remark,
   tenant_id, creator, create_time, updater, update_time, deleted)
SELECT
  '京B54321', 1, 'temporary', @lotA_id,
  @laneA_in_id, @gateA_in_id, DATE_SUB(NOW(), INTERVAL 5 HOUR), 'https://example.invalid/parking/entry/京B54321.jpg', '系统',
  @laneA_out_id, @gateA_out_id, DATE_SUB(NOW(), INTERVAL 10 MINUTE), 'https://example.invalid/parking/exit/京B54321.jpg', '系统',
  290, 20.00, 20.00, 'wechat', DATE_SUB(NOW(), INTERVAL 9 MINUTE), 1,
  'normal', @charge_rule_id, @pass_rule_id, 2, '示例已出场记录',
  @tenant_id, @creator, NOW(), @creator, NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1 FROM iot_parking_record
   WHERE plate_number = '京B54321' AND lot_id = @lotA_id AND tenant_id = @tenant_id AND deleted = b'0'
)
LIMIT 1;

SET @record_paid_id := (
  SELECT id FROM iot_parking_record
   WHERE plate_number = '京B54321' AND lot_id = @lotA_id AND tenant_id = @tenant_id AND deleted = b'0'
   ORDER BY id DESC LIMIT 1
);

-- ----------------------------
-- 11) 退款记录（iot_parking_refund_record）
--   - 仅当存在上面那条 record 时插入（防止孤儿数据）
-- ----------------------------
INSERT INTO iot_parking_refund_record
  (record_id, plate_number, out_trade_no, transaction_id, out_refund_no, refund_id,
   total_fee, refund_fee, refund_reason, refund_status, apply_time, refund_time,
   apply_user, audit_user, fail_reason, remark,
   tenant_id, creator, create_time, updater, update_time, deleted)
SELECT
  @record_paid_id, '京B54321',
  CONCAT('OUT_TRADE_', @record_paid_id), CONCAT('WX_TX_', @record_paid_id),
  CONCAT('OUT_REFUND_', @record_paid_id), CONCAT('WX_REFUND_', @record_paid_id),
  20.00, 20.00, '测试退款', 1, DATE_SUB(NOW(), INTERVAL 8 MINUTE), DATE_SUB(NOW(), INTERVAL 2 MINUTE),
  '系统', '系统', NULL, '示例退款成功记录',
  @tenant_id, @creator, NOW(), @creator, NOW(), b'0'
WHERE @record_paid_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM iot_parking_refund_record
     WHERE record_id = @record_paid_id AND tenant_id = @tenant_id AND deleted = b'0'
  )
LIMIT 1;

-- ----------------------------
-- 12) 微信用户（iot_parking_wechat_user）
-- ----------------------------
INSERT INTO iot_parking_wechat_user
  (openid, unionid, session_key, username, nickname, avatar, mobile, status, last_login_time, last_login_ip,
   tenant_id, creator, create_time, updater, update_time, deleted)
SELECT
  'oTEST_openid_001', NULL, NULL, 'wechat_user_001', '测试用户001', NULL, '13800000003', 0, NOW(), '127.0.0.1',
  @tenant_id, @creator, NOW(), @creator, NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1 FROM iot_parking_wechat_user
   WHERE openid = 'oTEST_openid_001' AND deleted = b'0'
)
LIMIT 1;

-- ----------------------------
-- 13) 批量历史记录（用于统计报表演示）
--   - 覆盖短停/中停/长停
--   - 覆盖工作日/周末、不同时段
--   - 覆盖微信/支付宝/现金/免费
-- ----------------------------
INSERT INTO iot_parking_record
  (plate_number, vehicle_type, vehicle_category, lot_id,
   entry_lane_id, entry_gate_id, entry_time, entry_photo_url, entry_operator,
   exit_lane_id, exit_gate_id, exit_time, exit_photo_url, exit_operator,
   parking_duration, charge_amount, paid_amount, payment_method, payment_time, payment_status,
   exit_type, charge_rule_id, pass_rule_id, record_status, remark,
   tenant_id, creator, create_time, updater, update_time, deleted)
SELECT
  t.plate_number, t.vehicle_type, t.vehicle_category, @lotA_id,
  @laneA_in_id, @gateA_in_id, t.entry_time, t.entry_photo_url, '系统',
  @laneA_out_id, @gateA_out_id, t.exit_time, t.exit_photo_url, '系统',
  t.parking_duration, t.charge_amount, t.paid_amount, t.payment_method, t.payment_time, t.payment_status,
  t.exit_type, @charge_rule_id, @pass_rule_id, 2, t.remark,
  @tenant_id, @creator, NOW(), @creator, NOW(), b'0'
FROM (
  SELECT '京C10001' AS plate_number, 1 AS vehicle_type, 'temporary' AS vehicle_category,
         DATE_SUB(NOW(), INTERVAL 1 DAY) - INTERVAL 2 HOUR AS entry_time,
         DATE_SUB(NOW(), INTERVAL 1 DAY) - INTERVAL 1 HOUR - INTERVAL 35 MINUTE AS exit_time,
         25 AS parking_duration, 5.00 AS charge_amount, 5.00 AS paid_amount, 'wechat' AS payment_method,
         DATE_SUB(NOW(), INTERVAL 1 DAY) - INTERVAL 1 HOUR - INTERVAL 34 MINUTE AS payment_time, 1 AS payment_status,
         'normal' AS exit_type, 'https://example.invalid/parking/entry/京C10001.jpg' AS entry_photo_url,
         'https://example.invalid/parking/exit/京C10001.jpg' AS exit_photo_url, '短停-工作日晚高峰' AS remark
  UNION ALL
  SELECT '京C10002', 1, 'temporary',
         DATE_SUB(NOW(), INTERVAL 1 DAY) - INTERVAL 10 HOUR,
         DATE_SUB(NOW(), INTERVAL 1 DAY) - INTERVAL 6 HOUR - INTERVAL 20 MINUTE,
         220, 14.00, 14.00, 'alipay',
         DATE_SUB(NOW(), INTERVAL 1 DAY) - INTERVAL 6 HOUR - INTERVAL 18 MINUTE, 1,
         'normal', 'https://example.invalid/parking/entry/京C10002.jpg',
         'https://example.invalid/parking/exit/京C10002.jpg', '中停-工作日白天'
  UNION ALL
  SELECT '京C10003', 3, 'temporary',
         DATE_SUB(NOW(), INTERVAL 2 DAY) - INTERVAL 18 HOUR,
         DATE_SUB(NOW(), INTERVAL 2 DAY) - INTERVAL 9 HOUR,
         540, 32.00, 32.00, 'wechat',
         DATE_SUB(NOW(), INTERVAL 2 DAY) - INTERVAL 8 HOUR - INTERVAL 58 MINUTE, 1,
         'normal', 'https://example.invalid/parking/entry/京C10003.jpg',
         'https://example.invalid/parking/exit/京C10003.jpg', '长停-新能源车'
  UNION ALL
  SELECT '京C10004', 1, 'monthly',
         DATE_SUB(NOW(), INTERVAL 3 DAY) - INTERVAL 9 HOUR,
         DATE_SUB(NOW(), INTERVAL 3 DAY) - INTERVAL 7 HOUR,
         120, 0.00, 0.00, 'card',
         DATE_SUB(NOW(), INTERVAL 3 DAY) - INTERVAL 6 HOUR - INTERVAL 59 MINUTE, 2,
         'free', 'https://example.invalid/parking/entry/京C10004.jpg',
         'https://example.invalid/parking/exit/京C10004.jpg', '月租放行'
  UNION ALL
  SELECT '京C10005', 2, 'temporary',
         DATE_SUB(NOW(), INTERVAL 4 DAY) - INTERVAL 12 HOUR,
         DATE_SUB(NOW(), INTERVAL 4 DAY) - INTERVAL 10 HOUR - INTERVAL 5 MINUTE,
         115, 8.00, 8.00, 'cash',
         DATE_SUB(NOW(), INTERVAL 4 DAY) - INTERVAL 10 HOUR - INTERVAL 4 MINUTE, 1,
         'normal', 'https://example.invalid/parking/entry/京C10005.jpg',
         'https://example.invalid/parking/exit/京C10005.jpg', '中停-现金支付'
  UNION ALL
  SELECT '京C10006', 1, 'free',
         DATE_SUB(NOW(), INTERVAL 5 DAY) - INTERVAL 3 HOUR,
         DATE_SUB(NOW(), INTERVAL 5 DAY) - INTERVAL 2 HOUR - INTERVAL 40 MINUTE,
         20, 0.00, 0.00, 'card',
         DATE_SUB(NOW(), INTERVAL 5 DAY) - INTERVAL 2 HOUR - INTERVAL 39 MINUTE, 2,
         'free', 'https://example.invalid/parking/entry/京C10006.jpg',
         'https://example.invalid/parking/exit/京C10006.jpg', '免费车快速通行'
  UNION ALL
  SELECT '京C10007', 1, 'temporary',
         DATE_SUB(NOW(), INTERVAL 6 DAY) - INTERVAL 20 HOUR,
         DATE_SUB(NOW(), INTERVAL 6 DAY) - INTERVAL 12 HOUR,
         480, 29.00, 29.00, 'alipay',
         DATE_SUB(NOW(), INTERVAL 6 DAY) - INTERVAL 11 HOUR - INTERVAL 58 MINUTE, 1,
         'normal', 'https://example.invalid/parking/entry/京C10007.jpg',
         'https://example.invalid/parking/exit/京C10007.jpg', '长停-周末'
  UNION ALL
  SELECT '京C10008', 1, 'temporary',
         DATE_SUB(NOW(), INTERVAL 7 DAY) - INTERVAL 6 HOUR,
         DATE_SUB(NOW(), INTERVAL 7 DAY) - INTERVAL 5 HOUR - INTERVAL 5 MINUTE,
         55, 5.00, 5.00, 'wechat',
         DATE_SUB(NOW(), INTERVAL 7 DAY) - INTERVAL 5 HOUR - INTERVAL 4 MINUTE, 1,
         'normal', 'https://example.invalid/parking/entry/京C10008.jpg',
         'https://example.invalid/parking/exit/京C10008.jpg', '短停-上午'
  UNION ALL
  SELECT '京C10009', 4, 'temporary',
         DATE_SUB(NOW(), INTERVAL 8 DAY) - INTERVAL 15 HOUR,
         DATE_SUB(NOW(), INTERVAL 8 DAY) - INTERVAL 2 HOUR,
         780, 50.00, 50.00, 'wechat',
         DATE_SUB(NOW(), INTERVAL 8 DAY) - INTERVAL 1 HOUR - INTERVAL 59 MINUTE, 1,
         'normal', 'https://example.invalid/parking/entry/京C10009.jpg',
         'https://example.invalid/parking/exit/京C10009.jpg', '超长停-封顶收费'
  UNION ALL
  SELECT '京C10010', 3, 'temporary',
         DATE_SUB(NOW(), INTERVAL 10 DAY) - INTERVAL 8 HOUR,
         DATE_SUB(NOW(), INTERVAL 10 DAY) - INTERVAL 6 HOUR,
         120, 8.00, 8.00, 'alipay',
         DATE_SUB(NOW(), INTERVAL 10 DAY) - INTERVAL 5 HOUR - INTERVAL 58 MINUTE, 1,
         'normal', 'https://example.invalid/parking/entry/京C10010.jpg',
         'https://example.invalid/parking/exit/京C10010.jpg', '中停-新能源车'
  UNION ALL
  SELECT '京C10011', 1, 'temporary',
         DATE_SUB(NOW(), INTERVAL 11 DAY) - INTERVAL 4 HOUR,
         DATE_SUB(NOW(), INTERVAL 11 DAY) - INTERVAL 3 HOUR - INTERVAL 10 MINUTE,
         50, 5.00, 5.00, 'wechat',
         DATE_SUB(NOW(), INTERVAL 11 DAY) - INTERVAL 3 HOUR - INTERVAL 9 MINUTE, 1,
         'normal', 'https://example.invalid/parking/entry/京C10011.jpg',
         'https://example.invalid/parking/exit/京C10011.jpg', '短停-午间'
  UNION ALL
  SELECT '京C10012', 1, 'temporary',
         DATE_SUB(NOW(), INTERVAL 12 DAY) - INTERVAL 22 HOUR,
         DATE_SUB(NOW(), INTERVAL 12 DAY) - INTERVAL 17 HOUR,
         300, 20.00, 20.00, 'cash',
         DATE_SUB(NOW(), INTERVAL 12 DAY) - INTERVAL 16 HOUR - INTERVAL 59 MINUTE, 1,
         'normal', 'https://example.invalid/parking/entry/京C10012.jpg',
         'https://example.invalid/parking/exit/京C10012.jpg', '夜间中长停'
) t
WHERE NOT EXISTS (
  SELECT 1
  FROM iot_parking_record r
  WHERE r.plate_number = t.plate_number
    AND r.lot_id = @lotA_id
    AND r.tenant_id = @tenant_id
    AND r.deleted = b'0'
)
LIMIT 100;

-- ----------------------------
-- 14) 增补在场车辆（用于“当前在场”演示）
-- ----------------------------
INSERT INTO iot_parking_present_vehicle
  (plate_number, vehicle_type, vehicle_category, lot_id, entry_lane_id, entry_gate_id, entry_time, entry_photo_url,
   entry_operator, parking_duration, long_term_flag, status, remark,
   tenant_id, creator, create_time, updater, update_time, deleted)
SELECT
  t.plate_number, t.vehicle_type, t.vehicle_category, @lotA_id, @laneA_in_id, @gateA_in_id, t.entry_time, t.entry_photo_url,
  '系统', t.parking_duration, t.long_term_flag, 0, t.remark,
  @tenant_id, @creator, NOW(), @creator, NOW(), b'0'
FROM (
  SELECT '京C20001' AS plate_number, 1 AS vehicle_type, 'temporary' AS vehicle_category,
         DATE_SUB(NOW(), INTERVAL 45 MINUTE) AS entry_time, 45 AS parking_duration, 0 AS long_term_flag,
         'https://example.invalid/parking/entry/京C20001.jpg' AS entry_photo_url, '临停在场-短停' AS remark
  UNION ALL
  SELECT '京C20002', 1, 'monthly',
         DATE_SUB(NOW(), INTERVAL 6 HOUR), 360, 0,
         'https://example.invalid/parking/entry/京C20002.jpg', '月租在场-白天'
  UNION ALL
  SELECT '京C20003', 2, 'temporary',
         DATE_SUB(NOW(), INTERVAL 28 HOUR), 1680, 1,
         'https://example.invalid/parking/entry/京C20003.jpg', '临停在场-超24小时'
) t
WHERE NOT EXISTS (
  SELECT 1
  FROM iot_parking_present_vehicle p
  WHERE p.plate_number = t.plate_number
    AND p.lot_id = @lotA_id
    AND p.tenant_id = @tenant_id
    AND p.deleted = b'0'
)
LIMIT 100;

-- 完成提示
SELECT
  '✅ iot_parking_seed_test_data.sql 已执行（幂等）' AS message,
  @tenant_id AS tenant_id,
  @lotA_id AS lot_id,
  @laneA_in_id AS lane_in_id,
  @laneA_out_id AS lane_out_id,
  @gateA_in_id AS gate_in_id,
  @gateA_out_id AS gate_out_id,
  @charge_rule_id AS charge_rule_id,
  @pass_rule_id AS pass_rule_id,
  @record_paid_id AS sample_record_id,
  (SELECT COUNT(1) FROM iot_parking_record
    WHERE lot_id = @lotA_id AND tenant_id = @tenant_id AND deleted = b'0') AS record_count,
  (SELECT COUNT(1) FROM iot_parking_present_vehicle
    WHERE lot_id = @lotA_id AND tenant_id = @tenant_id AND deleted = b'0') AS present_count;

