-- ========================================
-- 清理 iot_device_channel 表中的重复数据
-- ========================================

-- 1. 查找重复的记录（包括已删除的）
SELECT 
    device_id,
    channel_no,
    tenant_id,
    COUNT(*) as count,
    GROUP_CONCAT(id ORDER BY id) as ids,
    GROUP_CONCAT(deleted ORDER BY id) as deleted_flags
FROM iot_device_channel
GROUP BY device_id, channel_no, tenant_id
HAVING COUNT(*) > 1
ORDER BY count DESC;

-- 2. 查看具体的重复记录详情
SELECT 
    id,
    device_id,
    channel_no,
    tenant_id,
    deleted,
    create_time,
    update_time
FROM iot_device_channel
WHERE (device_id, channel_no, tenant_id) IN (
    SELECT device_id, channel_no, tenant_id
    FROM iot_device_channel
    GROUP BY device_id, channel_no, tenant_id
    HAVING COUNT(*) > 1
)
ORDER BY device_id, channel_no, id;

-- 3. 删除重复的已删除记录（保留最新的一条）
-- 注意：这会物理删除数据，请先备份！
DELETE t1 FROM iot_device_channel t1
INNER JOIN iot_device_channel t2 
WHERE t1.device_id = t2.device_id 
  AND t1.channel_no = t2.channel_no 
  AND t1.tenant_id = t2.tenant_id
  AND t1.deleted = 1
  AND t2.deleted = 1
  AND t1.id < t2.id;  -- 保留ID较大的（较新的）记录

-- 4. 处理未删除记录的重复（如果存在）
-- 如果有多条未删除的重复记录，保留最新的一条，其他的标记为删除
UPDATE iot_device_channel t1
INNER JOIN (
    SELECT 
        device_id,
        channel_no,
        tenant_id,
        MAX(id) as max_id
    FROM iot_device_channel
    WHERE deleted = 0
    GROUP BY device_id, channel_no, tenant_id
    HAVING COUNT(*) > 1
) t2 ON t1.device_id = t2.device_id 
    AND t1.channel_no = t2.channel_no 
    AND t1.tenant_id = t2.tenant_id
SET t1.deleted = 1
WHERE t1.id < t2.max_id
  AND t1.deleted = 0;

-- 5. 再次检查是否还有重复（应该只有未删除的记录，每组一条）
SELECT 
    device_id,
    channel_no,
    tenant_id,
    COUNT(*) as count
FROM iot_device_channel
WHERE deleted = 0
GROUP BY device_id, channel_no, tenant_id
HAVING COUNT(*) > 1;

-- 6. 如果没有重复了，创建唯一键约束
ALTER TABLE iot_device_channel 
ADD UNIQUE KEY uk_device_channel (device_id, channel_no, tenant_id);

-- 7. 验证
SHOW INDEX FROM iot_device_channel WHERE Key_name = 'uk_device_channel';
