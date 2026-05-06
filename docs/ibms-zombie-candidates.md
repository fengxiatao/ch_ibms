# IBMS 全前端僵尸候选清单（M1.6 阶段产出）

> 配套主计划：`docs/ibms-unified-data-source-plan.md`
> 生成日期：2026-05-06（本会话不删除任何文件）
> 算法：BFS 引用图（活根集合 → BFS 标可达 → 全集 - 可达 = 候选僵尸）
> 工具：`.tmp_sql/m1-zombie-bfs.py`（可重复执行）
>
> **统计**：全集 1916 / 活根 1259 / 可达（活）1834 / 僵尸候选 74 (排除 8 个 *.test.ts → Z0)

---

## 0. 算法保证与已知盲区

### 0.1 BFS 算法保证（用户硬约束）

> **只要任意活路径可达某文件，该文件就不被判为僵尸。**
>
> 形式化：`visited` 是从活根集合出发的可达闭包（被 visited 中文件引用的文件 ∈ visited），
> `Zombies = AllFiles - visited`。**与是否被 zombie 引用无关**。

### 0.2 活根集合（94 个 + `views/**` + `components/DiyEditor/**` + 5 个目录树）

```text
单文件 5 个：    main.ts, App.vue, permission.ts, router/index.ts, router/modules/remaining.ts
整目录入根：     store, layout, plugins, directives, locales, styles, config, types,
                views        ← 因 utils/routerHelper.ts:7 用 import.meta.glob("../views/**/*.{vue,tsx}")
                components/DiyEditor  ← 因 mobile/index.ts:15 用 glob("./*/*.vue")
```

### 0.3 已知盲区（动态 import 字符串拼接）

扫到 2 个文件含模板字符串 import（`import(\`...${x}...\`)`）：

- `yudao-ui-admin-vue3/src/hooks/web/useLocale.ts`
- `yudao-ui-admin-vue3/src/plugins/vueI18n/index.ts`

已通过把 `locales/` 整目录纳入活根抵消该风险。BFS 暂未扫描 `.scss/.css/.json` 中引用，但本次候选无样式/JSON 资产。

---

## Z1 高置信僵尸（共 37 项 — 旧 API / 独立 hooks / 业务 utils）

特征：≥60 天未修改 + 0 引用（已抽 5 项手工 grep 复核）+ 不在通用基础库目录。
**清理风险低**，建议下次会话作为 Batch-2/Batch-4 主体清理。

