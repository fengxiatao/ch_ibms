package cn.iocoder.yudao.module.iot.simulator.core;

/**
 * 协议处理器接口
 * 负责消息的编解码和业务处理
 *
 * @param <T> 消息类型
 * @author Kiro
 */
public interface ProtocolHandler<T> {

    /**
     * 获取协议类型
     *
     * @return 协议类型标识
     */
    String getProtocolType();

    /**
     * 编码消息为字节数组
     *
     * @param message 消息对象
     * @return 编码后的字节数组
     */
    byte[] encode(T message);

    /**
     * 解码字节数组为消息对象
     *
     * @param data 字节数组
     * @return 解码后的消息对象，解码失败返回null
     */
    T decode(byte[] data);

    /**
     * 处理接收到的消息
     *
     * @param simulator 设备模拟器实例
     * @param message   接收到的消息
     */
    void handleMessage(DeviceSimulator simulator, T message);

    /**
     * 创建心跳消息
     *
     * @param deviceId 设备ID
     * @return 心跳消息对象
     */
    T createHeartbeat(String deviceId);
}
