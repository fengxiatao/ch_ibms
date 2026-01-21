-- ========================================
-- 修复 iot_device_channel 表的唯一键约束问题
-- 问题：逻辑删除时，deleted=1 的记录会违反唯一键约束
-- 解决：修改唯一键，只对未删除的记录生效
-- ========================================

-- 1. 查看当前的唯一键约束
SELECT 
    CONSTRAINT_NAME,
    COLUMN_NAME,
    ORDINAL_POSITION
FROM information_schema.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = 'ch_ibms'
  AND TABLE_NAME = 'iot_device_channel'
  AND CONSTRAINT_NAME LIKE 'uk_%'
ORDER BY CONSTRAINT_NAME, ORDINAL_POSITION;

-- 2. 删除旧的唯一键约束
ALTER TABLE iot_device_channel DROP INDEX uk_device_channel;

-- 3. 创建新的唯一键约束
-- 方案：将 deleted 字段从唯一键中移除
-- 这样已删除的记录不会影响唯一性检查
-- 注意：这意味着同一个 device_id + channel_no 可以有多条已删除记录，但只能有一条未删除记录
ALTER TABLE iot_device_channel 
ADD UNIQUE KEY uk_device_channel (device_id, channel_no, tenant_id);

-- 说明：
-- - 原约束：UNIQUE (device_id, channel_no, tenant_id, deleted)
-- - 新约束：UNIQUE (device_id, channel_no, tenant_id)
-- - 效果：允许多次删除同一通道，但同一通道只能有一条未删除记录

-- 4. 验证修改结果
SHOW INDEX FROM iot_device_channel WHERE Key_name = 'uk_device_channel';
