# Session Handoff v40 — 2026-05-10

> 承接入口：先读本 handoff + `AGENTS.md` + `.cursorrules` + `.windsurfrules`；不要翻历史 vN。
> 中文回复；MySQL 只用 `mcp4_mysql_query`（mysql-ibms / `ch_ibms` 库）；后端 48888、前端 3000。

---

## 锚点 1：项目骨架（无变化）

- **后端**：`ruoyi-vue-pro/`（Spring Boot 3 + 多模块 Maven）
  - 本次涉及模块：`yudao-module-iot`（building 业务）
- **管理端**：`yudao-ui-admin-vue3/`（Vue3 + Vite + pnpm）
  - 本次涉及目录：`src/views/iot/building/newlight/{overview,device,control}/`

---

## 锚点 2：本次会话变更（v39 → v40）

### Commit

| Hash | 标题 |
|---|---|
| `c97e97f` | feat(GAP-013): 智能照明 3 大页接真实 API + 后端 statistics stub 填实 |

### 关键技术发现 / 修改清单

**后端**（`ruoyi-vue-pro/yudao-module-iot/yudao-module-iot-biz`）：

- `service/building/IbmsLightingServiceImpl.java` —— `getStatistics()` 重写：
  - 删除 mock 字面量 `150` / `85.5`
  - 补全 `gatewayOnlineCount` / `controllerOnlineCount` / `lightTotalCount` / `todayAlarmCount`
  - `totalPower` / `currentPower` 改为基于 `ibms_lighting_circuit.rated_power × brightness/100` 真实计算（单位 kW，保留 2 位小数）
- `dal/dataobject/building/IbmsLightingCircuitDO.java` —— 新增 `lightCount` 字段

**DDL & 种子**（`ruoyi-vue-pro/sql/`）：

- `ibms_smart_building.sql` —— `ibms_lighting_circuit` 加 `light_count INT` 列
- `ibms_smart_building_data.sql` —— 28 行回路按 `circuit_type` 推算补 `light_count`

**前端**（`yudao-ui-admin-vue3/src/views/iot/building/newlight/`）：

- `overview/index.vue` —— 接 `getStatistics + getAlarmPage`；能耗趋势 / 节能排名空态展示「端点开发中」；本会话修复 `pageSize 200 → 100`（后端硬限）
- `device/index.vue` —— 接 `getGatewayPage + getControllerPage + getStatistics`；删除 5 列（功率/能耗/运行时/控制器使用率）+ 删除 2 整块（使用率分析、历史查询）
- `control/index.vue` —— 接 `getCircuitPage + controlCircuit + dimCircuit`；按 `circuitType=2` 分 `dimming` / `normal` tab；亮度滑动 250ms 防抖；本会话修复 `pageSize 200 → 100`

### vue-tsc

- newlight：0 errors
- 全局：697 → 694（-3 零回归）

### 浏览器验收（admin / tenant=1，本会话已通过）

| 页面 | 验证项 | 结果 |
|---|---|---|
| `/iot/building/newlight/overview` | 在线率 100% / 网关 3 / 控制器 4 / 灯具 54 / 回路 14（9 开 5 关）/ 总功率 1.24kW | ✅ 0 console error |
| `/iot/building/newlight/device` | 7 台设备列表（3 网关 + 4 控制器）/ 总功率 1.24kW / 当前 0.78kW | ✅ 0 console error |
| `/iot/building/newlight/control` | 普通 tab 10 条 + 调光 tab 4 条 = 14 回路；调光显示 0/50/80/100% | ✅ 0 console error |

### 单租户隔离备忘（重要）

`ibms_lighting_*` 三表 DB 总量分布在 `tenant_id IN (1, 162)` 两个租户，**各租户结构相同**：

- 每租户：3 网关 + 4 控制器 + 14 回路 + `SUM(light_count) = 54`
- API 受租户隔离限制只返回当前租户数据 —— 这是**单源数据 + 多租户的正确行为**
- 交接里口径"28 行 / 103 灯"是两租户加和；以单租户视图（14 / 54）为准

---

## 锚点 3：CI 访问

- **Drone Server**：`http://test.sanligz.com.cn`
- **CH 仓库**：`fengxiatao/ch_ibms`
- **API 基址**：`http://test.sanligz.com.cn/api/repos/fengxiatao/ch_ibms`
- **Token**：与 jingyu 项目共用同一 drone 用户 token（沿用 v39，未变；不写入代码 / commit）
- **本次未触发 CI**（commit 尚未 push，等用户显式批准）

---

## 锚点 4：MySQL 连接

- **唯一允许工具**：`mcp4_mysql_query`（`mysql-ibms` / 本地 `ch_ibms`）
- 写操作（INSERT/UPDATE/DELETE/DDL）允许通过 MCP 直接执行；破坏性操作必先征得用户批准
- 禁用：`mcp5/6/7_mysql_query`（online / jingyu / parkingspace）

---

## 锚点 5：本机构建硬规则（沿用 `.cursor/rules/14-local-build.mdc`）

- AI 不得自动 `git push`、不得自动启动 yudao-server / pnpm dev
- `git add` 必须精确路径，禁 `.` / `-A` / `*`
- Maven 在 `cwd=e:\ch\ruoyi-vue-pro`，必带 `-q`，长输出重定向
- 单模块 `-pl <模块>`，禁 `-am`
- 临时文件放 `.tmp_sql/` 或 `tmp-check/`

---

## 锚点 6：下一步候选 + 给下次会话的建议

### 待办（按优先级）

1. **【需用户显式批准】push 当前 commit `c97e97f` 到 `origin/feature/m2-b-phase2-iot-device-do-cleanup`**
   - DoD：CI 在 Drone 出现新 build 且 `pnpm-build` step 通过
2. **GAP-013 子任务 B：能耗趋势 / 节能排名端点开发**
   - 后端补 `/admin-api/iot/building/lighting/energy-trend` + `/saving-rank`（聚合 `ibms_lighting_energy_*`）
   - 前端 overview 把"端点开发中"空态切回真实图表
   - DoD：0 mock、单租户视图非空、vue-tsc 0 回归
3. **GAP-013 子任务 C：照明告警 `/alarm/handle` 接入**
   - control / overview 列表加"处理"按钮调后端
4. **租户 162 重复数据排查（可选）**
   - 与 GAP 主表对齐策略一致，看是否需要清理 `tenant_id=162` 的 lighting 种子
5. **vue-tsc 全局 694 → 0 长期推进**（M6c）

### 给下次会话的建议

- 承接时先 `git log --oneline -n 3`，确认 `c97e97f` 是否已 push（若已 push 则跳过待办 1）
- 调 `getStatistics` 等分页接口务必 **`pageSize ≤ 100`**（后端硬限），新页面记得 grep `pageSize: 2` 防回归
- 单租户视角口径以 `tenant_id=1` 为准，与 v40 浏览器验收一致

---

## 关键访问凭据（短期）

- admin token（v40 验收用）：`f68dac62567e4a7893e90bec918d18f4`（`system_oauth2_access_token` 中可重取）
- 凭据轮换：会话结束即可失效，无需特别处理
