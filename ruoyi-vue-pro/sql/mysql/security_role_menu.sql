-- =============================================
-- 智慧安防角色权限配置
-- 创建时间: 2025-10-24
-- 说明: 为超级管理员角色分配智慧安防菜单权限
-- =============================================

-- 删除旧的角色菜单关联（如果存在）
DELETE FROM `system_role_menu` WHERE `menu_id` >= 5000 AND `menu_id` < 6000;

-- =============================================
-- 为超级管理员（role_id = 1）分配所有智慧安防菜单权限
-- =============================================

-- 批量插入角色菜单关联
-- 超级管理员自动拥有所有智慧安防菜单的访问权限
INSERT INTO `system_role_menu` (`role_id`, `menu_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
SELECT 
  1 AS role_id,
  m.id AS menu_id,
  '1' AS creator,
  NOW() AS create_time,
  '1' AS updater,
  NOW() AS update_time,
  b'0' AS deleted,
  1 AS tenant_id
FROM `system_menu` m
WHERE m.id >= 5000 
  AND m.id < 6000
  AND m.deleted = 0;

-- =============================================
-- 完成提示
-- =============================================
SELECT '智慧安防权限配置完成！' AS message,
       '超级管理员已获得 ' || COUNT(*) || ' 个菜单权限' AS detail
FROM `system_role_menu` 
WHERE `menu_id` >= 5000 AND `menu_id` < 6000 AND `deleted` = 0;

-- =============================================
-- 其他角色权限分配说明
-- =============================================
-- 如果需要为其他角色分配权限，可以执行以下SQL：
-- 
-- 示例1：为安防管理员角色（假设role_id = 10）分配基础权限
-- INSERT INTO `system_role_menu` (`role_id`, `menu_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
-- SELECT 10, m.id, '1', NOW(), '1', NOW(), b'0', 1
-- FROM `system_menu` m
-- WHERE m.id IN (5000, 5001, 5010, 5011, 5012, 5021, 5040) -- 选择特定菜单
--   AND m.deleted = 0;
--
-- 示例2：为普通用户角色分配只读权限（只查询，无增删改）
-- INSERT INTO `system_role_menu` (`role_id`, `menu_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
-- SELECT 5, m.id, '1', NOW(), '1', NOW(), b'0', 1
-- FROM `system_menu` m
-- WHERE m.id >= 5000 AND m.id < 6000
--   AND m.type IN (1, 2) -- 只分配目录和菜单，不分配按钮权限
--   AND m.deleted = 0;
-- =============================================


















