-- =====================================================
-- 德通设备数据迁移脚本
-- 版本: V1.1
-- 描述: 将 iot_detong_device 表数据迁移到统一的 iot_device 表
-- 作者: IoT Platform Team
-- 日期: 2024-12-11
-- =====================================================

-- 设置事务隔离级别
SET SESSION TRANSACTION ISOLATION LEVEL READ COMMITTED;

-- 开始事务
START TRANSACTION;

-- =====================================================
-- 第一步: 数据迁移前检查
-- =====================================================

-- 检查源表是否存在
SELECT '检查源表 iot_detong_device...' as step;
SELECT COUNT(*) as detong_device_count FROM iot_detong_device WHERE deleted = 0;

-- 检查目标表是否存在
SELECT '检查目标表 iot_device...' as step;
SELECT COUNT(*) as existing_device_count FROM iot_device WHERE device_type = 2;

-- =====================================================
-- 第二步: 执行数据迁移
-- =====================================================

SELECT '开始迁移德通设备数据...' as step;

INSERT INTO iot_device (
    id,
    device_name,
    nickname,
    serial_number,
    pic_url,
    group_ids,
    product_id,
    product_key,
    device_key,
    device_type,
    subsystem_code,
    gateway_id,
    state,
    online_time,
    offline_time,
    active_time,
    ip,
    firmware_id,
    device_secret,
    auth_type,
    location_type,
    latitude,
    longitude,
    area_id,
    address,
    campus_id,
    building_id,
    floor_id,
    room_id,
    local_x,
    local_y,
    local_z,
    config,
    job_config,
    tenant_id,
    creator,
    create_time,
    updater,
    update_time,
    deleted
)
SELECT 
    -- 基础字段
    dd.id,
    dd.device_name,
    dd.device_name as nickname,
    dd.station_code as serial_number,
    NULL as pic_url,
    NULL as group_ids,
    
    -- 产品相关字段
    1 as product_id,  -- 假设德通设备的产品ID为1,实际应根据业务调整
    'DETONG' as product_key,
    CONCAT('DETONG_', dd.station_code) as device_key,
    2 as device_type,  -- 德通设备类型编号为2
    'DETONG' as subsystem_code,
    
    -- 网关和状态字段
    NULL as gateway_id,
    CASE dd.status
        WHEN 1 THEN 1  -- 在线
        WHEN 0 THEN 0  -- 离线
        ELSE 2         -- 未知
    END as state,
    dd.last_heartbeat as online_time,
    NULL as offline_time,
    dd.create_time as active_time,
    
    -- 网络相关字段
    NULL as ip,
    NULL as firmware_id,
    dd.password as device_secret,
    'PASSWORD' as auth_type,
    
    -- 位置信息字段(德通设备暂不使用)
    NULL as location_type,
    NULL as latitude,
    NULL as longitude,
    NULL as area_id,
    NULL as address,
    NULL as campus_id,
    NULL as building_id,
    NULL as floor_id,
    NULL as room_id,
    NULL as local_x,
    NULL as local_y,
    NULL as local_z,
    
    -- 核心配置字段: 将德通特有字段转换为JSON
    JSON_OBJECT(
        'deviceType', 'DETONG',
        'stationCode', IFNULL(dd.station_code, ''),
        'deviceTypeCode', IFNULL(dd.device_type, 0),
        'provinceCode', IFNULL(dd.province_code, ''),
        'managementCode', IFNULL(dd.management_code, ''),
        'stationCodePart', IFNULL(dd.station_code_part, ''),
        'pileFront', IFNULL(dd.pile_front, ''),
        'pileBack', IFNULL(dd.pile_back, ''),
        'manufacturer', IFNULL(dd.manufacturer, ''),
        'sequenceNo', IFNULL(dd.sequence_no, ''),
        'teaKey', IFNULL(dd.tea_key, ''),
        'password', IFNULL(dd.password, ''),
        'lastHeartbeat', IFNULL(DATE_FORMAT(dd.last_heartbeat, '%Y-%m-%dT%H:%i:%s'), NULL)
    ) as config,
    
    NULL as job_config,
    
    -- 租户和审计字段
    dd.tenant_id,
    dd.creator,
    dd.create_time,
    dd.updater,
    dd.update_time,
    dd.deleted
    
FROM iot_detong_device dd
WHERE NOT EXISTS (
    SELECT 1 FROM iot_device d WHERE d.id = dd.id
)
AND dd.deleted = 0;

-- 获取迁移结果
SELECT '迁移完成!' as step;
SELECT ROW_COUNT() as migrated_count;

-- =====================================================
-- 第三步: 数据验证
-- =====================================================

SELECT '执行数据验证...' as step;

