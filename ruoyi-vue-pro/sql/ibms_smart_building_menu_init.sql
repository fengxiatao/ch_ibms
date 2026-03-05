-- =====================================================
-- 智慧建筑模块菜单初始化脚本
-- 更新日期: 2026-02-02
-- 说明: 此脚本可重复执行，使用 ON DUPLICATE KEY UPDATE 避免重复插入
-- 
-- 菜单结构:
--   智慧建筑 (5191)
--   ├── 建筑设备监控 (5194) - 目录
--   │   ├── 设备监控 (5204) - iot/building/bac/monitor/index
--   │   ├── 设备台账 (5205) - iot/building/bac/ledger/index
--   │   ├── 告警信息 (5206) - iot/building/bac/alarm/index
--   │   └── 系统日志 (7131) - iot/building/bac/log/index
--   ├── 智能照明 (5192) - 目录
--   │   ├── 照明控制 (5199) - iot/building/lighting/control/index
--   │   ├── 场景控制 (5200) - iot/building/lighting/scene/index
--   │   ├── 设备管理 (5201) - iot/building/lighting/device/index
--   │   ├── 操作日志 (7132) - iot/building/lighting/log/index
--   │   └── 告警信息 (7133) - iot/building/lighting/alarm/index
--   └── 环境监测 (5193) - 目录
--       ├── 环境传感器 (5202) - iot/building/env/sensor/index
--       ├── 告警管理 (5203) - iot/building/env/alarm/index
--       └── 系统设置 (7134) - iot/building/env/settings/index
--
-- 字段说明:
--   type: 1=目录, 2=菜单, 3=按钮
--   status: 0=正常, 1=停用
--   visible: 1=显示, 0=隐藏
--   keep_alive: 1=缓存, 0=不缓存
--   always_show: 1=总是显示, 0=自动
-- =====================================================

-- =====================================================
-- 一、一级菜单: 智慧建筑
-- =====================================================
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, keep_alive, visible, always_show, creator, create_time, updater, update_time, deleted)
VALUES (5191, '智慧建筑', 'building:view', 1, 55, 0, '/building', 'ep:office-building', 'LAYOUT', 'Building', 0, 1, 1, 1, '1', NOW(), '1', NOW(), 0)
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    permission = VALUES(permission),
    type = VALUES(type),
    sort = VALUES(sort),
    path = VALUES(path),
    icon = VALUES(icon),
    component = VALUES(component),
    component_name = VALUES(component_name),
    status = VALUES(status),
    visible = 1,
    always_show = VALUES(always_show),
    updater = '1',
    update_time = NOW();

-- =====================================================
-- 二、二级菜单: 建筑设备监控、智能照明、环境监测 (目录类型)
-- =====================================================

-- 建筑设备监控 (目录)
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, keep_alive, visible, always_show, creator, create_time, updater, update_time, deleted)
VALUES (5194, '建筑设备监控', 'building:cold-source', 1, 1, 5191, 'bac', 'ep:ice-tea', '', 'ColdSourceGroupControl', 0, 1, 1, 1, '1', NOW(), '1', NOW(), 0)
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    type = 1,
    sort = VALUES(sort),
    path = VALUES(path),
    icon = VALUES(icon),
    component = '',
    status = 0,
    visible = 1,
    always_show = VALUES(always_show),
    updater = '1',
    update_time = NOW();

-- 智能照明 (目录)
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, keep_alive, visible, always_show, creator, create_time, updater, update_time, deleted)
VALUES (5192, '智能照明', 'building:lighting', 1, 2, 5191, 'lighting', 'ep:sunny', '', 'IntelligentLighting', 0, 1, 1, 1, '1', NOW(), '1', NOW(), 0)
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    type = 1,
    sort = VALUES(sort),
    path = VALUES(path),
    icon = VALUES(icon),
    component = '',
    status = 0,
    visible = 1,
    always_show = VALUES(always_show),
    updater = '1',
    update_time = NOW();

-- 环境监测 (目录)
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, keep_alive, visible, always_show, creator, create_time, updater, update_time, deleted)
VALUES (5193, '环境监测', 'building:environment', 1, 3, 5191, 'env', 'ep:orange', '', 'EnvironmentMonitoring', 0, 1, 1, 1, '1', NOW(), '1', NOW(), 0)
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    type = 1,
    sort = VALUES(sort),
    path = VALUES(path),
    icon = VALUES(icon),
    component = '',
    status = 0,
    visible = 1,
    always_show = VALUES(always_show),
    updater = '1',
    update_time = NOW();

