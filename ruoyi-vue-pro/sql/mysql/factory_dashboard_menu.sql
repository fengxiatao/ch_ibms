-- =============================================
-- 智慧工厂菜单重构脚本（动态路由）
-- 目标结构：
-- - 智慧工厂
--   - 驾驶舱
--   - 告警管理
--   - 视频融合
--   - 立体化云防
--   - 业务协同
--   - 合规管理
--   - 环保监测
--   - 报表中心
--   - 品牌展示
-- 对应前端页面：
-- - yudao-ui-admin-vue3/src/views/factory/cockpit/index.vue
-- - yudao-ui-admin-vue3/src/views/factory/alarm/index.vue
-- - yudao-ui-admin-vue3/src/views/factory/videoFusion/index.vue
-- - yudao-ui-admin-vue3/src/views/factory/cloudDefense/index.vue
-- - yudao-ui-admin-vue3/src/views/factory/collaboration/index.vue
-- - yudao-ui-admin-vue3/src/views/factory/compliance/index.vue
-- - yudao-ui-admin-vue3/src/views/factory/environmental/index.vue
-- - yudao-ui-admin-vue3/src/views/factory/report/index.vue
-- - yudao-ui-admin-vue3/src/views/factory/brand/index.vue
--
-- 说明：
-- 1) 幂等：可重复执行（存在则更新，不存在则新增）
-- 2) 不强依赖固定主键，优先按 path/component 查找
-- 3) 执行后如前端未生效，请清理菜单缓存或重新登录
-- 4) Cursor 的 mysql-mcp-server 工具通常一次只接受单条 SQL；若通过 MCP 执行，请用 mysql 客户端
--    一次跑完整脚本，或按语句拆分（会话变量需在同一连接内连续执行）
-- =============================================

-- ================
-- 0. 根目录：/factory（智慧工厂）
-- ================
SET @factory_root_id :=
  (SELECT id
   FROM system_menu
   WHERE deleted = b'0'
     AND type = 1
     AND path = '/factory'
   ORDER BY id DESC
   LIMIT 1);

INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id,
  path, icon, component, component_name,
  status, visible, keep_alive, always_show,
  creator, create_time, updater, update_time, deleted
)
SELECT * FROM (
  SELECT 65300, '智慧工厂', '', 1, 60, 0,
         '/factory', 'ep:office-building', NULL, NULL,
         0, b'1', b'1', b'1',
         '1', NOW(), '1', NOW(), b'0'
) AS tmp
WHERE @factory_root_id IS NULL;

SET @factory_root_id :=
  (SELECT id
   FROM system_menu
   WHERE deleted = b'0'
     AND type = 1
     AND path = '/factory'
   ORDER BY id DESC
   LIMIT 1);

-- ================
-- 1. 一级业务菜单（type=2）
-- ================
SET @factory_cockpit_menu_id :=
  (SELECT id
   FROM system_menu
   WHERE deleted = b'0'
     AND parent_id = @factory_root_id
     AND (
       permission = 'factory:cockpit'
       OR path = 'cockpit'
       OR component = 'factory/cockpit/index'
       OR path = 'dashboard'
     )
   ORDER BY id DESC
   LIMIT 1);

INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id,
  path, icon, component, component_name,
  status, visible, keep_alive, always_show,
  creator, create_time, updater, update_time, deleted
)
SELECT * FROM (
  SELECT 65310, '驾驶舱', 'factory:cockpit', 2, 10, @factory_root_id,
         'cockpit', 'ep:monitor', 'factory/cockpit/index', 'FactoryCockpit',
         0, b'1', b'1', b'1',
         '1', NOW(), '1', NOW(), b'0'
) AS tmp
WHERE @factory_cockpit_menu_id IS NULL;

SET @factory_cockpit_menu_id :=
  (SELECT id
   FROM system_menu
   WHERE deleted = b'0'
     AND parent_id = @factory_root_id
     AND (
       permission = 'factory:cockpit'
       OR path = 'cockpit'
       OR component = 'factory/cockpit/index'
       OR path = 'dashboard'
     )
   ORDER BY id DESC
   LIMIT 1);

UPDATE system_menu
SET name = '驾驶舱',
    permission = 'factory:cockpit',
    type = 2,
    sort = 10,
    parent_id = @factory_root_id,
    path = 'cockpit',
    icon = 'ep:monitor',
    component = 'factory/cockpit/index',
    component_name = 'FactoryCockpit',
    status = 0,
    visible = b'1',
    keep_alive = b'1',
    always_show = b'1',
    updater = '1',
    update_time = NOW()
