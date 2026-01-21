-- ----------------------------
-- 修复 iot_camera_snapshot 表结构
-- 将 camera_id 重命名为 channel_id，并将 device_id 改为可空
-- ----------------------------

-- 1. 重命名 camera_id 为 channel_id
ALTER TABLE `iot_camera_snapshot` 
CHANGE COLUMN `camera_id` `channel_id` bigint(20) NOT NULL COMMENT '通道ID';

-- 2. 修改 device_id 为可空（因为现在主要使用 channel_id，device_id 作为冗余字段）
ALTER TABLE `iot_camera_snapshot` 
MODIFY COLUMN `device_id` bigint(20) DEFAULT NULL COMMENT '设备ID（冗余字段）';

-- 3. 修改 device_name 注释
ALTER TABLE `iot_camera_snapshot` 
MODIFY COLUMN `device_name` varchar(128) DEFAULT NULL COMMENT '通道名称（冗余字段，便于查询）';

-- 4. 更新索引
ALTER TABLE `iot_camera_snapshot` 
DROP INDEX `idx_camera_id`,
ADD INDEX `idx_channel_id` (`channel_id`);

-- 5. 验证修改
SELECT 
    COLUMN_NAME,
    COLUMN_TYPE,
    IS_NULLABLE,
    COLUMN_DEFAULT,
    COLUMN_COMMENT
FROM 
    INFORMATION_SCHEMA.COLUMNS
WHERE 
    TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'iot_camera_snapshot'
    AND COLUMN_NAME IN ('channel_id', 'device_id', 'device_name')
ORDER BY 
    ORDINAL_POSITION;
