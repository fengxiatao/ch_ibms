-- =============================================
-- 智慧安防-视频监控-视频告警管理 菜单&权限
-- 说明：
-- 1) 不强依赖固定 ID（避免与历史初始化脚本冲突），优先按 name/path 定位父菜单
-- 2) 若不存在则在 65000+ 段创建（该段在本仓库未作为保留段使用）
-- 3) 使用“存在则更新 / 不存在则插入”的方式，支持重复执行
-- 4) 执行后如前端未生效，请清理 Redis 菜单缓存或重启
-- =============================================

-- ================
-- 0. 基础定位
-- ================
SET @security_root_id :=
  (SELECT id FROM system_menu WHERE deleted = b'0' AND type = 1 AND path = '/security' ORDER BY id DESC LIMIT 1);

-- 如果库里还没有“智慧安防(/security)”根目录，则在 65000 段创建一个（不影响原有系统的其它菜单）
INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id,
  path, icon, component, component_name,
  status, visible, keep_alive, always_show,
  creator, create_time, updater, update_time, deleted
)
SELECT * FROM (
  SELECT 65000, '智慧安防', '', 1, 50, 0,
         '/security', 'fa-solid:shield-alt', NULL, NULL,
         0, b'1', b'1', b'1',
         '1', NOW(), '1', NOW(), b'0'
) AS tmp
WHERE @security_root_id IS NULL;

SET @security_root_id :=
  (SELECT id FROM system_menu WHERE deleted = b'0' AND type = 1 AND path = '/security' ORDER BY id DESC LIMIT 1);

SET @video_surveillance_dir_id :=
  (SELECT id
   FROM system_menu
   WHERE deleted = b'0'
     AND parent_id = @security_root_id
     AND type = 1
     AND path = 'video-surveillance'
   ORDER BY id DESC
   LIMIT 1);

-- 如果缺少“视频监控”目录，则创建
INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id,
  path, icon, component, component_name,
  status, visible, keep_alive, always_show,
  creator, create_time, updater, update_time, deleted
)
SELECT * FROM (
  SELECT 65010, '视频监控', '', 1, 10, @security_root_id,
         'video-surveillance', 'fa:video-camera', NULL, NULL,
         0, b'1', b'1', b'1',
         '1', NOW(), '1', NOW(), b'0'
) AS tmp
WHERE @video_surveillance_dir_id IS NULL;

SET @video_surveillance_dir_id :=
  (SELECT id
   FROM system_menu
   WHERE deleted = b'0'
     AND parent_id = @security_root_id
     AND type = 1
     AND path = 'video-surveillance'
   ORDER BY id DESC
   LIMIT 1);

-- ================
-- 1. 菜单：视频告警管理
-- ================
SET @video_alarm_menu_id :=
  (SELECT id
   FROM system_menu
   WHERE deleted = b'0'
     AND parent_id = @video_surveillance_dir_id
     AND type = 2
     AND (path = 'video-alarm-record' OR component = 'security/VideoSurveillance/VideoAlarmRecord/index')
   ORDER BY id DESC
   LIMIT 1);

-- 不存在则插入；存在则更新名称/权限/组件等
INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id,
  path, icon, component, component_name,
  status, visible, keep_alive, always_show,
  creator, create_time, updater, update_time, deleted
)
SELECT * FROM (
  SELECT 65015, '视频告警管理', 'security:video:alarm', 2, 5, @video_surveillance_dir_id,
         'video-alarm-record', 'fa:bell', 'security/VideoSurveillance/VideoAlarmRecord/index', 'VideoAlarmRecord',
         0, b'1', b'1', b'1',
         '1', NOW(), '1', NOW(), b'0'
) AS tmp
WHERE @video_alarm_menu_id IS NULL;

UPDATE system_menu
SET name = '视频告警管理',
    permission = 'security:video:alarm',
    icon = 'fa:bell',
    path = 'video-alarm-record',
    component = 'security/VideoSurveillance/VideoAlarmRecord/index',
    component_name = 'VideoAlarmRecord',
    updater = '1',
    update_time = NOW()
WHERE id = @video_alarm_menu_id;

SET @video_alarm_menu_id :=
  (SELECT id
   FROM system_menu
   WHERE deleted = b'0'
     AND parent_id = @video_surveillance_dir_id
     AND type = 2
     AND (path = 'video-alarm-record' OR component = 'security/VideoSurveillance/VideoAlarmRecord/index')
   ORDER BY id DESC
   LIMIT 1);

-- ================
-- 2. 按钮权限（type=3）
-- ================
INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id,
  path, icon, component, component_name,
  status, visible, keep_alive, always_show,
  creator, create_time, updater, update_time, deleted
)
SELECT * FROM (
  SELECT 65115, '查询', 'security:video:alarm:query', 3, 1, @video_alarm_menu_id,
         '', '', '', NULL,
         0, b'1', b'1', b'1',
         '1', NOW(), '1', NOW(), b'0'
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM system_menu WHERE deleted=b'0' AND parent_id=@video_alarm_menu_id AND type=3 AND permission='security:video:alarm:query');

INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id,
  path, icon, component, component_name,
  status, visible, keep_alive, always_show,
  creator, create_time, updater, update_time, deleted
)
SELECT * FROM (
  SELECT 65116, '处理', 'security:video:alarm:handle', 3, 2, @video_alarm_menu_id,
         '', '', '', NULL,
         0, b'1', b'1', b'1',
         '1', NOW(), '1', NOW(), b'0'
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM system_menu WHERE deleted=b'0' AND parent_id=@video_alarm_menu_id AND type=3 AND permission='security:video:alarm:handle');

INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id,
  path, icon, component, component_name,
  status, visible, keep_alive, always_show,
  creator, create_time, updater, update_time, deleted
)
SELECT * FROM (
  SELECT 65117, '删除', 'security:video:alarm:delete', 3, 3, @video_alarm_menu_id,
         '', '', '', NULL,
         0, b'1', b'1', b'1',
         '1', NOW(), '1', NOW(), b'0'
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM system_menu WHERE deleted=b'0' AND parent_id=@video_alarm_menu_id AND type=3 AND permission='security:video:alarm:delete');

INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id,
  path, icon, component, component_name,
  status, visible, keep_alive, always_show,
  creator, create_time, updater, update_time, deleted
)
SELECT * FROM (
  SELECT 65118, '导出', 'security:video:alarm:export', 3, 4, @video_alarm_menu_id,
         '', '', '', NULL,
         0, b'1', b'1', b'1',
         '1', NOW(), '1', NOW(), b'0'
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM system_menu WHERE deleted=b'0' AND parent_id=@video_alarm_menu_id AND type=3 AND permission='security:video:alarm:export');

-- ================
-- 3. 验证
-- ================
SELECT id, name, parent_id, type, path, component, permission
FROM system_menu
WHERE deleted = b'0'
  AND (
    id IN (65000, 65010, 65015, 65115, 65116, 65117, 65118)
    OR (type IN (1,2) AND component = 'security/VideoSurveillance/VideoAlarmRecord/index')
    OR (type = 3 AND parent_id = @video_alarm_menu_id)
  )
ORDER BY id;

