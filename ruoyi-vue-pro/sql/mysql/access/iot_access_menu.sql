-- =============================================
-- 智慧通行（门禁）模块菜单配置
-- 基于大华设备SDK的门禁管理系统
-- =============================================

-- 获取最大菜单ID
SET @max_menu_id = (SELECT IFNULL(MAX(id), 0) FROM system_menu);

-- =============================================
-- 一级菜单：智慧通行（放在IoT物联网下面，parent_id=4000）
-- =============================================
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
VALUES (@max_menu_id + 1, '智慧通行', '', 1, 20, 4000, 'smart-access', 'ep:key', NULL, NULL, 0, 1, 1, 1, '1', NOW(), '1', NOW(), 0);

SET @access_parent_id = @max_menu_id + 1;

-- =============================================
-- 二级菜单：人事管理
-- =============================================
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
VALUES (@max_menu_id + 2, '人事管理', '', 1, 1, @access_parent_id, 'personnel', 'ep:user', NULL, NULL, 0, 1, 1, 1, '1', NOW(), '1', NOW(), 0);

SET @personnel_parent_id = @max_menu_id + 2;

-- 组织架构
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
VALUES (@max_menu_id + 3, '组织架构', 'iot:access-department:query', 2, 1, @personnel_parent_id, 'department', 'ep:office-building', 'iot/access/department/index', 'AccessDepartment', 0, 1, 1, 1, '1', NOW(), '1', NOW(), 0);

-- 组织架构按钮权限
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
VALUES (@max_menu_id + 4, '部门新增', 'iot:access-department:create', 3, 1, @max_menu_id + 3, '', '', '', NULL, 0, 1, 1, 1, '1', NOW(), '1', NOW(), 0);
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
VALUES (@max_menu_id + 5, '部门修改', 'iot:access-department:update', 3, 2, @max_menu_id + 3, '', '', '', NULL, 0, 1, 1, 1, '1', NOW(), '1', NOW(), 0);
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
VALUES (@max_menu_id + 6, '部门删除', 'iot:access-department:delete', 3, 3, @max_menu_id + 3, '', '', '', NULL, 0, 1, 1, 1, '1', NOW(), '1', NOW(), 0);

-- 人员管理
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
VALUES (@max_menu_id + 7, '人员管理', 'iot:access-person:query', 2, 2, @personnel_parent_id, 'person', 'ep:avatar', 'iot/access/person/index', 'AccessPerson', 0, 1, 1, 1, '1', NOW(), '1', NOW(), 0);

-- 人员管理按钮权限
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
VALUES (@max_menu_id + 8, '人员新增', 'iot:access-person:create', 3, 1, @max_menu_id + 7, '', '', '', NULL, 0, 1, 1, 1, '1', NOW(), '1', NOW(), 0);
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
VALUES (@max_menu_id + 9, '人员修改', 'iot:access-person:update', 3, 2, @max_menu_id + 7, '', '', '', NULL, 0, 1, 1, 1, '1', NOW(), '1', NOW(), 0);
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
VALUES (@max_menu_id + 10, '人员删除', 'iot:access-person:delete', 3, 3, @max_menu_id + 7, '', '', '', NULL, 0, 1, 1, 1, '1', NOW(), '1', NOW(), 0);
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
VALUES (@max_menu_id + 11, '人员导入', 'iot:access-person:import', 3, 4, @max_menu_id + 7, '', '', '', NULL, 0, 1, 1, 1, '1', NOW(), '1', NOW(), 0);


-- =============================================
-- 二级菜单：权限配置
-- =============================================
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
VALUES (@max_menu_id + 12, '权限配置', '', 1, 2, @access_parent_id, 'permission', 'ep:lock', NULL, NULL, 0, 1, 1, 1, '1', NOW(), '1', NOW(), 0);

SET @permission_parent_id = @max_menu_id + 12;

-- 权限组管理
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
VALUES (@max_menu_id + 13, '权限组管理', 'iot:access-permission-group:query', 2, 1, @permission_parent_id, 'group', 'ep:collection', 'iot/access/permission-group/index', 'AccessPermissionGroup', 0, 1, 1, 1, '1', NOW(), '1', NOW(), 0);

-- 权限组按钮权限
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
VALUES (@max_menu_id + 14, '权限组新增', 'iot:access-permission-group:create', 3, 1, @max_menu_id + 13, '', '', '', NULL, 0, 1, 1, 1, '1', NOW(), '1', NOW(), 0);
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
VALUES (@max_menu_id + 15, '权限组修改', 'iot:access-permission-group:update', 3, 2, @max_menu_id + 13, '', '', '', NULL, 0, 1, 1, 1, '1', NOW(), '1', NOW(), 0);
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
VALUES (@max_menu_id + 16, '权限组删除', 'iot:access-permission-group:delete', 3, 3, @max_menu_id + 13, '', '', '', NULL, 0, 1, 1, 1, '1', NOW(), '1', NOW(), 0);

