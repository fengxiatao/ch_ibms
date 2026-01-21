/*
 Navicat Premium Data Transfer

 Source Server         : 126
 Source Server Type    : MySQL
 Source Server Version : 80043 (8.0.43-0ubuntu0.22.04.2)
 Source Host           : 192.168.1.126:3306
 Source Schema         : ch_ibms

 Target Server Type    : MySQL
 Target Server Version : 80043 (8.0.43-0ubuntu0.22.04.2)
 File Encoding         : 65001

 Date: 12/11/2025 20:46:41
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for iot_device
-- ----------------------------
DROP TABLE IF EXISTS `iot_device`;
CREATE TABLE `iot_device`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '设备ID',
  `device_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '设备名称',
  `nickname` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '设备备注名称',
  `serial_number` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '设备序列号',
  `pic_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '设备图片',
  `group_ids` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '设备分组编号集合（JSON）',
  `device_key` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '设备标识',
  `dxf_entity_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'DXF实体唯一标识（用于关联DXF图纸中的设备，格式：handle或自定义ID）',
  `product_id` bigint NOT NULL COMMENT '产品ID',
  `product_key` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '产品标识（冗余）',
  `device_type` tinyint NULL DEFAULT NULL COMMENT '设备类型（冗余）',
  `subsystem_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '所属子系统代码',
  `subsystem_override` tinyint(1) NULL DEFAULT 0 COMMENT '是否手动覆盖子系统归属',
  `menu_ids` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '关联的菜单ID列表（JSON）',
  `primary_menu_id` bigint NULL DEFAULT NULL COMMENT '主要菜单ID',
  `menu_override` tinyint(1) NULL DEFAULT 0 COMMENT '是否手动覆盖菜单配置',
  `device_secret` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '设备密钥',
  `auth_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '认证类型（如一机一密、动态注册）',
  `location_type` tinyint NULL DEFAULT NULL COMMENT '定位方式（1-GPS,2-北斗,3-WiFi,4-基站,5-其他）',
  `latitude` decimal(10, 6) NULL DEFAULT NULL COMMENT '设备位置的纬度',
  `longitude` decimal(10, 6) NULL DEFAULT NULL COMMENT '设备位置的经度',
  `area_id` int NULL DEFAULT NULL COMMENT '地区编码',
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '设备详细地址',
  `campus_id` bigint NULL DEFAULT NULL COMMENT '所属园区ID',
  `building_id` bigint NULL DEFAULT NULL COMMENT '所属建筑ID',
  `floor_id` bigint NULL DEFAULT NULL COMMENT '所属楼层ID',
  `room_id` bigint NULL DEFAULT NULL COMMENT '所属区域ID（房间）',
  `local_x` decimal(10, 2) NULL DEFAULT NULL COMMENT 'X坐标（米）',
  `local_y` decimal(10, 2) NULL DEFAULT NULL COMMENT 'Y坐标（米）',
  `local_z` decimal(10, 2) NULL DEFAULT NULL COMMENT 'Z坐标（安装高度，米）',
  `install_location` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '安装位置描述',
  `device_icon` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'ep:camera' COMMENT '????????con???',
  `device_icon_size` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'medium' COMMENT '鍥炬爣澶у皬',
  `install_height_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '安装高度类型',
  `config` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '设备配置（JSON格式）',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '设备状态：0-未激活 1-在线 2-离线 3-禁用',
  `gateway_id` bigint NULL DEFAULT NULL COMMENT '网关设备ID',
  `state` tinyint NULL DEFAULT 1 COMMENT '设备状态（0-激活,1-未激活,2-禁用）',
  `online_time` datetime NULL DEFAULT NULL COMMENT '最后上线时间',
  `offline_time` datetime NULL DEFAULT NULL COMMENT '最后离线时间',
  `ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'IP地址',
  `firmware_id` bigint NULL DEFAULT NULL COMMENT '固件编号',
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
  UNIQUE INDEX `uk_device_key`(`device_key` ASC, `tenant_id` ASC) USING BTREE COMMENT '设备标识唯一',
  INDEX `idx_product_id`(`product_id` ASC) USING BTREE,
  INDEX `idx_gateway_id`(`gateway_id` ASC) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_subsystem`(`subsystem_code` ASC) USING BTREE,
  INDEX `idx_campus`(`campus_id` ASC) USING BTREE,
  INDEX `idx_building`(`building_id` ASC) USING BTREE,
  INDEX `idx_floor`(`floor_id` ASC) USING BTREE,
  INDEX `idx_room`(`room_id` ASC) USING BTREE,
  INDEX `idx_state`(`state` ASC) USING BTREE,
  INDEX `idx_floor_dxf_entity`(`floor_id` ASC, `dxf_entity_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 71 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'IoT 设备表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of iot_device
-- ----------------------------
INSERT INTO `iot_device` VALUES (67, 'A栋智能制造中心-A栋4层-设备 1', 'A栋智能制造中心-A栋4层-设备 1', NULL, NULL, NULL, '5McgJPcXpau4LWCo_412109d669394330a25112884fb1d35a', NULL, 3, '5McgJPcXpau4LWCo', 0, NULL, 0, NULL, NULL, 0, '20324dd940d64f0eb18ad191ab2a0946', NULL, 3, NULL, NULL, NULL, NULL, 1, 1, 104, NULL, 14.02, 26.77, 2.80, '天花板中央', 'ep:camera', 'medium', 'ceiling', '{ \"vendor\": \"Dahua\",\"ip\":\"192.168.1.175\",  \"httpPort\": 80,  \"rtspPort\": 554,  \"username\": \"admin\",  \"password\": \"admin123\",  \"defaultChannel\": 1,  \"defaultSubType\": 0}', 0, NULL, 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '1', '2025-11-12 14:52:04', '1', '2025-11-12 12:44:44', b'0', 1);
INSERT INTO `iot_device` VALUES (68, 'A栋智能制造中心-A栋4层-设备 2', 'A栋智能制造中心-A栋4层-设备 2', NULL, NULL, NULL, '5McgJPcXpau4LWCo_d66c961729ef4b969bada4104020ff33', NULL, 3, '5McgJPcXpau4LWCo', 0, NULL, 0, NULL, NULL, 0, 'bcfbcf8c98ff4d7b9e9ae32abc456530', NULL, 3, NULL, NULL, NULL, NULL, 1, 1, 104, NULL, 31.97, 26.66, 2.80, '天花板中央', 'ep:camera', 'medium', 'ceiling', '{ \"vendor\": \"Dahua\",\"ip\":\"192.168.1.173\",  \"httpPort\": 80,  \"rtspPort\": 554,  \"username\": \"admin\",  \"password\": \"admin123\",  \"defaultChannel\": 1,  \"defaultSubType\": 0}', 0, NULL, 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '1', '2025-11-12 14:52:04', '1', '2025-11-12 12:44:52', b'0', 1);
INSERT INTO `iot_device` VALUES (69, 'A栋智能制造中心-A栋4层-设备 3', 'A栋智能制造中心-A栋4层-设备 3', NULL, NULL, NULL, '5McgJPcXpau4LWCo_bf53731551c74941891eb8cd708cdfee', NULL, 3, '5McgJPcXpau4LWCo', 0, NULL, 0, NULL, NULL, 0, '4d4516d2dc974bbd94e91092cab2874c', NULL, 3, NULL, NULL, NULL, NULL, 1, 1, 104, NULL, 39.41, 13.62, 2.80, '天花板中央', 'ep:camera', 'medium', 'ceiling', '{ \"vendor\": \"Dahua\",\"ip\":\"192.168.1.174\",  \"httpPort\": 80,  \"rtspPort\": 554,  \"username\": \"admin\",  \"password\": \"admin123\",  \"defaultChannel\": 1,  \"defaultSubType\": 0}17', 0, NULL, 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '1', '2025-11-12 14:52:04', '1', '2025-11-12 12:45:17', b'0', 1);
INSERT INTO `iot_device` VALUES (70, 'A01NVR', NULL, NULL, NULL, '', 'fT5Z2TCGtDIqE1BK_bcbd7b8cb4684f7b86ca1d5a8ea3a888', NULL, 4, 'fT5Z2TCGtDIqE1BK', 0, NULL, 0, NULL, NULL, 0, 'f2d9dabf115c49699c54e4e588bcdc0f', NULL, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'ep:camera', 'medium', NULL, '{ \"vendor\": \"Dahua\",\"ip\":\"192.168.1.200\",  \"httpPort\": 80,  \"rtspPort\": 554,  \"username\": \"admin\",  \"password\": \"admin123\",  \"defaultChannel\": 1,  \"defaultSubType\": 0}', 0, NULL, 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '1', '2025-11-12 15:48:56', '1', '2025-11-12 12:44:05', b'0', 1);

SET FOREIGN_KEY_CHECKS = 1;
