# CH IBMS v25 会话 Handoff（2026-05-06，M1 三件套闭环版）

> 主任务：完成 IBMS 统一数据源治理 **M1 + M1.5 + M1.6** 三件套（盘点 + 双向校验 + 僵尸候选）。
> 完整 DoD 14 项已全部通过，**0 个文件被删除**（M1.6 硬约束）。

---

## 1. 项目骨架（与 v24 一致）

- **后端**：`ruoyi-vue-pro/`  Spring Boot 3 + MyBatis Plus
  - `yudao-server`（fat-jar 入口，端口 48888）
  - `yudao-module-iot/yudao-module-iot-biz`（**本会话扫描重点**：`controller/admin/{ibms,access,building,security,alarm}/*`）
  - `yudao-module-iot/yudao-module-iot-newgateway/*`
- **管理端**：`yudao-ui-admin-vue3/`（**本会话扫描重点**：`src/views/{security,iot/access,iot/building,energy}` + 整个 `src/`）

---

## 2. 本次会话变更

### 2.1 commit 列表（双远端均已 push：origin = github + 内网；chvm1 = 内网）

| commit | 阶段 | 说明 |
|---|---|---|
| `d97a200` | M1 | 109 个业务 `index.vue` 全量盘点 + 三档差距清单（P0=28/P1=33/P2=32） |
| `954dfb8` | M1.5 + M1.6 | 双向校验报告 + BFS 僵尸候选清单（0 删除） |

### 2.2 文档变更（4 文件，+495/-3）

| 文件 | 变更 |
|---|---|
| `docs/ibms-coverage-matrix.md` | 重写为完整版（§1~§10，含 109 行业务矩阵 + 5 项关键页人工复核 + 三档差距清单） |
| `docs/ibms-bidirectional-gap.md` | **新建**：§A 业务模块需求 / §B IBMS 后端能力 / §C 17 项 GAP / §D 15 项富余资产 |
| `docs/ibms-zombie-candidates.md` | **新建**：82 个候选 Z0~Z3 分级 + 五层防御 + 5 批次清理规约 |
| `docs/ibms-unified-data-source-plan.md` | §3 插入 M1.5 / M1.6 段落（M0~M7 主线不变） |
| `docs/ibms-unified-progress.md` | append 4 行 + 当前阶段更新为 M1.6 完成 |

### 2.3 工具链产出（`.tmp_sql/`，gitignored，可重复执行）

```text
m1-scan.ps1            扫描 109 个 index.vue 提取 7 个特征 → m1-scan-result.csv
m1-classify-v2.ps1     按 v2 规则自动判定状态 + 工量分档 → m1-classified.csv
m1-gen-matrix.ps1      生成矩阵 §1~§7（PowerShell 5.1 需 UTF-8 BOM）
m1-gen-gap.ps1         生成 §8~§10（复核 + 三档差距）
m1-zombie-bfs.py       BFS 引用图扫描（Python 3.13）→ live.txt + candidates.txt
m1-gen-zombie-doc.py   候选 Z0~Z3 分级 + 文档生成
```

### 2.4 关键技术发现

1. **🟡聚合层 ≠ 后端缺数据**：`IotSecurityOverviewServiceImpl` 实测 27 处 `IbmsXxx` 引用；`IbmsBac/Energy/Env/Lighting Controller` 全是 IBMS 直连。M1 矩阵中标 🟡 的页面，**绝大多数底层已查 ibms_***，M2 不需补后端，前端 import 路径名容易误解。
2. **后端有，前端没用**：`AccessDashboardController` 已提供 6 个聚合端点（`statistics`/`real-time`/`trend`/`device-status`/`heatmap`/`abnormal-events`），但 `iot/access/visual-dashboard/index.vue` **0 调用** —— 是 M3 前端任务，不是 M2 后端任务。
3. **真正的后端缺口**集中在 security：周界 VisualBoard / `PersonnelControl/*` 7 子页 / 电子巡更可视化板 / 视频巡更大屏 / 建筑大屏空间聚合 等约 12 项 GAP（详见 `docs/ibms-bidirectional-gap.md` §C）。
4. **DB 富余资产**：3 张 bak 备份表（v22/v23 治理留下）+ `ibms_{lighting_scene_circuit, device_message, device_property_history}` 三个 0 行表，M2 评估清理。
5. **PowerShell 5.1 中文脚本陷阱**：`powershell -File foo.ps1` 默认按 GBK 解析脚本，含中文需保存为 **UTF-8 with BOM** 才能正确读入。`write_to_file` 默认无 BOM，需 `[System.Text.UTF8Encoding $true]` 重新写入。
6. **BFS 算法保证（满足硬约束）**：`visited` 是活根集合的可达闭包 → `Zombies = AllFiles - visited`，**与是否被 zombie 引用无关**。修复了 2 处漏抓：`views/**` 整树入活根（`utils/routerHelper.ts:7` 用 `import.meta.glob('../views/**/*.{vue,tsx}')`）；resolve 返回所有兄弟 `index.{ts,vue}` 候选。

### 2.5 本会话量化结果

