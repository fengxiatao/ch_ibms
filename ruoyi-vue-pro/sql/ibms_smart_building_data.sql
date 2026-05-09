-- =============================================
-- 智慧楼宇模块测试数据
-- 包含：环境监测、智能照明、楼宇自控、能耗计量
-- =============================================

-- =============================================
-- 1. 环境监测数据
-- =============================================

-- 环境传感器
INSERT INTO `ibms_env_sensor` (`sensor_code`, `sensor_name`, `sensor_type`, `area_id`, `area_name`, `floor`, `location`, `status`, `last_online_time`, `install_time`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`) VALUES
('ENV-TH-001', '大堂温湿度传感器', 1, 1, '一楼大堂', 'F1', '大堂入口处', 1, NOW(), '2024-01-15', '监测大堂环境温湿度', '1', NOW(), '1', NOW(), 0, 1),
('ENV-TH-002', '会议室温湿度传感器', 1, 2, '三楼会议室', 'F3', '会议室中央', 1, NOW(), '2024-01-15', '监测会议室温湿度', '1', NOW(), '1', NOW(), 0, 1),
('ENV-TH-003', '办公区温湿度传感器A', 1, 3, '五楼办公区', 'F5', '办公区A区', 1, NOW(), '2024-01-16', NULL, '1', NOW(), '1', NOW(), 0, 1),
('ENV-TH-004', '办公区温湿度传感器B', 1, 3, '五楼办公区', 'F5', '办公区B区', 0, DATE_SUB(NOW(), INTERVAL 2 HOUR), '2024-01-16', '设备离线待检修', '1', NOW(), '1', NOW(), 0, 1),
('ENV-PM-001', '大堂PM2.5传感器', 2, 1, '一楼大堂', 'F1', '大堂中央', 1, NOW(), '2024-02-01', '空气质量监测', '1', NOW(), '1', NOW(), 0, 1),
('ENV-PM-002', '地下停车场PM2.5传感器', 2, 4, '地下一层停车场', 'B1', '停车场入口', 1, NOW(), '2024-02-01', NULL, '1', NOW(), '1', NOW(), 0, 1),
('ENV-LUX-001', '大堂光照传感器', 3, 1, '一楼大堂', 'F1', '大堂天花板', 1, NOW(), '2024-02-15', '监测大堂光照强度', '1', NOW(), '1', NOW(), 0, 1),
('ENV-LUX-002', '办公区光照传感器', 3, 3, '五楼办公区', 'F5', '办公区天花板', 1, NOW(), '2024-02-15', NULL, '1', NOW(), '1', NOW(), 0, 1),
('ENV-NOISE-001', '大堂噪音传感器', 4, 1, '一楼大堂', 'F1', '大堂服务台', 1, NOW(), '2024-03-01', '噪音监测', '1', NOW(), '1', NOW(), 0, 1),
('ENV-NOISE-002', '会议室噪音传感器', 4, 2, '三楼会议室', 'F3', '会议室侧墙', 1, NOW(), '2024-03-01', NULL, '1', NOW(), '1', NOW(), 0, 1),
('ENV-PRESS-001', '楼顶气压传感器', 5, 5, '楼顶设备层', 'RF', '楼顶', 1, NOW(), '2024-03-15', '大气压力监测', '1', NOW(), '1', NOW(), 0, 1),
('ENV-TH-005', '数据中心温湿度传感器', 1, 6, '负一层数据中心', 'B1', '机房中央', 2, NOW(), '2024-01-20', '告警：温度偏高', '1', NOW(), '1', NOW(), 0, 1);

-- 环境监测数据记录（最近24小时的数据）
INSERT INTO `ibms_env_data_record` (`sensor_id`, `sensor_code`, `temperature`, `humidity`, `pm25`, `pm10`, `co2`, `formaldehyde`, `illuminance`, `noise`, `collect_time`, `create_time`, `tenant_id`) VALUES
-- 温湿度传感器数据
(1, 'ENV-TH-001', 24.5, 55.0, NULL, NULL, NULL, NULL, NULL, NULL, DATE_SUB(NOW(), INTERVAL 1 HOUR), NOW(), 1),
(1, 'ENV-TH-001', 24.8, 54.2, NULL, NULL, NULL, NULL, NULL, NULL, DATE_SUB(NOW(), INTERVAL 2 HOUR), NOW(), 1),
(1, 'ENV-TH-001', 24.2, 56.1, NULL, NULL, NULL, NULL, NULL, NULL, DATE_SUB(NOW(), INTERVAL 3 HOUR), NOW(), 1),
(1, 'ENV-TH-001', 23.9, 57.5, NULL, NULL, NULL, NULL, NULL, NULL, DATE_SUB(NOW(), INTERVAL 4 HOUR), NOW(), 1),
(1, 'ENV-TH-001', 23.5, 58.0, NULL, NULL, NULL, NULL, NULL, NULL, DATE_SUB(NOW(), INTERVAL 5 HOUR), NOW(), 1),
(2, 'ENV-TH-002', 23.8, 52.0, NULL, NULL, NULL, NULL, NULL, NULL, DATE_SUB(NOW(), INTERVAL 1 HOUR), NOW(), 1),
(2, 'ENV-TH-002', 24.0, 51.5, NULL, NULL, NULL, NULL, NULL, NULL, DATE_SUB(NOW(), INTERVAL 2 HOUR), NOW(), 1),
(2, 'ENV-TH-002', 23.5, 53.2, NULL, NULL, NULL, NULL, NULL, NULL, DATE_SUB(NOW(), INTERVAL 3 HOUR), NOW(), 1),
(3, 'ENV-TH-003', 25.2, 48.0, NULL, NULL, NULL, NULL, NULL, NULL, DATE_SUB(NOW(), INTERVAL 1 HOUR), NOW(), 1),
(3, 'ENV-TH-003', 25.5, 47.5, NULL, NULL, NULL, NULL, NULL, NULL, DATE_SUB(NOW(), INTERVAL 2 HOUR), NOW(), 1),
-- PM2.5传感器数据
(5, 'ENV-PM-001', NULL, NULL, 35.0, 48.0, NULL, NULL, NULL, NULL, DATE_SUB(NOW(), INTERVAL 1 HOUR), NOW(), 1),
(5, 'ENV-PM-001', NULL, NULL, 38.5, 52.0, NULL, NULL, NULL, NULL, DATE_SUB(NOW(), INTERVAL 2 HOUR), NOW(), 1),
(5, 'ENV-PM-001', NULL, NULL, 32.0, 45.0, NULL, NULL, NULL, NULL, DATE_SUB(NOW(), INTERVAL 3 HOUR), NOW(), 1),
(6, 'ENV-PM-002', NULL, NULL, 42.0, 58.0, NULL, NULL, NULL, NULL, DATE_SUB(NOW(), INTERVAL 1 HOUR), NOW(), 1),
(6, 'ENV-PM-002', NULL, NULL, 45.5, 62.0, NULL, NULL, NULL, NULL, DATE_SUB(NOW(), INTERVAL 2 HOUR), NOW(), 1),
-- 光照传感器数据
(7, 'ENV-LUX-001', NULL, NULL, NULL, NULL, NULL, NULL, 850.0, NULL, DATE_SUB(NOW(), INTERVAL 1 HOUR), NOW(), 1),
(7, 'ENV-LUX-001', NULL, NULL, NULL, NULL, NULL, NULL, 920.0, NULL, DATE_SUB(NOW(), INTERVAL 2 HOUR), NOW(), 1),
(8, 'ENV-LUX-002', NULL, NULL, NULL, NULL, NULL, NULL, 520.0, NULL, DATE_SUB(NOW(), INTERVAL 1 HOUR), NOW(), 1),
-- 噪音传感器数据
(9, 'ENV-NOISE-001', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 45.5, DATE_SUB(NOW(), INTERVAL 1 HOUR), NOW(), 1),
(9, 'ENV-NOISE-001', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 48.2, DATE_SUB(NOW(), INTERVAL 2 HOUR), NOW(), 1),
(10, 'ENV-NOISE-002', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 38.0, DATE_SUB(NOW(), INTERVAL 1 HOUR), NOW(), 1);

-- 环境告警记录
INSERT INTO `ibms_env_alarm` (`sensor_id`, `sensor_code`, `sensor_name`, `alarm_type`, `alarm_level`, `alarm_content`, `alarm_value`, `threshold_value`, `alarm_time`, `recover_time`, `status`, `handler`, `handle_time`, `handle_remark`, `create_time`, `update_time`, `tenant_id`) VALUES
(12, 'ENV-TH-005', '数据中心温湿度传感器', 1, 3, '数据中心温度过高，已超过告警阈值', '32.5', '30', DATE_SUB(NOW(), INTERVAL 30 MINUTE), NULL, 1, '张工', NOW(), '正在排查空调故障', NOW(), NOW(), 1),
(6, 'ENV-PM-002', '地下停车场PM2.5传感器', 3, 2, '停车场PM2.5浓度偏高', '85', '75', DATE_SUB(NOW(), INTERVAL 2 HOUR), DATE_SUB(NOW(), INTERVAL 1 HOUR), 3, NULL, NULL, NULL, NOW(), NOW(), 1),
(4, 'ENV-TH-004', '办公区温湿度传感器B', 8, 2, '设备离线超过2小时', NULL, NULL, DATE_SUB(NOW(), INTERVAL 2 HOUR), NULL, 0, NULL, NULL, NULL, NOW(), NOW(), 1);

-- =============================================
-- 2. 智能照明数据
-- =============================================

-- 照明网关
INSERT INTO `ibms_lighting_gateway` (`gateway_code`, `gateway_name`, `gateway_model`, `ip_address`, `mac_address`, `area_name`, `firmware_version`, `device_count`, `signal_strength`, `status`, `last_online_time`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`) VALUES
('GW-F1-001', '智能照明网关-1F', 'IoT-GW-2024', '192.168.1.101', 'AA:BB:CC:DD:EE:01', '一楼弱电间', 'v2.1.5', 24, '强', 1, NOW(), '负责一楼照明控制', '1', NOW(), '1', NOW(), 0, 1),
('GW-F2-001', '智能照明网关-2F', 'IoT-GW-2024', '192.168.1.102', 'AA:BB:CC:DD:EE:02', '二楼弱电间', 'v2.1.5', 18, '中', 1, NOW(), '负责二楼照明控制', '1', NOW(), '1', NOW(), 0, 1),
('GW-F3-001', '智能照明网关-3F', 'IoT-GW-2024', '192.168.1.103', 'AA:BB:CC:DD:EE:03', '三楼弱电间', 'v2.1.4', 20, '强', 1, NOW(), '负责三楼照明控制', '1', NOW(), '1', NOW(), 0, 1);

