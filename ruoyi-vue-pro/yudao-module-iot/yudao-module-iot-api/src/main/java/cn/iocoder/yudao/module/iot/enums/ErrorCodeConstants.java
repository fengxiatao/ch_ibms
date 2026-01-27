package cn.iocoder.yudao.module.iot.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/**
 * IoT 错误码枚举类
 * 
 * iot 系统，使用 1-050-000-000 段
 */
public interface ErrorCodeConstants {

    // ========== 摄像头相关 1-050-001-000 ==========
    ErrorCode CAMERA_NOT_EXISTS = new ErrorCode(1_050_001_001, "摄像头配置不存在");
    ErrorCode CAMERA_ALREADY_EXISTS = new ErrorCode(1_050_001_002, "摄像头配置已存在");
    ErrorCode CAMERA_CONNECTION_FAILED = new ErrorCode(1_050_001_003, "摄像头连接失败");
    
    // ========== 预置位相关 1-050-002-000 ==========
    ErrorCode PRESET_NOT_EXISTS = new ErrorCode(1_050_002_001, "预置位不存在");
    ErrorCode PRESET_ALREADY_EXISTS = new ErrorCode(1_050_002_002, "预置位已存在");
    ErrorCode PRESET_LIMIT_EXCEEDED = new ErrorCode(1_050_002_003, "预置位数量超过限制");
    
    // ========== 录像相关 1-050-003-000 ==========
    ErrorCode RECORDING_NOT_EXISTS = new ErrorCode(1_050_003_001, "录像记录不存在");
    ErrorCode RECORDING_ALREADY_STARTED = new ErrorCode(1_050_003_002, "录像已经开始");
    ErrorCode RECORDING_NOT_STARTED = new ErrorCode(1_050_003_003, "录像未开始");
    
    // ========== 报警相关 1-050_004-000 ==========
    ErrorCode ALARM_NOT_EXISTS = new ErrorCode(1_050_004_001, "报警记录不存在");
    
    // ========== 监控墙相关 1-050-005-000 ==========
    ErrorCode MONITOR_WALL_NOT_EXISTS = new ErrorCode(1_050_005_001, "监控墙不存在");
    ErrorCode MONITOR_WALL_ALREADY_EXISTS = new ErrorCode(1_050_005_002, "监控墙已存在");

    // ========== 报警配置相关 1-050-006-000 ==========
    ErrorCode ALERT_CONFIG_NOT_EXISTS = new ErrorCode(1_050_006_001, "报警配置不存在");

    // ========== 设备组相关 1-050-007-000 ==========
    ErrorCode DEVICE_GROUP_DELETE_FAIL_DEVICE_EXISTS = new ErrorCode(1_050_007_001, "设备组删除失败，组内还有设备");
    ErrorCode DEVICE_GROUP_NOT_EXISTS = new ErrorCode(1_050_007_002, "设备组不存在");

    // ========== 设备消息相关 1-050-008-000 ==========
    ErrorCode DEVICE_DOWNSTREAM_FAILED_SERVER_ID_NULL = new ErrorCode(1_050_008_001, "设备下行消息失败，服务器ID为空");

    // ========== OTA固件相关 1-050-009-000 ==========
    ErrorCode OTA_FIRMWARE_NOT_EXISTS = new ErrorCode(1_050_009_001, "OTA固件不存在");
    ErrorCode OTA_FIRMWARE_PRODUCT_VERSION_DUPLICATE = new ErrorCode(1_050_009_002, "OTA固件产品版本重复");

    // ========== 产品分类相关 1-050-010-000 ==========
    ErrorCode PRODUCT_CATEGORY_NOT_EXISTS = new ErrorCode(1_050_010_001, "产品分类不存在");

    // ========== 数据规则相关 1-050-011-000 ==========
    ErrorCode DATA_RULE_NOT_EXISTS = new ErrorCode(1_050_011_001, "数据规则不存在");

    // ========== 数据接收器相关 1-050-012-000 ==========
    ErrorCode DATA_SINK_DELETE_FAIL_USED_BY_RULE = new ErrorCode(1_050_012_001, "数据接收器删除失败，正在被规则使用");
    ErrorCode DATA_SINK_NOT_EXISTS = new ErrorCode(1_050_012_002, "数据接收器不存在");

    // ========== 场景规则相关 1-050-013-000 ==========
    ErrorCode RULE_SCENE_NOT_EXISTS = new ErrorCode(1_050_013_001, "场景规则不存在");

    // ========== 任务相关 1-050-014-000 ==========
    ErrorCode JOB_TYPE_DEFINITION_NOT_EXISTS = new ErrorCode(1_050_014_001, "任务类型定义不存在");
    ErrorCode TASK_CONFIG_NOT_EXISTS = new ErrorCode(1_050_014_002, "任务配置不存在");

