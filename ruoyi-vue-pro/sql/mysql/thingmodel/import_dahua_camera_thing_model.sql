-- ==========================================
-- 大华网络摄像头物模型导入脚本
-- 产品Key: dahua_camera_001
-- 版本: 1.0.0
-- 创建时间: 2025-10-24
-- ==========================================

-- 1. 创建产品（如果不存在）
INSERT INTO iot_product (
  id, name, product_key, category_id, device_type, 
  data_format, validate_type, status, description,
  creator, create_time, updater, update_time, deleted, tenant_id
) VALUES (
  62, 
  '大华网络摄像头', 
  'dahua_camera_001', 
  1,  -- 分类ID，根据实际情况调整
  3,  -- 设备类型：3-直连设备
  1,  -- 数据格式：1-JSON
  1,  -- 校验类型：1-弱校验
  0,  -- 状态：0-开发中
  'IBMS智慧安防-大华网络摄像头',
  '1',  -- 创建者
  NOW(),
  '1',  -- 更新者
  NOW(),
  0,  -- 未删除
  1   -- 租户ID
) ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  description = VALUES(description),
  updater = VALUES(updater),
  update_time = VALUES(update_time);

-- ==========================================
-- 2. 导入属性（Properties）
-- ==========================================

-- 2.1 在线状态
INSERT INTO iot_thing_model (
  product_id, product_key, identifier, name, description,
  type, property,
  creator, create_time, updater, update_time, deleted, tenant_id
) VALUES (
  62, 'dahua_camera_001', 'online_status', '在线状态', '设备是否在线',
  1, -- type=1 表示属性
  JSON_OBJECT(
    'identifier', 'online_status',
    'name', '在线状态',
    'accessMode', 'r',
    'required', true,
    'dataType', 'bool',
    'dataSpecsList', JSON_ARRAY(
      JSON_OBJECT('dataType', 'bool', 'name', '离线', 'value', 0),
      JSON_OBJECT('dataType', 'bool', 'name', '在线', 'value', 1)
    )
  ),
  '1', NOW(), '1', NOW(), 0, 1
);

-- 2.2 设备状态
INSERT INTO iot_thing_model (
  product_id, product_key, identifier, name, description,
  type, property,
  creator, create_time, updater, update_time, deleted, tenant_id
) VALUES (
  62, 'dahua_camera_001', 'device_status', '设备状态', '设备运行状态',
  1,
  JSON_OBJECT(
    'identifier', 'device_status',
    'name', '设备状态',
    'accessMode', 'r',
    'required', true,
    'dataType', 'enum',
    'dataSpecsList', JSON_ARRAY(
      JSON_OBJECT('dataType', 'enum', 'name', '离线', 'value', 0),
      JSON_OBJECT('dataType', 'enum', 'name', '在线正常', 'value', 1),
      JSON_OBJECT('dataType', 'enum', 'name', '在线异常', 'value', 2)
    )
  ),
  '1', NOW(), '1', NOW(), 0, 1
);

-- 2.3 主码流地址
INSERT INTO iot_thing_model (
  product_id, product_key, identifier, name, description,
  type, property,
  creator, create_time, updater, update_time, deleted, tenant_id
) VALUES (
  62, 'dahua_camera_001', 'video_stream_main', '主码流地址', '高清主码流（录像用）',
  1,
  JSON_OBJECT(
    'identifier', 'video_stream_main',
    'name', '主码流地址',
    'accessMode', 'r',
    'required', true,
    'dataType', 'struct',
    'dataSpecsList', JSON_ARRAY(
      JSON_OBJECT(
        'dataType', 'struct',
        'identifier', 'rtsp_url',
        'name', 'RTSP地址',
        'accessMode', 'r',
        'required', true,
        'childDataType', 'text',
        'dataSpecs', JSON_OBJECT('dataType', 'text', 'length', '256')
      ),
      JSON_OBJECT(
        'dataType', 'struct',
        'identifier', 'hls_url',
        'name', 'HLS地址',
        'accessMode', 'r',
        'required', false,
        'childDataType', 'text',
        'dataSpecs', JSON_OBJECT('dataType', 'text', 'length', '256')
      ),
      JSON_OBJECT(
        'dataType', 'struct',
        'identifier', 'flv_url',
        'name', 'FLV地址',
        'accessMode', 'r',
        'required', false,
        'childDataType', 'text',
        'dataSpecs', JSON_OBJECT('dataType', 'text', 'length', '256')
      ),
      JSON_OBJECT(
        'dataType', 'struct',
        'identifier', 'webrtc_url',
        'name', 'WebRTC地址',
        'accessMode', 'r',
        'required', false,
        'childDataType', 'text',
        'dataSpecs', JSON_OBJECT('dataType', 'text', 'length', '256')
      )
    )
  ),
  '1', NOW(), '1', NOW(), 0, 1
);

