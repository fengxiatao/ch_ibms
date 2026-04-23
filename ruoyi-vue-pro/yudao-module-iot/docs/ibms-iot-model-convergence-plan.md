# IBMS 双轨模型收敛（iot_* → ibms_*）

> 说明：本文档为仓库内长期版本副本，便于新会话 `@` 引用或与 Git 同步。  
> **约定**：每轮开发会话结束时，在本文件更新 **「进展与变更记录」** 一条，并同步 **「待办状态总览」** 表中的状态列（完成 / 部分 / 未开始），避免新会话从零推断进度。

## 背景与目标

- **问题**：业务早期使用芋道原生 `iot_device` / `iot_product` / `iot_device_channel`；现需以 **IBMS 编码与台账**（`ibms_device` / `ibms_product` / `ibms_channel`）为唯一主数据。
- **已定决策**（来自对话）：**一次性迁移**；**产品 IoT 独有字段**写入 `IbmsProductDO.extra`（`yudao-module-iot-biz/.../ibms/IbmsProductDO.java`）；**设备运行态**拆为 **`ibms_device`（台账）+ `ibms_device_runtime`（运行态）**。
- **网关边界**：`yudao-module-iot-core` 中 `IotDeviceRespDTO`、`DeviceProfileChangedMessage` **不改名**；NewGateway 只消费契约 DTO，不直接依赖 DO。

## 数据模型要点

- **台账**：延续 `IbmsDeviceDO` / `IbmsChannelDO`；按需补充原 `IotDeviceDO` 中**认证/展示**类列（如 `device_key`、`device_secret`、`subsystem_code`、`menu_ids` 等，以实际业务为准）。认证相关可落在 `ibms_device.extra`（如 `deviceSecret`）。
- **运行态**：新建表 `ibms_device_runtime`，主键 `device_id` 对齐 `ibms_device.id`；承载 `state`、`online_time`/`offline_time`/`active_time`、`firmware_id`、`gateway_id`、GIS/室内坐标、`config`、`job_config` 等（对齐原 `IotDeviceDO` 中运行与定位字段）。*（当前部分运行态仍写在 `ibms_device.extra.gatewayRuntimeState` 的过渡方案以代码为准。）*
- **产品 extra**：约定 JSON（如 `productKey`、`codecType`、`deviceType`/`netType`/`locationType`、`menuIds`、`primaryMenuId`、`jobConfig` 等），由 `IbmsProductExtra` + `IbmsProductExtraHelper` 统一读写。
- **通道**：`IbmsChannelDO.extra` 承接原 `IotDeviceChannelDO` 宽表中的视频/门禁等专用字段（流地址、PTZ、巡更/监控墙标记等）。

### `ibms_space` 与旧园区/楼栋/楼层/区域（GIS）映射约定

- **位置文案**：批量指派仍校验旧 GIS 表（`campus` / `building` / `floor` / `area`）层级关系，并写入 `ibms_channel.space`（展示路径）。
- **`ibms_channel.extra`（GIS 主键冗余）**：`gisCampusId`、`gisBuildingId`、`gisFloorId`、可选 `gisAreaId`，便于审计与后续同步任务。
- **`ibms_channel.space_id`**：仅在 `ibms_space.extra` 能反查时写入。请在目标空间行的 `extra` JSON 中配置与旧表主键一致的键（任一层级命中即采用**最细优先**：`gisAreaId` → `gisFloorId` → `gisBuildingId` → `gisCampusId`）。未配置映射时 `space_id` 置为 `NULL`，仍保留 `space` 文案与 `extra.gis*`。
- **权限**：IBMS 通道写操作使用 `iot:ibms-channel:*`；若角色仅有旧 `iot:channel:*`，需在菜单/权限中补齐 IBMS 通道权限点。

## 单一 ibms_* 完成定义（DoD）

> **执行计划（工单级）**：见 [`ibms-single-ledger-implementation-plan.md`](./ibms-single-ledger-implementation-plan.md)（工作包 WP-A～G、WP-F、验收与迭代切片）。  
> **删除旧代码（不并轨）**：必选工作包 **WP-G** 及文内「删除旧代码在计划中的位置」表，同文件 [`ibms-single-ledger-implementation-plan.md`](./ibms-single-ledger-implementation-plan.md)。  
> **目标**：去掉「先 IBMS 再回退 `iot_device`」的双轨分支，**台账与运行态以 `ibms_*` 为唯一数据源**；契约类名（如 `IotDeviceRespDTO`）可保留，但填充数据不得再依赖 `iot_*` 表。

### 四条硬条件（全部满足才算「真正单一」）

1. **数据库**：应用进程对 **`iot_device` / `iot_product` / `iot_device_channel`** 无读写（或表已 `RENAME`/`DROP` 且 ORM 已移除）；子系统若仍用 `device_id`，语义等于 **`ibms_device.id`**。
2. **设备主键**：不存在「同一逻辑设备两套 id」；历史迁移完成后 **`iot_device` 无未迁移有效行**（或行仅存审计/可删）。
3. **代码路径**：**无** `IotDeviceMapper` / `IotProductMapper` / `IotDeviceChannelMapper` 的**业务**调用；**`IbmsIotDualTrackDeviceResolver`** 等组件删除对 `iot_*` 的回退，或整类删除并由调用方直读 `IbmsDeviceDO` / `IbmsDeviceRuntimeDO`。
4. **网关与在线列表**：**`GET .../list-all-online`**（及等价 RPC）**仅**从 IBMS + runtime（或统一 Facade）组装 **`IotDeviceRespDTO`**，**不得**再 `selectOnlineDevicesNotInIbmsLedger()` 合并第二数据源。

### 阶段与上文「实施顺序」的对应关系

| 阶段 | 内容 | 对应「实施顺序」步骤 |
|------|------|----------------------|
| P1 数据收口 | 迁移/对账脚本；`iot_device` 可清空或仅留已废弃行 | 隐含在 2～4 之前 |
| P2 能力搬迁 | 门禁能力写回、OTA/Job/规则等最后一批 `IotDeviceMapper` 改为 IBMS | 2、4 |
| P3 去壳去回退 | `IbmsIotDualTrackDeviceResolver`、`IotLegacyIotDeviceSideEffects` 收口或删除 IoT 分支 | 4 |
| P4 接口与前端 | `IotDeviceController` 仅保留网关必需 URL（或迁至专用 Controller）；前端仅 `iot:ibms-*` | 5、6 |
| P5 库表清理 | 备份 + `iot_legacy_tables_rename_to_bak_optional.sql`（人工 uncomment）或 DROP | 6 |

### 环境验收建议（目标库 + 代码仓）

- **库**：`SELECT COUNT(*) FROM iot_device WHERE deleted=0`（及租户条件）为 **0**，或已确认可下线；`information_schema` 复核无指向待删表的 FK。
- **仓**（示例检索，命中业务代码需清零）：`IotDeviceMapper` / `selectOnlineDevicesNotInIbmsLedger` / `iotDeviceMapper`（除测试或生成物外）。
- **运行**：主服务 + NewGateway 联调一轮（上线/下发/视频/门禁/OTA 各抽一条冒烟路径）。

## 实施顺序（建议）