    // ========== 设备相关 1-050-015-000 ==========
    ErrorCode DEVICE_NOT_EXISTS = new ErrorCode(1_050_015_001, "设备不存在");
    ErrorCode DEVICE_NAME_EXISTS = new ErrorCode(1_050_015_002, "设备名称已存在");
    ErrorCode DEVICE_SERIAL_NUMBER_EXISTS = new ErrorCode(1_050_015_003, "设备序列号已存在");
    ErrorCode DEVICE_HAS_CHILDREN = new ErrorCode(1_050_015_004, "设备有子设备");
    ErrorCode DEVICE_GATEWAY_NOT_EXISTS = new ErrorCode(1_050_015_005, "设备网关不存在");
    ErrorCode DEVICE_NOT_GATEWAY = new ErrorCode(1_050_015_006, "设备不是网关");
    ErrorCode DEVICE_KEY_EXISTS = new ErrorCode(1_050_015_007, "设备密钥已存在");
    ErrorCode DEVICE_IMPORT_LIST_IS_EMPTY = new ErrorCode(1_050_015_008, "设备导入列表为空");
    ErrorCode DEVICE_NAME_EXISTS_IN_FLOOR = new ErrorCode(1_050_015_009, "设备名称在该楼层已存在");
    ErrorCode DEVICE_ALREADY_IMPORTED_FROM_DXF = new ErrorCode(1_050_015_010, "该DXF设备已导入");
    ErrorCode DEVICE_STATUS_BATCH_QUERY_LIMIT_EXCEEDED = new ErrorCode(1_050_015_011, "批量查询设备状态超过限制，最多100个");
    ErrorCode DEVICE_STATE_INVALID = new ErrorCode(1_050_015_012, "设备状态无效");
    ErrorCode DEVICE_KEEPALIVE_CHECK_FAILED = new ErrorCode(1_050_015_013, "设备保活检测失败");
    ErrorCode DEVICE_HEARTBEAT_TIMEOUT = new ErrorCode(1_050_015_014, "设备心跳超时");
    ErrorCode DEVICE_STATE_SYNC_FAILED = new ErrorCode(1_050_015_015, "设备状态同步失败");
    ErrorCode DEVICE_WEBSOCKET_PUSH_FAILED = new ErrorCode(1_050_015_016, "设备状态WebSocket推送失败");

    // ========== 产品相关 1-050-016-000 ==========
    ErrorCode PRODUCT_NOT_EXISTS = new ErrorCode(1_050_016_001, "产品不存在");
    ErrorCode PRODUCT_KEY_EXISTS = new ErrorCode(1_050_016_002, "产品密钥已存在");
    ErrorCode PRODUCT_DELETE_FAIL_HAS_DEVICE = new ErrorCode(1_050_016_003, "产品删除失败，还有设备");
    ErrorCode PRODUCT_STATUS_NOT_DELETE = new ErrorCode(1_050_016_004, "产品状态不允许删除");
    ErrorCode PRODUCT_IMAGE_INVALID_FORMAT = new ErrorCode(1_050_016_005, "产品图片格式无效");
    ErrorCode PRODUCT_IMAGE_URL_TOO_LONG = new ErrorCode(1_050_016_006, "产品图片URL过长");

    // ========== 物模型相关 1-050-017-000 ==========
    ErrorCode THING_MODEL_NOT_EXISTS = new ErrorCode(1_050_017_001, "物模型不存在");
    ErrorCode THING_MODEL_IDENTIFIER_INVALID = new ErrorCode(1_050_017_002, "物模型标识符无效");
    ErrorCode THING_MODEL_IDENTIFIER_EXISTS = new ErrorCode(1_050_017_003, "物模型标识符已存在");
    ErrorCode THING_MODEL_NAME_EXISTS = new ErrorCode(1_050_017_004, "物模型名称已存在");
    ErrorCode PRODUCT_STATUS_NOT_ALLOW_THING_MODEL = new ErrorCode(1_050_017_005, "产品状态不允许配置物模型");

    // ========== OTA任务相关 1-050-018-000 ==========
    ErrorCode OTA_TASK_NOT_EXISTS = new ErrorCode(1_050_018_001, "OTA任务不存在");
    ErrorCode OTA_TASK_CREATE_FAIL_NAME_DUPLICATE = new ErrorCode(1_050_018_002, "OTA任务创建失败，名称重复");
    ErrorCode OTA_TASK_CANCEL_FAIL_STATUS_END = new ErrorCode(1_050_018_003, "OTA任务取消失败，状态已结束");
    ErrorCode OTA_TASK_CREATE_FAIL_DEVICE_FIRMWARE_EXISTS = new ErrorCode(1_050_018_004, "OTA任务创建失败，设备固件已存在");
    ErrorCode OTA_TASK_CREATE_FAIL_DEVICE_OTA_IN_PROCESS = new ErrorCode(1_050_018_005, "OTA任务创建失败，设备OTA进行中");
    ErrorCode OTA_TASK_CREATE_FAIL_DEVICE_EMPTY = new ErrorCode(1_050_018_006, "OTA任务创建失败，设备列表为空");

    // ========== OTA任务记录相关 1-050-019-000 ==========
    ErrorCode OTA_TASK_RECORD_NOT_EXISTS = new ErrorCode(1_050_019_001, "OTA任务记录不存在");
    ErrorCode OTA_TASK_RECORD_CANCEL_FAIL_STATUS_ERROR = new ErrorCode(1_050_019_002, "OTA任务记录取消失败，状态错误");
    ErrorCode OTA_TASK_RECORD_UPDATE_PROGRESS_FAIL_NO_EXISTS = new ErrorCode(1_050_019_003, "OTA任务记录更新进度失败，记录不存在");

    // ========== 设备位置相关 1-050-020-000 ==========
    ErrorCode DEVICE_LOCATION_NOT_EXISTS = new ErrorCode(1_050_020_001, "设备位置不存在");
    ErrorCode DEVICE_LOCATION_ALREADY_EXISTS = new ErrorCode(1_050_020_002, "设备位置已存在");

