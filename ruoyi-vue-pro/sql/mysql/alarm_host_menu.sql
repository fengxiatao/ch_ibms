-- =============================================
-- 报警主机菜单权限配置
-- 创建时间: 2025-12-01
-- 说明: 为周界入侵模块添加报警主机管理功能
-- =============================================

-- 删除旧的报警主机菜单（如果存在）
DELETE FROM `system_menu` WHERE `id` >= 5101 AND `id` <= 5110;

-- =============================================
-- 报警主机管理菜单
-- =============================================

-- 5101: 报警主机（二级菜单）
INSERT INTO `system_menu` (
  `id`, `name`, `permission`, `type`, `sort`, `parent_id`, 
  `path`, `icon`, `component`, `component_name`, 
  `status`, `visible`, `keep_alive`, `always_show`, 
  `creator`, `create_time`, `updater`, `update_time`, `deleted`
) VALUES (
  5101, '报警主机', 'iot:alarm-host:query', 2, 1, 5100,
  'alarm-host', 'fa:server', 'security/PerimeterIntrusion/AlarmHost/index', 'AlarmHost',
  0, b'1', b'1', b'1',
  '1', NOW(), '1', NOW(), b'0'
);

-- =============================================
-- 报警主机按钮权限（三级菜单）
-- =============================================

-- 5102: 新增报警主机
INSERT INTO `system_menu` VALUES (
  5102, '新增报警主机', 'iot:alarm-host:create', 3, 1, 5101, 
  '', '', '', NULL, 
  0, b'1', b'1', b'1', 
  '1', NOW(), '1', NOW(), b'0'
);

-- 5103: 修改报警主机
INSERT INTO `system_menu` VALUES (
  5103, '修改报警主机', 'iot:alarm-host:update', 3, 2, 5101, 
  '', '', '', NULL, 
  0, b'1', b'1', b'1', 
  '1', NOW(), '1', NOW(), b'0'
);

-- 5104: 删除报警主机
INSERT INTO `system_menu` VALUES (
  5104, '删除报警主机', 'iot:alarm-host:delete', 3, 3, 5101, 
  '', '', '', NULL, 
  0, b'1', b'1', b'1', 
  '1', NOW(), '1', NOW(), b'0'
);

-- 5105: 布防操作
INSERT INTO `system_menu` VALUES (
  5105, '布防操作', 'iot:alarm-host:arm', 3, 4, 5101, 
  '', '', '', NULL, 
  0, b'1', b'1', b'1', 
  '1', NOW(), '1', NOW(), b'0'
);

-- 5106: 消警操作
INSERT INTO `system_menu` VALUES (
  5106, '消警操作', 'iot:alarm-host:clear-alarm', 3, 5, 5101, 
  '', '', '', NULL, 
  0, b'1', b'1', b'1', 
  '1', NOW(), '1', NOW(), b'0'
);

-- =============================================
-- 验证菜单创建结果
-- =============================================
SELECT 
  '报警主机菜单配置完成！' AS message,
  CONCAT('共创建 ', COUNT(*), ' 个菜单项') AS detail
FROM `system_menu` 
WHERE `id` >= 5101 AND `id` <= 5106 AND `deleted` = 0;

-- =============================================
-- 使用说明
-- =============================================
-- 1. 执行本脚本后，需要清理Redis缓存或重启应用
-- 2. 管理员登录后台，在"系统管理-菜单管理"中可以看到新菜单
-- 3. 在"系统管理-角色管理"中给角色分配报警主机的权限
-- 4. 前端刷新后即可看到"新增"按钮
-- 
-- 权限说明：
-- - iot:alarm-host:query: 查询报警主机列表
-- - iot:alarm-host:create: 新增报警主机
-- - iot:alarm-host:update: 修改报警主机
-- - iot:alarm-host:delete: 删除报警主机
-- - iot:alarm-host:arm: 布防/撤防操作
-- - iot:alarm-host:clear-alarm: 消警操作
-- =============================================
