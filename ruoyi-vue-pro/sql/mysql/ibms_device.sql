-- =============================================
-- IBMS 设备表结构（MySQL）
-- 说明：
--  - 表：ibms_device（设备）
--  - 可重复执行：建表使用 IF NOT EXISTS
--  - 测试数据见：ibms_device_init.sql
-- =============================================

SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `ibms_device` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '设备ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  `device_code` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '设备编码：F01-LBY-VI-CAM-HIK-001',
  `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '设备名称',
  `nickname` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注名称',
  `pic_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '设备图片',
  `device_key` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '设备唯一标识(对接MQTT等)',
  `device_secret` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '设备密钥',
  `auth_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '认证类型',
  `subsystem_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '子系统代码',
  `subsystem_override` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否手动覆盖子系统',
  `menu_ids` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '关联菜单ID JSON数组',
  `primary_menu_id` bigint NULL DEFAULT NULL COMMENT '主菜单ID',
  `menu_override` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否手动覆盖菜单',
  `dxf_entity_id` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'DXF实体ID',
  `device_type` int NULL DEFAULT NULL COMMENT '数值型设备类型(网关/物模型兼容)',
  `group_ids` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '设备分组ID集合JSON',
  `group_code` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '专业分组码：SA/ST/SB/SE/SF/GW',
  `system_code` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '系统码：VI/AC/BA/...',
  `device_type_code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '设备类型码：CAM/NVR/CTR/...',
  `product_model` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '产品型号',
  `brand` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '品牌码',
  `access_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '接入类型',
  `ip` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'IP地址',
  `protocol` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '接入协议',
  `sn` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '设备序列号',
  `product_key` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'ProductKey',
  `ibms_product_id` bigint NULL DEFAULT NULL COMMENT '关联 ibms_product.id',
  `point_count` int NOT NULL DEFAULT 0 COMMENT '通道总数',
  `points_online` int NOT NULL DEFAULT 0 COMMENT '在线通道数',
  `points_alarm` int NOT NULL DEFAULT 0 COMMENT '告警通道数',
  `space` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '空间位置文案',
  `extra` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '扩展 JSON',
  `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_device_code_tenant` (`tenant_id`, `device_code`) USING BTREE,
  KEY `idx_group_code` (`group_code`) USING BTREE,
  KEY `idx_system_code` (`system_code`) USING BTREE,
  KEY `idx_device_type_code` (`device_type_code`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='IBMS 设备';

