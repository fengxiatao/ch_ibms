-- ===== 验证1: 套餐113配置状态 =====
SELECT '=== 套餐113配置 ===' AS check_item;
SELECT id, name, excluded_menu_ids, LENGTH(menu_ids) AS menu_ids_len 
FROM system_tenant_package WHERE id = 113;

-- ===== 验证2: 套餐113中是否包含4000/71383/71384（应该不在）=====
SELECT '=== 4000树根/页面应不在套餐中 ===' AS check_item;
SELECT 
  FIND_IN_SET('4000', REPLACE(REPLACE(REPLACE(menu_ids, '[',''), ']',''),' ','')) AS has_4000,
  FIND_IN_SET('4001', REPLACE(REPLACE(REPLACE(menu_ids, '[',''), ']',''),' ','')) AS has_4001,
  FIND_IN_SET('4008', REPLACE(REPLACE(REPLACE(menu_ids, '[',''), ']',''),' ','')) AS has_4008,
  FIND_IN_SET('71376', REPLACE(REPLACE(REPLACE(menu_ids, '[',''), ']',''),' ','')) AS has_71376,
  FIND_IN_SET('71383', REPLACE(REPLACE(REPLACE(menu_ids, '[',''), ']',''),' ','')) AS has_71383,
  FIND_IN_SET('71384', REPLACE(REPLACE(REPLACE(menu_ids, '[',''), ']',''),' ','')) AS has_71384,
  FIND_IN_SET('71385', REPLACE(REPLACE(REPLACE(menu_ids, '[',''), ']',''),' ','')) AS has_71385
FROM system_tenant_package WHERE id = 113;

-- ===== 验证3: 跨模块必需的9个按钮权限应都在套餐中 =====
SELECT '=== 9个跨模块按钮应都在套餐中 ===' AS check_item;
SELECT 
  FIND_IN_SET('4003', REPLACE(REPLACE(REPLACE(menu_ids, '[',''), ']',''),' ','')) AS product_query,
  FIND_IN_SET('4009', REPLACE(REPLACE(REPLACE(menu_ids, '[',''), ']',''),' ','')) AS device_query,
  FIND_IN_SET('5232', REPLACE(REPLACE(REPLACE(menu_ids, '[',''), ']',''),' ','')) AS building_query,
  FIND_IN_SET('5237', REPLACE(REPLACE(REPLACE(menu_ids, '[',''), ']',''),' ','')) AS floor_query,
  FIND_IN_SET('5242', REPLACE(REPLACE(REPLACE(menu_ids, '[',''), ']',''),' ','')) AS area_query,
  FIND_IN_SET('6198', REPLACE(REPLACE(REPLACE(menu_ids, '[',''), ']',''),' ','')) AS channel_query,
  FIND_IN_SET('6203', REPLACE(REPLACE(REPLACE(menu_ids, '[',''), ']',''),' ','')) AS channel_sync,
  FIND_IN_SET('72568', REPLACE(REPLACE(REPLACE(menu_ids, '[',''), ']',''),' ','')) AS ibms_product_query,
  FIND_IN_SET('72569', REPLACE(REPLACE(REPLACE(menu_ids, '[',''), ']',''),' ','')) AS ibms_space_query
FROM system_tenant_package WHERE id = 113;

-- ===== 验证4: 租户162的role_menu数量正常 =====
SELECT '=== 租户162 role_menu 统计 ===' AS check_item;
SELECT COUNT(*) AS total, COUNT(DISTINCT menu_id) AS distinct_cnt 
FROM system_role_menu WHERE tenant_id = 162;

-- ===== 验证5: 检查4000树在role_menu中的分布 =====
SELECT '=== 租户162的4000树 role_menu 分布 ===' AS check_item;
SELECT m.type, COUNT(*) AS cnt FROM system_role_menu rm 
JOIN system_menu m ON m.id = rm.menu_id
WHERE rm.tenant_id = 162 AND (
  m.id = 4000 OR 
  m.parent_id = 4000 OR 
  m.parent_id IN (SELECT id FROM system_menu WHERE parent_id = 4000) OR
  m.parent_id IN (SELECT id FROM system_menu WHERE parent_id IN (SELECT id FROM system_menu WHERE parent_id = 4000))
)
GROUP BY m.type;
