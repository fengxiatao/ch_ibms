-- 快速修复 iot_access_event_log 表缺少的字段
-- 执行前请确保连接到正确的数据库

-- 1. 添加 capture_url 字段（这是导致错误的主要字段）
ALTER TABLE `iot_access_event_log` ADD COLUMN `capture_url` varchar(500) DEFAULT NULL COMMENT '抓拍图片URL（别名）';

-- 2. 添加 device_name 字段
ALTER TABLE `iot_access_event_log` ADD COLUMN `device_name` varchar(100) DEFAULT NULL COMMENT '设备名称';

-- 3. 添加 channel_name 字段
ALTER TABLE `iot_access_event_log` ADD COLUMN `channel_name` varchar(100) DEFAULT NULL COMMENT '通道名称';

-- 4. 添加 person_code 字段
ALTER TABLE `iot_access_event_log` ADD COLUMN `person_code` varchar(50) DEFAULT NULL COMMENT '人员编号';

-- 5. 添加 verify_result_desc 字段
ALTER TABLE `iot_access_event_log` ADD COLUMN `verify_result_desc` varchar(200) DEFAULT NULL COMMENT '验证结果描述';

-- 6. 添加 event_desc 字段
ALTER TABLE `iot_access_event_log` ADD COLUMN `event_desc` varchar(500) DEFAULT NULL COMMENT '事件描述';

-- 7. 添加 credential_type 字段
ALTER TABLE `iot_access_event_log` ADD COLUMN `credential_type` varchar(50) DEFAULT NULL COMMENT '凭证类型';

-- 8. 添加 credential_data 字段
ALTER TABLE `iot_access_event_log` ADD COLUMN `credential_data` varchar(500) DEFAULT NULL COMMENT '凭证数据';

-- 9. 添加 success 字段
ALTER TABLE `iot_access_event_log` ADD COLUMN `success` bit(1) DEFAULT NULL COMMENT '是否成功';
