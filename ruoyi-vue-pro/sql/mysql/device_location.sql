-- 设备位置表
CREATE TABLE IF NOT EXISTS `device_location` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `device_id` bigint NOT NULL COMMENT '设备ID',
  `floor_id` bigint NULL COMMENT '所属楼层ID',
  `building_id` bigint NULL COMMENT '所属建筑ID（冗余字段）',
  `area_id` bigint NULL COMMENT '所属区域ID',
  `local_x` decimal(10,3) NOT NULL COMMENT '本地X坐标（米）',
  `local_y` decimal(10,3) NOT NULL COMMENT '本地Y坐标（米）',
  `local_z` decimal(10,3) DEFAULT 0.000 COMMENT '本地Z坐标（米）',
  `global_longitude` decimal(12,8) NULL COMMENT '全局经度',
  `global_latitude` decimal(12,8) NULL COMMENT '全局纬度',
  `global_altitude` decimal(10,3) NULL COMMENT '全局海拔高度（米）',
  `install_date` date NULL COMMENT '安装日期',
  `installer` varchar(50) NULL COMMENT '安装人员',
  `remark` varchar(500) NULL COMMENT '备注',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_device_id` (`device_id`, `deleted`),
  KEY `idx_floor_id` (`floor_id`),
  KEY `idx_building_id` (`building_id`),
  KEY `idx_area_id` (`area_id`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='设备位置信息表';


