WHERE id = @factory_cockpit_menu_id;

UPDATE system_menu
SET deleted = b'1',
    updater = '1',
    update_time = NOW()
WHERE deleted = b'0'
  AND id <> @factory_cockpit_menu_id
  AND parent_id = @factory_root_id
  AND (
    path = 'dashboard'
    OR permission = 'factory:cockpit'
    OR component = 'factory/cockpit/index'
  );

SET @factory_alarm_menu_id :=
  (SELECT id
   FROM system_menu
   WHERE deleted = b'0'
     AND parent_id = @factory_root_id
     AND (
       permission = 'factory:alarm'
       OR path = 'alarm'
       OR component = 'factory/alarm/index'
     )
   ORDER BY id DESC
   LIMIT 1);

INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id,
  path, icon, component, component_name,
  status, visible, keep_alive, always_show,
  creator, create_time, updater, update_time, deleted
)
SELECT * FROM (
  SELECT 65341, '告警管理', 'factory:alarm', 2, 20, @factory_root_id,
         'alarm', 'ep:bell', 'factory/alarm/index', 'FactoryAlarm',
         0, b'1', b'1', b'1',
         '1', NOW(), '1', NOW(), b'0'
) AS tmp
WHERE @factory_alarm_menu_id IS NULL;

SET @factory_alarm_menu_id :=
  (SELECT id
   FROM system_menu
   WHERE deleted = b'0'
     AND parent_id = @factory_root_id
     AND (
       permission = 'factory:alarm'
       OR path = 'alarm'
       OR component = 'factory/alarm/index'
     )
   ORDER BY id DESC
   LIMIT 1);

UPDATE system_menu
SET name = '告警管理',
    permission = 'factory:alarm',
    type = 2,
    sort = 20,
    parent_id = @factory_root_id,
    path = 'alarm',
    icon = 'ep:bell',
    component = 'factory/alarm/index',
    component_name = 'FactoryAlarm',
    status = 0,
    visible = b'1',
    keep_alive = b'1',
    always_show = b'1',
    updater = '1',
    update_time = NOW()
WHERE id = @factory_alarm_menu_id;

SET @factory_video_fusion_menu_id :=
  (SELECT id
   FROM system_menu
   WHERE deleted = b'0'
     AND (
       permission = 'factory:video-fusion'
       OR path = 'video-fusion'
       OR component = 'factory/videoFusion/index'
       OR permission = 'factory:dashboard:video'
       OR path = 'video'
       OR component = 'factory/dashboard/Video/index'
     )
   ORDER BY id DESC
   LIMIT 1);

INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id,
  path, icon, component, component_name,
  status, visible, keep_alive, always_show,
  creator, create_time, updater, update_time, deleted
)
SELECT * FROM (
  SELECT 65316, '视频融合', 'factory:video-fusion', 2, 30, @factory_root_id,
         'video-fusion', 'ep:video-camera', 'factory/videoFusion/index', 'FactoryVideoFusion',
         0, b'1', b'1', b'1',
         '1', NOW(), '1', NOW(), b'0'
) AS tmp
WHERE @factory_video_fusion_menu_id IS NULL;

SET @factory_video_fusion_menu_id :=
  (SELECT id
   FROM system_menu
   WHERE deleted = b'0'
     AND (
       permission = 'factory:video-fusion'
       OR path = 'video-fusion'
       OR component = 'factory/videoFusion/index'
       OR permission = 'factory:dashboard:video'
       OR path = 'video'
       OR component = 'factory/dashboard/Video/index'
     )
   ORDER BY id DESC
   LIMIT 1);

UPDATE system_menu
SET name = '视频融合',
    permission = 'factory:video-fusion',
    type = 2,
    sort = 30,
    parent_id = @factory_root_id,
    path = 'video-fusion',
    icon = 'ep:video-camera',
    component = 'factory/videoFusion/index',
    component_name = 'FactoryVideoFusion',
    status = 0,
    visible = b'1',
    keep_alive = b'1',
    always_show = b'1',
    updater = '1',
    update_time = NOW()
WHERE id = @factory_video_fusion_menu_id;

UPDATE system_menu
SET deleted = b'1',
    updater = '1',
    update_time = NOW()
WHERE deleted = b'0'
  AND id <> @factory_video_fusion_menu_id
  AND (
    permission IN ('factory:video-fusion', 'factory:dashboard:video')
    OR path IN ('video-fusion', 'video')
    OR component IN ('factory/videoFusion/index', 'factory/dashboard/Video/index')
  );

