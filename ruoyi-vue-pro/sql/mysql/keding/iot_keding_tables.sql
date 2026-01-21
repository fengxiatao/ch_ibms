-- =============================================
-- 科鼎(Keding)协议数据库表结构
-- 全渠道量测水设施数据传输规约V2.3
-- =============================================

-- ----------------------------
-- 1. 科鼎设备表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `iot_keding_device` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `station_code` varchar(20) NOT NULL COMMENT '测站编码（唯一）',
    `device_name` varchar(100) DEFAULT NULL COMMENT '设备名称',
    `device_type` tinyint DEFAULT NULL COMMENT '设备类型：1-测控一体化闸门,2-测控分体式闸门,3-退水闸,4-节制闸,5-进水闸,6-水位计,7-流量计,8-流速仪,9-渗压计',
    `province_code` varchar(10) DEFAULT NULL COMMENT '行政区代码',
    `management_code` varchar(10) DEFAULT NULL COMMENT '管理处代码',
    `station_code_part` varchar(10) DEFAULT NULL COMMENT '站所代码',
    `pile_front` varchar(10) DEFAULT NULL COMMENT '桩号（前）',
    `pile_back` varchar(10) DEFAULT NULL COMMENT '桩号（后）',
    `manufacturer` varchar(10) DEFAULT NULL COMMENT '设备厂家',
    `sequence_no` varchar(10) DEFAULT NULL COMMENT '顺序编号',
    `tea_key` varchar(64) DEFAULT NULL COMMENT 'TEA加密密钥（JSON数组格式）',
    `password` varchar(10) DEFAULT NULL COMMENT '设备密码',
    `status` tinyint DEFAULT 0 COMMENT '状态：0-离线,1-在线',
    `last_heartbeat` datetime DEFAULT NULL COMMENT '最后心跳时间',
    `creator` varchar(64) DEFAULT '' COMMENT '创建者',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` varchar(64) DEFAULT '' COMMENT '更新者',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_station_code` (`station_code`, `deleted`),
    KEY `idx_device_type` (`device_type`),
    KEY `idx_status` (`status`),
    KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='科鼎设备表';

-- ----------------------------
-- 2. 科鼎报警记录表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `iot_keding_alarm` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `device_id` bigint DEFAULT NULL COMMENT '设备ID',
    `station_code` varchar(20) NOT NULL COMMENT '测站编码',
    `alarm_type` varchar(50) NOT NULL COMMENT '报警类型',
    `alarm_value` varchar(100) DEFAULT NULL COMMENT '报警值',
    `alarm_time` datetime NOT NULL COMMENT '报警时间',
    `status` tinyint DEFAULT 0 COMMENT '状态：0-未确认,1-已确认',
    `ack_time` datetime DEFAULT NULL COMMENT '确认时间',
    `ack_user` varchar(64) DEFAULT NULL COMMENT '确认人',
    `creator` varchar(64) DEFAULT '' COMMENT '创建者',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` varchar(64) DEFAULT '' COMMENT '更新者',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
    PRIMARY KEY (`id`),
    KEY `idx_device_id` (`device_id`),
    KEY `idx_station_code` (`station_code`),
    KEY `idx_alarm_time` (`alarm_time`),
    KEY `idx_status` (`status`),
    KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='科鼎报警记录表';

-- ----------------------------
-- 3. 科鼎控制记录表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `iot_keding_control_log` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `device_id` bigint DEFAULT NULL COMMENT '设备ID',
    `station_code` varchar(20) NOT NULL COMMENT '测站编码',
    `control_type` varchar(50) NOT NULL COMMENT '控制类型',
    `control_params` varchar(500) DEFAULT NULL COMMENT '控制参数(JSON)',
    `result` tinyint DEFAULT NULL COMMENT '结果：0-失败,1-成功',
    `error_message` varchar(500) DEFAULT NULL COMMENT '错误信息',
    `operator` varchar(64) DEFAULT NULL COMMENT '操作员',
    `operate_time` datetime NOT NULL COMMENT '操作时间',
    `creator` varchar(64) DEFAULT '' COMMENT '创建者',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` varchar(64) DEFAULT '' COMMENT '更新者',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
    PRIMARY KEY (`id`),
    KEY `idx_device_id` (`device_id`),
    KEY `idx_station_code` (`station_code`),
    KEY `idx_operate_time` (`operate_time`),
    KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='科鼎控制记录表';

-- ----------------------------
-- 4. 科鼎固件表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `iot_keding_firmware` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `name` varchar(100) NOT NULL COMMENT '固件名称',
    `version` varchar(50) NOT NULL COMMENT '版本号',
    `device_type` tinyint DEFAULT NULL COMMENT '适用设备类型',
    `file_path` varchar(500) DEFAULT NULL COMMENT '文件路径',
    `file_size` bigint DEFAULT NULL COMMENT '文件大小',
    `file_md5` varchar(32) DEFAULT NULL COMMENT 'MD5校验值',
    `description` varchar(500) DEFAULT NULL COMMENT '描述',
    `creator` varchar(64) DEFAULT '' COMMENT '创建者',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` varchar(64) DEFAULT '' COMMENT '更新者',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
    PRIMARY KEY (`id`),
    KEY `idx_device_type` (`device_type`),
    KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='科鼎固件表';

-- ----------------------------
-- 5. 科鼎升级任务表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `iot_keding_upgrade_task` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `device_id` bigint DEFAULT NULL COMMENT '设备ID',
    `station_code` varchar(20) NOT NULL COMMENT '测站编码',
    `firmware_id` bigint DEFAULT NULL COMMENT '固件ID',
    `firmware_version` varchar(50) DEFAULT NULL COMMENT '固件版本',
    `status` tinyint DEFAULT 0 COMMENT '状态：0-待执行,1-进行中,2-成功,3-失败,4-已取消,5-已拒绝',
    `progress` int DEFAULT 0 COMMENT '进度(0-100)',
    `total_frames` int DEFAULT NULL COMMENT '总帧数',
    `sent_frames` int DEFAULT 0 COMMENT '已发送帧数',
    `retry_count` int DEFAULT 0 COMMENT '重试次数',
    `start_time` datetime DEFAULT NULL COMMENT '开始时间',
    `end_time` datetime DEFAULT NULL COMMENT '结束时间',
    `error_message` varchar(500) DEFAULT NULL COMMENT '错误信息',
    `creator` varchar(64) DEFAULT '' COMMENT '创建者',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` varchar(64) DEFAULT '' COMMENT '更新者',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
    PRIMARY KEY (`id`),
    KEY `idx_device_id` (`device_id`),
    KEY `idx_station_code` (`station_code`),
    KEY `idx_status` (`status`),
    KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='科鼎升级任务表';

-- ----------------------------
-- 6. 科鼎升级日志表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `iot_keding_upgrade_log` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `task_id` bigint NOT NULL COMMENT '任务ID',
    `action` varchar(50) NOT NULL COMMENT '动作：START/PROGRESS/END/CANCEL/ERROR',
    `old_status` tinyint DEFAULT NULL COMMENT '原状态',
    `new_status` tinyint DEFAULT NULL COMMENT '新状态',
    `progress` int DEFAULT NULL COMMENT '当前进度',
    `message` varchar(500) DEFAULT NULL COMMENT '日志消息',
    `creator` varchar(64) DEFAULT '' COMMENT '创建者',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
    PRIMARY KEY (`id`),
    KEY `idx_task_id` (`task_id`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='科鼎升级日志表';
