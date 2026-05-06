# IBMS 统一数据源化治理计划（IBMS Unified Data Source Plan）

> 本文档是跨会话主计划。任何新会话承接 **必读** 本文件 + `docs/ibms-unified-progress.md`。
> 阶段交付物会更新到 `docs/ibms-coverage-matrix.md` 与 `docs/ibms-unified-progress.md`。

---

## 1. 终极目标（North Star）

**一句话**：让长辉 IBMS 4 个业务模块（智慧安防、智慧通行、智慧能源、智慧建筑）所展示的**所有设备、空间、通道、状态、运行态**都来源于"智慧物联（IBMS 基础模块）"的运维配置。最终：

1. 公司实施人员只需在"智慧物联"模块完成全套配置，4 个业务模块即可正常运行
2. 业务模块对客户开放、智慧物联模块对客户隐藏（菜单级或权限级）
3. 客户运行期 0 配置，业务模块即开即用

---

## 2. 现状基线（2026-05-06 调研结果）

### 2.1 后端

- ✅ **核心台账已建立**：`ibms_device`(49) / `ibms_channel`(137) / `ibms_space`(14) / `ibms_product` / `ibms_dict`
- ✅ **业务大类映射**：`IbmsBusinessMappingHelper`（`groupCode`：`SA`/`ST`/`SB`/`SE`/`SF`/`GW`；`systemCode`：子系统）
- ✅ **运行态分离**：`ibms_device_runtime` 单独存放在线状态/最后心跳/上一次告警等
- ⚠️ **混轨遗留**：`IotDeviceDO`/`IotAccessDeviceServiceImpl` 仍引用旧 `iot_device` 思路，但 DB 已无 `iot_device` 表 → 需要彻底换成 `IbmsDeviceDO`
- ⚠️ **专项扩展表与 ibms 弱关联**：`ibms_energy_meter`（已加 `ibms_device_id` 外键 ✅）、`ibms_hvac_device`、`ibms_water_device`、`ibms_lighting_*`、`ibms_env_*` 是否全部都有 `ibms_device_id` 外键？**M2 待校验**

### 2.2 前端 4 模块对接现状

| 业务模块 | 子目录 | IBMS 直连 | 聚合层 | 旧 iot_device | Mock |
|---|---|---|---|---|---|
| 智慧安防 | `views/security/` | VideoSurveillance/VisualBoard、PerimeterIntrusion/AlarmHost | SecurityOverview | — | 部分子模块 (M1 待盘点) |
| 智慧通行 | `views/iot/access/` | — | — | device、channel、management 等 | visual-dashboard |
| 智慧能源 | `views/energy/`<br>`views/iot/building/energy/` | building/energy/equipment | Overview、ConsumptionAnalysis | — | DeviceManagement、各 Mock 子页 |
| 智慧建筑 | `views/iot/building/` | — (M1 待确认) | — | — | building-visual-dashboard |

**详细矩阵在 `docs/ibms-coverage-matrix.md` 中维护，由 M1 阶段填充。**

### 2.3 5 大主要"断点"

1. **智慧通行 access 模块**：后端混用 `IotDeviceDO` 与 `IbmsDeviceDO`；前端 `@/api/iot/access` 与 `@/api/iot/ibms` 双轨，需要**单源化**
2. **3 个可视化大屏完全 mock**：`access/visual-dashboard`、`building-visual-dashboard`、`energy/DeviceManagement`
3. **智慧能源双套并存**：`views/energy/` 与 `views/iot/building/energy/`，菜单与路由都暴露，需要**决策保留**哪一套
4. **业务大类筛选机制使用率低**：仅 AlarmHost 等少数页面使用 `groupCode`/`systemCode` 过滤，多数业务页面没用
5. **聚合层（如 SecurityOverview）底层未必查 ibms_***：需要校验后端聚合 API 是否真正读 ibms 表

---

## 3. 阶段路线图（M0~M7）

每个阶段都有：**目标 / 输入 / 输出物 / DoD（完成定义） / 预估工量 / 依赖 / 风险**

### M0：主计划与跨会话承接基线（本次会话完成）

