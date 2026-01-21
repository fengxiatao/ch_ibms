-- 修复重复的IoT菜单
-- 问题：存在多个相同路径的菜单，导致 TooManyResultsException

USE ch_ibms;

-- =============================================
-- 第1步：查找重复菜单
-- =============================================

SELECT '[1] Finding Duplicate IoT Menus' AS info;

SELECT 
    id, 
    parent_id, 
    name, 
    path, 
    type,
    status,
    visible
FROM system_menu
WHERE name LIKE '%物联%' AND parent_id = 0
ORDER BY id;

-- =============================================
-- 第2步：统一使用 ID 4000 的菜单（保留原始的）
-- =============================================

-- 2.1 将 6102 下的子菜单移动到 4000 下
UPDATE system_menu
SET parent_id = 4000
WHERE parent_id = 6102;

SELECT '[2] Moved sub-menus from 6102 to 4000' AS message;

-- 2.2 删除角色菜单关联中的 6102
DELETE FROM system_role_menu
WHERE menu_id = 6102;

SELECT '[3] Deleted role-menu associations for 6102' AS message;

-- 2.3 删除重复的主菜单 6102
DELETE FROM system_menu
WHERE id = 6102;

SELECT '[4] Deleted duplicate menu 6102' AS message;

-- =============================================
-- 第3步：清理可能的其他重复菜单
-- =============================================

-- 检查是否还有其他重复的IoT菜单
SELECT '[5] Checking for other duplicates' AS info;

SELECT 
    path, 
    COUNT(*) as count
FROM system_menu
WHERE path LIKE '%iot%' 
    AND parent_id = 0
    AND deleted = 0
GROUP BY path
HAVING COUNT(*) > 1;

-- =============================================
-- 第4步：验证修复结果
-- =============================================

SELECT '[VERIFY] IoT Main Menu After Fix:' AS info;

SELECT 
    id, 
    parent_id, 
    name, 
    path, 
    component,
    type,
    status,
    visible
FROM system_menu
WHERE id = 4000 OR name LIKE '%物联%' AND parent_id = 0;

-- 统计 4000 下的子菜单数量
SELECT 
    '[VERIFY] Sub-menu count under ID 4000:' AS info,
    COUNT(*) AS count
FROM system_menu
WHERE parent_id = 4000;

SELECT '
[SUCCESS] Duplicate Menu Fix Completed!

Fixed:
1. Moved all sub-menus from 6102 to 4000
2. Deleted duplicate menu (ID 6102)
3. Now only one IoT main menu exists (ID 4000)

Next Steps:
1. Clear browser cache (Ctrl+Shift+R)
2. Logout and login again
3. IoT menus should work without errors now

' AS message;

















