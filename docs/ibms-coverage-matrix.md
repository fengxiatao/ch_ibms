# IBMS 业务模块覆盖矩阵（Coverage Matrix）

> 配套主计划：`docs/ibms-unified-data-source-plan.md`
> M1 阶段（2026-05-06）已完成 109 个 `index.vue` 全量盘点；后续阶段按改造进度更新"对接状态"列。
>
> **状态枚举**：
>
> - ✅IBMS直连 — 已使用 `@/api/iot/ibms/*`
> - 🟡聚合层 — 走 `@/api/iot/building/*` 或 `@/api/iot/security-overview`，底层是否查 ibms_* 待 M2 校验
> - 🟠旧access — 仍调 `@/api/iot/access/*` 设备/通道接口，需 M3 替换为 IBMS
> - 🟤旧API — 调 `@/api/iot/{spatial,device,video,patrol,alarm,videoPatrol,patrolplan}` 等老接口，需要按目标 M 阶段改造或评估弃用
> - ❌Mock/硬编码 — 0 处 API 调用 + 行数 ≥300（推断为可视化大屏 / 假数据展示页）
> - ⚪路由壳 — 0 处 API 调用 + 行数 <300（路由占位 / 子组件包装入口）
> - 🔵混合 — 同时调 ibms 与 access
> - 🟢业务自有 — `iot/access/parking/*`，停车独立表，合理使用 access API
>
> **优先级**：`P0` 客户必看 / `P1` 核心管理 / `P2` 边缘
>
> **改造工量**：`S` <200 行 / `M` <500 / `L` <1000 / `XL` ≥1000

---

## 1. 智慧安防（views/security/）