1. **DDL + DO/Mapper/基础 Service**：`ibms_device_runtime`；扩展 `ibms_device`；`IbmsDeviceRuntimeMapper` / Service；`IbmsProductExtraHelper`。
2. **扩展 IBMS Service 能力**：将 `IotDeviceService`、`IotProductService`、`IotDeviceChannelService` 中**网关/MQ/子系统依赖**的方法迁到 `IbmsDeviceService` / `IbmsProductService` / `IbmsChannelService`（或拆 Facade）。
3. **桥接与 MQ**：`IotDeviceCommonApiLocalImpl` 仅走 IBMS（+ runtime）；`DeviceProfileMessageBuilder` 去掉 `fromIotDevice`/`deleteIot`；`DeviceCommandBrandEnricher`、`DeviceStateChangeConsumer` 等改为以 IBMS 为主。
4. **按域替换引用**：视频 → 门禁 → 告警/长辉/OPC/规则 → 设备核心/Job/大屏 → 产品/物模型/OTA。
5. **前端**：`yudao-ui-admin-vue3` 将 `/iot/device/`、`/iot/product/`、`/iot/channel/` 等逐步统一切到 `/iot/ibms/*`（与权限 `iot:ibms-*` 对齐）。
6. **清理**：删除旧 DO/Mapper/Service/Controller；旧表改名 `_bak` 或 DROP（按环境策略）；`mvn clean install -DskipTests` 与主服务 + NewGateway 联调。

## 待办状态总览（随会话更新）

| 状态 | 项 | 说明 |
|:--:|----|------|
| 完成 | ddl-runtime | `ibms_device_runtime` DDL 已有；本轮落地 `IbmsDeviceRuntimeDO`/Mapper/`IbmsDeviceRuntimeService`；`ibms_device` 建表脚本与 DO 对齐扩展列；创建/删设备维护运行态行；`DeviceStateChangeConsumer` 与 extra 双写 `state`/上下线时间 |
| 完成 | product-extra | `IbmsProductExtra`、`IbmsProductExtraHelper`；Save/Resp VO 的 `extra`；更新时保留原 `extra` |
| 部分 | extend-ibms-services | 已加 `IbmsDeviceGatewaySupportService#updateGatewayDeviceStateWithTimestamp`（extra + runtime 双写）、`DeviceServiceResultConsumer` 在线检测落库改走 IBMS；`DeviceStateChangeConsumer` 复用同一入口；**`IbmsDeviceLedgerRuntimeHelper#buildLegacyAccessDeviceShell`** 供门禁能力刷新在无 `iot_device` 时使用；**`IbmsDeviceService#isDeviceExistsByIp`（`@TenantIgnore`）** + **`DiscoveryEventListener`** 双轨判存（IoT 侧改 **`IotLegacyIotDeviceSideEffects#isDeviceExistsByIp`**）；**`IbmsDeviceRuntimeService#updateJobConfig`**、**`IbmsDeviceMapper#selectCountByGroupId` / `#selectCountByIbmsProductId`**；**`IotAlarmHostServiceImpl#createAlarmHost`** 已改为 **`IbmsDeviceService#createDevice`**；**`IbmsProductService#getProductByLegacyIotProductKey`**、**`IbmsDeviceServiceImpl` 创建时写入 `ibms_product_id`**；`IotDeviceGroupServiceImpl` / **`IotProductServiceImpl#deleteProduct`** IoT 侧计数 **`IotDeviceMapper`**；**`IbmsIotDualTrackDeviceResolver`** / **`IotDeviceMessageServiceImpl`** / **`IotOtaTaskServiceImpl`** / **`IotOtaTaskRecordServiceImpl`** 已不再注入 **`IotDeviceService`**（Mapper 或 **`IotLegacyIotDeviceSideEffects`**）；**`IbmsDeviceService`** 增补 **`updateDeviceGroup` / `countDevicesByProductDualTrack` / `listSimpleDevices` / `deleteDeviceList`**；**`IotDeviceServiceImpl#getDeviceCountByProductId`** 改为双轨合计；**`updateDeviceGroup`** 在更新 **`iot_device`** 后同步 **`ibms_device.group_ids`**；**仍存** **`IotDeviceServiceImpl`** 本体（创建/导入/缓存等）；**`IotDeviceController`** CRUD 已 **Swagger deprecated** + **`/simple-list` 合并 IoT+IBMS** |
| 完成 | bridge-mq | `IotDeviceCommonApiLocalImpl` → IBMS；`DeviceProfileMessageBuilder` 去掉 IoT 分支；`DeviceCommandBrandEnricher`；`DeviceStateChangeConsumer` 去 `IotDeviceService`/iot 通道同步、并双写 `ibms_device_runtime`；`IotDeviceServiceImpl` 不再发 Profile MQ |
| 完成 | migrate-video-access | NVR 列表/心跳/录像/抓拍/通道同步与 **`NvrController`**、**`DhVideoController`**、**`ZlmStreamServiceImpl`** 已 IBMS 化；**`NvrQueryService#getChannelsByNvrId` / `refreshChannelsByNvrId`** 已改为返回 **`NvrScannedChannelRow`**（`IotDeviceChannelServiceImpl` 仅在调用 `syncNvrChannel` 前按需转为兼容壳）；NVR 台账壳仍用 **`IbmsDeviceLedgerRuntimeHelper#buildLegacyNvrDeviceShell`** |
| 部分 | migrate-rest-biz | 已改 `OpcControlServiceImpl`、`VideoPlaybackServiceImpl`、`OpcZoneConfigServiceImpl`/`OpcZoneConfigController`、`IotAccessEventLogServiceImpl`；`IbmsDeviceDahuaSdkHelper`；`IotAlertRecordServiceImpl`；**`AccessDeviceCapabilityRefreshJob`**；**`IotAccessDeviceCapabilityServiceImpl#refreshCapability`**（写回 `iot_device.config` 改 **`IotDeviceMapper` + `IotDeviceSpringCacheEvictor`**）；**`UniversalCameraCollector`**；**OTA**（**`IotOtaUpgradeJob`** / **`IotOtaTaskRecordController`** / **`IotOtaTaskServiceImpl`** / **`IotOtaTaskRecordServiceImpl`** 不再依赖 **`IotDeviceService`**，创任务校验走 **`IotDeviceMapper`** + **`IotLegacyIotDeviceSideEffects`**）；**`IbmsDeviceRuntimeService#updateFirmwareId`**；**`IotStatisticsController`**；**`OfflineCheckExecutor`**；**场景规则** / **`IotDeviceMessageServiceImpl`**（遗留状态更新 **`IotLegacyIotDeviceSideEffects`**） / **`IotDataRuleServiceImpl`** / **`UnifiedJobScheduler`** / **`IotDeviceDataCollectJob`** / **`AccessEventHandlerImpl`**；**`IotDeviceGroupService`** / **`DeviceJobConfigController`** / **`IotDevicePropertyController`**；**`IotDeviceChannelServiceImpl`** + **`IbmsIotDualTrackDeviceResolver`**（回退读改 **`IotDeviceMapper`**） + **`IbmsDeviceLedgerRuntimeHelper`**；**`IotDeviceServiceInvokeServiceImpl`** 支持仅 **IBMS** 记日志；**`IotDeviceController#/list-all-online`** 双轨合并；**前端** **`api/iot/device/device/index.ts`**：分页/详情/统计/精简列表/删/批量删/改分组 → **`/iot/ibms/device/*`**（**`create`/`update` 仍 `/iot/device`**，新台账编辑请走 **`api/iot/ibms/device`**）；**仍存** 其它仅 **`iot:device:*`** 的子路径（配置/物模型/消息/导入等） |
| 完成 | ibms-channel-menu-perm | **`sql/mysql/ibms_channel_permissions_grant_video_roles.sql`**：`iot:ibms-channel:*` 挂 `ibms-channel`；超级管理员 + 视频预览/回放角色；**`sql/mysql/ibms_channel_permissions_grant_tenant_and_video_admin_roles.sql`**：`tenant_admin` / 名称为「租户管理员」/ 名称含「视频」且含「管理」或「管理员」的角色；开发库 **ch_ibms** 已执行（含本会话 MCP 授权） |
| 完成 | frontend-api | `api/iot/device/index.ts`、`device/device/index.ts` **导出** → **`/iot/ibms/device/export-excel`**；**`api/iot/ibms/device.ts`** 增加 **`exportDeviceExcel`**；**`views/ibms/device/index.vue`** 导出改为服务端 Excel；读写路径均已 IBMS 化；`api/iot/channel` 与视频能力同前 |
| 部分 | cleanup-verify | `mvn clean install -DskipTests` 全量通过（本会话已再跑）；**未**删旧 `iot_*` 表与整套旧 CRUD；已新增可选脚本 **`sql/mysql/iot_legacy_tables_rename_to_bak_optional.sql`**（**注释模板**，须人工评估后执行，**默认不跑**）；新增可选 **`sql/mysql/iot_device_legacy_menu_hide_optional.sql`**（隐藏旧「IoT 设备管理」菜单，**默认不跑**）；已对 **ch_ibms** 经 MCP 补齐 **`ibms_device.ibms_product_id`、`ibms_device.group_ids`**；**本轮** 经 MCP 幂等插入 **`ibms_product` 报警主机种子**（见 **`sql/mysql/ibms_product_alarm_host_seed.sql`**）；**本轮** 经 **mysql-ibms（ch_ibms）** 只读确认库内存在 **`ibms_device`、`ibms_device_runtime`、`iot_device`** 三表；**本轮 MCP 评估（ch_ibms）**：`iot_device` 未删除行约 **10**、`ibms_device` 约 **11**、`ibms_device_runtime` 约 **0**；`information_schema` 未检出指向 **`iot_device`/`iot_product`/`iot_device_channel`** 的外键行（执行 **`RENAME`** 前仍须按环境复核应用层引用与 ORM） |
| 未开始 | single-ibms-ledger-only | 达到 **「单一 ibms_* 完成定义（DoD）」** 四条硬条件；与 **`extend-ibms-services` / `migrate-rest-biz` / `cleanup-verify`** 收尾同一里程碑，完成后可删 **`IbmsIotDualTrackDeviceResolver`** 的 `iot` 回退并执行表 **`RENAME`/DROP** |

