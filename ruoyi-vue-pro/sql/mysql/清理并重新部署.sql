-- ========================================
-- 清理旧的视频巡更菜单并重新部署
-- ========================================

-- 1. 删除所有视频巡更相关的菜单（包括子菜单和权限）
-- 先删除权限（type=3）
DELETE FROM system_menu 
WHERE parent_id IN (
    SELECT id FROM (
        SELECT id FROM system_menu 
        WHERE parent_id IN (
            SELECT id FROM system_menu WHERE name = '视频巡更'
        )
    ) AS tmp
) AND type = 3;

-- 删除子菜单（type=2）
DELETE FROM system_menu 
WHERE parent_id IN (
    SELECT id FROM (
        SELECT id FROM system_menu WHERE name = '视频巡更'
    ) AS tmp
);

-- 删除视频巡更目录（type=1）
DELETE FROM system_menu WHERE name = '视频巡更';

-- 2. 删除角色菜单关联
DELETE FROM system_role_menu 
WHERE menu_id NOT IN (SELECT id FROM system_menu);

-- 3. 验证清理结果
SELECT '清理完成，以下应该为空：' AS message;
SELECT * FROM system_menu WHERE name LIKE '%视频巡更%' OR name LIKE '%巡更计划%' OR name LIKE '%巡更任务%';

-- 4. 现在可以重新执行 video_patrol_menu_permissions.sql
SELECT '请执行: SOURCE video_patrol_menu_permissions.sql;' AS next_step;
