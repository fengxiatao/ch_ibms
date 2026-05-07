---
description: CH（长辉 IBMS）项目 - 2026-05-07 v27 会话交接
---

# CH 项目 v27 会话交接 - M2-D 验证闭环 + ClassCast 修复

> 必读：`AGENTS.md` + 本文件 + `docs/ibms-unified-progress.md`
> **不要**翻历史 vN（含 v26），除非用户明确要求。
> 当前分支：`snapshot/20260423-full`（与 v26 同分支，非 `main`）。

---

## 1. 项目骨架（不变）

- **后端**：`ruoyi-vue-pro/yudao-module-iot` Spring Boot 3 + MyBatis Plus
- **管理端**：`yudao-ui-admin-vue3/` Vue3 + Vite + Element Plus + pnpm
- **DB**：`ch_ibms` @ 127.0.0.1（仅 `mcp4_mysql_query` 只读访问）

## 2. 本次会话变更（v26 → v27）

| commit | 内容 |
|---|---|
| `60274b5` | **fix(M2-D): trend 500 异常 - MyBatis SUM(CASE) 返回 String 的 ClassCast 容错** |

### 2.1 M2-D 验证结果（v26 待执行项）

- ✅ `mvn clean install -DskipTests`（全量，2:28 min）通过
- ✅ `yudao-server.jar` 嵌入 `AccessDashboardServiceImpl.class` + `IotAccessEventLogMapper.class`
- ✅ Server 启动监听 48888
- ✅ 短时间范围（单日 / 本周）trend 200 OK，数据为 0（数据截止 2026-04-20，预期）
- ❌ **跨 30 天以上范围（如 2026-04-08 ~ 2026-05-07）触发 500** → 已由 `60274b5` 修复
- ✅ 修复后两个之前 500 的 URL 返回 200 + 真实数据

### 2.2 v26 验证流程暴露的 3 个坑（需写入 v27 验证流程）

1. **端口占用 → spring-boot:repackage 静默失败**
   - Windows 文件锁下 spring-boot-maven-plugin 的 "Replacing main artifact" 日志依旧 INFO 级输出，但实际未覆盖 jar
   - 必须**先杀 48888 旧进程再 mvn**
2. **m2 repo 污染 → framework starter jar 缺类**
   - 之前某次部分 install 把 `starter-job` (14999 bytes 缺 `BusinessJobHandler`) / `starter-mybatis` (缺 `BooleanToIntTypeHandler`) 的半成品装入 m2 repo
   - `-pl ... -am install` 不能修复：**只有全量 `mvn clean install -DskipTests`** 能一次性刷新所有 starter
3. **fat-jar 内嵌校验不能用 `jar tf serverJar | grep X`**
   - `jar tf` 对 fat-jar 只列顶层（BOOT-INF/lib/\*.jar 是文件不是展开目录）
   - 真正校验：抽出 `BOOT-INF/lib/yudao-module-iot-biz-*.jar` 再 `jar tf` 嵌套 jar

### 2.3 ClassCast 根因（`60274b5`）

- **SQL 列类型歧义**：MySQL `SUM(CASE WHEN ... THEN 1 ELSE 0 END)` 结果列为 DECIMAL；`COUNT(*)` 结果列为 BIGINT
- **MyBatis 映射**：`Map<String,Object>` + MySQL JDBC 在当前驱动配置下，`SUM(...)` 列被映射为 **String**（非 BigDecimal/Long）；`COUNT(*)` 正常映射为 Long
- **触发条件**：rows 非空（即时间范围包含 2026-04-20 之前有真实数据的日期）→ `((Number) r.get("alarmCount"))` 抛 CCE
- **修复方案**：新增 `toLong(Object) / toInt(Object)` helper 兼容 `Number | String | null`，替换 `AccessDashboardServiceImpl` 5 处强转点（hourly 4 + daily 4 + `sumByEventTypeIn` 1 + hour key 1）
- **无 SQL 改动**，纯 Java-side 防御

### 2.4 改动文件

1. `ruoyi-vue-pro/yudao-module-iot/yudao-module-iot-biz/src/main/java/cn/iocoder/yudao/module/iot/service/access/AccessDashboardServiceImpl.java`
   - +24 行（2 个 helper）/ -0 行；替换 9 处 cast（其中 4 处分布在 hourly/daily 块内，每块 4 处 → 实际值替换点 5 类）
   - 净 +34 / -10

## 3. 当前运行时状态

- Server PID：**6080** 监听 48888（本次会话末启动）
- Server 日志：`e:\ch\.tmp_sql\yudao-server-20260507-102757.log`
- 前端：未本地跑，通过管理端代理 `localhost:3000` 透过后端验证
- 6 个 AccessDashboard 端点全部真实可用：
  - `/iot/access/dashboard/statistics` / `real-time` / **`trend`（含修复）** / `device-status-overview` / `heatmap` / `abnormal-events`

## 4. 正确的 M2-D 验证流程（v27 标准，替代 v26 §3）

