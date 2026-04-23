# IoT 消息主题与消费组使用清单

> **维护约定**：Topic 与消息体以代码为准：`IotMessageTopics`（`yudao-module-iot-core`）及各 `*Message` DTO。本文仅作团队速查；变更常量或消费者时请同步改本文一行摘要。  
> **主流程文档**：`iot-newgateway-biz-mq-landing-plan.md`

---

## 1. RocketMQ Topic（与 `IotMessageTopics` 一致）

| 常量 / Topic 名 | 方向（典型） | 消息体（payload） | 说明 |
|-----------------|-------------|-------------------|------|
| `iot_device_service_invoke` | Biz → Gateway | `IotDeviceMessage` | 命令下行，按 `deviceType` + `commandType` 路由 |
| `iot_device_service_result` | Gateway → Biz | `IotDeviceMessage` | 命令回执，关联 `requestId` |
| `iot_device_state_changed` | Gateway → Biz | `DeviceStateChangeMessage` | 设备上下线等状态 |
| `iot_device_event_reported` | Gateway → Biz | `IotDeviceMessage` | 告警、刷卡、视频事件等 |
| `iot_device_profile_changed` | Biz → Gateway | `DeviceProfileChangedMessage` | 台账/配置缓存 upsert、delete |
| `iot_gateway_device_snapshot_request` | Gateway → Biz | `GatewayDeviceSnapshotRequestMessage` | 缓存未命中拉快照（Request） |
| `iot_gateway_device_snapshot_reply` | Biz → Gateway | `GatewayDeviceSnapshotReplyMessage` | 快照应答（Reply） |
| `iot_gateway_profile_warmup_request` | Gateway → Biz | `GatewayProfileWarmupRequestMessage` | 冷启动批量预热，Biz 等价全量重推 profile |

---

## 2. 消费者组（代码中的「逻辑组名」）

配置项 `yudao.iot.message-bus.consumer-group-prefix` 会在运行时拼到组名前（如 `ch-`、`dev-`）。下表为 **未加前缀** 的原始组名（与日志 `register` 中「原始」一致）。

### 2.1 NewGateway（`ConsumerConstants`：`iot-newgateway-*`）

| 逻辑组名 | Topic | 实现类 |
|----------|-------|--------|
| `iot-newgateway-device-command` | `iot_device_service_invoke` | `DeviceCommandConsumer` |
| `iot-newgateway-device-profile` | `iot_device_profile_changed` | `DeviceProfileChangedConsumer` |
| `iot-newgateway-device-snapshot-reply` | `iot_gateway_device_snapshot_reply` | `GatewayDeviceSnapshotReplyConsumer` |

### 2.2 IoT Biz（`iot-biz-*`）

| 逻辑组名 | Topic | 实现类 |
|----------|-------|--------|
| `iot-biz-device-result` | `iot_device_service_result` | `DeviceServiceResultConsumer` |
| `iot-biz-device-state` | `iot_device_state_changed` | `DeviceStateChangeConsumer` |
| `iot-biz-device-event` | `iot_device_event_reported` | `DeviceEventConsumer` |
| `iot-biz-gateway-snapshot-request` | `iot_gateway_device_snapshot_request` | `GatewayDeviceSnapshotRequestConsumer` |
| `iot-biz-gateway-profile-warmup` | `iot_gateway_profile_warmup_request` | `GatewayProfileWarmupRequestConsumer` |

---

## 3. 死信队列（DLQ）与重试

- **最大重试**：`IotRocketMQMessageBus` 中 `DefaultMQPushConsumer#setMaxReconsumeTimes(16)`（超过后进入死信队列，以当前客户端版本行为为准）。
- **指标**：进程内存在 `MeterRegistry` Bean 时（如启用 Actuator），每次消费失败会 `increment` 计数器 **`iot.messagebus.consume.failure`**，标签：`topic`、`consumer_group`（逻辑组名）、`subscriber`（消费者类简名）、`near_dlq`（`true`/`false`）。
- **日志**：消费抛错返回 `RECONSUME_LATER` 时，日志中带 `reconsumeTimes`；当达到「最后一次可重试」前，会附加提示 **下次失败将进入死信队列**，便于日志告警规则匹配。
- **DLQ Topic（常见命名）**：RocketMQ 会为消费组创建系统 Topic，名称通常为 **`%DLQ%` + 实际运行时的消费组全名**（含环境前缀，如 `ch-iot-newgateway-device-command`）。请以 Broker / Dashboard 为准，用 `mqadmin topicList` 或控制台检索 `%DLQ%`。
- **运维建议**：对 `%DLQ%*` 订阅积压配置告警；入 DLQ 后需人工排查消息体与消费逻辑，再决定是否重投或丢弃。

---

## 4. 相关代码入口

| 说明 | 路径 |
|------|------|
| Topic 常量 | `yudao-module-iot-core/.../IotMessageTopics.java` |
| RocketMQ 注册与监听 | `yudao-module-iot-core/.../IotRocketMQMessageBus.java` |
| 消费组前缀 | `IotMessageBusProperties#getPrefixedConsumerGroup` |
| 网关侧组名常量 | `yudao-module-iot-newgateway-core/.../ConsumerConstants.java` |