**图例**：完成 · 部分 · 未开始

---

## 进展与变更记录

### 2026-03-25 — 会话：新增「单一 ibms_*」实施计划文档

**文档**

- 新增 [`ibms-single-ledger-implementation-plan.md`](./ibms-single-ledger-implementation-plan.md)：工作包 **WP-A～F**（数据 / 后端域 / 网关在线列表 / API 与前端 / 去壳删类 / 库表清理）、通用验收、风险回滚、建议迭代切片。
- **DoD** 章节顶部增加指向该实施计划的链接；[`docs/README.md`](./README.md) 增加 **IBMS 台账收敛** 导航。

### 2026-03-25 — 会话：补充「单一 ibms_*」完成定义（DoD）

**文档**

- 新增章节 **「单一 ibms_* 完成定义（DoD）」**：四条硬条件、P1～P5 与「实施顺序」映射、库/代码/联调验收建议。
- **待办状态总览** 增加 **`single-ibms-ledger-only`**（未开始），作为去掉双轨与表清理的收口里程碑。

### 2026-03-25 — 会话：双轨收敛首轮（网关 / MQ / 产品 extra / 前端读路径）

**后端（yudao-module-iot-biz）**

- 新增：`IbmsProductExtra.java`、`IbmsProductExtraHelper.java`；`IbmsDeviceGatewaySupportService` + `IbmsDeviceGatewaySupportServiceImpl`。
- `IbmsDeviceMapper`：`selectByProductKeyAndName`、`selectByGatewayDeviceKey`、`selectListByGatewayRuntimeState`、`selectListAccessLikeDevices`。
- `IotDeviceCommonApiLocalImpl` 仅委托 `IbmsDeviceGatewaySupportService`（认证密钥读 `extra.deviceSecret`）。
- `DeviceProfileMessageBuilder` 移除 `fromIotDevice` / `deleteIot`；`IotDeviceServiceImpl` 移除设备 Profile MQ 发布。
- `DeviceCommandBrandEnricher`：仅 `ibms_device` + `extra` 补 brand。
- `DeviceStateChangeConsumer`：状态落库、发现清理、NVR 通道同步、门禁 `QUERY_CHANNELS` 设备类型均走 IBMS；构造器不再注入 `IotDeviceService` / `IotDeviceChannelService`。
- `AccessDeviceTypeConstants.getAccessDeviceType(IbmsDeviceDO)`（读 `extra`）。
- `OpcControlServiceImpl`：`IbmsDeviceMapper` + `sn` 替代 `IotDeviceService` + `serialNumber`。

**前端（yudao-ui-admin-vue3）**

- `src/api/iot/device/index.ts`：`getDevice` / `getDeviceList` → `/iot/ibms/device/*`，兼容 `deviceName` / `ipAddress` / `deviceKey`；写操作仍 `/iot/device`。
- `src/api/iot/channel/index.ts`：分页、详情、按设备列表、同步 → `/iot/ibms/channel/*`，`mapIbmsChannelRow` 映射旧 VO。

**构建**

- `ruoyi-vue-pro`：`mvn clean install -DskipTests` 已通过（记录时点：该轮会话内）。

**明确未做 / 后续**

- 未批量删除 `iot_device` 等旧表与旧 Controller/Service；Biz 内仍大量 `IotDeviceDO` 引用待按域替换。
- NewGateway + 主服务 **联调验证** 需在环境中单独记录结论（本记录仅反映代码与构建）。

### 2026-03-25 — 会话：`ibms_device_runtime` 与台账扩展列对齐

**后端（yudao-module-iot-biz）**

- 新增：`IbmsDeviceRuntimeDO`、`IbmsDeviceRuntimeMapper`、`IbmsDeviceRuntimeService` / `IbmsDeviceRuntimeServiceImpl`（`getByDeviceId`、`ensureRowForDevice`、`deleteByDeviceId`、`patchGatewayState`）。
- `IbmsDeviceDO` 增加与 `ibms_device_extend_convergence.sql` / 新版 `ibms_device.sql` 一致的台账扩展字段（`nickname`、`device_key`、`subsystem_code`、`group_ids` 等）。
- `IbmsDeviceServiceImpl`：创建设备后插入运行态行、删除设备前逻辑删除运行态行。
- `DeviceStateChangeConsumer#updateDatabaseStatus`：在写入 `ibms_device.extra.gatewayRuntimeState` 同时更新 `ibms_device_runtime.state` 与 `online_time`/`offline_time`（过渡双写）。

**SQL**

- `sql/mysql/ibms_device.sql`：建表语句合并原扩展列，便于全新库一次初始化；已存在库仍可用 `ibms_device_extend_convergence.sql` 增量变更。

**构建**

- `mvn clean install -DskipTests` 全量已通过（本会话）；顺带修正 `OpcControlServiceImpl` 中 IBMS 设备名应使用 `getName()`（原 `getDeviceName()` 无法编译）。

**明确未做 / 后续**

