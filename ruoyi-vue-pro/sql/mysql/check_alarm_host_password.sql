-- 查询报警主机的密码配置
SELECT 
    id,
    host_name,
    account,
    password,
    ip_address,
    port
FROM iot_alarm_host
WHERE account = '1234'
AND deleted = 0;

-- 如果密码为空，更新为常见密码进行测试
-- UPDATE iot_alarm_host SET password = '123456' WHERE account = '1234';
-- UPDATE iot_alarm_host SET password = '888888' WHERE account = '1234';
-- UPDATE iot_alarm_host SET password = '000000' WHERE account = '1234';
