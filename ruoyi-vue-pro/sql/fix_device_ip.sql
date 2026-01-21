-- 修复设备 IP 地址问题
-- 问题：设备表的 ip 字段为空，导致 NVR 心跳检测失败

-- 1. 查看当前设备 IP 状态
SELECT 
    id,
    device_name,
    ip,
    state,
    config
FROM iot_device
WHERE id = 84;

-- 2. 根据设备名称推断并更新 IP（如果设备名称包含 IP 信息）
-- 设备名称格式：device_192_168_1_200 -> IP: 192.168.1.200
UPDATE iot_device
SET ip = '192.168.1.200'
WHERE id = 84 AND ip IS NULL;

-- 3. 验证更新结果
SELECT 
    id,
    device_name,
    ip,
    state,
    config
FROM iot_device
WHERE id = 84;

-- 4. 批量修复：为所有 IP 为空的设备，从 config JSON 中提取 IP（如果存在）
-- 注意：这个语句需要 MySQL 5.7.8+ 支持 JSON 函数
UPDATE iot_device
SET ip = JSON_UNQUOTE(JSON_EXTRACT(config, '$.ip'))
WHERE ip IS NULL 
  AND config IS NOT NULL 
  AND JSON_VALID(config)
  AND JSON_EXTRACT(config, '$.ip') IS NOT NULL;

-- 5. 查看还有哪些设备的 IP 为空
SELECT 
    id,
    device_name,
    ip,
    state,
    config
FROM iot_device
WHERE ip IS NULL OR ip = ''
ORDER BY id;
