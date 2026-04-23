CREATE TABLE IF NOT EXISTS `iot_factory_report_template` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `tenant_id` BIGINT NOT NULL DEFAULT 1,
    `template_code` VARCHAR(64) NOT NULL,
    `template_name` VARCHAR(100) NOT NULL,
    `template_category` VARCHAR(32) NOT NULL,
    `template_desc` VARCHAR(255) DEFAULT '',
    `sort_no` INT NOT NULL DEFAULT 0,
    `status` VARCHAR(32) NOT NULL DEFAULT 'ENABLED',
    `cron_type` VARCHAR(32) NOT NULL DEFAULT 'MANUAL',
    `remark` VARCHAR(255) DEFAULT '',
    `creator` VARCHAR(64) DEFAULT '',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updater` VARCHAR(64) DEFAULT '',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` BIT(1) NOT NULL DEFAULT b'0',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_factory_report_template_code` (`tenant_id`, `template_code`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='工厂报表中心模板表';

CREATE TABLE IF NOT EXISTS `iot_factory_report_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `tenant_id` BIGINT NOT NULL DEFAULT 1,
    `template_id` BIGINT NOT NULL,
    `report_name` VARCHAR(120) NOT NULL,
    `report_category` VARCHAR(32) NOT NULL,
    `status` VARCHAR(32) NOT NULL DEFAULT 'SUCCESS',
    `biz_date` DATE NOT NULL,
    `generated_at` DATETIME NOT NULL,
    `file_name` VARCHAR(160) DEFAULT '',
    `file_url` VARCHAR(255) DEFAULT '',
    `operator_name` VARCHAR(64) DEFAULT '',
    `remark` VARCHAR(255) DEFAULT '',
    `creator` VARCHAR(64) DEFAULT '',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updater` VARCHAR(64) DEFAULT '',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` BIT(1) NOT NULL DEFAULT b'0',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_factory_report_record_name` (`tenant_id`, `report_name`, `deleted`),
    KEY `idx_factory_report_record_template_time` (`tenant_id`, `template_id`, `generated_at`),
    KEY `idx_factory_report_record_status` (`tenant_id`, `status`, `generated_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='工厂报表中心生成记录表';

INSERT INTO `iot_factory_report_template`
(`tenant_id`, `template_code`, `template_name`, `template_category`, `template_desc`, `sort_no`, `status`, `cron_type`, `remark`, `creator`, `updater`)
SELECT 1, 'DAILY_REPORT', '日报表', '日报表', '每日生产统计报表', 10, 'ENABLED', 'DAILY', '原型固定模板', 'system', 'system'
WHERE NOT EXISTS (
    SELECT 1 FROM `iot_factory_report_template`
    WHERE `tenant_id` = 1 AND `template_code` = 'DAILY_REPORT' AND `deleted` = b'0'
);

INSERT INTO `iot_factory_report_template`
(`tenant_id`, `template_code`, `template_name`, `template_category`, `template_desc`, `sort_no`, `status`, `cron_type`, `remark`, `creator`, `updater`)
SELECT 1, 'WEEKLY_REPORT', '周报表', '周报表', '每周生产汇总报表', 20, 'ENABLED', 'WEEKLY', '原型固定模板', 'system', 'system'
WHERE NOT EXISTS (
    SELECT 1 FROM `iot_factory_report_template`
    WHERE `tenant_id` = 1 AND `template_code` = 'WEEKLY_REPORT' AND `deleted` = b'0'
);

INSERT INTO `iot_factory_report_template`
(`tenant_id`, `template_code`, `template_name`, `template_category`, `template_desc`, `sort_no`, `status`, `cron_type`, `remark`, `creator`, `updater`)
SELECT 1, 'MONTHLY_REPORT', '月报表', '月报表', '每月生产统计报表', 30, 'ENABLED', 'MONTHLY', '原型固定模板', 'system', 'system'
WHERE NOT EXISTS (
    SELECT 1 FROM `iot_factory_report_template`
    WHERE `tenant_id` = 1 AND `template_code` = 'MONTHLY_REPORT' AND `deleted` = b'0'
);

