-- =====================================================
-- IBMS 单台账迁移：物理重命名
--   iot_device_message              -> ibms_device_message
--   iot_device_property_history    -> ibms_device_property_history
--
-- 适用前提：
-- 1) 建议先停止应用（避免运行中同时读写两套表名）
-- 2) 当前库（DATABASE()）里至少存在 iot_* 两张旧表时，下面会直接 RENAME
-- 3) 若旧表不存在而新表不存在，可先执行：
--      sql/mysql/iot_device_message.sql
--    （当前脚本已更新为 CREATE ibms_* 表）
--
-- 回滚：
-- 重新把 ibms_* rename 回 iot_* 即可（见文件末尾“Rollback”段）
-- =====================================================

-- 先迁移消息表
RENAME TABLE
    iot_device_message TO ibms_device_message;

-- 再迁移属性历史表
RENAME TABLE
    iot_device_property_history TO ibms_device_property_history;

-- =====================================================
-- Rollback（回滚片段）
-- =====================================================
-- 取消注释后执行（前提：ibms_* 表存在且 iot_* 表名不会冲突）
-- RENAME TABLE
--     ibms_device_message TO iot_device_message;
-- RENAME TABLE
--     ibms_device_property_history TO iot_device_property_history;

