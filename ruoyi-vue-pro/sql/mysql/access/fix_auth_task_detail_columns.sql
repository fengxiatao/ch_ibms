-- =============================================
-- 快速修复：添加 iot_access_auth_task_detail 表缺失的字段
-- 问题：Unknown column 'person_code' in 'field list'
-- 执行时间：2025-12-18
-- 状态：已执行
-- =============================================

-- 一次性添加所有缺失字段（如果字段已存在会报错，可忽略）
ALTER TABLE `iot_access_auth_task_detail` 
ADD COLUMN `person_code` varchar(64) DEFAULT NULL COMMENT '人员编号' AFTER `person_id`,
ADD COLUMN `person_name` varchar(64) DEFAULT NULL COMMENT '人员姓名' AFTER `person_code`,
ADD COLUMN `device_name` varchar(128) DEFAULT NULL COMMENT '设备名称' AFTER `device_id`,
ADD COLUMN `credential_types` varchar(128) DEFAULT NULL COMMENT '下发的凭证类型，逗号分隔' AFTER `error_message`;

-- 验证字段是否添加成功
-- SHOW COLUMNS FROM iot_access_auth_task_detail;
