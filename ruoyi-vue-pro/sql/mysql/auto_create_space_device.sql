-- =============================================
-- Auto Create Space Hierarchy and Device
-- Use case: Quick setup for testing/demo
-- Note: For production, recommend using frontend UI
-- =============================================

USE ch_ibms;

-- =============================================
-- Part 1: Create Space Hierarchy
-- =============================================

-- 1. Create Campus
INSERT INTO iot_space (
    name, code, type, level, parent_id, 
    address, longitude, latitude, description,
    creator, create_time, updater, update_time, deleted, tenant_id
) VALUES (
    'Changhui Technology Park', 'CHANGHUI_PARK', 'CAMPUS', 1, NULL,
    'Shenzhen, Guangdong', '113.941234', '22.544567', 'Changhui Smart Park',
    'system', NOW(), 'system', NOW(), 0, 1
);

SET @campus_id = LAST_INSERT_ID();
SELECT CONCAT('Campus ID: ', @campus_id) AS Result;

-- 2. Create Building
INSERT INTO iot_space (
    name, code, type, level, parent_id,
    total_floors, above_floors, below_floors, description,
    creator, create_time, updater, update_time, deleted, tenant_id
) VALUES (
    'Building A', 'BUILD_A', 'BUILDING', 2, @campus_id,
    30, 30, 0, 'Office Building A',
    'system', NOW(), 'system', NOW(), 0, 1
);

SET @building_id = LAST_INSERT_ID();
SELECT CONCAT('Building ID: ', @building_id) AS Result;

-- 3. Create Floor
INSERT INTO iot_space (
    name, code, type, level, parent_id,
    floor_number, floor_height, description,
    creator, create_time, updater, update_time, deleted, tenant_id
) VALUES (
    'Floor 19', 'FLOOR_19', 'FLOOR', 3, @building_id,
    19, 3.5, '19F Office Area',
    'system', NOW(), 'system', NOW(), 0, 1
);

SET @floor_id = LAST_INSERT_ID();
SELECT CONCAT('Floor ID: ', @floor_id) AS Result;

-- 4. Create Room
INSERT INTO iot_space (
    name, code, type, level, parent_id,
    area, description,
    creator, create_time, updater, update_time, deleted, tenant_id
) VALUES (
    'Room 1906', 'ROOM_1906', 'ROOM', 4, @floor_id,
    50, 'Office 1906, camera monitoring area',
    'system', NOW(), 'system', NOW(), 0, 1
);

SET @room_id = LAST_INSERT_ID();
SELECT CONCAT('Room ID: ', @room_id) AS Result;

-- Verify space hierarchy
SELECT 
    s1.id AS Campus_ID, s1.name AS Campus_Name,
    s2.id AS Building_ID, s2.name AS Building_Name,
    s3.id AS Floor_ID, s3.name AS Floor_Name,
    s4.id AS Room_ID, s4.name AS Room_Name
FROM iot_space s1
LEFT JOIN iot_space s2 ON s2.parent_id = s1.id AND s2.name = 'Building A'
LEFT JOIN iot_space s3 ON s3.parent_id = s2.id AND s3.name = 'Floor 19'
LEFT JOIN iot_space s4 ON s4.parent_id = s3.id AND s4.name = 'Room 1906'
WHERE s1.name = 'Changhui Technology Park'
  AND s1.deleted = 0;

-- =============================================
-- Part 2: Add Device (Base Data)
-- =============================================

-- Note: This method only inserts data, it will NOT:
-- 1. Test device connection
-- 2. Initialize device status
-- 3. Generate device secret
-- 4. Trigger device service registration
-- Recommendation: Add device via API or frontend

-- Query product ID
SET @product_id = (
    SELECT id FROM iot_product 
    WHERE product_key = 'dahua-network-camera' 
    LIMIT 1
);

SELECT CONCAT('Product ID: ', @product_id) AS Result;

-- Generate device secret (simple version, production needs more secure algorithm)
SET @device_secret = MD5(CONCAT('dahua_camera_1906_', UNIX_TIMESTAMP()));

-- Insert device
INSERT INTO iot_device (
    name, device_key, device_secret, product_id,
    status, ip_address, port,
    space_id, location_x, location_y, location_z,
    description,
    creator, create_time, updater, update_time, deleted, tenant_id
) VALUES (
    'Building A-19F-1906 Camera',
    'dahua_camera_1906',
    @device_secret,
    @product_id,
    1,  -- Initial status: offline (needs backend service to connect)
    '192.168.1.202',
    37777,
    @room_id,
    15, 25, 3,
    'Office 1906 monitoring camera, IP: 192.168.1.202',
    'system', NOW(), 'system', NOW(), 0, 1
);

SET @device_id = LAST_INSERT_ID();
SELECT CONCAT('Device ID: ', @device_id) AS Result;

-- Insert device config
INSERT INTO iot_device_config (
    device_id, config_key, config_value, config_type,
    creator, create_time, updater, update_time, deleted, tenant_id
) VALUES 
(@device_id, 'username', 'admin', 'CONNECTION', 'system', NOW(), 'system', NOW(), 0, 1),
(@device_id, 'password', 'admin123', 'CONNECTION', 'system', NOW(), 'system', NOW(), 0, 1),
(@device_id, 'manufacturer', 'Dahua', 'DEVICE', 'system', NOW(), 'system', NOW(), 0, 1),
(@device_id, 'model', 'DH-IPC-HFW1230S', 'DEVICE', 'system', NOW(), 'system', NOW(), 0, 1),
(@device_id, 'resolution', '1920x1080', 'DEVICE', 'system', NOW(), 'system', NOW(), 0, 1),
(@device_id, 'protocol', 'dahua', 'DEVICE', 'system', NOW(), 'system', NOW(), 0, 1),
(@device_id, 'ptz_support', 'false', 'DEVICE', 'system', NOW(), 'system', NOW(), 0, 1),
(@device_id, 'stream_url', 'rtsp://admin:admin123@192.168.1.202:554/cam/realmonitor?channel=1&subtype=0', 'STREAM', 'system', NOW(), 'system', NOW(), 0, 1);

-- =============================================
-- Verification
-- =============================================

SELECT '=== Verify Space Hierarchy ===' AS Prompt;
SELECT 
    id, name, type, level, parent_id
FROM iot_space
WHERE name IN ('Changhui Technology Park', 'Building A', 'Floor 19', 'Room 1906')
ORDER BY level;

SELECT '=== Verify Device ===' AS Prompt;
SELECT 
    d.id, d.name, d.device_key, d.status,
    d.ip_address, p.name AS Product_Name,
    s.name AS Space_Location
FROM iot_device d
LEFT JOIN iot_product p ON d.product_id = p.id
LEFT JOIN iot_space s ON d.space_id = s.id
WHERE d.device_key = 'dahua_camera_1906';

SELECT '=== Verify Device Config ===' AS Prompt;
SELECT config_key, config_value, config_type
FROM iot_device_config
WHERE device_id = @device_id;

-- =============================================
-- Important Notes
-- =============================================
SELECT '
[Important Notes]

1. Device status is "offline" by default. To make it online:
   - Restart the application (device management service will auto-connect)
   - Or manually click "Connect" in frontend Device Management

2. Next steps:
   - Visit frontend to verify space hierarchy
   - Check device in Device Management
   - Test device connection and functions
   - Configure alert rules

3. To delete and retry:
   DELETE FROM iot_device WHERE device_key = "dahua_camera_1906";
   DELETE FROM iot_space WHERE name IN ("Changhui Technology Park", "Building A", "Floor 19", "Room 1906");

' AS Notes;

















