-- OPC防区配置表
CREATE TABLE IF NOT EXISTS `iot_opc_zone_config` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `device_id` bigint NOT NULL COMMENT '设备ID（关联iot_device.id）',
  `area` int NOT NULL COMMENT '防区号（01-99）',
  `point` int NOT NULL COMMENT '点位号（001-999）',
  `zone_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '防区名称（手动配置）',
  `zone_type` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '防区类型（instant1/instant2/delay1/delay2/follow/24hour）',
  `location` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '位置信息（手动配置）',
  `camera_id` bigint DEFAULT NULL COMMENT '关联摄像头ID（手动配置）',
  `enabled` bit(1) NOT NULL DEFAULT b'1' COMMENT '是否启用',
  `remark` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `creator` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_device_area_point` (`device_id`,`area`,`point`,`deleted`),
  KEY `idx_device_id` (`device_id`),
  KEY `idx_camera_id` (`camera_id`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='OPC防区配置表';
