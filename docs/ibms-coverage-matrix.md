# IBMS 业务模块覆盖矩阵（Coverage Matrix）

> 配套主计划：`docs/ibms-unified-data-source-plan.md`
> 由 **M1 阶段** 全量填充，后续阶段按改造进度更新"对接状态"列。
> **更新规约**：
>
> - **状态枚举**：`✅IBMS直连` / `🟡聚合层` / `🟠旧iot_device` / `❌Mock` / `❓未盘点`
> - **优先级**：`P0` 客户必看 / `P1` 核心管理 / `P2` 边缘 / `P3` 仅内部
> - **改造工量**：`S` <2h / `M` 半天 / `L` 1~2 天 / `XL` >2 天

---

## 1. 智慧安防（views/security/）

| 路由 | 文件 | 状态 | 调用 API | 优先级 | 工量 | 阶段 | 备注 |
|---|---|---|---|---|---|---|---|
| `/security/video-surveillance/visual-board` | `VideoSurveillance/VisualBoard/index.vue` | ✅IBMS直连 | `getIbmsChannelPage` + ZLM 取流 | P0 | — | 已完成 | v23 已对接 |
| `/security/perimeter-intrusion/alarm-host` | `PerimeterIntrusion/AlarmHost/index.vue` | ✅IBMS直连 | `IbmsDeviceApi.getDevicePage(groupCode=SA)` | P0 | — | 已完成 | 报警主机管理 |
| `/security/security-overview` | `SecurityOverview/index.vue` | 🟡聚合层 | `@/api/iot/security-overview` | P0 | M | M6 | 底层是否查 ibms_* 待校验 |
| `/security/perimeter-intrusion/*` 其他 | `PerimeterIntrusion/*` | ❓未盘点 | — | — | — | M1 | 需扫子页 |
| `/security/electronic-patrol/*` | `ElectronicPatrol/*` | ❓未盘点 | — | — | — | M1 | — |
| `/security/e-patrol/*` | `EPatrol/*` | ❓未盘点 | — | — | — | M1 | — |
| `/security/video-analysis/*` | `VideoAnalysis/*` | ❓未盘点 | — | — | — | M1 | — |
| `/security/video-patrol/*` | `VideoPatrol/*` | ❓未盘点 | — | — | — | M1 | — |
| `/security/personnel-control/*` | `PersonnelControl/*` | ❓未盘点 | — | — | — | M1 | — |
| `/security/new-intrusion-alarm/*` | `NewIntrusionAlarm/*` | ❓未盘点 | — | — | — | M1 | — |

> **M1 待办**：递归扫描 `views/security/` 下所有 `index.vue`，逐行入表。

---

## 2. 智慧通行（views/iot/access/）

