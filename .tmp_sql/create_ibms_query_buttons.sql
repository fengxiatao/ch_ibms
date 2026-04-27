-- 创建 IBMS产品查询 和 IBMS空间查询 按钮菜单
-- 这两个权限原本只挂在 type=2 页面菜单上，缺少 type=3 按钮菜单
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
VALUES 
('IBMS产品查询', 'iot:ibms-product:query', 3, 0, 71383, '', '', '', '', 0, b'1', b'0', b'0', '1', NOW(), '1', NOW(), b'0'),
('IBMS空间查询', 'iot:ibms-space:query', 3, 0, 71384, '', '', '', '', 0, b'1', b'0', b'0', '1', NOW(), '1', NOW(), b'0');
