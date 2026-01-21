-- =============================================
-- 检查现有智慧安防菜单
-- =============================================

-- 查询所有包含"智慧安防"、"安防"、"视频监控"、"人员布控"等关键词的菜单
SELECT 
    id,
    name,
    parent_id,
    path,
    component,
    permission,
    type,
    sort,
    deleted
FROM system_menu
WHERE (
    name LIKE '%智慧安防%' 
    OR name LIKE '%安防%'
    OR name LIKE '%视频监控%'
    OR name LIKE '%人员布控%'
    OR name LIKE '%视频分析%'
    OR name LIKE '%电子巡更%'
    OR name LIKE '%周界%'
    OR path LIKE '%security%'
    OR component LIKE '%security%'
    OR permission LIKE '%security%'
)
ORDER BY id, parent_id, sort;

-- 统计现有安防菜单数量
SELECT 
    '现有安防相关菜单' AS 类型,
    COUNT(*) AS 数量
FROM system_menu
WHERE (
    name LIKE '%安防%'
    OR path LIKE '%security%'
    OR component LIKE '%security%'
    OR permission LIKE '%security%'
)
AND deleted = 0;

-- 检查5000-5999范围内是否已有菜单
SELECT 
    '5000-5999范围内菜单' AS 类型,
    COUNT(*) AS 数量,
    MIN(id) AS 最小ID,
    MAX(id) AS 最大ID
FROM system_menu
WHERE id >= 5000 AND id < 6000;

-- 查询当前最大菜单ID
SELECT 
    '当前最大菜单ID' AS 类型,
    MAX(id) AS 最大ID
FROM system_menu;


















