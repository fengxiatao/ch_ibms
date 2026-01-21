-- 重构防区状态字段：使用枚举替代 Boolean 字段

-- 1. 添加新的枚举字段
ALTER TABLE `iot_alarm_zone` 
ADD COLUMN `arm_status` tinyint NOT NULL DEFAULT 0 COMMENT '布防状态枚举：0-撤防，1-布防，2-旁路' AFTER `status_name`;

ALTER TABLE `iot_alarm_zone` 
ADD COLUMN `alarm_status` tinyint NOT NULL DEFAULT 0 COMMENT '报警状态枚举：0-正常，1-报警中，11-17各类报警' AFTER `arm_status`;