| 路由 | 文件 | 状态 | 调用 API | 优先级 | 工量 | 阶段 | 备注 |
|---|---|---|---|---|---|---|---|
| `/iot/access/device` | `device/index.vue` | 🟠旧iot_device | `AccessDeviceApi.getDevicePage` | P0 | M | M3 | 后端 service 已混用 IbmsDeviceMapper，需要单源化 |
| `/iot/access/channel` | `channel/index.vue` | 🟠旧iot_device | `AccessChannelApi.getChannelPage` | P0 | M | M3 | 同上 |
| `/smart-access/door/visual-dashboard` | `visual-dashboard/index.vue` | ❌Mock | `builtinData` 硬编码 | P0 | L | M3 | 24h 通行折线/排行全 mock |
| `/iot/access/management/*` | `management/*` | ❓未盘点 | — | — | — | M1 | — |
| `/iot/access/person` | `person/*` | ❓未盘点 | — | P1 | — | M1 | 业务自有数据，可能不需要走 IBMS |
| `/iot/access/card` | `card/*` | ❓未盘点 | — | P1 | — | M1 | — |
| `/iot/access/auth-task` | `auth-task/*` | ❓未盘点 | — | P1 | — | M1 | — |
| `/iot/access/parking/*` | `parking/*` | ❓未盘点 | — | P1 | — | M1 | 停车有独立表 `access_parking_*`，可能不需 IBMS |
| `/iot/access/dispatch/*` | `dispatch/*` | ❓未盘点 | — | — | — | M1 | — |
| `/iot/access/event` | `event/*` | ❓未盘点 | — | — | — | M1 | — |
| `/iot/access/operation-log` | `operation-log/*` | ❓未盘点 | — | — | — | M1 | — |
| `/iot/access/permission-group` | `permission-group/*` | ❓未盘点 | — | — | — | M1 | — |
| `/iot/access/department` | `department/*` | ❓未盘点 | — | — | — | M1 | — |
| `/iot/access/hr` | `hr/*` | ❓未盘点 | — | — | — | M1 | — |
| `/iot/access/map` | `map/*` | ❓未盘点 | — | — | — | M1 | — |
| `/iot/access/record` | `record/*` | ❓未盘点 | — | — | — | M1 | — |
| `/iot/access/alarm` | `alarm/*` | ❓未盘点 | — | — | — | M1 | — |
| `/iot/access/door-group` | `doorGroup/*` | ❓未盘点 | — | — | — | M1 | — |
| `/iot/access/door-post` | `doorPost/*` | ❓未盘点 | — | — | — | M1 | — |
| `/iot/access/auth-progress` | `auth-progress/*` | ❓未盘点 | — | — | — | M1 | — |
| `/iot/access/authorization` | `authorization/*` | ❓未盘点 | — | — | — | M1 | — |
| `/iot/access/device-sync` | `device-sync/*` | ❓未盘点 | — | — | — | M1 | — |
| `/iot/access/new-parking-management` | `NewParkingManagement/*` | ❓未盘点 | — | — | — | M1 | — |
| `/iot/access/new-visitor-management` | `NewVisitorManagement/*` | ❓未盘点 | — | — | — | M1 | — |

---

## 3. 智慧能源（views/energy/ + views/iot/building/energy/）

### 3.1 旧版 `views/energy/`

| 路由 | 文件 | 状态 | 调用 API | 优先级 | 工量 | 阶段 | 备注 |
|---|---|---|---|---|---|---|---|
| `/energy/overview` | `Overview/index.vue` | 🟡聚合层 | `@/api/iot/building/energy` (`IbmsEnergyOverviewVO`) | P0 | S | M5 | 数据源 `ibms_energy_statistics_daily` |
| `/energy/device-management` | `DeviceManagement/index.vue` | ❌Mock | — (0 处 API 调用) | P0 | M | M5 | **完全 mock** |
| `/energy/consumption-analysis` | `ConsumptionAnalysis/index.vue` | ❓未盘点 | — | P1 | — | M1 | — |
| `/energy/alarm-management` | `AlarmManagement/index.vue` | ❓未盘点 | — | P1 | — | M1 | — |
| `/energy/report-management` | `ReportManagement/index.vue` | ❓未盘点 | — | P1 | — | M1 | — |
| `/energy/system-settings` | `SystemSettings/index.vue` | ❓未盘点 | — | P2 | — | M1 | — |

### 3.2 新版 `views/iot/building/energy/`

| 路由 | 文件 | 状态 | 调用 API | 优先级 | 工量 | 阶段 | 备注 |
|---|---|---|---|---|---|---|---|
| `/iot/building/energy` | `index.vue` | ❓未盘点 | — | P0 | — | M1 | 入口页 |
| `/iot/building/energy/equipment` | `equipment/index.vue` | ✅IBMS直连 | `EnergyApi` + `getIbmsDevicePage` | P0 | — | 已完成 | **唯一已对接 IBMS 的能源页** |
| `/iot/building/energy/alarm` | `alarm/index.vue` | ❓未盘点 | — | P1 | — | M1 | — |
| `/iot/building/energy/analysis/period` | `analysis/period/index.vue` | ❓未盘点 | — | P1 | — | M1 | — |
| `/iot/building/energy/analysis/trend` | `analysis/trend/index.vue` | ❓未盘点 | — | P1 | — | M1 | — |
| `/iot/building/energy/ledger` | `ledger/index.vue` | ❓未盘点 | — | P1 | — | M1 | — |
| `/iot/building/energy/manual-reading` | `manual-reading/index.vue` | ❓未盘点 | — | P1 | — | M1 | — |
| `/iot/building/energy/monitor` | `monitor/index.vue` | ❓未盘点 | — | P0 | — | M1 | — |

> **M5 决策点 D-001**：保留哪一套？产品决策后大批量改造。