- **目标**：建立可承接的工作基线
- **输出物**：
  - `docs/ibms-unified-data-source-plan.md`（本文档）
  - `docs/ibms-coverage-matrix.md`（覆盖矩阵模板 + 5 行示范）
  - `docs/ibms-unified-progress.md`（跨会话进度跟踪）
  - 在最新 `docs/session-handoff-*.md` 中加入"IBMS 统一数据源治理"段落
- **DoD**：3 份文档落盘 + git commit + 在 handoff 中可被新会话发现

### M1：完整盘点（下次会话，约 30~45 分钟）

- **目标**：把 4 模块所有 vue 页面入矩阵，对每页标注：①是否调 IBMS API；②是否调旧 access/iot_device API；③是否走聚合层；④是否 mock
- **输入**：`yudao-ui-admin-vue3/src/views/{security,iot/access,iot/building,energy}` 全部 vue 文件
- **输出物**：`docs/ibms-coverage-matrix.md` 完整版（每行一页，约 50+ 行）；差距清单（按页面分类的"待改造列表"）
- **DoD**：
  1. 矩阵覆盖率 100%（4 模块每个 `index.vue` 都有一行）
  2. 每行都标注：路由路径 / 文件路径 / 数据源类型 / 调用的 API / 优先级 / 预估改造工量
  3. 输出"高/中/低"3 档优先级的差距清单
- **风险**：vue 文件数量较多，初次扫描可能漏 sub-tab 类页面 → 用 `Get-ChildItem -Recurse -Filter index.vue` 全量扫，配合关键 API 字符串 grep 二次校验

### M1.5：双向校验（前端需求 × 后端能力，本会话已完成）

- **目标**：识别"前端要而后端没有"（→ M2 补全）+ "后端有而前端没用"（→ M3~M6 接入）的双向缺口
- **输出物**：`docs/ibms-bidirectional-gap.md`
- **关键发现**：
  1. 矩阵中标 🟡 聚合层的页面，**底层服务实测已查 ibms_***（如 `IotSecurityOverviewServiceImpl` 27 处 `IbmsXxx` 引用、`IbmsBac/Energy/Env/Lighting Controller` 全是 IBMS 直连）
  2. `AccessDashboardController` 已提供 6 个聚合端点，但前端 `iot/access/visual-dashboard` **0 调用** —— 是"后端有，前端没用"，不需补后端
  3. 真正缺后端的：周界 VisualBoard / 电子巡更可视化板 / 视频巡更大屏 / `PersonnelControl/*` 7 子页 / 建筑大屏空间聚合 等约 12 项 GAP
- **DoD**：✅ `docs/ibms-bidirectional-gap.md` § A/§ B/§ C/§ D 完整 + § C 17 项 GAP 编号 + § D 15 项富余资产处置建议

### M1.6：全前端僵尸文件候选清单（本会话已完成，0 删除）

- **目标**：用 BFS 引用图算法识别"任意活路径不可达"的文件作为清理候选；本会话仅产出清单
- **算法**：活根集合（含 `views/**`、`components/DiyEditor/**`、5 个目录树）→ BFS 标可达 → 全集 - 可达 = 僵尸
- **输出物**：`docs/ibms-zombie-candidates.md` + `.tmp_sql/m1-zombie-{bfs.py,live.txt,candidates.txt}`
- **结果**：全集 1916 / 活 1834 / 候选 82 → 分级 **Z0=8 / Z1=37 / Z2=24 / Z3=13**
- **DoD**：✅ BFS 脚本落盘可重复 + Z0~Z3 分级 + Z1 抽样 5 项 0 引用复核 + **0 文件删除**
- **后续 M1.7（下次会话）**：按 `docs/ibms-zombie-candidates.md` 五层防御（build → vue-tsc → snapshot 分支 → 分批 commit → playwright smoke）+ 分批 1~5 顺序清理

### M2：后端能力补齐（多次会话，预估 1~2 个工作日）

