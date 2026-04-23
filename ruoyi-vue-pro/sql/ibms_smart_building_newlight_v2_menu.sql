-- =====================================================
-- 智慧建筑-智能照明V2（newlight）菜单脚本（后端动态路由正式入口）
-- 说明：
-- 1) 仅提供“菜单数据”SQL，不会自动执行；请在目标数据库手工执行/导入
-- 2) 若你的库中“智能照明”目录菜单 ID 非 5192，请修改下面的 @PARENT_LIGHTING_MENU_ID
-- 3) 执行后需在“角色管理”给对应角色分配菜单，否则前端拿不到动态路由
-- 4) 前端页面组件路径已在 yudao-ui-admin-vue3 中创建：
--    - src/views/iot/building/newlight/**/index.vue
-- =====================================================

-- 智能照明（目录）菜单 ID（默认来自 ibms_smart_building_menu_init.sql）
SET @PARENT_LIGHTING_MENU_ID = 5192;

-- 为“智能照明V2”目录与子菜单选择一组不冲突的 ID（如你们有统一号段，请按规范替换）
SET @NEWLIGHT_DIR_ID = 72400;
SET @NEWLIGHT_OVERVIEW_ID = 72401;
SET @NEWLIGHT_CONTROL_ID = 72402;
SET @NEWLIGHT_DEVICE_ID = 72403;
SET @NEWLIGHT_TASK_ID = 72404;
SET @NEWLIGHT_LOG_ID = 72405;
SET @NEWLIGHT_ALARM_ID = 72406;
SET @NEWLIGHT_CIRCUIT_ID = 72407;

-- =====================================================
-- 一、目录：智能照明V2
-- 路由：/building/lighting/newlight
-- =====================================================
INSERT INTO system_menu
  (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, keep_alive, visible, always_show, creator, create_time, updater, update_time, deleted)
VALUES
  (@NEWLIGHT_DIR_ID, '智能照明V2', 'building:lighting:v2', 1, 90, @PARENT_LIGHTING_MENU_ID, 'newlight', 'ep:star', '', 'IntelligentLightingV2', 0, 1, 1, 1, '1', NOW(), '1', NOW(), 0)
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  permission = VALUES(permission),
  type = 1,
  sort = VALUES(sort),
  parent_id = VALUES(parent_id),
  path = VALUES(path),
  icon = VALUES(icon),
  component = '',
  component_name = VALUES(component_name),
  status = 0,
  keep_alive = 1,
  visible = 1,
  always_show = VALUES(always_show),
  updater = '1',
  update_time = NOW();

-- =====================================================
-- 二、菜单：智能照明V2 子页面（7 个）
-- 注意：component 必须能匹配到前端 src/views 下的实际文件
-- =====================================================

-- 1) 数据总览
INSERT INTO system_menu
  (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, keep_alive, visible, always_show, creator, create_time, updater, update_time, deleted)
VALUES
  (@NEWLIGHT_OVERVIEW_ID, '数据总览', 'building:lighting:v2:overview', 2, 1, @NEWLIGHT_DIR_ID, 'overview', 'ep:data-analysis', 'iot/building/newlight/overview/index', 'NewLightOverview', 0, 1, 1, 1, '1', NOW(), '1', NOW(), 0)
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  permission = VALUES(permission),
  type = 2,
  sort = VALUES(sort),
  parent_id = VALUES(parent_id),
  path = VALUES(path),
  icon = VALUES(icon),
  component = VALUES(component),
  component_name = VALUES(component_name),
  status = 0,
  keep_alive = 1,
  visible = 1,
  updater = '1',
  update_time = NOW();

