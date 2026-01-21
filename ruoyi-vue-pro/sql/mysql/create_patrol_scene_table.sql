-- =====================================================
-- 视频巡更场景配置表（独立表方案）
-- =====================================================

-- ----------------------------
-- 视频巡更场景表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `iot_video_patrol_scene` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '场景ID',
  `task_id` bigint(20) NOT NULL COMMENT '所属任务ID',
  `scene_name` varchar(128) NOT NULL COMMENT '场景名称',
  `scene_order` int(11) NOT NULL DEFAULT 0 COMMENT '场景顺序',
  `duration` int(11) NOT NULL DEFAULT 30 COMMENT '播放时长(秒)',
  `grid_layout` varchar(10) NOT NULL DEFAULT '2x2' COMMENT '分屏布局(1x1/2x2/3x3/4x4)',
  `grid_count` int(11) NOT NULL DEFAULT 4 COMMENT '格子数量',
  `channels` json DEFAULT NULL COMMENT '通道配置列表（JSON格式）',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`),
  KEY `idx_task_id` (`task_id`),
  KEY `idx_scene_order` (`scene_order`),
  KEY `idx_tenant_id` (`tenant_id`),
  CONSTRAINT `fk_patrol_scene_task` FOREIGN KEY (`task_id`) REFERENCES `iot_video_patrol_task` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='IoT视频巡更场景配置表';

-- 说明：
-- channels 字段存储格式示例：
-- [
--   {
--     "gridPosition": 1,
--     "cameraId": 123,
--     "cameraName": "前台摄像头",
--     "cameraCode": "CAM001",
--     "streamUrl": "rtsp://...",
--     "streamType": 1
--   }
-- ]
