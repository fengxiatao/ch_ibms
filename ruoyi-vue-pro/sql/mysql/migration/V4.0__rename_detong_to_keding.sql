-- =============================================
-- 德通协议重命名为科鼎协议 - 数据库迁移脚本
-- 版本: V4.0
-- 日期: 2025-12-21
-- 说明: 将所有 iot_detong_* 表重命名为 iot_keding_*
-- =============================================

-- 1. 重命名设备表
RENAME TABLE `iot_detong_device` TO `iot_keding_device`;

-- 2. 重命名报警记录表
RENAME TABLE `iot_detong_alarm` TO `iot_keding_alarm`;

-- 3. 重命名控制记录表
RENAME TABLE `iot_detong_control_log` TO `iot_keding_control_log`;

-- 4. 重命名固件表
RENAME TABLE `iot_detong_firmware` TO `iot_keding_firmware`;

-- 5. 重命名升级任务表
RENAME TABLE `iot_detong_upgrade_task` TO `iot_keding_upgrade_task`;

-- 6. 重命名升级日志表
RENAME TABLE `iot_detong_upgrade_log` TO `iot_keding_upgrade_log`;

-- 7. 更新表注释（可选，需要MySQL 8.0+）
ALTER TABLE `iot_keding_device` COMMENT = '科鼎设备表';
ALTER TABLE `iot_keding_alarm` COMMENT = '科鼎报警记录表';
ALTER TABLE `iot_keding_control_log` COMMENT = '科鼎控制记录表';
ALTER TABLE `iot_keding_firmware` COMMENT = '科鼎固件表';
ALTER TABLE `iot_keding_upgrade_task` COMMENT = '科鼎升级任务表';
ALTER TABLE `iot_keding_upgrade_log` COMMENT = '科鼎升级日志表';

-- 验证迁移结果
SELECT 'Migration completed. Verifying tables...' AS status;
SELECT TABLE_NAME, TABLE_COMMENT 
FROM information_schema.TABLES 
WHERE TABLE_SCHEMA = DATABASE() 
  AND TABLE_NAME LIKE 'iot_keding_%'
ORDER BY TABLE_NAME;