| 路由/文件 | 状态 | 调用 API（前 70 字符） | 优先级 | 工量 | 阶段 |
|---|---|---|---|---|---|
| `security/ElectronicPatrol/PatrolManagement/index.vue` | ❌Mock/硬编码 | — | P0 | XL | M6 |
| `security/ElectronicPatrol/PatrolPlanManagement/index.vue` | 🟤旧API | `@/api/iot/patrol , @/api/iot/patrol` | P0 | M | M6 |
| `security/ElectronicPatrol/PatrolPointManagement/index.vue` | 🟤旧API | `@/api/iot/patrol , @/api/iot/patrol` | P0 | M | M6 |
| `security/ElectronicPatrol/PatrolPoints/index.vue` | 🟤旧API | `@/api/iot/spatial/campus , @/api/iot/spatial/building , @/api/iot/s...` | P2 | M | M6 |
| `security/ElectronicPatrol/PatrolRecordQuery/index.vue` | 🟤旧API | `@/api/iot/patrol , @/api/iot/patrol` | P2 | M | M6 |
| `security/ElectronicPatrol/PatrolRouteManagement/index.vue` | 🟤旧API | `@/api/iot/patrol , @/api/iot/patrol` | P0 | M | M6 |
| `security/ElectronicPatrol/PatrolRoutes/index.vue` | ❌Mock/硬编码 | — | P0 | XL | M6 |
| `security/ElectronicPatrol/PatrolVisualizationBoard/index.vue` | ❌Mock/硬编码 | — | P0 | XL | M6 |
| `security/EPatrol/Person/index.vue` | 🟤旧API | `@/api/iot/epatrol` | P2 | M | M6 |
| `security/EPatrol/Plan/index.vue` | 🟤旧API | `@/api/iot/epatrol` | P1 | L | M6 |
| `security/EPatrol/Point/index.vue` | 🟤旧API | `@/api/iot/epatrol` | P2 | M | M6 |
| `security/EPatrol/Route/index.vue` | 🟤旧API | `@/api/iot/epatrol` | P2 | M | M6 |
| `security/EPatrol/StickData/index.vue` | 🟤旧API | `@/api/iot/epatrol` | P2 | M | M6 |
| `security/EPatrol/Task/index.vue` | 🟤旧API | `@/api/iot/epatrol` | P1 | L | M6 |
| `security/index.vue` | 🟤旧API | `@/api/iot/device` | P1 | XL | M6 |
| `security/NewIntrusionAlarm/index.vue` | ⚪路由壳 | — | — | S | — |
| `security/PerimeterIntrusion/AlarmHost/index.vue` | ✅IBMS直连 | `@/api/iot/alarm/host , @/api/iot/ibms/device , @/api/iot/ibms/channel` | — | XL | — |
| `security/PerimeterIntrusion/AlarmOperationLog/index.vue` | 🟤旧API | `@/api/iot/alarm/operationLog , @/api/iot/alarm/host` | P2 | M | M6 |
| `security/PerimeterIntrusion/ArmingPlan/index.vue` | ❌Mock/硬编码 | — | P1 | L | M6 |
| `security/PerimeterIntrusion/PerimeterAlarmRecord/index.vue` | 🟤旧API | `@/api/iot/alarm/event , @/api/iot/alarm/host` | P1 | L | M6 |
| `security/PerimeterIntrusion/VisualBoard/index.vue` | ❌Mock/硬编码 | — | P0 | XL | M6 |
| `security/PerimeterIntrusion/ZoneManagement/index.vue` | 🟤旧API | `@/api/iot/alarm/host` | P0 | M | M6 |
| `security/PersonnelControl/ControlAlarmRecord/index.vue` | ❌Mock/硬编码 | — | P1 | L | M6 |
| `security/PersonnelControl/PersonnelCapture/index.vue` | ❌Mock/硬编码 | — | P1 | L | M6 |
| `security/PersonnelControl/PersonnelControl/index.vue` | ❌Mock/硬编码 | — | P1 | L | M6 |
| `security/PersonnelControl/PersonnelLibrary/index.vue` | ❌Mock/硬编码 | — | P1 | L | M6 |
| `security/PersonnelControl/PersonnelRecognition/index.vue` | ❌Mock/硬编码 | — | P1 | L | M6 |
| `security/PersonnelControl/PersonnelTrack/index.vue` | ❌Mock/硬编码 | — | P0 | L | M6 |
| `security/PersonnelControl/VehicleCapture/index.vue` | ❌Mock/硬编码 | — | P1 | L | M6 |
| `security/SecurityOverview/index.vue` | 🟡聚合层 | `@/api/iot/security-overview` | P0 | XL | M6 |
| `security/VideoAnalysis/AnalysisTasks/index.vue` | ❌Mock/硬编码 | — | P1 | L | M6 |
| `security/VideoAnalysis/EquipmentConfig/index.vue` | ⚪路由壳 | — | — | S | — |
| `security/VideoAnalysis/TerminalManagement/index.vue` | ⚪路由壳 | — | P0 | S | — |
| `security/VideoAnalysis/VideoAnalysisAlarm/index.vue` | ⚪路由壳 | — | — | S | — |
| `security/VideoPatrol/PatrolPlans/index.vue` | ❌Mock/硬编码 | — | P0 | XL | M6 |
| `security/VideoPatrol/PatrolPointManagement/index.vue` | 🟤旧API | `@/api/iot/videoPatrol , @/api/iot/videoPatrol` | P0 | M | M6 |
| `security/VideoPatrol/PatrolRecordQuery/index.vue` | 🟤旧API | `@/api/iot/videoPatrol , @/api/iot/videoPatrol` | P2 | M | M6 |
| `security/VideoPatrol/PatrolTask/index.vue` | 🟤旧API | `@/api/iot/device/device , @/api/iot/spatial/building , @/api/iot/sp...` | P1 | XL | M6 |
| `security/VideoPatrol/PatrolTaskManagement/index.vue` | 🟤旧API | `@/api/iot/videoPatrol , @/api/iot/videoPatrol , @/api/system/user` | P0 | L | M6 |
| `security/VideoPatrol/PatrolTasks/index.vue` | 🟤旧API | `@/api/iot/videoPatrol , @/api/iot/videoPatrol` | P2 | M | M6 |
| `security/VideoSurveillance/MultiScreenPreview/index.vue` | 🟤旧API | `@/api/iot/spatial/building , @/api/iot/spatial/floor , @/api/iot/sp...` | P1 | L | M6 |
| `security/VideoSurveillance/PatrolConfig/index.vue` | 🟤旧API | `@/api/iot/patrolplan , @/api/iot/patrolplan` | P2 | M | M6 |
| `security/VideoSurveillance/PatrolSchedule/index.vue` | 🟤旧API | `@/api/iot/video/patrolSchedule` | P2 | M | M6 |
| `security/VideoSurveillance/RealTimePreview/index.vue` | 🟤旧API | `@/api/iot/video , @/api/iot/video/nvr , @/api/iot/video/videoView` | P1 | XL | M6 |
| `security/VideoSurveillance/SnapshotRecord/index.vue` | ✅IBMS直连 | `@/api/iot/video , @/api/iot/ibms/space , @/api/iot/ibms/channel` | — | XL | — |
| `security/VideoSurveillance/VideoAlarmRecord/index.vue` | ❌Mock/硬编码 | — | P0 | XL | M6 |
| `security/VideoSurveillance/VideoPlayback/index.vue` | 🟤旧API | `@/api/security/playback` | P1 | XL | M6 |
| `security/VideoSurveillance/VisualBoard/index.vue` | ✅IBMS直连 | `@/api/iot/video/videoView , @/api/iot/security-overview , @/api/iot...` | P0 | XL | — |

