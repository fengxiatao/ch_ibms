-- =====================================
-- 智慧建筑12种设备产品插入脚本 (MySQL版本)
-- 数据库：MySQL
-- 创建时间：2025-11-04
-- 说明：icon字段与矢量图标文件名匹配
-- 矢量图标目录：F:\work\ch_ibms\平台开发平面图常用矢量图标
-- =====================================

-- 使用 INSERT IGNORE 避免重复插入
-- product_key 必须有唯一索引才能生效

-- 1. 枪型摄像机
INSERT IGNORE INTO iot_product (
    name, product_key, menu_ids, primary_menu_id, icon, pic_url, description,
    status, device_type, net_type, location_type, codec_type, job_config,
    creator, create_time, updater, update_time, deleted, tenant_id
)
VALUES (
    '枪型摄像机',
    'bullet_camera_001',
    '[1]',
    1,
    '枪型摄像机',  -- 与矢量图标文件名 "1、枪型摄像机.svg" 匹配
    '',
    '标准枪型网络摄像机，适用于室内外监控',
    1, -- 状态：启用
    0, -- 设备类型：直连设备
    1, -- 联网方式：以太网
    1, -- 定位方式：固定位置
    'onvif',
    '{"offlineCheck":{"enabled":true,"interval":10,"unit":"MINUTE","priority":3}}',
    '系统',
    CURRENT_TIMESTAMP,
    '系统',
    CURRENT_TIMESTAMP,
    0,
    1
);

-- 2. 半球摄像机
INSERT IGNORE INTO iot_product (
    name, product_key, menu_ids, primary_menu_id, icon, pic_url, description,
    status, device_type, net_type, location_type, codec_type, job_config,
    creator, create_time, updater, update_time, deleted, tenant_id
)
VALUES (
    '半球摄像机',
    'dome_camera_001',
    '[1]',
    1,
    '半球摄像机',  -- 与矢量图标文件名 "2、半球摄像机.svg" 匹配
    '',
    '半球型网络摄像机，美观隐蔽',
    1,
    0,
    1,
    1,
    'onvif',
    '{"offlineCheck":{"enabled":true,"interval":10,"unit":"MINUTE","priority":3}}',
    '系统',
    CURRENT_TIMESTAMP,
    '系统',
    CURRENT_TIMESTAMP,
    0,
    1
);

-- 3. 球形摄像机（球机）
INSERT IGNORE INTO iot_product (
    name, product_key, menu_ids, primary_menu_id, icon, pic_url, description,
    status, device_type, net_type, location_type, codec_type, job_config,
    creator, create_time, updater, update_time, deleted, tenant_id
)
VALUES (
    '球形摄像机',
    'ptz_camera_001',
    '[1]',
    1,
    '球形摄像机',  -- 与矢量图标文件名 "3、球形摄像机.svg" 匹配
    '',
    '球形云台摄像机，支持360度旋转',
    1,
    0,
    1,
    1,
    'onvif',
    '{"offlineCheck":{"enabled":true,"interval":10,"unit":"MINUTE","priority":3}}',
    '系统',
    CURRENT_TIMESTAMP,
    '系统',
    CURRENT_TIMESTAMP,
    0,
    1
);

-- 4. 车辆道闸
INSERT IGNORE INTO iot_product (
    name, product_key, menu_ids, primary_menu_id, icon, pic_url, description,
    status, device_type, net_type, location_type, codec_type, job_config,
    creator, create_time, updater, update_time, deleted, tenant_id
)
VALUES (
    '车辆道闸',
    'vehicle_barrier_001',
    '[1]',
    1,
    '车辆道闸',  -- 与矢量图标文件名 "4、车辆道闸.svg" 匹配
    '',
    '车辆通行道闸控制设备',
    1,
    0,
    1,
    1,
    'custom',
    '{"offlineCheck":{"enabled":true,"interval":5,"unit":"MINUTE","priority":2}}',
    '系统',
    CURRENT_TIMESTAMP,
    '系统',
    CURRENT_TIMESTAMP,
    0,
    1
);

