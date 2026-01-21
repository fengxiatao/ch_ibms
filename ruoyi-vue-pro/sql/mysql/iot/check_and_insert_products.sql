-- =====================================
-- 检查并插入12种设备产品 (MySQL版本)
-- 说明：先检查已存在的产品，避免重复
-- =====================================

-- ==================== 步骤1：检查已存在的产品 ====================

SELECT 
    '现有产品检查' AS info_type,
    product_key,
    name,
    status,
    create_time
FROM iot_product
WHERE product_key IN (
    'bullet_camera_001',       -- 枪型摄像机
    'dome_camera_001',         -- 半球摄像机
    'ptz_camera_001',          -- 球形摄像机
    'vehicle_barrier_001',     -- 车辆道闸
    'vehicle_recognition_001', -- 车辆识别一体机
    'pedestrian_gate_001',     -- 人行闸机
    'face_recognition_001',    -- 人脸识别一体机
    'patrol_point_001',        -- 巡更点
    'water_meter_001',         -- 水表
    'electricity_meter_001',   -- 电表
    'gas_meter_001',           -- 燃气表
    'attendance_machine_001'   -- 考勤机
)
ORDER BY create_time DESC;

-- 查看现有产品数量
SELECT 
    '统计信息' AS info_type,
    COUNT(*) AS already_exists_count,
    '个产品已存在' AS message
FROM iot_product
WHERE product_key IN (
    'bullet_camera_001', 'dome_camera_001', 'ptz_camera_001',
    'vehicle_barrier_001', 'vehicle_recognition_001', 'pedestrian_gate_001',
    'face_recognition_001', 'patrol_point_001', 'water_meter_001',
    'electricity_meter_001', 'gas_meter_001', 'attendance_machine_001'
);

-- ==================== 步骤2：查看缺失的产品（需要插入） ====================

SELECT 
    '缺失产品' AS info_type,
    t.product_key,
    t.product_name AS need_to_insert
FROM (
    SELECT 'bullet_camera_001' AS product_key, '枪型摄像机' AS product_name
    UNION ALL SELECT 'dome_camera_001', '半球摄像机'
    UNION ALL SELECT 'ptz_camera_001', '球形摄像机'
    UNION ALL SELECT 'vehicle_barrier_001', '车辆道闸'
    UNION ALL SELECT 'vehicle_recognition_001', '车辆识别一体机'
    UNION ALL SELECT 'pedestrian_gate_001', '人行闸机'
    UNION ALL SELECT 'face_recognition_001', '人脸识别一体机'
    UNION ALL SELECT 'patrol_point_001', '巡更点'
    UNION ALL SELECT 'water_meter_001', '水表'
    UNION ALL SELECT 'electricity_meter_001', '电表'
    UNION ALL SELECT 'gas_meter_001', '燃气表'
    UNION ALL SELECT 'attendance_machine_001', '考勤机'
) t
LEFT JOIN iot_product p ON p.product_key = t.product_key
WHERE p.product_key IS NULL;

-- ==================== 步骤3：如果确认要插入，取消下面的注释 ====================