- **目标**：让所有"业务模块需要的数据"都能从 ibms_* 表查到
- **任务清单**：
  1. **access 单源化**（核心）
     - 删除/弃用 `IotDeviceDO`，`IotAccessDeviceServiceImpl` 全部改用 `IbmsDeviceMapper`
     - `IotAccessDeviceController` 出参改用 `IbmsDeviceRespVO` 系列
     - 废弃 `@/api/iot/access` 中的 device/channel API（保留 person/permission/event 等业务接口）
  2. **专项扩展表外键校验**
     - `ibms_energy_meter` ✅ 已有 `ibms_device_id`
     - `ibms_hvac_device` / `ibms_water_device` / `ibms_lighting_controller` 等 → 是否都关联到 `ibms_device.id`？缺失则补
  3. **可视化大屏聚合 API**
     - `building-visual-dashboard`：需要"按空间树聚合的设备数 / 在线率 / 告警数"接口
     - `access/visual-dashboard`：需要"今日通行总数、设备健康度、人员在场实时"接口
     - `energy/DeviceManagement`：需要"按表具类型分组的统计"接口
  4. **业务大类筛选场景全验证**
     - 跑一遍 `groupCode IN (SA,ST,SB,SE,SF,GW)` 与 `systemCode` 笛卡尔积，确保前端任何业务页面都能基于这两个字段拿到准确的过滤结果
- **DoD**：
  1. `IotDeviceDO` 在 service 层 0 引用（搜索结果为空）
  2. 4 模块所需的"聚合接口"全部在 swagger 中可见
  3. `groupCode + systemCode` 组合过滤的单测/集成测全部通过
- **风险**：access 单源化涉及多个 controller/service/vo，需要严格回归通行模块功能；建议**新建 feature 分支**

### M3：智慧通行模块改造（约 1 个工作日）

- **目标**：通行模块前端所有数据源自 IBMS
- **任务清单**：
  1. `views/iot/access/device/index.vue`：`AccessDeviceApi` → `IbmsDeviceApi.getDevicePage({groupCode:'ST'})`
  2. `views/iot/access/channel/index.vue`：同理换 `IbmsChannelApi`
  3. `views/iot/access/visual-dashboard/index.vue`：替换所有 `builtinData` 调用为 M2 提供的真实聚合 API
  4. 其他 access 子页面（M1 矩阵中标 mock 的页面）逐一改造
- **DoD**：
  1. 通行模块所有 vue 文件中 `@/api/iot/access` 不再出现 `device`/`channel` 相关方法
  2. `builtinData` 在通行模块 0 引用
  3. 实施人员在物联模块新增一台门禁设备（`groupCode=ST, systemCode=SA-ACS`），通行模块设备列表/可视化大屏立即显示
- **依赖**：M2 必须先完成 access 单源化与 visual-dashboard 聚合 API

### M4：智慧建筑模块改造（约 1.5 个工作日）

- **目标**：建筑模块前端所有数据源自 IBMS
- **任务清单**：
  1. `views/iot/building/building-visual-dashboard/index.vue`：完全重写，接入：
     - `ibms_space` 树（空间分布）
     - `ibms_device` 按楼层/系统分组计数
     - `ibms_device_runtime` 在线/告警状态
  2. `bac/env/lighting/newlight` 子模块各自的 `device`/`controller` 列表 → 改用 IBMS 直连
  3. 专项扩展表（`ibms_lighting_controller` 等）`ibms_device_id` 外键填充补全
- **DoD**：
  1. 建筑模块所有 vue 文件 0 个 mock 数据
  2. 实施人员配置一台 BAC 设备（`groupCode=SB, systemCode=SB-BAC`），建筑模块各子页面立即出现
  3. 空间树编辑后，可视化大屏布局实时反映
- **依赖**：M2 聚合 API、专项扩展表外键已补齐

### M5：智慧能源双套合并（约 1 个工作日）

- **目标**：能源模块只保留一套前端代码与一套接口
- **决策点**（**M5 启动前必须先与产品确认**）：
  - **方案 A**：保留 `views/iot/building/energy/`（已对接 IBMS），删除 `views/energy/`
  - **方案 B**：保留 `views/energy/`（独立模块感更强），但其内部全部改用 IBMS API
- **任务清单**（按方案 A 列）：
  1. 路由 `views/energy/*` → 重定向到 `views/iot/building/energy/*` 或直接删除
  2. 菜单项调整
  3. `DeviceManagement/index.vue` 改用 `getIbmsDevicePage({groupCode:'SE'})`
  4. 各 mock 子页（`AlarmManagement`、`ReportManagement` 等）接入 IBMS
- **DoD**：
  1. 单一前端入口
  2. 实施人员配置一块电表（`groupCode=SE, systemCode=SE-EM`），能源模块仪表板/详情/告警/报表立即显示
- **依赖**：M2 ibms_energy_meter 已有 `ibms_device_id` 外键 ✅

