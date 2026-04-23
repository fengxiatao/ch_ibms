-- =============================================
-- IBMS 产品扩展属性补丁：接入/流媒体工程常用字段
-- 依据公开规范与行业惯例（非业务杜撰）：
-- - RTSP 默认端口 554：见 RFC 2326 / 7826 及 IANA 惯例
-- - HTTP(Web) 默认端口 80：IANA 注册
-- - Modbus RTU/TCP 从站地址（Unit ID）有效范围 1–247：Modbus 应用协议常见约定
--
-- 幂等：按 (product_id, prop_name) 已存在则跳过
-- =============================================

INSERT INTO `ibms_product_property` (
    `tenant_id`, `product_id`, `prop_name`, `label`, `type`, `options`, `default_value`, `unit`, `remark`,
    `creator`, `create_time`, `updater`, `update_time`, `deleted`
)
SELECT
    1, p.`id`, v.prop_name, v.label, v.type, v.options, v.default_value, v.unit,
    JSON_OBJECT('source', v.source_note),
    'system', NOW(), 'system', NOW(), b'0'
FROM (
    SELECT 'VI-DS-CAM-001' AS product_code, 'rtsp_port' AS prop_name, 'RTSP 端口' AS label, 'number' AS type,
           NULL AS options, '554' AS default_value, NULL AS unit,
           'IANA/RFC 惯例，RTSP 常用 554' AS source_note
    UNION ALL
    SELECT 'VI-DS-CAM-001', 'http_port', 'HTTP(Web) 端口', 'number',
           NULL, '80', NULL,
           'IANA 注册 HTTP 默认端口 80'
    UNION ALL
    SELECT 'VI-DP-CAM-001', 'rtsp_port', 'RTSP 端口', 'number',
           NULL, '554', NULL,
           'IANA/RFC 惯例，RTSP 常用 554'
    UNION ALL
    SELECT 'VI-DP-CAM-001', 'http_port', 'HTTP(Web) 端口', 'number',
           NULL, '80', NULL,
           'IANA 注册 HTTP 默认端口 80'
    UNION ALL
    SELECT 'EP-PM-METER-001', 'modbus_unit_id', 'Modbus 从站地址', 'number',
           NULL, '1', NULL,
           'Modbus Unit ID 常用范围 1-247'
    UNION ALL
    SELECT 'BA-DD-CONTR-001', 'modbus_unit_id', 'Modbus 从站地址', 'number',
           NULL, '1', NULL,
           'Modbus Unit ID 常用范围 1-247'
) v
JOIN `ibms_product` p
  ON p.`product_code` = v.product_code AND p.`deleted` = b'0'
LEFT JOIN `ibms_product_property` pp
  ON pp.`product_id` = p.`id` AND pp.`prop_name` = v.prop_name AND pp.`deleted` = b'0'
WHERE pp.`id` IS NULL;
