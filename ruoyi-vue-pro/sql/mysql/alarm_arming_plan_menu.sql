-- =============================================
-- 周界入侵-布防计划 菜单与权限配置
-- 创建时间: 2026-03-19
-- 说明: 为前端页面 security/PerimeterIntrusion/ArmingPlan/index 新增菜单与按钮权限
-- 前置条件:
-- 1) 周界入侵目录已存在（生产库中通常为 system_menu.id = 5138，名称“入侵报警”）
-- =============================================

-- 先清理（避免重复插入）
DELETE FROM `system_menu` WHERE `id` >= 72433 AND `id` <= 72437;

-- =============================================
-- 72433: 布防计划（二级菜单，与防区管理/报警记录平级）
-- always_show = b'0'：确保菜单可点击（即使有子按钮权限）
-- =============================================
INSERT INTO `system_menu` (
  `id`, `name`, `permission`, `type`, `sort`, `parent_id`,
  `path`, `icon`, `component`, `component_name`,
  `status`, `visible`, `keep_alive`, `always_show`,
  `creator`, `create_time`, `updater`, `update_time`, `deleted`
) VALUES (
  72433, '布防计划', 'security:perimeter:arming-plan', 2, 3, 5138,
  'arming-plan', 'ep:calendar', 'security/PerimeterIntrusion/ArmingPlan/index', 'ArmingPlan',
  0, b'1', b'1', b'0',
  '1', NOW(), '1', NOW(), b'0'
);

-- =============================================
-- 布防计划按钮权限（三级菜单）
-- =============================================

-- 72434: 新增
INSERT INTO `system_menu` VALUES (
  72434, '新增布防计划', 'security:perimeter:arming-plan:create', 3, 1, 72433,
  '', '', '', NULL,
  0, b'1', b'1', b'1',
  '1', NOW(), '1', NOW(), b'0'
);

-- 72435: 修改
INSERT INTO `system_menu` VALUES (
  72435, '修改布防计划', 'security:perimeter:arming-plan:update', 3, 2, 72433,
  '', '', '', NULL,
  0, b'1', b'1', b'1',
  '1', NOW(), '1', NOW(), b'0'
);

-- 72436: 删除
INSERT INTO `system_menu` VALUES (
  72436, '删除布防计划', 'security:perimeter:arming-plan:delete', 3, 3, 72433,
  '', '', '', NULL,
  0, b'1', b'1', b'1',
  '1', NOW(), '1', NOW(), b'0'
);

-- 72437: 启用/停用（可选）
INSERT INTO `system_menu` VALUES (
  72437, '启用停用', 'security:perimeter:arming-plan:status', 3, 4, 72433,
  '', '', '', NULL,
  0, b'1', b'1', b'1',
  '1', NOW(), '1', NOW(), b'0'
);

-- =============================================
-- 验证
-- =============================================
SELECT
  '布防计划菜单配置完成！' AS message,
  CONCAT('共创建 ', COUNT(*), ' 个菜单项') AS detail
FROM `system_menu`
WHERE `id` >= 72433 AND `id` <= 72437 AND `deleted` = b'0';

-- 查看周界入侵模块菜单（便于确认排序/路由）
-- SELECT id, name, parent_id, type, sort, path, component, component_name, permission
-- FROM system_menu
-- WHERE (id = 5138 OR parent_id = 5138) AND deleted = b'0'
-- ORDER BY sort, id;

