-- 插入报警主机记录（account=1234）
-- 注意：需要先确认 iot_device 表中是否有对应的设备记录

-- 1. 查询是否已存在 account=1234 的报警主机
SELECT * FROM iot_alarm_host WHERE account = '1234';

-- 2. 如果不存在，插入报警主机记录
-- 注意：device_id 需要关联到 iot_device 表中的设备ID
-- 如果没有对应的设备，需要先创建设备记录

-- 方案A：如果已有设备记录（假设 device_id = 106）
INSERT INTO iot_alarm_host (
    host_name,
    host_model,
    host_sn,
    device_id,
    zone_count,
    location,
    ip_address,
    port,
    account,
    password,
    status,
    creator,
    create_time,
    updater,
    update_time,
    deleted,
    tenant_id
) VALUES (
    '报警主机-1234',           -- 主机名称
    '未知型号',                 -- 主机型号
    NULL,                       -- 主机序列号
    NULL,                       -- 设备ID（如果有，填写对应的设备ID）
    8,                          -- 防区数量
    '192.168.1.210',           -- 安装位置
    '192.168.1.210',           -- IP地址
    NULL,                       -- 端口（报警主机主动连接，不需要端口）
    '1234',                     -- 主机账号（重要！）
    NULL,                       -- 主机密码
    0,                          -- 状态（0-离线，1-在线）
    'system',                   -- 创建者
    NOW(),                      -- 创建时间
    'system',                   -- 更新者
    NOW(),                      -- 更新时间
    0,                          -- 删除标志
    1                           -- 租户ID
);

-- 3. 验证插入结果
SELECT * FROM iot_alarm_host WHERE account = '1234';
