-- =============================================
-- IBMS 产品管理：测试数据初始化（来自前端原型硬编码）
-- 覆盖表：
-- - ibms_product
-- - ibms_product_point_type
-- - ibms_product_property
--
-- 可重复执行策略：
-- - 产品：按 product_code 唯一键判断，已存在则跳过插入
-- - 点位类型/属性：按 (product_id, code/prop_name) 判断，已存在则跳过插入
--
-- 注意：
-- - 默认 tenant_id = 1
-- - creator/updater = 'system'
-- - extra/remark/options 使用 JSON 字段（MySQL json）
-- =============================================

-- ========== 1) 产品 ==========
INSERT INTO `ibms_product` (
    `tenant_id`, `product_code`, `product_name`, `group_code`, `system_code`, `model_code`, `device_type_code`,
    `manufacturer`, `model_number`, `protocol`, `icon`, `color`, `description`, `extra`,
    `creator`, `create_time`, `updater`, `update_time`, `deleted`
)
SELECT
    1, v.product_code, v.product_name, v.group_code, v.system_code, v.model_code, v.device_type_code,
    v.manufacturer, v.model_number, v.protocol, v.icon, v.color, v.description, v.extra,
    'system', NOW(), 'system', NOW(), b'0'
FROM (
    SELECT 'VI-DS-CAM-001' AS product_code, '200万星光级网络摄像机' AS product_name,
           'SA' AS group_code, 'VI' AS system_code, 'DS' AS model_code, 'CAM' AS device_type_code,
           'HIK' AS manufacturer, 'DS-2CD3T26WD-I3' AS model_number, 'ONVIF/GB28181' AS protocol,
           'fa-video' AS icon, 'blue' AS color,
           '200万像素，星光级低照度，红外距离30米' AS description,
           JSON_OBJECT() AS extra
    UNION ALL
    SELECT 'VI-DP-CAM-001', '智能球型摄像机',
           'SA', 'VI', 'DP', 'CAM',
           'HIK', 'DS-2DE4225IW-DE', 'ONVIF',
           'fa-video', 'blue',
           '25倍光学变焦，360度旋转',
           JSON_OBJECT()
    UNION ALL
    SELECT 'AC-CR-READER-001', 'IC卡读卡器',
           'ST', 'AC', 'CR', 'READER',
           'ZKT', 'ZK-RF102', 'Wiegand',
           'fa-id-card', 'purple',
           '13.56MHz IC卡读卡器',
           JSON_OBJECT()
    UNION ALL
    SELECT 'AC-LR-READER-001', '人脸识别门禁终端',
           'ST', 'AC', 'LR', 'READER',
           'DAH', 'DH-ASI8214Y', 'TCP/IP',
           'fa-id-card', 'purple',
           '人脸识别门禁终端，支持活体检测',
           JSON_OBJECT()
    UNION ALL
    SELECT 'BA-TS-SENSOR-001', '智能温控器',
           'SB', 'BA', 'TS', 'SENSOR',
           'SIEM', 'RDF800', 'Modbus',
           'fa-thermometer-half', 'cyan',
           '房间温控器，支持Modbus通讯',
           JSON_OBJECT()
    UNION ALL
    SELECT 'BA-DD-CONTR-001', 'DDC控制器',
           'SB', 'BA', 'DD', 'DDC',
           'HON', 'XL50A', 'BACnet',
           'fa-microchip', 'cyan',
           '可编程DDC控制器，支持BACnet协议',
           JSON_OBJECT()
    UNION ALL
    -- 原型：system=EP, model=PM（已在 dict 补丁中补齐）
    SELECT 'EP-PM-METER-001', '三相智能电表',
           'SE', 'EP', 'PM', 'METER',
           'WASI', 'DTSD342-9', 'Modbus/DLT645',
           'fa-bolt', 'amber',
           '三相多功能电能表，支持Modbus和DL/T645',
           JSON_OBJECT()
    UNION ALL
    SELECT 'FD-SM-DETECTOR-001', '智能烟感探测器',
           'SF', 'FD', 'SM', 'DETECTOR',
           'SIEM', 'OP720', 'FD18',
           'fa-fire-extinguisher', 'rose',
           '光电式烟感探测器，带报警输出',
           JSON_OBJECT()
    UNION ALL
    SELECT 'PA-BC-AMPLIFIER-001', '广播功放',
           'SF', 'PA', 'BC', 'AMPLIFIER',
           'ITC', 'T-60', 'TCP/IP',
           'fa-volume-up', 'rose',
           '60W网络广播功放',
           JSON_OBJECT()
) v
LEFT JOIN `ibms_product` p
       ON p.`product_code` = v.product_code AND p.`deleted` = b'0'
WHERE p.`id` IS NULL;

-- ========== 2) 点位类型 ==========
INSERT INTO `ibms_product_point_type` (
    `tenant_id`, `product_id`, `point_type_code`, `name`, `count`, `data_type`, `remark`,
    `creator`, `create_time`, `updater`, `update_time`, `deleted`
)
SELECT
    1, p.`id`, v.point_type_code, v.name, v.cnt, v.data_type, JSON_OBJECT(),
    'system', NOW(), 'system', NOW(), b'0'
