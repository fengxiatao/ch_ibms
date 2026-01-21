-- =============================================
-- 检查用户权限配置
-- 创建时间: 2025-12-01
-- 说明: 检查当前用户是否有报警主机相关权限
-- =============================================

-- 1. 查看所有用户及其角色
SELECT 
    u.id as user_id,
    u.username,
    u.nickname,
    r.id as role_id,
    r.name as role_name,
    r.code as role_code
FROM system_users u
LEFT JOIN system_user_role ur ON u.id = ur.user_id
LEFT JOIN system_role r ON ur.role_id = r.id
WHERE u.deleted = 0 AND r.deleted = 0
ORDER BY u.id;

-- 2. 查看报警主机相关菜单
SELECT 
    id,
    name,
    permission,
    type,
    parent_id
FROM system_menu 
WHERE id >= 5101 AND id <= 5106
AND deleted = 0
ORDER BY id;

-- 3. 查看角色菜单权限分配
SELECT 
    r.name as role_name,
    m.name as menu_name,
    m.permission as permission_code
FROM system_role_menu rm
JOIN system_role r ON rm.role_id = r.id
JOIN system_menu m ON rm.menu_id = m.id
WHERE rm.menu_id >= 5101 AND rm.menu_id <= 5106
AND rm.deleted = 0
ORDER BY r.id, m.id;

-- 4. 检查超级管理员是否有所有权限
SELECT 
    '超级管理员权限检查' as check_type,
    COUNT(*) as assigned_permissions,
    '应该有6个权限' as expected
FROM system_role_menu rm
WHERE rm.role_id = 1 
AND rm.menu_id >= 5101 AND rm.menu_id <= 5106
AND rm.deleted = 0;

-- 5. 如果权限不足，自动分配给超级管理员
INSERT IGNORE INTO system_role_menu (role_id, menu_id, creator, create_time, updater, update_time, deleted) VALUES
(1, 5101, '1', NOW(), '1', NOW(), 0), -- 报警主机查询
(1, 5102, '1', NOW(), '1', NOW(), 0), -- 新增报警主机
(1, 5103, '1', NOW(), '1', NOW(), 0), -- 修改报警主机
(1, 5104, '1', NOW(), '1', NOW(), 0), -- 删除报警主机
(1, 5105, '1', NOW(), '1', NOW(), 0), -- 布防操作
(1, 5106, '1', NOW(), '1', NOW(), 0); -- 消警操作

-- 6. 再次检查权限分配结果
SELECT 
    '权限分配完成' as status,
    COUNT(*) as total_permissions
FROM system_role_menu rm
WHERE rm.role_id = 1 
AND rm.menu_id >= 5101 AND rm.menu_id <= 5106
AND rm.deleted = 0;

-- =============================================
-- 重要提示
-- =============================================
-- 执行完成后需要：
-- 1. 清理Redis缓存: redis-cli FLUSHALL
-- 2. 重启应用或重新登录
-- 3. 刷新前端页面
-- =============================================