-- =====================================================
-- 三、三级菜单: 建筑设备监控 子菜单
-- =====================================================

-- 设备监控
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, keep_alive, visible, always_show, creator, create_time, updater, update_time, deleted)
VALUES (5204, '设备监控', 'building:cold-source:topology', 2, 1, 5194, 'monitor', 'ep:share', 'iot/building/bac/monitor/index', 'BacMonitor', 0, 1, 1, 1, '1', NOW(), '1', NOW(), 0)
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    permission = VALUES(permission),
    type = 2,
    sort = VALUES(sort),
    path = VALUES(path),
    icon = VALUES(icon),
    component = VALUES(component),
    component_name = VALUES(component_name),
    status = 0,
    visible = 1,
    updater = '1',
    update_time = NOW();

-- 设备台账
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, keep_alive, visible, always_show, creator, create_time, updater, update_time, deleted)
VALUES (5205, '设备台账', 'building:cold-source:devices', 2, 2, 5194, 'ledger', 'ep:cpu', 'iot/building/bac/ledger/index', 'BacLedger', 0, 1, 1, 1, '1', NOW(), '1', NOW(), 0)
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    permission = VALUES(permission),
    type = 2,
    sort = VALUES(sort),
    path = VALUES(path),
    icon = VALUES(icon),
    component = VALUES(component),
    component_name = VALUES(component_name),
    status = 0,
    visible = 1,
    updater = '1',
    update_time = NOW();

-- 告警信息
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, keep_alive, visible, always_show, creator, create_time, updater, update_time, deleted)
VALUES (5206, '告警信息', 'building:cold-source:alarm', 2, 3, 5194, 'alarm', 'ep:warning-filled', 'iot/building/bac/alarm/index', 'BacAlarm', 0, 1, 1, 1, '1', NOW(), '1', NOW(), 0)
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    permission = VALUES(permission),
    type = 2,
    sort = VALUES(sort),
    path = VALUES(path),
    icon = VALUES(icon),
    component = VALUES(component),
    component_name = VALUES(component_name),
    status = 0,
    visible = 1,
    updater = '1',
    update_time = NOW();

-- 系统日志
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, keep_alive, visible, always_show, creator, create_time, updater, update_time, deleted)
VALUES (7131, '系统日志', 'iot:building-bac:query', 2, 4, 5194, 'log', 'ep:document', 'iot/building/bac/log/index', 'BacLog', 0, 1, 1, 1, '1', NOW(), '1', NOW(), 0)
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    permission = VALUES(permission),
    type = 2,
    sort = VALUES(sort),
    path = VALUES(path),
    icon = VALUES(icon),
    component = VALUES(component),
    component_name = VALUES(component_name),
    status = 0,
    visible = 1,
    updater = '1',
    update_time = NOW();

-- =====================================================
-- 四、三级菜单: 智能照明 子菜单
-- =====================================================

-- 照明控制
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, keep_alive, visible, always_show, creator, create_time, updater, update_time, deleted)
VALUES (5199, '照明控制', 'building:lighting:circuit', 2, 1, 5192, 'control', 'ep:switch-button', 'iot/building/lighting/control/index', 'LightingControl', 0, 1, 1, 1, '1', NOW(), '1', NOW(), 0)
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    permission = VALUES(permission),
    type = 2,
    sort = VALUES(sort),
    path = VALUES(path),
    icon = VALUES(icon),
    component = VALUES(component),
    component_name = VALUES(component_name),
    status = 0,
    visible = 1,
    updater = '1',
    update_time = NOW();

-- 场景控制
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, keep_alive, visible, always_show, creator, create_time, updater, update_time, deleted)
VALUES (5200, '场景控制', 'building:lighting:scene', 2, 2, 5192, 'scene', 'ep:picture', 'iot/building/lighting/scene/index', 'LightingScene', 0, 1, 1, 1, '1', NOW(), '1', NOW(), 0)
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    permission = VALUES(permission),
    type = 2,
    sort = VALUES(sort),
    path = VALUES(path),
    icon = VALUES(icon),
    component = VALUES(component),
    component_name = VALUES(component_name),
    status = 0,
    visible = 1,
    updater = '1',
    update_time = NOW();