| 最后修改 | 大小(字节) | 路径 | 处置 Batch |
|---|---|---|---|
| 2025-09-27 | 2436 | `yudao-ui-admin-vue3/src/api/access/accessControl/accessAuthorization/index.ts` | Batch-4 |
| 2025-09-28 | 1545 | `yudao-ui-admin-vue3/src/api/access/accessControl/accessRecord/index.ts` | Batch-4 |
| 2025-09-28 | 2587 | `yudao-ui-admin-vue3/src/api/access/accessControl/device/index.ts` | Batch-4 |
| 2025-11-12 | 2143 | `yudao-ui-admin-vue3/src/api/access/accessControl/dispatchCenter/index.ts` | Batch-4 |
| 2025-11-12 | 2572 | `yudao-ui-admin-vue3/src/api/access/accessControl/guardManagement/index.ts` | Batch-4 |
| 2025-09-28 | 4708 | `yudao-ui-admin-vue3/src/api/access/elevator/elevatorManagement/index.ts` | Batch-4 |
| 2026-03-05 | 4518 | `yudao-ui-admin-vue3/src/api/access/parking/parkingManagement/index.ts` | Batch-4 |
| 2025-09-28 | 3342 | `yudao-ui-admin-vue3/src/api/access/vehicle/vehicleManagement/index.ts` | Batch-4 |
| 2025-09-26 | 325 | `yudao-ui-admin-vue3/src/api/bpm/simple/index.ts` | Batch-4 |
| 2025-11-12 | 8102 | `yudao-ui-admin-vue3/src/api/fire/emergencyResponse/index.ts` | Batch-2 |
| 2025-11-12 | 4221 | `yudao-ui-admin-vue3/src/api/fire/fireAlarm/index.ts` | Batch-2 |
| 2025-11-12 | 6766 | `yudao-ui-admin-vue3/src/api/fire/fireSafety/index.ts` | Batch-2 |
| 2025-11-12 | 5895 | `yudao-ui-admin-vue3/src/api/fire/fireSuppression/index.ts` | Batch-2 |
| 2025-11-12 | 6146 | `yudao-ui-admin-vue3/src/api/fire/smokeControl/index.ts` | Batch-2 |
| 2025-11-16 | 1490 | `yudao-ui-admin-vue3/src/api/iot/camera.ts` | Batch-4 |
| 2025-11-12 | 3202 | `yudao-ui-admin-vue3/src/api/iot/device/config.ts` | Batch-4 |
| 2025-10-13 | 2331 | `yudao-ui-admin-vue3/src/api/iot/device/display/index.ts` | Batch-4 |
| 2025-10-26 | 1077 | `yudao-ui-admin-vue3/src/api/iot/device/event.ts` | Batch-4 |
| 2025-09-26 | 1298 | `yudao-ui-admin-vue3/src/api/iot/device/group/index.ts` | Batch-4 |
| 2025-10-22 | 508 | `yudao-ui-admin-vue3/src/api/iot/device/jobConfig.ts` | Batch-4 |
| 2025-10-22 | 5276 | `yudao-ui-admin-vue3/src/api/iot/device/location.ts` | Batch-4 |
| 2025-10-26 | 1563 | `yudao-ui-admin-vue3/src/api/iot/device/service.ts` | Batch-4 |
| 2025-10-22 | 1093 | `yudao-ui-admin-vue3/src/api/iot/jobType/index.ts` | Batch-4 |
| 2025-10-13 | 2811 | `yudao-ui-admin-vue3/src/api/iot/product/category/index.ts` | Batch-4 |
| 2025-10-22 | 991 | `yudao-ui-admin-vue3/src/api/iot/product/jobConfig.ts` | Batch-4 |
| 2025-11-25 | 546 | `yudao-ui-admin-vue3/src/api/iot/recording/types.ts` | Batch-4 |
| 2025-10-22 | 2195 | `yudao-ui-admin-vue3/src/api/iot/task/index.ts` | Batch-4 |
| 2025-11-23 | 2710 | `yudao-ui-admin-vue3/src/api/iot/videoView/index.ts` | Batch-4 |
| 2026-03-13 | 2832 | `yudao-ui-admin-vue3/src/api/security/camera/index.ts` | Batch-4 |
| 2025-12-06 | 5325 | `yudao-ui-admin-vue3/src/assets/floorplan-icons/iconConfig.ts` | Batch-4 |
| 2025-10-13 | 5917 | `yudao-ui-admin-vue3/src/hooks/iot/useDeviceDisplay.ts` | Batch-4 |
| 2026-03-05 | 10437 | `yudao-ui-admin-vue3/src/hooks/useAlertWebSocket.ts` | Batch-4 |
| 2025-12-23 | 4815 | `yudao-ui-admin-vue3/src/utils/accessEventInsertion.ts` | Batch-4 |
| 2025-12-15 | 1737 | `yudao-ui-admin-vue3/src/utils/cardUtils.ts` | Batch-4 |
| 2025-10-30 | 5058 | `yudao-ui-admin-vue3/src/utils/coordinate.ts` | Batch-4 |
| 2025-12-15 | 4082 | `yudao-ui-admin-vue3/src/utils/deviceTreeUtils.ts` | Batch-4 |
| 2025-12-15 | 2000 | `yudao-ui-admin-vue3/src/utils/personCodeUtils.ts` | Batch-4 |

## Z2 中等可疑（共 24 项 — 独立组件库 / 通用 web hooks）

特征：90~700+ 天未修改 + 模块化引用模式（可能在内部 glob 注册），需更长 review 期。
**清理风险中**，建议在 Batch-3 验证 build + smoke 后再处置。

