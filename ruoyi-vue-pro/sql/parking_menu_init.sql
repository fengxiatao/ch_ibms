-- =============================================
-- 停车场管理模块菜单初始化SQL
-- 数据库：ch_ibms
-- 父目录：6291（智慧通行）
-- 起始ID：6334（当前最大ID为6333）
-- 创建时间：2026-01-14
-- =============================================

-- 1. 停车场管理一级菜单（放在智慧通行6291下）
INSERT INTO `ch_ibms`.`system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES
(6334, '停车场管理', '', 1, 20, 6291, 'parking', 'ep:van', '', '', 0, 1, 1, 1, '1', NOW(), '1', NOW(), 0);

-- 2. 设备管理 - 道闸设备管理
INSERT INTO `ch_ibms`.`system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES
(6335, '道闸设备管理', 'iot:parking:gate:query', 2, 1, 6334, 'gate', 'ep:open', 'iot/access/parking/gate/index', 'ParkingGate', 0, 1, 1, 0, '1', NOW(), '1', NOW(), 0);

-- 道闸设备管理按钮权限
INSERT INTO `ch_ibms`.`system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES
(6336, '道闸新增', 'iot:parking:gate:create', 3, 1, 6335, '', '', '', '', 0, 1, 1, 0, '1', NOW(), '1', NOW(), 0),
(6337, '道闸编辑', 'iot:parking:gate:update', 3, 2, 6335, '', '', '', '', 0, 1, 1, 0, '1', NOW(), '1', NOW(), 0),
(6338, '道闸删除', 'iot:parking:gate:delete', 3, 3, 6335, '', '', '', '', 0, 1, 1, 0, '1', NOW(), '1', NOW(), 0);

-- 3. 车场配置子菜单（目录）
INSERT INTO `ch_ibms`.`system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES
(6339, '车场配置', '', 1, 2, 6334, 'config', 'ep:setting', '', '', 0, 1, 1, 1, '1', NOW(), '1', NOW(), 0);

-- 3.1 配置车场
INSERT INTO `ch_ibms`.`system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES
(6340, '配置车场', 'iot:parking:lot:query', 2, 1, 6339, 'lot', 'ep:office-building', 'iot/access/parking/lot/index', 'ParkingLot', 0, 1, 1, 0, '1', NOW(), '1', NOW(), 0);

-- 车场管理按钮权限
INSERT INTO `ch_ibms`.`system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES
(6341, '车场新增', 'iot:parking:lot:create', 3, 1, 6340, '', '', '', '', 0, 1, 1, 0, '1', NOW(), '1', NOW(), 0),
(6342, '车场编辑', 'iot:parking:lot:update', 3, 2, 6340, '', '', '', '', 0, 1, 1, 0, '1', NOW(), '1', NOW(), 0),
(6343, '车场删除', 'iot:parking:lot:delete', 3, 3, 6340, '', '', '', '', 0, 1, 1, 0, '1', NOW(), '1', NOW(), 0);

-- 3.2 配置车道
INSERT INTO `ch_ibms`.`system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES
(6344, '配置车道', 'iot:parking:lane:query', 2, 2, 6339, 'lane', 'ep:guide', 'iot/access/parking/lane/index', 'ParkingLane', 0, 1, 1, 0, '1', NOW(), '1', NOW(), 0);

-- 车道管理按钮权限
INSERT INTO `ch_ibms`.`system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES
(6345, '车道新增', 'iot:parking:lane:create', 3, 1, 6344, '', '', '', '', 0, 1, 1, 0, '1', NOW(), '1', NOW(), 0),
(6346, '车道编辑', 'iot:parking:lane:update', 3, 2, 6344, '', '', '', '', 0, 1, 1, 0, '1', NOW(), '1', NOW(), 0),
(6347, '车道删除', 'iot:parking:lane:delete', 3, 3, 6344, '', '', '', '', 0, 1, 1, 0, '1', NOW(), '1', NOW(), 0);

