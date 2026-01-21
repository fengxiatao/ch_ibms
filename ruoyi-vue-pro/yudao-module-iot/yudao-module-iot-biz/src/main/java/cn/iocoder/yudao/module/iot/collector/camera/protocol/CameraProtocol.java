package cn.iocoder.yudao.module.iot.collector.camera.protocol;

import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;

import java.util.Map;

/**
 * 摄像头协议接口
 * 
 * 定义统一的摄像头数据采集接口，支持不同品牌的摄像头
 * 
 * @author 长辉信息科技有限公司
 */
public interface CameraProtocol {
    
    /**
     * 获取协议名称
     * 
     * @return 协议名称，如 "dahua", "hikvision", "onvif"
     */
    String getProtocolName();
    
    /**
     * 判断是否支持该设备
     * 
     * @param device 设备信息
     * @return 是否支持
     */
    boolean supports(IotDeviceDO device);
    
    /**
     * 获取设备属性
     * 
     * @param device 设备信息
     * @return 属性键值对
     */
    Map<String, Object> getDeviceProperties(IotDeviceDO device);
    
    /**
     * 测试设备连接
     * 
     * @param device 设备信息
     * @return 是否在线
     */
    boolean testConnection(IotDeviceDO device);
}
