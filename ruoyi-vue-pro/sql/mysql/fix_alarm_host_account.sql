-- 修复报警主机的 account 字段
-- 问题：iot_alarm_host 表中 account 字段为 NULL，导致查询主机状态失败

-- 1. 查看当前报警主机记录
SELECT id, host_name, device_id, account, ip_address, location
FROM iot_alarm_host
WHERE id = 1;

-- 2. 更新 account 字段为 '1234'
UPDATE iot_alarm_host
SET account = '1234',
    updater = 'system',
    update_time = NOW()
WHERE id = 1;

-- 3. 验证更新结果
SELECT id, host_name, device_id, account, ip_address, location
FROM iot_alarm_host
WHERE id = 1;

-- 4. 如果有多个报警主机需要修复，可以根据 device_id 关联查询
-- 从 iot_device 表中获取 device_key，更新到 iot_alarm_host.account
UPDATE iot_alarm_host ah
INNER JOIN iot_device d ON ah.device_id = d.id
SET ah.account = d.device_key,
    ah.updater = 'system',
    ah.update_time = NOW()
WHERE ah.account IS NULL
  AND d.device_key IS NOT NULL;

-- 5. 最终验证
SELECT 
    ah.id AS host_id,
    ah.host_name,
    ah.account AS host_account,
    ah.device_id,
    d.device_name,
    d.device_key,
    ah.ip_address,
    ah.location
FROM iot_alarm_host ah
LEFT JOIN iot_device d ON ah.device_id = d.id
WHERE ah.id = 1;