-- 照明控制器
INSERT INTO `ibms_lighting_controller` (`controller_code`, `controller_name`, `controller_model`, `area_name`, `channel_count`, `rated_load`, `current_load`, `gateway_id`, `status`, `last_online_time`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`) VALUES
('CTRL-A1-001', '照明执行控制器-A1', 'LC-8CH-20A', 'A区一层', 8, '8通道/20A', '65%', 1, 1, NOW(), NULL, '1', NOW(), '1', NOW(), 0, 1),
('CTRL-A2-001', '照明执行控制器-A2', 'LC-8CH-20A', 'A区二层', 8, '8通道/20A', '48%', 2, 1, NOW(), NULL, '1', NOW(), '1', NOW(), 0, 1),
('CTRL-B1-001', '照明执行控制器-B1', 'LC-4CH-10A-DIM', 'B区展厅', 4, '4通道/10A(调光)', '72%', 1, 1, NOW(), '支持调光功能', '1', NOW(), '1', NOW(), 0, 1),
('CTRL-B2-001', '照明执行控制器-B2', 'LC-8CH-20A', 'B区二层', 8, '8通道/20A', '55%', 2, 1, NOW(), NULL, '1', NOW(), '1', NOW(), 0, 1);

-- 照明回路
INSERT INTO `ibms_lighting_circuit` (`circuit_code`, `circuit_name`, `circuit_type`, `area_id`, `area_name`, `floor`, `location`, `load_desc`, `rated_power`, `status`, `brightness`, `color_temp`, `controller_id`, `gateway_id`, `last_operate_time`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`) VALUES
('L-A1-01', '照明回路-01', 1, 1, 'A区一层', 'F1', '大堂东侧', 'LED灯具 x 8', 96, 1, 100, 4000, 1, 1, NOW(), NULL, '1', NOW(), '1', NOW(), 0, 1),
('L-A1-02', '照明回路-02', 1, 1, 'A区一层', 'F1', '大堂西侧', 'LED灯具 x 6', 72, 1, 100, 4000, 1, 1, NOW(), NULL, '1', NOW(), '1', NOW(), 0, 1),
('L-A1-03', '照明回路-03', 1, 1, 'A区一层', 'F1', '大堂中央', 'LED灯具 x 10', 120, 0, 0, 4000, 1, 1, DATE_SUB(NOW(), INTERVAL 2 HOUR), NULL, '1', NOW(), '1', NOW(), 0, 1),
('L-A1-04', '照明回路-04', 1, 1, 'A区一层', 'F1', '走廊', 'LED灯具 x 12', 144, 1, 80, 4000, 1, 1, NOW(), NULL, '1', NOW(), '1', NOW(), 0, 1),
('L-A2-01', '照明回路-05', 1, 2, 'A区二层', 'F2', '办公区东', 'LED灯具 x 8', 96, 1, 100, 4500, 2, 2, NOW(), NULL, '1', NOW(), '1', NOW(), 0, 1),
('L-A2-02', '照明回路-06', 1, 2, 'A区二层', 'F2', '办公区西', 'LED灯具 x 8', 96, 1, 100, 4500, 2, 2, NOW(), NULL, '1', NOW(), '1', NOW(), 0, 1),
('L-A2-03', '照明回路-07', 1, 2, 'A区二层', 'F2', '会议室A', 'LED灯具 x 4', 48, 0, 0, 4000, 2, 2, DATE_SUB(NOW(), INTERVAL 1 HOUR), NULL, '1', NOW(), '1', NOW(), 0, 1),
('L-A2-04', '照明回路-08', 1, 2, 'A区二层', 'F2', '会议室B', 'LED灯具 x 4', 48, 1, 100, 4000, 2, 2, NOW(), NULL, '1', NOW(), '1', NOW(), 0, 1),
('D-B1-001', '调光回路-01', 2, 3, 'B区展厅', 'F1', '展厅入口', '可调光LED射灯 x 6', 90, 1, 80, 3000, 3, 1, NOW(), '调光回路', '1', NOW(), '1', NOW(), 0, 1),
('D-B1-002', '调光回路-02', 2, 3, 'B区展厅', 'F1', '展区A', '可调光LED射灯 x 8', 120, 1, 50, 4500, 3, 1, NOW(), '调光回路', '1', NOW(), '1', NOW(), 0, 1),
('D-B1-003', '调光回路-03', 2, 3, 'B区展厅', 'F1', '展区B', '可调光LED射灯 x 8', 120, 1, 100, 5500, 3, 1, NOW(), '调光回路', '1', NOW(), '1', NOW(), 0, 1),
('D-B1-004', '调光回路-04', 2, 3, 'B区展厅', 'F1', '展区C', '可调光LED射灯 x 6', 90, 0, 0, 4000, 3, 1, DATE_SUB(NOW(), INTERVAL 3 HOUR), '调光回路', '1', NOW(), '1', NOW(), 0, 1),
('E-01', '应急照明-01', 3, 1, 'A区一层', 'F1', '消防通道', '应急灯 x 10', 50, 0, 0, 6500, 1, 1, NOW(), '应急照明回路', '1', NOW(), '1', NOW(), 0, 1),
('E-02', '应急照明-02', 3, 2, 'A区二层', 'F2', '消防通道', '应急灯 x 10', 50, 0, 0, 6500, 2, 2, NOW(), '应急照明回路', '1', NOW(), '1', NOW(), 0, 1);

