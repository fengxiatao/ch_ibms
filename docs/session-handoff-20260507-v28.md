---
description: CH（长辉 IBMS）项目 - 2026-05-07 v28 会话交接
---

# CH 项目 v28 会话交接 - M1.7 Batch-1+3+4 落地（60 文件清理）

> 必读：`AGENTS.md` + 本文件 + `docs/ibms-zombie-candidates.md`
> **不要**翻历史 vN（含 v27），除非用户明确要求。
> 当前分支：`snapshot/20260423-full`（与 v27 同分支）。
> Snapshot 兜底分支：`pre-zombie-cleanup-20260507`（HEAD 之前的 27bdadb，未 push）。

---

## 1. 项目骨架（不变）

- **后端**：`ruoyi-vue-pro/yudao-module-iot` Spring Boot 3 + MyBatis Plus
- **管理端**：`yudao-ui-admin-vue3/` Vue3 + Vite + Element Plus + pnpm
- **DB**：`ch_ibms` @ 127.0.0.1（仅 `mcp4_mysql_query` 只读访问）

## 2. 本次会话变更（v27 → v28）

| commit | Batch | 文件 | 删除行数 |
|---|---|---|---|
| `c86e5f8` | M1.7 Batch-1 | 4 个孤立 test | -1413 |
| `648e0c1` | M1.7 Batch-3 | 18 个 Z2 独立组件库 | -981 |
| `5453679` | M1.7 Batch-4 | 38 个 Z1+Z2 旧 api/hooks/utils | -4256 |
| `b9ef5d9` | fix | 复活 `floorplan-icons/iconConfig.ts` (BFS 漏抓 `export *`) | +247 |
| **合计** | | **60 删 / 1 复活 = 净 59 删** | **-6403** |

### 2.1 决策依据：方案 B（"被测+测试同步"严格语义）

v27 §5 描述的 batch 数量与 `docs/ibms-zombie-candidates.md` 不一致。本次按以下严格规则执行：

- **Batch-1 = 4 test**（仅删被测 utils 同时在 Z1 待删的 test）
  - 保留 `accessEventStatistics/Types/WebSocket.test.ts` + `authModeUtils.test.ts`（被测仍活）
- **Batch-3 = 18 项**（Z2 中标记 Batch-3 的独立组件库：bpmnProcessDesigner 10 + parkDigitalTwin 6 + Tinyflow 2）
- **Batch-4 = 38 项**（Z1 中 Batch-4 = 32，Z2 中 Batch-4 = 6）

未涉及 Z3 的 13 个 components/`<Name>/index.ts`（Batch-5，需更高强度 smoke）。

### 2.2 评估并放弃的候选

| 候选 | 放弃原因 |
|---|---|
| **(a) M2-B：access 单源化 GAP-011** | `IbmsDeviceDO` 缺 `state/onlineTime/offlineTime/gatewayId/config:DeviceConfig` 等关键字段；其中 `AccessDeviceConfig`（韦根 IO/读卡器配置）在 IbmsDeviceDO 完全无等价物。**实际工程量 1.5~2 天**，远超 v27 §5 "半天"估算。需独立会话。 |
| **(b) M2-C：building-visual-dashboard 聚合 GAP-002** | 后端新 controller + 前端 1087 行重写，需独立会话。 |

### 2.3 验证结果 ✅

```text
TS baseline (v27):                  1692 errors
TS after Batch-1+3+4 + fix:         1685 errors  (下降 7, 无新增)
pnpm build:dev (首次):              失败  →  发现 iconConfig.ts 被 export * 重导出
pnpm build:dev (fix 后):            成功 (Build successful, dist 产出)
剩余 rollup warnings (预先存在):     api/iot/device/device 缺 getDevice/createDevice,
                                    api/iot/product/product 缺 getSimpleProductList
日志: e:/ch/.tmp_sql/m17-vue-tsc-20260507.log,
     e:/ch/.tmp_sql/m17-pnpm-build2-20260507.log
```

### 2.4 BFS 算法盲区发现（M1.8 候选改进）

`.tmp_sql/m1-zombie-bfs.py` 未识别以下模式，导致 `iconConfig.ts` 误判为僵尸：

```ts
// src/assets/floorplan-icons/index.ts:74
export * from './iconConfig'   // BFS 未追踪此重导出
```

真实消费者通过 `import { ICON_CONFIGS } from '@/assets/floorplan-icons'` 访问 iconConfig 的具名导出，BFS 只看到 `index.ts` 是活的，未跟踪 `index.ts` 再导出的 `./iconConfig`。

