-- =============================================
-- 智慧通行（门禁）模块数据库表
-- 组织架构、人员管理、权限组、授权进度
-- 支持多租户(tenant_id)和软删除(deleted)
-- =============================================

-- ----------------------------
-- 1. 组织架构表（部门）
-- ----------------------------
DROP TABLE IF EXISTS `iot_access_department`;
CREATE TABLE `iot_access_department` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '部门ID',
    `parent_id` bigint NOT NULL DEFAULT 0 COMMENT '父部门ID（0表示根节点）',
    `dept_name` varchar(100) NOT NULL COMMENT '部门名称',
    `dept_code` varchar(50) DEFAULT NULL COMMENT '部门编码',
    `sort` int NOT NULL DEFAULT 0 COMMENT '显示顺序',
    `leader` varchar(100) DEFAULT NULL COMMENT '负责人',
    `phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
    `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
    `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0-正常，1-停用',
    `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
    `creator` varchar(64) DEFAULT '' COMMENT '创建者',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` varchar(64) DEFAULT '' COMMENT '更新者',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_dept_code` (`dept_code`),
    KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='门禁组织架构表';

-- ----------------------------
-- 2. 人员表
-- ----------------------------
DROP TABLE IF EXISTS `iot_access_person`;
CREATE TABLE `iot_access_person` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '人员ID',
    `person_code` varchar(50) NOT NULL COMMENT '人员编号',
    `person_name` varchar(100) NOT NULL COMMENT '人员姓名',
    `person_type` tinyint NOT NULL DEFAULT 1 COMMENT '人员类型：1-员工，2-访客，3-临时人员',
    `dept_id` bigint DEFAULT NULL COMMENT '部门ID',
    `id_card` varchar(20) DEFAULT NULL COMMENT '身份证号',
    `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
    `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
    `face_url` varchar(500) DEFAULT NULL COMMENT '人脸照片URL',
    `valid_start` datetime DEFAULT NULL COMMENT '有效期开始',
    `valid_end` datetime DEFAULT NULL COMMENT '有效期结束',
    `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0-正常，1-停用，2-过期',
    `remark` varchar(500) DEFAULT NULL COMMENT '备注',
    `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
    `creator` varchar(64) DEFAULT '' COMMENT '创建者',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` varchar(64) DEFAULT '' COMMENT '更新者',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_person_code` (`person_code`, `tenant_id`, `deleted`),
    KEY `idx_dept_id` (`dept_id`),
    KEY `idx_person_type` (`person_type`),
    KEY `idx_status` (`status`),
    KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='门禁人员表';

-- ----------------------------
-- 3. 人员凭证表
-- ----------------------------
DROP TABLE IF EXISTS `iot_access_person_credential`;
CREATE TABLE `iot_access_person_credential` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '凭证ID',
    `person_id` bigint NOT NULL COMMENT '人员ID',
    `credential_type` varchar(20) NOT NULL COMMENT '凭证类型：PASSWORD-密码，CARD-卡片，FINGERPRINT-指纹，FACE-人脸',
    `credential_data` varchar(500) DEFAULT NULL COMMENT '凭证数据（密码加密存储，卡号明文，指纹/人脸为文件路径）',
    `card_no` varchar(50) DEFAULT NULL COMMENT '卡号（仅卡片类型）',
    `finger_index` int DEFAULT NULL COMMENT '指纹序号（仅指纹类型，0-9）',
    `device_synced` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否已同步到设备',
    `sync_time` datetime DEFAULT NULL COMMENT '同步时间',
    `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0-正常，1-停用',
    `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
    `creator` varchar(64) DEFAULT '' COMMENT '创建者',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` varchar(64) DEFAULT '' COMMENT '更新者',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_card_no` (`card_no`, `tenant_id`, `deleted`),
    KEY `idx_person_id` (`person_id`),
    KEY `idx_credential_type` (`credential_type`),
    KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='门禁人员凭证表';


-- ----------------------------
-- 4. 权限组表
-- ----------------------------
DROP TABLE IF EXISTS `iot_access_permission_group`;
CREATE TABLE `iot_access_permission_group` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '权限组ID',
    `group_name` varchar(100) NOT NULL COMMENT '权限组名称',
    `time_template_id` bigint DEFAULT NULL COMMENT '时间模板ID',
    `auth_mode` varchar(50) DEFAULT 'CARD' COMMENT '认证方式：CARD-刷卡，FACE-人脸，FINGERPRINT-指纹，PASSWORD-密码，MULTI-多重认证',
    `description` varchar(500) DEFAULT NULL COMMENT '描述',
    `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0-正常，1-停用',
    `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
    `creator` varchar(64) DEFAULT '' COMMENT '创建者',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` varchar(64) DEFAULT '' COMMENT '更新者',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`),
    KEY `idx_time_template_id` (`time_template_id`),
    KEY `idx_status` (`status`),
    KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='门禁权限组表';

