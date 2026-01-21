-- =============================================
-- 科鼎协议菜单更新为长辉设备菜单脚本
-- 版本: V5.0
-- 日期: 2025-12-30
-- 说明: 将菜单中的"科鼎"更新为"长辉设备"
-- Requirements: 1.1 - 前端界面统一显示为"长辉设备"
-- =============================================

-- 更新父菜单名称和路径
UPDATE system_menu 
SET name = '长辉设备',
    path = 'changhui',
    updater = '1',
    update_time = NOW()
WHERE name = '科鼎协议' AND deleted = 0;

-- 更新设备管理菜单
UPDATE system_menu 
SET name = '设备管理',
    permission = 'iot:changhui-device:query',
    component = 'iot/changhui/device/index',
    component_name = 'ChanghuiDevice',
    updater = '1',
    update_time = NOW()
WHERE permission = 'iot:keding-device:query' AND deleted = 0;

UPDATE system_menu 
SET permission = 'iot:changhui-device:create',
    updater = '1',
    update_time = NOW()
WHERE permission = 'iot:keding-device:create' AND deleted = 0;

UPDATE system_menu 
SET permission = 'iot:changhui-device:update',
    updater = '1',
    update_time = NOW()
WHERE permission = 'iot:keding-device:update' AND deleted = 0;

UPDATE system_menu 
SET permission = 'iot:changhui-device:delete',
    updater = '1',
    update_time = NOW()
WHERE permission = 'iot:keding-device:delete' AND deleted = 0;

-- 更新数据管理菜单（如果存在）
UPDATE system_menu 
SET name = '数据管理',
    permission = 'iot:changhui-data:query',
    component = 'iot/changhui/data/index',
    component_name = 'ChanghuiData',
    updater = '1',
    update_time = NOW()
WHERE permission = 'iot:keding-data:query' AND deleted = 0;

-- 更新报警管理菜单
UPDATE system_menu 
SET name = '报警管理',
    permission = 'iot:changhui-alarm:query',
    component = 'iot/changhui/alarm/index',
    component_name = 'ChanghuiAlarm',
    updater = '1',
    update_time = NOW()
WHERE permission = 'iot:keding-alarm:query' AND deleted = 0;

UPDATE system_menu 
SET permission = 'iot:changhui-alarm:update',
    updater = '1',
    update_time = NOW()
WHERE permission = 'iot:keding-alarm:update' AND deleted = 0;

-- 更新远程控制菜单
UPDATE system_menu 
SET name = '远程控制',
    permission = 'iot:changhui-control:query',
    component = 'iot/changhui/control/index',
    component_name = 'ChanghuiControl',
    updater = '1',
    update_time = NOW()
WHERE permission = 'iot:keding-control:query' AND deleted = 0;

UPDATE system_menu 
SET permission = 'iot:changhui-control:create',
    updater = '1',
    update_time = NOW()
WHERE permission = 'iot:keding-control:create' AND deleted = 0;

-- 更新固件升级菜单
UPDATE system_menu 
SET name = '固件升级',
    permission = 'iot:changhui-upgrade:query',
    component = 'iot/changhui/upgrade/index',
    component_name = 'ChanghuiUpgrade',
    updater = '1',
    update_time = NOW()
WHERE permission = 'iot:keding-upgrade:query' AND deleted = 0;

UPDATE system_menu 
SET permission = 'iot:changhui-upgrade:create',
    updater = '1',
    update_time = NOW()
WHERE permission = 'iot:keding-upgrade:create' AND deleted = 0;

UPDATE system_menu 
SET permission = 'iot:changhui-upgrade:update',
    updater = '1',
    update_time = NOW()
WHERE permission = 'iot:keding-upgrade:update' AND deleted = 0;

-- 验证更新结果
SELECT 'Menu update completed. Verifying...' AS status;
SELECT id, name, permission, path, component, component_name 
FROM system_menu 
WHERE (name LIKE '%长辉%' OR permission LIKE '%changhui%') AND deleted = 0
ORDER BY id;
