-- =====================================================
-- IoT 任务监控菜单配置
-- =====================================================

-- 查找 IoT 模块的父菜单ID
SET @iot_parent_id = (SELECT id FROM system_menu WHERE name = 'IoT 物联网' LIMIT 1);

-- 如果找不到，使用默认值（需要根据实际情况调整）
SET @iot_parent_id = IFNULL(@iot_parent_id, 2100);

-- 插入"任务监控"菜单
INSERT INTO system_menu (
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status,
    visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
) VALUES (
    '任务监控',
    'iot:task:query',
    2,  -- 菜单类型：2=菜单
    90, -- 排序
    @iot_parent_id,
    'task-monitor',
    'ep:monitor',
    'iot/taskMonitor/index',
    'IotTaskMonitor',
    0,  -- 状态：0=正常
    TRUE,
    TRUE,
    FALSE,
    'admin',
    NOW(),
    'admin',
    NOW(),
    FALSE
);

-- 获取刚插入的菜单ID
SET @task_monitor_menu_id = LAST_INSERT_ID();

-- 分配给超级管理员角色
INSERT INTO system_role_menu (role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id)
SELECT 1, @task_monitor_menu_id, 'admin', NOW(), 'admin', NOW(), FALSE, 1
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM system_role_menu WHERE role_id = 1 AND menu_id = @task_monitor_menu_id
);

-- =====================================================
-- 验证
-- =====================================================

SELECT 
    id,
    name,
    path,
    component,
    icon,
    sort,
    parent_id
FROM system_menu 
WHERE name = '任务监控' OR id = @iot_parent_id
ORDER BY parent_id, sort;

SELECT '=== IoT任务监控菜单添加完成 ===' AS message;
