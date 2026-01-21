package cn.iocoder.yudao.module.iot.mq.consumer.device.handler;

import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;

/**
 * 设备事件处理器接口
 * 
 * <p>用于处理不同设备类型的事件上报。</p>
 * <p>每种设备类型可以实现自己的处理器来处理特定的事件业务逻辑。</p>
 * 
 * <p>Requirements: 6.2, 6.3</p>
 *
 * @author 长辉信息科技有限公司
 */
public interface DeviceEventHandler {

    /**
     * 获取处理器支持的设备类型
     * 
     * @return 设备类型标识（如 ACCESS_GEN1, ACCESS_GEN2, ALARM, NVR, CHANGHUI）
     */
    String getDeviceType();

    /**
     * 处理设备事件
     * 
     * <p>实现类可以在此方法中：</p>
     * <ul>
     *   <li>存储事件到数据库</li>
     *   <li>记录事件日志</li>
     *   <li>触发后续业务流程</li>
     * </ul>
     * 
     * @param message 设备事件消息
     */
    void handleEvent(IotDeviceMessage message);
}
