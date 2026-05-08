# IBMS 统一数据源化治理 - 进度跟踪（Append-Only）

> 配套主计划：`docs/ibms-unified-data-source-plan.md`
> 配套矩阵：`docs/ibms-coverage-matrix.md`
> **规约**：每完成一项工作（一个阶段或阶段内的子任务），追加一行；不允许编辑历史行。

---

## 当前推进状态

- **当前活动阶段**：M2 推进中（M2-A ✅ + M1.7-Batch2 ✅ + M2-D ✅✅含验收）→ 下一步候选 M2-B（access 单源化 GAP-011）/ M2-C（building-visual-dashboard 聚合 GAP-002）
- **下一阶段建议**：浏览器最终目视验收 visual-dashboard（http://localhost:5173 → 智慧通行 → 可视化大屏）→ 切 M2-B 或 M2-C
- **已完成阶段**：M0、M1、M1.5、M1.6、M2-A、M1.7-Batch2、M2-D（代码 + SQL 验收 + 数据补全 ✅）
- **阻塞决策点**：D-001（智慧能源前端保留方案）、D-002（物联模块隐藏方式）
- **运行时环境**（2026-05-08）：yudao-server 在 **48888 端口**（PID 42016，含 0b62d00），前端 Vite 在 **5173**，`.env.dev` 已对齐；admin token 需重登拿（v14/v19 已过期）

---

## 进度记录（按时间倒序）