-- 5. 车辆识别一体机
INSERT IGNORE INTO iot_product (
    name, product_key, menu_ids, primary_menu_id, icon, pic_url, description,
    status, device_type, net_type, location_type, codec_type, job_config,
    creator, create_time, updater, update_time, deleted, tenant_id
)
VALUES (
    '车辆识别一体机',
    'vehicle_recognition_001',
    '[1]',
    1,
    '车辆识别一体机',  -- 与矢量图标文件名 "5、车辆识别一体机.svg" 匹配
    '',
    '车牌识别一体机，支持车辆出入管理',
    1,
    0,
    1,
    1,
    'custom',
    '{"offlineCheck":{"enabled":true,"interval":5,"unit":"MINUTE","priority":2}}',
    '系统',
    CURRENT_TIMESTAMP,
    '系统',
    CURRENT_TIMESTAMP,
    0,
    1
);

-- 6. 人行闸机
INSERT IGNORE INTO iot_product (
    name, product_key, menu_ids, primary_menu_id, icon, pic_url, description,
    status, device_type, net_type, location_type, codec_type, job_config,
    creator, create_time, updater, update_time, deleted, tenant_id
)
VALUES (
    '人行闸机',
    'pedestrian_gate_001',
    '[1]',
    1,
    '人行闸机',  -- 与矢量图标文件名 "6、人行闸机.svg" 匹配
    '',
    '人员通行闸机，支持刷卡/人脸识别',
    1,
    0,
    1,
    1,
    'custom',
    '{"offlineCheck":{"enabled":true,"interval":5,"unit":"MINUTE","priority":2}}',
    '系统',
    CURRENT_TIMESTAMP,
    '系统',
    CURRENT_TIMESTAMP,
    0,
    1
);

-- 7. 人脸识别一体机
INSERT IGNORE INTO iot_product (
    name, product_key, menu_ids, primary_menu_id, icon, pic_url, description,
    status, device_type, net_type, location_type, codec_type, job_config,
    creator, create_time, updater, update_time, deleted, tenant_id
)
VALUES (
    '人脸识别一体机',
    'face_recognition_001',
    '[1]',
    1,
    '人脸识别一体机',  -- 与矢量图标文件名 "7、人脸识别一体机.svg" 匹配
    '',
    '人脸识别终端，支持人员身份认证',
    1,
    0,
    1,
    1,
    'custom',
    '{"offlineCheck":{"enabled":true,"interval":5,"unit":"MINUTE","priority":2}}',
    '系统',
    CURRENT_TIMESTAMP,
    '系统',
    CURRENT_TIMESTAMP,
    0,
    1
);

-- 8. 巡更点
INSERT IGNORE INTO iot_product (
    name, product_key, menu_ids, primary_menu_id, icon, pic_url, description,
    status, device_type, net_type, location_type, codec_type, job_config,
    creator, create_time, updater, update_time, deleted, tenant_id
)
VALUES (
    '巡更点',
    'patrol_point_001',
    '[1]',
    1,
    '巡更点',  -- 与矢量图标文件名 "8、巡更点.svg" 匹配
    '',
    '巡更打卡点，记录巡逻人员到达时间',
    1,
    0,
    1,
    1,
    'custom',
    '{}',
    '系统',
    CURRENT_TIMESTAMP,
    '系统',
    CURRENT_TIMESTAMP,
    0,
    1
);

-- 9. 水表
INSERT IGNORE INTO iot_product (
    name, product_key, menu_ids, primary_menu_id, icon, pic_url, description,
    status, device_type, net_type, location_type, codec_type, job_config,
    creator, create_time, updater, update_time, deleted, tenant_id
)
VALUES (
    '水表',
    'water_meter_001',
    '[1]',
    1,
    '水表',  -- 与矢量图标文件名 "9、水表.svg" 匹配
    '',
    '智能水表，实时监测用水量',
    1,
    0,
    1,
    1,
    'custom',
    '{}',
    '系统',
    CURRENT_TIMESTAMP,
    '系统',
    CURRENT_TIMESTAMP,
    0,
    1
);

-- 10. 电表
INSERT IGNORE INTO iot_product (
    name, product_key, menu_ids, primary_menu_id, icon, pic_url, description,
    status, device_type, net_type, location_type, codec_type, job_config,
    creator, create_time, updater, update_time, deleted, tenant_id
)
VALUES (
    '电表',
    'electricity_meter_001',
    '[1]',
    1,
    '电表',  -- 与矢量图标文件名 "10、电表.svg" 匹配
    '',
    '智能电表，实时监测用电量',
    1,
    0,
    1,
    1,
    'custom',
    '{}',
    '系统',
    CURRENT_TIMESTAMP,
    '系统',
    CURRENT_TIMESTAMP,
    0,
    1
);

