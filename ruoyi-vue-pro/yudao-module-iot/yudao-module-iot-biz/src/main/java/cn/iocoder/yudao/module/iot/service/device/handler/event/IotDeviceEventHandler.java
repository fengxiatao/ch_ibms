package cn.iocoder.yudao.module.iot.service.device.handler.event;

import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * IoT 设备事件处理器接口
 * 
 * 用于处理设备上报的事件，支持：
 * 1. 告警事件（如入侵报警、高温告警）
 * 2. 信息事件（如人脸识别、设备上线）
 * 3. 故障事件（如设备故障、传感器异常）
 * 4. 自定义事件
 *
 * @author 芋道源码
 */
public interface IotDeviceEventHandler {

    /**
     * 获取事件标识符
     * 
     * @return 事件标识符，如 "high_temperature"、"intrusion_alarm"、"face_detected"
     */
    String getEventIdentifier();

    /**
     * 判断是否支持该设备
     *
     * @param productKey 产品Key
     * @return 是否支持
     */
    boolean supportsDevice(String productKey);

    /**
     * 处理设备事件
     * 
     * @param device 设备信息
     * @param params 事件参数（包含identifier和其他业务参数）
     * @param eventTime 事件时间
     * @return 处理结果（可选，用于回复设备）
     */
    Object handleEvent(IotDeviceDO device, Map<String, Object> params, LocalDateTime eventTime);

    /**
     * 处理优先级（数字越小优先级越高）
     * 
     * @return 优先级，默认100
     */
    default int getOrder() {
        return 100;
    }
}