> 小计（48 项）：AGG=1 / IBMS=3 / LEGACY=25 / MOCK=15 / SHELL=4

---

## 2. 智慧通行（views/iot/access/）

| 路由/文件 | 状态 | 调用 API（前 70 字符） | 优先级 | 工量 | 阶段 |
|---|---|---|---|---|---|
| `iot/access/alarm/index.vue` | 🟠旧access | `@/api/iot/access/alarm , @/api/iot/device/device` | P2 | M | M3 |
| `iot/access/authorization/index.vue` | 🟠旧access | `@/api/iot/access/authorization , @/api/system/dept , @/api/iot/devi...` | P1 | L | M3 |
| `iot/access/auth-progress/index.vue` | 🟠旧access | `@/api/iot/access` | P2 | M | M3 |
| `iot/access/auth-task/index.vue` | 🟠旧access | `@/api/iot/access` | P2 | M | M3 |
| `iot/access/card/index.vue` | 🟠旧access | `@/api/iot/device/device , @/api/iot/spatial/area , @/api/iot/access...` | P1 | L | M3 |
| `iot/access/channel/index.vue` | 🟠旧access | `@/api/iot/access` | P0 | S | M3 |
| `iot/access/department/index.vue` | 🟠旧access | `@/api/iot/access` | P2 | M | M3 |
| `iot/access/device/index.vue` | 🟠旧access | `@/api/iot/access` | P0 | M | M3 |
| `iot/access/device-sync/index.vue` | 🟠旧access | `@/api/iot/access` | P1 | L | M3 |
| `iot/access/dispatch/index.vue` | 🟠旧access | `@/api/iot/access/dispatch , @/api/iot/device/device` | P2 | M | M3 |
| `iot/access/doorGroup/index.vue` | 🟠旧access | `@/api/iot/access/doorGroup` | P2 | M | M3 |
| `iot/access/doorPost/index.vue` | 🟠旧access | `@/api/iot/access/doorPost` | P2 | M | M3 |
| `iot/access/event/index.vue` | 🟠旧access | `@/api/iot/access` | P1 | L | M3 |
| `iot/access/hr/index.vue` | 🟠旧access | `@/api/iot/access` | P2 | M | M3 |
| `iot/access/management/index.vue` | 🟠旧access | `@/api/iot/access` | P0 | L | M3 |
| `iot/access/map/index.vue` | 🟠旧access | `@/api/iot/spatial/floor , @/api/iot/device/device , @/api/iot/acces...` | P1 | L | M3 |
| `iot/access/operation-log/index.vue` | 🟠旧access | `@/api/iot/access` | P2 | S | M3 |
| `iot/access/permission-group/index.vue` | 🟠旧access | `@/api/iot/access` | P1 | L | M3 |
| `iot/access/person/index.vue` | 🟠旧access | `@/api/iot/access` | P1 | L | M3 |
| `iot/access/record/index.vue` | 🟠旧access | `@/api/iot/access/record , @/api/iot/device/device` | P2 | M | M3 |
| `iot/access/test/index.vue` | 🟠旧access | `@/api/iot/access` | P1 | XL | M3 |
| `iot/access/visual-dashboard/index.vue` | 🔵混合 | `@/api/iot/access/dashboard , @/api/iot/access/record` | P0 | L | M3 |

