CREATE TABLE IF NOT EXISTS `iot_factory_collab_production_plan` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `tenant_id` BIGINT NOT NULL DEFAULT 1,
    `plan_code` VARCHAR(64) NOT NULL,
    `product_name` VARCHAR(100) NOT NULL,
    `batch_code` VARCHAR(64) NOT NULL,
    `line_name` VARCHAR(100) NOT NULL,
    `planned_quantity` INT NOT NULL DEFAULT 0,
    `completed_quantity` INT NOT NULL DEFAULT 0,
    `operator_name` VARCHAR(64) DEFAULT '',
    `planned_start_time` DATETIME NOT NULL,
    `planned_end_time` DATETIME DEFAULT NULL,
    `status` VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    `progress` INT NOT NULL DEFAULT 0,
    `remark` VARCHAR(255) DEFAULT '',
    `creator` VARCHAR(64) DEFAULT '',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updater` VARCHAR(64) DEFAULT '',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` BIT(1) NOT NULL DEFAULT b'0',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_factory_collab_plan_code` (`plan_code`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='业务协同-生产计划表';

CREATE TABLE IF NOT EXISTS `iot_factory_collab_batch_trace` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `tenant_id` BIGINT NOT NULL DEFAULT 1,
    `plan_id` BIGINT NOT NULL,
    `batch_code` VARCHAR(64) NOT NULL,
    `product_name` VARCHAR(100) NOT NULL,
    `current_process` VARCHAR(100) DEFAULT '',
    `current_location` VARCHAR(100) DEFAULT '',
    `completed_quantity` INT NOT NULL DEFAULT 0,
    `yield_rate` DECIMAL(8, 2) NOT NULL DEFAULT 0,
    `status` VARCHAR(32) NOT NULL DEFAULT '待开始',
    `trace_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `creator` VARCHAR(64) DEFAULT '',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updater` VARCHAR(64) DEFAULT '',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` BIT(1) NOT NULL DEFAULT b'0',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_factory_collab_batch_code` (`batch_code`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='业务协同-批次追踪表';

CREATE TABLE IF NOT EXISTS `iot_factory_collab_batch_environment` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `tenant_id` BIGINT NOT NULL DEFAULT 1,
    `batch_trace_id` BIGINT NOT NULL,
    `record_time` DATETIME NOT NULL,
    `temperature_value` DECIMAL(8, 2) NOT NULL DEFAULT 0,
    `humidity_value` DECIMAL(8, 2) NOT NULL DEFAULT 0,
    `pressure_value` DECIMAL(8, 2) NOT NULL DEFAULT 0,
    `ph_value` DECIMAL(8, 2) NOT NULL DEFAULT 0,
    `clean_level` VARCHAR(32) DEFAULT '',
    `recorder_name` VARCHAR(64) DEFAULT '',
    `creator` VARCHAR(64) DEFAULT '',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updater` VARCHAR(64) DEFAULT '',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` BIT(1) NOT NULL DEFAULT b'0',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_factory_collab_batch_env` (`tenant_id`, `batch_trace_id`, `record_time`, `clean_level`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='业务协同-批次环境记录表';

CREATE TABLE IF NOT EXISTS `iot_factory_collab_batch_quality` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `tenant_id` BIGINT NOT NULL DEFAULT 1,
    `batch_trace_id` BIGINT NOT NULL,
    `sample_name` VARCHAR(64) NOT NULL,
    `inspection_item` VARCHAR(64) NOT NULL,
    `standard_value` VARCHAR(64) NOT NULL,
    `measured_value` VARCHAR(64) NOT NULL,
    `result_status` VARCHAR(32) NOT NULL DEFAULT 'PASS',
    `record_time` DATETIME NOT NULL,
    `inspector_name` VARCHAR(64) DEFAULT '',
    `creator` VARCHAR(64) DEFAULT '',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updater` VARCHAR(64) DEFAULT '',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` BIT(1) NOT NULL DEFAULT b'0',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_factory_collab_batch_quality` (`tenant_id`, `batch_trace_id`, `sample_name`, `inspection_item`, `record_time`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='业务协同-批次质量记录表';

CREATE TABLE IF NOT EXISTS `iot_factory_collab_batch_personnel` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `tenant_id` BIGINT NOT NULL DEFAULT 1,
    `batch_trace_id` BIGINT NOT NULL,
    `role_name` VARCHAR(64) NOT NULL,
    `staff_name` VARCHAR(64) NOT NULL,
    `operation_name` VARCHAR(100) NOT NULL,
    `workstation_name` VARCHAR(100) DEFAULT '',
    `record_time` DATETIME NOT NULL,
    `duration_minutes` INT NOT NULL DEFAULT 0,
    `remark` VARCHAR(255) DEFAULT '',
    `creator` VARCHAR(64) DEFAULT '',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updater` VARCHAR(64) DEFAULT '',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` BIT(1) NOT NULL DEFAULT b'0',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_factory_collab_batch_personnel` (`tenant_id`, `batch_trace_id`, `staff_name`, `operation_name`, `record_time`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='业务协同-批次人员记录表';

CREATE TABLE IF NOT EXISTS `iot_factory_collab_batch_device` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `tenant_id` BIGINT NOT NULL DEFAULT 1,
    `batch_trace_id` BIGINT NOT NULL,
    `device_code` VARCHAR(64) NOT NULL,
    `device_name` VARCHAR(100) NOT NULL,
    `operation_name` VARCHAR(100) NOT NULL,
    `running_status` VARCHAR(32) NOT NULL DEFAULT '运行中',
    `parameter_summary` VARCHAR(255) DEFAULT '',
    `record_time` DATETIME NOT NULL,
    `operator_name` VARCHAR(64) DEFAULT '',
    `creator` VARCHAR(64) DEFAULT '',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updater` VARCHAR(64) DEFAULT '',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` BIT(1) NOT NULL DEFAULT b'0',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_factory_collab_batch_device` (`tenant_id`, `batch_trace_id`, `device_code`, `operation_name`, `record_time`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='业务协同-批次设备记录表';

CREATE TABLE IF NOT EXISTS `iot_factory_collab_batch_material` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `tenant_id` BIGINT NOT NULL DEFAULT 1,
    `batch_trace_id` BIGINT NOT NULL,
    `material_code` VARCHAR(64) NOT NULL,
    `material_name` VARCHAR(100) NOT NULL,
    `material_type` VARCHAR(64) NOT NULL,
    `material_batch_no` VARCHAR(64) NOT NULL,
    `planned_quantity` DECIMAL(12, 2) NOT NULL DEFAULT 0,
    `actual_quantity` DECIMAL(12, 2) NOT NULL DEFAULT 0,
    `unit` VARCHAR(16) NOT NULL DEFAULT '',
    `feeder_name` VARCHAR(64) DEFAULT '',
    `record_time` DATETIME NOT NULL,
    `creator` VARCHAR(64) DEFAULT '',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updater` VARCHAR(64) DEFAULT '',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` BIT(1) NOT NULL DEFAULT b'0',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_factory_collab_batch_material` (`tenant_id`, `batch_trace_id`, `material_code`, `material_batch_no`, `record_time`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='业务协同-批次原料记录表';

CREATE TABLE IF NOT EXISTS `iot_factory_collab_production_exception` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `tenant_id` BIGINT NOT NULL DEFAULT 1,
    `plan_id` BIGINT DEFAULT NULL,
    `batch_trace_id` BIGINT DEFAULT NULL,
    `alert_title` VARCHAR(120) NOT NULL,
    `level_label` VARCHAR(32) NOT NULL,
    `line_name` VARCHAR(100) DEFAULT '',
    `handler_name` VARCHAR(64) DEFAULT '',
    `status` VARCHAR(32) NOT NULL DEFAULT '待处理',
    `happened_at` DATETIME NOT NULL,
    `description` VARCHAR(255) DEFAULT '',
    `creator` VARCHAR(64) DEFAULT '',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updater` VARCHAR(64) DEFAULT '',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` BIT(1) NOT NULL DEFAULT b'0',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='业务协同-生产异常事件表';

CREATE TABLE IF NOT EXISTS `iot_factory_collab_energy_reading` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `tenant_id` BIGINT NOT NULL DEFAULT 1,
    `energy_type` VARCHAR(16) NOT NULL,
    `area_name` VARCHAR(100) NOT NULL,
    `device_name` VARCHAR(100) NOT NULL,
    `stat_time` DATETIME NOT NULL,
    `usage_value` DECIMAL(12, 2) NOT NULL DEFAULT 0,
    `unit` VARCHAR(16) NOT NULL,
    `yoy_rate` DECIMAL(8, 2) NOT NULL DEFAULT 0,
    `creator` VARCHAR(64) DEFAULT '',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updater` VARCHAR(64) DEFAULT '',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` BIT(1) NOT NULL DEFAULT b'0',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_factory_collab_energy_slot` (`tenant_id`, `energy_type`, `area_name`, `device_name`, `stat_time`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='业务协同-能源读数表';

CREATE TABLE IF NOT EXISTS `iot_factory_collab_energy_suggestion` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `tenant_id` BIGINT NOT NULL DEFAULT 1,
    `title` VARCHAR(120) NOT NULL,
    `content` VARCHAR(255) NOT NULL,
    `level_label` VARCHAR(32) NOT NULL,
    `status` VARCHAR(32) NOT NULL DEFAULT '待处理',
    `sort_no` INT NOT NULL DEFAULT 0,
    `creator` VARCHAR(64) DEFAULT '',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updater` VARCHAR(64) DEFAULT '',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` BIT(1) NOT NULL DEFAULT b'0',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='业务协同-节能建议表';

CREATE TABLE IF NOT EXISTS `iot_factory_collab_device` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `tenant_id` BIGINT NOT NULL DEFAULT 1,
    `device_code` VARCHAR(64) NOT NULL,
    `device_name` VARCHAR(100) NOT NULL,
    `category_name` VARCHAR(64) NOT NULL,
    `model_name` VARCHAR(64) NOT NULL,
    `area_name` VARCHAR(100) NOT NULL,
    `online_status` TINYINT NOT NULL DEFAULT 1,
    `running_status` VARCHAR(32) NOT NULL DEFAULT '运行中',
    `health_status` VARCHAR(32) NOT NULL DEFAULT '正常',
    `efficiency_rate` DECIMAL(8, 2) NOT NULL DEFAULT 0,
    `remark` VARCHAR(255) DEFAULT '',
    `creator` VARCHAR(64) DEFAULT '',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updater` VARCHAR(64) DEFAULT '',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` BIT(1) NOT NULL DEFAULT b'0',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_factory_collab_device_code` (`device_code`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='业务协同-设备台账表';

CREATE TABLE IF NOT EXISTS `iot_factory_collab_maintenance_plan` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `tenant_id` BIGINT NOT NULL DEFAULT 1,
    `device_id` BIGINT NOT NULL,
    `plan_name` VARCHAR(120) NOT NULL,
    `cycle_type` VARCHAR(32) NOT NULL,
    `last_execute_date` DATE DEFAULT NULL,
    `next_execute_date` DATE NOT NULL,
    `owner_name` VARCHAR(64) NOT NULL,
    `status` VARCHAR(32) NOT NULL DEFAULT 'PLANNED',
    `creator` VARCHAR(64) DEFAULT '',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updater` VARCHAR(64) DEFAULT '',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` BIT(1) NOT NULL DEFAULT b'0',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='业务协同-维保计划表';

CREATE TABLE IF NOT EXISTS `iot_factory_collab_maintenance_order` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `tenant_id` BIGINT NOT NULL DEFAULT 1,
    `plan_id` BIGINT NOT NULL,
    `device_id` BIGINT NOT NULL,
    `order_code` VARCHAR(64) NOT NULL,
    `scheduled_date` DATE NOT NULL,
    `completed_date` DATE DEFAULT NULL,
    `status` VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    `result` VARCHAR(64) DEFAULT '',
    `remark` VARCHAR(255) DEFAULT '',
    `creator` VARCHAR(64) DEFAULT '',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updater` VARCHAR(64) DEFAULT '',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` BIT(1) NOT NULL DEFAULT b'0',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_factory_collab_order_code` (`order_code`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='业务协同-维保工单表';

CREATE TABLE IF NOT EXISTS `iot_factory_collab_carbon_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `tenant_id` BIGINT NOT NULL DEFAULT 1,
    `source_name` VARCHAR(100) NOT NULL,
    `source_type` VARCHAR(32) NOT NULL,
    `stat_date` DATE NOT NULL,
    `emission_value` DECIMAL(12, 2) NOT NULL DEFAULT 0,
    `unit_emission_value` DECIMAL(12, 2) NOT NULL DEFAULT 0,
    `target_value` DECIMAL(12, 2) NOT NULL DEFAULT 0,
    `creator` VARCHAR(64) DEFAULT '',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updater` VARCHAR(64) DEFAULT '',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` BIT(1) NOT NULL DEFAULT b'0',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_factory_collab_carbon_record` (`tenant_id`, `source_name`, `source_type`, `stat_date`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='业务协同-碳排核算表';

CREATE TABLE IF NOT EXISTS `iot_factory_collab_carbon_target` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `tenant_id` BIGINT NOT NULL DEFAULT 1,
    `target_year` INT NOT NULL,
    `annual_target_value` DECIMAL(12, 2) NOT NULL DEFAULT 0,
    `monthly_target_value` DECIMAL(12, 2) NOT NULL DEFAULT 0,
    `owner_name` VARCHAR(64) DEFAULT '',
    `status` VARCHAR(32) NOT NULL DEFAULT '启用',
    `creator` VARCHAR(64) DEFAULT '',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updater` VARCHAR(64) DEFAULT '',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` BIT(1) NOT NULL DEFAULT b'0',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_factory_collab_target_year` (`tenant_id`, `target_year`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='业务协同-碳目标表';

CREATE TABLE IF NOT EXISTS `iot_factory_collab_carbon_trade` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `tenant_id` BIGINT NOT NULL DEFAULT 1,
    `trade_code` VARCHAR(64) NOT NULL,
    `trade_type` VARCHAR(16) NOT NULL,
    `quantity` DECIMAL(12, 2) NOT NULL DEFAULT 0,
    `unit_price` DECIMAL(12, 2) NOT NULL DEFAULT 0,
    `amount` DECIMAL(12, 2) NOT NULL DEFAULT 0,
    `balance_after` DECIMAL(12, 2) NOT NULL DEFAULT 0,
    `trade_date` DATE NOT NULL,
    `counterparty` VARCHAR(100) NOT NULL,
    `status` VARCHAR(32) NOT NULL DEFAULT '已登记',
    `creator` VARCHAR(64) DEFAULT '',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updater` VARCHAR(64) DEFAULT '',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` BIT(1) NOT NULL DEFAULT b'0',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_factory_collab_trade_code` (`trade_code`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='业务协同-碳交易表';

INSERT INTO `iot_factory_collab_production_plan`
(`tenant_id`, `plan_code`, `product_name`, `batch_code`, `line_name`, `planned_quantity`, `completed_quantity`,
 `operator_name`, `planned_start_time`, `planned_end_time`, `status`, `progress`, `remark`, `creator`, `updater`)
SELECT 1, 'PLAN-001', '护肤霜', 'BATCH001', '车间A-产线1', 1000, 750, '张三',
       '2026-04-13 08:00:00', '2026-04-13 16:00:00', 'IN_PROGRESS', 75, '实时排产任务', 'system', 'system'
WHERE NOT EXISTS (SELECT 1 FROM `iot_factory_collab_production_plan` WHERE `plan_code` = 'PLAN-001' AND `deleted` = b'0');

INSERT INTO `iot_factory_collab_production_plan`
(`tenant_id`, `plan_code`, `product_name`, `batch_code`, `line_name`, `planned_quantity`, `completed_quantity`,
 `operator_name`, `planned_start_time`, `planned_end_time`, `status`, `progress`, `remark`, `creator`, `updater`)
SELECT 1, 'PLAN-002', '洁面乳', 'BATCH002', '车间A-产线2', 800, 240, '李四',
       '2026-04-13 10:00:00', '2026-04-13 18:00:00', 'IN_PROGRESS', 30, '跨班组协同', 'system', 'system'
WHERE NOT EXISTS (SELECT 1 FROM `iot_factory_collab_production_plan` WHERE `plan_code` = 'PLAN-002' AND `deleted` = b'0');

INSERT INTO `iot_factory_collab_production_plan`
(`tenant_id`, `plan_code`, `product_name`, `batch_code`, `line_name`, `planned_quantity`, `completed_quantity`,
 `operator_name`, `planned_start_time`, `planned_end_time`, `status`, `progress`, `remark`, `creator`, `updater`)
SELECT 1, 'PLAN-003', '精华液', 'BATCH003', '洁净区C', 1200, 0, '待分配',
       '2026-04-13 14:00:00', '2026-04-13 22:00:00', 'PENDING', 0, '待分配班组', 'system', 'system'
WHERE NOT EXISTS (SELECT 1 FROM `iot_factory_collab_production_plan` WHERE `plan_code` = 'PLAN-003' AND `deleted` = b'0');

INSERT INTO `iot_factory_collab_production_plan`
(`tenant_id`, `plan_code`, `product_name`, `batch_code`, `line_name`, `planned_quantity`, `completed_quantity`,
 `operator_name`, `planned_start_time`, `planned_end_time`, `status`, `progress`, `remark`, `creator`, `updater`)
SELECT 1, 'PLAN-004', '面膜', 'BATCH004', '包装区', 500, 500, '王五',
       '2026-04-12 08:00:00', '2026-04-12 16:00:00', 'COMPLETED', 100, '前一日已完成', 'system', 'system'
WHERE NOT EXISTS (SELECT 1 FROM `iot_factory_collab_production_plan` WHERE `plan_code` = 'PLAN-004' AND `deleted` = b'0');

INSERT INTO `iot_factory_collab_batch_trace`
(`tenant_id`, `plan_id`, `batch_code`, `product_name`, `current_process`, `current_location`, `completed_quantity`,
 `yield_rate`, `status`, `trace_time`, `creator`, `updater`)
SELECT 1, p.id, 'BATCH001', '护肤霜', '灌装中', '车间A-产线1', 750, 98.60, 'IN_PROGRESS',
       '2026-04-13 11:30:00', 'system', 'system'
FROM `iot_factory_collab_production_plan` p
WHERE p.plan_code = 'PLAN-001' AND p.deleted = b'0'
  AND NOT EXISTS (SELECT 1 FROM `iot_factory_collab_batch_trace` WHERE `batch_code` = 'BATCH001' AND `deleted` = b'0');

INSERT INTO `iot_factory_collab_batch_trace`
(`tenant_id`, `plan_id`, `batch_code`, `product_name`, `current_process`, `current_location`, `completed_quantity`,
 `yield_rate`, `status`, `trace_time`, `creator`, `updater`)
SELECT 1, p.id, 'BATCH002', '洁面乳', '配料中', '车间A-产线2', 240, 96.80, 'IN_PROGRESS',
       '2026-04-13 11:40:00', 'system', 'system'
FROM `iot_factory_collab_production_plan` p
WHERE p.plan_code = 'PLAN-002' AND p.deleted = b'0'
  AND NOT EXISTS (SELECT 1 FROM `iot_factory_collab_batch_trace` WHERE `batch_code` = 'BATCH002' AND `deleted` = b'0');

INSERT INTO `iot_factory_collab_batch_trace`
(`tenant_id`, `plan_id`, `batch_code`, `product_name`, `current_process`, `current_location`, `completed_quantity`,
 `yield_rate`, `status`, `trace_time`, `creator`, `updater`)
SELECT 1, p.id, 'BATCH003', '精华液', '待投产', '洁净区C', 0, 0, '待开始',
       '2026-04-13 11:45:00', 'system', 'system'
FROM `iot_factory_collab_production_plan` p
WHERE p.plan_code = 'PLAN-003' AND p.deleted = b'0'
  AND NOT EXISTS (SELECT 1 FROM `iot_factory_collab_batch_trace` WHERE `batch_code` = 'BATCH003' AND `deleted` = b'0');

INSERT INTO `iot_factory_collab_batch_trace`
(`tenant_id`, `plan_id`, `batch_code`, `product_name`, `current_process`, `current_location`, `completed_quantity`,
 `yield_rate`, `status`, `trace_time`, `creator`, `updater`)
SELECT 1, p.id, 'BATCH004', '面膜', '完工入库', '包装区', 500, 100, 'COMPLETED',
       '2026-04-12 15:50:00', 'system', 'system'
FROM `iot_factory_collab_production_plan` p
WHERE p.plan_code = 'PLAN-004' AND p.deleted = b'0'
  AND NOT EXISTS (SELECT 1 FROM `iot_factory_collab_batch_trace` WHERE `batch_code` = 'BATCH004' AND `deleted` = b'0');

INSERT INTO `iot_factory_collab_batch_environment`
(`tenant_id`, `batch_trace_id`, `record_time`, `temperature_value`, `humidity_value`, `pressure_value`, `ph_value`,
 `clean_level`, `recorder_name`, `creator`, `updater`)
SELECT 1, b.id, '2026-04-13 08:20:00', 24.60, 43.20, 101.20, 6.80, 'A级', '环境监测员', 'system', 'system'
FROM `iot_factory_collab_batch_trace` b
WHERE b.batch_code = 'BATCH001' AND b.deleted = b'0'
  AND NOT EXISTS (
      SELECT 1 FROM `iot_factory_collab_batch_environment`
      WHERE `batch_trace_id` = b.id AND `record_time` = '2026-04-13 08:20:00' AND `clean_level` = 'A级' AND `deleted` = b'0'
  );

INSERT INTO `iot_factory_collab_batch_environment`
(`tenant_id`, `batch_trace_id`, `record_time`, `temperature_value`, `humidity_value`, `pressure_value`, `ph_value`,
 `clean_level`, `recorder_name`, `creator`, `updater`)
SELECT 1, b.id, '2026-04-13 10:10:00', 23.90, 45.80, 101.00, 6.70, 'A级', '环境监测员', 'system', 'system'
FROM `iot_factory_collab_batch_trace` b
WHERE b.batch_code = 'BATCH001' AND b.deleted = b'0'
  AND NOT EXISTS (
      SELECT 1 FROM `iot_factory_collab_batch_environment`
      WHERE `batch_trace_id` = b.id AND `record_time` = '2026-04-13 10:10:00' AND `clean_level` = 'A级' AND `deleted` = b'0'
  );

INSERT INTO `iot_factory_collab_batch_environment`
(`tenant_id`, `batch_trace_id`, `record_time`, `temperature_value`, `humidity_value`, `pressure_value`, `ph_value`,
 `clean_level`, `recorder_name`, `creator`, `updater`)
SELECT 1, b.id, '2026-04-13 10:40:00', 25.20, 47.10, 101.10, 6.60, 'B级', '环境监测员', 'system', 'system'
FROM `iot_factory_collab_batch_trace` b
WHERE b.batch_code = 'BATCH002' AND b.deleted = b'0'
  AND NOT EXISTS (
      SELECT 1 FROM `iot_factory_collab_batch_environment`
      WHERE `batch_trace_id` = b.id AND `record_time` = '2026-04-13 10:40:00' AND `clean_level` = 'B级' AND `deleted` = b'0'
  );

INSERT INTO `iot_factory_collab_batch_environment`
(`tenant_id`, `batch_trace_id`, `record_time`, `temperature_value`, `humidity_value`, `pressure_value`, `ph_value`,
 `clean_level`, `recorder_name`, `creator`, `updater`)
SELECT 1, b.id, '2026-04-13 14:10:00', 22.80, 41.50, 100.90, 6.90, 'A级', '环境监测员', 'system', 'system'
FROM `iot_factory_collab_batch_trace` b
WHERE b.batch_code = 'BATCH003' AND b.deleted = b'0'
  AND NOT EXISTS (
      SELECT 1 FROM `iot_factory_collab_batch_environment`
      WHERE `batch_trace_id` = b.id AND `record_time` = '2026-04-13 14:10:00' AND `clean_level` = 'A级' AND `deleted` = b'0'
  );

INSERT INTO `iot_factory_collab_batch_environment`
(`tenant_id`, `batch_trace_id`, `record_time`, `temperature_value`, `humidity_value`, `pressure_value`, `ph_value`,
 `clean_level`, `recorder_name`, `creator`, `updater`)
SELECT 1, b.id, '2026-04-12 15:20:00', 24.10, 42.40, 101.30, 7.00, 'A级', '环境监测员', 'system', 'system'
FROM `iot_factory_collab_batch_trace` b
WHERE b.batch_code = 'BATCH004' AND b.deleted = b'0'
  AND NOT EXISTS (
      SELECT 1 FROM `iot_factory_collab_batch_environment`
      WHERE `batch_trace_id` = b.id AND `record_time` = '2026-04-12 15:20:00' AND `clean_level` = 'A级' AND `deleted` = b'0'
  );

INSERT INTO `iot_factory_collab_batch_quality`
(`tenant_id`, `batch_trace_id`, `sample_name`, `inspection_item`, `standard_value`, `measured_value`,
 `result_status`, `record_time`, `inspector_name`, `creator`, `updater`)
SELECT 1, b.id, '中控样A', '黏度', '3200-3600cP', '3410cP', 'PASS', '2026-04-13 10:30:00', 'QC-王敏', 'system', 'system'
FROM `iot_factory_collab_batch_trace` b
WHERE b.batch_code = 'BATCH001' AND b.deleted = b'0'
  AND NOT EXISTS (
      SELECT 1 FROM `iot_factory_collab_batch_quality`
      WHERE `batch_trace_id` = b.id AND `sample_name` = '中控样A' AND `inspection_item` = '黏度' AND `deleted` = b'0'
  );

INSERT INTO `iot_factory_collab_batch_quality`
(`tenant_id`, `batch_trace_id`, `sample_name`, `inspection_item`, `standard_value`, `measured_value`,
 `result_status`, `record_time`, `inspector_name`, `creator`, `updater`)
SELECT 1, b.id, '中控样B', '微生物', '<100CFU/g', '48CFU/g', 'PASS', '2026-04-13 11:05:00', 'QC-王敏', 'system', 'system'
FROM `iot_factory_collab_batch_trace` b
WHERE b.batch_code = 'BATCH001' AND b.deleted = b'0'
  AND NOT EXISTS (
      SELECT 1 FROM `iot_factory_collab_batch_quality`
      WHERE `batch_trace_id` = b.id AND `sample_name` = '中控样B' AND `inspection_item` = '微生物' AND `deleted` = b'0'
  );

INSERT INTO `iot_factory_collab_batch_quality`
(`tenant_id`, `batch_trace_id`, `sample_name`, `inspection_item`, `standard_value`, `measured_value`,
 `result_status`, `record_time`, `inspector_name`, `creator`, `updater`)
SELECT 1, b.id, '首件样', 'pH', '5.5-6.5', '6.7', 'WARN', '2026-04-13 11:15:00', 'QC-李洁', 'system', 'system'
FROM `iot_factory_collab_batch_trace` b
WHERE b.batch_code = 'BATCH002' AND b.deleted = b'0'
  AND NOT EXISTS (
      SELECT 1 FROM `iot_factory_collab_batch_quality`
      WHERE `batch_trace_id` = b.id AND `sample_name` = '首件样' AND `inspection_item` = 'pH' AND `deleted` = b'0'
  );

INSERT INTO `iot_factory_collab_batch_quality`
(`tenant_id`, `batch_trace_id`, `sample_name`, `inspection_item`, `standard_value`, `measured_value`,
 `result_status`, `record_time`, `inspector_name`, `creator`, `updater`)
SELECT 1, b.id, '投产前确认', '外观', '澄清透明', '澄清透明', 'PASS', '2026-04-13 13:55:00', 'QC-陈璐', 'system', 'system'
FROM `iot_factory_collab_batch_trace` b
WHERE b.batch_code = 'BATCH003' AND b.deleted = b'0'
  AND NOT EXISTS (
      SELECT 1 FROM `iot_factory_collab_batch_quality`
      WHERE `batch_trace_id` = b.id AND `sample_name` = '投产前确认' AND `inspection_item` = '外观' AND `deleted` = b'0'
  );

INSERT INTO `iot_factory_collab_batch_quality`
(`tenant_id`, `batch_trace_id`, `sample_name`, `inspection_item`, `standard_value`, `measured_value`,
 `result_status`, `record_time`, `inspector_name`, `creator`, `updater`)
SELECT 1, b.id, '成品放行样', '净含量', '25±0.5g', '25.1g', 'PASS', '2026-04-12 15:40:00', 'QC-周晴', 'system', 'system'
FROM `iot_factory_collab_batch_trace` b
WHERE b.batch_code = 'BATCH004' AND b.deleted = b'0'
  AND NOT EXISTS (
      SELECT 1 FROM `iot_factory_collab_batch_quality`
      WHERE `batch_trace_id` = b.id AND `sample_name` = '成品放行样' AND `inspection_item` = '净含量' AND `deleted` = b'0'
  );

INSERT INTO `iot_factory_collab_batch_personnel`
(`tenant_id`, `batch_trace_id`, `role_name`, `staff_name`, `operation_name`, `workstation_name`,
 `record_time`, `duration_minutes`, `remark`, `creator`, `updater`)
SELECT 1, b.id, '班组长', '张三', '灌装复核', '灌装工位-01', '2026-04-13 10:55:00', 35, '完成批次切换确认', 'system', 'system'
FROM `iot_factory_collab_batch_trace` b
WHERE b.batch_code = 'BATCH001' AND b.deleted = b'0'
  AND NOT EXISTS (
      SELECT 1 FROM `iot_factory_collab_batch_personnel`
      WHERE `batch_trace_id` = b.id AND `staff_name` = '张三' AND `operation_name` = '灌装复核' AND `deleted` = b'0'
  );

INSERT INTO `iot_factory_collab_batch_personnel`
(`tenant_id`, `batch_trace_id`, `role_name`, `staff_name`, `operation_name`, `workstation_name`,
 `record_time`, `duration_minutes`, `remark`, `creator`, `updater`)
SELECT 1, b.id, '操作员', '李四', '配料投料', '制备工位-02', '2026-04-13 10:20:00', 48, '完成主料与香精投料', 'system', 'system'
FROM `iot_factory_collab_batch_trace` b
WHERE b.batch_code = 'BATCH002' AND b.deleted = b'0'
  AND NOT EXISTS (
      SELECT 1 FROM `iot_factory_collab_batch_personnel`
      WHERE `batch_trace_id` = b.id AND `staff_name` = '李四' AND `operation_name` = '配料投料' AND `deleted` = b'0'
  );

INSERT INTO `iot_factory_collab_batch_personnel`
(`tenant_id`, `batch_trace_id`, `role_name`, `staff_name`, `operation_name`, `workstation_name`,
 `record_time`, `duration_minutes`, `remark`, `creator`, `updater`)
SELECT 1, b.id, '调度员', '王五', '投产确认', '洁净区C', '2026-04-13 13:40:00', 20, '完成投产前资源校验', 'system', 'system'
FROM `iot_factory_collab_batch_trace` b
WHERE b.batch_code = 'BATCH003' AND b.deleted = b'0'
  AND NOT EXISTS (
      SELECT 1 FROM `iot_factory_collab_batch_personnel`
      WHERE `batch_trace_id` = b.id AND `staff_name` = '王五' AND `operation_name` = '投产确认' AND `deleted` = b'0'
  );

INSERT INTO `iot_factory_collab_batch_personnel`
(`tenant_id`, `batch_trace_id`, `role_name`, `staff_name`, `operation_name`, `workstation_name`,
 `record_time`, `duration_minutes`, `remark`, `creator`, `updater`)
SELECT 1, b.id, '质检员', '赵敏', '成品放行', '包装复核位', '2026-04-12 15:45:00', 25, '完成成品放行复核', 'system', 'system'
FROM `iot_factory_collab_batch_trace` b
WHERE b.batch_code = 'BATCH004' AND b.deleted = b'0'
  AND NOT EXISTS (
      SELECT 1 FROM `iot_factory_collab_batch_personnel`
      WHERE `batch_trace_id` = b.id AND `staff_name` = '赵敏' AND `operation_name` = '成品放行' AND `deleted` = b'0'
  );

INSERT INTO `iot_factory_collab_batch_device`
(`tenant_id`, `batch_trace_id`, `device_code`, `device_name`, `operation_name`, `running_status`,
 `parameter_summary`, `record_time`, `operator_name`, `creator`, `updater`)
SELECT 1, b.id, 'EQ-007', '灌装机-01', '灌装', '运行中', '速度 120 支/分钟，扭矩稳定', '2026-04-13 11:00:00', '张三', 'system', 'system'
FROM `iot_factory_collab_batch_trace` b
WHERE b.batch_code = 'BATCH001' AND b.deleted = b'0'
  AND NOT EXISTS (
      SELECT 1 FROM `iot_factory_collab_batch_device`
      WHERE `batch_trace_id` = b.id AND `device_code` = 'EQ-007' AND `operation_name` = '灌装' AND `deleted` = b'0'
  );

INSERT INTO `iot_factory_collab_batch_device`
(`tenant_id`, `batch_trace_id`, `device_code`, `device_name`, `operation_name`, `running_status`,
 `parameter_summary`, `record_time`, `operator_name`, `creator`, `updater`)
SELECT 1, b.id, 'EQ-009', '搅拌罐-01', '配料', '运行中', '转速 280rpm，真空度 -0.06MPa', '2026-04-13 10:05:00', '李四', 'system', 'system'
FROM `iot_factory_collab_batch_trace` b
WHERE b.batch_code = 'BATCH002' AND b.deleted = b'0'
  AND NOT EXISTS (
      SELECT 1 FROM `iot_factory_collab_batch_device`
      WHERE `batch_trace_id` = b.id AND `device_code` = 'EQ-009' AND `operation_name` = '配料' AND `deleted` = b'0'
  );

INSERT INTO `iot_factory_collab_batch_device`
(`tenant_id`, `batch_trace_id`, `device_code`, `device_name`, `operation_name`, `running_status`,
 `parameter_summary`, `record_time`, `operator_name`, `creator`, `updater`)
SELECT 1, b.id, 'EQ-010', '真空乳化机', '投产准备', '待机', '夹层预热完成，待批次切入', '2026-04-13 13:50:00', '王五', 'system', 'system'
FROM `iot_factory_collab_batch_trace` b
WHERE b.batch_code = 'BATCH003' AND b.deleted = b'0'
  AND NOT EXISTS (
      SELECT 1 FROM `iot_factory_collab_batch_device`
      WHERE `batch_trace_id` = b.id AND `device_code` = 'EQ-010' AND `operation_name` = '投产准备' AND `deleted` = b'0'
  );

INSERT INTO `iot_factory_collab_batch_device`
(`tenant_id`, `batch_trace_id`, `device_code`, `device_name`, `operation_name`, `running_status`,
 `parameter_summary`, `record_time`, `operator_name`, `creator`, `updater`)
SELECT 1, b.id, 'EQ-008', '包装线-01', '包装', '已完成', '累计包装 500 盒，视觉检测通过', '2026-04-12 15:35:00', '赵敏', 'system', 'system'
FROM `iot_factory_collab_batch_trace` b
WHERE b.batch_code = 'BATCH004' AND b.deleted = b'0'
  AND NOT EXISTS (
      SELECT 1 FROM `iot_factory_collab_batch_device`
      WHERE `batch_trace_id` = b.id AND `device_code` = 'EQ-008' AND `operation_name` = '包装' AND `deleted` = b'0'
  );

INSERT INTO `iot_factory_collab_batch_material`
(`tenant_id`, `batch_trace_id`, `material_code`, `material_name`, `material_type`, `material_batch_no`,
 `planned_quantity`, `actual_quantity`, `unit`, `feeder_name`, `record_time`, `creator`, `updater`)
SELECT 1, b.id, 'MAT-001', '基础乳化液', '主料', 'RM-202604-11', 680.00, 678.50, 'kg', '李四',
       '2026-04-13 09:20:00', 'system', 'system'
FROM `iot_factory_collab_batch_trace` b
WHERE b.batch_code = 'BATCH001' AND b.deleted = b'0'
  AND NOT EXISTS (
      SELECT 1 FROM `iot_factory_collab_batch_material`
      WHERE `batch_trace_id` = b.id AND `material_code` = 'MAT-001' AND `material_batch_no` = 'RM-202604-11' AND `deleted` = b'0'
  );

INSERT INTO `iot_factory_collab_batch_material`
(`tenant_id`, `batch_trace_id`, `material_code`, `material_name`, `material_type`, `material_batch_no`,
 `planned_quantity`, `actual_quantity`, `unit`, `feeder_name`, `record_time`, `creator`, `updater`)
SELECT 1, b.id, 'MAT-013', '活性清洁因子', '辅料', 'RM-202604-18', 96.00, 95.80, 'kg', '李四',
       '2026-04-13 09:55:00', 'system', 'system'
FROM `iot_factory_collab_batch_trace` b
WHERE b.batch_code = 'BATCH002' AND b.deleted = b'0'
  AND NOT EXISTS (
      SELECT 1 FROM `iot_factory_collab_batch_material`
      WHERE `batch_trace_id` = b.id AND `material_code` = 'MAT-013' AND `material_batch_no` = 'RM-202604-18' AND `deleted` = b'0'
  );

INSERT INTO `iot_factory_collab_batch_material`
(`tenant_id`, `batch_trace_id`, `material_code`, `material_name`, `material_type`, `material_batch_no`,
 `planned_quantity`, `actual_quantity`, `unit`, `feeder_name`, `record_time`, `creator`, `updater`)
SELECT 1, b.id, 'MAT-021', '高保湿精华基液', '主料', 'RM-202604-20', 880.00, 0.00, 'kg', '待分配',
       '2026-04-13 13:45:00', 'system', 'system'
FROM `iot_factory_collab_batch_trace` b
WHERE b.batch_code = 'BATCH003' AND b.deleted = b'0'
  AND NOT EXISTS (
      SELECT 1 FROM `iot_factory_collab_batch_material`
      WHERE `batch_trace_id` = b.id AND `material_code` = 'MAT-021' AND `material_batch_no` = 'RM-202604-20' AND `deleted` = b'0'
  );

INSERT INTO `iot_factory_collab_batch_material`
(`tenant_id`, `batch_trace_id`, `material_code`, `material_name`, `material_type`, `material_batch_no`,
 `planned_quantity`, `actual_quantity`, `unit`, `feeder_name`, `record_time`, `creator`, `updater`)
SELECT 1, b.id, 'MAT-031', '无纺布基材', '包装材料', 'PK-202604-02', 500.00, 500.00, '片', '王五',
       '2026-04-12 15:05:00', 'system', 'system'
FROM `iot_factory_collab_batch_trace` b
WHERE b.batch_code = 'BATCH004' AND b.deleted = b'0'
  AND NOT EXISTS (
      SELECT 1 FROM `iot_factory_collab_batch_material`
      WHERE `batch_trace_id` = b.id AND `material_code` = 'MAT-031' AND `material_batch_no` = 'PK-202604-02' AND `deleted` = b'0'
  );

INSERT INTO `iot_factory_collab_production_exception`
(`tenant_id`, `plan_id`, `batch_trace_id`, `alert_title`, `level_label`, `line_name`, `handler_name`,
 `status`, `happened_at`, `description`, `creator`, `updater`)
SELECT 1, p.id, b.id, '温度超标', '紧急', '车间A-产线1', '赵工', '处理中',
       '2026-04-13 11:28:00', '生产罐温度高于阈值', 'system', 'system'
FROM `iot_factory_collab_production_plan` p
INNER JOIN `iot_factory_collab_batch_trace` b ON b.plan_id = p.id AND b.deleted = b'0'
WHERE p.plan_code = 'PLAN-001' AND p.deleted = b'0'
  AND NOT EXISTS (SELECT 1 FROM `iot_factory_collab_production_exception` WHERE `alert_title` = '温度超标' AND `deleted` = b'0');

INSERT INTO `iot_factory_collab_production_exception`
(`tenant_id`, `plan_id`, `batch_trace_id`, `alert_title`, `level_label`, `line_name`, `handler_name`,
 `status`, `happened_at`, `description`, `creator`, `updater`)
SELECT 1, p.id, b.id, '电导率异常', '重要', '纯水系统', '设备组', '待处理',
       '2026-04-13 11:20:00', '纯水制备段参数异常', 'system', 'system'
FROM `iot_factory_collab_production_plan` p
INNER JOIN `iot_factory_collab_batch_trace` b ON b.plan_id = p.id AND b.deleted = b'0'
WHERE p.plan_code = 'PLAN-002' AND p.deleted = b'0'
  AND NOT EXISTS (SELECT 1 FROM `iot_factory_collab_production_exception` WHERE `alert_title` = '电导率异常' AND `deleted` = b'0');

INSERT INTO `iot_factory_collab_production_exception`
(`tenant_id`, `plan_id`, `batch_trace_id`, `alert_title`, `level_label`, `line_name`, `handler_name`,
 `status`, `happened_at`, `description`, `creator`, `updater`)
SELECT 1, p.id, b.id, '批次切换待确认', '提示', '洁净区C', '调度员', '待处理',
       '2026-04-13 10:50:00', '排产切换前需完成设备点检', 'system', 'system'
FROM `iot_factory_collab_production_plan` p
INNER JOIN `iot_factory_collab_batch_trace` b ON b.plan_id = p.id AND b.deleted = b'0'
WHERE p.plan_code = 'PLAN-003' AND p.deleted = b'0'
  AND NOT EXISTS (SELECT 1 FROM `iot_factory_collab_production_exception` WHERE `alert_title` = '批次切换待确认' AND `deleted` = b'0');

INSERT INTO `iot_factory_collab_energy_reading`
(`tenant_id`, `energy_type`, `area_name`, `device_name`, `stat_time`, `usage_value`, `unit`, `yoy_rate`, `creator`, `updater`)
VALUES
(1, 'electricity', '生产车间A', '空压机-01', '2026-04-13 00:00:00', 120, 'kWh', -5.2, 'system', 'system'),
(1, 'electricity', '生产车间B', '空调机组-01', '2026-04-13 02:00:00', 110, 'kWh', -5.2, 'system', 'system'),
(1, 'electricity', '办公区', '乳化罐-01', '2026-04-13 04:00:00', 100, 'kWh', -5.2, 'system', 'system'),
(1, 'electricity', '仓储区', '灌装机-01', '2026-04-13 06:00:00', 130, 'kWh', -5.2, 'system', 'system'),
(1, 'electricity', '生产车间A', '空压机-01', '2026-04-13 08:00:00', 180, 'kWh', -5.2, 'system', 'system'),
(1, 'electricity', '生产车间B', '空调机组-01', '2026-04-13 10:00:00', 200, 'kWh', -5.2, 'system', 'system'),
(1, 'electricity', '办公区', '乳化罐-01', '2026-04-13 12:00:00', 190, 'kWh', -5.2, 'system', 'system'),
(1, 'electricity', '仓储区', '灌装机-01', '2026-04-13 14:00:00', 210, 'kWh', -5.2, 'system', 'system'),
(1, 'electricity', '生产车间A', '空压机-01', '2026-04-13 16:00:00', 220, 'kWh', -5.2, 'system', 'system'),
(1, 'electricity', '生产车间B', '空调机组-01', '2026-04-13 18:00:00', 200, 'kWh', -5.2, 'system', 'system'),
(1, 'electricity', '办公区', '乳化罐-01', '2026-04-13 20:00:00', 170, 'kWh', -5.2, 'system', 'system'),
(1, 'electricity', '仓储区', '灌装机-01', '2026-04-13 22:00:00', 140, 'kWh', -5.2, 'system', 'system'),
(1, 'water', '生产车间A', '纯水系统-01', '2026-04-13 00:00:00', 60, 'm³', -3.1, 'system', 'system'),
(1, 'water', '生产车间B', '冷却塔-01', '2026-04-13 02:00:00', 55, 'm³', -3.1, 'system', 'system'),
(1, 'water', '办公区', '空调冷却回路', '2026-04-13 04:00:00', 50, 'm³', -3.1, 'system', 'system'),
(1, 'water', '仓储区', '喷淋系统', '2026-04-13 06:00:00', 65, 'm³', -3.1, 'system', 'system'),
(1, 'water', '生产车间A', '纯水系统-01', '2026-04-13 08:00:00', 90, 'm³', -3.1, 'system', 'system'),
(1, 'water', '生产车间B', '冷却塔-01', '2026-04-13 10:00:00', 100, 'm³', -3.1, 'system', 'system'),
(1, 'water', '办公区', '空调冷却回路', '2026-04-13 12:00:00', 95, 'm³', -3.1, 'system', 'system'),
(1, 'water', '仓储区', '喷淋系统', '2026-04-13 14:00:00', 110, 'm³', -3.1, 'system', 'system'),
(1, 'water', '生产车间A', '纯水系统-01', '2026-04-13 16:00:00', 115, 'm³', -3.1, 'system', 'system'),
(1, 'water', '生产车间B', '冷却塔-01', '2026-04-13 18:00:00', 100, 'm³', -3.1, 'system', 'system'),
(1, 'water', '办公区', '空调冷却回路', '2026-04-13 20:00:00', 80, 'm³', -3.1, 'system', 'system'),
(1, 'water', '仓储区', '喷淋系统', '2026-04-13 22:00:00', 70, 'm³', -3.1, 'system', 'system'),
(1, 'gas', '生产车间A', '蒸汽锅炉-01', '2026-04-13 00:00:00', 55, 'm³', -2.0, 'system', 'system'),
(1, 'gas', '生产车间B', '蒸汽锅炉-02', '2026-04-13 02:00:00', 52, 'm³', -2.0, 'system', 'system'),
(1, 'gas', '办公区', '食堂燃气', '2026-04-13 04:00:00', 48, 'm³', -2.0, 'system', 'system'),
(1, 'gas', '仓储区', '热水系统', '2026-04-13 06:00:00', 62, 'm³', -2.0, 'system', 'system'),
(1, 'gas', '生产车间A', '蒸汽锅炉-01', '2026-04-13 08:00:00', 88, 'm³', -2.0, 'system', 'system'),
(1, 'gas', '生产车间B', '蒸汽锅炉-02', '2026-04-13 10:00:00', 98, 'm³', -2.0, 'system', 'system'),
(1, 'gas', '办公区', '食堂燃气', '2026-04-13 12:00:00', 92, 'm³', -2.0, 'system', 'system'),
(1, 'gas', '仓储区', '热水系统', '2026-04-13 14:00:00', 108, 'm³', -2.0, 'system', 'system'),
(1, 'gas', '生产车间A', '蒸汽锅炉-01', '2026-04-13 16:00:00', 112, 'm³', -2.0, 'system', 'system'),
(1, 'gas', '生产车间B', '蒸汽锅炉-02', '2026-04-13 18:00:00', 96, 'm³', -2.0, 'system', 'system'),
(1, 'gas', '办公区', '食堂燃气', '2026-04-13 20:00:00', 78, 'm³', -2.0, 'system', 'system'),
(1, 'gas', '仓储区', '热水系统', '2026-04-13 22:00:00', 68, 'm³', -2.0, 'system', 'system')
ON DUPLICATE KEY UPDATE
`usage_value` = VALUES(`usage_value`),
`yoy_rate` = VALUES(`yoy_rate`),
`updater` = VALUES(`updater`),
`update_time` = CURRENT_TIMESTAMP;

INSERT INTO `iot_factory_collab_energy_suggestion`
(`tenant_id`, `title`, `content`, `level_label`, `status`, `sort_no`, `creator`, `updater`)
SELECT 1, '建议优化', '空压机夜间运行效率偏低，建议配置变频控制，预计节能 15%。', '建议', '待处理', 10, 'system', 'system'
WHERE NOT EXISTS (SELECT 1 FROM `iot_factory_collab_energy_suggestion` WHERE `title` = '建议优化' AND `deleted` = b'0');

INSERT INTO `iot_factory_collab_energy_suggestion`
(`tenant_id`, `title`, `content`, `level_label`, `status`, `sort_no`, `creator`, `updater`)
SELECT 1, '峰谷调节', '非生产时段可错峰用电，避开 9:00-12:00 高峰期。', '提醒', '待处理', 20, 'system', 'system'
WHERE NOT EXISTS (SELECT 1 FROM `iot_factory_collab_energy_suggestion` WHERE `title` = '峰谷调节' AND `deleted` = b'0');

INSERT INTO `iot_factory_collab_energy_suggestion`
(`tenant_id`, `title`, `content`, `level_label`, `status`, `sort_no`, `creator`, `updater`)
SELECT 1, '循环用水', '建议回收乳化冷却水，用于仓储喷淋与非关键清洗段。', '重要', '已处理', 30, 'system', 'system'
WHERE NOT EXISTS (SELECT 1 FROM `iot_factory_collab_energy_suggestion` WHERE `title` = '循环用水' AND `deleted` = b'0');

INSERT INTO `iot_factory_collab_device`
(`tenant_id`, `device_code`, `device_name`, `category_name`, `model_name`, `area_name`, `online_status`,
 `running_status`, `health_status`, `efficiency_rate`, `remark`, `creator`, `updater`)
VALUES
(1, 'EQ-001', '空压机-01', '空压机', 'AC-900', '动力车间', 1, '运行中', '正常', 95.00, '主供气设备', 'system', 'system'),
(1, 'EQ-002', '空压机-02', '空压机', 'AC-900', '动力车间', 1, '运行中', '正常', 92.00, '备用机组', 'system', 'system'),
(1, 'EQ-003', '纯水系统-01', '纯水系统', 'PW-100', '水处理间', 0, '维保中', '预警', 88.00, '例行维护', 'system', 'system'),
(1, 'EQ-004', '空调机组-01', '空调', 'AHU-500', '车间A', 1, '运行中', '正常', 90.00, '恒温恒湿', 'system', 'system'),
(1, 'EQ-005', '空调机组-02', '空调', 'AHU-500', '车间B', 1, '故障', '故障', 0.00, '待检修', 'system', 'system'),
(1, 'EQ-006', '乳化罐-01', '生产设备', 'EM-800', '制备区', 1, '运行中', '正常', 88.00, '主生产设备', 'system', 'system'),
(1, 'EQ-007', '灌装机-01', '生产设备', 'FM-260', '灌装区', 1, '运行中', '正常', 91.00, '灌装主线', 'system', 'system'),
(1, 'EQ-008', '包装线-01', '包装设备', 'PK-210', '包装区', 1, '停机', '正常', 0.00, '待切换批次', 'system', 'system'),
(1, 'EQ-009', '搅拌罐-01', '生产设备', 'MX-300', '制备区', 1, '运行中', '正常', 86.00, '配料段', 'system', 'system'),
(1, 'EQ-010', '真空乳化机', '生产设备', 'VM-600', '制备区', 1, '运行中', '正常', 89.00, '核心工序设备', 'system', 'system')
ON DUPLICATE KEY UPDATE
`device_name` = VALUES(`device_name`),
`area_name` = VALUES(`area_name`),
`online_status` = VALUES(`online_status`),
`running_status` = VALUES(`running_status`),
`health_status` = VALUES(`health_status`),
`efficiency_rate` = VALUES(`efficiency_rate`),
`updater` = VALUES(`updater`),
`update_time` = CURRENT_TIMESTAMP;

INSERT INTO `iot_factory_collab_maintenance_plan`
(`tenant_id`, `device_id`, `plan_name`, `cycle_type`, `last_execute_date`, `next_execute_date`, `owner_name`, `status`, `creator`, `updater`)
SELECT 1, d.id, '空压机月度点检', '月度', '2026-03-15', '2026-04-18', '设备管理员', 'IN_PROGRESS', 'system', 'system'
FROM `iot_factory_collab_device` d
WHERE d.device_code = 'EQ-001' AND d.deleted = b'0'
  AND NOT EXISTS (
      SELECT 1 FROM `iot_factory_collab_maintenance_plan`
      WHERE `plan_name` = '空压机月度点检' AND `deleted` = b'0'
  );

INSERT INTO `iot_factory_collab_maintenance_plan`
(`tenant_id`, `device_id`, `plan_name`, `cycle_type`, `last_execute_date`, `next_execute_date`, `owner_name`, `status`, `creator`, `updater`)
SELECT 1, d.id, '纯水系统周检', '周度', '2026-04-08', '2026-04-15', '维修班长', 'PLANNED', 'system', 'system'
FROM `iot_factory_collab_device` d
WHERE d.device_code = 'EQ-003' AND d.deleted = b'0'
  AND NOT EXISTS (
      SELECT 1 FROM `iot_factory_collab_maintenance_plan`
      WHERE `plan_name` = '纯水系统周检' AND `deleted` = b'0'
  );

INSERT INTO `iot_factory_collab_maintenance_order`
(`tenant_id`, `plan_id`, `device_id`, `order_code`, `scheduled_date`, `completed_date`, `status`, `result`, `remark`, `creator`, `updater`)
SELECT 1, p.id, p.device_id, 'MO-20260413-001', '2026-04-13', NULL, 'PENDING', '', '待执行', 'system', 'system'
FROM `iot_factory_collab_maintenance_plan` p
WHERE p.plan_name = '空压机月度点检' AND p.deleted = b'0'
  AND NOT EXISTS (
      SELECT 1 FROM `iot_factory_collab_maintenance_order`
      WHERE `order_code` = 'MO-20260413-001' AND `deleted` = b'0'
  );

INSERT INTO `iot_factory_collab_maintenance_order`
(`tenant_id`, `plan_id`, `device_id`, `order_code`, `scheduled_date`, `completed_date`, `status`, `result`, `remark`, `creator`, `updater`)
SELECT 1, p.id, p.device_id, 'MO-20260412-002', '2026-04-12', '2026-04-12', 'COMPLETED', '正常', '已完成点检', 'system', 'system'
FROM `iot_factory_collab_maintenance_plan` p
WHERE p.plan_name = '纯水系统周检' AND p.deleted = b'0'
  AND NOT EXISTS (
      SELECT 1 FROM `iot_factory_collab_maintenance_order`
      WHERE `order_code` = 'MO-20260412-002' AND `deleted` = b'0'
  );

INSERT INTO `iot_factory_collab_carbon_target`
(`tenant_id`, `target_year`, `annual_target_value`, `monthly_target_value`, `owner_name`, `status`, `creator`, `updater`)
SELECT 1, 2026, 1000.00, 1666.67, '碳资产专员', '启用', 'system', 'system'
WHERE NOT EXISTS (
    SELECT 1 FROM `iot_factory_collab_carbon_target`
    WHERE `target_year` = 2026 AND `deleted` = b'0'
);

INSERT INTO `iot_factory_collab_carbon_record`
(`tenant_id`, `source_name`, `source_type`, `stat_date`, `emission_value`, `unit_emission_value`, `target_value`, `creator`, `updater`)
VALUES
(1, '外购电', '能源间接排放', '2026-01-01', 980.00, 0.82, 1000.00, 'system', 'system'),
(1, '天然气', '能源直接排放', '2026-02-01', 1050.00, 0.83, 1000.00, 'system', 'system'),
(1, '蒸汽', '能源直接排放', '2026-03-01', 1100.00, 0.84, 1000.00, 'system', 'system'),
(1, '外购电', '能源间接排放', '2026-04-01', 420.00, 0.85, 1000.00, 'system', 'system'),
(1, '天然气', '能源直接排放', '2026-04-01', 310.00, 0.85, 1000.00, 'system', 'system'),
(1, '蒸汽', '能源直接排放', '2026-04-01', 280.00, 0.85, 1000.00, 'system', 'system'),
(1, '生产过程', '工艺排放', '2026-04-01', 240.00, 0.85, 1000.00, 'system', 'system'),
(1, '外购电', '能源间接排放', '2026-05-01', 1180.00, 0.86, 1000.00, 'system', 'system'),
(1, '天然气', '能源直接排放', '2026-06-01', 1230.00, 0.87, 1000.00, 'system', 'system')
ON DUPLICATE KEY UPDATE
`emission_value` = VALUES(`emission_value`),
`unit_emission_value` = VALUES(`unit_emission_value`),
`target_value` = VALUES(`target_value`),
`updater` = VALUES(`updater`),
`update_time` = CURRENT_TIMESTAMP;

INSERT INTO `iot_factory_collab_carbon_trade`
(`tenant_id`, `trade_code`, `trade_type`, `quantity`, `unit_price`, `amount`, `balance_after`, `trade_date`, `counterparty`, `status`, `creator`, `updater`)
SELECT 1, 'CT-202604-001', 'BUY', 300.00, 42.00, 12600.00, 300.00, '2026-04-08', '华东交易中心', '已登记', 'system', 'system'
WHERE NOT EXISTS (SELECT 1 FROM `iot_factory_collab_carbon_trade` WHERE `trade_code` = 'CT-202604-001' AND `deleted` = b'0');

INSERT INTO `iot_factory_collab_carbon_trade`
(`tenant_id`, `trade_code`, `trade_type`, `quantity`, `unit_price`, `amount`, `balance_after`, `trade_date`, `counterparty`, `status`, `creator`, `updater`)
SELECT 1, 'CT-202604-002', 'SELL', 120.00, 46.00, 5520.00, 180.00, '2026-04-10', '华中配额平台', '已登记', 'system', 'system'
WHERE NOT EXISTS (SELECT 1 FROM `iot_factory_collab_carbon_trade` WHERE `trade_code` = 'CT-202604-002' AND `deleted` = b'0');

INSERT INTO `iot_factory_collab_carbon_trade`
(`tenant_id`, `trade_code`, `trade_type`, `quantity`, `unit_price`, `amount`, `balance_after`, `trade_date`, `counterparty`, `status`, `creator`, `updater`)
SELECT 1, 'CT-202604-003', 'BUY', 80.00, 44.00, 3520.00, 260.00, '2026-04-12', '绿色资产公司', '已登记', 'system', 'system'
WHERE NOT EXISTS (SELECT 1 FROM `iot_factory_collab_carbon_trade` WHERE `trade_code` = 'CT-202604-003' AND `deleted` = b'0');

CREATE TABLE IF NOT EXISTS `iot_factory_compliance_gmp_point` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `tenant_id` BIGINT NOT NULL DEFAULT 1,
    `point_name` VARCHAR(64) NOT NULL,
    `sort_no` INT NOT NULL DEFAULT 0,
    `total_points` INT NOT NULL DEFAULT 0,
    `creator` VARCHAR(64) DEFAULT '',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updater` VARCHAR(64) DEFAULT '',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` BIT(1) NOT NULL DEFAULT b'0',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_factory_compliance_gmp_point` (`tenant_id`, `point_name`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='合规管理-GMP监测点位表';

CREATE TABLE IF NOT EXISTS `iot_factory_compliance_gmp_inspection` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `tenant_id` BIGINT NOT NULL DEFAULT 1,
    `point_id` BIGINT NOT NULL,
    `temperature_value` DECIMAL(10, 2) NOT NULL DEFAULT 0,
    `humidity_value` DECIMAL(10, 2) NOT NULL DEFAULT 0,
    `pressure_value` DECIMAL(10, 2) NOT NULL DEFAULT 0,
    `compliant_count` INT NOT NULL DEFAULT 0,
    `exceed_count` INT NOT NULL DEFAULT 0,
    `compliance_rate` DECIMAL(10, 2) NOT NULL DEFAULT 0,
    `status` VARCHAR(32) NOT NULL DEFAULT '正常',
    `checked_at` DATETIME NOT NULL,
    `creator` VARCHAR(64) DEFAULT '',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updater` VARCHAR(64) DEFAULT '',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` BIT(1) NOT NULL DEFAULT b'0',
    PRIMARY KEY (`id`),
    KEY `idx_factory_compliance_gmp_inspection_point` (`tenant_id`, `point_id`, `checked_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='合规管理-GMP检查记录表';

CREATE TABLE IF NOT EXISTS `iot_factory_compliance_gmp_exception` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `tenant_id` BIGINT NOT NULL DEFAULT 1,
    `point_id` BIGINT NOT NULL,
    `title` VARCHAR(100) NOT NULL,
    `status` VARCHAR(32) NOT NULL DEFAULT '待处理',
    `handler_name` VARCHAR(64) DEFAULT '',
    `description` VARCHAR(255) DEFAULT '',
    `happened_at` DATETIME NOT NULL,
    `creator` VARCHAR(64) DEFAULT '',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updater` VARCHAR(64) DEFAULT '',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` BIT(1) NOT NULL DEFAULT b'0',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='合规管理-GMP异常表';

CREATE TABLE IF NOT EXISTS `iot_factory_compliance_env_point` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `tenant_id` BIGINT NOT NULL DEFAULT 1,
    `region_name` VARCHAR(64) NOT NULL,
    `point_name` VARCHAR(64) NOT NULL,
    `sort_no` INT NOT NULL DEFAULT 0,
    `unit` VARCHAR(16) NOT NULL DEFAULT '',
    `standard_value` DECIMAL(12, 2) NOT NULL DEFAULT 0,
    `creator` VARCHAR(64) DEFAULT '',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updater` VARCHAR(64) DEFAULT '',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` BIT(1) NOT NULL DEFAULT b'0',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_factory_compliance_env_point` (`tenant_id`, `region_name`, `point_name`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='合规管理-环保监测点位表';

CREATE TABLE IF NOT EXISTS `iot_factory_compliance_env_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `tenant_id` BIGINT NOT NULL DEFAULT 1,
    `point_id` BIGINT NOT NULL,
    `region_name` VARCHAR(64) NOT NULL,
    `sort_no` INT NOT NULL DEFAULT 0,
    `current_value` DECIMAL(12, 2) NOT NULL DEFAULT 0,
    `standard_value` DECIMAL(12, 2) NOT NULL DEFAULT 0,
    `unit` VARCHAR(16) NOT NULL DEFAULT '',
    `exceed_count` INT NOT NULL DEFAULT 0,
    `compliance_rate` DECIMAL(10, 2) NOT NULL DEFAULT 0,
    `status` VARCHAR(32) NOT NULL DEFAULT '正常',
    `record_time` DATETIME NOT NULL,
    `creator` VARCHAR(64) DEFAULT '',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updater` VARCHAR(64) DEFAULT '',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` BIT(1) NOT NULL DEFAULT b'0',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='合规管理-环保监测记录表';

CREATE TABLE IF NOT EXISTS `iot_factory_compliance_env_alert` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `tenant_id` BIGINT NOT NULL DEFAULT 1,
    `point_id` BIGINT NOT NULL,
    `title` VARCHAR(100) NOT NULL,
    `status` VARCHAR(32) NOT NULL DEFAULT '待处理',
    `handler_name` VARCHAR(64) DEFAULT '',
    `description` VARCHAR(255) DEFAULT '',
    `happened_at` DATETIME NOT NULL,
    `creator` VARCHAR(64) DEFAULT '',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updater` VARCHAR(64) DEFAULT '',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` BIT(1) NOT NULL DEFAULT b'0',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='合规管理-环保告警表';

CREATE TABLE IF NOT EXISTS `iot_factory_compliance_batch_trace` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `tenant_id` BIGINT NOT NULL DEFAULT 1,
    `batch_code` VARCHAR(64) NOT NULL,
    `product_name` VARCHAR(100) NOT NULL,
    `checkpoint_count` INT NOT NULL DEFAULT 0,
    `issue_count` INT NOT NULL DEFAULT 0,
    `compliance_rate` DECIMAL(10, 2) NOT NULL DEFAULT 0,
    `status` VARCHAR(32) NOT NULL DEFAULT '进行中',
    `last_check_time` DATETIME NOT NULL,
    `creator` VARCHAR(64) DEFAULT '',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updater` VARCHAR(64) DEFAULT '',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` BIT(1) NOT NULL DEFAULT b'0',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_factory_compliance_batch_trace` (`tenant_id`, `batch_code`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='合规管理-批次追溯主表';

CREATE TABLE IF NOT EXISTS `iot_factory_compliance_batch_checkpoint` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `tenant_id` BIGINT NOT NULL DEFAULT 1,
    `batch_trace_id` BIGINT NOT NULL,
    `checkpoint_name` VARCHAR(100) NOT NULL,
    `checker_name` VARCHAR(64) DEFAULT '',
    `result_status` VARCHAR(32) NOT NULL DEFAULT 'PASS',
    `remark` VARCHAR(255) DEFAULT '',
    `checked_at` DATETIME NOT NULL,
    `creator` VARCHAR(64) DEFAULT '',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updater` VARCHAR(64) DEFAULT '',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` BIT(1) NOT NULL DEFAULT b'0',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='合规管理-批次检查点记录表';

CREATE TABLE IF NOT EXISTS `iot_factory_compliance_batch_document` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `tenant_id` BIGINT NOT NULL DEFAULT 1,
    `batch_trace_id` BIGINT NOT NULL,
    `document_name` VARCHAR(100) NOT NULL,
    `document_code` VARCHAR(64) NOT NULL,
    `status` VARCHAR(32) NOT NULL DEFAULT '已归档',
    `uploaded_at` DATETIME NOT NULL,
    `creator` VARCHAR(64) DEFAULT '',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updater` VARCHAR(64) DEFAULT '',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` BIT(1) NOT NULL DEFAULT b'0',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='合规管理-批次文档表';

CREATE TABLE IF NOT EXISTS `iot_factory_compliance_batch_release` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `tenant_id` BIGINT NOT NULL DEFAULT 1,
    `batch_trace_id` BIGINT NOT NULL,
    `release_code` VARCHAR(64) NOT NULL,
    `status` VARCHAR(32) NOT NULL DEFAULT '已放行',
    `approver_name` VARCHAR(64) DEFAULT '',
    `released_at` DATETIME NOT NULL,
    `creator` VARCHAR(64) DEFAULT '',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updater` VARCHAR(64) DEFAULT '',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` BIT(1) NOT NULL DEFAULT b'0',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='合规管理-批次放行表';

CREATE TABLE IF NOT EXISTS `iot_factory_compliance_batch_issue` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `tenant_id` BIGINT NOT NULL DEFAULT 1,
    `batch_trace_id` BIGINT NOT NULL,
    `issue_title` VARCHAR(100) NOT NULL,
    `status` VARCHAR(32) NOT NULL DEFAULT '待处理',
    `owner_name` VARCHAR(64) DEFAULT '',
    `issue_desc` VARCHAR(255) DEFAULT '',
    `happened_at` DATETIME NOT NULL,
    `creator` VARCHAR(64) DEFAULT '',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updater` VARCHAR(64) DEFAULT '',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` BIT(1) NOT NULL DEFAULT b'0',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='合规管理-批次异常表';

INSERT INTO `iot_factory_compliance_gmp_point` (`tenant_id`, `point_name`, `sort_no`, `total_points`, `creator`, `updater`)
SELECT 1, '洁净区A', 1, 4, 'system', 'system'
WHERE NOT EXISTS (SELECT 1 FROM `iot_factory_compliance_gmp_point` WHERE `point_name` = '洁净区A' AND `deleted` = b'0');
INSERT INTO `iot_factory_compliance_gmp_point` (`tenant_id`, `point_name`, `sort_no`, `total_points`, `creator`, `updater`)
SELECT 1, '洁净区B', 2, 4, 'system', 'system'
WHERE NOT EXISTS (SELECT 1 FROM `iot_factory_compliance_gmp_point` WHERE `point_name` = '洁净区B' AND `deleted` = b'0');
INSERT INTO `iot_factory_compliance_gmp_point` (`tenant_id`, `point_name`, `sort_no`, `total_points`, `creator`, `updater`)
SELECT 1, '洁净区C', 3, 4, 'system', 'system'
WHERE NOT EXISTS (SELECT 1 FROM `iot_factory_compliance_gmp_point` WHERE `point_name` = '洁净区C' AND `deleted` = b'0');
INSERT INTO `iot_factory_compliance_gmp_point` (`tenant_id`, `point_name`, `sort_no`, `total_points`, `creator`, `updater`)
SELECT 1, '洁净区D', 4, 4, 'system', 'system'
WHERE NOT EXISTS (SELECT 1 FROM `iot_factory_compliance_gmp_point` WHERE `point_name` = '洁净区D' AND `deleted` = b'0');
INSERT INTO `iot_factory_compliance_gmp_point` (`tenant_id`, `point_name`, `sort_no`, `total_points`, `creator`, `updater`)
SELECT 1, '洁净区E', 5, 4, 'system', 'system'
WHERE NOT EXISTS (SELECT 1 FROM `iot_factory_compliance_gmp_point` WHERE `point_name` = '洁净区E' AND `deleted` = b'0');
INSERT INTO `iot_factory_compliance_gmp_point` (`tenant_id`, `point_name`, `sort_no`, `total_points`, `creator`, `updater`)
SELECT 1, '原料仓库', 6, 5, 'system', 'system'
WHERE NOT EXISTS (SELECT 1 FROM `iot_factory_compliance_gmp_point` WHERE `point_name` = '原料仓库' AND `deleted` = b'0');
INSERT INTO `iot_factory_compliance_gmp_point` (`tenant_id`, `point_name`, `sort_no`, `total_points`, `creator`, `updater`)
SELECT 1, '成品仓库', 7, 5, 'system', 'system'
WHERE NOT EXISTS (SELECT 1 FROM `iot_factory_compliance_gmp_point` WHERE `point_name` = '成品仓库' AND `deleted` = b'0');

INSERT INTO `iot_factory_compliance_gmp_inspection`
(`tenant_id`, `point_id`, `temperature_value`, `humidity_value`, `pressure_value`, `compliant_count`, `exceed_count`, `compliance_rate`, `status`, `checked_at`, `creator`, `updater`)
SELECT 1, p.id, 22.00, 55.00, 15.00, 4, 0, 100.00, '正常', '2026-04-13 08:00:00', 'system', 'system'
FROM `iot_factory_compliance_gmp_point` p
WHERE p.point_name = '洁净区A' AND p.deleted = b'0'
  AND NOT EXISTS (SELECT 1 FROM `iot_factory_compliance_gmp_inspection` WHERE `point_id` = p.id AND `deleted` = b'0');
INSERT INTO `iot_factory_compliance_gmp_inspection`
(`tenant_id`, `point_id`, `temperature_value`, `humidity_value`, `pressure_value`, `compliant_count`, `exceed_count`, `compliance_rate`, `status`, `checked_at`, `creator`, `updater`)
SELECT 1, p.id, 21.00, 52.00, 12.00, 4, 0, 100.00, '正常', '2026-04-13 08:00:00', 'system', 'system'
FROM `iot_factory_compliance_gmp_point` p
WHERE p.point_name = '洁净区B' AND p.deleted = b'0'
  AND NOT EXISTS (SELECT 1 FROM `iot_factory_compliance_gmp_inspection` WHERE `point_id` = p.id AND `deleted` = b'0');
INSERT INTO `iot_factory_compliance_gmp_inspection`
(`tenant_id`, `point_id`, `temperature_value`, `humidity_value`, `pressure_value`, `compliant_count`, `exceed_count`, `compliance_rate`, `status`, `checked_at`, `creator`, `updater`)
SELECT 1, p.id, 23.00, 58.00, 8.00, 3, 1, 75.00, '注意', '2026-04-13 08:00:00', 'system', 'system'
FROM `iot_factory_compliance_gmp_point` p
WHERE p.point_name = '洁净区C' AND p.deleted = b'0'
  AND NOT EXISTS (SELECT 1 FROM `iot_factory_compliance_gmp_inspection` WHERE `point_id` = p.id AND `deleted` = b'0');
INSERT INTO `iot_factory_compliance_gmp_inspection`
(`tenant_id`, `point_id`, `temperature_value`, `humidity_value`, `pressure_value`, `compliant_count`, `exceed_count`, `compliance_rate`, `status`, `checked_at`, `creator`, `updater`)
SELECT 1, p.id, 22.00, 54.00, 14.00, 4, 0, 100.00, '正常', '2026-04-13 08:00:00', 'system', 'system'
FROM `iot_factory_compliance_gmp_point` p
WHERE p.point_name = '洁净区D' AND p.deleted = b'0'
  AND NOT EXISTS (SELECT 1 FROM `iot_factory_compliance_gmp_inspection` WHERE `point_id` = p.id AND `deleted` = b'0');
INSERT INTO `iot_factory_compliance_gmp_inspection`
(`tenant_id`, `point_id`, `temperature_value`, `humidity_value`, `pressure_value`, `compliant_count`, `exceed_count`, `compliance_rate`, `status`, `checked_at`, `creator`, `updater`)
SELECT 1, p.id, 24.00, 57.00, 9.00, 3, 1, 75.00, '注意', '2026-04-13 08:00:00', 'system', 'system'
FROM `iot_factory_compliance_gmp_point` p
WHERE p.point_name = '洁净区E' AND p.deleted = b'0'
  AND NOT EXISTS (SELECT 1 FROM `iot_factory_compliance_gmp_inspection` WHERE `point_id` = p.id AND `deleted` = b'0');
INSERT INTO `iot_factory_compliance_gmp_inspection`
(`tenant_id`, `point_id`, `temperature_value`, `humidity_value`, `pressure_value`, `compliant_count`, `exceed_count`, `compliance_rate`, `status`, `checked_at`, `creator`, `updater`)
SELECT 1, p.id, 20.00, 48.00, 16.00, 5, 0, 100.00, '正常', '2026-04-13 08:00:00', 'system', 'system'
FROM `iot_factory_compliance_gmp_point` p
WHERE p.point_name = '原料仓库' AND p.deleted = b'0'
  AND NOT EXISTS (SELECT 1 FROM `iot_factory_compliance_gmp_inspection` WHERE `point_id` = p.id AND `deleted` = b'0');
INSERT INTO `iot_factory_compliance_gmp_inspection`
(`tenant_id`, `point_id`, `temperature_value`, `humidity_value`, `pressure_value`, `compliant_count`, `exceed_count`, `compliance_rate`, `status`, `checked_at`, `creator`, `updater`)
SELECT 1, p.id, 21.00, 50.00, 15.00, 5, 0, 100.00, '正常', '2026-04-13 08:00:00', 'system', 'system'
FROM `iot_factory_compliance_gmp_point` p
WHERE p.point_name = '成品仓库' AND p.deleted = b'0'
  AND NOT EXISTS (SELECT 1 FROM `iot_factory_compliance_gmp_inspection` WHERE `point_id` = p.id AND `deleted` = b'0');

INSERT INTO `iot_factory_compliance_gmp_exception`
(`tenant_id`, `point_id`, `title`, `status`, `handler_name`, `description`, `happened_at`, `creator`, `updater`)
SELECT 1, p.id, '洁净区C压差异常', '处理中', '质量负责人', '压差低于 GMP 阈值 10Pa', '2026-04-13 08:05:00', 'system', 'system'
FROM `iot_factory_compliance_gmp_point` p
WHERE p.point_name = '洁净区C' AND p.deleted = b'0'
  AND NOT EXISTS (SELECT 1 FROM `iot_factory_compliance_gmp_exception` WHERE `point_id` = p.id AND `title` = '洁净区C压差异常' AND `deleted` = b'0');
INSERT INTO `iot_factory_compliance_gmp_exception`
(`tenant_id`, `point_id`, `title`, `status`, `handler_name`, `description`, `happened_at`, `creator`, `updater`)
SELECT 1, p.id, '洁净区E湿度偏高', '待复核', 'QA主管', '洁净区E湿度高于标准值 5%', '2026-04-13 08:10:00', 'system', 'system'
FROM `iot_factory_compliance_gmp_point` p
WHERE p.point_name = '洁净区E' AND p.deleted = b'0'
  AND NOT EXISTS (SELECT 1 FROM `iot_factory_compliance_gmp_exception` WHERE `point_id` = p.id AND `title` = '洁净区E湿度偏高' AND `deleted` = b'0');

INSERT INTO `iot_factory_compliance_env_point`
(`tenant_id`, `region_name`, `point_name`, `sort_no`, `unit`, `standard_value`, `creator`, `updater`)
SELECT 1, '废水处理站', 'COD监测点', 1, 'mg/L', 60.00, 'system', 'system'
WHERE NOT EXISTS (SELECT 1 FROM `iot_factory_compliance_env_point` WHERE `region_name` = '废水处理站' AND `point_name` = 'COD监测点' AND `deleted` = b'0');
INSERT INTO `iot_factory_compliance_env_point`
(`tenant_id`, `region_name`, `point_name`, `sort_no`, `unit`, `standard_value`, `creator`, `updater`)
SELECT 1, '废气处理塔', '颗粒物监测点', 2, 'mg/m3', 20.00, 'system', 'system'
WHERE NOT EXISTS (SELECT 1 FROM `iot_factory_compliance_env_point` WHERE `region_name` = '废气处理塔' AND `point_name` = '颗粒物监测点' AND `deleted` = b'0');
INSERT INTO `iot_factory_compliance_env_point`
(`tenant_id`, `region_name`, `point_name`, `sort_no`, `unit`, `standard_value`, `creator`, `updater`)
SELECT 1, '危废暂存间', 'VOC监测点', 3, 'ppm', 80.00, 'system', 'system'
WHERE NOT EXISTS (SELECT 1 FROM `iot_factory_compliance_env_point` WHERE `region_name` = '危废暂存间' AND `point_name` = 'VOC监测点' AND `deleted` = b'0');
INSERT INTO `iot_factory_compliance_env_point`
(`tenant_id`, `region_name`, `point_name`, `sort_no`, `unit`, `standard_value`, `creator`, `updater`)
SELECT 1, '锅炉房', '烟尘监测点', 4, 'mg/m3', 30.00, 'system', 'system'
WHERE NOT EXISTS (SELECT 1 FROM `iot_factory_compliance_env_point` WHERE `region_name` = '锅炉房' AND `point_name` = '烟尘监测点' AND `deleted` = b'0');

INSERT INTO `iot_factory_compliance_env_record`
(`tenant_id`, `point_id`, `region_name`, `sort_no`, `current_value`, `standard_value`, `unit`, `exceed_count`, `compliance_rate`, `status`, `record_time`, `creator`, `updater`)
SELECT 1, p.id, p.region_name, p.sort_no, 55.00, 60.00, 'mg/L', 0, 100.00, '正常', '2026-04-13 09:00:00', 'system', 'system'
FROM `iot_factory_compliance_env_point` p
WHERE p.region_name = '废水处理站' AND p.deleted = b'0'
  AND NOT EXISTS (SELECT 1 FROM `iot_factory_compliance_env_record` WHERE `point_id` = p.id AND `deleted` = b'0');
INSERT INTO `iot_factory_compliance_env_record`
(`tenant_id`, `point_id`, `region_name`, `sort_no`, `current_value`, `standard_value`, `unit`, `exceed_count`, `compliance_rate`, `status`, `record_time`, `creator`, `updater`)
SELECT 1, p.id, p.region_name, p.sort_no, 18.00, 20.00, 'mg/m3', 0, 100.00, '正常', '2026-04-13 09:05:00', 'system', 'system'
FROM `iot_factory_compliance_env_point` p
WHERE p.region_name = '废气处理塔' AND p.deleted = b'0'
  AND NOT EXISTS (SELECT 1 FROM `iot_factory_compliance_env_record` WHERE `point_id` = p.id AND `deleted` = b'0');
INSERT INTO `iot_factory_compliance_env_record`
(`tenant_id`, `point_id`, `region_name`, `sort_no`, `current_value`, `standard_value`, `unit`, `exceed_count`, `compliance_rate`, `status`, `record_time`, `creator`, `updater`)
SELECT 1, p.id, p.region_name, p.sort_no, 92.00, 80.00, 'ppm', 1, 72.00, '注意', '2026-04-13 09:10:00', 'system', 'system'
FROM `iot_factory_compliance_env_point` p
WHERE p.region_name = '危废暂存间' AND p.deleted = b'0'
  AND NOT EXISTS (SELECT 1 FROM `iot_factory_compliance_env_record` WHERE `point_id` = p.id AND `deleted` = b'0');
INSERT INTO `iot_factory_compliance_env_record`
(`tenant_id`, `point_id`, `region_name`, `sort_no`, `current_value`, `standard_value`, `unit`, `exceed_count`, `compliance_rate`, `status`, `record_time`, `creator`, `updater`)
SELECT 1, p.id, p.region_name, p.sort_no, 26.00, 30.00, 'mg/m3', 0, 96.00, '正常', '2026-04-13 09:15:00', 'system', 'system'
FROM `iot_factory_compliance_env_point` p
WHERE p.region_name = '锅炉房' AND p.deleted = b'0'
  AND NOT EXISTS (SELECT 1 FROM `iot_factory_compliance_env_record` WHERE `point_id` = p.id AND `deleted` = b'0');

INSERT INTO `iot_factory_compliance_env_alert`
(`tenant_id`, `point_id`, `title`, `status`, `handler_name`, `description`, `happened_at`, `creator`, `updater`)
SELECT 1, p.id, '危废暂存间 VOC 偏高', '处理中', '环保主管', 'VOC 指标超过标准值 12ppm', '2026-04-13 09:12:00', 'system', 'system'
FROM `iot_factory_compliance_env_point` p
WHERE p.region_name = '危废暂存间' AND p.deleted = b'0'
  AND NOT EXISTS (SELECT 1 FROM `iot_factory_compliance_env_alert` WHERE `point_id` = p.id AND `title` = '危废暂存间 VOC 偏高' AND `deleted` = b'0');

INSERT INTO `iot_factory_compliance_env_point`
(`tenant_id`, `region_name`, `point_name`, `sort_no`, `unit`, `standard_value`, `creator`, `updater`)
SELECT 1, src.region_name, src.point_name, src.sort_no, src.unit, src.standard_value, 'system', 'system'
FROM (
    SELECT 'VOCs' AS region_name, 'VOCs监测点' AS point_name, 10 AS sort_no, 'mg/m3' AS unit, 20.00 AS standard_value
    UNION ALL SELECT '颗粒物', '颗粒物监测点', 11, 'mg/m3', 50.00
    UNION ALL SELECT '二氧化硫', '二氧化硫监测点', 12, 'mg/m3', 50.00
    UNION ALL SELECT '氮氧化物', '氮氧化物监测点', 13, 'mg/m3', 100.00
    UNION ALL SELECT 'COD', 'COD监测点', 20, 'mg/L', 100.00
    UNION ALL SELECT '氨氮', '氨氮监测点', 21, 'mg/L', 15.00
    UNION ALL SELECT 'pH值', 'pH值监测点', 22, '', 14.00
    UNION ALL SELECT '流量', '流量监测点', 23, 'm3/h', 200.00
    UNION ALL SELECT '昼间', '昼间噪声监测点', 30, 'dB', 65.00
    UNION ALL SELECT '夜间', '夜间噪声监测点', 31, 'dB', 65.00
    UNION ALL SELECT '限值', '噪声限值点', 32, 'dB', 65.00
) src
WHERE NOT EXISTS (
    SELECT 1 FROM `iot_factory_compliance_env_point` p
    WHERE p.`tenant_id` = 1 AND p.`region_name` = src.region_name AND p.`point_name` = src.point_name AND p.`deleted` = b'0'
);

INSERT INTO `iot_factory_compliance_env_record`
(`tenant_id`, `point_id`, `region_name`, `sort_no`, `current_value`, `standard_value`, `unit`, `exceed_count`, `compliance_rate`, `status`, `record_time`, `creator`, `updater`)
SELECT 1, p.id, src.region_name, src.sort_no, src.current_value, src.standard_value, src.unit, src.exceed_count, src.compliance_rate, src.status, src.record_time, 'system', 'system'
FROM (
    SELECT 'VOCs' AS region_name, 'VOCs监测点' AS point_name, 10 AS sort_no, 12.50 AS current_value, 20.00 AS standard_value, 'mg/m3' AS unit, 0 AS exceed_count, 98.00 AS compliance_rate, '注意' AS status, '2026-04-14 10:30:00' AS record_time
    UNION ALL SELECT '颗粒物', '颗粒物监测点', 11, 35.00, 50.00, 'mg/m3', 0, 97.00, '注意', '2026-04-14 10:10:00'
    UNION ALL SELECT '二氧化硫', '二氧化硫监测点', 12, 8.00, 50.00, 'mg/m3', 0, 100.00, '正常', '2026-04-14 09:50:00'
    UNION ALL SELECT '氮氧化物', '氮氧化物监测点', 13, 45.00, 100.00, 'mg/m3', 0, 100.00, '正常', '2026-04-14 09:40:00'
    UNION ALL SELECT 'COD', 'COD监测点', 20, 45.00, 100.00, 'mg/L', 0, 100.00, '正常', '2026-04-14 09:00:00'
    UNION ALL SELECT '氨氮', '氨氮监测点', 21, 8.50, 15.00, 'mg/L', 0, 100.00, '正常', '2026-04-14 09:05:00'
    UNION ALL SELECT 'pH值', 'pH值监测点', 22, 7.20, 14.00, '', 0, 100.00, '正常', '2026-04-14 09:06:00'
    UNION ALL SELECT '流量', '流量监测点', 23, 120.00, 200.00, 'm3/h', 0, 100.00, '正常', '2026-04-14 09:08:00'
    UNION ALL SELECT '昼间', '昼间噪声监测点', 30, 58.00, 65.00, 'dB', 0, 100.00, '正常', '2026-04-14 10:00:00'
    UNION ALL SELECT '夜间', '夜间噪声监测点', 31, 52.00, 65.00, 'dB', 0, 96.00, '注意', '2026-04-14 10:05:00'
    UNION ALL SELECT '限值', '噪声限值点', 32, 65.00, 65.00, 'dB', 0, 100.00, '正常', '2026-04-14 10:05:00'
) src
INNER JOIN `iot_factory_compliance_env_point` p
    ON p.`tenant_id` = 1 AND p.`region_name` = src.region_name AND p.`point_name` = src.point_name AND p.`deleted` = b'0'
WHERE NOT EXISTS (
    SELECT 1 FROM `iot_factory_compliance_env_record` r
    WHERE r.`point_id` = p.id AND r.`deleted` = b'0'
);

INSERT INTO `iot_factory_compliance_env_alert`
(`tenant_id`, `point_id`, `title`, `status`, `handler_name`, `description`, `happened_at`, `creator`, `updater`)
SELECT 1, p.id, src.title, src.status, src.handler_name, src.description, src.happened_at, 'system', 'system'
FROM (
    SELECT 'VOCs' AS region_name, 'VOCs监测点' AS point_name, 'VOCs浓度预警' AS title, '处理中' AS status, '环保主管' AS handler_name, '生产车间VOCs浓度达到12.5mg/m3，接近限值 20mg/m3' AS description, '2026-04-14 10:30:00' AS happened_at
    UNION ALL SELECT 'COD', 'COD监测点', '废水排放正常', '已处理', '值班工程师', '污水处理站运行正常，当前 COD 为45mg/L，排放达标', '2026-04-14 09:00:00'
) src
INNER JOIN `iot_factory_compliance_env_point` p
    ON p.`tenant_id` = 1 AND p.`region_name` = src.region_name AND p.`point_name` = src.point_name AND p.`deleted` = b'0'
WHERE NOT EXISTS (
    SELECT 1 FROM `iot_factory_compliance_env_alert` a
    WHERE a.`point_id` = p.id AND a.`title` = src.title AND a.`deleted` = b'0'
);

INSERT INTO `iot_factory_compliance_batch_trace`
(`tenant_id`, `batch_code`, `product_name`, `checkpoint_count`, `issue_count`, `compliance_rate`, `status`, `last_check_time`, `creator`, `updater`)
SELECT 1, 'CB-20260413-001', '舒缓修护乳', 5, 0, 100.00, '已完成', '2026-04-13 11:20:00', 'system', 'system'
WHERE NOT EXISTS (SELECT 1 FROM `iot_factory_compliance_batch_trace` WHERE `batch_code` = 'CB-20260413-001' AND `deleted` = b'0');
INSERT INTO `iot_factory_compliance_batch_trace`
(`tenant_id`, `batch_code`, `product_name`, `checkpoint_count`, `issue_count`, `compliance_rate`, `status`, `last_check_time`, `creator`, `updater`)
SELECT 1, 'CB-20260413-002', '净肤洁面乳', 4, 1, 80.00, '整改中', '2026-04-13 12:10:00', 'system', 'system'
WHERE NOT EXISTS (SELECT 1 FROM `iot_factory_compliance_batch_trace` WHERE `batch_code` = 'CB-20260413-002' AND `deleted` = b'0');
INSERT INTO `iot_factory_compliance_batch_trace`
(`tenant_id`, `batch_code`, `product_name`, `checkpoint_count`, `issue_count`, `compliance_rate`, `status`, `last_check_time`, `creator`, `updater`)
SELECT 1, 'CB-20260413-003', '玻尿酸精华液', 6, 0, 96.00, '待复核', '2026-04-13 13:35:00', 'system', 'system'
WHERE NOT EXISTS (SELECT 1 FROM `iot_factory_compliance_batch_trace` WHERE `batch_code` = 'CB-20260413-003' AND `deleted` = b'0');

INSERT INTO `iot_factory_compliance_batch_checkpoint`
(`tenant_id`, `batch_trace_id`, `checkpoint_name`, `checker_name`, `result_status`, `remark`, `checked_at`, `creator`, `updater`)
SELECT 1, b.id, '投料复核', 'QA-张敏', 'PASS', '原料信息一致', '2026-04-13 10:00:00', 'system', 'system'
FROM `iot_factory_compliance_batch_trace` b
WHERE b.batch_code = 'CB-20260413-001' AND b.deleted = b'0'
  AND NOT EXISTS (SELECT 1 FROM `iot_factory_compliance_batch_checkpoint` WHERE `batch_trace_id` = b.id AND `checkpoint_name` = '投料复核' AND `deleted` = b'0');
INSERT INTO `iot_factory_compliance_batch_checkpoint`
(`tenant_id`, `batch_trace_id`, `checkpoint_name`, `checker_name`, `result_status`, `remark`, `checked_at`, `creator`, `updater`)
SELECT 1, b.id, '灌装前线清场', 'QA-张敏', 'PASS', '线清场确认通过', '2026-04-13 10:30:00', 'system', 'system'
FROM `iot_factory_compliance_batch_trace` b
WHERE b.batch_code = 'CB-20260413-001' AND b.deleted = b'0'
  AND NOT EXISTS (SELECT 1 FROM `iot_factory_compliance_batch_checkpoint` WHERE `batch_trace_id` = b.id AND `checkpoint_name` = '灌装前线清场' AND `deleted` = b'0');
INSERT INTO `iot_factory_compliance_batch_checkpoint`
(`tenant_id`, `batch_trace_id`, `checkpoint_name`, `checker_name`, `result_status`, `remark`, `checked_at`, `creator`, `updater`)
SELECT 1, b.id, '文件签核', 'QA-李洁', 'WARN', '批记录签字不完整', '2026-04-13 11:40:00', 'system', 'system'
FROM `iot_factory_compliance_batch_trace` b
WHERE b.batch_code = 'CB-20260413-002' AND b.deleted = b'0'
  AND NOT EXISTS (SELECT 1 FROM `iot_factory_compliance_batch_checkpoint` WHERE `batch_trace_id` = b.id AND `checkpoint_name` = '文件签核' AND `deleted` = b'0');
INSERT INTO `iot_factory_compliance_batch_checkpoint`
(`tenant_id`, `batch_trace_id`, `checkpoint_name`, `checker_name`, `result_status`, `remark`, `checked_at`, `creator`, `updater`)
SELECT 1, b.id, '成品放行复核', 'QA-王蕾', 'PASS', '待完成最终复核', '2026-04-13 13:20:00', 'system', 'system'
FROM `iot_factory_compliance_batch_trace` b
WHERE b.batch_code = 'CB-20260413-003' AND b.deleted = b'0'
  AND NOT EXISTS (SELECT 1 FROM `iot_factory_compliance_batch_checkpoint` WHERE `batch_trace_id` = b.id AND `checkpoint_name` = '成品放行复核' AND `deleted` = b'0');

INSERT INTO `iot_factory_compliance_batch_document`
(`tenant_id`, `batch_trace_id`, `document_name`, `document_code`, `status`, `uploaded_at`, `creator`, `updater`)
SELECT 1, b.id, '批生产记录', 'DOC-CB001', '已归档', '2026-04-13 11:30:00', 'system', 'system'
FROM `iot_factory_compliance_batch_trace` b
WHERE b.batch_code = 'CB-20260413-001' AND b.deleted = b'0'
  AND NOT EXISTS (SELECT 1 FROM `iot_factory_compliance_batch_document` WHERE `batch_trace_id` = b.id AND `document_code` = 'DOC-CB001' AND `deleted` = b'0');

INSERT INTO `iot_factory_compliance_batch_release`
(`tenant_id`, `batch_trace_id`, `release_code`, `status`, `approver_name`, `released_at`, `creator`, `updater`)
SELECT 1, b.id, 'REL-CB001', '已放行', 'QA经理', '2026-04-13 11:50:00', 'system', 'system'
FROM `iot_factory_compliance_batch_trace` b
WHERE b.batch_code = 'CB-20260413-001' AND b.deleted = b'0'
  AND NOT EXISTS (SELECT 1 FROM `iot_factory_compliance_batch_release` WHERE `batch_trace_id` = b.id AND `release_code` = 'REL-CB001' AND `deleted` = b'0');

INSERT INTO `iot_factory_compliance_batch_issue`
(`tenant_id`, `batch_trace_id`, `issue_title`, `status`, `owner_name`, `issue_desc`, `happened_at`, `creator`, `updater`)
SELECT 1, b.id, '批记录签字缺失', '整改中', '车间主任', '文件签核缺少第二复核人签字', '2026-04-13 11:45:00', 'system', 'system'
FROM `iot_factory_compliance_batch_trace` b
WHERE b.batch_code = 'CB-20260413-002' AND b.deleted = b'0'
  AND NOT EXISTS (SELECT 1 FROM `iot_factory_compliance_batch_issue` WHERE `batch_trace_id` = b.id AND `issue_title` = '批记录签字缺失' AND `deleted` = b'0');

INSERT INTO `iot_factory_compliance_batch_trace`
(`tenant_id`, `batch_code`, `product_name`, `checkpoint_count`, `issue_count`, `compliance_rate`, `status`, `last_check_time`, `creator`, `updater`)
SELECT 1, src.batch_code, src.product_name, src.checkpoint_count, src.issue_count, src.compliance_rate, src.status, src.last_check_time, 'system', 'system'
FROM (
    SELECT 'CB-20260414-004' AS batch_code, '舒润保湿水' AS product_name, 5 AS checkpoint_count, 0 AS issue_count, 100.00 AS compliance_rate, '已完成' AS status, '2026-04-14 09:20:00' AS last_check_time
    UNION ALL SELECT 'CB-20260414-005', '胶原修护霜', 6, 1, 98.00, '待复核', '2026-04-14 10:15:00'
    UNION ALL SELECT 'CB-20260414-006', '清透防晒乳', 5, 1, 82.00, '整改中', '2026-04-14 11:05:00'
    UNION ALL SELECT 'CB-20260414-007', '氨基酸洁面泡沫', 4, 0, 95.00, '待放行', '2026-04-14 11:40:00'
) src
WHERE NOT EXISTS (
    SELECT 1 FROM `iot_factory_compliance_batch_trace` b
    WHERE b.`batch_code` = src.batch_code AND b.`deleted` = b'0'
);

INSERT INTO `iot_factory_compliance_batch_checkpoint`
(`tenant_id`, `batch_trace_id`, `checkpoint_name`, `checker_name`, `result_status`, `remark`, `checked_at`, `creator`, `updater`)
SELECT 1, b.id, src.checkpoint_name, src.checker_name, src.result_status, src.remark, src.checked_at, 'system', 'system'
FROM (
    SELECT 'CB-20260414-004' AS batch_code, '投料复核' AS checkpoint_name, 'QA-陈颖' AS checker_name, 'PASS' AS result_status, '投料批号一致' AS remark, '2026-04-14 08:10:00' AS checked_at
    UNION ALL SELECT 'CB-20260414-004', '灌装首件确认', 'QA-陈颖', 'PASS', '首件外观合格', '2026-04-14 08:45:00'
    UNION ALL SELECT 'CB-20260414-004', '成品入库复核', '仓储-周宁', 'PASS', '入库数量一致', '2026-04-14 09:18:00'
    UNION ALL SELECT 'CB-20260414-005', '原料称量复核', 'QA-李洁', 'PASS', '称量误差在范围内', '2026-04-14 09:00:00'
    UNION ALL SELECT 'CB-20260414-005', '乳化段巡检', 'QA-李洁', 'PASS', '温控稳定', '2026-04-14 09:35:00'
    UNION ALL SELECT 'CB-20260414-005', '文件签核', 'QA-王蕾', 'WARN', '批记录补录中', '2026-04-14 10:12:00'
    UNION ALL SELECT 'CB-20260414-006', '投料复核', 'QA-张敏', 'PASS', '辅料信息一致', '2026-04-14 09:50:00'
    UNION ALL SELECT 'CB-20260414-006', '防晒剂含量复核', 'QC-孙悦', 'WARN', '复核结果待二次确认', '2026-04-14 10:25:00'
    UNION ALL SELECT 'CB-20260414-006', '成品留样', 'QC-孙悦', 'PASS', '留样已封存', '2026-04-14 11:00:00'
    UNION ALL SELECT 'CB-20260414-007', '投料复核', 'QA-刘婷', 'PASS', '洁面基料批次一致', '2026-04-14 10:20:00'
    UNION ALL SELECT 'CB-20260414-007', '泡沫密度检测', 'QC-赵青', 'PASS', '密度检测合格', '2026-04-14 10:55:00'
    UNION ALL SELECT 'CB-20260414-007', '包装前清场', 'QA-刘婷', 'PASS', '现场清场完成', '2026-04-14 11:32:00'
) src
INNER JOIN `iot_factory_compliance_batch_trace` b
    ON b.`batch_code` = src.batch_code AND b.`deleted` = b'0'
WHERE NOT EXISTS (
    SELECT 1 FROM `iot_factory_compliance_batch_checkpoint` c
    WHERE c.`batch_trace_id` = b.id AND c.`checkpoint_name` = src.checkpoint_name AND c.`deleted` = b'0'
);

INSERT INTO `iot_factory_compliance_batch_document`
(`tenant_id`, `batch_trace_id`, `document_name`, `document_code`, `status`, `uploaded_at`, `creator`, `updater`)
SELECT 1, b.id, src.document_name, src.document_code, src.status, src.uploaded_at, 'system', 'system'
FROM (
    SELECT 'CB-20260414-004' AS batch_code, '批生产记录' AS document_name, 'DOC-CB004' AS document_code, '已归档' AS status, '2026-04-14 09:10:00' AS uploaded_at
    UNION ALL SELECT 'CB-20260414-005', '中间产品检测单', 'DOC-CB005', '待复核', '2026-04-14 10:05:00'
    UNION ALL SELECT 'CB-20260414-006', '偏差处理单', 'DOC-CB006', '整改中', '2026-04-14 10:55:00'
    UNION ALL SELECT 'CB-20260414-007', '包装记录', 'DOC-CB007', '待归档', '2026-04-14 11:36:00'
) src
INNER JOIN `iot_factory_compliance_batch_trace` b
    ON b.`batch_code` = src.batch_code AND b.`deleted` = b'0'
WHERE NOT EXISTS (
    SELECT 1 FROM `iot_factory_compliance_batch_document` d
    WHERE d.`batch_trace_id` = b.id AND d.`document_code` = src.document_code AND d.`deleted` = b'0'
);

INSERT INTO `iot_factory_compliance_batch_release`
(`tenant_id`, `batch_trace_id`, `release_code`, `status`, `approver_name`, `released_at`, `creator`, `updater`)
SELECT 1, b.id, src.release_code, src.status, src.approver_name, src.released_at, 'system', 'system'
FROM (
    SELECT 'CB-20260414-004' AS batch_code, 'REL-CB004' AS release_code, '已放行' AS status, 'QA经理' AS approver_name, '2026-04-14 09:20:00' AS released_at
    UNION ALL SELECT 'CB-20260414-005', 'REL-CB005', '待审批', '质量总监', '2026-04-14 10:20:00'
    UNION ALL SELECT 'CB-20260414-007', 'REL-CB007', '待放行', 'QA经理', '2026-04-14 11:45:00'
) src
INNER JOIN `iot_factory_compliance_batch_trace` b
    ON b.`batch_code` = src.batch_code AND b.`deleted` = b'0'
WHERE NOT EXISTS (
    SELECT 1 FROM `iot_factory_compliance_batch_release` r
    WHERE r.`batch_trace_id` = b.id AND r.`release_code` = src.release_code AND r.`deleted` = b'0'
);

INSERT INTO `iot_factory_compliance_batch_issue`
(`tenant_id`, `batch_trace_id`, `issue_title`, `status`, `owner_name`, `issue_desc`, `happened_at`, `creator`, `updater`)
SELECT 1, b.id, src.issue_title, src.status, src.owner_name, src.issue_desc, src.happened_at, 'system', 'system'
FROM (
    SELECT 'CB-20260414-005' AS batch_code, '放行附件待补录' AS issue_title, '待处理' AS status, 'QA-王蕾' AS owner_name, '中间产品检测单需补上传电子签章页' AS issue_desc, '2026-04-14 10:18:00' AS happened_at
    UNION ALL SELECT 'CB-20260414-006', '防晒剂含量复核偏差', '整改中', 'QC-孙悦', '防晒剂含量复核结果需重新复测并补充说明', '2026-04-14 10:30:00'
) src
INNER JOIN `iot_factory_compliance_batch_trace` b
    ON b.`batch_code` = src.batch_code AND b.`deleted` = b'0'
WHERE NOT EXISTS (
    SELECT 1 FROM `iot_factory_compliance_batch_issue` i
    WHERE i.`batch_trace_id` = b.id AND i.`issue_title` = src.issue_title AND i.`deleted` = b'0'
);
