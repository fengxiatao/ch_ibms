CREATE TABLE IF NOT EXISTS `iot_factory_environmental_point` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `tenant_id` BIGINT NOT NULL DEFAULT 1,
    `point_code` VARCHAR(64) NOT NULL,
    `point_name` VARCHAR(100) NOT NULL,
    `category` VARCHAR(32) NOT NULL,
    `location_name` VARCHAR(100) DEFAULT '',
    `unit` VARCHAR(32) DEFAULT '',
    `min_value` DECIMAL(12, 2) NOT NULL DEFAULT 0,
    `limit_value` DECIMAL(12, 2) NOT NULL DEFAULT 0,
    `display_max_value` DECIMAL(12, 2) NOT NULL DEFAULT 0,
    `display_unit_text` VARCHAR(64) DEFAULT '',
    `display_order` INT NOT NULL DEFAULT 0,
    `enabled` TINYINT NOT NULL DEFAULT 1,
    `remark` VARCHAR(255) DEFAULT '',
    `creator` VARCHAR(64) DEFAULT '',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updater` VARCHAR(64) DEFAULT '',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` BIT(1) NOT NULL DEFAULT b'0',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_factory_environmental_point_code` (`point_code`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='工厂环保监测点位表';

CREATE TABLE IF NOT EXISTS `iot_factory_environmental_reading` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `tenant_id` BIGINT NOT NULL DEFAULT 1,
    `point_id` BIGINT NOT NULL,
    `reading_value` DECIMAL(12, 2) NOT NULL DEFAULT 0,
    `status` VARCHAR(32) NOT NULL DEFAULT '正常',
    `exceed_flag` TINYINT NOT NULL DEFAULT 0,
    `recorded_at` DATETIME NOT NULL,
    `source_type` VARCHAR(32) DEFAULT 'MANUAL',
    `creator` VARCHAR(64) DEFAULT '',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updater` VARCHAR(64) DEFAULT '',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` BIT(1) NOT NULL DEFAULT b'0',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_factory_environmental_reading_slot` (`tenant_id`, `point_id`, `recorded_at`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='工厂环保监测读数表';

CREATE TABLE IF NOT EXISTS `iot_factory_environmental_alert` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `tenant_id` BIGINT NOT NULL DEFAULT 1,
    `point_id` BIGINT NOT NULL,
    `alert_title` VARCHAR(120) NOT NULL,
    `alert_level` VARCHAR(32) NOT NULL DEFAULT 'WARNING',
    `status` VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    `current_value` DECIMAL(12, 2) NOT NULL DEFAULT 0,
    `limit_value` DECIMAL(12, 2) NOT NULL DEFAULT 0,
    `happened_at` DATETIME NOT NULL,
    `resolved_at` DATETIME DEFAULT NULL,
    `description` VARCHAR(255) DEFAULT '',
    `creator` VARCHAR(64) DEFAULT '',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updater` VARCHAR(64) DEFAULT '',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` BIT(1) NOT NULL DEFAULT b'0',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_factory_environmental_alert_event` (`tenant_id`, `point_id`, `alert_title`, `happened_at`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='工厂环保监测预警表';

INSERT INTO `iot_factory_environmental_point`
(`tenant_id`, `point_code`, `point_name`, `category`, `location_name`, `unit`, `min_value`, `limit_value`, `display_max_value`, `display_unit_text`, `display_order`, `enabled`, `remark`, `creator`, `updater`)
SELECT 1, 'VOCS_CONCENTRATION', 'VOCs', 'AIR', '生产车间', 'mg/m³', 0, 20, 20, 'mg/m³', 10, 1, '顶部 KPI 与废气监测卡共用', 'system', 'system'
WHERE NOT EXISTS (SELECT 1 FROM `iot_factory_environmental_point` WHERE `point_code` = 'VOCS_CONCENTRATION' AND `deleted` = b'0');

INSERT INTO `iot_factory_environmental_point`
(`tenant_id`, `point_code`, `point_name`, `category`, `location_name`, `unit`, `min_value`, `limit_value`, `display_max_value`, `display_unit_text`, `display_order`, `enabled`, `remark`, `creator`, `updater`)
SELECT 1, 'WASTEWATER_COD', 'COD', 'WASTEWATER', '污水处理站', 'mg/L', 0, 100, 100, 'mg/L', 20, 1, '顶部 KPI 与废水监测卡共用', 'system', 'system'
WHERE NOT EXISTS (SELECT 1 FROM `iot_factory_environmental_point` WHERE `point_code` = 'WASTEWATER_COD' AND `deleted` = b'0');

INSERT INTO `iot_factory_environmental_point`
(`tenant_id`, `point_code`, `point_name`, `category`, `location_name`, `unit`, `min_value`, `limit_value`, `display_max_value`, `display_unit_text`, `display_order`, `enabled`, `remark`, `creator`, `updater`)
SELECT 1, 'NOISE_LEVEL', '噪声等级', 'NOISE', '厂界1号点', 'dB', 0, 65, 65, 'dB', 30, 1, '顶部 KPI 总览口径', 'system', 'system'
WHERE NOT EXISTS (SELECT 1 FROM `iot_factory_environmental_point` WHERE `point_code` = 'NOISE_LEVEL' AND `deleted` = b'0');

INSERT INTO `iot_factory_environmental_point`
(`tenant_id`, `point_code`, `point_name`, `category`, `location_name`, `unit`, `min_value`, `limit_value`, `display_max_value`, `display_unit_text`, `display_order`, `enabled`, `remark`, `creator`, `updater`)
SELECT 1, 'EXHAUST_FLOW', '排气流量', 'AIR', '排放总口', 'm³/h', 0, 120, 120, 'm³/h', 40, 1, '顶部 KPI 口径', 'system', 'system'
WHERE NOT EXISTS (SELECT 1 FROM `iot_factory_environmental_point` WHERE `point_code` = 'EXHAUST_FLOW' AND `deleted` = b'0');

INSERT INTO `iot_factory_environmental_point`
(`tenant_id`, `point_code`, `point_name`, `category`, `location_name`, `unit`, `min_value`, `limit_value`, `display_max_value`, `display_unit_text`, `display_order`, `enabled`, `remark`, `creator`, `updater`)
SELECT 1, 'AMMONIA_NITROGEN', '氨氮', 'WASTEWATER', '污水处理站', 'mg/L', 0, 15, 15, 'mg/L', 50, 1, '废水监测子项', 'system', 'system'
WHERE NOT EXISTS (SELECT 1 FROM `iot_factory_environmental_point` WHERE `point_code` = 'AMMONIA_NITROGEN' AND `deleted` = b'0');

INSERT INTO `iot_factory_environmental_point`
(`tenant_id`, `point_code`, `point_name`, `category`, `location_name`, `unit`, `min_value`, `limit_value`, `display_max_value`, `display_unit_text`, `display_order`, `enabled`, `remark`, `creator`, `updater`)
SELECT 1, 'PH', 'pH值', 'WASTEWATER', '污水处理站', '', 6, 9, 12, '6-9范围内', 60, 1, '废水监测子项', 'system', 'system'
WHERE NOT EXISTS (SELECT 1 FROM `iot_factory_environmental_point` WHERE `point_code` = 'PH' AND `deleted` = b'0');

INSERT INTO `iot_factory_environmental_point`
(`tenant_id`, `point_code`, `point_name`, `category`, `location_name`, `unit`, `min_value`, `limit_value`, `display_max_value`, `display_unit_text`, `display_order`, `enabled`, `remark`, `creator`, `updater`)
SELECT 1, 'WASTEWATER_FLOW', '流量', 'WASTEWATER', '污水处理站', 'm³/h', 0, 200, 200, 'm³/h', 70, 1, '废水监测子项', 'system', 'system'
WHERE NOT EXISTS (SELECT 1 FROM `iot_factory_environmental_point` WHERE `point_code` = 'WASTEWATER_FLOW' AND `deleted` = b'0');

INSERT INTO `iot_factory_environmental_point`
(`tenant_id`, `point_code`, `point_name`, `category`, `location_name`, `unit`, `min_value`, `limit_value`, `display_max_value`, `display_unit_text`, `display_order`, `enabled`, `remark`, `creator`, `updater`)
SELECT 1, 'NOISE_DAY', '昼间', 'NOISE', '厂界1号点', 'dB', 0, 65, 65, 'dB', 80, 1, '噪声监测昼间点位', 'system', 'system'
WHERE NOT EXISTS (SELECT 1 FROM `iot_factory_environmental_point` WHERE `point_code` = 'NOISE_DAY' AND `deleted` = b'0');

INSERT INTO `iot_factory_environmental_point`
(`tenant_id`, `point_code`, `point_name`, `category`, `location_name`, `unit`, `min_value`, `limit_value`, `display_max_value`, `display_unit_text`, `display_order`, `enabled`, `remark`, `creator`, `updater`)
SELECT 1, 'NOISE_NIGHT', '夜间', 'NOISE', '厂界1号点', 'dB', 0, 65, 65, 'dB', 90, 1, '噪声监测夜间点位', 'system', 'system'
WHERE NOT EXISTS (SELECT 1 FROM `iot_factory_environmental_point` WHERE `point_code` = 'NOISE_NIGHT' AND `deleted` = b'0');

INSERT INTO `iot_factory_environmental_point`
(`tenant_id`, `point_code`, `point_name`, `category`, `location_name`, `unit`, `min_value`, `limit_value`, `display_max_value`, `display_unit_text`, `display_order`, `enabled`, `remark`, `creator`, `updater`)
SELECT 1, 'NOISE_LIMIT', '限值', 'NOISE', '厂界1号点', 'dB', 0, 65, 65, 'dB', 100, 1, '噪声监测限值项', 'system', 'system'
WHERE NOT EXISTS (SELECT 1 FROM `iot_factory_environmental_point` WHERE `point_code` = 'NOISE_LIMIT' AND `deleted` = b'0');

INSERT INTO `iot_factory_environmental_reading`
(`tenant_id`, `point_id`, `reading_value`, `status`, `exceed_flag`, `recorded_at`, `source_type`, `creator`, `updater`)
SELECT 1, p.id, 12.50, '正常', 0, '2026-04-14 10:30:00', 'SENSOR', 'system', 'system'
FROM `iot_factory_environmental_point` p
WHERE p.point_code = 'VOCS_CONCENTRATION' AND p.deleted = b'0'
  AND NOT EXISTS (
      SELECT 1 FROM `iot_factory_environmental_reading`
      WHERE `tenant_id` = 1 AND `point_id` = p.id AND `recorded_at` = '2026-04-14 10:30:00' AND `deleted` = b'0'
  );

INSERT INTO `iot_factory_environmental_reading`
(`tenant_id`, `point_id`, `reading_value`, `status`, `exceed_flag`, `recorded_at`, `source_type`, `creator`, `updater`)
SELECT 1, p.id, 45.00, '正常', 0, '2026-04-14 10:30:00', 'SENSOR', 'system', 'system'
FROM `iot_factory_environmental_point` p
WHERE p.point_code = 'WASTEWATER_COD' AND p.deleted = b'0'
  AND NOT EXISTS (
      SELECT 1 FROM `iot_factory_environmental_reading`
      WHERE `tenant_id` = 1 AND `point_id` = p.id AND `recorded_at` = '2026-04-14 10:30:00' AND `deleted` = b'0'
  );

INSERT INTO `iot_factory_environmental_reading`
(`tenant_id`, `point_id`, `reading_value`, `status`, `exceed_flag`, `recorded_at`, `source_type`, `creator`, `updater`)
SELECT 1, p.id, 58.00, '正常', 0, '2026-04-14 10:30:00', 'SENSOR', 'system', 'system'
FROM `iot_factory_environmental_point` p
WHERE p.point_code = 'NOISE_LEVEL' AND p.deleted = b'0'
  AND NOT EXISTS (
      SELECT 1 FROM `iot_factory_environmental_reading`
      WHERE `tenant_id` = 1 AND `point_id` = p.id AND `recorded_at` = '2026-04-14 10:30:00' AND `deleted` = b'0'
  );

INSERT INTO `iot_factory_environmental_reading`
(`tenant_id`, `point_id`, `reading_value`, `status`, `exceed_flag`, `recorded_at`, `source_type`, `creator`, `updater`)
SELECT 1, p.id, 85.00, '正常', 0, '2026-04-14 10:30:00', 'SENSOR', 'system', 'system'
FROM `iot_factory_environmental_point` p
WHERE p.point_code = 'EXHAUST_FLOW' AND p.deleted = b'0'
  AND NOT EXISTS (
      SELECT 1 FROM `iot_factory_environmental_reading`
      WHERE `tenant_id` = 1 AND `point_id` = p.id AND `recorded_at` = '2026-04-14 10:30:00' AND `deleted` = b'0'
  );

INSERT INTO `iot_factory_environmental_reading`
(`tenant_id`, `point_id`, `reading_value`, `status`, `exceed_flag`, `recorded_at`, `source_type`, `creator`, `updater`)
SELECT 1, p.id, 8.50, '正常', 0, '2026-04-14 10:30:00', 'SENSOR', 'system', 'system'
FROM `iot_factory_environmental_point` p
WHERE p.point_code = 'AMMONIA_NITROGEN' AND p.deleted = b'0'
  AND NOT EXISTS (
      SELECT 1 FROM `iot_factory_environmental_reading`
      WHERE `tenant_id` = 1 AND `point_id` = p.id AND `recorded_at` = '2026-04-14 10:30:00' AND `deleted` = b'0'
  );

INSERT INTO `iot_factory_environmental_reading`
(`tenant_id`, `point_id`, `reading_value`, `status`, `exceed_flag`, `recorded_at`, `source_type`, `creator`, `updater`)
SELECT 1, p.id, 7.20, '正常', 0, '2026-04-14 10:30:00', 'SENSOR', 'system', 'system'
FROM `iot_factory_environmental_point` p
WHERE p.point_code = 'PH' AND p.deleted = b'0'
  AND NOT EXISTS (
      SELECT 1 FROM `iot_factory_environmental_reading`
      WHERE `tenant_id` = 1 AND `point_id` = p.id AND `recorded_at` = '2026-04-14 10:30:00' AND `deleted` = b'0'
  );

INSERT INTO `iot_factory_environmental_reading`
(`tenant_id`, `point_id`, `reading_value`, `status`, `exceed_flag`, `recorded_at`, `source_type`, `creator`, `updater`)
SELECT 1, p.id, 120.00, '正常', 0, '2026-04-14 10:30:00', 'SENSOR', 'system', 'system'
FROM `iot_factory_environmental_point` p
WHERE p.point_code = 'WASTEWATER_FLOW' AND p.deleted = b'0'
  AND NOT EXISTS (
      SELECT 1 FROM `iot_factory_environmental_reading`
      WHERE `tenant_id` = 1 AND `point_id` = p.id AND `recorded_at` = '2026-04-14 10:30:00' AND `deleted` = b'0'
  );

INSERT INTO `iot_factory_environmental_reading`
(`tenant_id`, `point_id`, `reading_value`, `status`, `exceed_flag`, `recorded_at`, `source_type`, `creator`, `updater`)
SELECT 1, p.id, 58.00, '正常', 0, '2026-04-14 10:30:00', 'SENSOR', 'system', 'system'
FROM `iot_factory_environmental_point` p
WHERE p.point_code = 'NOISE_DAY' AND p.deleted = b'0'
  AND NOT EXISTS (
      SELECT 1 FROM `iot_factory_environmental_reading`
      WHERE `tenant_id` = 1 AND `point_id` = p.id AND `recorded_at` = '2026-04-14 10:30:00' AND `deleted` = b'0'
  );

INSERT INTO `iot_factory_environmental_reading`
(`tenant_id`, `point_id`, `reading_value`, `status`, `exceed_flag`, `recorded_at`, `source_type`, `creator`, `updater`)
SELECT 1, p.id, 52.00, '注意', 0, '2026-04-14 10:30:00', 'SENSOR', 'system', 'system'
FROM `iot_factory_environmental_point` p
WHERE p.point_code = 'NOISE_NIGHT' AND p.deleted = b'0'
  AND NOT EXISTS (
      SELECT 1 FROM `iot_factory_environmental_reading`
      WHERE `tenant_id` = 1 AND `point_id` = p.id AND `recorded_at` = '2026-04-14 10:30:00' AND `deleted` = b'0'
  );

INSERT INTO `iot_factory_environmental_reading`
(`tenant_id`, `point_id`, `reading_value`, `status`, `exceed_flag`, `recorded_at`, `source_type`, `creator`, `updater`)
SELECT 1, p.id, 65.00, '正常', 0, '2026-04-14 10:30:00', 'SYSTEM', 'system', 'system'
FROM `iot_factory_environmental_point` p
WHERE p.point_code = 'NOISE_LIMIT' AND p.deleted = b'0'
  AND NOT EXISTS (
      SELECT 1 FROM `iot_factory_environmental_reading`
      WHERE `tenant_id` = 1 AND `point_id` = p.id AND `recorded_at` = '2026-04-14 10:30:00' AND `deleted` = b'0'
  );

INSERT INTO `iot_factory_environmental_alert`
(`tenant_id`, `point_id`, `alert_title`, `alert_level`, `status`, `current_value`, `limit_value`, `happened_at`, `resolved_at`, `description`, `creator`, `updater`)
SELECT 1, p.id, 'VOCs - 生产车间', 'WARNING', 'ACTIVE', 18.50, 20.00, '2026-04-14 10:30:00', NULL, '当前值: 18.5 mg/m³ | 限值: 20', 'system', 'system'
FROM `iot_factory_environmental_point` p
WHERE p.point_code = 'VOCS_CONCENTRATION' AND p.deleted = b'0'
  AND NOT EXISTS (
      SELECT 1 FROM `iot_factory_environmental_alert`
      WHERE `tenant_id` = 1 AND `point_id` = p.id AND `alert_title` = 'VOCs - 生产车间' AND `happened_at` = '2026-04-14 10:30:00' AND `deleted` = b'0'
  );

INSERT INTO `iot_factory_environmental_alert`
(`tenant_id`, `point_id`, `alert_title`, `alert_level`, `status`, `current_value`, `limit_value`, `happened_at`, `resolved_at`, `description`, `creator`, `updater`)
SELECT 1, p.id, '废水COD - 污水处理站', 'INFO', 'RECOVERED', 85.00, 100.00, '2026-04-14 09:00:00', '2026-04-14 09:30:00', '当前值: 85 mg/L | 限值: 100', 'system', 'system'
FROM `iot_factory_environmental_point` p
WHERE p.point_code = 'WASTEWATER_COD' AND p.deleted = b'0'
  AND NOT EXISTS (
      SELECT 1 FROM `iot_factory_environmental_alert`
      WHERE `tenant_id` = 1 AND `point_id` = p.id AND `alert_title` = '废水COD - 污水处理站' AND `happened_at` = '2026-04-14 09:00:00' AND `deleted` = b'0'
  );
