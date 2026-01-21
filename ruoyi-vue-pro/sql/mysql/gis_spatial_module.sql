-- ========================================
-- GIS 空间设施模块数据库表结构
-- ========================================

SET NAMES utf8mb4;

-- ----------------------------
-- Table structure for ibms_gis_campus (园区表)
-- ----------------------------
DROP TABLE IF EXISTS `ibms_gis_campus`;
CREATE TABLE `ibms_gis_campus` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '园区ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '园区名称',
  `code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '园区编码',
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '园区地址',
  `area` decimal(10, 2) NULL DEFAULT NULL COMMENT '占地面积(平方米)',
  `longitude` decimal(10, 6) NULL DEFAULT NULL COMMENT '经度',
  `latitude` decimal(10, 6) NULL DEFAULT NULL COMMENT '纬度',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '园区描述',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-停用 1-启用',
  `job_config` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '定时任务配置(JSON格式)',
  `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_code` (`code`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 CHARACTER SET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='园区信息表';

-- ----------------------------
-- Table structure for ibms_gis_building (建筑表)
-- ----------------------------
DROP TABLE IF EXISTS `ibms_gis_building`;
CREATE TABLE `ibms_gis_building` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '建筑ID',
  `campus_id` bigint NOT NULL COMMENT '所属园区ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '建筑名称',
  `code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '建筑编码',
  `floors` int NULL DEFAULT NULL COMMENT '楼层数',
  `height` decimal(10, 2) NULL DEFAULT NULL COMMENT '建筑高度(米)',
  `area` decimal(10, 2) NULL DEFAULT NULL COMMENT '建筑面积(平方米)',
  `build_year` int NULL DEFAULT NULL COMMENT '建成年份',
  `longitude` decimal(10, 6) NULL DEFAULT NULL COMMENT '经度',
  `latitude` decimal(10, 6) NULL DEFAULT NULL COMMENT '纬度',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '建筑描述',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-停用 1-启用',
  `job_config` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '定时任务配置(JSON格式)',
  `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_campus_id` (`campus_id`) USING BTREE,
  KEY `idx_code` (`code`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 CHARACTER SET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='建筑信息表';

-- ----------------------------
-- Table structure for ibms_gis_floor (楼层表)
-- ----------------------------
DROP TABLE IF EXISTS `ibms_gis_floor`;
CREATE TABLE `ibms_gis_floor` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '楼层ID',
  `building_id` bigint NOT NULL COMMENT '所属建筑ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '楼层名称',
  `floor_number` int NOT NULL COMMENT '楼层编号',
  `height` decimal(10, 2) NULL DEFAULT NULL COMMENT '层高(米)',
  `area` decimal(10, 2) NULL DEFAULT NULL COMMENT '楼层面积(平方米)',
  `floor_plan_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '平面图URL',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '楼层描述',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-停用 1-启用',
  `job_config` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '定时任务配置(JSON格式)',
  `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_building_id` (`building_id`) USING BTREE,
  KEY `idx_floor_number` (`floor_number`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 CHARACTER SET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='楼层信息表';

-- ----------------------------
-- Table structure for ibms_gis_area (区域表)
-- ----------------------------
DROP TABLE IF EXISTS `ibms_gis_area`;
CREATE TABLE `ibms_gis_area` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '区域ID',
  `floor_id` bigint NOT NULL COMMENT '所属楼层ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '区域名称',
  `code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '区域编码',
  `area_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '区域类型',
  `area` decimal(10, 2) NULL DEFAULT NULL COMMENT '区域面积(平方米)',
  `position_x` decimal(10, 2) NULL DEFAULT NULL COMMENT 'X坐标',
  `position_y` decimal(10, 2) NULL DEFAULT NULL COMMENT 'Y坐标',
  `width` decimal(10, 2) NULL DEFAULT NULL COMMENT '宽度',
  `height` decimal(10, 2) NULL DEFAULT NULL COMMENT '高度',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '区域描述',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-停用 1-启用',
  `job_config` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '定时任务配置(JSON格式)',
  `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_floor_id` (`floor_id`) USING BTREE,
  KEY `idx_code` (`code`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 CHARACTER SET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='区域信息表';

-- ----------------------------
-- 插入测试数据
-- ----------------------------

-- 插入测试园区
INSERT INTO `ibms_gis_campus` (`id`, `name`, `code`, `address`, `area`, `longitude`, `latitude`, `description`, `status`, `tenant_id`) VALUES
(1, '智慧科技园', 'CAMPUS001', '广东省广州市天河区科技路1号', 50000.00, 113.3245, 23.1291, '智慧科技园区', 1, 1);

-- 插入测试建筑
INSERT INTO `ibms_gis_building` (`id`, `campus_id`, `name`, `code`, `floors`, `height`, `area`, `build_year`, `longitude`, `latitude`, `description`, `status`, `tenant_id`) VALUES
(1, 1, 'A栋', 'BUILDING_A', 10, 40.00, 8000.00, 2020, 113.3246, 23.1292, 'A栋办公楼', 1, 1),
(2, 1, 'B栋', 'BUILDING_B', 8, 32.00, 6400.00, 2021, 113.3244, 23.1290, 'B栋办公楼', 1, 1);

-- 插入测试楼层
INSERT INTO `ibms_gis_floor` (`id`, `building_id`, `name`, `floor_number`, `height`, `area`, `description`, `status`, `tenant_id`) VALUES
(1, 1, 'A栋1楼', 1, 4.00, 800.00, 'A栋一楼大厅', 1, 1),
(2, 1, 'A栋2楼', 2, 4.00, 800.00, 'A栋二楼办公区', 1, 1),
(3, 1, 'A栋3楼', 3, 4.00, 800.00, 'A栋三楼办公区', 1, 1),
(4, 2, 'B栋1楼', 1, 4.00, 800.00, 'B栋一楼大厅', 1, 1),
(5, 2, 'B栋2楼', 2, 4.00, 800.00, 'B栋二楼办公区', 1, 1);

-- 插入测试区域
INSERT INTO `ibms_gis_area` (`id`, `floor_id`, `name`, `code`, `area_type`, `area`, `description`, `status`, `tenant_id`) VALUES
(1, 1, '前台接待区', 'AREA_A1_001', '接待区', 50.00, 'A栋一楼前台接待区', 1, 1),
(2, 1, '会议室A101', 'AREA_A1_101', '会议室', 80.00, 'A栋一楼会议室', 1, 1),
(3, 2, '办公区域A', 'AREA_A2_001', '办公区', 300.00, 'A栋二楼办公区域A', 1, 1),
(4, 2, '办公区域B', 'AREA_A2_002', '办公区', 300.00, 'A栋二楼办公区域B', 1, 1),
(5, 4, 'B栋前台', 'AREA_B1_001', '接待区', 50.00, 'B栋一楼前台接待区', 1, 1);

-- ----------------------------
-- 菜单权限数据
-- ----------------------------

-- 空间设施主菜单（如果不存在）
INSERT INTO `system_menu` (`name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT * FROM (
    SELECT 
        '空间设施' as `name`,
        '' as `permission`,
        1 as `type`,
        85 as `sort`,
        0 as `parent_id`,
        '/gis' as `path`,
        'fa:building' as `icon`,
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
    SELECT 1 FROM `system_menu` WHERE `name` = '空间设施' AND `deleted` = 0
) LIMIT 1;

-- 获取空间设施主菜单ID
SET @gis_menu_id = (SELECT `id` FROM `system_menu` WHERE `name` = '空间设施' AND `deleted` = 0 LIMIT 1);

-- 园区管理菜单
INSERT INTO `system_menu` (`name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT * FROM (
    SELECT 
        '园区管理' as `name`,
        'iot:campus:query' as `permission`,
        2 as `type`,
        1 as `sort`,
        @gis_menu_id as `parent_id`,
        'campus' as `path`,
        'campus' as `icon`,
        'iot/gis/campus/index' as `component`,
        'GisCampus' as `component_name`,
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
    SELECT 1 FROM `system_menu` WHERE `name` = '园区管理' AND `parent_id` = @gis_menu_id AND `deleted` = 0
) LIMIT 1;

-- 建筑管理菜单
INSERT INTO `system_menu` (`name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT * FROM (
    SELECT 
        '建筑管理' as `name`,
        'iot:building:query' as `permission`,
        2 as `type`,
        2 as `sort`,
        @gis_menu_id as `parent_id`,
        'building' as `path`,
        'building' as `icon`,
        'iot/gis/building/index' as `component`,
        'GisBuilding' as `component_name`,
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
    SELECT 1 FROM `system_menu` WHERE `name` = '建筑管理' AND `parent_id` = @gis_menu_id AND `deleted` = 0
) LIMIT 1;

-- 楼层管理菜单
INSERT INTO `system_menu` (`name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT * FROM (
    SELECT 
        '楼层管理' as `name`,
        'iot:floor:query' as `permission`,
        2 as `type`,
        3 as `sort`,
        @gis_menu_id as `parent_id`,
        'floor' as `path`,
        'floor' as `icon`,
        'iot/gis/floor/index' as `component`,
        'GisFloor' as `component_name`,
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
    SELECT 1 FROM `system_menu` WHERE `name` = '楼层管理' AND `parent_id` = @gis_menu_id AND `deleted` = 0
) LIMIT 1;

-- 区域管理菜单
INSERT INTO `system_menu` (`name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT * FROM (
    SELECT 
        '区域管理' as `name`,
        'iot:area:query' as `permission`,
        2 as `type`,
        4 as `sort`,
        @gis_menu_id as `parent_id`,
        'area' as `path`,
        'area' as `icon`,
        'iot/gis/area/index' as `component`,
        'GisArea' as `component_name`,
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
    SELECT 1 FROM `system_menu` WHERE `name` = '区域管理' AND `parent_id` = @gis_menu_id AND `deleted` = 0
) LIMIT 1;

-- 完成
SELECT '✅ GIS空间设施模块表结构已成功创建！' as message;

