-- 视频巡检任务表
CREATE TABLE IF NOT EXISTS `iot_video_inspection_task` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '任务ID',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户ID',
    `task_name` VARCHAR(100) NOT NULL COMMENT '任务名称',
    `layout` VARCHAR(10) NOT NULL COMMENT '分屏布局（1x1, 2x2, 3x3, 4x4等）',
    `scenes` JSON COMMENT '场景配置（JSON格式）',
    `status` VARCHAR(20) DEFAULT 'draft' COMMENT '任务状态（draft-草稿, active-运行中, paused-已暂停）',
    `creator` VARCHAR(64) DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` VARCHAR(64) DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` BIT(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`),
    INDEX `idx_tenant_id` (`tenant_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='视频巡检任务表';

-- 场景配置JSON结构示例：
-- [
--   {
--     "cellIndex": 0,
--     "channels": [
--       {
--         "deviceId": 1,
--         "channelId": 1,
--         "channelName": "大厅入口",
--         "duration": 10,
--         "ip": "192.168.1.100",
--         "productId": 1,
--         "config": "{}",
--         "streamUrl": "rtsp://...",
--         "nvrId": 1,
--         "channelNo": 1,
--         "location": "一楼大厅",
--         "snapshot": "http://..."
--       }
--     ]
--   }
-- ]
