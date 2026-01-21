-- =====================================================
-- 快速修复 iot_access_operation_log 表缺失字段
-- 直接执行版本（忽略已存在的字段错误）
-- =====================================================

-- 添加所有缺失字段
ALTER TABLE `iot_access_operation_log` 
ADD COLUMN IF NOT EXISTS `device_name` varchar(128) DEFAULT NULL COMMENT '设备名称' AFTER `device_id`,
ADD COLUMN IF NOT EXISTS `result_desc` varchar(500) DEFAULT NULL COMMENT '结果描述' AFTER `result`,
ADD COLUMN IF NOT EXISTS `target_person_id` bigint DEFAULT NULL COMMENT '目标人员ID（授权操作时使用）' AFTER `request_params`,
ADD COLUMN IF NOT EXISTS `target_person_code` varchar(64) DEFAULT NULL COMMENT '目标人员编号' AFTER `target_person_id`,
ADD COLUMN IF NOT EXISTS `target_person_name` varchar(100) DEFAULT NULL COMMENT '目标人员姓名' AFTER `target_person_code`,
ADD COLUMN IF NOT EXISTS `permission_group_id` bigint DEFAULT NULL COMMENT '权限组ID' AFTER `target_person_name`,
ADD COLUMN IF NOT EXISTS `permission_group_name` varchar(100) DEFAULT NULL COMMENT '权限组名称' AFTER `permission_group_id`,
ADD COLUMN IF NOT EXISTS `auth_task_id` bigint DEFAULT NULL COMMENT '授权任务ID' AFTER `permission_group_name`,
ADD COLUMN IF NOT EXISTS `credential_types` varchar(200) DEFAULT NULL COMMENT '凭证类型列表（逗号分隔）' AFTER `auth_task_id`,
ADD COLUMN IF NOT EXISTS `success_credential_count` int DEFAULT NULL COMMENT '成功的凭证类型数量' AFTER `credential_types`,
ADD COLUMN IF NOT EXISTS `failed_credential_count` int DEFAULT NULL COMMENT '失败的凭证类型数量' AFTER `success_credential_count`,
ADD COLUMN IF NOT EXISTS `sdk_error_code` int DEFAULT NULL COMMENT 'SDK错误码' AFTER `failed_credential_count`;

-- 添加索引
ALTER TABLE `iot_access_operation_log` 
ADD INDEX IF NOT EXISTS `idx_target_person_id` (`target_person_id`),
ADD INDEX IF NOT EXISTS `idx_auth_task_id` (`auth_task_id`),
ADD INDEX IF NOT EXISTS `idx_permission_group_id` (`permission_group_id`);
