-- =============================================
-- 科鼎固件升级端到端测试数据
-- 用于 E2E 测试验证完整升级流程
-- =============================================

-- 设置租户ID（默认租户）
SET @tenant_id = 1;

-- =============================================
-- 1. E2E 测试设备数据
-- station_code: TEST001, TEST002, TEST003
-- =============================================

-- 清理已有 E2E 测试数据（避免重复插入）
DELETE FROM iot_keding_upgrade_log WHERE task_id IN (
    SELECT id FROM iot_keding_upgrade_task WHERE station_code IN ('TEST001', 'TEST002', 'TEST003')
);
DELETE FROM iot_keding_upgrade_task WHERE station_code IN ('TEST001', 'TEST002', 'TEST003');
DELETE FROM iot_keding_firmware WHERE version = '1.0.0-test';
DELETE FROM iot_keding_device WHERE station_code IN ('TEST001', 'TEST002', 'TEST003');

-- E2E 测试设备 1: 测控一体化闸门 (device_type=1)
INSERT INTO iot_keding_device (
    station_code, device_name, device_type, 
    province_code, management_code, station_code_part, 
    pile_front, pile_back, manufacturer, sequence_no,
    tea_key, password, status, tenant_id, creator, updater, create_time, update_time
) VALUES (
    'TEST001', 'E2E测试设备-1号', 1,
    '32', '01', '01',
    '001', '000', '01', '01',
    '[305419896, 2596069104, 305419896, 2596069104]', '123456', 
    1, -- 在线状态
    @tenant_id, 'admin', 'admin', NOW(), NOW()
);

-- E2E 测试设备 2: 测控分体式闸门 (device_type=2)
INSERT INTO iot_keding_device (
    station_code, device_name, device_type, 
    province_code, management_code, station_code_part, 
    pile_front, pile_back, manufacturer, sequence_no,
    tea_key, password, status, tenant_id, creator, updater, create_time, update_time
) VALUES (
    'TEST002', 'E2E测试设备-2号', 2,
    '32', '01', '01',
    '002', '000', '01', '02',
    '[305419896, 2596069104, 305419896, 2596069104]', '123456', 
    1, -- 在线状态
    @tenant_id, 'admin', 'admin', NOW(), NOW()
);

-- E2E 测试设备 3: 水位计 (device_type=6) - 用于批量升级测试
INSERT INTO iot_keding_device (
    station_code, device_name, device_type, 
    province_code, management_code, station_code_part, 
    pile_front, pile_back, manufacturer, sequence_no,
    tea_key, password, status, tenant_id, creator, updater, create_time, update_time
) VALUES (
    'TEST003', 'E2E测试设备-3号', 6,
    '32', '01', '01',
    '003', '000', '01', '03',
    '[305419896, 2596069104, 305419896, 2596069104]', '123456', 
    1, -- 在线状态
    @tenant_id, 'admin', 'admin', NOW(), NOW()
);

-- =============================================
-- 2. E2E 测试固件数据
-- version: 1.0.0-test
-- =============================================

-- E2E 测试固件 - 通用测试固件
INSERT INTO iot_keding_firmware (
    name, version, device_type, file_path, file_size, file_md5, description,
    tenant_id, creator, updater, create_time, update_time
) VALUES (
    'E2E测试固件', '1.0.0-test', 1, 
    '/firmware/e2e_test_v1.0.0.bin', 
    10240,  -- 10KB 测试固件
    'd41d8cd98f00b204e9800998ecf8427e',  -- 空文件的 MD5
    'E2E 端到端测试专用固件',
    @tenant_id, 'admin', 'admin', NOW(), NOW()
);

-- =============================================
-- 3. 查询验证
-- =============================================
SELECT '=== E2E 测试设备列表 ===' AS info;
SELECT id, station_code, device_name, device_type, status 
FROM iot_keding_device 
WHERE station_code IN ('TEST001', 'TEST002', 'TEST003')
ORDER BY station_code;

SELECT '=== E2E 测试固件列表 ===' AS info;
SELECT id, name, version, device_type, file_size, file_md5
FROM iot_keding_firmware 
WHERE version = '1.0.0-test';

-- =============================================
-- E2E 测试说明
-- =============================================
/*
E2E 测试数据说明：

1. 测试设备：
   - TEST001: 测控一体化闸门，在线状态，用于单设备升级测试
   - TEST002: 测控分体式闸门，在线状态，用于单设备升级测试
   - TEST003: 水位计，在线状态，用于批量升级测试

2. 测试固件：
   - 版本: 1.0.0-test
   - 大小: 10KB（便于快速测试）
   - 适用设备类型: 1（闸门类）

3. 模拟器配置：
   - SUCCESS 模式: 接受所有升级命令
   - REJECT 模式: 拒绝升级触发命令
   - FRAME_FAIL 模式: 在指定帧返回失败

4. 测试场景：
   - 单设备升级成功流程
   - 单设备升级失败流程（设备拒绝）
   - 单设备升级失败流程（帧传输失败）
   - 批量升级流程
   - 升级任务取消流程
*/
