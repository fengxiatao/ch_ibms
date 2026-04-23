-- =============================================
-- IBMS 设备测试数据初始化脚本（MySQL）
-- 说明：
--  - 根据 README-ibms-dev.md 中的示例设备整理
--  - 可重复执行：同 device_code 已存在时跳过
--  - 仅作为演示/联调用，生产环境请根据实际项目初始化
-- =============================================

-- 大堂摄像机-01：F01-LBY-VI-CAM-HIK-001
INSERT INTO ibms_device (
    device_code, name,
    group_code, system_code, device_type_code,
    product_model, brand, access_type,
    ip, protocol,
    sn, product_key,
    point_count, points_online, points_alarm,
    space, extra,
    creator, create_time, updater, update_time, deleted, tenant_id
)
SELECT
    'F01-LBY-VI-CAM-HIK-001'        AS device_code,
    '大堂摄像机-01'                  AS name,
    'SA'                            AS group_code,   -- 智慧安防
    'VI'                            AS system_code,  -- 视频监控
    'CAM'                           AS device_type_code,
    'DS-2CD3T26'                    AS product_model,
    'HIK'                           AS brand,
    'IP'                            AS access_type,
    '10.0.1.11'                     AS ip,
    'ONVIF'                         AS protocol,
    'SN-DEMO-VCAM-001'              AS sn,
    'PK-DEMO-VCAM-001'              AS product_key,
    16                              AS point_count,
    12                              AS points_online,
    0                               AS points_alarm,
    'F01 大堂'                      AS space,
    NULL                            AS extra,
    'system'                        AS creator,
    NOW()                           AS create_time,
    'system'                        AS updater,
    NOW()                           AS update_time,
    b'0'                            AS deleted,
    0                               AS tenant_id
WHERE NOT EXISTS (
    SELECT 1 FROM ibms_device WHERE device_code = 'F01-LBY-VI-CAM-HIK-001'
);

-- 门禁控制器-01：F01-LBY-AC-CTR-ZKT-001
INSERT INTO ibms_device (
    device_code, name,
    group_code, system_code, device_type_code,
    product_model, brand, access_type,
    ip, protocol,
    sn, product_key,
    point_count, points_online, points_alarm,
    space, extra,
    creator, create_time, updater, update_time, deleted, tenant_id
)
SELECT
    'F01-LBY-AC-CTR-ZKT-001'        AS device_code,
    '门禁控制器-01'                  AS name,
    'ST'                            AS group_code,   -- 智慧通行
    'AC'                            AS system_code,  -- 门禁系统
    'CONTR'                         AS device_type_code,
    'ZK-AC-4D'                      AS product_model,
    'ZKT'                           AS brand,
    'RS485'                         AS access_type,
    '10.0.1.21'                     AS ip,
    'MQTT'                          AS protocol,
    'SN-DEMO-AC-001'                AS sn,
    'PK-DEMO-AC-001'                AS product_key,
    4                               AS point_count,
    4                               AS points_online,
    0                               AS points_alarm,
    'F01 大堂'                      AS space,
    NULL                            AS extra,
    'system'                        AS creator,
    NOW()                           AS create_time,
    'system'                        AS updater,
    NOW()                           AS update_time,
    b'0'                            AS deleted,
    0                               AS tenant_id
WHERE NOT EXISTS (
    SELECT 1 FROM ibms_device WHERE device_code = 'F01-LBY-AC-CTR-ZKT-001'
);

-- 楼控网关-01：B01-PK-BA-DDC-JOH-001
INSERT INTO ibms_device (
    device_code, name,
    group_code, system_code, device_type_code,
    product_model, brand, access_type,
    ip, protocol,
    sn, product_key,
    point_count, points_online, points_alarm,
    space, extra,
    creator, create_time, updater, update_time, deleted, tenant_id
)
SELECT
    'B01-PK-BA-DDC-JOH-001'         AS device_code,
    '楼控网关-01'                    AS name,
    'SB'                            AS group_code,   -- 智慧建筑
    'BA'                            AS system_code,  -- 楼宇自控
    'DDC'                           AS device_type_code,
    'DDC-GW-32'                     AS product_model,
    'JOH'                           AS brand,
    'IP'                            AS access_type,
    '10.0.2.31'                     AS ip,
    'Modbus TCP'                    AS protocol,
    'SN-DEMO-BA-001'                AS sn,
    'PK-DEMO-BA-001'                AS product_key,
    32                              AS point_count,
    28                              AS points_online,
    1                               AS points_alarm,
    'B01 停车场'                    AS space,
    NULL                            AS extra,
    'system'                        AS creator,
    NOW()                           AS create_time,
    'system'                        AS updater,
    NOW()                           AS update_time,
    b'0'                            AS deleted,
    0                               AS tenant_id
WHERE NOT EXISTS (
    SELECT 1 FROM ibms_device WHERE device_code = 'B01-PK-BA-DDC-JOH-001'
);

