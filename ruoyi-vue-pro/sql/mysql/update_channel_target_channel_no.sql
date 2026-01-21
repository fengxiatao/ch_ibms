-- ============================================
-- 更新 iot_device_channel 表的 target_channel_no
-- ============================================
-- 说明：
-- 1. 对于有 target_ip 的通道（通过NVR接入的IPC），设置 target_channel_no = 1
-- 2. IPC 通常只有一个通道，所以默认为1
-- ============================================

USE ch_ibms;

-- 更新所有有 target_ip 但 target_channel_no 为 NULL 的记录
UPDATE iot_device_channel
SET target_channel_no = 1
WHERE target_ip IS NOT NULL 
  AND target_ip != ''
  AND target_channel_no IS NULL;

-- 查看更新结果
SELECT 
    id,
    device_id,
    channel_no,
    channel_name,
    target_ip,
    target_channel_no,
    CONCAT('rtsp://admin:admin123@', target_ip, ':554/cam/realmonitor?channel=', 
           IFNULL(target_channel_no, 1), '&subtype=0') AS generated_url
FROM iot_device_channel
WHERE device_id = 70
ORDER BY channel_no;

-- 验证：检查是否还有需要更新的记录
SELECT COUNT(*) AS need_update_count
FROM iot_device_channel
WHERE target_ip IS NOT NULL 
  AND target_ip != ''
  AND target_channel_no IS NULL;