-- 照明场景
INSERT INTO `ibms_lighting_scene` (`scene_code`, `scene_name`, `scene_icon`, `scene_desc`, `area_id`, `area_name`, `is_active`, `sort`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`) VALUES
('SCENE-MORNING', '上班模式', '🌅', '开启全部照明，亮度80%', NULL, '全区域', 0, 1, '工作日早晨自动执行', '1', NOW(), '1', NOW(), 0, 1),
('SCENE-MEETING', '会议模式', '📊', '会议室100%亮度，其他区域50%', 2, 'A区二层', 0, 2, '会议时使用', '1', NOW(), '1', NOW(), 0, 1),
('SCENE-LUNCH', '午休模式', '🍽️', '公共区域50%，办公区30%', NULL, '全区域', 0, 3, '午休时间节能', '1', NOW(), '1', NOW(), 0, 1),
('SCENE-OFFWORK', '下班模式', '🌙', '仅保留通道灯，亮度30%', NULL, '全区域', 0, 4, '下班后自动执行', '1', NOW(), '1', NOW(), 0, 1),
('SCENE-EXHIBIT', '展览模式', '✨', '展厅100%亮度，色温3000K暖光', 3, 'B区展厅', 0, 5, '展览时使用', '1', NOW(), '1', NOW(), 0, 1);

-- 照明定时任务
INSERT INTO `ibms_lighting_schedule` (`schedule_name`, `execute_time`, `weekdays`, `scene_id`, `scene_name`, `enabled`, `last_execute_time`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`) VALUES
('早晨自动开灯', '07:30', '1,2,3,4,5', 1, '上班模式', 1, DATE_SUB(NOW(), INTERVAL 7 HOUR), '工作日早晨执行', '1', NOW(), '1', NOW(), 0, 1),
('晚间自动关灯', '22:00', '0,1,2,3,4,5,6', 4, '下班模式', 1, DATE_SUB(NOW(), INTERVAL 2 DAY), '每天晚上执行', '1', NOW(), '1', NOW(), 0, 1),
('午休节能', '12:00', '1,2,3,4,5', 3, '午休模式', 1, NULL, '工作日中午执行', '1', NOW(), '1', NOW(), 0, 1),
('午休结束', '13:30', '1,2,3,4,5', 1, '上班模式', 1, NULL, '午休结束恢复', '1', NOW(), '1', NOW(), 0, 1);

