# IoT NewGateway × Biz：单文档分步落地指南（唯一主文档）

> **用法**：新建会话或新同事入场，**只维护、只打开本文**。自上而下按「步骤」执行；每步内的 `[x]` / `[ ]` 表示当前仓库是否已落地。  
> **维护**：完成某步后把该步勾选改为 `[x]`，并在文末「变更记录」记一行。  
> **说明**：下文已吸收原分散文档中的契约、红线、薄网关拆分、P0–P5、Playbook 要点；其它 `.md` 仅作可选速查（见附录 C）。

---

## 图例

| 标记 | 含义 |
|------|------|
| `[x]` | 已在仓库落地，可按「验证」自检 |
| `[ ]` | 未做或未完成 |
| **完成 / 部分 / 文档** | 人读状态，与勾选配合使用 |

---

## 步骤总览（目录）

| 序号 | 步骤 | 内容摘要 |
|------|------|----------|
| 0 | 原则与红线 | 轻量网关边界 |
| 1 | 环境与 RocketMQ | NameServer、Topic、消费组 |
| 2 | 契约冻结 | 命令与 profile、Request-Reply 字段 |
| 3 | Biz：设备配置下发 | `DeviceProfileChanged` |
| 4 | Biz：快照 Request-Reply | 网关缺参补快照 |
| 5 | Biz：网关 HTTP RPC | 默认关闭 + 条件控制器 |
| 6 | Gateway：Profile 缓存 | 消费 + 本地 map |
| 7 | Gateway：MQ 设备访问 | 缓存 miss → RR |
| 8 | 命令消息对齐 | `brand` / `messageVersion` 等 |
| 9 | 插件与多厂家 | NVR 等 Adapter 策略 |
| 10 | 可观测与会话 | 指标、健康检查、DeviceSession |
| 11 | 薄网关 + Maven 多模块 | 壳与 `gateway-domain-*` 拆分 |
| 12 | 业务波次 P0–P5 | IBMS 与业务交付节奏 |
| 附录 A | 新设备 Playbook | 竖切接入勾选 |
| 附录 B | 变更记录 | 人工填写 |
| 附录 C | 与其它文档关系 | 可选速查 |

---

## 步骤 0：原则与红线（轻量网关）

**目标**：网关只做接入与命令中转，不写业务库、不拉 Biz HTTP（新建基线）。

**允许**：RocketMQ 与 Biz 通信；Redis 做幂等/去重/限流；插件内 SDK、ConnectionManager、厂商 Adapter；`DeviceLifecycleManager`（或统一门面）作为状态上送入口。

**禁止**：依赖 `yudao-module-iot-biz`、业务 JDBC/MyBatis 写业务库；网关内做台账 CRUD、审批等业务规则；**新建基线**下用 HTTP/Feign 向 Biz 拉设备（改为 MQ + 缓存 + Request-Reply）。

**验证**：

- [x] `yudao-module-iot-newgateway` 聚合及其子模块 `*-core` / `*-bootstrap` 的 `pom.xml` 无 biz、无业务数据源 starter
- [x] 网关侧设备查询为 `GatewayMqIotDeviceCommonApi`（或等价），非 RestTemplate 拉 Biz

---

## 步骤 1：环境与 RocketMQ

**目标**：Biz 与 NewGateway 指向同一 NameServer，Topic 与消费者组就绪。

**动作**：

1. 在 Broker 创建 Topic（名称与 `iot-core` 中 `IotMessageTopics` 一致），至少包含：
   - 既有：`iot_device_service_invoke`、`iot_device_service_result`、`iot_device_state_changed`、`iot_device_event_reported`
   - 新增：`iot_device_profile_changed`、`iot_gateway_device_snapshot_request`、`iot_gateway_device_snapshot_reply`
   - 冷启动批量预热（Gateway→Biz，单向）：`iot_gateway_profile_warmup_request`
2. 配置 `yudao.iot.message-bus.type=rocketmq` 与 `name-server`（Server 与 Gateway 进程一致）。

**验证**：

- [x] Broker 上已创建上述 Topic（**192.168.1.126** `DefaultCluster`：`mqadmin updateTopic`；其中 4 个既有 Topic 已存在，3 个新增为 `iot_device_profile_changed`、`iot_gateway_device_snapshot_request`、`iot_gateway_device_snapshot_reply`）
- [x] 双方 `name-server` 一致（**ch / dev**：Biz `yudao-server` 与 NewGateway 均为 **`192.168.1.126:9876`**；`application-local.yaml` 仍为 `192.168.1.4:9876`，属本地隔离）。消费组：代码内为 `iot-newgateway-*`、`iot-biz-*`；**`application-ch.yaml` 下带前缀 `ch-`**（如 `ch-iot-newgateway-device-profile`、`ch-iot-biz-gateway-snapshot-request`），与 `yudao.iot.message-bus.consumer-group-prefix` 一致即可
- [x] 代码侧常量已存在（`IotMessageTopics`）

