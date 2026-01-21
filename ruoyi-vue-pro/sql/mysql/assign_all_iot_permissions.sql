-- 为admin分配所有IoT子菜单权限

USE ch_ibms;

-- 查找super_admin角色ID
SET @admin_role_id = (SELECT id FROM system_role WHERE code = 'super_admin' LIMIT 1);

-- 删除旧的权限
DELETE FROM system_role_menu
WHERE role_id = @admin_role_id
AND menu_id IN (
    SELECT id FROM system_menu
    WHERE id >= 4000 AND id < 7000  -- IoT菜单ID范围
);

-- 分配所有IoT相关菜单（包括主菜单和子菜单）
INSERT INTO system_role_menu (role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id)
SELECT DISTINCT
    @admin_role_id,
    m.id,
    'system',
    NOW(),
    'system',
    NOW(),
    0,
    1
FROM system_menu m
WHERE (
    -- 方式1：通过名称匹配
    m.name LIKE '%物联%' 
    OR m.name LIKE '%IoT%' 
    OR m.name LIKE '%设备%'
    OR m.name LIKE '%产品%'
    OR m.name LIKE '%规则%'
    OR m.name LIKE '%告警%'
    OR m.name LIKE '%空间%'
    OR m.name LIKE '%任务%'
    -- 方式2：通过路径匹配
    OR m.path LIKE '%iot%'
    OR m.path LIKE '%device%'
    OR m.path LIKE '%product%'
    OR m.path LIKE '%spatial%'
    OR m.path LIKE '%gis%'
    -- 方式3：通过ID范围（IoT模块通常在4000-6999范围）
    OR (m.id >= 4000 AND m.id < 7000)
)
AND m.deleted = 0;

-- 显示结果
SELECT CONCAT('已为super_admin分配 ', ROW_COUNT(), ' 个IoT菜单权限') AS result;

-- 验证：显示所有分配的IoT菜单
SELECT 
    m.id, m.parent_id, m.name, m.path, m.component, m.type
FROM system_role_menu rm
JOIN system_menu m ON rm.menu_id = m.id
JOIN system_role r ON rm.role_id = r.id
WHERE r.code = 'super_admin'
AND m.id >= 4000 AND m.id < 7000
ORDER BY m.parent_id, m.sort;

















