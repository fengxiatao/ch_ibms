# NewGateway 轻量红线（摘录）

> **主文档**：[`iot-newgateway-biz-mq-landing-plan.md`](./iot-newgateway-biz-mq-landing-plan.md) **步骤 0**。

## 允许

- RocketMQ 与 Biz 交互；Redis 用于幂等、去重、限流等非业务主存。
- 插件内 SDK、连接管理、厂商 Adapter（如 NVR / IPC）。
- `DeviceLifecycleManager`（或等价门面）作为状态上送统一入口。

## 禁止

- 网关模块依赖 `yudao-module-iot-biz` 或业务数据源（JDBC/MyBatis 写业务库）。
- 网关内实现业务规则、台账 CRUD、审批流等与设备接入无关的逻辑。
- **新建基线**：网关经 HTTP/OpenFeign 调用 Biz 拉设备（已改为 MQ + 本地缓存 + Request-Reply）。

## Maven / 依赖自检

- `yudao-module-iot-newgateway` 聚合及其子模块（`*-core`、`*-domain-*`、`*-bootstrap`）的 `pom.xml` 不应出现 `yudao-module-iot-biz`、业务 `mybatis-spring-boot-starter`（业务库）等依赖。

## 配置

- 业务服务：`yudao.iot.gateway-rpc.enabled=false`（默认），仅迁移期可改为 `true` 启用 `/rpc-api/iot/device/**`。
