# IoT 消息主题设计方案 V2.0
## 基于 AIOT 设备接入标准的优化方案

> **参考标准**：AIOT设备接入标准.docx  
> **设计目标**：结合行业标准和项目实际，设计规范、可扩展的 Topic 体系  
> 
> ⚠️ **重要说明**：本文档讨论的是 **RocketMQ Topic**（Gateway ↔ Biz 内部通信），  
> 不是 MQTT Topic（物理设备接入）。两者使用场景完全不同！  
> 详细架构说明请参考：[消息通信架构说明](./消息通信架构说明.md)

---

## 1. Topic 命名层次结构

### 1.1 总体设计原则

```
{namespace}/{module}/{device_id}/{category}/{function}/{direction}
```

| 层级 | 说明 | 示例值 | 是否必须 |
|------|------|--------|---------|
| namespace | 命名空间（项目级） | `ibms` 或 `iot` | 必须 |
| module | 模块标识 | `device`, `gateway`, `sys` | 必须 |
| device_id | 设备ID（变量） | `{device_id}` | 部分必须 |
| category | 功能类别 | `properties`, `commands`, `events` | 必须 |
| function | 具体功能 | `report`, `invoke`, `response` | 必须 |
| direction | 消息方向 | `up`, `down` | 可选 |

### 1.2 对比分析

| 方案 | Topic 示例 | 优点 | 缺点 |
|------|-----------|------|------|
| **当前方案** | `iot.device.online` | 简洁，易于理解 | 不支持设备级路由，扩展性有限 |
| **AIOT标准** | `$oc/devices/{device_id}/sys/properties/report` | 支持设备级路由，层次清晰 | 路径较长，需要变量替换 |
| **推荐方案** | `ibms/device/{device_id}/properties/up` | 结合两者优点，适合IBMS | 需要迁移成本 |

## 2. 新版 Topic 设计方案

### 2.1 设备生命周期（Device Lifecycle）

#### 2.1.1 设备上线/离线

```
# 当前方案（Gateway → Biz）
iot.device.online
iot.device.offline

# 新方案（推荐）
ibms/device/{device_id}/lifecycle/online
ibms/device/{device_id}/lifecycle/offline

# 优势：可以按设备ID订阅，支持更细粒度的消息过滤
```

#### 2.1.2 设备连接控制

```
# 请求（Biz → Gateway）
ibms/device/{device_id}/connection/connect/request
# 响应（Gateway → Biz）
ibms/device/{device_id}/connection/connect/response

# 请求（Biz → Gateway）
ibms/device/{device_id}/connection/disconnect/request
# 响应（Gateway → Biz）
ibms/device/{device_id}/connection/disconnect/response
```

### 2.2 设备数据上报（Data Report）

#### 2.2.1 属性上报

```
# 标准方案
ibms/device/{device_id}/properties/report

# 消息格式参考AIOT标准
{
    "services": [{
        "service_id": "Temperature",
        "properties": {
            "value": 25.5
        },
        "event_time": "2025-10-27T10:00:00Z"
    }]
}
```

#### 2.2.2 事件上报

```
# 标准方案
ibms/device/{device_id}/events/report

# 消息格式
{
    "event_type": "alarm",
    "event_id": "motion_detected",
    "event_time": "2025-10-27T10:00:00Z",
    "event_data": {
        "zone": "entrance",
        "confidence": 0.95
    }
}
```

### 2.3 设备命令下发（Command Invocation）

```
# 命令请求（Biz → Gateway）
ibms/device/{device_id}/commands/invoke/request_id={request_id}

# 命令响应（Gateway → Biz）
ibms/device/{device_id}/commands/invoke/response/request_id={request_id}

# 消息格式（请求）
{
    "command_id": "uuid",
    "command_name": "PTZ_CONTROL",
    "service_id": "CameraControl",
    "paras": {
        "action": "up",
        "speed": 50
    }
}

# 消息格式（响应）
{
    "request_id": "uuid",
    "result_code": 0,
    "result_desc": "success",
    "response_data": {
        "current_position": {
            "pan": 180,
            "tilt": 45
        }
    }
}
```

