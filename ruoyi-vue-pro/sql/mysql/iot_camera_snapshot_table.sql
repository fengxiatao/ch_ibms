-- ----------------------------
-- IoT 摄像头抓图记录表
-- ----------------------------

CREATE TABLE IF NOT EXISTS `iot_camera_snapshot` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `camera_id` bigint(20) NOT NULL COMMENT '摄像头ID',
  `device_id` bigint(20) NOT NULL COMMENT '设备ID',
  `device_name` varchar(128) DEFAULT NULL COMMENT '设备名称（冗余字段，便于查询）',
  `snapshot_url` varchar(512) NOT NULL COMMENT '快照URL',
  `snapshot_path` varchar(512) DEFAULT NULL COMMENT '快照文件路径',
  `file_size` bigint(20) DEFAULT 0 COMMENT '文件大小(字节)',
  `width` int(11) DEFAULT NULL COMMENT '图片宽度',
  `height` int(11) DEFAULT NULL COMMENT '图片高度',
  `capture_time` datetime NOT NULL COMMENT '抓拍时间',
  `snapshot_type` tinyint(4) NOT NULL DEFAULT 1 COMMENT '抓图类型(1:手动抓图 2:定时抓图 3:报警抓图 4:移动侦测抓图)',
  `trigger_event` varchar(128) DEFAULT NULL COMMENT '触发事件（用于报警抓图）',
  `event_type` varchar(64) DEFAULT NULL COMMENT '事件类型（motion_detected:移动侦测, alarm:报警等）',
  `description` varchar(512) DEFAULT NULL COMMENT '描述',
  `is_processed` tinyint(1) DEFAULT 0 COMMENT '是否已处理(0:未处理 1:已处理)',
  `processor` varchar(64) DEFAULT NULL COMMENT '处理人',
  `process_time` datetime DEFAULT NULL COMMENT '处理时间',
  `process_remark` varchar(512) DEFAULT NULL COMMENT '处理备注',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`),
  KEY `idx_camera_id` (`camera_id`),
  KEY `idx_device_id` (`device_id`),
  KEY `idx_capture_time` (`capture_time`),
  KEY `idx_snapshot_type` (`snapshot_type`),
  KEY `idx_event_type` (`event_type`),
  KEY `idx_is_processed` (`is_processed`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='IoT摄像头抓图记录表';

-- 插入示例数据（用于测试）
-- 注意：执行前请先确认设备已创建，并修改device_id和camera_id为实际值
/*
INSERT INTO `iot_camera_snapshot` 
(`camera_id`, `device_id`, `device_name`, `snapshot_url`, `file_size`, `width`, `height`, `capture_time`, `snapshot_type`, `event_type`, `description`, `tenant_id`)
VALUES
(1, 4, 'device_192_168_1_202', 'http://192.168.1.202/snapshot_2025_01_01_001.jpg', 65536, 1920, 1080, '2025-10-30 08:00:00', 1, NULL, '手动抓图测试', 1),
(1, 5, 'device_192_168_1_201', 'http://192.168.1.201/snapshot_2025_01_01_002.jpg', 73728, 1920, 1080, '2025-10-30 09:00:00', 1, NULL, '手动抓图测试', 1),
(1, 4, 'device_192_168_1_202', 'http://192.168.1.202/snapshot_2025_01_01_003.jpg', 71680, 1920, 1080, '2025-10-30 10:00:00', 3, 'motion_detected', '移动侦测触发抓图', 1),
(1, 5, 'device_192_168_1_201', 'http://192.168.1.201/snapshot_2025_01_01_004.jpg', 67584, 1920, 1080, '2025-10-30 11:00:00', 3, 'motion_detected', '移动侦测触发抓图', 1),
(1, 4, 'device_192_168_1_202', 'http://192.168.1.202/snapshot_2025_01_01_005.jpg', 69632, 1920, 1080, '2025-10-30 12:00:00', 2, NULL, '定时抓图', 1);
*/







