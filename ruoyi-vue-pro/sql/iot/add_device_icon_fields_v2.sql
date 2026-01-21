-- =============================================
-- 设备图标配置字段 - 数据库变更脚本 v2
-- 功能：为设备表添加图标配置相关字段
-- 版本：v2.0（修复语法问题）
-- 日期：2025-11-03
-- =============================================

USE ch_ibms;

-- 1. 添加设备图标配置字段（不使用IF NOT EXISTS，先检查是否存在）
SET @exist_device_icon = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = 'ch_ibms' AND TABLE_NAME = 'iot_device' AND COLUMN_NAME = 'device_icon');

SET @exist_device_icon_size = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = 'ch_ibms' AND TABLE_NAME = 'iot_device' AND COLUMN_NAME = 'device_icon_size');

-- 添加 device_icon 字段
SET @sql_add_device_icon = IF(@exist_device_icon = 0,
    'ALTER TABLE iot_device ADD COLUMN device_icon VARCHAR(100) DEFAULT ''ep:camera'' COMMENT ''设备图标（Icon值）'' AFTER install_location',
    'SELECT ''device_icon already exists'' AS result');
PREPARE stmt FROM @sql_add_device_icon;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加 device_icon_size 字段
SET @sql_add_device_icon_size = IF(@exist_device_icon_size = 0,
    'ALTER TABLE iot_device ADD COLUMN device_icon_size VARCHAR(20) DEFAULT ''medium'' COMMENT ''图标大小（small/medium/large）'' AFTER device_icon',
    'SELECT ''device_icon_size already exists'' AS result');
PREPARE stmt FROM @sql_add_device_icon_size;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2. 添加楼层DXF数据字段
SET @exist_dxf_file = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = 'ch_ibms' AND TABLE_NAME = 'iot_floor' AND COLUMN_NAME = 'dxf_file_url');

SET @sql_add_dxf = IF(@exist_dxf_file = 0,
    'ALTER TABLE iot_floor 
     ADD COLUMN dxf_file_url VARCHAR(500) DEFAULT NULL COMMENT ''DXF文件URL'' AFTER floor_plan_url,
     ADD COLUMN dxf_layer0_url VARCHAR(500) DEFAULT NULL COMMENT ''DXF 0图层数据URL'' AFTER dxf_file_url,
     ADD COLUMN dxf_uploaded_at DATETIME DEFAULT NULL COMMENT ''DXF上传时间'' AFTER dxf_layer0_url',
    'SELECT ''DXF fields already exist'' AS result');
PREPARE stmt FROM @sql_add_dxf;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 3. 添加空间坐标索引
SET @exist_coord_index = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
    WHERE TABLE_SCHEMA = 'ch_ibms' AND TABLE_NAME = 'iot_device' AND INDEX_NAME = 'idx_floor_coordinates');

SET @sql_add_coord_index = IF(@exist_coord_index = 0,
    'ALTER TABLE iot_device ADD INDEX idx_floor_coordinates (floor_id, local_x, local_y)',
    'SELECT ''idx_floor_coordinates already exists'' AS result');
PREPARE stmt FROM @sql_add_coord_index;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 4. 添加图标类型索引
SET @exist_icon_index = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
    WHERE TABLE_SCHEMA = 'ch_ibms' AND TABLE_NAME = 'iot_device' AND INDEX_NAME = 'idx_device_icon');

SET @sql_add_icon_index = IF(@exist_icon_index = 0,
    'ALTER TABLE iot_device ADD INDEX idx_device_icon (device_icon)',
    'SELECT ''idx_device_icon already exists'' AS result');
PREPARE stmt FROM @sql_add_icon_index;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- =============================================
-- 数据迁移：为已有设备设置默认图标
-- =============================================

UPDATE iot_device d
INNER JOIN iot_product p ON d.product_id = p.id
SET d.device_icon = CASE 
    WHEN p.name LIKE '%摄像头%' OR p.name LIKE '%枪机%' OR p.name LIKE '%半球%' THEN 'ep:camera'
    WHEN p.name LIKE '%球机%' THEN 'ep:video-camera'
    WHEN p.name LIKE '%门禁%' THEN 'ep:lock'
    WHEN p.name LIKE '%烟感%' OR p.name LIKE '%烟雾%' THEN 'ep:smoking'
    WHEN p.name LIKE '%温感%' OR p.name LIKE '%温度%' THEN 'ep:hot-water'
    WHEN p.name LIKE '%消火栓%' OR p.name LIKE '%消防栓%' THEN 'ep:turn-off'
    WHEN p.name LIKE '%报警%' THEN 'ep:bell'
    WHEN p.name LIKE '%灯%' OR p.name LIKE '%照明%' THEN 'ep:light'
    WHEN p.name LIKE '%传感器%' THEN 'ep:monitor'
    ELSE 'ep:platform'
END,
d.device_icon_size = 'medium'
WHERE (d.device_icon IS NULL OR d.device_icon = '' OR d.device_icon = 'ep:camera');

-- =============================================
-- 验证数据
-- =============================================

SELECT '✅ 设备图标字段' AS 检查项,
    COLUMN_NAME AS 字段名,
    COLUMN_TYPE AS 类型,
    COLUMN_DEFAULT AS 默认值,
    COLUMN_COMMENT AS 说明
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'ch_ibms'
    AND TABLE_NAME = 'iot_device'
    AND COLUMN_NAME IN ('device_icon', 'device_icon_size');

SELECT '✅ 楼层DXF字段' AS 检查项,
    COLUMN_NAME AS 字段名,
    COLUMN_TYPE AS 类型,
    COLUMN_COMMENT AS 说明
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'ch_ibms'
    AND TABLE_NAME = 'iot_floor'
    AND COLUMN_NAME LIKE 'dxf%';

SELECT '✅ 图标分布统计' AS 统计类型,
    device_icon AS 设备图标,
    COUNT(*) AS 设备数量
FROM iot_device
WHERE device_icon IS NOT NULL
GROUP BY device_icon
ORDER BY COUNT(*) DESC;

SELECT '✅ 脚本执行完成！' AS 执行结果;




