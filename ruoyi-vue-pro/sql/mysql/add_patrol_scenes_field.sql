-- =====================================================
-- 为视频巡更任务表添加场景配置字段
-- =====================================================

-- 添加场景配置字段
ALTER TABLE `iot_video_patrol_task` 
ADD COLUMN `patrol_scenes` json DEFAULT NULL COMMENT '场景配置列表（JSON格式，包含多个场景及其摄像头配置）' 
AFTER `task_code`;

-- 说明：
-- patrol_scenes 字段存储格式示例：
-- [
--   {
--     "sceneId": 1,
--     "sceneName": "场景1",
--     "sceneOrder": 0,
--     "duration": 30,
--     "gridLayout": "2x2",
--     "gridCount": 4,
--     "channels": [
--       {
--         "gridPosition": 1,
--         "cameraId": 123,
--         "cameraName": "前台摄像头",
--         "cameraCode": "CAM001",
--         "streamUrl": "rtsp://...",
--         "streamType": 1
--       }
--     ]
--   }
-- ]
