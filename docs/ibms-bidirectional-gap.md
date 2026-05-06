# IBMS 双向校验报告（前端需求 × 后端能力）

> 配套主计划：`docs/ibms-unified-data-source-plan.md`
> 配套矩阵：`docs/ibms-coverage-matrix.md`（M1 阶段产出）
> 生成日期：2026-05-06（M1.5 阶段）
> 输入：M1 109 行覆盖矩阵 + 后端 controller/service 实测扫描 + DB `ibms_*` 表清单（34 张，含 3 张 bak）

---

## 0. 关键发现（先看）

1. **🟡聚合层 ≠ "底层未对接 IBMS"**：`IotSecurityOverviewServiceImpl`、`IbmsBacController`、`IbmsEnergyController`、`IbmsEnvController`、`IbmsLightingController` 实测代码内含大量 `IbmsDeviceMapper` / `ibms_*` 查询（仅 SecurityOverview Service 就有 27 处 `IbmsXxx` 引用）。**M1 矩阵中标为🟡聚合层的页面，绝大多数底层已经在查 ibms_***。
2. **`AccessDashboardController` 已经提供 6 个聚合端点**（`statistics` / `real-time` / `trend` / `device-status` / `heatmap` / `abnormal-events`）。**M2-A 接入 + M2-D 根因修复完成（2026-05-06，commit `0b62d00`）**：原后端实现注入的 `AccessRecordMapper`/`AccessAlarmMapper` DO 标注 `iot_access_record`/`iot_access_alarm` 表在 DB 中**不存在**，导致 statistics/real-time 端点也只是"假上线"，trend/device-status/heatmap/abnormal-events 是显式 stub。本次全部迁到真实表 `iot_access_event_log`（56448 行真数据），4 个 stub 全部落地。
3. **真正的"后端缺"主要集中在 security 子模块**：周界 VisualBoard / 电子巡更可视化板 / 视频巡更计划 / 人员管控（7 子页全 mock）等大屏，目前都是前端硬编码假数据，**且后端尚无对应聚合 controller**。
4. **后端 0 行表与备份表是后端富余 / 待清理资产**：`ibms_device_message`、`ibms_device_property_history`、`ibms_lighting_scene_circuit` 当前 0 行；`ibms_channel_bak_20260504_business`、`ibms_channel_bak_20260505_legacy`、`ibms_device_bak_20260505_legacy` 是 v23 治理留下的备份，应在 M2 清理评估。

---

## A. 业务模块数据需求清单（按页面归类，30 类高频需求）

> 来源：M1 矩阵（109 行）按"数据用途"归类。每一类对齐到 1~多个页面 + 后端能力。

### A.1 智慧安防（48 页，33 项需改）

