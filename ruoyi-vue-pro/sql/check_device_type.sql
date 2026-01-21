-- 检查设备类型和状态
SELECT 
    id,
    device_name,
    device_type,
    state,
    ip,
    tcp_port,
    config
FROM iot_device
WHERE id IN (84, 85)
ORDER BY id;