- 环境需执行 `ibms_device_runtime.sql`（若尚未建表）；`IbmsDeviceMapper#selectListByGatewayRuntimeState` 等仍以 `extra` 为准，后续可改为读运行态表并下掉 extra 中的网关状态。
- `migrate-rest-biz` / `extend-ibms-services` 等项仍按总览表推进。

### 2026-03-25 — 会话：网关状态写入收口 + 大华视频参数 IBMS 化

**后端（yudao-module-iot-biz）**

- `IbmsDeviceGatewaySupportService` / `Impl`：新增 `updateGatewayDeviceStateWithTimestamp`，合并写入 `ibms_device.extra`（`gatewayRuntimeState` / `gatewayRuntimeAt`）与 `IbmsDeviceRuntimeService.patchGatewayState`。
- `DeviceStateChangeConsumer#updateDatabaseStatus`：改为调用上述方法，去掉重复的 extra 补丁逻辑。
- `DeviceServiceResultConsumer#handleOnlineCheckResult`：`CHECK_DEVICE_ONLINE` 落库从 `IotDeviceService.updateDeviceStateWithTimestamp` 改为 IBMS 双写（不再更新 `iot_device`）。
- `IbmsDeviceRuntimeServiceImpl#patchGatewayState`：设备**首次**上线时若 `active_time` 为空则补齐（对齐原 IoT 台账语义）。
- `DhVideoController`：`/iot/video/dh/*` 播放参数与回放前置校验改为 `IbmsDeviceMapper` + `IbmsDeviceRuntimeService`（台账 `ip`、运行态 `config`、台账 `extra` 接入字段）。

**数据库（mysql-ibms / ch_ibms）**

- 已确认库内存在 `ibms_device`；**当前环境未见 `ibms_device_runtime` 表**，若需 runtime 双写落库，请在目标库执行仓库内 `sql/mysql/ibms_device_runtime.sql`（或等价增量脚本）。

**构建**

- `mvn -pl yudao-module-iot/yudao-module-iot-biz -am compile -DskipTests` 已通过（本会话）。

**明确未做 / 后续**

- `ZlmStreamServiceImpl` 已迁 IBMS；NVR/录像/抓拍/心跳等后续在「2026-03-25 — NVR/录像/抓拍/心跳 IBMS 化」一节已推进（见总览表）。
- `migrate-rest-biz`、`frontend-api`、`cleanup-verify` 仍按总览表继续。

### 2026-03-25 — 会话：`ZlmStreamServiceImpl` IBMS 化 + 视频网络参数抽取

**后端（yudao-module-iot-biz）**

- 新增 `IbmsDeviceVideoNetworkResolver`：从 `ibms_device`（`ip`/`extra`）与 `ibms_device_runtime.config` 解析 IP、HTTP/RTSP 端口、用户名密码，供大华参数与 ZLM 拉流共用。
- `DhVideoController`：网络解析改为委托 `IbmsDeviceVideoNetworkResolver`（行为与原先私有方法一致）。
- `ZlmStreamServiceImpl`：去掉 `IotDeviceService` 依赖；设备与配置改为 `IbmsDeviceMapper.selectById` + `IbmsDeviceRuntimeService.getByDeviceId`；`buildRtspUrl` / `buildPlaybackRtspUrl` 使用台账 `name`/`productKey` 与运行态 `config`；修正 `parseChannelIdFromStreamKey` 对 `channel_{id}_sub` 的解析。

**数据库**

- 本轮无 DDL/DML（仍依赖既有 `ibms_device` / `ibms_device_runtime` 数据；未建 runtime 表的环境需先执行仓库内 `sql/mysql/ibms_device_runtime.sql`）。

**构建**

- `mvn -pl yudao-module-iot/yudao-module-iot-biz -am compile -DskipTests` 已通过（本会话）。

**明确未做 / 后续**

- 见总览表 `migrate-video-access`：通道刷新返回值仍为 `IotDeviceDO` 兼容层；其它域继续替换 `IotDeviceService`。

### 2026-03-25 — 会话：NVR/录像/抓拍/心跳 IBMS 化（migrate-video-access 续）

**后端（yudao-module-iot-biz）**

- 新增 `IbmsDeviceLedgerRuntimeHelper`：`resolveDeviceState`、`buildLegacyNvrDeviceShell`（通道同步仍吃 `IotDeviceDO` 时从 IBMS+运行态拼壳）。
- `IbmsDeviceMapper`：`selectListByIbmsProductId`、`selectNvrLikeDevices`（与历史 `product_id=4` / `deviceTypeCode=NVR` / `extra.deviceType` 对齐；未建 `ibms_device_runtime` 的库**不**在 SQL 里引用该表，避免查询失败）。
- `NvrQueryService#getNvrList` 改为 `List<IbmsDeviceDO>`；`NvrQueryServiceImpl` 使用 `IbmsDeviceMapper` + `IbmsDeviceRuntimeService` + `IbmsDeviceVideoNetworkResolver` 发扫描通道命令。
- `NvrController`、`NvrHeartbeatService`（`IbmsDeviceGatewaySupportService#updateGatewayDeviceStateWithTimestamp`）、`CameraRecordingServiceImpl`、`CameraSnapshotServiceImpl`（列表 VO 补全）、`IotDeviceChannelServiceImpl`（仅 IBMS 台账的 NVR 同步、`syncNvrChannels` 校验、`batchSyncAllNvrChannels` 在线判断）已切换。

**数据库（mysql-ibms / ch_ibms）**

- 本会话经 MCP 确认：当前库存在 `ibms_device`，**未见** `ibms_device_runtime`。运行态解析、双写等能力在已执行仓库内 `sql/mysql/ibms_device_runtime.sql` 的环境下方可落库；未建表时相关 Service 读运行态多为 `null`，依赖台账 `ip`/`extra` 仍可工作。

**构建**

- `mvn -pl yudao-module-iot/yudao-module-iot-biz -am compile -DskipTests` 已通过（本会话）。

**明确未做 / 后续**

- （已跟进）`AccessDeviceCapabilityRefreshJob` 已改为 IBMS 在线遍历；见下文「2026-03-25 — 会话：视频通道查询去 `IotDeviceDO`…」。
- 若需按运行态 `config.deviceType=NVR` 识别设备，可在已建 `ibms_device_runtime` 的环境于 Mapper 中恢复 EXISTS 类条件（见 `IbmsDeviceMapper` 注释）。

### 2026-03-25 — 会话：migrate-rest-biz 切片（安防回放 / OPC 防区 / 门禁事件 / 告警记录）

**后端（yudao-module-iot-biz）**

- 新增 `IbmsDeviceDahuaSdkHelper#resolveDahuaSdkPort`；`CameraRecordingServiceImpl` 改为调用该工具类。
- `VideoPlaybackServiceImpl`：`IotDeviceService` + `DeviceConfigHelper` → `IbmsDeviceMapper` + `IbmsDeviceRuntimeService` + `IbmsDeviceVideoNetworkResolver` + `IbmsDeviceDahuaSdkHelper`（单路/批量录像搜索参数）。
- `OpcZoneConfigServiceImpl` / `OpcZoneConfigController`：设备存在性与展示名改读 `ibms_device`。
- `IotAccessEventLogServiceImpl#resolveAccessDeviceType`：改 `AccessDeviceTypeConstants.getAccessDeviceType(IbmsDeviceDO)`（台账 `extra`）。
- `IotAlertRecordServiceImpl`：移除未注入使用的 `IotDeviceService`（原仅注释代码引用）。

**数据库**