-- 2.4 子码流地址
INSERT INTO iot_thing_model (
  product_id, product_key, identifier, name, description,
  type, property,
  creator, create_time, updater, update_time, deleted, tenant_id
) VALUES (
  62, 'dahua_camera_001', 'video_stream_sub', '子码流地址', '低分辨率子码流（预览用）',
  1,
  JSON_OBJECT(
    'identifier', 'video_stream_sub',
    'name', '子码流地址',
    'accessMode', 'r',
    'required', false,
    'dataType', 'struct',
    'dataSpecsList', JSON_ARRAY(
      JSON_OBJECT(
        'dataType', 'struct',
        'identifier', 'rtsp_url',
        'name', 'RTSP地址',
        'accessMode', 'r',
        'required', true,
        'childDataType', 'text',
        'dataSpecs', JSON_OBJECT('dataType', 'text', 'length', '256')
      ),
      JSON_OBJECT(
        'dataType', 'struct',
        'identifier', 'hls_url',
        'name', 'HLS地址',
        'accessMode', 'r',
        'required', false,
        'childDataType', 'text',
        'dataSpecs', JSON_OBJECT('dataType', 'text', 'length', '256')
      ),
      JSON_OBJECT(
        'dataType', 'struct',
        'identifier', 'flv_url',
        'name', 'FLV地址',
        'accessMode', 'r',
        'required', false,
        'childDataType', 'text',
        'dataSpecs', JSON_OBJECT('dataType', 'text', 'length', '256')
      )
    )
  ),
  '1', NOW(), '1', NOW(), 0, 1
);

-- 2.5 录像状态
INSERT INTO iot_thing_model (
  product_id, product_key, identifier, name, description,
  type, property,
  creator, create_time, updater, update_time, deleted, tenant_id
) VALUES (
  62, 'dahua_camera_001', 'recording_status', '录像状态', '当前录像状态',
  1,
  JSON_OBJECT(
    'identifier', 'recording_status',
    'name', '录像状态',
    'accessMode', 'r',
    'required', true,
    'dataType', 'enum',
    'dataSpecsList', JSON_ARRAY(
      JSON_OBJECT('dataType', 'enum', 'name', '未录像', 'value', 0),
      JSON_OBJECT('dataType', 'enum', 'name', '录像中', 'value', 1)
    )
  ),
  '1', NOW(), '1', NOW(), 0, 1
);

-- 2.6 推流状态
INSERT INTO iot_thing_model (
  product_id, product_key, identifier, name, description,
  type, property,
  creator, create_time, updater, update_time, deleted, tenant_id
) VALUES (
  62, 'dahua_camera_001', 'stream_status', '推流状态', 'ZLMediaKit推流状态',
  1,
  JSON_OBJECT(
    'identifier', 'stream_status',
    'name', '推流状态',
    'accessMode', 'r',
    'required', false,
    'dataType', 'enum',
    'dataSpecsList', JSON_ARRAY(
      JSON_OBJECT('dataType', 'enum', 'name', '未推流', 'value', 0),
      JSON_OBJECT('dataType', 'enum', 'name', '推流中', 'value', 1)
    )
  ),
  '1', NOW(), '1', NOW(), 0, 1
);

