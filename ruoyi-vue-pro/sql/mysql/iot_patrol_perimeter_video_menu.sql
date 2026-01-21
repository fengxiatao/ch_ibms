-- ========================================
-- 巡更管理、周界入侵、视频巡更模块菜单和权限配置 SQL
-- ========================================
-- 说明：为巡更管理、周界入侵、视频巡更模块的所有页面添加菜单和权限配置
-- 执行时间：2025-11-10
-- ========================================

-- ========================================
-- 1. 查询IoT主菜单ID（用于确定父菜单）
-- ========================================
SET @iot_root_id = (SELECT id FROM system_menu WHERE (name LIKE '%IoT%' OR name LIKE '%智慧物联%') AND type = 1 AND deleted = b'0' LIMIT 1);

-- 如果找不到，使用默认值（需要根据实际情况调整）
-- SET @iot_root_id = 2738;

-- ========================================
-- 2. 创建"巡更管理"目录菜单（如果不存在）
-- ========================================
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status,
    visible, keep_alive, always_show, creator,
    create_time, updater, update_time, deleted
)
SELECT * FROM (
    SELECT 
        '巡更管理' AS name,
        'iot:patrol:query' AS permission,
        1 AS type,  -- 目录
        40 AS sort,
        @iot_root_id AS parent_id,
        'patrol' AS path,
        'ep:map-location' AS icon,
        NULL AS component,
        NULL AS component_name,
        0 AS status,
        b'1' AS visible,
        b'1' AS keep_alive,
        b'1' AS always_show,
        '1' AS creator,
        NOW() AS create_time,
        '1' AS updater,
        NOW() AS update_time,
        b'0' AS deleted
) AS tmp
WHERE NOT EXISTS (
    SELECT 1 FROM system_menu WHERE name = '巡更管理' AND parent_id = @iot_root_id AND deleted = b'0'
) LIMIT 1;

-- 获取巡更管理目录ID
SET @patrol_root_id = (SELECT id FROM system_menu WHERE name = '巡更管理' AND parent_id = @iot_root_id AND deleted = b'0' LIMIT 1);

-- ========================================
-- 3. 创建巡更管理子菜单
-- ========================================

