-- ========================================
-- 视频巡更模块菜单配置 - 最终版
-- ========================================

-- 查找父菜单ID
SET @parent_id = COALESCE(
    (SELECT id FROM system_menu WHERE name LIKE '%IoT%' AND type = 1 AND deleted = 0 LIMIT 1),
    (SELECT id FROM system_menu WHERE name = '智慧安防' AND type = 1 AND deleted = 0 LIMIT 1)
);

-- 1. 创建视频巡更目录
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
VALUES ('视频巡更', '', 1, 60, @parent_id, 'VideoPatrol', 'ep:video-camera', NULL, 0, 1, 1, 1, '1', NOW(), '1', NOW(), 0);

SET @video_patrol_id = LAST_INSERT_ID();

-- 2. 创建子菜单
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
VALUES 
('巡更计划管理', '', 2, 1, @video_patrol_id, 'PatrolPlans', 'ep:calendar', 'security/VideoPatrol/PatrolPlans/index', 'VideoPatrolPlans', 0, 1, 1, 0, '1', NOW(), '1', NOW(), 0),
('巡更任务', '', 2, 2, @video_patrol_id, 'PatrolTasks', 'ep:list', 'security/VideoPatrol/PatrolTasks/index', 'VideoPatrolTasks', 0, 1, 1, 0, '1', NOW(), '1', NOW(), 0),
('任务管理', '', 2, 3, @video_patrol_id, 'PatrolTaskManagement', 'ep:setting', 'security/VideoPatrol/PatrolTaskManagement/index', 'VideoPatrolTaskManagement', 0, 1, 1, 0, '1', NOW(), '1', NOW(), 0),
('点位管理', '', 2, 4, @video_patrol_id, 'PatrolPointManagement', 'ep:location', 'security/VideoPatrol/PatrolPointManagement/index', 'VideoPatrolPointManagement', 0, 1, 1, 0, '1', NOW(), '1', NOW(), 0),
('记录查询', '', 2, 5, @video_patrol_id, 'PatrolRecordQuery', 'ep:document', 'security/VideoPatrol/PatrolRecordQuery/index', 'VideoPatrolRecordQuery', 0, 1, 1, 0, '1', NOW(), '1', NOW(), 0);

-- 获取子菜单ID
SET @plans_id = (SELECT id FROM system_menu WHERE name = '巡更计划管理' AND parent_id = @video_patrol_id LIMIT 1);
SET @tasks_id = (SELECT id FROM system_menu WHERE name = '巡更任务' AND parent_id = @video_patrol_id LIMIT 1);
SET @mgmt_id = (SELECT id FROM system_menu WHERE name = '任务管理' AND parent_id = @video_patrol_id LIMIT 1);
SET @point_id = (SELECT id FROM system_menu WHERE name = '点位管理' AND parent_id = @video_patrol_id LIMIT 1);
SET @record_id = (SELECT id FROM system_menu WHERE name = '记录查询' AND parent_id = @video_patrol_id LIMIT 1);

-- 3. 创建权限按钮
-- 3.1 巡更计划管理权限
INSERT INTO system_menu (name, permission, type, sort, parent_id, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
VALUES 
('巡更计划查询', 'iot:video-patrol-plan:query', 3, 1, @plans_id, 0, 0, 1, 0, '1', NOW(), '1', NOW(), 0),
('巡更计划创建', 'iot:video-patrol-plan:create', 3, 2, @plans_id, 0, 0, 1, 0, '1', NOW(), '1', NOW(), 0),
('巡更计划更新', 'iot:video-patrol-plan:update', 3, 3, @plans_id, 0, 0, 1, 0, '1', NOW(), '1', NOW(), 0),
('巡更计划删除', 'iot:video-patrol-plan:delete', 3, 4, @plans_id, 0, 0, 1, 0, '1', NOW(), '1', NOW(), 0),
('巡更计划导出', 'iot:video-patrol-plan:export', 3, 5, @plans_id, 0, 0, 1, 0, '1', NOW(), '1', NOW(), 0);

-- 3.2 巡更任务权限
INSERT INTO system_menu (name, permission, type, sort, parent_id, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
VALUES 
('巡更任务查询', 'iot:video-patrol-task:query', 3, 1, @tasks_id, 0, 0, 1, 0, '1', NOW(), '1', NOW(), 0),
('巡更任务执行', 'iot:video-patrol-task:execute', 3, 2, @tasks_id, 0, 0, 1, 0, '1', NOW(), '1', NOW(), 0),
('巡更任务导出', 'iot:video-patrol-task:export', 3, 3, @tasks_id, 0, 0, 1, 0, '1', NOW(), '1', NOW(), 0);

-- 3.3 任务管理权限
INSERT INTO system_menu (name, permission, type, sort, parent_id, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
VALUES 
('任务管理查询', 'iot:video-patrol-task-mgmt:query', 3, 1, @mgmt_id, 0, 0, 1, 0, '1', NOW(), '1', NOW(), 0),
('任务管理更新', 'iot:video-patrol-task-mgmt:update', 3, 2, @mgmt_id, 0, 0, 1, 0, '1', NOW(), '1', NOW(), 0),
('任务管理删除', 'iot:video-patrol-task-mgmt:delete', 3, 3, @mgmt_id, 0, 0, 1, 0, '1', NOW(), '1', NOW(), 0);

-- 3.4 点位管理权限
INSERT INTO system_menu (name, permission, type, sort, parent_id, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
VALUES 
('点位管理查询', 'iot:video-patrol-point:query', 3, 1, @point_id, 0, 0, 1, 0, '1', NOW(), '1', NOW(), 0),
('点位管理创建', 'iot:video-patrol-point:create', 3, 2, @point_id, 0, 0, 1, 0, '1', NOW(), '1', NOW(), 0),
('点位管理更新', 'iot:video-patrol-point:update', 3, 3, @point_id, 0, 0, 1, 0, '1', NOW(), '1', NOW(), 0),
('点位管理删除', 'iot:video-patrol-point:delete', 3, 4, @point_id, 0, 0, 1, 0, '1', NOW(), '1', NOW(), 0),
('点位管理导出', 'iot:video-patrol-point:export', 3, 5, @point_id, 0, 0, 1, 0, '1', NOW(), '1', NOW(), 0);

-- 3.5 记录查询权限
INSERT INTO system_menu (name, permission, type, sort, parent_id, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
VALUES 
('记录查询查询', 'iot:video-patrol-record:query', 3, 1, @record_id, 0, 0, 1, 0, '1', NOW(), '1', NOW(), 0),
('记录查询导出', 'iot:video-patrol-record:export', 3, 2, @record_id, 0, 0, 1, 0, '1', NOW(), '1', NOW(), 0),
('记录查询确认', 'iot:video-patrol-record:confirm', 3, 3, @record_id, 0, 0, 1, 0, '1', NOW(), '1', NOW(), 0),
('记录查询处理', 'iot:video-patrol-record:handle', 3, 4, @record_id, 0, 0, 1, 0, '1', NOW(), '1', NOW(), 0);

-- 4. 为管理员角色分配权限
INSERT IGNORE INTO system_role_menu (role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id)
SELECT 1, id, '1', NOW(), '1', NOW(), 0, 1
FROM system_menu
WHERE (id = @video_patrol_id OR parent_id = @video_patrol_id OR parent_id IN (@plans_id, @tasks_id, @mgmt_id, @point_id, @record_id))
AND deleted = 0;

-- 完成
SELECT 'Video Patrol Menu Deployment Completed!' AS Status;
