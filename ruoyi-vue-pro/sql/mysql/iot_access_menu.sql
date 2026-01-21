-- ========================================
-- 门禁管理模块菜单和权限配置 SQL
-- ========================================
-- 说明：为门禁管理模块的所有页面添加菜单和权限配置
-- 执行时间：2025-11-08
-- ========================================

-- ========================================
-- 1. 查询IoT主菜单ID（用于确定父菜单）
-- ========================================
SET @iot_root_id = (SELECT id FROM system_menu WHERE name LIKE '%IoT%' OR name LIKE '%智慧物联%' AND type = 1 AND deleted = b'0' LIMIT 1);

-- 如果找不到，使用默认值（需要根据实际情况调整）
-- SET @iot_root_id = 2738;

-- ========================================
-- 2. 创建"门禁管理"目录菜单（如果不存在）
-- ========================================
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status,
    visible, keep_alive, always_show, creator,
    create_time, updater, update_time, deleted
)
SELECT * FROM (
    SELECT 
        '门禁管理' AS name,
        'iot:access:query' AS permission,
        1 AS type,  -- 目录
        30 AS sort,
        @iot_root_id AS parent_id,
        'access' AS path,
        'ep:key' AS icon,
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
    SELECT 1 FROM system_menu WHERE name = '门禁管理' AND parent_id = @iot_root_id AND deleted = b'0'
) LIMIT 1;

-- 获取门禁管理目录ID
SET @access_menu_id = (SELECT id FROM system_menu WHERE name = '门禁管理' AND parent_id = @iot_root_id AND deleted = b'0' LIMIT 1);

-- ========================================
-- 3. 门禁电子地图
-- ========================================
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status,
    visible, keep_alive, always_show, creator,
    create_time, updater, update_time, deleted
)
SELECT * FROM (
    SELECT 
        '电子地图' AS name,
        'iot:access:map:query' AS permission,
        2 AS type,  -- 菜单
        1 AS sort,
        @access_menu_id AS parent_id,
        'map' AS path,
        'ep:map-location' AS icon,
        'iot/access/map/index' AS component,
        'AccessMap' AS component_name,
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
    SELECT 1 FROM system_menu WHERE component = 'iot/access/map/index' AND deleted = b'0'
) LIMIT 1;

-- ========================================
-- 4. 卡片列表
-- ========================================
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status,
    visible, keep_alive, always_show, creator,
    create_time, updater, update_time, deleted
)
SELECT * FROM (
    SELECT 
        '卡片列表' AS name,
        'iot:access:card:query' AS permission,
        2 AS type,
        2 AS sort,
        @access_menu_id AS parent_id,
        'card' AS path,
        'ep:document' AS icon,
        'iot/access/card/index' AS component,
        'AccessCard' AS component_name,
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
    SELECT 1 FROM system_menu WHERE component = 'iot/access/card/index' AND deleted = b'0'
) LIMIT 1;

-- ========================================
-- 5. 门组管理
-- ========================================
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status,
    visible, keep_alive, always_show, creator,
    create_time, updater, update_time, deleted
)
SELECT * FROM (
    SELECT 
        '门组管理' AS name,
        'iot:door-group:query' AS permission,
        2 AS type,
        3 AS sort,
        @access_menu_id AS parent_id,
        'door-group' AS path,
        'ep:office-building' AS icon,
        'iot/access/doorGroup/index' AS component,
        'DoorGroup' AS component_name,
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
    SELECT 1 FROM system_menu WHERE component = 'iot/access/doorGroup/index' AND deleted = b'0'
) LIMIT 1;

-- 门组管理按钮权限
INSERT INTO system_menu(name, permission, type, sort, parent_id, status, visible, creator, create_time, updater, update_time, deleted)
SELECT * FROM (
    SELECT '门组查询', 'iot:door-group:query', 3, 1, (SELECT id FROM system_menu WHERE component = 'iot/access/doorGroup/index' AND deleted = b'0' LIMIT 1), 0, b'1', '1', NOW(), '1', NOW(), b'0'
    UNION ALL SELECT '门组新增', 'iot:door-group:create', 3, 2, (SELECT id FROM system_menu WHERE component = 'iot/access/doorGroup/index' AND deleted = b'0' LIMIT 1), 0, b'1', '1', NOW(), '1', NOW(), b'0'
    UNION ALL SELECT '门组修改', 'iot:door-group:update', 3, 3, (SELECT id FROM system_menu WHERE component = 'iot/access/doorGroup/index' AND deleted = b'0' LIMIT 1), 0, b'1', '1', NOW(), '1', NOW(), b'0'
    UNION ALL SELECT '门组删除', 'iot:door-group:delete', 3, 4, (SELECT id FROM system_menu WHERE component = 'iot/access/doorGroup/index' AND deleted = b'0' LIMIT 1), 0, b'1', '1', NOW(), '1', NOW(), b'0'
    UNION ALL SELECT '门组导出', 'iot:door-group:export', 3, 5, (SELECT id FROM system_menu WHERE component = 'iot/access/doorGroup/index' AND deleted = b'0' LIMIT 1), 0, b'1', '1', NOW(), '1', NOW(), b'0'
) AS tmp
WHERE NOT EXISTS (
    SELECT 1 FROM system_menu WHERE permission IN ('iot:door-group:query', 'iot:door-group:create', 'iot:door-group:update', 'iot:door-group:delete', 'iot:door-group:export') AND deleted = b'0'
);

