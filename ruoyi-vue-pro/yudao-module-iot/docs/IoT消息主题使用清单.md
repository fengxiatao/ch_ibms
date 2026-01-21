# IoT 消息主题使用清单

## 1. Topic 定义位置

**文件路径**：`yudao-module-iot-core/src/main/java/cn/iocoder/yudao/module/iot/core/messagebus/topics/IotMessageTopics.java`

## 2. 当前已定义 Topic 列表

### 2.1 设备生命周期

| Topic | 方向 | 消息类型 | 发布位置 | 订阅位置 |
|-------|------|---------|---------|---------|
| `iot.device.online` | Gateway → Biz | IotDeviceMessage | Gateway - DeviceConnectSubscriber | Biz - DeviceOnlineConsumer |
| `iot.device.offline` | Gateway → Biz | IotDeviceMessage | Gateway - 连接超时检测 | Biz - DeviceOfflineConsumer |

### 2.2 设备数据上报

| Topic | 方向 | 消息类型 | 发布位置 | 订阅位置 |
|-------|------|---------|---------|---------|
| `iot.device.event.reported` | Gateway → Biz | OnvifDeviceEvent | Gateway - OnvifEventProcessor | Biz - DeviceEventConsumer |
| `iot.device.property.reported` | Gateway → Biz | IotDeviceMessage | Gateway - 设备数据采集 | Biz - 属性消费者 |
| `iot.device.state.changed` | Gateway → Biz | IotDeviceMessage | Gateway - 状态监控 | Biz - 状态消费者 |

### 2.3 设备控制

| Topic | 方向 | 消息类型 | 发布位置 | 订阅位置 |
|-------|------|---------|---------|---------|
| `iot.device.connect.request` | Biz → Gateway | IotDeviceMessage | Biz - IotDeviceActivationServiceImpl | Gateway - DeviceConnectSubscriber |
| `iot.device.disconnect.request` | Biz → Gateway | IotDeviceMessage | Biz - IotDeviceActivationServiceImpl | Gateway - DeviceDisconnectSubscriber |
| `iot.device.service.invoke` | Biz → Gateway | OnvifServiceRequest | Biz - IotDeviceServiceInvokeServiceImpl | Gateway - ServiceInvokeSubscriber |

### 2.4 控制响应

| Topic | 方向 | 消息类型 | 发布位置 | 订阅位置 |
|-------|------|---------|---------|---------|
| `iot.device.connect.result` | Gateway → Biz | IotDeviceMessage | Gateway - DeviceConnectSubscriber | Biz - 连接结果消费者 |
| `iot.device.service.result` | Gateway → Biz | OnvifServiceResponse | Gateway - ServiceInvokeSubscriber | Biz - ServiceResultConsumer |

### 2.5 设备发现

| Topic | 方向 | 消息类型 | 发布位置 | 订阅位置 |
|-------|------|---------|---------|---------|
| `iot.device.discovered` | Gateway → Biz | DiscoveredDevice | Gateway - DeviceDiscoveryManager | Biz - DeviceDiscoveredConsumer |
| `iot.device.scan.request` | Biz → Gateway | DeviceScanRequest | Biz - IotDeviceDiscoveryServiceImpl | Gateway - DeviceScanSubscriber |
| `iot.device.scan.result` | Gateway → Biz | DeviceScanResult | Gateway - DeviceScanSubscriber | Biz - DeviceScanResultConsumer |

### 2.6 设备配置

| Topic | 方向 | 消息类型 | 发布位置 | 订阅位置 |
|-------|------|---------|---------|---------|
| `iot.device.config.sync.request` | Biz → Gateway | OnvifConfigMessage | Biz - IotDeviceConfigServiceImpl | Gateway - 配置同步订阅者 |
| `iot.device.config.sync.result` | Gateway → Biz | OnvifConfigMessage | Gateway - 配置同步处理器 | Biz - 配置同步消费者 |
| `iot.device.config.apply.request` | Biz → Gateway | OnvifConfigMessage | Biz - IotDeviceConfigServiceImpl | Gateway - 配置应用订阅者 |
| `iot.device.config.apply.result` | Gateway → Biz | OnvifConfigMessage | Gateway - 配置应用处理器 | Biz - 配置应用消费者 |

## 3. 代码使用示例

### 3.1 发布消息（Publisher）

```java
@Component
public class DeviceDiscoveryManager {
    
    @Resource
    private IotMessageBus messageBus;
    
    public void publishDiscoveryEvent(DiscoveredDevice device) {
        // 直接使用常量发布
        messageBus.post(IotMessageTopics.DEVICE_DISCOVERED, device);
        log.info("[publishDiscoveryEvent][发布设备发现消息: {}]", device.getIp());
    }
}
```

### 3.2 订阅消息（Subscriber）

```java
@Component
@Slf4j
public class DeviceDiscoveredConsumer implements IotMessageSubscriber<DiscoveredDevice> {
    
    @Resource
    private IotMessageBus messageBus;
    
    @PostConstruct
    public void init() {
        // 注册订阅者
        messageBus.register(this);
    }
    
    @Override
    public String getTopic() {
        // 返回订阅的 Topic
        return IotMessageTopics.DEVICE_DISCOVERED;
    }
    
    @Override
    public String getGroup() {
        // 返回消费者组名
        return "iot-biz-device-discovered-consumer";
    }
    
    @Override
    public void onMessage(DiscoveredDevice device) {
        // 处理消息
        log.info("[onMessage][收到设备发现消息: {}]", device.getIp());
    }
}
```

