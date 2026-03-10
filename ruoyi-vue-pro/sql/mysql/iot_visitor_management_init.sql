-- =====================================================
-- 新访客管理（依据新原型）
-- 功能：建表 + 测试数据 + 菜单/权限初始化（用于动态路由）
-- 数据库：MySQL 8.x
-- =====================================================

-- ----------------------------
-- 1) 访客预约单
-- ----------------------------
CREATE TABLE IF NOT EXISTS `iot_visitor_appointment` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(64) NOT NULL COMMENT '访客姓名',
  `phone` varchar(32) NOT NULL COMMENT '联系电话',
  `type` varchar(32) NOT NULL COMMENT '访客类型：business/vip/contractor/interview',
  `company` varchar(128) DEFAULT NULL COMMENT '所属单位',
  `host` varchar(64) NOT NULL COMMENT '被访人',
  `host_dept` varchar(64) DEFAULT NULL COMMENT '被访人部门',
  `visit_time` datetime NOT NULL COMMENT '预约来访时间',
  `reason` varchar(255) NOT NULL COMMENT '来访事由',
  `areas` json NOT NULL COMMENT '访问区域（JSON数组）',
  `id_card` varchar(32) DEFAULT NULL COMMENT '身份证号',
  `car_no` varchar(32) DEFAULT NULL COMMENT '车牌号',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `status` varchar(16) NOT NULL DEFAULT 'pending' COMMENT '审批状态：pending/approved/rejected/cancelled',
  `approval_comment` varchar(500) DEFAULT NULL COMMENT '审批意见',
  `approval_time` datetime DEFAULT NULL COMMENT '审批时间',
  `approver_id` bigint DEFAULT NULL COMMENT '审批人用户ID',
  `sign_in_time` datetime DEFAULT NULL COMMENT '签到时间',
  `sign_out_time` datetime DEFAULT NULL COMMENT '签离时间',
  `current_location` varchar(128) DEFAULT NULL COMMENT '当前位置',
  `rating` decimal(3,2) DEFAULT NULL COMMENT '评价分数(0-5)',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  KEY `idx_visit_time` (`visit_time`),
  KEY `idx_status` (`status`),
  KEY `idx_type` (`type`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='新访客管理-访客预约单';

-- ----------------------------
-- 1.1) 访客异常事件
-- ----------------------------
CREATE TABLE IF NOT EXISTS `iot_visitor_abnormal_event` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `appointment_id` bigint DEFAULT NULL COMMENT '关联预约ID',
  `visitor_name` varchar(64) NOT NULL COMMENT '访客姓名',
  `visitor_phone` varchar(32) DEFAULT NULL COMMENT '访客电话',
  `abnormal_type` varchar(32) NOT NULL COMMENT '异常类型：overtime/unauthorized/noshow',
  `risk_level` varchar(16) NOT NULL COMMENT '风险等级：high/medium/low',
  `details` varchar(500) DEFAULT NULL COMMENT '详情',
  `event_time` datetime NOT NULL COMMENT '发生时间',
  `current_status` varchar(128) DEFAULT NULL COMMENT '当前状态/位置',
  `handled` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否已处理',
  `handler_id` bigint DEFAULT NULL COMMENT '处理人',
  `handle_time` datetime DEFAULT NULL COMMENT '处理时间',
  `handle_result` varchar(500) DEFAULT NULL COMMENT '处理结果',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  KEY `idx_event_time` (`event_time`),
  KEY `idx_type_level` (`abnormal_type`, `risk_level`),
  KEY `idx_handled` (`handled`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='新访客管理-异常事件';

-- ----------------------------
-- 2) 测试数据（可重复执行）
-- ----------------------------
INSERT INTO `iot_visitor_appointment`
(`id`,`name`,`phone`,`type`,`company`,`host`,`host_dept`,`visit_time`,`reason`,`areas`,`id_card`,`car_no`,`remark`,`status`,
 `approval_comment`,`approval_time`,`approver_id`,`sign_in_time`,`sign_out_time`,`current_location`,`rating`,
 `tenant_id`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
VALUES
(8,'周candidate','13700002222','interview','自由职业','技术总监','研发部',NOW() + INTERVAL 2 HOUR,'终面',JSON_ARRAY('meeting'),'','',NULL,'pending',
 NULL,NULL,NULL,NULL,NULL,NULL,NULL,
 1,'1',NOW(),'1',NOW(),b'0'),
(9,'吴顾问','13600003333','business','深圳管理咨询公司','咨询部','外部顾问',NOW() + INTERVAL 1 HOUR,'项目咨询',JSON_ARRAY('lobby','meeting'),NULL,NULL,NULL,'pending',
 NULL,NULL,NULL,NULL,NULL,NULL,NULL,
 1,'1',NOW(),'1',NOW(),b'0'),
(6,'赵总','13800009999','vip','上海科技有限公司','VP','战略部',NOW() + INTERVAL 3 HOUR,'战略合作洽谈',JSON_ARRAY('meeting'),NULL,NULL,NULL,'approved',
 '同意预约',NOW(),1,NULL,NULL,NULL,NULL,
 1,'1',NOW(),'1',NOW(),b'0')
ON DUPLICATE KEY UPDATE
  `name`=VALUES(`name`),
  `phone`=VALUES(`phone`),
  `type`=VALUES(`type`),
  `company`=VALUES(`company`),
  `host`=VALUES(`host`),
  `host_dept`=VALUES(`host_dept`),
  `visit_time`=VALUES(`visit_time`),
  `reason`=VALUES(`reason`),
  `areas`=VALUES(`areas`),
  `status`=VALUES(`status`),
  `approval_comment`=VALUES(`approval_comment`),
  `approval_time`=VALUES(`approval_time`);

-- 2.1) 异常事件测试数据（可重复执行）
INSERT INTO `iot_visitor_abnormal_event`
(`id`,`appointment_id`,`visitor_name`,`visitor_phone`,`abnormal_type`,`risk_level`,`details`,`event_time`,`current_status`,`handled`,
 `tenant_id`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
VALUES
(1001, 8, '周candidate', '13700002222', 'noshow', 'medium', '预约时间已过，未按时到访', NOW() - INTERVAL 30 MINUTE, '未到访', b'0',
 1,'1',NOW(),'1',NOW(),b'0'),
(1002, NULL, '李四', '13900005678', 'unauthorized', 'high', '无权限区域闯入被拦截', NOW() - INTERVAL 10 MINUTE, '试图进入：研发区', b'0',
 1,'1',NOW(),'1',NOW(),b'0')
ON DUPLICATE KEY UPDATE
  `details`=VALUES(`details`),
  `event_time`=VALUES(`event_time`),
  `current_status`=VALUES(`current_status`),
  `handled`=VALUES(`handled`);

-- ----------------------------
-- 3) 菜单/权限初始化（动态路由）
-- 说明：系统已有“新访客管理”(menu_id=7136)，这里补齐按钮权限
-- ----------------------------
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name,
                         status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
