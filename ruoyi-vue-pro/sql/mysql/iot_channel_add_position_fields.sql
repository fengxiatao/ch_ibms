-- =====================================================
-- IoT 设备通道表 - 添加位置字段
-- =====================================================
-- 
-- 设计理念：
-- 1. 通道位置默认继承设备位置
-- 2. 特殊场景（如门禁、消防）可以使用自定义位置
-- 3. use_custom_position 标记是否使用自定义位置
-- 
-- =====================================================

-- 添加位置相关字段
ALTER TABLE `iot_device_channel`
ADD COLUMN `use_custom_position` TINYINT(1) DEFAULT 0 COMMENT '是否使用自定义位置（0:继承设备位置 1:使用自定义位置）' AFTER `last_sync_time`,
ADD COLUMN `x_coordinate` DECIMAL(10,2) DEFAULT NULL COMMENT 'X坐标（米，仅当use_custom_position=1时有效）' AFTER `use_custom_position`,
ADD COLUMN `y_coordinate` DECIMAL(10,2) DEFAULT NULL COMMENT 'Y坐标（米，仅当use_custom_position=1时有效）' AFTER `x_coordinate`,
ADD COLUMN `z_coordinate` DECIMAL(10,2) DEFAULT NULL COMMENT 'Z坐标（安装高度，米，仅当use_custom_position=1时有效）' AFTER `y_coordinate`,
ADD INDEX `idx_custom_position` (`use_custom_position`);

-- =====================================================
-- 使用说明
-- =====================================================
-- 
-- 1. 默认情况（use_custom_position = 0）：
--    通道位置继承设备位置，x_coordinate 和 y_coordinate 为 NULL
--
-- 2. 自定义位置（use_custom_position = 1）：
--    通道使用自己的坐标，x_coordinate 和 y_coordinate 有值
--
-- 3. 位置获取优先级：
--    a) 如果 use_custom_position = 1，使用通道自己的坐标
--    b) 如果 target_device_id 不为空，使用目标设备的坐标
--    c) 否则使用所属设备（device_id）的坐标
--
-- 4. 典型应用场景：
--    - 视频通道：use_custom_position = 0（继承摄像头位置）
--    - 门禁通道：use_custom_position = 1（门的实际位置）
--    - 消防通道：use_custom_position = 1（探测器的实际位置）
--    - 能源通道：use_custom_position = 0（继承电表位置）
--
-- =====================================================

-- 示例数据

-- 示例1：视频通道（继承摄像头位置）
-- INSERT INTO iot_device_channel (
--   device_id, channel_no, channel_name, channel_type,
--   use_custom_position, x_coordinate, y_coordinate, z_coordinate
-- ) VALUES (
--   206, 1, '前台全景相机', 'VIDEO',
--   0, NULL, NULL, NULL  -- 继承设备位置（包括高度）
-- );

-- 示例2：门禁通道（使用自定义位置）
-- INSERT INTO iot_device_channel (
--   device_id, channel_no, channel_name, channel_type,
--   use_custom_position, x_coordinate, y_coordinate, z_coordinate
-- ) VALUES (
--   100, 1, '前门读卡器', 'ACCESS',
--   1, 300.50, 400.75, 1.20  -- 使用自定义位置，高度1.2米（读卡器安装高度）
-- );

-- 示例3：消防通道（使用自定义位置）
-- INSERT INTO iot_device_channel (
--   device_id, channel_no, channel_name, channel_type,
--   use_custom_position, x_coordinate, y_coordinate, z_coordinate
-- ) VALUES (
--   150, 1, '1号烟感', 'FIRE',
--   1, 200.00, 300.00, 2.80  -- 使用自定义位置，高度2.8米（烟感安装高度）
-- );

-- =====================================================
-- 验证
-- =====================================================

-- 查看表结构
DESC iot_device_channel;

-- 查看索引
SHOW INDEX FROM iot_device_channel WHERE Key_name = 'idx_custom_position';

SELECT '=== 通道位置字段添加完成 ===' AS message;