-- 数据总览权限点（按钮/接口权限）
INSERT INTO system_menu
  (name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT * FROM (
  SELECT '数据总览查询', 'building:lighting:v2:overview:query', 3, 1, @NEWLIGHT_OVERVIEW_ID, NULL, NULL, NULL, NULL, 0, 0, 1, 0, '1', NOW(), '1', NOW(), 0
) AS tmp
WHERE NOT EXISTS (
  SELECT 1 FROM system_menu WHERE parent_id = @NEWLIGHT_OVERVIEW_ID AND type = 3 AND permission LIKE 'building:lighting:v2:overview:%' AND deleted = 0
);

-- 2) 照明控制
INSERT INTO system_menu
  (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, keep_alive, visible, always_show, creator, create_time, updater, update_time, deleted)
VALUES
  (@NEWLIGHT_CONTROL_ID, '照明控制', 'building:lighting:v2:control', 2, 2, @NEWLIGHT_DIR_ID, 'control', 'ep:switch-button', 'iot/building/newlight/control/index', 'NewLightControl', 0, 1, 1, 1, '1', NOW(), '1', NOW(), 0)
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  permission = VALUES(permission),
  type = 2,
  sort = VALUES(sort),
  parent_id = VALUES(parent_id),
  path = VALUES(path),
  icon = VALUES(icon),
  component = VALUES(component),
  component_name = VALUES(component_name),
  status = 0,
  keep_alive = 1,
  visible = 1,
  updater = '1',
  update_time = NOW();

-- 照明控制权限点（按钮/接口权限）
INSERT INTO system_menu
  (name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT * FROM (
  SELECT '照明控制查询',   'building:lighting:v2:control:query',   3, 1, @NEWLIGHT_CONTROL_ID, NULL, NULL, NULL, NULL, 0, 0, 1, 0, '1', NOW(), '1', NOW(), 0 UNION ALL
  SELECT '照明控制执行',   'building:lighting:v2:control:execute', 3, 2, @NEWLIGHT_CONTROL_ID, NULL, NULL, NULL, NULL, 0, 0, 1, 0, '1', NOW(), '1', NOW(), 0 UNION ALL
  SELECT '照明控制导出',   'building:lighting:v2:control:export',  3, 3, @NEWLIGHT_CONTROL_ID, NULL, NULL, NULL, NULL, 0, 0, 1, 0, '1', NOW(), '1', NOW(), 0
) AS tmp
WHERE NOT EXISTS (
  SELECT 1 FROM system_menu WHERE parent_id = @NEWLIGHT_CONTROL_ID AND type = 3 AND permission LIKE 'building:lighting:v2:control:%' AND deleted = 0
);

-- 3) 设备管理
INSERT INTO system_menu
  (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, keep_alive, visible, always_show, creator, create_time, updater, update_time, deleted)
VALUES
  (@NEWLIGHT_DEVICE_ID, '设备管理', 'building:lighting:v2:device', 2, 3, @NEWLIGHT_DIR_ID, 'device', 'ep:cpu', 'iot/building/newlight/device/index', 'NewLightDevice', 0, 1, 1, 1, '1', NOW(), '1', NOW(), 0)
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  permission = VALUES(permission),
  type = 2,
  sort = VALUES(sort),
  parent_id = VALUES(parent_id),
  path = VALUES(path),
  icon = VALUES(icon),
  component = VALUES(component),
  component_name = VALUES(component_name),
  status = 0,
  keep_alive = 1,
  visible = 1,
  updater = '1',
  update_time = NOW();

