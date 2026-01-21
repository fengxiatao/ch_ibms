-- 视频定时轮巡计划表
CREATE TABLE `iot_video_patrol_schedule` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `name` varchar(100) NOT NULL COMMENT '计划名称',
  `patrol_plan_id` bigint NOT NULL COMMENT '轮巡计划ID',
  `schedule_type` tinyint NOT NULL COMMENT '计划类型：1-日计划 2-周计划',
  `start_time` time NOT NULL COMMENT '开始时间',
  `end_time` time NOT NULL COMMENT '结束时间',
  `week_days` varchar(20) DEFAULT NULL COMMENT '周几执行（周计划）：1,2,3,4,5,6,7 逗号分隔',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0-禁用 1-启用',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `creator` varchar(64) DEFAULT NULL COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
  PRIMARY KEY (`id`),
  KEY `idx_patrol_plan_id` (`patrol_plan_id`),
  KEY `idx_status` (`status`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='视频定时轮巡计划表';

-- 定时轮巡执行记录表
CREATE TABLE `iot_video_patrol_schedule_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `schedule_id` bigint NOT NULL COMMENT '定时计划ID',
  `schedule_name` varchar(100) NOT NULL COMMENT '计划名称',
  `patrol_plan_id` bigint NOT NULL COMMENT '轮巡计划ID',
  `patrol_plan_name` varchar(100) NOT NULL COMMENT '轮巡计划名称',
  `start_time` datetime NOT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `status` tinyint NOT NULL COMMENT '状态：1-执行中 2-已完成 3-异常停止',
  `error_msg` varchar(500) DEFAULT NULL COMMENT '错误信息',
  `creator` varchar(64) DEFAULT NULL COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
  PRIMARY KEY (`id`),
  KEY `idx_schedule_id` (`schedule_id`),
  KEY `idx_start_time` (`start_time`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='定时轮巡执行记录表';
