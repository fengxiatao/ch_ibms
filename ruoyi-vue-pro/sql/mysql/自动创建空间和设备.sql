-- =============================================
-- 自动创建空间层次和设备
-- 适用场景：快速搭建测试/演示环境
-- 注意：真实生产环境建议通过前端操作
-- =============================================

USE ch_ibms;

-- =============================================
-- 第一部分：创建空间层次
-- =============================================

-- 1. 创建园区
INSERT INTO iot_space (
    name, code, type, level, parent_id, 
    address, longitude, latitude, description,
    creator, create_time, updater, update_time, deleted, tenant_id
) VALUES (
    '长辉科技园区', 'CHANGHUI_PARK', 'CAMPUS', 1, NULL,
    '广东省深圳市南山区科技园', '113.941234', '22.544567', '长辉科技智慧园区',
    'system', NOW(), 'system', NOW(), 0, 1
);

SET @campus_id = LAST_INSERT_ID();
SELECT CONCAT('园区ID: ', @campus_id) AS 结果;

-- 2. 创建建筑
INSERT INTO iot_space (
    name, code, type, level, parent_id,
    total_floors, above_floors, below_floors, description,
    creator, create_time, updater, update_time, deleted, tenant_id
) VALUES (
    'A栋', 'BUILD_A', 'BUILDING', 2, @campus_id,
    30, 30, 0, '办公大楼A栋',
    'system', NOW(), 'system', NOW(), 0, 1
);

SET @building_id = LAST_INSERT_ID();
SELECT CONCAT('建筑ID: ', @building_id) AS 结果;

-- 3. 创建楼层
INSERT INTO iot_space (
    name, code, type, level, parent_id,
    floor_number, floor_height, description,
    creator, create_time, updater, update_time, deleted, tenant_id
) VALUES (
    '19楼', 'FLOOR_19', 'FLOOR', 3, @building_id,
    19, 3.5, '19层办公区',
    'system', NOW(), 'system', NOW(), 0, 1
);

SET @floor_id = LAST_INSERT_ID();
SELECT CONCAT('楼层ID: ', @floor_id) AS 结果;

-- 4. 创建区域（办公室）
INSERT INTO iot_space (
    name, code, type, level, parent_id,
    area, description,
    creator, create_time, updater, update_time, deleted, tenant_id
) VALUES (
    '1906办公室', 'ROOM_1906', 'ROOM', 4, @floor_id,
    50, '1906办公室，摄像头监控区域',
    'system', NOW(), 'system', NOW(), 0, 1
);

SET @room_id = LAST_INSERT_ID();
SELECT CONCAT('区域ID: ', @room_id) AS 结果;

-- 验证空间层次
SELECT 
    s1.id AS 园区ID, s1.name AS 园区名称,
    s2.id AS 建筑ID, s2.name AS 建筑名称,
    s3.id AS 楼层ID, s3.name AS 楼层名称,
    s4.id AS 区域ID, s4.name AS 区域名称
FROM iot_space s1
LEFT JOIN iot_space s2 ON s2.parent_id = s1.id AND s2.name = 'A栋'
LEFT JOIN iot_space s3 ON s3.parent_id = s2.id AND s3.name = '19楼'
LEFT JOIN iot_space s4 ON s4.parent_id = s3.id AND s4.name = '1906办公室'
WHERE s1.name = '长辉科技园区'
  AND s1.deleted = 0;

-- =============================================
-- 第二部分：添加设备（基础数据）
-- =============================================

-- ⚠️ 注意：此方式只插入数据，不会：
-- 1. 测试设备连接
-- 2. 初始化设备状态
-- 3. 生成设备密钥
-- 4. 触发设备服务注册
-- 建议：通过API或前端添加设备

-- 查询产品ID
SET @product_id = (
    SELECT id FROM iot_product 
    WHERE product_key = 'dahua-network-camera' 
    LIMIT 1
);

