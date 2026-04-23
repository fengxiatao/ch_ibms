-- =============================================
-- IBMS 产品/空间 API 权限点落库 + 授予超级管理员
-- 可重复执行（幂等）
-- 说明：与 IbmsProductController / IbmsSpaceController @PreAuthorize 一致。
-- =============================================

-- -----------------------------
-- IBMS 产品管理（iot:ibms-product:*）
-- -----------------------------
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, updater, deleted)
SELECT 'IBMS产品查询', 'iot:ibms-product:query', 3, 1,
       (SELECT id FROM system_menu WHERE deleted = b'0' AND path = 'ibms-product' ORDER BY id LIMIT 1),
       '', '', NULL, NULL, 0, b'1', b'1', b'1', '1', '1', b'0'
WHERE NOT EXISTS (SELECT 1 FROM system_menu WHERE deleted = b'0' AND permission = 'iot:ibms-product:query');

INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, updater, deleted)
SELECT 'IBMS产品创建', 'iot:ibms-product:create', 3, 2,
       (SELECT id FROM system_menu WHERE deleted = b'0' AND path = 'ibms-product' ORDER BY id LIMIT 1),
       '', '', NULL, NULL, 0, b'1', b'1', b'1', '1', '1', b'0'
WHERE NOT EXISTS (SELECT 1 FROM system_menu WHERE deleted = b'0' AND permission = 'iot:ibms-product:create');

INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, updater, deleted)
SELECT 'IBMS产品更新', 'iot:ibms-product:update', 3, 3,
       (SELECT id FROM system_menu WHERE deleted = b'0' AND path = 'ibms-product' ORDER BY id LIMIT 1),
       '', '', NULL, NULL, 0, b'1', b'1', b'1', '1', '1', b'0'
WHERE NOT EXISTS (SELECT 1 FROM system_menu WHERE deleted = b'0' AND permission = 'iot:ibms-product:update');

INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, updater, deleted)
SELECT 'IBMS产品删除', 'iot:ibms-product:delete', 3, 4,
       (SELECT id FROM system_menu WHERE deleted = b'0' AND path = 'ibms-product' ORDER BY id LIMIT 1),
       '', '', NULL, NULL, 0, b'1', b'1', b'1', '1', '1', b'0'
WHERE NOT EXISTS (SELECT 1 FROM system_menu WHERE deleted = b'0' AND permission = 'iot:ibms-product:delete');

INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, updater, deleted)
SELECT 'IBMS产品导出', 'iot:ibms-product:export', 3, 5,
       (SELECT id FROM system_menu WHERE deleted = b'0' AND path = 'ibms-product' ORDER BY id LIMIT 1),
       '', '', NULL, NULL, 0, b'1', b'1', b'1', '1', '1', b'0'
WHERE NOT EXISTS (SELECT 1 FROM system_menu WHERE deleted = b'0' AND permission = 'iot:ibms-product:export');

INSERT INTO system_role_menu (role_id, menu_id, tenant_id, creator, updater, deleted)
SELECT 1, m.id, 0, '1', '1', b'0'
FROM system_menu m
WHERE m.deleted = b'0' AND m.permission LIKE 'iot:ibms-product:%'
  AND NOT EXISTS (
    SELECT 1 FROM system_role_menu x
    WHERE x.deleted = b'0' AND x.role_id = 1 AND x.menu_id = m.id AND x.tenant_id = 0
  );

-- -----------------------------
-- IBMS 空间管理（iot:ibms-space:*）
-- -----------------------------
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, updater, deleted)
SELECT 'IBMS空间查询', 'iot:ibms-space:query', 3, 1,
       (SELECT id FROM system_menu WHERE deleted = b'0' AND path = 'ibms-space' ORDER BY id LIMIT 1),
       '', '', NULL, NULL, 0, b'1', b'1', b'1', '1', '1', b'0'
WHERE NOT EXISTS (SELECT 1 FROM system_menu WHERE deleted = b'0' AND permission = 'iot:ibms-space:query');

INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, updater, deleted)
SELECT 'IBMS空间创建', 'iot:ibms-space:create', 3, 2,
       (SELECT id FROM system_menu WHERE deleted = b'0' AND path = 'ibms-space' ORDER BY id LIMIT 1),
       '', '', NULL, NULL, 0, b'1', b'1', b'1', '1', '1', b'0'
WHERE NOT EXISTS (SELECT 1 FROM system_menu WHERE deleted = b'0' AND permission = 'iot:ibms-space:create');

INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, updater, deleted)
SELECT 'IBMS空间更新', 'iot:ibms-space:update', 3, 3,
       (SELECT id FROM system_menu WHERE deleted = b'0' AND path = 'ibms-space' ORDER BY id LIMIT 1),
       '', '', NULL, NULL, 0, b'1', b'1', b'1', '1', '1', b'0'
WHERE NOT EXISTS (SELECT 1 FROM system_menu WHERE deleted = b'0' AND permission = 'iot:ibms-space:update');

INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, updater, deleted)
SELECT 'IBMS空间删除', 'iot:ibms-space:delete', 3, 4,
       (SELECT id FROM system_menu WHERE deleted = b'0' AND path = 'ibms-space' ORDER BY id LIMIT 1),
       '', '', NULL, NULL, 0, b'1', b'1', b'1', '1', '1', b'0'
WHERE NOT EXISTS (SELECT 1 FROM system_menu WHERE deleted = b'0' AND permission = 'iot:ibms-space:delete');

INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, updater, deleted)
SELECT 'IBMS空间导出', 'iot:ibms-space:export', 3, 5,
       (SELECT id FROM system_menu WHERE deleted = b'0' AND path = 'ibms-space' ORDER BY id LIMIT 1),
       '', '', NULL, NULL, 0, b'1', b'1', b'1', '1', '1', b'0'
WHERE NOT EXISTS (SELECT 1 FROM system_menu WHERE deleted = b'0' AND permission = 'iot:ibms-space:export');

INSERT INTO system_role_menu (role_id, menu_id, tenant_id, creator, updater, deleted)
SELECT 1, m.id, 0, '1', '1', b'0'
FROM system_menu m
WHERE m.deleted = b'0' AND m.permission LIKE 'iot:ibms-space:%'
  AND NOT EXISTS (
    SELECT 1 FROM system_role_menu x
    WHERE x.deleted = b'0' AND x.role_id = 1 AND x.menu_id = m.id AND x.tenant_id = 0
  );