INSERT INTO `iot_factory_report_template`
(`tenant_id`, `template_code`, `template_name`, `template_category`, `template_desc`, `sort_no`, `status`, `cron_type`, `remark`, `creator`, `updater`)
SELECT 1, 'DEVICE_REPORT', '设备运行报表', '设备报表', '设备运行状态统计报表', 40, 'ENABLED', 'DAILY', '原型固定模板', 'system', 'system'
WHERE NOT EXISTS (
    SELECT 1 FROM `iot_factory_report_template`
    WHERE `tenant_id` = 1 AND `template_code` = 'DEVICE_REPORT' AND `deleted` = b'0'
);

INSERT INTO `iot_factory_report_template`
(`tenant_id`, `template_code`, `template_name`, `template_category`, `template_desc`, `sort_no`, `status`, `cron_type`, `remark`, `creator`, `updater`)
SELECT 1, 'ENERGY_REPORT', '能耗分析报表', '能耗报表', '能源消耗分析报表', 50, 'ENABLED', 'DAILY', '原型固定模板', 'system', 'system'
WHERE NOT EXISTS (
    SELECT 1 FROM `iot_factory_report_template`
    WHERE `tenant_id` = 1 AND `template_code` = 'ENERGY_REPORT' AND `deleted` = b'0'
);

INSERT INTO `iot_factory_report_template`
(`tenant_id`, `template_code`, `template_name`, `template_category`, `template_desc`, `sort_no`, `status`, `cron_type`, `remark`, `creator`, `updater`)
SELECT 1, 'QUALITY_REPORT', '质量分析报表', '质量报表', '质量检测分析报表', 60, 'ENABLED', 'DAILY', '原型固定模板', 'system', 'system'
WHERE NOT EXISTS (
    SELECT 1 FROM `iot_factory_report_template`
    WHERE `tenant_id` = 1 AND `template_code` = 'QUALITY_REPORT' AND `deleted` = b'0'
);

INSERT INTO `iot_factory_report_record`
(`tenant_id`, `template_id`, `report_name`, `report_category`, `status`, `biz_date`, `generated_at`, `file_name`, `file_url`, `operator_name`, `remark`, `creator`, `updater`)
SELECT 1, t.id, '日报表-20260415-0800', '日报表', 'SUCCESS', '2026-04-15', '2026-04-15 08:00:00', '日报表-20260415-0800.xls', '', '系统任务', '今日已生成', 'system', 'system'
FROM `iot_factory_report_template` t
WHERE t.template_code = 'DAILY_REPORT' AND t.deleted = b'0'
  AND NOT EXISTS (SELECT 1 FROM `iot_factory_report_record` WHERE `tenant_id` = 1 AND `report_name` = '日报表-20260415-0800' AND `deleted` = b'0');

INSERT INTO `iot_factory_report_record`
(`tenant_id`, `template_id`, `report_name`, `report_category`, `status`, `biz_date`, `generated_at`, `file_name`, `file_url`, `operator_name`, `remark`, `creator`, `updater`)
SELECT 1, t.id, '周报表-20260415-0900', '周报表', 'SUCCESS', '2026-04-15', '2026-04-15 09:00:00', '周报表-20260415-0900.xls', '', '系统任务', '今日已生成', 'system', 'system'
FROM `iot_factory_report_template` t
WHERE t.template_code = 'WEEKLY_REPORT' AND t.deleted = b'0'
  AND NOT EXISTS (SELECT 1 FROM `iot_factory_report_record` WHERE `tenant_id` = 1 AND `report_name` = '周报表-20260415-0900' AND `deleted` = b'0');

INSERT INTO `iot_factory_report_record`
(`tenant_id`, `template_id`, `report_name`, `report_category`, `status`, `biz_date`, `generated_at`, `file_name`, `file_url`, `operator_name`, `remark`, `creator`, `updater`)
SELECT 1, t.id, '月报表-20260415-0930', '月报表', 'FAILED', '2026-04-15', '2026-04-15 09:30:00', '月报表-20260415-0930.xls', '', '系统任务', '生成失败示例', 'system', 'system'
FROM `iot_factory_report_template` t
WHERE t.template_code = 'MONTHLY_REPORT' AND t.deleted = b'0'
  AND NOT EXISTS (SELECT 1 FROM `iot_factory_report_record` WHERE `tenant_id` = 1 AND `report_name` = '月报表-20260415-0930' AND `deleted` = b'0');