> 小计（22 项）：ACCESS=21 / MIX=1（M2-A：visual-dashboard 升级 ❌→🔵）

---

## 3. 智慧能源（views/energy/ + views/iot/building/energy/）

| 路由/文件 | 状态 | 调用 API（前 70 字符） | 优先级 | 工量 | 阶段 |
|---|---|---|---|---|---|
| `energy/AlarmManagement/index.vue` | 🟡聚合层 | `@/api/iot/building/energy , @/api/iot/building/energy` | P0 | M | M5 |
| `energy/ConsumptionAnalysis/index.vue` | 🟡聚合层 | `@/api/iot/building/energy , @/api/iot/building/energy` | P1 | L | M5 |
| `energy/DeviceManagement/index.vue` | ❌Mock/硬编码 | — | P0 | L | M5 |
| `energy/index.vue` | ⚪路由壳 | — | — | S | — |
| `energy/Overview/index.vue` | 🟡聚合层 | `@/api/iot/building/energy , @/api/iot/building/energy` | P0 | L | M5 |
| `energy/ReportManagement/index.vue` | 🟡聚合层 | `@/api/iot/building/energy` | P0 | M | M5 |
| `energy/SystemSettings/index.vue` | ❌Mock/硬编码 | — | P0 | XL | M5 |
| `iot/building/energy/alarm/index.vue` | 🟡聚合层 | `@/api/iot/building/energy , @/api/iot/building/energy` | P2 | M | M5 |
| `iot/building/energy/analysis/period/index.vue` | ❌Mock/硬编码 | — | P1 | L | M5 |
| `iot/building/energy/analysis/trend/index.vue` | 🟡聚合层 | `@/api/iot/building/energy` | P1 | L | M5 |
| `iot/building/energy/equipment/index.vue` | ✅IBMS直连 | `@/api/iot/building/energy , @/api/iot/building/energy , @/api/iot/i...` | — | L | — |
| `iot/building/energy/index.vue` | 🟡聚合层 | `@/api/iot/building/energy , @/api/iot/building/energy` | P2 | M | M5 |
| `iot/building/energy/ledger/index.vue` | 🟡聚合层 | `@/api/iot/building/energy , @/api/iot/building/energy` | P2 | M | M5 |
| `iot/building/energy/manual-reading/index.vue` | 🟡聚合层 | `@/api/iot/building/energy , @/api/iot/building/energy` | P1 | L | M5 |
| `iot/building/energy/monitor/index.vue` | 🟡聚合层 | `@/api/iot/building/energy , @/api/iot/building/energy` | P1 | L | M5 |
| `iot/building/energy/remote-reading/index.vue` | 🟡聚合层 | `@/api/iot/building/energy , @/api/iot/building/energy` | P1 | L | M5 |
| `iot/building/energy/report/index.vue` | 🟡聚合层 | `@/api/iot/building/energy` | P1 | L | M5 |
| `iot/building/energy/settings/index.vue` | 🟡聚合层 | `@/api/iot/building/energy , @/api/iot/building/energy` | P1 | L | M5 |

> 小计（18 项）：AGG=13 / IBMS=1 / MOCK=3 / SHELL=1

---

## 4. 智慧建筑（views/iot/building/，不含 energy 子目录）