### M6：智慧安防模块完善（约 1 个工作日）

- **目标**：补齐安防模块剩余子模块的 IBMS 对接
- **任务清单**：
  1. `views/security/ElectronicPatrol`、`VideoAnalysis`、`VideoPatrol`、`PersonnelControl`、`SecurityOverview` 等子模块 → 检查 + 改造（M1 矩阵驱动）
  2. `SecurityOverview` 后端聚合层 → 校验是否查 ibms_*，否则改造
  3. `NewIntrusionAlarm` 等新子模块 → 走 IBMS
- **DoD**：
  1. 安防模块所有 vue 页面 0 mock
  2. 实施人员配置一台摄像头（`groupCode=SA, systemCode=SA-VMS`），安防各子模块立即出现

### M7：验收与移交（约 0.5 个工作日）

- **目标**：端到端验证 + 物联模块隐藏开关 + 客户演示版
- **任务清单**：
  1. **演练剧本**：实施人员从空数据库开始，配置：1 个项目空间树 → 5 类设备各 1 台 → 验证 4 业务模块全部页面均有数据
  2. **物联模块隐藏开关**
     - 方案：菜单 `iot/ibms/*` 增加角色权限开关；客户角色不分配 → 自动不显示
     - 备选：环境变量 `VITE_HIDE_IBMS_MODULE=true` 控制（更轻量）
  3. **客户演示版打包**：去掉物联模块的 demo 数据 / 文案 / 帮助
- **DoD**：
  1. 演练通过：实施 0 干预下 4 业务模块全展示
  2. 客户登录看不到"智慧物联"菜单，但所有业务模块功能正常
  3. 出一份《客户演示版发布说明》

---

## 4. 跨会话承接规约

### 4.1 启动新会话时

```
我承接 IBMS 统一数据源化治理计划。请先只读：
- AGENTS.md
- docs/ibms-unified-data-source-plan.md（本主计划）
- docs/ibms-unified-progress.md（最新进度）
- docs/session-handoff-<最新日期>-vN.md
然后告诉我下一步建议（基于当前 progress 文件中的 M 阶段）。
```

### 4.2 完成阶段任务后

每完成一个 M 阶段（或 M 阶段中的一个子任务），需要：

1. 更新 `docs/ibms-unified-progress.md`（追加一行记录：日期 / 阶段 / commit / 备注）
2. M1/M2 完成时，**必须** 更新 `docs/ibms-coverage-matrix.md`
3. 在最新 handoff 中提及"IBMS 治理计划进展"
4. git commit + push

### 4.3 文档命名规范

- 主计划：`docs/ibms-unified-data-source-plan.md`（**不要** 带日期；本文档常驻）
- 覆盖矩阵：`docs/ibms-coverage-matrix.md`（常驻，每次更新覆盖）
- 进度跟踪：`docs/ibms-unified-progress.md`（常驻，append-only）
- 阶段性产物（如方案讨论）：`docs/ibms-{phase}-{topic}-<日期>.md`

---

## 5. 决策点登记（Decision Log）

| 编号 | 议题 | 决策 | 负责人 | 决策日期 |
|---|---|---|---|---|
| D-001 | 智慧能源前端保留哪一套（M5 前置） | 待定（推荐方案 A：保留 building/energy） | 产品 | — |
| D-002 | 物联模块隐藏方式（菜单权限 vs 环境变量，M7 前置） | 待定 | 产品 + 实施 | — |
| D-003 | access 模块单源化时 `IotDeviceDO` 是否物理删除（M2） | 推荐：标 @Deprecated 一个版本周期后删除 | 后端 | — |

---

## 6. 风险与备忘

- **回归测试压力大**：access/通行模块用户基数最大，M2/M3 改造务必先在 dev 环境跑全量回归
- **多租户**：所有改造必须保持 `tenantId` 透传（参考本次 v24 NPE 修复经验）
- **历史数据迁移**：如已有客户在用旧表数据，M2 单源化前必须出**数据迁移脚本**（`iot_device → ibms_device` 已不存在该问题，但其他专项扩展表可能有）
- **CI/CD**：每个 M 阶段都要先在 Drone CI（`http://test.sanligz.com.cn/api/repos/fengxiatao/ch_ibms`）跑一遍构建再合并
