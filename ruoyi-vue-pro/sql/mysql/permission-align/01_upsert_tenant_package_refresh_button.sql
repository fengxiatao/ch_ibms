-- 租户套餐：运维一键刷新各租户角色菜单（与 TenantPackageController.refreshAllTenantRoleMenu 对应）
-- 幂等：若已存在 permission=system:tenant-package:refresh 的按钮则跳过

SET NAMES utf8mb4;

INSERT INTO system_menu (
  name, permission, type, sort, parent_id, path, icon, component, component_name,
  status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT
  '租户套餐刷新角色菜单',
  'system:tenant-package:refresh',
  3,
  5,
  p.id,
  '',
  '',
  '',
  NULL,
  0,
  b'1', b'1', b'1',
  '1', NOW(), '1', NOW(), b'0'
FROM system_menu p
WHERE p.name = '租户套餐' AND p.type = 2 AND p.deleted = b'0'
  AND NOT EXISTS (
    SELECT 1 FROM system_menu x
    WHERE x.permission = 'system:tenant-package:refresh' AND x.deleted = b'0'
  )
LIMIT 1;
