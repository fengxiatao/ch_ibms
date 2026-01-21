-- =====================================================
-- IoT设备表结构重构 - 门禁设备数据迁移脚本
-- 功能: 将 iot_access_device 表数据迁移到统一的 iot_device 表
-- 版本: V1.0
-- 日期: 2024-12-11
-- =====================================================

-- 说明:
-- 1. 本脚本将门禁设备的特有字段转换为JSON格式存储在config字段中
-- 2. 迁移过程使用事务保护,确保数据一致性
-- 3. 使用 NOT EXISTS 避免重复迁移
-- 4. 保留原始数据,不删除遗留表

-- =====================================================
-- 第一步: 数据迁移前检查
-- =====================================================

-- 检查统一表是否存在config字段
SELECT 
    COLUMN_NAME, 
    DATA_TYPE, 
    COLUMN_TYPE
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'iot_device'
  AND COLUMN_NAME = 'config';

-- 检查遗留表数据量
SELECT 
    '门禁设备待迁移数量' as description,
    COUNT(*) as count
FROM iot_access_device
WHERE deleted = 0;

-- =====================================================
-- 第二步: 执行数据迁移
-- =====================================================

-- 开始事务
START TRANSACTION;

-- 迁移门禁设备数据到统一表
INSERT INTO iot_device (
    id,
    device_name,
    nickname,
    serial_number,
    pic_url,
    product_id,
    product_key,
    device_key,
    device_type,
    state,
    online_time,
    offline_time,
    active_time,
    ip,
    config,
    tenant_id,
    creator,
    create_time,
    updater,
    update_time,
    deleted
)
SELECT 
    ad.id,
    ad.device_name,
    ad.device_name as nickname,  -- 使用设备名称作为备注名称
    ad.device_sn as serial_number,
    NULL as pic_url,  -- 门禁设备暂无图片
    ad.product_id,
    'ACCESS' as product_key,  -- 门禁设备产品标识
    CONCAT('ACCESS_', ad.device_sn) as device_key,  -- 生成唯一设备标识
    1 as device_type,  -- 1表示门禁设备类型
    -- 状态映射: ONLINE->1, OFFLINE->0, 其他->2
    CASE ad.status
        WHEN 'ONLINE' THEN 1
        WHEN 'OFFLINE' THEN 0
        ELSE 2
    END as state,
    ad.last_online_time as online_time,
    NULL as offline_time,  -- 遗留表无此字段
    ad.activation_time as active_time,
    ad.ip_address as ip,
    -- 构建JSON配置对象
    JSON_OBJECT(
        'deviceType', 'ACCESS',
        'ipAddress', IFNULL(ad.ip_address, ''),
        'port', IFNULL(ad.port, 37777),
        'username', IFNULL(ad.username, ''),
        'password', IFNULL(ad.password, ''),
        'firmwareVersion', IFNULL(ad.firmware_version, ''),
        'sdkVersion', IFNULL(ad.sdk_version, ''),
        'activated', IFNULL(ad.activated, false),
        'activationTime', IFNULL(DATE_FORMAT(ad.activation_time, '%Y-%m-%dT%H:%i:%s'), NULL)
    ) as config,
    ad.tenant_id,
    ad.creator,
    ad.create_time,
    ad.updater,
    ad.update_time,
    ad.deleted
FROM iot_access_device ad
WHERE NOT EXISTS (
    -- 避免重复迁移
    SELECT 1 
    FROM iot_device d 
    WHERE d.id = ad.id
)
AND ad.deleted = 0;  -- 只迁移未删除的数据

-- 提交事务
COMMIT;

-- =====================================================
-- 第三步: 迁移后数据验证
-- =====================================================

-- 验证迁移数量
SELECT 
    '迁移结果统计' as description,
    (SELECT COUNT(*) FROM iot_access_device WHERE deleted = 0) as source_count,
    (SELECT COUNT(*) FROM iot_device WHERE device_type = 1 AND deleted = 0) as target_count,
    (SELECT COUNT(*) FROM iot_access_device WHERE deleted = 0) - 
    (SELECT COUNT(*) FROM iot_device WHERE device_type = 1 AND deleted = 0) as diff;

-- 验证配置字段完整性(抽样检查前10条)
SELECT 
    ad.id,
    ad.device_name,
    ad.ip_address as source_ip,
    JSON_UNQUOTE(JSON_EXTRACT(d.config, '$.ipAddress')) as target_ip,
    ad.port as source_port,
    JSON_EXTRACT(d.config, '$.port') as target_port,
    ad.username as source_username,
    JSON_UNQUOTE(JSON_EXTRACT(d.config, '$.username')) as target_username,
    CASE 
        WHEN ad.ip_address = JSON_UNQUOTE(JSON_EXTRACT(d.config, '$.ipAddress'))
         AND ad.port = JSON_EXTRACT(d.config, '$.port')
         AND ad.username = JSON_UNQUOTE(JSON_EXTRACT(d.config, '$.username'))
        THEN '✓ 一致'
        ELSE '✗ 不一致'
    END as validation_result
FROM iot_access_device ad
JOIN iot_device d ON ad.id = d.id
WHERE ad.deleted = 0
  AND d.device_type = 1
LIMIT 10;

-- 检查是否有迁移失败的记录
SELECT 
    '未迁移的门禁设备' as description,
    COUNT(*) as count
FROM iot_access_device ad
WHERE ad.deleted = 0
  AND NOT EXISTS (
      SELECT 1 FROM iot_device d WHERE d.id = ad.id
  );

-- =====================================================
-- 迁移完成提示
-- =====================================================

SELECT 
    '门禁设备数据迁移完成' as status,
    NOW() as completion_time,
    '请执行验证脚本确认数据完整性' as next_step;
