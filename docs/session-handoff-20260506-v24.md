# CH IBMS  v24 会话 Handoff（2026-05-06，闭环版）

> 主修复：reply 路径 `tenantId` 透传，根除 `IotHttpDataSinkAction` NPE。
> 完整 DoD（webhook-sink @ 9999 收到 `method=QUERY_CHANNELS` POST + 全程无 NPE）已通过。

---

## 1. 项目骨架（与 v23 一致）

- **后端**：`ruoyi-vue-pro/`  Spring Boot 3 + MyBatis Plus
  - `yudao-server`（fat-jar 入口，端口 48888）
  - `yudao-module-iot/yudao-module-iot-biz`（业务逻辑：DataRule、DataSink、IbmsChannel、DeviceCommandPublisher）
  - `yudao-module-iot/yudao-module-iot-newgateway/yudao-module-iot-newgateway-core`（设备命令路由 / NVR 插件桥接）
- **管理端**：`yudao-ui-admin-vue3/`（本会话未改动）

---

## 2. 本次会话变更

### 2.1 commit 列表

| commit | 说明 |
|---|---|
| `6e3f346` | **fix(iot): reply 路径 tenantId 透传，修复 IotHttpDataSinkAction NPE**（双远端已 push） |

### 2.2 代码变更（4 个 Java，+58/-8）

| 文件 | 变更 |
|---|---|
| `newgateway/consumer/DeviceCommandConsumer.java` | 从 envelope/payload 解析 `tenantId` 并传给 executor |
| `newgateway/core/executor/DeviceCommandExecutorService.java` | reply 消息（`publishCommandResult` 构造的 `IotDeviceMessage`）写入 `tenantId` |
| `biz/mq/producer/DeviceCommandPublisher.java` | 新增 6 参重载 `publishCommand(deviceType, deviceId, commandType, params, fixedRequestId, explicitTenantId)`；优先用显式 tenantId，回退 `TenantContextHolder` |
| `biz/service/ibms/channel/IbmsChannelServiceImpl.java` | `syncViaCommandBus` 显式传 `ibmsDevice.getTenantId()` 给 publisher |

### 2.3 关键技术发现 

- **mvn 静默失败陷阱**：`mvn -pl yudao-server install` 在 `yudao-server.jar` 被运行中 JVM 锁住时，spring-boot:repackage 无法替换 fat-jar 但**仍报 BUILD SUCCESS**。本次调试 v24 主修复时 3 轮"修了又测、还失败"全是因为新代码根本没进 fat-jar（内嵌 biz jar 始终是早期旧版）。
- **正确流程**（建议补充至 `.cursor/rules/14-local-build.mdc`）：每次重打 yudao-server 前**先 kill JVM**，然后用 `mvn -pl yudao-server clean install -DskipTests`（必须带 `clean`）。
- **IotMessageEnvelope.tenantId 与 payload.IotDeviceMessage.tenantId 是两条独立链路**：reply 路径下游 `IotHttpDataSinkAction:55` 用 `message.getTenantId()`（payload 内层），newgateway 端 reply 构造时必须显式 `setTenantId`。

### 2.4 实测验收（DoD 已闭环 ）

```text
2026-05-06 15:20:02.944 [IotRocketMQMessageBus] post envelope.tenantId=1 payload.tenantId=1
2026-05-06 15:20:03.978 [executeDataRule] 设备(12) 方法(QUERY_CHANNELS) 匹配到 1 条规则
2026-05-06 15:20:04.039 [IotHttpDataSinkAction:80] INFO 请求成功（HTTP 2xx）
```

`E:\ch\.tmp_sql\webhook-sink.log` 最新条目 `--- 2026-05-06 15:20:04.029 ---`：
```json
{"source":"v18-test","message":{"deviceId":12,"tenantId":1,"requestId":"5c34ad37-...",
 "method":"QUERY_CHANNELS","data":{"channels":[...16 channels...],"totalCount":16,"code":0,"msg":"成功"}}}
```

| DoD | 状态 |
|---|---|
| `executeDataRule` 对 `method=QUERY_CHANNELS` 命中 |  |
| `IotHttpDataSinkAction:80` INFO 请求成功（非 88 ERROR） |  |
| webhook-sink @ 9999 收到 POST 含 `tenantId=1` + `method=QUERY_CHANNELS` + 16 channels |  |
| 全程无 `NullPointerException.*getTenantId` |  |

---

## 3. CI 访问

