-- =============================================
-- ibms_device 台账扩展列（双轨收敛：承接原 iot_device 认证/子系统/菜单等）
-- 按需执行一次；若列已存在会报错，可逐条注释已执行部分
-- =============================================

SET NAMES utf8mb4;

ALTER TABLE `ibms_device`
  ADD COLUMN `ibms_product_id` bigint NULL DEFAULT NULL COMMENT '关联 ibms_product.id' AFTER `product_key`,
  ADD COLUMN `nickname` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注名称' AFTER `name`,
  ADD COLUMN `pic_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '设备图片' AFTER `nickname`,
  ADD COLUMN `device_key` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '设备唯一标识(对接MQTT等)' AFTER `pic_url`,
  ADD COLUMN `device_secret` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '设备密钥' AFTER `device_key`,
  ADD COLUMN `auth_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '认证类型' AFTER `device_secret`,
  ADD COLUMN `subsystem_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '子系统代码' AFTER `auth_type`,
  ADD COLUMN `subsystem_override` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否手动覆盖子系统' AFTER `subsystem_code`,
  ADD COLUMN `menu_ids` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '关联菜单ID JSON数组' AFTER `subsystem_override`,
  ADD COLUMN `primary_menu_id` bigint NULL DEFAULT NULL COMMENT '主菜单ID' AFTER `menu_ids`,
  ADD COLUMN `menu_override` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否手动覆盖菜单' AFTER `primary_menu_id`,
  ADD COLUMN `dxf_entity_id` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'DXF实体ID' AFTER `menu_override`,
  ADD COLUMN `device_type` int NULL DEFAULT NULL COMMENT '数值型设备类型(网关/物模型兼容)' AFTER `dxf_entity_id`,
  ADD COLUMN `group_ids` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '设备分组ID集合JSON' AFTER `device_type`;
