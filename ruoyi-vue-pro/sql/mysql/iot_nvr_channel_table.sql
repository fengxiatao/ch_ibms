-- =====================================================
-- IoT NVR 通道管理表
-- =====================================================
-- 
-- 设计说明：
-- 1. NVR 通道是 NVR 设备的逻辑通道，用于接入和管理摄像头
-- 2. 每个通道可以关联一个物理摄像头（通过 IP + 物理通道号）
-- 3. 支持通道的自定义配置、在线状态监控、流地址管理
-- 
-- 场景示例：
-- NVR (192.168.1.100) 的逻辑通道4 → 映射到摄像头 (192.168.1.206) 的物理通道1
-- =====================================================

CREATE TABLE IF NOT EXISTS `iot_nvr_channel` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '通道ID',
  
  -- ========== NVR 关联信息 ==========
  `nvr_id` bigint(20) NOT NULL COMMENT 'NVR设备ID（关联 iot_device.id，product_id=4）',
  `nvr_ip` varchar(64) DEFAULT NULL COMMENT 'NVR IP地址（冗余字段，便于查询）',
  `logical_channel_no` int(11) NOT NULL COMMENT 'NVR逻辑通道号（从1开始，如：通道1、通道2...）',
  
  -- ========== 摄像头关联信息 ==========
  `camera_device_id` bigint(20) DEFAULT NULL COMMENT '关联的摄像头设备ID（iot_device.id，product_id=3）',
  `camera_ip` varchar(64) DEFAULT NULL COMMENT '摄像头IP地址（如：192.168.1.206）',
  `physical_channel_no` int(11) DEFAULT 1 COMMENT '摄像头物理通道号（从1开始，默认为1）',
  
  -- ========== 通道基本信息 ==========
  `channel_name` varchar(128) DEFAULT NULL COMMENT '通道名称（如：前台全景相机）',
  `channel_code` varchar(64) DEFAULT NULL COMMENT '通道编码（如：CH-001）',
  `location` varchar(256) DEFAULT NULL COMMENT '安装位置（如：A栋1层大厅）',
  `building_id` bigint(20) DEFAULT NULL COMMENT '所属建筑ID',
  `floor_id` bigint(20) DEFAULT NULL COMMENT '所属楼层ID',
  `area_id` bigint(20) DEFAULT NULL COMMENT '所属区域ID',
  
  -- ========== 设备信息 ==========
  `device_type` varchar(32) DEFAULT 'IPC' COMMENT '设备类型（IPC:网络摄像机, DVR:硬盘录像机, NVR:网络录像机）',
  `manufacturer` varchar(64) DEFAULT NULL COMMENT '厂商（如：Dahua、Hikvision）',
  `model` varchar(128) DEFAULT NULL COMMENT '型号（如：IPC-HFW5241E）',
  `serial_number` varchar(128) DEFAULT NULL COMMENT '序列号',
  
  -- ========== 连接信息 ==========
  `protocol` varchar(32) DEFAULT 'ONVIF' COMMENT '协议类型（ONVIF、RTSP、GB28181）',
  `rtsp_port` int(11) DEFAULT 554 COMMENT 'RTSP端口',
  `http_port` int(11) DEFAULT 80 COMMENT 'HTTP端口',
  `tcp_port` int(11) DEFAULT 37777 COMMENT 'SDK TCP端口（大华：37777，海康：8000）',
  `username` varchar(64) DEFAULT 'admin' COMMENT '登录用户名',
  `password` varchar(256) DEFAULT NULL COMMENT '登录密码（加密存储）',
  
  -- ========== 流地址信息 ==========
  `stream_url_main` varchar(512) DEFAULT NULL COMMENT '主码流地址（高清）',
  `stream_url_sub` varchar(512) DEFAULT NULL COMMENT '子码流地址（标清）',
  `snapshot_url` varchar(512) DEFAULT NULL COMMENT '快照地址',
  
  -- ========== 能力信息 ==========
  `ptz_support` tinyint(1) DEFAULT 0 COMMENT '是否支持云台控制（0:否 1:是）',
  `audio_support` tinyint(1) DEFAULT 0 COMMENT '是否支持音频（0:否 1:是）',
  `resolution` varchar(32) DEFAULT '1920x1080' COMMENT '分辨率',
  `frame_rate` int(11) DEFAULT 25 COMMENT '帧率(FPS)',
  `bit_rate` int(11) DEFAULT 2048 COMMENT '码率(Kbps)',
  
  -- ========== 状态信息 ==========
  `online_status` tinyint(4) DEFAULT 0 COMMENT '在线状态（0:离线 1:在线 2:故障）',
  `enable_status` tinyint(4) DEFAULT 1 COMMENT '启用状态（0:禁用 1:启用）',
  `last_online_time` datetime DEFAULT NULL COMMENT '最后在线时间',
  `last_sync_time` datetime DEFAULT NULL COMMENT '最后同步时间（从NVR同步通道信息）',
  
  -- ========== 扩展配置 ==========
  `config` text DEFAULT NULL COMMENT '扩展配置（JSON格式，存储其他自定义配置）',
  `description` varchar(512) DEFAULT NULL COMMENT '描述',
  `sort` int(11) DEFAULT 0 COMMENT '排序',
  
  -- ========== 系统字段 ==========
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '租户编号',
  
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_nvr_logical_channel` (`nvr_id`, `logical_channel_no`, `deleted`, `tenant_id`),
  KEY `idx_camera_device_id` (`camera_device_id`),
  KEY `idx_camera_ip` (`camera_ip`),
  KEY `idx_nvr_ip` (`nvr_ip`),
  KEY `idx_online_status` (`online_status`),
  KEY `idx_enable_status` (`enable_status`),
  KEY `idx_building_id` (`building_id`),
  KEY `idx_floor_id` (`floor_id`),
  KEY `idx_area_id` (`area_id`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='IoT NVR通道表';

-- =====================================================
-- 示例数据：NVR 通道4 映射到 192.168.1.206 的物理通道1
-- =====================================================

-- 假设：
-- - NVR 设备ID = 100，IP = 192.168.1.100
-- - 摄像头设备ID = 206，IP = 192.168.1.206

INSERT INTO `iot_nvr_channel` (
  `nvr_id`,
  `nvr_ip`,
  `logical_channel_no`,
  `camera_device_id`,
  `camera_ip`,
  `physical_channel_no`,
  `channel_name`,
  `channel_code`,
  `location`,
  `device_type`,
  `manufacturer`,
  `model`,
  `protocol`,
  `rtsp_port`,
  `http_port`,
  `tcp_port`,
  `username`,
  `password`,
  `stream_url_main`,
  `stream_url_sub`,
  `snapshot_url`,
  `ptz_support`,
  `audio_support`,
  `resolution`,
  `frame_rate`,
  `bit_rate`,
  `online_status`,
  `enable_status`,
  `last_online_time`,
  `last_sync_time`,
  `sort`,
  `tenant_id`
) VALUES (
  100,                                    -- nvr_id: NVR设备ID
  '192.168.1.100',                        -- nvr_ip: NVR IP地址
  4,                                      -- logical_channel_no: NVR逻辑通道4
  206,                                    -- camera_device_id: 摄像头设备ID
  '192.168.1.206',                        -- camera_ip: 摄像头IP
  1,                                      -- physical_channel_no: 摄像头物理通道1
  '前台全景相机',                          -- channel_name
  'CH-004',                               -- channel_code
  'A栋1层前台大厅',                        -- location
  'IPC',                                  -- device_type
  'Dahua',                                -- manufacturer
  'IPC-HFW5241E-Z',                       -- model
  'ONVIF',                                -- protocol
  554,                                    -- rtsp_port
  80,                                     -- http_port
  37777,                                  -- tcp_port
  'admin',                                -- username
  'encrypted_password_here',              -- password (应该加密)
  'rtsp://admin:admin123@192.168.1.206:554/cam/realmonitor?channel=1&subtype=0',  -- stream_url_main
  'rtsp://admin:admin123@192.168.1.206:554/cam/realmonitor?channel=1&subtype=1',  -- stream_url_sub
  'http://admin:admin123@192.168.1.206/cgi-bin/snapshot.cgi?channel=1',           -- snapshot_url
  1,                                      -- ptz_support: 支持云台
  1,                                      -- audio_support: 支持音频
  '1920x1080',                            -- resolution
  25,                                     -- frame_rate
  4096,                                   -- bit_rate
  1,                                      -- online_status: 在线
  1,                                      -- enable_status: 启用
  NOW(),                                  -- last_online_time
  NOW(),                                  -- last_sync_time
  4,                                      -- sort
  1                                       -- tenant_id
);

-- =====================================================
-- 更多示例：同一个NVR的其他通道
-- =====================================================

-- 通道1：前门摄像头
INSERT INTO `iot_nvr_channel` (
  `nvr_id`, `nvr_ip`, `logical_channel_no`, `camera_ip`, `physical_channel_no`,
  `channel_name`, `channel_code`, `location`, `online_status`, `enable_status`, `sort`, `tenant_id`
) VALUES (
  100, '192.168.1.100', 1, '192.168.1.201', 1,
  '前门摄像头', 'CH-001', 'A栋1层前门', 1, 1, 1, 1
);

-- 通道2：后门摄像头
INSERT INTO `iot_nvr_channel` (
  `nvr_id`, `nvr_ip`, `logical_channel_no`, `camera_ip`, `physical_channel_no`,
  `channel_name`, `channel_code`, `location`, `online_status`, `enable_status`, `sort`, `tenant_id`
) VALUES (
  100, '192.168.1.100', 2, '192.168.1.202', 1,
  '后门摄像头', 'CH-002', 'A栋1层后门', 1, 1, 2, 1
);

-- 通道3：停车场摄像头
INSERT INTO `iot_nvr_channel` (
  `nvr_id`, `nvr_ip`, `logical_channel_no`, `camera_ip`, `physical_channel_no`,
  `channel_name`, `channel_code`, `location`, `online_status`, `enable_status`, `sort`, `tenant_id`
) VALUES (
  100, '192.168.1.100', 3, '192.168.1.203', 1,
  '停车场入口', 'CH-003', 'A栋地下停车场', 1, 1, 3, 1
);

-- =====================================================
-- 特殊场景：一个摄像头有多个物理通道（全景+细节）
-- =====================================================

-- 192.168.1.206 的物理通道1：全景视角
INSERT INTO `iot_nvr_channel` (
  `nvr_id`, `nvr_ip`, `logical_channel_no`, `camera_ip`, `physical_channel_no`,
  `channel_name`, `channel_code`, `location`, `description`, `sort`, `tenant_id`
) VALUES (
  100, '192.168.1.100', 4, '192.168.1.206', 1,
  '前台全景相机', 'CH-004', 'A栋1层前台', '广角镜头，覆盖整个前台区域', 4, 1
);

-- 192.168.1.206 的物理通道2：细节视角
INSERT INTO `iot_nvr_channel` (
  `nvr_id`, `nvr_ip`, `logical_channel_no`, `camera_ip`, `physical_channel_no`,
  `channel_name`, `channel_code`, `location`, `description`, `sort`, `tenant_id`
) VALUES (
  100, '192.168.1.100', 5, '192.168.1.206', 2,
  '前台细节相机', 'CH-005', 'A栋1层前台', '长焦镜头，捕捉人脸细节', 5, 1
);

-- =====================================================
-- 查询示例
-- =====================================================

-- 1. 查询某个NVR的所有通道
-- SELECT * FROM iot_nvr_channel WHERE nvr_id = 100 AND deleted = 0 ORDER BY logical_channel_no;

-- 2. 查询某个摄像头IP的所有通道
-- SELECT * FROM iot_nvr_channel WHERE camera_ip = '192.168.1.206' AND deleted = 0;

-- 3. 查询在线的通道
-- SELECT * FROM iot_nvr_channel WHERE online_status = 1 AND deleted = 0;

-- 4. 查询支持云台的通道
-- SELECT * FROM iot_nvr_channel WHERE ptz_support = 1 AND deleted = 0;

-- 5. 查询某个位置的通道
-- SELECT * FROM iot_nvr_channel WHERE location LIKE '%前台%' AND deleted = 0;