- **Drone Server**：`http://test.sanligz.com.cn`（= `192.168.1.253` 内网）
- **CH 仓库**：`fengxiatao/ch_ibms`
- **API 基址**：`http://test.sanligz.com.cn/api/repos/fengxiatao/ch_ibms`
- **Token**：与 jingyu 共用 drone 用户 token，存放于 v23 handoff "关键访问凭据"段；本会话未涉及 CI API 调用
- **push 双远端已完成**：`origin` (CI 内网 + github.com) + `chvm1` (CI 内网) 均到 `6e3f346` 

---

## 4. MySQL 连接

- **本地开发库**：`mcp4_mysql_query`（`mysql-ibms`） `ch_ibms` @ `127.0.0.1:3306`，**只读**
- **写操作**：通过 `run_command` 执行 `mysql -uroot -p123456 -h127.0.0.1 -P3306 ch_ibms ...`（密码来自 `application-local.yaml` / `application-ch.yaml`）
- **本次未做 DB 持久变更**（沿用 v18 的 `iot_data_sink.id=10000015` + `iot_data_rule.id=10000006/10000007` + `ibms_device.id=12 nvr0001`）

 命令行 mysql 在 PowerShell 下的输出捕获有问题；使用 `cmd /c "mysql ... -e ""SELECT...;"""` 可正常拿到结果。

---

## 5. 本机构建硬规则（v24 修订）

详见 `.cursor/rules/14-local-build.mdc`，**v24 新增血泪经验**：

> 每次重打 yudao-server 前必须 kill 端口 48888 上的 JVM 进程，然后用 `mvn -pl yudao-server clean install -DskipTests`（必须带 `clean`）。否则 spring-boot:repackage 写不进锁定的 fat-jar，BUILD SUCCESS 是假的，新代码不会生效。

**验证 fat-jar 是否真的更新**（解压 fat-jar 内嵌 biz jar 看时间戳）：

```powershell
Add-Type -Assembly 'System.IO.Compression.FileSystem'
$z = [System.IO.Compression.ZipFile]::OpenRead('E:\ch\ruoyi-vue-pro\yudao-server\target\yudao-server.jar')
$bizEntry = $z.Entries | Where-Object { $_.FullName -like 'BOOT-INF/lib/yudao-module-iot-biz*.jar' }
"Embedded biz lastWrite=$($bizEntry.LastWriteTime)"  # 应是当前时间，不是 11/17/2025
$z.Dispose()
```

---

## 6. 下一步候选 + 给 v25 的建议

### 6.1 推荐候选（按价值/成本排序）

| # | 候选 | 价值 | 工作量 | DoD |
|---|---|---|---|---|
| 1 | 把 `mvn 静默失败` 写入 `.cursor/rules/14-local-build.mdc` | 高 | 5min | rule 文件追加段落 + 提交，下次新会话承接时被 LSP 索引 |
| 2 | 单元测试 `DeviceCommandPublisher` 6 参重载 + reply 路径 tenantId 回归 | 中 | 30min | `mvn test` 含 2~3 条新 test，断言 explicitTenantId 优先于 ctxTenantId |
| 3 | 把 v24 修复路径泛化到其他 publishCommand 调用方 | 中 | 1h | grep 5 参旧版调用点，逐一改为 6 参显式 tenantId |
| 4 | 多租户切换场景（tenant=2/3）下 sync-from-device 端到端 | 中 | 45min | mcp4 切租户上下文触发，webhook-sink 的 message.tenantId 与请求 tenant-id 一致 |
| 5 | 清理 `.tmp_sql/` 目录积累过百的 log/json 产物 | 低 | 5min | 按 LastWriteTime 删旧文件 |

### 6.2 给 v25 的关键提醒

- 不要再翻 v23 之前的历史 handoff（除非用户明确要求）。从本文件 + AGENTS.md 起步即可。
- 不要先 commit 再启动 yudao-server 验证本会话教训：调试日志不进 fat-jar 是常态，验证完再 commit。
- 若仍要排查 reply 路径 NPE 类问题，先用 javap 验证 fat-jar 内嵌 biz jar 的字节码包含期望逻辑，再看运行时日志，避免被假 BUILD SUCCESS 误导。
- **TenantContextHolder 在 RocketMQ Consumer 线程内不可靠**任何 reply/异步路径需显式透传 tenantId，不能依赖 ThreadLocal。

---

## 7. 当前分支状态

- **HEAD**：`snapshot/20260423-full @ 6e3f346`（本会话 v24 commit），双远端已 push
- **本地 untracked（不入 commit）**：
  - `.tmp_sql/yudao-server-v24-r*.log` / `webhook-sink.log` / `commit_msg_v24.txt` / 多份 `_v24_*.json/.txt`
  - 历史 `docs/session-handoff-20260504-v2~v10.md`（曾用名，未追踪）
  - `docs/session-handoff-20260506-v24-wip.md`（中间版，本文为最终版）