SELECT CONCAT('产品ID: ', @product_id) AS 结果;

-- 生成设备密钥（简单版本，生产环境需要更安全的算法）
SET @device_secret = MD5(CONCAT('dahua_camera_1906_', UNIX_TIMESTAMP()));

-- 插入设备
INSERT INTO iot_device (
    name, device_key, device_secret, product_id,
    status, ip_address, port,
    space_id, location_x, location_y, location_z,
    description,
    creator, create_time, updater, update_time, deleted, tenant_id
) VALUES (
    'A栋19楼1906办公室摄像头',
    'dahua_camera_1906',
    @device_secret,
    @product_id,
    1,  -- 初始状态：离线（需要后台服务连接后才能在线）
    '192.168.1.202',
    37777,
    @room_id,
    15, 25, 3,
    '1906办公室监控摄像头，IP:192.168.1.202',
    'system', NOW(), 'system', NOW(), 0, 1
);

SET @device_id = LAST_INSERT_ID();
SELECT CONCAT('设备ID: ', @device_id) AS 结果;

-- 插入设备扩展配置
INSERT INTO iot_device_config (
    device_id, config_key, config_value, config_type,
    creator, create_time, updater, update_time, deleted, tenant_id
) VALUES 
(@device_id, 'username', 'admin', 'CONNECTION', 'system', NOW(), 'system', NOW(), 0, 1),
(@device_id, 'password', 'admin123', 'CONNECTION', 'system', NOW(), 'system', NOW(), 0, 1),
(@device_id, 'manufacturer', '大华', 'DEVICE', 'system', NOW(), 'system', NOW(), 0, 1),
(@device_id, 'model', 'DH-IPC-HFW1230S', 'DEVICE', 'system', NOW(), 'system', NOW(), 0, 1),
(@device_id, 'resolution', '1920x1080', 'DEVICE', 'system', NOW(), 'system', NOW(), 0, 1),
(@device_id, 'protocol', 'dahua', 'DEVICE', 'system', NOW(), 'system', NOW(), 0, 1),
(@device_id, 'ptz_support', 'false', 'DEVICE', 'system', NOW(), 'system', NOW(), 0, 1),
(@device_id, 'stream_url', 'rtsp://admin:admin123@192.168.1.202:554/cam/realmonitor?channel=1&subtype=0', 'STREAM', 'system', NOW(), 'system', NOW(), 0, 1);

-- =============================================
-- 验证结果
-- =============================================

SELECT '=== 验证空间层次 ===' AS 提示;
SELECT 
    id, name, type, level, parent_id
FROM iot_space
WHERE name IN ('长辉科技园区', 'A栋', '19楼', '1906办公室')
ORDER BY level;

SELECT '=== 验证设备 ===' AS 提示;
SELECT 
    d.id, d.name, d.device_key, d.status,
    d.ip_address, p.name AS 产品名称,
    s.name AS 空间位置
FROM iot_device d
LEFT JOIN iot_product p ON d.product_id = p.id
LEFT JOIN iot_space s ON d.space_id = s.id
WHERE d.device_key = 'dahua_camera_1906';

SELECT '=== 验证设备配置 ===' AS 提示;
SELECT config_key, config_value, config_type
FROM iot_device_config
WHERE device_id = @device_id;

-- =============================================
-- 重要提示
-- =============================================
SELECT '
⚠️ 重要提示：

1. 设备状态为"离线"是正常的，需要：
   - 重启应用（设备管理服务会自动连接）
   - 或在前端"设备管理"中点击"连接"

2. 后续操作：
   - 访问前端验证空间层次
   - 在设备管理中查看设备
   - 测试设备连接和功能
   - 配置告警规则

3. 如需删除重来：
   DELETE FROM iot_device WHERE device_key = "dahua_camera_1906";
   DELETE FROM iot_space WHERE name IN ("长辉科技园区", "A栋", "19楼", "1906办公室");

' AS 说明;

















