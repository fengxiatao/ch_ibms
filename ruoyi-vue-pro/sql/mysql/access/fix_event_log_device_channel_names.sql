-- 修复事件日志中缺失的设备名称和通道名称
-- 执行此脚本前请先备份数据

-- 1. 更新设备名称
UPDATE iot_access_event_log e
INNER JOIN iot_device d ON e.device_id = d.id
SET e.device_name = d.device_name
WHERE e.device_name IS NULL OR e.device_name = '';

-- 2. 更新通道名称（通过 channel_id 关联）
UPDATE iot_access_event_log e
INNER JOIN iot_device_channel c ON e.channel_id = c.id
SET e.channel_name = c.channel_name
WHERE (e.channel_name IS NULL OR e.channel_name = '') AND e.channel_id IS NOT NULL;

-- 3. 更新通道名称（通过 device_id + channel_no 关联，如果 channel_id 存储的是 channel_no）
UPDATE iot_access_event_log e
INNER JOIN iot_device_channel c ON e.device_id = c.device_id AND e.channel_id = c.channel_no
SET e.channel_name = c.channel_name
WHERE (e.channel_name IS NULL OR e.channel_name = '') AND e.channel_id IS NOT NULL;

-- 4. 验证更新结果
SELECT 
    COUNT(*) as total_events,
    SUM(CASE WHEN device_name IS NOT NULL AND device_name != '' THEN 1 ELSE 0 END) as with_device_name,
    SUM(CASE WHEN channel_name IS NOT NULL AND channel_name != '' THEN 1 ELSE 0 END) as with_channel_name
FROM iot_access_event_log;
