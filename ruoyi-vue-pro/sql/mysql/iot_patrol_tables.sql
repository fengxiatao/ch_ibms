-- =====================================================
-- IoT 电子巡更模块 - 数据库表结构
-- =====================================================

-- ----------------------------
-- 1. 巡更点位表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `iot_patrol_point` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `point_name` varchar(128) NOT NULL COMMENT '点位名称',
  `point_code` varchar(64) NOT NULL COMMENT '点位编码（唯一标识，用于扫描识别）',
  `point_type` tinyint(4) NOT NULL DEFAULT 1 COMMENT '点位类型(1:NFC标签 2:二维码 3:RFID 4:蓝牙信标)',
  `building_id` bigint(20) DEFAULT NULL COMMENT '所属建筑ID',
  `floor_id` bigint(20) DEFAULT NULL COMMENT '所属楼层ID',
  `area_id` bigint(20) DEFAULT NULL COMMENT '所属区域ID',
  `location` varchar(256) DEFAULT NULL COMMENT '详细位置描述',
  `longitude` decimal(10, 7) DEFAULT NULL COMMENT '经度',
  `latitude` decimal(10, 7) DEFAULT NULL COMMENT '纬度',
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
  KEY `idx_building_id` (`building_id`),
  KEY `idx_floor_id` (`floor_id`),
  KEY `idx_area_id` (`area_id`),
  KEY `idx_status` (`status`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='IoT巡更点位表';

-- ----------------------------
-- 2. 巡更路线表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `iot_patrol_route` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `route_name` varchar(128) NOT NULL COMMENT '路线名称',
  `route_code` varchar(64) NOT NULL COMMENT '路线编码',
  `point_ids` json NOT NULL COMMENT '点位ID列表（JSON数组，按巡更顺序）',
  `point_count` int(11) DEFAULT 0 COMMENT '点位数量',
  `estimated_duration` int(11) DEFAULT 0 COMMENT '预计巡更时长(分钟)',
  `description` varchar(512) DEFAULT NULL COMMENT '路线描述',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态(0:停用 1:启用)',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_route_code` (`route_code`, `deleted`, `tenant_id`),
  KEY `idx_status` (`status`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='IoT巡更路线表';

-- ----------------------------
-- 3. 巡更计划表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `iot_patrol_plan` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `plan_name` varchar(128) NOT NULL COMMENT '计划名称',
  `plan_code` varchar(64) NOT NULL COMMENT '计划编码',
  `plan_type` tinyint(4) NOT NULL DEFAULT 1 COMMENT '计划类型(1:手持式巡更 2:设备扫描巡更)',
  `route_id` bigint(20) NOT NULL COMMENT '巡更路线ID',
  `patrol_mode` tinyint(4) NOT NULL DEFAULT 1 COMMENT '巡更模式(1:顺序巡更 2:随机巡更)',
  `schedule_type` tinyint(4) NOT NULL DEFAULT 1 COMMENT '排班类型(1:每天 2:工作日 3:周末 4:自定义)',
  `schedule_config` json DEFAULT NULL COMMENT '排班配置（JSON格式，自定义时存储具体日期）',
  `time_slots` json NOT NULL COMMENT '时间段配置（JSON数组，[{"startTime":"08:00","endTime":"09:00"}]）',
  `patrol_user_ids` json NOT NULL COMMENT '巡更人员ID列表（JSON数组）',
  `start_date` date NOT NULL COMMENT '计划开始日期',
  `end_date` date DEFAULT NULL COMMENT '计划结束日期（NULL表示长期有效）',
  `allow_early_checkin` tinyint(1) DEFAULT 1 COMMENT '允许提前签到(0:否 1:是)',
  `early_checkin_minutes` int(11) DEFAULT 15 COMMENT '允许提前签到分钟数',
  `allow_late_checkin` tinyint(1) DEFAULT 1 COMMENT '允许延迟签到(0:否 1:是)',
  `late_checkin_minutes` int(11) DEFAULT 15 COMMENT '允许延迟签到分钟数',
  `miss_alert` tinyint(1) DEFAULT 1 COMMENT '漏巡报警(0:否 1:是)',
  `timeout_alert` tinyint(1) DEFAULT 1 COMMENT '超时报警(0:否 1:是)',
  `timeout_minutes` int(11) DEFAULT 30 COMMENT '超时报警分钟数',
  `description` varchar(512) DEFAULT NULL COMMENT '计划描述',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态(0:停用 1:启用 2:已过期)',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_plan_code` (`plan_code`, `deleted`, `tenant_id`),
  KEY `idx_route_id` (`route_id`),
  KEY `idx_status` (`status`),
  KEY `idx_start_date` (`start_date`),
  KEY `idx_end_date` (`end_date`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='IoT巡更计划表';

-- ----------------------------
-- 4. 巡更记录表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `iot_patrol_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `plan_id` bigint(20) NOT NULL COMMENT '计划ID',
  `route_id` bigint(20) NOT NULL COMMENT '路线ID',
  `point_id` bigint(20) NOT NULL COMMENT '点位ID',
  `point_name` varchar(128) DEFAULT NULL COMMENT '点位名称（冗余字段）',
  `user_id` bigint(20) NOT NULL COMMENT '巡更人员ID',
  `user_name` varchar(64) DEFAULT NULL COMMENT '巡更人员姓名（冗余字段）',
  `scheduled_time` datetime NOT NULL COMMENT '计划巡更时间',
  `actual_time` datetime DEFAULT NULL COMMENT '实际巡更时间',
  `checkin_type` tinyint(4) DEFAULT NULL COMMENT '签到类型(1:正常 2:提前 3:延迟 4:漏巡)',
  `time_diff_minutes` int(11) DEFAULT NULL COMMENT '时间差(分钟)，正数为延迟，负数为提前',
  `scan_method` tinyint(4) DEFAULT NULL COMMENT '扫描方式(1:NFC 2:二维码 3:RFID 4:蓝牙 5:手动签到)',
  `longitude` decimal(10, 7) DEFAULT NULL COMMENT '签到经度',
  `latitude` decimal(10, 7) DEFAULT NULL COMMENT '签到纬度',
  `location_accuracy` int(11) DEFAULT NULL COMMENT '位置精度(米)',
  `device_info` varchar(256) DEFAULT NULL COMMENT '设备信息（手机型号等）',
  `photos` json DEFAULT NULL COMMENT '现场照片URL列表（JSON数组）',
  `remark` varchar(512) DEFAULT NULL COMMENT '备注',
  `is_abnormal` tinyint(1) DEFAULT 0 COMMENT '是否异常(0:正常 1:异常)',
  `abnormal_reason` varchar(256) DEFAULT NULL COMMENT '异常原因',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`),
  KEY `idx_plan_id` (`plan_id`),
  KEY `idx_route_id` (`route_id`),
  KEY `idx_point_id` (`point_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_scheduled_time` (`scheduled_time`),
  KEY `idx_actual_time` (`actual_time`),
  KEY `idx_checkin_type` (`checkin_type`),
  KEY `idx_is_abnormal` (`is_abnormal`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='IoT巡更记录表';

-- ----------------------------
-- 初始化示例数据（可选）
-- ----------------------------

-- 示例巡更点位
INSERT INTO `iot_patrol_point` (`point_name`, `point_code`, `point_type`, `location`, `description`, `status`, `sort`) VALUES
('主入口', 'PT001', 1, '一楼主入口', '大楼主入口巡更点', 1, 1),
('消防通道A', 'PT002', 1, '一楼东侧消防通道', '消防通道巡查点', 1, 2),
('电梯厅', 'PT003', 2, '一楼电梯厅', '电梯厅巡查点', 1, 3),
('停车场入口', 'PT004', 1, '地下一层停车场入口', '停车场入口巡查点', 1, 4),
('配电室', 'PT005', 1, '地下一层配电室门口', '配电室巡查点', 1, 5);

-- 示例巡更路线
INSERT INTO `iot_patrol_route` (`route_name`, `route_code`, `point_ids`, `point_count`, `estimated_duration`, `description`, `status`) VALUES
('一楼日常巡更', 'RT001', JSON_ARRAY(1, 2, 3), 3, 15, '一楼主要区域日常巡更路线', 1),
('地下室巡更', 'RT002', JSON_ARRAY(4, 5), 2, 10, '地下室及配电室巡更路线', 1),
('全楼巡更', 'RT003', JSON_ARRAY(1, 2, 3, 4, 5), 5, 30, '全楼巡更路线', 1);

-- 示例巡更计划
INSERT INTO `iot_patrol_plan` (`plan_name`, `plan_code`, `plan_type`, `route_id`, `patrol_mode`, `schedule_type`, `time_slots`, `patrol_user_ids`, `start_date`, `allow_early_checkin`, `early_checkin_minutes`, `allow_late_checkin`, `late_checkin_minutes`, `miss_alert`, `timeout_alert`, `timeout_minutes`, `description`, `status`) VALUES
('日常巡更计划', 'PL001', 1, 1, 1, 1, JSON_ARRAY(JSON_OBJECT('startTime', '08:00', 'endTime', '09:00'), JSON_OBJECT('startTime', '18:00', 'endTime', '19:00')), JSON_ARRAY(1, 2), CURDATE(), 1, 15, 1, 15, 1, 1, 30, '每日早晚巡更', 1);






