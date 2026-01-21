-- 为报警主机表添加系统状态字段
-- 兼容MySQL 5.7和8.0所有版本

-- 添加 system_status 字段
ALTER TABLE `iot_alarm_host` 
ADD COLUMN `system_status` TINYINT DEFAULT 0 COMMENT '系统状态：0=撤防，1=布防，2=居家布防' AFTER `alarm_status`;

-- 添加 last_query_time 字段
ALTER TABLE `iot_alarm_host` 
ADD COLUMN `last_query_time` DATETIME DEFAULT NULL COMMENT '最后查询时间' AFTER `system_status`;