| 最后修改 | 大小(字节) | 路径 | 处置 Batch |
|---|---|---|---|
| 2025-10-13 | 1121 | `yudao-ui-admin-vue3/src/components/IoT/DeviceComponents/index.ts` | Batch-4 |
| 2025-09-26 | 1101 | `yudao-ui-admin-vue3/src/components/Tinyflow/ui/index.d.ts` | Batch-3 |
| 2025-09-26 | 329496 | `yudao-ui-admin-vue3/src/components/Tinyflow/ui/index.umd.js` | Batch-3 |
| 2025-09-26 | 9175 | `yudao-ui-admin-vue3/src/components/bpmnProcessDesigner/package/penal/listeners/template.js` | Batch-3 |
| 2025-09-26 | 220 | `yudao-ui-admin-vue3/src/components/bpmnProcessDesigner/src/highlight/index.js` | Batch-3 |
| 2025-09-26 | 694 | `yudao-ui-admin-vue3/src/components/bpmnProcessDesigner/src/modules/custom-renderer/CustomRenderer.js` | Batch-3 |
| 2025-09-26 | 147 | `yudao-ui-admin-vue3/src/components/bpmnProcessDesigner/src/modules/custom-renderer/index.js` | Batch-3 |
| 2025-09-26 | 355 | `yudao-ui-admin-vue3/src/components/bpmnProcessDesigner/src/modules/rules/CustomRules.js` | Batch-3 |
| 2025-09-26 | 132 | `yudao-ui-admin-vue3/src/components/bpmnProcessDesigner/src/modules/rules/index.js` | Batch-3 |
| 2025-09-26 | 1210 | `yudao-ui-admin-vue3/src/components/bpmnProcessDesigner/src/translations.ts` | Batch-3 |
| 2025-09-26 | 962 | `yudao-ui-admin-vue3/src/components/bpmnProcessDesigner/src/utils/directive/clickOutSide.js` | Batch-3 |
| 2025-09-26 | 224 | `yudao-ui-admin-vue3/src/components/bpmnProcessDesigner/src/utils/index.js` | Batch-3 |
| 2025-09-26 | 1237 | `yudao-ui-admin-vue3/src/components/bpmnProcessDesigner/src/utils/xml2json.js` | Batch-3 |
| 2023-10-30 | 3071 | `yudao-ui-admin-vue3/src/components/parkDigitalTwin/modules/GeometryEdit/index.js` | Batch-3 |
| 2023-10-30 | 361 | `yudao-ui-admin-vue3/src/components/parkDigitalTwin/modules/Lights/Light.js` | Batch-3 |
| 2023-10-30 | 1483 | `yudao-ui-admin-vue3/src/components/parkDigitalTwin/modules/SunLensflare/index.js` | Batch-3 |
| 2023-10-30 | 1480 | `yudao-ui-admin-vue3/src/components/parkDigitalTwin/modules/Weather/SunLensflare.js` | Batch-3 |
| 2026-04-23 | 1836 | `yudao-ui-admin-vue3/src/components/parkDigitalTwin/modules/WeatherControl/index.js` | Batch-3 |
| 2023-10-30 | 9984 | `yudao-ui-admin-vue3/src/components/parkDigitalTwin/shaders/fragmentGress.js` | Batch-3 |
| 2025-09-26 | 358 | `yudao-ui-admin-vue3/src/hooks/web/useEmitt.ts` | Batch-4 |
| 2025-09-26 | 1258 | `yudao-ui-admin-vue3/src/hooks/web/useGuide.ts` | Batch-4 |
| 2025-09-26 | 487 | `yudao-ui-admin-vue3/src/hooks/web/useNetwork.ts` | Batch-4 |
| 2025-09-26 | 1909 | `yudao-ui-admin-vue3/src/hooks/web/useTimeAgo.ts` | Batch-4 |
| 2025-09-26 | 8356 | `yudao-ui-admin-vue3/src/utils/domUtils.ts` | Batch-4 |

## Z3 低风险候选（共 13 项 — components 通用 1-2 行 export 文件）

特征：`yudao-ui-admin-vue3/src/components/<Name>/index.ts`，文件 ≤200 字节，
内容形如 `export { default as <Name> } from "./src/<Name>.vue"`。

