/**
 * 消息发布模块
 * 
 * <p>提供统一的消息发布能力，所有设备插件通过此模块发布消息到消息总线。</p>
 * 
 * <p>核心组件：</p>
 * <ul>
 *   <li>{@link cn.iocoder.yudao.module.iot.newgateway.core.message.GatewayMessagePublisher} - 网关消息发布器</li>
 * </ul>
 * 
 * <p>复用 iot-core 中的公共类：</p>
 * <ul>
 *   <li>DeviceStateChangeMessage - 设备状态变更消息</li>
 *   <li>IotMessageTopics - 消息主题常量</li>
 *   <li>IotMessageBus - 消息总线接口</li>
 * </ul>
 * 
 * <p>Requirements: 2.1, 2.3</p>
 *
 * @author 长辉信息科技有限公司
 */
package cn.iocoder.yudao.module.iot.newgateway.core.message;