-- ----------------------------
-- 5. 权限组设备关联表
-- ----------------------------
DROP TABLE IF EXISTS `iot_access_permission_group_device`;
CREATE TABLE `iot_access_permission_group_device` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `group_id` bigint NOT NULL COMMENT '权限组ID',
    `device_id` bigint NOT NULL COMMENT '设备ID',
    `channel_id` bigint DEFAULT NULL COMMENT '通道ID（为空表示设备所有通道）',
    `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
    `creator` varchar(64) DEFAULT '' COMMENT '创建者',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` varchar(64) DEFAULT '' COMMENT '更新者',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_group_device_channel` (`group_id`, `device_id`, `channel_id`, `deleted`),
    KEY `idx_group_id` (`group_id`),
    KEY `idx_device_id` (`device_id`),
    KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='门禁权限组设备关联表';

-- ----------------------------
-- 6. 权限组人员关联表
-- ----------------------------
DROP TABLE IF EXISTS `iot_access_permission_group_person`;
CREATE TABLE `iot_access_permission_group_person` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `group_id` bigint NOT NULL COMMENT '权限组ID',
    `person_id` bigint NOT NULL COMMENT '人员ID',
    `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
    `creator` varchar(64) DEFAULT '' COMMENT '创建者',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` varchar(64) DEFAULT '' COMMENT '更新者',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_group_person` (`group_id`, `person_id`, `deleted`),
    KEY `idx_group_id` (`group_id`),
    KEY `idx_person_id` (`person_id`),
    KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='门禁权限组人员关联表';

-- ----------------------------
-- 7. 时间模板表
-- ----------------------------
DROP TABLE IF EXISTS `iot_access_time_template`;
CREATE TABLE `iot_access_time_template` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '模板ID',
    `template_name` varchar(100) NOT NULL COMMENT '模板名称',
    `week_config` json DEFAULT NULL COMMENT '周配置（JSON格式，包含周一到周日的时间段）',
    `holiday_config` json DEFAULT NULL COMMENT '节假日配置（JSON格式）',
    `description` varchar(500) DEFAULT NULL COMMENT '描述',
    `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0-正常，1-停用',
    `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
    `creator` varchar(64) DEFAULT '' COMMENT '创建者',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` varchar(64) DEFAULT '' COMMENT '更新者',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`),
    KEY `idx_status` (`status`),
    KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='门禁时间模板表';


-- ----------------------------
-- 8. 授权任务表
-- ----------------------------
DROP TABLE IF EXISTS `iot_access_auth_task`;
CREATE TABLE `iot_access_auth_task` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '任务ID',
    `task_type` varchar(50) NOT NULL COMMENT '任务类型：ADD-新增权限，UPDATE-更新权限，DELETE-删除权限，SYNC-同步权限',
    `group_id` bigint DEFAULT NULL COMMENT '权限组ID',
    `device_id` bigint DEFAULT NULL COMMENT '设备ID',
    `total_count` int NOT NULL DEFAULT 0 COMMENT '总数量',
    `success_count` int NOT NULL DEFAULT 0 COMMENT '成功数量',
    `fail_count` int NOT NULL DEFAULT 0 COMMENT '失败数量',
    `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0-待执行，1-执行中，2-已完成，3-部分失败，4-全部失败',
    `start_time` datetime DEFAULT NULL COMMENT '开始时间',
    `end_time` datetime DEFAULT NULL COMMENT '结束时间',
    `error_message` varchar(500) DEFAULT NULL COMMENT '错误信息',
    `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
    `creator` varchar(64) DEFAULT '' COMMENT '创建者',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` varchar(64) DEFAULT '' COMMENT '更新者',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`),
    KEY `idx_group_id` (`group_id`),
    KEY `idx_device_id` (`device_id`),
    KEY `idx_status` (`status`),
    KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='门禁授权任务表';

-- ----------------------------
-- 9. 授权任务明细表
-- ----------------------------
DROP TABLE IF EXISTS `iot_access_auth_task_detail`;
CREATE TABLE `iot_access_auth_task_detail` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '明细ID',
    `task_id` bigint NOT NULL COMMENT '任务ID',
    `person_id` bigint NOT NULL COMMENT '人员ID',
    `device_id` bigint NOT NULL COMMENT '设备ID',
    `channel_id` bigint DEFAULT NULL COMMENT '通道ID',
    `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0-待执行，1-执行中，2-成功，3-失败',
    `retry_count` int NOT NULL DEFAULT 0 COMMENT '重试次数',
    `error_message` varchar(500) DEFAULT NULL COMMENT '错误信息',
    `execute_time` datetime DEFAULT NULL COMMENT '执行时间',
    `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
    `creator` varchar(64) DEFAULT '' COMMENT '创建者',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` varchar(64) DEFAULT '' COMMENT '更新者',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`),
    KEY `idx_task_id` (`task_id`),
    KEY `idx_person_id` (`person_id`),
    KEY `idx_device_id` (`device_id`),
    KEY `idx_status` (`status`),
    KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='门禁授权任务明细表';

