package cn.iocoder.yudao.module.iot.mq.consumer.device.handler;

import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.mq.message.changhui.ChanghuiAlarmMessage;
import cn.iocoder.yudao.module.iot.core.mq.message.changhui.ChanghuiDataReportMessage;
import cn.iocoder.yudao.module.iot.core.mq.message.changhui.ChanghuiUpgradeStatusMessage;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceEventLogDO;
import cn.iocoder.yudao.module.iot.dal.mysql.device.IotDeviceEventLogMapper;
import cn.iocoder.yudao.module.iot.enums.device.ChanghuiDeviceTypeConstants;
import cn.iocoder.yudao.module.iot.service.changhui.upgrade.ChanghuiUpgradeService;
import cn.iocoder.yudao.module.iot.websocket.DeviceMessagePushService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 长辉设备事件处理器
 * 
 * <p>处理长辉/德通等使用相同 TCP 协议的设备事件上报，包括：</p>
 * <ul>
 *   <li>数据上报事件 - 记录设备数据并推送 WebSocket 通知</li>
 *   <li>报警事件 - 记录设备报警并推送 WebSocket 通知</li>
 *   <li>升级状态事件 - 记录升级进度并推送 WebSocket 通知</li>
 *   <li>状态变更事件 - 记录设备状态变化</li>
 * </ul>
 * 
 * <p>Requirements: 1.7, 5.3-5.5, 11.1-11.8, 13.1</p>
 *
 * @author 长辉信息科技有限公司
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ChanghuiDeviceEventHandler implements DeviceEventHandler {

    private static final String LOG_PREFIX = "[ChanghuiDeviceEventHandler]";

    /**
     * 设备类型标识
     */
    private static final String DEVICE_TYPE = ChanghuiDeviceTypeConstants.CHANGHUI;

    /**
     * 事件类型常量 - 数据上报
     */
    public static final String EVENT_TYPE_DATA_REPORT = "DATA_REPORT";
    
    /**
     * 事件类型常量 - 报警
     */
    public static final String EVENT_TYPE_ALARM = "ALARM";
    
    /**
     * 事件类型常量 - 升级状态
     */
    public static final String EVENT_TYPE_UPGRADE_STATUS = "UPGRADE_STATUS";
    
    /**
     * 事件类型常量 - 升级进度
     */
    public static final String EVENT_TYPE_UPGRADE_PROGRESS = "UPGRADE_PROGRESS";
    
    /**
     * 事件类型常量 - 升级完成
     */
    public static final String EVENT_TYPE_UPGRADE_COMPLETE = "UPGRADE_COMPLETE";
    
    /**
     * 事件类型常量 - 升级失败
     */
    public static final String EVENT_TYPE_UPGRADE_FAILED = "UPGRADE_FAILED";
    
    /**
     * 事件类型常量 - 升级触发成功
     */
    public static final String EVENT_TYPE_TRIGGER_SUCCESS = "TRIGGER_SUCCESS";
    
    /**
     * 事件类型常量 - 升级触发失败
     */
    public static final String EVENT_TYPE_TRIGGER_FAILED = "TRIGGER_FAILED";
    
    /**
     * 事件类型常量 - URL接收成功
     */
    public static final String EVENT_TYPE_URL_RECEIVED = "URL_RECEIVED";
    
    /**
     * 事件类型常量 - URL接收失败
     */
    public static final String EVENT_TYPE_URL_RECEIVE_FAILED = "URL_RECEIVE_FAILED";
    
    /**
     * 事件类型常量 - 固件下载中
     */
    public static final String EVENT_TYPE_DOWNLOADING = "DOWNLOADING";
    
    /**
     * 事件类型常量 - 固件下载完成
     */
    public static final String EVENT_TYPE_DOWNLOAD_COMPLETE = "DOWNLOAD_COMPLETE";

    private final IotDeviceEventLogMapper eventLogMapper;
    private final DeviceMessagePushService deviceMessagePushService;
    private final ChanghuiUpgradeService changhuiUpgradeService;

    // TODO: 待任务 10 完成后注入 ChanghuiDataService
    // private final ChanghuiDataService changhuiDataService;
    
    // TODO: 待任务 11 完成后注入 ChanghuiAlarmService
    // private final ChanghuiAlarmService changhuiAlarmService;
    
    // TODO: 待任务 14 完成后注入 ChanghuiUpgradeService
    // private final ChanghuiUpgradeService changhuiUpgradeService;

    @Override
    public String getDeviceType() {
        return DEVICE_TYPE;
    }

    @Override
    public void handleEvent(IotDeviceMessage message) {
        if (message == null) {
            log.warn("{} 收到空消息，忽略", LOG_PREFIX);
            return;
        }

        Long deviceId = message.getDeviceId();
        Map<String, Object> params = extractParams(message);
        
        if (params == null || params.isEmpty()) {
            log.warn("{} 事件参数为空: deviceId={}", LOG_PREFIX, deviceId);
            return;
        }

        // 获取事件类型，优先从 params 中获取，其次从 method 获取
        String eventType = determineEventType(params, message.getMethod());

        log.info("{} 处理长辉设备事件: deviceId={}, eventType={}", LOG_PREFIX, deviceId, eventType);

        try {
            // 根据事件类型路由到不同的处理方法
            routeEventByType(deviceId, eventType, params);
        } catch (Exception e) {
            log.error("{} 处理长辉设备事件失败: deviceId={}, eventType={}", 
                    LOG_PREFIX, deviceId, eventType, e);
        }
    }

    /**
     * 根据事件类型路由到不同的处理方法
     * 
     * <p>Requirements: 1.7, 11.1-11.8, 13.1, 5.3-5.5</p>
     * 
     * @param deviceId 设备ID
     * @param eventType 事件类型
     * @param params 事件参数
     */
    private void routeEventByType(Long deviceId, String eventType, Map<String, Object> params) {
        if (eventType == null) {
            log.warn("{} 事件类型为空，使用默认处理: deviceId={}", LOG_PREFIX, deviceId);
            recordChanghuiEvent(deviceId, "UNKNOWN", params);
            return;
        }

        switch (eventType.toUpperCase()) {
            case EVENT_TYPE_DATA_REPORT:
                handleDataReportEvent(deviceId, params);
                break;
            case EVENT_TYPE_ALARM:
                handleAlarmEvent(deviceId, params);
                break;
            case EVENT_TYPE_TRIGGER_SUCCESS:
                handleTriggerSuccessEvent(deviceId, params);
                break;
            case EVENT_TYPE_TRIGGER_FAILED:
                handleTriggerFailedEvent(deviceId, params);
                break;
            case EVENT_TYPE_URL_RECEIVED:
            case EVENT_TYPE_URL_RECEIVE_FAILED:
            case EVENT_TYPE_DOWNLOADING:
            case EVENT_TYPE_DOWNLOAD_COMPLETE:
                handleUpgradeUrlStatusEvent(deviceId, eventType, params);
                break;
            case EVENT_TYPE_UPGRADE_STATUS:
            case EVENT_TYPE_UPGRADE_PROGRESS:
            case EVENT_TYPE_UPGRADE_COMPLETE:
            case EVENT_TYPE_UPGRADE_FAILED:
                handleUpgradeStatusEvent(deviceId, eventType, params);
                break;
            default:
                // 其他事件类型使用通用处理
                handleGenericEvent(deviceId, eventType, params);
                break;
        }
    }

    /**
     * 处理数据上报事件
     * 
     * <p>Requirements: 11.1-11.8</p>
     * 
     * @param deviceId 设备ID
     * @param params 事件参数
     */
    private void handleDataReportEvent(Long deviceId, Map<String, Object> params) {
        log.info("{} 处理数据上报事件: deviceId={}", LOG_PREFIX, deviceId);
        
        try {
            // 提取数据上报信息
            String stationCode = getStringParam(params, "stationCode");
            String indicator = getStringParam(params, "indicator");
            BigDecimal value = getBigDecimalParam(params, "value");
            String unit = getStringParam(params, "unit");
            
            // TODO: 待任务 10 完成后，调用 ChanghuiDataService 保存数据
            // ChanghuiDataSaveReqVO reqVO = new ChanghuiDataSaveReqVO();
            // reqVO.setDeviceId(deviceId);
            // reqVO.setStationCode(stationCode);
            // reqVO.setIndicator(indicator);
            // reqVO.setValue(value);
            // changhuiDataService.saveData(reqVO);
            
            // 记录事件到数据库
            recordChanghuiEvent(deviceId, EVENT_TYPE_DATA_REPORT, params);
            
            // 构建事件数据用于 WebSocket 推送
            Map<String, Object> eventData = new HashMap<>();
            eventData.put("stationCode", stationCode);
            eventData.put("indicator", indicator);
            eventData.put("value", value);
            eventData.put("unit", unit);
            eventData.put("timestamp", System.currentTimeMillis());
            
            // 推送 WebSocket 通知
            deviceMessagePushService.pushDeviceEvent(deviceId, DEVICE_TYPE, EVENT_TYPE_DATA_REPORT, eventData);
            
            log.info("{} 数据上报事件处理完成: deviceId={}, stationCode={}, indicator={}, value={}",
                    LOG_PREFIX, deviceId, stationCode, indicator, value);
        } catch (Exception e) {
            log.error("{} 处理数据上报事件失败: deviceId={}", LOG_PREFIX, deviceId, e);
        }
    }

    /**
     * 处理报警事件
     * 
     * <p>Requirements: 13.1</p>
     * 
     * @param deviceId 设备ID
     * @param params 事件参数
     */
    private void handleAlarmEvent(Long deviceId, Map<String, Object> params) {
        log.info("{} 处理报警事件: deviceId={}", LOG_PREFIX, deviceId);
        
        try {
            // 提取报警信息
            String stationCode = getStringParam(params, "stationCode");
            Integer alarmType = getIntegerParam(params, "alarmType");
            String alarmValue = getStringParam(params, "alarmValue");
            String description = getStringParam(params, "description");
            
            // TODO: 待任务 11 完成后，调用 ChanghuiAlarmService 保存报警
            // ChanghuiAlarmSaveReqVO reqVO = new ChanghuiAlarmSaveReqVO();
            // reqVO.setDeviceId(deviceId);
            // reqVO.setStationCode(stationCode);
            // reqVO.setAlarmType(alarmType);
            // reqVO.setAlarmValue(alarmValue);
            // changhuiAlarmService.saveAlarm(reqVO);
            
            // 记录事件到数据库
            recordChanghuiEvent(deviceId, EVENT_TYPE_ALARM, params);
            
            // 构建事件数据用于 WebSocket 推送
            Map<String, Object> eventData = new HashMap<>();
            eventData.put("stationCode", stationCode);
            eventData.put("alarmType", alarmType);
            eventData.put("alarmTypeName", getAlarmTypeName(alarmType));
            eventData.put("alarmValue", alarmValue);
            eventData.put("description", description);
            eventData.put("timestamp", System.currentTimeMillis());
            
            // 推送 WebSocket 通知
            deviceMessagePushService.pushDeviceEvent(deviceId, DEVICE_TYPE, EVENT_TYPE_ALARM, eventData);
            
            log.info("{} 报警事件处理完成: deviceId={}, stationCode={}, alarmType={}, alarmValue={}",
                    LOG_PREFIX, deviceId, stationCode, alarmType, alarmValue);
        } catch (Exception e) {
            log.error("{} 处理报警事件失败: deviceId={}", LOG_PREFIX, deviceId, e);
        }
    }

    /**
     * 处理升级触发成功事件
     * 
     * <p>设备确认可以升级，需要发送 UPGRADE_URL 命令</p>
     * 
     * @param deviceId 设备ID
     * @param params 事件参数
     */
    private void handleTriggerSuccessEvent(Long deviceId, Map<String, Object> params) {
        log.info("{} 处理升级触发成功事件: deviceId={}", LOG_PREFIX, deviceId);
        
        try {
            String stationCode = getStringParam(params, "stationCode");
            
            // 调用升级服务处理触发成功，自动发送 UPGRADE_URL
            changhuiUpgradeService.handleTriggerSuccess(deviceId, stationCode);
            
            // 记录事件到数据库
            recordChanghuiEvent(deviceId, EVENT_TYPE_TRIGGER_SUCCESS, params);
            
            // 构建事件数据用于 WebSocket 推送
            Map<String, Object> eventData = new HashMap<>();
            eventData.put("stationCode", stationCode);
            eventData.put("status", "TRIGGER_SUCCESS");
            eventData.put("message", "设备确认可以升级，正在发送升级URL");
            eventData.put("timestamp", System.currentTimeMillis());
            
            // 推送 WebSocket 通知
            deviceMessagePushService.pushDeviceEvent(deviceId, DEVICE_TYPE, EVENT_TYPE_TRIGGER_SUCCESS, eventData);
            
            log.info("{} 升级触发成功事件处理完成: deviceId={}, stationCode={}", LOG_PREFIX, deviceId, stationCode);
        } catch (Exception e) {
            log.error("{} 处理升级触发成功事件失败: deviceId={}", LOG_PREFIX, deviceId, e);
        }
    }
    
    /**
     * 处理升级触发失败事件
     * 
     * <p>设备拒绝升级</p>
     * 
     * @param deviceId 设备ID
     * @param params 事件参数
     */
    private void handleTriggerFailedEvent(Long deviceId, Map<String, Object> params) {
        log.error("{} 处理升级触发失败事件: deviceId={}", LOG_PREFIX, deviceId);
        
        try {
            String stationCode = getStringParam(params, "stationCode");
            String errorMessage = getStringParam(params, "errorMessage");
            
            // 调用升级服务处理触发失败
            changhuiUpgradeService.handleTriggerFailed(deviceId, stationCode, errorMessage);
            
            // 记录事件到数据库
            recordChanghuiEvent(deviceId, EVENT_TYPE_TRIGGER_FAILED, params);
            
            // 构建事件数据用于 WebSocket 推送
            Map<String, Object> eventData = new HashMap<>();
            eventData.put("stationCode", stationCode);
            eventData.put("status", "TRIGGER_FAILED");
            eventData.put("message", "设备拒绝升级");
            eventData.put("errorMessage", errorMessage);
            eventData.put("timestamp", System.currentTimeMillis());
            
            // 推送 WebSocket 通知
            deviceMessagePushService.pushDeviceEvent(deviceId, DEVICE_TYPE, EVENT_TYPE_TRIGGER_FAILED, eventData);
            
            log.info("{} 升级触发失败事件处理完成: deviceId={}, stationCode={}, error={}", 
                    LOG_PREFIX, deviceId, stationCode, errorMessage);
        } catch (Exception e) {
            log.error("{} 处理升级触发失败事件失败: deviceId={}", LOG_PREFIX, deviceId, e);
        }
    }
    
    /**
     * 处理升级URL状态事件
     * 
     * <p>处理 URL_RECEIVED、URL_RECEIVE_FAILED、DOWNLOADING、DOWNLOAD_COMPLETE 事件</p>
     * 
     * @param deviceId 设备ID
     * @param eventType 事件类型
     * @param params 事件参数
     */
    private void handleUpgradeUrlStatusEvent(Long deviceId, String eventType, Map<String, Object> params) {
        log.info("{} 处理升级URL状态事件: deviceId={}, eventType={}", LOG_PREFIX, deviceId, eventType);
        
        try {
            String stationCode = getStringParam(params, "stationCode");
            Integer progress = getIntegerParam(params, "progress");
            String errorMessage = getStringParam(params, "errorMessage");
            Long taskId = getLongParam(params, "taskId");
            
            log.info("{} 升级URL状态事件参数: taskId={}, stationCode={}, progress={}, errorMessage={}", 
                    LOG_PREFIX, taskId, stationCode, progress, errorMessage);
            
            // 更新升级任务进度
            if (taskId != null) {
                log.info("{} 使用taskId更新进度: taskId={}, progress={}", LOG_PREFIX, taskId, progress);
                if (EVENT_TYPE_URL_RECEIVE_FAILED.equals(eventType)) {
                    changhuiUpgradeService.completeUpgrade(taskId, false, errorMessage != null ? errorMessage : "设备接收URL失败");
                } else if (progress != null) {
                    changhuiUpgradeService.updateUpgradeProgress(taskId, progress);
                } else {
                    log.warn("{} progress为null，跳过更新: taskId={}", LOG_PREFIX, taskId);
                }
            } else if (stationCode != null && !stationCode.isEmpty()) {
                log.info("{} 使用stationCode更新进度: stationCode={}, progress={}", LOG_PREFIX, stationCode, progress);
                if (EVENT_TYPE_URL_RECEIVE_FAILED.equals(eventType)) {
                    changhuiUpgradeService.completeUpgradeByStationCode(stationCode, false, 
                            errorMessage != null ? errorMessage : "设备接收URL失败");
                } else if (progress != null) {
                    changhuiUpgradeService.updateUpgradeProgressByStationCode(stationCode, progress);
                } else {
                    log.warn("{} progress为null，跳过更新: stationCode={}", LOG_PREFIX, stationCode);
                }
            } else {
                log.warn("{} taskId和stationCode都为空，无法更新进度", LOG_PREFIX);
            }
            
            // 记录事件到数据库
            recordChanghuiEvent(deviceId, eventType, params);
            
            // 构建事件数据用于 WebSocket 推送
            Map<String, Object> eventData = new HashMap<>();
            eventData.put("stationCode", stationCode);
            eventData.put("status", eventType);
            eventData.put("statusName", getUrlStatusName(eventType));
            eventData.put("progress", progress);
            eventData.put("errorMessage", errorMessage);
            eventData.put("taskId", taskId);
            eventData.put("timestamp", System.currentTimeMillis());
            
            // 推送 WebSocket 通知
            deviceMessagePushService.pushDeviceEvent(deviceId, DEVICE_TYPE, eventType, eventData);
            
            log.info("{} 升级URL状态事件处理完成: deviceId={}, stationCode={}, status={}, progress={}",
                    LOG_PREFIX, deviceId, stationCode, eventType, progress);
        } catch (Exception e) {
            log.error("{} 处理升级URL状态事件失败: deviceId={}", LOG_PREFIX, deviceId, e);
        }
    }
    
    /**
     * 获取URL状态名称
     */
    private String getUrlStatusName(String eventType) {
        if (eventType == null) {
            return "未知";
        }
        switch (eventType.toUpperCase()) {
            case EVENT_TYPE_URL_RECEIVED:
                return "URL接收成功";
            case EVENT_TYPE_URL_RECEIVE_FAILED:
                return "URL接收失败";
            case EVENT_TYPE_DOWNLOADING:
                return "固件下载中";
            case EVENT_TYPE_DOWNLOAD_COMPLETE:
                return "固件下载完成";
            default:
                return eventType;
        }
    }

    /**
     * 处理升级状态事件
     * 
     * <p>Requirements: 5.3-5.5</p>
     * 
     * @param deviceId 设备ID
     * @param eventType 事件类型
     * @param params 事件参数
     */
    private void handleUpgradeStatusEvent(Long deviceId, String eventType, Map<String, Object> params) {
        log.info("{} 处理升级状态事件: deviceId={}, eventType={}", LOG_PREFIX, deviceId, eventType);
        
        try {
            // 提取升级状态信息
            String stationCode = getStringParam(params, "stationCode");
            Integer status = getIntegerParam(params, "status");
            Integer progress = getIntegerParam(params, "progress");
            String errorMessage = getStringParam(params, "errorMessage");
            Long taskId = getLongParam(params, "taskId");
            
            // 更新升级任务进度/结果（优先 taskId，缺省用 stationCode 关联进行中任务）
            // 注意：当 progress=100 时，无论 eventType 是 UPGRADE_PROGRESS 还是 UPGRADE_COMPLETE，都应标记为完成
            boolean isProgressComplete = progress != null && progress >= 100;
            
            if (taskId != null) {
                if (EVENT_TYPE_UPGRADE_COMPLETE.equals(eventType) || 
                    (EVENT_TYPE_UPGRADE_PROGRESS.equals(eventType) && isProgressComplete)) {
                    // progress=100 或 UPGRADE_COMPLETE 事件，标记升级完成
                    changhuiUpgradeService.completeUpgrade(taskId, true, null);
                } else if (EVENT_TYPE_UPGRADE_PROGRESS.equals(eventType)) {
                    changhuiUpgradeService.updateUpgradeProgress(taskId, progress);
                } else if (EVENT_TYPE_UPGRADE_FAILED.equals(eventType)) {
                    changhuiUpgradeService.completeUpgrade(taskId, false, errorMessage);
                } else if (EVENT_TYPE_UPGRADE_STATUS.equals(eventType) && progress != null) {
                    // 兜底：如果上游只发 UPGRADE_STATUS，也按 progress 更新（或完成）
                    if (isProgressComplete) {
                        changhuiUpgradeService.completeUpgrade(taskId, true, null);
                    } else {
                        changhuiUpgradeService.updateUpgradeProgress(taskId, progress);
                    }
                }
            } else if (stationCode != null && !stationCode.isEmpty()) {
                if (EVENT_TYPE_UPGRADE_COMPLETE.equals(eventType) || 
                    (EVENT_TYPE_UPGRADE_PROGRESS.equals(eventType) && isProgressComplete)) {
                    // progress=100 或 UPGRADE_COMPLETE 事件，标记升级完成
                    changhuiUpgradeService.completeUpgradeByStationCode(stationCode, true, null);
                } else if (EVENT_TYPE_UPGRADE_PROGRESS.equals(eventType)) {
                    changhuiUpgradeService.updateUpgradeProgressByStationCode(stationCode, progress);
                } else if (EVENT_TYPE_UPGRADE_FAILED.equals(eventType)) {
                    changhuiUpgradeService.completeUpgradeByStationCode(stationCode, false, errorMessage);
                } else if (EVENT_TYPE_UPGRADE_STATUS.equals(eventType) && progress != null) {
                    // 兜底：如果上游只发 UPGRADE_STATUS，也按 progress 更新（或完成）
                    if (isProgressComplete) {
                        changhuiUpgradeService.completeUpgradeByStationCode(stationCode, true, null);
                    } else {
                        changhuiUpgradeService.updateUpgradeProgressByStationCode(stationCode, progress);
                    }
                }
            }
            
            // 记录事件到数据库
            recordChanghuiEvent(deviceId, eventType, params);
            
            // 构建事件数据用于 WebSocket 推送
            Map<String, Object> eventData = new HashMap<>();
            eventData.put("stationCode", stationCode);
            eventData.put("status", status);
            eventData.put("statusName", getUpgradeStatusName(status));
            eventData.put("progress", progress);
            eventData.put("errorMessage", errorMessage);
            eventData.put("taskId", taskId);
            eventData.put("timestamp", System.currentTimeMillis());
            
            // 推送 WebSocket 通知
            deviceMessagePushService.pushDeviceEvent(deviceId, DEVICE_TYPE, eventType, eventData);
            
            log.info("{} 升级状态事件处理完成: deviceId={}, stationCode={}, status={}, progress={}",
                    LOG_PREFIX, deviceId, stationCode, status, progress);
        } catch (Exception e) {
            log.error("{} 处理升级状态事件失败: deviceId={}", LOG_PREFIX, deviceId, e);
        }
    }

    /**
     * 处理通用事件
     * 
     * @param deviceId 设备ID
     * @param eventType 事件类型
     * @param params 事件参数
     */
    private void handleGenericEvent(Long deviceId, String eventType, Map<String, Object> params) {
        log.info("{} 处理通用事件: deviceId={}, eventType={}", LOG_PREFIX, deviceId, eventType);
        
        try {
            // 记录事件到数据库
            recordChanghuiEvent(deviceId, eventType, params);
            
            // 推送 WebSocket 通知
            deviceMessagePushService.pushDeviceEvent(deviceId, DEVICE_TYPE, eventType, params);
            
            log.info("{} 通用事件处理完成: deviceId={}, eventType={}", LOG_PREFIX, deviceId, eventType);
        } catch (Exception e) {
            log.error("{} 处理通用事件失败: deviceId={}, eventType={}", LOG_PREFIX, deviceId, eventType, e);
        }
    }

    /**
     * 记录长辉设备事件到数据库
     * 
     * @param deviceId 设备ID
     * @param eventType 事件类型
     * @param params 事件参数
     */
    private void recordChanghuiEvent(Long deviceId, String eventType, Map<String, Object> params) {
        try {
            String eventName = getStringParam(params, "eventName");
            if (eventName == null || eventName.isEmpty()) {
                eventName = determineEventName(eventType);
            }
            
            String eventLevel = getStringParam(params, "eventLevel");
            if (eventLevel == null || eventLevel.isEmpty()) {
                eventLevel = determineEventLevel(eventType);
            }

            // 获取测站编码
            String stationCode = getStringParam(params, "stationCode");

            // 构建事件记录
            IotDeviceEventLogDO eventDO = IotDeviceEventLogDO.builder()
                .deviceId(deviceId)
                .eventIdentifier(eventType)
                .eventName(eventName)
                .eventType(eventLevel)
                .eventData(params.toString())
                .eventTime(LocalDateTime.now())
                .processed(false)
                .build();

            // 保存事件
            eventLogMapper.insert(eventDO);
            log.debug("{} 长辉设备事件已记录: deviceId={}, eventType={}, eventName={}, stationCode={}",
                    LOG_PREFIX, deviceId, eventType, eventName, stationCode);
        } catch (Exception e) {
            log.error("{} 记录长辉设备事件失败: deviceId={}, eventType={}", 
                    LOG_PREFIX, deviceId, eventType, e);
        }
    }

    /**
     * 确定事件类型
     * 
     * @param params 事件参数
     * @param method 消息方法
     * @return 事件类型
     */
    private String determineEventType(Map<String, Object> params, String method) {
        Object eventTypeValue = params.get("eventType");
        
        if (eventTypeValue != null) {
            // 如果是数字类型，先进行映射转换
            if (eventTypeValue instanceof Number) {
                Integer eventTypeInt = ((Number) eventTypeValue).intValue();
                String mappedType = mapIntegerEventType(eventTypeInt);
                if (mappedType != null && !mappedType.startsWith("UNKNOWN_")) {
                    return mappedType;
                }
            }
            
            // 如果是字符串类型，检查是否是纯数字字符串
            String eventTypeStr = eventTypeValue.toString();
            if (!eventTypeStr.isEmpty()) {
                // 尝试解析为数字并映射
                try {
                    Integer eventTypeInt = Integer.parseInt(eventTypeStr);
                    String mappedType = mapIntegerEventType(eventTypeInt);
                    if (mappedType != null && !mappedType.startsWith("UNKNOWN_")) {
                        return mappedType;
                    }
                } catch (NumberFormatException ignored) {
                    // 不是数字，当作字符串事件类型处理
                }
                // 直接返回字符串事件类型（如 "UPGRADE_PROGRESS"）
                return eventTypeStr;
            }
        }
        
        // 使用 method 作为事件类型
        if (method != null && !method.isEmpty()) {
            return method;
        }
        
        return null;
    }

    /**
     * 将 Integer 类型的事件类型映射为字符串
     * 
     * <p>注意：升级相关事件需要使用 EventType 常量（不是 Status 常量）进行映射！</p>
     * <ul>
     *   <li>EventType.UPGRADE_STATUS = 1</li>
     *   <li>EventType.UPGRADE_PROGRESS = 2</li>
     *   <li>EventType.UPGRADE_COMPLETE = 3</li>
     *   <li>EventType.UPGRADE_FAILED = 4</li>
     * </ul>
     * 
     * @param eventTypeInt 事件类型整数
     * @return 事件类型字符串
     */
    private String mapIntegerEventType(Integer eventTypeInt) {
        if (eventTypeInt == null) {
            return null;
        }
        // 映射 ChanghuiDataReportMessage.EventType
        if (eventTypeInt == ChanghuiDataReportMessage.EventType.DATA_REPORT) {
            return EVENT_TYPE_DATA_REPORT;
        }
        // 映射 ChanghuiAlarmMessage.EventType
        if (eventTypeInt == ChanghuiAlarmMessage.EventType.ALARM) {
            return EVENT_TYPE_ALARM;
        }
        // 映射 ChanghuiUpgradeStatusMessage.EventType（注意：使用 EventType 而不是 Status！）
        if (eventTypeInt == ChanghuiUpgradeStatusMessage.EventType.UPGRADE_STATUS) {
            return EVENT_TYPE_UPGRADE_STATUS;
        }
        if (eventTypeInt == ChanghuiUpgradeStatusMessage.EventType.UPGRADE_PROGRESS) {
            return EVENT_TYPE_UPGRADE_PROGRESS;
        }
        if (eventTypeInt == ChanghuiUpgradeStatusMessage.EventType.UPGRADE_COMPLETE) {
            return EVENT_TYPE_UPGRADE_COMPLETE;
        }
        if (eventTypeInt == ChanghuiUpgradeStatusMessage.EventType.UPGRADE_FAILED) {
            return EVENT_TYPE_UPGRADE_FAILED;
        }
        // 新增的升级流程事件类型
        if (eventTypeInt == ChanghuiUpgradeStatusMessage.EventType.TRIGGER_SUCCESS) {
            return EVENT_TYPE_TRIGGER_SUCCESS;
        }
        if (eventTypeInt == ChanghuiUpgradeStatusMessage.EventType.TRIGGER_FAILED) {
            return EVENT_TYPE_TRIGGER_FAILED;
        }
        if (eventTypeInt == ChanghuiUpgradeStatusMessage.EventType.URL_RECEIVED) {
            return EVENT_TYPE_URL_RECEIVED;
        }
        if (eventTypeInt == ChanghuiUpgradeStatusMessage.EventType.URL_RECEIVE_FAILED) {
            return EVENT_TYPE_URL_RECEIVE_FAILED;
        }
        if (eventTypeInt == ChanghuiUpgradeStatusMessage.EventType.DOWNLOADING) {
            return EVENT_TYPE_DOWNLOADING;
        }
        if (eventTypeInt == ChanghuiUpgradeStatusMessage.EventType.DOWNLOAD_COMPLETE) {
            return EVENT_TYPE_DOWNLOAD_COMPLETE;
        }
        return "UNKNOWN_" + eventTypeInt;
    }

    /**
     * 从消息中提取参数
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> extractParams(IotDeviceMessage message) {
        Object params = message.getParams();
        if (params instanceof Map) {
            return (Map<String, Object>) params;
        }
        return null;
    }

    /**
     * 获取字符串参数
     */
    private String getStringParam(Map<String, Object> params, String key) {
        Object value = params.get(key);
        return value != null ? value.toString() : null;
    }

    /**
     * 获取整数参数
     */
    private Integer getIntegerParam(Map<String, Object> params, String key) {
        Object value = params.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 获取长整数参数
     */
    private Long getLongParam(Map<String, Object> params, String key) {
        Object value = params.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Long) {
            return (Long) value;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 获取 BigDecimal 参数
     */
    private BigDecimal getBigDecimalParam(Map<String, Object> params, String key) {
        Object value = params.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        if (value instanceof Number) {
            return BigDecimal.valueOf(((Number) value).doubleValue());
        }
        try {
            return new BigDecimal(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 确定事件名称
     */
    private String determineEventName(String eventType) {
        if (eventType == null) {
            return "未知事件";
        }
        switch (eventType.toUpperCase()) {
            case EVENT_TYPE_DATA_REPORT:
                return "数据上报";
            case EVENT_TYPE_ALARM:
                return "设备报警";
            case EVENT_TYPE_UPGRADE_STATUS:
                return "升级状态";
            case EVENT_TYPE_UPGRADE_PROGRESS:
                return "升级进度";
            case EVENT_TYPE_UPGRADE_COMPLETE:
                return "升级完成";
            case EVENT_TYPE_UPGRADE_FAILED:
                return "升级失败";
            case "STATUS_CHANGE":
                return "状态变更";
            case "HEARTBEAT":
                return "心跳";
            case "WATER_LEVEL":
                return "水位数据";
            case "FLOW_RATE":
                return "流量数据";
            case "GATE_POSITION":
                return "闸门位置";
            case "GATE_CONTROL":
                return "闸门控制";
            default:
                return eventType;
        }
    }

    /**
     * 确定事件级别
     */
    private String determineEventLevel(String eventType) {
        if (eventType == null) {
            return "info";
        }
        switch (eventType.toUpperCase()) {
            case EVENT_TYPE_ALARM:
            case EVENT_TYPE_UPGRADE_FAILED:
                return "error";
            case EVENT_TYPE_UPGRADE_STATUS:
            case EVENT_TYPE_UPGRADE_PROGRESS:
                return "alert";
            default:
                return "info";
        }
    }

    /**
     * 获取报警类型名称
     * 
     * @param alarmType 报警类型
     * @return 报警类型名称
     */
    private String getAlarmTypeName(Integer alarmType) {
        if (alarmType == null) {
            return "UNKNOWN";
        }
        switch (alarmType) {
            case ChanghuiAlarmMessage.AlarmType.OVER_TORQUE:
                return "过扭矩报警";
            case ChanghuiAlarmMessage.AlarmType.OVER_CURRENT:
                return "过电流报警";
            case ChanghuiAlarmMessage.AlarmType.OVER_VOLTAGE:
                return "过电压报警";
            case ChanghuiAlarmMessage.AlarmType.LOW_VOLTAGE:
                return "欠电压报警";
            case ChanghuiAlarmMessage.AlarmType.WATER_LEVEL:
                return "水位报警";
            case ChanghuiAlarmMessage.AlarmType.GATE_POSITION:
                return "闸位报警";
            case ChanghuiAlarmMessage.AlarmType.COMMUNICATION_FAULT:
                return "通信故障";
            case ChanghuiAlarmMessage.AlarmType.DEVICE_FAULT:
                return "设备故障";
            default:
                return "其他报警";
        }
    }

    /**
     * 获取升级状态名称
     * 
     * @param status 升级状态
     * @return 升级状态名称
     */
    private String getUpgradeStatusName(Integer status) {
        if (status == null) {
            return "UNKNOWN";
        }
        switch (status) {
            case ChanghuiUpgradeStatusMessage.Status.PENDING:
                return "等待中";
            case ChanghuiUpgradeStatusMessage.Status.IN_PROGRESS:
                return "进行中";
            case ChanghuiUpgradeStatusMessage.Status.SUCCESS:
                return "成功";
            case ChanghuiUpgradeStatusMessage.Status.FAILED:
                return "失败";
            case ChanghuiUpgradeStatusMessage.Status.CANCELLED:
                return "已取消";
            default:
                return "未知";
        }
    }
}