-- 照明操作日志
INSERT INTO `ibms_lighting_operation_log` (`operation_type`, `target_type`, `target_id`, `target_name`, `operation_content`, `operator`, `operator_ip`, `result`, `error_msg`, `operate_time`, `create_time`, `tenant_id`) VALUES
(1, 1, 1, '照明回路-01', '手动开启回路', '管理员', '192.168.1.50', 1, NULL, DATE_SUB(NOW(), INTERVAL 30 MINUTE), NOW(), 1),
(2, 2, 2, '会议模式', '执行场景：会议室100%亮度', '系统', NULL, 1, NULL, DATE_SUB(NOW(), INTERVAL 1 HOUR), NOW(), 1),
(3, 2, 1, '上班模式', '定时任务触发：开启全部照明', '系统', NULL, 1, NULL, DATE_SUB(NOW(), INTERVAL 7 HOUR), NOW(), 1),
(1, 1, 9, '调光回路-01', '调节亮度至 80%', '操作员A', '192.168.1.55', 1, NULL, DATE_SUB(NOW(), INTERVAL 2 HOUR), NOW(), 1),
(4, 3, 1, '智能照明网关-1F', '设备心跳检测正常', '系统', NULL, 1, NULL, DATE_SUB(NOW(), INTERVAL 15 MINUTE), NOW(), 1),
(1, 1, 5, '照明回路-05', '延时关闭设置：30分钟', '管理员', '192.168.1.50', 1, NULL, DATE_SUB(NOW(), INTERVAL 45 MINUTE), NOW(), 1);

-- 照明告警
INSERT INTO `ibms_lighting_alarm` (`device_type`, `device_id`, `device_name`, `alarm_level`, `alarm_content`, `alarm_time`, `duration`, `status`, `handler`, `handle_time`, `handle_remark`, `create_time`, `update_time`, `tenant_id`) VALUES
(3, 3, '照明执行控制器-B1', 3, '通道3电流过载，超过额定值120%', DATE_SUB(NOW(), INTERVAL 5 MINUTE), '5分钟', 1, '张工', NOW(), '正在检修', NOW(), NOW(), 1),
(2, 2, '智能照明网关-2F', 2, '信号强度低于阈值（当前-75dBm）', DATE_SUB(NOW(), INTERVAL 15 MINUTE), '15分钟', 0, NULL, NULL, NULL, NOW(), NOW(), 1),
(1, 7, '照明回路-07', 1, '灯具离线，通信超时', DATE_SUB(NOW(), INTERVAL 1 HOUR), '已恢复', 2, '系统自动', DATE_SUB(NOW(), INTERVAL 30 MINUTE), '设备自动恢复', NOW(), NOW(), 1),
(1, 10, '调光回路-02', 2, '色温调节异常，反馈值与设定值偏差过大', DATE_SUB(NOW(), INTERVAL 10 MINUTE), '10分钟', 0, NULL, NULL, NULL, NOW(), NOW(), 1);

