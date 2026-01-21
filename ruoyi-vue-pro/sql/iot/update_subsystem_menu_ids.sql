-- =============================================
-- 更新子系统菜单ID（根据实际菜单）
-- 创建时间: 2025-11-03
-- =============================================

-- 清空旧数据
TRUNCATE TABLE `iot_subsystem`;

-- 重新插入数据（使用实际的菜单ID）
-- 一级子系统
INSERT INTO `iot_subsystem` (
  `code`, `name`, `parent_code`, `level`, `menu_id`, `menu_path`, 
  `icon`, `description`, `sort`, `enabled`, `creator`, `create_time`, `updater`, `update_time`, `deleted`
) VALUES (
  'security', '智慧安防', NULL, 1, 5132, '/security',
  'fa-solid:shield-alt', 'IBMS智慧安防子系统，包括视频监控、人员布控等功能', 10, 1,
  '1', NOW(), '1', NOW(), 0
);

-- 二级子系统（智慧安防）
INSERT INTO `iot_subsystem` (
  `code`, `name`, `parent_code`, `level`, `menu_id`, `menu_path`, 
  `icon`, `description`, `sort`, `enabled`, `creator`, `create_time`, `updater`, `update_time`, `deleted`
) VALUES
  ('security.overview', '安防概览', 'security', 2, 5219, '/security/security-overview',
   'fa:dashboard', '安防概览大屏', 1, 1, '1', NOW(), '1', NOW(), 0),
  ('security.video', '视频监控', 'security', 2, 5133, '/security/video-surveillance',
   'fa:video-camera', '网络摄像头监控、实时预览、录像回放、抓拍记录', 2, 1, '1', NOW(), '1', NOW(), 0),
  ('security.personnel', '人员布控', 'security', 2, 5134, '/security/personnel-control',
   'fa:users', '人脸识别、人员库管理、布控任务', 3, 1, '1', NOW(), '1', NOW(), 0),
  ('security.analysis', '视频分析', 'security', 2, 5135, '/security/video-analysis',
   'fa:chart-line', '视频智能分析', 4, 1, '1', NOW(), '1', NOW(), 0),
  ('security.patrol', '电子巡更', 'security', 2, 5136, '/security/electronic-patrol',
   'fa:map-marked-alt', '电子巡更管理', 5, 1, '1', NOW(), '1', NOW(), 0),
  ('security.video-patrol', '视频巡更', 'security', 2, 5137, '/security/video-patrol',
   'fa:video', '视频巡更管理', 6, 1, '1', NOW(), '1', NOW(), 0),
  ('security.perimeter', '周界入侵', 'security', 2, 5138, '/security/perimeter',
   'fa:crosshairs', '周界入侵检测、电子围栏', 7, 1, '1', NOW(), '1', NOW(), 0);

-- 验证数据
SELECT '=== 子系统数据更新完成 ===' AS message;

SELECT 
    s.code AS 子系统代码,
    s.name AS 子系统名称,
    s.level AS 层级,
    s.menu_id AS 菜单ID,
    m.name AS 菜单名称
FROM `iot_subsystem` s
LEFT JOIN `system_menu` m ON s.menu_id = m.id
ORDER BY s.level, s.sort;




