-- =============================================
-- 智慧安防模块数据表
-- 创建时间: 2025-10-24
-- 说明: 包含人员库、人脸识别、车辆管理、视频分析等功能
-- =============================================

-- ----------------------------
-- 1. 人员库表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `security_personnel_library` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `person_name` varchar(100) NOT NULL COMMENT '姓名',
  `person_no` varchar(50) DEFAULT NULL COMMENT '人员编号',
  `id_card` varchar(20) DEFAULT NULL COMMENT '身份证号',
  `gender` tinyint DEFAULT NULL COMMENT '性别（1男 2女）',
  `age` int DEFAULT NULL COMMENT '年龄',
  `phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `department` varchar(100) DEFAULT NULL COMMENT '部门',
  `position` varchar(100) DEFAULT NULL COMMENT '职位',
  `person_type` varchar(50) NOT NULL DEFAULT 'employee' COMMENT '人员类型（employee员工 visitor访客 blacklist黑名单 vip重点人员）',
  `face_photo_url` varchar(500) DEFAULT NULL COMMENT '人脸照片URL',
  `face_feature` text COMMENT '人脸特征值（算法提取）',
  `is_controlled` tinyint DEFAULT 0 COMMENT '是否布控（0否 1是）',
  `control_level` varchar(20) DEFAULT NULL COMMENT '布控级别（low低 medium中 high高 urgent紧急）',
  `control_start_time` datetime DEFAULT NULL COMMENT '布控开始时间',
  `control_end_time` datetime DEFAULT NULL COMMENT '布控结束时间',
  `control_reason` varchar(500) DEFAULT NULL COMMENT '布控原因',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（0禁用 1启用）',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
  PRIMARY KEY (`id`),
  KEY `idx_person_no` (`person_no`),
  KEY `idx_id_card` (`id_card`),
  KEY `idx_person_type` (`person_type`),
  KEY `idx_is_controlled` (`is_controlled`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='安防-人员库';

-- ----------------------------
-- 2. 人脸抓拍记录表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `security_face_capture` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `capture_no` varchar(50) NOT NULL COMMENT '抓拍编号',
  `device_id` bigint NOT NULL COMMENT '设备ID（摄像头）',
  `device_name` varchar(100) DEFAULT NULL COMMENT '设备名称',
  `camera_id` bigint DEFAULT NULL COMMENT '摄像头ID（iot_camera表）',
  `capture_time` datetime NOT NULL COMMENT '抓拍时间',
  `capture_location` varchar(200) DEFAULT NULL COMMENT '抓拍地点',
  `area_id` bigint DEFAULT NULL COMMENT '区域ID',
  `face_image_url` varchar(500) NOT NULL COMMENT '人脸图片URL',
  `scene_image_url` varchar(500) DEFAULT NULL COMMENT '场景图片URL',
  `face_quality` int DEFAULT NULL COMMENT '人脸质量评分（0-100）',
  `face_rect` varchar(100) DEFAULT NULL COMMENT '人脸坐标（x,y,width,height）',
  `face_feature` text COMMENT '人脸特征值',
  `age_range` varchar(20) DEFAULT NULL COMMENT '年龄段',
  `gender` tinyint DEFAULT NULL COMMENT '性别（1男 2女）',
  `glasses` tinyint DEFAULT NULL COMMENT '眼镜（0无 1有）',
  `mask` tinyint DEFAULT NULL COMMENT '口罩（0无 1有）',
  `hat` tinyint DEFAULT NULL COMMENT '帽子（0无 1有）',
  `is_recognized` tinyint DEFAULT 0 COMMENT '是否已识别（0否 1是）',
  `person_id` bigint DEFAULT NULL COMMENT '识别人员ID',
  `person_name` varchar(100) DEFAULT NULL COMMENT '识别人员姓名',
  `similarity` decimal(5,2) DEFAULT NULL COMMENT '相似度（0-100）',
  `is_alarm` tinyint DEFAULT 0 COMMENT '是否告警（0否 1是）',
  `alarm_type` varchar(50) DEFAULT NULL COMMENT '告警类型',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_capture_no` (`capture_no`),
  KEY `idx_device_id` (`device_id`),
  KEY `idx_capture_time` (`capture_time`),
  KEY `idx_is_recognized` (`is_recognized`),
  KEY `idx_person_id` (`person_id`),
  KEY `idx_is_alarm` (`is_alarm`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='安防-人脸抓拍记录';

-- ----------------------------
-- 3. 人脸识别记录表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `security_face_recognition` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `capture_id` bigint NOT NULL COMMENT '抓拍记录ID',
  `person_id` bigint NOT NULL COMMENT '人员ID',
  `person_name` varchar(100) DEFAULT NULL COMMENT '人员姓名',
  `person_type` varchar(50) DEFAULT NULL COMMENT '人员类型',
  `similarity` decimal(5,2) NOT NULL COMMENT '相似度（0-100）',
  `recognition_time` datetime NOT NULL COMMENT '识别时间',
  `recognition_result` varchar(20) NOT NULL COMMENT '识别结果（success成功 failed失败）',
  `confidence` decimal(5,2) DEFAULT NULL COMMENT '置信度',
  `is_pass` tinyint DEFAULT NULL COMMENT '是否通过（0否 1是）',
  `pass_reason` varchar(200) DEFAULT NULL COMMENT '通过原因',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
  PRIMARY KEY (`id`),
  KEY `idx_capture_id` (`capture_id`),
  KEY `idx_person_id` (`person_id`),
  KEY `idx_recognition_time` (`recognition_time`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='安防-人脸识别记录';

-- ----------------------------
-- 4. 人员布控配置表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `security_personnel_control` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `control_name` varchar(100) NOT NULL COMMENT '布控名称',
  `person_id` bigint NOT NULL COMMENT '人员ID',
  `person_name` varchar(100) DEFAULT NULL COMMENT '人员姓名',
  `control_type` varchar(50) NOT NULL COMMENT '布控类型（blacklist黑名单 vip重点人员 visitor访客）',
  `control_level` varchar(20) NOT NULL DEFAULT 'medium' COMMENT '布控级别（low低 medium中 high高 urgent紧急）',
  `control_areas` json DEFAULT NULL COMMENT '布控区域（区域ID数组）',
  `control_cameras` json DEFAULT NULL COMMENT '布控摄像头（设备ID数组）',
  `start_time` datetime NOT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `time_ranges` json DEFAULT NULL COMMENT '时间段（可选，JSON数组）',
  `week_days` varchar(50) DEFAULT NULL COMMENT '星期（1-7，逗号分隔）',
  `control_reason` varchar(500) DEFAULT NULL COMMENT '布控原因',
  `alarm_enabled` tinyint DEFAULT 1 COMMENT '是否告警（0否 1是）',
  `alarm_type` varchar(50) DEFAULT 'warning' COMMENT '告警类型',
  `alarm_threshold` decimal(5,2) DEFAULT 80.00 COMMENT '告警阈值（相似度）',
  `notify_users` json DEFAULT NULL COMMENT '通知人员（用户ID数组）',
  `notify_methods` varchar(100) DEFAULT 'system,email' COMMENT '通知方式（system系统 email邮件 sms短信）',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（0停用 1启用）',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
  PRIMARY KEY (`id`),
  KEY `idx_person_id` (`person_id`),
  KEY `idx_control_type` (`control_type`),
  KEY `idx_status` (`status`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='安防-人员布控配置';

-- ----------------------------
-- 5. 人员追踪记录表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `security_personnel_track` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `person_id` bigint NOT NULL COMMENT '人员ID',
  `person_name` varchar(100) DEFAULT NULL COMMENT '人员姓名',
  `capture_id` bigint NOT NULL COMMENT '抓拍记录ID',
  `device_id` bigint NOT NULL COMMENT '设备ID',
  `device_name` varchar(100) DEFAULT NULL COMMENT '设备名称',
  `area_id` bigint DEFAULT NULL COMMENT '区域ID',
  `area_name` varchar(100) DEFAULT NULL COMMENT '区域名称',
  `track_time` datetime NOT NULL COMMENT '追踪时间',
  `face_image_url` varchar(500) DEFAULT NULL COMMENT '人脸图片',
  `similarity` decimal(5,2) DEFAULT NULL COMMENT '相似度',
  `direction` varchar(20) DEFAULT NULL COMMENT '移动方向（in进入 out离开）',
  `stay_duration` int DEFAULT NULL COMMENT '停留时长（秒）',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
  PRIMARY KEY (`id`),
  KEY `idx_person_id` (`person_id`),
  KEY `idx_track_time` (`track_time`),
  KEY `idx_device_id` (`device_id`),
  KEY `idx_area_id` (`area_id`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='安防-人员追踪记录';

-- ----------------------------
-- 6. 车辆抓拍记录表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `security_vehicle_capture` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `capture_no` varchar(50) NOT NULL COMMENT '抓拍编号',
  `device_id` bigint NOT NULL COMMENT '设备ID',
  `device_name` varchar(100) DEFAULT NULL COMMENT '设备名称',
  `capture_time` datetime NOT NULL COMMENT '抓拍时间',
  `capture_location` varchar(200) DEFAULT NULL COMMENT '抓拍地点',
  `vehicle_image_url` varchar(500) NOT NULL COMMENT '车辆图片URL',
  `plate_image_url` varchar(500) DEFAULT NULL COMMENT '车牌图片URL',
  `plate_number` varchar(20) DEFAULT NULL COMMENT '车牌号码',
  `plate_color` varchar(20) DEFAULT NULL COMMENT '车牌颜色（blue蓝 yellow黄 green绿 white白 black黑）',
  `vehicle_type` varchar(50) DEFAULT NULL COMMENT '车辆类型（car小车 bus大客车 truck货车 motorcycle摩托车）',
  `vehicle_color` varchar(50) DEFAULT NULL COMMENT '车辆颜色',
  `vehicle_brand` varchar(50) DEFAULT NULL COMMENT '车辆品牌',
  `direction` varchar(20) DEFAULT NULL COMMENT '行驶方向（in进入 out离开）',
  `speed` decimal(5,2) DEFAULT NULL COMMENT '车速（km/h）',
  `confidence` decimal(5,2) DEFAULT NULL COMMENT '识别置信度',
  `is_alarm` tinyint DEFAULT 0 COMMENT '是否告警（0否 1是）',
  `alarm_type` varchar(50) DEFAULT NULL COMMENT '告警类型',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_capture_no` (`capture_no`),
  KEY `idx_device_id` (`device_id`),
  KEY `idx_capture_time` (`capture_time`),
  KEY `idx_plate_number` (`plate_number`),
  KEY `idx_is_alarm` (`is_alarm`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='安防-车辆抓拍记录';

-- ----------------------------
-- 7. 视频分析任务表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `security_video_analysis_task` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `task_name` varchar(100) NOT NULL COMMENT '任务名称',
  `task_type` varchar(50) NOT NULL COMMENT '任务类型（face_detection人脸检测 behavior_analysis行为分析 intrusion_detection入侵检测 crowd_density人群密度 fire_smoke火焰烟雾）',
  `device_ids` json NOT NULL COMMENT '分析设备（设备ID数组）',
  `area_ids` json DEFAULT NULL COMMENT '分析区域（区域ID数组）',
  `analysis_config` json DEFAULT NULL COMMENT '分析配置（JSON）',
  `schedule_type` varchar(20) NOT NULL DEFAULT 'realtime' COMMENT '调度类型（realtime实时 scheduled定时 manual手动）',
  `schedule_config` json DEFAULT NULL COMMENT '调度配置（cron表达式等）',
  `alarm_enabled` tinyint DEFAULT 1 COMMENT '是否告警（0否 1是）',
  `alarm_rules` json DEFAULT NULL COMMENT '告警规则（JSON）',
  `result_storage` varchar(20) DEFAULT 'database' COMMENT '结果存储（database数据库 file文件 both两者）',
  `retention_days` int DEFAULT 30 COMMENT '数据保留天数',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（0停用 1启用 2运行中）',
  `last_run_time` datetime DEFAULT NULL COMMENT '最后运行时间',
  `next_run_time` datetime DEFAULT NULL COMMENT '下次运行时间',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
  PRIMARY KEY (`id`),
  KEY `idx_task_type` (`task_type`),
  KEY `idx_status` (`status`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='安防-视频分析任务';

-- ----------------------------
-- 8. 视频分析告警表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `security_video_analysis_alarm` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `alarm_no` varchar(50) NOT NULL COMMENT '告警编号',
  `task_id` bigint NOT NULL COMMENT '任务ID',
  `task_name` varchar(100) DEFAULT NULL COMMENT '任务名称',
  `alarm_type` varchar(50) NOT NULL COMMENT '告警类型',
  `alarm_level` varchar(20) NOT NULL DEFAULT 'medium' COMMENT '告警级别（low低 medium中 high高 urgent紧急）',
  `device_id` bigint NOT NULL COMMENT '设备ID',
  `device_name` varchar(100) DEFAULT NULL COMMENT '设备名称',
  `area_id` bigint DEFAULT NULL COMMENT '区域ID',
  `alarm_time` datetime NOT NULL COMMENT '告警时间',
  `alarm_desc` varchar(500) DEFAULT NULL COMMENT '告警描述',
  `alarm_image_url` varchar(500) DEFAULT NULL COMMENT '告警图片',
  `alarm_video_url` varchar(500) DEFAULT NULL COMMENT '告警视频',
  `analysis_result` json DEFAULT NULL COMMENT '分析结果（JSON）',
  `is_handled` tinyint DEFAULT 0 COMMENT '是否处理（0否 1是）',
  `handle_time` datetime DEFAULT NULL COMMENT '处理时间',
  `handle_user` varchar(64) DEFAULT NULL COMMENT '处理人',
  `handle_result` varchar(500) DEFAULT NULL COMMENT '处理结果',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_alarm_no` (`alarm_no`),
  KEY `idx_task_id` (`task_id`),
  KEY `idx_alarm_type` (`alarm_type`),
  KEY `idx_alarm_time` (`alarm_time`),
  KEY `idx_is_handled` (`is_handled`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='安防-视频分析告警';

-- ----------------------------
-- 9. 安防统计数据表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `security_statistics` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `stat_date` date NOT NULL COMMENT '统计日期',
  `stat_hour` int DEFAULT NULL COMMENT '统计小时（0-23，用于小时统计）',
  `stat_type` varchar(50) NOT NULL COMMENT '统计类型（daily日统计 hourly小时统计）',
  
  -- 设备统计
  `total_cameras` int DEFAULT 0 COMMENT '摄像头总数',
  `online_cameras` int DEFAULT 0 COMMENT '在线摄像头数',
  `offline_cameras` int DEFAULT 0 COMMENT '离线摄像头数',
  
  -- 人脸统计
  `face_capture_count` int DEFAULT 0 COMMENT '人脸抓拍数',
  `face_recognition_count` int DEFAULT 0 COMMENT '人脸识别数',
  `stranger_count` int DEFAULT 0 COMMENT '陌生人数',
  
  -- 车辆统计
  `vehicle_capture_count` int DEFAULT 0 COMMENT '车辆抓拍数',
  `vehicle_in_count` int DEFAULT 0 COMMENT '车辆进入数',
  `vehicle_out_count` int DEFAULT 0 COMMENT '车辆离开数',
  
  -- 告警统计
  `total_alarms` int DEFAULT 0 COMMENT '总告警数',
  `handled_alarms` int DEFAULT 0 COMMENT '已处理告警数',
  `unhandled_alarms` int DEFAULT 0 COMMENT '未处理告警数',
  `alarm_by_level` json DEFAULT NULL COMMENT '按级别统计告警（JSON）',
  `alarm_by_type` json DEFAULT NULL COMMENT '按类型统计告警（JSON）',
  
  -- 其他统计
  `analysis_task_count` int DEFAULT 0 COMMENT '分析任务数',
  `controlled_person_count` int DEFAULT 0 COMMENT '布控人员数',
  
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_stat_date_hour_type` (`stat_date`, `stat_hour`, `stat_type`, `tenant_id`),
  KEY `idx_stat_date` (`stat_date`),
  KEY `idx_stat_type` (`stat_type`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='安防-统计数据';

-- ----------------------------
-- 插入测试数据（可选）
-- ----------------------------

-- 插入测试人员
INSERT INTO `security_personnel_library` (`person_name`, `person_no`, `gender`, `person_type`, `remark`)
VALUES 
  ('张三', 'P001', 1, 'employee', '测试员工'),
  ('李四', 'P002', 1, 'visitor', '测试访客'),
  ('王五', 'P003', 1, 'blacklist', '测试黑名单')
ON DUPLICATE KEY UPDATE `person_name` = VALUES(`person_name`);

-- 完成提示
SELECT '智慧安防数据表创建完成！共创建9张表。' AS message;



