---

## 4. 智慧建筑（views/iot/building/）

| 路由 | 文件 | 状态 | 调用 API | 优先级 | 工量 | 阶段 | 备注 |
|---|---|---|---|---|---|---|---|
| `/iot/building/visual-dashboard` | `building-visual-dashboard/index.vue` | ❌Mock | — (0 处 API 调用) | P0 | L | M4 | **完全 mock**，需要重写 |
| `/building/visual-dashboard` | `building-visual-dashboard/index.vue` | ❌Mock | 同上 | P0 | — | M4 | 路由别名 |
| `/iot/building/bac/*` | `bac/*` | ❓未盘点 | — | P0 | — | M1 | 楼宇自控（核心） |
| `/iot/building/env/*` | `env/*` | ❓未盘点 | — | P1 | — | M1 | 环境监测 |
| `/iot/building/lighting/*` | `lighting/*` | ❓未盘点 | — | P1 | — | M1 | 旧照明 |
| `/iot/building/newlight/*` | `newlight/*` | ❓未盘点 | — | P1 | — | M1 | 新照明（overview/control/device/task/log/alarm/circuit 7 子页） |

---

## 5. 智慧物联（基础模块，参考用）

| 路由 | 文件 | 状态 | 备注 |
|---|---|---|---|
| `/iot/ibms/device` | `iot/ibms/device/*` | ✅基础源头 | `ibms_device` CRUD |
| `/iot/ibms/channel` | `iot/ibms/channel/*` | ✅基础源头 | `ibms_channel` CRUD |
| `/iot/ibms/space` | `iot/ibms/space/*` | ✅基础源头 | `ibms_space` 树 |
| `/iot/ibms/product` | `iot/ibms/product/*` | ✅基础源头 | `ibms_product` |
| `/iot/ibms/dict` | `iot/ibms/dict/*` | ✅基础源头 | `ibms_dict` |

---

## 6. M1 盘点扫描脚本（参考）

```powershell
# 在 e:\ch 根目录执行
Get-ChildItem yudao-ui-admin-vue3\src\views\security, yudao-ui-admin-vue3\src\views\iot\access, yudao-ui-admin-vue3\src\views\iot\building, yudao-ui-admin-vue3\src\views\energy -Recurse -Filter index.vue | ForEach-Object {
  $f = $_.FullName
  $hasIbms = (Select-String -Path $f -Pattern "@/api/iot/ibms" -SimpleMatch -ErrorAction SilentlyContinue).Count
  $hasAccess = (Select-String -Path $f -Pattern "@/api/iot/access" -SimpleMatch -ErrorAction SilentlyContinue).Count
  $hasBuilding = (Select-String -Path $f -Pattern "@/api/iot/building" -SimpleMatch -ErrorAction SilentlyContinue).Count
  $hasMock = (Select-String -Path $f -Pattern "builtinData|mockData|fakeData" -SimpleMatch -ErrorAction SilentlyContinue).Count
  $apiCount = (Select-String -Path $f -Pattern "from '@/api" -SimpleMatch -ErrorAction SilentlyContinue).Count
  [PSCustomObject]@{
    File = $f.Substring(28)
    IBMS = $hasIbms
    Access = $hasAccess
    Building = $hasBuilding
    Mock = $hasMock
    AnyApi = $apiCount
  }
} | Sort-Object File | Format-Table -AutoSize
```

执行结果导入本表对应模块表格的 **状态/调用 API** 两列。

---

## 7. 完成度统计（自动 / 手动）

| 模块 | 总页面数 | ✅IBMS直连 | 🟡聚合层 | 🟠旧iot_device | ❌Mock | ❓未盘点 |
|---|---|---|---|---|---|---|
| 智慧安防 | 待 M1 统计 | 2 | 1 | 0 | 0 | 7+ |
| 智慧通行 | 待 M1 统计 | 0 | 0 | 2 | 1 | 22+ |
| 智慧能源 | 待 M1 统计 | 1 | 1 | 0 | 1 | 11+ |
| 智慧建筑 | 待 M1 统计 | 0 | 0 | 0 | 1 | 多 |
| **合计** | — | **3** | **2** | **2** | **3** | **40+** |

> 当 ❓未盘点 = 0 时，M1 阶段视为完成。

