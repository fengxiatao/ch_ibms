-- =============================================
-- IBMS 通道表结构 + 测试数据（MySQL）
-- 说明：
--  - 表：ibms_channel（通道/点位）
--  - 可重复执行：建表使用 IF NOT EXISTS；测试数据使用 INSERT ... WHERE NOT EXISTS
--  - 依赖：ibms_space（空间ID）以及 ibms_device（设备 SN / 名称）
-- =============================================

SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `ibms_channel` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '通道ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  `space_id` bigint NULL DEFAULT NULL COMMENT '空间ID（关联 ibms_space.id，可为空表示未分配）',
  `device_id` bigint NULL DEFAULT NULL COMMENT '设备ID（关联 ibms_device.id，可为空）',
  `code` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '通道编码，如 F01-LBY-VI-VT-001',
  `channel_no` int NOT NULL DEFAULT 1 COMMENT '通道号',
  `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '通道名称',
  `business` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '业务分类：security/access/alarm/parking/building/environment/lighting/energy',
  `type_code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '通道类型码（点位类型码），如 VT/DR/DI/DO/AI/AI_AN/LT/PM...',
  `category` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '通道类别（展示文案）',
  `system_type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '系统类型，如 VI/AC/AL/BA/EN...',
  `data_source` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '数据源，如 NVR/CTR/GW/DDC/Meter',
  `ip` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'IP地址',
  `mac` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'MAC地址',
  `device_sn` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '设备序列号（冗余）',
  `device_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '所属设备名称（冗余）',
  `space` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '空间位置文案（冗余）',
  `current_value` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '当前值',
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'online' COMMENT '状态：online/offline/warning/armed',
  `extra` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '扩展 JSON',
  `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_code_tenant` (`tenant_id`, `code`) USING BTREE,
  KEY `idx_space_id` (`space_id`) USING BTREE,
  KEY `idx_device_id` (`device_id`) USING BTREE,
  KEY `idx_business` (`business`) USING BTREE,
  KEY `idx_type_code` (`type_code`) USING BTREE,
  KEY `idx_system_type` (`system_type`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='IBMS 通道（点位）';

-- ----------------------------
-- 测试数据（与前端 Demo 示例对齐）
-- space_id 参考：ibms_space.sql 中的固定 ID
-- ----------------------------

-- 1) 视频通道：F01-LBY-VI-VT-001（关联空间 F01-LBY）
INSERT INTO `ibms_channel` (
  `id`, `tenant_id`, `space_id`, `device_id`,
  `code`, `channel_no`, `name`,
  `business`, `type_code`, `category`, `system_type`, `data_source`,
  `ip`, `mac`, `device_sn`, `device_name`, `space`,
  `current_value`, `status`, `extra`,
  `creator`, `create_time`, `updater`, `update_time`, `deleted`
)
SELECT
  3001, 0, 1002, NULL,
  'F01-LBY-VI-VT-001', 1, '大堂视频通道-001',
  'security', 'VT', '视频监控', 'VI', 'NVR',
  '10.0.1.11', 'AA:BB:CC:DD:EE:01', 'SN-DEMO-VCAM-001', '大堂摄像机-01', 'F01 大堂',
  '在线', 'online', NULL,
  'system', NOW(), 'system', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1 FROM `ibms_channel` WHERE `tenant_id` = 0 AND `code` = 'F01-LBY-VI-VT-001' AND `deleted` = b'0'
);

-- 2) 门禁点：F01-LBY-AC-DR-001（布防）
INSERT INTO `ibms_channel` (
  `id`, `tenant_id`, `space_id`, `device_id`,
  `code`, `channel_no`, `name`,
  `business`, `type_code`, `category`, `system_type`, `data_source`,
  `ip`, `mac`, `device_sn`, `device_name`, `space`,
  `current_value`, `status`, `extra`,
  `creator`, `create_time`, `updater`, `update_time`, `deleted`
)
SELECT
  3002, 0, 1002, NULL,
  'F01-LBY-AC-DR-001', 1, '门禁点-001',
  'access', 'DR', '门禁通行', 'AC', 'CTR',
  '10.0.1.21', 'AA:BB:CC:DD:EE:02', 'SN-DEMO-AC-001', '门禁控制器-01', 'F01 大堂',
  '0', 'armed', NULL,
  'system', NOW(), 'system', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1 FROM `ibms_channel` WHERE `tenant_id` = 0 AND `code` = 'F01-LBY-AC-DR-001' AND `deleted` = b'0'
);

-- 3) 报警输入：B01-PK-AL-DI-001（告警）
INSERT INTO `ibms_channel` (
  `id`, `tenant_id`, `space_id`, `device_id`,
  `code`, `channel_no`, `name`,
  `business`, `type_code`, `category`, `system_type`, `data_source`,
  `ip`, `mac`, `device_sn`, `device_name`, `space`,
  `current_value`, `status`, `extra`,
  `creator`, `create_time`, `updater`, `update_time`, `deleted`
)
SELECT
  3003, 0, 2002, NULL,
  'B01-PK-AL-DI-001', 1, '周界报警输入-001',
  'alarm', 'DI', '入侵报警', 'AL', 'GW',
  '10.0.2.31', 'AA:BB:CC:DD:EE:03', 'SN-DEMO-BA-001', '楼控网关-01', 'B01 停车场',
  '告警', 'warning', NULL,
  'system', NOW(), 'system', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1 FROM `ibms_channel` WHERE `tenant_id` = 0 AND `code` = 'B01-PK-AL-DI-001' AND `deleted` = b'0'
);

