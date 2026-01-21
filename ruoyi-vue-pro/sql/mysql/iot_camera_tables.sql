-- ----------------------------
-- IoT 网络摄像头功能扩展表
-- ----------------------------

-- 1. 摄像头扩展信息表
CREATE TABLE IF NOT EXISTS `iot_camera` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `device_id` bigint(20) NOT NULL COMMENT '设备ID',
  `stream_url_main` varchar(512) DEFAULT NULL COMMENT '主码流地址',
  `stream_url_sub` varchar(512) DEFAULT NULL COMMENT '子码流地址',
  `rtsp_port` int(11) DEFAULT 554 COMMENT 'RTSP端口',
  `onvif_port` int(11) DEFAULT 80 COMMENT 'ONVIF端口',
  `username` varchar(64) DEFAULT 'admin' COMMENT '登录用户名',
  `password` varchar(256) DEFAULT NULL COMMENT '登录密码（加密）',
  `manufacturer` varchar(64) DEFAULT '大华' COMMENT '厂商',
  `model` varchar(128) DEFAULT NULL COMMENT '型号',
  `ptz_support` tinyint(1) DEFAULT 0 COMMENT '是否支持PTZ(0:不支持 1:支持)',
  `audio_support` tinyint(1) DEFAULT 0 COMMENT '是否支持音频(0:不支持 1:支持)',
  `resolution` varchar(32) DEFAULT '1920*1080' COMMENT '分辨率',
  `frame_rate` int(11) DEFAULT 25 COMMENT '帧率(FPS)',
  `bit_rate` int(11) DEFAULT 2048 COMMENT '码率(Kbps)',
  `preset_count` int(11) DEFAULT 0 COMMENT '预置位数量',
  `brightness` int(11) DEFAULT 50 COMMENT '亮度(0-100)',
  `contrast` int(11) DEFAULT 50 COMMENT '对比度(0-100)',
  `saturation` int(11) DEFAULT 50 COMMENT '饱和度(0-100)',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_device_id` (`device_id`, `deleted`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='IoT摄像头扩展信息表';

-- 2. 摄像头预置位表
CREATE TABLE IF NOT EXISTS `iot_camera_preset` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `camera_id` bigint(20) NOT NULL COMMENT '摄像头ID',
  `preset_id` int(11) NOT NULL COMMENT '预置位编号(1-255)',
  `name` varchar(64) NOT NULL COMMENT '预置位名称',
  `pan_angle` float DEFAULT NULL COMMENT '水平角度(-180~180)',
  `tilt_angle` float DEFAULT NULL COMMENT '垂直角度(-90~90)',
  `zoom_level` int(11) DEFAULT NULL COMMENT '变倍级别(1-10)',
  `description` varchar(255) DEFAULT NULL COMMENT '描述',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_camera_preset` (`camera_id`, `preset_id`, `deleted`),
  KEY `idx_camera_id` (`camera_id`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='IoT摄像头预置位表';

-- 3. 摄像头录像记录表
CREATE TABLE IF NOT EXISTS `iot_camera_recording` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `camera_id` bigint(20) NOT NULL COMMENT '摄像头ID',
  `start_time` datetime NOT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `duration` int(11) DEFAULT 0 COMMENT '时长(秒)',
  `file_path` varchar(512) DEFAULT NULL COMMENT '文件路径',
  `file_size` bigint(20) DEFAULT 0 COMMENT '文件大小(字节)',
  `file_url` varchar(512) DEFAULT NULL COMMENT '文件访问URL',
  `recording_type` tinyint(4) NOT NULL COMMENT '录像类型(1:手动 2:定时 3:报警触发 4:移动侦测)',
  `status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '状态(0:录像中 1:已完成 2:已停止 3:异常)',
  `error_msg` varchar(255) DEFAULT NULL COMMENT '错误信息',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`),
  KEY `idx_camera_id` (`camera_id`),
  KEY `idx_start_time` (`start_time`),
  KEY `idx_status` (`status`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='IoT摄像头录像记录表';

-- 4. 摄像头报警记录表
CREATE TABLE IF NOT EXISTS `iot_camera_alarm` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `camera_id` bigint(20) NOT NULL COMMENT '摄像头ID',
  `device_name` varchar(128) DEFAULT NULL COMMENT '设备名称（冗余字段，便于查询）',
  `alarm_type` varchar(64) NOT NULL COMMENT '报警类型(motion_detected:移动侦测,video_loss:视频丢失,disk_full:磁盘满,connection_lost:连接丢失)',
  `alarm_time` datetime NOT NULL COMMENT '报警时间',
  `alarm_level` tinyint(4) NOT NULL COMMENT '报警级别(1:低 2:中 3:高 4:严重)',
  `snapshot_url` varchar(512) DEFAULT NULL COMMENT '快照地址',
  `video_url` varchar(512) DEFAULT NULL COMMENT '视频片段地址',
  `description` varchar(512) DEFAULT NULL COMMENT '描述',
  `status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '状态(0:未处理 1:已确认 2:已处理 3:误报 4:已忽略)',
  `handler` varchar(64) DEFAULT NULL COMMENT '处理人',
  `handle_time` datetime DEFAULT NULL COMMENT '处理时间',
  `handle_remark` varchar(512) DEFAULT NULL COMMENT '处理备注',
  `auto_handled` tinyint(1) DEFAULT 0 COMMENT '是否自动处理(0:否 1:是)',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`),
  KEY `idx_camera_id` (`camera_id`),
  KEY `idx_alarm_time` (`alarm_time`),
  KEY `idx_alarm_type` (`alarm_type`),
  KEY `idx_status` (`status`),
  KEY `idx_alarm_level` (`alarm_level`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='IoT摄像头报警记录表';

-- 5. 摄像头监控墙配置表
CREATE TABLE IF NOT EXISTS `iot_monitor_wall` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `name` varchar(128) NOT NULL COMMENT '监控墙名称',
  `layout` varchar(16) NOT NULL DEFAULT '2x2' COMMENT '布局(1x1,2x2,3x3,4x4)',
  `camera_ids` text DEFAULT NULL COMMENT '摄像头ID列表(JSON数组)',
  `is_default` tinyint(1) DEFAULT 0 COMMENT '是否默认(0:否 1:是)',
  `description` varchar(255) DEFAULT NULL COMMENT '描述',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='IoT监控墙配置表';

-- 初始化默认监控墙
INSERT INTO `iot_monitor_wall` (`name`, `layout`, `camera_ids`, `is_default`, `description`, `creator`, `tenant_id`)
VALUES ('默认监控墙', '2x2', '[]', 1, '系统默认监控墙，可显示4路摄像头', 'admin', 1)
ON DUPLICATE KEY UPDATE `name` = `name`;

-- 为大华摄像头设备创建扩展信息（假设device_id=1，请根据实际情况修改）
-- 注意：执行前请先确认设备已创建，并修改device_id为实际值
/*
INSERT INTO `iot_camera` (`device_id`, `stream_url_main`, `stream_url_sub`, `rtsp_port`, `username`, `password`, `ptz_support`, `tenant_id`)
VALUES (
  1,  -- 请修改为实际的device_id
  'rtsp://192.168.1.202:554/cam/realmonitor?channel=1&subtype=0',
  'rtsp://192.168.1.202:554/cam/realmonitor?channel=1&subtype=1',
  554,
  'admin',
  'admin123',  -- 请使用加密后的密码
  0,
  1
);
*/