    // ========== 园区相关 1-050-021-000 ==========
    ErrorCode CAMPUS_NOT_EXISTS = new ErrorCode(1_050_021_001, "园区不存在");
    ErrorCode CAMPUS_HAS_BUILDINGS = new ErrorCode(1_050_021_002, "园区有建筑物");
    ErrorCode CAMPUS_CODE_DUPLICATE = new ErrorCode(1_050_021_003, "园区编码重复");

    // ========== 建筑物相关 1-050-022-000 ==========
    ErrorCode BUILDING_NOT_EXISTS = new ErrorCode(1_050_022_001, "建筑物不存在");
    ErrorCode BUILDING_HAS_FLOORS = new ErrorCode(1_050_022_002, "建筑物有楼层");
    ErrorCode BUILDING_CODE_DUPLICATE = new ErrorCode(1_050_022_003, "建筑物编码重复");
    ErrorCode BUILDING_NOT_BELONG_TO_CAMPUS = new ErrorCode(1_050_022_004, "建筑不属于该园区");

    // ========== 楼层相关 1-050-023-000 ==========
    ErrorCode FLOOR_NOT_EXISTS = new ErrorCode(1_050_023_001, "楼层不存在");
    ErrorCode FLOOR_DXF_NOT_BOUND = new ErrorCode(1_050_023_002, "该楼层没有绑定DXF文件");
    ErrorCode FLOOR_DXF_FILE_NOT_EXISTS = new ErrorCode(1_050_023_003, "DXF文件不存在");
    ErrorCode FLOOR_NOT_BELONG_TO_BUILDING = new ErrorCode(1_050_023_004, "楼层不属于该建筑");

    // ========== 区域相关 1-050-046-000 ==========
    ErrorCode AREA_NOT_EXISTS = new ErrorCode(1_050_046_001, "区域不存在");
    ErrorCode AREA_NOT_BELONG_TO_FLOOR = new ErrorCode(1_050_046_002, "区域不属于该楼层");
    ErrorCode AREA_NOT_BELONG_TO_BUILDING = new ErrorCode(1_050_046_003, "区域不属于该建筑");

    // ========== 巡更计划相关 1-050-024-000 ==========
    ErrorCode PATROL_PLAN_NOT_EXISTS = new ErrorCode(1_050_024_001, "巡更计划不存在");
    ErrorCode PATROL_PLAN_ALREADY_ENABLED = new ErrorCode(1_050_024_002, "巡更计划已启用");

    // ========== 巡更任务相关 1-050-025-000 ==========
    ErrorCode PATROL_TASK_NOT_EXISTS = new ErrorCode(1_050_025_001, "巡更任务不存在");
    ErrorCode PATROL_TASK_STATUS_ERROR = new ErrorCode(1_050_025_002, "巡更任务状态错误");

    // ========== 巡更点位相关 1-050-026-000 ==========
    ErrorCode PATROL_POINT_NOT_EXISTS = new ErrorCode(1_050_026_001, "巡更点位不存在");
    ErrorCode PATROL_POINT_ALREADY_IN_USE = new ErrorCode(1_050_026_002, "巡更点位正在被使用");

    // ========== 巡更线路相关 1-050-027-000 ==========
    ErrorCode PATROL_ROUTE_NOT_EXISTS = new ErrorCode(1_050_027_001, "巡更线路不存在");
    ErrorCode PATROL_ROUTE_ALREADY_IN_USE = new ErrorCode(1_050_027_002, "巡更线路正在被使用");

    // ========== 巡更记录相关 1-050-028-000 ==========
    ErrorCode PATROL_RECORD_NOT_EXISTS = new ErrorCode(1_050_028_001, "巡更记录不存在");

    // ========== 门禁授权相关 1-050-029-000 ==========
    ErrorCode ACCESS_AUTHORIZATION_NOT_EXISTS = new ErrorCode(1_050_029_001, "门禁授权不存在");
    ErrorCode ACCESS_AUTHORIZATION_TIME_INVALID = new ErrorCode(1_050_029_002, "授权时间无效");

    // ========== 门禁记录相关 1-050-030-000 ==========
    ErrorCode ACCESS_RECORD_NOT_EXISTS = new ErrorCode(1_050_030_001, "门禁记录不存在");

    // ========== 门禁告警相关 1-050-031-000 ==========
    ErrorCode ACCESS_ALARM_NOT_EXISTS = new ErrorCode(1_050_031_001, "门禁告警不存在");
    ErrorCode ACCESS_ALARM_ALREADY_HANDLED = new ErrorCode(1_050_031_002, "门禁告警已处理");

    // ========== 门禁下发相关 1-050-032-000 ==========
    ErrorCode ACCESS_DISPATCH_NOT_EXISTS = new ErrorCode(1_050_032_001, "门禁下发记录不存在");

    // ========== 门组相关 1-050-033-000 ==========
    ErrorCode DOOR_GROUP_NOT_EXISTS = new ErrorCode(1_050_033_001, "门组不存在");
    ErrorCode DOOR_GROUP_ALREADY_IN_USE = new ErrorCode(1_050_033_002, "门组正在被使用");

