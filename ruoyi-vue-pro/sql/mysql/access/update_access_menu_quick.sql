-- =============================================
-- 智慧通行菜单快速更新脚本
-- 用于已有数据库的菜单结构更新
-- =============================================

USE ch_ibms;

-- 获取智慧通行父菜单ID
SET @access_parent_id = (
    SELECT id FROM system_menu 
    WHERE name = '智慧通行' 
    AND deleted = 0 
    LIMIT 1
);

SELECT CONCAT('智慧通行菜单ID: ', IFNULL(@access_parent_id, '未找到')) AS info;

-- 如果找不到智慧通行菜单，尝试查找其他可能的名称
SET @access_parent_id = IFNULL(@access_parent_id, (
    SELECT id FROM system_menu 
    WHERE name LIKE '%门禁%' OR name LIKE '%通行%'
    AND type = 1 
    AND deleted = 0 
    LIMIT 1
));

-- =============================================
-- 1. 隐藏独立的"设备管理"菜单
-- =============================================
UPDATE system_menu 
SET visible = 0, 
    updater = '1', 
    update_time = NOW()
WHERE name = '设备管理' 
AND parent_id = @access_parent_id 
AND deleted = 0;

SELECT CONCAT('隐藏设备管理菜单，受影响行数: ', ROW_COUNT()) AS result;

-- =============================================
-- 2. 检查门禁管理菜单是否存在
-- =============================================
SET @management_exists = (
    SELECT COUNT(*) FROM system_menu 
    WHERE name = '门禁管理' 
    AND parent_id = @access_parent_id 
    AND deleted = 0
);

SELECT CONCAT('门禁管理菜单存在: ', IF(@management_exists > 0, '是', '否')) AS info;

-- =============================================
-- 3. 如果不存在，创建门禁管理菜单
-- =============================================
SET @new_menu_id = (SELECT IFNULL(MAX(id), 0) + 1 FROM system_menu);

INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT 
    @new_menu_id,
    '门禁管理',
    'iot:access-management:query',
    2,
    3,
    @access_parent_id,
    'management',
    'ep:key',
    'iot/access/management/index',
    'AccessManagement',
    0, 1, 1, 1, '1', NOW(), '1', NOW(), 0
FROM DUAL
WHERE @management_exists = 0 AND @access_parent_id IS NOT NULL;

SELECT CONCAT('创建门禁管理菜单: ', IF(ROW_COUNT() > 0, '成功', '跳过（已存在）')) AS result;

-- 获取门禁管理菜单ID
SET @management_menu_id = (
    SELECT id FROM system_menu 
    WHERE name = '门禁管理' 
    AND parent_id = @access_parent_id 
    AND deleted = 0 
    LIMIT 1
);

-- =============================================
-- 4. 添加按钮权限
-- =============================================
SET @btn_base_id = (SELECT IFNULL(MAX(id), 0) + 1 FROM system_menu);

-- 门控操作权限
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT @btn_base_id, '门控操作', 'iot:access-management:control', 3, 1, @management_menu_id, '', '', '', NULL, 0, 1, 1, 1, '1', NOW(), '1', NOW(), 0
FROM DUAL
WHERE @management_menu_id IS NOT NULL
AND NOT EXISTS (SELECT 1 FROM system_menu WHERE permission = 'iot:access-management:control' AND deleted = 0);

-- =============================================
-- 5. 为超级管理员分配权限
-- =============================================
INSERT INTO system_role_menu (role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id)
SELECT 1, @management_menu_id, '1', NOW(), '1', NOW(), 0, 1
FROM DUAL
WHERE @management_menu_id IS NOT NULL
AND NOT EXISTS (
    SELECT 1 FROM system_role_menu 
    WHERE role_id = 1 AND menu_id = @management_menu_id AND deleted = 0
);

SELECT '权限分配完成' AS result;

-- =============================================
-- 6. 验证结果
-- =============================================
SELECT 
    id,
    name,
    path,
    component,
    sort,
    CASE visible WHEN 1 THEN '显示' ELSE '隐藏' END AS visible_status
FROM system_menu 
WHERE parent_id = @access_parent_id 
AND type = 2 
AND deleted = 0
ORDER BY sort;

SELECT '菜单更新完成！' AS final_result;