-- 2.7 云台支持
INSERT INTO iot_thing_model (
  product_id, product_key, identifier, name, description,
  type, property,
  creator, create_time, updater, update_time, deleted, tenant_id
) VALUES (
  62, 'dahua_camera_001', 'ptz_support', '云台支持', '是否支持云台控制',
  1,
  JSON_OBJECT(
    'identifier', 'ptz_support',
    'name', '云台支持',
    'accessMode', 'r',
    'required', false,
    'dataType', 'bool',
    'dataSpecsList', JSON_ARRAY(
      JSON_OBJECT('dataType', 'bool', 'name', '不支持', 'value', 0),
      JSON_OBJECT('dataType', 'bool', 'name', '支持', 'value', 1)
    )
  ),
  '1', NOW(), '1', NOW(), 0, 1
);

-- 2.8 云台位置
INSERT INTO iot_thing_model (
  product_id, product_key, identifier, name, description,
  type, property,
  creator, create_time, updater, update_time, deleted, tenant_id
) VALUES (
  62, 'dahua_camera_001', 'ptz_position', '云台位置', '云台当前位置',
  1,
  JSON_OBJECT(
    'identifier', 'ptz_position',
    'name', '云台位置',
    'accessMode', 'r',
    'required', false,
    'dataType', 'struct',
    'dataSpecsList', JSON_ARRAY(
      JSON_OBJECT(
        'dataType', 'struct',
        'identifier', 'pan',
        'name', '水平角度',
        'accessMode', 'r',
        'required', false,
        'childDataType', 'int',
        'dataSpecs', JSON_OBJECT('dataType', 'int', 'min', '0', 'max', '360', 'step', '1', 'unit', '°', 'unitName', '度')
      ),
      JSON_OBJECT(
        'dataType', 'struct',
        'identifier', 'tilt',
        'name', '垂直角度',
        'accessMode', 'r',
        'required', false,
        'childDataType', 'int',
        'dataSpecs', JSON_OBJECT('dataType', 'int', 'min', '0', 'max', '180', 'step', '1', 'unit', '°', 'unitName', '度')
      ),
      JSON_OBJECT(
        'dataType', 'struct',
        'identifier', 'zoom',
        'name', '变倍',
        'accessMode', 'r',
        'required', false,
        'childDataType', 'int',
        'dataSpecs', JSON_OBJECT('dataType', 'int', 'min', '1', 'max', '30', 'step', '1', 'unit', 'x', 'unitName', '倍')
      )
    )
  ),
  '1', NOW(), '1', NOW(), 0, 1
);

-- 2.9 移动侦测
INSERT INTO iot_thing_model (
  product_id, product_key, identifier, name, description,
  type, property,
  creator, create_time, updater, update_time, deleted, tenant_id
) VALUES (
  62, 'dahua_camera_001', 'motion_detection_enabled', '移动侦测', '是否启用移动侦测',
  1,
  JSON_OBJECT(
    'identifier', 'motion_detection_enabled',
    'name', '移动侦测',
    'accessMode', 'rw',
    'required', false,
    'dataType', 'bool',
    'dataSpecsList', JSON_ARRAY(
      JSON_OBJECT('dataType', 'bool', 'name', '关闭', 'value', 0),
      JSON_OBJECT('dataType', 'bool', 'name', '开启', 'value', 1)
    )
  ),
  '1', NOW(), '1', NOW(), 0, 1
);

-- 2.10 移动侦测灵敏度
INSERT INTO iot_thing_model (
  product_id, product_key, identifier, name, description,
  type, property,
  creator, create_time, updater, update_time, deleted, tenant_id
) VALUES (
  62, 'dahua_camera_001', 'motion_sensitivity', '移动侦测灵敏度', '移动侦测灵敏度等级',
  1,
  JSON_OBJECT(
    'identifier', 'motion_sensitivity',
    'name', '移动侦测灵敏度',
    'accessMode', 'rw',
    'required', false,
    'dataType', 'int',
    'dataSpecs', JSON_OBJECT('dataType', 'int', 'min', '1', 'max', '10', 'step', '1', 'defaultValue', '5', 'unit', '', 'unitName', '级')
  ),
  '1', NOW(), '1', NOW(), 0, 1
);