    // ========== 门岗相关 1-050-034-000 ==========
    ErrorCode DOOR_POST_NOT_EXISTS = new ErrorCode(1_050_034_001, "门岗不存在");

    // ========== 访客记录相关 1-050-035-000 ==========
    ErrorCode VISITOR_RECORD_NOT_EXISTS = new ErrorCode(1_050_035_001, "访客记录不存在");

    // ========== 访客申请相关 1-050-036-000 ==========
    ErrorCode VISITOR_APPLICATION_NOT_EXISTS = new ErrorCode(1_050_036_001, "访客申请不存在");
    ErrorCode VISITOR_APPLICATION_ALREADY_APPROVED = new ErrorCode(1_050_036_002, "访客申请已审批");

    // ========== 访客白名单相关 1-050-037-000 ==========
    ErrorCode VISITOR_WHITELIST_NOT_EXISTS = new ErrorCode(1_050_037_001, "访客白名单不存在");
    ErrorCode VISITOR_WHITELIST_ALREADY_EXISTS = new ErrorCode(1_050_037_002, "访客白名单已存在");

    // ========== 访客黑名单相关 1-050-038-000 ==========
    ErrorCode VISITOR_BLACKLIST_NOT_EXISTS = new ErrorCode(1_050_038_001, "访客黑名单不存在");
    ErrorCode VISITOR_BLACKLIST_ALREADY_EXISTS = new ErrorCode(1_050_038_002, "访客黑名单已存在");

    // ========== 车辆记录相关 1-050-039-000 ==========
    ErrorCode VEHICLE_RECORD_NOT_EXISTS = new ErrorCode(1_050_039_001, "车辆记录不存在");

    // ========== 车辆白名单相关 1-050-040-000 ==========
    ErrorCode VEHICLE_WHITELIST_NOT_EXISTS = new ErrorCode(1_050_040_001, "车辆白名单不存在");

    // ========== 车辆黑名单相关 1-050-041-000 ==========
    ErrorCode VEHICLE_BLACKLIST_NOT_EXISTS = new ErrorCode(1_050_041_001, "车辆黑名单不存在");

    // ========== 周界告警相关 1-050-042-000 ==========
    ErrorCode PERIMETER_ALARM_NOT_EXISTS = new ErrorCode(1_050_042_001, "周界告警不存在");

    // ========== 设备通道相关 1-050-043-000 ==========
    ErrorCode CHANNEL_NOT_EXISTS = new ErrorCode(1_050_043_001, "设备通道不存在");
    ErrorCode CHANNEL_NO_EXISTS = new ErrorCode(1_050_043_002, "通道号已存在");
    ErrorCode CHANNEL_ALREADY_IN_USE = new ErrorCode(1_050_043_003, "通道正在被使用");

    // ========== 视频巡检任务相关 1-050-044-000 ==========
    ErrorCode VIDEO_INSPECTION_TASK_NOT_EXISTS = new ErrorCode(1_050_044_001, "视频巡检任务不存在");
    ErrorCode VIDEO_INSPECTION_TASK_NAME_DUPLICATE = new ErrorCode(1_050_044_002, "视频巡检任务名称已存在");
    ErrorCode VIDEO_INSPECTION_TASK_STATUS_ERROR = new ErrorCode(1_050_044_003, "视频巡检任务状态错误");

    // ========== 视频轮巡计划相关 1-050-045-000 ==========
    ErrorCode VIDEO_PATROL_PLAN_NOT_EXISTS = new ErrorCode(1_050_045_001, "轮巡计划不存在");
    ErrorCode VIDEO_PATROL_PLAN_CODE_DUPLICATE = new ErrorCode(1_050_045_002, "轮巡计划编码已存在");
    ErrorCode VIDEO_PATROL_PLAN_HAS_TASKS = new ErrorCode(1_050_045_003, "轮巡计划下存在任务，无法删除");
    ErrorCode VIDEO_PATROL_PLAN_NOT_ENABLED = new ErrorCode(1_050_045_004, "轮巡计划未启用");
    ErrorCode VIDEO_PATROL_PLAN_ALREADY_RUNNING = new ErrorCode(1_050_045_005, "轮巡计划已在运行中");
    ErrorCode VIDEO_PATROL_PLAN_NOT_RUNNING = new ErrorCode(1_050_045_006, "轮巡计划未在运行中");

    // ========== 视频轮巡任务相关 1-050-047-000 ==========
    ErrorCode VIDEO_PATROL_TASK_NOT_EXISTS = new ErrorCode(1_050_047_001, "轮巡任务不存在");
    ErrorCode VIDEO_PATROL_TASK_CODE_DUPLICATE = new ErrorCode(1_050_047_002, "轮巡任务编码已存在");
    ErrorCode VIDEO_PATROL_TASK_HAS_SCENES = new ErrorCode(1_050_047_003, "轮巡任务下存在场景，无法删除");
    ErrorCode VIDEO_PATROL_TASK_NOT_ENABLED = new ErrorCode(1_050_047_004, "轮巡任务未启用");
    ErrorCode VIDEO_PATROL_TASK_ALREADY_RUNNING = new ErrorCode(1_050_047_005, "轮巡任务已在运行中");
    ErrorCode VIDEO_PATROL_TASK_NOT_RUNNING = new ErrorCode(1_050_047_006, "轮巡任务未在运行中");

