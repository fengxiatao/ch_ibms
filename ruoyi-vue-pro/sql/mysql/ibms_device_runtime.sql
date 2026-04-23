-- =============================================
-- IBMS 设备运行态表（MySQL）
-- 主键 device_id 对齐 ibms_device.id（1:1）
-- 与 ibms_device 台账拆分：状态、定位、DeviceConfig、job_config 等
-- =============================================

SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `ibms_device_runtime` (
  `device_id` bigint NOT NULL COMMENT '设备ID，对应 ibms_device.id',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  `state` int NULL DEFAULT NULL COMMENT '设备状态，见 IotDeviceStateEnum',
  `online_time` datetime NULL DEFAULT NULL COMMENT '最后上线时间',
  `offline_time` datetime NULL DEFAULT NULL COMMENT '最后离线时间',
  `active_time` datetime NULL DEFAULT NULL COMMENT '激活时间',
  `firmware_id` bigint NULL DEFAULT NULL COMMENT '固件ID',
  `gateway_id` bigint NULL DEFAULT NULL COMMENT '关联网关设备ID',
  `location_type` int NULL DEFAULT NULL COMMENT '定位方式',
  `latitude` decimal(10,7) NULL DEFAULT NULL COMMENT '纬度',
  `longitude` decimal(10,7) NULL DEFAULT NULL COMMENT '经度',
  `area_id` int NULL DEFAULT NULL COMMENT '地区编码',
  `address` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '详细地址',
  `campus_id` bigint NULL DEFAULT NULL COMMENT '园区ID',
  `building_id` bigint NULL DEFAULT NULL COMMENT '建筑ID',
  `floor_id` bigint NULL DEFAULT NULL COMMENT '楼层ID',
  `room_id` bigint NULL DEFAULT NULL COMMENT '房间/区域ID',
  `local_x` decimal(12,4) NULL DEFAULT NULL COMMENT '室内X坐标(米)',
  `local_y` decimal(12,4) NULL DEFAULT NULL COMMENT '室内Y坐标(米)',
  `local_z` decimal(12,4) NULL DEFAULT NULL COMMENT '室内Z坐标(米)',
  `install_location` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '安装位置描述',
  `install_height_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '安装高度类型',
  `config` json NULL COMMENT '设备类型配置 JSON（DeviceConfig）',
  `job_config` varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '定时任务配置 JSON',
  `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`device_id`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE,
  KEY `idx_state` (`state`) USING BTREE,
  KEY `idx_gateway_id` (`gateway_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='IBMS 设备运行态';