-- ========================================
-- 6. 门岗管理
-- ========================================
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status,
    visible, keep_alive, always_show, creator,
    create_time, updater, update_time, deleted
)
SELECT * FROM (
    SELECT 
        '门岗管理' AS name,
        'iot:door-post:query' AS permission,
        2 AS type,
        4 AS sort,
        @access_menu_id AS parent_id,
        'door-post' AS path,
        'ep:position' AS icon,
        'iot/access/doorPost/index' AS component,
        'DoorPost' AS component_name,
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
    SELECT 1 FROM system_menu WHERE component = 'iot/access/doorPost/index' AND deleted = b'0'
) LIMIT 1;

-- 门岗管理按钮权限
INSERT INTO system_menu(name, permission, type, sort, parent_id, status, visible, creator, create_time, updater, update_time, deleted)
SELECT * FROM (
    SELECT '门岗查询', 'iot:door-post:query', 3, 1, (SELECT id FROM system_menu WHERE component = 'iot/access/doorPost/index' AND deleted = b'0' LIMIT 1), 0, b'1', '1', NOW(), '1', NOW(), b'0'
    UNION ALL SELECT '门岗新增', 'iot:door-post:create', 3, 2, (SELECT id FROM system_menu WHERE component = 'iot/access/doorPost/index' AND deleted = b'0' LIMIT 1), 0, b'1', '1', NOW(), '1', NOW(), b'0'
    UNION ALL SELECT '门岗修改', 'iot:door-post:update', 3, 3, (SELECT id FROM system_menu WHERE component = 'iot/access/doorPost/index' AND deleted = b'0' LIMIT 1), 0, b'1', '1', NOW(), '1', NOW(), b'0'
    UNION ALL SELECT '门岗删除', 'iot:door-post:delete', 3, 4, (SELECT id FROM system_menu WHERE component = 'iot/access/doorPost/index' AND deleted = b'0' LIMIT 1), 0, b'1', '1', NOW(), '1', NOW(), b'0'
    UNION ALL SELECT '门岗导出', 'iot:door-post:export', 3, 5, (SELECT id FROM system_menu WHERE component = 'iot/access/doorPost/index' AND deleted = b'0' LIMIT 1), 0, b'1', '1', NOW(), '1', NOW(), b'0'
) AS tmp
WHERE NOT EXISTS (
    SELECT 1 FROM system_menu WHERE permission IN ('iot:door-post:query', 'iot:door-post:create', 'iot:door-post:update', 'iot:door-post:delete', 'iot:door-post:export') AND deleted = b'0'
);

-- ========================================
-- 7. 授权管理
-- ========================================
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status,
    visible, keep_alive, always_show, creator,
    create_time, updater, update_time, deleted
)
SELECT * FROM (
    SELECT 
        '授权管理' AS name,
        'iot:access-authorization:query' AS permission,
        2 AS type,
        5 AS sort,
        @access_menu_id AS parent_id,
        'authorization' AS path,
        'ep:user' AS icon,
        'iot/access/authorization/index' AS component,
        'AccessAuthorization' AS component_name,
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
    SELECT 1 FROM system_menu WHERE component = 'iot/access/authorization/index' AND deleted = b'0'
) LIMIT 1;