- **运行中后台进程**：
  - yudao-server PID `39496` @ port 48888（v24 修复版）
  - webhook-sink @ port 9999 (PowerShell HttpListener)
- **DB 持久变更**：本会话**无**新增（沿用 v18 的 sink 10000015 / rule 10000006/10000007 / device 12 nvr0001）

---

## 8. 快速复现 / 回归命令

```powershell
# 1. 登录拿 token
$body = @{username='admin';password='admin123';captchaVerification=''} | ConvertTo-Json
$r = Invoke-RestMethod -Uri 'http://127.0.0.1:48888/admin-api/system/auth/login' -Method Post `
     -Body $body -ContentType 'application/json' -Headers @{'tenant-id'='1'}
$tok = $r.data.accessToken

# 2. 触发 sync-from-device（v24 修复主路径）
Invoke-RestMethod -Method Post `
  -Uri "http://127.0.0.1:48888/admin-api/iot/ibms/channel/sync-from-device?deviceId=12" `
  -Headers @{'tenant-id'='1';'Authorization'="Bearer $tok"} -TimeoutSec 60

# 3. 看 webhook 收到（应有新条目，body 含 tenantId=1, method=QUERY_CHANNELS）
Get-Content E:\ch\.tmp_sql\webhook-sink.log | Select-String '^---' | Select-Object -Last 2
Get-Content E:\ch\.tmp_sql\webhook-sink.log -Tail 1

# 4. 看 yudao-server 全程无 NPE
(Get-Content E:\ch\.tmp_sql\yudao-server-v24-final.log | Where-Object { $_ -match 'NullPointerException' }).Count  # 应=0

# 5. 重启 yudao-server（v24 教训：先 kill，再 clean install）
$pid_kill = (Get-NetTCPConnection -LocalPort 48888 -State Listen -ErrorAction SilentlyContinue).OwningProcess
if ($pid_kill) { Stop-Process -Id $pid_kill -Force }
Start-Sleep -Seconds 5
Set-Location e:\ch\ruoyi-vue-pro
mvn -pl yudao-server clean install -DskipTests   # 必须带 clean
# 然后用 wmic/Invoke-CimMethod 模板启动新 JVM

# 6. webhook sink 如挂掉重启
Invoke-CimMethod -ClassName Win32_Process -MethodName Create -Arguments @{
  CommandLine='powershell.exe -NoProfile -ExecutionPolicy Bypass -File E:\ch\.tmp_sql\webhook-sink.ps1'
  CurrentDirectory='E:\ch'
}
```

---

## 9. 参考文件索引

- `.cursorrules`  前端布局规范
- `.windsurfrules`  Windsurf MCP 规则摘要
- `.cursor/rules/14-local-build.mdc`  本机构建硬规则（v25 候选 #1 待补 mvn 静默失败陷阱）
- `AGENTS.md`  AI Agent 项目承接规范（六大锚点）
- 本文件  v24 完整闭环 handoff

---

## 10. v25+ 长期治理计划入口（IBMS 统一数据源）

**目标**：让 4 个业务模块（智慧安防/通行/能源/建筑）的所有设备/空间/通道/状态展示**100% 来源于"智慧物联"模块运维配置**，最终物联模块可隐藏、业务模块独立交付客户。

**新会话承接顺序**（必读）：

1. `AGENTS.md`
2. `docs/ibms-unified-data-source-plan.md` 主计划（M0~M7 阶段路线图）
3. `docs/ibms-coverage-matrix.md` 覆盖矩阵（每页面对接现状）
4. `docs/ibms-unified-progress.md` 进度跟踪（append-only）
5. 本 handoff（v24）

**当前阶段**：M0 已完成（计划基线落地），下一步进入 M1（完整盘点 50+ vue 页面）。

**5 大核心断点**（详见主计划 §2.3）：

1. 智慧通行 access 模块前后端双轨（IotDeviceDO + IbmsDeviceDO）
2. 3 个可视化大屏完全 mock（`access/visual-dashboard`、`building-visual-dashboard`、`energy/DeviceManagement`）
3. 智慧能源前端两套并存（`views/energy/` 与 `views/iot/building/energy/`）
4. 业务大类 `groupCode/systemCode` 筛选机制使用率低
5. SecurityOverview 等聚合层底层未必查 ibms_*

**待决策点**：

- D-001：智慧能源前端保留方案（`views/energy/` vs `views/iot/building/energy/`，M5 前置）
- D-002：物联模块隐藏方式（菜单权限 vs 环境变量，M7 前置）
- `docs/session-handoff-20260506-v23.md`  上一份 handoff