-- =============================================
-- 3. 楼宇自控数据
-- =============================================

-- 暖通设备
INSERT INTO `ibms_hvac_device` (`device_code`, `device_name`, `device_type`, `area_id`, `area_name`, `floor`, `location`, `status`, `run_mode`, `set_temp`, `room_temp`, `wind_speed`, `filter_status`, `pressure`, `run_hours`, `maintain_status`, `next_maintain_date`, `last_update_time`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`) VALUES
('AC-1F-01', '空调机组-01', 1, 1, '一楼大堂', 'F1', '大堂区域', 1, 1, 24.0, 23.5, 2, '正常', NULL, 4580, '正常', '2026-03-15', NOW(), NULL, '1', NOW(), '1', NOW(), 0, 1),
('AC-2F-01', '空调机组-02', 1, 2, '二楼东区', 'F2', '办公区东', 2, 1, 24.0, 24.2, 4, '正常', NULL, 3200, '正常', '2026-04-20', NOW(), NULL, '1', NOW(), '1', NOW(), 0, 1),
('AC-2F-02', '空调机组-03', 1, 2, '二楼西区', 'F2', '办公区西', 1, 1, 25.0, 24.8, 2, '需更换', NULL, 5100, '需保养', '2026-02-01', NOW(), '滤网需更换', '1', NOW(), '1', NOW(), 0, 1),
('AC-3F-01', '空调机组-04', 1, 3, '三楼东区', 'F3', '办公区东', 1, 2, 22.0, 21.5, 3, '正常', NULL, 2800, '正常', '2026-05-10', NOW(), NULL, '1', NOW(), '1', NOW(), 0, 1),
('AC-3F-02', '空调机组-05', 1, 3, '三楼西区', 'F3', '办公区西', 0, NULL, NULL, 22.0, NULL, '正常', NULL, 2500, '正常', '2026-04-15', DATE_SUB(NOW(), INTERVAL 2 HOUR), '已停机', '1', NOW(), '1', NOW(), 0, 1),
('AC-MR-01', '空调机组-06', 1, 4, '三楼会议室', 'F3', '大会议室', 2, 3, 24.0, 24.0, 4, '正常', NULL, 1200, '正常', '2026-06-01', NOW(), NULL, '1', NOW(), '1', NOW(), 0, 1),
('FA-01', '新风机组-01', 2, 1, '一层新风机房', 'F1', '新风机房', 1, NULL, NULL, NULL, NULL, '正常', 120.5, 7500, '正常', '2026-04-20', NOW(), NULL, '1', NOW(), '1', NOW(), 0, 1),
('FA-02', '新风机组-02', 2, 2, '二层新风机房', 'F2', '新风机房', 1, NULL, NULL, NULL, NULL, '正常', 135.2, 6800, '正常', '2026-05-15', NOW(), NULL, '1', NOW(), '1', NOW(), 0, 1),
('FA-03', '新风机组-03', 2, 3, '三层新风机房', 'F3', '新风机房', 1, NULL, NULL, NULL, NULL, '需更换', 128.0, 8200, '需保养', '2026-02-10', NOW(), '滤网需更换', '1', NOW(), '1', NOW(), 0, 1),
('FA-04', '新风机组-04', 2, 5, 'B1机房', 'B1', '地下机房', 3, NULL, NULL, NULL, NULL, '正常', 150.0, 4500, '故障待修', NULL, NOW(), '过滤网压差过大', '1', NOW(), '1', NOW(), 0, 1),
('SF-01', '送风机-01', 3, 1, '一层卫生间', 'F1', '卫生间', 1, NULL, NULL, NULL, NULL, NULL, NULL, 2100, '正常', '2026-05-15', NOW(), NULL, '1', NOW(), '1', NOW(), 0, 1),
('SF-02', '送风机-02', 3, 2, '二层走廊', 'F2', '走廊', 1, NULL, NULL, NULL, NULL, NULL, NULL, 1800, '正常', '2026-05-20', NOW(), NULL, '1', NOW(), '1', NOW(), 0, 1),
('SF-03', '送风机-03', 3, 3, '三层电梯厅', 'F3', '电梯厅', 0, NULL, NULL, NULL, NULL, NULL, NULL, 1500, '正常', '2026-06-01', DATE_SUB(NOW(), INTERVAL 1 HOUR), NULL, '1', NOW(), '1', NOW(), 0, 1),
('SF-04', '送风机-04', 3, 1, '一层茶水间', 'F1', '茶水间', 1, NULL, NULL, NULL, NULL, NULL, NULL, 950, '正常', '2026-06-15', NOW(), NULL, '1', NOW(), '1', NOW(), 0, 1),
('EF-01', '排风机-01', 4, 1, '一层卫生间', 'F1', '卫生间', 1, NULL, NULL, NULL, NULL, NULL, NULL, 2200, '正常', '2026-05-15', NOW(), NULL, '1', NOW(), '1', NOW(), 0, 1),
('EF-02', '排风机-02', 4, 2, '二层厨房', 'F2', '厨房', 0, NULL, NULL, NULL, NULL, NULL, NULL, 3500, '正常', '2026-04-20', DATE_SUB(NOW(), INTERVAL 30 MINUTE), '已停机', '1', NOW(), '1', NOW(), 0, 1),
('EF-03', '排风机-03', 4, 5, 'B1机房', 'B1', '机房', 1, NULL, NULL, NULL, NULL, NULL, NULL, 4200, '正常', '2026-05-01', NOW(), NULL, '1', NOW(), '1', NOW(), 0, 1),
('EF-04', '排风机-04', 4, 3, '三层吸烟室', 'F3', '吸烟室', 1, NULL, NULL, NULL, NULL, NULL, NULL, 1800, '正常', '2026-06-01', NOW(), NULL, '1', NOW(), '1', NOW(), 0, 1);

-- 给排水设备
INSERT INTO `ibms_water_device` (`device_code`, `device_name`, `device_type`, `area_id`, `area_name`, `location`, `status`, `run_mode`, `pressure`, `water_level`, `run_hours`, `maintain_status`, `last_update_time`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`) VALUES
('WP-B2-01', '生活水泵-1#', 1, 6, 'B2生活泵房', 'B2层泵房', 1, 1, 0.35, NULL, 3240, '正常', NOW(), '一用一备', '1', NOW(), '1', NOW(), 0, 1),
('WP-B2-02', '生活水泵-2#', 1, 6, 'B2生活泵房', 'B2层泵房', 2, 1, 0.00, NULL, 1560, '正常', NOW(), '备用泵', '1', NOW(), '1', NOW(), 0, 1),
('DP-B2-01', '集水坑排污泵-1#', 2, 6, 'B2集水坑', 'B2层集水坑', 1, 1, NULL, 35.0, 890, '正常', NOW(), '自动运行', '1', NOW(), '1', NOW(), 0, 1),
('DP-B2-02', '集水坑排污泵-2#', 2, 6, 'B2集水坑', 'B2层集水坑', 0, 1, NULL, 35.0, 420, '正常', NOW(), '备用泵', '1', NOW(), '1', NOW(), 0, 1),
('DP-B1-01', '消防电梯排污泵', 2, 5, 'B1电梯基坑', 'B1层电梯基坑', 1, 1, NULL, 42.0, 1200, '需保养', NOW(), '连续运行较长', '1', NOW(), '1', NOW(), 0, 1),
('TK-RF-01', '屋顶生活水箱', 3, 7, '屋顶水箱间', '屋顶', 1, NULL, NULL, 75.0, NULL, '正常', NOW(), '容量50立方米', '1', NOW(), '1', NOW(), 0, 1),
('TK-B2-01', '地下消防水池', 3, 6, 'B2水池间', 'B2层', 1, NULL, NULL, 92.0, NULL, '正常', NOW(), '容量200立方米', '1', NOW(), '1', NOW(), 0, 1),
('TK-B2-02', '地下低区水箱', 3, 6, 'B2水箱间', 'B2层', 1, NULL, NULL, 68.0, NULL, '正常', NOW(), '容量30立方米', '1', NOW(), '1', NOW(), 0, 1);

