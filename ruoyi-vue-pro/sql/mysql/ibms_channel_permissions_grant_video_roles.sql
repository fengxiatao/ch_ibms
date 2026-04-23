-- =============================================
-- IBMS 通道 API 权限点（iot:ibms-channel:*）落库 + 授予视频相关角色
-- 可重复执行（幂等）
-- 说明：
--   1) 在「IBMS 通道管理」页面菜单（path=ibms-channel）下插入 type=3 按钮权限，供 @ss.hasPermission 校验与角色分配。
--   2) 自动授予：① 超级管理员 role_id=1；② 已拥有「视频预览/回放」查询权限菜单的角色（视为原视频运维角色集）。
--   若贵司视频角色使用其它权限标识，可追加 IN 列表或改为固定 role_id。
-- =============================================

-- 1) 按钮菜单（与 IbmsChannelController @PreAuthorize 一致）
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, updater, deleted)
SELECT 'IBMS通道查询', 'iot:ibms-channel:query', 3, 1,
       (SELECT id FROM system_menu WHERE deleted = b'0' AND path = 'ibms-channel' ORDER BY id LIMIT 1),
       '', '', NULL, NULL, 0, b'1', b'1', b'1', '1', '1', b'0'
WHERE NOT EXISTS (SELECT 1 FROM system_menu WHERE deleted = b'0' AND permission = 'iot:ibms-channel:query');

INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, updater, deleted)
SELECT 'IBMS通道创建', 'iot:ibms-channel:create', 3, 2,
       (SELECT id FROM system_menu WHERE deleted = b'0' AND path = 'ibms-channel' ORDER BY id LIMIT 1),
       '', '', NULL, NULL, 0, b'1', b'1', b'1', '1', '1', b'0'
WHERE NOT EXISTS (SELECT 1 FROM system_menu WHERE deleted = b'0' AND permission = 'iot:ibms-channel:create');

INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, updater, deleted)
SELECT 'IBMS通道更新', 'iot:ibms-channel:update', 3, 3,
       (SELECT id FROM system_menu WHERE deleted = b'0' AND path = 'ibms-channel' ORDER BY id LIMIT 1),
       '', '', NULL, NULL, 0, b'1', b'1', b'1', '1', '1', b'0'
WHERE NOT EXISTS (SELECT 1 FROM system_menu WHERE deleted = b'0' AND permission = 'iot:ibms-channel:update');

INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, updater, deleted)
SELECT 'IBMS通道删除', 'iot:ibms-channel:delete', 3, 4,
       (SELECT id FROM system_menu WHERE deleted = b'0' AND path = 'ibms-channel' ORDER BY id LIMIT 1),
       '', '', NULL, NULL, 0, b'1', b'1', b'1', '1', '1', b'0'
WHERE NOT EXISTS (SELECT 1 FROM system_menu WHERE deleted = b'0' AND permission = 'iot:ibms-channel:delete');

INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, updater, deleted)
SELECT 'IBMS通道导出', 'iot:ibms-channel:export', 3, 5,
       (SELECT id FROM system_menu WHERE deleted = b'0' AND path = 'ibms-channel' ORDER BY id LIMIT 1),
       '', '', NULL, NULL, 0, b'1', b'1', b'1', '1', '1', b'0'
WHERE NOT EXISTS (SELECT 1 FROM system_menu WHERE deleted = b'0' AND permission = 'iot:ibms-channel:export');

-- 2) 授予超级管理员（系统租户 0）
INSERT INTO system_role_menu (role_id, menu_id, tenant_id, creator, updater, deleted)
SELECT 1, m.id, 0, '1', '1', b'0'
FROM system_menu m
WHERE m.deleted = b'0' AND m.permission LIKE 'iot:ibms-channel:%'
  AND NOT EXISTS (
    SELECT 1 FROM system_role_menu x
    WHERE x.deleted = b'0' AND x.role_id = 1 AND x.menu_id = m.id AND x.tenant_id = 0
  );

-- 3) 授予「已拥有视频预览/回放查询菜单」的角色（同租户）
INSERT INTO system_role_menu (role_id, menu_id, tenant_id, creator, updater, deleted)
SELECT DISTINCT sr.id, m.id, sr.tenant_id, '1', '1', b'0'
FROM system_role sr
INNER JOIN system_role_menu srm ON srm.role_id = sr.id AND srm.deleted = b'0'
INNER JOIN system_menu vm ON vm.id = srm.menu_id AND vm.deleted = b'0'
  AND vm.permission IN ('iot:video:preview:query', 'iot:video:playback:query')
INNER JOIN system_menu m ON m.deleted = b'0' AND m.permission LIKE 'iot:ibms-channel:%'
WHERE NOT EXISTS (
  SELECT 1 FROM system_role_menu x
  WHERE x.deleted = b'0' AND x.role_id = sr.id AND x.menu_id = m.id AND x.tenant_id = sr.tenant_id
);