- 本轮无 DDL；逻辑仍依赖 `ibms_device`（及可选 `ibms_device_runtime` 丰富端口/账号解析）。

**构建**

- `mvn -pl yudao-module-iot/yudao-module-iot-biz -am compile -DskipTests` 已通过（本会话）。

**明确未做 / 后续**

- `IotSceneRuleServiceImpl` 等仍依赖 `IotDeviceService`，可按域继续替换（采集器/OTA 见「采集器 + OTA IBMS 化」一节）。

### 2026-03-25 — 会话：视频通道查询去 `IotDeviceDO` + 能力刷新 Job IBMS 化 + 库建 `ibms_device_runtime`

**后端（yudao-module-iot-biz）**

- 新增 `NvrScannedChannelRow`；`NvrQueryService` / `NvrQueryServiceImpl` 通道列表与刷新返回该类型；`IotDeviceChannelServiceImpl#syncDeviceChannels` 在调用 `syncNvrChannel` 前用 `toLegacyChannelDeviceForSync` 转换。
- `IbmsDeviceLedgerRuntimeHelper#buildLegacyAccessDeviceShell`：从 `ibms_device` + `ibms_device_runtime` 拼门禁用 `GenericDeviceConfig` 壳。
- `IotAccessDeviceCapabilityServiceImpl#refreshCapability`：`iot_device` 无记录时回退 IBMS 壳；`updateDeviceConfigCapabilitySnapshot` 在无 `iot_device` 时合并写入 `ibms_device.extra.accessCapabilities`；`getDevicePort` 支持 `GenericDeviceConfig` 的 `port` / `tcpPort`。
- `AccessDeviceCapabilityRefreshJob`：改为 `IbmsDeviceMapper#selectListByGatewayRuntimeState(ONLINE)`；NVR 分支写 `extra.nvrCapabilities`；门禁分支调用 `refreshCapability(ibmsId)`。

**数据库（mysql-ibms / ch_ibms，MCP `execute_sql`）**

- 已执行 `CREATE TABLE IF NOT EXISTS ibms_device_runtime`（与仓库 `sql/mysql/ibms_device_runtime.sql` 一致）；此前环境仅有 `ibms_device` / `iot_device`、无运行态表。

**构建**

- `mvn -pl yudao-module-iot/yudao-module-iot-biz -am compile -DskipTests` 已通过（本会话）。

**明确未做 / 后续**

- 仍仅存 `iot_device`、尚未同步 IBMS 台账的门禁设备 **不再** 被本 Job 扫描（以 IBMS 在线为准）；双轨期间若有缺口需单独迁移或临时补偿任务。
- `migrate-rest-biz` 其余类（规则/统计/离线检测/设备 CRUD 等）仍待替换 `IotDeviceService`（采集器与 OTA 推送/固件切片见下文「采集器 + OTA IBMS 化」）。

### 2026-03-25 — 会话：采集器 + OTA IBMS 化（migrate-rest-biz 续）

**后端（yudao-module-iot-biz）**

- `IbmsDeviceLedgerRuntimeHelper`：新增 **`buildLegacyCameraCollectorShell`**（台账 IP/账号 + `brand`/`extra.vendor` → 协议 `vendor`）、**`buildLegacyOtaDeviceShell`**（id/租户/在线态/产品/运行态固件）。
- `IbmsDeviceRuntimeService` / `Impl`：新增 **`updateFirmwareId`**（有则更新，无运行态行则按台账补插入）。
- `UniversalCameraCollector`：`IotDeviceService#getDeviceListByProductId` → **`IbmsDeviceMapper#selectListByIbmsProductId`** + 上述 camera 壳（产品 ID 列表仍为 62–65，含义对齐 **`ibms_product.id`**）。
- `IotOtaUpgradeJob`：设备解析 **优先 IBMS 壳**，否则回退 **`getDeviceFromCache`**（双轨兼容）。
- `IotOtaTaskRecordServiceImpl#updateOtaRecordProgress`：升级成功时若存在 **`ibms_device`** 则 **`updateFirmwareId`**，否则仍更新 **`iot_device`**。
- `IotOtaTaskRecordController` 分页：设备展示名在 IoT Map 缺省时 **回退 `ibms_device.name`**。

**数据库（mysql-ibms / ch_ibms，MCP）**

- 已确认存在表 **`ibms_device_runtime`**（与本仓库 DDL 一致即可）。

**构建**

- `mvn -pl yudao-module-iot/yudao-module-iot-biz -am compile -DskipTests` 已通过（本会话）。

**明确未做 / 后续**

- （已跟进）见下文「2026-03-25 — 会话：OTA 创建任务 IBMS 化 + IBMS 通道视频/巡更/设备树 API」。

### 2026-03-25 — 会话：OTA 创建任务 IBMS 化 + IBMS 通道视频/巡更/设备树 API

**后端（yudao-module-iot-biz）**

- **`IotOtaTaskServiceImpl#validateOtaTaskDeviceScope`**：勾选设备时若 **去重后的 id 均在 `ibms_device`**，则按 **`ibms_product_id` 与固件 `productId`（= `ibms_product.id`）** 校验，并用 **`IbmsDeviceLedgerRuntimeHelper#buildLegacyOtaDeviceShell`** 生成与 `createOtaTaskRecordList` 兼容的壳；**否则回退** `IotDeviceService.validateDeviceListExists`。全部设备时若 **`selectListByIbmsProductId` 非空则走 IBMS**，否则回退 **`getDeviceListByProductId`**。
- **`IbmsChannelMapper#selectListVideoOrientedChannels`**；**`IbmsChannelService` / `Impl`**：`listVideoChannels`、`listPatrolChannels`、`listMonitorChannels`（**`extra`：`enableStatus` / `isPatrol` / `isMonitor` / `sort` / `monitorPosition`**，与计划文档「宽字段进 extra」一致）；**`batchEnable/DisableChannels`、`batchSetPatrol`、`batchSetMonitor`**；**`batchSyncAllNvrChannels`** 委托 **`IotDeviceChannelService`**；**`getDeviceTree`**（默认 **`ibms_device.system_code=VI`** + 子通道）。
- **`IbmsChannelController`**：新增 **`/video/list`、`/video/patrol`、`/video/monitor`、`/device-tree`、`/sync-all-nvr`、`/batch/*`**，权限沿用 **`iot:ibms-channel:*`**。
- 新增 **`IbmsDeviceTreeNodeRespVO`**。

**前端（yudao-ui-admin-vue3）**

- **`src/api/iot/channel/index.ts`**：`getVideoChannels`、`getPatrolChannels`、`getMonitorChannels`、`syncAllNvrChannels`、批量与 **`getDeviceTree`** 改为 **`/iot/ibms/channel/*`**（**`batchAssignSpatial` / 通道写** 的 IBMS 化见下文「通道空间指派…」一节）。

**数据库（mysql-ibms / ch_ibms，MCP `execute_sql`）**

- 只读确认：`ibms_channel` 有数据即可承载视频/巡更筛选；**巡更/监控墙/启用** 依赖行上 **`extra` JSON**，无则视为默认启用、未勾选巡更/监控墙。

**构建**

- `mvn -pl yudao-module-iot/yudao-module-iot-biz -am compile -DskipTests` 已通过（本会话）。

**明确未做 / 后续**

- 见下文「通道空间指派 IBMS 化 + 统计/离线检查切片」。

### 2026-03-25 — 会话：通道空间指派 IBMS 化 + 统计/离线检查切片

