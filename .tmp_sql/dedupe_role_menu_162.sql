-- 清理租户162的 system_role_menu 重复数据
-- 保留每组 (role_id, menu_id, tenant_id) 中 id 最小的那条
-- 删除所有重复的后续记录

-- 先统计清理前
SELECT COUNT(*) AS before_total, COUNT(DISTINCT role_id, menu_id) AS before_distinct 
FROM system_role_menu WHERE tenant_id = 162;

-- 执行去重删除
DELETE rm1 FROM system_role_menu rm1
INNER JOIN system_role_menu rm2 
  ON rm1.role_id = rm2.role_id 
  AND rm1.menu_id = rm2.menu_id
  AND rm1.tenant_id = rm2.tenant_id
  AND rm1.id > rm2.id
WHERE rm1.tenant_id = 162;

-- 清理后统计
SELECT COUNT(*) AS after_total, COUNT(DISTINCT role_id, menu_id) AS after_distinct 
FROM system_role_menu WHERE tenant_id = 162;