---

## 步骤 2：契约冻结（命令 + Profile + Request-Reply）

**目标**：Biz 与 Gateway 对消息体字段与 Topic 达成一致，优先「只增字段、不改语义」。

### 2.1 命令下行 `DEVICE_SERVICE_INVOKE`（`IotDeviceMessage`）

| 字段 | 必填 | 说明 |
|------|------|------|
| `deviceId` | 是 | 设备主键 |
| `requestId` | 是 | 幂等与回执关联 |
| `method` / `params.commandType` | 是 | 命令类型 |
| `params.deviceType` | 是 | 插件 `deviceType`（如 `NVR`） |
| `brand` / `vendorKey` / `pluginId` | 否 | 多厂家与扩展，可放消息体顶层或 `params` |
| `messageVersion` | 否 | 默认 `1` |

**兼容**：缺 `brand` 等时，网关按插件默认厂商处理（如 NVR 默认大华路径）。

### 2.2 配置下发 `DEVICE_PROFILE_CHANGED`（`DeviceProfileChangedMessage`）

- `op`：`UPSERT` / `DELETE`；必含 `deviceId`；`UPSERT` 建议含 `tenantId`、`deviceType`、`config`、`address`、`brand` 等。

### 2.3 Request-Reply

- 请求：`GatewayDeviceSnapshotRequestMessage`（`correlationId` + `deviceId`）→ Topic `GATEWAY_DEVICE_SNAPSHOT_REQUEST`
- 应答：`GatewayDeviceSnapshotReplyMessage` → Topic `GATEWAY_DEVICE_SNAPSHOT_REPLY`
- Biz 消费组：`iot-biz-gateway-snapshot-request`；网关应答消费组：`iot-newgateway-device-snapshot-reply`

### 2.4 状态与事件

沿用现有 `DEVICE_STATE_CHANGED`、`DEVICE_EVENT_REPORTED`、`DEVICE_SERVICE_RESULT`。

**验证**：

- [x] `IotDeviceMessage` 已扩展可选头字段；`DeviceProfileChangedMessage` 等与代码一致
- [x] Topic / 消费组 / DLQ 运维说明见 `docs/iot-message-topics-inventory.md`

---

## 步骤 3：Biz — 设备配置下发（`DeviceProfileChanged`）

**目标**：台账变更推送到网关缓存，网关不读业务库。

**动作**：在 IoT 设备与 IBMS 设备的创建/更新/删除后发布 MQ。

**验证**：

- [x] `DeviceProfileChangedPublisher` + `IbmsDeviceServiceImpl` / `IotDeviceServiceImpl` 挂钩

---

## 步骤 4：Biz — 快照 Request-Reply

**目标**：网关缓存未命中时，经 MQ 向 Biz 索取只读设备快照。

**验证**：

- [x] `GatewayDeviceSnapshotRequestConsumer` 已实现并注册消费组

---

## 步骤 5：Biz — 网关专用 HTTP RPC（默认关闭）

**目标**：新建环境不暴露网关拉设备 HTTP；迁移期可打开。

**动作**：`yudao.iot.gateway-rpc.enabled=false`（默认）；为 `true` 时注册 `IotDeviceGatewayRpcController`。

**验证**：

- [x] `IotDeviceCommonApiLocalImpl`（`@Primary`）供进程内使用；条件 RPC 控制器存在
- [x] `yudao-server` 默认配置为 `gateway-rpc.enabled=false`

---

## 步骤 6：Gateway — 消费 Profile + 本地缓存

**目标**：维护 `deviceId → IotDeviceRespDTO`（或等价）内存缓存。

**验证**：

- [x] `DeviceProfileChangedConsumer`、`GatewayDeviceProfileCache`

---

## 步骤 7：Gateway — MQ 设备访问与启动列表

**目标**：插件与启动初始化只从缓存读列表；单台可 RR 补快照。

**验证**：

- [x] `GatewayMqIotDeviceCommonApi`、`GatewayDeviceSnapshotClient`、`GatewayDeviceSnapshotReplyConsumer`
- [x] `GatewayStartupInitializer` 通过 `deviceApi` 读到的是缓存实现
- [x] 管理端「全量重推 profile」：`POST /iot/ibms/device/repush-gateway-profiles`（当前租户 IBMS 全量 UPSERT → MQ）
- [x] 网关侧冷启动批量预热：缓存为空且配置了 `iot.gateway.startup.tenant-id` 时发 `iot_gateway_profile_warmup_request`，Biz 等价于全量重推；可配 `profile-warmup-on-cold-start` / `profile-warmup-wait-seconds`

---

