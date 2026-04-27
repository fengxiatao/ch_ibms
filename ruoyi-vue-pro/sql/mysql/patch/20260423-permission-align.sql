-- =====================================================================
-- 租户套餐 x 租户管理员权限对齐补丁（20260423）
-- 目的：把 system_menu.permission 统一到后端 @PreAuthorize 的唯一真源
-- 执行方式：通过 mysql-ibms MCP 依次 run。先 SELECT 核对，再 UPDATE。
-- 所有语句都做成幂等（WHERE 条件精确匹配 + EXISTS 校验），可反复执行。
-- =====================================================================

-- ---------------------------------------------------------------------
-- 0. 事前预检：列出所有可能漂移的权限串，确认影响范围
-- ---------------------------------------------------------------------

-- 0.1 门禁告警：期望只看到 iot:access-alarm:query / iot:access-alarm:update
SELECT id, name, permission, parent_id, type
FROM system_menu
WHERE (permission LIKE 'iot:access-alarm:%') AND deleted = b'0'
ORDER BY permission, id;

-- 0.2 巡更点位 / 路线：期望只看到 iot:epatrol-*，不应出现 iot:patrol-point:* / iot:patrol-route:*
SELECT id, name, permission, parent_id, type
FROM system_menu
WHERE (permission LIKE 'iot:patrol-point:%' OR permission LIKE 'iot:patrol-route:%' OR permission LIKE 'iot:epatrol-%')
  AND deleted = b'0'
ORDER BY permission, id;

-- 0.3 巡更任务 / 计划附加权限（前端用了但后端没有）
SELECT id, name, permission, parent_id, type
FROM system_menu
WHERE permission IN ('iot:patrol-task:complete', 'iot:patrol-plan:trigger',
                     'iot:patrol-record:export', 'iot:patrol-point:generate-qrcode',
                     'iot:ota-firmware:delete',
                     'iot:access-authorization:export', 'iot:access-record:export',
                     'iot:access-dispatch:export', 'iot:access-dispatch:redispatch',
                     'iot:campus:export')
  AND deleted = b'0';

-- ---------------------------------------------------------------------
-- 1. 门禁告警：iot:access-alarm:handle / batch-handle -> iot:access-alarm:update
-- ---------------------------------------------------------------------

-- 1.1 备份将要被改的记录
SELECT id, name, permission, '[backup-before-update]' AS note
FROM system_menu
WHERE permission IN ('iot:access-alarm:handle', 'iot:access-alarm:batch-handle')
  AND deleted = b'0';

-- 1.2 统一到 update（后端 AccessAlarmController.java:52）
UPDATE system_menu
SET permission = 'iot:access-alarm:update',
    name       = CASE WHEN name IN ('告警记录处理','告警记录批量处理') THEN '告警记录处置' ELSE name END,
    update_time = NOW()
WHERE permission IN ('iot:access-alarm:handle', 'iot:access-alarm:batch-handle')
  AND deleted = b'0';

-- 1.3 去除 iot:access-alarm:export（后端无此接口；若未来要加导出再新增）
UPDATE system_menu
SET deleted = b'1', update_time = NOW()
WHERE permission = 'iot:access-alarm:export'
  AND deleted = b'0';

-- ---------------------------------------------------------------------
-- 2. 巡更点位：iot:patrol-point:* -> iot:epatrol-point:*（后端 EpatrolPointController）
-- ---------------------------------------------------------------------

-- 2.1 备份
SELECT id, name, permission, '[backup-before-update]' AS note
FROM system_menu
WHERE permission LIKE 'iot:patrol-point:%' AND deleted = b'0';

-- 2.2 批量对齐（点位）
UPDATE system_menu SET permission = 'iot:epatrol-point:query',  update_time = NOW() WHERE permission = 'iot:patrol-point:query'  AND deleted = b'0';
UPDATE system_menu SET permission = 'iot:epatrol-point:create', update_time = NOW() WHERE permission = 'iot:patrol-point:create' AND deleted = b'0';
UPDATE system_menu SET permission = 'iot:epatrol-point:update', update_time = NOW() WHERE permission = 'iot:patrol-point:update' AND deleted = b'0';
UPDATE system_menu SET permission = 'iot:epatrol-point:delete', update_time = NOW() WHERE permission = 'iot:patrol-point:delete' AND deleted = b'0';

