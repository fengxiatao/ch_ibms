package cn.iocoder.yudao.module.iot.mq.consumer.device.handler;

import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceEventLogDO;
import cn.iocoder.yudao.module.iot.dal.mysql.device.IotDeviceEventLogMapper;
import cn.iocoder.yudao.module.iot.enums.device.NvrDeviceTypeConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * NVR 设备事件处理器
 * 
 * <p>处理 NVR/DVR 设备的事件上报，包括：</p>
 * <ul>
 *   <li>移动侦测事件 - 记录移动侦测报警</li>
 *   <li>视频丢失事件 - 记录视频丢失报警</li>
 *   <li>遮挡报警事件 - 记录遮挡报警</li>
 *   <li>录像状态事件 - 记录录像状态变化</li>
 *   <li>硬盘状态事件 - 记录硬盘状态变化</li>
 * </ul>
 * 
 * <p>Requirements: 1.4, 6.2, 6.3</p>
 *
 * @author 长辉信息科技有限公司
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class NvrDeviceEventHandler implements DeviceEventHandler {

    private static final String LOG_PREFIX = "[NvrDeviceEventHandler]";

    /**
     * 设备类型标识
     */
    private static final String DEVICE_TYPE = NvrDeviceTypeConstants.NVR;

    private final IotDeviceEventLogMapper eventLogMapper;

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

        String eventType = getStringParam(params, "eventType");
        if (eventType == null || eventType.isEmpty()) {
            eventType = message.getMethod();
        }

        log.info("{} 处理 NVR 事件: deviceId={}, eventType={}", LOG_PREFIX, deviceId, eventType);

        try {
            // 记录事件到数据库
            recordNvrEvent(deviceId, eventType, params);
        } catch (Exception e) {
            log.error("{} 处理 NVR 事件失败: deviceId={}, eventType={}", 
                    LOG_PREFIX, deviceId, eventType, e);
        }
    }

    /**
     * 记录 NVR 事件到数据库
     * 
     * @param deviceId 设备ID
     * @param eventType 事件类型
     * @param params 事件参数
     */
    private void recordNvrEvent(Long deviceId, String eventType, Map<String, Object> params) {
        try {
            String eventName = getStringParam(params, "eventName");
            if (eventName == null || eventName.isEmpty()) {
                eventName = determineEventName(eventType);
            }
            
            String eventLevel = getStringParam(params, "eventLevel");
            if (eventLevel == null || eventLevel.isEmpty()) {
                eventLevel = determineEventLevel(eventType);
            }

            // 获取通道号
            String channelNo = getStringParam(params, "channelNo");

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

            // 保存事件（使用忽略租户模式，因为设备事件可能没有租户上下文）
            TenantUtils.executeIgnore(() -> eventLogMapper.insert(eventDO));
            log.info("{} NVR 事件已记录: deviceId={}, eventType={}, eventName={}, channelNo={}",
                    LOG_PREFIX, deviceId, eventType, eventName, channelNo);
        } catch (Exception e) {
            log.error("{} 记录 NVR 事件失败: deviceId={}, eventType={}", 
                    LOG_PREFIX, deviceId, eventType, e);
        }
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
     * 确定事件名称
     */
    private String determineEventName(String eventType) {
        if (eventType == null) {
            return "未知事件";
        }
        switch (eventType.toUpperCase()) {
            case "MOTION_DETECTION":
                return "移动侦测";
            case "VIDEO_LOSS":
                return "视频丢失";
            case "VIDEO_BLIND":
                return "视频遮挡";
            case "RECORDING_START":
                return "录像开始";
            case "RECORDING_STOP":
                return "录像停止";
            case "DISK_FULL":
                return "硬盘满";
            case "DISK_ERROR":
                return "硬盘故障";
            case "DISK_UNFORMAT":
                return "硬盘未格式化";
            case "NETWORK_DISCONNECT":
                return "网络断开";
            case "IP_CONFLICT":
                return "IP 冲突";
            case "ILLEGAL_ACCESS":
                return "非法访问";
            case "LINE_CROSSING":
                return "越界检测";
            case "INTRUSION":
                return "区域入侵";
            case "FACE_DETECTION":
                return "人脸检测";
            case "AUDIO_EXCEPTION":
                return "音频异常";
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
            case "VIDEO_LOSS":
            case "DISK_ERROR":
            case "NETWORK_DISCONNECT":
            case "ILLEGAL_ACCESS":
                return "error";
            case "MOTION_DETECTION":
            case "VIDEO_BLIND":
            case "DISK_FULL":
            case "DISK_UNFORMAT":
            case "IP_CONFLICT":
            case "LINE_CROSSING":
            case "INTRUSION":
            case "AUDIO_EXCEPTION":
                return "alert";
            default:
                return "info";
        }
    }
}
