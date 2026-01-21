# IoT 消息主题（Topic）命名规范

## 1. 概述

本文档定义 IoT 模块中消息总线（MessageBus）的 Topic 命名规范，确保系统消息主题命名的一致性、可读性和可维护性。

## 2. 命名格式

### 2.1 基本格式

```
{namespace}.{resource}.{sub-resource}.{action}
```

**各部分说明：**

| 部分 | 说明 | 是否必须 | 示例 |
|------|------|---------|------|
| namespace | 命名空间，固定为 `iot` | 必须 | iot |
| resource | 资源类型 | 必须 | device, product, alarm |
| sub-resource | 子资源或功能模块 | 可选 | config, service, property |
| action | 操作动作 | 必须 | online, offline, discovered, sync |

### 2.2 命名规则

1. **全小写字母**：所有单词均使用小写字母
2. **点号分隔**：使用英文句号 `.` 分隔各部分
3. **动词使用过去分词**：操作动作优先使用过去分词形式（discovered、connected、synced）
4. **避免缩写**：使用完整单词，避免使用缩写（除非是行业通用缩写）
5. **语义清晰**：命名应准确反映消息的用途和内容

## 3. 资源类型（Resource）

### 3.1 核心资源

| 资源名 | 说明 | 示例 Topic |
|--------|------|-----------|
| device | 设备 | iot.device.online |
| product | 产品 | iot.product.created |
| alarm | 告警 | iot.alarm.triggered |
| data | 数据 | iot.data.reported |
| rule | 规则 | iot.rule.executed |

### 3.2 子资源（Sub-Resource）

| 子资源名 | 说明 | 适用资源 | 示例 |
|---------|------|----------|------|
| config | 配置 | device, product | iot.device.config.sync.request |
| service | 服务调用 | device | iot.device.service.invoke |
| property | 属性 | device | iot.device.property.reported |
| event | 事件 | device | iot.device.event.reported |
| state | 状态 | device, alarm | iot.device.state.changed |

## 4. 操作动作（Action）

### 4.1 生命周期动作

| 动作 | 说明 | 示例 |
|------|------|------|
| created | 创建 | iot.product.created |
| updated | 更新 | iot.device.updated |
| deleted | 删除 | iot.device.deleted |
| activated | 激活 | iot.device.activated |
| deactivated | 停用 | iot.device.deactivated |

### 4.2 连接状态动作

| 动作 | 说明 | 示例 |
|------|------|------|
| online | 上线 | iot.device.online |
| offline | 离线 | iot.device.offline |
| connected | 已连接 | iot.device.connected |
| disconnected | 已断开 | iot.device.disconnected |

### 4.3 数据操作动作

| 动作 | 说明 | 示例 |
|------|------|------|
| reported | 上报 | iot.device.property.reported |
| synced | 同步 | iot.device.config.synced |
| changed | 变更 | iot.device.state.changed |
| discovered | 发现 | iot.device.discovered |

### 4.4 请求-响应动作

| 动作类型 | 动作后缀 | 说明 | 示例 |
|---------|---------|------|------|
| 请求 | .request | 请求操作 | iot.device.service.invoke.request |
| 响应 | .response | 响应结果 | iot.device.service.invoke.response |
| 结果 | .result | 执行结果 | iot.device.config.sync.result |

**注意**：为简化命名，当消息方向明确时，可省略 `.request` 和 `.response` 后缀。

## 5. 消息方向约定

### 5.1 Gateway → Biz

从网关层到业务层的消息，通常是**上报类**消息：

```
iot.device.online              # 设备上线
iot.device.offline             # 设备离线
iot.device.event.reported      # 事件上报（建议格式）
iot.device.property.reported   # 属性上报（建议格式）
iot.device.discovered          # 设备发现
```

### 5.2 Biz → Gateway

从业务层到网关层的消息，通常是**控制类/请求类**消息：

```
iot.device.connect.request     # 连接设备请求
iot.device.disconnect.request  # 断开设备请求
iot.device.service.invoke      # 服务调用
iot.device.config.apply        # 应用配置
```

### 5.3 响应消息

响应消息应在原请求 Topic 基础上添加 `.result` 或 `.response`：

```
请求: iot.device.connect.request
响应: iot.device.connect.result
或
响应: iot.device.connect.response
```

## 6. 完整示例

### 6.1 设备生命周期

```java
// 设备发现
iot.device.discovered

// 设备上线/离线
iot.device.online
iot.device.offline

// 设备连接/断开（请求-响应）
iot.device.connect.request
iot.device.connect.result
iot.device.disconnect.request
iot.device.disconnect.result
```

### 6.2 设备数据

```java
// 属性上报
iot.device.property.reported

// 事件上报
iot.device.event.reported

// 状态变化
iot.device.state.changed
```

### 6.3 设备服务

```java
// 服务调用（请求-响应）
iot.device.service.invoke
iot.device.service.result
```

