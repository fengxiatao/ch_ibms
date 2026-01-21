-- 检查设备管理菜单配置

USE ch_ibms;

-- 查找"智慧物联"主菜单
SELECT '=== 智慧物联主菜单 ===' AS info;
SELECT id, parent_id, name, path, component, status, visible, type
FROM system_menu
WHERE name = '智慧物联' OR name LIKE '智慧物联%';

-- 查找"设备接入"目录
SELECT '=== 设备接入目录 ===' AS info;
SELECT id, parent_id, name, path, component, status, visible, type
FROM system_menu
WHERE name = '设备接入';

-- 查找"设备管理"菜单
SELECT '=== 设备管理菜单 ===' AS info;
SELECT id, parent_id, name, path, component, status, visible, type
FROM system_menu
WHERE name = '设备管理' AND path LIKE '%device%';

-- 查找IoT下所有菜单
SELECT '=== IoT所有菜单 ===' AS info;
SELECT id, parent_id, name, path, component, type, status, visible
FROM system_menu
WHERE parent_id IN (
    SELECT id FROM system_menu WHERE name LIKE '%物联%'
)
ORDER BY parent_id, sort;

