-- 验证数据数量
SELECT 
    '数据数量验证' as validation_type,
    (SELECT COUNT(*) FROM iot_detong_device WHERE deleted = 0) as source_count,
    (SELECT COUNT(*) FROM iot_device WHERE device_type = 2 AND deleted = 0) as target_count,
    CASE 
        WHEN (SELECT COUNT(*) FROM iot_detong_device WHERE deleted = 0) = 
             (SELECT COUNT(*) FROM iot_device WHERE device_type = 2 AND deleted = 0)
        THEN '✓ 通过'
        ELSE '✗ 失败'
    END as result;

-- 验证关键字段
SELECT 
    '关键字段验证' as validation_type,
    COUNT(*) as total_records,
    SUM(CASE 
        WHEN dd.device_name = d.device_name 
        AND dd.station_code = d.serial_number
        AND dd.tenant_id = d.tenant_id
        THEN 1 ELSE 0 
    END) as matched_records,
    CASE 
        WHEN COUNT(*) = SUM(CASE 
            WHEN dd.device_name = d.device_name 
            AND dd.station_code = d.serial_number
            AND dd.tenant_id = d.tenant_id
            THEN 1 ELSE 0 
        END)
        THEN '✓ 通过'
        ELSE '✗ 失败'
    END as result
FROM iot_detong_device dd
JOIN iot_device d ON dd.id = d.id
WHERE dd.deleted = 0 AND d.deleted = 0;

-- 验证JSON配置字段
SELECT 
    'JSON配置验证' as validation_type,
    COUNT(*) as total_records,
    SUM(CASE 
        WHEN dd.station_code = JSON_UNQUOTE(JSON_EXTRACT(d.config, '$.stationCode'))
        OR (dd.station_code IS NULL AND JSON_UNQUOTE(JSON_EXTRACT(d.config, '$.stationCode')) = '')
        THEN 1 ELSE 0 
    END) as station_code_matched,
    SUM(CASE 
        WHEN dd.device_type = JSON_EXTRACT(d.config, '$.deviceTypeCode')
        OR (dd.device_type IS NULL AND JSON_EXTRACT(d.config, '$.deviceTypeCode') = 0)
        THEN 1 ELSE 0 
    END) as device_type_matched,
    SUM(CASE 
        WHEN dd.tea_key = JSON_UNQUOTE(JSON_EXTRACT(d.config, '$.teaKey'))
        OR (dd.tea_key IS NULL AND JSON_UNQUOTE(JSON_EXTRACT(d.config, '$.teaKey')) = '')
        THEN 1 ELSE 0 
    END) as tea_key_matched,
    CASE 
        WHEN COUNT(*) = SUM(CASE 
            WHEN dd.station_code = JSON_UNQUOTE(JSON_EXTRACT(d.config, '$.stationCode'))
            OR (dd.station_code IS NULL AND JSON_UNQUOTE(JSON_EXTRACT(d.config, '$.stationCode')) = '')
            THEN 1 ELSE 0 
        END)
        THEN '✓ 通过'
        ELSE '✗ 失败'
    END as result
FROM iot_detong_device dd
JOIN iot_device d ON dd.id = d.id
WHERE dd.deleted = 0 AND d.deleted = 0;

-- =====================================================
-- 第四步: 提交或回滚
-- =====================================================

-- 如果所有验证通过,提交事务
-- 如果有验证失败,需要手动回滚: ROLLBACK;

SELECT '所有验证完成,请检查结果后决定是否提交事务' as step;
SELECT '如果验证通过,执行: COMMIT;' as action;
SELECT '如果验证失败,执行: ROLLBACK;' as action;

-- 提交事务(需要手动执行)
-- COMMIT;

-- =====================================================
-- 迁移完成说明
-- =====================================================

/*
迁移完成后的数据结构:

iot_device表中的德通设备记录:
- device_type = 2 (德通设备)
- serial_number = station_code (测站编码)
- config字段包含所有德通特有字段的JSON:
  {
    "deviceType": "DETONG",
    "stationCode": "测站编码",
    "deviceTypeCode": 设备类型代码,
    "provinceCode": "行政区代码",
    "managementCode": "管理处代码",
    "stationCodePart": "站所代码",
    "pileFront": "桩号前",
    "pileBack": "桩号后",
    "manufacturer": "设备厂家",
    "sequenceNo": "顺序编号",
    "teaKey": "TEA加密密钥",
    "password": "设备密码",
    "lastHeartbeat": "最后心跳时间"
  }

字段映射关系:
- id → id (保持不变)
- device_name → device_name
- station_code → serial_number
- status → state (0→0离线, 1→1在线)
- last_heartbeat → online_time
- 所有特有字段 → config (JSON)

注意事项:
1. 迁移使用事务保护,确保数据一致性
2. 使用NOT EXISTS避免重复迁移
3. 内置数据验证,确保迁移质量
4. 需要手动执行COMMIT或ROLLBACK
5. 建议先在测试环境验证
*/
