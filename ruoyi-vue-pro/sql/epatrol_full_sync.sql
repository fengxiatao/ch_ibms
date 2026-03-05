-- =====================================================
-- 电子巡更模块 - 完整同步脚本（表结构+数据）
-- 目标数据库：192.168.1.126 ch_ibms
-- 生成时间：2026-01-27
-- =====================================================

-- 注意：此脚本会删除并重建所有巡更相关表！
-- 执行前请先备份目标数据库！

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =====================================================
-- 第一步：删除旧表（按依赖顺序）
-- =====================================================
DROP TABLE IF EXISTS `iot_epatrol_task_record`;
DROP TABLE IF EXISTS `iot_epatrol_task`;
DROP TABLE IF EXISTS `iot_epatrol_plan_period`;
DROP TABLE IF EXISTS `iot_epatrol_plan`;
DROP TABLE IF EXISTS `iot_epatrol_route_point`;
DROP TABLE IF EXISTS `iot_epatrol_route`;
DROP TABLE IF EXISTS `iot_epatrol_point`;
DROP TABLE IF EXISTS `iot_epatrol_person`;

-- =====================================================
-- 第二步：创建表结构
-- =====================================================

-- 1. 巡更人员表
CREATE TABLE `iot_epatrol_person` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '人员姓名',
  `phone` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '联系电话',
  `patrol_stick_no` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '巡更棒编号（硬件编号）',
  `person_card_no` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '人员卡编号（硬件编号）',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
  `remark` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `creator` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户编号',
  PRIMARY KEY (`id`),
  KEY `idx_phone` (`phone`),
  KEY `idx_patrol_stick_no` (`patrol_stick_no`),
  KEY `idx_person_card_no` (`person_card_no`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='电子巡更-巡更人员表';

-- 2. 巡更点位表
CREATE TABLE `iot_epatrol_point` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `point_no` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '点位编号（硬件编号）',
  `point_name` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '点位名称',
  `point_location` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '点位位置',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
  `remark` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `creator` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户编号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_point_no_tenant` (`point_no`,`tenant_id`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='电子巡更-巡更点表';

-- 3. 巡更路线表
CREATE TABLE `iot_epatrol_route` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `route_name` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '路线名称',
  `point_count` int NOT NULL DEFAULT '0' COMMENT '包含巡更点数量',
  `total_duration` int NOT NULL DEFAULT '0' COMMENT '路线总耗时（分钟）',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
  `remark` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `creator` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户编号',
  PRIMARY KEY (`id`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='电子巡更-巡更路线表';

-- 4. 路线-点位关联表
CREATE TABLE `iot_epatrol_route_point` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `route_id` bigint NOT NULL COMMENT '路线ID',
  `point_id` bigint NOT NULL COMMENT '点位ID',
  `sort` int NOT NULL DEFAULT '0' COMMENT '顺序（从1开始）',
  `interval_minutes` int NOT NULL DEFAULT '0' COMMENT '到下一个点位的间隔时间（分钟）',
  `creator` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户编号',
  PRIMARY KEY (`id`),
  KEY `idx_route_id` (`route_id`),
  KEY `idx_point_id` (`point_id`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='电子巡更-路线点位关联表';

-- 5. 巡更计划表
CREATE TABLE `iot_epatrol_plan` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `plan_code` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '计划编号',
  `plan_name` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '计划名称',
  `route_id` bigint NOT NULL COMMENT '巡更路线ID',
  `start_date` date NOT NULL COMMENT '开始日期',
  `end_date` date DEFAULT NULL COMMENT '结束日期',
  `weekdays` json NOT NULL COMMENT '星期选择（JSON数组，如[1,2,3,4,5]，1=周一）',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '计划状态：0-未开始，1-执行中，2-已过期',
  `remark` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `creator` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户编号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_plan_code_tenant` (`plan_code`,`tenant_id`),
  KEY `idx_route_id` (`route_id`),
  KEY `idx_start_date` (`start_date`),
  KEY `idx_end_date` (`end_date`),
  KEY `idx_status` (`status`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='电子巡更-巡更计划表';

-- 6. 计划时段表
CREATE TABLE `iot_epatrol_plan_period` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `plan_id` bigint NOT NULL COMMENT '计划ID',
  `route_id` bigint DEFAULT NULL COMMENT '路线ID（可单独设置）',
  `start_time` time NOT NULL COMMENT '开始时间',
  `duration_minutes` int NOT NULL COMMENT '巡更时长（分钟，应大于路线总用时）',
  `person_ids` json NOT NULL COMMENT '巡更人员ID数组',
  `creator` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户编号',
  PRIMARY KEY (`id`),
  KEY `idx_plan_id` (`plan_id`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='电子巡更-计划时段表';

-- 7. 巡更任务表
CREATE TABLE `iot_epatrol_task` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `task_code` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '任务编号',
  `plan_id` bigint NOT NULL COMMENT '关联计划ID',
  `period_id` bigint NOT NULL COMMENT '关联时段ID',
  `route_id` bigint NOT NULL COMMENT '巡更路线ID',
  `task_date` date NOT NULL COMMENT '任务日期',
  `planned_start_time` datetime NOT NULL COMMENT '计划开始时间',
  `planned_end_time` datetime NOT NULL COMMENT '计划结束时间',
  `person_ids` json NOT NULL COMMENT '巡更人员ID数组',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '任务状态：0-未巡，1-已巡',
  `submit_time` datetime DEFAULT NULL COMMENT '提交时间',
  `submitter_id` bigint DEFAULT NULL COMMENT '提交人ID',
  `remark` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `creator` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户编号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_task_code_tenant` (`task_code`,`tenant_id`),
  KEY `idx_plan_id` (`plan_id`),
  KEY `idx_period_id` (`period_id`),
  KEY `idx_route_id` (`route_id`),
  KEY `idx_task_date` (`task_date`),
  KEY `idx_status` (`status`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='电子巡更-巡更任务表';

-- 8. 任务打卡记录表
CREATE TABLE `iot_epatrol_task_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `task_id` bigint NOT NULL COMMENT '任务ID',
  `point_id` bigint NOT NULL COMMENT '点位ID',
  `point_no` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '点位编号',
  `point_name` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '点位名称',
  `person_id` bigint DEFAULT NULL COMMENT '巡更人员ID',
  `person_name` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '巡更人员姓名',
  `expected_sort` int DEFAULT NULL COMMENT '预期顺序',
  `actual_sort` int DEFAULT NULL COMMENT '实际顺序',
  `planned_time` datetime DEFAULT NULL COMMENT '计划到达时间',
  `actual_time` datetime DEFAULT NULL COMMENT '实际到达时间',
  `patrol_status` tinyint DEFAULT NULL COMMENT '巡更状态：1-准时，2-早到，3-晚到，4-未到，5-顺序错',
  `time_diff_seconds` int DEFAULT NULL COMMENT '时间差（秒，正数为晚到，负数为早到）',
  `remark` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `creator` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户编号',
  PRIMARY KEY (`id`),
  KEY `idx_task_id` (`task_id`),
  KEY `idx_point_id` (`point_id`),
  KEY `idx_person_id` (`person_id`),
  KEY `idx_patrol_status` (`patrol_status`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='电子巡更-任务打卡记录表';

-- =====================================================
-- 第三步：插入数据
-- =====================================================

-- 1. 巡更人员数据
INSERT INTO iot_epatrol_person (id, name, phone, patrol_stick_no, person_card_no, status, remark, creator, create_time, updater, update_time, deleted, tenant_id) VALUES
(1, 'A君', '13812345678', 'PT123456789', 'MG147258', 1, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1),
(2, 'B君', '18087654321', 'PT121314151', 'MG123456', 1, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1),
(3, 'C君', '13914725899', 'PT191817161', 'MG125809', 1, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1);

-- 2. 巡更点位数据
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

-- 3. 巡更路线数据
INSERT INTO iot_epatrol_route (id, route_name, point_count, total_duration, status, remark, creator, create_time, updater, update_time, deleted, tenant_id) VALUES
(1, '出入口路线', 5, 60, 1, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1),
(2, 'A栋巡更路线', 6, 30, 1, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1),
(3, 'B栋巡更路线', 5, 30, 1, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1);

-- 4. 路线-点位关联数据
INSERT INTO iot_epatrol_route_point (id, route_id, point_id, sort, interval_minutes, creator, create_time, updater, update_time, deleted, tenant_id) VALUES
(14, 1, 1, 1, 10, 'admin', NOW(), 'admin', NOW(), 0, 1),
(15, 1, 4, 2, 10, 'admin', NOW(), 'admin', NOW(), 0, 1),
(16, 1, 3, 3, 10, 'admin', NOW(), 'admin', NOW(), 0, 1),
(17, 1, 11, 4, 10, 'admin', NOW(), 'admin', NOW(), 0, 1),
(18, 1, 12, 5, 10, 'admin', NOW(), 'admin', NOW(), 0, 1),
(19, 2, 4, 1, 5, 'admin', NOW(), 'admin', NOW(), 0, 1),
(20, 2, 5, 2, 5, 'admin', NOW(), 'admin', NOW(), 0, 1),
(21, 2, 6, 3, 5, 'admin', NOW(), 'admin', NOW(), 0, 1),
(22, 2, 7, 4, 5, 'admin', NOW(), 'admin', NOW(), 0, 1),
(23, 2, 8, 5, 5, 'admin', NOW(), 'admin', NOW(), 0, 1),
(24, 2, 9, 6, 5, 'admin', NOW(), 'admin', NOW(), 0, 1),
(25, 3, 3, 1, 8, 'admin', NOW(), 'admin', NOW(), 0, 1),
(26, 3, 13, 2, 8, 'admin', NOW(), 'admin', NOW(), 0, 1),
(27, 3, 14, 3, 8, 'admin', NOW(), 'admin', NOW(), 0, 1),
(28, 3, 10, 4, 8, 'admin', NOW(), 'admin', NOW(), 0, 1);

-- 5. 巡更计划数据
INSERT INTO iot_epatrol_plan (id, plan_code, plan_name, route_id, start_date, end_date, weekdays, status, remark, creator, create_time, updater, update_time, deleted, tenant_id) VALUES
(1, 'PL202512280001', '日常巡更01', 1, '2025-12-29', '2025-12-29', '[1, 2, 3, 4, 5]', 2, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1),
(2, 'PL202601100001', '夜间巡更02', 1, '2026-01-11', '2026-01-15', '[1, 2, 3, 4, 5]', 1, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1),
(3, 'PL202601100002', '日间巡更02', 1, '2026-01-11', '2026-01-20', '[1, 2, 3, 4, 5]', 1, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1),
(4, 'PL202601120001', '日间巡更01', 1, '2026-01-21', '2026-01-31', '[1, 2, 3, 4, 5]', 0, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1);

-- 6. 计划时段数据
INSERT INTO iot_epatrol_plan_period (id, plan_id, route_id, start_time, duration_minutes, person_ids, creator, create_time, updater, update_time, deleted, tenant_id) VALUES
(1, 1, 1, '02:00:00', 60, '[1, 2]', 'admin', NOW(), 'admin', NOW(), 0, 1),
(2, 2, 1, '02:00:00', 60, '[3]', 'admin', NOW(), 'admin', NOW(), 0, 1),
(3, 2, 2, '08:00:00', 30, '[1]', 'admin', NOW(), 'admin', NOW(), 0, 1),
(4, 2, 2, '14:00:00', 60, '[2]', 'admin', NOW(), 'admin', NOW(), 0, 1),
(5, 2, 1, '19:00:00', 60, '[4]', 'admin', NOW(), 'admin', NOW(), 0, 1),
(6, 3, 1, '14:00:00', 60, '[2]', 'admin', NOW(), 'admin', NOW(), 0, 1),
(7, 4, 1, '19:00:00', 60, '[3]', 'admin', NOW(), 'admin', NOW(), 0, 1);

-- 7. 巡更任务数据
INSERT INTO iot_epatrol_task (id, task_code, plan_id, period_id, route_id, task_date, planned_start_time, planned_end_time, person_ids, status, submit_time, submitter_id, remark, creator, create_time, updater, update_time, deleted, tenant_id) VALUES
(1, 'TA202512290001', 1, 1, 1, '2025-12-29', '2025-12-29 02:00:00', '2025-12-29 03:00:00', '[1]', 1, '2025-12-29 03:05:00', NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1),
(2, 'TA202601110001', 2, 3, 2, '2026-01-11', '2026-01-11 08:00:00', '2026-01-11 08:30:00', '[1]', 1, '2026-01-11 08:35:00', NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1),
(3, 'TA202601110002', 3, 6, 1, '2026-01-11', '2026-01-11 14:00:00', '2026-01-11 15:00:00', '[2]', 1, '2026-01-11 15:05:00', NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1),
(4, 'TA202601121001', 4, 7, 1, '2026-01-12', '2026-01-12 19:00:00', '2026-01-12 20:00:00', '[3]', 0, NULL, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1);

-- 8. 任务打卡记录数据
INSERT INTO iot_epatrol_task_record (id, task_id, point_id, point_no, point_name, person_id, person_name, expected_sort, actual_sort, planned_time, actual_time, patrol_status, time_diff_seconds, remark, creator, create_time, updater, update_time, deleted, tenant_id) VALUES
(16, 1, 1, 'XD20263331867', '园区大门口', 1, 'A君', 1, 1, '2025-12-29 02:00:00', '2025-12-29 02:02:00', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1),
(17, 1, 4, 'XD20261234001', 'A栋主入口', 1, 'A君', 2, 2, '2025-12-29 02:10:00', '2025-12-29 02:12:00', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1),
(18, 1, 3, 'XD13593369861', 'B栋主入口', 1, 'A君', 3, NULL, '2025-12-29 02:20:00', NULL, 4, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1),
(19, 1, 11, 'XD20261234008', 'C栋主入口', 1, 'A君', 4, 3, '2025-12-29 02:30:00', '2025-12-29 02:35:00', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1),
(20, 1, 15, 'XD20261234012', '物资仓库', 1, 'A君', 5, NULL, '2025-12-29 02:40:00', NULL, 4, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1),
(21, 2, 4, 'XD20261234001', 'A栋主入口', 1, 'A君', 1, 1, '2026-01-11 08:00:00', '2026-01-11 08:02:00', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1),
(22, 2, 5, 'XD20261234002', 'A栋1楼', 1, 'A君', 2, 2, '2026-01-11 08:05:00', '2026-01-11 08:07:00', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1),
(23, 2, 6, 'XD20261234003', 'A栋2楼', 1, 'A君', 3, 3, '2026-01-11 08:10:00', '2026-01-11 08:12:00', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1),
(24, 2, 7, 'XD20261234004', 'A栋3楼', 1, 'A君', 4, 4, '2026-01-11 08:15:00', '2026-01-11 08:18:00', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1),
(25, 2, 8, 'XD20261234005', 'A栋4楼', 1, 'A君', 5, 5, '2026-01-11 08:20:00', '2026-01-11 08:23:00', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1),
(26, 2, 9, 'XD20261234006', 'A栋天面', 1, 'A君', 6, 6, '2026-01-11 08:25:00', '2026-01-11 08:28:00', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1),
(27, 3, 1, 'XD20263331867', '园区大门口', 2, 'B君', 1, 1, '2026-01-11 14:00:00', '2026-01-11 14:05:00', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1),
(28, 3, 4, 'XD20261234001', 'A栋主入口', 2, 'B君', 2, 2, '2026-01-11 14:10:00', '2026-01-11 14:15:00', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1),
(29, 3, 3, 'XD13593369861', 'B栋主入口', 2, 'B君', 3, 3, '2026-01-11 14:20:00', '2026-01-11 14:25:00', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1),
(30, 3, 11, 'XD20261234008', 'C栋主入口', 2, 'B君', 4, 4, '2026-01-11 14:30:00', '2026-01-11 14:35:00', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1),
(31, 3, 12, 'XD20261234009', '园区后门', 2, 'B君', 5, 5, '2026-01-11 14:40:00', '2026-01-11 14:45:00', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1);

SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================
-- 执行完成提示
-- =====================================================
SELECT '电子巡更模块完整同步完成！' as message;