-- 2.11 分辨率
INSERT INTO iot_thing_model (
  product_id, product_key, identifier, name, description,
  type, property,
  creator, create_time, updater, update_time, deleted, tenant_id
) VALUES (
  62, 'dahua_camera_001', 'resolution', '分辨率', '视频分辨率',
  1,
  JSON_OBJECT(
    'identifier', 'resolution',
    'name', '分辨率',
    'accessMode', 'r',
    'required', false,
    'dataType', 'text',
    'dataSpecs', JSON_OBJECT('dataType', 'text', 'length', '32')
  ),
  '1', NOW(), '1', NOW(), 0, 1
);

-- 2.12-2.20: 其他属性（简化版，实际使用时可补充）
INSERT INTO iot_thing_model (product_id, product_key, identifier, name, description, type, property, creator, create_time, updater, update_time, deleted, tenant_id) VALUES
(62, 'dahua_camera_001', 'frame_rate', '帧率', '视频帧率', 1, JSON_OBJECT('identifier', 'frame_rate', 'name', '帧率', 'accessMode', 'r', 'required', false, 'dataType', 'int', 'dataSpecs', JSON_OBJECT('dataType', 'int', 'min', '1', 'max', '60', 'step', '1', 'unit', 'fps', 'unitName', '帧/秒')), '1', NOW(), '1', NOW(), 0, 1),
(62, 'dahua_camera_001', 'bit_rate', '码率', '视频码率', 1, JSON_OBJECT('identifier', 'bit_rate', 'name', '码率', 'accessMode', 'r', 'required', false, 'dataType', 'int', 'dataSpecs', JSON_OBJECT('dataType', 'int', 'min', '256', 'max', '8192', 'step', '128', 'unit', 'kbps', 'unitName', '千比特/秒')), '1', NOW(), '1', NOW(), 0, 1),
(62, 'dahua_camera_001', 'device_model', '设备型号', '摄像头型号', 1, JSON_OBJECT('identifier', 'device_model', 'name', '设备型号', 'accessMode', 'r', 'required', false, 'dataType', 'text', 'dataSpecs', JSON_OBJECT('dataType', 'text', 'length', '64')), '1', NOW(), '1', NOW(), 0, 1),
(62, 'dahua_camera_001', 'firmware_version', '固件版本', '固件版本号', 1, JSON_OBJECT('identifier', 'firmware_version', 'name', '固件版本', 'accessMode', 'r', 'required', false, 'dataType', 'text', 'dataSpecs', JSON_OBJECT('dataType', 'text', 'length', '32')), '1', NOW(), '1', NOW(), 0, 1),
(62, 'dahua_camera_001', 'hardware_version', '硬件版本', '硬件版本号', 1, JSON_OBJECT('identifier', 'hardware_version', 'name', '硬件版本', 'accessMode', 'r', 'required', false, 'dataType', 'text', 'dataSpecs', JSON_OBJECT('dataType', 'text', 'length', '32')), '1', NOW(), '1', NOW(), 0, 1),
(62, 'dahua_camera_001', 'serial_number', '序列号', '设备序列号', 1, JSON_OBJECT('identifier', 'serial_number', 'name', '序列号', 'accessMode', 'r', 'required', false, 'dataType', 'text', 'dataSpecs', JSON_OBJECT('dataType', 'text', 'length', '64')), '1', NOW(), '1', NOW(), 0, 1),
(62, 'dahua_camera_001', 'ip_address', 'IP地址', '设备IP地址', 1, JSON_OBJECT('identifier', 'ip_address', 'name', 'IP地址', 'accessMode', 'r', 'required', false, 'dataType', 'text', 'dataSpecs', JSON_OBJECT('dataType', 'text', 'length', '64')), '1', NOW(), '1', NOW(), 0, 1),
(62, 'dahua_camera_001', 'device_temperature', '设备温度', '设备当前温度', 1, JSON_OBJECT('identifier', 'device_temperature', 'name', '设备温度', 'accessMode', 'r', 'required', false, 'dataType', 'float', 'dataSpecs', JSON_OBJECT('dataType', 'float', 'min', '-40.0', 'max', '85.0', 'step', '0.1', 'precise', '1', 'unit', '℃', 'unitName', '摄氏度')), '1', NOW(), '1', NOW(), 0, 1),
(62, 'dahua_camera_001', 'cpu_usage', 'CPU使用率', '设备CPU使用率', 1, JSON_OBJECT('identifier', 'cpu_usage', 'name', 'CPU使用率', 'accessMode', 'r', 'required', false, 'dataType', 'float', 'dataSpecs', JSON_OBJECT('dataType', 'float', 'min', '0.0', 'max', '100.0', 'step', '0.1', 'precise', '1', 'unit', '%', 'unitName', '百分比')), '1', NOW(), '1', NOW(), 0, 1),
(62, 'dahua_camera_001', 'memory_usage', '内存使用率', '设备内存使用率', 1, JSON_OBJECT('identifier', 'memory_usage', 'name', '内存使用率', 'accessMode', 'r', 'required', false, 'dataType', 'float', 'dataSpecs', JSON_OBJECT('dataType', 'float', 'min', '0.0', 'max', '100.0', 'step', '0.1', 'precise', '1', 'unit', '%', 'unitName', '百分比')), '1', NOW(), '1', NOW(), 0, 1);

