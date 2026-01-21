-- 设备图标字段添加脚本（简化版）
USE ch_ibms;

-- 添加设备图标字段（如果不存在）
ALTER TABLE iot_device 
ADD COLUMN device_icon VARCHAR(100) DEFAULT 'ep:camera' COMMENT '设备图标';

ALTER TABLE iot_device 
ADD COLUMN device_icon_size VARCHAR(20) DEFAULT 'medium' COMMENT '图标大小';

-- 添加楼层DXF字段（如果不存在）
ALTER TABLE iot_floor 
ADD COLUMN dxf_file_url VARCHAR(500) DEFAULT NULL COMMENT 'DXF文件URL';

ALTER TABLE iot_floor 
ADD COLUMN dxf_layer0_url VARCHAR(500) DEFAULT NULL COMMENT 'DXF 0图层JSON';

ALTER TABLE iot_floor 
ADD COLUMN dxf_uploaded_at DATETIME DEFAULT NULL COMMENT 'DXF上传时间';

-- 添加索引
ALTER TABLE iot_device ADD INDEX idx_floor_coordinates (floor_id, local_x, local_y);
ALTER TABLE iot_device ADD INDEX idx_device_icon (device_icon);

-- 为已有设备设置默认图标
UPDATE iot_device d
INNER JOIN iot_product p ON d.product_id = p.id
SET d.device_icon = CASE 
    WHEN p.name LIKE '%摄像头%' THEN 'ep:camera'
    WHEN p.name LIKE '%球机%' THEN 'ep:video-camera'
    WHEN p.name LIKE '%门禁%' THEN 'ep:lock'
    WHEN p.name LIKE '%烟感%' THEN 'ep:smoking'
    ELSE 'ep:camera'
END
WHERE d.device_icon = 'ep:camera';

-- 验证
SELECT COLUMN_NAME, COLUMN_TYPE, COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'ch_ibms' AND TABLE_NAME = 'iot_device'
AND COLUMN_NAME IN ('device_icon', 'device_icon_size');