-- 楼宇自控告警
INSERT INTO `ibms_bac_alarm` (`device_type`, `device_id`, `device_name`, `alarm_level`, `alarm_content`, `alarm_time`, `duration`, `status`, `handler`, `handle_time`, `handle_remark`, `create_time`, `update_time`, `tenant_id`) VALUES
(1, 10, '新风机组-04', 3, '过滤网压差过大，需要立即更换', DATE_SUB(NOW(), INTERVAL 30 MINUTE), '30分钟', 1, '王工', NOW(), '已安排更换滤网', NOW(), NOW(), 1),
(2, 8, '地下低区水箱', 2, '液位传感器通讯异常', DATE_SUB(NOW(), INTERVAL 15 MINUTE), '15分钟', 0, NULL, NULL, NULL, NOW(), NOW(), 1),
(1, 3, '空调机组-03', 2, '盘管温度过低，防冻保护预警', DATE_SUB(NOW(), INTERVAL 45 MINUTE), '15分钟', 2, '李工', DATE_SUB(NOW(), INTERVAL 30 MINUTE), '已调整运行参数', NOW(), NOW(), 1),
(1, 16, '排风机-02', 1, '运行电流异常波动', DATE_SUB(NOW(), INTERVAL 2 HOUR), '已恢复', 2, '系统', DATE_SUB(NOW(), INTERVAL 1 HOUR), '自动恢复', NOW(), NOW(), 1),
(1, 13, '送风机-03', 2, '皮带打滑，风量不足', DATE_SUB(NOW(), INTERVAL 2 HOUR), '2小时', 0, NULL, NULL, NULL, NOW(), NOW(), 1),
(2, 5, '消防电梯排污泵', 2, '连续运行超时，需检查', DATE_SUB(NOW(), INTERVAL 45 MINUTE), '45分钟', 1, '张工', NOW(), '检查中', NOW(), NOW(), 1);

