-- =============================================
-- 科鼎协议模拟测试数据
-- 用于模拟器调试，在现场联调前验证协议实现
-- =============================================

-- 设置租户ID（默认租户）
SET @tenant_id = 1;

-- =============================================
-- 1. 科鼎设备测试数据 - 覆盖所有9种设备类型
-- =============================================

-- 清理已有测试数据（可选，谨慎使用）
-- DELETE FROM iot_keding_device WHERE station_code LIKE 'TEST%';

-- 1.1 闸门类设备（5种）
-- 测控一体化闸门 (device_type=1)
INSERT INTO iot_keding_device (
    station_code, device_name, device_type, 
    province_code, management_code, station_code_part, 
    pile_front, pile_back, manufacturer, sequence_no,
    tea_key, password, status, tenant_id, creator, updater
) VALUES (
    'TEST01010101', '模拟一体化闸门-1号', 1,
    '32', '01', '01',
    '001', '000', '01', '01',
    '[305419896, 2596069104, 305419896, 2596069104]', '123456', 0, @tenant_id, 'admin', 'admin'
);

-- 测控分体式闸门 (device_type=2)
INSERT INTO iot_keding_device (
    station_code, device_name, device_type, 
    province_code, management_code, station_code_part, 
    pile_front, pile_back, manufacturer, sequence_no,
    tea_key, password, status, tenant_id, creator, updater
) VALUES (
    'TEST01010202', '模拟分体式闸门-1号', 2,
    '32', '01', '01',
    '002', '000', '01', '02',
    '[305419896, 2596069104, 305419896, 2596069104]', '123456', 0, @tenant_id, 'admin', 'admin'
);

-- 退水闸 (device_type=3)
INSERT INTO iot_keding_device (
    station_code, device_name, device_type, 
    province_code, management_code, station_code_part, 
    pile_front, pile_back, manufacturer, sequence_no,
    tea_key, password, status, tenant_id, creator, updater
) VALUES (
    'TEST01010303', '模拟退水闸-1号', 3,
    '32', '01', '01',
    '003', '000', '01', '03',
    '[305419896, 2596069104, 305419896, 2596069104]', '123456', 0, @tenant_id, 'admin', 'admin'
);

-- 节制闸 (device_type=4)
INSERT INTO iot_keding_device (
    station_code, device_name, device_type, 
    province_code, management_code, station_code_part, 
    pile_front, pile_back, manufacturer, sequence_no,
    tea_key, password, status, tenant_id, creator, updater
) VALUES (
    'TEST01010404', '模拟节制闸-1号', 4,
    '32', '01', '01',
    '004', '000', '01', '04',
    '[305419896, 2596069104, 305419896, 2596069104]', '123456', 0, @tenant_id, 'admin', 'admin'
);

-- 进水闸 (device_type=5)
INSERT INTO iot_keding_device (
    station_code, device_name, device_type, 
    province_code, management_code, station_code_part, 
    pile_front, pile_back, manufacturer, sequence_no,
    tea_key, password, status, tenant_id, creator, updater
) VALUES (
    'TEST01010505', '模拟进水闸-1号', 5,
    '32', '01', '01',
    '005', '000', '01', '05',
    '[305419896, 2596069104, 305419896, 2596069104]', '123456', 0, @tenant_id, 'admin', 'admin'
);

-- 1.2 仪表类设备（4种）
-- 水位计 (device_type=6)
INSERT INTO iot_keding_device (
    station_code, device_name, device_type, 
    province_code, management_code, station_code_part, 
    pile_front, pile_back, manufacturer, sequence_no,
    tea_key, password, status, tenant_id, creator, updater
) VALUES (
    'TEST01010606', '模拟水位计-1号', 6,
    '32', '01', '01',
    '006', '000', '01', '06',
    '[305419896, 2596069104, 305419896, 2596069104]', '123456', 0, @tenant_id, 'admin', 'admin'
);

-- 流量计 (device_type=7)
INSERT INTO iot_keding_device (
    station_code, device_name, device_type, 
    province_code, management_code, station_code_part, 
    pile_front, pile_back, manufacturer, sequence_no,
    tea_key, password, status, tenant_id, creator, updater
) VALUES (
    'TEST01010707', '模拟流量计-1号', 7,
    '32', '01', '01',
    '007', '000', '01', '07',
    '[305419896, 2596069104, 305419896, 2596069104]', '123456', 0, @tenant_id, 'admin', 'admin'
);

