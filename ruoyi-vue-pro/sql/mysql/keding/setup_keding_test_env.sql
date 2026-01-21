-- =============================================
-- 科鼎测试环境一键设置脚本
-- 执行顺序：清理德通 -> 创建科鼎表 -> 插入测试数据
-- =============================================

SELECT '========================================' AS info;
SELECT '科鼎测试环境设置开始' AS info;
SELECT '========================================' AS info;

-- =============================================
-- Step 1: 清理德通表
-- =============================================
SELECT '' AS info;
SELECT '>>> Step 1: 清理德通表 <<<' AS info;

SET FOREIGN_KEY_CHECKS = 0;

-- 清空并删除德通表
DROP TABLE IF EXISTS `iot_detong_upgrade_log`;
DROP TABLE IF EXISTS `iot_detong_upgrade_task`;
DROP TABLE IF EXISTS `iot_detong_control_log`;
DROP TABLE IF EXISTS `iot_detong_alarm`;
DROP TABLE IF EXISTS `iot_detong_firmware`;
DROP TABLE IF EXISTS `iot_detong_device`;

SET FOREIGN_KEY_CHECKS = 1;

SELECT '✓ 德通表清理完成' AS result;

-- =============================================
-- Step 2: 确保科鼎表存在
-- =============================================
SELECT '' AS info;
SELECT '>>> Step 2: 创建科鼎表结构 <<<' AS info;

