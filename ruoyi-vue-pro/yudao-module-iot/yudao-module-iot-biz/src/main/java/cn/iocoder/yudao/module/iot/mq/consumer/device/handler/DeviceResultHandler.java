package cn.iocoder.yudao.module.iot.mq.consumer.device.handler;

import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;

/**
 * 设备命令结果处理器接口
 * 
 * <p>用于处理不同设备类型的命令执行结果。</p>
 * <p>每种设备类型可以实现自己的处理器来处理特定的业务逻辑。</p>
 * 
 * <p>Requirements: 4.2, 4.3</p>
 *
 * @author 长辉信息科技有限公司
 */
public interface DeviceResultHandler {

    /**
     * 获取处理器支持的设备类型
     * 
     * @return 设备类型标识（如 ACCESS_GEN1, ACCESS_GEN2, ALARM, NVR, CHANGHUI）
     */
    String getDeviceType();

    /**
     * 处理命令执行结果
     * 
     * <p>实现类可以在此方法中：</p>
     * <ul>
     *   <li>更新数据库中的设备状态</li>
     *   <li>记录操作日志</li>
     *   <li>触发后续业务流程</li>
     * </ul>
     * 
     * @param message 命令执行结果消息
     */
    void handleResult(IotDeviceMessage message);
}
