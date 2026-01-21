-- =============================================
-- 摄像头抓图/录像表 媒体增强字段与索引 - 数据库变更脚本
-- 版本：v1.0
-- 日期：2025-11-13
-- =============================================

USE ch_ibms;

-- ========== iot_camera_snapshot 字段增强 ==========
ALTER TABLE iot_camera_snapshot
ADD COLUMN IF NOT EXISTS channel TINYINT NULL COMMENT '通道号' AFTER device_name,
ADD COLUMN IF NOT EXISTS stream_type TINYINT NULL COMMENT '码流类型(0:子码流 1:主码流)' AFTER channel,
ADD COLUMN IF NOT EXISTS vendor VARCHAR(32) NULL COMMENT '厂商' AFTER stream_type,
ADD COLUMN IF NOT EXISTS source VARCHAR(32) NULL COMMENT '来源(h5,upload,gateway,onvif,...)' AFTER vendor,
ADD COLUMN IF NOT EXISTS operator_user_id BIGINT NULL COMMENT '操作人用户ID' AFTER source,
ADD COLUMN IF NOT EXISTS file_hash VARCHAR(64) NULL COMMENT '文件哈希(sha256)' AFTER snapshot_path,
ADD COLUMN IF NOT EXISTS storage_config_id BIGINT NULL COMMENT '存储配置ID' AFTER file_hash,
ADD COLUMN IF NOT EXISTS thumbnail_url VARCHAR(512) NULL COMMENT '缩略图URL' AFTER snapshot_url,
ADD COLUMN IF NOT EXISTS extra JSON NULL COMMENT '扩展字段' AFTER process_remark;

-- 索引
ALTER TABLE iot_camera_snapshot
ADD INDEX IF NOT EXISTS idx_device_time (device_id, capture_time),
ADD INDEX IF NOT EXISTS idx_camera_time (camera_id, capture_time),
ADD INDEX IF NOT EXISTS idx_snapshot_type (snapshot_type),
ADD INDEX IF NOT EXISTS idx_event_type (event_type),
ADD INDEX IF NOT EXISTS idx_processed (is_processed);

-- ========== iot_camera_recording 字段增强 ==========
ALTER TABLE iot_camera_recording
ADD COLUMN IF NOT EXISTS channel TINYINT NULL COMMENT '通道号' AFTER camera_id,
ADD COLUMN IF NOT EXISTS stream_type TINYINT NULL COMMENT '码流类型(0:子码流 1:主码流)' AFTER channel,
ADD COLUMN IF NOT EXISTS video_codec VARCHAR(16) NULL COMMENT '视频编码' AFTER file_url,
ADD COLUMN IF NOT EXISTS audio_codec VARCHAR(16) NULL COMMENT '音频编码' AFTER video_codec,
ADD COLUMN IF NOT EXISTS resolution_w INT NULL COMMENT '分辨率宽' AFTER audio_codec,
ADD COLUMN IF NOT EXISTS resolution_h INT NULL COMMENT '分辨率高' AFTER resolution_w,
ADD COLUMN IF NOT EXISTS frame_rate DECIMAL(6,2) NULL COMMENT '帧率' AFTER resolution_h,
ADD COLUMN IF NOT EXISTS bit_rate INT NULL COMMENT '码率(bps)' AFTER frame_rate,
ADD COLUMN IF NOT EXISTS has_audio TINYINT NULL COMMENT '是否含音频(0/1)' AFTER bit_rate,
ADD COLUMN IF NOT EXISTS vendor VARCHAR(32) NULL COMMENT '厂商' AFTER has_audio,
ADD COLUMN IF NOT EXISTS source VARCHAR(32) NULL COMMENT '来源(h5,upload,gateway,onvif,...)' AFTER vendor,
ADD COLUMN IF NOT EXISTS operator_user_id BIGINT NULL COMMENT '操作人用户ID' AFTER source,
ADD COLUMN IF NOT EXISTS file_hash VARCHAR(64) NULL COMMENT '文件哈希(sha256)' AFTER file_path,
ADD COLUMN IF NOT EXISTS storage_config_id BIGINT NULL COMMENT '存储配置ID' AFTER file_hash,
ADD COLUMN IF NOT EXISTS thumbnail_url VARCHAR(512) NULL COMMENT '缩略图URL' AFTER file_url,
ADD COLUMN IF NOT EXISTS extra JSON NULL COMMENT '扩展字段' AFTER error_msg;

-- 索引
ALTER TABLE iot_camera_recording
ADD INDEX IF NOT EXISTS idx_camera_start (camera_id, start_time),
ADD INDEX IF NOT EXISTS idx_status (status),
ADD INDEX IF NOT EXISTS idx_recording_type (recording_type);

-- =============================================
-- 脚本执行完成
-- =============================================
SELECT '✅ 媒体增强字段与索引已应用（如不存在则新增）。' AS 执行结果;
