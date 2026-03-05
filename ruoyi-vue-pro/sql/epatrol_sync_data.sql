-- =====================================================
-- 电子巡更模块 - 数据同步脚本
-- 目标数据库：192.168.1.126 ch_ibms
-- 生成时间：2026-01-27
-- =====================================================

-- 注意：执行前请先备份目标数据库！
-- 本脚本会先清空旧数据再插入新数据

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =====================================================
-- 1. 巡更人员表
-- =====================================================
DELETE FROM iot_epatrol_person WHERE deleted = 0;

INSERT INTO iot_epatrol_person (id, name, phone, patrol_stick_no, person_card_no, status, remark, creator, create_time, updater, update_time, deleted, tenant_id) VALUES
(1, 'A君', '13812345678', 'PT123456789', 'MG147258', 1, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1),
(2, 'B君', '18087654321', 'PT121314151', 'MG123456', 1, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1),
(3, 'C君', '13914725899', 'PT191817161', 'MG125809', 1, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1);

-- =====================================================
-- 2. 巡更点位表
-- =====================================================
DELETE FROM iot_epatrol_point WHERE deleted = 0;

INSERT INTO iot_epatrol_point (id, point_no, point_name, point_location, status, remark, creator, create_time, updater, update_time, deleted, tenant_id) VALUES
(1, 'XD20263331867', '园区大门口', '园区主入口', 1, NULL, 'admin', '2026-01-08 10:00:00', 'admin', '2026-01-08 10:00:00', 0, 1),
(2, 'XD183688881888', 'A栋地下室机房', 'A栋地下室', 1, NULL, 'admin', '2026-01-08 10:00:00', 'admin', '2026-01-08 10:00:00', 0, 1),
(3, 'XD13593369861', 'B栋主入口', 'B栋主入口', 1, NULL, 'admin', '2026-01-10 10:00:00', 'admin', '2026-01-10 10:00:00', 0, 1),
(4, 'XD20261234001', 'A栋主入口', 'A栋大门口', 1, NULL, 'admin', '2026-01-08 10:00:00', 'admin', '2026-01-08 10:00:00', 0, 1),
(5, 'XD20261234002', 'A栋1楼', 'A栋1楼大厅', 1, NULL, 'admin', '2026-01-08 10:00:00', 'admin', '2026-01-08 10:00:00', 0, 1),
(6, 'XD20261234003', 'A栋2楼', 'A栋2楼走廊', 1, NULL, 'admin', '2026-01-08 10:00:00', 'admin', '2026-01-08 10:00:00', 0, 1),
(7, 'XD20261234004', 'A栋3楼', 'A栋3楼走廊', 1, NULL, 'admin', '2026-01-08 10:00:00', 'admin', '2026-01-08 10:00:00', 0, 1),
(8, 'XD20261234005', 'A栋4楼', 'A栋4楼走廊', 1, NULL, 'admin', '2026-01-08 10:00:00', 'admin', '2026-01-08 10:00:00', 0, 1),
(9, 'XD20261234006', 'A栋天面', 'A栋天面', 1, NULL, 'admin', '2026-01-08 10:00:00', 'admin', '2026-01-08 10:00:00', 0, 1),
(10, 'XD20261234007', 'B栋天面', 'B栋天面', 1, NULL, 'admin', '2026-01-08 10:00:00', 'admin', '2026-01-08 10:00:00', 0, 1),
(11, 'XD20261234008', 'C栋主入口', 'C栋大门口', 1, NULL, 'admin', '2026-01-08 10:00:00', 'admin', '2026-01-08 10:00:00', 0, 1),
(12, 'XD20261234009', '园区后门', '园区后门', 1, NULL, 'admin', '2026-01-08 10:00:00', 'admin', '2026-01-08 10:00:00', 0, 1),
(13, 'XD20261234010', 'B栋机房', 'B栋机房', 1, NULL, 'admin', '2026-01-08 10:00:00', 'admin', '2026-01-08 10:00:00', 0, 1),
(14, 'XD20261234011', 'B栋财务室门口', 'B栋财务室门口', 1, NULL, 'admin', '2026-01-08 10:00:00', 'admin', '2026-01-08 10:00:00', 0, 1),
(15, 'XD20261234012', '物资仓库', '物资仓库', 1, NULL, 'admin', '2026-01-08 10:00:00', 'admin', '2026-01-08 10:00:00', 0, 1);

-- =====================================================
-- 3. 巡更路线表
-- =====================================================
DELETE FROM iot_epatrol_route WHERE deleted = 0;

INSERT INTO iot_epatrol_route (id, route_name, point_count, total_duration, status, remark, creator, create_time, updater, update_time, deleted, tenant_id) VALUES
(1, '出入口路线', 5, 60, 1, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1),
(2, 'A栋巡更路线', 6, 30, 1, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1),
(3, 'B栋巡更路线', 5, 30, 1, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1);

