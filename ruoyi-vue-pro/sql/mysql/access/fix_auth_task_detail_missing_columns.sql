-- =============================================
-- 修复 iot_access_auth_task_detail 表缺失字段
-- 问题: DO 类中有 last_error 字段，但数据库表中缺失
-- 执行时间: 2024-12-18
-- =============================================

-- 添加 last_error 字段（如果不存在）
-- 注意：此脚本已执行，last_error 字段已添加
ALTER TABLE `iot_access_auth_task_detail` 
ADD COLUMN `last_error` varchar(500) DEFAULT NULL COMMENT '最后错误信息（用于重试场景）' AFTER `retry_count`;

-- 验证字段是否存在
SELECT COLUMN_NAME, DATA_TYPE, COLUMN_COMMENT 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE() 
AND TABLE_NAME = 'iot_access_auth_task_detail' 
AND COLUMN_NAME = 'last_error';