INSERT INTO `iot_factory_report_record`
(`tenant_id`, `template_id`, `report_name`, `report_category`, `status`, `biz_date`, `generated_at`, `file_name`, `file_url`, `operator_name`, `remark`, `creator`, `updater`)
SELECT 1, t.id, '设备运行报表-20260415-0800', '设备报表', 'SUCCESS', '2026-04-15', '2026-04-15 08:00:00', '设备运行报表-20260415-0800.xls', '', '设备班长', '今日已生成', 'system', 'system'
FROM `iot_factory_report_template` t
WHERE t.template_code = 'DEVICE_REPORT' AND t.deleted = b'0'
  AND NOT EXISTS (SELECT 1 FROM `iot_factory_report_record` WHERE `tenant_id` = 1 AND `report_name` = '设备运行报表-20260415-0800' AND `deleted` = b'0');

INSERT INTO `iot_factory_report_record`
(`tenant_id`, `template_id`, `report_name`, `report_category`, `status`, `biz_date`, `generated_at`, `file_name`, `file_url`, `operator_name`, `remark`, `creator`, `updater`)
SELECT 1, t.id, '能耗分析报表-20260415-0700', '能耗报表', 'SUCCESS', '2026-04-15', '2026-04-15 07:00:00', '能耗分析报表-20260415-0700.xls', '', '能源专员', '今日已生成', 'system', 'system'
FROM `iot_factory_report_template` t
WHERE t.template_code = 'ENERGY_REPORT' AND t.deleted = b'0'
  AND NOT EXISTS (SELECT 1 FROM `iot_factory_report_record` WHERE `tenant_id` = 1 AND `report_name` = '能耗分析报表-20260415-0700' AND `deleted` = b'0');

INSERT INTO `iot_factory_report_record`
(`tenant_id`, `template_id`, `report_name`, `report_category`, `status`, `biz_date`, `generated_at`, `file_name`, `file_url`, `operator_name`, `remark`, `creator`, `updater`)
SELECT 1, t.id, '质量分析报表-20260414-1800', '质量报表', 'SUCCESS', '2026-04-14', '2026-04-14 18:00:00', '质量分析报表-20260414-1800.xls', '', '质量经理', '昨日生成', 'system', 'system'
FROM `iot_factory_report_template` t
WHERE t.template_code = 'QUALITY_REPORT' AND t.deleted = b'0'
  AND NOT EXISTS (SELECT 1 FROM `iot_factory_report_record` WHERE `tenant_id` = 1 AND `report_name` = '质量分析报表-20260414-1800' AND `deleted` = b'0');

INSERT INTO `iot_factory_report_record`
(`tenant_id`, `template_id`, `report_name`, `report_category`, `status`, `biz_date`, `generated_at`, `file_name`, `file_url`, `operator_name`, `remark`, `creator`, `updater`)
SELECT 1, t.id, '日报表-20260414-1800', '日报表', 'SUCCESS', '2026-04-14', '2026-04-14 18:00:00', '日报表-20260414-1800.xls', '', '系统任务', '历史生成记录', 'system', 'system'
FROM `iot_factory_report_template` t
WHERE t.template_code = 'DAILY_REPORT' AND t.deleted = b'0'
  AND NOT EXISTS (SELECT 1 FROM `iot_factory_report_record` WHERE `tenant_id` = 1 AND `report_name` = '日报表-20260414-1800' AND `deleted` = b'0');

INSERT INTO `iot_factory_report_record`
(`tenant_id`, `template_id`, `report_name`, `report_category`, `status`, `biz_date`, `generated_at`, `file_name`, `file_url`, `operator_name`, `remark`, `creator`, `updater`)
SELECT 1, t.id, '周报表-20260408-1800', '周报表', 'SUCCESS', '2026-04-08', '2026-04-08 18:00:00', '周报表-20260408-1800.xls', '', '系统任务', '历史生成记录', 'system', 'system'
FROM `iot_factory_report_template` t
WHERE t.template_code = 'WEEKLY_REPORT' AND t.deleted = b'0'
  AND NOT EXISTS (SELECT 1 FROM `iot_factory_report_record` WHERE `tenant_id` = 1 AND `report_name` = '周报表-20260408-1800' AND `deleted` = b'0');

