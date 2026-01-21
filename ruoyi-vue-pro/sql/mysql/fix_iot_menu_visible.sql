-- 修复IoT菜单可见性和路径问题

USE ch_ibms;

-- =============================================
-- 第1步：显示所有IoT菜单（最关键！）
-- =============================================

UPDATE system_menu
SET visible = 0  -- 0=显示, 1=隐藏
WHERE id >= 4000 AND id < 7000;

SELECT '[1] 已设置所有IoT菜单为可见（visible=0）' AS message;

-- =============================================
-- 第2步：修复设备管理菜单的component路径
-- =============================================

-- 修复 ID 6104 的component路径
UPDATE system_menu
SET component = 'iot/device/device/index'
WHERE id = 6104;

SELECT '[2] 已修复设备管理菜单component路径' AS message;

-- =============================================
-- 第3步：验证修复结果
-- =============================================

SELECT '[VERIFY] IoT Menu Configuration:' AS info;

SELECT 
    id, 
    parent_id,
    name,
    path,
    component,
    type,
    status AS '状态(0=启用)',
    visible AS '可见(0=显示)',
    CASE 
        WHEN type = 1 THEN '目录'
        WHEN type = 2 THEN '菜单'
        WHEN type = 3 THEN '按钮'
    END AS '类型'
FROM system_menu
WHERE id IN (4000, 4001, 4002, 4008, 6102, 6103, 6104)
ORDER BY id;

SELECT '
[SUCCESS] IoT Menu Fix Completed!

Fixed:
1. Set all IoT menus visible=0 (SHOW)
2. Fixed device menu component: iot/device/device/index

Next Steps:
1. Clear browser cache (Ctrl+Shift+Delete)
2. Refresh page (F5)
3. Try accessing IoT menus again

' AS message;

















