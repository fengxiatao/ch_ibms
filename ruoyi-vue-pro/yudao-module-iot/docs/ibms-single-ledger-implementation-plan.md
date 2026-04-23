# 单一 ibms_* 台账实施计划（执行版）

> **用途**：把 [`ibms-iot-model-convergence-plan.md`](./ibms-iot-model-convergence-plan.md) 里的 **「单一 ibms_* 完成定义（DoD）」** 落成可拆任务、可验收的工单；**进度与状态仍以主文档「待办状态总览」为准**，本文件可随迭代增删任务行。  
> **边界**：契约 DTO（如 `IotDeviceRespDTO`）可不改名，但数据来源必须收口到 `ibms_*` + `ibms_device_runtime`。  
> **原则（不并轨）**：**不允许**把「长期双写 / 先改 `iot_*` 再同步 `ibms_*`」作为终态；迁移期可以分迭代替换，但**每一域收口后的默认动作是删除旧分支与旧类**，只保留 `ibms_*` 数据面与对应 Service/Mapper。**本计划明确包含删除旧代码步骤**（见 **WP-G**，并与 **WP-E / WP-F** 衔接）。

## 1. 与主文档的对应关系

| 本计划阶段 | 主文档 |
|------------|--------|
| 验收标准 | [单一 ibms_* 完成定义（DoD）](./ibms-iot-model-convergence-plan.md#单一-ibms_-完成定义dod) |
| 宏观顺序 | [实施顺序（建议）](./ibms-iot-model-convergence-plan.md#实施顺序建议) |
| 里程碑行 | `single-ibms-ledger-only`（待办总览） |

### 删除旧代码在计划中的位置（摘要）

| 阶段 | 做什么 |
|------|--------|
| WP-B～D | **替换**实现，使新路径只写/只读 `ibms_*`；每完成一域即**删掉**该域内的 `iot_*` 回退分支，**不要**留「并行两套逻辑」。 |
| WP-E | **删除**双轨解析器中的 IoT 分支、废弃 Controller 方法或整体迁移路由后删方法。 |
| **WP-G** | **强制删除**：双写同步、`IotDeviceServiceImpl` 等整类（无引用后）、Mapper/XML/DO、双轨组件类、无用前端页与 API。 |
| WP-F | 表 `RENAME`/DROP 后（或与之同时）**物理删除**已无表可映射的 ORM 与生成物。 |

## 2. 前置条件（每环境执行本计划前）

- [x] 已执行或确认存在 `ibms_device_runtime`（见 `sql/mysql/ibms_device_runtime.sql`）。**2025-03-25**：已在 `ch_ibms`（mysql-ibms MCP）确认表存在。
- [x] 目标库 `ibms_device` 扩展列与仓库 DO/脚本一致（见 `ibms_device_extend_convergence.sql` / 幂等版 `ibms_device_extend_convergence_idempotent.sql`）。**2025-03-25**：已在 `ch_ibms`（mysql-ibms MCP）补齐 `nickname`～`device_type` 等缺失列。
- [x] 模块构建：已执行 `mvn -pl yudao-module-iot/yudao-module-iot-biz -am compile -DskipTests`（**2025-03-25**）。若改到 `yudao-framework` 等公共模块，请改做全量 `mvn clean install -DskipTests`。

## 3. 工作包（按推荐顺序）

### WP-A 数据迁移与对账（P1）

| 任务 | 说明 | 完成判据 | 状态 |
|------|------|----------|------|
| A1 设备行迁移 | 将仍只在 `iot_device` 的台账迁入 `ibms_device`（+ `extra` / `ibms_product_id` / `group_ids`），`device_id` 策略与现网约定一致（通常保留同一数值主键需事先评估） | 对账：`iot_device` 有效行可枚举为「待废弃」或 0 | **已完成**（**2025-03-25**）：已对 `ch_ibms` 执行幂等 INSERT；仓库脚本 `sql/mysql/ibms_single_ledger_a1_migrate_iot_device.sql`；对账 `device_iot_only_by_id=0` |
| A2 产品与通道 | `iot_product` → `ibms_product`；`iot_device_channel` → `ibms_channel`（含 `extra` 视频/门禁字段） | 业务查询通道不再依赖 `iot_device_channel` | **已完成**（**2025-03-25**）：脚本 `sql/mysql/ibms_single_ledger_a2_migrate_product_channel.sql`；MCP 执行后 `product_iot_only_by_id=0`、`channel_iot_only_by_id=0`；并回填 `ibms_device.ibms_product_id` 与 `nickname`/`device_key`/`device_secret` |
| A3 对账脚本/SQL | 幂等、可重复执行；输出差异报告（仅 IoT / 仅 IBMS / 字段不一致） | DBA/负责人在目标库签字或工单留档 | **已完成**：`sql/mysql/ibms_single_ledger_a3_reconcile.sql`（已增 `product_iot_only_by_id` / `channel_iot_only_by_id`）；**2025-03-25** MCP：`device`/`product`/`channel` 三项 `*_iot_only_by_id=0` |

### WP-B 后端按域去 `iot_*`（P2，可与 A 并行部分）

以下项来自当前主文档 **「部分」** 域，实施时以 **`rg IotDeviceMapper`** / **`rg IotProductMapper`** / **`rg IotDeviceChannelMapper`** 在 `yudao-module-iot-biz` 内清零业务引用为目标（测试代码除外）。

| 任务 | 主要落点（示例，以检索为准） | 完成判据 |
|------|------------------------------|----------|
| B1 门禁能力写回 | `IotAccessDeviceCapabilityServiceImpl#refreshCapability` 等：由写 `iot_device.config` 改为写 `ibms_device_runtime.config` 或台账 `extra`（与现设计一致） | **已完成**（**2025-03-25**）：快照写入 `ibms_device_runtime.config`（`IbmsDeviceRuntimeService#saveRuntimeConfig`），本路径已移除 `IotDeviceMapper#update` |
| B2 OTA / 任务校验 | `IotOtaTaskServiceImpl`、`IotLegacyIotDeviceSideEffects`：校验只读 IBMS | **已完成（2026-03-25）**：`IotOtaTaskServiceImpl#validateOtaTaskDeviceScope` 仅使用 `IbmsDeviceMapper` 校验设备范围，缺失直接抛 `DEVICE_NOT_EXISTS`，禁止回退到 `iot_device`；删除 legacy fallback 方法（如 `validateLegacyIotDeviceListExists`）后 OTA 任务/记录不再依赖 `iot_device` 行存在 |
| B3 规则 / Job / 采集 | `IotDataRuleServiceImpl`、`UnifiedJobScheduler`、`IotDeviceDataCollectJob`、`UniversalCameraCollector` 等。设备解析/校验以 `ibms_*` 为准 | **已完成（2026-03-26）**：`IotDataRuleServiceImpl` 与 `UniversalCameraCollector` 已单台账；`IbmsIotDualTrackDeviceResolver` 已移除 `IotDeviceMapper` 与 `iot_device` 回退/合并分支，`IotDeviceDataCollectJob` / `UnifiedJobScheduler` / `IotDeviceOfflineCheckJob` 仍调用该类但仅拼 IBMS 壳（离线检测仅 `ibms_device_runtime` 在线） |
| B4 分组 / 物模型 / Job 配置 | `IotDeviceGroupServiceImpl`、`DeviceJobConfigController`、`IotDevicePropertyController`：计数与更新以 `ibms_device` / runtime 为准 | **已完成（2026-03-25）**：设备计数仅 `ibms_device`（如 `IotDeviceGroupServiceImpl#countDevicesAssignedToGroup`）；Job 配置写入/删除仅 `ibms_device_runtime`（`DeviceJobConfigController` 直接调用 `IbmsDeviceRuntimeService`）；设备属性/物模型查询仅 `ibms_device`（`IotDevicePropertyController` 直接查 `IbmsDeviceMapper`，不再走 `IbmsIotDualTrackDeviceResolver` 解析器回退路径） |
| B5 通道服务 | `IotDeviceChannelServiceImpl` + `IbmsIotDualTrackDeviceResolver`：去掉 `iot` 回退 | **已完成（2026-03-26）**：`IotDeviceChannelServiceImpl` 创建设备校验/同步通道、`syncAccessChannels` / `syncNvrChannels` 仅 `ibms_device` + `IbmsDeviceLedgerRuntimeHelper`；并补齐 NVR+IPC 视频通道（legacy: `VIDEO`，IBMS: `typeCode=VT*`）的 `create/update/delete/get/page` + NVR/IPC 同步统一读写 `ibms_channel`（legacy 字段进 `extra`）；`IbmsIotDualTrackDeviceResolver` 已零 `IotDeviceMapper` |
| B6 设备导入与创建 | `IotDeviceServiceImpl#import` / `create`：写入仅 `ibms_*`（或委托 `IbmsDeviceService`） | **已完成（2026-03-26）**：`createDevice` 委托 `IbmsDeviceService#createDevice` + 回填 `product_key`/`device_key`/`device_secret`/`sn`/分组/GIS 等到 `ibms_device`/`ibms_device_runtime`；`importDevice` 按 `product_key+name` 查 `ibms_device`，更新走 `patchIbmsDeviceFromLegacyImport`；`updateDevice` 在仅 IBMS 台账时走 `patchIbmsDeviceFromLegacyUpdate`；校验楼层/DXF/序列号/重名改用 `IbmsDeviceMapper` |

### WP-C 网关与在线列表（DoD-4）

| 任务 | 说明 | 完成判据 |
|------|------|----------|
| C1 `list-all-online` | `IotDeviceController#getAllOnlineDevices` 仅 `IbmsDeviceGatewaySupportService`（或等价），删除 `iotDeviceMapper.selectOnlineDevicesNotInIbmsLedger`。**已完成（2025-03-25）**：Controller 已去除 `IotDeviceMapper`；`IbmsDeviceGatewaySupportServiceImpl#listOnlineDevices` 合并 `extra.gatewayRuntimeState` 在线与 `ibms_device_runtime.state` 在线（避免仅依赖 extra 时遗漏已迁入设备）。`IotDeviceMapper` 中 `selectOnlineDeviceIdsNotInIbmsLedger` / `selectOnlineDevicesNotInIbmsLedger` 已删除。 | 方法内无 `IotDeviceMapper` |
| C2 NewGateway 启动拉设备 | 确认网关消费的 URL/DTO 与 C1 一致，无第二数据源 | 联调记录（需人工在网关侧确认仍调用同一 `list-all-online` 且行为满足预期） |

### WP-D API 与前端（P4）

| 任务 | 说明 | 完成判据 |
|------|------|----------|
| D1 写路径统一 | `create`/`update`/导入模板等仍指向 `/iot/device` 的，迁到 `/iot/ibms/device` 或专用 IBMS API | **已完成（2026-03-26）**：`IbmsDeviceController` 补齐 `/iot/ibms/device/import` 与 `/iot/ibms/device/get-import-template`；仓库内脚本/文档对旧写入口的写路径引用已迁到 `/iot/ibms/device/*`，避免新集成继续依赖旧写入口 |
| D2 权限 | 菜单仅 `iot:ibms-device:*` 等；执行 `ibms_replace_legacy_iot_device_product_channel_menus.sql`（按需） | **已完成（2026-03-26）**：已在 `ch_ibms.system_menu` 执行 `ibms_replace_legacy_iot_device_product_channel_menus.sql`（幂等），当前 IBMS discovery 菜单可见；IoT 相关重复/弃用菜单在库中已下线（不可见或不存在） |
| D3 隐藏旧菜单 | 可选：`iot_device_legacy_menu_hide_optional.sql` | 用户不可从侧栏进入废弃页 |

### WP-E 去壳与删类（P3，依赖 B/C）

| 任务 | 说明 | 完成判据 |
|------|------|----------|
| E1 `IbmsIotDualTrackDeviceResolver` | **删除** `iotDeviceMapper` 全部分支；调用方改为只读 `IbmsDeviceDO` / runtime 后，**无引用则删除整类** | **已完成（2026-03-26）**：已从 `IbmsIotDualTrackDeviceResolver` 内移除 `IotDeviceMapper` 注入与所有 `iot_device` 回退/合并分支（类内零 `IotDeviceMapper`） |
| E2 `IotLegacyIotDeviceSideEffects` | **删除**或合并为仅操作 `ibms_*` 的 Helper；禁止保留「先 legacy 再 IBMS」 | **已完成（2026-03-26）**：`IotLegacyIotDeviceSideEffects` 已改为仅委托 `ibms_device_runtime`（状态/固件更新走 `IbmsDeviceRuntimeService`），同时 `isDeviceExistsByIp` 仅走 `IbmsDeviceService`，Helper 内零 `iot_device` 写副作用 |
| E3 `IotDeviceController` | **删除**已标记 deprecated 的 CRUD/分页/导出等整段方法；网关用路径迁至 `IotDeviceGatewayRpcController`（或等价）后 **Controller 内不再并存两套台账 API** | **已完成（2026-03-26）**：`IotDeviceController` 已将 deprecated 的 CRUD/分页/导出/分组更新/导入入口统一返回 `410`，强制改用 `/iot/ibms/device`；保留的为网关初始化等非台账 CRUD 能力 |

### WP-G 删除旧代码与去并轨（必选，与 E 衔接、在 F 前尽量完成）

> **目标**：终态**没有**「IoT 台账 + IBMS 台账」并行维护；删除所有仅服务于 `iot_*` 的实现与双写。

| 任务 | 说明 | 完成判据 |
|------|------|----------|
| G1 去掉双写 | 删除「更新 `iot_device` 后再同步 `ibms_device`」或反向同步；**只保留对 `ibms_*` / `ibms_device_runtime` 的写入** | **已完成（2026-03-26）**：已移除 `IotDeviceServiceImpl` 对 `iot_device` 的 update/delete 写入；`IotLegacyIotDeviceSideEffects` 状态/固件更新仅写 `ibms_device_runtime`；`IotAccessDeviceServiceImpl` 激活/停用与 `DeviceCoordinateSyncService` DXF 坐标同步也已改写 `ibms_device_runtime`；`ChanghuiDeviceServiceImpl` 已移除 `iotDeviceMapper.insert/update/delete`，改为写 `ibms_device`/`ibms_device_runtime`；`IotGisMapper.xml#updateDeviceLocation` 已改为写 `ibms_device_runtime.latitude/longitude`。除 `sql/mysql/*` 迁移/运维脚本外，应用层 `src/main/java` + `src/main/resources/mapper` 已无 `iot_device` 写入 DML。 |
| G2 删除 `IotDeviceServiceImpl` 等整类 | 在 B6、D1 完成后，创建设备/导入/分组等已全部委托 `IbmsDeviceService` 或迁入 IBMS 专用 Service 后，**删除** `IotDeviceServiceImpl`；同步删除仅被其使用的私有工具方法 | 无 Bean 实现 `IotDeviceService` 或接口本身删除；**已完成（2026-03-26）**：已新增 `IbmsLegacyIotDeviceAdapterService` 承接 legacy 创建/导入等入口，并删除 `IotDeviceServiceImpl`，`yudao-module-iot-biz` 编译通过 |
| G3 删除 IoT 台账 Controller 残余 | 删除 `IotProductController` / `IotDeviceGroupController` 等已由 IBMS 接管的旧控制器中**重复**能力；路由以 `/iot/ibms/*` 为准 | 管理端不存在功能等价的 `/iot/device` 与 `/iot/ibms/device` 双入口；**已完成（2026-03-26）**：已将 `IotProductController`（`/iot/product`）与 `IotDeviceGroupController`（`/iot/device-group`）统一下线为 `410` |
| G4 删除持久层（`iot_*` 表映射） | **删除** `IotDeviceMapper.java`、对应 `IotDeviceMapper.xml`、`IotDeviceDO`（及 `iot_product`、`iot_device_channel` 等同理），在 G1～G2 与 F1 条件满足后执行 | `yudao-module-iot-biz` 编译通过；无残留 XML `namespace` |
| G5 删除前端旧面与 API | 删除不再挂载菜单的 `views/iot/device/**`、`views/iot/product/**` 等；删除或合并仅调用已删后端的 `api/iot/device/**` | 构建通过；路由无指向已删页 |
| G6 清理计数/统计双轨 | `getDeviceCountByProductId`、`countDevicesByProductDualTrack` 等改为**仅** `ibms_device` 统计后，删除「双轨合计」方法与注释 | 产品详情/统计屏数据与 IBMS 一致且无 `dual` 命名残留 | **已完成（2026-03-26）**：移除 `countDevicesByProductDualTrack`，`/iot/ibms/device/count` 仅统计 `ibms_device`；更新 Service/Controller 并清理 `IbmsDeviceServiceImpl` 对 `IotDeviceMapper` 的注入与双轨计算逻辑。 |

### WP-F 库表清理（P5，最后）

| 任务 | 说明 | 完成判据 |
|------|------|----------|
| F1 应用层确认 | **WP-G 已完成**；全仓 `IotDeviceMapper` / `IotProductMapper` / `IotDeviceChannelMapper` 等业务引用为 **0**；主服务 + NewGateway 冒烟通过 | 工单/发布说明 + `rg` 截图或 CI 检索记录 |
| F2 备份 | 全库逻辑备份 | DBA 确认 |
| F3 执行 rename | `sql/mysql/iot_legacy_tables_rename_to_bak_optional.sql` 按环境 uncomment 并执行 | 表已 `_bak` 或 DROP |
| F4 删 ORM 收尾 | 若 G4 因依赖顺序未删尽，在表下线后**必须**删除剩余 DO/Mapper/XML；删除 `iot_*` 表对应 Flyway/初始化脚本中的业务依赖说明（若有） | `mvn clean install -DskipTests` 通过 |

## 4. 每工作包通用验收

1. `mvn clean install -DskipTests`（涉及 `yudao-framework` / 多模块时全量）。
2. 相关模块单测或手工冒烟（设备创建、网关上线、视频拉流、门禁刷新、OTA 创建至少各一条）。
3. 更新 [`ibms-iot-model-convergence-plan.md`](./ibms-iot-model-convergence-plan.md)：**进展与变更记录** 一条 + **待办状态总览** 相应行。

## 5. 风险与回滚

- **风险**：迁移主键不一致导致外键/历史日志无法关联 → A1 前必须冻结方案并做对账。
- **风险**：rename 过早 → 生产立刻 `Table doesn't exist` → 仅 F 在 F1 之后执行；`RENAME` 可逆，回滚需同一窗口。
- **风险**：网关与 Biz 版本不一致 → 按仓库「构建与运行版本一致性」规则发布。

## 6. 建议的迭代切片（示例）

- **迭代 1**：B1 + B5 局部 + C1 + **G1 局部**（去掉本域双写）。
- **迭代 2**：B2 + B3 + B4 + **G6**。
- **迭代 3**：D1 + D2 + B6 + **G5**（前端删旧）。
- **迭代 4**：E1–E3 + **G2 + G3 + G4**（删 Service/Controller/Mapper/DO）。
- **迭代 5**：F1–F4（表 rename + ORM 收尾）。

（切片可按人力调整；**不要**在未完成 A/B/C 与 **G1～G4 所涉引用清零** 前执行 F3；**不并轨**意味着每迭代合并代码时应倾向「删旧分支」而非「新旧并存」。）

---

**文档版本**：与仓库同维护；新增工作包时请补「完成判据」一行，避免无法验收。
