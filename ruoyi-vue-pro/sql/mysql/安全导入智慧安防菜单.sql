-- =============================================
-- 智慧安防菜单安全导入脚本
-- 创建时间: 2025-10-24
-- 说明: 检查现有菜单，只导入缺失的菜单，避免重复
-- =============================================

-- =============================================
-- 第一步：检查现有菜单情况
-- =============================================

-- 1.1 查询现有智慧安防菜单
SELECT '=== 现有智慧安防菜单 ===' AS 提示;
SELECT 
    id, name, parent_id, path, type, sort, deleted
FROM system_menu
WHERE (
    name LIKE '%智慧安防%' 
    OR name LIKE '%安防概览%'
    OR name LIKE '%视频监控%'
    OR name LIKE '%人员布控%'
    OR name LIKE '%视频分析%'
    OR path LIKE '%security%'
)
ORDER BY id;

-- 1.2 检查5000-5999范围
SELECT '=== 5000-5999范围检查 ===' AS 提示;
SELECT 
    COUNT(*) AS 现有菜单数,
    MIN(id) AS 最小ID,
    MAX(id) AS 最大ID
FROM system_menu
WHERE id >= 5000 AND id < 6000;

-- 1.3 查询最大菜单ID
SELECT '=== 当前最大菜单ID ===' AS 提示;
SELECT MAX(id) AS 最大ID FROM system_menu;

-- =============================================
-- 第二步：安全策略选择
-- =============================================

-- 策略1：如果5000-5999范围内没有菜单，直接导入
-- 策略2：如果已有菜单，需要先删除旧菜单（谨慎！）
-- 策略3：使用新的ID范围（推荐）

-- =============================================
-- 第三步：智能清理（可选，慎用！）
-- =============================================

-- ⚠️ 警告：此操作会删除现有智慧安防菜单！
-- ⚠️ 请先备份数据库或确认可以删除！
-- ⚠️ 如果不需要删除，请注释掉下面的DELETE语句

-- 删除现有智慧安防菜单（包括所有子菜单）
-- DELETE FROM system_menu 
-- WHERE id IN (
--     SELECT id FROM (
--         SELECT m1.id
--         FROM system_menu m1
--         WHERE m1.name = '智慧安防' 
--            OR m1.parent_id IN (
--                SELECT m2.id FROM system_menu m2 WHERE m2.name = '智慧安防'
--            )
--     ) AS temp
-- );

-- 或者：只删除5000-5999范围内的菜单
-- DELETE FROM system_menu WHERE id >= 5000 AND id < 6000;

-- =============================================
-- 第四步：导入新菜单
-- =============================================

-- 4.1 一级菜单：智慧安防
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
)
ON DUPLICATE KEY UPDATE
  `name` = '智慧安防',
  `icon` = 'fa-solid:shield-alt',
  `updater` = '1',
  `update_time` = NOW();

-- 4.2 安防概览
INSERT INTO `system_menu` VALUES (
  5001, '安防概览', 'security:overview:view', 2, 1, 5000,
  'overview', 'fa:dashboard', 'security/SecurityOverview/index', 'SecurityOverview',
  0, b'1', b'1', b'1',
  '1', NOW(), '1', NOW(), b'0'
)
ON DUPLICATE KEY UPDATE
  `name` = '安防概览',
  `updater` = '1',
  `update_time` = NOW();

-- 4.3 视频监控（父菜单）
INSERT INTO `system_menu` VALUES (
  5010, '视频监控', '', 1, 10, 5000,
  'video-surveillance', 'fa:video-camera', NULL, NULL,
  0, b'1', b'1', b'1',
  '1', NOW(), '1', NOW(), b'0'
)
ON DUPLICATE KEY UPDATE
  `name` = '视频监控',
  `updater` = '1',
  `update_time` = NOW();

-- 4.4 实时预览
INSERT INTO `system_menu` VALUES (
  5011, '实时预览', 'security:video:realtime', 2, 1, 5010,
  'realtime-preview', 'fa:eye', 'security/VideoSurveillance/RealTimePreview/index', 'RealTimePreview',
  0, b'1', b'1', b'1',
  '1', NOW(), '1', NOW(), b'0'
)
ON DUPLICATE KEY UPDATE
  `name` = '实时预览',
  `updater` = '1',
  `update_time` = NOW();

