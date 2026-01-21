-- =============================================
-- 科鼎测试环境验证脚本
-- 验证德通表已删除、科鼎表存在、测试数据正确
-- =============================================

SELECT '========================================' AS info;
SELECT '科鼎测试环境验证' AS info;
SELECT '========================================' AS info;

-- =============================================
-- 1. 验证德通表已删除
-- =============================================
SELECT '' AS info;
SELECT '>>> 1. 验证德通表已删除 <<<' AS info;

SELECT 
    CASE 
        WHEN COUNT(*) = 0 THEN '✓ PASS: 德通表已全部删除'
        ELSE CONCAT('✗ FAIL: 仍存在 ', COUNT(*), ' 个德通表')
    END AS detong_tables_check
FROM information_schema.tables 
WHERE table_schema = DATABASE() 
AND table_name LIKE 'iot_detong_%';

-- 列出残留的德通表（如果有）
SELECT table_name AS '残留德通表'
FROM information_schema.tables 
WHERE table_schema = DATABASE() 
AND table_name LIKE 'iot_detong_%';

-- =============================================
-- 2. 验证科鼎表存在
-- =============================================
SELECT '' AS info;
SELECT '>>> 2. 验证科鼎表存在 <<<' AS info;

SELECT 
    CASE 
        WHEN COUNT(*) >= 6 THEN CONCAT('✓ PASS: 科鼎表完整 (', COUNT(*), ' 个表)')
        ELSE CONCAT('✗ FAIL: 科鼎表不完整，当前 ', COUNT(*), ' 个表')
    END AS keding_tables_check
FROM information_schema.tables 
WHERE table_schema = DATABASE() 
AND table_name LIKE 'iot_keding_%';

-- 列出所有科鼎表
SELECT table_name AS '科鼎表名称'
FROM information_schema.tables 
WHERE table_schema = DATABASE() 
AND table_name LIKE 'iot_keding_%'
ORDER BY table_name;

-- =============================================
-- 3. 验证测试设备数据
-- =============================================
SELECT '' AS info;
SELECT '>>> 3. 验证测试设备数据 <<<' AS info;

-- 检查是否有9种设备类型
SELECT 
    CASE 
        WHEN COUNT(DISTINCT device_type) = 9 THEN '✓ PASS: 9种设备类型全部存在'
        ELSE CONCAT('✗ FAIL: 只有 ', COUNT(DISTINCT device_type), ' 种设备类型')
    END AS device_types_check
FROM iot_keding_device 
WHERE station_code LIKE 'TEST%';

-- 列出测试设备详情
SELECT 
    station_code AS '测站编码',
    device_name AS '设备名称',
    device_type AS '设备类型',
    CASE 
        WHEN tea_key IS NOT NULL AND tea_key != '' THEN '✓'
        ELSE '✗'
    END AS 'TEA密钥',
    CASE 
        WHEN password IS NOT NULL AND password != '' THEN '✓'
        ELSE '✗'
    END AS '密码'
FROM iot_keding_device 
WHERE station_code LIKE 'TEST%'
ORDER BY device_type;

-- =============================================
-- 4. 验证测试固件数据
-- =============================================
SELECT '' AS info;
SELECT '>>> 4. 验证测试固件数据 <<<' AS info;

-- 检查是否有至少3个固件
SELECT 
    CASE 
        WHEN COUNT(*) >= 3 THEN CONCAT('✓ PASS: 固件数量充足 (', COUNT(*), ' 个)')
        ELSE CONCAT('✗ FAIL: 固件数量不足，当前 ', COUNT(*), ' 个')
    END AS firmware_count_check
FROM iot_keding_firmware 
WHERE tenant_id = 1;

-- 列出测试固件详情
SELECT 
    id AS 'ID',
    name AS '固件名称',
    version AS '版本',
    device_type AS '设备类型',
    file_size AS '文件大小',
    CASE 
        WHEN file_md5 IS NOT NULL AND file_md5 != '' THEN '✓'
        ELSE '✗'
    END AS 'MD5'
FROM iot_keding_firmware 
WHERE tenant_id = 1;

-- =============================================
-- 5. 数据完整性检查
-- =============================================
SELECT '' AS info;
SELECT '>>> 5. 数据完整性检查 <<<' AS info;

-- 检查设备数据完整性
SELECT 
    CASE 
        WHEN COUNT(*) = 0 THEN '✓ PASS: 所有设备数据完整'
        ELSE CONCAT('✗ FAIL: ', COUNT(*), ' 个设备数据不完整')
    END AS device_integrity_check
FROM iot_keding_device 
WHERE station_code LIKE 'TEST%'
AND (
    tea_key IS NULL OR tea_key = '' OR
    password IS NULL OR password = '' OR
    tenant_id IS NULL OR tenant_id <= 0 OR
    creator IS NULL OR creator = ''
);

-- 检查固件数据完整性
SELECT 
    CASE 
        WHEN COUNT(*) = 0 THEN '✓ PASS: 所有固件数据完整'
        ELSE CONCAT('✗ FAIL: ', COUNT(*), ' 个固件数据不完整')
    END AS firmware_integrity_check
FROM iot_keding_firmware 
WHERE tenant_id = 1
AND (
    name IS NULL OR name = '' OR
    version IS NULL OR version = '' OR
    file_path IS NULL OR file_path = '' OR
    file_size IS NULL OR file_size <= 0 OR
    file_md5 IS NULL OR file_md5 = ''
);

-- =============================================
-- 6. 总结
-- =============================================
SELECT '' AS info;
SELECT '========================================' AS info;
SELECT '验证完成' AS info;
SELECT '========================================' AS info;