| 路由/文件 | 状态 | 调用 API（前 70 字符） | 优先级 | 工量 | 阶段 |
|---|---|---|---|---|---|
| `iot/building/bac/alarm/index.vue` | ❌Mock/硬编码 | — | P2 | M | M4 |
| `iot/building/bac/ledger/index.vue` | 🟡聚合层 | `@/api/iot/building/bac` | P2 | M | M4 |
| `iot/building/bac/log/index.vue` | ⚪路由壳 | — | — | M | — |
| `iot/building/bac/monitor/index.vue` | 🟡聚合层 | `@/api/iot/building/bac` | P2 | M | M4 |
| `iot/building/building-visual-dashboard/index.vue` | ❌Mock/硬编码 | — | P0 | XL | M4 |
| `iot/building/env/alarm/index.vue` | ❌Mock/硬编码 | — | P2 | M | M4 |
| `iot/building/env/overview/index.vue` | 🟡聚合层 | `@/api/iot/building/env` | P0 | XL | M4 |
| `iot/building/env/sensor/index.vue` | 🟡聚合层 | `@/api/iot/building/env` | P2 | M | M4 |
| `iot/building/env/settings/index.vue` | ⚪路由壳 | — | — | M | — |
| `iot/building/lighting/alarm/index.vue` | ❌Mock/硬编码 | — | P2 | M | M4 |
| `iot/building/lighting/control/index.vue` | 🟡聚合层 | `@/api/iot/building/lighting` | P2 | M | M4 |
| `iot/building/lighting/device/index.vue` | ❌Mock/硬编码 | — | P0 | M | M4 |
| `iot/building/lighting/log/index.vue` | ⚪路由壳 | — | — | M | — |
| `iot/building/lighting/scene/index.vue` | 🟡聚合层 | `@/api/iot/building/lighting` | P2 | M | M4 |
| `iot/building/newlight/alarm/index.vue` | ⚪路由壳 | — | — | S | — |
| `iot/building/newlight/circuit/index.vue` | ⚪路由壳 | — | — | S | — |
| `iot/building/newlight/control/index.vue` | ❌Mock/硬编码 | — | P1 | L | M4 |
| `iot/building/newlight/device/index.vue` | ❌Mock/硬编码 | — | P0 | XL | M4 |
| `iot/building/newlight/log/index.vue` | ⚪路由壳 | — | — | S | — |
| `iot/building/newlight/overview/index.vue` | ❌Mock/硬编码 | — | P0 | L | M4 |
| `iot/building/newlight/task/index.vue` | ⚪路由壳 | — | — | S | — |

> 小计（21 项）：AGG=6 / MOCK=8 / SHELL=7

---

## 5. 智慧物联（基础模块，参考用，未入 M1 扫描）

| 路由 | 文件 | 状态 | 备注 |
|---|---|---|---|
| `/iot/ibms/device` | `iot/ibms/device/*` | ✅基础源头 | `ibms_device` CRUD |
| `/iot/ibms/channel` | `iot/ibms/channel/*` | ✅基础源头 | `ibms_channel` CRUD |
| `/iot/ibms/space` | `iot/ibms/space/*` | ✅基础源头 | `ibms_space` 树 |
| `/iot/ibms/product` | `iot/ibms/product/*` | ✅基础源头 | `ibms_product` |
| `/iot/ibms/dict` | `iot/ibms/dict/*` | ✅基础源头 | `ibms_dict` |

---

## 6. M1 扫描脚本与产出（可重复执行）

- 扫描脚本：`.tmp_sql/m1-scan.ps1`（提取 7 个特征 → CSV）
- 分类脚本：`.tmp_sql/m1-classify-v2.ps1`（自动判定状态 + 工量分档）
- 矩阵生成：`.tmp_sql/m1-gen-matrix.ps1`
- 原始 CSV：`.tmp_sql/m1-scan-result.csv`、`.tmp_sql/m1-classified.csv`

判定规则（v2）：

```text
AnyApi=0 AND (Mock关键字>0 OR Lines>=300)  -> ❌Mock/硬编码
AnyApi=0 AND Lines<300                     -> ⚪路由壳
IBMS>0 AND ACC=0                           -> ✅IBMS直连
ACC>0 AND IBMS=0                           -> 🟠旧access  (parking/* -> 🟢业务自有)
IBMS>0 AND ACC>0                           -> 🔵混合
BLD>0 AND IBMS=0 AND ACC=0                 -> 🟡聚合层
其余 AnyApi>0                              -> 🟤旧API（spatial/device/video/patrol/alarm 等）
```

