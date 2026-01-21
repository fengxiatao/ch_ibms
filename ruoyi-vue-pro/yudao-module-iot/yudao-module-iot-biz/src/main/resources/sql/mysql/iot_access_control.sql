-- =============================================
-- 智慧通道（门禁）模块数据库表
-- 基于大华门禁设备SDK设计
-- =============================================

-- 1. 门禁产品表（产品型号和能力定义）
CREATE TABLE IF NOT EXISTS `iot_access_product` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '产品ID',
  `product_name` varchar(100) NOT NULL COMMENT '产品名称',
  `product_model` varchar(100) NOT NULL COMMENT '产品型号（如：DH-ASI7203S-W、DH-ASC2202B-S）',
  `manufacturer` varchar(100) DEFAULT '大华' COMMENT '厂商名称',
  `device_type` varchar(50) NOT NULL COMMENT '设备类型：FINGERPRINT-指纹一体机, FACE-人脸一体机, CONTROLLER-门禁控制器',
  `generation` int NOT NULL DEFAULT 2 COMMENT '设备代数：1-一代设备, 2-二代设备',
  `support_fingerprint` bit(1) DEFAULT b'0' COMMENT '是否支持指纹识别',
  `support_face` bit(1) DEFAULT b'0' COMMENT '是否支持人脸识别',
  `support_card` bit(1) DEFAULT b'1' COMMENT '是否支持刷卡',
  `support_password` bit(1) DEFAULT b'1' COMMENT '是否支持密码',
  `support_qrcode` bit(1) DEFAULT b'0' COMMENT '是否支持二维码',
  `max_users` int DEFAULT 10000 COMMENT '最大用户数',
  `max_cards` int DEFAULT 10000 COMMENT '最大卡片数',
  `max_faces` int DEFAULT 10000 COMMENT '最大人脸数',
  `max_fingerprints` int DEFAULT 10000 COMMENT '最大指纹数',
  `max_doors` int DEFAULT 4 COMMENT '最大门数量',
  `description` varchar(500) DEFAULT NULL COMMENT '产品描述',
  `enabled` bit(1) NOT NULL DEFAULT b'1' COMMENT '是否启用',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_product_model` (`product_model`, `deleted`),
  KEY `idx_device_type` (`device_type`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='门禁产品表';

-- 2. 门禁设备表（具体设备实例）
CREATE TABLE IF NOT EXISTS `iot_access_device` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '设备ID',
  `product_id` bigint NOT NULL COMMENT '产品ID（关联iot_access_product.id）',
  `device_name` varchar(100) NOT NULL COMMENT '设备名称',
  `device_sn` varchar(100) DEFAULT NULL COMMENT '设备序列号',
  `ip_address` varchar(50) NOT NULL COMMENT 'IP地址',
  `port` int NOT NULL DEFAULT 37777 COMMENT '端口号',
  `username` varchar(100) NOT NULL DEFAULT 'admin' COMMENT '登录用户名',
  `password` varchar(200) NOT NULL COMMENT '登录密码（加密存储）',
  `location` varchar(200) DEFAULT NULL COMMENT '安装位置',
  `status` varchar(20) DEFAULT 'OFFLINE' COMMENT '设备状态：ONLINE-在线, OFFLINE-离线, FAULT-故障',
  `connection_status` varchar(20) DEFAULT 'DISCONNECTED' COMMENT '连接状态：CONNECTED-已连接, DISCONNECTED-未连接, CONNECTING-连接中',
  `last_online_time` datetime DEFAULT NULL COMMENT '最后在线时间',
  `firmware_version` varchar(50) DEFAULT NULL COMMENT '固件版本',
  `sdk_version` varchar(50) DEFAULT NULL COMMENT 'SDK版本',
  `login_handle` bigint DEFAULT NULL COMMENT 'SDK登录句柄（运行时）',
  `activated` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否已激活',
  `activation_time` datetime DEFAULT NULL COMMENT '激活时间',
  `enabled` bit(1) NOT NULL DEFAULT b'1' COMMENT '是否启用',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ip_port` (`ip_address`, `port`, `deleted`),
  KEY `idx_product_id` (`product_id`),
  KEY `idx_device_sn` (`device_sn`),
  KEY `idx_status` (`status`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='门禁设备表';

-- 3. 门禁通道表（门禁点位/门）
CREATE TABLE IF NOT EXISTS `iot_access_channel` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '通道ID',
  `device_id` bigint NOT NULL COMMENT '设备ID（关联iot_access_device.id）',
  `channel_no` int NOT NULL COMMENT '通道号（从0开始）',
  `channel_name` varchar(100) NOT NULL COMMENT '通道名称',
  `door_name` varchar(100) DEFAULT NULL COMMENT '门名称',
  `location` varchar(200) DEFAULT NULL COMMENT '位置信息',
  `door_type` varchar(50) DEFAULT 'NORMAL' COMMENT '门类型：NORMAL-普通门, EMERGENCY-紧急门, FIRE-消防门',
  `open_mode` varchar(50) DEFAULT 'CARD' COMMENT '开门方式：CARD-刷卡, FACE-人脸, FINGERPRINT-指纹, PASSWORD-密码, REMOTE-远程, MULTI-多重验证',
  `open_duration` int DEFAULT 5 COMMENT '开门持续时间（秒）',
  `alarm_duration` int DEFAULT 30 COMMENT '报警持续时间（秒）',
  `door_sensor_enabled` bit(1) DEFAULT b'1' COMMENT '是否启用门磁检测',
  `timeout_alarm_enabled` bit(1) DEFAULT b'1' COMMENT '是否启用超时报警',
  `force_alarm_enabled` bit(1) DEFAULT b'1' COMMENT '是否启用强制开门报警',
  `status` varchar(20) DEFAULT 'CLOSED' COMMENT '门状态：OPEN-开启, CLOSED-关闭, ABNORMAL-异常',
  `lock_status` varchar(20) DEFAULT 'LOCKED' COMMENT '锁状态：LOCKED-已锁, UNLOCKED-未锁',
  `camera_id` bigint DEFAULT NULL COMMENT '关联摄像头ID（用于联动抓拍）',
  `enabled` bit(1) NOT NULL DEFAULT b'1' COMMENT '是否启用',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_device_channel` (`device_id`, `channel_no`, `deleted`),
  KEY `idx_device_id` (`device_id`),
  KEY `idx_camera_id` (`camera_id`),
  KEY `idx_status` (`status`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='门禁通道表';

-- 4. 门禁操作记录表
CREATE TABLE IF NOT EXISTS `iot_access_operation_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `device_id` bigint DEFAULT NULL COMMENT '设备ID',
  `channel_id` bigint DEFAULT NULL COMMENT '通道ID',
  `operation_type` varchar(50) NOT NULL COMMENT '操作类型：OPEN_DOOR-开门, CLOSE_DOOR-关门, LOCK-上锁, UNLOCK-解锁, QUERY-查询, REFRESH-刷新',
  `operation_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  `operator_id` bigint DEFAULT NULL COMMENT '操作人ID',
  `operator_name` varchar(100) DEFAULT NULL COMMENT '操作人姓名',
  `result` varchar(20) DEFAULT 'SUCCESS' COMMENT '操作结果：SUCCESS-成功, FAILED-失败',
  `error_message` varchar(500) DEFAULT NULL COMMENT '错误信息',
  `request_id` varchar(64) DEFAULT NULL COMMENT '请求ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  KEY `idx_device_id` (`device_id`),
  KEY `idx_channel_id` (`channel_id`),
  KEY `idx_operation_time` (`operation_time`),
  KEY `idx_operation_type` (`operation_type`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='门禁操作记录表';

-- 5. 门禁事件记录表（刷卡、人脸识别等事件）
CREATE TABLE IF NOT EXISTS `iot_access_event_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `device_id` bigint NOT NULL COMMENT '设备ID',
  `channel_id` bigint DEFAULT NULL COMMENT '通道ID',
  `event_type` varchar(50) NOT NULL COMMENT '事件类型：CARD_SWIPE-刷卡, FACE_RECOGNIZE-人脸识别, FINGERPRINT-指纹, PASSWORD-密码, DOOR_OPEN-开门, DOOR_CLOSE-关门, ALARM-报警',
  `event_time` datetime NOT NULL COMMENT '事件时间',
  `user_id` varchar(100) DEFAULT NULL COMMENT '用户ID',
  `user_name` varchar(100) DEFAULT NULL COMMENT '用户姓名',
  `card_no` varchar(100) DEFAULT NULL COMMENT '卡号',
  `verify_mode` varchar(50) DEFAULT NULL COMMENT '验证方式',
  `verify_result` varchar(20) DEFAULT NULL COMMENT '验证结果：SUCCESS-成功, FAILED-失败',
  `fail_reason` varchar(200) DEFAULT NULL COMMENT '失败原因',
  `snapshot_url` varchar(500) DEFAULT NULL COMMENT '抓拍图片URL',
  `temperature` decimal(5,2) DEFAULT NULL COMMENT '体温（℃）',
  `mask_status` varchar(20) DEFAULT NULL COMMENT '口罩状态：WEAR-佩戴, NOT_WEAR-未佩戴',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  KEY `idx_device_id` (`device_id`),
  KEY `idx_channel_id` (`channel_id`),
  KEY `idx_event_time` (`event_time`),
  KEY `idx_event_type` (`event_type`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_card_no` (`card_no`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='门禁事件记录表';

-- =============================================
-- 初始化数据：添加大华门禁产品
-- =============================================

-- 插入指纹一体机产品（一代设备）
INSERT INTO `iot_access_product` 
(`product_name`, `product_model`, `manufacturer`, `device_type`, `generation`, 
 `support_fingerprint`, `support_face`, `support_card`, `support_password`, 
 `max_users`, `max_cards`, `max_fingerprints`, `max_doors`, `description`, `tenant_id`)
VALUES 
('大华指纹门禁一体机', 'DH-ASI7203S-W', '大华', 'FINGERPRINT', 1, 
 b'1', b'0', b'1', b'1', 
 3000, 3000, 3000, 1, '支持指纹、刷卡、密码等多种开门方式的一代门禁设备', 0);

-- 插入人脸识别一体机产品（二代设备）
INSERT INTO `iot_access_product` 
(`product_name`, `product_model`, `manufacturer`, `device_type`, `generation`, 
 `support_fingerprint`, `support_face`, `support_card`, `support_password`, `support_qrcode`,
 `max_users`, `max_cards`, `max_faces`, `max_doors`, `description`, `tenant_id`)
VALUES 
('大华人脸门禁一体机', 'DH-ASC2202B-S', '大华', 'FACE', 2, 
 b'0', b'1', b'1', b'1', b'1',
 10000, 10000, 10000, 2, '支持人脸识别、刷卡、密码、二维码等多种开门方式的二代门禁设备', 0);

-- 插入门禁控制器产品（二代设备）
INSERT INTO `iot_access_product` 
(`product_name`, `product_model`, `manufacturer`, `device_type`, `generation`, 
 `support_fingerprint`, `support_face`, `support_card`, `support_password`, 
 `max_users`, `max_cards`, `max_doors`, `description`, `tenant_id`)
VALUES 
('大华门禁控制器', 'DH-ASC1204C-D', '大华', 'CONTROLLER', 2, 
 b'0', b'0', b'1', b'1', 
 10000, 10000, 4, '四门门禁控制器，支持刷卡、密码等开门方式', 0);

-- =============================================
-- 添加实际设备（根据用户提供的信息）
-- =============================================

-- 添加192.168.1.207 - 指纹一体机
INSERT INTO `iot_access_device` 
(`product_id`, `device_name`, `ip_address`, `port`, `username`, `password`, 
 `location`, `status`, `connection_status`, `enabled`, `tenant_id`)
VALUES 
(1, '1号楼大厅指纹门禁', '192.168.1.207', 37777, 'admin', 'admin123', 
 '1号楼大厅入口', 'OFFLINE', 'DISCONNECTED', b'1', 0);

-- 添加192.168.1.208 - 人脸识别一体机
INSERT INTO `iot_access_device` 
(`product_id`, `device_name`, `ip_address`, `port`, `username`, `password`, 
 `location`, `status`, `connection_status`, `enabled`, `tenant_id`)
VALUES 
(2, '1号楼大厅人脸门禁', '192.168.1.208', 37777, 'admin', 'admin123', 
 '1号楼大厅出口', 'OFFLINE', 'DISCONNECTED', b'1', 0);