| # | 需求 | 涉及页面 | 当前状态 |
|---|---|---|---|
| A1-1 | 摄像头实时预览（取流 + 在线状态） | `VideoSurveillance/RealTimePreview`、`VideoSurveillance/MultiScreenPreview` | 🟤旧 `@/api/iot/video` + `@/api/iot/spatial`，未走 IBMS |
| A1-2 | 摄像头分组（按建筑/楼层） | `VideoSurveillance/RealTimePreview`、`VideoSurveillance/SnapshotRecord` | `SnapshotRecord` 已 ✅ 用 ibms/space + ibms/channel；其他仍用 `spatial/*` |
| A1-3 | 视频抓图记录 | `VideoSurveillance/SnapshotRecord` | ✅ 已 IBMS 直连 |
| A1-4 | 视频回放 | `VideoSurveillance/VideoPlayback` | 🟤 `@/api/security/playback`（独立 controller） |
| A1-5 | 视频告警记录大屏 | `VideoSurveillance/VideoAlarmRecord` | ❌ Mock，后端缺聚合 |
| A1-6 | 视频监控大屏 | `VideoSurveillance/VisualBoard` | ✅ 已用 ibms + security-overview |
| A1-7 | 视频巡更（计划/任务/记录/点位） | `VideoPatrol/*` 6 页 | 🟤 `@/api/iot/videoPatrol`，独立体系；PatrolPlans 大屏 ❌Mock |
| A1-8 | 视频巡更任务调度（设备+时间窗） | `VideoPatrol/PatrolTask` | 🟤 调 `@/api/iot/device/device` + `@/api/iot/spatial/*`，待统一 |
| A1-9 | 视频分析任务/告警/终端/算法配置 | `VideoAnalysis/*` 4 页 | ❌ AnalysisTasks 大屏 mock；其他 3 页是 ⚪ 路由壳 |
| A1-10 | 报警主机管理 | `PerimeterIntrusion/AlarmHost` | ✅ 已 IBMS（`groupCode=SA`） |
| A1-11 | 报警操作日志 / 周界报警记录 / 防区管理 | `PerimeterIntrusion/AlarmOperationLog/PerimeterAlarmRecord/ZoneManagement` | 🟤 走 `@/api/iot/alarm/*`（事件/操作/主机三表），逻辑独立 |
| A1-12 | 周界 VisualBoard 大屏 | `PerimeterIntrusion/VisualBoard` | ❌ Mock，后端缺聚合 |
| A1-13 | 布防计划 | `PerimeterIntrusion/ArmingPlan` | ❌ Mock，后端缺 controller |
| A1-14 | 安防总览大屏 | `SecurityOverview/index.vue` | 🟡 `@/api/iot/security-overview` —— 实测底层 27 处 ibms_* 引用，**已 IBMS 直连** |
| A1-15 | 电子巡更（计划/路线/任务/点位/可视化板） | `ElectronicPatrol/*` 8 页 + `EPatrol/*` 6 页 | 🟤 `@/api/iot/patrol` + `@/api/iot/epatrol`，**两套并存**；可视化板 ❌Mock |
| A1-16 | 人员管控（识别/捕获/库/控制/告警/轨迹/车辆） | `PersonnelControl/*` 7 页 | ❌ 全 Mock，后端缺 controller |
| A1-17 | 新版入侵告警 | `NewIntrusionAlarm/index.vue` | ⚪ 路由壳，需求未启动 |

### A.2 智慧通行（22 页，21 项需改）

| # | 需求 | 涉及页面 | 当前状态 |
|---|---|---|---|
| A2-1 | 门禁设备列表/详情 | `iot/access/device`、`iot/access/management` | 🟠 `AccessDeviceApi`（后端 service 已混用 IbmsDeviceMapper，单源化未完成） |
| A2-2 | 门禁通道列表 | `iot/access/channel` | 🟠 `AccessChannelApi` |
| A2-3 | 通行记录 | `iot/access/record`、`iot/access/event` | 🟠 已对接 access 表 |
| A2-4 | 人员/部门/卡片/HR 同步 | `iot/access/person/department/card/hr` | 🟠 业务自有数据，不一定需走 IBMS |
| A2-5 | 权限组/授权/授权进度/任务 | `iot/access/permission-group/authorization/auth-progress/auth-task` | 🟠 业务逻辑，可保留 access |
| A2-6 | 设备同步/分发/操作日志 | `iot/access/device-sync/dispatch/operation-log` | 🟠 业务逻辑 |
| A2-7 | 通行可视化大屏（24h 折线/今日通行/排行/在场） | `iot/access/visual-dashboard` | ❌ Mock —— **AccessDashboardController 已有 6 端点，前端未接！** |
| A2-8 | 门禁地图/告警/门组/岗 | `iot/access/map/alarm/doorGroup/doorPost` | 🟠 业务逻辑 |
| A2-9 | 停车（独立 access_parking_*） | `iot/access/parking/*`（M1 未涉及） | 🟢 业务自有 |

### A.3 智慧能源（18 页，16 项需改）