-- =====================================================
-- 4. 路线-点位关联表
-- =====================================================
DELETE FROM iot_epatrol_route_point WHERE deleted = 0;

INSERT INTO iot_epatrol_route_point (id, route_id, point_id, sort, interval_minutes, creator, create_time, updater, update_time, deleted, tenant_id) VALUES
-- 出入口路线：园区大门口 -> A栋主入口 -> B栋主入口 -> C栋主入口 -> 园区后门
(14, 1, 1, 1, 10, 'admin', NOW(), 'admin', NOW(), 0, 1),
(15, 1, 4, 2, 10, 'admin', NOW(), 'admin', NOW(), 0, 1),
(16, 1, 3, 3, 10, 'admin', NOW(), 'admin', NOW(), 0, 1),
(17, 1, 11, 4, 10, 'admin', NOW(), 'admin', NOW(), 0, 1),
(18, 1, 12, 5, 10, 'admin', NOW(), 'admin', NOW(), 0, 1),
-- A栋巡更路线：A栋主入口 -> A栋1楼 -> A栋2楼 -> A栋3楼 -> A栋4楼 -> A栋天面
(19, 2, 4, 1, 5, 'admin', NOW(), 'admin', NOW(), 0, 1),
(20, 2, 5, 2, 5, 'admin', NOW(), 'admin', NOW(), 0, 1),
(21, 2, 6, 3, 5, 'admin', NOW(), 'admin', NOW(), 0, 1),
(22, 2, 7, 4, 5, 'admin', NOW(), 'admin', NOW(), 0, 1),
(23, 2, 8, 5, 5, 'admin', NOW(), 'admin', NOW(), 0, 1),
(24, 2, 9, 6, 5, 'admin', NOW(), 'admin', NOW(), 0, 1),
-- B栋巡更路线：B栋主入口 -> B栋机房 -> B栋财务室门口 -> B栋天面
(25, 3, 3, 1, 8, 'admin', NOW(), 'admin', NOW(), 0, 1),
(26, 3, 13, 2, 8, 'admin', NOW(), 'admin', NOW(), 0, 1),
(27, 3, 14, 3, 8, 'admin', NOW(), 'admin', NOW(), 0, 1),
(28, 3, 10, 4, 8, 'admin', NOW(), 'admin', NOW(), 0, 1);

-- =====================================================
-- 5. 巡更计划表
-- =====================================================
DELETE FROM iot_epatrol_plan WHERE deleted = 0;

INSERT INTO iot_epatrol_plan (id, plan_code, plan_name, route_id, start_date, end_date, weekdays, status, remark, creator, create_time, updater, update_time, deleted, tenant_id) VALUES
(1, 'PL202512280001', '日常巡更01', 1, '2025-12-29', '2025-12-29', '[1, 2, 3, 4, 5]', 2, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1),
(2, 'PL202601100001', '夜间巡更02', 1, '2026-01-11', '2026-01-15', '[1, 2, 3, 4, 5]', 1, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1),
(3, 'PL202601100002', '日间巡更02', 1, '2026-01-11', '2026-01-20', '[1, 2, 3, 4, 5]', 1, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1),
(4, 'PL202601120001', '日间巡更01', 1, '2026-01-21', '2026-01-31', '[1, 2, 3, 4, 5]', 0, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1);

-- =====================================================
-- 6. 计划时段表
-- =====================================================
DELETE FROM iot_epatrol_plan_period WHERE deleted = 0;

INSERT INTO iot_epatrol_plan_period (id, plan_id, route_id, start_time, duration_minutes, person_ids, creator, create_time, updater, update_time, deleted, tenant_id) VALUES
(1, 1, 1, '02:00:00', 60, '[1, 2]', 'admin', NOW(), 'admin', NOW(), 0, 1),
(2, 2, 1, '02:00:00', 60, '[3]', 'admin', NOW(), 'admin', NOW(), 0, 1),
(3, 2, 2, '08:00:00', 30, '[1]', 'admin', NOW(), 'admin', NOW(), 0, 1),
(4, 2, 2, '14:00:00', 60, '[2]', 'admin', NOW(), 'admin', NOW(), 0, 1),
(5, 2, 1, '19:00:00', 60, '[4]', 'admin', NOW(), 'admin', NOW(), 0, 1),
(6, 3, 1, '14:00:00', 60, '[2]', 'admin', NOW(), 'admin', NOW(), 0, 1),
(7, 4, 1, '19:00:00', 60, '[3]', 'admin', NOW(), 'admin', NOW(), 0, 1);

-- =====================================================
-- 7. 巡更任务表
-- =====================================================
DELETE FROM iot_epatrol_task WHERE deleted = 0;

