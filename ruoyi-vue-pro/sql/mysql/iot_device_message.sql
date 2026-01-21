-- =====================================================
-- IoT 设备消息表（替代 TDEngine 的 device_message 超级表）
-- 用于存储设备上下行消息
-- =====================================================

-- 设备消息主表
CREATE TABLE IF NOT EXISTS `iot_device_message` (
    `id` VARCHAR(50) NOT NULL COMMENT '消息编号',
    `device_id` BIGINT NOT NULL COMMENT '设备编号',
    `tenant_id` BIGINT NULL COMMENT '租户编号',
    `server_id` VARCHAR(50) NULL COMMENT '服务编号',
    `report_time` BIGINT NULL COMMENT '上报时间戳',
    `ts` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '存储时间戳',
    `upstream` TINYINT(1) NULL COMMENT '是否上行消息',
    `reply` TINYINT(1) NULL COMMENT '是否回复消息',
    `identifier` VARCHAR(100) NULL COMMENT '标识符',
    `request_id` VARCHAR(50) NULL COMMENT '请求编号',
    `method` VARCHAR(100) NULL COMMENT '方法',
    `params` TEXT NULL COMMENT '参数',
    `data` TEXT NULL COMMENT '数据',
    `code` INT NULL COMMENT '响应码',
    `msg` VARCHAR(256) NULL COMMENT '响应消息',
    PRIMARY KEY (`id`),
    INDEX `idx_device_id` (`device_id`),
    INDEX `idx_ts` (`ts`),
    INDEX `idx_device_ts` (`device_id`, `ts`),
    INDEX `idx_method` (`method`),
    INDEX `idx_request_id` (`request_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='IoT设备消息表';

-- =====================================================
-- IoT 设备属性历史表（替代 TDEngine 的 product_property_* 超级表）
-- 每个产品共享一张属性历史表，使用 product_id 区分
-- =====================================================

CREATE TABLE IF NOT EXISTS `iot_device_property_history` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `device_id` BIGINT NOT NULL COMMENT '设备编号',
    `product_id` BIGINT NOT NULL COMMENT '产品编号',
    `identifier` VARCHAR(100) NOT NULL COMMENT '属性标识符',
    `value` TEXT NULL COMMENT '属性值（JSON格式）',
    `report_time` BIGINT NULL COMMENT '上报时间戳',
    `ts` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '存储时间戳',
    PRIMARY KEY (`id`),
    INDEX `idx_device_id` (`device_id`),
    INDEX `idx_product_id` (`product_id`),
    INDEX `idx_identifier` (`identifier`),
    INDEX `idx_ts` (`ts`),
    INDEX `idx_device_identifier_ts` (`device_id`, `identifier`, `ts`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='IoT设备属性历史表';

-- =====================================================
-- OPC 报警记录表（替代 TDEngine 的 opc_alarm_record 超级表）
-- =====================================================

CREATE TABLE IF NOT EXISTS `iot_opc_alarm_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `account_id` INT NULL COMMENT '账号（主机标识）',
    `event_code` INT NULL COMMENT '事件代码',
    `area` SMALLINT NULL COMMENT '防区号',
    `point` SMALLINT NULL COMMENT '点位号',
    `sequence` INT NULL COMMENT '序列号',
    `event_description` VARCHAR(200) NULL COMMENT '事件描述',
    `level` VARCHAR(20) NULL COMMENT '事件级别',
    `type` VARCHAR(20) NULL COMMENT '事件类型',
    `receive_time` DATETIME(3) NULL COMMENT '接收时间',
    `remote_address` VARCHAR(50) NULL COMMENT '远程地址',
    `remote_port` INT NULL COMMENT '远程端口',
    `raw_message` VARCHAR(500) NULL COMMENT '原始消息',
    `device_id` BIGINT NULL COMMENT '设备编号',
    `device_name` VARCHAR(100) NULL COMMENT '设备名称',
    `area_name` VARCHAR(100) NULL COMMENT '防区名称',
    `point_name` VARCHAR(100) NULL COMMENT '点位名称',
    `location` VARCHAR(200) NULL COMMENT '位置',
    `handled` TINYINT NULL DEFAULT 0 COMMENT '是否已处理',
    `handle_time` DATETIME(3) NULL COMMENT '处理时间',
    `handle_by` VARCHAR(50) NULL COMMENT '处理人',
    `handle_remark` VARCHAR(500) NULL COMMENT '处理备注',
    `create_time` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    `ts` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '时间戳',
    PRIMARY KEY (`id`),
    INDEX `idx_device_id` (`device_id`),
    INDEX `idx_ts` (`ts`),
    INDEX `idx_account_id` (`account_id`),
    INDEX `idx_event_code` (`event_code`),
    INDEX `idx_receive_time` (`receive_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='OPC报警记录表';
