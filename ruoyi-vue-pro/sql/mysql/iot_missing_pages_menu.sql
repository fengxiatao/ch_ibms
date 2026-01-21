-- ========================================
-- IoT 缺失页面菜单批量添加 SQL
-- ========================================
-- 说明：为所有存在前端页面但缺失菜单的IoT功能添加菜单记录
-- ========================================
-- 执行前请先执行检查SQL，确认哪些菜单缺失
-- ========================================

-- ========================================
-- 【重要】先查询父菜单ID
-- ========================================
-- 请先执行以下查询，获取正确的 parent_id：
/*
SELECT id, name, path, component 
FROM system_menu 
WHERE (name LIKE '%设备%' OR name LIKE '%物联%' OR component LIKE 'iot/%')
  AND type IN (1, 2)  -- 目录或菜单
  AND deleted = b'0'
ORDER BY parent_id, sort;
*/

-- 假设的父菜单ID（请根据实际情况修改）：
-- @iot_root_id      = IoT根菜单ID（智慧物联）
-- @device_manage_id = 设备管理菜单ID
-- @video_manage_id  = 视频管理菜单ID
-- @spatial_manage_id = 空间管理菜单ID

-- ========================================
-- 1. 设备发现（Device Discovery）
-- ========================================
-- path: iot/device/discovery
-- 父菜单：设备接入/设备管理

INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status,
    visible, keep_alive, always_show, creator,
    create_time, updater, update_time, deleted
) VALUES (
    '设备发现', '', 2, 11, 2738,  -- parent_id 需要调整
    'discovery', 'ep:search', 'iot/device/discovery/index', 'IotDeviceDiscovery', 0,
    b'1', b'1', b'1', '1',
    NOW(), '1', NOW(), b'0'
);

-- ========================================
-- 2. 设备配置（Device Config）
-- ========================================
-- path: iot/device/config

INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status,
    visible, keep_alive, always_show, creator,
    create_time, updater, update_time, deleted
) VALUES (
    '设备配置', 'iot:device:config:query', 2, 12, 2738,  -- parent_id 需要调整
    'config', 'ep:setting', 'iot/device/config/index', 'IotDeviceConfig', 0,
    b'1', b'1', b'1', '1',
    NOW(), '1', NOW(), b'0'
);

-- ========================================
-- 3. 设备控制（Device Control）
-- ========================================
-- path: iot/device/control

INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status,
    visible, keep_alive, always_show, creator,
    create_time, updater, update_time, deleted
) VALUES (
    '设备控制', 'iot:device:control:query', 2, 13, 2738,  -- parent_id 需要调整
    'control', 'ep:operation', 'iot/device/control/index', 'IotDeviceControl', 0,
    b'1', b'1', b'1', '1',
    NOW(), '1', NOW(), b'0'
);

-- ========================================
-- 4. 设备事件（Device Event）
-- ========================================
-- path: iot/device/event

INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status,
    visible, keep_alive, always_show, creator,
    create_time, updater, update_time, deleted
) VALUES (
    '设备事件', 'iot:device:event:query', 2, 14, 2738,  -- parent_id 需要调整
    'event', 'ep:bell', 'iot/device/event/index', 'IotDeviceEvent', 0,
    b'1', b'1', b'1', '1',
    NOW(), '1', NOW(), b'0'
);

-- ========================================
-- 5. 视频预览（Video Preview）
-- ========================================
-- path: iot/video/preview

INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status,
    visible, keep_alive, always_show, creator,
    create_time, updater, update_time, deleted
) VALUES (
    '视频预览', 'iot:video:preview:query', 2, 20, 2738,  -- parent_id 需要调整（视频管理菜单下）
    'preview', 'ep:video-camera', 'iot/video/preview/index', 'IotVideoPreview', 0,
    b'1', b'1', b'1', '1',
    NOW(), '1', NOW(), b'0'
);

-- ========================================
-- 6. 视频回放（Video Playback）
-- ========================================
-- path: iot/video/playback

INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status,
    visible, keep_alive, always_show, creator,
    create_time, updater, update_time, deleted
) VALUES (
    '视频回放', 'iot:video:playback:query', 2, 21, 2738,  -- parent_id 需要调整（视频管理菜单下）
    'playback', 'ep:video-play', 'iot/video/playback/index', 'IotVideoPlayback', 0,
    b'1', b'1', b'1', '1',
    NOW(), '1', NOW(), b'0'
);

-- ========================================
-- 7. GIS地图（GIS Map）
-- ========================================
-- path: iot/gis

INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status,
    visible, keep_alive, always_show, creator,
    create_time, updater, update_time, deleted
) VALUES (
    'GIS地图', 'iot:gis:query', 2, 30, 2738,  -- parent_id 需要调整（空间管理菜单下）
    'gis', 'ep:location', 'iot/gis/index', 'IotGis', 0,
    b'1', b'1', b'1', '1',
    NOW(), '1', NOW(), b'0'
);

-- ========================================
-- 8. 空间平面图（Floor Plan）
-- ========================================
-- path: iot/spatial/floorplan

INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status,
    visible, keep_alive, always_show, creator,
    create_time, updater, update_time, deleted
) VALUES (
    '空间平面图', 'iot:spatial:floorplan:query', 2, 35, 2738,  -- parent_id 需要调整（空间管理菜单下）
    'floorplan', 'ep:map-location', 'iot/spatial/floorplan/index', 'IotSpatialFloorplan', 0,
    b'1', b'1', b'1', '1',
    NOW(), '1', NOW(), b'0'
);

-- ========================================
-- 9. 任务监控（Task Monitor）
-- ========================================
-- path: iot/task/monitor

INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status,
    visible, keep_alive, always_show, creator,
    create_time, updater, update_time, deleted
) VALUES (
    '任务监控', 'iot:task:monitor:query', 2, 40, 2738,  -- parent_id 需要调整（任务管理菜单下）
    'monitor', 'ep:monitor', 'iot/task/monitor/index', 'IotTaskMonitor', 0,
    b'1', b'1', b'1', '1',
    NOW(), '1', NOW(), b'0'
);

-- ========================================
-- 10. 物模型管理（Thing Model）
-- ========================================
-- path: iot/thingmodel

INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status,
    visible, keep_alive, always_show, creator,
    create_time, updater, update_time, deleted
) VALUES (
    '物模型', 'iot:thingmodel:query', 2, 45, 2738,  -- parent_id 需要调整
    'thingmodel', 'ep:data-board', 'iot/thingmodel/index', 'IotThingModel', 0,
    b'1', b'1', b'1', '1',
    NOW(), '1', NOW(), b'0'
);

-- ========================================
-- 11. 数据规则（Data Rule）
-- ========================================
-- path: iot/rule/data

INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status,
    visible, keep_alive, always_show, creator,
    create_time, updater, update_time, deleted
) VALUES (
    '数据规则', 'iot:rule:data:query', 2, 50, 2738,  -- parent_id 需要调整（规则引擎菜单下）
    'data', 'ep:data-analysis', 'iot/rule/data/index', 'IotRuleData', 0,
    b'1', b'1', b'1', '1',
    NOW(), '1', NOW(), b'0'
);

-- ========================================
-- 检查SQL - 查询刚添加的菜单
-- ========================================
/*
SELECT 
    id, 
    name, 
    component, 
    path, 
    parent_id, 
    type,
    sort,
    icon,
    visible
FROM system_menu 
WHERE name IN (
    '设备发现', '设备配置', '设备控制', '设备事件',
    '视频预览', '视频回放', 'GIS地图', '空间平面图',
    '任务监控', '物模型', '数据规则'
)
  AND deleted = b'0'
ORDER BY type, sort;
*/

-- ========================================
-- 查询所有IoT前端页面对应的菜单（检查遗漏）
-- ========================================
/*
SELECT 
    id, 
    name, 
    component, 
    path, 
    parent_id, 
    type,
    sort
FROM system_menu 
WHERE component LIKE 'iot/%'
  AND type = 2  -- 只看菜单，不看按钮
  AND deleted = b'0'
ORDER BY parent_id, sort;
*/

-- ========================================
-- 注意事项
-- ========================================
-- 1. 所有 parent_id 都设置为 2738，实际使用时需要根据数据库调整
-- 2. 建议逐条执行并检查，而不是一次性全部执行
-- 3. 某些页面可能已经有菜单，执行前请先检查
-- 4. 执行后需要重新登录前端以刷新路由缓存
-- 5. 如果菜单不显示，检查 visible 和 status 字段

-- ========================================
-- 获取正确 parent_id 的参考SQL
-- ========================================
/*
-- 查询IoT模块的根菜单
SELECT id, name, path FROM system_menu WHERE name LIKE '%智慧物联%' OR name LIKE '%IoT%';

-- 查询设备管理相关菜单
SELECT id, name, path FROM system_menu WHERE name LIKE '%设备%' AND type = 1;

-- 查询视频管理相关菜单
SELECT id, name, path FROM system_menu WHERE name LIKE '%视频%' AND type = 1;

-- 查询空间管理相关菜单
SELECT id, name, path FROM system_menu WHERE name LIKE '%空间%' AND type = 1;
*/














