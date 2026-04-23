-- IBMS 报警主机产品种子（与历史 iot_product.product_key = ALARM_HOST_PRODUCT 对齐）
-- 依赖：字典中已有 ibms_system=AL、ibms_device_model=AL-C、ibms_device_type=SERVER、ibms_brand=OTH 等（或等价编码）
-- 幂等：按 extra.productKey 已存在则跳过

INSERT INTO `ibms_product` (
    `tenant_id`, `product_code`, `product_name`, `group_code`, `system_code`, `model_code`, `device_type_code`,
    `manufacturer`, `model_number`, `protocol`, `icon`, `color`, `description`, `extra`,
    `creator`, `create_time`, `updater`, `update_time`, `deleted`
)
SELECT
    1,
    'AL-AL-C-SERVER-OTH-001',
    'PS600 报警主机',
    'SA',
    'AL',
    'AL-C',
    'SERVER',
    'OTH',
    'PS600',
    'TCP/PS600',
    'ep:bell',
    'red',
    'PS600 协议报警主机（extra.productKey 对齐 IoT ALARM_HOST_PRODUCT）',
    JSON_OBJECT('productKey', 'ALARM_HOST_PRODUCT', 'codecType', 'ALARM_PS600'),
    'system', NOW(), 'system', NOW(), b'0'
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM `ibms_product` p
    WHERE p.`deleted` = 0
      AND JSON_UNQUOTE(JSON_EXTRACT(p.`extra`, '$.productKey')) = 'ALARM_HOST_PRODUCT'
);