```powershell
# 0) 杀 48888 旧进程（关键：先释放文件锁）
$old = (Get-NetTCPConnection -LocalPort 48888 -State Listen -EA SilentlyContinue).OwningProcess
if ($old) { Stop-Process -Id $old -Force; Start-Sleep 3 }

# 1) 若 m2 repo 未污染，单模块即可；否则全量：
cd e:\ch\ruoyi-vue-pro
mvn -pl yudao-module-iot/yudao-module-iot-biz install -DskipTests
mvn -pl yudao-server clean package -DskipTests
# 全量（慎用，3 min+，会连带打 newgateway 可能失败但不影响主 server）：
# mvn clean install -DskipTests

# 2) 校验 jar 时间戳 + 嵌套校验
$f = Get-Item "yudao-server\target\yudao-server.jar"; $f.LastWriteTime
$jarExe = "C:\Program Files\Java\jdk-17\bin\jar.exe"
$tmp = "$env:TEMP\fv-$(Get-Date -f HHmmss)"; New-Item -I Directory $tmp | Out-Null
Push-Location $tmp
& $jarExe xf $f.FullName "BOOT-INF/lib/yudao-module-iot-biz-2025.09-SNAPSHOT.jar"
& $jarExe tf "BOOT-INF\lib\yudao-module-iot-biz-2025.09-SNAPSHOT.jar" | ? { $_ -like "*AccessDashboard*" -or $_ -like "*IotAccessEventLog*" }
Pop-Location; Remove-Item -R -F $tmp

# 3) 启动
Start-Process java -ArgumentList "-jar","yudao-server\target\yudao-server.jar" -WorkingDirectory (pwd) -WindowStyle Hidden

# 4) Smoke（含跨月长范围）
# http://localhost:3000/admin-api/iot/access/dashboard/trend?startTime=2026-04-08&endTime=2026-05-07  -> 200 + 非空
# http://localhost:3000/admin-api/iot/access/dashboard/trend?startTime=2025-06-01&endTime=2026-05-07  -> 200 + 非空
```

## 5. 下一步候选

| 候选 | 价值 | 风险 | 建议 |
|---|---|---|---|
| **推送 60274b5 到双远端** | 高（规定动作） | 无 | `git push origin snapshot/20260423-full && git push chvm1 snapshot/20260423-full` |
| **M2-B：access 单源化（GAP-011）** | 高 | 中 | 12 个 service 103 处 `IotDeviceDO` 引用 → 统一 `IbmsDeviceDO`；**必须新 feature 分支**；半天 |
| **M2-C：building-visual-dashboard 聚合（GAP-002）** | 高 | 中 | 后端新 `IbmsSpaceController.dashboard-stats` + 前端 1087 行重写 |
| **M1.7 Batch-1 + Batch-4** | 中 | 低 | 8 test + 13 Z1 utils（被测+测试同步删） |
| **M1.7 Batch-3** | 低 | 极低 | 24 个 Z2 视图壳 |
| **治理：清理僵尸 java 进程** | 低 | 低 | 昨日 5 月 6 日起的 8 个旧 java.exe（PID 11240/13464/18720/23204/27744/28788/32888/35096）仍占内存，IDE/newgateway 相关，需用户确认哪些可 kill |

## 6. CI 访问（不变）

- Drone Server：`http://test.sanligz.com.cn`（= `192.168.1.253`）
- CH 仓库：`fengxiatao/ch_ibms`
- API 基址：`http://test.sanligz.com.cn/api/repos/fengxiatao/ch_ibms`
- Token：与 jingyu 共用 drone token（见前序 handoff 凭据段，禁止入库）

## 7. MCP 数据库连接（强制）

- **唯一允许**：`mcp4_mysql_query`（`mysql-ibms` → `ch_ibms` @ 127.0.0.1，**只读**）
- **写操作**：`run_command` + mysql 命令行 + 用户明确批准
- **禁用**：`mcp5/6/7_*`（线上库 / jingyu / parking）

## 8. 本机构建硬规则（补充）

- 见 `.cursor/rules/14-local-build.mdc`
- vue-tsc 必须设 `$env:NODE_OPTIONS='--max-old-space-size=8192'`
- baseline TS 错误数：**1692**
- **新增（v27）**：Maven build 前**必杀 48888 旧进程**；fat-jar 校验必用嵌套 jar 抽取法（见 §4 步骤 2）

## 9. 新会话承接句式

```text
我承接 IBMS 治理。当前进度：M2-D 验证+ClassCast 修复（commit 60274b5，分支 snapshot/20260423-full）。
请先只读 docs/session-handoff-20260507-v27.md + AGENTS.md，
然后等我说：(a) push 双远端 / (b) M2-B 开新分支 / (c) M2-C / (d) M1.7 剩余 Batch。
MySQL 仅用 mcp4_mysql_query；mvn 前必杀 48888；vue-tsc 前 $env:NODE_OPTIONS='--max-old-space-size=8192'。
```
