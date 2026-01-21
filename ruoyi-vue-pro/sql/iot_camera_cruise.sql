-- 摄像头巡航路线表
CREATE TABLE `iot_camera_cruise` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '巡航路线ID',
  `channel_id` bigint NOT NULL COMMENT '通道ID',
  `cruise_name` varchar(100) NOT NULL COMMENT '巡航路线名称',
  `description` varchar(500) DEFAULT NULL COMMENT '描述',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态（0:停止 1:运行中）',
  `dwell_time` int NOT NULL DEFAULT '5' COMMENT '每个预设点停留时间（秒）',
  `loop_enabled` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否循环（0:否 1:是）',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户编号',
  PRIMARY KEY (`id`),
  KEY `idx_channel_id` (`channel_id`) COMMENT '通道索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='摄像头巡航路线表';

-- 摄像头巡航点表
CREATE TABLE `iot_camera_cruise_point` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '巡航点ID',
  `cruise_id` bigint NOT NULL COMMENT '巡航路线ID',
  `preset_id` bigint NOT NULL COMMENT '预设点ID',
  `sort_order` int NOT NULL COMMENT '顺序（从1开始）',
  `dwell_time` int DEFAULT NULL COMMENT '停留时间（秒，为空则使用路线默认值）',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户编号',
  PRIMARY KEY (`id`),
  KEY `idx_cruise_id` (`cruise_id`) COMMENT '巡航路线索引',
  KEY `idx_preset_id` (`preset_id`) COMMENT '预设点索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='摄像头巡航点表';