已对 `Search/Tooltip/Sticky/Highlight/Verifition` 5 项做精确 grep（`from '@/components/<Name>'`）：**0 引用**。
但因 `index.vue` 与 `src/<Name>.vue` 仍存在，**真正消费者可能直接 import 子文件**而绕过 index.ts，
删除 index.ts 不会影响实际功能，但保险起见列入低风险档。

| 最后修改 | 大小 | 路径 | 处置 Batch |
|---|---|---|---|
| 2025-09-26 | 78 | `yudao-ui-admin-vue3/src/components/ConfigGlobal/index.ts` | Batch-5 |
| 2025-09-26 | 93 | `yudao-ui-admin-vue3/src/components/ContentDetailWrap/index.ts` | Batch-5 |
| 2025-09-26 | 57 | `yudao-ui-admin-vue3/src/components/Error/index.ts` | Batch-5 |
| 2025-09-26 | 69 | `yudao-ui-admin-vue3/src/components/Highlight/index.ts` | Batch-5 |
| 2025-09-26 | 60 | `yudao-ui-admin-vue3/src/components/IFrame/index.ts` | Batch-5 |
| 2025-09-26 | 63 | `yudao-ui-admin-vue3/src/components/Infotip/index.ts` | Batch-5 |
| 2025-09-26 | 72 | `yudao-ui-admin-vue3/src/components/JsonEditor/index.ts` | Batch-5 |
| 2025-09-26 | 78 | `yudao-ui-admin-vue3/src/components/OperateLogV2/index.ts` | Batch-5 |
| 2025-09-26 | 60 | `yudao-ui-admin-vue3/src/components/Search/index.ts` | Batch-5 |
| 2025-09-26 | 60 | `yudao-ui-admin-vue3/src/components/Sticky/index.ts` | Batch-5 |
| 2025-09-26 | 63 | `yudao-ui-admin-vue3/src/components/Tooltip/index.ts` | Batch-5 |
| 2025-09-26 | 60 | `yudao-ui-admin-vue3/src/components/Verifition/index.ts` | Batch-5 |
| 2025-09-26 | 125 | `yudao-ui-admin-vue3/src/components/XButton/index.ts` | Batch-5 |

## Z0 排除（共 8 项 — 测试文件，不计入僵尸）

Vitest 测试文件由 `vitest run` 自动执行，不通过 import 链暴露给应用。**保留**。

| 最后修改 | 路径 |
|---|---|
| 2025-12-23 | `yudao-ui-admin-vue3/src/utils/accessEventInsertion.test.ts` |
| 2025-12-23 | `yudao-ui-admin-vue3/src/utils/accessEventStatistics.test.ts` |
| 2025-12-23 | `yudao-ui-admin-vue3/src/utils/accessEventTypes.test.ts` |
| 2025-12-23 | `yudao-ui-admin-vue3/src/utils/accessEventWebSocket.test.ts` |
| 2025-12-15 | `yudao-ui-admin-vue3/src/utils/authModeUtils.test.ts` |
| 2025-12-15 | `yudao-ui-admin-vue3/src/utils/cardUtils.test.ts` |
| 2025-12-22 | `yudao-ui-admin-vue3/src/utils/deviceTreeUtils.test.ts` |
| 2025-12-15 | `yudao-ui-admin-vue3/src/utils/personCodeUtils.test.ts` |

---

## Step 12 抽样人工复核（5 项）

