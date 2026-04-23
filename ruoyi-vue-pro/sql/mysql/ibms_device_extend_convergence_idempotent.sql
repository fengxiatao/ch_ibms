-- =============================================
-- ibms_device 台账扩展列（幂等：仅添加尚不存在的列）
-- 与 ibms_device_extend_convergence.sql 字段一致；已执行过部分列时可安全重复执行
-- =============================================

SET NAMES utf8mb4;

-- nickname
SET @s := (
  SELECT IF(COUNT(*) = 0,
    'ALTER TABLE `ibms_device` ADD COLUMN `nickname` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT ''备注名称'' AFTER `name`',
    'SELECT ''skip nickname''')
  FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ibms_device' AND COLUMN_NAME = 'nickname');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- pic_url
SET @s := (
  SELECT IF(COUNT(*) = 0,
    'ALTER TABLE `ibms_device` ADD COLUMN `pic_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT ''设备图片'' AFTER `nickname`',
    'SELECT ''skip pic_url''')
  FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ibms_device' AND COLUMN_NAME = 'pic_url');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- device_key
SET @s := (
  SELECT IF(COUNT(*) = 0,
    'ALTER TABLE `ibms_device` ADD COLUMN `device_key` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT ''设备唯一标识(对接MQTT等)'' AFTER `pic_url`',
    'SELECT ''skip device_key''')
  FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ibms_device' AND COLUMN_NAME = 'device_key');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- device_secret
SET @s := (
  SELECT IF(COUNT(*) = 0,
    'ALTER TABLE `ibms_device` ADD COLUMN `device_secret` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT ''设备密钥'' AFTER `device_key`',
    'SELECT ''skip device_secret''')
  FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ibms_device' AND COLUMN_NAME = 'device_secret');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- auth_type
SET @s := (
  SELECT IF(COUNT(*) = 0,
    'ALTER TABLE `ibms_device` ADD COLUMN `auth_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT ''认证类型'' AFTER `device_secret`',
    'SELECT ''skip auth_type''')
  FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ibms_device' AND COLUMN_NAME = 'auth_type');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- subsystem_code
SET @s := (
  SELECT IF(COUNT(*) = 0,
    'ALTER TABLE `ibms_device` ADD COLUMN `subsystem_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT ''子系统代码'' AFTER `auth_type`',
    'SELECT ''skip subsystem_code''')
  FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ibms_device' AND COLUMN_NAME = 'subsystem_code');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- subsystem_override
SET @s := (
  SELECT IF(COUNT(*) = 0,
    'ALTER TABLE `ibms_device` ADD COLUMN `subsystem_override` bit(1) NOT NULL DEFAULT b''0'' COMMENT ''是否手动覆盖子系统'' AFTER `subsystem_code`',
    'SELECT ''skip subsystem_override''')
  FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ibms_device' AND COLUMN_NAME = 'subsystem_override');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- menu_ids
SET @s := (
  SELECT IF(COUNT(*) = 0,
    'ALTER TABLE `ibms_device` ADD COLUMN `menu_ids` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT ''关联菜单ID JSON数组'' AFTER `subsystem_override`',
    'SELECT ''skip menu_ids''')
  FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ibms_device' AND COLUMN_NAME = 'menu_ids');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- primary_menu_id
SET @s := (
  SELECT IF(COUNT(*) = 0,
    'ALTER TABLE `ibms_device` ADD COLUMN `primary_menu_id` bigint NULL DEFAULT NULL COMMENT ''主菜单ID'' AFTER `menu_ids`',
    'SELECT ''skip primary_menu_id''')
  FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ibms_device' AND COLUMN_NAME = 'primary_menu_id');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- menu_override
SET @s := (
  SELECT IF(COUNT(*) = 0,
    'ALTER TABLE `ibms_device` ADD COLUMN `menu_override` bit(1) NOT NULL DEFAULT b''0'' COMMENT ''是否手动覆盖菜单'' AFTER `primary_menu_id`',
    'SELECT ''skip menu_override''')
  FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ibms_device' AND COLUMN_NAME = 'menu_override');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- dxf_entity_id
SET @s := (
  SELECT IF(COUNT(*) = 0,
    'ALTER TABLE `ibms_device` ADD COLUMN `dxf_entity_id` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT ''DXF实体ID'' AFTER `menu_override`',
    'SELECT ''skip dxf_entity_id''')
  FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ibms_device' AND COLUMN_NAME = 'dxf_entity_id');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- device_type (int)
SET @s := (
  SELECT IF(COUNT(*) = 0,
    'ALTER TABLE `ibms_device` ADD COLUMN `device_type` int NULL DEFAULT NULL COMMENT ''数值型设备类型(网关/物模型兼容)'' AFTER `dxf_entity_id`',
    'SELECT ''skip device_type''')
  FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ibms_device' AND COLUMN_NAME = 'device_type');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ibms_product_id（若仅有旧表无此列时补齐；多数环境已存在则跳过）
SET @s := (
  SELECT IF(COUNT(*) = 0,
    'ALTER TABLE `ibms_device` ADD COLUMN `ibms_product_id` bigint NULL DEFAULT NULL COMMENT ''关联 ibms_product.id'' AFTER `product_key`',
    'SELECT ''skip ibms_product_id''')
  FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ibms_device' AND COLUMN_NAME = 'ibms_product_id');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- group_ids
SET @s := (
  SELECT IF(COUNT(*) = 0,
    'ALTER TABLE `ibms_device` ADD COLUMN `group_ids` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT ''设备分组ID集合JSON'' AFTER `device_type`',
    'SELECT ''skip group_ids''')
  FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ibms_device' AND COLUMN_NAME = 'group_ids');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;