-- 授权管理按钮权限
INSERT INTO system_menu(name, permission, type, sort, parent_id, status, visible, creator, create_time, updater, update_time, deleted)
SELECT * FROM (
    SELECT '授权查询', 'iot:access-authorization:query', 3, 1, (SELECT id FROM system_menu WHERE component = 'iot/access/authorization/index' AND deleted = b'0' LIMIT 1), 0, b'1', '1', NOW(), '1', NOW(), b'0'
    UNION ALL SELECT '授权新增', 'iot:access-authorization:create', 3, 2, (SELECT id FROM system_menu WHERE component = 'iot/access/authorization/index' AND deleted = b'0' LIMIT 1), 0, b'1', '1', NOW(), '1', NOW(), b'0'
    UNION ALL SELECT '授权修改', 'iot:access-authorization:update', 3, 3, (SELECT id FROM system_menu WHERE component = 'iot/access/authorization/index' AND deleted = b'0' LIMIT 1), 0, b'1', '1', NOW(), '1', NOW(), b'0'
    UNION ALL SELECT '授权删除', 'iot:access-authorization:delete', 3, 4, (SELECT id FROM system_menu WHERE component = 'iot/access/authorization/index' AND deleted = b'0' LIMIT 1), 0, b'1', '1', NOW(), '1', NOW(), b'0'
    UNION ALL SELECT '授权导出', 'iot:access-authorization:export', 3, 5, (SELECT id FROM system_menu WHERE component = 'iot/access/authorization/index' AND deleted = b'0' LIMIT 1), 0, b'1', '1', NOW(), '1', NOW(), b'0'
) AS tmp
WHERE NOT EXISTS (
    SELECT 1 FROM system_menu WHERE permission IN ('iot:access-authorization:query', 'iot:access-authorization:create', 'iot:access-authorization:update', 'iot:access-authorization:delete', 'iot:access-authorization:export') AND deleted = b'0'
);

-- ========================================
-- 8. 通行记录
-- ========================================
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status,
    visible, keep_alive, always_show, creator,
    create_time, updater, update_time, deleted
)
SELECT * FROM (
    SELECT 
        '通行记录' AS name,
        'iot:access-record:query' AS permission,
        2 AS type,
        6 AS sort,
        @access_menu_id AS parent_id,
        'record' AS path,
        'ep:document' AS icon,
        'iot/access/record/index' AS component,
        'AccessRecord' AS component_name,
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
    SELECT 1 FROM system_menu WHERE component = 'iot/access/record/index' AND deleted = b'0'
) LIMIT 1;

-- 通行记录按钮权限
INSERT INTO system_menu(name, permission, type, sort, parent_id, status, visible, creator, create_time, updater, update_time, deleted)
SELECT * FROM (
    SELECT '通行记录查询', 'iot:access-record:query', 3, 1, (SELECT id FROM system_menu WHERE component = 'iot/access/record/index' AND deleted = b'0' LIMIT 1), 0, b'1', '1', NOW(), '1', NOW(), b'0'
    UNION ALL SELECT '通行记录导出', 'iot:access-record:export', 3, 2, (SELECT id FROM system_menu WHERE component = 'iot/access/record/index' AND deleted = b'0' LIMIT 1), 0, b'1', '1', NOW(), '1', NOW(), b'0'
) AS tmp
WHERE NOT EXISTS (
    SELECT 1 FROM system_menu WHERE permission IN ('iot:access-record:query', 'iot:access-record:export') AND deleted = b'0'
);

-- ========================================
-- 9. 下发记录
-- ========================================
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status,
    visible, keep_alive, always_show, creator,
    create_time, updater, update_time, deleted
)
SELECT * FROM (
    SELECT 
        '下发记录' AS name,
        'iot:access-dispatch:query' AS permission,
        2 AS type,
        7 AS sort,
        @access_menu_id AS parent_id,
        'dispatch' AS path,
        'ep:upload' AS icon,
        'iot/access/dispatch/index' AS component,
        'AccessDispatch' AS component_name,
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
    SELECT 1 FROM system_menu WHERE component = 'iot/access/dispatch/index' AND deleted = b'0'
) LIMIT 1;

-- 下发记录按钮权限
INSERT INTO system_menu(name, permission, type, sort, parent_id, status, visible, creator, create_time, updater, update_time, deleted)
SELECT * FROM (
    SELECT '下发记录查询', 'iot:access-dispatch:query', 3, 1, (SELECT id FROM system_menu WHERE component = 'iot/access/dispatch/index' AND deleted = b'0' LIMIT 1), 0, b'1', '1', NOW(), '1', NOW(), b'0'
    UNION ALL SELECT '下发记录重新下发', 'iot:access-dispatch:redispatch', 3, 2, (SELECT id FROM system_menu WHERE component = 'iot/access/dispatch/index' AND deleted = b'0' LIMIT 1), 0, b'1', '1', NOW(), '1', NOW(), b'0'
    UNION ALL SELECT '下发记录导出', 'iot:access-dispatch:export', 3, 3, (SELECT id FROM system_menu WHERE component = 'iot/access/dispatch/index' AND deleted = b'0' LIMIT 1), 0, b'1', '1', NOW(), '1', NOW(), b'0'
) AS tmp
WHERE NOT EXISTS (
    SELECT 1 FROM system_menu WHERE permission IN ('iot:access-dispatch:query', 'iot:access-dispatch:redispatch', 'iot:access-dispatch:export') AND deleted = b'0'
);

