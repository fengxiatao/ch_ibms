-- =============================================
-- 【可选 / 高风险】IoT 旧表重命名为 _bak（cleanup-verify 用）
-- =============================================
-- 仅在业务已完全切到 ibms_* 且已备份库的前提下，由 DBA 在维护窗口手工执行。
-- 默认不要执行：重命名会导致仍依赖 iot_* 的代码立刻失败。
-- 执行前请确认无 FK 指向下列表；若有需先删外键。
--
-- RENAME TABLE iot_device TO iot_device_bak;
-- RENAME TABLE iot_product TO iot_product_bak;
-- RENAME TABLE iot_device_channel TO iot_device_channel_bak;
-- （按需继续列出其它仅旧链路使用的表）
-- =============================================

SELECT 1 AS skip_placeholder;