-- 3.3 免费车辆
INSERT INTO `ch_ibms`.`system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES
(6348, '免费车辆', 'iot:parking:free-vehicle:query', 2, 3, 6339, 'free-vehicle', 'ep:ticket', 'iot/access/parking/vehicle/free/index', 'ParkingFreeVehicle', 0, 1, 1, 0, '1', NOW(), '1', NOW(), 0);

-- 免费车管理按钮权限
INSERT INTO `ch_ibms`.`system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES
(6349, '免费车新增', 'iot:parking:free-vehicle:create', 3, 1, 6348, '', '', '', '', 0, 1, 1, 0, '1', NOW(), '1', NOW(), 0),
(6350, '免费车编辑', 'iot:parking:free-vehicle:update', 3, 2, 6348, '', '', '', '', 0, 1, 1, 0, '1', NOW(), '1', NOW(), 0),
(6351, '免费车删除', 'iot:parking:free-vehicle:delete', 3, 3, 6348, '', '', '', '', 0, 1, 1, 0, '1', NOW(), '1', NOW(), 0);

-- 3.4 月卡车辆
INSERT INTO `ch_ibms`.`system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES
(6352, '月卡车辆', 'iot:parking:monthly-vehicle:query', 2, 4, 6339, 'monthly-vehicle', 'ep:bank-card', 'iot/access/parking/vehicle/monthly/index', 'ParkingMonthlyVehicle', 0, 1, 1, 0, '1', NOW(), '1', NOW(), 0);

-- 月卡车管理按钮权限
INSERT INTO `ch_ibms`.`system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES
(6353, '月卡车新增', 'iot:parking:monthly-vehicle:create', 3, 1, 6352, '', '', '', '', 0, 1, 1, 0, '1', NOW(), '1', NOW(), 0),
(6354, '月卡车编辑', 'iot:parking:monthly-vehicle:update', 3, 2, 6352, '', '', '', '', 0, 1, 1, 0, '1', NOW(), '1', NOW(), 0),
(6355, '月卡车删除', 'iot:parking:monthly-vehicle:delete', 3, 3, 6352, '', '', '', '', 0, 1, 1, 0, '1', NOW(), '1', NOW(), 0),
(6356, '月卡车续费', 'iot:parking:monthly-vehicle:recharge', 3, 4, 6352, '', '', '', '', 0, 1, 1, 0, '1', NOW(), '1', NOW(), 0);

-- 3.5 配置放行规则
INSERT INTO `ch_ibms`.`system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES
(6357, '配置放行规则', 'iot:parking:pass-rule:query', 2, 5, 6339, 'pass-rule', 'ep:operation', 'iot/access/parking/pass-rule/index', 'ParkingPassRule', 0, 1, 1, 0, '1', NOW(), '1', NOW(), 0);

-- 放行规则按钮权限
INSERT INTO `ch_ibms`.`system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES
(6358, '放行规则新增', 'iot:parking:pass-rule:create', 3, 1, 6357, '', '', '', '', 0, 1, 1, 0, '1', NOW(), '1', NOW(), 0),
(6359, '放行规则编辑', 'iot:parking:pass-rule:update', 3, 2, 6357, '', '', '', '', 0, 1, 1, 0, '1', NOW(), '1', NOW(), 0),
(6360, '放行规则删除', 'iot:parking:pass-rule:delete', 3, 3, 6357, '', '', '', '', 0, 1, 1, 0, '1', NOW(), '1', NOW(), 0);

