-- =============================================
-- 长辉设备迁移脚本
-- 将 changhui_device 表数据迁移到 iot_device 表
-- 设备特有属性存储在 config JSON 字段中
-- =============================================

-- 说明：
-- 1. 此脚本将 changhui_device 表中的设备迁移到 iot_device 表
-- 2. 长辉设备特有属性（stationCode, teaKey, password等）存储在 config 字段中
-- 3. 迁移后，changhui_device 表将被保留作为备份，可在验证后删除
-- 4. 需要先创建长辉设备对应的产品记录（product_id=100）

-- 步骤1：确保长辉设备产品存在
-- 如果不存在，需要先创建产品
INSERT IGNORE INTO `iot_product` (
    `id`, `name`, `product_key`, `device_type`, `description`, 
    `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`
) VALUES (
    100, '长辉TCP设备', 'CHANGHUI_TCP', 1, '长辉、科鼎、德通等使用相同TCP协议的设备',
    'system', NOW(), 'system', NOW(), 0, 1
);

-- 步骤2：迁移设备数据
-- 将 changhui_device 表数据插入到 iot_device 表
INSERT INTO `iot_device` (
    `device_name`,
    `product_id`,
    `product_key`,
    `device_type`,
    `state`,
    `online_time`,
    `config`,
    `creator`,
    `create_time`,
    `updater`,
    `update_time`,
    `deleted`,
    `tenant_id`
)
SELECT 
    cd.device_name,
    100 AS product_id,
    'CHANGHUI_TCP' AS product_key,
    cd.device_type,
    CASE WHEN cd.status = 1 THEN 1 ELSE 0 END AS state,
    cd.last_heartbeat AS online_time,
    JSON_OBJECT(
        'deviceType', 'CHANGHUI',
        'stationCode', cd.station_code,
        'teaKey', cd.tea_key,
        'password', cd.password,
        'provinceCode', cd.province_code,
        'managementCode', cd.management_code,
        'stationCodePart', cd.station_code_part,
        'pileFront', cd.pile_front,
        'pileBack', cd.pile_back,
        'manufacturer', cd.manufacturer,
        'sequenceNo', cd.sequence_no,
        'changhuiDeviceType', cd.device_type
    ) AS config,
    cd.creator,
    cd.create_time,
    cd.updater,
    cd.update_time,
    cd.deleted,
    cd.tenant_id
FROM `changhui_device` cd
WHERE NOT EXISTS (
    -- 避免重复迁移：检查是否已存在相同 stationCode 的设备
    SELECT 1 FROM `iot_device` id 
    WHERE JSON_UNQUOTE(JSON_EXTRACT(id.config, '$.stationCode')) = cd.station_code
    AND id.product_id = 100
    AND id.deleted = cd.deleted
    AND id.tenant_id = cd.tenant_id
);

-- 步骤3：更新关联表的 device_id
-- 更新 changhui_data 表的 device_id
UPDATE `changhui_data` cd
INNER JOIN `iot_device` id ON JSON_UNQUOTE(JSON_EXTRACT(id.config, '$.stationCode')) = cd.station_code
    AND id.product_id = 100
    AND id.tenant_id = cd.tenant_id
SET cd.device_id = id.id
WHERE cd.device_id IS NULL OR cd.device_id NOT IN (SELECT id FROM iot_device);

-- 更新 changhui_alarm 表的 device_id
UPDATE `changhui_alarm` ca
INNER JOIN `iot_device` id ON JSON_UNQUOTE(JSON_EXTRACT(id.config, '$.stationCode')) = ca.station_code
    AND id.product_id = 100
    AND id.tenant_id = ca.tenant_id
SET ca.device_id = id.id
WHERE ca.device_id IS NULL OR ca.device_id NOT IN (SELECT id FROM iot_device);

-- 更新 changhui_control_log 表的 device_id
UPDATE `changhui_control_log` ccl
INNER JOIN `iot_device` id ON JSON_UNQUOTE(JSON_EXTRACT(id.config, '$.stationCode')) = ccl.station_code
    AND id.product_id = 100
    AND id.tenant_id = ccl.tenant_id
SET ccl.device_id = id.id
WHERE ccl.device_id IS NULL OR ccl.device_id NOT IN (SELECT id FROM iot_device);

-- 更新 changhui_upgrade_task 表的 device_id
UPDATE `changhui_upgrade_task` cut
INNER JOIN `iot_device` id ON JSON_UNQUOTE(JSON_EXTRACT(id.config, '$.stationCode')) = cut.station_code
    AND id.product_id = 100
    AND id.tenant_id = cut.tenant_id
SET cut.device_id = id.id
WHERE cut.device_id IS NULL OR cut.device_id NOT IN (SELECT id FROM iot_device);

-- 步骤4：验证迁移结果
-- 检查迁移的设备数量
SELECT 
    'changhui_device (原表)' AS table_name,
    COUNT(*) AS count
FROM `changhui_device`
WHERE deleted = 0
UNION ALL
SELECT 
    'iot_device (长辉设备)' AS table_name,
    COUNT(*) AS count
FROM `iot_device`
WHERE product_id = 100 AND deleted = 0;

-- 步骤5：（可选）重命名原表作为备份
-- 在确认迁移成功后执行
-- RENAME TABLE `changhui_device` TO `changhui_device_backup`;

-- 步骤6：（可选）删除原表
-- 在确认迁移成功且备份完成后执行
-- DROP TABLE IF EXISTS `changhui_device_backup`;
