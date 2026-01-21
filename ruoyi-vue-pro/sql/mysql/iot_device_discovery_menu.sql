-- ========================================
-- IoT 设备发现菜单 SQL
-- ========================================
-- 说明：将设备发现页面添加到系统菜单，位于"设备接入"模块下
-- ========================================

-- 注意：执行前请先确认 parent_id
-- 通过以下SQL查询"设备接入"菜单的ID：
-- SELECT id, name, path FROM system_menu WHERE name = '设备接入' OR component LIKE '%device%' ORDER BY id;

-- ========================================
-- 1. 添加"设备发现"菜单（二级菜单）
-- ========================================
-- parent_id 需要根据实际情况调整（通常是"设备接入"或"智慧物联"菜单的ID）
-- 假设 parent_id = 2738（请根据实际情况修改）

INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status,
    visible, keep_alive, always_show, creator,
    create_time, updater, update_time, deleted
) VALUES (
    '设备发现', '', 2, 10, 2738,
    'discovery', 'ep:search', 'iot/device/discovery/index', 'IotDeviceDiscovery', 0,
    b'1', b'1', b'1', '1',
    NOW(), '1', NOW(), b'0'
);

-- ========================================
-- 2. 获取刚插入的菜单ID（用于添加按钮权限）
-- ========================================
SET @discovery_menu_id = LAST_INSERT_ID();

-- ========================================
-- 3. 添加"设备发现"相关的按钮权限（三级菜单）
-- ========================================

-- 3.1 查看权限
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status,
    visible, keep_alive, always_show, creator,
    create_time, updater, update_time, deleted
) VALUES (
    '设备发现查询', 'iot:device:discovery:query', 3, 1, @discovery_menu_id,
    '', '', '', '', 0,
    b'1', b'1', b'1', '1',
    NOW(), '1', NOW(), b'0'
);

-- 3.2 手动扫描权限
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status,
    visible, keep_alive, always_show, creator,
    create_time, updater, update_time, deleted
) VALUES (
    '设备发现扫描', 'iot:device:discovery:scan', 3, 2, @discovery_menu_id,
    '', '', '', '', 0,
    b'1', b'1', b'1', '1',
    NOW(), '1', NOW(), b'0'
);

-- 3.3 忽略设备权限
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status,
    visible, keep_alive, always_show, creator,
    create_time, updater, update_time, deleted
) VALUES (
    '设备发现忽略', 'iot:device:discovery:ignore', 3, 3, @discovery_menu_id,
    '', '', '', '', 0,
    b'1', b'1', b'1', '1',
    NOW(), '1', NOW(), b'0'
);

-- 3.4 激活设备权限（跳转到设备管理添加）
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status,
    visible, keep_alive, always_show, creator,
    create_time, updater, update_time, deleted
) VALUES (
    '设备发现激活', 'iot:device:discovery:activate', 3, 4, @discovery_menu_id,
    '', '', '', '', 0,
    b'1', b'1', b'1', '1',
    NOW(), '1', NOW(), b'0'
);

-- ========================================
-- 4. 查询验证
-- ========================================
-- 执行以下SQL验证菜单是否插入成功：
-- SELECT id, name, component, path, parent_id, type, sort 
-- FROM system_menu 
-- WHERE name = '设备发现' OR parent_id = @discovery_menu_id
-- ORDER BY type, sort;

-- ========================================
-- 5. 权限分配（可选）
-- ========================================
-- 如果需要将这些权限分配给管理员角色（role_id = 1）：
/*
INSERT INTO system_role_menu (role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id)
SELECT 1, id, '1', NOW(), '1', NOW(), b'0', 1
FROM system_menu
WHERE name IN ('设备发现', '设备发现查询', '设备发现扫描', '设备发现忽略', '设备发现激活')
  AND deleted = b'0';
*/

-- ========================================
-- 注意事项
-- ========================================
-- 1. parent_id 必须根据实际的"设备接入"或相关父菜单ID进行调整
-- 2. 菜单排序 sort 可能需要调整以确保显示顺序正确
-- 3. icon 可以根据需要修改（当前使用 'ep:search'）
-- 4. 执行前建议先备份 system_menu 表
-- 5. 执行后需要清除前端路由缓存或重新登录

-- ========================================
-- 查询已有IoT菜单的参考SQL
-- ========================================
-- SELECT id, name, component, path, parent_id, type, sort, icon
-- FROM system_menu
-- WHERE component LIKE 'iot/%'
--    OR name LIKE '%物联%'
--    OR name LIKE '%设备%'
-- ORDER BY parent_id, sort;














