-- 检查 NVR 设备的产品信息
SELECT 
    d.id AS device_id,
    d.device_name,
    d.device_type AS device_type,
    d.state,
    d.product_id,
    p.name AS product_name,
    p.device_type AS product_device_type,
    p.category_name
FROM iot_device d
LEFT JOIN iot_product p ON d.product_id = p.id
WHERE d.id IN (84, 85);

-- 查看所有 NVR 产品
SELECT 
    id,
    name,
    device_type,
    category_name
FROM iot_product
WHERE category_name LIKE '%NVR%' OR name LIKE '%NVR%';