-- 流速仪 (device_type=8)
INSERT INTO iot_keding_device (
    station_code, device_name, device_type, 
    province_code, management_code, station_code_part, 
    pile_front, pile_back, manufacturer, sequence_no,
    tea_key, password, status, tenant_id, creator, updater
) VALUES (
    'TEST01010808', '模拟流速仪-1号', 8,
    '32', '01', '01',
    '008', '000', '01', '08',
    '[305419896, 2596069104, 305419896, 2596069104]', '123456', 0, @tenant_id, 'admin', 'admin'
);

-- 渗压计 (device_type=9)
INSERT INTO iot_keding_device (
    station_code, device_name, device_type, 
    province_code, management_code, station_code_part, 
    pile_front, pile_back, manufacturer, sequence_no,
    tea_key, password, status, tenant_id, creator, updater
) VALUES (
    'TEST01010909', '模拟渗压计-1号', 9,
    '32', '01', '01',
    '009', '000', '01', '09',
    '[305419896, 2596069104, 305419896, 2596069104]', '123456', 0, @tenant_id, 'admin', 'admin'
);

-- =============================================
-- 2. 测试固件数据（用于升级测试）
-- =============================================
INSERT INTO iot_keding_firmware (
    name, version, device_type, file_path, file_size, file_md5, description,
    tenant_id, creator, updater
) VALUES 
('闸门控制器固件', 'V1.0.0', 1, '/firmware/gate_v1.0.0.bin', 102400, 'abc123def456', '测试固件-闸门类设备通用', @tenant_id, 'admin', 'admin'),
('水位计固件', 'V1.0.0', 6, '/firmware/water_level_v1.0.0.bin', 81920, 'def456abc123', '测试固件-水位计专用', @tenant_id, 'admin', 'admin'),
('流量计固件', 'V1.0.0', 7, '/firmware/flow_meter_v1.0.0.bin', 81920, 'ghi789jkl012', '测试固件-流量计专用', @tenant_id, 'admin', 'admin');

-- =============================================
-- 3. 查询验证
-- =============================================
SELECT '=== 科鼎测试设备列表 ===' AS info;
SELECT id, station_code, device_name, device_type, status 
FROM iot_keding_device 
WHERE station_code LIKE 'TEST%' 
ORDER BY device_type;

SELECT '=== 测试固件列表 ===' AS info;
SELECT id, name, version, device_type, file_size 
FROM iot_keding_firmware 
WHERE tenant_id = @tenant_id;

-- =============================================
-- 模拟器启动命令参考
-- =============================================
/*
闸门类设备测试命令：

1. 测控一体化闸门 (默认)
   --protocol keding --station-code TEST01010101 --device-type INTEGRATED_GATE --mode success

2. 测控分体式闸门
   --protocol keding --station-code TEST01010202 --device-type SEPARATE_GATE --mode success

3. 退水闸
   --protocol keding --station-code TEST01010303 --device-type DRAIN_GATE --mode success

4. 节制闸
   --protocol keding --station-code TEST01010404 --device-type CONTROL_GATE --mode success

5. 进水闸
   --protocol keding --station-code TEST01010505 --device-type INLET_GATE --mode success

仪表类设备测试命令：

6. 水位计
   --protocol keding --station-code TEST01010606 --device-type WATER_LEVEL_METER --mode success --report-interval 30

7. 流量计
   --protocol keding --station-code TEST01010707 --device-type FLOW_METER --mode success --report-interval 30

8. 流速仪
   --protocol keding --station-code TEST01010808 --device-type VELOCITY_METER --mode success --report-interval 30

9. 渗压计
   --protocol keding --station-code TEST01010909 --device-type SEEPAGE_METER --mode success --report-interval 30

异常模式测试：

-- 超时模式（2秒延迟）
   --protocol keding --station-code TEST01010101 --mode timeout --delay 2000

-- 帧失败模式（第5、10帧失败）
   --protocol keding --station-code TEST01010101 --mode frame_fail --fail-frames 5,10

-- 拒绝模式
   --protocol keding --station-code TEST01010101 --mode reject
*/
