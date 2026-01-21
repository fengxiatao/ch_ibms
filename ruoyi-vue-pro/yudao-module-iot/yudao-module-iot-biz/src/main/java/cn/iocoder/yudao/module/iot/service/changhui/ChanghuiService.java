package cn.iocoder.yudao.module.iot.service.changhui;

/**
 * 长辉设备服务接口
 * 
 * <p>提供长辉TCP模拟设备的控制功能，主要用于远程升级</p>
 * <p>所有命令通过统一的 DeviceCommandPublisher 发送到 DEVICE_SERVICE_INVOKE 主题</p>
 *
 * @author 长辉信息科技有限公司
 * @see cn.iocoder.yudao.module.iot.mq.producer.DeviceCommandPublisher
 */
public interface ChanghuiService {

    /**
     * 触发设备升级
     * 
     * <p>发送升级触发命令到设备，设备收到后会准备进入升级模式</p>
     * 
     * @param deviceId 设备ID
     * @return 请求ID，用于关联响应
     * @throws IllegalArgumentException 如果 deviceId 为空
     */
    String triggerUpgrade(Long deviceId);

    /**
     * 下发升级URL
     * 
     * <p>发送升级URL到设备，设备会从该URL下载固件进行升级</p>
     * 
     * @param deviceId 设备ID
     * @param url      固件下载URL
     * @return 请求ID，用于关联响应
     * @throws IllegalArgumentException 如果 deviceId 或 url 为空
     */
    String sendUpgradeUrl(Long deviceId, String url);

    /**
     * 查询设备状态
     * 
     * <p>查询设备当前状态，包括在线状态、升级状态等</p>
     * 
     * @param deviceId 设备ID
     * @return 请求ID，用于关联响应
     * @throws IllegalArgumentException 如果 deviceId 为空
     */
    String queryStatus(Long deviceId);

    /**
     * 执行设备升级（完整流程）
     * 
     * <p>先触发升级，然后下发升级URL，完成完整的升级流程</p>
     * 
     * @param deviceId 设备ID
     * @param url      固件下载URL
     * @return 请求ID，用于关联响应
     * @throws IllegalArgumentException 如果 deviceId 或 url 为空
     */
    String executeUpgrade(Long deviceId, String url);
}
