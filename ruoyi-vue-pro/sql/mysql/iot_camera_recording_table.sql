-- ----------------------------
-- IoT 摄像头录像记录表
-- ----------------------------

CREATE TABLE IF NOT EXISTS `iot_camera_recording` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `camera_id` bigint(20) NOT NULL COMMENT '摄像头ID（关联iot_device表）',
  `start_time` datetime NOT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `duration` int(11) DEFAULT 0 COMMENT '时长(秒)',
  `file_path` varchar(512) DEFAULT NULL COMMENT '文件路径（服务器存储路径）',
  `file_size` bigint(20) DEFAULT 0 COMMENT '文件大小(字节)',
  `file_url` varchar(512) DEFAULT NULL COMMENT '文件访问URL',
  `recording_type` tinyint(4) NOT NULL DEFAULT 1 COMMENT '录像类型(1:手动 2:定时 3:报警触发 4:移动侦测)',
  `status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '状态(0:录像中 1:已完成 2:已停止 3:异常)',
  `error_msg` varchar(512) DEFAULT NULL COMMENT '错误信息',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`),
  KEY `idx_camera_id` (`camera_id`),
  KEY `idx_start_time` (`start_time`),
  KEY `idx_recording_type` (`recording_type`),
  KEY `idx_status` (`status`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='IoT摄像头录像记录表';

-- 插入示例数据（用于测试）
-- 注意：执行前请先确认设备已创建，并修改camera_id为实际值
/*
INSERT INTO `iot_camera_recording` 
(`camera_id`, `start_time`, `end_time`, `duration`, `file_path`, `file_size`, `file_url`, `recording_type`, `status`, `tenant_id`)
VALUES
(4, '2025-10-30 08:00:00', '2025-10-30 09:00:00', 3600, '/recordings/4/20251030/recording_1730255600000_4.mp4', 524288000, 'http://192.168.1.246:8080/recordings/4/20251030/recording_1730255600000_4.mp4', 1, 1, 1),
(5, '2025-10-30 10:00:00', '2025-10-30 11:00:00', 3600, '/recordings/5/20251030/recording_1730262800000_5.mp4', 536870912, 'http://192.168.1.246:8080/recordings/5/20251030/recording_1730262800000_5.mp4', 1, 1, 1),
(4, '2025-10-30 14:00:00', '2025-10-30 15:00:00', 3600, '/recordings/4/20251030/recording_1730277200000_4.mp4', 519430144, 'http://192.168.1.246:8080/recordings/4/20251030/recording_1730277200000_4.mp4', 2, 1, 1),
(5, '2025-10-30 16:00:00', NULL, 0, '/recordings/5/20251030/recording_1730284400000_5.mp4', 0, 'http://192.168.1.246:8080/recordings/5/20251030/recording_1730284400000_5.mp4', 1, 0, 1);
*/







