-- ----------------------------
-- 入侵报警模块数据库表
-- ----------------------------

-- 1. 报警规则配置表
CREATE TABLE IF NOT EXISTS `iot_alarm_rule` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '规则ID',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户编号',
    `name` VARCHAR(100) NOT NULL COMMENT '规则名称',
    `channel_id` BIGINT NOT NULL COMMENT '通道ID',
    `channel_name` VARCHAR(100) DEFAULT NULL COMMENT '通道名称',
    `device_id` BIGINT NOT NULL COMMENT '设备ID',
    `device_name` VARCHAR(100) DEFAULT NULL COMMENT '设备名称',
    
    -- 报警类型配置
    `alarm_type` VARCHAR(50) NOT NULL COMMENT '报警类型：IVS-智能报警, NORMAL-普通报警',
    `event_codes` VARCHAR(500) DEFAULT NULL COMMENT '监听的事件码列表，逗号分隔，如：1400,1401,1402',
    `event_names` VARCHAR(500) DEFAULT NULL COMMENT '事件名称列表，逗号分隔',
    
    -- 报警级别和处理
    `alarm_level` TINYINT NOT NULL DEFAULT 1 COMMENT '报警级别：1-低, 2-中, 3-高, 4-紧急',
    `enable_sound` TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用声音告警：0-否, 1-是',
    `enable_popup` TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用弹窗告警：0-否, 1-是',
    `enable_record` TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用录像联动：0-否, 1-是',
    `record_duration` INT DEFAULT 30 COMMENT '联动录像时长（秒）',
    
    -- 布防时间配置
    `arm_mode` VARCHAR(20) NOT NULL DEFAULT 'ALWAYS' COMMENT '布防模式：ALWAYS-全天, SCHEDULE-定时',
    `arm_schedule` TEXT DEFAULT NULL COMMENT '布防时间表（JSON格式）',
    
    -- 通知配置
    `notify_users` VARCHAR(500) DEFAULT NULL COMMENT '通知用户ID列表，逗号分隔',
    `notify_methods` VARCHAR(100) DEFAULT NULL COMMENT '通知方式：SMS-短信, EMAIL-邮件, WECHAT-微信',
    
    -- 状态和备注
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '规则状态：0-禁用, 1-启用',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    
    -- 审计字段
    `creator` VARCHAR(64) DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` VARCHAR(64) DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` BIT NOT NULL DEFAULT b'0' COMMENT '是否删除',
    
    PRIMARY KEY (`id`),
    INDEX `idx_tenant_id` (`tenant_id`),
    INDEX `idx_channel_id` (`channel_id`),
    INDEX `idx_device_id` (`device_id`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='入侵报警规则配置表';

-- 2. 报警记录表（MySQL - 用于查询和管理）
CREATE TABLE IF NOT EXISTS `iot_alarm_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户编号',
    
    -- 关联信息
    `rule_id` BIGINT DEFAULT NULL COMMENT '触发的规则ID',
    `rule_name` VARCHAR(100) DEFAULT NULL COMMENT '规则名称',
    `channel_id` BIGINT NOT NULL COMMENT '通道ID',
    `channel_name` VARCHAR(100) DEFAULT NULL COMMENT '通道名称',
    `device_id` BIGINT NOT NULL COMMENT '设备ID',
    `device_name` VARCHAR(100) DEFAULT NULL COMMENT '设备名称',
    
    -- 报警事件信息
    `alarm_type` VARCHAR(50) NOT NULL COMMENT '报警类型：IVS-智能报警, NORMAL-普通报警',
    `event_code` INT NOT NULL COMMENT '事件码（1400-1499）',
    `event_name` VARCHAR(100) NOT NULL COMMENT '事件名称',
    `alarm_level` TINYINT NOT NULL DEFAULT 1 COMMENT '报警级别：1-低, 2-中, 3-高, 4-紧急',
    
    -- 报警详细数据
    `area` SMALLINT DEFAULT NULL COMMENT '防区号',
    `area_name` VARCHAR(100) DEFAULT NULL COMMENT '防区名称',
    `point` SMALLINT DEFAULT NULL COMMENT '点位号',
    `point_name` VARCHAR(100) DEFAULT NULL COMMENT '点位名称',
    `sequence` INT DEFAULT NULL COMMENT '序列号',
    `alarm_time` DATETIME NOT NULL COMMENT '报警时间',
    `receive_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '接收时间',
    
    -- 报警快照和录像
    `snapshot_url` VARCHAR(500) DEFAULT NULL COMMENT '报警快照URL',
    `video_url` VARCHAR(500) DEFAULT NULL COMMENT '联动录像URL',
    `video_start_time` DATETIME DEFAULT NULL COMMENT '录像开始时间',
    `video_end_time` DATETIME DEFAULT NULL COMMENT '录像结束时间',
    
    -- 原始数据
    `raw_data` TEXT DEFAULT NULL COMMENT '原始报警数据（JSON格式）',
    
    -- 处理状态
    `process_status` TINYINT NOT NULL DEFAULT 0 COMMENT '处理状态：0-未处理, 1-已处理, 2-已忽略',
    `process_time` DATETIME DEFAULT NULL COMMENT '处理时间',
    `process_user` VARCHAR(64) DEFAULT NULL COMMENT '处理人',
    `process_remark` VARCHAR(500) DEFAULT NULL COMMENT '处理备注',
    
    -- 审计字段
    `creator` VARCHAR(64) DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` VARCHAR(64) DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` BIT NOT NULL DEFAULT b'0' COMMENT '是否删除',
    
    PRIMARY KEY (`id`),
    INDEX `idx_tenant_id` (`tenant_id`),
    INDEX `idx_rule_id` (`rule_id`),
    INDEX `idx_channel_id` (`channel_id`),
    INDEX `idx_device_id` (`device_id`),
    INDEX `idx_alarm_time` (`alarm_time`),
    INDEX `idx_process_status` (`process_status`),
    INDEX `idx_event_code` (`event_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='入侵报警记录表';

-- 3. 报警统计表（可选，用于快速统计）
CREATE TABLE IF NOT EXISTS `iot_alarm_statistics` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '统计ID',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户编号',
    `stat_date` DATE NOT NULL COMMENT '统计日期',
    `channel_id` BIGINT NOT NULL COMMENT '通道ID',
    `device_id` BIGINT NOT NULL COMMENT '设备ID',
    `event_code` INT NOT NULL COMMENT '事件码',
    `alarm_count` INT NOT NULL DEFAULT 0 COMMENT '报警次数',
    `processed_count` INT NOT NULL DEFAULT 0 COMMENT '已处理次数',
    `ignored_count` INT NOT NULL DEFAULT 0 COMMENT '已忽略次数',
    
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_stat` (`tenant_id`, `stat_date`, `channel_id`, `event_code`),
    INDEX `idx_stat_date` (`stat_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='入侵报警统计表';

-- 4. 报警事件码字典表
CREATE TABLE IF NOT EXISTS `iot_alarm_event_dict` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '字典ID',
    `event_code` INT NOT NULL COMMENT '事件码',
    `event_name` VARCHAR(100) NOT NULL COMMENT '事件名称',
    `event_category` VARCHAR(50) NOT NULL COMMENT '事件分类：IVS-智能报警, NORMAL-普通报警',
    `event_description` VARCHAR(500) DEFAULT NULL COMMENT '事件描述',
    `default_level` TINYINT NOT NULL DEFAULT 2 COMMENT '默认报警级别',
    `sort` INT NOT NULL DEFAULT 0 COMMENT '排序',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用, 1-启用',
    
    `creator` VARCHAR(64) DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` VARCHAR(64) DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` BIT NOT NULL DEFAULT b'0' COMMENT '是否删除',
    
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_event_code` (`event_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='报警事件码字典表';

-- 插入常见的报警事件码（根据大华文档）
INSERT INTO `iot_alarm_event_dict` (`event_code`, `event_name`, `event_category`, `event_description`, `default_level`, `sort`) VALUES
(1400, '入侵检测', 'IVS', '检测到有物体入侵预设区域', 3, 1),
(1401, '绊线检测', 'IVS', '检测到有物体穿越预设警戒线', 3, 2),
(1402, '物品遗留', 'IVS', '检测到有物品遗留在预设区域', 2, 3),
(1403, '物品丢失', 'IVS', '检测到预设区域内物品丢失', 2, 4),
(1404, '徘徊检测', 'IVS', '检测到有人在预设区域徘徊', 2, 5),
(1405, '人员聚集', 'IVS', '检测到预设区域内人员聚集', 2, 6),
(1406, '快速移动', 'IVS', '检测到有物体快速移动', 2, 7),
(1407, '停车检测', 'IVS', '检测到车辆违规停车', 2, 8),
(1408, '区域入侵', 'IVS', '检测到有物体进入禁区', 3, 9),
(1409, '逆行检测', 'IVS', '检测到车辆或行人逆行', 2, 10),
(1410, '人脸检测', 'IVS', '检测到人脸出现', 1, 11),
(1411, '人数统计', 'IVS', '区域内人数超过阈值', 2, 12),
(1450, '视频遮挡', 'NORMAL', '摄像头被遮挡', 3, 20),
(1451, '视频丢失', 'NORMAL', '视频信号丢失', 4, 21),
(1452, '移动侦测', 'NORMAL', '检测到画面移动', 1, 22),
(1453, '音频异常', 'NORMAL', '检测到异常声音', 2, 23),
(1454, '存储异常', 'NORMAL', '存储设备异常', 3, 24),
(1455, '网络断开', 'NORMAL', '网络连接断开', 3, 25);

-- 5. 报警主机表
CREATE TABLE IF NOT EXISTS `iot_alarm_host` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主机ID',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户编号',
    `device_id` BIGINT NOT NULL COMMENT '关联设备ID',
    `host_name` VARCHAR(100) NOT NULL COMMENT '主机名称',
    `host_model` VARCHAR(50) DEFAULT NULL COMMENT '主机型号',
    `host_sn` VARCHAR(100) DEFAULT NULL COMMENT '主机序列号',
    `zone_count` INT NOT NULL DEFAULT 0 COMMENT '防区数量',
    `online_status` TINYINT NOT NULL DEFAULT 0 COMMENT '在线状态：0-离线, 1-在线',
    `arm_status` VARCHAR(20) NOT NULL DEFAULT 'DISARM' COMMENT '布防状态：DISARM-撤防, ARM_ALL-全部布防, ARM_EMERGENCY-紧急布防',
    `alarm_status` TINYINT NOT NULL DEFAULT 0 COMMENT '报警状态：0-正常, 1-报警中',
    `location` VARCHAR(200) DEFAULT NULL COMMENT '安装位置',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    
    -- 审计字段
    `creator` VARCHAR(64) DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` VARCHAR(64) DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` BIT NOT NULL DEFAULT b'0' COMMENT '是否删除',
    
    PRIMARY KEY (`id`),
    INDEX `idx_tenant_id` (`tenant_id`),
    INDEX `idx_device_id` (`device_id`),
    INDEX `idx_online_status` (`online_status`),
    INDEX `idx_arm_status` (`arm_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='报警主机表';

-- 6. 防区表
CREATE TABLE IF NOT EXISTS `iot_alarm_zone` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '防区ID',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户编号',
    `host_id` BIGINT NOT NULL COMMENT '所属主机ID',
    `zone_no` INT NOT NULL COMMENT '防区编号',
    `zone_name` VARCHAR(100) NOT NULL COMMENT '防区名称',
    `zone_type` VARCHAR(50) DEFAULT NULL COMMENT '防区类型：DOOR-门磁, PIR-红外, SMOKE-烟感, GAS-燃气, GLASS-玻璃破碎, EMERGENCY-紧急按钮',
    `area_location` VARCHAR(200) DEFAULT NULL COMMENT '区域位置',
    `zone_status` VARCHAR(20) NOT NULL DEFAULT 'DISARM' COMMENT '防区状态：ARM-布防, DISARM-撤防, BYPASS-旁路',
    `online_status` TINYINT NOT NULL DEFAULT 0 COMMENT '在线状态：0-离线, 1-在线',
    `is_important` TINYINT NOT NULL DEFAULT 0 COMMENT '是否重要防区：0-否, 1-是',
    `is_24h` TINYINT NOT NULL DEFAULT 0 COMMENT '是否24小时防区：0-否, 1-是',
    `alarm_count` INT NOT NULL DEFAULT 0 COMMENT '报警次数统计',
    `last_alarm_time` DATETIME DEFAULT NULL COMMENT '最后报警时间',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    
    -- 审计字段
    `creator` VARCHAR(64) DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` VARCHAR(64) DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` BIT NOT NULL DEFAULT b'0' COMMENT '是否删除',
    
    PRIMARY KEY (`id`),
    INDEX `idx_tenant_id` (`tenant_id`),
    INDEX `idx_host_id` (`host_id`),
    INDEX `idx_zone_no` (`zone_no`),
    INDEX `idx_zone_status` (`zone_status`),
    INDEX `idx_online_status` (`online_status`),
    UNIQUE KEY `uk_host_zone` (`host_id`, `zone_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='防区表';

-- 7. 定时布防任务表
CREATE TABLE IF NOT EXISTS `iot_alarm_schedule` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '任务ID',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户编号',
    `host_id` BIGINT NOT NULL COMMENT '报警主机ID',
    `task_name` VARCHAR(100) NOT NULL COMMENT '任务名称',
    `arm_type` VARCHAR(20) NOT NULL COMMENT '布防类型：ARM_ALL-全部布防, ARM_EMERGENCY-紧急布防, DISARM-撤防',
    `weekdays` VARCHAR(50) NOT NULL COMMENT '星期几执行，逗号分隔：1-7表示周一到周日',
    `start_time` TIME NOT NULL COMMENT '开始时间',
    `end_time` TIME DEFAULT NULL COMMENT '结束时间（撤防时使用）',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用, 1-启用',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    
    -- 审计字段
    `creator` VARCHAR(64) DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` VARCHAR(64) DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` BIT NOT NULL DEFAULT b'0' COMMENT '是否删除',
    
    PRIMARY KEY (`id`),
    INDEX `idx_tenant_id` (`tenant_id`),
    INDEX `idx_host_id` (`host_id`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='定时布防任务表';

-- 修改报警规则表，添加主机和防区关联字段
ALTER TABLE `iot_alarm_rule` ADD COLUMN `host_id` BIGINT DEFAULT NULL COMMENT '报警主机ID' AFTER `device_name`;
ALTER TABLE `iot_alarm_rule` ADD COLUMN `zone_id` BIGINT DEFAULT NULL COMMENT '防区ID' AFTER `host_id`;
ALTER TABLE `iot_alarm_rule` ADD INDEX `idx_host_id` (`host_id`);
ALTER TABLE `iot_alarm_rule` ADD INDEX `idx_zone_id` (`zone_id`);

-- 修改报警记录表，添加主机和防区关联字段
ALTER TABLE `iot_alarm_record` ADD COLUMN `host_id` BIGINT DEFAULT NULL COMMENT '报警主机ID' AFTER `device_name`;
ALTER TABLE `iot_alarm_record` ADD COLUMN `zone_id` BIGINT DEFAULT NULL COMMENT '防区ID' AFTER `host_id`;
ALTER TABLE `iot_alarm_record` ADD INDEX `idx_host_id` (`host_id`);
ALTER TABLE `iot_alarm_record` ADD INDEX `idx_zone_id` (`zone_id`);
