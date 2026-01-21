-- =====================================================
-- 修复通道3的云台支持标识
-- =====================================================
-- 
-- 问题：SDK可以获取到通道3支持云台，但数据库中ptz_support=0
-- 原因：同步时SDK返回的config中没有ptzSupport字段，导致使用默认值false
-- 
-- =====================================================

USE ch_ibms;

-- 1. 查看当前通道3的状态
SELECT 
    id,
    channel_no,
    channel_name,
    target_ip,
    ptz_support,
    last_sync_time
FROM iot_device_channel
WHERE device_id = 70 AND channel_no = 3;

-- 2. 修复通道3的ptz_support
UPDATE iot_device_channel
SET ptz_support = 1
WHERE device_id = 70 
  AND channel_no = 3;

-- 3. 验证修复结果
SELECT 
    id,
    channel_no,
    channel_name,
    target_ip,
    ptz_support,
    last_sync_time
FROM iot_device_channel
WHERE device_id = 70 AND channel_no = 3;

-- 4. （可选）如果有其他球机通道，也一起修复
-- 根据target_ip或channel_name来识别球机
UPDATE iot_device_channel
SET ptz_support = 1
WHERE device_id = 70
  AND (
    -- 方式1：根据IP地址（如果你知道哪些IP是球机）
    target_ip IN ('192.168.1.202', '192.168.1.201')
    
    -- 方式2：根据通道名称包含关键字
    OR channel_name LIKE '%PTZ%'
    OR channel_name LIKE '%DOME%'
    OR channel_name LIKE '%球机%'
    OR channel_name LIKE '%云台%'
  );

-- 5. 查看所有通道的ptz_support状态
SELECT 
    channel_no,
    channel_name,
    target_ip,
    ptz_support,
    CASE 
        WHEN ptz_support = 1 THEN '✅ 支持云台'
        ELSE '❌ 不支持云台'
    END as ptz_status
FROM iot_device_channel
WHERE device_id = 70
ORDER BY channel_no;
