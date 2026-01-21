-- ========================================
-- 修复电子巡更、周界入侵、视频巡更菜单路由配置
-- ========================================
-- 说明：将security_menu.sql中创建的菜单（parent_id=5000）移动到IoT模块下
--       并配置正确的component路径指向新开发的页面
-- 执行时间：2025-11-10
-- ========================================

-- ========================================
-- 1. 查询IoT主菜单ID
-- ========================================
SET @iot_root_id = (SELECT id FROM system_menu WHERE (name LIKE '%IoT%' OR name LIKE '%智慧物联%') AND type = 1 AND deleted = b'0' LIMIT 1);

-- ========================================
-- 2. 查询或创建巡更管理目录
-- ========================================
-- 如果不存在，创建巡更管理目录
INSERT INTO system_menu(name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT * FROM (
    SELECT '巡更管理', 'iot:patrol:query', 1, 40, @iot_root_id, 'patrol', 'ep:map-location', NULL, NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
) AS tmp WHERE NOT EXISTS (SELECT 1 FROM system_menu WHERE name = '巡更管理' AND parent_id = @iot_root_id AND deleted = b'0') LIMIT 1;

SET @patrol_root_id = (SELECT id FROM system_menu WHERE name = '巡更管理' AND parent_id = @iot_root_id AND deleted = b'0' LIMIT 1);

-- ========================================
-- 3. 更新电子巡更菜单（ID=5090）
-- ========================================
-- 如果security_menu.sql中创建的菜单存在，将其移动到IoT模块下
UPDATE system_menu 
SET parent_id = @patrol_root_id,
    path = 'patrol',
    icon = 'ep:map-location',
    permission = 'iot:patrol:query',
    component = NULL,  -- 目录菜单
    component_name = NULL
WHERE (id = 5090 OR (name = '电子巡更' AND parent_id = 5000)) AND deleted = b'0';

-- 如果不存在，创建电子巡更菜单（作为巡更管理的别名或入口）
-- 注意：如果已经存在巡更管理目录，这个菜单可能不需要

-- ========================================
-- 4. 查询或创建周界入侵目录
-- ========================================
INSERT INTO system_menu(name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT * FROM (
    SELECT '周界入侵', 'iot:perimeter:query', 1, 50, @iot_root_id, 'perimeter', 'ep:warning', NULL, NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
) AS tmp WHERE NOT EXISTS (SELECT 1 FROM system_menu WHERE name = '周界入侵' AND parent_id = @iot_root_id AND deleted = b'0') LIMIT 1;

SET @perimeter_root_id = (SELECT id FROM system_menu WHERE name = '周界入侵' AND parent_id = @iot_root_id AND deleted = b'0' LIMIT 1);

-- 更新周界入侵菜单（ID=5100）
UPDATE system_menu 
SET parent_id = @perimeter_root_id,
    path = 'perimeter',
    icon = 'ep:warning',
    permission = 'iot:perimeter:query',
    component = NULL,  -- 待开发完成后更新
    component_name = NULL
WHERE (id = 5100 OR (name = '周界入侵' AND parent_id = 5000)) AND deleted = b'0';

-- ========================================
-- 5. 查询或创建视频巡更目录
-- ========================================
INSERT INTO system_menu(name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT * FROM (
    SELECT '视频巡更', 'iot:video-patrol:query', 1, 60, @iot_root_id, 'video-patrol', 'ep:video-camera', NULL, NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
) AS tmp WHERE NOT EXISTS (SELECT 1 FROM system_menu WHERE name = '视频巡更' AND parent_id = @iot_root_id AND deleted = b'0') LIMIT 1;

SET @video_patrol_root_id = (SELECT id FROM system_menu WHERE name = '视频巡更' AND parent_id = @iot_root_id AND deleted = b'0' LIMIT 1);

-- 更新视频巡查菜单（ID=5110）
UPDATE system_menu 
SET parent_id = @video_patrol_root_id,
    path = 'video-patrol',
    icon = 'ep:video-camera',
    permission = 'iot:video-patrol:query',
    component = NULL,  -- 待确认前端页面路径后更新
    component_name = NULL
WHERE (id = 5110 OR (name = '视频巡查' AND parent_id = 5000) OR (name = '视频巡更' AND parent_id = 5000)) AND deleted = b'0';

-- ========================================
-- 6. 确保巡更管理的子菜单已正确配置
-- ========================================
-- 检查巡更计划菜单是否存在且component正确
UPDATE system_menu 
SET component = 'iot/patrol/plan/index',
    component_name = 'PatrolPlan',
    path = 'plan'
WHERE name = '巡更计划' AND parent_id = @patrol_root_id AND (component IS NULL OR component != 'iot/patrol/plan/index') AND deleted = b'0';

-- 检查巡更线路菜单
UPDATE system_menu 
SET component = 'iot/patrol/route/index',
    component_name = 'PatrolRoute',
    path = 'route'
WHERE name = '巡更线路' AND parent_id = @patrol_root_id AND (component IS NULL OR component != 'iot/patrol/route/index') AND deleted = b'0';

-- 检查巡更点位菜单
UPDATE system_menu 
SET component = 'iot/patrol/point/index',
    component_name = 'PatrolPoint',
    path = 'point'
WHERE name = '巡更点位' AND parent_id = @patrol_root_id AND (component IS NULL OR component != 'iot/patrol/point/index') AND deleted = b'0';

-- 检查巡更任务菜单
UPDATE system_menu 
SET component = 'iot/patrol/task/index',
    component_name = 'PatrolTask',
    path = 'task'
WHERE name = '巡更任务' AND parent_id = @patrol_root_id AND (component IS NULL OR component != 'iot/patrol/task/index') AND deleted = b'0';

-- 检查巡更记录菜单
UPDATE system_menu 
SET component = 'iot/patrol/record/index',
    component_name = 'PatrolRecord',
    path = 'record'
WHERE name = '巡更记录' AND parent_id = @patrol_root_id AND (component IS NULL OR component != 'iot/patrol/record/index') AND deleted = b'0';

-- ========================================
-- 完成提示
-- ========================================
SELECT '菜单路由配置修复完成！' AS message,
       'IoT根菜单ID: ' || COALESCE(@iot_root_id, 'NULL') || 
       ', 巡更管理目录ID: ' || COALESCE(@patrol_root_id, 'NULL') ||
       ', 周界入侵目录ID: ' || COALESCE(@perimeter_root_id, 'NULL') ||
       ', 视频巡更目录ID: ' || COALESCE(@video_patrol_root_id, 'NULL') AS detail;

-- ========================================
-- 验证查询：检查菜单配置是否正确
-- ========================================
SELECT 
    id, name, parent_id, path, component, component_name, permission
FROM system_menu 
WHERE (name IN ('巡更管理', '电子巡更', '周界入侵', '视频巡更', '视频巡查', '巡更计划', '巡更线路', '巡更点位', '巡更任务', '巡更记录')
       OR id IN (5090, 5100, 5110))
  AND deleted = b'0'
ORDER BY id;



















