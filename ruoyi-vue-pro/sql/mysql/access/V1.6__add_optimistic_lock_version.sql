-- 添加乐观锁版本字段
-- 用于并发更新时的冲突检测，避免锁竞争

-- 为 iot_access_person_device_auth 表添加 version 字段
ALTER TABLE iot_access_person_device_auth 
ADD COLUMN version INT DEFAULT 0 COMMENT '乐观锁版本号';

-- 为 iot_access_auth_task_detail 表添加重试计数和错误信息字段
ALTER TABLE iot_access_auth_task_detail 
ADD COLUMN retry_count INT DEFAULT 0 COMMENT '重试次数',
ADD COLUMN last_error VARCHAR(500) COMMENT '最后错误信息';

-- 更新现有记录的 version 为 0
UPDATE iot_access_person_device_auth SET version = 0 WHERE version IS NULL;
