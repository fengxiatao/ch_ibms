-- =====================================================
-- 智慧楼宇模块数据库设计
-- 包含：环境监测、智能照明、楼宇自控、能耗计量
-- =====================================================

-- -------------------------------------------
-- 1. 环境监测模块
-- -------------------------------------------

-- 环境传感器表
DROP TABLE IF EXISTS `ibms_env_sensor`;
CREATE TABLE `ibms_env_sensor` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `sensor_code` varchar(50) NOT NULL COMMENT '传感器编号',
    `sensor_name` varchar(100) NOT NULL COMMENT '传感器名称',
    `sensor_type` tinyint NOT NULL COMMENT '传感器类型：1-温湿度 2-空气质量 3-光照 4-噪音 5-压力',
    `area_id` bigint DEFAULT NULL COMMENT '所属区域ID',
    `area_name` varchar(100) DEFAULT NULL COMMENT '所属区域名称',
    `floor` varchar(20) DEFAULT NULL COMMENT '楼层',
    `location` varchar(200) DEFAULT NULL COMMENT '详细位置',
    `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-离线 1-在线 2-告警 3-故障',
    `last_online_time` datetime DEFAULT NULL COMMENT '最后在线时间',
    `install_time` datetime DEFAULT NULL COMMENT '安装时间',
    `remark` varchar(500) DEFAULT NULL COMMENT '备注',
    `creator` varchar(64) DEFAULT '' COMMENT '创建者',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` varchar(64) DEFAULT '' COMMENT '更新者',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sensor_code` (`sensor_code`, `tenant_id`),
    KEY `idx_sensor_type` (`sensor_type`),
    KEY `idx_area_id` (`area_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='环境传感器表';

-- 环境监测数据记录表
DROP TABLE IF EXISTS `ibms_env_data_record`;
CREATE TABLE `ibms_env_data_record` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `sensor_id` bigint NOT NULL COMMENT '传感器ID',
    `sensor_code` varchar(50) NOT NULL COMMENT '传感器编号',
    `temperature` decimal(5,2) DEFAULT NULL COMMENT '温度(℃)',
    `humidity` decimal(5,2) DEFAULT NULL COMMENT '湿度(%RH)',
    `pm25` decimal(8,2) DEFAULT NULL COMMENT 'PM2.5(μg/m³)',
    `pm10` decimal(8,2) DEFAULT NULL COMMENT 'PM10(μg/m³)',
    `co2` decimal(8,2) DEFAULT NULL COMMENT 'CO2浓度(ppm)',
    `formaldehyde` decimal(6,3) DEFAULT NULL COMMENT '甲醛(mg/m³)',
    `illuminance` decimal(10,2) DEFAULT NULL COMMENT '光照度(lux)',
    `noise` decimal(6,2) DEFAULT NULL COMMENT '噪音(dB)',
    `collect_time` datetime NOT NULL COMMENT '采集时间',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`),
    KEY `idx_sensor_id` (`sensor_id`),
    KEY `idx_collect_time` (`collect_time`),
    KEY `idx_sensor_code` (`sensor_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='环境监测数据记录表';