-- 楼宇自控系统日志
INSERT INTO `ibms_bac_system_log` (`log_type`, `device_type`, `device_id`, `device_name`, `event_desc`, `event_value`, `operator`, `log_time`, `create_time`, `tenant_id`) VALUES
(1, 1, 1, '空调机组-01', '设定温度调节', '24°C→25°C', '管理员', DATE_SUB(NOW(), INTERVAL 30 MINUTE), NOW(), 1),
(2, 2, 1, '生活水泵-1#', '压力低于设定值', '0.30MPa', NULL, DATE_SUB(NOW(), INTERVAL 45 MINUTE), NOW(), 1),
(2, 1, 3, '空调机组-03', '周期性除霜完成', '运行正常', NULL, DATE_SUB(NOW(), INTERVAL 1 HOUR), NOW(), 1),
(1, 1, 16, '排风机-02', '远程启停操作', '停止→运行', '操作员A', DATE_SUB(NOW(), INTERVAL 2 HOUR), NOW(), 1),
(2, 2, 3, '集水坑排污泵-1#', '自动运行启动', '自动模式', NULL, DATE_SUB(NOW(), INTERVAL 3 HOUR), NOW(), 1),
(1, 1, 2, '空调机组-02', '模式切换', '制冷→通风', '管理员', DATE_SUB(NOW(), INTERVAL 4 HOUR), NOW(), 1),
(3, 1, 5, '空调机组-05', '设备状态变更', '运行→停止', NULL, DATE_SUB(NOW(), INTERVAL 2 HOUR), NOW(), 1),
(3, 2, 6, '屋顶生活水箱', '液位变化', '80%→75%', NULL, DATE_SUB(NOW(), INTERVAL 1 HOUR), NOW(), 1);

-- =============================================
-- 4. 能耗计量数据
-- =============================================

