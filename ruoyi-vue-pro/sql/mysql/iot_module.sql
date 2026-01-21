-- ========================================
-- IoT 物联网模块数据库表结构
-- ========================================

SET NAMES utf8mb4;

-- ----------------------------
-- Table structure for iot_product
-- ----------------------------
DROP TABLE IF EXISTS `iot_product`;
CREATE TABLE `iot_product` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '产品ID',
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '产品名称',
  `product_key` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '产品标识',
  `category_id` bigint NULL DEFAULT NULL COMMENT '产品分类编号',
  `icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '产品图标',
  `pic_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '产品图片',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '产品描述',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '产品状态：0-开发中 1-已发布',
  `device_type` tinyint NOT NULL DEFAULT 0 COMMENT '设备类型：0-直连设备 1-网关设备 2-网关子设备',
  `net_type` tinyint NULL DEFAULT NULL COMMENT '联网方式：1-WiFi 2-蜂窝(2G/3G/4G/5G) 3-以太网 4-LoRaWAN 5-其他',
  `location_type` tinyint NULL DEFAULT NULL COMMENT '定位方式：1-GPS 2-北斗 3-WiFi 4-基站 5-其他',
  `codec_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '数据格式(编解码器类型)',
  `job_config` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '定时任务配置(JSON格式)',
  `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_product_key` (`product_key`,`tenant_id`) USING BTREE COMMENT '产品标识唯一',
  KEY `idx_category_id` (`category_id`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 CHARACTER SET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='IoT 产品表';

-- ----------------------------
-- Table structure for iot_product_category
-- ----------------------------
DROP TABLE IF EXISTS `iot_product_category`;
CREATE TABLE `iot_product_category` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分类名称',
  `parent_id` bigint NOT NULL DEFAULT 0 COMMENT '父分类ID',
  `icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '分类图标',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '分类描述',
  `sort` int NOT NULL DEFAULT 0 COMMENT '显示排序',
  `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_parent_id` (`parent_id`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 CHARACTER SET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='IoT 产品分类表';

-- ----------------------------
-- Table structure for iot_device
-- ----------------------------
DROP TABLE IF EXISTS `iot_device`;
CREATE TABLE `iot_device` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '设备ID',
  `device_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '设备名称',
  `device_key` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '设备标识',
  `product_id` bigint NOT NULL COMMENT '产品ID',
  `device_secret` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '设备密钥',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '设备状态：0-未激活 1-在线 2-离线 3-禁用',
  `gateway_id` bigint NULL DEFAULT NULL COMMENT '网关设备ID',
  `ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'IP地址',
  `firmware_version` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '固件版本',
  `last_online_time` datetime NULL DEFAULT NULL COMMENT '最后上线时间',
  `active_time` datetime NULL DEFAULT NULL COMMENT '激活时间',
  `location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '设备位置',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '设备描述',
  `job_config` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '定时任务配置(JSON格式)',
  `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_device_key` (`device_key`,`tenant_id`) USING BTREE COMMENT '设备标识唯一',
  KEY `idx_product_id` (`product_id`) USING BTREE,
  KEY `idx_gateway_id` (`gateway_id`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 CHARACTER SET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='IoT 设备表';

-- ----------------------------
-- 插入测试数据
-- ----------------------------

-- 插入产品分类
INSERT INTO `iot_product_category` (`id`, `name`, `parent_id`, `icon`, `description`, `sort`, `tenant_id`) VALUES
(1, '智能安防', 0, 'camera', '智能安防设备', 1, 1),
(2, '视频监控', 1, 'video-camera', '视频监控摄像头', 1, 1),
(3, '门禁系统', 1, 'key', '门禁控制设备', 2, 1),
(4, '环境监测', 0, 'cloud', '环境传感器设备', 2, 1),
(5, '温湿度传感器', 4, 'temp', '温湿度监测', 1, 1);

-- 插入测试产品
INSERT INTO `iot_product` (`id`, `name`, `product_key`, `category_id`, `description`, `status`, `device_type`, `net_type`, `codec_type`, `tenant_id`) VALUES
(1, '海康威视摄像头', 'hikvision_camera_001', 2, '海康威视网络摄像机', 1, 0, 3, 'JSON', 1),
(2, '大华摄像头', 'dahua_camera_001', 2, '大华网络摄像机', 1, 0, 3, 'JSON', 1);

-- 插入测试设备
INSERT INTO `iot_device` (`id`, `device_name`, `device_key`, `product_id`, `device_secret`, `status`, `firmware_version`, `description`, `tenant_id`) VALUES
(1, '大门摄像头', 'camera_gate_001', 1, 'hk123456', 1, 'V5.6.0', '主大门监控摄像头', 1),
(2, '停车场摄像头', 'camera_parking_001', 2, 'dh123456', 1, 'V3.2.1', '停车场监控摄像头', 1);

-- ----------------------------
-- 菜单权限数据
-- ----------------------------

-- IoT 物联网主菜单（如果不存在）
INSERT INTO `system_menu` (`name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT * FROM (
    SELECT 
        'IoT 物联网' as `name`,
        '' as `permission`,
        1 as `type`,
        80 as `sort`,
        0 as `parent_id`,
        '/iot' as `path`,
        'fa:microchip' as `icon`,
        NULL as `component`,
        NULL as `component_name`,
        0 as `status`,
        1 as `visible`,
        1 as `keep_alive`,
        1 as `always_show`,
        '1' as `creator`,
        NOW() as `create_time`,
        '1' as `updater`,
        NOW() as `update_time`,
        0 as `deleted`
) AS tmp
WHERE NOT EXISTS (
    SELECT 1 FROM `system_menu` WHERE `name` = 'IoT 物联网' AND `deleted` = 0
) LIMIT 1;

-- 获取IoT主菜单ID（用于后续插入子菜单）
SET @iot_menu_id = (SELECT `id` FROM `system_menu` WHERE `name` = 'IoT 物联网' AND `deleted` = 0 LIMIT 1);

-- 产品管理菜单
INSERT INTO `system_menu` (`name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT * FROM (
    SELECT 
        '产品管理' as `name`,
        'iot:product:query' as `permission`,
        2 as `type`,
        1 as `sort`,
        @iot_menu_id as `parent_id`,
        'product' as `path`,
        'product' as `icon`,
        'iot/product/product/index' as `component`,
        'IotProduct' as `component_name`,
        0 as `status`,
        1 as `visible`,
        1 as `keep_alive`,
        1 as `always_show`,
        '1' as `creator`,
        NOW() as `create_time`,
        '1' as `updater`,
        NOW() as `update_time`,
        0 as `deleted`
) AS tmp
WHERE NOT EXISTS (
    SELECT 1 FROM `system_menu` WHERE `name` = '产品管理' AND `parent_id` = @iot_menu_id AND `deleted` = 0
) LIMIT 1;

-- 设备管理菜单
INSERT INTO `system_menu` (`name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT * FROM (
    SELECT 
        '设备管理' as `name`,
        'iot:device:query' as `permission`,
        2 as `type`,
        2 as `sort`,
        @iot_menu_id as `parent_id`,
        'device' as `path`,
        'device' as `icon`,
        'iot/device/index' as `component`,
        'IotDevice' as `component_name`,
        0 as `status`,
        1 as `visible`,
        1 as `keep_alive`,
        1 as `always_show`,
        '1' as `creator`,
        NOW() as `create_time`,
        '1' as `updater`,
        NOW() as `update_time`,
        0 as `deleted`
) AS tmp
WHERE NOT EXISTS (
    SELECT 1 FROM `system_menu` WHERE `name` = '设备管理' AND `parent_id` = @iot_menu_id AND `deleted` = 0
) LIMIT 1;

-- 完成
SELECT '✅ IoT模块表结构已成功创建！' as message;