/*
-- 使用 INSERT IGNORE 安全插入（不会覆盖已存在的数据）

-- 2. 半球摄像机
INSERT IGNORE INTO iot_product (
    name, product_key, menu_ids, primary_menu_id, icon, pic_url, description,
    status, device_type, net_type, location_type, codec_type, job_config,
    creator, create_time, updater, update_time, deleted, tenant_id
) VALUES (
    '半球摄像机', 'dome_camera_001', '[1]', 1, 'ep:video-camera', '',
    '半球型网络摄像机，美观隐蔽', 1, 0, 1, 1, 'onvif',
    '{"offlineCheck":{"enabled":true,"interval":10,"unit":"MINUTE","priority":3}}',
    '系统', CURRENT_TIMESTAMP, '系统', CURRENT_TIMESTAMP, 0, 1
);

-- 3. 球形摄像机
INSERT IGNORE INTO iot_product (
    name, product_key, menu_ids, primary_menu_id, icon, pic_url, description,
    status, device_type, net_type, location_type, codec_type, job_config,
    creator, create_time, updater, update_time, deleted, tenant_id
) VALUES (
    '球形摄像机', 'ptz_camera_001', '[1]', 1, 'ep:camera-filled', '',
    '球形云台摄像机，支持360度旋转', 1, 0, 1, 1, 'onvif',
    '{"offlineCheck":{"enabled":true,"interval":10,"unit":"MINUTE","priority":3}}',
    '系统', CURRENT_TIMESTAMP, '系统', CURRENT_TIMESTAMP, 0, 1
);

-- 4. 车辆道闸
INSERT IGNORE INTO iot_product (
    name, product_key, menu_ids, primary_menu_id, icon, pic_url, description,
    status, device_type, net_type, location_type, codec_type, job_config,
    creator, create_time, updater, update_time, deleted, tenant_id
) VALUES (
    '车辆道闸', 'vehicle_barrier_001', '[1]', 1, 'ep:unlock', '',
    '车辆通行道闸控制设备', 1, 0, 1, 1, 'custom',
    '{"offlineCheck":{"enabled":true,"interval":5,"unit":"MINUTE","priority":2}}',
    '系统', CURRENT_TIMESTAMP, '系统', CURRENT_TIMESTAMP, 0, 1
);

-- 5. 车辆识别一体机
INSERT IGNORE INTO iot_product (
    name, product_key, menu_ids, primary_menu_id, icon, pic_url, description,
    status, device_type, net_type, location_type, codec_type, job_config,
    creator, create_time, updater, update_time, deleted, tenant_id
) VALUES (
    '车辆识别一体机', 'vehicle_recognition_001', '[1]', 1, 'ep:document-checked', '',
    '车牌识别一体机，支持车辆出入管理', 1, 0, 1, 1, 'custom',
    '{"offlineCheck":{"enabled":true,"interval":5,"unit":"MINUTE","priority":2}}',
    '系统', CURRENT_TIMESTAMP, '系统', CURRENT_TIMESTAMP, 0, 1
);

-- 6. 人行闸机
INSERT IGNORE INTO iot_product (
    name, product_key, menu_ids, primary_menu_id, icon, pic_url, description,
    status, device_type, net_type, location_type, codec_type, job_config,
    creator, create_time, updater, update_time, deleted, tenant_id
) VALUES (
    '人行闸机', 'pedestrian_gate_001', '[1]', 1, 'ep:lock', '',
    '人员通行闸机，支持刷卡/人脸识别', 1, 0, 1, 1, 'custom',
    '{"offlineCheck":{"enabled":true,"interval":5,"unit":"MINUTE","priority":2}}',
    '系统', CURRENT_TIMESTAMP, '系统', CURRENT_TIMESTAMP, 0, 1
);

-- 7. 人脸识别一体机
INSERT IGNORE INTO iot_product (
    name, product_key, menu_ids, primary_menu_id, icon, pic_url, description,
    status, device_type, net_type, location_type, codec_type, job_config,
    creator, create_time, updater, update_time, deleted, tenant_id
) VALUES (
    '人脸识别一体机', 'face_recognition_001', '[1]', 1, 'ep:user', '',
    '人脸识别终端，支持人员身份认证', 1, 0, 1, 1, 'custom',
    '{"offlineCheck":{"enabled":true,"interval":5,"unit":"MINUTE","priority":2}}',
    '系统', CURRENT_TIMESTAMP, '系统', CURRENT_TIMESTAMP, 0, 1
);

-- 8. 巡更点
INSERT IGNORE INTO iot_product (
    name, product_key, menu_ids, primary_menu_id, icon, pic_url, description,
    status, device_type, net_type, location_type, codec_type, job_config,
    creator, create_time, updater, update_time, deleted, tenant_id
) VALUES (
    '巡更点', 'patrol_point_001', '[1]', 1, 'ep:location', '',
    '巡更打卡点，记录巡逻人员到达时间', 1, 0, 1, 1, 'custom', '{}',
    '系统', CURRENT_TIMESTAMP, '系统', CURRENT_TIMESTAMP, 0, 1
);

-- 9. 水表
INSERT IGNORE INTO iot_product (
    name, product_key, menu_ids, primary_menu_id, icon, pic_url, description,
    status, device_type, net_type, location_type, codec_type, job_config,
    creator, create_time, updater, update_time, deleted, tenant_id
) VALUES (
    '水表', 'water_meter_001', '[1]', 1, 'ep:operation', '',
    '智能水表，实时监测用水量', 1, 0, 1, 1, 'custom', '{}',
    '系统', CURRENT_TIMESTAMP, '系统', CURRENT_TIMESTAMP, 0, 1
);

-- 10. 电表
INSERT IGNORE INTO iot_product (
    name, product_key, menu_ids, primary_menu_id, icon, pic_url, description,
    status, device_type, net_type, location_type, codec_type, job_config,
    creator, create_time, updater, update_time, deleted, tenant_id
) VALUES (
    '电表', 'electricity_meter_001', '[1]', 1, 'ep:odometer', '',
    '智能电表，实时监测用电量', 1, 0, 1, 1, 'custom', '{}',
    '系统', CURRENT_TIMESTAMP, '系统', CURRENT_TIMESTAMP, 0, 1
);

-- 11. 燃气表
INSERT IGNORE INTO iot_product (
    name, product_key, menu_ids, primary_menu_id, icon, pic_url, description,
    status, device_type, net_type, location_type, codec_type, job_config,
    creator, create_time, updater, update_time, deleted, tenant_id
) VALUES (
    '燃气表', 'gas_meter_001', '[1]', 1, 'ep:aim', '',
    '智能燃气表，实时监测燃气使用', 1, 0, 1, 1, 'custom', '{}',
    '系统', CURRENT_TIMESTAMP, '系统', CURRENT_TIMESTAMP, 0, 1
);

-- 12. 考勤机
INSERT IGNORE INTO iot_product (
    name, product_key, menu_ids, primary_menu_id, icon, pic_url, description,
    status, device_type, net_type, location_type, codec_type, job_config,
    creator, create_time, updater, update_time, deleted, tenant_id
) VALUES (
    '考勤机', 'attendance_machine_001', '[1]', 1, 'ep:calendar', '',
    '考勤打卡机，支持指纹/人脸识别', 1, 0, 1, 1, 'custom', '{}',
    '系统', CURRENT_TIMESTAMP, '系统', CURRENT_TIMESTAMP, 0, 1
);

-- 验证插入结果
SELECT 
    '插入后统计' AS info_type,
    COUNT(*) AS total_count,
    '个产品' AS message
FROM iot_product
WHERE product_key IN (
    'bullet_camera_001', 'dome_camera_001', 'ptz_camera_001',
    'vehicle_barrier_001', 'vehicle_recognition_001', 'pedestrian_gate_001',
    'face_recognition_001', 'patrol_point_001', 'water_meter_001',
    'electricity_meter_001', 'gas_meter_001', 'attendance_machine_001'
);
*/