-- 能源表具
INSERT INTO `ibms_energy_meter` (`meter_code`, `meter_name`, `meter_type`, `area_id`, `area_name`, `floor`, `location`, `status`, `current_reading`, `unit`, `multiplier`, `last_reading_time`, `communication_type`, `install_time`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`) VALUES
('EM-MAIN-01', '总电表', 1, NULL, '配电房', 'B1', '总配电柜', 1, 125680.500, 'kWh', 1, NOW(), 1, '2023-01-01', '全楼总用电计量', '1', NOW(), '1', NOW(), 0, 1),
('EM-F1-01', '一楼电表', 1, 1, '一楼', 'F1', '一楼配电箱', 1, 28560.250, 'kWh', 1, NOW(), 1, '2023-01-01', '一楼用电分表', '1', NOW(), '1', NOW(), 0, 1),
('EM-F2-01', '二楼电表', 1, 2, '二楼', 'F2', '二楼配电箱', 1, 32180.750, 'kWh', 1, NOW(), 1, '2023-01-01', '二楼用电分表', '1', NOW(), '1', NOW(), 0, 1),
('EM-F3-01', '三楼电表', 1, 3, '三楼', 'F3', '三楼配电箱', 1, 29450.300, 'kWh', 1, NOW(), 1, '2023-01-01', '三楼用电分表', '1', NOW(), '1', NOW(), 0, 1),
('EM-AC-01', '空调电表', 1, NULL, '空调系统', 'RF', '空调机房', 1, 45620.800, 'kWh', 1, NOW(), 1, '2023-01-01', '空调系统专用电表', '1', NOW(), '1', NOW(), 0, 1),
('EM-LIGHT-01', '照明电表', 1, NULL, '照明系统', 'B1', '照明配电柜', 1, 18950.200, 'kWh', 1, NOW(), 1, '2023-01-01', '照明系统专用电表', '1', NOW(), '1', NOW(), 0, 1),
('WM-MAIN-01', '总水表', 2, NULL, '给水间', 'B2', '给水总管', 1, 8560.000, 'm³', 1, NOW(), 2, '2023-01-01', '全楼总用水计量', '1', NOW(), '1', NOW(), 0, 1),
('WM-F1-01', '一楼水表', 2, 1, '一楼', 'F1', '一楼给水管', 1, 2150.500, 'm³', 1, NOW(), 2, '2023-01-01', '一楼用水分表', '1', NOW(), '1', NOW(), 0, 1),
('WM-F2-01', '二楼水表', 2, 2, '二楼', 'F2', '二楼给水管', 1, 2580.300, 'm³', 1, NOW(), 2, '2023-01-01', '二楼用水分表', '1', NOW(), '1', NOW(), 0, 1),
('WM-F3-01', '三楼水表', 2, 3, '三楼', 'F3', '三楼给水管', 1, 2320.200, 'm³', 1, NOW(), 2, '2023-01-01', '三楼用水分表', '1', NOW(), '1', NOW(), 0, 1),
('GM-MAIN-01', '总燃气表', 3, NULL, '燃气间', 'B1', '燃气总管', 1, 1520.000, 'm³', 1, NOW(), 3, '2023-06-01', '食堂燃气计量', '1', NOW(), '1', NOW(), 0, 1),
('CM-AC-01', '冷量表', 4, NULL, '冷站', 'B1', '冷冻水总管', 1, 12580.500, 'kWh', 1, NOW(), 1, '2023-06-01', '空调冷量计量', '1', NOW(), '1', NOW(), 0, 1);

-- 能耗记录（最近7天每天的记录）
INSERT INTO `ibms_energy_record` (`meter_id`, `meter_code`, `meter_type`, `reading_value`, `consumption`, `reading_time`, `record_type`, `create_time`, `tenant_id`) VALUES
-- 总电表
(1, 'EM-MAIN-01', 1, 125680.500, 1250.500, NOW(), 1, NOW(), 1),
(1, 'EM-MAIN-01', 1, 124430.000, 1180.200, DATE_SUB(NOW(), INTERVAL 1 DAY), 1, NOW(), 1),
(1, 'EM-MAIN-01', 1, 123249.800, 1320.600, DATE_SUB(NOW(), INTERVAL 2 DAY), 1, NOW(), 1),
(1, 'EM-MAIN-01', 1, 121929.200, 980.500, DATE_SUB(NOW(), INTERVAL 3 DAY), 1, NOW(), 1),
(1, 'EM-MAIN-01', 1, 120948.700, 920.300, DATE_SUB(NOW(), INTERVAL 4 DAY), 1, NOW(), 1),
-- 一楼电表
(2, 'EM-F1-01', 1, 28560.250, 285.250, NOW(), 1, NOW(), 1),
(2, 'EM-F1-01', 1, 28275.000, 268.500, DATE_SUB(NOW(), INTERVAL 1 DAY), 1, NOW(), 1),
(2, 'EM-F1-01', 1, 28006.500, 298.200, DATE_SUB(NOW(), INTERVAL 2 DAY), 1, NOW(), 1),
-- 总水表
(7, 'WM-MAIN-01', 2, 8560.000, 25.500, NOW(), 1, NOW(), 1),
(7, 'WM-MAIN-01', 2, 8534.500, 28.200, DATE_SUB(NOW(), INTERVAL 1 DAY), 1, NOW(), 1),
(7, 'WM-MAIN-01', 2, 8506.300, 22.800, DATE_SUB(NOW(), INTERVAL 2 DAY), 1, NOW(), 1);

-- 能耗日统计（最近30天）
INSERT INTO `ibms_energy_statistics_daily` (`meter_id`, `meter_code`, `meter_type`, `area_id`, `stat_date`, `consumption`, `cost`, `create_time`, `tenant_id`) VALUES
-- 总电表统计
(1, 'EM-MAIN-01', 1, NULL, CURDATE(), 1250.500, 812.83, NOW(), 1),
(1, 'EM-MAIN-01', 1, NULL, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 1180.200, 767.13, NOW(), 1),
(1, 'EM-MAIN-01', 1, NULL, DATE_SUB(CURDATE(), INTERVAL 2 DAY), 1320.600, 858.39, NOW(), 1),
(1, 'EM-MAIN-01', 1, NULL, DATE_SUB(CURDATE(), INTERVAL 3 DAY), 980.500, 637.33, NOW(), 1),
(1, 'EM-MAIN-01', 1, NULL, DATE_SUB(CURDATE(), INTERVAL 4 DAY), 920.300, 598.20, NOW(), 1),
(1, 'EM-MAIN-01', 1, NULL, DATE_SUB(CURDATE(), INTERVAL 5 DAY), 1150.800, 747.52, NOW(), 1),
(1, 'EM-MAIN-01', 1, NULL, DATE_SUB(CURDATE(), INTERVAL 6 DAY), 1280.500, 832.33, NOW(), 1),
-- 一楼电表统计
(2, 'EM-F1-01', 1, 1, CURDATE(), 285.250, 185.41, NOW(), 1),
(2, 'EM-F1-01', 1, 1, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 268.500, 174.53, NOW(), 1),
(2, 'EM-F1-01', 1, 1, DATE_SUB(CURDATE(), INTERVAL 2 DAY), 298.200, 193.83, NOW(), 1),
-- 二楼电表统计
(3, 'EM-F2-01', 1, 2, CURDATE(), 320.180, 208.12, NOW(), 1),
(3, 'EM-F2-01', 1, 2, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 305.600, 198.64, NOW(), 1),
(3, 'EM-F2-01', 1, 2, DATE_SUB(CURDATE(), INTERVAL 2 DAY), 335.500, 218.08, NOW(), 1),
-- 总水表统计
(7, 'WM-MAIN-01', 2, NULL, CURDATE(), 25.500, 127.50, NOW(), 1),
(7, 'WM-MAIN-01', 2, NULL, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 28.200, 141.00, NOW(), 1),
(7, 'WM-MAIN-01', 2, NULL, DATE_SUB(CURDATE(), INTERVAL 2 DAY), 22.800, 114.00, NOW(), 1);

-- 能耗告警
INSERT INTO `ibms_energy_alarm` (`meter_id`, `meter_code`, `meter_name`, `alarm_type`, `alarm_level`, `alarm_content`, `alarm_value`, `threshold_value`, `alarm_time`, `status`, `handler`, `handle_time`, `handle_remark`, `create_time`, `update_time`, `tenant_id`) VALUES
(3, 'EM-F2-01', '二楼电表', 1, 2, '二楼日用电量超过预警值', '335.5', '300', DATE_SUB(NOW(), INTERVAL 2 DAY), 2, '管理员', DATE_SUB(NOW(), INTERVAL 1 DAY), '已通知相关部门节约用电', NOW(), NOW(), 1),
(5, 'EM-AC-01', '空调电表', 2, 2, '空调用电异常波动，较昨日增长35%', '520.8', '400', DATE_SUB(NOW(), INTERVAL 6 HOUR), 1, '李工', NOW(), '检查空调运行情况', NOW(), NOW(), 1),
(7, 'WM-MAIN-01', '总水表', 2, 1, '用水量较上周同期增长15%', '180.5', '160', DATE_SUB(NOW(), INTERVAL 1 DAY), 0, NULL, NULL, NULL, NOW(), NOW(), 1);