-- 设备管理权限点（按钮/接口权限）
INSERT INTO system_menu
  (name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT * FROM (
  SELECT '设备查询',   'building:lighting:v2:device:query',   3, 1, @NEWLIGHT_DEVICE_ID, NULL, NULL, NULL, NULL, 0, 0, 1, 0, '1', NOW(), '1', NOW(), 0 UNION ALL
  SELECT '设备创建',   'building:lighting:v2:device:create',  3, 2, @NEWLIGHT_DEVICE_ID, NULL, NULL, NULL, NULL, 0, 0, 1, 0, '1', NOW(), '1', NOW(), 0 UNION ALL
  SELECT '设备更新',   'building:lighting:v2:device:update',  3, 3, @NEWLIGHT_DEVICE_ID, NULL, NULL, NULL, NULL, 0, 0, 1, 0, '1', NOW(), '1', NOW(), 0 UNION ALL
  SELECT '设备删除',   'building:lighting:v2:device:delete',  3, 4, @NEWLIGHT_DEVICE_ID, NULL, NULL, NULL, NULL, 0, 0, 1, 0, '1', NOW(), '1', NOW(), 0 UNION ALL
  SELECT '设备导出',   'building:lighting:v2:device:export',  3, 5, @NEWLIGHT_DEVICE_ID, NULL, NULL, NULL, NULL, 0, 0, 1, 0, '1', NOW(), '1', NOW(), 0
) AS tmp
WHERE NOT EXISTS (
  SELECT 1 FROM system_menu WHERE parent_id = @NEWLIGHT_DEVICE_ID AND type = 3 AND permission LIKE 'building:lighting:v2:device:%' AND deleted = 0
);

-- 4) 任务管理
INSERT INTO system_menu
  (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, keep_alive, visible, always_show, creator, create_time, updater, update_time, deleted)
VALUES
  (@NEWLIGHT_TASK_ID, '任务管理', 'building:lighting:v2:task', 2, 4, @NEWLIGHT_DIR_ID, 'task', 'ep:calendar', 'iot/building/newlight/task/index', 'NewLightTask', 0, 1, 1, 1, '1', NOW(), '1', NOW(), 0)
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  permission = VALUES(permission),
  type = 2,
  sort = VALUES(sort),
  parent_id = VALUES(parent_id),
  path = VALUES(path),
  icon = VALUES(icon),
  component = VALUES(component),
  component_name = VALUES(component_name),
  status = 0,
  keep_alive = 1,
  visible = 1,
  updater = '1',
  update_time = NOW();

-- 任务管理权限点（按钮/接口权限）
INSERT INTO system_menu
  (name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT * FROM (
  SELECT '任务查询',   'building:lighting:v2:task:query',   3, 1, @NEWLIGHT_TASK_ID, NULL, NULL, NULL, NULL, 0, 0, 1, 0, '1', NOW(), '1', NOW(), 0 UNION ALL
  SELECT '任务创建',   'building:lighting:v2:task:create',  3, 2, @NEWLIGHT_TASK_ID, NULL, NULL, NULL, NULL, 0, 0, 1, 0, '1', NOW(), '1', NOW(), 0 UNION ALL
  SELECT '任务更新',   'building:lighting:v2:task:update',  3, 3, @NEWLIGHT_TASK_ID, NULL, NULL, NULL, NULL, 0, 0, 1, 0, '1', NOW(), '1', NOW(), 0 UNION ALL
  SELECT '任务删除',   'building:lighting:v2:task:delete',  3, 4, @NEWLIGHT_TASK_ID, NULL, NULL, NULL, NULL, 0, 0, 1, 0, '1', NOW(), '1', NOW(), 0 UNION ALL
  SELECT '任务执行',   'building:lighting:v2:task:execute', 3, 5, @NEWLIGHT_TASK_ID, NULL, NULL, NULL, NULL, 0, 0, 1, 0, '1', NOW(), '1', NOW(), 0 UNION ALL
  SELECT '任务导出',   'building:lighting:v2:task:export',  3, 6, @NEWLIGHT_TASK_ID, NULL, NULL, NULL, NULL, 0, 0, 1, 0, '1', NOW(), '1', NOW(), 0
) AS tmp
WHERE NOT EXISTS (
  SELECT 1 FROM system_menu WHERE parent_id = @NEWLIGHT_TASK_ID AND type = 3 AND permission LIKE 'building:lighting:v2:task:%' AND deleted = 0
);

-- 5) 日志管理
INSERT INTO system_menu
  (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, keep_alive, visible, always_show, creator, create_time, updater, update_time, deleted)
