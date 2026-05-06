---
description: CH（长辉 IBMS）项目 - 2026-05-06 v26 会话交接
---

# CH 项目 v26 会话交接 - M2-A + M1.7-Batch2 + M2-D 闭环

> 必读：`AGENTS.md` + 本文件 + `docs/ibms-unified-progress.md` + `docs/ibms-coverage-matrix.md` + `docs/ibms-bidirectional-gap.md`
> **不要**翻历史 vN（除非用户明确要求）。

---

## 1. 项目骨架（不变）

- **后端**：`ruoyi-vue-pro/yudao-module-iot` Spring Boot 3 + MyBatis Plus
- **管理端**：`yudao-ui-admin-vue3/` Vue3 + Vite + Element Plus + pnpm
- **DB**：`ch_ibms` @ 127.0.0.1（仅 `mcp4_mysql_query` 只读访问）

## 2. 本次会话变更（v25 → v26）

| commit | 阶段 | 内容 |
|---|---|---|
| `ed251c6` | M2-A | `iot/access/visual-dashboard` 接入 statistics + hourly 端点（前端层） |
| `1309748` | docs | M2-A 进度+矩阵入库 |
| `3d9948f` | M1.7-Batch2 | 删除 5 个未引用 `api/fire/*/index.ts` 僵尸文件 |
| `0b62d00` | **M2-D** | **AccessDashboard 6 端点全面对接真实表 `iot_access_event_log`（GAP-001 根因修复）** |
| `a0396d4` | docs | M2-D + Batch-2 入库；GAP-001 状态升级为已完成 |

**Snapshot 分支**：`pre-zombie-cleanup-20260506`（删除前预 push，已在双远端）

### 2.1 M2-D 重大根因发现

原 `AccessDashboardServiceImpl` 注入 `AccessRecordMapper`/`AccessAlarmMapper`，DO 标注的 `iot_access_record`/`iot_access_alarm` 表在 `ch_ibms` 数据库中**不存在**：

- statistics / real-time 端点：所有查询运行时抛 SQLException → 全局兜底为空响应 → 前端永远 fallback 到 mock（M2-A 的"接入真实数据"实际是空操作！）
- trend / device-status / heatmap / abnormal-events 端点：本来就是 stub（return new ArrayList/HashMap）

**修复**：迁到真实表 `iot_access_event_log`（56448 行真数据，时间跨度 2025-12-22 ~ 2026-04-20）

### 2.2 M2-D 改动文件

1. `ruoyi-vue-pro/yudao-module-iot/yudao-module-iot-biz/src/main/java/cn/iocoder/yudao/module/iot/dal/mysql/access/IotAccessEventLogMapper.java`
   - 加 7 个聚合 SQL：`countByTimeRange` / `countAlarmsByTimeRange` / `selectDailyTrend` / `selectHourlyTrend` / `selectHeatmap` / `selectEventTypeDistribution` / `selectAbnormalList`
2. `ruoyi-vue-pro/yudao-module-iot/yudao-module-iot-biz/src/main/java/cn/iocoder/yudao/module/iot/service/access/AccessDashboardServiceImpl.java`
   - 完全重写：弃用 record/alarm Mapper；6 端点全部走 IotAccessEventLogMapper；4 stub 落地；同比增长率改用昨日真实数据
3. `yudao-ui-admin-vue3/src/api/iot/access/dashboard.ts`
   - 4 端点 RespVO 类型补齐（AccessTrendRespVO / AccessDeviceStatusOverviewRespVO / AccessHeatmapItem / AccessAbnormalEventItem）
4. `yudao-ui-admin-vue3/src/views/iot/access/visual-dashboard/index.vue`
   - 弃用 `getHourlyTrafficStatistics`（底层 record 表同样不存在）
   - 切到 `getAccessTrend(startTime, endTime)`，today/week/month/year 全部走真实数据
   - 加 `watch(dateRange)` 按需懒加载 + 缓存

### 2.3 数据画像（运行时心理预期）

- 表 56448 行，最新数据 **2026-04-20**（**今日 0 行**：4 个 metric 卡片今日数会显示 0 — 真实状态）
- event_type 分布：
  - `DOOR_NOT_CLOSED` 51092（被归为 ALARM）
  - `REMOTE_OPEN` 3631 / `CARD_SWIPE` 1392 / `FACE_RECOGNIZE` 195（NORMAL）
  - `FORCED_OPEN` 2 / `VERIFY_FAILED` 2（ALARM/ABNORMAL）
