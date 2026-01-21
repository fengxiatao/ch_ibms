-- =============================================
-- 长辉设备迁移回滚脚本
-- 将 iot_device 表中的长辉设备数据回滚到 changhui_device 表
-- =============================================

-- 警告：此脚本会删除 iot_device 表中的长辉设备记录
-- 请确保 changhui_device 表中有完整的备份数据

-- 步骤1：恢复关联表的 device_id（指向 changhui_device）
-- 更新 changhui_data 表的 device_id
UPDATE `changhui_data` cd
INNER JOIN `changhui_device` cdev ON cdev.station_code = cd.station_code
    AND cdev.tenant_id = cd.tenant_id
SET cd.device_id = cdev.id;

-- 更新 changhui_alarm 表的 device_id
UPDATE `changhui_alarm` ca
INNER JOIN `changhui_device` cdev ON cdev.station_code = ca.station_code
    AND cdev.tenant_id = ca.tenant_id
SET ca.device_id = cdev.id;

-- 更新 changhui_control_log 表的 device_id
UPDATE `changhui_control_log` ccl
INNER JOIN `changhui_device` cdev ON cdev.station_code = ccl.station_code
    AND cdev.tenant_id = ccl.tenant_id
SET ccl.device_id = cdev.id;

-- 更新 changhui_upgrade_task 表的 device_id
UPDATE `changhui_upgrade_task` cut
INNER JOIN `changhui_device` cdev ON cdev.station_code = cut.station_code
    AND cdev.tenant_id = cut.tenant_id
SET cut.device_id = cdev.id;

-- 步骤2：删除 iot_device 表中的长辉设备记录
DELETE FROM `iot_device` WHERE product_id = 100;

-- 步骤3：（可选）删除长辉设备产品记录
-- DELETE FROM `iot_product` WHERE id = 100;

-- 步骤4：验证回滚结果
SELECT 
    'changhui_device' AS table_name,
    COUNT(*) AS count
FROM `changhui_device`
WHERE deleted = 0
UNION ALL
SELECT 
    'iot_device (长辉设备)' AS table_name,
    COUNT(*) AS count
FROM `iot_device`
WHERE product_id = 100 AND deleted = 0;
