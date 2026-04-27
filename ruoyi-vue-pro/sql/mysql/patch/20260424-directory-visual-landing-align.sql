-- =====================================================================
-- 目录点击可视化落地 - 菜单与租户套餐对齐补丁（20260424）
-- 目标：
-- 1) 为智慧通行/智慧楼宇/智慧能源补齐“可视化落地页”菜单（可隐藏）
-- 2) 将可视化菜单补入已包含对应目录的 system_tenant_package.menu_ids
-- 3) 后续通过租户套餐刷新接口，将 menu_ids 重新下发到各租户管理员角色
-- 说明：
-- - system_tenant_package.menu_ids 为 JSON 字符串（varchar），本脚本仅处理 JSON_VALID=1 的记录
-- - 语句幂等，可重复执行
-- =====================================================================

SET NAMES utf8mb4;

-- ---------------------------------------------------------------------
-- 0. 预检：目录菜单是否存在
-- ---------------------------------------------------------------------
SELECT id, name, type, path, component, visible
FROM system_menu
WHERE deleted = b'0'
  AND name IN ('智慧通行', '智慧楼宇', '智慧建筑', '智慧能源')
ORDER BY id;

-- ---------------------------------------------------------------------
-- 1. 智慧通行：可视化落地页（隐藏菜单）
-- ---------------------------------------------------------------------
SET @access_parent_id = (
  SELECT id FROM system_menu
  WHERE deleted = b'0' AND type = 1 AND name = '智慧通行'
  ORDER BY id DESC LIMIT 1
);

INSERT INTO system_menu (
  name, permission, type, sort, parent_id, path, icon, component, component_name,
  status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT
  '可视化门禁', '', 2, 1, @access_parent_id, 'visual-dashboard', 'ep:data-board',
  'iot/access/visual-dashboard/index', 'AccessVisualDashboard',
  0, b'0', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
FROM DUAL
WHERE @access_parent_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM system_menu
    WHERE deleted = b'0' AND component = 'iot/access/visual-dashboard/index'
  );

SET @access_visual_id = (
  SELECT id FROM system_menu
  WHERE deleted = b'0' AND component = 'iot/access/visual-dashboard/index'
  ORDER BY id DESC LIMIT 1
);

UPDATE system_tenant_package tp
SET tp.menu_ids = JSON_ARRAY_APPEND(CAST(tp.menu_ids AS JSON), '$', CAST(@access_visual_id AS JSON)),
    tp.update_time = NOW()
WHERE tp.deleted = b'0'
  AND @access_parent_id IS NOT NULL
  AND @access_visual_id IS NOT NULL
  AND JSON_VALID(tp.menu_ids)
  AND JSON_CONTAINS(CAST(tp.menu_ids AS JSON), CAST(@access_parent_id AS JSON), '$')
  AND NOT JSON_CONTAINS(CAST(tp.menu_ids AS JSON), CAST(@access_visual_id AS JSON), '$');

-- ---------------------------------------------------------------------
-- 2. 智慧楼宇/智慧建筑：可视化落地页（隐藏菜单）
-- ---------------------------------------------------------------------
SET @building_parent_id = (
  SELECT id FROM system_menu
  WHERE deleted = b'0' AND type = 1 AND name IN ('智慧楼宇', '智慧建筑')
  ORDER BY FIELD(name, '智慧楼宇', '智慧建筑'), id DESC
  LIMIT 1
);

INSERT INTO system_menu (
  name, permission, type, sort, parent_id, path, icon, component, component_name,
  status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT
  '智慧楼宇可视化', '', 2, 1, @building_parent_id, 'visual-dashboard', 'ep:office-building',
  'iot/building/building-visual-dashboard/index', 'BuildingVisualDashboard',
  0, b'0', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
FROM DUAL
WHERE @building_parent_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM system_menu
    WHERE deleted = b'0' AND component = 'iot/building/building-visual-dashboard/index'
  );

SET @building_visual_id = (
  SELECT id FROM system_menu
  WHERE deleted = b'0' AND component = 'iot/building/building-visual-dashboard/index'
  ORDER BY id DESC LIMIT 1
);

UPDATE system_tenant_package tp
SET tp.menu_ids = JSON_ARRAY_APPEND(CAST(tp.menu_ids AS JSON), '$', CAST(@building_visual_id AS JSON)),
    tp.update_time = NOW()
WHERE tp.deleted = b'0'
  AND @building_parent_id IS NOT NULL
  AND @building_visual_id IS NOT NULL
  AND JSON_VALID(tp.menu_ids)
  AND JSON_CONTAINS(CAST(tp.menu_ids AS JSON), CAST(@building_parent_id AS JSON), '$')
  AND NOT JSON_CONTAINS(CAST(tp.menu_ids AS JSON), CAST(@building_visual_id AS JSON), '$');

-- ---------------------------------------------------------------------
-- 3. 智慧能源：可视化落地页（隐藏菜单）
-- ---------------------------------------------------------------------
SET @energy_parent_id = (
  SELECT id FROM system_menu
  WHERE deleted = b'0' AND type = 1 AND name = '智慧能源'
  ORDER BY id DESC LIMIT 1
);

INSERT INTO system_menu (
  name, permission, type, sort, parent_id, path, icon, component, component_name,
  status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT
  '智慧能源总览', '', 2, 1, @energy_parent_id, 'overview', 'ep:lightning',
  'energy/Overview/index', 'EnergyOverview',
  0, b'0', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
FROM DUAL
WHERE @energy_parent_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM system_menu
    WHERE deleted = b'0' AND component = 'energy/Overview/index'
  );

SET @energy_visual_id = (
  SELECT id FROM system_menu
  WHERE deleted = b'0' AND component = 'energy/Overview/index'
  ORDER BY id DESC LIMIT 1
);

UPDATE system_tenant_package tp
SET tp.menu_ids = JSON_ARRAY_APPEND(CAST(tp.menu_ids AS JSON), '$', CAST(@energy_visual_id AS JSON)),
    tp.update_time = NOW()
WHERE tp.deleted = b'0'
  AND @energy_parent_id IS NOT NULL
  AND @energy_visual_id IS NOT NULL
  AND JSON_VALID(tp.menu_ids)
  AND JSON_CONTAINS(CAST(tp.menu_ids AS JSON), CAST(@energy_parent_id AS JSON), '$')
  AND NOT JSON_CONTAINS(CAST(tp.menu_ids AS JSON), CAST(@energy_visual_id AS JSON), '$');

-- ---------------------------------------------------------------------
-- 4. 结果校验
-- ---------------------------------------------------------------------
SELECT id, name, parent_id, path, component, visible, deleted
FROM system_menu
WHERE deleted = b'0'
  AND component IN (
    'iot/access/visual-dashboard/index',
    'iot/building/building-visual-dashboard/index',
    'energy/Overview/index'
  )
ORDER BY id;

SELECT id, name, JSON_LENGTH(CAST(menu_ids AS JSON)) AS menu_count, update_time
FROM system_tenant_package
WHERE deleted = b'0' AND JSON_VALID(menu_ids)
ORDER BY id;

-- ---------------------------------------------------------------------
-- 5. 执行后动作（必做）
-- ---------------------------------------------------------------------
-- 方式 A：刷新全部套餐下租户管理员角色菜单
--   POST /admin-api/system/tenant-package/refresh-all-tenant-role-menu
-- 方式 B：按套餐精确重同步
--   POST /admin-api/system/tenant-package/resync-role-menu?id={packageId}