### 2.4 设备配置管理（Configuration）

```
# 配置同步请求
ibms/device/{device_id}/config/sync/request

# 配置同步响应
ibms/device/{device_id}/config/sync/response

# 配置应用请求
ibms/device/{device_id}/config/apply/request

# 配置应用响应
ibms/device/{device_id}/config/apply/response
```

### 2.5 设备发现（Device Discovery）

```
# 设备扫描请求（Biz → Gateway，广播）
ibms/gateway/discovery/scan/request

# 设备扫描结果（Gateway → Biz）
ibms/gateway/discovery/scan/response

# 设备发现通知（Gateway → Biz，单设备）
ibms/gateway/discovery/device/found

# 消息格式
{
    "device_ip": "192.168.1.100",
    "device_mac": "00:11:22:33:44:55",
    "protocol": "onvif",
    "vendor": "Dahua",
    "model": "IPC-HFW5831E",
    "discovery_time": "2025-10-27T10:00:00Z"
}
```

### 2.6 网关子设备（Gateway Sub-devices）

```
# 子设备批量属性上报
ibms/gateway/{gateway_id}/subdevices/properties/report

# 消息格式
{
    "devices": [
        {
            "device_id": "sub_device_001",
            "services": [{
                "service_id": "Temperature",
                "properties": {
                    "value": 25.5
                },
                "event_time": "2025-10-27T10:00:00Z"
            }]
        }
    ]
}
```

## 3. 通配符订阅支持

### 3.1 RocketMQ 通配符规则

RocketMQ 不支持复杂通配符，建议：
1. 使用多个具体 Topic 订阅
2. 在应用层实现消息过滤

### 3.2 MQTT 通配符规则（未来扩展）

```
# 单层通配符 +
ibms/device/+/properties/report      # 订阅所有设备的属性上报

# 多层通配符 #
ibms/device/{device_id}/#            # 订阅某设备的所有消息
ibms/device/+/events/#               # 订阅所有设备的所有事件
```

## 4. 迁移方案

### 4.1 双Topic并行期（推荐）

```java
// 同时发布新旧两个Topic（过渡期）
public void publishDeviceOnline(Long deviceId, IotDeviceMessage message) {
    // 旧Topic（兼容）
    messageBus.post(IotMessageTopics.DEVICE_ONLINE, message);
    
    // 新Topic（推荐）
    String newTopic = String.format("ibms/device/%d/lifecycle/online", deviceId);
    messageBus.post(newTopic, message);
}
```

### 4.2 Consumer Group 迁移

```java
// 旧Consumer（标记废弃）
@Deprecated
public class DeviceOnlineConsumerOld implements IotMessageSubscriber<IotDeviceMessage> {
    @Override
    public String getTopic() {
        return IotMessageTopics.DEVICE_ONLINE;  // 旧Topic
    }
    
    @Override
    public String getGroup() {
        return "iot-biz-device-online-consumer-old";
    }
}

// 新Consumer
public class DeviceOnlineConsumer implements IotMessageSubscriber<IotDeviceMessage> {
    @Override
    public String getTopic() {
        // 注意：RocketMQ不支持通配符，需要为每个设备创建订阅
        // 或者使用固定的设备列表
        return "ibms/device/*/lifecycle/online";  // 理想情况
    }
    
    @Override
    public String getGroup() {
        return "iot-biz-device-lifecycle-consumer";
    }
}
```

### 4.3 变量替换工具类

```java
/**
 * Topic 工具类
 */
public class IotTopicUtils {
    
    /**
     * 构建设备级Topic
     */
    public static String buildDeviceTopic(String template, Long deviceId) {
        return template.replace("{device_id}", String.valueOf(deviceId));
    }
    
    /**
     * 构建请求-响应Topic
     */
    public static String buildRequestTopic(String template, Long deviceId, String requestId) {
        return template
            .replace("{device_id}", String.valueOf(deviceId))
            .replace("{request_id}", requestId);
    }
    
    /**
     * 从Topic中提取设备ID
     */
    public static Long extractDeviceId(String topic) {
        // 从 ibms/device/123/properties/report 提取 123
        String[] parts = topic.split("/");
        if (parts.length >= 3 && "device".equals(parts[1])) {
            return Long.parseLong(parts[2]);
        }
        return null;
    }
}
```

