-- =============================================
-- 科鼎协议回滚为德通协议 - 数据库回滚脚本
-- 版本: V4.0 Rollback
-- 日期: 2025-12-21
-- 说明: 将所有 iot_keding_* 表回滚为 iot_detong_*
-- =============================================

-- 1. 回滚设备表
RENAME TABLE `iot_keding_device` TO `iot_detong_device`;

-- 2. 回滚报警记录表
RENAME TABLE `iot_keding_alarm` TO `iot_detong_alarm`;

-- 3. 回滚控制记录表
RENAME TABLE `iot_keding_control_log` TO `iot_detong_control_log`;

-- 4. 回滚固件表
RENAME TABLE `iot_keding_firmware` TO `iot_detong_firmware`;

-- 5. 回滚升级任务表
RENAME TABLE `iot_keding_upgrade_task` TO `iot_detong_upgrade_task`;

-- 6. 回滚升级日志表
RENAME TABLE `iot_keding_upgrade_log` TO `iot_detong_upgrade_log`;

-- 7. 恢复表注释
ALTER TABLE `iot_detong_device` COMMENT = '德通设备表';
ALTER TABLE `iot_detong_alarm` COMMENT = '德通报警记录表';
ALTER TABLE `iot_detong_control_log` COMMENT = '德通控制记录表';
ALTER TABLE `iot_detong_firmware` COMMENT = '德通固件表';
ALTER TABLE `iot_detong_upgrade_task` COMMENT = '德通升级任务表';
ALTER TABLE `iot_detong_upgrade_log` COMMENT = '德通升级日志表';

-- 验证回滚结果
SELECT 'Rollback completed. Verifying tables...' AS status;
SELECT TABLE_NAME, TABLE_COMMENT 
FROM information_schema.TABLES 
WHERE TABLE_SCHEMA = DATABASE() 
  AND TABLE_NAME LIKE 'iot_detong_%'
ORDER BY TABLE_NAME;
