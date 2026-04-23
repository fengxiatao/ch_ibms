# 薄网关壳 + 领域子模块（摘录）

> **唯一主文档**（分步落地 + 勾选）：[`iot-newgateway-biz-mq-landing-plan.md`](./iot-newgateway-biz-mq-landing-plan.md) **步骤 11**。  
> 本文仅保留结构速查；进度请在主文档勾选。

## 目标

- **壳模块**：MQ、路由、`DevicePluginRegistry`、`DeviceLifecycleManager`（或统一门面）、Actuator/Micrometer、Request-Reply 与 profile 缓存。
- **领域子模块**（示例）：`gateway-domain-video`（NVR/IPC）、`gateway-domain-access`、`gateway-domain-bms`、`gateway-domain-energy`，承载重 SDK / native。

## 当前仓库 Maven 形态（已落地）

路径：`yudao-module-iot-newgateway/`（**packaging pom** 聚合）

| 子模块 | artifactId | 内容 |
|--------|--------------|------|
| core | `yudao-module-iot-newgateway-core` | MQ、profile/RR、注册表、生命周期、**瘦核心**：报警/长辉/模板等；**仅 JNA**（`NativeLibraryLoader`），**无 `com.netsdk` 依赖** |
| domain-access | `yudao-module-iot-newgateway-domain-access` | `plugins.accessgen1`、`plugins.accessgen2` + **NetSDK** |
| domain-video | `yudao-module-iot-newgateway-domain-video` | `plugins.nvr`、`plugins.ipc` + **NetSDK** |
| bootstrap | `yudao-module-iot-newgateway-bootstrap` | `IotNewGatewayServerApplication`、`src/main/resources`，Spring Boot repackage；**产物名** `yudao-module-iot-newgateway.jar`（与拆分前一致） |

本地运行：IDE **Use classpath of module** 请选择 **`yudao-module-iot-newgateway-bootstrap`**。

## 推荐 Maven 形态（可选演进）

父 POM（示例名 `yudao-module-iot-gateway`）：

- `gateway-core-transport`：消息、路由、注册表、状态门面、profile 缓存客户端
- `gateway-domain-video`：`nvr` / `ipc` 与厂商 Adapter
- `gateway-domain-access` / `gateway-domain-bms` / `gateway-domain-energy`：按需
- `gateway-bootstrap`：Spring Boot `main`，按 profile 组合领域 jar

**何时继续拆**：例如独立 `domain-alarm`（纯 TCP/Vert.x）、或按厂家再拆视频子模块等。

## 与当前仓库关系

`accessgen1`/`accessgen2` 在 `yudao-module-iot-newgateway-domain-access`；`nvr`/`ipc` 在 `yudao-module-iot-newgateway-domain-video`；报警/长辉/模板等在瘦 `core`。可按目录继续增加 `domain-*`。