**后端（yudao-module-iot-biz）**

- 新增 **`IotGisSpatialLocationBuilder`**：园区/楼栋/楼层/区域校验与位置文案（原 `IotDeviceChannelServiceImpl#querySpatialInfo` 逻辑收口）；**`IotDeviceChannelServiceImpl#batchAssignSpatial`** 改为委托该组件（`iot_device_channel` 行为不变）。
- **`IbmsSpaceMapper#selectByExtraGisId`**：按 **`extra`** 中 **`gisAreaId` / `gisFloorId` / `gisBuildingId` / `gisCampusId`** 反查 `ibms_space`（最细优先）。
- **`IbmsChannelService` / `Impl#batchAssignSpatial`**：更新 **`ibms_channel.space`、`space_id`、`extra.gis*`**；**`IbmsChannelController`** `POST /iot/ibms/channel/batch/assign-spatial`（权限 **`iot:ibms-channel:update`**）。
- **`IbmsDeviceMapper`**：`selectCountByCreateTime`、`selectDeviceCountMapByState`（**`ibms_device` LEFT JOIN `ibms_device_runtime`**，无运行态行计为 **state=0 未激活**）。
- **`IotStatisticsController`**：设备总数/今日新增/按状态统计改走 **`IbmsDeviceMapper`**。
- **`OfflineCheckExecutor`**：`PRODUCT` / `DEVICE` 任务改 **`IbmsDeviceMapper` + `IbmsDeviceRuntimeService`** 判定离线。

**前端（yudao-ui-admin-vue3）**

- **`src/api/iot/channel/index.ts`**：`createChannel` / `updateChannel` / `deleteChannel` / **`batchAssignSpatial`** → **`/iot/ibms/channel/*`**；`batchAssignSpatial` 类型补充可选 **`areaId`**；新增 **`channelVoToIbmsSavePayload`** 映射。

**数据库（mysql-ibms / ch_ibms，MCP）**

- 已确认存在 **`ibms_space`**；样例行 **`extra` 多为空**，需按上文约定写入 **`gis*`** 后 **`space_id`** 才可自动关联。

**构建**

- `mvn -pl yudao-module-iot/yudao-module-iot-biz -am compile -DskipTests` 已通过（本会话）。

**明确未做 / 后续**

- （已跟进）数据规则 / 采集 Job / 统一调度 / 门禁事件 / 设备消息状态等见本节之后记录；**`cleanup-verify`** 未删旧表。

### 2026-03-25 — 会话：场景规则与设备消息 IBMS 双轨 + IBMS 通道权限 SQL

**后端（yudao-module-iot-biz）**

- 新增 **`IbmsIotDualTrackDeviceResolver`**：`getDeviceShellPreferIbmsThenIot`、`listDeviceShellsByProductIdPreferIbmsThenIot`、`getProductShellPreferIotThenIbms`（复用 **`IbmsDeviceLedgerRuntimeHelper#buildLegacyOtaDeviceShell`**，产品以 **`ibms_device.ibms_product_id`** 对齐规则触发器中的 **`productId`**）。
- **`IotSceneRuleServiceImpl`**、**`IotDeviceControlSceneRuleAction`**、**`IotDeviceServiceInvokeSceneRuleAction`**：去掉直接 **`getDeviceFromCache` / `getDeviceListByProductId`**，改为上述 Resolver。
- **`IotDeviceMessageServiceImpl#sendDeviceMessage(IotDeviceMessage)`**：设备存在性改为 Resolver，便于仅台账设备下发下行消息（仍依赖 Redis **`serverId`** 等既有网关链路）。

**数据库（mysql-ibms / ch_ibms，MCP + 仓库脚本）**

- 新增 **`sql/mysql/ibms_channel_permissions_grant_video_roles.sql`**；已在 **ch_ibms** 插入 **`iot:ibms-channel:query/create/update/delete/export`** 按钮菜单，并为 **role_id=1** 绑定全部上述菜单（**tenant_id=0**）。

**构建**

- `mvn -pl yudao-module-iot/yudao-module-iot-biz -am compile -DskipTests` 已通过（本会话）。

**明确未做 / 后续**

- （已跟进）见下文「2026-03-25 — 会话：数据规则 / 统一调度 / 采集 Job…」；其余 **`IotDeviceService`** 引用见 **`cleanup-verify`** 与 grep。

### 2026-03-25 — 会话：数据规则 / 统一调度 / 采集 Job / 门禁事件 / 设备消息状态 / 前端设备写 / 通道权限扩展

**后端（yudao-module-iot-biz）**

- **`IbmsIotDualTrackDeviceResolver`**：`listDeviceShellsWithJobConfigPreferIbmsMergedWithIot`（`ibms_device_runtime.job_config` + 未覆盖的 `iot_device.job_config`）。
- **`UnifiedJobScheduler`**：产品下设备列表与带 **jobConfig** 设备列表改走 Resolver（不再注入 **`IotDeviceService`**）。
- **`IotDataRuleServiceImpl`**：数据源设备 ID 校验 **先在 `ibms_device` 消解，剩余再走 `validateDeviceListExists`**。
- **`IotDeviceDataCollectJob`**：按产品采集在线设备走 Resolver；全量在线 = **`IbmsDeviceRuntimeMapper#selectOnlineDeviceIds`** ∪ **`IotDeviceMapper#selectOnlineDeviceIdsNotInIbmsLedger`**（仍无 **`collectDeviceData`** 实现，执行结果仍为失败计数，但目标设备集合已双轨）。
- **`AccessEventHandlerImpl`**：**`getDeviceFromCache` / `getDevice`** 改为 **`getDeviceShellPreferIbmsThenIot`**。
- **`IotDeviceMessageServiceImpl`**：上行 **`STATE_UPDATE`** 若 **`ibms_device`** 存在则 **`IbmsDeviceGatewaySupportService#updateGatewayDeviceStateWithTimestamp`**，否则 **`IotDeviceService#updateDeviceState`**。
- **`IbmsDeviceRuntimeMapper`**：`selectDeviceIdsByState` / `selectOnlineDeviceIds`；**`IotDeviceMapper`**：`selectOnlineDeviceIdsNotInIbmsLedger`。

**前端（yudao-ui-admin-vue3）**

- **`src/api/iot/device/index.ts`**：`createDevice` / `updateDevice` / `deleteDevice` → **`/iot/ibms/device/*`**（导出仍旧路径）。

**数据库（mysql-ibms / ch_ibms，MCP `execute_sql`）**

- 新增仓库脚本 **`sql/mysql/ibms_channel_permissions_grant_tenant_and_video_admin_roles.sql`**；已在 **ch_ibms** 执行幂等授权（租户管理员/视频管理类角色 + `iot:ibms-channel:*`）。

**构建**

- `mvn clean install -DskipTests` 全量已通过（本会话）。

**明确未做 / 后续**

- **`cleanup-verify`**：未删旧 **`iot_*`** 表与旧 CRUD；**`IotDeviceService`** 在发现、通道、部分 Controller/Job 仍有引用，可继续按域收缩。

### 2026-03-25 — 会话：IBMS 设备 Excel 导出 + 发现双轨判存 + 权限落库 + cleanup 可选脚本

**后端（yudao-module-iot-biz）**