    // ========== 视频轮巡场景相关 1-050-048-000 ==========
    ErrorCode VIDEO_PATROL_SCENE_NOT_EXISTS = new ErrorCode(1_050_048_001, "轮巡场景不存在");

    // ========== 视频轮巡记录相关 1-050-049-000 ==========
    ErrorCode VIDEO_PATROL_RECORD_NOT_EXISTS = new ErrorCode(1_050_049_001, "轮巡记录不存在");
    ErrorCode VIDEO_PATROL_RECORD_ALREADY_HANDLED = new ErrorCode(1_050_049_002, "轮巡记录已处理");

    // ========== 视频定时轮巡计划相关 1-050-050-000 ==========
    ErrorCode VIDEO_PATROL_SCHEDULE_NOT_EXISTS = new ErrorCode(1_050_050_001, "定时轮巡计划不存在");
    ErrorCode VIDEO_PATROL_SCHEDULE_NAME_DUPLICATE = new ErrorCode(1_050_050_002, "定时轮巡计划名称已存在");
    ErrorCode VIDEO_PATROL_SCHEDULE_TIME_INVALID = new ErrorCode(1_050_050_003, "定时轮巡计划时间无效");

    // ========== 摄像头预设点相关 1-050-051-000 ==========
    ErrorCode CAMERA_PRESET_NOT_EXISTS = new ErrorCode(1_050_051_001, "摄像头预设点不存在");
    ErrorCode CAMERA_PRESET_NO_EXISTS = new ErrorCode(1_050_051_002, "预设点编号已存在");
    ErrorCode CAMERA_PRESET_USED_BY_CRUISE = new ErrorCode(1_050_051_003, "预设点被巡航路线使用中，无法删除");

    // ========== 摄像头巡航相关 1-050-052-000 ==========
    ErrorCode CAMERA_CRUISE_NOT_EXISTS = new ErrorCode(1_050_052_001, "巡航路线不存在");
    ErrorCode CAMERA_CRUISE_NAME_EXISTS = new ErrorCode(1_050_052_002, "巡航路线名称已存在");
    ErrorCode CAMERA_CRUISE_NO_POINTS = new ErrorCode(1_050_052_003, "巡航路线没有预设点");
    ErrorCode CAMERA_CRUISE_POINT_NOT_EXISTS = new ErrorCode(1_050_052_004, "巡航点不存在");
    ErrorCode CAMERA_CRUISE_POINTS_TOO_FEW = new ErrorCode(1_050_052_005, "巡航路线至少需要2个预设点");

    // ========== OPC防区配置相关 1-050-053-000 ==========
    ErrorCode OPC_ZONE_CONFIG_NOT_EXISTS = new ErrorCode(1_050_053_001, "OPC防区配置不存在");
    ErrorCode OPC_ZONE_CONFIG_EXISTS = new ErrorCode(1_050_053_002, "OPC防区配置已存在");

    // ========== 报警主机相关 1-050-054-000 ==========
    ErrorCode ALARM_HOST_NOT_EXISTS = new ErrorCode(1_050_054_001, "报警主机不存在");
    ErrorCode ALARM_HOST_DEVICE_NOT_EXISTS = new ErrorCode(1_050_054_002, "报警主机关联设备不存在");
    ErrorCode ALARM_HOST_OFFLINE = new ErrorCode(1_050_054_003, "报警主机离线");

    // ========== 报警主机分区相关 1-050-055-000 ==========
    ErrorCode ALARM_PARTITION_NOT_EXISTS = new ErrorCode(1_050_055_001, "报警主机分区不存在");

    // ========== 报警主机防区相关 1-050-056-000 ==========
    ErrorCode ALARM_ZONE_NOT_EXISTS = new ErrorCode(1_050_056_001, "报警主机防区不存在");

    // ========== 门禁组织架构相关 1-050-057-000 ==========
    ErrorCode ACCESS_DEPARTMENT_NOT_EXISTS = new ErrorCode(1_050_057_001, "门禁部门不存在");
    ErrorCode ACCESS_DEPARTMENT_CODE_DUPLICATE = new ErrorCode(1_050_057_002, "门禁部门编码重复");
    ErrorCode ACCESS_DEPARTMENT_PARENT_NOT_EXISTS = new ErrorCode(1_050_057_003, "父部门不存在");
    ErrorCode ACCESS_DEPARTMENT_PARENT_ERROR = new ErrorCode(1_050_057_004, "父部门不能是自己");
    ErrorCode ACCESS_DEPARTMENT_HAS_CHILDREN = new ErrorCode(1_050_057_005, "部门下有子部门，无法删除");
    ErrorCode ACCESS_DEPARTMENT_HAS_PERSON = new ErrorCode(1_050_057_006, "部门下有人员，无法删除");
    ErrorCode ACCESS_DEPARTMENT_NAME_DUPLICATE = new ErrorCode(1_050_057_007, "同级部门名称已存在");

    // ========== 门禁人员相关 1-050-058-000 ==========
    ErrorCode ACCESS_PERSON_NOT_EXISTS = new ErrorCode(1_050_058_001, "门禁人员不存在");
    ErrorCode ACCESS_PERSON_CODE_DUPLICATE = new ErrorCode(1_050_058_002, "人员编号重复");
    ErrorCode ACCESS_PERSON_EXPIRED = new ErrorCode(1_050_058_003, "人员已过期");
    ErrorCode ACCESS_CARD_NO_DUPLICATE = new ErrorCode(1_050_058_004, "卡号已存在");
    ErrorCode ACCESS_PERSON_FACE_UPLOAD_FAILED = new ErrorCode(1_050_058_005, "人脸照片上传失败");
    ErrorCode ACCESS_PERSON_BATCH_CREATE_FAILED = new ErrorCode(1_050_058_006, "批量创建人员失败，存在重复编号或卡号");

