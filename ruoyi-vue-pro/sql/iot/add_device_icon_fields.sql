-- =============================================
-- 设备图标配置字段 - 数据库变更脚本
-- 功能：为设备表添加图标配置相关字段
-- 版本：v1.0
-- 日期：2025-11-03
-- =============================================

USE ch_ibms;

-- 1. 添加设备图标配置字段
ALTER TABLE iot_device 
ADD COLUMN IF NOT EXISTS device_icon VARCHAR(100) DEFAULT 'ep:camera' COMMENT '设备图标（Icon值，如 ep:camera, ep:lock 等）' AFTER install_location,
ADD COLUMN IF NOT EXISTS device_icon_size VARCHAR(20) DEFAULT 'medium' COMMENT '图标大小（small:16px, medium:24px, large:32px）' AFTER device_icon;

-- 2. 添加楼层DXF数据字段（如果楼层表需要）
ALTER TABLE iot_floor
ADD COLUMN IF NOT EXISTS dxf_file_url VARCHAR(500) DEFAULT NULL COMMENT 'DXF文件URL（完整DXF文件）' AFTER floor_plan_url,
ADD COLUMN IF NOT EXISTS dxf_layer0_url VARCHAR(500) DEFAULT NULL COMMENT 'DXF 0图层数据URL（预处理的JSON格式）' AFTER dxf_file_url,
ADD COLUMN IF NOT EXISTS dxf_uploaded_at DATETIME DEFAULT NULL COMMENT 'DXF文件上传时间' AFTER dxf_layer0_url;

-- 3. 添加空间坐标索引（优化附近设备查询性能）
ALTER TABLE iot_device
ADD INDEX IF NOT EXISTS idx_floor_coordinates (floor_id, local_x, local_y) COMMENT '楼层坐标索引（用于附近设备查询）';

-- 4. 添加图标类型索引（用于按设备类型筛选）
ALTER TABLE iot_device
ADD INDEX IF NOT EXISTS idx_device_icon (device_icon) COMMENT '设备图标索引';

-- =============================================
-- 数据迁移：为已有设备设置默认图标
-- =============================================

-- 根据设备类型设置默认图标
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
d.device_icon_size = CASE
    WHEN p.name LIKE '%重点%' OR p.name LIKE '%VIP%' THEN 'large'
    WHEN p.name LIKE '%密集%' THEN 'small'
    ELSE 'medium'
END
WHERE d.device_icon IS NULL OR d.device_icon = '';

-- =============================================
-- 验证数据
-- =============================================

-- 检查字段是否添加成功
SELECT 
    COLUMN_NAME,
    COLUMN_TYPE,
    COLUMN_DEFAULT,
    COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'ch_ibms'
    AND TABLE_NAME = 'iot_device'
    AND COLUMN_NAME IN ('device_icon', 'device_icon_size');

-- 查看图标分布统计
SELECT 
    device_icon AS '设备图标',
    device_icon_size AS '图标大小',
    COUNT(*) AS '设备数量'
FROM iot_device
GROUP BY device_icon, device_icon_size
ORDER BY COUNT(*) DESC;

-- =============================================
-- 回滚脚本（如需要）
-- =============================================

/*
-- 删除添加的字段
ALTER TABLE iot_device 
DROP COLUMN IF EXISTS device_icon,
DROP COLUMN IF EXISTS device_icon_size;

-- 删除索引
ALTER TABLE iot_device
DROP INDEX IF EXISTS idx_floor_coordinates,
DROP INDEX IF EXISTS idx_device_icon;

-- 删除楼层DXF字段
ALTER TABLE iot_floor
DROP COLUMN IF EXISTS dxf_file_url,
DROP COLUMN IF EXISTS dxf_layer0_url,
DROP COLUMN IF EXISTS dxf_uploaded_at;
*/

-- =============================================
-- 脚本执行完成
-- =============================================
SELECT '✅ 设备图标配置字段添加完成！' AS 执行结果;