-- 环境告警记录表
DROP TABLE IF EXISTS `ibms_env_alarm`;
CREATE TABLE `ibms_env_alarm` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `sensor_id` bigint NOT NULL COMMENT '传感器ID',
    `sensor_code` varchar(50) NOT NULL COMMENT '传感器编号',
    `sensor_name` varchar(100) NOT NULL COMMENT '传感器名称',
    `alarm_type` tinyint NOT NULL COMMENT '告警类型：1-温度异常 2-湿度异常 3-PM2.5超标 4-PM10超标 5-CO2超标 6-设备离线',
    `alarm_level` tinyint NOT NULL COMMENT '告警级别：1-提示 2-警告 3-严重',
    `alarm_content` varchar(500) NOT NULL COMMENT '告警内容',
    `alarm_value` varchar(50) DEFAULT NULL COMMENT '告警值',
    `threshold_value` varchar(50) DEFAULT NULL COMMENT '阈值',
    `alarm_time` datetime NOT NULL COMMENT '告警时间',
    `recover_time` datetime DEFAULT NULL COMMENT '恢复时间',
    `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0-未处理 1-处理中 2-已处理 3-已恢复',
    `handler` varchar(64) DEFAULT NULL COMMENT '处理人',
    `handle_time` datetime DEFAULT NULL COMMENT '处理时间',
    `handle_remark` varchar(500) DEFAULT NULL COMMENT '处理备注',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`),
    KEY `idx_sensor_id` (`sensor_id`),
    KEY `idx_alarm_time` (`alarm_time`),
    KEY `idx_status` (`status`),
    KEY `idx_alarm_level` (`alarm_level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='环境告警记录表';

-- -------------------------------------------
-- 2. 智能照明模块
-- -------------------------------------------

-- 照明回路表
DROP TABLE IF EXISTS `ibms_lighting_circuit`;
CREATE TABLE `ibms_lighting_circuit` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `circuit_code` varchar(50) NOT NULL COMMENT '回路编号',
    `circuit_name` varchar(100) NOT NULL COMMENT '回路名称',
    `circuit_type` tinyint NOT NULL COMMENT '回路类型：1-普通照明 2-调光回路 3-应急照明',
    `area_id` bigint DEFAULT NULL COMMENT '所属区域ID',
    `area_name` varchar(100) DEFAULT NULL COMMENT '所属区域名称',
    `floor` varchar(20) DEFAULT NULL COMMENT '楼层',
    `location` varchar(200) DEFAULT NULL COMMENT '详细位置',
    `load_desc` varchar(200) DEFAULT NULL COMMENT '负载描述',
    `rated_power` decimal(10,2) DEFAULT NULL COMMENT '额定功率(W)',
    `light_count` int DEFAULT 0 COMMENT '灯具数量',
    `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0-关闭 1-开启 2-故障',
    `brightness` tinyint DEFAULT 100 COMMENT '亮度(0-100)',
    `color_temp` int DEFAULT 4000 COMMENT '色温(K)',
    `controller_id` bigint DEFAULT NULL COMMENT '控制器ID',
    `gateway_id` bigint DEFAULT NULL COMMENT '网关ID',
    `last_operate_time` datetime DEFAULT NULL COMMENT '最后操作时间',
    `remark` varchar(500) DEFAULT NULL COMMENT '备注',
    `creator` varchar(64) DEFAULT '' COMMENT '创建者',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` varchar(64) DEFAULT '' COMMENT '更新者',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_circuit_code` (`circuit_code`, `tenant_id`),
    KEY `idx_circuit_type` (`circuit_type`),
    KEY `idx_area_id` (`area_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='照明回路表';

-- 照明场景表
DROP TABLE IF EXISTS `ibms_lighting_scene`;
CREATE TABLE `ibms_lighting_scene` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `scene_code` varchar(50) NOT NULL COMMENT '场景编号',
    `scene_name` varchar(100) NOT NULL COMMENT '场景名称',
    `scene_icon` varchar(50) DEFAULT '✨' COMMENT '场景图标',
    `scene_desc` varchar(500) DEFAULT NULL COMMENT '场景描述',
    `area_id` bigint DEFAULT NULL COMMENT '适用区域ID',
    `area_name` varchar(100) DEFAULT NULL COMMENT '适用区域名称',
    `is_active` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否激活',
    `sort` int NOT NULL DEFAULT 0 COMMENT '排序',
    `remark` varchar(500) DEFAULT NULL COMMENT '备注',
    `creator` varchar(64) DEFAULT '' COMMENT '创建者',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` varchar(64) DEFAULT '' COMMENT '更新者',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_scene_code` (`scene_code`, `tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='照明场景表';

-- 照明场景回路关联表
DROP TABLE IF EXISTS `ibms_lighting_scene_circuit`;
CREATE TABLE `ibms_lighting_scene_circuit` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `scene_id` bigint NOT NULL COMMENT '场景ID',
    `circuit_id` bigint NOT NULL COMMENT '回路ID',
    `action` tinyint NOT NULL COMMENT '操作：0-关闭 1-开启',
    `brightness` tinyint DEFAULT 100 COMMENT '亮度(0-100)',
    `color_temp` int DEFAULT 4000 COMMENT '色温(K)',
    `delay_seconds` int DEFAULT 0 COMMENT '延迟执行秒数',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`),
    KEY `idx_scene_id` (`scene_id`),
    KEY `idx_circuit_id` (`circuit_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='照明场景回路关联表';

-- 照明定时任务表
DROP TABLE IF EXISTS `ibms_lighting_schedule`;
CREATE TABLE `ibms_lighting_schedule` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `schedule_name` varchar(100) NOT NULL COMMENT '任务名称',
    `execute_time` varchar(10) NOT NULL COMMENT '执行时间(HH:mm)',
    `weekdays` varchar(20) NOT NULL COMMENT '执行星期(逗号分隔，0-6表示周日到周六)',
    `scene_id` bigint NOT NULL COMMENT '执行场景ID',
    `scene_name` varchar(100) DEFAULT NULL COMMENT '执行场景名称',
    `enabled` bit(1) NOT NULL DEFAULT b'1' COMMENT '是否启用',
    `last_execute_time` datetime DEFAULT NULL COMMENT '最后执行时间',
    `remark` varchar(500) DEFAULT NULL COMMENT '备注',
    `creator` varchar(64) DEFAULT '' COMMENT '创建者',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` varchar(64) DEFAULT '' COMMENT '更新者',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`),
    KEY `idx_scene_id` (`scene_id`),
    KEY `idx_enabled` (`enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='照明定时任务表';

-- 照明网关表
DROP TABLE IF EXISTS `ibms_lighting_gateway`;
CREATE TABLE `ibms_lighting_gateway` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `gateway_code` varchar(50) NOT NULL COMMENT '网关编号',
    `gateway_name` varchar(100) NOT NULL COMMENT '网关名称',
    `gateway_model` varchar(100) DEFAULT NULL COMMENT '网关型号',
    `ip_address` varchar(50) DEFAULT NULL COMMENT 'IP地址',
    `mac_address` varchar(50) DEFAULT NULL COMMENT 'MAC地址',
    `area_name` varchar(100) DEFAULT NULL COMMENT '安装位置',
    `firmware_version` varchar(50) DEFAULT NULL COMMENT '固件版本',
    `device_count` int DEFAULT 0 COMMENT '接入设备数',
    `signal_strength` varchar(20) DEFAULT NULL COMMENT '信号强度',
    `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-离线 1-在线 2-故障',
    `last_online_time` datetime DEFAULT NULL COMMENT '最后在线时间',
    `remark` varchar(500) DEFAULT NULL COMMENT '备注',
    `creator` varchar(64) DEFAULT '' COMMENT '创建者',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` varchar(64) DEFAULT '' COMMENT '更新者',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_gateway_code` (`gateway_code`, `tenant_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='照明网关表';

-- 照明控制器表
DROP TABLE IF EXISTS `ibms_lighting_controller`;
CREATE TABLE `ibms_lighting_controller` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `controller_code` varchar(50) NOT NULL COMMENT '控制器编号',
    `controller_name` varchar(100) NOT NULL COMMENT '控制器名称',
    `controller_model` varchar(100) DEFAULT NULL COMMENT '控制器型号',
    `area_name` varchar(100) DEFAULT NULL COMMENT '安装位置',
    `channel_count` int DEFAULT 8 COMMENT '通道数',
    `rated_load` varchar(50) DEFAULT NULL COMMENT '额定负载',
    `current_load` varchar(20) DEFAULT NULL COMMENT '当前负载率',
    `gateway_id` bigint DEFAULT NULL COMMENT '所属网关ID',
    `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-离线 1-在线 2-故障',
    `last_online_time` datetime DEFAULT NULL COMMENT '最后在线时间',
    `remark` varchar(500) DEFAULT NULL COMMENT '备注',
    `creator` varchar(64) DEFAULT '' COMMENT '创建者',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` varchar(64) DEFAULT '' COMMENT '更新者',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_controller_code` (`controller_code`, `tenant_id`),
    KEY `idx_gateway_id` (`gateway_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='照明控制器表';

-- 照明操作日志表
DROP TABLE IF EXISTS `ibms_lighting_operation_log`;
CREATE TABLE `ibms_lighting_operation_log` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `operation_type` tinyint NOT NULL COMMENT '操作类型：1-手动控制 2-场景执行 3-定时任务 4-系统操作',
    `target_type` tinyint NOT NULL COMMENT '操作对象类型：1-回路 2-场景 3-网关 4-控制器',
    `target_id` bigint DEFAULT NULL COMMENT '操作对象ID',
    `target_name` varchar(100) DEFAULT NULL COMMENT '操作对象名称',
    `operation_content` varchar(500) NOT NULL COMMENT '操作内容',
    `operator` varchar(64) DEFAULT NULL COMMENT '操作人',
    `operator_ip` varchar(50) DEFAULT NULL COMMENT '操作IP',
    `result` tinyint NOT NULL DEFAULT 1 COMMENT '结果：0-失败 1-成功',
    `error_msg` varchar(500) DEFAULT NULL COMMENT '错误信息',
    `operate_time` datetime NOT NULL COMMENT '操作时间',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`),
    KEY `idx_operate_time` (`operate_time`),
    KEY `idx_operation_type` (`operation_type`),
    KEY `idx_target_id` (`target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='照明操作日志表';

-- 照明告警表
DROP TABLE IF EXISTS `ibms_lighting_alarm`;
CREATE TABLE `ibms_lighting_alarm` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `device_type` tinyint NOT NULL COMMENT '设备类型：1-回路 2-网关 3-控制器',
    `device_id` bigint NOT NULL COMMENT '设备ID',
    `device_name` varchar(100) NOT NULL COMMENT '设备名称',
    `alarm_level` tinyint NOT NULL COMMENT '告警级别：1-提示 2-警告 3-严重',
    `alarm_content` varchar(500) NOT NULL COMMENT '告警内容',
    `alarm_time` datetime NOT NULL COMMENT '告警时间',
    `duration` varchar(50) DEFAULT NULL COMMENT '持续时间',
    `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0-未处理 1-处理中 2-已处理',
    `handler` varchar(64) DEFAULT NULL COMMENT '处理人',
    `handle_time` datetime DEFAULT NULL COMMENT '处理时间',
    `handle_remark` varchar(500) DEFAULT NULL COMMENT '处理备注',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`),
    KEY `idx_alarm_time` (`alarm_time`),
    KEY `idx_device_id` (`device_id`),
    KEY `idx_status` (`status`),
    KEY `idx_alarm_level` (`alarm_level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='照明告警表';

-- -------------------------------------------
-- 3. 楼宇自控模块
-- -------------------------------------------

-- 暖通设备表（空调机组、新风机组、送排风机）
DROP TABLE IF EXISTS `ibms_hvac_device`;
CREATE TABLE `ibms_hvac_device` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `device_code` varchar(50) NOT NULL COMMENT '设备编号',
    `device_name` varchar(100) NOT NULL COMMENT '设备名称',
    `device_type` tinyint NOT NULL COMMENT '设备类型：1-空调机组 2-新风机组 3-送风机 4-排风机',
    `area_id` bigint DEFAULT NULL COMMENT '所属区域ID',
    `area_name` varchar(100) DEFAULT NULL COMMENT '所属区域名称',
    `floor` varchar(20) DEFAULT NULL COMMENT '楼层',
    `location` varchar(200) DEFAULT NULL COMMENT '详细位置',
    `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0-停止 1-运行 2-待机 3-故障',
    `run_mode` tinyint DEFAULT NULL COMMENT '运行模式：1-制冷 2-制热 3-通风 4-自动',
    `set_temp` decimal(4,1) DEFAULT NULL COMMENT '设定温度(℃)',
    `room_temp` decimal(4,1) DEFAULT NULL COMMENT '室内温度(℃)',
    `wind_speed` tinyint DEFAULT NULL COMMENT '风速：1-低速 2-中速 3-高速 4-自动',
    `filter_status` varchar(20) DEFAULT NULL COMMENT '滤网状态',
    `pressure` decimal(6,1) DEFAULT NULL COMMENT '风压(Pa)',
    `run_hours` int DEFAULT 0 COMMENT '累计运行时长(小时)',
    `maintain_status` varchar(20) DEFAULT '正常' COMMENT '维护状态',
    `next_maintain_date` date DEFAULT NULL COMMENT '下次维护日期',
    `last_update_time` datetime DEFAULT NULL COMMENT '最后更新时间',
    `remark` varchar(500) DEFAULT NULL COMMENT '备注',
    `creator` varchar(64) DEFAULT '' COMMENT '创建者',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` varchar(64) DEFAULT '' COMMENT '更新者',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_device_code` (`device_code`, `tenant_id`),
    KEY `idx_device_type` (`device_type`),
    KEY `idx_area_id` (`area_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='暖通设备表';

-- 给排水设备表
DROP TABLE IF EXISTS `ibms_water_device`;
CREATE TABLE `ibms_water_device` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `device_code` varchar(50) NOT NULL COMMENT '设备编号',
    `device_name` varchar(100) NOT NULL COMMENT '设备名称',
    `device_type` tinyint NOT NULL COMMENT '设备类型：1-生活水泵 2-排污泵 3-水箱水池',
    `area_id` bigint DEFAULT NULL COMMENT '所属区域ID',
    `area_name` varchar(100) DEFAULT NULL COMMENT '所属区域名称',
    `location` varchar(200) DEFAULT NULL COMMENT '详细位置',
    `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0-停止 1-运行 2-待机 3-故障',
    `run_mode` tinyint DEFAULT 1 COMMENT '运行模式：1-自动 2-手动',
    `pressure` decimal(5,2) DEFAULT NULL COMMENT '出口压力(MPa)',
    `water_level` decimal(5,1) DEFAULT NULL COMMENT '液位(%)',
    `run_hours` int DEFAULT 0 COMMENT '累计运行时长(小时)',
    `maintain_status` varchar(20) DEFAULT '正常' COMMENT '维护状态',
    `last_update_time` datetime DEFAULT NULL COMMENT '最后更新时间',
    `remark` varchar(500) DEFAULT NULL COMMENT '备注',
    `creator` varchar(64) DEFAULT '' COMMENT '创建者',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` varchar(64) DEFAULT '' COMMENT '更新者',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_device_code` (`device_code`, `tenant_id`),
    KEY `idx_device_type` (`device_type`),
    KEY `idx_area_id` (`area_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='给排水设备表';

-- 楼宇自控告警表
DROP TABLE IF EXISTS `ibms_bac_alarm`;
CREATE TABLE `ibms_bac_alarm` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `device_type` tinyint NOT NULL COMMENT '设备类型：1-暖通 2-给排水',
    `device_id` bigint NOT NULL COMMENT '设备ID',
    `device_name` varchar(100) NOT NULL COMMENT '设备名称',
    `alarm_level` tinyint NOT NULL COMMENT '告警级别：1-提示 2-重要 3-紧急',
    `alarm_content` varchar(500) NOT NULL COMMENT '告警内容',
    `alarm_time` datetime NOT NULL COMMENT '告警时间',
    `duration` varchar(50) DEFAULT NULL COMMENT '持续时间',
    `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0-未处理 1-处理中 2-已处理',
    `handler` varchar(64) DEFAULT NULL COMMENT '处理人',
    `handle_time` datetime DEFAULT NULL COMMENT '处理时间',
    `handle_remark` varchar(500) DEFAULT NULL COMMENT '处理备注',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`),
    KEY `idx_alarm_time` (`alarm_time`),
    KEY `idx_device_id` (`device_id`),
    KEY `idx_status` (`status`),
    KEY `idx_alarm_level` (`alarm_level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='楼宇自控告警表';

-- 楼宇自控系统日志表
DROP TABLE IF EXISTS `ibms_bac_system_log`;
CREATE TABLE `ibms_bac_system_log` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `log_type` tinyint NOT NULL COMMENT '日志类型：1-远程控制 2-系统事件 3-设备状态变更',
    `device_type` tinyint NOT NULL COMMENT '设备类型：1-暖通 2-给排水',
    `device_id` bigint DEFAULT NULL COMMENT '设备ID',
    `device_name` varchar(100) DEFAULT NULL COMMENT '设备名称',
    `event_desc` varchar(500) NOT NULL COMMENT '事件描述',
    `event_value` varchar(100) DEFAULT NULL COMMENT '数值/状态',
    `operator` varchar(64) DEFAULT NULL COMMENT '操作人',
    `log_time` datetime NOT NULL COMMENT '日志时间',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`),
    KEY `idx_log_time` (`log_time`),
    KEY `idx_log_type` (`log_type`),
    KEY `idx_device_id` (`device_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='楼宇自控系统日志表';

-- -------------------------------------------
-- 4. 能耗计量模块
-- -------------------------------------------

-- 能源表具表
DROP TABLE IF EXISTS `ibms_energy_meter`;
CREATE TABLE `ibms_energy_meter` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `meter_code` varchar(50) NOT NULL COMMENT '表具编号',
    `meter_name` varchar(100) NOT NULL COMMENT '表具名称',
    `meter_type` tinyint NOT NULL COMMENT '表具类型：1-电表 2-水表 3-燃气表 4-冷量表 5-热量表',
    `area_id` bigint DEFAULT NULL COMMENT '所属区域ID',
    `area_name` varchar(100) DEFAULT NULL COMMENT '所属区域名称',
    `floor` varchar(20) DEFAULT NULL COMMENT '楼层',
    `location` varchar(200) DEFAULT NULL COMMENT '安装位置',
    `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-离线 1-在线 2-故障',
    `current_reading` decimal(15,3) DEFAULT 0 COMMENT '当前读数',
    `unit` varchar(20) DEFAULT NULL COMMENT '计量单位',
    `multiplier` decimal(10,4) DEFAULT 1 COMMENT '倍率',
    `last_reading_time` datetime DEFAULT NULL COMMENT '最后抄表时间',
    `communication_type` tinyint DEFAULT 1 COMMENT '通讯方式：1-RS485 2-MBUS 3-LoRa 4-NB-IoT',
    `install_time` datetime DEFAULT NULL COMMENT '安装时间',
    `remark` varchar(500) DEFAULT NULL COMMENT '备注',
    `creator` varchar(64) DEFAULT '' COMMENT '创建者',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` varchar(64) DEFAULT '' COMMENT '更新者',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_meter_code` (`meter_code`, `tenant_id`),
    KEY `idx_meter_type` (`meter_type`),
    KEY `idx_area_id` (`area_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='能源表具表';

-- 能耗记录表
DROP TABLE IF EXISTS `ibms_energy_record`;
CREATE TABLE `ibms_energy_record` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `meter_id` bigint NOT NULL COMMENT '表具ID',
    `meter_code` varchar(50) NOT NULL COMMENT '表具编号',
    `meter_type` tinyint NOT NULL COMMENT '表具类型',
    `reading_value` decimal(15,3) NOT NULL COMMENT '抄表读数',
    `consumption` decimal(15,3) DEFAULT NULL COMMENT '消耗量',
    `reading_time` datetime NOT NULL COMMENT '抄表时间',
    `record_type` tinyint NOT NULL DEFAULT 1 COMMENT '记录类型：1-自动采集 2-人工抄表',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`),
    KEY `idx_meter_id` (`meter_id`),
    KEY `idx_reading_time` (`reading_time`),
    KEY `idx_meter_type` (`meter_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='能耗记录表';

-- 能耗统计表（日统计）
DROP TABLE IF EXISTS `ibms_energy_statistics_daily`;
CREATE TABLE `ibms_energy_statistics_daily` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `meter_id` bigint NOT NULL COMMENT '表具ID',
    `meter_code` varchar(50) NOT NULL COMMENT '表具编号',
    `meter_type` tinyint NOT NULL COMMENT '表具类型',
    `area_id` bigint DEFAULT NULL COMMENT '区域ID',
    `stat_date` date NOT NULL COMMENT '统计日期',
    `consumption` decimal(15,3) NOT NULL DEFAULT 0 COMMENT '消耗量',
    `cost` decimal(12,2) DEFAULT 0 COMMENT '费用',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_meter_date` (`meter_id`, `stat_date`),
    KEY `idx_stat_date` (`stat_date`),
    KEY `idx_meter_type` (`meter_type`),
    KEY `idx_area_id` (`area_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='能耗统计表-日';

-- 能耗告警表
DROP TABLE IF EXISTS `ibms_energy_alarm`;
CREATE TABLE `ibms_energy_alarm` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `meter_id` bigint NOT NULL COMMENT '表具ID',
    `meter_code` varchar(50) NOT NULL COMMENT '表具编号',
    `meter_name` varchar(100) NOT NULL COMMENT '表具名称',
    `alarm_type` tinyint NOT NULL COMMENT '告警类型：1-用量超标 2-异常波动 3-设备离线 4-通讯异常',
    `alarm_level` tinyint NOT NULL COMMENT '告警级别：1-提示 2-警告 3-严重',
    `alarm_content` varchar(500) NOT NULL COMMENT '告警内容',
    `alarm_value` varchar(50) DEFAULT NULL COMMENT '告警值',
    `threshold_value` varchar(50) DEFAULT NULL COMMENT '阈值',
    `alarm_time` datetime NOT NULL COMMENT '告警时间',
    `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0-未处理 1-处理中 2-已处理',
    `handler` varchar(64) DEFAULT NULL COMMENT '处理人',
    `handle_time` datetime DEFAULT NULL COMMENT '处理时间',
    `handle_remark` varchar(500) DEFAULT NULL COMMENT '处理备注',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`),
    KEY `idx_meter_id` (`meter_id`),
    KEY `idx_alarm_time` (`alarm_time`),
    KEY `idx_status` (`status`),
    KEY `idx_alarm_level` (`alarm_level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='能耗告警表';
