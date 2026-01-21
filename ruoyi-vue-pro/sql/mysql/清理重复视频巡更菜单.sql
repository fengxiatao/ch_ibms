-- ========================================
-- 清理重复的视频巡更菜单
-- ========================================

-- 查看当前的重复记录
SELECT id, name, path, parent_id, create_time 
FROM system_menu 
WHERE name = '视频巡更' AND deleted = 0
ORDER BY id;

-- 删除这3条视频巡更记录及其所有子菜单和权限

-- 1. 删除ID为6117的视频巡更及其子菜单
DELETE FROM system_menu WHERE id = 6117 OR parent_id = 6117;
DELETE FROM system_menu WHERE parent_id IN (
    SELECT id FROM (SELECT id FROM system_menu WHERE parent_id = 6117) AS tmp
);

-- 2. 删除ID为6118的视频巡更及其子菜单
DELETE FROM system_menu WHERE id = 6118 OR parent_id = 6118;
DELETE FROM system_menu WHERE parent_id IN (
    SELECT id FROM (SELECT id FROM system_menu WHERE parent_id = 6118) AS tmp
);

-- 3. 删除ID为6144的视频巡更及其子菜单
DELETE FROM system_menu WHERE id = 6144 OR parent_id = 6144;
DELETE FROM system_menu WHERE parent_id IN (
    SELECT id FROM (SELECT id FROM system_menu WHERE parent_id = 6144) AS tmp
);

-- 4. 清理无效的角色菜单关联
DELETE FROM system_role_menu WHERE menu_id NOT IN (SELECT id FROM system_menu);

-- 5. 验证清理结果（应该为空）
SELECT '清理完成，以下应该为空：' AS message;
SELECT * FROM system_menu WHERE name = '视频巡更' AND deleted = 0;

SELECT '现在可以执行: SOURCE video_patrol_menu_final.sql;' AS next_step;