-- 11. 燃气表
INSERT IGNORE INTO iot_product (
    name, product_key, menu_ids, primary_menu_id, icon, pic_url, description,
    status, device_type, net_type, location_type, codec_type, job_config,
    creator, create_time, updater, update_time, deleted, tenant_id
)
VALUES (
    '燃气表',
    'gas_meter_001',
    '[1]',
    1,
    '燃气表',  -- 与矢量图标文件名 "11、燃气表.svg" 匹配
    '',
    '智能燃气表，实时监测燃气使用',
    1,
    0,
    1,
    1,
    'custom',
    '{}',
    '系统',
    CURRENT_TIMESTAMP,
    '系统',
    CURRENT_TIMESTAMP,
    0,
    1
);

-- 12. 考勤机
INSERT IGNORE INTO iot_product (
    name, product_key, menu_ids, primary_menu_id, icon, pic_url, description,
    status, device_type, net_type, location_type, codec_type, job_config,
    creator, create_time, updater, update_time, deleted, tenant_id
)
VALUES (
    '考勤机',
    'attendance_machine_001',
    '[1]',
    1,
    '考勤机',  -- 与矢量图标文件名 "12、考勤机.svg" 匹配
    '',
    '考勤打卡机，支持指纹/人脸识别',
    1,
    0,
    1,
    1,
    'custom',
    '{}',
    '系统',
    CURRENT_TIMESTAMP,
    '系统',
    CURRENT_TIMESTAMP,
    0,
    1
);

-- ==================== 验证查询 ====================

-- 查询所有12种产品
SELECT 
    id, 
    name, 
    product_key, 
    icon,  -- 现在icon字段存储的是中文设备类型名称
    status, 
    device_type,
    codec_type
FROM iot_product
WHERE product_key IN (
    'bullet_camera_001', 'dome_camera_001', 'ptz_camera_001',
    'vehicle_barrier_001', 'vehicle_recognition_001', 'pedestrian_gate_001',
    'face_recognition_001', 'patrol_point_001', 'water_meter_001',
    'electricity_meter_001', 'gas_meter_001', 'attendance_machine_001'
)
ORDER BY id;

-- 统计产品数量
SELECT 
    COUNT(*) as total_products,
    '个产品已导入' as message
FROM iot_product
WHERE product_key IN (
    'bullet_camera_001', 'dome_camera_001', 'ptz_camera_001',
    'vehicle_barrier_001', 'vehicle_recognition_001', 'pedestrian_gate_001',
    'face_recognition_001', 'patrol_point_001', 'water_meter_001',
    'electricity_meter_001', 'gas_meter_001', 'attendance_machine_001'
);

-- ==================== 图标映射关系说明 ====================
/*
产品表 icon 字段 → 前端矢量图标文件映射关系：

数据库存储          →  前端使用                      →  矢量图标文件
---------------------------------------------------------------------------
'枪型摄像机'        →  getDeviceIconSvg('枪型摄像机')  →  1、枪型摄像机.svg
'半球摄像机'        →  getDeviceIconSvg('半球摄像机')  →  2、半球摄像机.svg
'球形摄像机'        →  getDeviceIconSvg('球形摄像机')  →  3、球形摄像机.svg
'车辆道闸'          →  getDeviceIconSvg('车辆道闸')    →  4、车辆道闸.svg
'车辆识别一体机'    →  getDeviceIconSvg('车辆识别一体机') → 5、车辆识别一体机.svg
'人行闸机'          →  getDeviceIconSvg('人行闸机')    →  6、人行闸机.svg
'人脸识别一体机'    →  getDeviceIconSvg('人脸识别一体机') → 7、人脸识别一体机.svg
'巡更点'            →  getDeviceIconSvg('巡更点')      →  8、巡更点.svg
'水表'              →  getDeviceIconSvg('水表')        →  9、水表.svg
'电表'              →  getDeviceIconSvg('电表')        →  10、电表.svg
'燃气表'            →  getDeviceIconSvg('燃气表')      →  11、燃气表.svg
'考勤机'            →  getDeviceIconSvg('考勤机')      →  12、考勤机.svg

前端代码位置：
- 图标管理：yudao-ui-admin-vue3/src/assets/floorplan-icons/index.ts
- 使用示例：FloorPlanEditorV2.vue 中的 addDeviceToCanvas() 函数
*/

