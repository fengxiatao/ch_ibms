-- 快速修复：添加 version 字段到 iot_access_person_device_auth 表
-- 直接执行此脚本即可修复 "Unknown column 'version' in 'field list'" 错误

ALTER TABLE `iot_access_person_device_auth` 
ADD COLUMN IF NOT EXISTS `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号' AFTER `credential_hash`;

-- 如果上面的语法不支持，使用下面的语句：
-- ALTER TABLE `iot_access_person_device_auth` ADD COLUMN `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号' AFTER `credential_hash`;