-- 科鼎设备表
CREATE TABLE IF NOT EXISTS `iot_keding_device` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `station_code` varchar(20) NOT NULL COMMENT '测站编码（唯一）',
    `device_name` varchar(100) DEFAULT NULL COMMENT '设备名称',
    `device_type` tinyint DEFAULT NULL COMMENT '设备类型',
    `province_code` varchar(10) DEFAULT NULL COMMENT '行政区代码',
    `management_code` varchar(10) DEFAULT NULL COMMENT '管理处代码',
    `station_code_part` varchar(10) DEFAULT NULL COMMENT '站所代码',
    `pile_front` varchar(10) DEFAULT NULL COMMENT '桩号（前）',
    `pile_back` varchar(10) DEFAULT NULL COMMENT '桩号（后）',
    `manufacturer` varchar(10) DEFAULT NULL COMMENT '设备厂家',
    `sequence_no` varchar(10) DEFAULT NULL COMMENT '顺序编号',
    `tea_key` varchar(64) DEFAULT NULL COMMENT 'TEA加密密钥',
    `password` varchar(10) DEFAULT NULL COMMENT '设备密码',
    `status` tinyint DEFAULT 0 COMMENT '状态',
    `last_heartbeat` datetime DEFAULT NULL COMMENT '最后心跳时间',
    `creator` varchar(64) DEFAULT '' COMMENT '创建者',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` varchar(64) DEFAULT '' COMMENT '更新者',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_station_code` (`station_code`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='科鼎设备表';

-- 科鼎报警表
CREATE TABLE IF NOT EXISTS `iot_keding_alarm` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `device_id` bigint DEFAULT NULL COMMENT '设备ID',
    `station_code` varchar(20) NOT NULL COMMENT '测站编码',
    `alarm_type` varchar(50) NOT NULL COMMENT '报警类型',
    `alarm_value` varchar(100) DEFAULT NULL COMMENT '报警值',
    `alarm_time` datetime NOT NULL COMMENT '报警时间',
    `status` tinyint DEFAULT 0 COMMENT '状态',
    `ack_time` datetime DEFAULT NULL COMMENT '确认时间',
    `ack_user` varchar(64) DEFAULT NULL COMMENT '确认人',
    `creator` varchar(64) DEFAULT '' COMMENT '创建者',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` varchar(64) DEFAULT '' COMMENT '更新者',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='科鼎报警记录表';

-- 科鼎控制记录表
CREATE TABLE IF NOT EXISTS `iot_keding_control_log` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `device_id` bigint DEFAULT NULL COMMENT '设备ID',
    `station_code` varchar(20) NOT NULL COMMENT '测站编码',
    `control_type` varchar(50) NOT NULL COMMENT '控制类型',
    `control_params` varchar(500) DEFAULT NULL COMMENT '控制参数',
    `result` tinyint DEFAULT NULL COMMENT '结果',
    `error_message` varchar(500) DEFAULT NULL COMMENT '错误信息',
    `operator` varchar(64) DEFAULT NULL COMMENT '操作员',
    `operate_time` datetime NOT NULL COMMENT '操作时间',
    `creator` varchar(64) DEFAULT '' COMMENT '创建者',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` varchar(64) DEFAULT '' COMMENT '更新者',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='科鼎控制记录表';

-- 科鼎固件表
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
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='科鼎固件表';

-- 科鼎升级任务表
CREATE TABLE IF NOT EXISTS `iot_keding_upgrade_task` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `device_id` bigint DEFAULT NULL COMMENT '设备ID',
    `station_code` varchar(20) NOT NULL COMMENT '测站编码',
    `firmware_id` bigint DEFAULT NULL COMMENT '固件ID',
    `firmware_version` varchar(50) DEFAULT NULL COMMENT '固件版本',
    `status` tinyint DEFAULT 0 COMMENT '状态',
    `progress` int DEFAULT 0 COMMENT '进度',
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
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='科鼎升级任务表';

-- 科鼎升级日志表
CREATE TABLE IF NOT EXISTS `iot_keding_upgrade_log` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `task_id` bigint NOT NULL COMMENT '任务ID',
    `action` varchar(50) NOT NULL COMMENT '动作',
    `old_status` tinyint DEFAULT NULL COMMENT '原状态',
    `new_status` tinyint DEFAULT NULL COMMENT '新状态',
    `progress` int DEFAULT NULL COMMENT '当前进度',
    `message` varchar(500) DEFAULT NULL COMMENT '日志消息',
    `creator` varchar(64) DEFAULT '' COMMENT '创建者',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='科鼎升级日志表';

SELECT '✓ 科鼎表结构创建完成' AS result;

-- =============================================
-- Step 3: 清理旧测试数据并插入新数据
-- =============================================
SELECT '' AS info;
SELECT '>>> Step 3: 插入科鼎测试数据 <<<' AS info;

SET @tenant_id = 1;

-- 清理旧测试数据
DELETE FROM iot_keding_device WHERE station_code LIKE 'TEST%';
DELETE FROM iot_keding_firmware WHERE tenant_id = @tenant_id AND name LIKE '%测试%';

-- 插入9种设备类型的测试设备
INSERT INTO iot_keding_device (station_code, device_name, device_type, province_code, management_code, station_code_part, pile_front, pile_back, manufacturer, sequence_no, tea_key, password, status, tenant_id, creator, updater) VALUES
('TEST01010101', '模拟一体化闸门-1号', 1, '32', '01', '01', '001', '000', '01', '01', '[305419896, 2596069104, 305419896, 2596069104]', '123456', 0, @tenant_id, 'admin', 'admin'),
('TEST01010202', '模拟分体式闸门-1号', 2, '32', '01', '01', '002', '000', '01', '02', '[305419896, 2596069104, 305419896, 2596069104]', '123456', 0, @tenant_id, 'admin', 'admin'),
('TEST01010303', '模拟退水闸-1号', 3, '32', '01', '01', '003', '000', '01', '03', '[305419896, 2596069104, 305419896, 2596069104]', '123456', 0, @tenant_id, 'admin', 'admin'),
('TEST01010404', '模拟节制闸-1号', 4, '32', '01', '01', '004', '000', '01', '04', '[305419896, 2596069104, 305419896, 2596069104]', '123456', 0, @tenant_id, 'admin', 'admin'),
('TEST01010505', '模拟进水闸-1号', 5, '32', '01', '01', '005', '000', '01', '05', '[305419896, 2596069104, 305419896, 2596069104]', '123456', 0, @tenant_id, 'admin', 'admin'),
('TEST01010606', '模拟水位计-1号', 6, '32', '01', '01', '006', '000', '01', '06', '[305419896, 2596069104, 305419896, 2596069104]', '123456', 0, @tenant_id, 'admin', 'admin'),
('TEST01010707', '模拟流量计-1号', 7, '32', '01', '01', '007', '000', '01', '07', '[305419896, 2596069104, 305419896, 2596069104]', '123456', 0, @tenant_id, 'admin', 'admin'),
('TEST01010808', '模拟流速仪-1号', 8, '32', '01', '01', '008', '000', '01', '08', '[305419896, 2596069104, 305419896, 2596069104]', '123456', 0, @tenant_id, 'admin', 'admin'),
('TEST01010909', '模拟渗压计-1号', 9, '32', '01', '01', '009', '000', '01', '09', '[305419896, 2596069104, 305419896, 2596069104]', '123456', 0, @tenant_id, 'admin', 'admin');

-- 插入测试固件
INSERT INTO iot_keding_firmware (name, version, device_type, file_path, file_size, file_md5, description, tenant_id, creator, updater) VALUES 
('闸门控制器测试固件', 'V1.0.0', 1, '/firmware/gate_v1.0.0.bin', 102400, 'abc123def456', '测试固件-闸门类设备通用', @tenant_id, 'admin', 'admin'),
('水位计测试固件', 'V1.0.0', 6, '/firmware/water_level_v1.0.0.bin', 81920, 'def456abc123', '测试固件-水位计专用', @tenant_id, 'admin', 'admin'),
('流量计测试固件', 'V1.0.0', 7, '/firmware/flow_meter_v1.0.0.bin', 81920, 'ghi789jkl012', '测试固件-流量计专用', @tenant_id, 'admin', 'admin');

SELECT '✓ 科鼎测试数据插入完成' AS result;

-- =============================================
-- Step 4: 验证结果
-- =============================================
SELECT '' AS info;
SELECT '>>> Step 4: 验证设置结果 <<<' AS info;

SELECT '--- 德通表检查 ---' AS info;
SELECT 
    CASE 
        WHEN COUNT(*) = 0 THEN '✓ 德通表已全部删除'
        ELSE CONCAT('✗ 仍存在 ', COUNT(*), ' 个德通表')
    END AS detong_check
FROM information_schema.tables 
WHERE table_schema = DATABASE() 
AND table_name LIKE 'iot_detong_%';

SELECT '--- 科鼎表检查 ---' AS info;
SELECT 
    CASE 
        WHEN COUNT(*) = 6 THEN '✓ 科鼎表完整 (6个表)'
        ELSE CONCAT('✗ 科鼎表不完整，当前 ', COUNT(*), ' 个表')
    END AS keding_check
FROM information_schema.tables 
WHERE table_schema = DATABASE() 
AND table_name LIKE 'iot_keding_%';

SELECT '--- 测试设备统计 ---' AS info;
SELECT device_type, COUNT(*) AS count, device_name 
FROM iot_keding_device 
WHERE station_code LIKE 'TEST%' 
GROUP BY device_type, device_name
ORDER BY device_type;

SELECT '--- 测试固件统计 ---' AS info;
SELECT id, name, version, device_type 
FROM iot_keding_firmware 
WHERE tenant_id = @tenant_id;

SELECT '' AS info;
SELECT '========================================' AS info;
SELECT '科鼎测试环境设置完成!' AS info;
SELECT '========================================' AS info;