    // ========== 门禁权限组相关 1-050-059-000 ==========
    ErrorCode ACCESS_PERMISSION_GROUP_NOT_EXISTS = new ErrorCode(1_050_059_001, "权限组不存在");
    ErrorCode ACCESS_PERMISSION_GROUP_HAS_PERSON = new ErrorCode(1_050_059_002, "权限组下有人员，无法删除");

    // ========== 门禁授权任务相关 1-050-060-000 ==========
    ErrorCode ACCESS_AUTH_TASK_NOT_EXISTS = new ErrorCode(1_050_060_001, "授权任务不存在");
    ErrorCode ACCESS_AUTH_TASK_CANNOT_RETRY = new ErrorCode(1_050_060_002, "任务状态不允许重试");
    ErrorCode ACCESS_AUTH_TASK_DETAIL_NOT_EXISTS = new ErrorCode(1_050_060_003, "授权任务明细不存在");
    ErrorCode ACCESS_AUTH_TASK_GROUP_ID_REQUIRED = new ErrorCode(1_050_060_004, "权限组下发任务必须指定权限组ID");
    ErrorCode ACCESS_AUTH_TASK_PERSON_ID_REQUIRED = new ErrorCode(1_050_060_005, "单人下发/撤销任务必须指定人员ID");
    ErrorCode ACCESS_AUTH_TASK_STATUS_INVALID = new ErrorCode(1_050_060_006, "任务状态不允许此操作");
    ErrorCode ACCESS_AUTH_TASK_NO_FAILED_DETAILS = new ErrorCode(1_050_060_007, "没有失败的任务明细可重试");

    // ========== 门禁设备相关 1-050-061-000 ==========
    ErrorCode ACCESS_DEVICE_NOT_EXISTS = new ErrorCode(1_050_061_001, "门禁设备不存在");
    ErrorCode ACCESS_DEVICE_OFFLINE = new ErrorCode(1_050_061_002, "门禁设备离线");
    ErrorCode ACCESS_DEVICE_ACTIVATE_FAILED = new ErrorCode(1_050_061_003, "门禁设备激活失败");

    // ========== 门禁卡相关 1-050-062-000 ==========
    ErrorCode ACCESS_CARD_ADD_FAILED = new ErrorCode(1_050_062_001, "添加门禁卡失败");
    ErrorCode ACCESS_CARD_UPDATE_FAILED = new ErrorCode(1_050_062_002, "更新门禁卡失败");
    ErrorCode ACCESS_CARD_DELETE_FAILED = new ErrorCode(1_050_062_003, "删除门禁卡失败");
    ErrorCode ACCESS_CARD_QUERY_FAILED = new ErrorCode(1_050_062_004, "查询门禁卡失败");
    ErrorCode ACCESS_CARD_CLEAR_FAILED = new ErrorCode(1_050_062_005, "清空门禁卡失败");

    // ========== 门禁通道相关 1-050-063-000 ==========
    ErrorCode ACCESS_CHANNEL_NOT_EXISTS = new ErrorCode(1_050_063_001, "门禁通道不存在");
    ErrorCode ACCESS_CHANNEL_OFFLINE = new ErrorCode(1_050_063_002, "门禁通道离线");
    ErrorCode ACCESS_CHANNEL_DISABLED = new ErrorCode(1_050_063_003, "门禁通道已禁用");

    // ========== 门禁事件日志相关 1-050-064-000 ==========
    ErrorCode ACCESS_EVENT_NOT_EXISTS = new ErrorCode(1_050_064_001, "门禁事件不存在");

    // ========== 门禁操作日志相关 1-050-065-000 ==========
    ErrorCode ACCESS_OPERATION_LOG_NOT_EXISTS = new ErrorCode(1_050_065_001, "门禁操作日志不存在");

    // ========== 门禁凭证相关 1-050-066-000 ==========
    ErrorCode ACCESS_CREDENTIAL_NOT_EXISTS = new ErrorCode(1_050_066_001, "门禁凭证不存在");
    ErrorCode ACCESS_CREDENTIAL_VERIFY_FAILED = new ErrorCode(1_050_066_002, "门禁凭证验证失败");
    ErrorCode ACCESS_CREDENTIAL_TYPE_INVALID = new ErrorCode(1_050_066_003, "凭证类型无效");
    ErrorCode ACCESS_CARD_NOT_LOST = new ErrorCode(1_050_066_004, "卡片未挂失，无法解挂");
    ErrorCode ACCESS_FINGERPRINT_INDEX_DUPLICATE = new ErrorCode(1_050_066_005, "该手指已录入指纹，请选择其他手指");
    ErrorCode ACCESS_FINGERPRINT_INDEX_INVALID = new ErrorCode(1_050_066_006, "指纹序号无效，应为0-9");
    ErrorCode ACCESS_FINGERPRINT_MAX_COUNT = new ErrorCode(1_050_066_007, "已达到最大指纹数量限制（10个）");
    ErrorCode ACCESS_PASSWORD_ALREADY_EXISTS = new ErrorCode(1_050_066_008, "该人员已设置开门密码，请使用修改功能");

