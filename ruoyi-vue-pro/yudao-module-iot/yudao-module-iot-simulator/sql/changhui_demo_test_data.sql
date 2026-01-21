-- =============================================
-- 长辉设备模拟器 - 演示测试数据
-- 用于客户演示设备心跳、在线、升级等功能
-- 
-- 注意：项目统一使用 iot_device 表管理所有设备
-- 长辉设备特有配置存储在 config JSON 字段中
-- =============================================

-- ----------------------------
-- 1. 创建 CHANGHUI 演示产品（如果不存在）
-- ----------------------------
INSERT INTO `iot_product` (`product_key`, `name`, `description`, `device_type`, `status`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
SELECT 'CHANGHUI_DEMO', '长辉测控设备(演示)', '用于演示的长辉协议设备产品-支持心跳、升级等功能', 0, 1, '1', NOW(), '1', NOW(), b'0', 1
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM `iot_product` WHERE `product_key` = 'CHANGHUI_DEMO' AND `deleted` = b'0'
);

-- 获取产品ID
SET @changhui_product_id = (SELECT `id` FROM `iot_product` WHERE `product_key` = 'CHANGHUI_DEMO' AND `deleted` = b'0' LIMIT 1);

-- ----------------------------
-- 2. 创建演示设备（统一使用 iot_device 表）
-- ----------------------------

-- 设备1：用于演示正常升级流程（SUCCESS模式）
INSERT INTO `iot_device` (
    `device_name`, `nickname`, `device_key`, `product_id`, `product_key`, `device_type`,
    `config`, `status`, `state`,
    `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`
) SELECT
    '演示设备-成功模式', '长辉演示设备1', '01020304050607080910', @changhui_product_id, 'CHANGHUI_DEMO', 0,
    '{"stationCode":"01020304050607080910","teaKey":"[305419896, 2596069104, 305419896, 2596069104]","password":"0000","manufacturer":"长辉","deviceType":1}',
    0, 0,
    '1', NOW(), '1', NOW(), b'0', 1
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM `iot_device` WHERE `device_key` = '01020304050607080910' AND `deleted` = b'0'
);

-- 设备2：用于演示升级拒绝（REJECT模式）
INSERT INTO `iot_device` (
    `device_name`, `nickname`, `device_key`, `product_id`, `product_key`, `device_type`,
    `config`, `status`, `state`,
    `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`
) SELECT
    '演示设备-拒绝模式', '长辉演示设备2', '01020304050607080911', @changhui_product_id, 'CHANGHUI_DEMO', 0,
    '{"stationCode":"01020304050607080911","teaKey":"[305419896, 2596069104, 305419896, 2596069104]","password":"0000","manufacturer":"长辉","deviceType":1}',
    0, 0,
    '1', NOW(), '1', NOW(), b'0', 1
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM `iot_device` WHERE `device_key` = '01020304050607080911' AND `deleted` = b'0'
);

-- 设备3：用于演示升级失败（FRAME_FAIL模式）
INSERT INTO `iot_device` (
    `device_name`, `nickname`, `device_key`, `product_id`, `product_key`, `device_type`,
    `config`, `status`, `state`,
    `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`
) SELECT
    '演示设备-失败模式', '长辉演示设备3', '01020304050607080912', @changhui_product_id, 'CHANGHUI_DEMO', 0,
    '{"stationCode":"01020304050607080912","teaKey":"[305419896, 2596069104, 305419896, 2596069104]","password":"0000","manufacturer":"长辉","deviceType":1}',
    0, 0,
    '1', NOW(), '1', NOW(), b'0', 1
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM `iot_device` WHERE `device_key` = '01020304050607080912' AND `deleted` = b'0'
);

-- ----------------------------
-- 3. 创建演示固件（用于升级测试）
-- ----------------------------
INSERT INTO `changhui_firmware` (
    `name`, `version`, `device_type`, `file_path`, `file_size`, `file_md5`, `description`,
    `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`
) SELECT
    '演示固件V2.0', 'V2.0.0', 1, '/firmware/demo/changhui_v2.0.0.bin', 1024000, 'demo_md5_hash',
    '用于演示的固件版本',
    '1', NOW(), '1', NOW(), b'0', 1
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM `changhui_firmware` WHERE `version` = 'V2.0.0' AND `deleted` = b'0'
);

-- ----------------------------
-- 验证插入结果
-- ----------------------------
SELECT '===== 长辉演示设备（iot_device表）=====' AS info;
SELECT `id`, `device_key`, `device_name`, `product_key`, `status`, 
       JSON_EXTRACT(`config`, '$.stationCode') AS station_code
FROM `iot_device` 
WHERE `device_key` LIKE '010203040506070809%' AND `deleted` = b'0';

SELECT '===== 演示固件 =====' AS info;
SELECT `id`, `name`, `version`, `device_type` 
FROM `changhui_firmware` 
WHERE `version` = 'V2.0.0' AND `deleted` = b'0';
