package cn.iocoder.yudao.module.iot.mq.consumer.device.handler;

import cn.iocoder.yudao.module.iot.core.gateway.dto.DeviceStateChangeMessage;

/**
 * 设备状态处理器接口
 * 
 * <p>用于处理不同设备类型的状态变更。</p>
 * <p>每种设备类型可以实现自己的处理器来处理特定的状态变更业务逻辑。</p>
 * 
 * <p>Requirements: 2.5, 8.1, 8.2</p>
 *
 * @author 长辉信息科技有限公司
 */
public interface DeviceStateHandler {

    /**
     * 获取处理器支持的设备类型
     * 
     * @return 设备类型标识（如 ACCESS_GEN1, ACCESS_GEN2, ALARM, NVR, CHANGHUI）
     */
    String getDeviceType();

    /**
     * 处理设备状态变更
     * 
     * <p>实现类可以在此方法中：</p>
     * <ul>
     *   <li>更新设备特定的状态表</li>
     *   <li>触发设备特定的业务流程</li>
     *   <li>记录状态变更日志</li>
     * </ul>
     * 
     * @param message 设备状态变更消息
     */
    void handleStateChange(DeviceStateChangeMessage message);
}
