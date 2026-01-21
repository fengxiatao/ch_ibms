-- =============================================
-- 长辉设备菜单回滚为科鼎协议菜单脚本
-- 版本: V5.0 Rollback
-- 日期: 2025-12-30
-- 说明: 将菜单中的"长辉设备"回滚为"科鼎协议"
-- =============================================

-- 回滚父菜单名称和路径
UPDATE system_menu 
SET name = '科鼎协议',
    path = 'keding',
    updater = '1',
    update_time = NOW()
WHERE name = '长辉设备' AND deleted = 0;

-- 回滚设备管理菜单
UPDATE system_menu 
SET permission = 'iot:keding-device:query',
    component = 'iot/keding/device/index',
    component_name = 'KedingDevice',
    updater = '1',
    update_time = NOW()
WHERE permission = 'iot:changhui-device:query' AND deleted = 0;

UPDATE system_menu 
SET permission = 'iot:keding-device:create',
    updater = '1',
    update_time = NOW()
WHERE permission = 'iot:changhui-device:create' AND deleted = 0;

UPDATE system_menu 
SET permission = 'iot:keding-device:update',
    updater = '1',
    update_time = NOW()
WHERE permission = 'iot:changhui-device:update' AND deleted = 0;

UPDATE system_menu 
SET permission = 'iot:keding-device:delete',
    updater = '1',
    update_time = NOW()
WHERE permission = 'iot:changhui-device:delete' AND deleted = 0;

-- 回滚数据管理菜单（如果存在）
UPDATE system_menu 
SET permission = 'iot:keding-data:query',
    component = 'iot/keding/data/index',
    component_name = 'KedingData',
    updater = '1',
    update_time = NOW()
WHERE permission = 'iot:changhui-data:query' AND deleted = 0;

-- 回滚报警管理菜单
UPDATE system_menu 
SET permission = 'iot:keding-alarm:query',
    component = 'iot/keding/alarm/index',
    component_name = 'KedingAlarm',
    updater = '1',
    update_time = NOW()
WHERE permission = 'iot:changhui-alarm:query' AND deleted = 0;

UPDATE system_menu 
SET permission = 'iot:keding-alarm:update',
    updater = '1',
    update_time = NOW()
WHERE permission = 'iot:changhui-alarm:update' AND deleted = 0;

-- 回滚远程控制菜单
UPDATE system_menu 
SET permission = 'iot:keding-control:query',
    component = 'iot/keding/control/index',
    component_name = 'KedingControl',
    updater = '1',
    update_time = NOW()
WHERE permission = 'iot:changhui-control:query' AND deleted = 0;

UPDATE system_menu 
SET permission = 'iot:keding-control:create',
    updater = '1',
    update_time = NOW()
WHERE permission = 'iot:changhui-control:create' AND deleted = 0;

-- 回滚固件升级菜单
UPDATE system_menu 
SET permission = 'iot:keding-upgrade:query',
    component = 'iot/keding/upgrade/index',
    component_name = 'KedingUpgrade',
    updater = '1',
    update_time = NOW()
WHERE permission = 'iot:changhui-upgrade:query' AND deleted = 0;

UPDATE system_menu 
SET permission = 'iot:keding-upgrade:create',
    updater = '1',
    update_time = NOW()
WHERE permission = 'iot:changhui-upgrade:create' AND deleted = 0;

UPDATE system_menu 
SET permission = 'iot:keding-upgrade:update',
    updater = '1',
    update_time = NOW()
WHERE permission = 'iot:changhui-upgrade:update' AND deleted = 0;

-- 验证回滚结果
SELECT 'Menu rollback completed. Verifying...' AS status;
SELECT id, name, permission, path, component, component_name 
FROM system_menu 
WHERE (name LIKE '%科鼎%' OR permission LIKE '%keding%') AND deleted = 0
ORDER BY id;
