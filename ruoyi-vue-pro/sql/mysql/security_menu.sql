-- =============================================
-- 智慧安防模块菜单配置
-- 创建时间: 2025-10-24
-- 说明: 包含智慧安防的完整菜单结构和权限配置
-- =============================================

-- 删除旧菜单（如果存在）
DELETE FROM `system_menu` WHERE `id` >= 5000 AND `id` < 6000;

-- =============================================
-- 一级菜单：智慧安防
-- =============================================
INSERT INTO `system_menu` (
  `id`, `name`, `permission`, `type`, `sort`, `parent_id`, 
  `path`, `icon`, `component`, `component_name`, 
  `status`, `visible`, `keep_alive`, `always_show`, 
  `creator`, `create_time`, `updater`, `update_time`, `deleted`
) VALUES (
  5000, '智慧安防', '', 1, 50, 0,
  '/security', 'fa-solid:shield-alt', NULL, NULL,
  0, b'1', b'1', b'1',
  '1', NOW(), '1', NOW(), b'0'
);

-- =============================================
-- 二级菜单
-- =============================================

-- 5001: 安防概览
INSERT INTO `system_menu` VALUES (
  5001, '安防概览', 'security:overview:view', 2, 1, 5000,
  'overview', 'fa:dashboard', 'security/SecurityOverview/index', 'SecurityOverview',
  0, b'1', b'1', b'1',
  '1', NOW(), '1', NOW(), b'0'
);

-- 5010: 视频监控
INSERT INTO `system_menu` VALUES (
  5010, '视频监控', '', 1, 10, 5000,
  'video-surveillance', 'fa:video-camera', NULL, NULL,
  0, b'1', b'1', b'1',
  '1', NOW(), '1', NOW(), b'0'
);

-- 5011: 实时预览
INSERT INTO `system_menu` VALUES (
  5011, '实时预览', 'security:video:realtime', 2, 1, 5010,
  'realtime-preview', 'fa:eye', 'security/VideoSurveillance/RealTimePreview/index', 'RealTimePreview',
  0, b'1', b'1', b'1',
  '1', NOW(), '1', NOW(), b'0'
);

-- 5012: 多画面预览
INSERT INTO `system_menu` VALUES (
  5012, '多画面预览', 'security:video:multiscreen', 2, 2, 5010,
  'multiscreen-preview', 'fa:th', 'security/VideoSurveillance/MultiScreenPreview/index', 'MultiScreenPreview',
  0, b'1', b'1', b'1',
  '1', NOW(), '1', NOW(), b'0'
);

-- 5013: 录像回放
INSERT INTO `system_menu` VALUES (
  5013, '录像回放', 'security:video:playback', 2, 3, 5010,
  'video-playback', 'fa:play-circle', 'security/VideoSurveillance/VideoPlayback/index', 'VideoPlayback',
  0, b'1', b'1', b'1',
  '1', NOW(), '1', NOW(), b'0'
);

-- 5014: 抓拍记录
INSERT INTO `system_menu` VALUES (
  5014, '抓拍记录', 'security:video:snapshot', 2, 4, 5010,
  'snapshot-record', 'fa:camera', 'security/VideoSurveillance/SnapshotRecord/index', 'SnapshotRecord',
  0, b'1', b'1', b'1',
  '1', NOW(), '1', NOW(), b'0'
);

-- 5015: 视频告警记录
INSERT INTO `system_menu` VALUES (
  5015, '视频告警记录', 'security:video:alarm', 2, 5, 5010,
  'video-alarm-record', 'fa:bell', 'security/VideoSurveillance/VideoAlarmRecord/index', 'VideoAlarmRecord',
  0, b'1', b'1', b'1',
  '1', NOW(), '1', NOW(), b'0'
);

-- 5020: 人员布控
INSERT INTO `system_menu` VALUES (
  5020, '人员布控', '', 1, 20, 5000,
  'personnel-control', 'fa:users', NULL, NULL,
  0, b'1', b'1', b'1',
  '1', NOW(), '1', NOW(), b'0'
);

-- 5021: 人员库
INSERT INTO `system_menu` VALUES (
  5021, '人员库', 'security:personnel-library:query', 2, 1, 5020,
  'personnel-library', 'fa:user', 'security/PersonnelControl/PersonnelLibrary/index', 'PersonnelLibrary',
  0, b'1', b'1', b'1',
  '1', NOW(), '1', NOW(), b'0'
);

