-- =============================================
-- 快速修复：添加 iot_access_auth_task 表缺失的 person_id 字段
-- 执行时间: 2024-12-18
-- 问题: Unknown column 'person_id' in 'field list'
-- =============================================

-- 添加 person_id 字段
ALTER TABLE `iot_access_auth_task` 
ADD COLUMN IF NOT EXISTS `person_id` bigint DEFAULT NULL COMMENT '人员ID（单人下发/撤销时必填）' AFTER `group_id`;

-- 添加索引（如果不存在）
-- 注意：MySQL 8.0+ 支持 IF NOT EXISTS，低版本需要手动检查
CREATE INDEX IF NOT EXISTS `idx_person_id` ON `iot_access_auth_task` (`person_id`);
