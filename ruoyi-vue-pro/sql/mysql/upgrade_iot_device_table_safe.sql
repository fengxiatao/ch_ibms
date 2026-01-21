-- ========================================
-- IoT 设备表结构安全升级脚本
-- 可以重复执行，自动跳过已存在的字段
-- ========================================
-- 问题：Unknown column 'nickname' in 'field list'
-- 日期：2025-10-22
-- ========================================

USE ch_ibms;

-- ========================================
-- 创建辅助存储过程（用于安全添加字段）
-- ========================================

DELIMITER $$

DROP PROCEDURE IF EXISTS add_column_if_not_exists$$
CREATE PROCEDURE add_column_if_not_exists(
    IN tableName VARCHAR(128),
    IN columnName VARCHAR(128),
    IN columnDefinition TEXT
)
BEGIN
    DECLARE column_count INT;
    
    SELECT COUNT(*) INTO column_count
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = tableName
      AND COLUMN_NAME = columnName;
    
    IF column_count = 0 THEN
        SET @sql = CONCAT('ALTER TABLE `', tableName, '` ADD COLUMN `', columnName, '` ', columnDefinition);
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
        SELECT CONCAT('✅ 已添加字段: ', columnName) AS result;
    ELSE
        SELECT CONCAT('⏭️  字段已存在，跳过: ', columnName) AS result;
    END IF;
END$$

DELIMITER ;

-- ========================================
-- 执行字段添加
-- ========================================

SELECT '开始升级 iot_device 表结构...' AS info;

-- 1. 添加 nickname 字段
CALL add_column_if_not_exists('iot_device', 'nickname', 
    'VARCHAR(64) NULL COMMENT ''设备备注名称'' AFTER `device_name`');

-- 2. 添加 serial_number 字段
CALL add_column_if_not_exists('iot_device', 'serial_number', 
    'VARCHAR(64) NULL COMMENT ''设备序列号'' AFTER `nickname`');

-- 3. 添加 pic_url 字段
CALL add_column_if_not_exists('iot_device', 'pic_url', 
    'VARCHAR(255) NULL COMMENT ''设备图片'' AFTER `serial_number`');

-- 4. 添加 group_ids 字段
CALL add_column_if_not_exists('iot_device', 'group_ids', 
    'VARCHAR(500) NULL COMMENT ''设备分组编号集合（逗号分隔）'' AFTER `pic_url`');

-- 5. 添加 product_key 字段
CALL add_column_if_not_exists('iot_device', 'product_key', 
    'VARCHAR(64) NULL COMMENT ''产品标识'' AFTER `product_id`');

-- 6. 添加 device_type 字段
CALL add_column_if_not_exists('iot_device', 'device_type', 
    'TINYINT NULL COMMENT ''设备类型：0-直连设备 1-网关设备 2-网关子设备'' AFTER `product_key`');

-- 7. 添加 state 字段（如果使用的是旧的 status 字段，需要先改名）
-- 检查是否需要将 status 改为 state
SET @status_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS 
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'iot_device' AND COLUMN_NAME = 'status');
SET @state_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS 
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'iot_device' AND COLUMN_NAME = 'state');

-- 如果 status 存在但 state 不存在，则重命名
SELECT CASE 
    WHEN @status_exists > 0 AND @state_exists = 0 THEN 
        (SELECT '⚠️ 需要将 status 字段重命名为 state' AS warning)
    WHEN @state_exists > 0 THEN 
        (SELECT '✅ state 字段已存在' AS info)
    ELSE 
        (SELECT '❌ status 和 state 字段都不存在，请检查表结构' AS error)
END;

-- 如果需要，执行重命名（需要手动执行，因为涉及数据迁移）
-- ALTER TABLE `iot_device` CHANGE COLUMN `status` `state` TINYINT NOT NULL DEFAULT 0 COMMENT '设备状态：0-未激活 1-在线 2-离线';

-- 8. 添加 online_time 字段
CALL add_column_if_not_exists('iot_device', 'online_time', 
    'DATETIME NULL COMMENT ''最后上线时间'' AFTER `state`');

-- 9. 添加 offline_time 字段
CALL add_column_if_not_exists('iot_device', 'offline_time', 
    'DATETIME NULL COMMENT ''最后离线时间'' AFTER `online_time`');

-- 10. 添加 firmware_id 字段
CALL add_column_if_not_exists('iot_device', 'firmware_id', 
    'BIGINT NULL COMMENT ''固件编号'' AFTER `ip`');

-- 11. 添加 auth_type 字段
CALL add_column_if_not_exists('iot_device', 'auth_type', 
    'VARCHAR(32) NULL COMMENT ''认证类型（如一机一密、动态注册）'' AFTER `device_secret`');

-- 12. 添加 location_type 字段
CALL add_column_if_not_exists('iot_device', 'location_type', 
    'TINYINT NULL COMMENT ''定位方式：1-GPS 2-北斗 3-WiFi 4-基站 5-其他'' AFTER `firmware_id`');

-- 13. 添加 latitude 字段
CALL add_column_if_not_exists('iot_device', 'latitude', 
    'DECIMAL(10, 6) NULL COMMENT ''设备位置的纬度'' AFTER `location_type`');

-- 14. 添加 longitude 字段
CALL add_column_if_not_exists('iot_device', 'longitude', 
    'DECIMAL(10, 6) NULL COMMENT ''设备位置的经度'' AFTER `latitude`');

-- 15. 添加 area_id 字段
CALL add_column_if_not_exists('iot_device', 'area_id', 
    'INT NULL COMMENT ''地区编码'' AFTER `longitude`');

-- 16. 添加 address 字段
CALL add_column_if_not_exists('iot_device', 'address', 
    'VARCHAR(255) NULL COMMENT ''设备详细地址'' AFTER `area_id`');

-- 17. 添加 config 字段
CALL add_column_if_not_exists('iot_device', 'config', 
    'TEXT NULL COMMENT ''设备配置（JSON格式）'' AFTER `address`');

-- job_config 字段应该已经存在

-- ========================================
-- 完成提示
-- ========================================

SELECT '✅ iot_device 表结构升级完成！' AS result;

-- 清理辅助存储过程
DROP PROCEDURE IF EXISTS add_column_if_not_exists;

-- 显示最终表结构
SELECT 
    ORDINAL_POSITION AS '序号',
    COLUMN_NAME AS '字段名',
    COLUMN_TYPE AS '数据类型',
    IS_NULLABLE AS '允许空',
    COLUMN_COMMENT AS '注释'
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'iot_device'
ORDER BY ORDINAL_POSITION;





