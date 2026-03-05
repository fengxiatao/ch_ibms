-- =====================================================
-- 电子巡更模块 - 表结构创建脚本
-- 目标数据库：192.168.1.126 ch_ibms
-- 生成时间：2026-01-27
-- =====================================================

-- 注意：如果表已存在会跳过创建
-- 先执行此脚本创建表，再执行 epatrol_sync_data.sql 同步数据

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =====================================================
-- 1. 巡更人员表
-- =====================================================
CREATE TABLE IF NOT EXISTS `iot_epatrol_person` (
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

-- =====================================================
-- 2. 巡更点位表
-- =====================================================
CREATE TABLE IF NOT EXISTS `iot_epatrol_point` (
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

-- =====================================================
-- 3. 巡更路线表
-- =====================================================
CREATE TABLE IF NOT EXISTS `iot_epatrol_route` (
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

-- =====================================================
-- 4. 路线-点位关联表
-- =====================================================
CREATE TABLE IF NOT EXISTS `iot_epatrol_route_point` (
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

-- =====================================================
-- 5. 巡更计划表
-- =====================================================
CREATE TABLE IF NOT EXISTS `iot_epatrol_plan` (
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

-- =====================================================
-- 6. 计划时段表
-- =====================================================
CREATE TABLE IF NOT EXISTS `iot_epatrol_plan_period` (
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

-- =====================================================
-- 7. 巡更任务表
-- =====================================================
CREATE TABLE IF NOT EXISTS `iot_epatrol_task` (
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

-- =====================================================
-- 8. 任务打卡记录表
-- =====================================================
CREATE TABLE IF NOT EXISTS `iot_epatrol_task_record` (
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

SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================
-- 执行完成提示
-- =====================================================
SELECT '电子巡更模块表结构创建完成！' as message;