FROM (
    SELECT 'VI-DS-CAM-001' AS product_code, 'VT' AS point_type_code, '视频通道' AS name, 1 AS cnt, '视频流' AS data_type
    UNION ALL SELECT 'VI-DP-CAM-001', 'VT', '视频通道', 1, '视频流'
    UNION ALL SELECT 'AC-CR-READER-001', 'DR', '门禁点', 1, '逻辑点'
    UNION ALL SELECT 'AC-LR-READER-001', 'DR', '门禁点', 1, '逻辑点'
    UNION ALL SELECT 'BA-TS-SENSOR-001', 'AI_AN', '模拟输入', 1, 'AI'
    UNION ALL SELECT 'BA-TS-SENSOR-001', 'DI', '数字输入', 2, 'DI'
    UNION ALL SELECT 'BA-TS-SENSOR-001', 'DO', '数字输出', 1, 'DO'
    UNION ALL SELECT 'BA-DD-CONTR-001', 'DI', '数字输入', 8, 'DI'
    UNION ALL SELECT 'BA-DD-CONTR-001', 'DO', '数字输出', 6, 'DO'
    UNION ALL SELECT 'BA-DD-CONTR-001', 'AI_AN', '模拟输入', 4, 'AI'
    UNION ALL SELECT 'BA-DD-CONTR-001', 'AO_AN', '模拟输出', 2, 'AO'
    UNION ALL SELECT 'EP-PM-METER-001', 'PM', '电能监测', 3, 'AI'
    UNION ALL SELECT 'EP-PM-METER-001', 'AI_AN', '模拟输入', 6, 'AI'
    UNION ALL SELECT 'FD-SM-DETECTOR-001', 'FP', '消防点位', 1, '火警/故障'
    UNION ALL SELECT 'FD-SM-DETECTOR-001', 'AI', '报警输入', 1, 'DI'
    UNION ALL SELECT 'PA-BC-AMPLIFIER-001', 'BC', '广播通道', 4, '音频'
) v
JOIN `ibms_product` p
  ON p.`product_code` = v.product_code AND p.`deleted` = b'0'
LEFT JOIN `ibms_product_point_type` t
  ON t.`product_id` = p.`id` AND t.`point_type_code` = v.point_type_code AND t.`deleted` = b'0'
WHERE t.`id` IS NULL;

-- ========== 3) 产品属性 ==========
INSERT INTO `ibms_product_property` (
    `tenant_id`, `product_id`, `prop_name`, `label`, `type`, `options`, `default_value`, `unit`, `remark`,
    `creator`, `create_time`, `updater`, `update_time`, `deleted`
)
SELECT
    1, p.`id`, v.prop_name, v.label, v.type, v.options, v.default_value, v.unit, JSON_OBJECT(),
    'system', NOW(), 'system', NOW(), b'0'
FROM (
    SELECT 'VI-DS-CAM-001' AS product_code, 'resolution' AS prop_name, '分辨率' AS label, 'select' AS type,
           JSON_ARRAY('1920x1080','1280x720') AS options, '1920x1080' AS default_value, NULL AS unit
    UNION ALL
    SELECT 'VI-DS-CAM-001', 'night_vision', '夜视距离', 'number',
           NULL, '30', '米'
    UNION ALL
    SELECT 'VI-DP-CAM-001', 'zoom', '光学变焦', 'select',
           JSON_ARRAY('20x','25x','30x'), '25x', NULL
    UNION ALL
    SELECT 'VI-DP-CAM-001', 'preset', '预置位数量', 'number',
           NULL, '256', '个'
    UNION ALL
    SELECT 'AC-CR-READER-001', 'read_distance', '读卡距离', 'number',
           NULL, '5', 'cm'
    UNION ALL
    SELECT 'AC-CR-READER-001', 'frequency', '频率', 'select',
           JSON_ARRAY('13.56MHz','125kHz'), '13.56MHz', NULL
    UNION ALL
    SELECT 'AC-LR-READER-001', 'capacity', '人脸容量', 'number',
           NULL, '10000', '人'
    UNION ALL
    SELECT 'AC-LR-READER-001', 'liveness', '活体检测', 'checkbox',
           NULL, 'true', NULL
    UNION ALL
    SELECT 'BA-TS-SENSOR-001', 'control_type', '控制类型', 'select',
           JSON_ARRAY('2管制','4管制'), '2管制', NULL
    UNION ALL
    SELECT 'BA-TS-SENSOR-001', 'fan_speed', '风速控制', 'select',
           JSON_ARRAY('三速','无极'), '三速', NULL
    UNION ALL
    SELECT 'BA-DD-CONTR-001', 'cpu', '处理器', 'text',
           NULL, '32位ARM', NULL
    UNION ALL
    SELECT 'BA-DD-CONTR-001', 'memory', '内存', 'select',
           JSON_ARRAY('128MB','256MB','512MB'), '256MB', NULL
    UNION ALL
    SELECT 'EP-PM-METER-001', 'accuracy_class', '精度等级', 'select',
           JSON_ARRAY('0.5S','1.0'), '1.0', NULL
    UNION ALL
    SELECT 'EP-PM-METER-001', 'communication', '通讯方式', 'select',
           JSON_ARRAY('RS485','4G'), 'RS485', NULL
    UNION ALL
    SELECT 'FD-SM-DETECTOR-001', 'sensitivity', '灵敏度', 'select',
           JSON_ARRAY('高','中','低'), '中', NULL
    UNION ALL
    SELECT 'FD-SM-DETECTOR-001', 'alarm_led', '报警指示灯', 'checkbox',
           NULL, 'true', NULL
    UNION ALL
    SELECT 'PA-BC-AMPLIFIER-001', 'power', '功率', 'select',
           JSON_ARRAY('60W','120W','240W'), '60W', NULL
    UNION ALL
    SELECT 'PA-BC-AMPLIFIER-001', 'zone', '分区数量', 'number',
           NULL, '4', '个'
) v
JOIN `ibms_product` p
  ON p.`product_code` = v.product_code AND p.`deleted` = b'0'
LEFT JOIN `ibms_product_property` pp
  ON pp.`product_id` = p.`id` AND pp.`prop_name` = v.prop_name AND pp.`deleted` = b'0'
WHERE pp.`id` IS NULL;