| 维度 | 数字 |
|---|---|
| 业务页面盘点 | 109 个 `index.vue`（security 48 / iot/access 22 / iot/building 32 / energy 7） |
| 状态分布 | IBMS=4 / 🟡聚合=20 / 🟠access=21 / 🟤旧API=25 / ❌Mock=27 / ⚪壳=12 |
| 待改造 | 93 项（P0=28, P1=33, P2=32） |
| 双向校验 GAP | §C 17 项（前端要后端没有） / §D 15 项（后端有前端没用） |
| BFS 全集 | 1916 个（vue/ts/tsx/js/jsx） |
| BFS 活根 | 1259（含 `views/**` 整树） |
| BFS 可达 | 1834 |
| 僵尸候选 | 82（Z0=8 / Z1=37 / Z2=24 / Z3=13） |
| 抽样复核 | 10 项（5 项扫描+5 项 BFS）全部一致，0 误判 |
| 删除文件 | **0**（M1.6 硬约束） |
| MySQL 写操作 | **0**（仅只读 mcp4 查 ibms_*、controller 路径） |

---

## 3. CI 访问

- **Drone Server**：`http://test.sanligz.com.cn`（= `192.168.1.253` 内网）
- **CH 仓库**：`fengxiatao/ch_ibms`
- **API 基址**：`http://test.sanligz.com.cn/api/repos/fengxiatao/ch_ibms`
- **Token**：与 jingyu 共用 drone 用户 token，存放于 v23/v24 handoff "关键访问凭据"段；本会话未涉及 CI API 调用
- **push 双远端已完成**：`origin`（CI 内网 + github.com） + `chvm1`（CI 内网）均到 `954dfb8` ✅

---

## 4. MySQL 连接

- **本地开发库**：`mcp4_mysql_query`（`mysql-ibms`） `ch_ibms` @ `127.0.0.1:3306`，**只读**
- **本会话仅用 mcp4 做 1 次只读查询**（`information_schema.TABLES` 列出 34 张 ibms_* 表 + 行数）
- **未做任何写操作**（INSERT/UPDATE/DELETE/DDL = 0）
- 禁用名单（mcp5/6/7）继续遵守

---

## 5. 本机构建硬规则（v24 已写入 `.cursor/rules/14-local-build.mdc`）

> 本会话纯文档改动，未触发 mvn / pnpm，但下次 M1.7 / M2 必看：
>
> - mvn 必须 `clean install -DskipTests`，重打 yudao-server 前先 kill 端口 48888 上的 JVM
> - 验证 fat-jar 内嵌 biz jar lastWrite ≠ 上一构建（v24 教训：BUILD SUCCESS 可能是假的）
> - **M1.7 新增规约（本 handoff 首次落地）**：见 `docs/ibms-zombie-candidates.md` "后续清理执行规约"段，五层防御 + 5 批次

---

## 6. 下一步候选 + 给 v26 的建议

### 6.1 推荐候选（按价值/风险排序）