    // ========== 德通设备相关 1-050-067-000 ==========
    ErrorCode DETONG_DEVICE_NOT_FOUND = new ErrorCode(1_050_067_001, "德通设备不存在");
    ErrorCode DETONG_DEVICE_OFFLINE = new ErrorCode(1_050_067_002, "德通设备离线");
    ErrorCode DETONG_DEVICE_CONTROL_FAILED = new ErrorCode(1_050_067_003, "德通设备控制失败");
    ErrorCode DETONG_DEVICE_CONFIG_FAILED = new ErrorCode(1_050_067_004, "德通设备配置失败");

    // ========== 德通报警相关 1-050-068-000 ==========
    ErrorCode DETONG_ALARM_NOT_EXISTS = new ErrorCode(1_050_068_001, "德通报警记录不存在");
    ErrorCode DETONG_ALARM_ALREADY_ACKNOWLEDGED = new ErrorCode(1_050_068_002, "德通报警已确认");

    // ========== 德通固件升级相关 1-050-069-000 ==========
    ErrorCode DETONG_FIRMWARE_NOT_EXISTS = new ErrorCode(1_050_069_001, "德通固件不存在");
    ErrorCode DETONG_UPGRADE_TASK_NOT_EXISTS = new ErrorCode(1_050_069_002, "德通升级任务不存在");
    ErrorCode DETONG_UPGRADE_TASK_CANNOT_CANCEL = new ErrorCode(1_050_069_003, "德通升级任务无法取消");

    // ========== 门禁授权记录相关 1-050-070-000 ==========
    ErrorCode ACCESS_AUTH_RECORD_NOT_EXISTS = new ErrorCode(1_050_070_001, "授权记录不存在");
    ErrorCode ACCESS_AUTH_RECORD_STATUS_NOT_FAILED = new ErrorCode(1_050_070_002, "授权记录状态不是失败，无法重试");
    ErrorCode ACCESS_AUTH_RETRY_FAILED = new ErrorCode(1_050_070_003, "授权重试失败");

    // ========== 长辉设备相关 1-050-074-000 ==========
    ErrorCode CHANGHUI_DEVICE_NOT_FOUND = new ErrorCode(1_050_074_001, "长辉设备不存在");
    ErrorCode CHANGHUI_DEVICE_OFFLINE = new ErrorCode(1_050_074_002, "长辉设备离线");
    ErrorCode CHANGHUI_DEVICE_CONTROL_FAILED = new ErrorCode(1_050_074_003, "长辉设备控制失败");
    ErrorCode CHANGHUI_DEVICE_CONFIG_FAILED = new ErrorCode(1_050_074_004, "长辉设备配置失败");

    // ========== 长辉报警相关 1-050-075-000 ==========
    ErrorCode CHANGHUI_ALARM_NOT_EXISTS = new ErrorCode(1_050_075_001, "长辉报警记录不存在");
    ErrorCode CHANGHUI_ALARM_ALREADY_ACKNOWLEDGED = new ErrorCode(1_050_075_002, "长辉报警已确认");

    // ========== 长辉固件升级相关 1-050-076-000 ==========
    ErrorCode CHANGHUI_FIRMWARE_NOT_EXISTS = new ErrorCode(1_050_076_001, "长辉固件不存在");
    ErrorCode CHANGHUI_UPGRADE_TASK_NOT_EXISTS = new ErrorCode(1_050_076_002, "长辉升级任务不存在");
    ErrorCode CHANGHUI_UPGRADE_TASK_CANNOT_CANCEL = new ErrorCode(1_050_076_003, "长辉升级任务无法取消");

    // ========== 停车场车场相关 1-050-077-000 ==========
    ErrorCode PARKING_LOT_NOT_EXISTS = new ErrorCode(1_050_077_001, "车场不存在");
    ErrorCode PARKING_LOT_NAME_EXISTS = new ErrorCode(1_050_077_002, "车场名称已存在");
    ErrorCode PARKING_LOT_HAS_LANES = new ErrorCode(1_050_077_003, "车场下有车道，无法删除");
    ErrorCode PARKING_LOT_HAS_GATES = new ErrorCode(1_050_077_004, "车场下有道闸，无法删除");

    // ========== 道闸设备相关 1-050-078-000 ==========
    ErrorCode PARKING_GATE_NOT_EXISTS = new ErrorCode(1_050_078_001, "道闸设备不存在");
    ErrorCode PARKING_GATE_OFFLINE = new ErrorCode(1_050_078_002, "道闸设备离线");
    ErrorCode PARKING_GATE_IP_EXISTS = new ErrorCode(1_050_078_003, "道闸设备IP地址已存在");

    // ========== 车道相关 1-050-079-000 ==========
    ErrorCode PARKING_LANE_NOT_EXISTS = new ErrorCode(1_050_079_001, "车道不存在");
    ErrorCode PARKING_LANE_CODE_EXISTS = new ErrorCode(1_050_079_002, "车道编码已存在");
    ErrorCode PARKING_LANE_HAS_GATES = new ErrorCode(1_050_079_003, "车道下有道闸，无法删除");

