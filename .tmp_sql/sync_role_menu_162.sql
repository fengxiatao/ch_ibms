-- 将套餐113的新增菜单权限同步到租户162的租户管理员角色(159)
-- 只需添加当前role_menu中缺失的菜单ID

-- 添加缺失的菜单权限
INSERT INTO system_role_menu (role_id, menu_id, tenant_id, creator, create_time, updater, update_time, deleted)
SELECT 159, m.id, 162, 'hide-iot-sync', NOW(), 'hide-iot-sync', NOW(), b'0'
FROM system_menu m
WHERE m.id IN (4003, 4009, 5232, 5237, 5242, 6198, 6203, 72568, 72569)
  AND NOT EXISTS (
    SELECT 1 FROM system_role_menu rm
    WHERE rm.role_id = 159 AND rm.tenant_id = 162 AND rm.menu_id = m.id
  );

-- 验证结果
SELECT rm.menu_id, m.name, m.permission, m.type
FROM system_role_menu rm
JOIN system_menu m ON m.id = rm.menu_id
WHERE rm.role_id = 159 AND rm.tenant_id = 162 
  AND rm.menu_id IN (4003, 4009, 5232, 5237, 5242, 6198, 6203, 72568, 72569)
ORDER BY rm.menu_id;

-- 最终统计
SELECT COUNT(*) AS final_total, COUNT(DISTINCT menu_id) AS final_distinct 
FROM system_role_menu WHERE tenant_id = 162;