- **`IbmsDeviceController#exportDeviceExcel`**：`PageParam.PAGE_SIZE_NONE` 全量分页 + **`ExcelUtils`** 写出；新增 **`IbmsDeviceExcelVO`**（EasyExcel 列头）。
- **`IbmsDevicePageReqVO` / `IbmsDeviceMapper`**：增加 **`ibmsProductId`** 筛选（导出/列表与按产品过滤一致）。
- **`IbmsDeviceService#isDeviceExistsByIp`**（**`@TenantIgnore`**，按 **`ibms_device.ip`**）；**`DiscoveryEventListener`**：`exists = ibms || iot`（双轨）。

**前端（yudao-ui-admin-vue3）**

- **`src/api/iot/ibms/device.ts`**：**`exportDeviceExcel`**；**`IbmsDevicePageReqVO.ibmsProductId`**。
- **`src/api/iot/device/index.ts`**、**`src/api/iot/device/device/index.ts`**：导出改 **`/iot/ibms/device/export-excel`**（参数映射 **`keyword` / `systemCode` / `ibmsProductId`**）。
- **`src/views/ibms/device/index.vue`**：导出按钮改为 **`IbmsDeviceApi.exportDeviceExcel`**（与当前筛选条件一致）。

**数据库（mysql-ibms / ch_ibms，MCP `execute_sql`）**

- 执行仓库 **`sql/mysql/ibms_device_permissions_buttons.sql`** 等价语句：插入 **`iot:ibms-device:query/create/update/delete/export`** 按钮并授予 **role_id=1**；**`system_menu.id=71385`**（**ibms-device**）补齐 **`permission=iot:ibms-device:query`** 且补绑超管菜单（若缺失）。

**构建**

- `mvn -pl yudao-module-iot/yudao-module-iot-biz -am compile -DskipTests` 已通过（本会话）。

**明确未做 / 后续**

- **`extend-ibms-services` / `migrate-rest-biz`**：仍有多处 **`IotDeviceService`** 依赖（分组、产品分类、离线 Job、物模型 Controller 等），按域继续收缩。
- **`cleanup-verify`**：仅提供 **`iot_legacy_tables_rename_to_bak_optional.sql`**，**不在环境自动 RENAME/DROP**。

### 2026-03-25 — 会话：分组/产品删除/job_config/离线 Job/物模型属性 IBMS 双轨收口

**后端（yudao-module-iot-biz）**

- **`IbmsDeviceMapper`**：`selectCountByGroupId`（`FIND_IN_SET` + `group_ids`）、`selectCountByIbmsProductId`。
- **`IbmsDeviceRuntimeService` / `Impl`**：`updateJobConfig`（无运行态行时按台账补插）。
- **`IotDeviceGroupService` / `Impl`**：`countDevicesAssignedToGroup`（`iot_device.group_id` + IBMS `group_ids`）；**`IotDeviceGroupController`** 分页设备数改用该方法。
- **`IotProductServiceImpl#deleteProduct`**：设备数校验 = **`getDeviceCountByProductId` + `selectCountByIbmsProductId`**（产品主键与 **`ibms_product.id` 对齐约定**）。
- **`DeviceJobConfigController`**：若存在 **`ibms_device`** 主键则读写 **`ibms_device_runtime.job_config`**，否则回退 **`IotDeviceService`**。
- **`IbmsIotDualTrackDeviceResolver`**：`listOnlineDeviceShellsMergedForOfflineCheck`；**`IotDeviceOfflineCheckJob`** 改为使用该列表。
- **`IbmsDeviceLedgerRuntimeHelper#buildLegacyOtaDeviceShell`**：补 **`productKey`**，供离线 Job 日志与消息链路。
- **`IotDevicePropertyController`**：`getDeviceShellPreferIbmsThenIot` 替代 **`getDevice`**。
- **`IotDeviceMessageController`**：移除未使用的 **`IotDeviceService`** 注入。
- **`IotProductCategoryServiceImpl`**：移除已无用的 **`IotDeviceService`** 与多余 static import。
- **`IotAccessDeviceCapabilityServiceImpl`**：写 **`ibms_device.extra`** 分支用 **`IotDeviceMapper#selectById`** 判断是否存在 **`iot_device`** 行。

**数据库（mysql-ibms / ch_ibms，MCP `execute_sql`）**

- 当前库 **`ibms_device`** 为极简列集时缺少分组/产品外键列：已执行 **`ALTER TABLE ibms_device ADD ibms_product_id`**、**`ADD group_ids`**（与仓库增量脚本语义一致，已存在列的环境需跳过避免重复执行）。

**构建**

- `mvn -pl yudao-module-iot/yudao-module-iot-biz -am compile -DskipTests` 已通过（本会话）。

**明确未做 / 后续**

- 报警主机/通道双轨收口见 **下文**「报警主机台账 IBMS 化」记录；**`cleanup-verify`** 仍未 RENAME/DROP 旧 **`iot_*`** 表。

### 2026-03-25 — 会话：报警主机台账 IBMS 化 + 旧 IoT 通道 Service 去依赖

**后端（yudao-module-iot-biz）**

- **`IbmsProductMapper#selectByExtraLegacyProductKey`**；**`IbmsProductService#getProductByLegacyIotProductKey`**。
- **`IbmsDeviceMapper#selectMaxNumericSuffixByDeviceCodePrefix`**：报警主机等设备按编码前缀递增流水。
- **`IbmsDeviceServiceImpl#createDevice`**：匹配到产品模板时写入 **`ibms_product_id`**。
- **`IotAlarmHostServiceImpl`**：`createAlarmHost` 改为 **`IbmsDeviceService#createDevice`**，`extra` 写入 **`deviceKey`/`account`/`tcpPort`/密码** 供网关解析；**`deleteAlarmHost`** 在删主机记录后 **`ibmsDeviceService.deleteDevice`**。
- **`IotDeviceChannelServiceImpl`**：移除 **`IotDeviceService`**，**`createChannel` / `syncDeviceChannels` / `syncAccessChannels` / `syncNvrChannels`** 改 **`IbmsIotDualTrackDeviceResolver`** 与 **`IbmsDeviceLedgerRuntimeHelper`**（门禁用 **Access** 壳并补 **`deviceKey`**，NVR 同步用 **NVR** 壳）。

**数据库（mysql-ibms / ch_ibms，MCP `execute_sql`）**

- 幂等插入 **`ibms_product`**：`product_code=AL-AL-C-SERVER-OTH-001`，**`extra.productKey=ALARM_HOST_PRODUCT`**（与历史 **`iot_product.product_key`** 对齐）；仓库脚本 **`sql/mysql/ibms_product_alarm_host_seed.sql`**。

**构建**

- `mvn -pl yudao-module-iot/yudao-module-iot-biz -am compile -DskipTests` 已通过（本会话）。

**明确未做 / 后续**

- **`cleanup-verify`**：仍未 RENAME/DROP **`iot_*`**；**`IotDeviceController`** 等旧 CRUD 仍在（**`/list-all-online`** 的 IBMS 合并见 **下文**）。

### 2026-03-25 — 会话：收缩 `IotDeviceService` 引用 + 网关全量在线列表 IBMS 优先

**后端（yudao-module-iot-biz）**

