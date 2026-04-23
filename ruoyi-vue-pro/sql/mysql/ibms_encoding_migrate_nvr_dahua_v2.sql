-- IBMS 编码规范 v2 示例：大华 NVR 产品/设备/通道（幂等：按 id 更新，可按环境改 id）
-- 产品：{系统}-{型号码}-{设备类型}-{品牌}-{流水}
-- 设备：同构 + 实例流水
-- 通道：{设备编码}-{点位类型}{两位序号}

UPDATE ibms_product
SET product_code = 'VI-NV-NVR-DAH-001',
    model_number = 'DH-NVR2104HS-P-HD/H',
    updater = '1',
    update_time = NOW()
WHERE id = 20 AND deleted = 0;

UPDATE ibms_device
SET device_code = 'VI-NV-NVR-DAH-001',
    brand = 'DAH',
    product_model = 'DH-NVR2104HS-P-HD/H',
    updater = '1',
    update_time = NOW()
WHERE id = 12 AND deleted = 0;

UPDATE ibms_channel
SET code = CONCAT('VI-NV-NVR-DAH-001-VT', LPAD(channel_no, 2, '0')), updater = '1', update_time = NOW()
WHERE device_id = 12 AND deleted = 0;
