-- =============================================
-- 门禁授权操作日志字段扩展
-- Requirements: 12.1, 12.2, 12.3, 12.4
-- =============================================

-- 添加设备名称字段
ALTER TABLE `iot_access_operation_log` 
ADD COLUMN `device_name` varchar(128) DEFAULT NULL COMMENT '设备名称' AFTER `device_id`;

-- 添加授权操作扩展字段
ALTER TABLE `iot_access_operation_log` 
ADD COLUMN `target_person_id` bigint DEFAULT NULL COMMENT '目标人员ID（授权操作时使用）' AFTER `request_params`,
ADD COLUMN `target_person_code` varchar(64) DEFAULT NULL COMMENT '目标人员编号（授权操作时使用）' AFTER `target_person_id`,
ADD COLUMN `target_person_name` varchar(64) DEFAULT NULL COMMENT '目标人员姓名（授权操作时使用）' AFTER `target_person_code`,
ADD COLUMN `permission_group_id` bigint DEFAULT NULL COMMENT '权限组ID（授权操作时使用）' AFTER `target_person_name`,
ADD COLUMN `permission_group_name` varchar(128) DEFAULT NULL COMMENT '权限组名称（授权操作时使用）' AFTER `permission_group_id`,
ADD COLUMN `auth_task_id` bigint DEFAULT NULL COMMENT '授权任务ID（授权操作时使用）' AFTER `permission_group_name`,
ADD COLUMN `credential_types` varchar(128) DEFAULT NULL COMMENT '凭证类型列表（逗号分隔，如：FACE,CARD,FINGERPRINT）' AFTER `auth_task_id`,
ADD COLUMN `success_credential_count` int DEFAULT NULL COMMENT '成功的凭证类型数量' AFTER `credential_types`,
ADD COLUMN `failed_credential_count` int DEFAULT NULL COMMENT '失败的凭证类型数量' AFTER `success_credential_count`,
ADD COLUMN `sdk_error_code` int DEFAULT NULL COMMENT 'SDK错误码' AFTER `failed_credential_count`;

-- 添加索引以支持授权日志查询（Requirements: 12.4）
ALTER TABLE `iot_access_operation_log` 
ADD INDEX `idx_target_person_id` (`target_person_id`),
ADD INDEX `idx_auth_task_id` (`auth_task_id`),
ADD INDEX `idx_permission_group_id` (`permission_group_id`);

-- 添加result_desc字段（如果不存在）
-- 注意：此字段可能已存在，如果执行报错可忽略
-- ALTER TABLE `iot_access_operation_log` 
-- ADD COLUMN `result_desc` varchar(500) DEFAULT NULL COMMENT '结果描述' AFTER `result`;
