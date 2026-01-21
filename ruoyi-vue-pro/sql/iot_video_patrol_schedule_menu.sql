-- =============================================
-- 定时轮巡菜单配置 SQL 脚本
-- 功能：自动添加"定时轮巡"菜单及其按钮权限
-- 执行前提：确保"视频监控"父菜单已存在
-- =============================================

-- 1. 查询"视频监控"菜单的ID（用于设置父菜单）
-- 如果查询不到，请手动修改下面的 @parent_menu_id
SELECT @parent_menu_id := id 
FROM system_menu 
WHERE name = '视频监控' 
  AND deleted = 0 
LIMIT 1;

-- 验证父菜单ID是否找到
SELECT CONCAT('父菜单ID: ', IFNULL(@parent_menu_id, '未找到')) AS '查询结果';

-- 如果上面查询不到，请取消下面这行的注释，并手动设置父菜单ID
-- SET @parent_menu_id = 2100;  -- 请替换为实际的父菜单ID

-- =============================================
-- 2. 插入"定时轮巡"主菜单
-- =============================================
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
    '定时轮巡',                                           -- 菜单名称
    '',                                                   -- 主菜单不需要权限标识
    2,                                                    -- type: 2=菜单
    4,                                                    -- 排序（在视频监控下第4位）
    @parent_menu_id,                                      -- 父菜单ID（视频监控）
    'patrol-schedule',                                    -- 路由地址
    'schedule',                                           -- 图标
    'security/VideoSurveillance/PatrolSchedule/index',   -- 组件路径
    'PatrolSchedule',                                     -- 组件名称
    0,                                                    -- status: 0=正常
    1,                                                    -- visible: 1=显示
    1,                                                    -- keep_alive: 1=缓存
    0,                                                    -- always_show: 0=不总是显示
    '1',                                                  -- 创建者
    NOW(),                                                -- 创建时间
    '1',                                                  -- 更新者
    NOW(),                                                -- 更新时间
    0                                                     -- deleted: 0=未删除
);

-- 获取刚插入的菜单ID
SET @menu_id = LAST_INSERT_ID();

-- 验证菜单是否插入成功
SELECT CONCAT('定时轮巡菜单ID: ', @menu_id) AS '插入结果';

-- =============================================
-- 3. 插入按钮权限（5个操作权限）
-- =============================================
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
) VALUES 
-- 3.1 查询权限
(
    '定时轮巡查询',
    'iot:video-patrol-schedule:query',
    3,                      -- type: 3=按钮
    1,                      -- 排序
    @menu_id,               -- 父菜单ID（定时轮巡）
    '',
    '',
    '',
    NULL,
    0,
    1,
    1,
    0,
    '1',
    NOW(),
    '1',
    NOW(),
    0
),
-- 3.2 创建权限
(
    '定时轮巡创建',
    'iot:video-patrol-schedule:create',
    3,
    2,
    @menu_id,
    '',
    '',
    '',
    NULL,
    0,
    1,
    1,
    0,
    '1',
    NOW(),
    '1',
    NOW(),
    0
),
-- 3.3 更新权限
(
    '定时轮巡更新',
    'iot:video-patrol-schedule:update',
    3,
    3,
    @menu_id,
    '',
    '',
    '',
    NULL,
    0,
    1,
    1,
    0,
    '1',
    NOW(),
    '1',
    NOW(),
    0
),
-- 3.4 删除权限
(
    '定时轮巡删除',
    'iot:video-patrol-schedule:delete',
    3,
    4,
    @menu_id,
    '',
    '',
    '',
    NULL,
    0,
    1,
    1,
    0,
    '1',
    NOW(),
    '1',
    NOW(),
    0
),
-- 3.5 导出权限
(
    '定时轮巡导出',
    'iot:video-patrol-schedule:export',
    3,
    5,
    @menu_id,
    '',
    '',
    '',
    NULL,
    0,
    1,
    1,
    0,
    '1',
    NOW(),
    '1',
    NOW(),
    0
);

-- =============================================
-- 4. 验证插入结果
-- =============================================
SELECT 
    id,
    name,
    permission,
    type,
    sort,
    parent_id,
    path,
    component
FROM system_menu 
WHERE id = @menu_id 
   OR parent_id = @menu_id
ORDER BY type, sort;

-- =============================================
-- 5. 查看完整的菜单树结构
-- =============================================
SELECT 
    CASE 
        WHEN m.type = 2 THEN CONCAT('  └─ ', m.name)
        WHEN m.type = 3 THEN CONCAT('      └─ ', m.name)
        ELSE m.name
    END AS '菜单结构',
    m.permission AS '权限标识',
    CASE m.type 
        WHEN 1 THEN '目录'
        WHEN 2 THEN '菜单'
        WHEN 3 THEN '按钮'
    END AS '类型'
FROM system_menu m
WHERE (m.id = @parent_menu_id OR m.parent_id = @parent_menu_id OR m.parent_id = @menu_id)
  AND m.deleted = 0
ORDER BY m.parent_id, m.sort;

-- =============================================
-- 执行完成提示
-- =============================================
SELECT '✅ 定时轮巡菜单配置完成！' AS '执行结果',
       '请刷新浏览器并重新登录查看菜单' AS '下一步操作';

-- =============================================
-- 回滚脚本（如果需要删除刚才添加的菜单）
-- =============================================
-- 取消下面的注释可以删除刚才添加的菜单
/*
DELETE FROM system_menu WHERE id = @menu_id OR parent_id = @menu_id;
SELECT '已删除定时轮巡菜单及其子菜单' AS '回滚结果';
*/