-- 5021按钮权限
INSERT INTO `system_menu` VALUES (5022, '新增人员', 'security:personnel-library:create', 3, 1, 5021, '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0');
INSERT INTO `system_menu` VALUES (5023, '修改人员', 'security:personnel-library:update', 3, 2, 5021, '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0');
INSERT INTO `system_menu` VALUES (5024, '删除人员', 'security:personnel-library:delete', 3, 3, 5021, '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0');
INSERT INTO `system_menu` VALUES (5025, '导入人员', 'security:personnel-library:import', 3, 4, 5021, '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0');
INSERT INTO `system_menu` VALUES (5026, '导出人员', 'security:personnel-library:export', 3, 5, 5021, '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0');

-- 5030: 人员布控配置
INSERT INTO `system_menu` VALUES (
  5030, '布控配置', 'security:personnel-control:query', 2, 2, 5020,
  'personnel-control-config', 'fa:crosshairs', 'security/PersonnelControl/PersonnelControl/index', 'PersonnelControlConfig',
  0, b'1', b'1', b'1',
  '1', NOW(), '1', NOW(), b'0'
);

-- 5030按钮权限
INSERT INTO `system_menu` VALUES (5031, '新增布控', 'security:personnel-control:create', 3, 1, 5030, '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0');
INSERT INTO `system_menu` VALUES (5032, '修改布控', 'security:personnel-control:update', 3, 2, 5030, '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0');
INSERT INTO `system_menu` VALUES (5033, '删除布控', 'security:personnel-control:delete', 3, 3, 5030, '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0');
INSERT INTO `system_menu` VALUES (5034, '启用停用', 'security:personnel-control:status', 3, 4, 5030, '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0');

-- 5040: 人脸抓拍
INSERT INTO `system_menu` VALUES (
  5040, '人脸抓拍', 'security:face-capture:query', 2, 3, 5020,
  'personnel-capture', 'fa:camera-retro', 'security/PersonnelControl/PersonnelCapture/index', 'PersonnelCapture',
  0, b'1', b'1', b'1',
  '1', NOW(), '1', NOW(), b'0'
);

-- 5040按钮权限
INSERT INTO `system_menu` VALUES (5041, '删除抓拍', 'security:face-capture:delete', 3, 1, 5040, '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0');
INSERT INTO `system_menu` VALUES (5042, '导出抓拍', 'security:face-capture:export', 3, 2, 5040, '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0');

-- 5045: 人脸识别记录
INSERT INTO `system_menu` VALUES (
  5045, '识别记录', 'security:face-recognition:query', 2, 4, 5020,
  'personnel-recognition', 'fa:check-circle', 'security/PersonnelControl/PersonnelRecognition/index', 'PersonnelRecognition',
  0, b'1', b'1', b'1',
  '1', NOW(), '1', NOW(), b'0'
);

-- 5050: 人员追踪
INSERT INTO `system_menu` VALUES (
  5050, '人员追踪', 'security:personnel-track:query', 2, 5, 5020,
  'personnel-track', 'fa:location-arrow', 'security/PersonnelControl/PersonnelTrack/index', 'PersonnelTrack',
  0, b'1', b'1', b'1',
  '1', NOW(), '1', NOW(), b'0'
);

-- 5055: 布控告警记录
INSERT INTO `system_menu` VALUES (
  5055, '布控告警', 'security:personnel-control:alarm', 2, 6, 5020,
  'control-alarm-record', 'fa:exclamation-triangle', 'security/PersonnelControl/ControlAlarmRecord/index', 'ControlAlarmRecord',
  0, b'1', b'1', b'1',
  '1', NOW(), '1', NOW(), b'0'
);

-- 5056: 车辆抓拍
INSERT INTO `system_menu` VALUES (
  5056, '车辆抓拍', 'security:vehicle-capture:query', 2, 7, 5020,
  'vehicle-capture', 'fa:car', 'security/PersonnelControl/VehicleCapture/index', 'VehicleCapture',
  0, b'1', b'1', b'1',
  '1', NOW(), '1', NOW(), b'0'
);