-- 授权进度
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
VALUES (@max_menu_id + 17, '授权进度', 'iot:access-auth-task:query', 2, 2, @permission_parent_id, 'auth-task', 'ep:loading', 'iot/access/auth-task/index', 'AccessAuthTask', 0, 1, 1, 1, '1', NOW(), '1', NOW(), 0);

-- 授权进度按钮权限
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
VALUES (@max_menu_id + 18, '重新下发', 'iot:access-auth-task:retry', 3, 1, @max_menu_id + 17, '', '', '', NULL, 0, 1, 1, 1, '1', NOW(), '1', NOW(), 0);

-- =============================================
-- 二级菜单：设备管理
-- =============================================
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
VALUES (@max_menu_id + 19, '设备管理', 'iot:access-device:query', 2, 3, @access_parent_id, 'device', 'ep:monitor', 'iot/access/device/index', 'AccessDevice', 0, 1, 1, 1, '1', NOW(), '1', NOW(), 0);

-- 设备管理按钮权限
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
VALUES (@max_menu_id + 20, '设备激活', 'iot:access-device:activate', 3, 1, @max_menu_id + 19, '', '', '', NULL, 0, 1, 1, 1, '1', NOW(), '1', NOW(), 0);
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
VALUES (@max_menu_id + 21, '设备停用', 'iot:access-device:deactivate', 3, 2, @max_menu_id + 19, '', '', '', NULL, 0, 1, 1, 1, '1', NOW(), '1', NOW(), 0);

-- =============================================
-- 二级菜单：通道管理
-- =============================================
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
VALUES (@max_menu_id + 22, '通道管理', 'iot:access-channel:query', 2, 4, @access_parent_id, 'channel', 'ep:open', 'iot/access/channel/index', 'AccessChannel', 0, 1, 1, 1, '1', NOW(), '1', NOW(), 0);

-- 通道管理按钮权限
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
VALUES (@max_menu_id + 23, '远程开门', 'iot:access-channel:open-door', 3, 1, @max_menu_id + 22, '', '', '', NULL, 0, 1, 1, 1, '1', NOW(), '1', NOW(), 0);
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
VALUES (@max_menu_id + 24, '远程关门', 'iot:access-channel:close-door', 3, 2, @max_menu_id + 22, '', '', '', NULL, 0, 1, 1, 1, '1', NOW(), '1', NOW(), 0);
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
VALUES (@max_menu_id + 25, '设置常开', 'iot:access-channel:always-open', 3, 3, @max_menu_id + 22, '', '', '', NULL, 0, 1, 1, 1, '1', NOW(), '1', NOW(), 0);
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
VALUES (@max_menu_id + 26, '设置常闭', 'iot:access-channel:always-closed', 3, 4, @max_menu_id + 22, '', '', '', NULL, 0, 1, 1, 1, '1', NOW(), '1', NOW(), 0);


-- =============================================
-- 二级菜单：事件监控
-- =============================================
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
VALUES (@max_menu_id + 27, '事件监控', 'iot:access-event:query', 2, 5, @access_parent_id, 'event', 'ep:bell', 'iot/access/event/index', 'AccessEvent', 0, 1, 1, 1, '1', NOW(), '1', NOW(), 0);

-- =============================================
-- 二级菜单：操作日志
-- =============================================
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
VALUES (@max_menu_id + 28, '操作日志', 'iot:access-operation-log:query', 2, 6, @access_parent_id, 'operation-log', 'ep:document', 'iot/access/operation-log/index', 'AccessOperationLog', 0, 1, 1, 1, '1', NOW(), '1', NOW(), 0);

-- =============================================
-- 二级菜单：测试工具
-- =============================================
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
VALUES (@max_menu_id + 29, '测试工具', 'iot:access-test:query', 2, 99, @access_parent_id, 'test', 'ep:tools', 'iot/access/test/index', 'AccessTest', 0, 1, 1, 1, '1', NOW(), '1', NOW(), 0);

-- =============================================
-- 为超级管理员角色分配权限
-- =============================================
INSERT INTO system_role_menu (role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id)
SELECT 1, id, '1', NOW(), '1', NOW(), 0, 1 FROM system_menu WHERE id BETWEEN @max_menu_id + 1 AND @max_menu_id + 29;

-- =============================================
-- 输出菜单ID范围，便于后续维护
-- =============================================
SELECT CONCAT('智慧通行菜单ID范围: ', @max_menu_id + 1, ' - ', @max_menu_id + 29) AS menu_info;