INSERT INTO `iot_factory_report_record`
(`tenant_id`, `template_id`, `report_name`, `report_category`, `status`, `biz_date`, `generated_at`, `file_name`, `file_url`, `operator_name`, `remark`, `creator`, `updater`)
SELECT 1, t.id, '月报表-20260331-0900', '月报表', 'SUCCESS', '2026-03-31', '2026-03-31 09:00:00', '月报表-20260331-0900.xls', '', '系统任务', '月度收口记录', 'system', 'system'
FROM `iot_factory_report_template` t
WHERE t.template_code = 'MONTHLY_REPORT' AND t.deleted = b'0'
  AND NOT EXISTS (SELECT 1 FROM `iot_factory_report_record` WHERE `tenant_id` = 1 AND `report_name` = '月报表-20260331-0900' AND `deleted` = b'0');

INSERT INTO `iot_factory_report_record`
(`tenant_id`, `template_id`, `report_name`, `report_category`, `status`, `biz_date`, `generated_at`, `file_name`, `file_url`, `operator_name`, `remark`, `creator`, `updater`)
SELECT 1, t.id, '设备运行报表-20260414-0800', '设备报表', 'SUCCESS', '2026-04-14', '2026-04-14 08:00:00', '设备运行报表-20260414-0800.xls', '', '设备班长', '历史生成记录', 'system', 'system'
FROM `iot_factory_report_template` t
WHERE t.template_code = 'DEVICE_REPORT' AND t.deleted = b'0'
  AND NOT EXISTS (SELECT 1 FROM `iot_factory_report_record` WHERE `tenant_id` = 1 AND `report_name` = '设备运行报表-20260414-0800' AND `deleted` = b'0');

INSERT INTO `iot_factory_report_record`
(`tenant_id`, `template_id`, `report_name`, `report_category`, `status`, `biz_date`, `generated_at`, `file_name`, `file_url`, `operator_name`, `remark`, `creator`, `updater`)
SELECT 1, t.id, '能耗分析报表-20260414-0700', '能耗报表', 'SUCCESS', '2026-04-14', '2026-04-14 07:00:00', '能耗分析报表-20260414-0700.xls', '', '能源专员', '历史生成记录', 'system', 'system'
FROM `iot_factory_report_template` t
WHERE t.template_code = 'ENERGY_REPORT' AND t.deleted = b'0'
  AND NOT EXISTS (SELECT 1 FROM `iot_factory_report_record` WHERE `tenant_id` = 1 AND `report_name` = '能耗分析报表-20260414-0700' AND `deleted` = b'0');

INSERT INTO `iot_factory_report_record`
(`tenant_id`, `template_id`, `report_name`, `report_category`, `status`, `biz_date`, `generated_at`, `file_name`, `file_url`, `operator_name`, `remark`, `creator`, `updater`)
SELECT 1, t.id, '质量分析报表-20260413-1800', '质量报表', 'SUCCESS', '2026-04-13', '2026-04-13 18:00:00', '质量分析报表-20260413-1800.xls', '', '质量经理', '历史生成记录', 'system', 'system'
FROM `iot_factory_report_template` t
WHERE t.template_code = 'QUALITY_REPORT' AND t.deleted = b'0'
  AND NOT EXISTS (SELECT 1 FROM `iot_factory_report_record` WHERE `tenant_id` = 1 AND `report_name` = '质量分析报表-20260413-1800' AND `deleted` = b'0');

INSERT INTO `iot_factory_report_record`
(`tenant_id`, `template_id`, `report_name`, `report_category`, `status`, `biz_date`, `generated_at`, `file_name`, `file_url`, `operator_name`, `remark`, `creator`, `updater`)
SELECT 1, t.id, '日报表-20260413-1800', '日报表', 'SUCCESS', '2026-04-13', '2026-04-13 18:00:00', '日报表-20260413-1800.xls', '', '系统任务', '历史生成记录', 'system', 'system'
FROM `iot_factory_report_template` t
WHERE t.template_code = 'DAILY_REPORT' AND t.deleted = b'0'
  AND NOT EXISTS (SELECT 1 FROM `iot_factory_report_record` WHERE `tenant_id` = 1 AND `report_name` = '日报表-20260413-1800' AND `deleted` = b'0');