VALUES
  (@NEWLIGHT_LOG_ID, '日志管理', 'building:lighting:v2:log', 2, 5, @NEWLIGHT_DIR_ID, 'log', 'ep:document', 'iot/building/newlight/log/index', 'NewLightLog', 0, 1, 1, 1, '1', NOW(), '1', NOW(), 0)
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  permission = VALUES(permission),
  type = 2,
  sort = VALUES(sort),
  parent_id = VALUES(parent_id),
  path = VALUES(path),
  icon = VALUES(icon),
  component = VALUES(component),
  component_name = VALUES(component_name),
  status = 0,
  keep_alive = 1,
  visible = 1,
  updater = '1',
  update_time = NOW();

-- 日志管理权限点（按钮/接口权限）
INSERT INTO system_menu
  (name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT * FROM (
  SELECT '日志查询',   'building:lighting:v2:log:query',  3, 1, @NEWLIGHT_LOG_ID, NULL, NULL, NULL, NULL, 0, 0, 1, 0, '1', NOW(), '1', NOW(), 0 UNION ALL
  SELECT '日志导出',   'building:lighting:v2:log:export', 3, 2, @NEWLIGHT_LOG_ID, NULL, NULL, NULL, NULL, 0, 0, 1, 0, '1', NOW(), '1', NOW(), 0
) AS tmp
WHERE NOT EXISTS (
  SELECT 1 FROM system_menu WHERE parent_id = @NEWLIGHT_LOG_ID AND type = 3 AND permission LIKE 'building:lighting:v2:log:%' AND deleted = 0
);

-- 6) 告警信息
INSERT INTO system_menu
  (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, keep_alive, visible, always_show, creator, create_time, updater, update_time, deleted)
VALUES
  (@NEWLIGHT_ALARM_ID, '告警信息', 'building:lighting:v2:alarm', 2, 6, @NEWLIGHT_DIR_ID, 'alarm', 'ep:warning-filled', 'iot/building/newlight/alarm/index', 'NewLightAlarm', 0, 1, 1, 1, '1', NOW(), '1', NOW(), 0)
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  permission = VALUES(permission),
  type = 2,
  sort = VALUES(sort),
  parent_id = VALUES(parent_id),
  path = VALUES(path),
  icon = VALUES(icon),
  component = VALUES(component),
  component_name = VALUES(component_name),
  status = 0,
  keep_alive = 1,
  visible = 1,
  updater = '1',
  update_time = NOW();

-- 告警信息权限点（按钮/接口权限）
INSERT INTO system_menu
  (name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT * FROM (
  SELECT '告警查询',   'building:lighting:v2:alarm:query',   3, 1, @NEWLIGHT_ALARM_ID, NULL, NULL, NULL, NULL, 0, 0, 1, 0, '1', NOW(), '1', NOW(), 0 UNION ALL
  SELECT '告警处理',   'building:lighting:v2:alarm:handle',  3, 2, @NEWLIGHT_ALARM_ID, NULL, NULL, NULL, NULL, 0, 0, 1, 0, '1', NOW(), '1', NOW(), 0 UNION ALL
  SELECT '告警导出',   'building:lighting:v2:alarm:export',  3, 3, @NEWLIGHT_ALARM_ID, NULL, NULL, NULL, NULL, 0, 0, 1, 0, '1', NOW(), '1', NOW(), 0
) AS tmp
WHERE NOT EXISTS (
  SELECT 1 FROM system_menu WHERE parent_id = @NEWLIGHT_ALARM_ID AND type = 3 AND permission LIKE 'building:lighting:v2:alarm:%' AND deleted = 0
);

-- 7) 回路配置
INSERT INTO system_menu
  (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, keep_alive, visible, always_show, creator, create_time, updater, update_time, deleted)
VALUES
  (@NEWLIGHT_CIRCUIT_ID, '回路配置', 'building:lighting:v2:circuit', 2, 7, @NEWLIGHT_DIR_ID, 'circuit', 'ep:setting', 'iot/building/newlight/circuit/index', 'NewLightCircuit', 0, 1, 1, 1, '1', NOW(), '1', NOW(), 0)
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  permission = VALUES(permission),
  type = 2,
  sort = VALUES(sort),
  parent_id = VALUES(parent_id),
  path = VALUES(path),
  icon = VALUES(icon),
  component = VALUES(component),
  component_name = VALUES(component_name),
  status = 0,
  keep_alive = 1,
  visible = 1,
  updater = '1',
  update_time = NOW();

