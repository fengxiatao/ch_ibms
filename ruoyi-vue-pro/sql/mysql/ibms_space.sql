-- =============================================
-- IBMS 空间表结构 + 测试数据（MySQL）
-- 说明：
--  - 表：ibms_space（空间树）
--  - 可重复执行：建表使用 IF NOT EXISTS；测试数据使用 INSERT ... WHERE NOT EXISTS
-- =============================================

SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `ibms_space` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '空间ID',
  `parent_id` bigint NOT NULL DEFAULT 0 COMMENT '父空间ID（0 为根）',
  `space_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '空间编码（组合）：code[-sub_code]，如 F01 / F01-LBY',
  `code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '区域码，如 F01/B01/PK/LB/OUT',
  `sub_code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '子区域码，如 LBY/FM（可选）',
  `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '空间名称',
  `type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '空间类型：floor/area/room',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序',
  `extra` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '扩展 JSON',
  `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_space_code_tenant` (`tenant_id`, `space_code`) USING BTREE,
  KEY `idx_parent_id` (`parent_id`) USING BTREE,
  KEY `idx_code` (`code`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='IBMS 空间（树）';

-- ----------------------------
-- 测试数据（与前端 Demo 示例对齐）
-- 约定：使用固定 ID，方便通道 seed 直接引用
-- tenant_id 统一为 0（演示/测试环境）
-- ----------------------------

-- F01（地上 1 层）
INSERT INTO `ibms_space` (
  `id`, `parent_id`, `space_code`, `code`, `sub_code`, `name`, `type`, `sort`, `extra`,
  `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`
)
SELECT
  1001, 0, 'F01', 'F01', NULL, '地上 1 层 (F01)', 'floor', 1, NULL,
  'system', NOW(), 'system', NOW(), b'0', 0
WHERE NOT EXISTS (
  SELECT 1 FROM `ibms_space` WHERE `tenant_id` = 0 AND `space_code` = 'F01' AND `deleted` = b'0'
);

-- F01-LBY（大堂）
INSERT INTO `ibms_space` (
  `id`, `parent_id`, `space_code`, `code`, `sub_code`, `name`, `type`, `sort`, `extra`,
  `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`
)
SELECT
  1002, 1001, 'F01-LBY', 'LB', 'LBY', '大堂 (LBY)', 'area', 10, NULL,
  'system', NOW(), 'system', NOW(), b'0', 0
WHERE NOT EXISTS (
  SELECT 1 FROM `ibms_space` WHERE `tenant_id` = 0 AND `space_code` = 'F01-LBY' AND `deleted` = b'0'
);

-- F01-OUT（室外）
INSERT INTO `ibms_space` (
  `id`, `parent_id`, `space_code`, `code`, `sub_code`, `name`, `type`, `sort`, `extra`,
  `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`
)
SELECT
  1003, 1001, 'F01-OUT', 'OUT', NULL, '室外 (OUT)', 'area', 20, NULL,
  'system', NOW(), 'system', NOW(), b'0', 0
WHERE NOT EXISTS (
  SELECT 1 FROM `ibms_space` WHERE `tenant_id` = 0 AND `space_code` = 'F01-OUT' AND `deleted` = b'0'
);

-- B01（地下一层）
INSERT INTO `ibms_space` (
  `id`, `parent_id`, `space_code`, `code`, `sub_code`, `name`, `type`, `sort`, `extra`,
  `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`
)
SELECT
  2001, 0, 'B01', 'B01', NULL, '地下一层 (B01)', 'floor', 2, NULL,
  'system', NOW(), 'system', NOW(), b'0', 0
WHERE NOT EXISTS (
  SELECT 1 FROM `ibms_space` WHERE `tenant_id` = 0 AND `space_code` = 'B01' AND `deleted` = b'0'
);

-- B01-PK（停车场）
INSERT INTO `ibms_space` (
  `id`, `parent_id`, `space_code`, `code`, `sub_code`, `name`, `type`, `sort`, `extra`,
  `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`
)
SELECT
  2002, 2001, 'B01-PK', 'PK', NULL, '停车场 (PK)', 'area', 10, NULL,
  'system', NOW(), 'system', NOW(), b'0', 0
WHERE NOT EXISTS (
  SELECT 1 FROM `ibms_space` WHERE `tenant_id` = 0 AND `space_code` = 'B01-PK' AND `deleted` = b'0'
);