INSERT INTO `iot_factory_report_record`
(`tenant_id`, `template_id`, `report_name`, `report_category`, `status`, `biz_date`, `generated_at`, `file_name`, `file_url`, `operator_name`, `remark`, `creator`, `updater`)
SELECT 1, t.id, '周报表-20260401-1800', '周报表', 'SUCCESS', '2026-04-01', '2026-04-01 18:00:00', '周报表-20260401-1800.xls', '', '系统任务', '历史生成记录', 'system', 'system'
FROM `iot_factory_report_template` t
WHERE t.template_code = 'WEEKLY_REPORT' AND t.deleted = b'0'
  AND NOT EXISTS (SELECT 1 FROM `iot_factory_report_record` WHERE `tenant_id` = 1 AND `report_name` = '周报表-20260401-1800' AND `deleted` = b'0');

INSERT INTO `iot_factory_report_record`
(`tenant_id`, `template_id`, `report_name`, `report_category`, `status`, `biz_date`, `generated_at`, `file_name`, `file_url`, `operator_name`, `remark`, `creator`, `updater`)
SELECT 1, t.id, '设备运行报表-20260413-0800', '设备报表', 'SUCCESS', '2026-04-13', '2026-04-13 08:00:00', '设备运行报表-20260413-0800.xls', '', '设备班长', '历史生成记录', 'system', 'system'
FROM `iot_factory_report_template` t
WHERE t.template_code = 'DEVICE_REPORT' AND t.deleted = b'0'
  AND NOT EXISTS (SELECT 1 FROM `iot_factory_report_record` WHERE `tenant_id` = 1 AND `report_name` = '设备运行报表-20260413-0800' AND `deleted` = b'0');

INSERT INTO `iot_factory_report_record`
(`tenant_id`, `template_id`, `report_name`, `report_category`, `status`, `biz_date`, `generated_at`, `file_name`, `file_url`, `operator_name`, `remark`, `creator`, `updater`)
SELECT 1, t.id, '能耗分析报表-20260413-0700', '能耗报表', 'FAILED', '2026-04-13', '2026-04-13 07:00:00', '能耗分析报表-20260413-0700.xls', '', '能源专员', '能耗接口抖动导致失败', 'system', 'system'
FROM `iot_factory_report_template` t
WHERE t.template_code = 'ENERGY_REPORT' AND t.deleted = b'0'
  AND NOT EXISTS (SELECT 1 FROM `iot_factory_report_record` WHERE `tenant_id` = 1 AND `report_name` = '能耗分析报表-20260413-0700' AND `deleted` = b'0');

INSERT INTO `iot_factory_report_record`
(`tenant_id`, `template_id`, `report_name`, `report_category`, `status`, `biz_date`, `generated_at`, `file_name`, `file_url`, `operator_name`, `remark`, `creator`, `updater`)
SELECT 1, t.id, '质量分析报表-20260412-1800', '质量报表', 'SUCCESS', '2026-04-12', '2026-04-12 18:00:00', '质量分析报表-20260412-1800.xls', '', '质量经理', '历史生成记录', 'system', 'system'
FROM `iot_factory_report_template` t
WHERE t.template_code = 'QUALITY_REPORT' AND t.deleted = b'0'
  AND NOT EXISTS (SELECT 1 FROM `iot_factory_report_record` WHERE `tenant_id` = 1 AND `report_name` = '质量分析报表-20260412-1800' AND `deleted` = b'0');

INSERT INTO `iot_factory_report_record`
(`tenant_id`, `template_id`, `report_name`, `report_category`, `status`, `biz_date`, `generated_at`, `file_name`, `file_url`, `operator_name`, `remark`, `creator`, `updater`)
SELECT 1, t.id, '月报表-20260228-0900', '月报表', 'SUCCESS', '2026-02-28', '2026-02-28 09:00:00', '月报表-20260228-0900.xls', '', '系统任务', '月度收口记录', 'system', 'system'
FROM `iot_factory_report_template` t
WHERE t.template_code = 'MONTHLY_REPORT' AND t.deleted = b'0'
  AND NOT EXISTS (SELECT 1 FROM `iot_factory_report_record` WHERE `tenant_id` = 1 AND `report_name` = '月报表-20260228-0900' AND `deleted` = b'0');