    // ========== 免费车相关 1-050-080-000 ==========
    ErrorCode PARKING_FREE_VEHICLE_NOT_EXISTS = new ErrorCode(1_050_080_001, "免费车不存在");
    ErrorCode PARKING_FREE_VEHICLE_PLATE_EXISTS = new ErrorCode(1_050_080_002, "免费车车牌号已存在");
    ErrorCode PARKING_FREE_VEHICLE_EXPIRED = new ErrorCode(1_050_080_003, "免费车已过期");

    // ========== 月租车相关 1-050-081-000 ==========
    ErrorCode PARKING_MONTHLY_VEHICLE_NOT_EXISTS = new ErrorCode(1_050_081_001, "月租车不存在");
    ErrorCode PARKING_MONTHLY_VEHICLE_PLATE_EXISTS = new ErrorCode(1_050_081_002, "月租车车牌号已存在");
    ErrorCode PARKING_MONTHLY_VEHICLE_EXPIRED = new ErrorCode(1_050_081_003, "月租车已过期");

    // ========== 放行规则相关 1-050-082-000 ==========
    ErrorCode PARKING_PASS_RULE_NOT_EXISTS = new ErrorCode(1_050_082_001, "放行规则不存在");
    ErrorCode PARKING_PASS_RULE_IN_USE = new ErrorCode(1_050_082_002, "放行规则正在使用中，无法删除");

    // ========== 收费规则相关 1-050-083-000 ==========
    ErrorCode PARKING_CHARGE_RULE_NOT_EXISTS = new ErrorCode(1_050_083_001, "收费规则不存在");
    ErrorCode PARKING_CHARGE_RULE_IN_USE = new ErrorCode(1_050_083_002, "收费规则正在使用中，无法删除");

    // ========== 收费规则应用相关 1-050-084-000 ==========
    ErrorCode PARKING_CHARGE_RULE_APPLY_NOT_EXISTS = new ErrorCode(1_050_084_001, "收费规则应用不存在");

    // ========== 在场车辆相关 1-050-085-000 ==========
    ErrorCode PARKING_PRESENT_VEHICLE_NOT_EXISTS = new ErrorCode(1_050_085_001, "在场车辆不存在");
    ErrorCode PARKING_PRESENT_VEHICLE_ALREADY_EXISTS = new ErrorCode(1_050_085_002, "车辆已在场");

    // ========== 停车记录相关 1-050-086-000 ==========
    ErrorCode PARKING_RECORD_NOT_EXISTS = new ErrorCode(1_050_086_001, "停车记录不存在");
    ErrorCode PARKING_RECORD_ALREADY_PAID = new ErrorCode(1_050_086_002, "停车费用已支付");
    ErrorCode PARKING_RECORD_NOT_IN_LOT = new ErrorCode(1_050_086_003, "车辆不在场");

    // ========== 月卡充值相关 1-050-087-000 ==========
    ErrorCode PARKING_MONTHLY_RECHARGE_NOT_EXISTS = new ErrorCode(1_050_087_001, "月卡充值记录不存在");

    // ========== 微信支付相关 1-050-088-000 ==========
    ErrorCode WECHAT_AUTH_FAILED = new ErrorCode(1_050_088_001, "微信授权失败");
    ErrorCode WECHAT_PAY_FAILED = new ErrorCode(1_050_088_002, "微信支付失败");
    ErrorCode WECHAT_PAY_ORDER_NOT_EXISTS = new ErrorCode(1_050_088_003, "支付订单不存在");
    ErrorCode WECHAT_PAY_SIGN_INVALID = new ErrorCode(1_050_088_004, "支付签名验证失败");
    ErrorCode WECHAT_REFUND_FAILED = new ErrorCode(1_050_088_005, "微信退款失败");
    ErrorCode WECHAT_REFUND_NOT_EXISTS = new ErrorCode(1_050_088_006, "退款记录不存在");
    ErrorCode WECHAT_REFUND_ALREADY_PROCESSED = new ErrorCode(1_050_088_007, "该订单已申请过退款");
    ErrorCode WECHAT_REFUND_AMOUNT_EXCEED = new ErrorCode(1_050_088_008, "退款金额不能超过支付金额");

    // ========== 电子巡更-人员相关 1-050-090-000 ==========
    ErrorCode EPATROL_PERSON_NOT_EXISTS = new ErrorCode(1_050_090_001, "巡更人员不存在");

    // ========== 电子巡更-点位相关 1-050-091-000 ==========
    ErrorCode EPATROL_POINT_NOT_EXISTS = new ErrorCode(1_050_091_001, "巡更点不存在");
    ErrorCode EPATROL_POINT_NO_EXISTS = new ErrorCode(1_050_091_002, "点位编号已存在");

    // ========== 电子巡更-路线相关 1-050-092-000 ==========
    ErrorCode EPATROL_ROUTE_NOT_EXISTS = new ErrorCode(1_050_092_001, "巡更路线不存在");

    // ========== 电子巡更-计划相关 1-050-093-000 ==========
    ErrorCode EPATROL_PLAN_NOT_EXISTS = new ErrorCode(1_050_093_001, "巡更计划不存在");

    // ========== 电子巡更-任务相关 1-050-094-000 ==========
    ErrorCode EPATROL_TASK_NOT_EXISTS = new ErrorCode(1_050_094_001, "巡更任务不存在");
    ErrorCode EPATROL_TASK_ALREADY_SUBMITTED = new ErrorCode(1_050_094_002, "巡更任务已提交");

}


