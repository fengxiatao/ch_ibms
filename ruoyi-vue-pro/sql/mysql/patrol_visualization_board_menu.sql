-- =============================================
-- 智慧安防-电子巡更-巡更可视化 菜单&权限（动态路由）
-- 页面：
-- - yudao-ui-admin-vue3/src/views/security/ElectronicPatrol/PatrolVisualizationBoard/index.vue
-- 路由期望：
-- - /security/electronic-patrol/visualization-board
--
-- 设计说明：
-- 1) 不强依赖固定 ID，避免与历史初始化脚本冲突
-- 2) 优先按 path/component 定位；不存在才在 65200+ 段创建
-- 3) 重复执行安全（存在则更新/跳过）
-- =============================================

-- ================
-- 0. 定位（或创建）/security 根目录（智慧安防）
-- ================
SET @security_root_id :=
  (SELECT id FROM system_menu WHERE deleted = b'0' AND type = 1 AND path = '/security' ORDER BY id DESC LIMIT 1);

INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id,
  path, icon, component, component_name,
  status, visible, keep_alive, always_show,
  creator, create_time, updater, update_time, deleted
)
SELECT * FROM (
  SELECT 65200, '智慧安防', '', 1, 50, 0,
         '/security', 'fa-solid:shield-alt', NULL, NULL,
         0, b'1', b'1', b'1',
         '1', NOW(), '1', NOW(), b'0'
) AS tmp
WHERE @security_root_id IS NULL;

SET @security_root_id :=
  (SELECT id FROM system_menu WHERE deleted = b'0' AND type = 1 AND path = '/security' ORDER BY id DESC LIMIT 1);

-- ================
-- 1. 定位（或创建）父目录：电子巡更
-- ================
SET @epatrol_dir_id :=
  (SELECT id
   FROM system_menu
   WHERE deleted = b'0'
     AND type = 1
     AND parent_id = @security_root_id
     AND (path = 'electronic-patrol' OR name = '电子巡更')
   ORDER BY id DESC
   LIMIT 1);

INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id,
  path, icon, component, component_name,
  status, visible, keep_alive, always_show,
  creator, create_time, updater, update_time, deleted
)
SELECT * FROM (
  SELECT 65210, '电子巡更', '', 1, 40, @security_root_id,
         'electronic-patrol', 'fa:map-marker', NULL, NULL,
         0, b'1', b'1', b'1',
         '1', NOW(), '1', NOW(), b'0'
) AS tmp
WHERE @epatrol_dir_id IS NULL;

SET @epatrol_dir_id :=
  (SELECT id
   FROM system_menu
   WHERE deleted = b'0'
     AND type = 1
     AND parent_id = @security_root_id
     AND (path = 'electronic-patrol' OR name = '电子巡更')
   ORDER BY id DESC
   LIMIT 1);

-- ================
-- 2. 子菜单：巡更可视化
-- ================
SET @patrol_board_menu_id :=
  (SELECT id
   FROM system_menu
   WHERE deleted = b'0'
     AND type = 2
     AND parent_id = @epatrol_dir_id
     AND (path = 'visualization-board' OR component = 'security/ElectronicPatrol/PatrolVisualizationBoard/index')
   ORDER BY id DESC
   LIMIT 1);

INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id,
  path, icon, component, component_name,
  status, visible, keep_alive, always_show,
  creator, create_time, updater, update_time, deleted
)
SELECT * FROM (
  SELECT 65211, '巡更可视化', 'security:epatrol:visualization', 2, 1, @epatrol_dir_id,
         'visualization-board', 'ep:monitor', 'security/ElectronicPatrol/PatrolVisualizationBoard/index', 'PatrolVisualizationBoard',
         0, b'1', b'1', b'1',
         '1', NOW(), '1', NOW(), b'0'
) AS tmp
WHERE @patrol_board_menu_id IS NULL;

UPDATE system_menu
SET name = '巡更可视化',
    permission = 'security:epatrol:visualization',
    icon = 'ep:monitor',
    path = 'visualization-board',
    component = 'security/ElectronicPatrol/PatrolVisualizationBoard/index',
    component_name = 'PatrolVisualizationBoard',
    updater = '1',
    update_time = NOW()
WHERE id = @patrol_board_menu_id;

SET @patrol_board_menu_id :=
  (SELECT id
   FROM system_menu
   WHERE deleted = b'0'
     AND type = 2
     AND parent_id = @epatrol_dir_id
     AND (path = 'visualization-board' OR component = 'security/ElectronicPatrol/PatrolVisualizationBoard/index')
   ORDER BY id DESC
   LIMIT 1);

-- ================
-- 3. 按钮权限（可选）
-- ================
INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id,
  path, icon, component, component_name,
  status, visible, keep_alive, always_show,
  creator, create_time, updater, update_time, deleted
)
SELECT * FROM (
  SELECT 65212, '查看', 'security:epatrol:visualization:query', 3, 1, @patrol_board_menu_id,
         '', '', '', NULL,
         0, b'1', b'1', b'1',
         '1', NOW(), '1', NOW(), b'0'
) AS tmp
WHERE NOT EXISTS (
  SELECT 1 FROM system_menu
  WHERE deleted = b'0' AND type = 3 AND parent_id = @patrol_board_menu_id
    AND permission = 'security:epatrol:visualization:query'
);

-- ================
-- 4. 验证
-- ================
SELECT id, name, parent_id, type, path, component, permission
FROM system_menu
WHERE deleted = b'0'
  AND (
    id IN (65200, 65210, 65211, 65212)
    OR component = 'security/ElectronicPatrol/PatrolVisualizationBoard/index'
    OR (type = 2 AND parent_id = @epatrol_dir_id AND path = 'visualization-board')
  )
ORDER BY id;

