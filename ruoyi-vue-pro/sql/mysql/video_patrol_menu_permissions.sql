-- ========================================
-- 视频巡更模块 - 完整菜单和权限配置 SQL
-- ========================================
-- 说明：为视频巡更模块的所有页面添加菜单和权限配置
-- 执行时间：2025-11-16
-- ========================================

-- ========================================
-- 1. 查询IoT主菜单ID（用于确定父菜单）
-- ========================================
SET @iot_root_id = (SELECT id FROM system_menu WHERE (name LIKE '%IoT%' OR name LIKE '%智慧物联%') AND type = 1 AND deleted = 0 LIMIT 1);

-- 如果找不到IoT菜单，尝试查找"智慧安防"菜单
SET @security_root_id = (SELECT id FROM system_menu WHERE name = '智慧安防' AND type = 1 AND deleted = 0 LIMIT 1);

-- 优先使用IoT菜单，如果没有则使用智慧安防菜单
SET @parent_menu_id = COALESCE(@iot_root_id, @security_root_id);

-- ========================================
-- 2. 创建"视频巡更"目录菜单（如果不存在）
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
        '' AS permission,
        1 AS type,  -- 目录
        60 AS sort,
        @parent_menu_id AS parent_id,
        'VideoPatrol' AS path,
        'ep:video-camera' AS icon,
        NULL AS component,
        NULL AS component_name,
        0 AS status,
        1 AS visible,
        1 AS keep_alive,
        1 AS always_show,
        '1' AS creator,
        NOW() AS create_time,
        '1' AS updater,
        NOW() AS update_time,
        0 AS deleted
) AS tmp
WHERE NOT EXISTS (
    SELECT 1 FROM system_menu WHERE name = '视频巡更' AND parent_id = @parent_menu_id AND deleted = 0
) LIMIT 1;

-- 获取视频巡更目录ID
SET @video_patrol_root_id = (SELECT id FROM system_menu WHERE name = '视频巡更' AND parent_id = @parent_menu_id AND deleted = 0 LIMIT 1);

-- ========================================
-- 3. 创建视频巡更子菜单
-- ========================================