- 新增 **`IotDeviceSpringCacheEvictor`**：在绕过 **`IotDeviceServiceImpl`** 直接 `updateById` 时按 Spring Cache 约定清理 **`iot:device`**。
- **`IotDeviceMapper#selectOnlineDevicesNotInIbmsLedger`**：供 **`IotDeviceController#/list-all-online`** 与 **`IbmsDeviceGatewaySupportService#listOnlineDevices`** 结果合并（去重策略：同一 `id` 以 IBMS 为准，遗留行仅 `NOT EXISTS ibms_device`）。
- **`IotDeviceGroupServiceImpl`**、**`IotProductServiceImpl#deleteProduct`**：**IoT 侧计数**改 **`IotDeviceMapper`**。
- **`DeviceJobConfigController`**、**`IotOtaUpgradeJob`**、**`IotOtaTaskRecordController`**、**`IotDataRuleServiceImpl`**：去除对 **`IotDeviceService`** 的依赖（改为 **`IotDeviceMapper`** 或等价逻辑）。
- **`IotDeviceServiceInvokeServiceImpl`**：无 **`iot_device`** 行时允许 **`ibms_device`** 记服务调用日志。
- **`IotAccessDeviceCapabilityServiceImpl`**：写回 **`iot_device.config`** 改 **`IotDeviceMapper` + `IotDeviceSpringCacheEvictor`**。

**数据库（mysql-ibms / ch_ibms，MCP `execute_sql`）**

- 只读校验：`information_schema` 确认 **`ibms_device`、`ibms_device_runtime`、`iot_device`** 均存在（无新增 DDL）。

**构建**

- `mvn clean install -DskipTests` 全量已通过（本会话）。

**明确未做 / 后续**

- **`IotDeviceController`** 除 **`/list-all-online`** 外仍为历史 **`/iot/device`** CRUD；**`cleanup-verify`** 仍未 RENAME/DROP **`iot_*`**。

### 2026-03-25 — 会话：`IotLegacyIotDeviceSideEffects` + 双轨 Resolver / 消息 / OTA 去 `IotDeviceService` 注入

**后端（yudao-module-iot-biz）**

- 新增 **`IotLegacyIotDeviceSideEffects`**：`isDeviceExistsByIp`、`updateDeviceState`（DB + **`IotDeviceSpringCacheEvictor`** + WebSocket）、`updateDeviceFirmware`；供发现、上行消息、OTA 进度等收口遗留 **`iot_device`** 写路径。
- **`IotDeviceServiceImpl`**：`isDeviceExistsByIp` / **`updateDeviceState(IotDeviceDO,Integer)`** / **`updateDeviceFirmware`** 委托上述组件，避免逻辑分叉。
- **`DiscoveryEventListener`**：IoT 侧判存改 **`IotLegacyIotDeviceSideEffects`**。
- **`IotDeviceMessageServiceImpl`**：`STATE_UPDATE` 非 IBMS 分支改 **`IotLegacyIotDeviceSideEffects#updateDeviceState`**。
- **`IotOtaTaskServiceImpl`**：勾选/全量设备回退路径改 **`IotDeviceMapper`** + 本地 **`validateLegacyIotDeviceListExists`**。
- **`IotOtaTaskRecordServiceImpl`**：升级成功写 **`iot_device.firmware_id`** 改 **`IotLegacyIotDeviceSideEffects#updateDeviceFirmware`**。
- **`IbmsIotDualTrackDeviceResolver`**：回退读改 **`IotDeviceMapper`**（`selectById` / `selectListByProductId` / `selectListByState` / `selectDevicesWithJobConfig`），不再注入 **`IotDeviceService`**。

**数据库（mysql-ibms / ch_ibms，MCP `execute_sql`）**

- 只读确认：`information_schema` 下 **`ibms_device`、`ibms_device_runtime`、`iot_device`** 三表均存在（计数 **3**）。

**构建**

- `mvn -pl yudao-module-iot/yudao-module-iot-biz -am compile -DskipTests` 已通过（本会话）。

**明确未做 / 后续**

- **`IotDeviceController`** 仍保留完整 **`IotDeviceService`** CRUD（前端主路径已 IBMS 时，可后续标记废弃或改为委托 **`IbmsDeviceService`**）。
- **`cleanup-verify`**：仍未对 **`iot_*`** 执行 RENAME/DROP。

### 2026-03-25 — 会话：`IotDeviceController` 废弃标注 + IBMS 设备 API 补全 + 前端 `DeviceApi` 主路径 IBMS

**后端（yudao-module-iot-biz）**

- **`IbmsDeviceController`**：新增 **`PUT /update-group`**、**`GET /count`**（IBMS 单台账）、**`GET /simple-list`**、**`DELETE /delete-list`**；权限沿用 **`iot:ibms-device:*`**。
- **`IbmsDeviceService` / `Impl`**：**`updateDeviceGroup`**（写 **`group_ids`** + 重推 Profile）、**`countDevicesByProduct`**、**`listSimpleDevices`**、**`deleteDeviceList`**；**`getDevice` / `getDevicePage`** 补 **`ibms_device_runtime.state`**（**`IbmsDeviceRuntimeMapper#selectStateMapByDeviceIds`**）；**`IbmsDeviceRespVO`** 增加 **`ibmsProductId` / `state` / `deviceType`**。
- **`IbmsDeviceMapper#selectSimpleList`**；**`IotDeviceServiceImpl`**：**`getDeviceCountByProductId`** 走双轨合计；**`updateDeviceGroup`** 在无 **`iot_device`** 命中时仍更新 **IBMS** 分组。
- **`IotDeviceController`**：类与 **Swagger** 标明兼容保留；除网关 **`/list-all-online`**、导入/模板、认证、按 productKey 查询外，**历史 CRUD 接口标记 `deprecated`**；**`/simple-list`** 合并 **`iot_device` + `ibms_device`**。

**前端（yudao-ui-admin-vue3）**

- **`src/api/iot/device/device/index.ts`**：分页、详情、数量、精简列表、删除、批量删、改分组 → **`/iot/ibms/device/*`**；**`create`/`update` 仍 `/iot/device`**（注释说明台账编辑请用 **`@/api/iot/ibms/device`**）。

**SQL**

- **`sql/mysql/iot_device_legacy_menu_hide_optional.sql`**：可选隐藏旧 IoT 设备菜单（注释模板）。

**数据库（mysql-ibms / ch_ibms，MCP `execute_sql`）**

- 行数抽样：**`iot_device`**≈10、**`ibms_device`**≈11、**`ibms_device_runtime`**≈0；**`information_schema`** 未检出指向 **`iot_*`** 的外键元数据（**`iot_legacy_tables_rename_to_bak_optional.sql` 仍须目标环境人工评估**）。

**构建**

- `mvn -pl yudao-module-iot/yudao-module-iot-biz -am compile -DskipTests` 已通过（本会话）。

**明确未做 / 后续**

- **`DeviceApi.createDevice`/`updateDevice`** 与 **IBMS 保存 VO** 未做自动映射；依赖旧 **`iot_device`** 表单的页面需逐步改为 **`IbmsDeviceSaveReqVO`** 或专用兼容层。
- **`cleanup-verify`**：仍未对 **`iot_*`** 执行 RENAME/DROP。

## 跨会话怎么用

- 新会话中 **`@` 本文件**，并写明「从待办哪一项继续」或「先 grep `IotDeviceDO` 剩余引用」。
- **会话结束前**：在 **「进展与变更记录」** 追加一节（日期 + 标题），并更新 **「待办状态总览」** 表里对应行的状态与说明。
- 大改动按「可编译切片」提交，避免一次改 90+ 文件难以回滚。

## 风险摘要

- 引用面大（Biz 内曾约 90+ 文件依赖 `IotDeviceDO`）；需分提交、每步可编译。
- `IbmsChannelDO` 宽字段进 `extra` 后，列表筛选需约定索引或冗余列策略。
- 规则/告警 DO 若存 `productId`，需统一改为 `ibms_product.id`。