## 4. Consumer Group 命名规范

### 4.1 命名格式

```
{module}-{function}-consumer
```

**示例**：
- `iot-biz-device-online-consumer`
- `iot-biz-device-discovered-consumer`
- `iot-gateway-service-invoke-consumer`
- `iot-gateway-device-connect-consumer`

### 4.2 当前已使用的 Consumer Group

| Consumer Group | 订阅 Topic | 所在模块 | 类名 |
|---------------|-----------|---------|------|
| `iot-biz-device-online-consumer` | iot.device.online | Biz | DeviceOnlineConsumer |
| `iot-biz-device-offline-consumer` | iot.device.offline | Biz | DeviceOfflineConsumer |
| `iot-biz-device-event-consumer` | iot.device.event.reported | Biz | DeviceEventConsumer |
| `iot-biz-device-discovered-consumer` | iot.device.discovered | Biz | DeviceDiscoveredConsumer |
| `iot-biz-service-result-consumer` | iot.device.service.result | Biz | ServiceResultConsumer |
| `iot-biz-device-scan-result-consumer` | iot.device.scan.result | Biz | DeviceScanResultConsumer |
| `iot-gateway-device-connect-consumer` | iot.device.connect.request | Gateway | DeviceConnectSubscriber |
| `iot-gateway-device-disconnect-consumer` | iot.device.disconnect.request | Gateway | DeviceDisconnectSubscriber |
| `iot-gateway-service-invoke-consumer` | iot.device.service.invoke | Gateway | ServiceInvokeSubscriber |
| `iot-gateway-device-scan-consumer` | iot.device.scan.request | Gateway | DeviceScanSubscriber |

## 5. 添加新 Topic 的步骤

### 5.1 定义 Topic 常量

在 `IotMessageTopics` 接口中添加：

```java
/**
 * 新功能消息
 * <ul>
 *   <li>消息类型: YourMessageType</li>
 *   <li>消息方向: Gateway → Biz 或 Biz → Gateway</li>
 *   <li>触发时机: 描述何时发送此消息</li>
 * </ul>
 */
String YOUR_NEW_TOPIC = "iot.resource.action";
```

### 5.2 创建发布者

```java
@Component
public class YourPublisher {
    @Resource
    private IotMessageBus messageBus;
    
    public void publishMessage(YourMessageType message) {
        messageBus.post(IotMessageTopics.YOUR_NEW_TOPIC, message);
    }
}
```

### 5.3 创建订阅者

```java
@Component
public class YourConsumer implements IotMessageSubscriber<YourMessageType> {
    
    @Resource
    private IotMessageBus messageBus;
    
    @PostConstruct
    public void init() {
        messageBus.register(this);
    }
    
    @Override
    public String getTopic() {
        return IotMessageTopics.YOUR_NEW_TOPIC;
    }
    
    @Override
    public String getGroup() {
        return "iot-module-function-consumer";
    }
    
    @Override
    public void onMessage(YourMessageType message) {
        // 处理逻辑
    }
}
```

### 5.4 更新文档

- 在 `IoT消息主题命名规范.md` 中添加示例
- 在本文件中更新 Topic 列表和使用清单

## 6. 废弃 Topic 的处理

### 6.1 标记为 @Deprecated

```java
/**
 * 旧的 Topic 名称
 * @deprecated 请使用 {@link #NEW_TOPIC_NAME}
 */
@Deprecated
String OLD_TOPIC_NAME = "iot.old.topic";
```

### 6.2 保留过渡期

- 新旧 Topic 同时支持至少 2 个版本周期
- 在注释中说明废弃原因和迁移路径
- 在代码中添加警告日志

### 6.3 完全移除

- 确认所有使用方已迁移
- 从 `IotMessageTopics` 接口中删除
- 更新文档

## 7. 注意事项

### 7.1 Topic 命名

- ✅ 使用全小写字母和点号分隔
- ✅ 语义清晰，能够准确表达消息用途
- ✅ 遵循 `iot.{resource}.{action}` 格式
- ❌ 避免使用缩写
- ❌ 避免使用下划线或驼峰命名

### 7.2 消息类型

- 每个 Topic 应明确定义消息类型
- 消息类型应实现序列化（通常使用 JSON）
- 避免在消息中传递大量数据

### 7.3 Consumer Group

- 每个订阅者应有唯一的 Consumer Group
- Consumer Group 命名应清晰表达所属模块和功能
- 同一 Consumer Group 的多个实例会负载均衡消费消息

### 7.4 消息幂等性

- 消息可能重复投递，订阅者应实现幂等性处理
- 可使用消息ID或业务唯一键去重

### 7.5 错误处理

- 订阅者处理失败时，抛出异常会触发消息重试
- RocketMQ 默认重试 16 次
- 考虑实现死信队列处理

## 8. 相关文档

- [IoT消息主题命名规范.md](./IoT消息主题命名规范.md) - 详细的命名规范
- [RocketMQ 官方文档](https://rocketmq.apache.org/docs/quickStart/01quickstart/)
- [MessageBus 设计文档](../README.md) - 消息总线架构设计

---

**文档版本**：v1.0  
**最后更新**：2025-10-27  
**维护者**：长辉信息科技有限公司














