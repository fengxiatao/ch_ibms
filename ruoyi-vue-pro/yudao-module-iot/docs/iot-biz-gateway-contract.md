# Biz ↔ NewGateway 契约说明（摘录）

> **主文档**（分步落地）：[`iot-newgateway-biz-mq-landing-plan.md`](./iot-newgateway-biz-mq-landing-plan.md) **步骤 2**。以下与主文档一致，便于单独打开查阅。

## 命令下行（`IotMessageTopics.DEVICE_SERVICE_INVOKE`）

载荷类型：`IotDeviceMessage`

| 字段 | 必填 | 说明 |
|------|------|------|
| `deviceId` | 是 | 设备主键（IBMS 与 `ibms_device.id` 对齐场景下同源） |
| `requestId` | 是 | 幂等/回执关联 |
| `method` / `params.commandType` | 是 | 业务命令类型，消费端兼容 `method` 与 `params.commandType` |
| `params.deviceType` | 是 | 网关插件 `deviceType`（如 `NVR`、`ACCESS_GEN1`） |
| `brand` | 否 | IBMS 品牌码（`HIK`、`DAH` 等），写入消息体顶层或 `params.brand` |
| `vendorKey` | 否 | 厂商/协议键，兼容扩展 |
| `pluginId` | 否 | 显式插件 id |
| `messageVersion` | 否 | 契约版本，默认 `1` |

**兼容策略**：缺省 `brand` / `vendorKey` / `pluginId` 时，网关按插件默认厂商处理；NVR 缺 brand 时默认走大华 SDK 路径。

## 设备配置下发（`IotMessageTopics.DEVICE_PROFILE_CHANGED`）

载荷类型：`DeviceProfileChangedMessage`

- `op`：`UPSERT` / `DELETE`
- 必含：`deviceId`；`UPSERT` 建议含 `tenantId`、`deviceType`、`config`、`address`、`brand` 等连接字段

## Request-Reply 补快照

- 请求：`GATEWAY_DEVICE_SNAPSHOT_REQUEST` → `GatewayDeviceSnapshotRequestMessage`（`correlationId` + `deviceId`）
- 应答：`GATEWAY_DEVICE_SNAPSHOT_REPLY` → `GatewayDeviceSnapshotReplyMessage`（`code` + `device`）

Biz 侧消费组：`iot-biz-gateway-snapshot-request`。网关侧应答消费组：`iot-newgateway-device-snapshot-reply`。

## 状态与事件

沿用既有 `DEVICE_STATE_CHANGED`、`DEVICE_EVENT_REPORTED`、`DEVICE_SERVICE_RESULT` 语义，不在此重复。
