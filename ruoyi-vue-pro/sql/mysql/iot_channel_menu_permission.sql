-- =====================================================
-- IoT 设备通道管理 - 菜单和权限配置
-- =====================================================

-- 1. 查询 IoT 模块的父菜单ID
SET @iot_parent_id = (SELECT id FROM system_menu WHERE name = 'IoT' AND type = 1 LIMIT 1);

-- 如果 IoT 模块不存在，先创建
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT 'IoT', '', 1, 100, 0, '/iot', 'ep:cpu', NULL, NULL, 0, TRUE, TRUE, TRUE, '1', NOW(), '1', NOW(), FALSE
WHERE NOT EXISTS (SELECT 1 FROM system_menu WHERE name = 'IoT' AND type = 1);

-- 重新获取 IoT 模块ID
SET @iot_parent_id = (SELECT id FROM system_menu WHERE name = 'IoT' AND type = 1 LIMIT 1);

-- 2. 创建通道管理菜单（一级菜单）
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
VALUES ('通道管理', '', 2, 30, @iot_parent_id, 'channel', 'ep:video-camera', 'iot/channel/index', 'IotChannel', 0, TRUE, TRUE, FALSE, '1', NOW(), '1', NOW(), FALSE);

-- 获取通道管理菜单ID
SET @channel_menu_id = LAST_INSERT_ID();

-- 3. 创建通道管理的按钮权限
-- 3.1 查询权限
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
VALUES ('查询通道', 'iot:channel:query', 3, 1, @channel_menu_id, '', '', '', NULL, 0, TRUE, TRUE, FALSE, '1', NOW(), '1', NOW(), FALSE);

-- 3.2 创建权限
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
VALUES ('创建通道', 'iot:channel:create', 3, 2, @channel_menu_id, '', '', '', NULL, 0, TRUE, TRUE, FALSE, '1', NOW(), '1', NOW(), FALSE);

-- 3.3 更新权限
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
VALUES ('更新通道', 'iot:channel:update', 3, 3, @channel_menu_id, '', '', '', NULL, 0, TRUE, TRUE, FALSE, '1', NOW(), '1', NOW(), FALSE);

-- 3.4 删除权限
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
VALUES ('删除通道', 'iot:channel:delete', 3, 4, @channel_menu_id, '', '', '', NULL, 0, TRUE, TRUE, FALSE, '1', NOW(), '1', NOW(), FALSE);

-- 3.5 导出权限
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
VALUES ('导出通道', 'iot:channel:export', 3, 5, @channel_menu_id, '', '', '', NULL, 0, TRUE, TRUE, FALSE, '1', NOW(), '1', NOW(), FALSE);

-- 3.6 同步权限
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
VALUES ('同步通道', 'iot:channel:sync', 3, 6, @channel_menu_id, '', '', '', NULL, 0, TRUE, TRUE, FALSE, '1', NOW(), '1', NOW(), FALSE);

-- 4. 为超级管理员角色分配权限
-- 获取超级管理员角色ID（通常是1）
SET @admin_role_id = 1;

-- 分配菜单权限
INSERT INTO system_role_menu (role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id)
SELECT @admin_role_id, id, '1', NOW(), '1', NOW(), FALSE, 1
FROM system_menu
WHERE (id = @channel_menu_id OR parent_id = @channel_menu_id)
  AND NOT EXISTS (
    SELECT 1 FROM system_role_menu 
    WHERE role_id = @admin_role_id AND menu_id = system_menu.id
  );

-- 5. 创建通道类型字典
-- 5.1 创建字典类型
INSERT INTO system_dict_type (name, type, status, remark, creator, create_time, updater, update_time, deleted, deleted_time)
VALUES ('IoT 通道类型', 'iot_channel_type', 0, 'IoT 设备通道类型', '1', NOW(), '1', NOW(), FALSE, NULL)
ON DUPLICATE KEY UPDATE name = name;

-- 获取字典类型ID
SET @dict_type_id = (SELECT id FROM system_dict_type WHERE type = 'iot_channel_type' LIMIT 1);

-- 5.2 创建字典数据
INSERT INTO system_dict_data (sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted)
VALUES 
(1, '视频', 'VIDEO', 'iot_channel_type', 0, 'primary', '', '视频通道', '1', NOW(), '1', NOW(), FALSE),
(2, '门禁', 'ACCESS', 'iot_channel_type', 0, 'success', '', '门禁通道', '1', NOW(), '1', NOW(), FALSE),
(3, '消防', 'FIRE', 'iot_channel_type', 0, 'danger', '', '消防通道', '1', NOW(), '1', NOW(), FALSE),
(4, '能源', 'ENERGY', 'iot_channel_type', 0, 'warning', '', '能源通道', '1', NOW(), '1', NOW(), FALSE),
(5, '广播', 'BROADCAST', 'iot_channel_type', 0, 'info', '', '广播通道', '1', NOW(), '1', NOW(), FALSE)
ON DUPLICATE KEY UPDATE label = VALUES(label);

-- 6. 查询结果验证
SELECT '=== 通道管理菜单创建完成 ===' AS message;
SELECT 
    m.id AS menu_id,
    m.name AS menu_name,
    m.permission,
    m.type AS menu_type,
    m.path,
    m.component,
    CASE 
        WHEN m.type = 1 THEN '目录'
        WHEN m.type = 2 THEN '菜单'
        WHEN m.type = 3 THEN '按钮'
    END AS type_name
FROM system_menu m
WHERE m.id = @channel_menu_id OR m.parent_id = @channel_menu_id
ORDER BY m.type, m.sort;

SELECT '=== 通道类型字典创建完成 ===' AS message;
SELECT 
    d.label,
    d.value,
    d.dict_type,
    d.color_type
FROM system_dict_data d
WHERE d.dict_type = 'iot_channel_type'
ORDER BY d.sort;

-- =====================================================
-- 使用说明
-- =====================================================
-- 
-- 1. 执行此SQL后，系统会自动创建：
--    - 通道管理菜单（在 IoT 模块下）
--    - 6个按钮权限（查询、创建、更新、删除、导出、同步）
--    - 通道类型字典（5种类型）
--    - 超级管理员的权限分配
--
-- 2. 前端路由会自动生成：
--    路径：/iot/channel
--    组件：iot/channel/index
--    名称：IotChannel
--
-- 3. 权限标识：
--    - iot:channel:query  - 查询通道
--    - iot:channel:create - 创建通道
--    - iot:channel:update - 更新通道
--    - iot:channel:delete - 删除通道
--    - iot:channel:export - 导出通道
--    - iot:channel:sync   - 同步通道
--
-- 4. 字典类型：
--    - iot_channel_type - 通道类型字典
--
-- =====================================================