-- 2.21 存储状态
INSERT INTO iot_thing_model (
  product_id, product_key, identifier, name, description,
  type, property,
  creator, create_time, updater, update_time, deleted, tenant_id
) VALUES (
  62, 'dahua_camera_001', 'storage_status', '存储状态', 'SD卡或硬盘状态',
  1,
  JSON_OBJECT(
    'identifier', 'storage_status',
    'name', '存储状态',
    'accessMode', 'r',
    'required', false,
    'dataType', 'enum',
    'dataSpecsList', JSON_ARRAY(
      JSON_OBJECT('dataType', 'enum', 'name', '无存储', 'value', 0),
      JSON_OBJECT('dataType', 'enum', 'name', '正常', 'value', 1),
      JSON_OBJECT('dataType', 'enum', 'name', '已满', 'value', 2),
      JSON_OBJECT('dataType', 'enum', 'name', '异常', 'value', 3)
    )
  ),
  '1', NOW(), '1', NOW(), 0, 1
);

-- 2.22 夜视模式
INSERT INTO iot_thing_model (
  product_id, product_key, identifier, name, description,
  type, property,
  creator, create_time, updater, update_time, deleted, tenant_id
) VALUES (
  62, 'dahua_camera_001', 'night_vision_mode', '夜视模式', '夜视功能模式',
  1,
  JSON_OBJECT(
    'identifier', 'night_vision_mode',
    'name', '夜视模式',
    'accessMode', 'rw',
    'required', false,
    'dataType', 'enum',
    'dataSpecsList', JSON_ARRAY(
      JSON_OBJECT('dataType', 'enum', 'name', '关闭', 'value', 0),
      JSON_OBJECT('dataType', 'enum', 'name', '自动', 'value', 1),
      JSON_OBJECT('dataType', 'enum', 'name', '强制开启', 'value', 2)
    )
  ),
  '1', NOW(), '1', NOW(), 0, 1
);

-- 2.23 音频功能
INSERT INTO iot_thing_model (
  product_id, product_key, identifier, name, description,
  type, property,
  creator, create_time, updater, update_time, deleted, tenant_id
) VALUES (
  62, 'dahua_camera_001', 'audio_enabled', '音频功能', '是否启用音频',
  1,
  JSON_OBJECT(
    'identifier', 'audio_enabled',
    'name', '音频功能',
    'accessMode', 'rw',
    'required', false,
    'dataType', 'bool',
    'dataSpecsList', JSON_ARRAY(
      JSON_OBJECT('dataType', 'bool', 'name', '关闭', 'value', 0),
      JSON_OBJECT('dataType', 'bool', 'name', '开启', 'value', 1)
    )
  ),
  '1', NOW(), '1', NOW(), 0, 1
);

-- ==========================================
-- 3. 导入事件（Events）
-- ==========================================