VALUES
(71360, '查询', 'security:visitor:query', 3, 1, 7136, '', '', '', '', 0, 1, 1, 1, '1', NOW(), '1', NOW(), 0),
(71361, '新增预约', 'security:visitor:create', 3, 2, 7136, '', '', '', '', 0, 1, 1, 1, '1', NOW(), '1', NOW(), 0),
(71362, '修改预约', 'security:visitor:update', 3, 3, 7136, '', '', '', '', 0, 1, 1, 1, '1', NOW(), '1', NOW(), 0),
(71363, '删除预约', 'security:visitor:delete', 3, 4, 7136, '', '', '', '', 0, 1, 1, 1, '1', NOW(), '1', NOW(), 0),
(71364, '审批', 'security:visitor:approve', 3, 5, 7136, '', '', '', '', 0, 1, 1, 1, '1', NOW(), '1', NOW(), 0),
(71365, '签离', 'security:visitor:sign-out', 3, 6, 7136, '', '', '', '', 0, 1, 1, 1, '1', NOW(), '1', NOW(), 0)
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- 超级管理员授权（role_id=1）
INSERT IGNORE INTO system_role_menu (role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id)
VALUES
(1, 7136, '1', NOW(), '1', NOW(), 0, 1),
(1, 71360, '1', NOW(), '1', NOW(), 0, 1),
(1, 71361, '1', NOW(), '1', NOW(), 0, 1),
(1, 71362, '1', NOW(), '1', NOW(), 0, 1),
(1, 71363, '1', NOW(), '1', NOW(), 0, 1),
(1, 71364, '1', NOW(), '1', NOW(), 0, 1),
(1, 71365, '1', NOW(), '1', NOW(), 0, 1);

