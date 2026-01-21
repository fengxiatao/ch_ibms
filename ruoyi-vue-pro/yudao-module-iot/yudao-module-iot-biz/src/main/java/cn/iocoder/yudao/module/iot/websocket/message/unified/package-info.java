/**
 * 统一 WebSocket 消息 DTO 包
 * 
 * <p>本包包含用于 Biz 层与前端 WebSocket 通信的统一消息格式定义。</p>
 * 
 * <h2>消息类型</h2>
 * <ul>
 *   <li>{@link cn.iocoder.yudao.module.iot.websocket.message.unified.UnifiedDeviceStatusMessage} - 设备状态消息</li>
 *   <li>{@link cn.iocoder.yudao.module.iot.websocket.message.unified.UnifiedDeviceEventMessage} - 设备事件消息</li>
 *   <li>{@link cn.iocoder.yudao.module.iot.websocket.message.unified.UnifiedCommandResultMessage} - 命令结果消息</li>
 * </ul>
 * 
 * <h2>设计原则</h2>
 * <ul>
 *   <li>所有消息都包含 messageType 字段用于前端识别消息类型</li>
 *   <li>所有消息都包含 deviceId 和 deviceType 字段用于设备识别</li>
 *   <li>所有消息都包含 timestamp 字段用于时间追踪</li>
 * </ul>
 * 
 * <h2>Requirements</h2>
 * <ul>
 *   <li>Requirements 8.1 - 设备状态消息格式</li>
 *   <li>Requirements 8.2 - 设备事件消息格式</li>
 *   <li>Requirements 8.3 - 命令结果消息格式</li>
 *   <li>Requirements 8.4 - 消息格式一致性</li>
 * </ul>
 * 
 * @author 长辉信息科技有限公司
 */
package cn.iocoder.yudao.module.iot.websocket.message.unified;
