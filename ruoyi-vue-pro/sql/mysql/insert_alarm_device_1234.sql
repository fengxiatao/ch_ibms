-- 插入报警主机设备记录（完整版）
-- 包含 iot_device 和 iot_alarm_host 两张表

USE ch_ibms;

-- ==================== 第一步：插入设备记录 ====================
-- 注意：报警主机需要先在 iot_device 表中创建设备记录

INSERT INTO iot_device (
    id,
    device_key,
    device_name,
    product_id,
    device_type,
    gateway_id,
    status,
    status_last_update_time,
    active_time,
    ip,
    firmware_version,
    device_secret,
    mqtt_client_id,
    mqtt_username,
    mqtt_password,
    auth_type,
    latitude,
    longitude,
    area_id,
    address,
    creator,
    create_time,
    updater,
    update_time,
    deleted,
    tenant_id
) VALUES (
    1001,                           -- id（设备ID，需要唯一）
    '1234',                         -- device_key（设备标识，对应报警主机的account）⭐
    '报警主机-1234',                -- device_name
    1,                              -- product_id（需要先创建产品，或使用已有产品ID）
    1,                              -- device_type（1-直连设备）
    NULL,                           -- gateway_id（直连设备为NULL）
    1,                              -- status（1-在线，0-离线）
    NOW(),                          -- status_last_update_time
    NOW(),                          -- active_time
    '192.168.1.210',                -- ip（报警主机IP）
    'IP9500-V1.8',                  -- firmware_version
    'alarm_secret_1234',            -- device_secret
    NULL,                           -- mqtt_client_id（报警主机使用TCP，不需要MQTT）
    NULL,                           -- mqtt_username
    NULL,                           -- mqtt_password
    'account',                      -- auth_type（账号认证）⭐
    0.0,                            -- latitude
    0.0,                            -- longitude
    NULL,                           -- area_id
    '测试地址',                     -- address
    '1',                            -- creator
    NOW(),                          -- create_time
    '1',                            -- updater
    NOW(),                          -- update_time
    0,                              -- deleted
    1                               -- tenant_id
) ON DUPLICATE KEY UPDATE
    device_name = VALUES(device_name),
    ip = VALUES(ip),
    status = VALUES(status),
    update_time = NOW();

-- ==================== 第二步：插入报警主机记录 ====================

INSERT INTO iot_alarm_host (
    id,
    device_id,
    host_name,
    host_model,
    host_sn,
    zone_count,
    location,
    ip_address,
    port,
    account,
    password,
    status,
    online_status,
    last_heartbeat_time,
    remark,
    creator,
    create_time,
    updater,
    update_time,
    deleted,
    tenant_id
) VALUES (
    1,                              -- id（报警主机ID）
    1001,                           -- device_id（关联上面创建的设备）⭐
    '报警主机-1234',                -- host_name
    'IP9500',                       -- host_model
    'SN1234567890',                 -- host_sn
    128,                            -- zone_count
    '测试机房',                     -- location
    '192.168.1.210',                -- ip_address
    9988,                           -- port
    '1234',                         -- account（与device_key一致）⭐
    '123456',                       -- password
    1,                              -- status（1-启用）
    1,                              -- online_status（1-在线）
    NOW(),                          -- last_heartbeat_time
    '测试报警主机',                 -- remark
    '1',                            -- creator
    NOW(),                          -- create_time
    '1',                            -- updater
    NOW(),                          -- update_time
    0,                              -- deleted
    1                               -- tenant_id
) ON DUPLICATE KEY UPDATE
    device_id = VALUES(device_id),
    host_name = VALUES(host_name),
    ip_address = VALUES(ip_address),
    account = VALUES(account),
    online_status = VALUES(online_status),
    last_heartbeat_time = VALUES(last_heartbeat_time),
    update_time = NOW();

-- ==================== 验证数据 ====================

-- 查询设备记录
SELECT id, device_key, device_name, product_id, status, ip 
FROM iot_device 
WHERE device_key = '1234';

-- 查询报警主机记录
SELECT id, device_id, host_name, account, ip_address, online_status 
FROM iot_alarm_host 
WHERE account = '1234';

-- ==================== 说明 ====================
-- 1. device_key 必须与 account 一致（都是 '1234'）
-- 2. iot_alarm_host.device_id 必须关联到 iot_device.id
-- 3. auth_type 设置为 'account' 表示使用账号认证
-- 4. product_id 需要先创建产品，或使用已有产品ID（这里使用1）