SET @factory_cloud_defense_menu_id :=
  (SELECT id
   FROM system_menu
   WHERE deleted = b'0'
     AND parent_id = @factory_root_id
     AND (
       permission = 'factory:cloud-defense'
       OR path = 'cloud-defense'
       OR component = 'factory/cloudDefense/index'
     )
   ORDER BY id DESC
   LIMIT 1);

INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id,
  path, icon, component, component_name,
  status, visible, keep_alive, always_show,
  creator, create_time, updater, update_time, deleted
)
SELECT * FROM (
  SELECT 65342, '立体化云防', 'factory:cloud-defense', 2, 40, @factory_root_id,
         'cloud-defense', 'ep:shield', 'factory/cloudDefense/index', 'FactoryCloudDefense',
         0, b'1', b'1', b'1',
         '1', NOW(), '1', NOW(), b'0'
) AS tmp
WHERE @factory_cloud_defense_menu_id IS NULL;

SET @factory_cloud_defense_menu_id :=
  (SELECT id
   FROM system_menu
   WHERE deleted = b'0'
     AND parent_id = @factory_root_id
     AND (
       permission = 'factory:cloud-defense'
       OR path = 'cloud-defense'
       OR component = 'factory/cloudDefense/index'
     )
   ORDER BY id DESC
   LIMIT 1);

UPDATE system_menu
SET name = '立体化云防',
    permission = 'factory:cloud-defense',
    type = 2,
    sort = 40,
    parent_id = @factory_root_id,
    path = 'cloud-defense',
    icon = 'ep:shield',
    component = 'factory/cloudDefense/index',
    component_name = 'FactoryCloudDefense',
    status = 0,
    visible = b'1',
    keep_alive = b'1',
    always_show = b'1',
    updater = '1',
    update_time = NOW()
WHERE id = @factory_cloud_defense_menu_id;

SET @factory_collaboration_menu_id :=
  (SELECT id
   FROM system_menu
   WHERE deleted = b'0'
     AND parent_id = @factory_root_id
     AND (
       permission = 'factory:collaboration'
       OR path = 'collaboration'
       OR component = 'factory/collaboration/index'
     )
   ORDER BY id DESC
   LIMIT 1);

INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id,
  path, icon, component, component_name,
  status, visible, keep_alive, always_show,
  creator, create_time, updater, update_time, deleted
)
SELECT * FROM (
  SELECT 65343, '业务协同', 'factory:collaboration', 2, 50, @factory_root_id,
         'collaboration', 'ep:histogram', 'factory/collaboration/index', 'FactoryCollaboration',
         0, b'1', b'1', b'1',
         '1', NOW(), '1', NOW(), b'0'
) AS tmp
WHERE @factory_collaboration_menu_id IS NULL;

SET @factory_collaboration_menu_id :=
  (SELECT id
   FROM system_menu
   WHERE deleted = b'0'
     AND parent_id = @factory_root_id
     AND (
       permission = 'factory:collaboration'
       OR path = 'collaboration'
       OR component = 'factory/collaboration/index'
     )
   ORDER BY id DESC
   LIMIT 1);

UPDATE system_menu
SET name = '业务协同',
    permission = 'factory:collaboration',
    type = 2,
    sort = 50,
    parent_id = @factory_root_id,
    path = 'collaboration',
    icon = 'ep:histogram',
    component = 'factory/collaboration/index',
    component_name = 'FactoryCollaboration',
    status = 0,
    visible = b'1',
    keep_alive = b'1',
    always_show = b'1',
    updater = '1',
    update_time = NOW()
WHERE id = @factory_collaboration_menu_id;

SET @factory_compliance_menu_id :=
  (SELECT id
   FROM system_menu
   WHERE deleted = b'0'
     AND parent_id = @factory_root_id
     AND (
       permission = 'factory:compliance'
       OR path = 'compliance'
       OR component = 'factory/compliance/index'
     )
   ORDER BY id DESC
   LIMIT 1);

INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id,
  path, icon, component, component_name,
  status, visible, keep_alive, always_show,
  creator, create_time, updater, update_time, deleted
)
SELECT * FROM (
  SELECT 65344, '合规管理', 'factory:compliance', 2, 60, @factory_root_id,
         'compliance', 'ep:document-checked', 'factory/compliance/index', 'FactoryCompliance',
         0, b'1', b'1', b'1',
         '1', NOW(), '1', NOW(), b'0'
) AS tmp
WHERE @factory_compliance_menu_id IS NULL;