| # | 需求 | 涉及页面 | 当前状态 |
|---|---|---|---|
| A3-1 | 能源仪表板（总量/分项/趋势） | `energy/Overview`、`iot/building/energy/index` | 🟡 `IbmsEnergyController` —— **底层已查 `ibms_energy_statistics_daily`** |
| A3-2 | 表具管理（电/水/气/冷热） | `energy/DeviceManagement`、`iot/building/energy/equipment` | DeviceManagement ❌ Mock；equipment ✅ IBMS（已对接） |
| A3-3 | 用能分析（周期/趋势） | `energy/ConsumptionAnalysis`、`iot/building/energy/analysis/{period,trend}` | period ❌Mock；trend/Consumption ✅ 走 IbmsEnergyController |
| A3-4 | 告警管理 | `energy/AlarmManagement`、`iot/building/energy/alarm` | ✅ 走 ibms_energy_alarm |
| A3-5 | 报表/抄表（人工+远程） | `energy/ReportManagement`、`iot/building/energy/report/manual-reading/remote-reading` | ✅ 走 IbmsEnergyController + ibms_energy_manual_reading |
| A3-6 | 能耗实时监测 | `iot/building/energy/monitor` | ✅ 走 IbmsEnergyController |
| A3-7 | 能耗台账 | `iot/building/energy/ledger` | ✅ 走 IbmsEnergyController |
| A3-8 | 系统设置（费率/阈值） | `energy/SystemSettings`、`iot/building/energy/settings` | SystemSettings ❌ Mock；settings ✅ |

### A.4 智慧建筑（21 页，14 项需改，不含 energy 子目录）

| # | 需求 | 涉及页面 | 当前状态 |
|---|---|---|---|
| A4-1 | 建筑可视化大屏（空间树+设备分组+在线率+告警） | `building-visual-dashboard` | ❌ Mock，后端 IbmsSpace + IbmsDevice 数据已具备但**缺空间维度聚合 API** |
| A4-2 | 楼宇自控监测/台账/告警/日志 | `bac/monitor/ledger/alarm/log` | monitor/ledger ✅ 走 IbmsBacController；alarm ❌Mock；log ⚪ 路由壳 |
| A4-3 | 环境监测（总览/传感器/告警/设置） | `env/overview/sensor/alarm/settings` | overview/sensor ✅ 走 IbmsEnvController；alarm ❌Mock；settings ⚪ |
| A4-4 | 旧照明（场景/控制/设备/告警/日志） | `lighting/scene/control/device/alarm/log` | scene/control ✅ 走 IbmsLightingController；device/alarm ❌Mock；log ⚪ |
| A4-5 | 新照明（总览/控制/设备/电路/任务/告警/日志） | `newlight/overview/control/device/circuit/task/alarm/log` | 全部 ❌Mock 或 ⚪ 路由壳，**后端 IbmsLightingController 已有，但前端未接** |

---

## B. IBMS 后端能力清单（实测 2026-05-06）

### B.1 数据库表（`ch_ibms` 库 34 张 `ibms_*` 表）

```text
核心台账（行数实测）：
  ibms_device                         49 行  ← 唯一设备主台账
  ibms_channel                       137 行  ← 通道/点位
  ibms_space                          14 行  ← 空间树
  ibms_product                        59 行  ← 产品定义
  ibms_product_property               37 行
  ibms_product_point_type             39 行
  ibms_device_runtime                 31 行  ← 在线/告警状态
  ibms_discovered_device               8 行  ← 发现设备

专项扩展表：
  ibms_energy_meter                   46 行  (有 ibms_device_id)
  ibms_energy_record                  42 行
  ibms_energy_statistics_daily       126 行
  ibms_energy_alarm                   19 行
  ibms_energy_manual_reading           6 行
  ibms_energy_rate                    12 行
  ibms_hvac_device                    36 行  (待校验外键)
  ibms_water_device                   16 行  (待校验外键)
  ibms_lighting_controller             8 行
  ibms_lighting_gateway                6 行
  ibms_lighting_circuit               28 行
  ibms_lighting_scene                 10 行
  ibms_lighting_schedule               8 行
  ibms_lighting_alarm                  8 行
  ibms_lighting_operation_log         12 行
  ibms_lighting_scene_circuit          0 行  ⚠️ 0 行，未使用
  ibms_env_sensor                     11 行
  ibms_env_data_record               198 行
  ibms_env_alarm                       4 行
  ibms_bac_alarm                      12 行
  ibms_bac_system_log                 16 行

低使用 / 0 行：
  ibms_device_message                  0 行  ⚠️ 待评估
  ibms_device_property_history         0 行  ⚠️ 待评估

历史备份（应在 M2 清理）：
  ibms_channel_bak_20260504_business 119 行  ← v22 治理产物
  ibms_channel_bak_20260505_legacy    30 行  ← v23 治理产物
  ibms_device_bak_20260505_legacy      7 行  ← v23 治理产物
```

