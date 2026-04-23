-- ============================================================================
-- 将「发现设备」物理表从 iot_discovered_device 重命名为 ibms_discovered_device
-- （仅改表名，列、索引、数据不变，与 IbmsDiscoveredDeviceDO 对齐）
--
-- 执行前：
--   1. 备份数据库
--   2. 确认不存在同名表 ibms_discovered_device
--
-- 若已为重命名后的库，请勿重复执行（会报表不存在）。
-- ============================================================================

USE ch_ibms;

RENAME TABLE `iot_discovered_device` TO `ibms_discovered_device`;
