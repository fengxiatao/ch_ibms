-- =============================================
-- 智慧通行菜单结构更新
-- Requirements: 8.1, 8.2
-- 1. 隐藏独立的"设备管理"菜单
-- 2. 添加新的"门禁管理"菜单（控制器+门通道组合视图）
-- =============================================

-- 获取智慧通行父菜单ID
SET @access_parent_id = (
    SELECT id FROM system_menu 
    WHERE name = '智慧通行' 
    AND parent_id = 4000 
    AND deleted = 0 
    LIMIT 1
);

-- =============================================
-- 步骤1：隐藏独立的"设备管理"菜单 (Requirements: 8.1)
-- 将 visible 设为 0，保留数据但不在菜单中显示
-- =============================================
UPDATE system_menu 
SET visible = 0, 
    updater = '1', 
    update_time = NOW()
WHERE name = '设备管理' 
AND parent_id = @access_parent_id 
AND path = 'device'
AND deleted = 0;

SELECT CONCAT('[1] 已隐藏设备管理菜单，受影响行数: ', ROW_COUNT()) AS message;

-- =============================================
-- 步骤2：检查是否已存在"门禁管理"菜单
-- =============================================
SET @management_menu_exists = (
    SELECT COUNT(*) FROM system_menu 
    WHERE name = '门禁管理' 
    AND parent_id = @access_parent_id 
    AND deleted = 0
);

-- =============================================
-- 步骤3：添加新的"门禁管理"菜单 (Requirements: 8.2)
-- 如果不存在则创建
-- =============================================
SET @max_menu_id = (SELECT IFNULL(MAX(id), 0) FROM system_menu);

INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT 
    @max_menu_id + 1,
    '门禁管理',
    'iot:access-management:query',
    2,  -- type=2 表示菜单
    3,  -- sort=3，放在权限配置后面
    @access_parent_id,
    'management',
    'ep:key',
    'iot/access/management/index',
    'AccessManagement',
    0,  -- status=0 正常
    1,  -- visible=1 显示
    1,  -- keep_alive=1 缓存
    1,  -- always_show=1 总是显示
    '1',
    NOW(),
    '1',
    NOW(),
    0
FROM DUAL
WHERE @management_menu_exists = 0;

SELECT CONCAT('[2] 门禁管理菜单创建状态: ', IF(@management_menu_exists = 0, '已创建', '已存在，跳过')) AS message;

-- =============================================
-- 步骤4：添加门禁管理的按钮权限
-- =============================================
SET @management_menu_id = (
    SELECT id FROM system_menu 
    WHERE name = '门禁管理' 
    AND parent_id = @access_parent_id 
    AND deleted = 0 
    LIMIT 1
);

-- 门控操作权限（与Controller中的@PreAuthorize注解一致）
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT 
    @max_menu_id + 2,
    '门控操作',
    'iot:access-management:control',
    3,  -- type=3 表示按钮
    1,
    @management_menu_id,
    '',
    '',
    '',
    NULL,
    0, 1, 1, 1, '1', NOW(), '1', NOW(), 0
FROM DUAL
WHERE @management_menu_exists = 0 AND @management_menu_id IS NOT NULL;

-- 远程开门权限
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT 
    @max_menu_id + 3,
    '远程开门',
    'iot:access-management:open-door',
    3,
    2,
    @management_menu_id,
    '',
    '',
    '',
    NULL,
    0, 1, 1, 1, '1', NOW(), '1', NOW(), 0
FROM DUAL
WHERE @management_menu_exists = 0 AND @management_menu_id IS NOT NULL;

-- 远程关门权限
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT 
    @max_menu_id + 4,
    '远程关门',
    'iot:access-management:close-door',
    3,
    3,
    @management_menu_id,
    '',
    '',
    '',
    NULL,
    0, 1, 1, 1, '1', NOW(), '1', NOW(), 0
FROM DUAL
WHERE @management_menu_exists = 0 AND @management_menu_id IS NOT NULL;

-- 设置常开权限
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT 
    @max_menu_id + 5,
    '设置常开',
    'iot:access-management:always-open',
    3,
    4,
    @management_menu_id,
    '',
    '',
    '',
    NULL,
    0, 1, 1, 1, '1', NOW(), '1', NOW(), 0
FROM DUAL
WHERE @management_menu_exists = 0 AND @management_menu_id IS NOT NULL;

-- 设置常闭权限
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT 
    @max_menu_id + 6,
    '设置常闭',
    'iot:access-management:always-closed',
    3,
    5,
    @management_menu_id,
    '',
    '',
    '',
    NULL,
    0, 1, 1, 1, '1', NOW(), '1', NOW(), 0
FROM DUAL
WHERE @management_menu_exists = 0 AND @management_menu_id IS NOT NULL;

SELECT '[3] 门禁管理按钮权限已添加' AS message;

-- =============================================
-- 步骤5：为超级管理员角色分配新菜单权限
-- =============================================
INSERT INTO system_role_menu (role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id)
SELECT 1, id, '1', NOW(), '1', NOW(), 0, 1 
FROM system_menu 
WHERE id BETWEEN @max_menu_id + 1 AND @max_menu_id + 6
AND NOT EXISTS (
    SELECT 1 FROM system_role_menu 
    WHERE role_id = 1 AND menu_id = system_menu.id AND deleted = 0
);

SELECT '[4] 已为超级管理员分配门禁管理权限' AS message;

-- =============================================
-- 步骤6：调整菜单排序，确保门禁管理在合适位置
-- =============================================
-- 门禁管理排在第3位（人事管理=1, 权限配置=2, 门禁管理=3）
UPDATE system_menu 
SET sort = 3, update_time = NOW()
WHERE name = '门禁管理' 
AND parent_id = @access_parent_id 
AND deleted = 0;

-- 通道管理排在第4位
UPDATE system_menu 
SET sort = 4, update_time = NOW()
WHERE name = '通道管理' 
AND parent_id = @access_parent_id 
AND deleted = 0;

-- 事件监控排在第5位
UPDATE system_menu 
SET sort = 5, update_time = NOW()
WHERE name = '事件监控' 
AND parent_id = @access_parent_id 
AND deleted = 0;

-- 操作日志排在第6位
UPDATE system_menu 
SET sort = 6, update_time = NOW()
WHERE name = '操作日志' 
AND parent_id = @access_parent_id 
AND deleted = 0;

SELECT '[5] 菜单排序已调整' AS message;

-- =============================================
-- 验证结果
-- =============================================
SELECT 
    id,
    name,
    path,
    component,
    sort,
    visible,
    CASE visible WHEN 1 THEN '显示' ELSE '隐藏' END AS visible_desc
FROM system_menu 
WHERE parent_id = @access_parent_id 
AND type = 2 
AND deleted = 0
ORDER BY sort;

SELECT '[完成] 智慧通行菜单结构更新完成' AS message;
