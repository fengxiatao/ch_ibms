-- =============================================
-- 门禁授权任务表结构更新
-- 版本: V1.2
-- 说明: 根据授权下发功能设计文档更新表结构
-- =============================================

-- ----------------------------
-- 1. 更新授权任务表 iot_access_auth_task
-- 添加 person_id 字段（单人下发时使用）
-- 移除 device_id 字段（设备信息在明细表中）
-- ----------------------------
-- 检查并添加 person_id 字段
SET @exist_person_id := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'iot_access_auth_task' AND COLUMN_NAME = 'person_id');
SET @sql_add_person_id := IF(@exist_person_id = 0, 
    'ALTER TABLE `iot_access_auth_task` ADD COLUMN `person_id` bigint DEFAULT NULL COMMENT ''人员ID（单人下发/撤销时必填）'' AFTER `group_id`',
    'SELECT ''person_id column already exists''');
PREPARE stmt FROM @sql_add_person_id;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加 person_id 索引
SET @exist_idx := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'iot_access_auth_task' AND INDEX_NAME = 'idx_person_id');
SET @sql_add_idx := IF(@exist_idx = 0, 
    'ALTER TABLE `iot_access_auth_task` ADD INDEX `idx_person_id` (`person_id`)',
    'SELECT ''idx_person_id already exists''');
PREPARE stmt FROM @sql_add_idx;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ----------------------------
-- 2. 更新授权任务明细表 iot_access_auth_task_detail
-- 添加冗余字段便于查询展示
-- ----------------------------
-- 添加 person_code 字段
SET @exist_person_code := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'iot_access_auth_task_detail' AND COLUMN_NAME = 'person_code');
SET @sql_add_person_code := IF(@exist_person_code = 0, 
    'ALTER TABLE `iot_access_auth_task_detail` ADD COLUMN `person_code` varchar(64) DEFAULT NULL COMMENT ''人员编号'' AFTER `person_id`',
    'SELECT ''person_code column already exists''');
PREPARE stmt FROM @sql_add_person_code;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加 person_name 字段
SET @exist_person_name := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'iot_access_auth_task_detail' AND COLUMN_NAME = 'person_name');
SET @sql_add_person_name := IF(@exist_person_name = 0, 
    'ALTER TABLE `iot_access_auth_task_detail` ADD COLUMN `person_name` varchar(64) DEFAULT NULL COMMENT ''人员姓名'' AFTER `person_code`',
    'SELECT ''person_name column already exists''');
PREPARE stmt FROM @sql_add_person_name;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加 device_name 字段
SET @exist_device_name := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'iot_access_auth_task_detail' AND COLUMN_NAME = 'device_name');
SET @sql_add_device_name := IF(@exist_device_name = 0, 
    'ALTER TABLE `iot_access_auth_task_detail` ADD COLUMN `device_name` varchar(128) DEFAULT NULL COMMENT ''设备名称'' AFTER `device_id`',
    'SELECT ''device_name column already exists''');
PREPARE stmt FROM @sql_add_device_name;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加 credential_types 字段
SET @exist_credential_types := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'iot_access_auth_task_detail' AND COLUMN_NAME = 'credential_types');
SET @sql_add_credential_types := IF(@exist_credential_types = 0, 
    'ALTER TABLE `iot_access_auth_task_detail` ADD COLUMN `credential_types` varchar(128) DEFAULT NULL COMMENT ''下发的凭证类型，逗号分隔'' AFTER `error_message`',
    'SELECT ''credential_types column already exists''');
PREPARE stmt FROM @sql_add_credential_types;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 移除 retry_count 字段（如果存在）- 重试逻辑改为重新创建明细
SET @exist_retry_count := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'iot_access_auth_task_detail' AND COLUMN_NAME = 'retry_count');
SET @sql_drop_retry_count := IF(@exist_retry_count > 0, 
    'ALTER TABLE `iot_access_auth_task_detail` DROP COLUMN `retry_count`',
    'SELECT ''retry_count column does not exist''');
PREPARE stmt FROM @sql_drop_retry_count;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ----------------------------
-- 3. 创建人员授权状态表 iot_access_person_device_auth
-- ----------------------------
CREATE TABLE IF NOT EXISTS `iot_access_person_device_auth` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `person_id` bigint NOT NULL COMMENT '人员ID',
    `device_id` bigint NOT NULL COMMENT '设备ID',
    `channel_id` bigint DEFAULT NULL COMMENT '通道ID',
    `auth_status` tinyint NOT NULL DEFAULT 0 COMMENT '授权状态：0-未授权，1-已授权，2-授权中，3-授权失败',
    `last_dispatch_time` datetime DEFAULT NULL COMMENT '最后下发时间',
    `last_dispatch_result` varchar(500) DEFAULT NULL COMMENT '最后下发结果',
    `credential_hash` varchar(64) DEFAULT NULL COMMENT '凭证哈希（用于增量检测）',
    `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
    `creator` varchar(64) DEFAULT '' COMMENT '创建者',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` varchar(64) DEFAULT '' COMMENT '更新者',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_person_device_channel` (`person_id`, `device_id`, `channel_id`, `tenant_id`, `deleted`),
    KEY `idx_person_id` (`person_id`),
    KEY `idx_device_id` (`device_id`),
    KEY `idx_auth_status` (`auth_status`),
    KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='人员设备授权状态表';

-- ----------------------------
-- 4. 更新任务明细表状态值说明
-- 原状态：0-待执行，1-执行中，2-成功，3-失败
-- 新状态：0-待执行，1-成功，2-失败（简化状态）
-- ----------------------------
-- 将原状态2(成功)更新为1
UPDATE `iot_access_auth_task_detail` SET `status` = 1 WHERE `status` = 2;
-- 将原状态3(失败)更新为2
UPDATE `iot_access_auth_task_detail` SET `status` = 2 WHERE `status` = 3;
-- 将原状态1(执行中)更新为0(待执行)，因为执行中的任务重启后需要重新执行
UPDATE `iot_access_auth_task_detail` SET `status` = 0 WHERE `status` = 1;