### 6.4 设备配置

```java
// 配置同步（请求-响应）
iot.device.config.sync.request
iot.device.config.sync.result

// 配置应用（请求-响应）
iot.device.config.apply.request
iot.device.config.apply.result
```

### 6.5 设备扫描

```java
// 扫描请求-结果
iot.device.scan.request
iot.device.scan.result
```

## 7. RocketMQ Topic 配置

### 7.1 Topic 命名转换

由于 RocketMQ Topic 名称限制，实际发送时可能需要做转换：

```java
// 代码中的 Topic（逻辑名称）
String logicalTopic = "iot.device.online";

// RocketMQ Topic（物理名称，如果需要的话）
String physicalTopic = "iot_device_online";  // 将点号转为下划线
```

**当前实现**：项目直接使用点号形式作为 RocketMQ Topic，RocketMQ 支持此格式。

### 7.2 Consumer Group 命名

Consumer Group 应与订阅的模块和功能相关：

```
格式: {module}-{function}-consumer

示例:
- iot-biz-device-online-consumer
- iot-gateway-service-invoke-consumer
- iot-biz-device-discovered-consumer
```

## 8. 代码实现位置

### 8.1 Topic 常量定义

**位置**：`yudao-module-iot-core/src/main/java/cn/iocoder/yudao/module/iot/core/messagebus/topics/IotMessageTopics.java`

**示例**：
```java
public interface IotMessageTopics {
    /**
     * 设备上线通知
     * 消息类型: IotDeviceMessage
     * 消息方向: Gateway → Biz
     */
    String DEVICE_ONLINE = "iot.device.online";
}
```

### 8.2 Topic 使用

**发布消息**：
```java
@Resource
private IotMessageBus messageBus;

// 发布消息
messageBus.post(IotMessageTopics.DEVICE_ONLINE, deviceMessage);
```

**订阅消息**：
```java
@Component
public class DeviceOnlineConsumer implements IotMessageSubscriber<IotDeviceMessage> {
    
    @Override
    public String getTopic() {
        return IotMessageTopics.DEVICE_ONLINE;
    }
    
    @Override
    public String getGroup() {
        return "iot-biz-device-online-consumer";
    }
    
    @Override
    public void onMessage(IotDeviceMessage message) {
        // 处理消息
    }
}
```

## 9. 命名检查清单

在添加新 Topic 前，请检查以下事项：

- [ ] Topic 名称符合 `iot.{resource}.{action}` 或 `iot.{resource}.{sub-resource}.{action}` 格式
- [ ] 使用全小写字母，单词之间用点号分隔
- [ ] 动作使用过去分词或明确的动词形式
- [ ] 在 `IotMessageTopics` 接口中添加常量定义
- [ ] 添加完整的 JavaDoc 注释（消息类型、消息方向）
- [ ] Consumer Group 命名符合规范
- [ ] 更新本文档的示例（如果是新的资源类型或动作）

## 10. 不推荐的命名

❌ **错误示例**：

```java
// 使用大写字母
String DEVICE_ONLINE = "IOT.DEVICE.ONLINE";  

// 使用下划线分隔
String DEVICE_ONLINE = "iot_device_online";  

// 使用缩写
String DEV_ON = "iot.dev.on";  

// 动作不明确
String DEVICE = "iot.device";  

// 过度嵌套
String COMPLEX = "iot.device.config.network.wifi.sync.request.v2";
```

✅ **正确示例**：

```java
String DEVICE_ONLINE = "iot.device.online";
String DEVICE_CONFIG_SYNCED = "iot.device.config.synced";
String DEVICE_DISCOVERED = "iot.device.discovered";
```

## 11. 版本管理

当需要对 Topic 进行不兼容变更时，建议通过以下方式处理：

1. **新增 Topic**：创建新的 Topic 名称（如添加 v2 后缀）
2. **保留旧 Topic**：旧 Topic 继续保留一段时间，标记为 `@Deprecated`
3. **文档说明**：在注释中说明迁移路径

```java
/**
 * 设备上线通知（旧版本，已废弃）
 * @deprecated 请使用 {@link #DEVICE_ONLINE_V2}
 */
@Deprecated
String DEVICE_ONLINE = "iot.device.online";

/**
 * 设备上线通知（新版本）
 * 消息类型: IotDeviceOnlineMessage
 */
String DEVICE_ONLINE_V2 = "iot.device.online.v2";
```

## 12. 参考资料

- [RocketMQ 最佳实践](https://rocketmq.apache.org/docs/bestPractice/01bestpractice)
- [阿里云 IoT 物联网平台消息通信](https://help.aliyun.com/document_detail/89300.html)
- [MQTT Topic 命名规范](https://www.hivemq.com/blog/mqtt-essentials-part-5-mqtt-topics-best-practices/)

---

**文档版本**：v1.0  
**最后更新**：2025-10-27  
**维护者**：长辉信息科技有限公司














