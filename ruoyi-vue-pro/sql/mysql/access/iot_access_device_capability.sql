-- 设备能力缓存表
-- 用于缓存门禁设备的能力信息，避免频繁查询设备
-- @author Kiro

CREATE TABLE IF NOT EXISTS `iot_access_device_capability` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `device_id` bigint NOT NULL COMMENT '设备ID',
    `device_generation` tinyint DEFAULT 2 COMMENT '设备代数：1-一代 2-二代',
    
    -- 用户能力
    `max_users` int DEFAULT 10000 COMMENT '最大用户数',
    `max_cards` int DEFAULT 10000 COMMENT '最大卡片数',
    `max_faces` int DEFAULT 3000 COMMENT '最大人脸数',
    `max_fingerprints` int DEFAULT 3000 COMMENT '最大指纹数',
    
    -- 当前使用量
    `current_users` int DEFAULT 0 COMMENT '当前用户数',
    `current_cards` int DEFAULT 0 COMMENT '当前卡片数',
    `current_faces` int DEFAULT 0 COMMENT '当前人脸数',
    `current_fingerprints` int DEFAULT 0 COMMENT '当前指纹数',
    
    -- 功能支持标志
    `sup_face_service` tinyint DEFAULT 1 COMMENT '是否支持人脸服务：0-否 1-是',
    `sup_fingerprint_service` tinyint DEFAULT 1 COMMENT '是否支持指纹服务：0-否 1-是',
    `sup_card_service` tinyint DEFAULT 1 COMMENT '是否支持独立卡片服务：0-否 1-是',
    `sup_holiday_plan` tinyint DEFAULT 0 COMMENT '是否支持假日计划：0-否 1-是',
    
    -- 批量下发速率限制
    `max_insert_rate_user` int DEFAULT 10 COMMENT '每次下发最大用户数',
    `max_insert_rate_card` int DEFAULT 10 COMMENT '每次下发最大卡数',
    `max_insert_rate_face` int DEFAULT 5 COMMENT '每次下发最大人脸数',
    `max_insert_rate_fingerprint` int DEFAULT 10 COMMENT '每次下发最大指纹数',
    
    -- 其他能力
    `channels` int DEFAULT 4 COMMENT '通道数',
    `max_cards_per_user` int DEFAULT 5 COMMENT '每用户最大卡数',
    `max_fingerprints_per_user` int DEFAULT 10 COMMENT '每用户最大指纹数',
    `max_face_image_size` int DEFAULT 200 COMMENT '最大人脸图片大小(KB)',
    
    -- 完整能力集JSON
    `capability_json` text COMMENT '完整能力集JSON',
    
    -- 缓存管理
    `last_query_time` datetime DEFAULT NULL COMMENT '最后查询时间',
    `cache_expire_time` datetime DEFAULT NULL COMMENT '缓存过期时间',
    
    -- 通用字段
    `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
    `creator` varchar(64) DEFAULT '' COMMENT '创建者',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` varchar(64) DEFAULT '' COMMENT '更新者',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_device_id` (`device_id`, `deleted`),
    KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='设备能力缓存表';
