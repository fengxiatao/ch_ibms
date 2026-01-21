-- =============================================
-- 更新报警主机表结构
-- 创建时间: 2025-12-01
-- 说明: 添加连接配置相关字段
-- =============================================

-- 检查表是否存在
SELECT TABLE_NAME 
FROM INFORMATION_SCHEMA.TABLES 
WHERE TABLE_SCHEMA = 'ch_ibms' 
AND TABLE_NAME = 'iot_alarm_host';

-- 如果表不存在，创建完整表结构
CREATE TABLE IF NOT EXISTS `iot_alarm_host` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主机ID',
  `device_id` bigint NOT NULL COMMENT '关联设备ID',
  `host_name` varchar(100) NOT NULL COMMENT '主机名称',
  `host_model` varchar(100) DEFAULT NULL COMMENT '主机型号',
  `host_sn` varchar(100) DEFAULT NULL COMMENT '主机序列号',
  `zone_count` int DEFAULT '0' COMMENT '防区数量',
  `online_status` tinyint DEFAULT '0' COMMENT '在线状态：0-离线, 1-在线',
  `arm_status` varchar(20) DEFAULT 'DISARM' COMMENT '布防状态：DISARM-撤防, ARM_ALL-全部布防, ARM_EMERGENCY-紧急布防',
  `alarm_status` tinyint DEFAULT '0' COMMENT '报警状态：0-正常, 1-报警中',
  `location` varchar(200) DEFAULT NULL COMMENT '安装位置',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP地址',
  `port` int DEFAULT NULL COMMENT '端口',
  `account` varchar(50) DEFAULT NULL COMMENT '主机账号',
  `password` varchar(100) DEFAULT NULL COMMENT '主机密码',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户编号',
  PRIMARY KEY (`id`),
  KEY `idx_device_id` (`device_id`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='报警主机表';

-- 如果表已存在，添加新字段（如果不存在的话）
-- 添加IP地址字段
SET @sql = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
   WHERE TABLE_SCHEMA = 'ch_ibms' 
   AND TABLE_NAME = 'iot_alarm_host' 
   AND COLUMN_NAME = 'ip_address') = 0,
  'ALTER TABLE iot_alarm_host ADD COLUMN ip_address varchar(45) DEFAULT NULL COMMENT ''IP地址''',
  'SELECT ''ip_address column already exists'' as message'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加端口字段
SET @sql = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
   WHERE TABLE_SCHEMA = 'ch_ibms' 
   AND TABLE_NAME = 'iot_alarm_host' 
   AND COLUMN_NAME = 'port') = 0,
  'ALTER TABLE iot_alarm_host ADD COLUMN port int DEFAULT NULL COMMENT ''端口''',
  'SELECT ''port column already exists'' as message'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加账号字段
SET @sql = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
   WHERE TABLE_SCHEMA = 'ch_ibms' 
   AND TABLE_NAME = 'iot_alarm_host' 
   AND COLUMN_NAME = 'account') = 0,
  'ALTER TABLE iot_alarm_host ADD COLUMN account varchar(50) DEFAULT NULL COMMENT ''主机账号''',
  'SELECT ''account column already exists'' as message'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加密码字段
SET @sql = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
   WHERE TABLE_SCHEMA = 'ch_ibms' 
   AND TABLE_NAME = 'iot_alarm_host' 
   AND COLUMN_NAME = 'password') = 0,
  'ALTER TABLE iot_alarm_host ADD COLUMN password varchar(100) DEFAULT NULL COMMENT ''主机密码''',
  'SELECT ''password column already exists'' as message'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ========== 添加基础审计与多租户字段（如缺失） ==========
-- creator
SET @sql = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
   WHERE TABLE_SCHEMA = 'ch_ibms' 
   AND TABLE_NAME = 'iot_alarm_host' 
   AND COLUMN_NAME = 'creator') = 0,
  'ALTER TABLE iot_alarm_host ADD COLUMN creator varchar(64) DEFAULT '''' COMMENT ''创建者'' AFTER `password`',
  'SELECT ''creator column already exists'' as message'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- create_time
SET @sql = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
   WHERE TABLE_SCHEMA = 'ch_ibms' 
   AND TABLE_NAME = 'iot_alarm_host' 
   AND COLUMN_NAME = 'create_time') = 0,
  'ALTER TABLE iot_alarm_host ADD COLUMN create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT ''创建时间'' AFTER `creator`',
  'SELECT ''create_time column already exists'' as message'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- updater
SET @sql = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
   WHERE TABLE_SCHEMA = 'ch_ibms' 
   AND TABLE_NAME = 'iot_alarm_host' 
   AND COLUMN_NAME = 'updater') = 0,
  'ALTER TABLE iot_alarm_host ADD COLUMN updater varchar(64) DEFAULT '''' COMMENT ''更新者'' AFTER `create_time`',
  'SELECT ''updater column already exists'' as message'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- update_time
SET @sql = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
   WHERE TABLE_SCHEMA = 'ch_ibms' 
   AND TABLE_NAME = 'iot_alarm_host' 
   AND COLUMN_NAME = 'update_time') = 0,
  'ALTER TABLE iot_alarm_host ADD COLUMN update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT ''更新时间'' AFTER `updater`',
  'SELECT ''update_time column already exists'' as message'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- deleted
SET @sql = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
   WHERE TABLE_SCHEMA = 'ch_ibms' 
   AND TABLE_NAME = 'iot_alarm_host' 
   AND COLUMN_NAME = 'deleted') = 0,
  'ALTER TABLE iot_alarm_host ADD COLUMN deleted bit(1) NOT NULL DEFAULT b''0'' COMMENT ''是否删除'' AFTER `update_time`',
  'SELECT ''deleted column already exists'' as message'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- tenant_id
SET @sql = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
   WHERE TABLE_SCHEMA = 'ch_ibms' 
   AND TABLE_NAME = 'iot_alarm_host' 
   AND COLUMN_NAME = 'tenant_id') = 0,
  'ALTER TABLE iot_alarm_host ADD COLUMN tenant_id bigint NOT NULL DEFAULT 0 COMMENT ''租户编号'' AFTER `deleted`',
  'SELECT ''tenant_id column already exists'' as message'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 为 tenant_id 创建索引（如不存在）
SET @sql = (SELECT IF(
  (SELECT COUNT(1) FROM INFORMATION_SCHEMA.STATISTICS 
   WHERE TABLE_SCHEMA = 'ch_ibms' 
   AND TABLE_NAME = 'iot_alarm_host' 
   AND INDEX_NAME = 'idx_tenant_id') = 0,
  'ALTER TABLE iot_alarm_host ADD INDEX idx_tenant_id (tenant_id)',
  'SELECT ''idx_tenant_id already exists'' as message'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 验证表结构
SELECT 
  COLUMN_NAME,
  DATA_TYPE,
  IS_NULLABLE,
  COLUMN_DEFAULT,
  COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'ch_ibms' 
AND TABLE_NAME = 'iot_alarm_host'
ORDER BY ORDINAL_POSITION;

-- =============================================
-- 使用说明
-- =============================================
-- 1. 执行本脚本后，表结构将包含连接配置字段
-- 2. 重启后端应用以加载新的字段映射
-- 3. 前端表单现在可以正常提交包含连接信息的数据
-- =============================================