---

## 7. 完成度统计

| 模块 | 总页面 | IBMS | 聚合 | 旧access | 旧API | Mock | 路由壳 |
|---|---|---|---|---|---|---|---|
| security | 48 | 3 | 1 | 0 | 25 | 15 | 4 |
| iot/access | 22 | 0 | 0 | 21 | 0 | 0 | 0 |
| energy（含 iot/building/energy/） | 18 | 1 | 13 | 0 | 0 | 3 | 1 |
| iot/building（不含 energy） | 21 | 0 | 6 | 0 | 0 | 8 | 7 |
| **合计** | **109** | **4** | **20** | **21** | **25** | **26** | **12** |

> ❓未盘点 = 0，M1 阶段视为完成。
> M2-A 后新增"🔵混合"= 1（`iot/access/visual-dashboard`），统计未单列；可在 M2 收尾后补"混合"列。

---

## 8. M1 关键页面人工复核记录（2026-05-06）

| # | 文件 | 扫描结论 | 人工复核 | 备注 |
|---|---|---|---|---|
| 1 | `iot/building/building-visual-dashboard/index.vue` | ❌Mock/硬编码（1087 行 / 0 API） | ✅一致 | 仅引用 `echarts` + `app store`，确为大屏硬编码假数据，M4 重写 |
| 2 | `iot/access/visual-dashboard/index.vue` | ❌Mock（1338 行 / 0 API / `builtinData`×3） | ✅一致 | 仅 `echarts`/`dayjs`，今日通行/折线全 mock，M3 重写 |
| 3 | `security/SecurityOverview/index.vue` | 🟡聚合层（1438 行） | ✅一致 | 调 `@/api/iot/security-overview` + `useIotWebSocket`，**底层 controller 是否查 ibms_* 待 M2 校验** |
| 4 | `iot/access/management/index.vue` | 🟠旧access（896 行） | ✅一致 | 调 `@/api/iot/access` + `useAccessDeviceStatusWebSocket`，M3 单源化 |
| 5 | `iot/building/bac/monitor/index.vue` | 🟡聚合层（445 行） | ✅一致 | 调 `@/api/iot/building/bac`，M4 校验底层是否走 ibms_* |

> 5 项扫描结论与人工复核 100% 一致，扫描判定规则可信。

---

## 9. M1 差距清单（按优先级分档，共 93 项待改造）

### P0 （28 项）

| 文件 | 状态 | 工量 | 阶段 |
|---|---|---|---|
| `energy/AlarmManagement/index.vue` | 🟡 AGG | M | M5 |
| `energy/DeviceManagement/index.vue` | ❌ MOCK | L | M5 |
| `energy/Overview/index.vue` | 🟡 AGG | L | M5 |
| `energy/ReportManagement/index.vue` | 🟡 AGG | M | M5 |
| `energy/SystemSettings/index.vue` | ❌ MOCK | XL | M5 |
| `iot/access/channel/index.vue` | 🟠 ACCESS | S | M3 |
| `iot/access/device/index.vue` | 🟠 ACCESS | M | M3 |
| `iot/access/management/index.vue` | 🟠 ACCESS | L | M3 |
| `iot/access/visual-dashboard/index.vue` | ❌ MOCK | XL | M3 |
| `iot/building/building-visual-dashboard/index.vue` | ❌ MOCK | XL | M4 |
| `iot/building/env/overview/index.vue` | 🟡 AGG | XL | M4 |
| `iot/building/lighting/device/index.vue` | ❌ MOCK | M | M4 |
| `iot/building/newlight/device/index.vue` | ❌ MOCK | XL | M4 |
| `iot/building/newlight/overview/index.vue` | ❌ MOCK | L | M4 |
| `security/ElectronicPatrol/PatrolManagement/index.vue` | ❌ MOCK | XL | M6 |
| `security/ElectronicPatrol/PatrolPlanManagement/index.vue` | 🟤 LEGACY | M | M6 |
| `security/ElectronicPatrol/PatrolPointManagement/index.vue` | 🟤 LEGACY | M | M6 |
| `security/ElectronicPatrol/PatrolRouteManagement/index.vue` | 🟤 LEGACY | M | M6 |
| `security/ElectronicPatrol/PatrolRoutes/index.vue` | ❌ MOCK | XL | M6 |
| `security/ElectronicPatrol/PatrolVisualizationBoard/index.vue` | ❌ MOCK | XL | M6 |
| `security/PerimeterIntrusion/VisualBoard/index.vue` | ❌ MOCK | XL | M6 |
| `security/PerimeterIntrusion/ZoneManagement/index.vue` | 🟤 LEGACY | M | M6 |
| `security/PersonnelControl/PersonnelTrack/index.vue` | ❌ MOCK | L | M6 |
| `security/SecurityOverview/index.vue` | 🟡 AGG | XL | M6 |
| `security/VideoPatrol/PatrolPlans/index.vue` | ❌ MOCK | XL | M6 |
| `security/VideoPatrol/PatrolPointManagement/index.vue` | 🟤 LEGACY | M | M6 |
| `security/VideoPatrol/PatrolTaskManagement/index.vue` | 🟤 LEGACY | L | M6 |
| `security/VideoSurveillance/VideoAlarmRecord/index.vue` | ❌ MOCK | XL | M6 |

