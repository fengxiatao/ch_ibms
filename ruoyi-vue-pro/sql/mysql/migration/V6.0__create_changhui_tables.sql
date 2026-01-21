-- =============================================
-- 长辉(Changhui)设备数据库表结构
-- 统一管理长辉、科鼎、德通等使用相同TCP协议的设备
-- 基于全渠道量测水设施数据传输规约V2.3（IP9500_OPC协议）
-- =============================================

-- ----------------------------
-- 1. 长辉设备表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `changhui_device` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `station_code` varchar(20) NOT NULL COMMENT '测站编码（唯一）：行政区代码(2B)+管理处代码(1B)+站所代码(1B)+桩号(3B)+设备类型(1B)+设备厂家(1B)+顺序编号(1B)',
    `device_name` varchar(100) DEFAULT NULL COMMENT '设备名称',
    `device_type` tinyint DEFAULT NULL COMMENT '设备类型：1-测控一体化闸门,2-测控分体式闸门,3-退水闸,4-节制闸,5-进水闸,6-水位计,7-流量计,8-流速仪,9-渗压计',
    `province_code` varchar(10) DEFAULT NULL COMMENT '行政区代码（省编码）',
    `management_code` varchar(10) DEFAULT NULL COMMENT '管理处代码（管理单位编码）',
    `station_code_part` varchar(10) DEFAULT NULL COMMENT '站所代码（测站编码部分）',
    `pile_front` varchar(10) DEFAULT NULL COMMENT '桩号（前）',
    `pile_back` varchar(10) DEFAULT NULL COMMENT '桩号（后）',
    `manufacturer` varchar(50) DEFAULT NULL COMMENT '设备厂家/制造商',
    `sequence_no` varchar(10) DEFAULT NULL COMMENT '顺序编号/序列号',
    `tea_key` varchar(100) DEFAULT NULL COMMENT 'TEA加密密钥（JSON数组格式，如[1234567890,1234567890,1234567890,1234567890]）',
    `password` varchar(20) DEFAULT NULL COMMENT '设备密码',
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='长辉设备表';

-- ----------------------------
-- 2. 长辉数据采集表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `changhui_data` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `device_id` bigint DEFAULT NULL COMMENT '设备ID',
    `station_code` varchar(20) NOT NULL COMMENT '测站编码',
    `indicator` varchar(50) NOT NULL COMMENT '指标类型：waterLevel-水位(m),instantFlow-瞬时流量(L/s),instantVelocity-瞬时流速(m/s),cumulativeFlow-累计流量(m³),gatePosition-闸位(mm),temperature-温度(°C),seepagePressure-渗压(kPa),load-荷载(kN)',
    `value` decimal(20,6) DEFAULT NULL COMMENT '数值',
    `timestamp` datetime NOT NULL COMMENT '采集时间/数据时间戳',
    `creator` varchar(64) DEFAULT '' COMMENT '创建者',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` varchar(64) DEFAULT '' COMMENT '更新者',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
    PRIMARY KEY (`id`),
    KEY `idx_device_id` (`device_id`),
    KEY `idx_station_code` (`station_code`),
    KEY `idx_indicator` (`indicator`),
    KEY `idx_timestamp` (`timestamp`),
    KEY `idx_station_indicator_time` (`station_code`, `indicator`, `timestamp`),
    KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='长辉数据采集表';

-- ----------------------------
-- 3. 长辉报警记录表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `changhui_alarm` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `device_id` bigint DEFAULT NULL COMMENT '设备ID',
    `station_code` varchar(20) NOT NULL COMMENT '测站编码',
    `alarm_type` varchar(50) NOT NULL COMMENT '报警类型：OVER_TORQUE-过力矩,OVER_CURRENT-过电流,OVER_VOLTAGE-过电压,LOW_VOLTAGE-低电压,WATER_LEVEL-水位超限,GATE_POSITION-闸位超限',
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
    KEY `idx_alarm_type` (`alarm_type`),
    KEY `idx_alarm_time` (`alarm_time`),
    KEY `idx_status` (`status`),
    KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='长辉报警记录表';

-- ----------------------------
-- 4. 长辉控制日志表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `changhui_control_log` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `device_id` bigint DEFAULT NULL COMMENT '设备ID',
    `station_code` varchar(20) NOT NULL COMMENT '测站编码',
    `control_type` varchar(50) NOT NULL COMMENT '控制类型：MODE_SWITCH-模式切换,MANUAL_CONTROL-手动控制,AUTO_CONTROL-自动控制',
    `control_params` varchar(500) DEFAULT NULL COMMENT '控制参数(JSON格式)，如{"action":"rise"}或{"targetValue":1.5,"controlMode":"flow"}',
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
    KEY `idx_control_type` (`control_type`),
    KEY `idx_operate_time` (`operate_time`),
    KEY `idx_result` (`result`),
    KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='长辉控制日志表';

-- ----------------------------
-- 5. 长辉固件表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `changhui_firmware` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `name` varchar(100) NOT NULL COMMENT '固件名称',
    `version` varchar(50) NOT NULL COMMENT '版本号',
    `device_type` tinyint DEFAULT NULL COMMENT '适用设备类型',
    `file_path` varchar(500) DEFAULT NULL COMMENT '文件路径',
    `file_size` bigint DEFAULT NULL COMMENT '文件大小（字节）',
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
    KEY `idx_version` (`version`),
    KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='长辉固件表';

-- ----------------------------
-- 6. 长辉升级任务表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `changhui_upgrade_task` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `device_id` bigint DEFAULT NULL COMMENT '设备ID',
    `station_code` varchar(20) NOT NULL COMMENT '测站编码',
    `firmware_id` bigint DEFAULT NULL COMMENT '固件ID',
    `firmware_version` varchar(50) DEFAULT NULL COMMENT '固件版本',
    `upgrade_mode` tinyint DEFAULT 0 COMMENT '升级模式：0-TCP帧传输,1-HTTP URL下载',
    `firmware_url` varchar(500) DEFAULT NULL COMMENT '固件下载URL（HTTP模式使用）',
    `status` tinyint DEFAULT 0 COMMENT '状态：0-待执行,1-进行中,2-成功,3-失败,4-已取消,5-已拒绝',
    `progress` int DEFAULT 0 COMMENT '进度(0-100)',
    `total_frames` int DEFAULT NULL COMMENT '总帧数（TCP帧传输模式使用）',
    `sent_frames` int DEFAULT 0 COMMENT '已发送帧数（TCP帧传输模式使用）',
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
    KEY `idx_firmware_id` (`firmware_id`),
    KEY `idx_status` (`status`),
    KEY `idx_start_time` (`start_time`),
    KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='长辉升级任务表';
