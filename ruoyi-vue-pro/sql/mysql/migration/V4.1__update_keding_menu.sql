-- =============================================
-- 科鼎协议菜单更新脚本
-- 版本: V4.1
-- 日期: 2025-12-21
-- 说明: 更新菜单中的德通为科鼎
-- =============================================

-- 更新父菜单名称和路径
UPDATE system_menu 
SET name = '科鼎协议',
    path = 'keding',
    updater = '1',
    update_time = NOW()
WHERE name = '德通协议' AND deleted = 0;

-- 更新设备管理菜单
UPDATE system_menu 
SET permission = 'iot:keding-device:query',
    component = 'iot/keding/device/index',
    component_name = 'KedingDevice',
    updater = '1',
    update_time = NOW()
WHERE permission = 'iot:detong-device:query' AND deleted = 0;

UPDATE system_menu 
SET permission = 'iot:keding-device:create',
    updater = '1',
    update_time = NOW()
WHERE permission = 'iot:detong-device:create' AND deleted = 0;

UPDATE system_menu 
SET permission = 'iot:keding-device:update',
    updater = '1',
    update_time = NOW()
WHERE permission = 'iot:detong-device:update' AND deleted = 0;

UPDATE system_menu 
SET permission = 'iot:keding-device:delete',
    updater = '1',
    update_time = NOW()
WHERE permission = 'iot:detong-device:delete' AND deleted = 0;

-- 更新报警管理菜单
UPDATE system_menu 
SET permission = 'iot:keding-alarm:query',
    component = 'iot/keding/alarm/index',
    component_name = 'KedingAlarm',
    updater = '1',
    update_time = NOW()
WHERE permission = 'iot:detong-alarm:query' AND deleted = 0;

UPDATE system_menu 
SET permission = 'iot:keding-alarm:update',
    updater = '1',
    update_time = NOW()
WHERE permission = 'iot:detong-alarm:update' AND deleted = 0;

-- 更新远程控制菜单
UPDATE system_menu 
SET permission = 'iot:keding-control:query',
    component = 'iot/keding/control/index',
    component_name = 'KedingControl',
    updater = '1',
    update_time = NOW()
WHERE permission = 'iot:detong-control:query' AND deleted = 0;

UPDATE system_menu 
SET permission = 'iot:keding-control:create',
    updater = '1',
    update_time = NOW()
WHERE permission = 'iot:detong-control:create' AND deleted = 0;

-- 更新固件升级菜单
UPDATE system_menu 
SET permission = 'iot:keding-upgrade:query',
    component = 'iot/keding/upgrade/index',
    component_name = 'KedingUpgrade',
    updater = '1',
    update_time = NOW()
WHERE permission = 'iot:detong-upgrade:query' AND deleted = 0;

UPDATE system_menu 
SET permission = 'iot:keding-upgrade:create',
    updater = '1',
    update_time = NOW()
WHERE permission = 'iot:detong-upgrade:create' AND deleted = 0;

UPDATE system_menu 
SET permission = 'iot:keding-upgrade:update',
    updater = '1',
    update_time = NOW()
WHERE permission = 'iot:detong-upgrade:update' AND deleted = 0;

-- 验证更新结果
SELECT 'Menu update completed. Verifying...' AS status;
SELECT id, name, permission, path, component, component_name 
FROM system_menu 
WHERE (name LIKE '%科鼎%' OR permission LIKE '%keding%') AND deleted = 0
ORDER BY id;