## 步骤 8：命令消息对齐（`brand` 等）

**目标**：发布命令时带齐契约字段，消费端合并进 `params`。

**验证**：

- [x] `DeviceCommandPublisher` 写 `messageVersion` 并从 `params` 透传 `brand` 等；`DeviceCommandConsumer` 合并顶层字段到 `params`
- [x] 台账补全：`DeviceCommandBrandEnricher` 在 `params` 未带 `brand` 时按 `ibms_device.brand` → `iot_device.config`（`brand` / `manufacturer` / `vendorKey`）自动写入；调用方仍可显式传入覆盖

---

## 步骤 9：插件与多厂家（示例：NVR）

**目标**：同一 `deviceType` 下按品牌选 SDK 路径；未接入品牌明确失败提示。

**验证**：

- [x] `NvrAdapterFactory` + `NvrPlugin` 中海康路径显式拒绝；大华 SDK 路径保留
- [x] 插件统一维护 `DeviceSessionRegistry`：主动连接（NVR/IPC/门禁一代/二代）在 SDK 登录与断线；被动（`AlarmPlugin`、`ChanghuiPlugin`）在 TCP 连接上线/心跳恢复上线时 put，断开时 remove；`plugins/template/TemplatePlugin` 模板同步该模式（默认禁用）

---

## 步骤 10：可观测性、会话与能力快照

**目标**：指标、就绪检查、会话注册表、能力 DTO 可扩展。

**验证**：

- [x] `DeviceCapabilitySnapshot`、`GatewayCapabilityProvider`（NVR 示例）
- [x] `DeviceSessionRegistry` + NVR 登录/登出/断线
- [x] `gateway.command.completed`（Micrometer）、`GatewayMqReadinessHealthIndicator`
- [x] `gateway.command.completed` 已带 tag：`pluginId`、`vendor`（params 优先 `brand`/`vendorKey`/`manufacturer`，否则回落插件注解 `vendor`）
- [x] DLQ：`IotRocketMQMessageBus` 消费失败日志带 `reconsumeTimes`，临近上限时提示将进入 DLQ；Micrometer 计数器 `iot.messagebus.consume.failure`（带 `near_dlq` 等 tag）；清单文档说明 `%DLQ%` 与运维告警；`gateway.sessions.registered` Gauge 与就绪探针 `registeredDeviceSessions`；冷启动重试：`gateway.startup.retry.queue.size` / `.pending` / `.exhausted` 与就绪明细 `startupRetryQueueSize`、`startupRetryQueuePending`、`startupRetryQueueExhausted`

---

## 步骤 11：薄网关 + Maven 多模块（物理拆分）

**目标**：壳模块只保留 MQ、路由、注册表、生命周期门面、profile/RR 客户端、Actuator；重 SDK 下沉 `gateway-domain-*`。

**推荐结构**（示例父工程名 `yudao-module-iot-gateway`）：

- `gateway-core-transport`：消息、路由、`DevicePluginRegistry`、状态门面、profile 缓存与 RR 客户端
- `gateway-domain-video`：`nvr` / `ipc` 与厂商 Adapter
- `gateway-domain-access` / `gateway-domain-bms` / `gateway-domain-energy`：按需
- `gateway-bootstrap`：Spring Boot `main`，按 profile 组合领域 jar

**何时拆**：单模块 JNA/native 冲突、镜像体积或发布节奏要求物理隔离时。

**验证**：

- [x] 父 POM 与子模块已创建：`core`、`domain-access`（门禁 NetSDK）、`domain-video`（NVR/IPC NetSDK）、`bootstrap`（启动类与 `application*.yaml`）；可执行包 `yudao-module-iot-newgateway-bootstrap/target/yudao-module-iot-newgateway.jar`
- [x] **瘦 core**：`yudao-module-iot-newgateway-core` 已移除 `com.netsdk` 依赖；NetSDK 仅在 `domain-access` / `domain-video` 声明

---

## 步骤 12：业务波次 P0–P5（与网关并行）

**说明**：以下为业务交付节奏，与步骤 0–11 正交；完成度在业务侧验收。

| 阶段 | 要点 |
|------|------|
| **P0** | 字典与 IBMS 产品/设备/空间稳定；Biz→Gateway profile；命令字段对齐；状态上送口径统一 |
| **P1** | 视频 SA/VI：NVR、IPC、通道、预览回放 |
| **P2** | 报警与巡更 AL/GR |
| **P3** | 通行与停车 ST/AC、CA |
| **P4** | 建筑 SB/BA、LI、EL |
| **P5** | 能源 SE/EP、EN |

**并行**：P0 优先；P1 与 P2 可分线。

**验证**：

- [x] 路线图已在团队内达成共识（可结合菜单与字典脚本验收）
- [ ] 各 P 阶段按项目范围在代码/菜单中逐项勾选（由项目经理维护）

