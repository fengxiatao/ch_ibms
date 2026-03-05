-- =====================================================
-- 访客管理和停车场管理模块升级脚本
-- 日期: 2026-01-30
-- 说明: 根据新版原型重构访客管理和停车场管理模块
-- =====================================================

-- 1. 创建停车场黑名单车辆表
CREATE TABLE IF NOT EXISTS `iot_parking_blacklist` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `plate_number` varchar(20) NOT NULL COMMENT '车牌号码',
  `reason` varchar(500) NOT NULL COMMENT '黑名单原因',
  `end_time` datetime NOT NULL COMMENT '结束时间（黑名单有效期）',
  `lot_id` bigint DEFAULT NULL COMMENT '车场ID（可选，为空表示所有车场）',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0-生效中 1-已解除',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  KEY `idx_plate_number` (`plate_number`),
  KEY `idx_end_time` (`end_time`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='停车场黑名单车辆表';

-- 2. 创建访客来访事由表
CREATE TABLE IF NOT EXISTS `iot_visitor_reason` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `reason_name` varchar(100) NOT NULL COMMENT '来访事由名称',
  `sort` int DEFAULT 0 COMMENT '排序',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0-正常 1-停用',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  KEY `idx_tenant_id` (`tenant_id`),
  UNIQUE KEY `idx_reason_tenant` (`reason_name`, `tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='访客来访事由表';

-- 2.1 如果表已存在但没有唯一索引，添加唯一索引（忽略错误）
-- ALTER IGNORE TABLE iot_visitor_reason ADD UNIQUE INDEX idx_reason_tenant (reason_name, tenant_id);

-- 3. 插入默认来访事由（INSERT IGNORE 避免重复插入报错）
INSERT IGNORE INTO iot_visitor_reason (reason_name, sort, status, tenant_id) VALUES 
('参观', 1, 0, 1),
('拜访', 2, 0, 1),
('面试开会', 3, 0, 1),
('送货', 4, 0, 1),
('维修', 5, 0, 1),
('其他', 99, 0, 1);

-- 4. 为车场表添加免费时长字段（如果不存在）
-- 注意：MySQL 5.7 不支持 IF NOT EXISTS 语法，需要先检查
SET @column_exists = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'iot_parking_lot' 
    AND COLUMN_NAME = 'free_duration'
);

SET @sql = IF(@column_exists = 0, 
    'ALTER TABLE iot_parking_lot ADD COLUMN `free_duration` int DEFAULT 120 COMMENT ''免费时长（分钟）'' AFTER `monthly_fee`',
    'SELECT ''Column free_duration already exists''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 5. 创建访客开门记录表（如果不存在）
CREATE TABLE IF NOT EXISTS `iot_visitor_access_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `apply_id` bigint NOT NULL COMMENT '访客申请ID',
  `visitor_id` bigint NOT NULL COMMENT '访客ID',
  `device_id` bigint NOT NULL COMMENT '设备ID',
  `device_name` varchar(100) DEFAULT NULL COMMENT '设备名称',
  `channel_id` bigint DEFAULT NULL COMMENT '通道ID',
  `channel_name` varchar(100) DEFAULT NULL COMMENT '通道名称',
  `verify_mode` varchar(20) DEFAULT NULL COMMENT '验证方式：CARD-刷卡 FACE-人脸 FINGERPRINT-指纹',
  `event_time` datetime NOT NULL COMMENT '事件时间',
  `success` tinyint NOT NULL DEFAULT 1 COMMENT '是否成功：0-失败 1-成功',
  `capture_url` varchar(500) DEFAULT NULL COMMENT '抓拍图片URL',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  KEY `idx_apply_id` (`apply_id`),
  KEY `idx_visitor_id` (`visitor_id`),
  KEY `idx_device_id` (`device_id`),
  KEY `idx_event_time` (`event_time`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='访客开门记录表';

-- =====================================================
-- 菜单权限配置
-- =====================================================

-- 6. 在访客管理(5047)下新增"待访列表"菜单
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) 
VALUES (7120, '待访列表', 'iot:visitor-apply:waiting', 2, 3, 5047, 'waiting', 'ep:clock', 'iot/visitor/apply/waiting', 'VisitorWaiting', 0, 1, 1, 1, '1', NOW(), '1', NOW(), 0)
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- 7. 在车场配置(6339)下新增"黑名单车辆"菜单
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) 
VALUES (7121, '黑名单车辆', 'iot:parking:blacklist:query', 2, 8, 6339, 'blacklist', 'ep:warning', 'iot/access/parking/vehicle/blacklist/index', 'ParkingBlacklist', 0, 1, 1, 1, '1', NOW(), '1', NOW(), 0)
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- 8. 为待访列表添加操作按钮权限
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES 
(7122, '签到', 'iot:visitor-apply:check-in', 3, 1, 7120, '', '', '', '', 0, 1, 1, 1, '1', NOW(), '1', NOW(), 0),
(7123, '签离', 'iot:visitor-apply:check-out', 3, 2, 7120, '', '', '', '', 0, 1, 1, 1, '1', NOW(), '1', NOW(), 0),
(7124, '下发权限', 'iot:visitor-apply:dispatch', 3, 3, 7120, '', '', '', '', 0, 1, 1, 1, '1', NOW(), '1', NOW(), 0),
(7125, '回收权限', 'iot:visitor-apply:revoke', 3, 4, 7120, '', '', '', '', 0, 1, 1, 1, '1', NOW(), '1', NOW(), 0)
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- 9. 为黑名单车辆添加操作按钮权限
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES 
(7126, '新增黑名单', 'iot:parking:blacklist:create', 3, 1, 7121, '', '', '', '', 0, 1, 1, 1, '1', NOW(), '1', NOW(), 0),
(7127, '编辑黑名单', 'iot:parking:blacklist:update', 3, 2, 7121, '', '', '', '', 0, 1, 1, 1, '1', NOW(), '1', NOW(), 0),
(7128, '删除黑名单', 'iot:parking:blacklist:delete', 3, 3, 7121, '', '', '', '', 0, 1, 1, 1, '1', NOW(), '1', NOW(), 0),
(7129, '解除黑名单', 'iot:parking:blacklist:release', 3, 4, 7121, '', '', '', '', 0, 1, 1, 1, '1', NOW(), '1', NOW(), 0)
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- 10. 将新菜单权限授予超级管理员角色(role_id=1)
INSERT IGNORE INTO system_role_menu (role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES 
(1, 7120, '1', NOW(), '1', NOW(), 0, 1),
(1, 7121, '1', NOW(), '1', NOW(), 0, 1),
(1, 7122, '1', NOW(), '1', NOW(), 0, 1),
(1, 7123, '1', NOW(), '1', NOW(), 0, 1),
(1, 7124, '1', NOW(), '1', NOW(), 0, 1),
(1, 7125, '1', NOW(), '1', NOW(), 0, 1),
(1, 7126, '1', NOW(), '1', NOW(), 0, 1),
(1, 7127, '1', NOW(), '1', NOW(), 0, 1),
(1, 7128, '1', NOW(), '1', NOW(), 0, 1),
(1, 7129, '1', NOW(), '1', NOW(), 0, 1);

-- 11. 调整访客管理下的菜单排序
UPDATE system_menu SET sort = 1 WHERE id = 5057;  -- 访客预约
UPDATE system_menu SET sort = 2 WHERE id = 7120;  -- 待访列表
UPDATE system_menu SET sort = 3 WHERE id = 6332;  -- 来访记录

-- 12. 隐藏重复的老菜单
UPDATE system_menu SET visible = 0 WHERE id IN (5048, 5058, 5059, 5060);

-- =====================================================
-- 删除停车场模块废弃的菜单（软删除）
-- =====================================================

-- 13. 删除子菜单（按钮权限）
UPDATE system_menu SET deleted = 1 WHERE id IN (
  6336, 6337, 6338,   -- 道闸管理按钮
  6345, 6346, 6347,   -- 车道管理按钮
  6349, 6350, 6351,   -- 免费车辆按钮
  6358, 6359, 6360,   -- 放行规则按钮
  6362, 6363, 6364,   -- 收费规则按钮
  6366, 6367, 6368    -- 收费规则应用按钮
);

-- 14. 删除主菜单
UPDATE system_menu SET deleted = 1 WHERE id IN (
  6335,  -- 道闸设备管理
  6344,  -- 配置车道
  6348,  -- 免费车辆
  6357,  -- 配置放行规则
  6361,  -- 配置收费规则
  6365,  -- 收费规则应用
  6373   -- 月卡充值记录
);

-- 15. 删除对应的角色菜单关联
DELETE FROM system_role_menu WHERE menu_id IN (
  6335, 6336, 6337, 6338,
  6344, 6345, 6346, 6347,
  6348, 6349, 6350, 6351,
  6357, 6358, 6359, 6360,
  6361, 6362, 6363, 6364,
  6365, 6366, 6367, 6368,
  6373
);

-- =====================================================
-- 前端页面变更说明
-- =====================================================
-- 
-- 访客管理模块：
-- 1. 访客预约列表 (apply/index.vue) - 重构为新样式，增加日期快捷筛选
-- 2. 待访列表 (apply/waiting.vue) - 新增页面，展示已审批通过的访客
-- 3. 访客记录 (record/index.vue) - 重构为新样式，增加统计卡片
-- 4. 访客登记表单 (apply/VisitorApplyForm.vue) - 重构为新样式
-- 5. 开门记录弹窗 (record/VisitorAccessRecordDialog.vue) - 简化显示
--
-- 停车场管理模块：
-- 1. 黑名单车辆 (vehicle/blacklist/index.vue) - 新增页面
-- 2. 车场信息 (lot/index.vue) - 重构为详情+车道列表+开关闸
-- 3. 月卡车辆 (vehicle/monthly/index.vue) - 简化字段显示
-- 4. 在场车辆 (record/present/index.vue) - 增加统计卡片
-- 5. 车辆进出记录 (record/history/index.vue) - 调整字段和样式
--
-- =====================================================
