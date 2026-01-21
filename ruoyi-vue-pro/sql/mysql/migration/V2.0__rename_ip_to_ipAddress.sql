-- ========================================
-- IP 字段重命名迁移脚本
-- 将 ip 字段统一重命名为 ip_address
-- ========================================

-- 1. 修改 iot_device 表
ALTER TABLE `iot_device` 
CHANGE COLUMN `ip` `ip_address` VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'IP地址';

-- 2. 修改 iot_discovered_device 表
ALTER TABLE `iot_discovered_device` 
CHANGE COLUMN `ip` `ip_address` VARCHAR(50) NOT NULL COMMENT 'IP地址';

-- 更新唯一索引
ALTER TABLE `iot_discovered_device` 
DROP INDEX `uk_ip_discovery_time`,
ADD UNIQUE KEY `uk_ip_address_discovery_time` (`ip_address`, `discovery_time`, `deleted`) COMMENT 'IP地址和发现时间唯一索引';

-- 更新普通索引
ALTER TABLE `iot_discovered_device` 
DROP INDEX `idx_ip`,
ADD KEY `idx_ip_address` (`ip_address`) COMMENT 'IP地址索引';

-- ========================================
-- 验证迁移结果
-- ========================================
-- SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS 
-- WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'iot_device' AND COLUMN_NAME = 'ip_address';