-- 3.1 巡更计划管理
INSERT INTO system_menu(name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted FROM (
    SELECT '巡更计划管理' AS name, '' AS permission, 2 AS type, 1 AS sort, @video_patrol_root_id AS parent_id, 'PatrolPlans' AS path, 'ep:calendar' AS icon, 'security/VideoPatrol/PatrolPlans/index' AS component, 'VideoPatrolPlans' AS component_name, 0 AS status, 1 AS visible, 1 AS keep_alive, 0 AS always_show, '1' AS creator, NOW() AS create_time, '1' AS updater, NOW() AS update_time, 0 AS deleted
) AS tmp WHERE NOT EXISTS (SELECT 1 FROM system_menu WHERE name = '巡更计划管理' AND parent_id = @video_patrol_root_id AND deleted = 0) LIMIT 1;

SET @patrol_plans_id = (SELECT id FROM system_menu WHERE name = '巡更计划管理' AND parent_id = @video_patrol_root_id AND deleted = 0 LIMIT 1);

-- 巡更计划管理权限
INSERT INTO system_menu(name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT * FROM (
    SELECT '巡更计划查询', 'iot:video-patrol-plan:query', 3, 1, @patrol_plans_id, NULL, NULL, NULL, NULL, 0, 0, 1, 0, '1', NOW(), '1', NOW(), 0 UNION ALL
    SELECT '巡更计划创建', 'iot:video-patrol-plan:create', 3, 2, @patrol_plans_id, NULL, NULL, NULL, NULL, 0, 0, 1, 0, '1', NOW(), '1', NOW(), 0 UNION ALL
    SELECT '巡更计划更新', 'iot:video-patrol-plan:update', 3, 3, @patrol_plans_id, NULL, NULL, NULL, NULL, 0, 0, 1, 0, '1', NOW(), '1', NOW(), 0 UNION ALL
    SELECT '巡更计划删除', 'iot:video-patrol-plan:delete', 3, 4, @patrol_plans_id, NULL, NULL, NULL, NULL, 0, 0, 1, 0, '1', NOW(), '1', NOW(), 0 UNION ALL
    SELECT '巡更计划导出', 'iot:video-patrol-plan:export', 3, 5, @patrol_plans_id, NULL, NULL, NULL, NULL, 0, 0, 1, 0, '1', NOW(), '1', NOW(), 0
) AS tmp WHERE NOT EXISTS (SELECT 1 FROM system_menu WHERE permission LIKE 'iot:video-patrol-plan:%' AND parent_id = @patrol_plans_id AND deleted = 0);

-- 3.2 巡更任务
INSERT INTO system_menu(name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT * FROM (
    SELECT '巡更任务', '', 2, 2, @video_patrol_root_id, 'PatrolTasks', 'ep:list', 'security/VideoPatrol/PatrolTasks/index', 'VideoPatrolTasks', 0, 1, 1, 0, '1', NOW(), '1', NOW(), 0
) AS tmp WHERE NOT EXISTS (SELECT 1 FROM system_menu WHERE name = '巡更任务' AND parent_id = @video_patrol_root_id AND deleted = 0) LIMIT 1;

SET @patrol_tasks_id = (SELECT id FROM system_menu WHERE name = '巡更任务' AND parent_id = @video_patrol_root_id AND deleted = 0 LIMIT 1);

-- 巡更任务权限
INSERT INTO system_menu(name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT * FROM (
    SELECT '巡更任务查询', 'iot:video-patrol-task:query', 3, 1, @patrol_tasks_id, NULL, NULL, NULL, NULL, 0, 0, 1, 0, '1', NOW(), '1', NOW(), 0 UNION ALL
    SELECT '巡更任务执行', 'iot:video-patrol-task:execute', 3, 2, @patrol_tasks_id, NULL, NULL, NULL, NULL, 0, 0, 1, 0, '1', NOW(), '1', NOW(), 0 UNION ALL
    SELECT '巡更任务导出', 'iot:video-patrol-task:export', 3, 3, @patrol_tasks_id, NULL, NULL, NULL, NULL, 0, 0, 1, 0, '1', NOW(), '1', NOW(), 0
) AS tmp WHERE NOT EXISTS (SELECT 1 FROM system_menu WHERE permission LIKE 'iot:video-patrol-task:%' AND parent_id = @patrol_tasks_id AND deleted = 0);

-- 3.3 任务管理
INSERT INTO system_menu(name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT * FROM (
    SELECT '任务管理', '', 2, 3, @video_patrol_root_id, 'PatrolTaskManagement', 'ep:setting', 'security/VideoPatrol/PatrolTaskManagement/index', 'VideoPatrolTaskManagement', 0, 1, 1, 0, '1', NOW(), '1', NOW(), 0
) AS tmp WHERE NOT EXISTS (SELECT 1 FROM system_menu WHERE name = '任务管理' AND parent_id = @video_patrol_root_id AND deleted = 0) LIMIT 1;

SET @task_management_id = (SELECT id FROM system_menu WHERE name = '任务管理' AND parent_id = @video_patrol_root_id AND deleted = 0 LIMIT 1);

-- 任务管理权限
INSERT INTO system_menu(name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT * FROM (
    SELECT '任务管理查询', 'iot:video-patrol-task-mgmt:query', 3, 1, @task_management_id, NULL, NULL, NULL, NULL, 0, 0, 1, 0, '1', NOW(), '1', NOW(), 0 UNION ALL
    SELECT '任务管理更新', 'iot:video-patrol-task-mgmt:update', 3, 2, @task_management_id, NULL, NULL, NULL, NULL, 0, 0, 1, 0, '1', NOW(), '1', NOW(), 0 UNION ALL
    SELECT '任务管理删除', 'iot:video-patrol-task-mgmt:delete', 3, 3, @task_management_id, NULL, NULL, NULL, NULL, 0, 0, 1, 0, '1', NOW(), '1', NOW(), 0
) AS tmp WHERE NOT EXISTS (SELECT 1 FROM system_menu WHERE permission LIKE 'iot:video-patrol-task-mgmt:%' AND parent_id = @task_management_id AND deleted = 0);

-- 3.4 点位管理
INSERT INTO system_menu(name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT * FROM (
    SELECT '点位管理', '', 2, 4, @video_patrol_root_id, 'PatrolPointManagement', 'ep:location', 'security/VideoPatrol/PatrolPointManagement/index', 'VideoPatrolPointManagement', 0, 1, 1, 0, '1', NOW(), '1', NOW(), 0
) AS tmp WHERE NOT EXISTS (SELECT 1 FROM system_menu WHERE name = '点位管理' AND parent_id = @video_patrol_root_id AND deleted = 0) LIMIT 1;

SET @point_management_id = (SELECT id FROM system_menu WHERE name = '点位管理' AND parent_id = @video_patrol_root_id AND deleted = 0 LIMIT 1);

-- 点位管理权限
INSERT INTO system_menu(name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT * FROM (
    SELECT '点位管理查询', 'iot:video-patrol-point:query', 3, 1, @point_management_id, NULL, NULL, NULL, NULL, 0, 0, 1, 0, '1', NOW(), '1', NOW(), 0 UNION ALL
    SELECT '点位管理创建', 'iot:video-patrol-point:create', 3, 2, @point_management_id, NULL, NULL, NULL, NULL, 0, 0, 1, 0, '1', NOW(), '1', NOW(), 0 UNION ALL
    SELECT '点位管理更新', 'iot:video-patrol-point:update', 3, 3, @point_management_id, NULL, NULL, NULL, NULL, 0, 0, 1, 0, '1', NOW(), '1', NOW(), 0 UNION ALL
    SELECT '点位管理删除', 'iot:video-patrol-point:delete', 3, 4, @point_management_id, NULL, NULL, NULL, NULL, 0, 0, 1, 0, '1', NOW(), '1', NOW(), 0 UNION ALL
    SELECT '点位管理导出', 'iot:video-patrol-point:export', 3, 5, @point_management_id, NULL, NULL, NULL, NULL, 0, 0, 1, 0, '1', NOW(), '1', NOW(), 0
) AS tmp WHERE NOT EXISTS (SELECT 1 FROM system_menu WHERE permission LIKE 'iot:video-patrol-point:%' AND parent_id = @point_management_id AND deleted = 0);

-- 3.5 记录查询
INSERT INTO system_menu(name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT * FROM (
    SELECT '记录查询', '', 2, 5, @video_patrol_root_id, 'PatrolRecordQuery', 'ep:document', 'security/VideoPatrol/PatrolRecordQuery/index', 'VideoPatrolRecordQuery', 0, 1, 1, 0, '1', NOW(), '1', NOW(), 0
) AS tmp WHERE NOT EXISTS (SELECT 1 FROM system_menu WHERE name = '记录查询' AND parent_id = @video_patrol_root_id AND deleted = 0) LIMIT 1;

SET @record_query_id = (SELECT id FROM system_menu WHERE name = '记录查询' AND parent_id = @video_patrol_root_id AND deleted = 0 LIMIT 1);

-- 记录查询权限
INSERT INTO system_menu(name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT * FROM (
    SELECT '记录查询查询', 'iot:video-patrol-record:query', 3, 1, @record_query_id, NULL, NULL, NULL, NULL, 0, 0, 1, 0, '1', NOW(), '1', NOW(), 0 UNION ALL
    SELECT '记录查询导出', 'iot:video-patrol-record:export', 3, 2, @record_query_id, NULL, NULL, NULL, NULL, 0, 0, 1, 0, '1', NOW(), '1', NOW(), 0 UNION ALL
    SELECT '记录查询确认', 'iot:video-patrol-record:confirm', 3, 3, @record_query_id, NULL, NULL, NULL, NULL, 0, 0, 1, 0, '1', NOW(), '1', NOW(), 0 UNION ALL
    SELECT '记录查询处理', 'iot:video-patrol-record:handle', 3, 4, @record_query_id, NULL, NULL, NULL, NULL, 0, 0, 1, 0, '1', NOW(), '1', NOW(), 0
) AS tmp WHERE NOT EXISTS (SELECT 1 FROM system_menu WHERE permission LIKE 'iot:video-patrol-record:%' AND parent_id = @record_query_id AND deleted = 0);

-- ========================================
-- 4. 为管理员角色分配所有视频巡更权限
-- ========================================
-- 获取管理员角色ID（通常是1）
SET @admin_role_id = 1;

-- 为管理员角色分配视频巡更目录权限
INSERT IGNORE INTO system_role_menu (role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id)
SELECT @admin_role_id, @video_patrol_root_id, '1', NOW(), '1', NOW(), 0, 1
WHERE @video_patrol_root_id IS NOT NULL;

-- 为管理员角色分配所有视频巡更子菜单权限
INSERT IGNORE INTO system_role_menu (role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id)
SELECT @admin_role_id, id, '1', NOW(), '1', NOW(), 0, 1
FROM system_menu
WHERE parent_id = @video_patrol_root_id AND deleted = 0;

-- 为管理员角色分配所有视频巡更操作权限
INSERT IGNORE INTO system_role_menu (role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id)
SELECT @admin_role_id, id, '1', NOW(), '1', NOW(), 0, 1
FROM system_menu
WHERE parent_id IN (
    SELECT id FROM system_menu WHERE parent_id = @video_patrol_root_id AND deleted = 0
) AND type = 3 AND deleted = 0;

-- ========================================
-- 完成提示
-- ========================================
SELECT '视频巡更模块菜单和权限配置完成！' AS message,
       CONCAT(
           '视频巡更目录ID: ', COALESCE(@video_patrol_root_id, 'NULL'), 
           ', 父菜单ID: ', COALESCE(@parent_menu_id, 'NULL'),
           ', 巡更计划管理ID: ', COALESCE(@patrol_plans_id, 'NULL'),
           ', 巡更任务ID: ', COALESCE(@patrol_tasks_id, 'NULL'),
           ', 任务管理ID: ', COALESCE(@task_management_id, 'NULL'),
           ', 点位管理ID: ', COALESCE(@point_management_id, 'NULL'),
           ', 记录查询ID: ', COALESCE(@record_query_id, 'NULL')
       ) AS detail;

-- ========================================
-- 验证菜单结构
-- ========================================
SELECT 
    m1.id AS '目录ID',
    m1.name AS '目录名称',
    m2.id AS '菜单ID',
    m2.name AS '菜单名称',
    m2.path AS '路径',
    m2.component AS '组件',
    COUNT(m3.id) AS '权限数量'
FROM system_menu m1
LEFT JOIN system_menu m2 ON m2.parent_id = m1.id AND m2.deleted = 0
LEFT JOIN system_menu m3 ON m3.parent_id = m2.id AND m3.type = 3 AND m3.deleted = 0
WHERE m1.name = '视频巡更' AND m1.deleted = 0
GROUP BY m1.id, m1.name, m2.id, m2.name, m2.path, m2.component
ORDER BY m2.sort;
