-- ========================================
-- 添加视频监控-轮巡配置菜单和权限
-- ========================================

-- 1. 查找视频监控的父菜单ID（假设已存在）
-- 如果没有视频监控父菜单，需要先创建

-- 2. 添加"轮巡配置"菜单
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
    '轮巡配置',                                    -- 菜单名称
    'security:patrol-config:query',                -- 权限标识
    2,                                             -- 类型：2=菜单
    30,                                            -- 排序
    (SELECT id FROM (SELECT id FROM system_menu WHERE name = '视频监控' AND type = 1 LIMIT 1) AS temp), -- 父菜单ID
    'patrol-config',                               -- 路由地址
    'ep:setting',                                  -- 图标
    'security/VideoSurveillance/PatrolConfig/index', -- 组件路径
    'PatrolConfig',                                -- 组件名称
    0,                                             -- 状态：0=正常
    true,                                          -- 是否可见
    true,                                          -- 是否缓存
    false,                                         -- 是否总是显示
    'admin',                                       -- 创建者
    NOW(),                                         -- 创建时间
    'admin',                                       -- 更新者
    NOW(),                                         -- 更新时间
    0                                              -- 是否删除：0=未删除
);

-- 3. 获取刚插入的菜单ID，用于添加按钮权限
SET @patrol_config_menu_id = LAST_INSERT_ID();

-- 4. 添加"查询"按钮权限
INSERT INTO system_menu (
    name, 
    permission, 
    type, 
    sort, 
    parent_id, 
    path, 
    icon, 
    component, 
    status, 
    visible, 
    keep_alive, 
    creator, 
    create_time, 
    updater, 
    update_time, 
    deleted
) VALUES (
    '轮巡配置查询',
    'security:patrol-config:query',
    3,                                             -- 类型：3=按钮
    1,
    @patrol_config_menu_id,
    '',
    '',
    '',
    0,
    true,
    true,
    'admin',
    NOW(),
    'admin',
    NOW(),
    0
);

-- 5. 添加"创建"按钮权限
INSERT INTO system_menu (
    name, 
    permission, 
    type, 
    sort, 
    parent_id, 
    path, 
    icon, 
    component, 
    status, 
    visible, 
    keep_alive, 
    creator, 
    create_time, 
    updater, 
    update_time, 
    deleted
) VALUES (
    '轮巡配置创建',
    'security:patrol-config:create',
    3,
    2,
    @patrol_config_menu_id,
    '',
    '',
    '',
    0,
    true,
    true,
    'admin',
    NOW(),
    'admin',
    NOW(),
    0
);

-- 6. 添加"更新"按钮权限
INSERT INTO system_menu (
    name, 
    permission, 
    type, 
    sort, 
    parent_id, 
    path, 
    icon, 
    component, 
    status, 
    visible, 
    keep_alive, 
    creator, 
    create_time, 
    updater, 
    update_time, 
    deleted
) VALUES (
    '轮巡配置更新',
    'security:patrol-config:update',
    3,
    3,
    @patrol_config_menu_id,
    '',
    '',
    '',
    0,
    true,
    true,
    'admin',
    NOW(),
    'admin',
    NOW(),
    0
);

-- 7. 添加"删除"按钮权限
INSERT INTO system_menu (
    name, 
    permission, 
    type, 
    sort, 
    parent_id, 
    path, 
    icon, 
    component, 
    status, 
    visible, 
    keep_alive, 
    creator, 
    create_time, 
    updater, 
    update_time, 
    deleted
) VALUES (
    '轮巡配置删除',
    'security:patrol-config:delete',
    3,
    4,
    @patrol_config_menu_id,
    '',
    '',
    '',
    0,
    true,
    true,
    'admin',
    NOW(),
    'admin',
    NOW(),
    0
);

-- 8. 添加"任务编辑"子页面（隐藏菜单，用于编辑任务详情）
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
    '任务编辑',                                    -- 菜单名称
    'security:patrol-config:update',               -- 权限标识（复用更新权限）
    2,                                             -- 类型：2=菜单
    31,                                            -- 排序
    (SELECT id FROM (SELECT id FROM system_menu WHERE name = '视频监控' AND type = 1 LIMIT 1) AS temp), -- 父菜单ID
    'patrol-config/task-edit',                     -- 路由地址
    '',                                            -- 图标
    'security/VideoSurveillance/PatrolConfig/TaskEdit', -- 组件路径
    'PatrolTaskEdit',                              -- 组件名称
    0,                                             -- 状态：0=正常
    false,                                         -- 是否可见：false=隐藏
    true,                                          -- 是否缓存
    false,                                         -- 是否总是显示
    'admin',                                       -- 创建者
    NOW(),                                         -- 创建时间
    'admin',                                       -- 更新者
    NOW(),                                         -- 更新时间
    0                                              -- 是否删除：0=未删除
);

-- 9. 验证插入结果
SELECT 
    id,
    name,
    permission,
    type,
    parent_id,
    path,
    component,
    visible
FROM system_menu 
WHERE name LIKE '%轮巡%' OR name LIKE '%任务编辑%'
ORDER BY parent_id, sort;