-- 回路配置权限点（按钮/接口权限）
INSERT INTO system_menu
  (name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT * FROM (
  SELECT '回路查询',   'building:lighting:v2:circuit:query',   3, 1, @NEWLIGHT_CIRCUIT_ID, NULL, NULL, NULL, NULL, 0, 0, 1, 0, '1', NOW(), '1', NOW(), 0 UNION ALL
  SELECT '回路创建',   'building:lighting:v2:circuit:create',  3, 2, @NEWLIGHT_CIRCUIT_ID, NULL, NULL, NULL, NULL, 0, 0, 1, 0, '1', NOW(), '1', NOW(), 0 UNION ALL
  SELECT '回路更新',   'building:lighting:v2:circuit:update',  3, 3, @NEWLIGHT_CIRCUIT_ID, NULL, NULL, NULL, NULL, 0, 0, 1, 0, '1', NOW(), '1', NOW(), 0 UNION ALL
  SELECT '回路删除',   'building:lighting:v2:circuit:delete',  3, 4, @NEWLIGHT_CIRCUIT_ID, NULL, NULL, NULL, NULL, 0, 0, 1, 0, '1', NOW(), '1', NOW(), 0 UNION ALL
  SELECT '回路导出',   'building:lighting:v2:circuit:export',  3, 5, @NEWLIGHT_CIRCUIT_ID, NULL, NULL, NULL, NULL, 0, 0, 1, 0, '1', NOW(), '1', NOW(), 0
) AS tmp
WHERE NOT EXISTS (
  SELECT 1 FROM system_menu WHERE parent_id = @NEWLIGHT_CIRCUIT_ID AND type = 3 AND permission LIKE 'building:lighting:v2:circuit:%' AND deleted = 0
);

-- =====================================================
-- 三、角色授权（可选示例）
-- 说明：不同项目 role_id 不同；建议在后端“角色管理”里勾选菜单
-- 下面仅给出示例（若你们超级管理员 role_id=1）
-- =====================================================
-- 说明：项目开启多租户时，system_role_menu 通常包含 tenant_id；下面按 tenant_id=1 示例
-- 如你们管理员 role_id 不是 1 / tenant_id 不是 1，请按实际替换
-- SET @ADMIN_ROLE_ID = 1;
-- SET @TENANT_ID = 1;
--
-- INSERT IGNORE INTO system_role_menu (role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id)
-- SELECT @ADMIN_ROLE_ID, id, '1', NOW(), '1', NOW(), 0, @TENANT_ID
-- FROM system_menu
-- WHERE id IN (
--   @NEWLIGHT_DIR_ID,
--   @NEWLIGHT_OVERVIEW_ID,
--   @NEWLIGHT_CONTROL_ID,
--   @NEWLIGHT_DEVICE_ID,
--   @NEWLIGHT_TASK_ID,
--   @NEWLIGHT_LOG_ID,
--   @NEWLIGHT_ALARM_ID,
--   @NEWLIGHT_CIRCUIT_ID
-- ) AND deleted = 0;
--
-- INSERT IGNORE INTO system_role_menu (role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id)
-- SELECT @ADMIN_ROLE_ID, id, '1', NOW(), '1', NOW(), 0, @TENANT_ID
-- FROM system_menu
-- WHERE parent_id IN (
--   @NEWLIGHT_OVERVIEW_ID,
--   @NEWLIGHT_CONTROL_ID,
--   @NEWLIGHT_DEVICE_ID,
--   @NEWLIGHT_TASK_ID,
--   @NEWLIGHT_LOG_ID,
--   @NEWLIGHT_ALARM_ID,
--   @NEWLIGHT_CIRCUIT_ID
-- ) AND type = 3 AND deleted = 0;