-- 4.5 多画面预览
INSERT INTO `system_menu` VALUES (
  5012, '多画面预览', 'security:video:multiscreen', 2, 2, 5010,
  'multiscreen-preview', 'fa:th', 'security/VideoSurveillance/MultiScreenPreview/index', 'MultiScreenPreview',
  0, b'1', b'1', b'1',
  '1', NOW(), '1', NOW(), b'0'
)
ON DUPLICATE KEY UPDATE
  `name` = '多画面预览',
  `updater` = '1',
  `update_time` = NOW();

-- 4.6 录像回放
INSERT INTO `system_menu` VALUES (
  5013, '录像回放', 'security:video:playback', 2, 3, 5010,
  'video-playback', 'fa:play-circle', 'security/VideoSurveillance/VideoPlayback/index', 'VideoPlayback',
  0, b'1', b'1', b'1',
  '1', NOW(), '1', NOW(), b'0'
)
ON DUPLICATE KEY UPDATE
  `name` = '录像回放',
  `updater` = '1',
  `update_time` = NOW();

-- 4.7 抓拍记录
INSERT INTO `system_menu` VALUES (
  5014, '抓拍记录', 'security:video:snapshot', 2, 4, 5010,
  'snapshot-record', 'fa:camera', 'security/VideoSurveillance/SnapshotRecord/index', 'SnapshotRecord',
  0, b'1', b'1', b'1',
  '1', NOW(), '1', NOW(), b'0'
)
ON DUPLICATE KEY UPDATE
  `name` = '抓拍记录',
  `updater` = '1',
  `update_time` = NOW();

-- 4.8 视频告警记录
INSERT INTO `system_menu` VALUES (
  5015, '视频告警记录', 'security:video:alarm', 2, 5, 5010,
  'video-alarm-record', 'fa:bell', 'security/VideoSurveillance/VideoAlarmRecord/index', 'VideoAlarmRecord',
  0, b'1', b'1', b'1',
  '1', NOW(), '1', NOW(), b'0'
)
ON DUPLICATE KEY UPDATE
  `name` = '视频告警记录',
  `updater` = '1',
  `update_time` = NOW();

-- 4.9 人员布控（父菜单）
INSERT INTO `system_menu` VALUES (
  5020, '人员布控', '', 1, 20, 5000,
  'personnel-control', 'fa:users', NULL, NULL,
  0, b'1', b'1', b'1',
  '1', NOW(), '1', NOW(), b'0'
)
ON DUPLICATE KEY UPDATE
  `name` = '人员布控',
  `updater` = '1',
  `update_time` = NOW();

-- 继续添加其他菜单...
-- （为了简洁，这里只列出部分，完整版请参考security_menu.sql）

-- =============================================
-- 第五步：验证导入结果
-- =============================================

SELECT '=== 导入后菜单统计 ===' AS 提示;
SELECT 
    COUNT(*) AS 菜单总数
FROM system_menu
WHERE id >= 5000 AND id < 6000
AND deleted = 0;

SELECT '=== 智慧安防菜单结构 ===' AS 提示;
SELECT 
    id, 
    name, 
    parent_id,
    type,
    CASE type
        WHEN 1 THEN '目录'
        WHEN 2 THEN '菜单'
        WHEN 3 THEN '按钮'
    END AS 类型,
    path
FROM system_menu
WHERE id >= 5000 AND id < 6000
AND deleted = 0
ORDER BY id;

-- =============================================
-- 使用说明
-- =============================================
-- 
-- 1. 先执行"检查现有安防菜单.sql"，查看现状
-- 2. 根据检查结果选择策略：
--    - 如果没有冲突：直接执行本脚本
--    - 如果有冲突：取消注释DELETE语句（谨慎！）
-- 3. 使用ON DUPLICATE KEY UPDATE避免主键冲突
-- 4. 验证导入结果
-- 5. 清理Redis缓存
-- 6. 重启应用
-- =============================================


















