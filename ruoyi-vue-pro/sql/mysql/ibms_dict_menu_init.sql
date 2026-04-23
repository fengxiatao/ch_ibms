-- =============================================
-- IBMS 菜单初始化（MySQL）
-- 目的：通过 system_menu 实现动态路由与侧边栏菜单
-- 说明：可重复执行，已存在则跳过
-- =============================================

-- 0) 清理旧菜单结构（曾经挂在 /iot/ibms/dict 下）
-- 说明：仅做软删除，避免重复菜单占位；可重复执行
UPDATE `system_menu` m
JOIN (
  SELECT `id` AS iot_id
  FROM `system_menu`
  WHERE `parent_id` = 0 AND `path` = '/iot' AND `deleted` = b'0'
  LIMIT 1
) p ON m.`parent_id` = p.iot_id
SET m.`deleted` = b'1', m.`update_time` = NOW(), m.`updater` = 'system'
WHERE m.`deleted` = b'0' AND m.`path` = 'ibms';

UPDATE `system_menu` c
JOIN (
  SELECT m.`id` AS ibms_id
  FROM `system_menu` m
  JOIN (
    SELECT `id` AS iot_id
    FROM `system_menu`
    WHERE `parent_id` = 0 AND `path` = '/iot' AND `deleted` = b'0'
    LIMIT 1
  ) p ON m.`parent_id` = p.iot_id
  WHERE m.`path` = 'ibms'
  LIMIT 1
) t ON c.`parent_id` = t.ibms_id
SET c.`deleted` = b'1', c.`update_time` = NOW(), c.`updater` = 'system'
WHERE c.`deleted` = b'0';

-- 1) 菜单：IBMS 字典管理（type=2 菜单，父级=智慧物联）
-- component 必须能匹配前端 views 下的组件路径（routerHelper 会用 import.meta.glob 解析）
INSERT INTO `system_menu`
(`name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT
  'IBMS 字典管理', 'system:dict:query', 2, 90,
  (SELECT `id` FROM `system_menu` WHERE `parent_id` = 0 AND `path` = '/iot' AND `deleted` = b'0' LIMIT 1),
  'ibms-dict', 'ep:collection-tag', 'ibms/dict/index', 'IbmsDict', 0, b'1', b'1', b'1',
  'system', NOW(), 'system', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1 FROM `system_menu`
  WHERE `deleted` = b'0'
    AND `parent_id` = (SELECT `id` FROM `system_menu` WHERE `parent_id` = 0 AND `path` = '/iot' AND `deleted` = b'0' LIMIT 1)
    AND `path` = 'ibms-dict'
);

-- 2) 菜单：IBMS 产品管理
INSERT INTO `system_menu`
(`name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT
  'IBMS 产品管理', 'iot:ibms-product:query', 2, 91,
  (SELECT `id` FROM `system_menu` WHERE `parent_id` = 0 AND `path` = '/iot' AND `deleted` = b'0' LIMIT 1),
  'ibms-product', 'ep:box', 'ibms/product/index', 'IbmsProduct', 0, b'1', b'1', b'1',
  'system', NOW(), 'system', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1 FROM `system_menu`
  WHERE `deleted` = b'0'
    AND `parent_id` = (SELECT `id` FROM `system_menu` WHERE `parent_id` = 0 AND `path` = '/iot' AND `deleted` = b'0' LIMIT 1)
    AND `path` = 'ibms-product'
);

-- 3) 菜单：IBMS 空间管理
INSERT INTO `system_menu`
(`name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT
  'IBMS 空间管理', 'iot:ibms-space:query', 2, 92,
  (SELECT `id` FROM `system_menu` WHERE `parent_id` = 0 AND `path` = '/iot' AND `deleted` = b'0' LIMIT 1),
  'ibms-space', 'ep:map-location', 'ibms/space/index', 'IbmsSpace', 0, b'1', b'1', b'1',
  'system', NOW(), 'system', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1 FROM `system_menu`
  WHERE `deleted` = b'0'
    AND `parent_id` = (SELECT `id` FROM `system_menu` WHERE `parent_id` = 0 AND `path` = '/iot' AND `deleted` = b'0' LIMIT 1)
    AND `path` = 'ibms-space'
);

-- 4) 菜单：IBMS 设备管理（页面骨架预留；权限后续补齐）
INSERT INTO `system_menu`
(`name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT
  'IBMS 设备管理', '', 2, 93,
  (SELECT `id` FROM `system_menu` WHERE `parent_id` = 0 AND `path` = '/iot' AND `deleted` = b'0' LIMIT 1),
  'ibms-device', 'ep:cpu', 'ibms/device/index', 'IbmsDevice', 0, b'1', b'1', b'1',
  'system', NOW(), 'system', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1 FROM `system_menu`
  WHERE `deleted` = b'0'
    AND `parent_id` = (SELECT `id` FROM `system_menu` WHERE `parent_id` = 0 AND `path` = '/iot' AND `deleted` = b'0' LIMIT 1)
    AND `path` = 'ibms-device'
);

-- 5) 菜单：IBMS 通道管理（页面骨架预留；权限后续补齐）
INSERT INTO `system_menu`
(`name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT
  'IBMS 通道管理', '', 2, 94,
  (SELECT `id` FROM `system_menu` WHERE `parent_id` = 0 AND `path` = '/iot' AND `deleted` = b'0' LIMIT 1),
  'ibms-channel', 'ep:share', 'ibms/channel/index', 'IbmsChannel', 0, b'1', b'1', b'1',
  'system', NOW(), 'system', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1 FROM `system_menu`
  WHERE `deleted` = b'0'
    AND `parent_id` = (SELECT `id` FROM `system_menu` WHERE `parent_id` = 0 AND `path` = '/iot' AND `deleted` = b'0' LIMIT 1)
    AND `path` = 'ibms-channel'
);

