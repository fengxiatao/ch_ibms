-- =============================================
-- 巡检任务菜单和权限配置
-- 日期: 2024-11-18
-- 说明: 添加巡检任务菜单和相关权限
-- =============================================

-- ========================================
-- 第一部分：菜单配置
-- ========================================

-- 1. 查询智慧安防菜单ID（假设已存在）
-- SELECT id FROM system_menu WHERE name = '智慧安防' AND type = 1;
-- 假设智慧安防菜单ID为 2000（需要根据实际情况修改）

-- 2. 查询视频巡更菜单ID（假设已存在）
-- SELECT id FROM system_menu WHERE name = '视频巡更' AND parent_id = 2000;
-- 假设视频巡更菜单ID为 2100（需要根据实际情况修改）

-- 3. 添加"巡检任务"菜单
INSERT INTO system_menu (
    name,
    permission,
    type,
    sort,
    parent_id,
    path,
    icon,
    component,
    component_name,
    status,
    visible,
    keep_alive,
    always_show,
    creator,
    create_time,
    updater,
    update_time,
    deleted
) VALUES (
    '巡检任务',                                    -- 菜单名称
    'security:patrol-task:query',                  -- 权限标识
    2,                                              -- 类型：2-菜单
    3,                                              -- 排序
    2100,                                           -- 父菜单ID（视频巡更）
    'patrol-task',                                  -- 路由地址
    'ep:video-camera',                              -- 图标
    'security/VideoPatrol/PatrolTask/index',        -- 组件路径
    'PatrolTask',                                   -- 组件名称
    0,                                              -- 状态：0-正常
    true,                                           -- 可见
    true,                                           -- 缓存
    false,                                          -- 总是显示
    '1',                                            -- 创建者
    NOW(),                                          -- 创建时间
    '1',                                            -- 更新者
    NOW(),                                          -- 更新时间
    false                                           -- 删除标记
);

-- 获取刚插入的菜单ID
SET @patrol_task_menu_id = LAST_INSERT_ID();

-- ========================================
-- 第二部分：按钮权限配置
-- ========================================

-- 1. 查询权限
INSERT INTO system_menu (
    name,
    permission,
    type,
    sort,
    parent_id,
    path,
    icon,
    component,
    component_name,
    status,
    visible,
    keep_alive,
    always_show,
    creator,
    create_time,
    updater,
    update_time,
    deleted
) VALUES (
    '巡检任务查询',
    'security:patrol-task:query',
    3,                                              -- 类型：3-按钮
    1,
    @patrol_task_menu_id,
    '',
    '',
    '',
    '',
    0,
    true,
    true,
    false,
    '1',
    NOW(),
    '1',
    NOW(),
    false
);

-- 2. 开始巡检权限
INSERT INTO system_menu (
    name,
    permission,
    type,
    sort,
    parent_id,
    path,
    icon,
    component,
    component_name,
    status,
    visible,
    keep_alive,
    always_show,
    creator,
    create_time,
    updater,
    update_time,
    deleted
) VALUES (
    '开始巡检',
    'security:patrol-task:start',
    3,
    2,
    @patrol_task_menu_id,
    '',
    '',
    '',
    '',
    0,
    true,
    true,
    false,
    '1',
    NOW(),
    '1',
    NOW(),
    false
);

-- 3. 暂停巡检权限
INSERT INTO system_menu (
    name,
    permission,
    type,
    sort,
    parent_id,
    path,
    icon,
    component,
    component_name,
    status,
    visible,
    keep_alive,
    always_show,
    creator,
    create_time,
    updater,
    update_time,
    deleted
) VALUES (
    '暂停巡检',
    'security:patrol-task:pause',
    3,
    3,
    @patrol_task_menu_id,
    '',
    '',
    '',
    '',
    0,
    true,
    true,
    false,
    '1',
    NOW(),
    '1',
    NOW(),
    false
);

-- 4. 刷新任务权限
INSERT INTO system_menu (
    name,
    permission,
    type,
    sort,
    parent_id,
    path,
    icon,
    component,
    component_name,
    status,
    visible,
    keep_alive,
    always_show,
    creator,
    create_time,
    updater,
    update_time,
    deleted
) VALUES (
    '刷新任务',
    'security:patrol-task:refresh',
    3,
    4,
    @patrol_task_menu_id,
    '',
    '',
    '',
    '',
    0,
    true,
    true,
    false,
    '1',
    NOW(),
    '1',
    NOW(),
    false
);

-- ========================================
-- 第三部分：角色权限分配（示例）
-- ========================================

-- 为管理员角色（role_id = 1）分配权限
-- 注意：需要根据实际的角色ID进行调整

-- 分配菜单权限
INSERT INTO system_role_menu (role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id)
SELECT 1, id, '1', NOW(), '1', NOW(), false, 1
FROM system_menu
WHERE permission LIKE 'security:patrol-task:%'
  AND deleted = false;

-- ========================================
-- 验证脚本
-- ========================================

-- 查看新增的菜单
SELECT id, name, permission, type, sort, parent_id, path, component
FROM system_menu
WHERE permission LIKE 'security:patrol-task:%'
  AND deleted = false
ORDER BY sort;

-- 查看角色权限分配
SELECT rm.role_id, r.name AS role_name, m.name AS menu_name, m.permission
FROM system_role_menu rm
JOIN system_role r ON rm.role_id = r.id
JOIN system_menu m ON rm.menu_id = m.id
WHERE m.permission LIKE 'security:patrol-task:%'
  AND rm.deleted = false
  AND m.deleted = false
ORDER BY rm.role_id, m.sort;

-- ========================================
-- 回滚脚本（如需删除）
-- ========================================

/*
-- 删除角色权限
DELETE FROM system_role_menu
WHERE menu_id IN (
    SELECT id FROM system_menu
    WHERE permission LIKE 'security:patrol-task:%'
      AND deleted = false
);

-- 删除菜单
DELETE FROM system_menu
WHERE permission LIKE 'security:patrol-task:%'
  AND deleted = false;
*/
