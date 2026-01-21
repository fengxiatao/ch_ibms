/**
 * 设备生命周期管理包
 * 
 * <p>提供设备状态管理和状态变更消息发布功能：</p>
 * <ul>
 *   <li>{@link cn.iocoder.yudao.module.iot.newgateway.core.lifecycle.DeviceLifecycleManager} - 设备生命周期管理器</li>
 *   <li>{@link cn.iocoder.yudao.module.iot.newgateway.core.lifecycle.DeviceStateChangePublisher} - 状态变更消息发布器</li>
 * </ul>
 * 
 * <p>状态转换规则（主动/被动连接设备统一）：</p>
 * <ul>
 *   <li>INACTIVE → ONLINE ↔ OFFLINE</li>
 * </ul>
 * 
 * <p>复用 iot-core 中的公共类：</p>
 * <ul>
 *   <li>IotDeviceStateEnum - 设备状态枚举</li>
 *   <li>DeviceStateChangeMessage - 状态变更消息</li>
 *   <li>ConnectionMode - 连接模式枚举</li>
 * </ul>
 * 
 * @author 长辉信息科技有限公司
 */
package cn.iocoder.yudao.module.iot.newgateway.core.lifecycle;