### B.2 后端 Controller 清单

```text
1) IBMS 基础源头（cn.iocoder...controller.admin.ibms.*）—— 5 个：
   - IbmsChannelController         /admin-api/iot/ibms/channel/*
   - IbmsDeviceController          /admin-api/iot/ibms/device/*
   - IbmsDeviceDiscoveryController /admin-api/iot/ibms/discovery/*
   - IbmsProductController         /admin-api/iot/ibms/product/*
   - IbmsSpaceController           /admin-api/iot/ibms/space/*

2) IBMS 业务聚合（cn.iocoder...controller.admin.building.*）—— 4 个：
   - IbmsBacController             /admin-api/iot/building/bac/*       (查 ibms_bac_*, ibms_device)
   - IbmsEnergyController          /admin-api/iot/building/energy/*    (查 ibms_energy_*)
   - IbmsEnvController             /admin-api/iot/building/env/*       (查 ibms_env_*)
   - IbmsLightingController        /admin-api/iot/building/lighting/*  (查 ibms_lighting_*)

3) Access 业务聚合（cn.iocoder...controller.admin.access.*）—— 22 个：
   - AccessDashboardController     /admin-api/iot/access/dashboard/*  ⚠️ 6 端点前端未接
   - IotAccessDeviceController     /admin-api/iot/access/device       (混用 IbmsDeviceMapper, 待 M2 单源化)
   - IotAccessChannelController    /admin-api/iot/access/channel
   - IotAccess{Person,Card,Department,Credential,PermissionGroup,...}Controller
   - DoorGroup/DoorPost/AccessRecord/AccessAlarm/AccessVideo Controller

4) Security 聚合（cn.iocoder...controller.admin.security.*）—— 2 个：
   - IotSecurityOverviewController /admin-api/iot/security-overview/* (Service 27 处 ibms_* 引用，已 IBMS 直连)
   - VideoPlaybackController       /admin-api/security/playback

5) Alarm（cn.iocoder...controller.admin.alarm.*）—— 3 个：
   - IotAlarmHostController        /admin-api/iot/alarm/host
   - IotAlarmEventController       /admin-api/iot/alarm/event
   - IotAlarmOperationLogController /admin-api/iot/alarm/operationLog

6) 业务大类映射工具：IbmsBusinessMappingHelper（提供 groupCode SA/ST/SB/SE/SF/GW + systemCode 字典）
```

### B.3 前端 API 目录扫描（84 个 `.ts` 文件）

```text
@/api/iot/ibms/*       ✅ 基础源头（device/channel/space/product/discovery）
@/api/iot/building/*   ✅ 业务聚合（bac/energy/env/lighting，底层即 IBMS 直连）
@/api/iot/security-overview ✅ 安防总览（底层 IBMS 直连）
@/api/iot/access/*     🟠 通行（device/channel 待单源化，其余业务自有）
@/api/iot/alarm/*      🟤 周界报警三表（host/event/operationLog）
@/api/iot/video/*      🟤 视频体系（与 SnapshotRecord 已 IBMS 兼容；其他大屏未接）
@/api/iot/spatial/*    🟤 老空间（campus/building/floor/area），需 M3/M6 替换为 ibms_space
@/api/iot/device/*     🟤 老设备 API，需替换为 ibms_device
@/api/iot/{patrol,patrolplan,videoPatrol,epatrol}/* 🟤 巡更体系（4 套并存，M6 整合）
@/api/iot/parking/*    🟢 停车独立体系（access_parking_*）
@/api/iot/{gis,changhui,task,videoView,cameraCruise,...} 🟤 边缘子模块
```

---

## C. 前端要而后端没有（→ M2 后端补全清单）

