-- =============================================
-- IBMS 设备 API 权限点（iot:ibms-device:*）落库 + 授予超级管理员
-- 可重复执行（幂等）
-- 说明：在 path=ibms-device 的页面菜单下插入 type=3 按钮，与 IbmsDeviceController @PreAuthorize 一致。
-- =============================================

INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, updater, deleted)
SELECT 'IBMS设备查询', 'iot:ibms-device:query', 3, 1,
       (SELECT id FROM system_menu WHERE deleted = b'0' AND path = 'ibms-device' ORDER BY id LIMIT 1),
       '', '', NULL, NULL, 0, b'1', b'1', b'1', '1', '1', b'0'
WHERE NOT EXISTS (SELECT 1 FROM system_menu WHERE deleted = b'0' AND permission = 'iot:ibms-device:query');

INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, updater, deleted)
SELECT 'IBMS设备创建', 'iot:ibms-device:create', 3, 2,
       (SELECT id FROM system_menu WHERE deleted = b'0' AND path = 'ibms-device' ORDER BY id LIMIT 1),
       '', '', NULL, NULL, 0, b'1', b'1', b'1', '1', '1', b'0'
WHERE NOT EXISTS (SELECT 1 FROM system_menu WHERE deleted = b'0' AND permission = 'iot:ibms-device:create');

INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, updater, deleted)
SELECT 'IBMS设备更新', 'iot:ibms-device:update', 3, 3,
       (SELECT id FROM system_menu WHERE deleted = b'0' AND path = 'ibms-device' ORDER BY id LIMIT 1),
       '', '', NULL, NULL, 0, b'1', b'1', b'1', '1', '1', b'0'
WHERE NOT EXISTS (SELECT 1 FROM system_menu WHERE deleted = b'0' AND permission = 'iot:ibms-device:update');

INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, updater, deleted)
SELECT 'IBMS设备删除', 'iot:ibms-device:delete', 3, 4,
       (SELECT id FROM system_menu WHERE deleted = b'0' AND path = 'ibms-device' ORDER BY id LIMIT 1),
       '', '', NULL, NULL, 0, b'1', b'1', b'1', '1', '1', b'0'
WHERE NOT EXISTS (SELECT 1 FROM system_menu WHERE deleted = b'0' AND permission = 'iot:ibms-device:delete');

INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, updater, deleted)
SELECT 'IBMS设备导出', 'iot:ibms-device:export', 3, 5,
       (SELECT id FROM system_menu WHERE deleted = b'0' AND path = 'ibms-device' ORDER BY id LIMIT 1),
       '', '', NULL, NULL, 0, b'1', b'1', b'1', '1', '1', b'0'
WHERE NOT EXISTS (SELECT 1 FROM system_menu WHERE deleted = b'0' AND permission = 'iot:ibms-device:export');

INSERT INTO system_role_menu (role_id, menu_id, tenant_id, creator, updater, deleted)
SELECT 1, m.id, 0, '1', '1', b'0'
FROM system_menu m
WHERE m.deleted = b'0' AND m.permission LIKE 'iot:ibms-device:%'
  AND NOT EXISTS (
    SELECT 1 FROM system_role_menu x
    WHERE x.deleted = b'0' AND x.role_id = 1 AND x.menu_id = m.id AND x.tenant_id = 0
  );
