package cn.iocoder.yudao.module.iot.service.device.handler.property;

import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.service.device.property.IotDevicePropertyService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 设备属性处理调度器
 * 
 * 负责协调多个属性处理器的执行，处理流程：
 * 1. 前置处理钩子
 * 2. 默认存储（TDengine）
 * 3. 执行自定义处理器
 * 4. 后置处理钩子
 *
 * @author 芋道源码
 */
@Component
@Slf4j
public class DevicePropertyProcessor {

    @Resource
    private List<IotDevicePropertyHandler> propertyHandlers;

    @Resource
    private IotDevicePropertyService devicePropertyService;

    /**
     * 处理设备属性上报
     * 
     * @param device 设备信息
     * @param properties 属性Map
     * @param reportTime 上报时间
     */
    public void processProperty(IotDeviceDO device, Map<String, Object> properties, LocalDateTime reportTime) {
        
        // 1. 获取适用的处理器（按优先级排序）
        List<IotDevicePropertyHandler> applicableHandlers = propertyHandlers.stream()
            .filter(handler -> handler.supportsDevice(device.getProductKey()))
            .sorted(Comparator.comparing(IotDevicePropertyHandler::getOrder))
            .collect(Collectors.toList());
        
        log.debug("[属性处理][设备: {}, 适用处理器数: {}]", 
            device.getDeviceName(), applicableHandlers.size());
        
        // 2. 前置处理钩子
        for (IotDevicePropertyHandler handler : applicableHandlers) {
            try {
                if (!handler.beforePropertyHandle(device, properties)) {
                    log.warn("[属性处理终止][设备: {}, 处理器: {}]", 
                        device.getDeviceName(), handler.getClass().getSimpleName());
                    return;
                }
            } catch (Exception e) {
                log.error("[属性前置处理失败][设备: {}, 处理器: {}]", 
                    device.getDeviceName(), handler.getClass().getSimpleName(), e);
            }
        }
        
        // 3. 默认存储到TDengine（保持原有逻辑）
        try {
            devicePropertyService.saveDevicePropertyToTDengine(device, properties, reportTime);
        } catch (Exception e) {
            log.error("[属性存储失败][设备: {}]", device.getDeviceName(), e);
            // 存储失败不影响后续处理器执行
        }
        
        // 4. 执行自定义处理器
        for (IotDevicePropertyHandler handler : applicableHandlers) {
            try {
                // 只处理指定属性，或全部属性
                String identifier = handler.getPropertyIdentifier();
                if (identifier == null || properties.containsKey(identifier)) {
                    handler.handleProperty(device, properties, reportTime);
                    log.debug("[属性处理成功][设备: {}, 处理器: {}]", 
                        device.getDeviceName(), handler.getClass().getSimpleName());
                }
            } catch (Exception e) {
                log.error("[属性处理失败][设备: {}, 处理器: {}]", 
                    device.getDeviceName(), handler.getClass().getSimpleName(), e);
                // 单个处理器失败不影响其他处理器
            }
        }
        
        // 5. 后置处理钩子
        for (IotDevicePropertyHandler handler : applicableHandlers) {
            try {
                handler.afterPropertyHandle(device, properties);
            } catch (Exception e) {
                log.error("[属性后置处理失败][设备: {}, 处理器: {}]", 
                    device.getDeviceName(), handler.getClass().getSimpleName(), e);
            }
        }
    }
}







