-- =============================================
-- 回滚脚本：删除长辉(Changhui)设备数据库表
-- 警告：此操作将删除所有长辉相关数据，请谨慎执行！
-- =============================================

-- 删除升级任务表
DROP TABLE IF EXISTS `changhui_upgrade_task`;

-- 删除固件表
DROP TABLE IF EXISTS `changhui_firmware`;

-- 删除控制日志表
DROP TABLE IF EXISTS `changhui_control_log`;

-- 删除报警记录表
DROP TABLE IF EXISTS `changhui_alarm`;

-- 删除数据采集表
DROP TABLE IF EXISTS `changhui_data`;

-- 删除设备表
DROP TABLE IF EXISTS `changhui_device`;
