-- ----------------------------
-- IoT 数据大屏菜单 SQL
-- ----------------------------

-- 添加数据大屏菜单
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status,
    visible, keep_alive, always_show, creator,
    create_time, updater, update_time, deleted
) VALUES (
    '数据大屏', '', 2, 6, 2738,
    'dashboard', 'ep:data-analysis', 'iot/dashboard/index', 'IotDashboard', 0,
    b'1', b'1', b'1', '1',
    NOW(), '1', NOW(), b'0'
);












