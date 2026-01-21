-- =====================================================
-- 调试 ptz_support 字段
-- =====================================================

USE ch_ibms;

-- 1. 查看所有通道的 ptz_support 状态
SELECT 
    id,
    device_id,
    channel_no,
    channel_name,
    channel_sub_type,
    target_ip,
    ptz_support,  -- ✅ 检查这个字段的值
    online_status
FROM iot_device_channel
WHERE device_id = 70  -- 替换为你的NVR ID
ORDER BY channel_no;

-- 2. 检查 ptz_support 字段的数据类型
SHOW COLUMNS FROM iot_device_channel LIKE 'ptz_support';

-- 3. 统计 ptz_support 的值分布
SELECT 
    ptz_support,
    COUNT(*) as count
FROM iot_device_channel
WHERE device_id = 70
GROUP BY ptz_support;

-- 4. 如果 ptz_support 都是 NULL 或 0，手动设置球机通道为支持云台
-- （根据通道名称或 channel_sub_type 判断）
UPDATE iot_device_channel
SET ptz_support = 1
WHERE device_id = 70
  AND (
    channel_sub_type LIKE '%PTZ%' 
    OR channel_sub_type LIKE '%DOME%'
    OR channel_name LIKE '%球机%'
    OR channel_name LIKE '%云台%'
  );

-- 5. 查看更新后的结果
SELECT 
    channel_no,
    channel_name,
    channel_sub_type,
    ptz_support
FROM iot_device_channel
WHERE device_id = 70
ORDER BY channel_no;