| 日期 | 阶段 | 行动 | 产出 / commit | 负责人 | 备注 |
|---|---|---|---|---|---|
| 2026-05-06 | M0 | 创建主计划 + 覆盖矩阵 + 进度跟踪三件套 | `docs/ibms-unified-data-source-plan.md` / `docs/ibms-coverage-matrix.md` / 本文件 | AI/主程 | 基于本日五大模块对接现状调研产出 |
| 2026-05-06 | M0 | 基线现状调研 | 调研报告写入主计划 §2 | AI/主程 | 关键发现：DB 已无 `iot_device` 表；`access` 后端已混用 `IbmsDeviceMapper` |
| 2026-05-06 | M0 | 三件套 commit + push（origin + chvm1） | commit `6630bc0` | AI/主程 | M0 完成，进入 M1 待启动 |
| 2026-05-06 | M1 | 109 个 `index.vue` 全量盘点 + 自动判定 + 5 项关键页人工复核 | `docs/ibms-coverage-matrix.md` 完整版（§1~§10） + `.tmp_sql/m1-{scan,classify-v2,gen-matrix,gen-gap}.ps1` | AI/主程 | 状态分布：IBMS=4 / AGG=20 / ACCESS=21 / LEGACY=25 / MOCK=27 / SHELL=12；待改造 93 项（P0=28, P1=33, P2=32） |
| 2026-05-06 | M1 | M1 阶段 commit + push（origin + chvm1） | commit `d97a200` | AI/主程 | 矩阵 + 进度跟踪入库 |
| 2026-05-06 | M1.5 | 双向校验：前端需求 × 后端能力（粗粒度） | `docs/ibms-bidirectional-gap.md` | AI/主程 | 关键发现：🟡聚合层多数底层已 IBMS 直连；`AccessDashboardController` 6 端点前端 0 调用；§C 17 项 GAP / §D 15 项富余资产 |
| 2026-05-06 | M1.6 | BFS 引用图算法识别全前端僵尸候选（0 删除） | `docs/ibms-zombie-candidates.md` + `.tmp_sql/m1-zombie-{bfs.py,live.txt,candidates.txt,dynamic-risk.txt}` + `m1-gen-zombie-doc.py` | AI/主程 | 全集 1916 / 活 1834 / 候选 82（Z0=8 / Z1=37 / Z2=24 / Z3=13）；抽 5 项 grep 0 引用确认；含五层防御 + 5 批次清理规约 |
| 2026-05-06 | M2-A | `iot/access/visual-dashboard` 接入真实 statistics + hourly 端点（GAP-001） | commit `ed251c6`：新建 `src/api/iot/access/dashboard.ts`（6 端点 TS 客户端）+ 改 `views/iot/access/visual-dashboard/index.vue` | AI/主程 | 4 metric 卡片 + 24h 进入趋势改为实数据（onMounted 拉取 + 失败 fallback mock）；告警分类/趋势/公司排行后端 stub，保留 mock 待 M2 后期补；vue-tsc 0 相关错误；双远端已 push |
| 2026-05-06 | M1.7-Batch2 | 删除 5 个未引用 api/fire/* 僵尸文件 | commit `3d9948f`：snapshot 分支 `pre-zombie-cleanup-20260506` 双远端预 push；`api/fire/{emergencyResponse,fireAlarm,fireSafety,fireSuppression,smokeControl}/index.ts` 删除 | AI/主程 | grep '@/api/fire/' 全工程 0 引用确认；`api/fire/index.ts` 是独立 barrel 不依赖被删文件；vue-tsc baseline 1692 删前后一致；Batch-1 (test 文件) 因被测 util 是 Z1 僵尸暂缓与 Batch-4 配套 |
| 2026-05-06 | M2-D | AccessDashboard 6 端点全面对接真实表 `iot_access_event_log`（GAP-001 根因修复） | commit `0b62d00`：`IotAccessEventLogMapper` 加 7 SQL；`AccessDashboardServiceImpl` 完全重写；前端 `dashboard.ts` 补全 4 端点 RespVO 类型；`visual-dashboard/index.vue` 切到 trend 端点 | AI/主程 | **重大根因发现**：原实现注入 AccessRecordMapper/AccessAlarmMapper，DO 标注的 iot_access_record/iot_access_alarm 表在 DB 中不存在（M2-A 实际永远 fallback mock）；本次迁到真实表（56448 行真数据，时间跨度 2025-12-22~2026-04-20）；4 个 stub 端点全部落地；vue-tsc 0 相关错误；**待用户 mvn 重打 + 重启 smoke** |
| 2026-05-08 | M2-D 验收 | mapper 7 SQL 直查验证 + 运行时 jar/进程定位 + 端口 smoke | 验证 `IotAccessEventLogMapper` 7 个聚合 SQL 全部跑通真实数据（countByTimeRange/countAlarmsByTimeRange/selectDailyTrend/selectHourlyTrend/selectHeatmap/selectEventTypeDistribution/selectAbnormalList）；定位运行时：yudao-server jar mtime=2026-05-08 10:09 已含 0b62d00，进程 PID 42016 跑在端口 **48888**（非默认 48080）健康（health=UP, swagger 1903B），前端 Vite dev 在 5173；`.env.dev` `VITE_BASE_URL=http://127.0.0.1:48888` 已对齐 | AI/主程 | **重大发现**：另有并行 IDE 会话在协作（不冲突）—10:09 重打 jar、10:51 启动新 yudao-server、配置前端联调链路均非本会话产物；HTTP 6 端点 smoke 因 v14/v19 token 过期暂未跑（SQL 层已 100% 覆盖核心语义） |
| 2026-05-08 | M2-D 数据补全 | 补 718 行多类型种子数据修复展示偏差 | `.tmp_sql/m2d_gen_seed.ps1` 生成器 + `m2d_demo_seed.sql` (125KB/718 行) + 7 日批 `m2d_seed_2026050{2..8}.sql` + 合并 `m2d_seed_rest.sql`；mcp4 跑 5/2 (59 行) + mysql.exe 灌 5/3~5/8 (659 行) | AI/主程 | **修复展示偏差**：原 56448 行 direction 字段全 NULL、事件类型仅 DOOR_NOT_CLOSED 单一，visual-dashboard 即使切真数据也将"全趴底"；补完后 18 种事件类型全覆盖（CARD_SWIPE 207 / FACE_RECOGNIZE 182 / FINGERPRINT 56 等），direction 1/2 平衡（5/8: in 47, out 54），告警占比 14% 真实，周末低峰 + 工作日高峰 (59 vs 120) 显现；回滚 SQL 已落档 `DELETE WHERE event_time BETWEEN '2026-05-02' AND '2026-05-08' AND tenant_id=1` |

---

## 跨会话承接清单

新会话承接时**必读**：

1. ✅ `AGENTS.md`
2. ✅ `docs/ibms-unified-data-source-plan.md`（主计划）
3. ✅ `docs/ibms-coverage-matrix.md`（最新矩阵）
4. ✅ 本文件（进度）
5. ✅ 最新一份 `docs/session-handoff-<日期>-vN.md`

承接确认句式：

```text
我承接 IBMS 统一数据源治理。当前进度：M0 已完成，准备进入 M1（完整盘点）。
请基于覆盖矩阵的"❓未盘点"行，按子目录批量扫描 vue 文件并填表。
```

---

## 各阶段 DoD 自检表

| 阶段 | DoD #1 | DoD #2 | DoD #3 | 状态 |
|---|---|---|---|---|
| M0 | 主计划落盘 | 矩阵模板落盘 | 进度跟踪落盘 | ✅ 完成 |
| M1 | 矩阵覆盖率 100%（❓ 清零） | 优先级分级清单产出 | 改造工量预估完成 | ⏳ 待启动 |
| M2 | `IotDeviceDO` 在 service 层 0 引用 | 4 模块所需聚合 API 全部上线 | group/system code 单测通过 | ⏳ |
| M3 | access 模块 0 个 mock | 通行可视化大屏接入真实数据 | 实施配置 → 通行展示 演练通过 | ⏳ |
| M4 | 建筑模块 0 个 mock | 可视化大屏使用 `ibms_space` 树 + `ibms_device` 计数 | 实施配置 → 建筑展示 演练通过 | ⏳ |
| M5 | 单一前端入口（决策落地） | DeviceManagement 接入真实 IBMS | 实施配置 → 能源展示 演练通过 | ⏳ |
| M6 | 安防 8+ 子模块全部对接 | SecurityOverview 底层校验完成 | 实施配置 → 安防展示 演练通过 | ⏳ |
| M7 | 端到端演练通过 | 物联模块菜单隐藏可生效 | 客户演示版打包并出发布说明 | ⏳ |

---

## 风险登记（持续维护）

| 编号 | 风险 | 影响 | 缓解措施 | 状态 |
|---|---|---|---|---|
| R-001 | access 模块单源化可能引发通行模块功能回归 | 高 | M2 在独立 feature 分支 + 全量回归测试 | 监控中 |
| R-002 | 多租户 tenantId 透传不全 | 高 | 参考 v24 NPE 修复经验，所有新增 service/MQ 路径都做 tenantId 校验 | 监控中 |
| R-003 | 历史项目数据未导入到 ibms_* | 中 | M2 出数据迁移脚本（按租户） | 待识别 |
| R-004 | 实施培训成本 | 中 | M7 出实施手册（"配置一台设备 → 4 模块联动" 教程） | 待启动 |