-- 3.1 移动侦测告警
INSERT INTO iot_thing_model (
  product_id, product_key, identifier, name, description,
  type, event,
  creator, create_time, updater, update_time, deleted, tenant_id
) VALUES (
  62, 'dahua_camera_001', 'motion_detected', '移动侦测告警', '检测到画面移动',
  2, -- type=2 表示事件
  JSON_OBJECT(
    'identifier', 'motion_detected',
    'name', '移动侦测告警',
    'type', 'alert',
    'required', false,
    'method', 'thing.event.motion_detected.post',
    'outputParams', JSON_ARRAY(
      JSON_OBJECT('identifier', 'snapshot_url', 'name', '告警快照', 'direction', 'output', 'paraOrder', 0, 'dataType', 'text', 'dataSpecs', JSON_OBJECT('dataType', 'text', 'length', '256')),
      JSON_OBJECT('identifier', 'confidence', 'name', '置信度', 'direction', 'output', 'paraOrder', 1, 'dataType', 'float', 'dataSpecs', JSON_OBJECT('dataType', 'float', 'min', '0.0', 'max', '100.0', 'step', '0.1', 'unit', '%', 'unitName', '百分比')),
      JSON_OBJECT('identifier', 'alarm_time', 'name', '告警时间', 'direction', 'output', 'paraOrder', 2, 'dataType', 'date')
    )
  ),
  '1', NOW(), '1', NOW(), 0, 1
);

-- 3.2-3.6: 其他事件
INSERT INTO iot_thing_model (product_id, product_key, identifier, name, description, type, event, creator, create_time, updater, update_time, deleted, tenant_id) VALUES
(62, 'dahua_camera_001', 'device_offline', '设备离线', '设备断开连接', 2, JSON_OBJECT('identifier', 'device_offline', 'name', '设备离线', 'type', 'error', 'required', false, 'method', 'thing.event.device_offline.post', 'outputParams', JSON_ARRAY(JSON_OBJECT('identifier', 'offline_time', 'name', '离线时间', 'direction', 'output', 'paraOrder', 0, 'dataType', 'date'), JSON_OBJECT('identifier', 'offline_reason', 'name', '离线原因', 'direction', 'output', 'paraOrder', 1, 'dataType', 'text', 'dataSpecs', JSON_OBJECT('dataType', 'text', 'length', '256')))), '1', NOW(), '1', NOW(), 0, 1),
(62, 'dahua_camera_001', 'device_online', '设备上线', '设备重新连接', 2, JSON_OBJECT('identifier', 'device_online', 'name', '设备上线', 'type', 'info', 'required', false, 'method', 'thing.event.device_online.post', 'outputParams', JSON_ARRAY(JSON_OBJECT('identifier', 'online_time', 'name', '上线时间', 'direction', 'output', 'paraOrder', 0, 'dataType', 'date'))), '1', NOW(), '1', NOW(), 0, 1),
(62, 'dahua_camera_001', 'video_loss', '视频丢失告警', '视频信号丢失', 2, JSON_OBJECT('identifier', 'video_loss', 'name', '视频丢失告警', 'type', 'error', 'required', false, 'method', 'thing.event.video_loss.post', 'outputParams', JSON_ARRAY(JSON_OBJECT('identifier', 'alarm_time', 'name', '告警时间', 'direction', 'output', 'paraOrder', 0, 'dataType', 'date'))), '1', NOW(), '1', NOW(), 0, 1),
(62, 'dahua_camera_001', 'storage_full', '存储已满告警', 'SD卡或硬盘已满', 2, JSON_OBJECT('identifier', 'storage_full', 'name', '存储已满告警', 'type', 'alert', 'required', false, 'method', 'thing.event.storage_full.post', 'outputParams', JSON_ARRAY(JSON_OBJECT('identifier', 'alarm_time', 'name', '告警时间', 'direction', 'output', 'paraOrder', 0, 'dataType', 'date'), JSON_OBJECT('identifier', 'storage_capacity', 'name', '存储容量', 'direction', 'output', 'paraOrder', 1, 'dataType', 'int', 'dataSpecs', JSON_OBJECT('dataType', 'int', 'min', '0', 'max', '10000000', 'step', '1', 'unit', 'MB', 'unitName', '兆字节')))), '1', NOW(), '1', NOW(), 0, 1),
(62, 'dahua_camera_001', 'high_temperature', '高温告警', '设备温度过高', 2, JSON_OBJECT('identifier', 'high_temperature', 'name', '高温告警', 'type', 'alert', 'required', false, 'method', 'thing.event.high_temperature.post', 'outputParams', JSON_ARRAY(JSON_OBJECT('identifier', 'temperature', 'name', '当前温度', 'direction', 'output', 'paraOrder', 0, 'dataType', 'float', 'dataSpecs', JSON_OBJECT('dataType', 'float', 'min', '-40.0', 'max', '85.0', 'step', '0.1', 'unit', '℃', 'unitName', '摄氏度')), JSON_OBJECT('identifier', 'alarm_time', 'name', '告警时间', 'direction', 'output', 'paraOrder', 1, 'dataType', 'date'))), '1', NOW(), '1', NOW(), 0, 1);