### P1 （33 项）

| 文件 | 状态 | 工量 | 阶段 |
|---|---|---|---|
| `energy/ConsumptionAnalysis/index.vue` | 🟡 AGG | L | M5 |
| `iot/access/authorization/index.vue` | 🟠 ACCESS | L | M3 |
| `iot/access/card/index.vue` | 🟠 ACCESS | L | M3 |
| `iot/access/device-sync/index.vue` | 🟠 ACCESS | L | M3 |
| `iot/access/event/index.vue` | 🟠 ACCESS | L | M3 |
| `iot/access/map/index.vue` | 🟠 ACCESS | L | M3 |
| `iot/access/permission-group/index.vue` | 🟠 ACCESS | L | M3 |
| `iot/access/person/index.vue` | 🟠 ACCESS | L | M3 |
| `iot/access/test/index.vue` | 🟠 ACCESS | XL | M3 |
| `iot/building/energy/analysis/period/index.vue` | ❌ MOCK | L | M5 |
| `iot/building/energy/analysis/trend/index.vue` | 🟡 AGG | L | M5 |
| `iot/building/energy/manual-reading/index.vue` | 🟡 AGG | L | M5 |
| `iot/building/energy/monitor/index.vue` | 🟡 AGG | L | M5 |
| `iot/building/energy/remote-reading/index.vue` | 🟡 AGG | L | M5 |
| `iot/building/energy/report/index.vue` | 🟡 AGG | L | M5 |
| `iot/building/energy/settings/index.vue` | 🟡 AGG | L | M5 |
| `iot/building/newlight/control/index.vue` | ❌ MOCK | L | M4 |
| `security/EPatrol/Plan/index.vue` | 🟤 LEGACY | L | M6 |
| `security/EPatrol/Task/index.vue` | 🟤 LEGACY | L | M6 |
| `security/index.vue` | 🟤 LEGACY | XL | M6 |
| `security/PerimeterIntrusion/ArmingPlan/index.vue` | ❌ MOCK | L | M6 |
| `security/PerimeterIntrusion/PerimeterAlarmRecord/index.vue` | 🟤 LEGACY | L | M6 |
| `security/PersonnelControl/ControlAlarmRecord/index.vue` | ❌ MOCK | L | M6 |
| `security/PersonnelControl/PersonnelCapture/index.vue` | ❌ MOCK | L | M6 |
| `security/PersonnelControl/PersonnelControl/index.vue` | ❌ MOCK | L | M6 |
| `security/PersonnelControl/PersonnelLibrary/index.vue` | ❌ MOCK | L | M6 |
| `security/PersonnelControl/PersonnelRecognition/index.vue` | ❌ MOCK | L | M6 |
| `security/PersonnelControl/VehicleCapture/index.vue` | ❌ MOCK | L | M6 |
| `security/VideoAnalysis/AnalysisTasks/index.vue` | ❌ MOCK | L | M6 |
| `security/VideoPatrol/PatrolTask/index.vue` | 🟤 LEGACY | XL | M6 |
| `security/VideoSurveillance/MultiScreenPreview/index.vue` | 🟤 LEGACY | L | M6 |
| `security/VideoSurveillance/RealTimePreview/index.vue` | 🟤 LEGACY | XL | M6 |
| `security/VideoSurveillance/VideoPlayback/index.vue` | 🟤 LEGACY | XL | M6 |