-- ----------------------------
-- 10. 操作日志表
-- ----------------------------
DROP TABLE IF EXISTS `iot_access_operation_log`;
CREATE TABLE `iot_access_operation_log` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    `device_id` bigint DEFAULT NULL COMMENT '设备ID',
    `channel_id` bigint DEFAULT NULL COMMENT '通道ID',
    `operation_type` varchar(50) NOT NULL COMMENT '操作类型：OPEN_DOOR-开门，CLOSE_DOOR-关门，ALWAYS_OPEN-常开，ALWAYS_CLOSED-常闭，CANCEL_ALWAYS-取消常开常闭',
    `operation_time` datetime NOT NULL COMMENT '操作时间',
    `operator_id` bigint DEFAULT NULL COMMENT '操作人ID',
    `operator_name` varchar(100) DEFAULT NULL COMMENT '操作人姓名',
    `result` tinyint NOT NULL DEFAULT 1 COMMENT '结果：0-失败，1-成功',
    `error_message` varchar(500) DEFAULT NULL COMMENT '错误信息',
    `request_params` json DEFAULT NULL COMMENT '请求参数',
    `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
    `creator` varchar(64) DEFAULT '' COMMENT '创建者',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` varchar(64) DEFAULT '' COMMENT '更新者',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`),
    KEY `idx_device_id` (`device_id`),
    KEY `idx_channel_id` (`channel_id`),
    KEY `idx_operation_type` (`operation_type`),
    KEY `idx_operation_time` (`operation_time`),
    KEY `idx_operator_id` (`operator_id`),
    KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='门禁操作日志表';

-- ----------------------------
-- 11. 事件日志表
-- ----------------------------
DROP TABLE IF EXISTS `iot_access_event_log`;
CREATE TABLE `iot_access_event_log` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '事件ID',
    `device_id` bigint NOT NULL COMMENT '设备ID',
    `channel_id` bigint DEFAULT NULL COMMENT '通道ID',
    `event_type` varchar(50) NOT NULL COMMENT '事件类型：CARD_SWIPE-刷卡，FACE_RECOGNIZE-人脸识别，FINGERPRINT-指纹，PASSWORD-密码，DOOR_OPEN-开门，DOOR_CLOSE-关门，ALARM-报警',
    `event_time` datetime NOT NULL COMMENT '事件时间',
    `person_id` bigint DEFAULT NULL COMMENT '人员ID',
    `person_name` varchar(100) DEFAULT NULL COMMENT '人员姓名',
    `card_no` varchar(50) DEFAULT NULL COMMENT '卡号',
    `verify_mode` varchar(50) DEFAULT NULL COMMENT '验证方式',
    `verify_result` tinyint DEFAULT NULL COMMENT '验证结果：0-失败，1-成功',
    `fail_reason` varchar(200) DEFAULT NULL COMMENT '失败原因',
    `snapshot_url` varchar(500) DEFAULT NULL COMMENT '抓拍图片URL',
    `temperature` decimal(5,2) DEFAULT NULL COMMENT '体温（℃）',
    `mask_status` tinyint DEFAULT NULL COMMENT '口罩状态：0-未佩戴，1-已佩戴',
    `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
    `creator` varchar(64) DEFAULT '' COMMENT '创建者',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` varchar(64) DEFAULT '' COMMENT '更新者',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`),
    KEY `idx_device_id` (`device_id`),
    KEY `idx_channel_id` (`channel_id`),
    KEY `idx_event_type` (`event_type`),
    KEY `idx_event_time` (`event_time`),
    KEY `idx_person_id` (`person_id`),
    KEY `idx_card_no` (`card_no`),
    KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='门禁事件日志表';

-- ----------------------------
-- 初始化数据：默认时间模板
-- ----------------------------
INSERT INTO `iot_access_time_template` (`template_name`, `week_config`, `description`, `tenant_id`) VALUES
('全天候', '{"monday":["00:00-23:59"],"tuesday":["00:00-23:59"],"wednesday":["00:00-23:59"],"thursday":["00:00-23:59"],"friday":["00:00-23:59"],"saturday":["00:00-23:59"],"sunday":["00:00-23:59"]}', '7x24小时全天候通行', 0),
('工作日', '{"monday":["08:00-18:00"],"tuesday":["08:00-18:00"],"wednesday":["08:00-18:00"],"thursday":["08:00-18:00"],"friday":["08:00-18:00"],"saturday":[],"sunday":[]}', '周一至周五工作时间', 0);