| 编号 | 业务页面 | 需求 | 后端缺什么 | 优先级 | 目标 M 阶段 |
|---|---|---|---|---|---|
| GAP-001 | `iot/access/visual-dashboard` | ✅ **2026-05-06 已修复**（M2-A `ed251c6` + M2-D `0b62d00`）：6 端点全部真实落地 + 前端切到 trend 端点 + 4 metric 卡片 + today/week/month/year 趋势走真数据 | ~~后端 record/alarm 表不存在，trend/device-status/heatmap/abnormal-events 是 stub~~ → 已迁到 `iot_access_event_log` 并实现 4 stub | P0 | ✅ 已完成 |
| GAP-002 | `iot/building/building-visual-dashboard` | 按空间树聚合的设备数 / 在线率 / 告警计数 / 楼层热力 | `IbmsSpaceController` 缺 `/dashboard-stats` 聚合端点 | P0 | M2 → M4 |
| GAP-003 | `energy/DeviceManagement` | 表具按 `meter_type/group_code` 分组统计、批量 CRUD | `IbmsEnergyController` 已有 meter CRUD，**缺分组统计端点** | P0 | M2 → M5 |
| GAP-004 | `security/PerimeterIntrusion/VisualBoard` | 周界大屏：防区状态分布 / 24h 报警折线 / 主机在线率 | 缺 `/iot/alarm/host/dashboard` 聚合（基于 alarm_host + alarm_event） | P0 | M2 → M6 |
| GAP-005 | `security/PerimeterIntrusion/ArmingPlan` | 布防计划 CRUD / 时段 / 设备绑定 | 后端无 `ArmingPlan` controller / table | P1 | M2 → M6 |
| GAP-006 | `security/PersonnelControl/*`（7 子页） | 人员库 / 识别记录 / 捕获 / 轨迹 / 车辆识别 / 控制 / 告警 | 后端无对应 controller / table（除 IBMS 设备外） | P0/P1 | M2 → M6 |
| GAP-007 | `security/VideoSurveillance/VideoAlarmRecord` | 视频告警大屏：分类统计 + 24h 趋势 | 缺视频告警聚合端点（事件源待识别） | P0 | M2 → M6 |
| GAP-008 | `security/ElectronicPatrol/PatrolVisualizationBoard` | 电子巡更可视化：完成率/路线热力/打卡分布 | 缺 patrol 聚合端点 | P0 | M2 → M6 |
| GAP-009 | `security/VideoPatrol/PatrolPlans` | 视频巡更计划大屏 | 缺 videoPatrol 聚合端点 | P0 | M2 → M6 |
| GAP-010 | `security/VideoAnalysis/AnalysisTasks` | 视频分析任务大屏 / 算法配置 | 后端无 VideoAnalysis controller | P1 | M2 → M6 |
| GAP-011 | `iot/access/device` 单源化 | 后端 `IotAccessDeviceController` 仍混用 IotDeviceDO | 改为 `IbmsDeviceMapper` 单一数据源 + 出参换 `IbmsDeviceRespVO` | P0 | M2 |
| GAP-012 | `iot/building/lighting/device` `/alarm` | 旧照明子页 mock | `IbmsLightingController` 缺 `device 列表 + alarm 聚合`（部分已有，需对齐前端字段） | P1 | M2 → M4 |
| GAP-013 | `iot/building/newlight/*`（7 子页） | 新照明全套数据 | `IbmsLightingController` 已有底座，**新前端字段映射 / 大屏聚合端点缺** | P0/P1 | M2 → M4 |
| GAP-014 | `iot/building/env/alarm`、`iot/building/bac/alarm` | 子模块告警 mock | 已有 ibms_env_alarm / ibms_bac_alarm 表 + 部分 controller，**缺前端列表 + 详情接口对齐** | P2 | M2 → M4 |
| GAP-015 | `views/security/index.vue` 入口 | 调 `@/api/iot/device`（老 API） | 走 `IbmsDeviceController` 重构 | P1 | M6 |
| GAP-016 | `views/security/VideoSurveillance/{RealTimePreview,MultiScreenPreview,VideoPlayback}` | 视频流 + 空间分组 | 已有 ibms_channel + ibms_space，**前端切换 import 路径**即可（部分已通过 SnapshotRecord 验证） | P1 | M3/M6 |
| GAP-017 | 专项扩展表外键 | `ibms_hvac_device` / `ibms_water_device` 是否有 `ibms_device_id` | DDL 校验 + 补外键迁移 | P1 | M2 |

---

## D. 后端有而前端没用 / 富余资产（→ M3~M6 接入或评估弃用）