| # | 候选 | 价值 | 风险 | 工作量 | DoD |
|---|---|---|---|---|---|
| 1 | **M2-A：`AccessDashboardController` → `iot/access/visual-dashboard` 接入** | 高（消除 1 个 P0 大屏 mock） | 低（后端已就绪） | 30~60min | visual-dashboard 不再用 builtinData，6 个 dashboard API 真实返回数据 |
| 2 | **M2-B：access 单源化（GAP-011）** | 高（核心断点 1） | 中（涉及多 controller/service） | 半天 | `IotAccessDeviceServiceImpl` 删 IotDeviceDO，全用 IbmsDeviceMapper；前端 `iot/access/device` 仍可正常 |
| 3 | **M1.7-Batch1+Batch2 僵尸清理** | 中（前端 build 时间略减） | 低（仅 *.test.ts + api/fire/*） | 30min | snapshot 分支 + git rm 14 文件 + pnpm build/test 通过 |
| 4 | **M2-C：building-visual-dashboard 后端聚合（GAP-002）** | 高（消除 1 个 P0 大屏） | 中（需新设计 IbmsSpace 聚合 API） | 半天 | `/admin-api/iot/ibms/space/dashboard-stats` 返回按空间树聚合的设备数+在线率+告警数 |
| 5 | 把 v25 教训写入 `.cursor/rules/14-local-build.mdc` | 低 | 极低 | 5min | rule 文件追加 PowerShell UTF-8 BOM 段 + BFS 算法保证段 |

### 6.2 给 v26 的关键提醒

- **不要再翻 v24 之前的 handoff**（除用户明确要求）。从 v25 + AGENTS.md + 4 份治理文档（plan/matrix/gap/zombies）起步。
- **M1.7 清理执行强制要求**：按 `docs/ibms-zombie-candidates.md` 五层防御逐步推进，**禁止一次性 git rm 全部**。Batch-5（components 1-行 export）必须有 playwright smoke 兜底。
- **M2 后端改造核心提醒**：access 单源化（GAP-011）涉及 `IotAccessDeviceController/Service/VO` 三层，建议**新 feature 分支**，回归测试后合并。
- **前端 build / vue-tsc 必须先在 yudao-ui-admin-vue3 跑一遍**（`pnpm install` 可能本地没装过），拿 baseline 后再做任何代码改动。
- **continue session 命令模板**（粘贴给 v26 起始）：

```text
我承接 CH IBMS v26，工作区 e:\ch，分支 snapshot/20260423-full（HEAD=954dfb8）。
请只读：
  1) AGENTS.md
  2) docs/session-handoff-20260506-v25.md（本 handoff）
  3) docs/ibms-unified-data-source-plan.md（主计划，M0~M7）
  4) docs/ibms-coverage-matrix.md（M1 矩阵）
  5) docs/ibms-bidirectional-gap.md（M1.5 双向校验，§C 17 GAP / §D 15 富余）
  6) docs/ibms-zombie-candidates.md（M1.6 候选清单 + 五层防御）
然后告诉我下一步建议（候选见 v25 §6.1）。
MySQL MCP 仅用 mcp4_mysql_query；本机构建硬规则见 .cursor/rules/14-local-build.mdc + v24 教训。
```

---

## 7. 当前分支状态

- **HEAD**：`snapshot/20260423-full @ 954dfb8`
- **双远端**：origin（github + 内网）+ chvm1（内网）均到 954dfb8 ✅
- **本地 untracked（不入 commit）**：
  - `.tmp_sql/m1-{scan,classify,classify-v2,gen-matrix,gen-gap,gen-zombie-doc}.{ps1,py}` 等工具链
  - `.tmp_sql/m1-{scan-result,classified}.csv` + `m1-zombie-{live,candidates,dynamic-risk}.txt`
  - `.tmp_sql/m1-matrix-section{1to7,8to10}.md` 中间产物
  - `docs/session-handoff-20260504-v2~v10.md`、`docs/session-handoff-20260506-v24-wip.md`（历史 wip，未追踪）
  - `wvp-GB28181-pro/`、`大华海康代码/`、`__MACOSX/`、`ruoyi-vue-pro/yudao-module-ai/`、`ruoyi-vue-pro/yudao-server/bin/` 等
- **运行中后台进程**：v24 残留（不属本会话产物）
  - yudao-server PID `39496` @ port 48888（v24 修复版，未重启）
  - webhook-sink @ port 9999
- **DB 持久变更**：本会话**无**

---

## 8. 关键复盘笔记

### 8.1 扫描判定规则（v2，写入矩阵 §6）

```text
AnyApi=0 AND (Mock关键字>0 OR Lines>=300)  -> ❌Mock/硬编码
AnyApi=0 AND Lines<300                     -> ⚪路由壳
IBMS>0 AND ACC=0                           -> ✅IBMS直连
ACC>0 AND IBMS=0                           -> 🟠旧access  (parking/* -> 🟢业务自有)
IBMS>0 AND ACC>0                           -> 🔵混合
BLD>0 AND IBMS=0 AND ACC=0                 -> 🟡聚合层
其余 AnyApi>0                              -> 🟤旧API（spatial/device/video/patrol/alarm 等）
```

升级要点（v1 → v2）：把 `AnyApi=0 + Lines>=300` 识别为 MOCK，避免 `building-visual-dashboard` / `energy/DeviceManagement` 等"硬编码大屏"被误归 STATIC。

### 8.2 BFS 算法保证（写入候选清单 §0.1）

```text
visited = closure(roots, follow=imports)
Zombies = AllFiles - visited
∀ f ∈ visited, ∀ ref ∈ imports(f) → resolve(ref) ∈ visited
```

只要任意活路径可达 → 不判僵尸。被 zombie 引用不影响判定（zombie 不在 visited 内，其 imports 不进 BFS）。

### 8.3 五层防御（写入候选清单 §"后续清理执行规约"）

```
1. pnpm build              （构建产物 vs BFS 活集合一致性）
2. pnpm vue-tsc --noEmit   （类型检查 30 秒级反馈）
3. snapshot 分支            （pre-zombie-cleanup-<日期>，双远端 push）
4. 分批 commit              （Batch-1~5，单 commit 单 revert 兜底）
5. mcp-playwright smoke     （仅 Batch-5 高风险批次，跑 P0 矩阵）
```

---

## 9. 参考文件索引

- `AGENTS.md` — AI Agent 项目承接规范（六大锚点）
- `.cursorrules` / `.windsurfrules` — 前端布局与 MCP 规则
- `.cursor/rules/14-local-build.mdc` — 本机构建硬规则（v24 + v26 待补）
- `docs/ibms-unified-data-source-plan.md` — IBMS 治理主计划（M0~M7，本会话补 M1.5/M1.6）
- `docs/ibms-coverage-matrix.md` — M1 109 行业务矩阵
- `docs/ibms-bidirectional-gap.md` — M1.5 双向校验
- `docs/ibms-zombie-candidates.md` — M1.6 僵尸清单
- `docs/ibms-unified-progress.md` — 进度跟踪（append-only）
- `docs/session-handoff-20260506-v24.md` — 上一份 handoff（reply 路径 tenantId 透传）
- `docs/session-handoff-20260506-v25.md` — 本文件