-- 5060: 视频分析
INSERT INTO `system_menu` VALUES (
  5060, '视频分析', '', 1, 30, 5000,
  'video-analysis', 'fa:bar-chart', NULL, NULL,
  0, b'1', b'1', b'1',
  '1', NOW(), '1', NOW(), b'0'
);

-- 5061: 分析任务
INSERT INTO `system_menu` VALUES (
  5061, '分析任务', 'security:video-analysis:query', 2, 1, 5060,
  'analysis-tasks', 'fa:tasks', 'security/VideoAnalysis/AnalysisTasks/index', 'AnalysisTasks',
  0, b'1', b'1', b'1',
  '1', NOW(), '1', NOW(), b'0'
);

-- 5061按钮权限
INSERT INTO `system_menu` VALUES (5062, '新增任务', 'security:video-analysis:create', 3, 1, 5061, '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0');
INSERT INTO `system_menu` VALUES (5063, '修改任务', 'security:video-analysis:update', 3, 2, 5061, '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0');
INSERT INTO `system_menu` VALUES (5064, '删除任务', 'security:video-analysis:delete', 3, 3, 5061, '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0');
INSERT INTO `system_menu` VALUES (5065, '启动停止', 'security:video-analysis:control', 3, 4, 5061, '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0');

-- 5070: 分析告警
INSERT INTO `system_menu` VALUES (
  5070, '分析告警', 'security:video-analysis:alarm', 2, 2, 5060,
  'video-analysis-alarm', 'fa:warning', 'security/VideoAnalysis/VideoAnalysisAlarm/index', 'VideoAnalysisAlarm',
  0, b'1', b'1', b'1',
  '1', NOW(), '1', NOW(), b'0'
);

-- 5070按钮权限
INSERT INTO `system_menu` VALUES (5071, '处理告警', 'security:video-analysis:handle', 3, 1, 5070, '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0');

-- 5075: 设备配置
INSERT INTO `system_menu` VALUES (
  5075, '设备配置', 'security:video-analysis:config', 2, 3, 5060,
  'equipment-config', 'fa:cog', 'security/VideoAnalysis/EquipmentConfig/index', 'EquipmentConfig',
  0, b'1', b'1', b'1',
  '1', NOW(), '1', NOW(), b'0'
);

-- 5080: 终端管理
INSERT INTO `system_menu` VALUES (
  5080, '终端管理', 'security:video-analysis:terminal', 2, 4, 5060,
  'terminal-management', 'fa:desktop', 'security/VideoAnalysis/TerminalManagement/index', 'TerminalManagement',
  0, b'1', b'1', b'1',
  '1', NOW(), '1', NOW(), b'0'
);

-- 5090: 电子巡更（可选功能）
INSERT INTO `system_menu` VALUES (
  5090, '电子巡更', '', 1, 40, 5000,
  'electronic-patrol', 'fa:map-marker', NULL, NULL,
  0, b'1', b'1', b'1',
  '1', NOW(), '1', NOW(), b'0'
);

-- 5100: 周界入侵（可选功能）
INSERT INTO `system_menu` VALUES (
  5100, '周界入侵', '', 1, 50, 5000,
  'perimeter-intrusion', 'fa:exclamation-circle', NULL, NULL,
  0, b'1', b'1', b'1',
  '1', NOW(), '1', NOW(), b'0'
);

-- 5110: 视频巡查（可选功能）
INSERT INTO `system_menu` VALUES (
  5110, '视频巡查', '', 1, 60, 5000,
  'video-patrol', 'fa:search', NULL, NULL,
  0, b'1', b'1', b'1',
  '1', NOW(), '1', NOW(), b'0'
);

-- =============================================
-- 完成提示
-- =============================================
SELECT '智慧安防菜单配置完成！' AS message,
       '共创建 ' || COUNT(*) || ' 个菜单项' AS detail
FROM `system_menu` 
WHERE `id` >= 5000 AND `id` < 6000 AND `deleted` = 0;

-- =============================================
-- 使用说明
-- =============================================
-- 1. 执行本脚本后，需要清理Redis缓存或重启应用
-- 2. 管理员登录后台，在"系统管理-菜单管理"中可以看到新菜单
-- 3. 在"系统管理-角色管理"中给角色分配智慧安防的权限
-- 4. 前端刷新后即可看到智慧安防模块
-- =============================================


