-- 3.1 巡更计划
INSERT INTO system_menu(name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT * FROM (
    SELECT '巡更计划', 'iot:patrol-plan:query', 2, 1, @patrol_root_id, 'plan', 'ep:calendar', 'iot/patrol/plan/index', 'PatrolPlan', 0, b'1', b'1', b'0', '1', NOW(), '1', NOW(), b'0'
) AS tmp WHERE NOT EXISTS (SELECT 1 FROM system_menu WHERE name = '巡更计划' AND parent_id = @patrol_root_id AND deleted = b'0') LIMIT 1;

SET @patrol_plan_id = (SELECT id FROM system_menu WHERE name = '巡更计划' AND parent_id = @patrol_root_id AND deleted = b'0' LIMIT 1);

-- 巡更计划权限
INSERT INTO system_menu(name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT * FROM (
    SELECT '巡更计划查询', 'iot:patrol-plan:query', 3, 1, @patrol_plan_id, NULL, NULL, NULL, NULL, 0, b'0', b'1', b'0', '1', NOW(), '1', NOW(), b'0' UNION ALL
    SELECT '巡更计划创建', 'iot:patrol-plan:create', 3, 2, @patrol_plan_id, NULL, NULL, NULL, NULL, 0, b'0', b'1', b'0', '1', NOW(), '1', NOW(), b'0' UNION ALL
    SELECT '巡更计划更新', 'iot:patrol-plan:update', 3, 3, @patrol_plan_id, NULL, NULL, NULL, NULL, 0, b'0', b'1', b'0', '1', NOW(), '1', NOW(), b'0' UNION ALL
    SELECT '巡更计划删除', 'iot:patrol-plan:delete', 3, 4, @patrol_plan_id, NULL, NULL, NULL, NULL, 0, b'0', b'1', b'0', '1', NOW(), '1', NOW(), b'0' UNION ALL
    SELECT '巡更计划导出', 'iot:patrol-plan:export', 3, 5, @patrol_plan_id, NULL, NULL, NULL, NULL, 0, b'0', b'1', b'0', '1', NOW(), '1', NOW(), b'0'
) AS tmp WHERE NOT EXISTS (SELECT 1 FROM system_menu WHERE permission LIKE 'iot:patrol-plan:%' AND deleted = b'0');

-- 3.2 巡更线路
INSERT INTO system_menu(name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT * FROM (
    SELECT '巡更线路', 'iot:patrol-route:query', 2, 2, @patrol_root_id, 'route', 'ep:connection', 'iot/patrol/route/index', 'PatrolRoute', 0, b'1', b'1', b'0', '1', NOW(), '1', NOW(), b'0'
) AS tmp WHERE NOT EXISTS (SELECT 1 FROM system_menu WHERE name = '巡更线路' AND parent_id = @patrol_root_id AND deleted = b'0') LIMIT 1;

SET @patrol_route_id = (SELECT id FROM system_menu WHERE name = '巡更线路' AND parent_id = @patrol_root_id AND deleted = b'0' LIMIT 1);

-- 巡更线路权限
INSERT INTO system_menu(name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT * FROM (
    SELECT '巡更线路查询', 'iot:patrol-route:query', 3, 1, @patrol_route_id, NULL, NULL, NULL, NULL, 0, b'0', b'1', b'0', '1', NOW(), '1', NOW(), b'0' UNION ALL
    SELECT '巡更线路创建', 'iot:patrol-route:create', 3, 2, @patrol_route_id, NULL, NULL, NULL, NULL, 0, b'0', b'1', b'0', '1', NOW(), '1', NOW(), b'0' UNION ALL
    SELECT '巡更线路更新', 'iot:patrol-route:update', 3, 3, @patrol_route_id, NULL, NULL, NULL, NULL, 0, b'0', b'1', b'0', '1', NOW(), '1', NOW(), b'0' UNION ALL
    SELECT '巡更线路删除', 'iot:patrol-route:delete', 3, 4, @patrol_route_id, NULL, NULL, NULL, NULL, 0, b'0', b'1', b'0', '1', NOW(), '1', NOW(), b'0' UNION ALL
    SELECT '巡更线路导出', 'iot:patrol-route:export', 3, 5, @patrol_route_id, NULL, NULL, NULL, NULL, 0, b'0', b'1', b'0', '1', NOW(), '1', NOW(), b'0'
) AS tmp WHERE NOT EXISTS (SELECT 1 FROM system_menu WHERE permission LIKE 'iot:patrol-route:%' AND deleted = b'0');

-- 3.3 巡更点位
INSERT INTO system_menu(name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT * FROM (
    SELECT '巡更点位', 'iot:patrol-point:query', 2, 3, @patrol_root_id, 'point', 'ep:location', 'iot/patrol/point/index', 'PatrolPoint', 0, b'1', b'1', b'0', '1', NOW(), '1', NOW(), b'0'
) AS tmp WHERE NOT EXISTS (SELECT 1 FROM system_menu WHERE name = '巡更点位' AND parent_id = @patrol_root_id AND deleted = b'0') LIMIT 1;

SET @patrol_point_id = (SELECT id FROM system_menu WHERE name = '巡更点位' AND parent_id = @patrol_root_id AND deleted = b'0' LIMIT 1);

-- 巡更点位权限
INSERT INTO system_menu(name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT * FROM (
    SELECT '巡更点位查询', 'iot:patrol-point:query', 3, 1, @patrol_point_id, NULL, NULL, NULL, NULL, 0, b'0', b'1', b'0', '1', NOW(), '1', NOW(), b'0' UNION ALL
    SELECT '巡更点位创建', 'iot:patrol-point:create', 3, 2, @patrol_point_id, NULL, NULL, NULL, NULL, 0, b'0', b'1', b'0', '1', NOW(), '1', NOW(), b'0' UNION ALL
    SELECT '巡更点位更新', 'iot:patrol-point:update', 3, 3, @patrol_point_id, NULL, NULL, NULL, NULL, 0, b'0', b'1', b'0', '1', NOW(), '1', NOW(), b'0' UNION ALL
    SELECT '巡更点位删除', 'iot:patrol-point:delete', 3, 4, @patrol_point_id, NULL, NULL, NULL, NULL, 0, b'0', b'1', b'0', '1', NOW(), '1', NOW(), b'0' UNION ALL
    SELECT '巡更点位导出', 'iot:patrol-point:export', 3, 5, @patrol_point_id, NULL, NULL, NULL, NULL, 0, b'0', b'1', b'0', '1', NOW(), '1', NOW(), b'0'
) AS tmp WHERE NOT EXISTS (SELECT 1 FROM system_menu WHERE permission LIKE 'iot:patrol-point:%' AND deleted = b'0');

-- 3.4 巡更任务
INSERT INTO system_menu(name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT * FROM (
    SELECT '巡更任务', 'iot:patrol-task:query', 2, 4, @patrol_root_id, 'task', 'ep:list', 'iot/patrol/task/index', 'PatrolTask', 0, b'1', b'1', b'0', '1', NOW(), '1', NOW(), b'0'
) AS tmp WHERE NOT EXISTS (SELECT 1 FROM system_menu WHERE name = '巡更任务' AND parent_id = @patrol_root_id AND deleted = b'0') LIMIT 1;

SET @patrol_task_id = (SELECT id FROM system_menu WHERE name = '巡更任务' AND parent_id = @patrol_root_id AND deleted = b'0' LIMIT 1);

-- 巡更任务权限
INSERT INTO system_menu(name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT * FROM (
    SELECT '巡更任务查询', 'iot:patrol-task:query', 3, 1, @patrol_task_id, NULL, NULL, NULL, NULL, 0, b'0', b'1', b'0', '1', NOW(), '1', NOW(), b'0' UNION ALL
    SELECT '巡更任务导出', 'iot:patrol-task:export', 3, 2, @patrol_task_id, NULL, NULL, NULL, NULL, 0, b'0', b'1', b'0', '1', NOW(), '1', NOW(), b'0'
) AS tmp WHERE NOT EXISTS (SELECT 1 FROM system_menu WHERE permission LIKE 'iot:patrol-task:%' AND deleted = b'0');

-- 3.5 巡更记录
INSERT INTO system_menu(name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT * FROM (
    SELECT '巡更记录', 'iot:patrol-record:query', 2, 5, @patrol_root_id, 'record', 'ep:document', 'iot/patrol/record/index', 'PatrolRecord', 0, b'1', b'1', b'0', '1', NOW(), '1', NOW(), b'0'
) AS tmp WHERE NOT EXISTS (SELECT 1 FROM system_menu WHERE name = '巡更记录' AND parent_id = @patrol_root_id AND deleted = b'0') LIMIT 1;

SET @patrol_record_id = (SELECT id FROM system_menu WHERE name = '巡更记录' AND parent_id = @patrol_root_id AND deleted = b'0' LIMIT 1);

-- 巡更记录权限
INSERT INTO system_menu(name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT * FROM (
    SELECT '巡更记录查询', 'iot:patrol-record:query', 3, 1, @patrol_record_id, NULL, NULL, NULL, NULL, 0, b'0', b'1', b'0', '1', NOW(), '1', NOW(), b'0' UNION ALL
    SELECT '巡更记录导出', 'iot:patrol-record:export', 3, 2, @patrol_record_id, NULL, NULL, NULL, NULL, 0, b'0', b'1', b'0', '1', NOW(), '1', NOW(), b'0'
) AS tmp WHERE NOT EXISTS (SELECT 1 FROM system_menu WHERE permission LIKE 'iot:patrol-record:%' AND deleted = b'0');

-- ========================================
-- 4. 创建"周界入侵"目录菜单（如果不存在）
-- ========================================
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status,
    visible, keep_alive, always_show, creator,
    create_time, updater, update_time, deleted
)
SELECT * FROM (
    SELECT 
        '周界入侵' AS name,
        'iot:perimeter:query' AS permission,
        1 AS type,  -- 目录
        50 AS sort,
        @iot_root_id AS parent_id,
        'perimeter' AS path,
        'ep:warning' AS icon,
        NULL AS component,
        NULL AS component_name,
        0 AS status,
        b'1' AS visible,
        b'1' AS keep_alive,
        b'1' AS always_show,
        '1' AS creator,
        NOW() AS create_time,
        '1' AS updater,
        NOW() AS update_time,
        b'0' AS deleted
) AS tmp
WHERE NOT EXISTS (
    SELECT 1 FROM system_menu WHERE name = '周界入侵' AND parent_id = @iot_root_id AND deleted = b'0'
) LIMIT 1;

-- 获取周界入侵目录ID
SET @perimeter_root_id = (SELECT id FROM system_menu WHERE name = '周界入侵' AND parent_id = @iot_root_id AND deleted = b'0' LIMIT 1);

-- 注意：周界入侵模块的后端和前端页面尚未开发，这里先创建菜单结构
-- 待开发完成后，再更新component路径

-- ========================================
-- 5. 创建"视频巡更"目录菜单（如果不存在）
-- ========================================
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status,
    visible, keep_alive, always_show, creator,
    create_time, updater, update_time, deleted
)
SELECT * FROM (
    SELECT 
        '视频巡更' AS name,
        'iot:video-patrol:query' AS permission,
        1 AS type,  -- 目录
        60 AS sort,
        @iot_root_id AS parent_id,
        'video-patrol' AS path,
        'ep:video-camera' AS icon,
        NULL AS component,
        NULL AS component_name,
        0 AS status,
        b'1' AS visible,
        b'1' AS keep_alive,
        b'1' AS always_show,
        '1' AS creator,
        NOW() AS create_time,
        '1' AS updater,
        NOW() AS update_time,
        b'0' AS deleted
) AS tmp
WHERE NOT EXISTS (
    SELECT 1 FROM system_menu WHERE name = '视频巡更' AND parent_id = @iot_root_id AND deleted = b'0'
) LIMIT 1;

-- 获取视频巡更目录ID
SET @video_patrol_root_id = (SELECT id FROM system_menu WHERE name = '视频巡更' AND parent_id = @iot_root_id AND deleted = b'0' LIMIT 1);

-- 注意：视频巡更模块已有部分后端代码，但前端页面路径需要确认
-- 如果已有前端页面，需要更新component路径

-- ========================================
-- 6. 更新security_menu.sql中已存在的菜单（如果存在）
-- ========================================
-- 更新电子巡更菜单（如果存在且component为NULL）
-- 注意：security_menu.sql中的"电子巡更"应该指向巡更管理目录，而不是具体页面
-- 如果security_menu.sql中的菜单ID是5090，且parent_id是5000（智慧安防），需要移动到IoT模块下
UPDATE system_menu 
SET parent_id = @patrol_root_id,
    component = NULL,  -- 目录菜单不需要component
    component_name = NULL,
    path = 'patrol',
    permission = 'iot:patrol:query',
    icon = 'ep:map-location'
WHERE (name = '电子巡更' OR id = 5090) AND component IS NULL AND deleted = b'0';

-- 更新周界入侵菜单（如果存在且component为NULL）
-- 注意：周界入侵模块尚未开发前端页面，暂时保持为目录
UPDATE system_menu 
SET parent_id = @perimeter_root_id,
    component = NULL,  -- 待开发完成后更新
    component_name = NULL,
    path = 'perimeter',
    permission = 'iot:perimeter:query',
    icon = 'ep:warning'
WHERE (name = '周界入侵' OR id = 5100) AND component IS NULL AND deleted = b'0';

-- 更新视频巡查菜单（如果存在且component为NULL）
-- 注意：视频巡更模块已有后端代码，但前端页面路径需要确认
UPDATE system_menu 
SET parent_id = @video_patrol_root_id,
    component = NULL,  -- 待确认前端页面路径后更新
    component_name = NULL,
    path = 'video-patrol',
    permission = 'iot:video-patrol:query',
    icon = 'ep:video-camera'
WHERE (name = '视频巡查' OR name = '视频巡更' OR id = 5110) AND component IS NULL AND deleted = b'0';

-- ========================================
-- 完成提示
-- ========================================
SELECT '巡更管理、周界入侵、视频巡更菜单配置完成！' AS message,
       '巡更管理目录ID: ' || COALESCE(@patrol_root_id, 'NULL') || 
       ', 周界入侵目录ID: ' || COALESCE(@perimeter_root_id, 'NULL') ||
       ', 视频巡更目录ID: ' || COALESCE(@video_patrol_root_id, 'NULL') AS detail;

