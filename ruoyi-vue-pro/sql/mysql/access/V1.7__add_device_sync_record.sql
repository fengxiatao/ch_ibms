-- =====================================================
-- 门禁设备人员同步记录表
-- 用于记录每次设备人员对账、清理、同步操作的结果
-- =====================================================

CREATE TABLE IF NOT EXISTS `iot_access_device_sync_record` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `device_id` bigint NOT NULL COMMENT '设备ID',
    `device_name` varchar(100) DEFAULT NULL COMMENT '设备名称',
    `sync_type` tinyint NOT NULL DEFAULT '1' COMMENT '同步类型：1-对账检查，2-清理多余，3-补发缺失，4-全量同步',
    `sync_status` tinyint NOT NULL DEFAULT '0' COMMENT '同步状态：0-进行中，1-成功，2-部分成功，3-失败',
    `system_user_count` int DEFAULT '0' COMMENT '系统应有人员数',
    `device_user_count` int DEFAULT '0' COMMENT '设备实际人员数',
    `synced_count` int DEFAULT '0' COMMENT '已同步人员数',
    `system_only_count` int DEFAULT '0' COMMENT '系统多余人员数（系统有、设备无）',
    `device_only_count` int DEFAULT '0' COMMENT '设备多余人员数（设备有、系统无）',
    `cleaned_count` int DEFAULT '0' COMMENT '已清理人员数',
    `repaired_count` int DEFAULT '0' COMMENT '已补发人员数',
    `sync_start_time` datetime DEFAULT NULL COMMENT '同步开始时间',
    `sync_end_time` datetime DEFAULT NULL COMMENT '同步结束时间',
    `operator_id` bigint DEFAULT NULL COMMENT '操作人ID',
    `operator_name` varchar(50) DEFAULT NULL COMMENT '操作人名称',
    `error_message` varchar(500) DEFAULT NULL COMMENT '错误信息',
    `system_only_user_ids` text COMMENT '系统多余用户ID列表（JSON数组）',
    `device_only_user_ids` text COMMENT '设备多余用户ID列表（JSON数组）',
    `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户ID',
    `creator` varchar(64) DEFAULT '' COMMENT '创建者',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` varchar(64) DEFAULT '' COMMENT '更新者',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`),
    KEY `idx_device_id` (`device_id`),
    KEY `idx_sync_type` (`sync_type`),
    KEY `idx_sync_status` (`sync_status`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='门禁设备人员同步记录表';