**改进方向**：BFS resolve 目标文件后需扫描其 AST 中的 `export * from 'X'` / `export { a, b } from 'X'` / `import * as X from` 再把 X 拉入活集合。

## 3. 当前运行时状态

- Server PID 6080 监听 48888（v27 启动，未杀）
- 前端：未本地跑 dev server
- HEAD：`b9ef5d9`（snapshot/20260423-full，本地领先 origin/chvm1 **四个** commit：c86e5f8 / 648e0c1 / 5453679 / b9ef5d9）

## 4. 兜底回滚指引

```powershell
# 全部回滚（回到 v27 HEAD）
git reset --hard 27bdadb

# 单批回滚（保留其他 batch）
git revert 5453679    # 仅回滚 Batch-4
git revert 648e0c1    # 仅回滚 Batch-3
git revert c86e5f8    # 仅回滚 Batch-1

# Snapshot 兜底分支
git switch pre-zombie-cleanup-20260507   # 直接切到 27bdadb
```

## 5. 下一步候选

| 候选 | 价值 | 风险 | 建议 |
|---|---|---|---|
| **push 三批 commit 到双远端** | 高（规定动作） | 无 | `git push origin snapshot/20260423-full && git push chvm1 snapshot/20260423-full && git push origin pre-zombie-cleanup-20260507 && git push chvm1 pre-zombie-cleanup-20260507` |
| **M2-B：access 单源化（GAP-011）** | 高 | **高** | 必须开 `feature/m2-b-access-single-source` 分支；先扩展 `IbmsDeviceDO`/`IbmsDeviceMapper`（runtime JOIN 视图 + AccessDeviceConfig 迁移到 extra），再逐个 service 重写 |
| **M2-C：building-visual-dashboard 聚合（GAP-002）** | 高 | 中 | 后端新 `IbmsSpaceController.dashboard-stats` + 前端 1087 行重写 |
| **M1.7 Batch-2 + Batch-5** | 低 | 中-高 | Batch-2（5 个 fire/* api，已在 v26 c4 commit 3d9948f 落地一部分，需复核）；Batch-5（13 个 components 1-行 export，需 playwright P0 全量 smoke + 7 天观察期） |
| **治理：清理僵尸 java 进程** | 低 | 低 | v27 §5 提到的 8 个旧 java.exe（PID 11240/13464/18720/23204/27744/28788/32888/35096） |

## 6. CI 访问（不变）

- Drone Server：`http://test.sanligz.com.cn`（= `192.168.1.253`）
- CH 仓库：`fengxiatao/ch_ibms`
- API 基址：`http://test.sanligz.com.cn/api/repos/fengxiatao/ch_ibms`
- Token：与 jingyu 共用 drone token（见前序 handoff 凭据段，禁止入库）

## 7. MCP 数据库连接（强制）

- **唯一允许**：`mcp4_mysql_query`（`mysql-ibms` → `ch_ibms` @ 127.0.0.1，**只读**）
- **写操作**：`run_command` + mysql 命令行 + 用户明确批准
- **禁用**：`mcp5/6/7_*`（线上库 / jingyu / parking）

## 8. 本机构建硬规则（v27 继承 + v28 强化）

- 见 `.cursor/rules/14-local-build.mdc`
- vue-tsc 必须设 `$env:NODE_OPTIONS='--max-old-space-size=8192'`
- baseline TS 错误数：**1692**（v27 起未变；本次 cleanup 应保持或下降）
- Maven build 前**必杀 48888 旧进程**；fat-jar 校验必用嵌套 jar 抽取法
- **新增（v28）**：M1.7 清理硬约束 = 必须先有 snapshot 分支（pre-zombie-cleanup-`<日期>`） + 分批 commit + 每批 vue-tsc/build 验证

## 9. 新会话承接句式

```text
我承接 IBMS 治理。当前进度：M1.7 Batch-1+3+4 已落地（60 文件，commits c86e5f8 / 648e0c1 / 5453679），分支 snapshot/20260423-full。
请先只读 docs/session-handoff-20260507-v28.md + AGENTS.md，
然后等我说：(a) push 双远端 / (b) 起 M2-B 新分支 / (c) M2-C 聚合 / (d) M1.7 Batch-2/5。
MySQL 仅用 mcp4_mysql_query；mvn 前必杀 48888；vue-tsc 前 $env:NODE_OPTIONS='--max-old-space-size=8192'。
```