| 资产 | 类型 | 引用 | 处置建议 |
|---|---|---|---|
| `AccessDashboardController.{statistics,real-time,trend,device-status,heatmap,abnormal-events}` | API（6 端点） | ✅ 已被 `iot/access/visual-dashboard` 接入（commit `0b62d00`） | ~~M3 优先：直接接入~~ → 已完成（M2-A + M2-D） |
| `IbmsBacController.*` | API | 仅 `bac/monitor`、`bac/ledger` 用 | M4 让 `bac/alarm`、`bac/log` 接入 |
| `IbmsEnergyController.*` | API | `energy/Overview`、`Consumption` 等已用 | M5 让 `DeviceManagement`、`SystemSettings` 接入 |
| `IbmsEnvController.*` | API | `env/overview/sensor` 已用 | M4 让 `env/alarm`、`env/settings` 接入 |
| `IbmsLightingController.*` | API | `lighting/scene/control` 已用 | M4 让 `lighting/device/alarm`、`newlight/*` 全套接入 |
| `IbmsDeviceDiscoveryController` | API | 仅 `iot/ibms/discovery` 运维页 | 保留为运维专用，正常 |
| `IbmsBusinessMappingHelper`（groupCode/systemCode 字典） | 工具类 | 仅 AlarmHost / equipment 等少数页用 | **M3~M6 各业务页面查询时统一加 groupCode 过滤**（已写入主计划 §2.3） |
| `ibms_device.network_protocol` | 字段 | 业务页 0 引用 | 仅 `iot/ibms/device` 运维页用，保留 |
| `ibms_device_message` | 表 | 0 行 + 0 引用 | M2 评估：保留为预留扩展 / 或并入 `ibms_device_property_history` |
| `ibms_device_property_history` | 表 | 0 行 + 0 引用 | 同上 |
| `ibms_lighting_scene_circuit` | 表 | 0 行 + 0 引用 | M4 检查 `lighting/scene` 子页是否真用，否则 drop |
| `ibms_channel_bak_20260504_business`（119 行） | 表 | 仅备份 | **M2 清理**（保留 30 天后 drop） |
| `ibms_channel_bak_20260505_legacy`（30 行） | 表 | 仅备份 | 同上 |
| `ibms_device_bak_20260505_legacy`（7 行） | 表 | 仅备份 | 同上 |
| `IotSecurityOverviewService` 内 27 处 ibms_* 引用 | 服务 | `SecurityOverview/index.vue` 用 | 已 IBMS 直连，**M6 把矩阵中 🟡 标记升级为 ✅** |
| `VideoPlaybackController` | API | `VideoSurveillance/VideoPlayback` 用 | 待 M6 验证底层数据源是否走 ibms_channel |

---

## E. 阶段映射汇总

| M 阶段 | 前端任务（基于 M1 矩阵） | 后端任务（基于本报告 § C） |
|---|---|---|
| **M2** | — | GAP-002/003/004/005/006/007/008/009/010/011/017（补 dashboard 聚合端点 + access 单源化 + 专项扩展表外键校验） |
| **M3** | 21 项 access 改造 + visual-dashboard 接入 GAP-001 | — |
| **M4** | 14 项 building 改造（bac/env/lighting/newlight/visual-dashboard） | GAP-013 字段对齐 |
| **M5** | 16 项 energy 改造 | GAP-003 |
| **M6** | 33 项 security 改造（含矩阵升级 SecurityOverview 🟡→✅） | GAP-005~010、GAP-015 |

---

## F. M1.5 DoD 自检

| # | 检查项 | 状态 |
|---|---|---|
| 7 | `docs/ibms-bidirectional-gap.md` 含 §A/§B/§C/§D 四段 | ✅ |
| 8 | §C 缺口清单每行有 GAP 编号 + 优先级 + 目标 M 阶段 | ✅（17 项） |
| 9 | §D 富余资产清单每行有"处置建议" | ✅（15 项） |
| 额外 | §0 关键发现澄清"🟡聚合层 ≠ 未对接 IBMS"重要事实 | ✅ |
| 额外 | §E 双向缺口对应到 M2~M6 阶段 | ✅ |