INSERT INTO iot_epatrol_task (id, task_code, plan_id, period_id, route_id, task_date, planned_start_time, planned_end_time, person_ids, status, submit_time, submitter_id, remark, creator, create_time, updater, update_time, deleted, tenant_id) VALUES
(1, 'TA202512290001', 1, 1, 1, '2025-12-29', '2025-12-29 02:00:00', '2025-12-29 03:00:00', '[1]', 1, '2025-12-29 03:05:00', NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1),
(2, 'TA202601110001', 2, 3, 2, '2026-01-11', '2026-01-11 08:00:00', '2026-01-11 08:30:00', '[1]', 1, '2026-01-11 08:35:00', NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1),
(3, 'TA202601110002', 3, 6, 1, '2026-01-11', '2026-01-11 14:00:00', '2026-01-11 15:00:00', '[2]', 1, '2026-01-11 15:05:00', NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1),
(4, 'TA202601121001', 4, 7, 1, '2026-01-12', '2026-01-12 19:00:00', '2026-01-12 20:00:00', '[3]', 0, NULL, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1);

-- =====================================================
-- 8. 任务巡更记录表
-- =====================================================
DELETE FROM iot_epatrol_task_record WHERE deleted = 0;

INSERT INTO iot_epatrol_task_record (id, task_id, point_id, point_no, point_name, person_id, person_name, expected_sort, actual_sort, planned_time, actual_time, patrol_status, time_diff_seconds, remark, creator, create_time, updater, update_time, deleted, tenant_id) VALUES
-- 任务1的记录（部分完成）
(16, 1, 1, 'XD20263331867', '园区大门口', 1, 'A君', 1, 1, '2025-12-29 02:00:00', '2025-12-29 02:02:00', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1),
(17, 1, 4, 'XD20261234001', 'A栋主入口', 1, 'A君', 2, 2, '2025-12-29 02:10:00', '2025-12-29 02:12:00', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1),
(18, 1, 3, 'XD13593369861', 'B栋主入口', 1, 'A君', 3, NULL, '2025-12-29 02:20:00', NULL, 4, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1),
(19, 1, 11, 'XD20261234008', 'C栋主入口', 1, 'A君', 4, 3, '2025-12-29 02:30:00', '2025-12-29 02:35:00', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1),
(20, 1, 15, 'XD20261234012', '物资仓库', 1, 'A君', 5, NULL, '2025-12-29 02:40:00', NULL, 4, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1),
-- 任务2的记录（全部完成）
(21, 2, 4, 'XD20261234001', 'A栋主入口', 1, 'A君', 1, 1, '2026-01-11 08:00:00', '2026-01-11 08:02:00', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1),
(22, 2, 5, 'XD20261234002', 'A栋1楼', 1, 'A君', 2, 2, '2026-01-11 08:05:00', '2026-01-11 08:07:00', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1),
(23, 2, 6, 'XD20261234003', 'A栋2楼', 1, 'A君', 3, 3, '2026-01-11 08:10:00', '2026-01-11 08:12:00', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1),
(24, 2, 7, 'XD20261234004', 'A栋3楼', 1, 'A君', 4, 4, '2026-01-11 08:15:00', '2026-01-11 08:18:00', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1),
(25, 2, 8, 'XD20261234005', 'A栋4楼', 1, 'A君', 5, 5, '2026-01-11 08:20:00', '2026-01-11 08:23:00', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1),
(26, 2, 9, 'XD20261234006', 'A栋天面', 1, 'A君', 6, 6, '2026-01-11 08:25:00', '2026-01-11 08:28:00', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1),
-- 任务3的记录（全部完成）
(27, 3, 1, 'XD20263331867', '园区大门口', 2, 'B君', 1, 1, '2026-01-11 14:00:00', '2026-01-11 14:05:00', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1),
(28, 3, 4, 'XD20261234001', 'A栋主入口', 2, 'B君', 2, 2, '2026-01-11 14:10:00', '2026-01-11 14:15:00', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1),
(29, 3, 3, 'XD13593369861', 'B栋主入口', 2, 'B君', 3, 3, '2026-01-11 14:20:00', '2026-01-11 14:25:00', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1),
(30, 3, 11, 'XD20261234008', 'C栋主入口', 2, 'B君', 4, 4, '2026-01-11 14:30:00', '2026-01-11 14:35:00', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1),
(31, 3, 12, 'XD20261234009', '园区后门', 2, 'B君', 5, 5, '2026-01-11 14:40:00', '2026-01-11 14:45:00', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1);

-- =====================================================
-- 9. 菜单数据（导入巡更棒数据）
-- =====================================================
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT 7119, '导入巡更棒数据', '', 2, 6, 7100, 'stick-data', 'ep:upload', 'security/EPatrol/StickData/index', 'EPatrolStickData', 0, 1, 1, 1, 'admin', NOW(), 'admin', NOW(), 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM system_menu WHERE id = 7119);

SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================
-- 执行完成提示
-- =====================================================
SELECT '电子巡更模块数据同步完成！' as message;
