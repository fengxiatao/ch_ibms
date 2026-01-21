-- =====================================================
-- IoT 视频巡更模块 - 数据库表结构
-- =====================================================

-- ----------------------------
-- 1. 视频巡更点位表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `iot_video_patrol_point` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `point_name` varchar(128) NOT NULL COMMENT '点位名称',
  `point_code` varchar(64) NOT NULL COMMENT '点位编码',
  `camera_id` bigint(20) NOT NULL COMMENT '关联摄像头设备ID',
  `camera_name` varchar(128) DEFAULT NULL COMMENT '摄像头名称（冗余字段）',
  `building_id` bigint(20) DEFAULT NULL COMMENT '所属建筑ID',
  `floor_id` bigint(20) DEFAULT NULL COMMENT '所属楼层ID',
  `area_id` bigint(20) DEFAULT NULL COMMENT '所属区域ID',
  `location` varchar(256) DEFAULT NULL COMMENT '详细位置描述',
  `check_items` json DEFAULT NULL COMMENT '检查项目配置（JSON数组，如：人员到岗、设备状态等）',
  `ai_rules` json DEFAULT NULL COMMENT 'AI智能判断规则配置（JSON格式）',
  `snapshot_duration` int(11) DEFAULT 5 COMMENT '抓拍时长(秒)，用于视频片段录制',
  `description` varchar(512) DEFAULT NULL COMMENT '点位描述',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态(0:停用 1:启用)',
  `sort` int(11) DEFAULT 0 COMMENT '排序',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_point_code` (`point_code`, `deleted`, `tenant_id`),
  KEY `idx_camera_id` (`camera_id`),
  KEY `idx_building_id` (`building_id`),
  KEY `idx_floor_id` (`floor_id`),
  KEY `idx_area_id` (`area_id`),
  KEY `idx_status` (`status`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='IoT视频巡更点位表';

-- ----------------------------
-- 2. 视频巡更任务表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `iot_video_patrol_task` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `task_name` varchar(128) NOT NULL COMMENT '任务名称',
  `task_code` varchar(64) NOT NULL COMMENT '任务编码',
  `point_ids` json NOT NULL COMMENT '巡更点位ID列表（JSON数组，按巡更顺序）',
  `point_count` int(11) DEFAULT 0 COMMENT '点位数量',
  `schedule_type` tinyint(4) NOT NULL DEFAULT 1 COMMENT '排班类型(1:每天 2:工作日 3:周末 4:自定义)',
  `schedule_config` json DEFAULT NULL COMMENT '排班配置（JSON格式，自定义时存储具体日期）',
  `time_slots` json NOT NULL COMMENT '时间段配置（JSON数组，[{"startTime":"08:00","intervalMinutes":60}]）',
  `interval_minutes` int(11) DEFAULT 60 COMMENT '巡更间隔(分钟)',
  `auto_snapshot` tinyint(1) DEFAULT 1 COMMENT '自动抓拍(0:否 1:是)',
  `auto_recording` tinyint(1) DEFAULT 0 COMMENT '自动录像(0:否 1:是)',
  `recording_duration` int(11) DEFAULT 10 COMMENT '录像时长(秒)',
  `ai_analysis` tinyint(1) DEFAULT 1 COMMENT '启用AI分析(0:否 1:是)',
  `alert_on_abnormal` tinyint(1) DEFAULT 1 COMMENT '异常报警(0:否 1:是)',
  `alert_user_ids` json DEFAULT NULL COMMENT '报警接收人ID列表（JSON数组）',
  `start_date` date NOT NULL COMMENT '任务开始日期',
  `end_date` date DEFAULT NULL COMMENT '任务结束日期（NULL表示长期有效）',
  `description` varchar(512) DEFAULT NULL COMMENT '任务描述',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态(0:停用 1:启用 2:已过期)',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_task_code` (`task_code`, `deleted`, `tenant_id`),
  KEY `idx_status` (`status`),
  KEY `idx_start_date` (`start_date`),
  KEY `idx_end_date` (`end_date`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='IoT视频巡更任务表';

-- ----------------------------
-- 3. 视频巡更记录表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `iot_video_patrol_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `task_id` bigint(20) NOT NULL COMMENT '任务ID',
  `point_id` bigint(20) NOT NULL COMMENT '点位ID',
  `point_name` varchar(128) DEFAULT NULL COMMENT '点位名称（冗余字段）',
  `camera_id` bigint(20) NOT NULL COMMENT '摄像头ID',
  `camera_name` varchar(128) DEFAULT NULL COMMENT '摄像头名称（冗余字段）',
  `patrol_time` datetime NOT NULL COMMENT '巡更时间',
  `snapshot_url` varchar(512) DEFAULT NULL COMMENT '抓拍快照URL',
  `video_url` varchar(512) DEFAULT NULL COMMENT '录像片段URL',
  `snapshot_count` int(11) DEFAULT 1 COMMENT '抓拍数量',
  `ai_result` json DEFAULT NULL COMMENT 'AI分析结果（JSON格式，包含各检查项的判断结果）',
  `ai_score` decimal(5, 2) DEFAULT NULL COMMENT 'AI分析评分(0-100)',
  `ai_status` tinyint(4) DEFAULT NULL COMMENT 'AI判断状态(1:正常 2:异常 3:无法判断)',
  `ai_abnormal_items` json DEFAULT NULL COMMENT 'AI检测到的异常项（JSON数组）',
  `manual_confirmed` tinyint(1) DEFAULT 0 COMMENT '人工确认(0:未确认 1:已确认)',
  `manual_confirmer` varchar(64) DEFAULT NULL COMMENT '人工确认人',
  `manual_confirm_time` datetime DEFAULT NULL COMMENT '人工确认时间',
  `manual_status` tinyint(4) DEFAULT NULL COMMENT '人工确认状态(1:正常 2:异常)',
  `manual_remark` varchar(512) DEFAULT NULL COMMENT '人工备注',
  `is_abnormal` tinyint(1) DEFAULT 0 COMMENT '是否异常(0:正常 1:异常)，综合AI和人工判断',
  `abnormal_reason` varchar(512) DEFAULT NULL COMMENT '异常原因',
  `alert_sent` tinyint(1) DEFAULT 0 COMMENT '是否已发送报警(0:否 1:是)',
  `alert_time` datetime DEFAULT NULL COMMENT '报警时间',
  `handled` tinyint(1) DEFAULT 0 COMMENT '是否已处理(0:未处理 1:已处理)',
  `handler` varchar(64) DEFAULT NULL COMMENT '处理人',
  `handle_time` datetime DEFAULT NULL COMMENT '处理时间',
  `handle_remark` varchar(512) DEFAULT NULL COMMENT '处理备注',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`),
  KEY `idx_task_id` (`task_id`),
  KEY `idx_point_id` (`point_id`),
  KEY `idx_camera_id` (`camera_id`),
  KEY `idx_patrol_time` (`patrol_time`),
  KEY `idx_ai_status` (`ai_status`),
  KEY `idx_manual_status` (`manual_status`),
  KEY `idx_is_abnormal` (`is_abnormal`),
  KEY `idx_handled` (`handled`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='IoT视频巡更记录表';

-- ----------------------------
-- 初始化示例数据（可选）
-- ----------------------------

-- 示例视频巡更点位（假设已有摄像头设备ID为1-5）
INSERT INTO `iot_video_patrol_point` (`point_name`, `point_code`, `camera_id`, `camera_name`, `location`, `check_items`, `snapshot_duration`, `description`, `status`, `sort`) VALUES
('前台监控点', 'VPT001', 1, '前台摄像头', '一楼前台', JSON_ARRAY('人员到岗', '环境整洁'), 5, '前台值班人员监控点', 1, 1),
('消防通道监控点', 'VPT002', 2, '消防通道摄像头', '一楼东侧消防通道', JSON_ARRAY('通道畅通', '消防设施完好'), 5, '消防通道巡查监控点', 1, 2),
('停车场出入口', 'VPT003', 3, '停车场摄像头', '地下一层停车场入口', JSON_ARRAY('车辆进出正常', '无违停'), 5, '停车场监控点', 1, 3);

-- 示例视频巡更任务
INSERT INTO `iot_video_patrol_task` (`task_name`, `task_code`, `point_ids`, `point_count`, `schedule_type`, `time_slots`, `interval_minutes`, `auto_snapshot`, `auto_recording`, `recording_duration`, `ai_analysis`, `alert_on_abnormal`, `start_date`, `description`, `status`) VALUES
('前台值班监控', 'VPT001', JSON_ARRAY(1), 1, 1, JSON_ARRAY(JSON_OBJECT('startTime', '08:00', 'intervalMinutes', 60), JSON_OBJECT('startTime', '18:00', 'intervalMinutes', 30)), 60, 1, 0, 10, 1, 1, CURDATE(), '前台值班人员到岗监控', 1),
('消防安全巡查', 'VPT002', JSON_ARRAY(2), 1, 1, JSON_ARRAY(JSON_OBJECT('startTime', '09:00', 'intervalMinutes', 120)), 120, 1, 1, 10, 1, 1, CURDATE(), '消防通道安全巡查', 1),
('全区域巡查', 'VPT003', JSON_ARRAY(1, 2, 3), 3, 1, JSON_ARRAY(JSON_OBJECT('startTime', '00:00', 'intervalMinutes', 240)), 240, 1, 0, 10, 1, 1, CURDATE(), '全区域定时巡查', 1);