SET @factory_compliance_menu_id :=
  (SELECT id
   FROM system_menu
   WHERE deleted = b'0'
     AND parent_id = @factory_root_id
     AND (
       permission = 'factory:compliance'
       OR path = 'compliance'
       OR component = 'factory/compliance/index'
     )
   ORDER BY id DESC
   LIMIT 1);

UPDATE system_menu
SET name = '合规管理',
    permission = 'factory:compliance',
    type = 2,
    sort = 60,
    parent_id = @factory_root_id,
    path = 'compliance',
    icon = 'ep:document-checked',
    component = 'factory/compliance/index',
    component_name = 'FactoryCompliance',
    status = 0,
    visible = b'1',
    keep_alive = b'1',
    always_show = b'1',
    updater = '1',
    update_time = NOW()
WHERE id = @factory_compliance_menu_id;

SET @factory_environmental_menu_id :=
  (SELECT id
   FROM system_menu
   WHERE deleted = b'0'
     AND parent_id = @factory_root_id
     AND (
       permission = 'factory:environmental'
       OR path = 'environmental'
       OR component = 'factory/environmental/index'
     )
   ORDER BY id DESC
   LIMIT 1);

INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id,
  path, icon, component, component_name,
  status, visible, keep_alive, always_show,
  creator, create_time, updater, update_time, deleted
)
SELECT * FROM (
  SELECT 65345, '环保监测', 'factory:environmental', 2, 70, @factory_root_id,
         'environmental', 'ep:cloudy', 'factory/environmental/index', 'FactoryEnvironmental',
         0, b'1', b'1', b'1',
         '1', NOW(), '1', NOW(), b'0'
) AS tmp
WHERE @factory_environmental_menu_id IS NULL;

SET @factory_environmental_menu_id :=
  (SELECT id
   FROM system_menu
   WHERE deleted = b'0'
     AND parent_id = @factory_root_id
     AND (
       permission = 'factory:environmental'
       OR path = 'environmental'
       OR component = 'factory/environmental/index'
     )
   ORDER BY id DESC
   LIMIT 1);

UPDATE system_menu
SET name = '环保监测',
    permission = 'factory:environmental',
    type = 2,
    sort = 70,
    parent_id = @factory_root_id,
    path = 'environmental',
    icon = 'ep:cloudy',
    component = 'factory/environmental/index',
    component_name = 'FactoryEnvironmental',
    status = 0,
    visible = b'1',
    keep_alive = b'1',
    always_show = b'1',
    updater = '1',
    update_time = NOW()
WHERE id = @factory_environmental_menu_id;

SET @factory_report_menu_id :=
  (SELECT id
   FROM system_menu
   WHERE deleted = b'0'
     AND parent_id = @factory_root_id
     AND (
       permission = 'factory:report'
       OR path = 'report'
       OR component = 'factory/report/index'
     )
   ORDER BY id DESC
   LIMIT 1);

INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id,
  path, icon, component, component_name,
  status, visible, keep_alive, always_show,
  creator, create_time, updater, update_time, deleted
)
SELECT * FROM (
  SELECT 65346, '报表中心', 'factory:report', 2, 80, @factory_root_id,
         'report', 'ep:document', 'factory/report/index', 'FactoryReport',
         0, b'1', b'1', b'1',
         '1', NOW(), '1', NOW(), b'0'
) AS tmp
WHERE @factory_report_menu_id IS NULL;

SET @factory_report_menu_id :=
  (SELECT id
   FROM system_menu
   WHERE deleted = b'0'
     AND parent_id = @factory_root_id
     AND (
       permission = 'factory:report'
       OR path = 'report'
       OR component = 'factory/report/index'
     )
   ORDER BY id DESC
   LIMIT 1);

UPDATE system_menu
SET name = '报表中心',
    permission = 'factory:report',
    type = 2,
    sort = 80,
    parent_id = @factory_root_id,
    path = 'report',
    icon = 'ep:document',
    component = 'factory/report/index',
    component_name = 'FactoryReport',
    status = 0,
    visible = b'1',
    keep_alive = b'1',
    always_show = b'1',
    updater = '1',
    update_time = NOW()
WHERE id = @factory_report_menu_id;

SET @factory_brand_menu_id :=
  (SELECT id
   FROM system_menu
   WHERE deleted = b'0'
     AND parent_id = @factory_root_id
     AND (
       permission = 'factory:brand'
       OR path = 'brand'
       OR component = 'factory/brand/index'
     )
   ORDER BY id DESC
   LIMIT 1);

INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id,
  path, icon, component, component_name,
  status, visible, keep_alive, always_show,
  creator, create_time, updater, update_time, deleted
)
SELECT * FROM (
  SELECT 65347, '品牌展示', 'factory:brand', 2, 90, @factory_root_id,
         'brand', 'ep:view', 'factory/brand/index', 'FactoryBrand',
         0, b'1', b'1', b'1',
         '1', NOW(), '1', NOW(), b'0'
) AS tmp
WHERE @factory_brand_menu_id IS NULL;

SET @factory_brand_menu_id :=
  (SELECT id
   FROM system_menu
   WHERE deleted = b'0'
     AND parent_id = @factory_root_id
     AND (
       permission = 'factory:brand'
       OR path = 'brand'
       OR component = 'factory/brand/index'
     )
   ORDER BY id DESC
   LIMIT 1);

UPDATE system_menu
SET name = '品牌展示',
    permission = 'factory:brand',
    type = 2,
    sort = 90,
    parent_id = @factory_root_id,
    path = 'brand',
    icon = 'ep:view',
    component = 'factory/brand/index',
    component_name = 'FactoryBrand',
    status = 0,
    visible = b'1',
    keep_alive = b'1',
    always_show = b'1',
    updater = '1',
    update_time = NOW()
WHERE id = @factory_brand_menu_id;

-- ================
-- 2. 清理旧驾驶舱二级菜单与按钮
-- ================
UPDATE system_menu
SET deleted = b'1',
    updater = '1',
    update_time = NOW()
WHERE deleted = b'0'
  AND (
    permission IN (
      'factory:dashboard:overview',
      'factory:dashboard:production',
      'factory:dashboard:environment',
      'factory:dashboard:safety',
      'factory:dashboard:energy'
    )
    OR component IN (
      'factory/dashboard/Overview/index',
      'factory/dashboard/Production/index',
      'factory/dashboard/Environment/index',
      'factory/dashboard/Safety/index',
      'factory/dashboard/Energy/index'
    )
    OR permission IN (
      'factory:dashboard:overview:query',
      'factory:dashboard:production:query',
      'factory:dashboard:environment:query',
      'factory:dashboard:safety:query',
      'factory:dashboard:energy:query'
    )
  );

-- ================
-- 3. 按钮权限（type=3）
-- ================
SET @factory_cockpit_query_button_id :=
  (SELECT id
   FROM system_menu
   WHERE deleted = b'0'
     AND type = 3
     AND (
       permission = 'factory:cockpit:query'
       OR permission = 'factory:dashboard:overview:query'
     )
   ORDER BY id DESC
   LIMIT 1);

INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id,
  path, icon, component, component_name,
  status, visible, keep_alive, always_show,
  creator, create_time, updater, update_time, deleted
)
SELECT * FROM (
  SELECT 65321, '查看', 'factory:cockpit:query', 3, 1, @factory_cockpit_menu_id,
         '', '', '', NULL,
         0, b'1', b'1', b'1',
         '1', NOW(), '1', NOW(), b'0'
) AS tmp
WHERE @factory_cockpit_query_button_id IS NULL;

SET @factory_cockpit_query_button_id :=
  (SELECT id
   FROM system_menu
   WHERE deleted = b'0'
     AND type = 3
     AND (
       permission = 'factory:cockpit:query'
       OR permission = 'factory:dashboard:overview:query'
     )
   ORDER BY id DESC
   LIMIT 1);

UPDATE system_menu
SET name = '查看',
    permission = 'factory:cockpit:query',
    type = 3,
    sort = 1,
    parent_id = @factory_cockpit_menu_id,
    path = '',
    icon = '',
    component = '',
    component_name = NULL,
    status = 0,
    visible = b'1',
    keep_alive = b'1',
    always_show = b'1',
    deleted = b'0',
    updater = '1',
    update_time = NOW()
WHERE id = @factory_cockpit_query_button_id;

SET @factory_alarm_query_button_id :=
  (SELECT id
   FROM system_menu
   WHERE deleted = b'0'
     AND type = 3
     AND permission = 'factory:alarm:query'
   ORDER BY id DESC
   LIMIT 1);

INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id,
  path, icon, component, component_name,
  status, visible, keep_alive, always_show,
  creator, create_time, updater, update_time, deleted
)
SELECT * FROM (
  SELECT 65351, '查看', 'factory:alarm:query', 3, 1, @factory_alarm_menu_id,
         '', '', '', NULL,
         0, b'1', b'1', b'1',
         '1', NOW(), '1', NOW(), b'0'
) AS tmp
WHERE @factory_alarm_query_button_id IS NULL;

