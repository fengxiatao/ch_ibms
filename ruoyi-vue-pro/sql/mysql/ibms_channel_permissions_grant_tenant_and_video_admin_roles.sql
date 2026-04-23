-- =============================================
-- 为「租户管理员」与名称含「视频」的管理类角色补充 iot:ibms-channel:* 菜单权限
-- 依赖：已存在 ibms-channel 父菜单及 iot:ibms-channel:* 按钮（见 ibms_channel_permissions_grant_video_roles.sql）
-- 幂等：可重复执行
-- =============================================

INSERT INTO system_role_menu (role_id, menu_id, tenant_id, creator, updater, deleted)
SELECT sr.id, m.id, sr.tenant_id, '1', '1', b'0'
FROM system_role sr
INNER JOIN system_menu m ON m.deleted = b'0' AND m.permission LIKE 'iot:ibms-channel:%'
WHERE sr.deleted = b'0'
  AND (
    sr.code = 'tenant_admin'
    OR sr.name = '租户管理员'
    OR (sr.name LIKE '%视频%' AND (sr.name LIKE '%管理%' OR sr.name LIKE '%管理员%'))
  )
  AND NOT EXISTS (
    SELECT 1 FROM system_role_menu x
    WHERE x.deleted = b'0' AND x.role_id = sr.id AND x.menu_id = m.id AND x.tenant_id = sr.tenant_id
  );