-- ==========================================
-- 4. 导入服务（Services）
-- ==========================================

-- 4.1 云台控制
INSERT INTO iot_thing_model (
  product_id, product_key, identifier, name, description,
  type, service,
  creator, create_time, updater, update_time, deleted, tenant_id
) VALUES (
  62, 'dahua_camera_001', 'ptz_control', '云台控制', '控制云台转动和变倍',
  3, -- type=3 表示服务
  JSON_OBJECT(
    'identifier', 'ptz_control',
    'name', '云台控制',
    'callType', 'async',
    'required', false,
    'method', 'thing.service.ptz_control',
    'inputParams', JSON_ARRAY(
      JSON_OBJECT(
        'identifier', 'action',
        'name', '动作',
        'direction', 'input',
        'paraOrder', 0,
        'dataType', 'enum',
        'dataSpecsList', JSON_ARRAY(
          JSON_OBJECT('dataType', 'enum', 'name', '向上', 'value', 1),
          JSON_OBJECT('dataType', 'enum', 'name', '向下', 'value', 2),
          JSON_OBJECT('dataType', 'enum', 'name', '向左', 'value', 3),
          JSON_OBJECT('dataType', 'enum', 'name', '向右', 'value', 4),
          JSON_OBJECT('dataType', 'enum', 'name', '放大', 'value', 5),
          JSON_OBJECT('dataType', 'enum', 'name', '缩小', 'value', 6),
          JSON_OBJECT('dataType', 'enum', 'name', '停止', 'value', 0)
        )
      ),
      JSON_OBJECT('identifier', 'speed', 'name', '速度', 'direction', 'input', 'paraOrder', 1, 'dataType', 'int', 'dataSpecs', JSON_OBJECT('dataType', 'int', 'min', '1', 'max', '8', 'step', '1', 'defaultValue', '5', 'unit', '', 'unitName', '级'))
    ),
    'outputParams', JSON_ARRAY(
      JSON_OBJECT('identifier', 'success', 'name', '执行结果', 'direction', 'output', 'paraOrder', 0, 'dataType', 'bool', 'dataSpecsList', JSON_ARRAY(JSON_OBJECT('dataType', 'bool', 'name', '失败', 'value', 0), JSON_OBJECT('dataType', 'bool', 'name', '成功', 'value', 1)))
    )
  ),
  '1', NOW(), '1', NOW(), 0, 1
);