### P2 （32 项）

| 文件 | 状态 | 工量 | 阶段 |
|---|---|---|---|
| `iot/access/alarm/index.vue` | 🟠 ACCESS | M | M3 |
| `iot/access/auth-progress/index.vue` | 🟠 ACCESS | M | M3 |
| `iot/access/auth-task/index.vue` | 🟠 ACCESS | M | M3 |
| `iot/access/department/index.vue` | 🟠 ACCESS | M | M3 |
| `iot/access/dispatch/index.vue` | 🟠 ACCESS | M | M3 |
| `iot/access/doorGroup/index.vue` | 🟠 ACCESS | M | M3 |
| `iot/access/doorPost/index.vue` | 🟠 ACCESS | M | M3 |
| `iot/access/hr/index.vue` | 🟠 ACCESS | M | M3 |
| `iot/access/operation-log/index.vue` | 🟠 ACCESS | S | M3 |
| `iot/access/record/index.vue` | 🟠 ACCESS | M | M3 |
| `iot/building/bac/alarm/index.vue` | ❌ MOCK | M | M4 |
| `iot/building/bac/ledger/index.vue` | 🟡 AGG | M | M4 |
| `iot/building/bac/monitor/index.vue` | 🟡 AGG | M | M4 |
| `iot/building/energy/alarm/index.vue` | 🟡 AGG | M | M5 |
| `iot/building/energy/index.vue` | 🟡 AGG | M | M5 |
| `iot/building/energy/ledger/index.vue` | 🟡 AGG | M | M5 |
| `iot/building/env/alarm/index.vue` | ❌ MOCK | M | M4 |
| `iot/building/env/sensor/index.vue` | 🟡 AGG | M | M4 |
| `iot/building/lighting/alarm/index.vue` | ❌ MOCK | M | M4 |
| `iot/building/lighting/control/index.vue` | 🟡 AGG | M | M4 |
| `iot/building/lighting/scene/index.vue` | 🟡 AGG | M | M4 |
| `security/ElectronicPatrol/PatrolPoints/index.vue` | 🟤 LEGACY | M | M6 |
| `security/ElectronicPatrol/PatrolRecordQuery/index.vue` | 🟤 LEGACY | M | M6 |
| `security/EPatrol/Person/index.vue` | 🟤 LEGACY | M | M6 |
| `security/EPatrol/Point/index.vue` | 🟤 LEGACY | M | M6 |
| `security/EPatrol/Route/index.vue` | 🟤 LEGACY | M | M6 |
| `security/EPatrol/StickData/index.vue` | 🟤 LEGACY | M | M6 |
| `security/PerimeterIntrusion/AlarmOperationLog/index.vue` | 🟤 LEGACY | M | M6 |
| `security/VideoPatrol/PatrolRecordQuery/index.vue` | 🟤 LEGACY | M | M6 |
| `security/VideoPatrol/PatrolTasks/index.vue` | 🟤 LEGACY | M | M6 |
| `security/VideoSurveillance/PatrolConfig/index.vue` | 🟤 LEGACY | M | M6 |
| `security/VideoSurveillance/PatrolSchedule/index.vue` | 🟤 LEGACY | M | M6 |

---

## 10. 阶段路线（M1 差距清单 → 按阶段汇总）

| 阶段 | 改造项 | 主要状态分布 |
|---|---|---|
| M3 | 22 | ACCESS=21 / MOCK=1 |
| M4 | 14 | AGG=6 / MOCK=8 |
| M5 | 16 | AGG=13 / MOCK=3 |
| M6 | 41 | AGG=1 / LEGACY=25 / MOCK=15 |