## 5. RocketMQ 适配方案

### 5.1 问题：RocketMQ 不支持 `/` 通配符

RocketMQ Topic 命名限制：
- 支持字母、数字、下划线、连字符
- 建议使用点号或下划线分隔

### 5.2 解决方案A：转换为点号格式

```java
// 逻辑Topic（代码中使用）
String logicalTopic = "ibms/device/{device_id}/properties/report";

// 物理Topic（RocketMQ实际使用）
String physicalTopic = logicalTopic.replace("/", ".");
// 结果：ibms.device.{device_id}.properties.report
```

### 5.3 解决方案B：简化分层

```java
// 针对RocketMQ优化的Topic设计
public interface IotMessageTopicsV2 {
    
    // 设备生命周期
    String DEVICE_LIFECYCLE_PREFIX = "ibms.device.lifecycle.";
    String DEVICE_ONLINE_TEMPLATE = DEVICE_LIFECYCLE_PREFIX + "{device_id}.online";
    String DEVICE_OFFLINE_TEMPLATE = DEVICE_LIFECYCLE_PREFIX + "{device_id}.offline";
    
    // 属性上报
    String DEVICE_PROPERTIES_PREFIX = "ibms.device.properties.";
    String DEVICE_PROPERTY_REPORT_TEMPLATE = DEVICE_PROPERTIES_PREFIX + "{device_id}.report";
    
    // 事件上报
    String DEVICE_EVENTS_PREFIX = "ibms.device.events.";
    String DEVICE_EVENT_REPORT_TEMPLATE = DEVICE_EVENTS_PREFIX + "{device_id}.report";
    
    // 命令下发
    String DEVICE_COMMANDS_PREFIX = "ibms.device.commands.";
    String DEVICE_COMMAND_REQUEST_TEMPLATE = DEVICE_COMMANDS_PREFIX + "{device_id}.{request_id}.request";
    String DEVICE_COMMAND_RESPONSE_TEMPLATE = DEVICE_COMMANDS_PREFIX + "{device_id}.{request_id}.response";
}
```

### 5.4 推荐方案：分级订阅

```java
/**
 * 使用前缀模式订阅
 */
@Component
public class DevicePropertiesConsumer {
    
    @PostConstruct
    public void init() {
        // 订阅所有设备的属性上报（使用前缀）
        String topicPattern = "ibms.device.properties.*";
        
        // 注意：RocketMQ需要明确的Topic名称，不支持通配符
        // 实际实现中，需要：
        // 1. 为每个设备创建独立的Topic
        // 2. 或使用统一的Topic，在消息体中包含device_id
    }
}
```

## 6. 最终推荐方案

### 6.1 折中方案：保留简洁性，增强语义

```java
public interface IotMessageTopicsV2 {
    
    // ==================== 设备级消息（包含device_id变量） ====================
    
    /**
     * 设备属性上报
     * Topic模板: ibms.device.properties.{device_id}
     * 消息方向: Gateway → Biz
     */
    String DEVICE_PROPERTY_REPORT = "ibms.device.properties.{device_id}";
    
    /**
     * 设备事件上报
     * Topic模板: ibms.device.events.{device_id}
     * 消息方向: Gateway → Biz
     */
    String DEVICE_EVENT_REPORT = "ibms.device.events.{device_id}";
    
    /**
     * 设备命令请求
     * Topic模板: ibms.device.commands.{device_id}.request
     * 消息方向: Biz → Gateway
     */
    String DEVICE_COMMAND_REQUEST = "ibms.device.commands.{device_id}.request";
    
    /**
     * 设备命令响应
     * Topic模板: ibms.device.commands.{device_id}.response
     * 消息方向: Gateway → Biz
     */
    String DEVICE_COMMAND_RESPONSE = "ibms.device.commands.{device_id}.response";
    
    // ==================== 广播级消息（不包含device_id） ====================
    
    /**
     * 设备发现通知
     * Topic: ibms.gateway.discovery.found
     * 消息方向: Gateway → Biz
     */
    String GATEWAY_DEVICE_DISCOVERED = "ibms.gateway.discovery.found";
    
    /**
     * 设备扫描请求
     * Topic: ibms.gateway.discovery.scan.request
     * 消息方向: Biz → Gateway
     */
    String GATEWAY_SCAN_REQUEST = "ibms.gateway.discovery.scan.request";
}
```