-- 3.6 配置收费规则
INSERT INTO `ch_ibms`.`system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES
(6361, '配置收费规则', 'iot:parking:charge-rule:query', 2, 6, 6339, 'charge-rule', 'ep:money', 'iot/access/parking/charge-rule/index', 'ParkingChargeRule', 0, 1, 1, 0, '1', NOW(), '1', NOW(), 0);

-- 收费规则按钮权限
INSERT INTO `ch_ibms`.`system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES
(6362, '收费规则新增', 'iot:parking:charge-rule:create', 3, 1, 6361, '', '', '', '', 0, 1, 1, 0, '1', NOW(), '1', NOW(), 0),
(6363, '收费规则编辑', 'iot:parking:charge-rule:update', 3, 2, 6361, '', '', '', '', 0, 1, 1, 0, '1', NOW(), '1', NOW(), 0),
(6364, '收费规则删除', 'iot:parking:charge-rule:delete', 3, 3, 6361, '', '', '', '', 0, 1, 1, 0, '1', NOW(), '1', NOW(), 0);

-- 3.7 收费规则应用
INSERT INTO `ch_ibms`.`system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES
(6365, '收费规则应用', 'iot:parking:charge-rule-apply:query', 2, 7, 6339, 'charge-rule-apply', 'ep:coin', 'iot/access/parking/charge-rule-apply/index', 'ParkingChargeRuleApply', 0, 1, 1, 0, '1', NOW(), '1', NOW(), 0);

-- 收费规则应用按钮权限
INSERT INTO `ch_ibms`.`system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES
(6366, '收费应用新增', 'iot:parking:charge-rule-apply:create', 3, 1, 6365, '', '', '', '', 0, 1, 1, 0, '1', NOW(), '1', NOW(), 0),
(6367, '收费应用编辑', 'iot:parking:charge-rule-apply:update', 3, 2, 6365, '', '', '', '', 0, 1, 1, 0, '1', NOW(), '1', NOW(), 0),
(6368, '收费应用删除', 'iot:parking:charge-rule-apply:delete', 3, 3, 6365, '', '', '', '', 0, 1, 1, 0, '1', NOW(), '1', NOW(), 0);

-- 4. 记录查询子菜单（目录）
INSERT INTO `ch_ibms`.`system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES
(6369, '记录查询', '', 1, 3, 6334, 'record', 'ep:document', '', '', 0, 1, 1, 1, '1', NOW(), '1', NOW(), 0);

-- 4.1 在场车辆
INSERT INTO `ch_ibms`.`system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES
(6370, '在场车辆', 'iot:parking:present-vehicle:query', 2, 1, 6369, 'present', 'ep:location', 'iot/access/parking/record/present/index', 'ParkingPresentVehicle', 0, 1, 1, 0, '1', NOW(), '1', NOW(), 0);

-- 在场车辆按钮权限
INSERT INTO `ch_ibms`.`system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES
(6371, '强制出场', 'iot:parking:present-vehicle:force-exit', 3, 1, 6370, '', '', '', '', 0, 1, 1, 0, '1', NOW(), '1', NOW(), 0);

-- 4.2 进出记录（含收费）
INSERT INTO `ch_ibms`.`system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES
(6372, '进出记录', 'iot:parking:record:query', 2, 2, 6369, 'history', 'ep:list', 'iot/access/parking/record/history/index', 'ParkingRecord', 0, 1, 1, 0, '1', NOW(), '1', NOW(), 0);

-- 4.3 月卡充值记录
INSERT INTO `ch_ibms`.`system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES
(6373, '月卡充值记录', 'iot:parking:recharge-record:query', 2, 3, 6369, 'recharge', 'ep:wallet', 'iot/access/parking/record/recharge/index', 'ParkingRechargeRecord', 0, 1, 1, 0, '1', NOW(), '1', NOW(), 0);

-- =============================================
-- 为超级管理员角色分配所有停车场权限
-- =============================================
-- 查询超级管理员角色ID
-- SELECT id FROM ch_ibms.system_role WHERE code = 'super_admin';
-- 假设角色ID为1是超级管理员

INSERT INTO `ch_ibms`.`system_role_menu` (`role_id`, `menu_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 1, id, '1', NOW(), '1', NOW(), 0
FROM `ch_ibms`.`system_menu`
WHERE id BETWEEN 6334 AND 6373
ON DUPLICATE KEY UPDATE `update_time` = NOW();
