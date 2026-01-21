package cn.iocoder.yudao.module.iot.service.device.handler.property;

import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * IoT 设备属性处理器接口
 * 
 * 用于扩展设备属性上报的处理逻辑，支持：
 * 1. 属性值告警（如温度过高）
 * 2. 属性联动（如状态变化触发其他设备）
 * 3. 属性统计（如能耗统计）
 * 4. 自定义业务逻辑
 *
 * @author 芋道源码
 */
public interface IotDevicePropertyHandler {

    /**
     * 获取处理器标识符（可选，用于特定属性）
     * 
     * @return 属性标识符，如 "temperature"、"humidity"；返回 null 表示处理所有属性
     */
    default String getPropertyIdentifier() {
        return null;
    }

    /**
     * 判断是否支持该设备
     *
     * @param productKey 产品Key
     * @return 是否支持
     */
    boolean supportsDevice(String productKey);

    /**
     * 属性处理前置钩子
     * 
     * @param device 设备信息
     * @param properties 属性Map（key=属性标识符，value=属性值）
     * @return true=继续处理，false=终止后续处理
     */
    default boolean beforePropertyHandle(IotDeviceDO device, Map<String, Object> properties) {
        return true;
    }

    /**
     * 处理设备属性
     * 
     * @param device 设备信息
     * @param properties 属性Map（key=属性标识符，value=属性值）
     * @param reportTime 上报时间
     */
    void handleProperty(IotDeviceDO device, Map<String, Object> properties, LocalDateTime reportTime);

    /**
     * 属性处理后置钩子
     * 
     * @param device 设备信息
     * @param properties 属性Map（key=属性标识符，value=属性值）
     */
    default void afterPropertyHandle(IotDeviceDO device, Map<String, Object> properties) {
        // 默认空实现
    }

    /**
     * 处理优先级（数字越小优先级越高）
     * 
     * @return 优先级，默认100
     */
    default int getOrder() {
        return 100;
    }
}







