package cn.iocoder.yudao.module.iot.service.device.handler;

import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;

import java.util.Map;

/**
 * IoT 设备服务处理器接口
 * 
 * 所有设备服务处理器都需要实现此接口
 * 
 * @author 长辉信息科技有限公司
 */
public interface IotDeviceServiceHandler {
    
    /**
     * 获取服务标识符
     * 
     * @return 服务标识符，如 "capture_image", "start_recording" 等
     */
    String getServiceIdentifier();
    
    /**
     * 处理服务调用
     * 
     * @param params 服务参数
     * @param device 设备信息
     * @return 服务执行结果
     */
    Object handleService(Map<String, Object> params, IotDeviceDO device);
    
    /**
     * 判断是否支持指定的设备产品
     * 
     * @param productKey 产品Key
     * @return true=支持, false=不支持
     */
    boolean supportsDevice(String productKey);
    
    /**
     * 获取服务名称
     * 
     * @return 服务名称
     */
    default String getServiceName() {
        return getServiceIdentifier();
    }
    
    /**
     * 获取服务描述
     * 
     * @return 服务描述
     */
    default String getServiceDescription() {
        return "";
    }
}







