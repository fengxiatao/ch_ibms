-- ========================================
-- 检查和清理重复的视频巡更菜单
-- ========================================

-- 1. 查看所有"视频巡更"菜单
SELECT 
    id,
    name,
    parent_id,
    path,
    type,
    deleted,
    create_time
FROM system_menu 
WHERE name = '视频巡更'
ORDER BY id;

-- 2. 查看父菜单信息
SELECT 
    m1.id AS menu_id,
    m1.name AS menu_name,
    m1.parent_id,
    m2.name AS parent_name,
    m1.deleted
FROM system_menu m1
LEFT JOIN system_menu m2 ON m1.parent_id = m2.id
WHERE m1.name = '视频巡更';

-- 3. 删除重复的"视频巡更"菜单（保留最新的一个）
-- 注意：执行前请先查看上面的结果，确认要删除哪些

-- 方案A：如果要删除旧的，保留最新的
-- DELETE FROM system_menu 
-- WHERE name = '视频巡更' 
-- AND id NOT IN (
--     SELECT * FROM (
--         SELECT MAX(id) FROM system_menu WHERE name = '视频巡更' AND deleted = 0
--     ) AS tmp
-- );

-- 方案B：如果要删除所有，重新创建
-- UPDATE system_menu SET deleted = 1 WHERE name = '视频巡更';

-- 4. 查看视频巡更的子菜单
SELECT 
    m1.id AS parent_id,
    m1.name AS parent_name,
    m2.id AS menu_id,
    m2.name AS menu_name,
    m2.path,
    m2.component
FROM system_menu m1
LEFT JOIN system_menu m2 ON m2.parent_id = m1.id AND m2.deleted = 0
WHERE m1.name = '视频巡更' AND m1.deleted = 0
ORDER BY m1.id, m2.sort;

-- 5. 统计视频巡更相关菜单数量
SELECT 
    '视频巡更目录' AS type,
    COUNT(*) AS count
FROM system_menu 
WHERE name = '视频巡更' AND deleted = 0
UNION ALL
SELECT 
    '子菜单' AS type,
    COUNT(*) AS count
FROM system_menu 
WHERE parent_id IN (SELECT id FROM system_menu WHERE name = '视频巡更' AND deleted = 0)
AND deleted = 0;