-- ========================================
-- 10. 告警记录
-- ========================================
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status,
    visible, keep_alive, always_show, creator,
    create_time, updater, update_time, deleted
)
SELECT * FROM (
    SELECT 
        '告警记录' AS name,
        'iot:access-alarm:query' AS permission,
        2 AS type,
        8 AS sort,
        @access_menu_id AS parent_id,
        'alarm' AS path,
        'ep:bell' AS icon,
        'iot/access/alarm/index' AS component,
        'AccessAlarm' AS component_name,
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
    SELECT 1 FROM system_menu WHERE component = 'iot/access/alarm/index' AND deleted = b'0'
) LIMIT 1;

-- 告警记录按钮权限
INSERT INTO system_menu(name, permission, type, sort, parent_id, status, visible, creator, create_time, updater, update_time, deleted)
SELECT * FROM (
    SELECT '告警记录查询', 'iot:access-alarm:query', 3, 1, (SELECT id FROM system_menu WHERE component = 'iot/access/alarm/index' AND deleted = b'0' LIMIT 1), 0, b'1', '1', NOW(), '1', NOW(), b'0'
    UNION ALL SELECT '告警记录处理', 'iot:access-alarm:handle', 3, 2, (SELECT id FROM system_menu WHERE component = 'iot/access/alarm/index' AND deleted = b'0' LIMIT 1), 0, b'1', '1', NOW(), '1', NOW(), b'0'
    UNION ALL SELECT '告警记录批量处理', 'iot:access-alarm:batch-handle', 3, 3, (SELECT id FROM system_menu WHERE component = 'iot/access/alarm/index' AND deleted = b'0' LIMIT 1), 0, b'1', '1', NOW(), '1', NOW(), b'0'
    UNION ALL SELECT '告警记录导出', 'iot:access-alarm:export', 3, 4, (SELECT id FROM system_menu WHERE component = 'iot/access/alarm/index' AND deleted = b'0' LIMIT 1), 0, b'1', '1', NOW(), '1', NOW(), b'0'
) AS tmp
WHERE NOT EXISTS (
    SELECT 1 FROM system_menu WHERE permission IN ('iot:access-alarm:query', 'iot:access-alarm:handle', 'iot:access-alarm:batch-handle', 'iot:access-alarm:export') AND deleted = b'0'
);

-- ========================================
-- 11. 验证查询 - 检查所有菜单是否创建成功
-- ========================================
SELECT 
    id,
    name,
    permission,
    type,
    path,
    component,
    parent_id,
    sort,
    visible,
    status
FROM system_menu 
WHERE parent_id = @access_menu_id
   OR component LIKE 'iot/access/%'
ORDER BY parent_id, sort;

-- ========================================
-- 12. 统计信息
-- ========================================
SELECT 
    '门禁管理菜单总数' AS metric,
    COUNT(*) AS value
FROM system_menu 
WHERE (parent_id = @access_menu_id OR component LIKE 'iot/access/%')
  AND deleted = b'0'

UNION ALL

SELECT 
    '门禁管理页面菜单数' AS metric,
    COUNT(*) AS value
FROM system_menu 
WHERE component LIKE 'iot/access/%'
  AND type = 2
  AND deleted = b'0'

UNION ALL

SELECT 
    '门禁管理按钮权限数' AS metric,
    COUNT(*) AS value
FROM system_menu 
WHERE permission LIKE 'iot:access:%' 
   OR permission LIKE 'iot:door-%'
  AND type = 3
  AND deleted = b'0';

-- ========================================
-- 使用说明
-- ========================================
-- 1. 执行前请先确认 @iot_root_id 的值是否正确
-- 2. 如果找不到IoT主菜单，请手动设置 @iot_root_id
-- 3. 执行后需要重新登录前端以刷新路由缓存
-- 4. 如果菜单不显示，检查 visible 和 status 字段
-- 5. 权限配置需要分配给相应的角色才能使用




















