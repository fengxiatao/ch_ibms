-- =============================================
-- 快速修复：创建缺失的 iot_access_person_device_auth 表
-- 执行方式：在 MySQL 客户端中执行此脚本
-- =============================================

-- 创建人员设备授权状态表
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
