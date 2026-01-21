-- 修复 iot_access_event_log 表缺少的字段
-- 问题：实体类 IotAccessEventLogDO 中有 capture_url 等字段，但数据库表中没有

-- 添加 capture_url 字段
ALTER TABLE `iot_access_event_log` 
ADD COLUMN `capture_url` varchar(500) DEFAULT NULL COMMENT '抓拍图片URL（别名）' AFTER `snapshot_url`;

-- 添加其他可能缺少的字段
ALTER TABLE `iot_access_event_log` 
ADD COLUMN IF NOT EXISTS `device_name` varchar(100) DEFAULT NULL COMMENT '设备名称' AFTER `device_id`,
ADD COLUMN IF NOT EXISTS `channel_name` varchar(100) DEFAULT NULL COMMENT '通道名称' AFTER `channel_id`,
ADD COLUMN IF NOT EXISTS `person_code` varchar(50) DEFAULT NULL COMMENT '人员编号' AFTER `person_name`,
ADD COLUMN IF NOT EXISTS `verify_result_desc` varchar(200) DEFAULT NULL COMMENT '验证结果描述' AFTER `verify_result`,
ADD COLUMN IF NOT EXISTS `event_desc` varchar(500) DEFAULT NULL COMMENT '事件描述' AFTER `mask_status`,
ADD COLUMN IF NOT EXISTS `credential_type` varchar(50) DEFAULT NULL COMMENT '凭证类型' AFTER `event_desc`,
ADD COLUMN IF NOT EXISTS `credential_data` varchar(500) DEFAULT NULL COMMENT '凭证数据' AFTER `credential_type`,
ADD COLUMN IF NOT EXISTS `success` bit(1) DEFAULT NULL COMMENT '是否成功' AFTER `credential_data`;

-- 如果上面的 IF NOT EXISTS 语法不支持，可以使用下面的单独语句（忽略已存在的错误）
-- ALTER TABLE `iot_access_event_log` ADD COLUMN `device_name` varchar(100) DEFAULT NULL COMMENT '设备名称' AFTER `device_id`;
-- ALTER TABLE `iot_access_event_log` ADD COLUMN `channel_name` varchar(100) DEFAULT NULL COMMENT '通道名称' AFTER `channel_id`;
-- ALTER TABLE `iot_access_event_log` ADD COLUMN `person_code` varchar(50) DEFAULT NULL COMMENT '人员编号' AFTER `person_name`;
-- ALTER TABLE `iot_access_event_log` ADD COLUMN `verify_result_desc` varchar(200) DEFAULT NULL COMMENT '验证结果描述' AFTER `verify_result`;
-- ALTER TABLE `iot_access_event_log` ADD COLUMN `event_desc` varchar(500) DEFAULT NULL COMMENT '事件描述' AFTER `mask_status`;
-- ALTER TABLE `iot_access_event_log` ADD COLUMN `credential_type` varchar(50) DEFAULT NULL COMMENT '凭证类型' AFTER `event_desc`;
-- ALTER TABLE `iot_access_event_log` ADD COLUMN `credential_data` varchar(500) DEFAULT NULL COMMENT '凭证数据' AFTER `credential_type`;
-- ALTER TABLE `iot_access_event_log` ADD COLUMN `success` bit(1) DEFAULT NULL COMMENT '是否成功' AFTER `credential_data`;