### 6.2 使用示例

```java
@Component
public class DevicePropertyPublisher {
    
    @Resource
    private IotMessageBus messageBus;
    
    public void publishProperty(Long deviceId, DevicePropertyMessage message) {
        // 构建实际Topic
        String topic = IotTopicUtils.buildDeviceTopic(
            IotMessageTopicsV2.DEVICE_PROPERTY_REPORT, 
            deviceId
        );
        // 结果: ibms.device.properties.12345
        
        messageBus.post(topic, message);
    }
}

@Component
public class DevicePropertyConsumer implements IotMessageSubscriber<DevicePropertyMessage> {
    
    @Override
    public String getTopic() {
        // 方案1：订阅特定设备
        return "ibms.device.properties.12345";
        
        // 方案2：订阅所有设备（需要应用层过滤）
        // return "ibms.device.properties.*";  // RocketMQ不支持
    }
    
    @Override
    public String getGroup() {
        return "ibms-biz-device-property-consumer";
    }
    
    @Override
    public void onMessage(DevicePropertyMessage message) {
        Long deviceId = IotTopicUtils.extractDeviceId(getTopic());
        // 处理消息
    }
}
```

## 7. 实施建议

### 7.1 短期（当前版本）

1. **保持现有Topic不变**，确保系统稳定
2. **新增V2版本接口**，与现有接口并行
3. **逐步迁移**核心功能到V2

### 7.2 中期（下一版本）

1. **全面切换到V2 Topic**
2. **废弃旧Topic**，保留6个月过渡期
3. **更新文档和培训**

### 7.3 长期（未来规划）

1. 支持更多设备协议（**CoAP**、**LwM2M**）
2. 实现 **边缘计算网关**（本地处理能力）
3. 支持 **设备级订阅**和**动态Topic管理**

**注意**：项目已使用 EMQX 作为 MQTT Broker（设备接入层），RocketMQ 用于内部微服务通信，两者各司其职，不应混淆。详见：[消息通信架构说明](./消息通信架构说明.md)

## 8. 对比总结

| 特性 | 当前方案 | AIOT标准 | 推荐方案V2 |
|------|---------|---------|-----------|
| **简洁性** | ✅ 优秀 | ❌ 较长 | ✅ 良好 |
| **设备级路由** | ❌ 不支持 | ✅ 支持 | ✅ 支持 |
| **可扩展性** | ⚠️ 一般 | ✅ 优秀 | ✅ 良好 |
| **RocketMQ兼容** | ✅ 完美 | ⚠️ 需转换 | ✅ 良好 |
| **MQTT兼容** | ⚠️ 一般 | ✅ 完美 | ✅ 良好 |
| **迁移成本** | - | ❌ 高 | ⚠️ 中等 |

## 9. 参考资料

- [AIOT设备接入标准.docx](../AIOT设备接入标准.docx)
- [华为IoT平台 - Topic说明](https://support.huaweicloud.com/usermanual-iothub/iot_01_0021.html)
- [阿里云IoT - Topic规范](https://help.aliyun.com/document_detail/89300.html)
- [MQTT Topic最佳实践](https://www.hivemq.com/blog/mqtt-essentials-part-5-mqtt-topics-best-practices/)

---

**文档版本**：v2.0  
**最后更新**：2025-10-27  
**维护者**：长辉信息科技有限公司