| # | 候选 | 等级 | grep `from '<spec>'` | 结论 |
|---|---|---|---|---|
| 1 | `api/security/camera/index.ts` | Z1 | `@/api/security/camera` → 0 hits | ✅ 真僵尸 |
| 2 | `api/iot/recording/types.ts` | Z1 | `@/api/iot/recording` → 0 hits | ✅ 真僵尸 |
| 3 | `api/iot/camera.ts` | Z1 | `@/api/iot/camera` (无后缀) → 0 hits | ✅ 真僵尸（注意 `cameraPreset`/`cameraCruise` 是不同文件） |
| 4 | `utils/coordinate.ts` | Z1 | `@/utils/coordinate` (无后缀) → 0 hits | ✅ 真僵尸（`utils/coordinate/transform.ts` 仍活，但是不同文件） |
| 5 | `components/Search/index.ts` 等 5 项 | Z3 | `@/components/{Search,Tooltip,Sticky,Highlight,Verifition}` → 0 hits | ✅ 真僵尸（但子目录 src/*.vue 仍可能被直接引用） |

---

## 后续清理执行规约（M1.7 / 下次会话）

**本会话硬约束**：0 个文件删除。本节为下次会话的执行指南。

### 五层防御（按性价比排序，不要跳过任何一层）

1. **`pnpm build` 验证**（最低成本最高确定性）：构建产物中实际进入 bundle 的源文件清单 vs 本 BFS 的活集合，不一致即说明 BFS 漏抓。
2. **`pnpm vue-tsc --noEmit` 类型检查**：30 秒级反馈，源文件 import 路径错立即报错。
3. **删除前打 snapshot 分支** ：`git branch pre-zombie-cleanup-<日期> HEAD`，并 push 双远端。
4. **分批删除 + 单 commit**：每批一个 commit，build + tsc 通过再下一批；失败 `git revert <sha>` 单批回滚。
5. **mcp-playwright smoke**（仅 Batch-5 高风险批次）：跑 M1 矩阵 P0 页面（28 项），监控 console 是否有 `Failed to fetch dynamically imported module`。

### 分批策略（按风险递增）

| Batch | 候选 | 数量 | 验证 | 风险 |
|---|---|---|---|---|
| Batch-1 | `*.test.ts` 测试文件（Z0） | 8 | `pnpm test` | 极低 |
| Batch-2 | `api/fire/*` 6 个（前端 0 个 `views/fire/*`） | 5 | build | 低 |
| Batch-3 | 独立组件库 `parkDigitalTwin/bpmnProcessDesigner/Tinyflow/IoT/DeviceComponents/hooks/web/*`（Z2） | 18 | build + dev server | 低-中 |
| Batch-4 | 旧 `api/{access,iot}/*` + 业务 utils + hooks（Z1 主体） | 32 | build + 手工/playwright P0 smoke | 中 |
| Batch-5 | `components/<Name>/index.ts` 通用 1-行 export（Z3） | 13 | build + playwright 全量 P0/P1 smoke + 7 天观察期 | 高 |

### 启动指令模板（粘贴给 M1.7 执行 AI）

```text
我承接 M1.7 僵尸文件清理。请按 docs/ibms-zombie-candidates.md 的"后续清理执行规约"五层防御 + 分批策略执行。
启动前：
  1. git branch pre-zombie-cleanup-<今日> HEAD && git push origin chvm1 同名分支
  2. 在 yudao-ui-admin-vue3 跑 pnpm install && pnpm build && pnpm vue-tsc --noEmit 取 baseline
从 Batch-1 开始，每批：git rm + commit + build + tsc，失败 revert。Batch-5 前需启动 yudao-server + playwright smoke。
```

### DoD 检查表（M1.6 本会话）

| # | 检查项 | 状态 |
|---|---|---|
| 10 | BFS 脚本落盘 + 可重复执行 | ✅ `.tmp_sql/m1-zombie-bfs.py` |
| 11 | 含 Z0/Z1/Z2/Z3 四档分级（Z0=8, Z1=37, Z2=24, Z3=13） | ✅ |
| 12 | Z1 抽样 5 项已人工 grep 确认 0 引用 | ✅ |
| 13 | **本会话 0 个文件被删除** | ✅ |

### BFS 改进历史（本会话）

| # | 问题 | 修复 |
|---|---|---|
| 1 | 初版仅活根 5 文件 + 8 目录 → 1199 候选（误判 `views/*` 大量页面） | 把 `views/**` 整树纳入活根（routerHelper.ts 用 `import.meta.glob` 验证） |
| 2 | `resolve_spec` 仅返回首个命中，导致 `@/components/Pagination` 解析到 `index.vue` 后 `index.ts` 被判僵尸 | 改为返回 **所有** 兄弟 index 候选，避免误判（候选数从 84 → 82） |
