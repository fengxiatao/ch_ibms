# IBMS 与网关分阶段路线图（P0–P5）（摘录）

> **主文档**：[`iot-newgateway-biz-mq-landing-plan.md`](./iot-newgateway-biz-mq-landing-plan.md) **步骤 12**。  
> **单一台账验收**：去掉 `iot_*` 双轨、仅保留 `ibms_*` 的完成条件见 [`ibms-iot-model-convergence-plan.md`](./ibms-iot-model-convergence-plan.md) **「单一 ibms_* 完成定义（DoD）」**。

## P0 平台底座

- 字典：`ibms_group` / `ibms_system` / `ibms_device_type` / `ibms_device_model` / `ibms_brand` / `ibms_region` 等固化。
- IBMS 产品/设备/空间与 Biz API 稳定。
- Biz → Gateway：`DeviceProfileChanged` 推送；命令字段见主文档 **步骤 2**（或 `iot-biz-gateway-contract.md` 摘录）。
- 网关：本地缓存 + 缺参 Request-Reply；状态上送唯一 Topic/DTO 文档化。

## P1 智慧安防 — 视频（SA / VI）

- 台账：NVR、IPC；通道同步与预览/回放链路。
- 网关：`nvr` / `ipc` 插件；NVR 多厂家 Adapter（当前大华 SDK 已接，海康路径预留报错提示）。

## P2 智慧安防 — 报警与巡更（AL / GR）

- 报警主机插件联调；告警进 Biz/库；菜单与设备类型与字典对齐。
- 巡更：以业务任务与台账为主，设备侧按实际采集器映射 `deviceType`。

## P3 智慧通行 — 门禁与停车（ST / AC、CA）

- Access Gen1/2 与 IBMS 设备类型对齐。
- 停车场：道闸/车检等 `deviceType` 先定字典与产品模板再实现插件。

## P4 智慧建筑（SB / BA、LI、EL）

- 楼控协议族：Modbus / BACnet / MQTT 等，按子域拆插件 + Adapter。

## P5 智慧能源（SE / EP、EN）

- 表计采集：轮询或设备上报二选一为主；可与 P4 共用采集插件若协议相近。

并行策略：P0 优先或随竖切迭代补齐；P1 与 P2 可分线并行。
