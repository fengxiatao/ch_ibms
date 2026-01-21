-- =============================================
-- 分配报警主机权限给管理员角色
-- 创建时间: 2025-12-01
-- 说明: 给超级管理员角色分配报警主机的所有权限
-- =============================================

-- 查看当前角色
SELECT id, name, code FROM system_role WHERE deleted = 0;

-- 给超级管理员角色(ID=1)分配报警主机权限
INSERT IGNORE INTO system_role_menu (role_id, menu_id, creator, create_time, updater, update_time, deleted) VALUES
(1, 5101, '1', NOW(), '1', NOW(), 0), -- 报警主机查询
(1, 5102, '1', NOW(), '1', NOW(), 0), -- 新增报警主机
(1, 5103, '1', NOW(), '1', NOW(), 0), -- 修改报警主机
(1, 5104, '1', NOW(), '1', NOW(), 0), -- 删除报警主机
(1, 5105, '1', NOW(), '1', NOW(), 0), -- 布防操作
(1, 5106, '1', NOW(), '1', NOW(), 0); -- 消警操作

-- 如果有其他角色需要权限，可以添加类似的INSERT语句
-- 例如：给角色ID=2的角色分配权限
-- INSERT IGNORE INTO system_role_menu (role_id, menu_id, creator, create_time, updater, update_time, deleted) VALUES
-- (2, 5101, '1', NOW(), '1', NOW(), 0),
-- (2, 5102, '1', NOW(), '1', NOW(), 0);

-- 验证权限分配结果
SELECT 
    r.name AS role_name,
    m.name AS menu_name,
    m.permission AS permission_code
FROM system_role_menu rm
JOIN system_role r ON rm.role_id = r.id
JOIN system_menu m ON rm.menu_id = m.id
WHERE rm.menu_id >= 5101 AND rm.menu_id <= 5106
AND rm.deleted = 0
ORDER BY r.id, m.id;

-- =============================================
-- 使用说明
-- =============================================
-- 1. 执行本脚本后，需要清理Redis缓存
-- 2. 重新登录系统或刷新页面
-- 3. 检查"新增"按钮是否显示
-- =============================================