-- 2.3 去除前端 generate-qrcode 等后端不存在的按钮权限（若存在）
UPDATE system_menu
SET deleted = b'1', update_time = NOW()
WHERE permission IN ('iot:patrol-point:export', 'iot:patrol-point:generate-qrcode')
  AND deleted = b'0';

-- ---------------------------------------------------------------------
-- 3. 巡更路线：iot:patrol-route:* -> iot:epatrol-route:*（后端 EpatrolRouteController）
-- ---------------------------------------------------------------------

SELECT id, name, permission, '[backup-before-update]' AS note
FROM system_menu
WHERE permission LIKE 'iot:patrol-route:%' AND deleted = b'0';

UPDATE system_menu SET permission = 'iot:epatrol-route:query',  update_time = NOW() WHERE permission = 'iot:patrol-route:query'  AND deleted = b'0';
UPDATE system_menu SET permission = 'iot:epatrol-route:create', update_time = NOW() WHERE permission = 'iot:patrol-route:create' AND deleted = b'0';
UPDATE system_menu SET permission = 'iot:epatrol-route:update', update_time = NOW() WHERE permission = 'iot:patrol-route:update' AND deleted = b'0';
UPDATE system_menu SET permission = 'iot:epatrol-route:delete', update_time = NOW() WHERE permission = 'iot:patrol-route:delete' AND deleted = b'0';

UPDATE system_menu
SET deleted = b'1', update_time = NOW()
WHERE permission = 'iot:patrol-route:export' AND deleted = b'0';

-- ---------------------------------------------------------------------
-- 4. 巡更任务/计划附加权限（后端无对应，前端应去除/改名）
-- ---------------------------------------------------------------------

-- 4.1 iot:patrol-task:complete -> iot:patrol-task:update（语义上"完成"复用 update 处置接口）
UPDATE system_menu SET permission = 'iot:patrol-task:update', update_time = NOW()
WHERE permission = 'iot:patrol-task:complete' AND deleted = b'0';

-- 4.2 iot:patrol-plan:trigger -> iot:patrol-plan:start（后端 PatrolPlanController 有 start）
UPDATE system_menu SET permission = 'iot:patrol-plan:start', update_time = NOW()
WHERE permission = 'iot:patrol-plan:trigger' AND deleted = b'0';

-- 4.3 iot:patrol-record:export 后端无此接口，隐藏
UPDATE system_menu SET deleted = b'1', update_time = NOW()
WHERE permission = 'iot:patrol-record:export' AND deleted = b'0';

-- ---------------------------------------------------------------------
-- 5. 其他前端无后端对应的权限（删除或保留由前端 v-hasPermi 兜底）
--    本次仅清理确实是菜单 system_menu 里已挂按钮的，不动前端孤立按钮
-- ---------------------------------------------------------------------

UPDATE system_menu SET deleted = b'1', update_time = NOW()
WHERE permission IN (
    'iot:ota-firmware:delete',
    'iot:access-authorization:export',
    'iot:access-record:export',
    'iot:access-dispatch:export',
    'iot:access-dispatch:redispatch',
    'iot:campus:export'
) AND deleted = b'0';

-- ---------------------------------------------------------------------
-- 6. 事后复检
-- ---------------------------------------------------------------------

-- 期望不再出现 iot:patrol-point:* / iot:patrol-route:* / iot:access-alarm:(handle|batch-handle|export)
SELECT 'REMAINING_BAD' AS tag, id, name, permission
FROM system_menu
WHERE deleted = b'0'
  AND (permission LIKE 'iot:patrol-point:%'
    OR permission LIKE 'iot:patrol-route:%'
    OR permission IN ('iot:access-alarm:handle','iot:access-alarm:batch-handle','iot:access-alarm:export',
                      'iot:patrol-task:complete','iot:patrol-plan:trigger','iot:patrol-record:export',
                      'iot:patrol-point:generate-qrcode','iot:ota-firmware:delete'));

-- ---------------------------------------------------------------------
-- 7. 下一步（此脚本跑完后）
--    通过管理后台或接口触发套餐刷新，把已修正的 menuIds 重新分发给各租户管理员角色：
--      POST /admin-api/system/tenant-package/refresh-all-tenant-role-menu
--    （需要 system:tenant-package:refresh 权限，见 TenantPackageController#refreshAllTenantRoleMenu）
-- ---------------------------------------------------------------------