-- 4.2-4.5: 其他服务
INSERT INTO iot_thing_model (product_id, product_key, identifier, name, description, type, service, creator, create_time, updater, update_time, deleted, tenant_id) VALUES
(62, 'dahua_camera_001', 'capture_snapshot', '抓拍快照', '立即抓拍一张快照', 3, JSON_OBJECT('identifier', 'capture_snapshot', 'name', '抓拍快照', 'callType', 'async', 'required', false, 'method', 'thing.service.capture_snapshot', 'inputParams', JSON_ARRAY(), 'outputParams', JSON_ARRAY(JSON_OBJECT('identifier', 'snapshot_url', 'name', '快照地址', 'direction', 'output', 'paraOrder', 0, 'dataType', 'text', 'dataSpecs', JSON_OBJECT('dataType', 'text', 'length', '256')), JSON_OBJECT('identifier', 'success', 'name', '执行结果', 'direction', 'output', 'paraOrder', 1, 'dataType', 'bool', 'dataSpecsList', JSON_ARRAY(JSON_OBJECT('dataType', 'bool', 'name', '失败', 'value', 0), JSON_OBJECT('dataType', 'bool', 'name', '成功', 'value', 1))))), '1', NOW(), '1', NOW(), 0, 1),
(62, 'dahua_camera_001', 'start_record', '开始录像', '手动开始录像', 3, JSON_OBJECT('identifier', 'start_record', 'name', '开始录像', 'callType', 'async', 'required', false, 'method', 'thing.service.start_record', 'inputParams', JSON_ARRAY(JSON_OBJECT('identifier', 'duration', 'name', '录像时长', 'direction', 'input', 'paraOrder', 0, 'dataType', 'int', 'dataSpecs', JSON_OBJECT('dataType', 'int', 'min', '10', 'max', '7200', 'step', '1', 'defaultValue', '3600', 'unit', 's', 'unitName', '秒'))), 'outputParams', JSON_ARRAY(JSON_OBJECT('identifier', 'success', 'name', '执行结果', 'direction', 'output', 'paraOrder', 0, 'dataType', 'bool', 'dataSpecsList', JSON_ARRAY(JSON_OBJECT('dataType', 'bool', 'name', '失败', 'value', 0), JSON_OBJECT('dataType', 'bool', 'name', '成功', 'value', 1))))), '1', NOW(), '1', NOW(), 0, 1),
(62, 'dahua_camera_001', 'stop_record', '停止录像', '停止手动录像', 3, JSON_OBJECT('identifier', 'stop_record', 'name', '停止录像', 'callType', 'async', 'required', false, 'method', 'thing.service.stop_record', 'inputParams', JSON_ARRAY(), 'outputParams', JSON_ARRAY(JSON_OBJECT('identifier', 'success', 'name', '执行结果', 'direction', 'output', 'paraOrder', 0, 'dataType', 'bool', 'dataSpecsList', JSON_ARRAY(JSON_OBJECT('dataType', 'bool', 'name', '失败', 'value', 0), JSON_OBJECT('dataType', 'bool', 'name', '成功', 'value', 1))))), '1', NOW(), '1', NOW(), 0, 1),
(62, 'dahua_camera_001', 'reboot', '重启设备', '重启摄像头设备', 3, JSON_OBJECT('identifier', 'reboot', 'name', '重启设备', 'callType', 'async', 'required', false, 'method', 'thing.service.reboot', 'inputParams', JSON_ARRAY(), 'outputParams', JSON_ARRAY(JSON_OBJECT('identifier', 'success', 'name', '执行结果', 'direction', 'output', 'paraOrder', 0, 'dataType', 'bool', 'dataSpecsList', JSON_ARRAY(JSON_OBJECT('dataType', 'bool', 'name', '失败', 'value', 0), JSON_OBJECT('dataType', 'bool', 'name', '成功', 'value', 1))))), '1', NOW(), '1', NOW(), 0, 1);

-- ==========================================
-- 5. 验证导入结果
-- ==========================================

-- 查询产品
SELECT * FROM iot_product WHERE product_key = 'dahua_camera_001';

-- 查询物模型统计
SELECT 
  type,
  CASE 
    WHEN type = 1 THEN '属性'
    WHEN type = 2 THEN '事件'
    WHEN type = 3 THEN '服务'
  END AS type_name,
  COUNT(*) AS count
FROM iot_thing_model
WHERE product_key = 'dahua_camera_001'
GROUP BY type;

-- 查询所有物模型
SELECT id, identifier, name, description, type
FROM iot_thing_model
WHERE product_key = 'dahua_camera_001'
ORDER BY type, id;

-- ==========================================
-- 导入完成！
-- ==========================================

-- 预期结果：
-- - 产品：1 条（大华网络摄像头）
-- - 属性：23 条
-- - 事件：6 条
-- - 服务：5 条
-- 总计：35 条物模型功能定义