SET @factory_alarm_query_button_id :=
  (SELECT id
   FROM system_menu
   WHERE deleted = b'0'
     AND type = 3
     AND permission = 'factory:alarm:query'
   ORDER BY id DESC
   LIMIT 1);

UPDATE system_menu
SET name = '查看',
    permission = 'factory:alarm:query',
    type = 3,
    sort = 1,
    parent_id = @factory_alarm_menu_id,
    path = '',
    icon = '',
    component = '',
    component_name = NULL,
    status = 0,
    visible = b'1',
    keep_alive = b'1',
    always_show = b'1',
    updater = '1',
    update_time = NOW()
WHERE id = @factory_alarm_query_button_id;

SET @factory_video_fusion_query_button_id :=
  (SELECT id
   FROM system_menu
   WHERE deleted = b'0'
     AND type = 3
     AND (
       permission = 'factory:video-fusion:query'
       OR permission = 'factory:dashboard:video:query'
     )
   ORDER BY id DESC
   LIMIT 1);

INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id,
  path, icon, component, component_name,
  status, visible, keep_alive, always_show,
  creator, create_time, updater, update_time, deleted
)
SELECT * FROM (
  SELECT 65326, '查看', 'factory:video-fusion:query', 3, 1, @factory_video_fusion_menu_id,
         '', '', '', NULL,
         0, b'1', b'1', b'1',
         '1', NOW(), '1', NOW(), b'0'
) AS tmp
WHERE @factory_video_fusion_query_button_id IS NULL;

SET @factory_video_fusion_query_button_id :=
  (SELECT id
   FROM system_menu
   WHERE deleted = b'0'
     AND type = 3
     AND (
       permission = 'factory:video-fusion:query'
       OR permission = 'factory:dashboard:video:query'
     )
   ORDER BY id DESC
   LIMIT 1);

UPDATE system_menu
SET name = '查看',
    permission = 'factory:video-fusion:query',
    type = 3,
    sort = 1,
    parent_id = @factory_video_fusion_menu_id,
    path = '',
    icon = '',
    component = '',
    component_name = NULL,
    status = 0,
    visible = b'1',
    keep_alive = b'1',
    always_show = b'1',
    deleted = b'0',
    updater = '1',
    update_time = NOW()
WHERE id = @factory_video_fusion_query_button_id;

SET @factory_cloud_defense_query_button_id :=
  (SELECT id
   FROM system_menu
   WHERE deleted = b'0'
     AND type = 3
     AND permission = 'factory:cloud-defense:query'
   ORDER BY id DESC
   LIMIT 1);

INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id,
  path, icon, component, component_name,
  status, visible, keep_alive, always_show,
  creator, create_time, updater, update_time, deleted
)
SELECT * FROM (
  SELECT 65352, '查看', 'factory:cloud-defense:query', 3, 1, @factory_cloud_defense_menu_id,
         '', '', '', NULL,
         0, b'1', b'1', b'1',
         '1', NOW(), '1', NOW(), b'0'
) AS tmp
WHERE @factory_cloud_defense_query_button_id IS NULL;

SET @factory_cloud_defense_query_button_id :=
  (SELECT id
   FROM system_menu
   WHERE deleted = b'0'
     AND type = 3
     AND permission = 'factory:cloud-defense:query'
   ORDER BY id DESC
   LIMIT 1);

UPDATE system_menu
SET name = '查看',
    permission = 'factory:cloud-defense:query',
    type = 3,
    sort = 1,
    parent_id = @factory_cloud_defense_menu_id,
    path = '',
    icon = '',
    component = '',
    component_name = NULL,
    status = 0,
    visible = b'1',
    keep_alive = b'1',
    always_show = b'1',
    updater = '1',
    update_time = NOW()
WHERE id = @factory_cloud_defense_query_button_id;

SET @factory_collaboration_query_button_id :=
  (SELECT id
   FROM system_menu
   WHERE deleted = b'0'
     AND type = 3
     AND permission = 'factory:collaboration:query'
   ORDER BY id DESC
   LIMIT 1);

INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id,
  path, icon, component, component_name,
  status, visible, keep_alive, always_show,
  creator, create_time, updater, update_time, deleted
)
SELECT * FROM (
  SELECT 65353, '查看', 'factory:collaboration:query', 3, 1, @factory_collaboration_menu_id,
         '', '', '', NULL,
         0, b'1', b'1', b'1',
         '1', NOW(), '1', NOW(), b'0'
) AS tmp
WHERE @factory_collaboration_query_button_id IS NULL;

