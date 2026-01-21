-- =====================================================
-- IBMS 设备通道统一管理表
-- =====================================================
-- 
-- 设计理念：
-- 1. 通道是IBMS系统中的核心抽象概念，代表一个可访问的数据流或控制端点
-- 2. 不同子系统的设备都可能有多个通道：
--    - 视频监控：NVR/DVR的视频通道
--    - 门禁系统：门禁控制器的门点通道
--    - 消防系统：消防主机的探测器通道
--    - 能源管理：电表的多路回路通道
--    - 广播系统：广播主机的分区通道
-- 3. 统一的通道管理可以实现：
--    - 视频巡更：基于通道列表进行巡更
--    - 多屏预览：选择多个通道进行组合显示
--    - 录像回放：针对通道进行录像查询和回放
--    - 联动控制：通道之间的联动（如门禁触发视频录像）
-- 
-- =====================================================

CREATE TABLE IF NOT EXISTS `iot_device_channel` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '通道ID',
  
  -- ========== 设备关联信息 ==========
  `device_id` bigint(20) NOT NULL COMMENT '所属设备ID（关联 iot_device.id）',
  `device_type` varchar(32) NOT NULL COMMENT '设备类型（NVR、DVR、ACCESS_CONTROLLER、FIRE_PANEL、METER、BROADCAST）',
  `product_id` bigint(20) DEFAULT NULL COMMENT '产品ID（关联 iot_product.id）',
  
  -- ========== 通道基本信息 ==========
  `channel_no` int(11) NOT NULL COMMENT '通道号（从1开始，设备上的物理通道号）',
  `channel_name` varchar(128) NOT NULL COMMENT '通道名称（如：前台全景相机、前门读卡器、1号烟感）',
  `channel_code` varchar(64) DEFAULT NULL COMMENT '通道编码（如：CH-VIDEO-001、CH-ACCESS-001）',
  `channel_type` varchar(32) DEFAULT NULL COMMENT '通道类型（VIDEO、AUDIO、ACCESS、FIRE、ENERGY、BROADCAST）',
  `channel_sub_type` varchar(32) DEFAULT NULL COMMENT '通道子类型（如视频：IPC、PTZ；门禁：CARD_READER、FINGERPRINT）',
  
  -- ========== 位置信息 ==========
  `location` varchar(256) DEFAULT NULL COMMENT '安装位置（如：A栋1层前台大厅）',
  `building_id` bigint(20) DEFAULT NULL COMMENT '所属建筑ID',
  `floor_id` bigint(20) DEFAULT NULL COMMENT '所属楼层ID',
  `area_id` bigint(20) DEFAULT NULL COMMENT '所属区域ID',
  `space_id` bigint(20) DEFAULT NULL COMMENT '所属空间ID',
  
  -- ========== 目标设备信息（用于通道映射） ==========
  `target_device_id` bigint(20) DEFAULT NULL COMMENT '目标设备ID（如NVR通道关联的摄像头设备ID）',
  `target_ip` varchar(64) DEFAULT NULL COMMENT '目标设备IP（如摄像头IP：192.168.1.206）',
  `target_port` int(11) DEFAULT NULL COMMENT '目标设备端口',
  `target_channel_no` int(11) DEFAULT NULL COMMENT '目标设备通道号（如摄像头的物理通道号）',
  
  -- ========== 连接信息 ==========
  `protocol` varchar(32) DEFAULT NULL COMMENT '协议类型（ONVIF、RTSP、GB28181、Wiegand、Modbus）',
  `username` varchar(64) DEFAULT NULL COMMENT '登录用户名',
  `password` varchar(256) DEFAULT NULL COMMENT '登录密码（加密存储）',
  
  -- ========== 视频通道专用字段 ==========
  `stream_url_main` varchar(512) DEFAULT NULL COMMENT '主码流地址（视频通道）',
  `stream_url_sub` varchar(512) DEFAULT NULL COMMENT '子码流地址（视频通道）',
  `snapshot_url` varchar(512) DEFAULT NULL COMMENT '快照地址（视频通道）',
  `ptz_support` tinyint(1) DEFAULT 0 COMMENT '是否支持云台（视频通道）',
  `audio_support` tinyint(1) DEFAULT 0 COMMENT '是否支持音频（视频通道）',
  `resolution` varchar(32) DEFAULT NULL COMMENT '分辨率（视频通道）',
  `frame_rate` int(11) DEFAULT NULL COMMENT '帧率FPS（视频通道）',
  `bit_rate` int(11) DEFAULT NULL COMMENT '码率Kbps（视频通道）',
  
  -- ========== 门禁通道专用字段 ==========
  `door_name` varchar(128) DEFAULT NULL COMMENT '门点名称（门禁通道）',
  `door_direction` varchar(16) DEFAULT NULL COMMENT '门方向（IN、OUT、BOTH）',
  `card_reader_type` varchar(32) DEFAULT NULL COMMENT '读卡器类型（IC、ID、FINGERPRINT、FACE）',
  `lock_type` varchar(32) DEFAULT NULL COMMENT '锁类型（ELECTRIC、MAGNETIC、MOTOR）',
  
  -- ========== 消防通道专用字段 ==========
  `detector_type` varchar(32) DEFAULT NULL COMMENT '探测器类型（SMOKE、HEAT、GAS、MANUAL）',
  `alarm_level` int(11) DEFAULT NULL COMMENT '报警级别（1-5）',
  
  -- ========== 能源通道专用字段 ==========
  `meter_type` varchar(32) DEFAULT NULL COMMENT '表计类型（ELECTRIC、WATER、GAS、HEAT）',
  `circuit_name` varchar(128) DEFAULT NULL COMMENT '回路名称（电表通道）',
  `measurement_unit` varchar(16) DEFAULT NULL COMMENT '计量单位（kWh、m³、L）',
  
  -- ========== 能力信息 ==========
  `capabilities` text DEFAULT NULL COMMENT '通道能力（JSON格式，存储通道支持的功能列表）',
  
  -- ========== 状态信息 ==========
  `online_status` tinyint(4) DEFAULT 0 COMMENT '在线状态（0:离线 1:在线 2:故障 3:未知）',
  `enable_status` tinyint(4) DEFAULT 1 COMMENT '启用状态（0:禁用 1:启用）',
  `alarm_status` tinyint(4) DEFAULT 0 COMMENT '报警状态（0:正常 1:报警 2:故障）',
  `last_online_time` datetime DEFAULT NULL COMMENT '最后在线时间',
  `last_sync_time` datetime DEFAULT NULL COMMENT '最后同步时间',
  
  -- ========== 业务配置 ==========
  `is_recording` tinyint(1) DEFAULT 0 COMMENT '是否录像（视频通道）',
  `is_patrol` tinyint(1) DEFAULT 0 COMMENT '是否加入巡更',
  `is_monitor` tinyint(1) DEFAULT 0 COMMENT '是否加入监控墙',
  `patrol_duration` int(11) DEFAULT 30 COMMENT '巡更停留时长（秒）',
  `monitor_position` int(11) DEFAULT NULL COMMENT '监控墙位置（1-16）',
  
  -- ========== 扩展配置 ==========
  `config` text DEFAULT NULL COMMENT '扩展配置（JSON格式，存储其他自定义配置）',
  `description` varchar(512) DEFAULT NULL COMMENT '描述',
  `sort` int(11) DEFAULT 0 COMMENT '排序',
  `tags` varchar(256) DEFAULT NULL COMMENT '标签（逗号分隔，如：重点,24小时,高清）',
  
  -- ========== 系统字段 ==========
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '租户编号',
  
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_device_channel` (`device_id`, `channel_no`, `deleted`, `tenant_id`),
  KEY `idx_device_type` (`device_type`),
  KEY `idx_channel_type` (`channel_type`),
  KEY `idx_target_device_id` (`target_device_id`),
  KEY `idx_target_ip` (`target_ip`),
  KEY `idx_online_status` (`online_status`),
  KEY `idx_enable_status` (`enable_status`),
  KEY `idx_alarm_status` (`alarm_status`),
  KEY `idx_is_patrol` (`is_patrol`),
  KEY `idx_is_monitor` (`is_monitor`),
  KEY `idx_building_id` (`building_id`),
  KEY `idx_floor_id` (`floor_id`),
  KEY `idx_area_id` (`area_id`),
  KEY `idx_space_id` (`space_id`),
  KEY `idx_tenant_id` (`tenant_id`)
  
  -- ========== 外键约束（可选，根据需要启用） ==========
  -- 注意：由于使用了逻辑删除，外键约束可能会影响性能，建议在应用层保证数据一致性
  -- CONSTRAINT `fk_channel_device` FOREIGN KEY (`device_id`) REFERENCES `iot_device` (`id`) ON DELETE CASCADE,
  -- CONSTRAINT `fk_channel_target_device` FOREIGN KEY (`target_device_id`) REFERENCES `iot_device` (`id`) ON DELETE SET NULL
  
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='IoT设备通道统一管理表';

-- =====================================================
-- 关联关系说明
-- =====================================================
-- 
-- 1. 主设备关联（必填）：
--    device_id → iot_device.id
--    表示该通道属于哪个物理设备（如：NVR、门禁控制器、消防主机）
--
-- 2. 目标设备关联（可选）：
--    target_device_id → iot_device.id
--    表示该通道映射到哪个目标设备（如：NVR通道映射到摄像头）
--
-- 3. 虚拟通道 vs 物理通道：
--    - 虚拟通道：iot_device_channel 表中的记录（软件层面的抽象）
--    - 物理通道：实际硬件设备上的通道接口（硬件层面的实体）
--    - channel_no：主设备上的逻辑通道号
--    - target_channel_no：目标设备上的物理通道号
--
-- 4. 典型场景：
--    场景A：NVR通道
--      - device_id: NVR设备ID (如: 100)
--      - channel_no: NVR逻辑通道号 (如: 4)
--      - target_device_id: 摄像头设备ID (如: 206)
--      - target_channel_no: 摄像头物理通道号 (如: 1)
--
--    场景B：门禁控制器通道
--      - device_id: 门禁控制器ID (如: 200)
--      - channel_no: 控制器通道号 (如: 1)
--      - target_device_id: NULL (门禁通道通常不需要二级映射)
--
--    场景C：独立摄像头（直连）
--      - device_id: 摄像头设备ID (如: 206)
--      - channel_no: 1 (独立摄像头通常只有一个通道)
--      - target_device_id: NULL (自己就是目标设备)
--
-- =====================================================

-- =====================================================
-- 示例数据：视频监控通道
-- =====================================================

-- NVR设备（假设device_id=100）
-- NVR通道4 → 摄像头192.168.1.206的物理通道1
INSERT INTO `iot_device_channel` (
  `device_id`, `device_type`, `product_id`,
  `channel_no`, `channel_name`, `channel_code`, `channel_type`, `channel_sub_type`,
  `location`, `building_id`, `floor_id`, `area_id`,
  `target_device_id`, `target_ip`, `target_channel_no`,
  `protocol`, `username`, `password`,
  `stream_url_main`, `stream_url_sub`, `snapshot_url`,
  `ptz_support`, `audio_support`, `resolution`, `frame_rate`, `bit_rate`,
  `online_status`, `enable_status`,
  `is_recording`, `is_patrol`, `is_monitor`, `patrol_duration`, `monitor_position`,
  `tags`, `sort`, `tenant_id`
) VALUES (
  100, 'NVR', 4,                                                    -- 设备信息
  4, '前台全景相机', 'CH-VIDEO-004', 'VIDEO', 'IPC',                -- 通道基本信息
  'A栋1层前台大厅', 1, 1, 1,                                        -- 位置信息
  206, '192.168.1.206', 1,                                          -- 目标设备（摄像头）
  'ONVIF', 'admin', 'encrypted_password',                           -- 连接信息
  'rtsp://admin:admin123@192.168.1.206:554/cam/realmonitor?channel=1&subtype=0',  -- 主码流
  'rtsp://admin:admin123@192.168.1.206:554/cam/realmonitor?channel=1&subtype=1',  -- 子码流
  'http://admin:admin123@192.168.1.206/cgi-bin/snapshot.cgi?channel=1',           -- 快照
  1, 1, '1920x1080', 25, 4096,                                      -- 视频参数
  1, 1,                                                             -- 状态
  1, 1, 1, 30, 1,                                                   -- 业务配置
  '重点,24小时,高清', 4, 1                                          -- 扩展信息
);

-- =====================================================
-- 示例数据：门禁通道
-- =====================================================

-- 门禁控制器设备（假设device_id=200）
-- 通道1：前门读卡器
INSERT INTO `iot_device_channel` (
  `device_id`, `device_type`, `product_id`,
  `channel_no`, `channel_name`, `channel_code`, `channel_type`, `channel_sub_type`,
  `location`, `building_id`, `floor_id`,
  `protocol`,
  `door_name`, `door_direction`, `card_reader_type`, `lock_type`,
  `online_status`, `enable_status`, `alarm_status`,
  `tags`, `sort`, `tenant_id`
) VALUES (
  200, 'ACCESS_CONTROLLER', 5,                                      -- 设备信息
  1, '前门读卡器', 'CH-ACCESS-001', 'ACCESS', 'CARD_READER',       -- 通道基本信息
  'A栋1层前门', 1, 1,                                               -- 位置信息
  'Wiegand',                                                        -- 协议
  '前门', 'IN', 'IC', 'ELECTRIC',                                   -- 门禁专用字段
  1, 1, 0,                                                          -- 状态
  '重点,常开', 1, 1                                                 -- 扩展信息
);

-- 通道2：后门读卡器
INSERT INTO `iot_device_channel` (
  `device_id`, `device_type`, `channel_no`, `channel_name`, `channel_code`, 
  `channel_type`, `channel_sub_type`, `location`,
  `door_name`, `door_direction`, `card_reader_type`, `lock_type`,
  `online_status`, `enable_status`, `sort`, `tenant_id`
) VALUES (
  200, 'ACCESS_CONTROLLER', 2, '后门读卡器', 'CH-ACCESS-002',
  'ACCESS', 'CARD_READER', 'A栋1层后门',
  '后门', 'OUT', 'IC', 'MAGNETIC',
  1, 1, 2, 1
);

-- =====================================================
-- 示例数据：消防通道
-- =====================================================

-- 消防主机设备（假设device_id=300）
-- 通道1：1号烟感探测器
INSERT INTO `iot_device_channel` (
  `device_id`, `device_type`, `product_id`,
  `channel_no`, `channel_name`, `channel_code`, `channel_type`, `channel_sub_type`,
  `location`, `building_id`, `floor_id`, `area_id`,
  `detector_type`, `alarm_level`,
  `online_status`, `enable_status`, `alarm_status`,
  `sort`, `tenant_id`
) VALUES (
  300, 'FIRE_PANEL', 6,                                             -- 设备信息
  1, '1号烟感', 'CH-FIRE-001', 'FIRE', 'SMOKE',                    -- 通道基本信息
  'A栋1层走廊', 1, 1, 1,                                            -- 位置信息
  'SMOKE', 3,                                                       -- 消防专用字段
  1, 1, 0,                                                          -- 状态
  1, 1                                                              -- 系统字段
);

-- =====================================================
-- 示例数据：能源通道
-- =====================================================

-- 电表设备（假设device_id=400）
-- 通道1：照明回路
INSERT INTO `iot_device_channel` (
  `device_id`, `device_type`, `product_id`,
  `channel_no`, `channel_name`, `channel_code`, `channel_type`,
  `location`, `building_id`, `floor_id`,
  `protocol`,
  `meter_type`, `circuit_name`, `measurement_unit`,
  `online_status`, `enable_status`,
  `sort`, `tenant_id`
) VALUES (
  400, 'METER', 7,                                                  -- 设备信息
  1, '1层照明回路', 'CH-ENERGY-001', 'ENERGY',                     -- 通道基本信息
  'A栋1层配电间', 1, 1,                                             -- 位置信息
  'Modbus',                                                         -- 协议
  'ELECTRIC', '照明回路1', 'kWh',                                   -- 能源专用字段
  1, 1,                                                             -- 状态
  1, 1                                                              -- 系统字段
);

-- =====================================================
-- 常用查询视图
-- =====================================================

-- 1. 视频通道视图（用于视频巡更、多屏预览）
CREATE OR REPLACE VIEW `v_video_channels` AS
SELECT 
  c.id AS channel_id,
  c.device_id,
  d.device_name AS device_name,
  d.ip AS device_ip,
  c.channel_no,
  c.channel_name,
  c.channel_code,
  c.location,
  c.target_ip AS camera_ip,
  c.target_channel_no AS camera_channel,
  c.stream_url_main,
  c.stream_url_sub,
  c.snapshot_url,
  c.ptz_support,
  c.audio_support,
  c.resolution,
  c.online_status,
  c.enable_status,
  c.is_recording,
  c.is_patrol,
  c.is_monitor,
  c.patrol_duration,
  c.monitor_position,
  c.tags,
  c.building_id,
  c.floor_id,
  c.area_id,
  c.tenant_id
FROM iot_device_channel c
LEFT JOIN iot_device d ON c.device_id = d.id
WHERE c.channel_type = 'VIDEO' 
  AND c.deleted = 0 
  AND d.deleted = 0;

-- 2. 门禁通道视图
CREATE OR REPLACE VIEW `v_access_channels` AS
SELECT 
  c.id AS channel_id,
  c.device_id,
  d.device_name AS controller_name,
  c.channel_no,
  c.channel_name,
  c.door_name,
  c.door_direction,
  c.card_reader_type,
  c.lock_type,
  c.location,
  c.online_status,
  c.enable_status,
  c.alarm_status,
  c.building_id,
  c.floor_id,
  c.tenant_id
FROM iot_device_channel c
LEFT JOIN iot_device d ON c.device_id = d.id
WHERE c.channel_type = 'ACCESS' 
  AND c.deleted = 0 
  AND d.deleted = 0;

-- 3. 消防通道视图
CREATE OR REPLACE VIEW `v_fire_channels` AS
SELECT 
  c.id AS channel_id,
  c.device_id,
  d.device_name AS panel_name,
  c.channel_no,
  c.channel_name,
  c.detector_type,
  c.alarm_level,
  c.location,
  c.online_status,
  c.enable_status,
  c.alarm_status,
  c.building_id,
  c.floor_id,
  c.area_id,
  c.tenant_id
FROM iot_device_channel c
LEFT JOIN iot_device d ON c.device_id = d.id
WHERE c.channel_type = 'FIRE' 
  AND c.deleted = 0 
  AND d.deleted = 0;

-- =====================================================
-- 常用查询SQL示例
-- =====================================================

-- 1. 查询所有在线的视频通道（用于视频巡更）
-- SELECT * FROM v_video_channels WHERE online_status = 1 AND enable_status = 1 AND is_patrol = 1 ORDER BY sort;

-- 2. 查询监控墙的视频通道（用于多屏预览）
-- SELECT * FROM v_video_channels WHERE is_monitor = 1 ORDER BY monitor_position;

-- 3. 查询某个NVR的所有通道
-- SELECT * FROM iot_device_channel WHERE device_id = 100 AND deleted = 0 ORDER BY channel_no;

-- 4. 查询某个位置的所有通道（跨系统）
-- SELECT channel_type, channel_name, location, online_status FROM iot_device_channel WHERE location LIKE '%前台%' AND deleted = 0;

-- 5. 查询支持云台的视频通道
-- SELECT * FROM v_video_channels WHERE ptz_support = 1;

-- 6. 查询报警状态的通道（跨系统）
-- SELECT device_type, channel_type, channel_name, location, alarm_status FROM iot_device_channel WHERE alarm_status > 0 AND deleted = 0;
