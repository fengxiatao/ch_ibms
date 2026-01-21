-- ========================================
-- IoT 菜单完整性检查 SQL
-- ========================================
-- 说明：检查哪些前端页面存在但菜单缺失
-- ========================================

-- ========================================
-- 1. 查询所有已配置的IoT菜单
-- ========================================
SELECT 
    id, 
    name, 
    component, 
    path, 
    parent_id,
    type,
    sort,
    icon,
    visible,
    status
FROM system_menu 
WHERE component LIKE 'iot/%'
  AND type = 2  -- 只看菜单页面，不看按钮
  AND deleted = b'0'
ORDER BY parent_id, sort;

-- ========================================
-- 2. 检查具体页面的菜单是否存在
-- ========================================

-- 2.1 设备发现
SELECT 
    CASE 
        WHEN COUNT(*) > 0 THEN '✅ 已存在'
        ELSE '❌ 缺失'
    END AS status,
    '设备发现' AS page_name,
    'iot/device/discovery/index' AS component
FROM system_menu 
WHERE component = 'iot/device/discovery/index'
  AND deleted = b'0';

-- 2.2 设备配置
SELECT 
    CASE 
        WHEN COUNT(*) > 0 THEN '✅ 已存在'
        ELSE '❌ 缺失'
    END AS status,
    '设备配置' AS page_name,
    'iot/device/config/index' AS component
FROM system_menu 
WHERE component = 'iot/device/config/index'
  AND deleted = b'0';

-- 2.3 设备控制
SELECT 
    CASE 
        WHEN COUNT(*) > 0 THEN '✅ 已存在'
        ELSE '❌ 缺失'
    END AS status,
    '设备控制' AS page_name,
    'iot/device/control/index' AS component
FROM system_menu 
WHERE component = 'iot/device/control/index'
  AND deleted = b'0';

-- 2.4 设备事件
SELECT 
    CASE 
        WHEN COUNT(*) > 0 THEN '✅ 已存在'
        ELSE '❌ 缺失'
    END AS status,
    '设备事件' AS page_name,
    'iot/device/event/index' AS component
FROM system_menu 
WHERE component = 'iot/device/event/index'
  AND deleted = b'0';

-- 2.5 视频预览
SELECT 
    CASE 
        WHEN COUNT(*) > 0 THEN '✅ 已存在'
        ELSE '❌ 缺失'
    END AS status,
    '视频预览' AS page_name,
    'iot/video/preview/index' AS component
FROM system_menu 
WHERE component = 'iot/video/preview/index'
  AND deleted = b'0';

-- 2.6 视频回放
SELECT 
    CASE 
        WHEN COUNT(*) > 0 THEN '✅ 已存在'
        ELSE '❌ 缺失'
    END AS status,
    '视频回放' AS page_name,
    'iot/video/playback/index' AS component
FROM system_menu 
WHERE component = 'iot/video/playback/index'
  AND deleted = b'0';

-- 2.7 GIS地图
SELECT 
    CASE 
        WHEN COUNT(*) > 0 THEN '✅ 已存在'
        ELSE '❌ 缺失'
    END AS status,
    'GIS地图' AS page_name,
    'iot/gis/index' AS component
FROM system_menu 
WHERE component = 'iot/gis/index'
  AND deleted = b'0';

-- 2.8 任务监控
SELECT 
    CASE 
        WHEN COUNT(*) > 0 THEN '✅ 已存在'
        ELSE '❌ 缺失'
    END AS status,
    '任务监控' AS page_name,
    'iot/task/monitor/index' AS component
FROM system_menu 
WHERE component = 'iot/task/monitor/index'
  AND deleted = b'0';

-- 2.9 物模型
SELECT 
    CASE 
        WHEN COUNT(*) > 0 THEN '✅ 已存在'
        ELSE '❌ 缺失'
    END AS status,
    '物模型' AS page_name,
    'iot/thingmodel/index' AS component
FROM system_menu 
WHERE component = 'iot/thingmodel/index'
  AND deleted = b'0';

-- 2.10 数据规则
SELECT 
    CASE 
        WHEN COUNT(*) > 0 THEN '✅ 已存在'
        ELSE '❌ 缺失'
    END AS status,
    '数据规则' AS page_name,
    'iot/rule/data/index' AS component
FROM system_menu 
WHERE component = 'iot/rule/data/index'
  AND deleted = b'0';

-- 2.11 空间平面图
SELECT 
    CASE 
        WHEN COUNT(*) > 0 THEN '✅ 已存在'
        ELSE '❌ 缺失'
    END AS status,
    '空间平面图' AS page_name,
    'iot/spatial/floorplan/index' AS component
FROM system_menu 
WHERE component = 'iot/spatial/floorplan/index'
  AND deleted = b'0';

-- ========================================
-- 3. 统计总览
-- ========================================
SELECT 
    '总菜单数' AS metric,
    COUNT(*) AS value
FROM system_menu 
WHERE component LIKE 'iot/%'
  AND type = 2
  AND deleted = b'0'

UNION ALL

SELECT 
    '需要检查的页面数' AS metric,
    11 AS value  -- 上面列出的页面数

UNION ALL

SELECT 
    '已配置页面数' AS metric,
    (
        (SELECT COUNT(*) FROM system_menu WHERE component = 'iot/device/discovery/index' AND deleted = b'0') +
        (SELECT COUNT(*) FROM system_menu WHERE component = 'iot/device/config/index' AND deleted = b'0') +
        (SELECT COUNT(*) FROM system_menu WHERE component = 'iot/device/control/index' AND deleted = b'0') +
        (SELECT COUNT(*) FROM system_menu WHERE component = 'iot/device/event/index' AND deleted = b'0') +
        (SELECT COUNT(*) FROM system_menu WHERE component = 'iot/video/preview/index' AND deleted = b'0') +
        (SELECT COUNT(*) FROM system_menu WHERE component = 'iot/video/playback/index' AND deleted = b'0') +
        (SELECT COUNT(*) FROM system_menu WHERE component = 'iot/gis/index' AND deleted = b'0') +
        (SELECT COUNT(*) FROM system_menu WHERE component = 'iot/task/monitor/index' AND deleted = b'0') +
        (SELECT COUNT(*) FROM system_menu WHERE component = 'iot/thingmodel/index' AND deleted = b'0') +
        (SELECT COUNT(*) FROM system_menu WHERE component = 'iot/rule/data/index' AND deleted = b'0') +
        (SELECT COUNT(*) FROM system_menu WHERE component = 'iot/spatial/floorplan/index' AND deleted = b'0')
    ) AS value;

-- ========================================
-- 4. 查询父菜单信息（用于添加新菜单时确定 parent_id）
-- ========================================
SELECT 
    id, 
    name, 
    path,
    component,
    type
FROM system_menu 
WHERE (
    name LIKE '%智慧物联%' 
    OR name LIKE '%IoT%'
    OR name LIKE '%设备%'
    OR name LIKE '%视频%'
    OR name LIKE '%空间%'
    OR name LIKE '%任务%'
    OR name LIKE '%规则%'
)
  AND type IN (1, 2)  -- 目录或菜单
  AND deleted = b'0'
ORDER BY type, sort;

-- ========================================
-- 使用说明
-- ========================================
-- 1. 执行第1部分：查看所有已有的IoT菜单
-- 2. 执行第2部分：检查具体页面是否有菜单（❌ 表示缺失）
-- 3. 执行第3部分：查看统计总览
-- 4. 执行第4部分：获取父菜单ID，用于添加新菜单
-- 5. 对于缺失的菜单，使用 iot_missing_pages_menu.sql 进行添加