SET @factory_collaboration_query_button_id :=
  (SELECT id
   FROM system_menu
   WHERE deleted = b'0'
     AND type = 3
     AND permission = 'factory:collaboration:query'
   ORDER BY id DESC
   LIMIT 1);

UPDATE system_menu
SET name = '查看',
    permission = 'factory:collaboration:query',
    type = 3,
    sort = 1,
    parent_id = @factory_collaboration_menu_id,
    path = '',
    icon = '',
    component = '',
    component_name = NULL,
    status = 0,
    visible = b'1',
    keep_alive = b'1',
    always_show = b'1',
    updater = '1',
    update_time = NOW()
WHERE id = @factory_collaboration_query_button_id;

SET @factory_compliance_query_button_id :=
  (SELECT id
   FROM system_menu
   WHERE deleted = b'0'
     AND type = 3
     AND permission = 'factory:compliance:query'
   ORDER BY id DESC
   LIMIT 1);

INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id,
  path, icon, component, component_name,
  status, visible, keep_alive, always_show,
  creator, create_time, updater, update_time, deleted
)
SELECT * FROM (
  SELECT 65354, '查看', 'factory:compliance:query', 3, 1, @factory_compliance_menu_id,
         '', '', '', NULL,
         0, b'1', b'1', b'1',
         '1', NOW(), '1', NOW(), b'0'
) AS tmp
WHERE @factory_compliance_query_button_id IS NULL;

SET @factory_compliance_query_button_id :=
  (SELECT id
   FROM system_menu
   WHERE deleted = b'0'
     AND type = 3
     AND permission = 'factory:compliance:query'
   ORDER BY id DESC
   LIMIT 1);

UPDATE system_menu
SET name = '查看',
    permission = 'factory:compliance:query',
    type = 3,
    sort = 1,
    parent_id = @factory_compliance_menu_id,
    path = '',
    icon = '',
    component = '',
    component_name = NULL,
    status = 0,
    visible = b'1',
    keep_alive = b'1',
    always_show = b'1',
    updater = '1',
    update_time = NOW()
WHERE id = @factory_compliance_query_button_id;

SET @factory_environmental_query_button_id :=
  (SELECT id
   FROM system_menu
   WHERE deleted = b'0'
     AND type = 3
     AND permission = 'factory:environmental:query'
   ORDER BY id DESC
   LIMIT 1);

INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id,
  path, icon, component, component_name,
  status, visible, keep_alive, always_show,
  creator, create_time, updater, update_time, deleted
)
SELECT * FROM (
  SELECT 65355, '查看', 'factory:environmental:query', 3, 1, @factory_environmental_menu_id,
         '', '', '', NULL,
         0, b'1', b'1', b'1',
         '1', NOW(), '1', NOW(), b'0'
) AS tmp
WHERE @factory_environmental_query_button_id IS NULL;

SET @factory_environmental_query_button_id :=
  (SELECT id
   FROM system_menu
   WHERE deleted = b'0'
     AND type = 3
     AND permission = 'factory:environmental:query'
   ORDER BY id DESC
   LIMIT 1);

UPDATE system_menu
SET name = '查看',
    permission = 'factory:environmental:query',
    type = 3,
    sort = 1,
    parent_id = @factory_environmental_menu_id,
    path = '',
    icon = '',
    component = '',
    component_name = NULL,
    status = 0,
    visible = b'1',
    keep_alive = b'1',
    always_show = b'1',
    updater = '1',
    update_time = NOW()
WHERE id = @factory_environmental_query_button_id;

SET @factory_report_query_button_id :=
  (SELECT id
   FROM system_menu
   WHERE deleted = b'0'
     AND type = 3
     AND permission = 'factory:report:query'
   ORDER BY id DESC
   LIMIT 1);

INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id,
  path, icon, component, component_name,
  status, visible, keep_alive, always_show,
  creator, create_time, updater, update_time, deleted
)
SELECT * FROM (
  SELECT 65356, '查看', 'factory:report:query', 3, 1, @factory_report_menu_id,
         '', '', '', NULL,
         0, b'1', b'1', b'1',
         '1', NOW(), '1', NOW(), b'0'
) AS tmp
WHERE @factory_report_query_button_id IS NULL;

SET @factory_report_query_button_id :=
  (SELECT id
   FROM system_menu
   WHERE deleted = b'0'
     AND type = 3
     AND permission = 'factory:report:query'
   ORDER BY id DESC
   LIMIT 1);

