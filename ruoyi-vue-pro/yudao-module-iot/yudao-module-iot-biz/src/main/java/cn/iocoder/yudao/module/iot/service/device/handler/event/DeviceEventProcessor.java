package cn.iocoder.yudao.module.iot.service.device.handler.event;

import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.service.device.event.IotDeviceEventService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 设备事件处理调度器
 * 
 * 负责协调多个事件处理器的执行，处理流程：
 * 1. 记录事件到数据库
 * 2. 查找匹配的事件处理器
 * 3. 按优先级执行处理器
 *
 * @author 芋道源码
 */
@Component
@Slf4j
public class DeviceEventProcessor {

    @Resource
    private List<IotDeviceEventHandler> eventHandlers;

    @Resource
    private IotDeviceEventService deviceEventService;

    /**
     * 处理设备事件上报
     * 
     * @param device 设备信息
     * @param eventIdentifier 事件标识符
     * @param params 事件参数
     * @param eventTime 事件时间
     * @return 最后一个处理器的返回结果
     */
    public Object processEvent(IotDeviceDO device, String eventIdentifier, 
                              Map<String, Object> params, LocalDateTime eventTime) {
        
        log.info("[事件处理][设备: {}, 事件: {}]", device.getDeviceName(), eventIdentifier);
        
        // 1. 记录事件到数据库/日志
        try {
            deviceEventService.saveDeviceEvent(device, eventIdentifier, params, eventTime);
        } catch (Exception e) {
            log.error("[事件存储失败][设备: {}, 事件: {}]", device.getDeviceName(), eventIdentifier, e);
            // 存储失败不影响后续处理器执行
        }
        
        // 2. 查找匹配的事件处理器
        List<IotDeviceEventHandler> matchedHandlers = eventHandlers.stream()
            .filter(handler -> handler.getEventIdentifier().equals(eventIdentifier))
            .filter(handler -> handler.supportsDevice(device.getProductKey()))
            .sorted(Comparator.comparing(IotDeviceEventHandler::getOrder))
            .collect(Collectors.toList());
        
        if (matchedHandlers.isEmpty()) {
            log.debug("[无事件处理器][设备: {}, 事件: {}]", device.getDeviceName(), eventIdentifier);
            return null;
        }
        
        log.debug("[事件处理器匹配][设备: {}, 事件: {}, 处理器数: {}]", 
            device.getDeviceName(), eventIdentifier, matchedHandlers.size());
        
        // 3. 执行事件处理器（按优先级）
        Object lastResult = null;
        for (IotDeviceEventHandler handler : matchedHandlers) {
            try {
                lastResult = handler.handleEvent(device, params, eventTime);
                log.info("[事件处理成功][设备: {}, 事件: {}, 处理器: {}]", 
                    device.getDeviceName(), eventIdentifier, handler.getClass().getSimpleName());
            } catch (Exception e) {
                log.error("[事件处理失败][设备: {}, 事件: {}, 处理器: {}]", 
                    device.getDeviceName(), eventIdentifier, handler.getClass().getSimpleName(), e);
                // 单个处理器失败不影响其他处理器
            }
        }
        
        return lastResult;
    }
}