---

## 附录 A：新设备类型接入 Playbook（竖切勾选）

接一种新设备时按序自检（避免改全局路由或第二套状态机）：

- [ ] 字典：`ibms_device_type` / `ibms_device_model` / `ibms_point_type` / `ibms_brand` 等是否需增项
- [ ] 产品模板与 `ibms_device.extra` 字段约定一致
- [ ] 台账变更后 Biz 发 `DeviceProfileChanged`
- [ ] `deviceType` 字符串与 `PluginConstants`、发布命令处一致；命令是否带 `brand`
- [ ] 网关：老连接模式 → 只加 Adapter/分支；新协议族 → 新 `@DevicePlugin` + `iot.newgateway.plugins.enabled.*`
- [ ] 登录/断线走统一生命周期门面；禁止网关访问业务库与新增 Biz HTTP
- [ ] 前端：字典、`src/api`、路由与权限点
- [ ] 回归：单设备、并发、断线重连、缓存与 RR 超时

---

## 附录 B：变更记录（人工填写）

| 日期 | 摘要 |
|------|------|
| 2026-03-24 | 步骤 8：新增 `DeviceCommandBrandEnricher`，发布命令时按台账自动补全 `brand` |
|  | 合并为单文档分步结构 |
| 2026-03-24 | 步骤 7：管理端 IBMS 全量重推网关 Profile（`IbmsDeviceController#repushGatewayProfiles`） |
| 2026-03-24 | 步骤 1：126 Broker 补齐 3 个 Topic；核对 Biz/NewGateway `name-server` 与 `ch-` 消费组前缀 |
| 2026-03-24 | 步骤 7：网关冷启动 MQ 批量预热（`iot_gateway_profile_warmup_request` + `GatewayProfileWarmupRequestConsumer`） |
| 2026-03-24 | 步骤 11：NewGateway 拆为多模块（core / domain-video / bootstrap），可执行 jar 名仍为 `yudao-module-iot-newgateway.jar` |
| 2026-03-24 | 步骤 11 演进：新增 `domain-access`，门禁迁出；core 去掉 NetSDK 与 `jna-platform` |
| 2026-03-24 | 步骤 10：`gateway.command.completed` 增加 `pluginId`、`vendor` 标签（`DevicePluginRegistry` + 命令 params） |
| 2026-03-24 | 步骤 9：`IpcPlugin`、`AccessGen1Plugin`、`AccessGen2Plugin` 对齐 NVR 的 `DeviceSessionRegistry` 注册与清理 |
| 2026-03-24 | 步骤 2/10：新增 `iot-message-topics-inventory.md`；RocketMQ 消费失败临近 DLQ 时日志提示 |
| 2026-03-24 | 步骤 10：`iot.messagebus.consume.failure` 指标；`gateway.sessions.registered` 与就绪探针 `registeredDeviceSessions` |
| 2026-03-24 | 步骤 9：`AlarmPlugin`、`ChanghuiPlugin` 接入 `DeviceSessionRegistry`（被动 TCP 连接） |
| 2026-03-24 | 步骤 9：`TemplatePlugin` 模板与 `PluginConstants#PLUGIN_ID_TEMPLATE` 对齐会话注册 |
| 2026-03-24 | 步骤 10：`gateway.startup.retry.queue.size` Gauge（`DeviceInitRetryManager` 队列条目） |
| 2026-03-24 | 步骤 10：就绪探针增加 `startupRetryQueueSize` |
| 2026-03-24 | 步骤 10：重试队列拆分为 pending / exhausted（Gauge + readiness） |

---

## 附录 C：与其它文档关系（可选速查）

以下文件内容与本文对应步骤重复或摘录，**不必并行维护两套勾选**；以本文为准。

| 文件 | 对应本文 |
|------|----------|
| `iot-biz-gateway-contract.md` | 步骤 2 |
| `iot-gateway-lightweight-redlines.md` | 步骤 0 |
| `iot-gateway-multimodule-and-thin-shell.md` | 步骤 11 |
| `ibms-roadmap-p0-p5.md` | 步骤 12 |
| `playbook-new-device-type.md` | 附录 A |
| `iot-message-topics-inventory.md` | 步骤 2 / 10：Topic、消费组、DLQ 清单 |
| `yudao-module-iot-newgateway/docs/迁移指南.md` | 网关模块迁移操作 |
| `yudao-module-iot-newgateway/docs/二次开发指南.md` | 插件开发细节 |

---

## 新建会话一句话（给 AI / 同事）

> 打开 `yudao-module-iot/docs/iot-newgateway-biz-mq-landing-plan.md`，从**步骤 0** 做到**步骤 12**，看每步 `[x]`/`[ ]`；**MQ-only 核心闭环**在步骤 1–8。