UPDATE system_menu
SET name = '查看',
    permission = 'factory:report:query',
    type = 3,
    sort = 1,
    parent_id = @factory_report_menu_id,
    path = '',
    icon = '',
    component = '',
    component_name = NULL,
    status = 0,
    visible = b'1',
    keep_alive = b'1',
    always_show = b'1',
    updater = '1',
    update_time = NOW()
WHERE id = @factory_report_query_button_id;

SET @factory_brand_query_button_id :=
  (SELECT id
   FROM system_menu
   WHERE deleted = b'0'
     AND type = 3
     AND permission = 'factory:brand:query'
   ORDER BY id DESC
   LIMIT 1);

INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id,
  path, icon, component, component_name,
  status, visible, keep_alive, always_show,
  creator, create_time, updater, update_time, deleted
)
SELECT * FROM (
  SELECT 65357, '查看', 'factory:brand:query', 3, 1, @factory_brand_menu_id,
         '', '', '', NULL,
         0, b'1', b'1', b'1',
         '1', NOW(), '1', NOW(), b'0'
) AS tmp
WHERE @factory_brand_query_button_id IS NULL;

SET @factory_brand_query_button_id :=
  (SELECT id
   FROM system_menu
   WHERE deleted = b'0'
     AND type = 3
     AND permission = 'factory:brand:query'
   ORDER BY id DESC
   LIMIT 1);

UPDATE system_menu
SET name = '查看',
    permission = 'factory:brand:query',
    type = 3,
    sort = 1,
    parent_id = @factory_brand_menu_id,
    path = '',
    icon = '',
    component = '',
    component_name = NULL,
    status = 0,
    visible = b'1',
    keep_alive = b'1',
    always_show = b'1',
    updater = '1',
    update_time = NOW()
WHERE id = @factory_brand_query_button_id;

-- ================
-- 4. 角色授权（system_role_menu）
-- ================
INSERT INTO system_role_menu (role_id, menu_id, tenant_id, creator, updater, deleted)
SELECT sr.id, sm.id, sr.tenant_id, '1', '1', b'0'
FROM system_role sr
INNER JOIN system_menu sm
  ON sm.deleted = b'0'
 AND (
      sm.id IN (
        @factory_root_id,
        @factory_cockpit_menu_id,
        @factory_alarm_menu_id,
        @factory_video_fusion_menu_id,
        @factory_cloud_defense_menu_id,
        @factory_collaboration_menu_id,
        @factory_compliance_menu_id,
        @factory_environmental_menu_id,
        @factory_report_menu_id,
        @factory_brand_menu_id,
        @factory_cockpit_query_button_id,
        @factory_alarm_query_button_id,
        @factory_video_fusion_query_button_id,
        @factory_cloud_defense_query_button_id,
        @factory_collaboration_query_button_id,
        @factory_compliance_query_button_id,
        @factory_environmental_query_button_id,
        @factory_report_query_button_id,
        @factory_brand_query_button_id
      )
 )
WHERE sr.deleted = b'0'
  AND (
    sr.code IN ('super_admin', 'tenant_admin')
    OR sr.name IN ('超级管理员', '租户管理员')
    OR sr.name LIKE '%管理员%'
  )
  AND NOT EXISTS (
    SELECT 1
    FROM system_role_menu srm
    WHERE srm.deleted = b'0'
      AND srm.role_id = sr.id
      AND srm.menu_id = sm.id
      AND srm.tenant_id = sr.tenant_id
  );

-- ================
-- 5. 验证
-- ================
SELECT id, name, parent_id, type, path, component, permission
FROM system_menu
WHERE deleted = b'0'
  AND (
    id = @factory_root_id
    OR parent_id = @factory_root_id
    OR permission LIKE 'factory:%'
  )
ORDER BY type, sort, id;

SELECT sr.id AS role_id, sr.name AS role_name, sr.code AS role_code, COUNT(*) AS granted_count
FROM system_role_menu srm
INNER JOIN system_role sr ON sr.id = srm.role_id AND sr.deleted = b'0'
INNER JOIN system_menu sm ON sm.id = srm.menu_id AND sm.deleted = b'0'
WHERE srm.deleted = b'0'
  AND (
    sm.id = @factory_root_id
    OR sm.parent_id = @factory_root_id
    OR sm.permission LIKE 'factory:%'
  )
GROUP BY sr.id, sr.name, sr.code
ORDER BY granted_count DESC, sr.id;
