-- =====================================================
-- 智慧楼宇模块 - 菜单及权限配置
-- 执行前请确认菜单ID不冲突
-- =====================================================

-- 获取 IOT 模块的父菜单ID（假设IOT模块的菜单ID为5000或附近）
-- 如果不存在，需要先创建智慧楼宇一级目录

-- =====================================================
-- 1. 创建智慧楼宇一级目录菜单
-- =====================================================
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) 
VALUES (8000, '智慧楼宇', '', 1, 100, 0, '/building', 'ep:office-building', '', '', 0, 1, 1, 1, '1', NOW(), '1', NOW(), 0)
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- =====================================================
-- 2. 环境监测模块菜单
-- =====================================================
-- 2.1 环境监测目录
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) 
VALUES (8100, '环境监测', '', 1, 1, 8000, 'env', 'ep:monitor', '', '', 0, 1, 1, 1, '1', NOW(), '1', NOW(), 0)
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- 2.2 环境传感器页面
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) 
VALUES (8101, '环境监控', 'iot:building-env:query', 2, 1, 8100, 'index', 'ep:data-line', 'iot/building/env/index', 'BuildingEnvMonitor', 0, 1, 1, 1, '1', NOW(), '1', NOW(), 0)
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- 2.3 环境传感器按钮权限
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES 
(8102, '传感器新增', 'iot:building-env:create', 3, 1, 8101, '', '', '', '', 0, 1, 1, 1, '1', NOW(), '1', NOW(), 0),
(8103, '传感器编辑', 'iot:building-env:update', 3, 2, 8101, '', '', '', '', 0, 1, 1, 1, '1', NOW(), '1', NOW(), 0),
(8104, '传感器删除', 'iot:building-env:delete', 3, 3, 8101, '', '', '', '', 0, 1, 1, 1, '1', NOW(), '1', NOW(), 0)
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- 2.4 环境告警页面
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) 
VALUES (8110, '环境告警', 'iot:building-env:query', 2, 2, 8100, 'alarm', 'ep:warning', 'iot/building/env/alarm', 'BuildingEnvAlarm', 0, 1, 1, 1, '1', NOW(), '1', NOW(), 0)
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- =====================================================
-- 3. 智能照明模块菜单
-- =====================================================
-- 3.1 智能照明目录
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) 
VALUES (8200, '智能照明', '', 1, 2, 8000, 'lighting', 'ep:sunrise', '', '', 0, 1, 1, 1, '1', NOW(), '1', NOW(), 0)
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- 3.2 照明控制页面
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) 
VALUES (8201, '照明控制', 'iot:building-lighting:query', 2, 1, 8200, 'index', 'ep:sunny', 'iot/building/lighting/index', 'BuildingLighting', 0, 1, 1, 1, '1', NOW(), '1', NOW(), 0)
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- 3.3 照明控制按钮权限
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES 
(8202, '照明开关', 'iot:building-lighting:control', 3, 1, 8201, '', '', '', '', 0, 1, 1, 1, '1', NOW(), '1', NOW(), 0),
(8203, '亮度调节', 'iot:building-lighting:control', 3, 2, 8201, '', '', '', '', 0, 1, 1, 1, '1', NOW(), '1', NOW(), 0),
(8204, '场景执行', 'iot:building-lighting:control', 3, 3, 8201, '', '', '', '', 0, 1, 1, 1, '1', NOW(), '1', NOW(), 0)
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- 3.4 场景管理页面
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) 
VALUES (8210, '场景管理', 'iot:building-lighting:query', 2, 2, 8200, 'scene', 'ep:magic-stick', 'iot/building/lighting/scene', 'BuildingLightingScene', 0, 1, 1, 1, '1', NOW(), '1', NOW(), 0)
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- 3.5 场景管理按钮权限
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES 
(8211, '场景新增', 'iot:building-lighting:create', 3, 1, 8210, '', '', '', '', 0, 1, 1, 1, '1', NOW(), '1', NOW(), 0),
(8212, '场景编辑', 'iot:building-lighting:update', 3, 2, 8210, '', '', '', '', 0, 1, 1, 1, '1', NOW(), '1', NOW(), 0),
(8213, '场景删除', 'iot:building-lighting:delete', 3, 3, 8210, '', '', '', '', 0, 1, 1, 1, '1', NOW(), '1', NOW(), 0)
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- 3.6 定时任务页面
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) 
VALUES (8220, '定时任务', 'iot:building-lighting:query', 2, 3, 8200, 'schedule', 'ep:clock', 'iot/building/lighting/schedule', 'BuildingLightingSchedule', 0, 1, 1, 1, '1', NOW(), '1', NOW(), 0)
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- 3.7 照明告警页面
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) 
VALUES (8230, '照明告警', 'iot:building-lighting:query', 2, 4, 8200, 'alarm', 'ep:warning', 'iot/building/lighting/alarm', 'BuildingLightingAlarm', 0, 1, 1, 1, '1', NOW(), '1', NOW(), 0)
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- =====================================================
-- 4. 楼宇自控模块菜单
-- =====================================================
-- 4.1 楼宇自控目录
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) 
VALUES (8300, '楼宇自控', '', 1, 3, 8000, 'bac', 'ep:setting', '', '', 0, 1, 1, 1, '1', NOW(), '1', NOW(), 0)
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- 4.2 楼宇自控首页
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) 
VALUES (8301, '设备监控', 'iot:building-bac:query', 2, 1, 8300, 'index', 'ep:cpu', 'iot/building/bac/index', 'BuildingBac', 0, 1, 1, 1, '1', NOW(), '1', NOW(), 0)
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- 4.3 楼宇自控按钮权限
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES 
(8302, '设备启停', 'iot:building-bac:control', 3, 1, 8301, '', '', '', '', 0, 1, 1, 1, '1', NOW(), '1', NOW(), 0),
(8303, '参数设置', 'iot:building-bac:update', 3, 2, 8301, '', '', '', '', 0, 1, 1, 1, '1', NOW(), '1', NOW(), 0)
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- 4.4 暖通设备页面
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) 
VALUES (8310, '暖通设备', 'iot:building-bac:query', 2, 2, 8300, 'hvac', 'ep:odometer', 'iot/building/bac/hvac', 'BuildingHvac', 0, 1, 1, 1, '1', NOW(), '1', NOW(), 0)
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- 4.5 给排水设备页面
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) 
VALUES (8320, '给排水设备', 'iot:building-bac:query', 2, 3, 8300, 'water', 'mdi:water', 'iot/building/bac/water', 'BuildingWater', 0, 1, 1, 1, '1', NOW(), '1', NOW(), 0)
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- 4.6 楼宇自控告警页面
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) 
VALUES (8330, '自控告警', 'iot:building-bac:query', 2, 4, 8300, 'alarm', 'ep:warning', 'iot/building/bac/alarm', 'BuildingBacAlarm', 0, 1, 1, 1, '1', NOW(), '1', NOW(), 0)
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- =====================================================
-- 5. 能耗计量模块菜单
-- =====================================================
-- 5.1 能耗计量目录
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) 
VALUES (8400, '能耗计量', '', 1, 4, 8000, 'energy', 'ep:histogram', '', '', 0, 1, 1, 1, '1', NOW(), '1', NOW(), 0)
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- 5.2 能耗总览页面
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) 
VALUES (8401, '能耗总览', 'iot:building-energy:query', 2, 1, 8400, 'index', 'ep:trend-charts', 'iot/building/energy/index', 'BuildingEnergy', 0, 1, 1, 1, '1', NOW(), '1', NOW(), 0)
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- 5.3 仪表管理页面
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) 
VALUES (8410, '仪表管理', 'iot:building-energy:query', 2, 2, 8400, 'meter', 'ep:meter', 'iot/building/energy/meter', 'BuildingEnergyMeter', 0, 1, 1, 1, '1', NOW(), '1', NOW(), 0)
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- 5.4 仪表管理按钮权限
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES 
(8411, '仪表新增', 'iot:building-energy:create', 3, 1, 8410, '', '', '', '', 0, 1, 1, 1, '1', NOW(), '1', NOW(), 0),
(8412, '仪表编辑', 'iot:building-energy:update', 3, 2, 8410, '', '', '', '', 0, 1, 1, 1, '1', NOW(), '1', NOW(), 0),
(8413, '仪表删除', 'iot:building-energy:delete', 3, 3, 8410, '', '', '', '', 0, 1, 1, 1, '1', NOW(), '1', NOW(), 0)
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- 5.5 能耗统计页面
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) 
VALUES (8420, '能耗统计', 'iot:building-energy:query', 2, 3, 8400, 'statistics', 'ep:data-analysis', 'iot/building/energy/statistics', 'BuildingEnergyStatistics', 0, 1, 1, 1, '1', NOW(), '1', NOW(), 0)
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- 5.6 能耗告警页面
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) 
VALUES (8430, '能耗告警', 'iot:building-energy:query', 2, 4, 8400, 'alarm', 'ep:warning', 'iot/building/energy/alarm', 'BuildingEnergyAlarm', 0, 1, 1, 1, '1', NOW(), '1', NOW(), 0)
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- =====================================================
-- 6. 将所有新菜单权限授予超级管理员角色(role_id=1)
-- =====================================================
INSERT IGNORE INTO system_role_menu (role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES 
-- 智慧楼宇一级目录
(1, 8000, '1', NOW(), '1', NOW(), 0, 1),
-- 环境监测
(1, 8100, '1', NOW(), '1', NOW(), 0, 1),
(1, 8101, '1', NOW(), '1', NOW(), 0, 1),
(1, 8102, '1', NOW(), '1', NOW(), 0, 1),
(1, 8103, '1', NOW(), '1', NOW(), 0, 1),
(1, 8104, '1', NOW(), '1', NOW(), 0, 1),
(1, 8110, '1', NOW(), '1', NOW(), 0, 1),
-- 智能照明
(1, 8200, '1', NOW(), '1', NOW(), 0, 1),
(1, 8201, '1', NOW(), '1', NOW(), 0, 1),
(1, 8202, '1', NOW(), '1', NOW(), 0, 1),
(1, 8203, '1', NOW(), '1', NOW(), 0, 1),
(1, 8204, '1', NOW(), '1', NOW(), 0, 1),
(1, 8210, '1', NOW(), '1', NOW(), 0, 1),
(1, 8211, '1', NOW(), '1', NOW(), 0, 1),
(1, 8212, '1', NOW(), '1', NOW(), 0, 1),
(1, 8213, '1', NOW(), '1', NOW(), 0, 1),
(1, 8220, '1', NOW(), '1', NOW(), 0, 1),
(1, 8230, '1', NOW(), '1', NOW(), 0, 1),
-- 楼宇自控
(1, 8300, '1', NOW(), '1', NOW(), 0, 1),
(1, 8301, '1', NOW(), '1', NOW(), 0, 1),
(1, 8302, '1', NOW(), '1', NOW(), 0, 1),
(1, 8303, '1', NOW(), '1', NOW(), 0, 1),
(1, 8310, '1', NOW(), '1', NOW(), 0, 1),
(1, 8320, '1', NOW(), '1', NOW(), 0, 1),
(1, 8330, '1', NOW(), '1', NOW(), 0, 1),
-- 能耗计量
(1, 8400, '1', NOW(), '1', NOW(), 0, 1),
(1, 8401, '1', NOW(), '1', NOW(), 0, 1),
(1, 8410, '1', NOW(), '1', NOW(), 0, 1),
(1, 8411, '1', NOW(), '1', NOW(), 0, 1),
(1, 8412, '1', NOW(), '1', NOW(), 0, 1),
(1, 8413, '1', NOW(), '1', NOW(), 0, 1),
(1, 8420, '1', NOW(), '1', NOW(), 0, 1),
(1, 8430, '1', NOW(), '1', NOW(), 0, 1);

-- =====================================================
-- 7. 完成提示
-- =====================================================
-- 执行完成后，需要清除Redis缓存或重启后端服务以使菜单生效
-- 菜单结构：
-- 智慧楼宇 (8000)
-- ├── 环境监测 (8100)
-- │   ├── 环境监控 (8101)
-- │   └── 环境告警 (8110)
-- ├── 智能照明 (8200)
-- │   ├── 照明控制 (8201)
-- │   ├── 场景管理 (8210)
-- │   ├── 定时任务 (8220)
-- │   └── 照明告警 (8230)
-- ├── 楼宇自控 (8300)
-- │   ├── 设备监控 (8301)
-- │   ├── 暖通设备 (8310)
-- │   ├── 给排水设备 (8320)
-- │   └── 自控告警 (8330)
-- └── 能耗计量 (8400)
--     ├── 能耗总览 (8401)
--     ├── 仪表管理 (8410)
--     ├── 能耗统计 (8420)
--     └── 能耗告警 (8430)
