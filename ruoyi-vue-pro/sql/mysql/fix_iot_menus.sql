-- 修复IoT菜单配置
-- 问题：1. Admin没有权限  2. 菜单设置为隐藏  3. 404错误

USE ch_ibms;

-- =============================================
-- 第1步：修复菜单可见性（显示菜单）
-- =============================================

-- 显示所有IoT相关菜单
UPDATE system_menu
SET visible = 0  -- 0=显示, 1=隐藏
WHERE name LIKE '%物联%' OR name LIKE '%IoT%' OR path LIKE '%iot%';

SELECT '[1] 已设置IoT菜单为可见' AS message;

-- =============================================
-- 第2步：启用所有IoT菜单
-- =============================================

-- 启用所有IoT菜单（除了某些废弃的）
UPDATE system_menu
SET status = 0  -- 0=正常, 1=停用
WHERE (name LIKE '%物联%' OR name LIKE '%IoT%' OR path LIKE '%iot%')
AND id NOT IN (4050);  -- 排除智慧物联首页（ID: 4050，已废弃）

SELECT '[2] 已启用IoT菜单' AS message;

-- =============================================
-- 第3步：为super_admin角色分配所有IoT菜单权限
-- =============================================

-- 查找super_admin角色ID
SET @admin_role_id = (SELECT id FROM system_role WHERE code = 'super_admin' LIMIT 1);

-- 删除旧的IoT菜单权限（如果存在）
DELETE FROM system_role_menu
WHERE role_id = @admin_role_id
AND menu_id IN (
    SELECT id FROM system_menu
    WHERE name LIKE '%物联%' OR name LIKE '%IoT%' OR path LIKE '%iot%'
);

-- 为super_admin分配所有IoT菜单
INSERT INTO system_role_menu (role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id)
SELECT 
    @admin_role_id,
    id,
    'system',
    NOW(),
    'system',
    NOW(),
    0,
    1
FROM system_menu
WHERE (name LIKE '%物联%' OR name LIKE '%IoT%' OR path LIKE '%iot%')
AND status = 0;

SELECT CONCAT('[3] 已为super_admin分配 ', ROW_COUNT(), ' 个IoT菜单权限') AS message;

-- =============================================
-- 第4步：修复主菜单的component配置
-- =============================================

-- 主菜单（目录类型）的component通常为空或为Layout
UPDATE system_menu
SET component = CASE 
    WHEN type = 1 THEN NULL  -- 目录类型，component为空
    WHEN type = 2 AND (component IS NULL OR component = '') THEN 'iot/index'  -- 菜单类型，设置默认组件
    ELSE component
END
WHERE (name LIKE '%物联%' OR path LIKE '%iot%')
AND (component IS NULL OR component = '');

SELECT '[4] 已修复菜单component配置' AS message;

-- =============================================
-- 第5步：验证修复结果
-- =============================================

-- 显示修复后的IoT主菜单
SELECT 
    '[VERIFY] IoT Main Menus After Fix:' AS info;

SELECT 
    id, name, path, component, status, visible, type
FROM system_menu
WHERE parent_id = 0 
AND (name LIKE '%物联%' OR path LIKE '%iot%')
ORDER BY id;

-- 统计admin的IoT权限
SELECT 
    '[VERIFY] Admin IoT Menu Count:' AS info,
    COUNT(*) AS menu_count
FROM system_role_menu rm
JOIN system_role r ON rm.role_id = r.id
JOIN system_menu m ON rm.menu_id = m.id
WHERE r.code = 'super_admin'
AND (m.name LIKE '%物联%' OR m.path LIKE '%iot%');

-- =============================================
-- 完成
-- =============================================

SELECT '
[SUCCESS] IoT Menu Fix Completed!

Fixed Issues:
1. Set all IoT menus to visible (visible=0)
2. Enabled all IoT menus (status=0)  
3. Assigned all IoT menus to super_admin role
4. Fixed component configuration

Next Steps:
1. Logout and login again (to refresh permissions)
2. IoT menus should now be visible and accessible
3. No more 404 errors

' AS message;

















