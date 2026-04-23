/**
 * 消息消费者包
 * <p>
 * 接收来自 Biz 层的命令消息，并分发给对应的设备插件处理。
 * </p>
 * 
 * <h2>统一消息通道架构</h2>
 * <p>所有设备命令统一通过 {@code iot_device_service_invoke} 主题下发，
 * 通过消息中的 {@code deviceType} 和 {@code commandType} 路由到对应的设备插件。</p>
 * 
 * <h2>主要组件</h2>
 * <ul>
 *     <li>{@link cn.iocoder.yudao.module.iot.newgateway.consumer.DeviceCommandConsumer} - 
 *         设备控制命令消费者，监听 {@code iot_device_service_invoke} 主题</li>
 *     <li>{@link cn.iocoder.yudao.module.iot.newgateway.consumer.ConsumerConstants} - 
 *         消费者常量定义</li>
 * </ul>
 * 
 * <h2>消息流向</h2>
 * <pre>
 * Biz 层 → IotMessageBus (iot_device_service_invoke)
 *                     ↓
 *         DeviceCommandConsumer
 *                     ↓
 *         DevicePluginRegistry → DeviceHandler
 *                                     ↓
 *                               执行命令
 *                                     ↓
 *                               发布结果 (iot_device_service_result)
 * </pre>
 * 
 * <h2>Requirements</h2>
 * <ul>
 *     <li>2.1 - 统一消息发布机制</li>
 *     <li>3.1 - 插件注册表</li>
 *     <li>4.1 - 设备处理器接口</li>
 * </ul>
 */
package cn.iocoder.yudao.module.iot.newgateway.consumer;
