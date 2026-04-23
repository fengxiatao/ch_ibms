CREATE TABLE IF NOT EXISTS `iot_cloud_defense_area` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `tenant_id` BIGINT NOT NULL DEFAULT 0,
    `area_code` VARCHAR(64) NOT NULL,
    `area_name` VARCHAR(100) NOT NULL,
    `area_type` VARCHAR(32) DEFAULT NULL,
    `space_id` BIGINT DEFAULT NULL,
    `layout_x` DECIMAL(6, 2) NOT NULL DEFAULT 0,
    `layout_y` DECIMAL(6, 2) NOT NULL DEFAULT 0,
    `layout_width` DECIMAL(6, 2) NOT NULL DEFAULT 0,
    `layout_height` DECIMAL(6, 2) NOT NULL DEFAULT 0,
    `detail_text` VARCHAR(255) DEFAULT NULL,
    `sort` INT NOT NULL DEFAULT 0,
    `enabled` TINYINT NOT NULL DEFAULT 1,
    `creator` VARCHAR(64) DEFAULT '',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updater` VARCHAR(64) DEFAULT '',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` BIT(1) NOT NULL DEFAULT b'0',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_cloud_defense_area_code` (`area_code`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='立体化云防区域表';

CREATE TABLE IF NOT EXISTS `iot_cloud_defense_point` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `tenant_id` BIGINT NOT NULL DEFAULT 0,
    `area_id` BIGINT NOT NULL,
    `device_id` BIGINT DEFAULT NULL,
    `channel_id` BIGINT DEFAULT NULL,
    `point_code` VARCHAR(64) NOT NULL,
    `point_name` VARCHAR(100) NOT NULL,
    `point_type` VARCHAR(32) DEFAULT NULL,
    `layout_x` DECIMAL(6, 2) NOT NULL DEFAULT 0,
    `layout_y` DECIMAL(6, 2) NOT NULL DEFAULT 0,
    `armed_status` TINYINT NOT NULL DEFAULT 0,
    `alarm_status` TINYINT NOT NULL DEFAULT 0,
    `online_status` TINYINT NOT NULL DEFAULT 0,
    `sort` INT NOT NULL DEFAULT 0,
    `enabled` TINYINT NOT NULL DEFAULT 1,
    `creator` VARCHAR(64) DEFAULT '',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updater` VARCHAR(64) DEFAULT '',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` BIT(1) NOT NULL DEFAULT b'0',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_cloud_defense_point_code` (`point_code`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='立体化云防点位表';

CREATE TABLE IF NOT EXISTS `iot_cloud_defense_mode` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `tenant_id` BIGINT NOT NULL DEFAULT 0,
    `mode_code` VARCHAR(64) NOT NULL,
    `mode_name` VARCHAR(100) NOT NULL,
    `icon` VARCHAR(64) DEFAULT NULL,
    `status_text` VARCHAR(255) DEFAULT NULL,
    `sort` INT NOT NULL DEFAULT 0,
    `enabled` TINYINT NOT NULL DEFAULT 1,
    `creator` VARCHAR(64) DEFAULT '',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updater` VARCHAR(64) DEFAULT '',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` BIT(1) NOT NULL DEFAULT b'0',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_cloud_defense_mode_code` (`mode_code`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='立体化云防模式表';

CREATE TABLE IF NOT EXISTS `iot_cloud_defense_area_device_rel` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `tenant_id` BIGINT NOT NULL DEFAULT 0,
    `area_id` BIGINT NOT NULL,
    `device_id` BIGINT DEFAULT NULL,
    `channel_id` BIGINT DEFAULT NULL,
    `sort` INT NOT NULL DEFAULT 0,
    `creator` VARCHAR(64) DEFAULT '',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updater` VARCHAR(64) DEFAULT '',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` BIT(1) NOT NULL DEFAULT b'0',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_cloud_defense_area_device` (`area_id`, `device_id`, `channel_id`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='立体化云防区域设备关系表';

CREATE TABLE IF NOT EXISTS `iot_cloud_defense_score_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `tenant_id` BIGINT NOT NULL DEFAULT 0,
    `score` INT NOT NULL DEFAULT 0,
    `score_level` VARCHAR(32) DEFAULT NULL,
    `score_time` DATETIME NOT NULL,
    `remark` VARCHAR(255) DEFAULT NULL,
    `creator` VARCHAR(64) DEFAULT '',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updater` VARCHAR(64) DEFAULT '',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` BIT(1) NOT NULL DEFAULT b'0',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='立体化云防评分日志表';

INSERT INTO `iot_cloud_defense_mode`
(`tenant_id`, `mode_code`, `mode_name`, `icon`, `status_text`, `sort`, `enabled`, `creator`, `updater`)
SELECT 1, 'perimeter-defense', '周界防护', 'ep:position', '当前模式已接入真实布防与设备联动', 10, 1, 'system', 'system'
WHERE NOT EXISTS (
    SELECT 1 FROM `iot_cloud_defense_mode` WHERE `mode_code` = 'perimeter-defense' AND `deleted` = b'0'
);

INSERT INTO `iot_cloud_defense_mode`
(`tenant_id`, `mode_code`, `mode_name`, `icon`, `status_text`, `sort`, `enabled`, `creator`, `updater`)
SELECT 1, 'area-intrusion', '区域入侵', 'ep:lock', '已保留真实业务承接位，待后端能力进一步接入', 20, 1, 'system', 'system'
WHERE NOT EXISTS (
    SELECT 1 FROM `iot_cloud_defense_mode` WHERE `mode_code` = 'area-intrusion' AND `deleted` = b'0'
);

INSERT INTO `iot_cloud_defense_mode`
(`tenant_id`, `mode_code`, `mode_name`, `icon`, `status_text`, `sort`, `enabled`, `creator`, `updater`)
SELECT 1, 'behavior-analysis', '行为分析', 'ep:data-analysis', '已保留真实业务承接位，待后端能力进一步接入', 30, 1, 'system', 'system'
WHERE NOT EXISTS (
    SELECT 1 FROM `iot_cloud_defense_mode` WHERE `mode_code` = 'behavior-analysis' AND `deleted` = b'0'
);

INSERT INTO `iot_cloud_defense_mode`
(`tenant_id`, `mode_code`, `mode_name`, `icon`, `status_text`, `sort`, `enabled`, `creator`, `updater`)
SELECT 1, 'track-tracing', '轨迹追踪', 'ep:guide', '已保留真实业务承接位，待后端能力进一步接入', 40, 1, 'system', 'system'
WHERE NOT EXISTS (
    SELECT 1 FROM `iot_cloud_defense_mode` WHERE `mode_code` = 'track-tracing' AND `deleted` = b'0'
);

INSERT INTO `iot_cloud_defense_mode`
(`tenant_id`, `mode_code`, `mode_name`, `icon`, `status_text`, `sort`, `enabled`, `creator`, `updater`)
SELECT 1, 'smart-patrol', '智能巡检', 'ep:timer', '已保留真实业务承接位，待后端能力进一步接入', 50, 1, 'system', 'system'
WHERE NOT EXISTS (
    SELECT 1 FROM `iot_cloud_defense_mode` WHERE `mode_code` = 'smart-patrol' AND `deleted` = b'0'
);

INSERT INTO `iot_cloud_defense_mode`
(`tenant_id`, `mode_code`, `mode_name`, `icon`, `status_text`, `sort`, `enabled`, `creator`, `updater`)
SELECT 1, 'safety-posture', '安全态势', 'ep:trend-charts', '已保留真实业务承接位，待后端能力进一步接入', 60, 1, 'system', 'system'
WHERE NOT EXISTS (
    SELECT 1 FROM `iot_cloud_defense_mode` WHERE `mode_code` = 'safety-posture' AND `deleted` = b'0'
);

INSERT INTO `ibms_device`
(`tenant_id`, `device_code`, `name`, `system_code`, `device_type_code`, `product_model`, `brand`, `point_count`, `points_online`, `points_alarm`, `space`, `creator`, `updater`)
SELECT 1, 'CF-VI-CAM-001', '高清枪机-01', 'VI', 'CAM', 'CF-CAM-4K', 'HIK', 1, 1, 0, '周界防护区', 'system', 'system'
WHERE NOT EXISTS (SELECT 1 FROM `ibms_device` WHERE `device_code` = 'CF-VI-CAM-001' AND `deleted` = b'0');

INSERT INTO `ibms_device`
(`tenant_id`, `device_code`, `name`, `system_code`, `device_type_code`, `product_model`, `brand`, `point_count`, `points_online`, `points_alarm`, `space`, `creator`, `updater`)
SELECT 1, 'CF-VI-CAM-002', '高清球机-02', 'VI', 'CAM', 'CF-PTZ-2K', 'DAH', 1, 1, 0, '仓储防护区', 'system', 'system'
WHERE NOT EXISTS (SELECT 1 FROM `ibms_device` WHERE `device_code` = 'CF-VI-CAM-002' AND `deleted` = b'0');

INSERT INTO `ibms_device`
(`tenant_id`, `device_code`, `name`, `system_code`, `device_type_code`, `product_model`, `brand`, `point_count`, `points_online`, `points_alarm`, `space`, `creator`, `updater`)
SELECT 1, 'CF-VI-CAM-003', '全景鹰眼', 'VI', 'CAM', 'CF-EAGLE-360', 'HIK', 1, 1, 0, '生产防护区', 'system', 'system'
WHERE NOT EXISTS (SELECT 1 FROM `ibms_device` WHERE `device_code` = 'CF-VI-CAM-003' AND `deleted` = b'0');

INSERT INTO `ibms_device`
(`tenant_id`, `device_code`, `name`, `system_code`, `device_type_code`, `product_model`, `brand`, `point_count`, `points_online`, `points_alarm`, `space`, `creator`, `updater`)
SELECT 1, 'CF-VI-CAM-004', '热成像相机', 'VI', 'CAM', 'CF-THERMAL-01', 'DAH', 1, 1, 1, '办公防护区', 'system', 'system'
WHERE NOT EXISTS (SELECT 1 FROM `ibms_device` WHERE `device_code` = 'CF-VI-CAM-004' AND `deleted` = b'0');

INSERT INTO `ibms_device`
(`tenant_id`, `device_code`, `name`, `system_code`, `device_type_code`, `product_model`, `brand`, `point_count`, `points_online`, `points_alarm`, `space`, `creator`, `updater`)
SELECT 1, 'CF-AC-CAM-005', '人脸识别相机', 'AC', 'CAM', 'CF-FACE-01', 'ZKT', 1, 1, 0, '重点区域A', 'system', 'system'
WHERE NOT EXISTS (SELECT 1 FROM `ibms_device` WHERE `device_code` = 'CF-AC-CAM-005' AND `deleted` = b'0');

INSERT INTO `ibms_device`
(`tenant_id`, `device_code`, `name`, `system_code`, `device_type_code`, `product_model`, `brand`, `point_count`, `points_online`, `points_alarm`, `space`, `creator`, `updater`)
SELECT 1, 'CF-AC-CAM-006', '车牌识别相机', 'AC', 'CAM', 'CF-PLATE-01', 'ZKT', 1, 1, 0, '周界防护区', 'system', 'system'
WHERE NOT EXISTS (SELECT 1 FROM `ibms_device` WHERE `device_code` = 'CF-AC-CAM-006' AND `deleted` = b'0');

INSERT INTO `ibms_channel`
(`tenant_id`, `space_id`, `device_id`, `code`, `channel_no`, `name`, `business`, `type_code`, `system_type`, `space`, `status`, `creator`, `updater`)
SELECT 1, 1003, d.id, 'CF-CH-001', 1, '高清枪机-01视频通道', 'security', 'VT', 'VI', '周界防护区', 'online', 'system', 'system'
FROM `ibms_device` d
WHERE d.device_code = 'CF-VI-CAM-001' AND d.deleted = b'0'
  AND NOT EXISTS (SELECT 1 FROM `ibms_channel` WHERE `code` = 'CF-CH-001' AND `deleted` = b'0');

INSERT INTO `ibms_channel`
(`tenant_id`, `space_id`, `device_id`, `code`, `channel_no`, `name`, `business`, `type_code`, `system_type`, `space`, `status`, `creator`, `updater`)
SELECT 1, 2002, d.id, 'CF-CH-002', 1, '高清球机-02视频通道', 'security', 'VT', 'VI', '仓储防护区', 'online', 'system', 'system'
FROM `ibms_device` d
WHERE d.device_code = 'CF-VI-CAM-002' AND d.deleted = b'0'
  AND NOT EXISTS (SELECT 1 FROM `ibms_channel` WHERE `code` = 'CF-CH-002' AND `deleted` = b'0');

INSERT INTO `ibms_channel`
(`tenant_id`, `space_id`, `device_id`, `code`, `channel_no`, `name`, `business`, `type_code`, `system_type`, `space`, `status`, `creator`, `updater`)
SELECT 1, 1002, d.id, 'CF-CH-003', 1, '全景鹰眼视频通道', 'security', 'VT', 'VI', '生产防护区', 'online', 'system', 'system'
FROM `ibms_device` d
WHERE d.device_code = 'CF-VI-CAM-003' AND d.deleted = b'0'
  AND NOT EXISTS (SELECT 1 FROM `ibms_channel` WHERE `code` = 'CF-CH-003' AND `deleted` = b'0');

INSERT INTO `ibms_channel`
(`tenant_id`, `space_id`, `device_id`, `code`, `channel_no`, `name`, `business`, `type_code`, `system_type`, `space`, `status`, `creator`, `updater`)
SELECT 1, 2003, d.id, 'CF-CH-004', 1, '热成像相机视频通道', 'alarm', 'VT', 'VI', '办公防护区', 'warning', 'system', 'system'
FROM `ibms_device` d
WHERE d.device_code = 'CF-VI-CAM-004' AND d.deleted = b'0'
  AND NOT EXISTS (SELECT 1 FROM `ibms_channel` WHERE `code` = 'CF-CH-004' AND `deleted` = b'0');

INSERT INTO `ibms_channel`
(`tenant_id`, `space_id`, `device_id`, `code`, `channel_no`, `name`, `business`, `type_code`, `system_type`, `space`, `status`, `creator`, `updater`)
SELECT 1, 1002, d.id, 'CF-CH-005', 1, '人脸识别相机通道', 'access', 'FR', 'AC', '重点区域A', 'online', 'system', 'system'
FROM `ibms_device` d
WHERE d.device_code = 'CF-AC-CAM-005' AND d.deleted = b'0'
  AND NOT EXISTS (SELECT 1 FROM `ibms_channel` WHERE `code` = 'CF-CH-005' AND `deleted` = b'0');

INSERT INTO `ibms_channel`
(`tenant_id`, `space_id`, `device_id`, `code`, `channel_no`, `name`, `business`, `type_code`, `system_type`, `space`, `status`, `creator`, `updater`)
SELECT 1, 1003, d.id, 'CF-CH-006', 1, '车牌识别相机通道', 'access', 'LPR', 'AC', '周界防护区', 'online', 'system', 'system'
FROM `ibms_device` d
WHERE d.device_code = 'CF-AC-CAM-006' AND d.deleted = b'0'
  AND NOT EXISTS (SELECT 1 FROM `ibms_channel` WHERE `code` = 'CF-CH-006' AND `deleted` = b'0');

INSERT INTO `iot_cloud_defense_area`
(`tenant_id`, `area_code`, `area_name`, `area_type`, `space_id`, `layout_x`, `layout_y`, `layout_width`, `layout_height`, `detail_text`, `sort`, `enabled`, `creator`, `updater`)
SELECT 1, 'CF-AREA-001', '周界防护区', 'perimeter', 1003, 4.00, 12.00, 92.00, 66.00, '4设备 / 外围环线', 10, 1, 'system', 'system'
WHERE NOT EXISTS (SELECT 1 FROM `iot_cloud_defense_area` WHERE `area_code` = 'CF-AREA-001' AND `deleted` = b'0');

INSERT INTO `iot_cloud_defense_area`
(`tenant_id`, `area_code`, `area_name`, `area_type`, `space_id`, `layout_x`, `layout_y`, `layout_width`, `layout_height`, `detail_text`, `sort`, `enabled`, `creator`, `updater`)
SELECT 1, 'CF-AREA-002', '仓储防护区', 'storage', 2002, 12.00, 40.00, 22.00, 36.00, '2设备 / 仓储通道', 20, 1, 'system', 'system'
WHERE NOT EXISTS (SELECT 1 FROM `iot_cloud_defense_area` WHERE `area_code` = 'CF-AREA-002' AND `deleted` = b'0');

INSERT INTO `iot_cloud_defense_area`
(`tenant_id`, `area_code`, `area_name`, `area_type`, `space_id`, `layout_x`, `layout_y`, `layout_width`, `layout_height`, `detail_text`, `sort`, `enabled`, `creator`, `updater`)
SELECT 1, 'CF-AREA-003', '生产防护区', 'production', 1002, 42.00, 40.00, 38.00, 36.00, '3设备 / 生产核心区', 30, 1, 'system', 'system'
WHERE NOT EXISTS (SELECT 1 FROM `iot_cloud_defense_area` WHERE `area_code` = 'CF-AREA-003' AND `deleted` = b'0');

INSERT INTO `iot_cloud_defense_area`
(`tenant_id`, `area_code`, `area_name`, `area_type`, `space_id`, `layout_x`, `layout_y`, `layout_width`, `layout_height`, `detail_text`, `sort`, `enabled`, `creator`, `updater`)
SELECT 1, 'CF-AREA-004', '办公防护区', 'office', 2003, 84.00, 40.00, 12.00, 30.00, '2设备 / 办公入口', 40, 1, 'system', 'system'
WHERE NOT EXISTS (SELECT 1 FROM `iot_cloud_defense_area` WHERE `area_code` = 'CF-AREA-004' AND `deleted` = b'0');

INSERT INTO `iot_cloud_defense_area`
(`tenant_id`, `area_code`, `area_name`, `area_type`, `space_id`, `layout_x`, `layout_y`, `layout_width`, `layout_height`, `detail_text`, `sort`, `enabled`, `creator`, `updater`)
SELECT 1, 'CF-AREA-005', '重点区域A', 'focus', 1002, 68.00, 76.00, 18.00, 12.00, '2设备 / 重点联防', 50, 1, 'system', 'system'
WHERE NOT EXISTS (SELECT 1 FROM `iot_cloud_defense_area` WHERE `area_code` = 'CF-AREA-005' AND `deleted` = b'0');

INSERT INTO `iot_cloud_defense_area_device_rel`
(`tenant_id`, `area_id`, `device_id`, `channel_id`, `sort`, `creator`, `updater`)
SELECT 1, a.id, d.id, c.id, 10, 'system', 'system'
FROM `iot_cloud_defense_area` a
JOIN `ibms_device` d ON d.device_code = 'CF-VI-CAM-001' AND d.deleted = b'0'
JOIN `ibms_channel` c ON c.code = 'CF-CH-001' AND c.deleted = b'0'
WHERE a.area_code = 'CF-AREA-001' AND a.deleted = b'0'
  AND NOT EXISTS (SELECT 1 FROM `iot_cloud_defense_area_device_rel` WHERE `area_id` = a.id AND `device_id` = d.id AND `deleted` = b'0');

INSERT INTO `iot_cloud_defense_area_device_rel`
(`tenant_id`, `area_id`, `device_id`, `channel_id`, `sort`, `creator`, `updater`)
SELECT 1, a.id, d.id, c.id, 20, 'system', 'system'
FROM `iot_cloud_defense_area` a
JOIN `ibms_device` d ON d.device_code = 'CF-AC-CAM-006' AND d.deleted = b'0'
JOIN `ibms_channel` c ON c.code = 'CF-CH-006' AND c.deleted = b'0'
WHERE a.area_code = 'CF-AREA-001' AND a.deleted = b'0'
  AND NOT EXISTS (SELECT 1 FROM `iot_cloud_defense_area_device_rel` WHERE `area_id` = a.id AND `device_id` = d.id AND `deleted` = b'0');

INSERT INTO `iot_cloud_defense_area_device_rel`
(`tenant_id`, `area_id`, `device_id`, `channel_id`, `sort`, `creator`, `updater`)
SELECT 1, a.id, d.id, c.id, 10, 'system', 'system'
FROM `iot_cloud_defense_area` a
JOIN `ibms_device` d ON d.device_code = 'CF-VI-CAM-002' AND d.deleted = b'0'
JOIN `ibms_channel` c ON c.code = 'CF-CH-002' AND c.deleted = b'0'
WHERE a.area_code = 'CF-AREA-002' AND a.deleted = b'0'
  AND NOT EXISTS (SELECT 1 FROM `iot_cloud_defense_area_device_rel` WHERE `area_id` = a.id AND `device_id` = d.id AND `deleted` = b'0');

INSERT INTO `iot_cloud_defense_area_device_rel`
(`tenant_id`, `area_id`, `device_id`, `channel_id`, `sort`, `creator`, `updater`)
SELECT 1, a.id, d.id, c.id, 10, 'system', 'system'
FROM `iot_cloud_defense_area` a
JOIN `ibms_device` d ON d.device_code = 'CF-VI-CAM-003' AND d.deleted = b'0'
JOIN `ibms_channel` c ON c.code = 'CF-CH-003' AND c.deleted = b'0'
WHERE a.area_code = 'CF-AREA-003' AND a.deleted = b'0'
  AND NOT EXISTS (SELECT 1 FROM `iot_cloud_defense_area_device_rel` WHERE `area_id` = a.id AND `device_id` = d.id AND `deleted` = b'0');

INSERT INTO `iot_cloud_defense_area_device_rel`
(`tenant_id`, `area_id`, `device_id`, `channel_id`, `sort`, `creator`, `updater`)
SELECT 1, a.id, d.id, c.id, 10, 'system', 'system'
FROM `iot_cloud_defense_area` a
JOIN `ibms_device` d ON d.device_code = 'CF-VI-CAM-004' AND d.deleted = b'0'
JOIN `ibms_channel` c ON c.code = 'CF-CH-004' AND c.deleted = b'0'
WHERE a.area_code = 'CF-AREA-004' AND a.deleted = b'0'
  AND NOT EXISTS (SELECT 1 FROM `iot_cloud_defense_area_device_rel` WHERE `area_id` = a.id AND `device_id` = d.id AND `deleted` = b'0');

INSERT INTO `iot_cloud_defense_area_device_rel`
(`tenant_id`, `area_id`, `device_id`, `channel_id`, `sort`, `creator`, `updater`)
SELECT 1, a.id, d.id, c.id, 10, 'system', 'system'
FROM `iot_cloud_defense_area` a
JOIN `ibms_device` d ON d.device_code = 'CF-AC-CAM-005' AND d.deleted = b'0'
JOIN `ibms_channel` c ON c.code = 'CF-CH-005' AND c.deleted = b'0'
WHERE a.area_code = 'CF-AREA-005' AND a.deleted = b'0'
  AND NOT EXISTS (SELECT 1 FROM `iot_cloud_defense_area_device_rel` WHERE `area_id` = a.id AND `device_id` = d.id AND `deleted` = b'0');

INSERT INTO `iot_cloud_defense_point`
(`tenant_id`, `area_id`, `device_id`, `channel_id`, `point_code`, `point_name`, `point_type`, `layout_x`, `layout_y`, `armed_status`, `alarm_status`, `online_status`, `sort`, `enabled`, `creator`, `updater`)
SELECT 1, a.id, d.id, c.id, 'CF-POINT-001', '001', 'sensor', 6.00, 54.00, 1, 0, 1, 10, 1, 'system', 'system'
FROM `iot_cloud_defense_area` a
JOIN `ibms_device` d ON d.device_code = 'CF-VI-CAM-001' AND d.deleted = b'0'
JOIN `ibms_channel` c ON c.code = 'CF-CH-001' AND c.deleted = b'0'
WHERE a.area_code = 'CF-AREA-001' AND a.deleted = b'0'
  AND NOT EXISTS (SELECT 1 FROM `iot_cloud_defense_point` WHERE `point_code` = 'CF-POINT-001' AND `deleted` = b'0');

INSERT INTO `iot_cloud_defense_point`
(`tenant_id`, `area_id`, `device_id`, `channel_id`, `point_code`, `point_name`, `point_type`, `layout_x`, `layout_y`, `armed_status`, `alarm_status`, `online_status`, `sort`, `enabled`, `creator`, `updater`)
SELECT 1, a.id, d.id, c.id, 'CF-POINT-002', '004', 'sensor', 25.00, 20.00, 1, 0, 1, 20, 1, 'system', 'system'
FROM `iot_cloud_defense_area` a
JOIN `ibms_device` d ON d.device_code = 'CF-VI-CAM-002' AND d.deleted = b'0'
JOIN `ibms_channel` c ON c.code = 'CF-CH-002' AND c.deleted = b'0'
WHERE a.area_code = 'CF-AREA-001' AND a.deleted = b'0'
  AND NOT EXISTS (SELECT 1 FROM `iot_cloud_defense_point` WHERE `point_code` = 'CF-POINT-002' AND `deleted` = b'0');

INSERT INTO `iot_cloud_defense_point`
(`tenant_id`, `area_id`, `device_id`, `channel_id`, `point_code`, `point_name`, `point_type`, `layout_x`, `layout_y`, `armed_status`, `alarm_status`, `online_status`, `sort`, `enabled`, `creator`, `updater`)
SELECT 1, a.id, d.id, c.id, 'CF-POINT-003', '005', 'sensor', 50.00, 20.00, 1, 0, 1, 30, 1, 'system', 'system'
FROM `iot_cloud_defense_area` a
JOIN `ibms_device` d ON d.device_code = 'CF-VI-CAM-003' AND d.deleted = b'0'
JOIN `ibms_channel` c ON c.code = 'CF-CH-003' AND c.deleted = b'0'
WHERE a.area_code = 'CF-AREA-001' AND a.deleted = b'0'
  AND NOT EXISTS (SELECT 1 FROM `iot_cloud_defense_point` WHERE `point_code` = 'CF-POINT-003' AND `deleted` = b'0');

INSERT INTO `iot_cloud_defense_point`
(`tenant_id`, `area_id`, `device_id`, `channel_id`, `point_code`, `point_name`, `point_type`, `layout_x`, `layout_y`, `armed_status`, `alarm_status`, `online_status`, `sort`, `enabled`, `creator`, `updater`)
SELECT 1, a.id, d.id, c.id, 'CF-POINT-004', '006', 'sensor', 75.00, 20.00, 1, 0, 1, 40, 1, 'system', 'system'
FROM `iot_cloud_defense_area` a
JOIN `ibms_device` d ON d.device_code = 'CF-VI-CAM-004' AND d.deleted = b'0'
JOIN `ibms_channel` c ON c.code = 'CF-CH-004' AND c.deleted = b'0'
WHERE a.area_code = 'CF-AREA-001' AND a.deleted = b'0'
  AND NOT EXISTS (SELECT 1 FROM `iot_cloud_defense_point` WHERE `point_code` = 'CF-POINT-004' AND `deleted` = b'0');

INSERT INTO `iot_cloud_defense_point`
(`tenant_id`, `area_id`, `device_id`, `channel_id`, `point_code`, `point_name`, `point_type`, `layout_x`, `layout_y`, `armed_status`, `alarm_status`, `online_status`, `sort`, `enabled`, `creator`, `updater`)
SELECT 1, a.id, d.id, c.id, 'CF-POINT-005', '007', 'sensor', 94.00, 54.00, 1, 0, 1, 50, 1, 'system', 'system'
FROM `iot_cloud_defense_area` a
JOIN `ibms_device` d ON d.device_code = 'CF-AC-CAM-005' AND d.deleted = b'0'
JOIN `ibms_channel` c ON c.code = 'CF-CH-005' AND c.deleted = b'0'
WHERE a.area_code = 'CF-AREA-001' AND a.deleted = b'0'
  AND NOT EXISTS (SELECT 1 FROM `iot_cloud_defense_point` WHERE `point_code` = 'CF-POINT-005' AND `deleted` = b'0');

INSERT INTO `iot_cloud_defense_point`
(`tenant_id`, `area_id`, `device_id`, `channel_id`, `point_code`, `point_name`, `point_type`, `layout_x`, `layout_y`, `armed_status`, `alarm_status`, `online_status`, `sort`, `enabled`, `creator`, `updater`)
SELECT 1, a.id, d.id, c.id, 'CF-POINT-006', '011', 'sensor', 8.00, 76.00, 1, 1, 1, 60, 1, 'system', 'system'
FROM `iot_cloud_defense_area` a
JOIN `ibms_device` d ON d.device_code = 'CF-AC-CAM-006' AND d.deleted = b'0'
JOIN `ibms_channel` c ON c.code = 'CF-CH-006' AND c.deleted = b'0'
WHERE a.area_code = 'CF-AREA-001' AND a.deleted = b'0'
  AND NOT EXISTS (SELECT 1 FROM `iot_cloud_defense_point` WHERE `point_code` = 'CF-POINT-006' AND `deleted` = b'0');

INSERT INTO `iot_alarm_zone`
(`host_id`, `partition_id`, `zone_no`, `zone_name`, `zone_type`, `zone_status`, `area_location`, `status`, `status_name`, `arm_status`, `alarm_status`, `online_status`, `alarm_count`, `last_alarm_time`, `tenant_id`, `creator`, `updater`)
SELECT 10000001, 10000020, 301, '周界防护区', 'DOOR', 'ARM', '周界防护区', 'A', '布防', 1, 0, 1, 2, NOW() - INTERVAL 1 DAY, 1, 'system', 'system'
WHERE NOT EXISTS (SELECT 1 FROM `iot_alarm_zone` WHERE `host_id` = 10000001 AND `zone_no` = 301 AND `deleted` = b'0');

INSERT INTO `iot_alarm_zone`
(`host_id`, `partition_id`, `zone_no`, `zone_name`, `zone_type`, `zone_status`, `area_location`, `status`, `status_name`, `arm_status`, `alarm_status`, `online_status`, `alarm_count`, `last_alarm_time`, `tenant_id`, `creator`, `updater`)
SELECT 10000001, 10000020, 302, '仓储防护区', 'PIR', 'ARM', '仓储防护区', 'A', '布防', 1, 0, 1, 1, NOW() - INTERVAL 2 DAY, 1, 'system', 'system'
WHERE NOT EXISTS (SELECT 1 FROM `iot_alarm_zone` WHERE `host_id` = 10000001 AND `zone_no` = 302 AND `deleted` = b'0');

INSERT INTO `iot_alarm_zone`
(`host_id`, `partition_id`, `zone_no`, `zone_name`, `zone_type`, `zone_status`, `area_location`, `status`, `status_name`, `arm_status`, `alarm_status`, `online_status`, `alarm_count`, `last_alarm_time`, `tenant_id`, `creator`, `updater`)
SELECT 10000001, 10000020, 303, '生产防护区', 'PIR', 'DISARM', '生产防护区', 'b', '撤防', 0, 0, 1, 0, NULL, 1, 'system', 'system'
WHERE NOT EXISTS (SELECT 1 FROM `iot_alarm_zone` WHERE `host_id` = 10000001 AND `zone_no` = 303 AND `deleted` = b'0');

INSERT INTO `iot_alarm_zone`
(`host_id`, `partition_id`, `zone_no`, `zone_name`, `zone_type`, `zone_status`, `area_location`, `status`, `status_name`, `arm_status`, `alarm_status`, `online_status`, `alarm_count`, `last_alarm_time`, `tenant_id`, `creator`, `updater`)
SELECT 10000001, 10000020, 304, '办公防护区', 'PIR', 'DISARM', '办公防护区', 'b', '撤防', 0, 0, 1, 0, NULL, 1, 'system', 'system'
WHERE NOT EXISTS (SELECT 1 FROM `iot_alarm_zone` WHERE `host_id` = 10000001 AND `zone_no` = 304 AND `deleted` = b'0');

INSERT INTO `iot_alarm_zone`
(`host_id`, `partition_id`, `zone_no`, `zone_name`, `zone_type`, `zone_status`, `area_location`, `status`, `status_name`, `arm_status`, `alarm_status`, `online_status`, `alarm_count`, `last_alarm_time`, `tenant_id`, `creator`, `updater`)
SELECT 10000001, 10000020, 305, '重点区域A', 'PIR', 'ARM', '重点区域A', 'B', '布防+报警', 1, 1, 1, 3, NOW() - INTERVAL 10 MINUTE, 1, 'system', 'system'
WHERE NOT EXISTS (SELECT 1 FROM `iot_alarm_zone` WHERE `host_id` = 10000001 AND `zone_no` = 305 AND `deleted` = b'0');

INSERT INTO `iot_cloud_defense_score_log`
(`tenant_id`, `score`, `score_level`, `score_time`, `remark`, `creator`, `updater`)
SELECT 1, 92, '优秀', NOW(), '周界联防整体态势稳定', 'system', 'system'
WHERE NOT EXISTS (
    SELECT 1 FROM `iot_cloud_defense_score_log`
    WHERE `score` = 92 AND `score_level` = '优秀' AND `deleted` = b'0'
);

INSERT INTO `iot_device_event_log`
(`tenant_id`, `device_id`, `device_name`, `event_identifier`, `event_name`, `event_type`, `event_data`, `event_time`, `processed`, `creator`, `updater`)
SELECT 1, d.id, d.name, 'CF_INTRUSION_001', '周界入侵告警', 'alert', '{"level":"major","area":"周界防护区"}', NOW() - INTERVAL 30 MINUTE, 0, 'system', 'system'
FROM `ibms_device` d
WHERE d.device_code = 'CF-VI-CAM-001' AND d.deleted = b'0'
  AND NOT EXISTS (SELECT 1 FROM `iot_device_event_log` WHERE `event_identifier` = 'CF_INTRUSION_001' AND `deleted` = b'0');

INSERT INTO `iot_device_event_log`
(`tenant_id`, `device_id`, `device_name`, `event_identifier`, `event_name`, `event_type`, `event_data`, `event_time`, `processed`, `creator`, `updater`)
SELECT 1, d.id, d.name, 'CF_INTRUSION_002', '仓储区域告警', 'alert', '{"level":"warning","area":"仓储防护区"}', NOW() - INTERVAL 20 MINUTE, 0, 'system', 'system'
FROM `ibms_device` d
WHERE d.device_code = 'CF-VI-CAM-002' AND d.deleted = b'0'
  AND NOT EXISTS (SELECT 1 FROM `iot_device_event_log` WHERE `event_identifier` = 'CF_INTRUSION_002' AND `deleted` = b'0');

INSERT INTO `iot_device_event_log`
(`tenant_id`, `device_id`, `device_name`, `event_identifier`, `event_name`, `event_type`, `event_data`, `event_time`, `processed`, `creator`, `updater`)
SELECT 1, d.id, d.name, 'CF_INTRUSION_003', '重点区域联防告警', 'alert', '{"level":"critical","area":"重点区域A"}', NOW() - INTERVAL 10 MINUTE, 0, 'system', 'system'
FROM `ibms_device` d
WHERE d.device_code = 'CF-AC-CAM-005' AND d.deleted = b'0'
  AND NOT EXISTS (SELECT 1 FROM `iot_device_event_log` WHERE `event_identifier` = 'CF_INTRUSION_003' AND `deleted` = b'0');