-- 设备管理
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, keep_alive, visible, always_show, creator, create_time, updater, update_time, deleted)
VALUES (5201, '设备管理', 'building:lighting:alarm', 2, 3, 5192, 'device', 'ep:warning', 'iot/building/lighting/device/index', 'LightingDevice', 0, 1, 1, 1, '1', NOW(), '1', NOW(), 0)
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    permission = VALUES(permission),
    type = 2,
    sort = VALUES(sort),
    path = VALUES(path),
    icon = VALUES(icon),
    component = VALUES(component),
    component_name = VALUES(component_name),
    status = 0,
    visible = 1,
    updater = '1',
    update_time = NOW();

-- 操作日志
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, keep_alive, visible, always_show, creator, create_time, updater, update_time, deleted)
VALUES (7132, '操作日志', 'iot:building-lighting:query', 2, 4, 5192, 'log', 'ep:document', 'iot/building/lighting/log/index', 'LightingLog', 0, 1, 1, 1, '1', NOW(), '1', NOW(), 0)
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    permission = VALUES(permission),
    type = 2,
    sort = VALUES(sort),
    path = VALUES(path),
    icon = VALUES(icon),
    component = VALUES(component),
    component_name = VALUES(component_name),
    status = 0,
    visible = 1,
    updater = '1',
    update_time = NOW();

-- 告警信息
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, keep_alive, visible, always_show, creator, create_time, updater, update_time, deleted)
VALUES (7133, '告警信息', 'iot:building-lighting:query', 2, 5, 5192, 'alarm', 'ep:warning', 'iot/building/lighting/alarm/index', 'LightingAlarm', 0, 1, 1, 1, '1', NOW(), '1', NOW(), 0)
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    permission = VALUES(permission),
    type = 2,
    sort = VALUES(sort),
    path = VALUES(path),
    icon = VALUES(icon),
    component = VALUES(component),
    component_name = VALUES(component_name),
    status = 0,
    visible = 1,
    updater = '1',
    update_time = NOW();

-- =====================================================
-- 五、三级菜单: 环境监测 子菜单
-- =====================================================

-- 环境传感器
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, keep_alive, visible, always_show, creator, create_time, updater, update_time, deleted)
VALUES (5202, '环境传感器', 'building:environment:sensors', 2, 1, 5193, 'sensor', 'ep:aim', 'iot/building/env/sensor/index', 'EnvSensor', 0, 1, 1, 1, '1', NOW(), '1', NOW(), 0)
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    permission = VALUES(permission),
    type = 2,
    sort = VALUES(sort),
    path = VALUES(path),
    icon = VALUES(icon),
    component = VALUES(component),
    component_name = VALUES(component_name),
    status = 0,
    visible = 1,
    updater = '1',
    update_time = NOW();

-- 告警管理
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, keep_alive, visible, always_show, creator, create_time, updater, update_time, deleted)
VALUES (5203, '告警管理', 'building:environment:alarm', 2, 2, 5193, 'alarm', 'ep:bell', 'iot/building/env/alarm/index', 'EnvAlarm', 0, 1, 1, 1, '1', NOW(), '1', NOW(), 0)
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    permission = VALUES(permission),
    type = 2,
    sort = VALUES(sort),
    path = VALUES(path),
    icon = VALUES(icon),
    component = VALUES(component),
    component_name = VALUES(component_name),
    status = 0,
    visible = 1,
    updater = '1',
    update_time = NOW();

-- 系统设置
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, keep_alive, visible, always_show, creator, create_time, updater, update_time, deleted)
VALUES (7134, '系统设置', 'iot:building-env:query', 2, 3, 5193, 'settings', 'ep:setting', 'iot/building/env/settings/index', 'EnvSettings', 0, 1, 1, 1, '1', NOW(), '1', NOW(), 0)
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    permission = VALUES(permission),
    type = 2,
    sort = VALUES(sort),
    path = VALUES(path),
    icon = VALUES(icon),
    component = VALUES(component),
    component_name = VALUES(component_name),
    status = 0,
    visible = 1,
    updater = '1',
    update_time = NOW();

-- =====================================================
-- 执行完成提示
-- =====================================================
SELECT '智慧建筑模块菜单初始化完成' AS message;
SELECT id, name, parent_id, path, component, status, visible 
FROM system_menu 
WHERE id IN (5191, 5192, 5193, 5194, 5199, 5200, 5201, 5202, 5203, 5204, 5205, 5206, 7131, 7132, 7133, 7134)
ORDER BY parent_id, sort;
