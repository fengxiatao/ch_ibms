package cn.iocoder.yudao.module.iot.core.messagebus.topics;

/**
 * IoT 消息主题常量
 * 
 * <p>用于定义 MessageBus 的主题，实现 biz ↔ gateway 解耦通信</p>
 * 
 * <h3>统一消息通道架构（4 条核心通道）：</h3>
 * <pre>
 * 所有设备类型（门禁、报警、NVR、长辉等）都必须走这 4 条统一通道，
 * 通过消息参数中的 deviceType 和 commandType 来区分，
 * 禁止为不同设备类型创建不同的 Topic。
 * 
 * | 通道       | Topic                    | 方向                | 路由字段                    |
 * |------------|--------------------------|---------------------|----------------------------|
 * | 命令下行   | DEVICE_SERVICE_INVOKE    | Biz → NewGateway    | deviceType + commandType   |
 * | 命令回执   | DEVICE_SERVICE_RESULT    | NewGateway → Biz    | requestId                  |
 * | 状态变化   | DEVICE_STATE_CHANGED     | NewGateway → Biz    | deviceType + deviceId      |
 * | 事件上报   | DEVICE_EVENT_REPORTED    | NewGateway → Biz    | deviceType + eventType     |
 * </pre>
 * 
 * <h3>命名规范：</h3>
 * <pre>
 * 格式: iot_{resource}_{sub-resource}_{action}
 * 注意：RocketMQ Topic 命名规则只允许 [a-zA-Z0-9_-] 字符，不支持点号(.)
 * </pre>
 *
 * @author 长辉信息科技有限公司
 */
public interface IotMessageTopics {
    
    // ==================== 核心统一通道（4 条） ====================
    
    /**
     * 设备服务调用请求（命令下行）
     * <ul>
     *   <li>消息方向: Biz → NewGateway</li>
     *   <li>消息类型: IotDeviceMessage</li>
     *   <li>路由字段: deviceType + commandType</li>
     *   <li>响应消息: {@link #DEVICE_SERVICE_RESULT}</li>
     *   <li>用途: 所有设备控制命令（开门、布防、PTZ控制等）统一走此通道</li>
     * </ul>
     */
    String DEVICE_SERVICE_INVOKE = "iot_device_service_invoke";
    
    /**
     * 设备服务调用结果（命令回执）
     * <ul>
     *   <li>消息方向: NewGateway → Biz</li>
     *   <li>消息类型: IotDeviceMessage</li>
     *   <li>路由字段: requestId</li>
     *   <li>对应请求: {@link #DEVICE_SERVICE_INVOKE}</li>
     *   <li>用途: 所有命令执行结果统一走此通道</li>
     * </ul>
     */
    String DEVICE_SERVICE_RESULT = "iot_device_service_result";
    
    /**
     * 设备状态变化通知
     * <ul>
     *   <li>消息方向: NewGateway → Biz</li>
     *   <li>消息类型: DeviceStateChangeMessage</li>
     *   <li>路由字段: deviceType + deviceId</li>
     *   <li>触发时机: 设备状态发生变化时（OFFLINE→ONLINE, ONLINE→OFFLINE, INACTIVE→ONLINE）</li>
     *   <li>用途: 所有设备类型的状态变化统一走此通道，Biz服务接收后更新数据库状态并通过WebSocket推送给前端</li>
     * </ul>
     * @see cn.iocoder.yudao.module.iot.core.gateway.dto.DeviceStateChangeMessage
     */
    String DEVICE_STATE_CHANGED = "iot_device_state_changed";
    
    /**
     * 设备事件上报
     * <ul>
     *   <li>消息方向: NewGateway → Biz</li>
     *   <li>消息类型: IotDeviceMessage</li>
     *   <li>路由字段: deviceType + eventType</li>
     *   <li>触发时机: 设备产生事件（告警、刷卡、人脸识别、移动侦测等）</li>
     *   <li>用途: 所有设备类型的事件统一走此通道，通过 deviceType 区分设备类型</li>
     * </ul>
     */
    String DEVICE_EVENT_REPORTED = "iot_device_event_reported";
}
