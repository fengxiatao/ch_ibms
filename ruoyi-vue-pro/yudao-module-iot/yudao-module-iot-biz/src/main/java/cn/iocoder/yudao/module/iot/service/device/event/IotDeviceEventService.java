package cn.iocoder.yudao.module.iot.service.device.event;

import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * IoT 设备事件 Service 接口
 *
 * @author 芋道源码
 */
public interface IotDeviceEventService {

    /**
     * 保存设备事件
     * 
     * @param device 设备信息
     * @param eventIdentifier 事件标识符
     * @param params 事件参数
     * @param eventTime 事件时间
     */
    void saveDeviceEvent(IotDeviceDO device, String eventIdentifier, 
                        Map<String, Object> params, LocalDateTime eventTime);
}