- `direction` 字段大部分 NULL → trend 的 inData/outData 多为 0；service 层已 fallback 到 accessData
- `verify_result` 大部分 NULL → real-time `result` 字段用 `success` 字段或 ABNORMAL 类型兜底

## 3. 用户侧验证指令（**待执行**）

```powershell
# 1) 后端打包（IOT 模块单独打 + 验证 fat-jar 嵌入）
cd e:\ch\ruoyi-vue-pro
mvn -pl yudao-module-iot/yudao-module-iot-biz -am clean install -DskipTests
mvn -pl yudao-server -am package -DskipTests

# 2) 校验 jar 内嵌（v24 教训：BUILD SUCCESS ≠ 内嵌正确）
$jar = "yudao-server\target\yudao-server.jar"
(Get-Item $jar).LastWriteTime
# 应大于 1 分钟前

# 3) 杀旧进程 + 启动新进程
$pid48888 = (Get-NetTCPConnection -LocalPort 48888 -State Listen -ErrorAction SilentlyContinue).OwningProcess
if ($pid48888) { Stop-Process -Id $pid48888 -Force }
java -jar $jar

# 4) Smoke 测（待端口 48888 起来后）
# 浏览器登录管理端 → /iot/access/visual-dashboard
# 期望：
#   - 4 metric 卡片：今日通行 0（数据截止 2026-04-20）；门禁设备总数应 = ibms_device WHERE systemCode IN ('AC','IC') 行数
#   - 24h 趋势：今日 0；切到"本周/本月"应有真实数据曲线（按事件密度）
#   - F12 Network：/iot/access/dashboard/{statistics,trend} 200 + 非空响应
#   - F12 Console：无 "[AccessVisualDashboard] xxx 接口失败" 警告
```

## 4. 下一步候选

| 候选 | 价值 | 风险 | 建议 |
|---|---|---|---|
| **M2-D 验证** | 高（确认 M2 是否真落地） | 低 | **先做**：用户侧 mvn + 重启 + smoke |
| **M2-B：access 单源化（GAP-011）** | 高 | 中 | 12 个 service 103 处 `IotDeviceDO` 引用，**必须新 feature 分支**；半天 |
| **M2-C：building-visual-dashboard 聚合（GAP-002）** | 高 | 中 | 后端新设计 `IbmsSpaceController.dashboard-stats` + 前端 1087 行重写 |
| **M1.7 Batch-1 + Batch-4 配套** | 中（前端 build 时间↓） | 低 | 8 test + 13 Z1 utils（被测+测试同步删） |
| **M1.7 Batch-3** | 低 | 极低 | 24 个 Z2 视图壳删除 |

## 5. CI 访问（不变）

- Drone Server：`http://test.sanligz.com.cn`（= `192.168.1.253`）
- CH 仓库：`fengxiatao/ch_ibms`
- API 基址：`http://test.sanligz.com.cn/api/repos/fengxiatao/ch_ibms`
- Token：与 jingyu 共用 drone token（前序 handoff 凭据段，禁止入库）

## 6. MCP 数据库连接（强制）

- **唯一允许**：`mcp4_mysql_query`（`mysql-ibms` → `ch_ibms` @ 127.0.0.1，**只读**）
- **写操作**：`run_command` + mysql 命令行 + 用户明确批准
- **禁用**：`mcp5/6/7_*`（线上库 / jingyu / parking）

## 7. 本机构建硬规则

- 见 `.cursor/rules/14-local-build.mdc`
- vue-tsc 必须设 `$env:NODE_OPTIONS='--max-old-space-size=8192'`（4GB 默认堆会 OOM）
- baseline TS 错误数：**1692**（删除/修改后必须验证 == baseline，避免引入新错误）

## 8. 新会话承接句式

```text
我承接 IBMS 治理。当前进度：M2-A + M1.7-Batch2 + M2-D 已完成（commit a0396d4）。
请先只读 docs/session-handoff-20260506-v26.md + AGENTS.md，
然后等我说先做 M2-D 验证 / 还是开 M2-B 新分支。
MySQL 仅用 mcp4_mysql_query；vue-tsc 跑前先 $env:NODE_OPTIONS='--max-old-space-size=8192'。
```
