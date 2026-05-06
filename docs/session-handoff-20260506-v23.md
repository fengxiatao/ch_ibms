# Session Handoff 2026-05-06 v23（v22 候选 #1 负路径实测 + 候选 #3 调试端点守卫 双落地）

> 承接规则：仅读本文件 + `AGENTS.md`，**不要翻历史 vN（含 v1~v22）**（除非用户明确要求）。

---

## 1. 项目骨架

- **后端**：`ruoyi-vue-pro/`
- **管理端**：`yudao-ui-admin-vue3/`
- **DB**：MySQL 8 @ `127.0.0.1:3306/ch_ibms`（`root/123456`）
- **Redis**：`127.0.0.1:6379/db=1`（Spring Cache 使用）

---

## 2. 本次会话变更

### 2.1 执行内容

连续落地两个 v22 候选 + 对 v22 §6 错误描述做澄清：

- **候选 #1（负路径实测）**：验证 sink URL 不通时的错误链路日志完整、不阻塞主流程（纯实测，无代码改动）
- **候选 #3（调试端点守卫）**：抽离 `IotDebugDeviceMessageController` + `@ConditionalOnProperty` 守卫
- **澄清 v22 §6 错误描述**：v22 `IotDataRuleDispatcher` 的 ERROR 日志在 sink 不通场景下**不会触发**（见 §2.4 发现 16）

### 2.2 commit 列表（snapshot/20260423-full）

| commit | 描述 | 文件数 | 改动行 |
|---|---|---|---|
| `9418222` | refactor(iot): 调试端点抽离至 IotDebugDeviceMessageController + ConditionalOnProperty 守卫 (v23 候选 #3) | 2 | +84 / -45 |

已 push 双远端：
- `ssh://root@192.168.1.253/opt/ci/cache/git/ch_ibms.git` → `52fb371..9418222`
- `git@github.com:fengxiatao/ch_ibms.git` → `52fb371..9418222`

### 2.3 落地改动详情

#### 候选 #1：负路径实测（纯测试，无代码改动）

**步骤**：
1. mcp4 UPDATE `iot_data_sink.config.url` 10000015 → `http://127.0.0.1:9998/dead-port`
2. 清 Redis `iot:data_sink:10000015` + `iot:data_sink:1:10000015`（Spring Cache 缓存）
3. 触发 `thing.property.post`（device 10000126）
4. 恢复 URL → 清缓存 → 再触发验证恢复

**日志证据**（`~/logs/yudao-server.log`）：

```
09:36:56.980 [DeviceEventConsumer] 收到事件: deviceType=UNKNOWN, deviceId=10000126, eventType=thing.property.post
09:36:56.995 ERROR [IotHttpDataSinkAction:88] [execute][... 请求异常(null)]
             org.springframework.web.client.ResourceAccessException: I/O error on POST request for
             "http://127.0.0.1:9998/dead-port": Connection refused: no further information
09:36:57.001 ERROR [IotDataRuleServiceImpl:270] [executeDataRuleAction][消息(...) 数据目的(10000015) 执行异常]
             (完整堆栈：IotHttpDataSinkAction → executeDataRuleAction → executeDataRule →
              IotDataRuleDispatcher.dispatch:55)
```

**恢复后**（URL 改回 9999）：webhook sink 09:38:05 收到 `v23_marker=recovery-test`，完整闭环 ✅

**DoD 达成**：

| 验证点 | 结果 |
|---|---|
| webhook sink 不收 POST（sink 不通时） | ✅ |
| `[execute][... 请求异常]` ERROR 完整 | ✅ at `IotHttpDataSinkAction:88` |
| `[executeDataRuleAction][... 执行异常]` ERROR 完整 | ✅ at `IotDataRuleServiceImpl:270` |
| 主流程不阻塞 | ✅ consumer 日志正常打印 |
| 恢复 URL 后下一条正常闭环 | ✅ webhook 09:38:05 收到 |
| dispatcher 自身 catch 未触发 | ✅（**正确行为**，见发现 16） |

#### 候选 #3：调试端点守卫

**新建 `IotDebugDeviceMessageController.java`**（`.../controller/admin/device/`）：
- `@ConditionalOnProperty(name = "yudao.iot.debug-endpoints.enabled", havingValue = "true")` — 默认 `matchIfMissing=false`，缺省时 controller bean **不注册**
- 保留 `/iot/device/message/test-publish-state-change` 端点不变（复用 `iot:device:message-end` 权限）
- `iotMessageBus` 字段从原 controller 迁移至本类

**修改 `IotDeviceMessageController.java`**：
- 移除字段：`iotMessageBus`
- 移除方法：`testPublishStateChange`
- 移除 imports（6 个）：`ConnectionMode`、`DeviceStateChangeMessage`、`IotMessageBus`、`IotMessageTopics`、`IotDeviceStateEnum`、`Qualifier`

### 2.4 关键技术发现（v23 新增 2 条）

#### 发现 15（v23 澄清 v22 §2.6 / §6 错误认知）：应用日志**早就在落盘**

v22 handoff §2.6 断言"fat-jar stdout 仅捕获启动早期日志，深度日志需配 logback file appender" — **错**。

事实：
- 全部 `application-*.yaml` profile 都已配 `logging.file.name: ${user.home}/logs/${spring.application.name}.log`
- `logback-spring.xml` 已注册 `RollingFileAppender` + `AsyncAppender` + root INFO level
- **日志路径**：`C:\Users\Administrator\logs\yudao-server.log`（Windows 本机）
- 运行时深度日志（`[DeviceEventConsumer] 收到事件` / `[executeDataRule]` / `[IotHttpDataSinkAction execute]` / `[executeDataRuleAction]` 等）全部落盘，**v22 §6 候选 #2 根本无需做**

v22 之所以觉得"stdout 只有启动早期"，是因为 logback 一旦初始化后 async appender 接管，stdout 就仅剩 JVM/启动期早期输出（36KB 左右）。真实 server 日志不在 `.tmp_sql\*.log` stdout 重定向里，而在 `${user.home}/logs/`。

#### 发现 16：`IotDataRuleDispatcher` 的 catch 在 sink URL 不通场景下**不会触发**

v22 §6 候选 #1 描述"验证 dispatcher 的 ERROR 日志格式 `[IotDataRuleDispatcher] 触发数据流转规则失败`" — **概念偏差**。

技术事实：`IotDataRuleServiceImpl.executeDataRule()` 内部已有 try-catch 包住整个 rule 遍历 + sink action 调用（`IotDataRuleServiceImpl:222`），ResourceAccessException 在 `[executeDataRuleAction]` 这一层（`:270`）已被完整 catch 并 log ERROR。service 整体**不会**向 `dispatcher.dispatch()` 层抛出异常，所以 dispatcher 的 try-catch 是**兜底**（仅在 service 漏抛 RuntimeException 时触发，比如 NPE、序列化错）。

**sink URL 不通 → 正常日志链路**（按 v23 实测）：
1. `ERROR [IotHttpDataSinkAction:88] [execute][... 请求异常(null)]`
2. `ERROR [IotDataRuleServiceImpl:270] [executeDataRuleAction][消息(...) 数据目的(...) 执行异常]`
3. **不会有** `[IotDataRuleDispatcher] 触发数据流转规则失败`

**结论**：dispatcher 这层 catch 是好的防御式设计（不影响消费链路），但其日志消息是"看不到才是对的" — 看到了反而说明 service 漏了某种异常未处理。

### 2.5 运行时实测（两阶段守卫验证）

**阶段 1：默认配置（无 `-D`）**

```
POST /admin-api/iot/device/message/test-publish-state-change
→ 200 {"code":404,"msg":"请求地址不存在:admin-api/iot/device/message/test-publish-state-change","data":null}
```

（HTTP 200 由全局异常处理器返回；业务 code=404 = 路由未注册，controller bean 未加载）

**阶段 2：`-Dyudao.iot.debug-endpoints.enabled=true` 启动**

```
POST /admin-api/iot/device/message/test-publish-state-change body={..., reason: v23_marker=guard-enabled}
→ 200 {"code":0,"data":true}
→ webhook sink 09:43:54 收到 method=thing.state.update state=1 v23_marker=guard-enabled
```

完整数据流转链路（DeviceStateChangeConsumer → buildStateUpdateMessage → IotDataRuleDispatcher.dispatch → IotDataRuleServiceImpl.executeDataRule → IotHttpDataSinkAction）通畅 ✅

### 2.6 未验证项 / 挂账给 v24

1. **DeviceServiceResultConsumer 路径完整回归实测**（10~20 分钟）— v22 代码改动等价，但实测证据仍缺
2. **本地开发 profile 启用 debug-endpoints 的固化**（2 分钟）— 是否在 `application-local.yaml` 加 `yudao.iot.debug-endpoints.enabled: true` 让本机开发免 `-D`
3. **v22 §6 候选 #2（fat-jar 日志落盘）作废并通知团队** — 已证实是误判
4. **Redis Spring Cache 未失效的潜在 bug**（副产物发现）— `IotDataSinkServiceImpl.updateDataSink` 无 `@CacheEvict`，通过后台修改 sink config 后，缓存不自动失效。v23 负路径实测需要手工 DEL redis key 才能生效

---

## 3. CI 访问

沿用 v19 §3，无变化。

---

## 4. MySQL 连接

| 工具 | 用途 | 读 | 写 |
|---|---|---|---|
| `mcp4_mysql_query`（mysql-ibms） | 本地 `ch_ibms` | ✓ | ✓（DML 直写；DDL 走 run_command） |

本次会话 mcp4 写操作：UPDATE `iot_data_sink.config` URL 两次（9999↔9998），均已恢复原状。

---

## 5. 本机构建硬规则

- v10~v20 全部条款沿用
- **v23 新增 1 条（23-1）：Spring Cache key 清理**
  - `iot_data_sink` / `iot_data_rule` 表通过 mcp4 UPDATE 后，**必须**清 Redis key 才能让业务层生效
  - Redis：`127.0.0.1:6379 db=1`，key 格式：`iot:data_sink:{id}`、`iot:data_sink:{tenantId}:{id}`、`iot:data_rule_list:{tenantId}:{deviceId}_{method}_{identifier}`
  - 清除方法（无 redis-cli 时）：PowerShell .NET TcpClient 发 RESP，或调用 controller update 接口（当前 updateDataSink **未加 @CacheEvict**，实际只能手工清）
  - 参考本次会话 §9 命令模板

---

## 6. 下一步候选 + 给下次会话的建议

### 当前分支状态

- **分支**：`snapshot/20260423-full` @ `9418222`（v23 doc 待 commit）
- **后台进程**（留给 v24 直接复用）：
  - RocketMQ NameServer PID 17048 @ 9876（沿用）
  - RocketMQ Broker PID 19104（沿用）
  - **yudao-server fat-jar PID 16516 @ 48888**（v23 代码 in 内存，**含 `-Dyudao.iot.debug-endpoints.enabled=true` 启动**）
  - webhook sink @ 9999（沿用）

### 候选（按价值/复杂度排序）

#### 1. DeviceServiceResultConsumer 路径完整回归实测（10~20 分钟）

v22/v23 抽切面 + 守卫改动后，DeviceServiceResultConsumer 的 dispatcher 路径仍未实证（需 service.invoke reply 链路）。

步骤草稿：
- 找一个网关侧能稳定 reply 的 device（ACCESS_*、CAMERA 等）
- 发 `POST /admin-api/iot/device/message/send` body method=`thing.service.invoke`，params=某 service identifier
- 等待 gateway reply → DEVICE_SERVICE_RESULT topic → `DeviceServiceResultConsumer.onMessage` → `dataRuleDispatcher.dispatch(msg, "DeviceServiceResultConsumer")`
- 可能需新增 rule 10000009 method=`thing.service.invoke_reply`（或实际 method，视 gateway 实现）

DoD：webhook sink 收到 service reply 的 POST；yudao-server.log 有 `[DeviceServiceResultConsumer] 收到响应` + `[executeDataRule]` + `[executeDataRuleAction] 执行成功`

#### 2. 本地开发 profile 固化 debug-endpoints（2 分钟）

在 `application-local.yaml` 加：
```yaml
yudao:
  iot:
    debug-endpoints:
      enabled: true
```
DoD：无 `-D` 启动（使用 local profile）调试端点可用；prod/dev profile 仍 404。

#### 3. Redis Spring Cache 自动失效（10~20 分钟）

给 `IotDataSinkServiceImpl.updateDataSink` / `deleteDataSink` 加 `@CacheEvict(value = RedisKeyConstants.DATA_SINK, key = "#updateReqVO.id")`，修复发现 15 的副产物 bug。

DoD：通过 `PUT /admin-api/iot/data-sink/update` 改 URL 后，无需手工 DEL redis key，业务立即生效。

#### 4. 与产品确认规则引擎 dead UI 走向 / CI v2 真实 mvn / 其他延续

沿用 v22 §6。

### 给下次会话的建议

- **首选 #1 + #2 组合**：一起做，#2 免后续每次启动都要 `-D`，#1 补齐 DeviceServiceResultConsumer 路径证据
- **#3 单独**：是 bug 修复而非新功能，需要评审（涉及缓存一致性）
- **规则 19-1 必守**：grep 证活
- **规则 20-1 必守**：改 Java 后 `mvn clean package`，不带 `-q`
- **规则 23-1 新增**：改 sink/rule 表后必须清 Redis key

---

## 7. 关键访问凭据

沿用 v19 §7。本次会话 admin/tenant=1 accessToken：`96e537e0ff1f449d9ccda2dcdc1120e2`（参考，已过期周期内）。

---

## 8. 未 push 的本地状态

- **本地 commit**（已 push 双远端 `9418222`）：refactor(iot): 调试端点守卫
- **本次 doc commit 待加入**：本文件 v23
- **本地 untracked（不入 commit）**：
  - `E:\ch\.tmp_sql\*.log` / `*.ps1` / `commit_msg_v23_3.txt`
  - 历史 `docs/session-handoff-20260504-v2~v10.md`（曾用名，未追踪）
- **DB 持久变更**：无（sink 10000015 URL 已恢复原始 `http://127.0.0.1:9999/webhook-test`）
- **运行中后台进程**：见 §6.当前分支状态

---

## 9. 实测命令速查（v24 候选 #1 / #3 用）

```powershell
# 0. 复用 token
$body = @{username='admin';password='admin123';captchaVerification=''} | ConvertTo-Json
$r = Invoke-RestMethod -Uri 'http://127.0.0.1:48888/admin-api/system/auth/login' -Method Post -Body $body -ContentType 'application/json' -Headers @{'tenant-id'='1'}
$token = $r.data.accessToken

# 1. 触发 thing.property.post (DeviceEventConsumer 路径)
$body = '{"deviceId":10000126,"method":"thing.property.post","params":{"v24_marker":"test"}}'
Invoke-RestMethod -Uri 'http://127.0.0.1:48888/admin-api/iot/device/message/send' -Method Post -Body $body -ContentType 'application/json' -Headers @{'tenant-id'='1'; 'Authorization'="Bearer $token"}

# 2. 触发 thing.state.update (DeviceStateChangeConsumer 路径；需 debug-endpoints ON)
$body = '{"deviceId":10000126,"newState":1,"previousState":0,"reason":"v24_marker=test","deviceType":"CAMERA","tenantId":1}'
Invoke-RestMethod -Uri 'http://127.0.0.1:48888/admin-api/iot/device/message/test-publish-state-change' -Method Post -Body $body -ContentType 'application/json' -Headers @{'tenant-id'='1'; 'Authorization'="Bearer $token"}

# 3. 触发 thing.service.invoke (DeviceServiceResultConsumer 路径；需 gateway reply 支持)
# TODO（候选 #1 实测时确定 method 和 service identifier）

# 4. 看 server 端完整日志（v23 纠正：~/logs/ 才是真实日志）
Get-Content "$env:USERPROFILE\logs\yudao-server.log" -Tail 50
Select-String -Path "$env:USERPROFILE\logs\yudao-server.log" -Pattern 'IotDataRuleDispatcher|executeDataRule|HttpDataSink|DeviceServiceResultConsumer'

# 5. 看 webhook sink
Get-Content E:\ch\.tmp_sql\webhook-sink.log -Tail 20

# 6. 清 Redis Spring Cache key（规则 23-1）
$tcp = New-Object System.Net.Sockets.TcpClient('127.0.0.1', 6379); $s = $tcp.GetStream()
function Send($cmds) { $sb = "*$($cmds.Count)`r`n"; foreach ($c in $cmds) { $sb += "`$$($c.Length)`r`n$c`r`n" }; $b = [System.Text.Encoding]::ASCII.GetBytes($sb); $s.Write($b, 0, $b.Length); Start-Sleep -Milliseconds 200; $buf = New-Object byte[] 4096; $n = $s.Read($buf, 0, $buf.Length); return [System.Text.Encoding]::ASCII.GetString($buf, 0, $n) }
Send @('SELECT','1') | Out-Null
Send @('DEL','iot:data_sink:10000015','iot:data_sink:1:10000015')
$tcp.Close()

# 7. 改 Java 后强制 clean package（规则 20-1）
cd e:\ch\ruoyi-vue-pro
mvn -pl yudao-module-iot/yudao-module-iot-biz,yudao-server -am clean package -DskipTests -T 1C

# 8. kill + wmic 重启（规则 16-2 rev v19）— 注意保留 debug-endpoints ON
$pid_to_kill = (Get-NetTCPConnection -LocalPort 48888 -State Listen).OwningProcess
Stop-Process -Id $pid_to_kill -Force
Start-Sleep -Seconds 3
$log='E:\ch\.tmp_sql\yudao-server-v24.log'
"=== v24 启动 $(Get-Date) ===" | Out-File -FilePath $log -Encoding utf8
Invoke-CimMethod -ClassName Win32_Process -MethodName Create -Arguments @{
  CommandLine = "cmd /c `"C:\Program Files\Java\jdk-17\bin\java.exe`" -Dyudao.iot.debug-endpoints.enabled=true -jar E:\ch\ruoyi-vue-pro\yudao-server\target\yudao-server.jar --server.port=48888 >> $log 2>&1"
  CurrentDirectory = 'E:\ch\ruoyi-vue-pro'
}
Start-Sleep -Seconds 50
netstat -ano | Select-String ':48888.*LISTENING'

# 9. mcp4 DML 示例（规则 16-1 + 23-1）
# 用 mcp4_mysql_query 执行：UPDATE iot_data_sink SET config = JSON_REPLACE(config, '$.url', 'xxx') WHERE id = 10000015;
# 然后必须走上面步骤 6 清 Redis key
```

关键文件：
- `IotDataRuleDispatcher.java`（v22 新增）
- `IotDebugDeviceMessageController.java`（v23 新增，带 `@ConditionalOnProperty` 守卫）
- `DeviceEventConsumer.java` / `DeviceServiceResultConsumer.java` / `DeviceStateChangeConsumer.java`（v22 统一 dispatch）
- `IotDataRuleServiceImpl.java`：第 205 行 `executeDataRule(IotDeviceMessage)` 入口（内部 catch 已完整）
- `IotDataSinkServiceImpl.java`：第 80 行 `getDataSinkFromCache`（`@Cacheable`），第 48 行 `updateDataSink`（**缺 `@CacheEvict`** — v24 候选 #3）
- `logback-spring.xml` + `application-*.yaml` 中 `logging.file.name`：日志落盘到 `${user.home}/logs/yudao-server.log`

---

## 10. 给新会话 AI 的承接提示词

```text
我是 CH（长辉 IBMS）项目主程，工作区 e:\ch，按 docs/session-handoff-20260506-v23.md 承接。
请先只读该 handoff + AGENTS.md，勿翻历史 vN（v1~v22），然后等我选下一步。
CI：见 handoff §3；MySQL 用 mcp4_mysql_query，DML 可直写（v19 实证）；
本机构建硬规则见 §5（含 16-2 rev v19 wmic + 19-1 grep 证活 + 20-1 强制 clean package + 23-1 Spring Cache key 清理）。

当前分支：snapshot/20260423-full @ 9418222（v23 doc 待 commit，已 push 双远端）

v23 核心结论（已 commit + push）：
  - 9418222 refactor(iot): 调试端点抽离至 IotDebugDeviceMessageController + ConditionalOnProperty 守卫
  - 负路径实测全通：sink URL 不通时 [IotHttpDataSinkAction 请求异常] + [executeDataRuleAction 执行异常] ERROR 完整，
    主流程不阻塞，恢复后下一条闭环；dispatcher 自身 catch 未触发（正确行为）
  - 调试端点守卫两阶段验证：默认 404；-Dyudao.iot.debug-endpoints.enabled=true 可用
  - 澄清 v22 §2.6 误判：应用日志早就在 ~/logs/yudao-server.log 落盘（logback + yaml 已配置）
  - 副产物 bug：IotDataSinkServiceImpl.updateDataSink 缺 @CacheEvict

后台进程（留给 v24 直接复用）：
  - RocketMQ NameServer PID 17048 / Broker PID 19104
  - yudao-server fat-jar PID 16516 @ 48888（v23 代码 + debug-endpoints ON）
  - webhook sink @ 9999

DB 测试记录（沿用 v22，未变化）：
  - sink 10000015 (URL 已恢复 9999) / rule 10000006 (property.post) / rule 10000007 (state.update) / device 10000126

下次候选（详见 §6）：
1. DeviceServiceResultConsumer 路径完整回归（service.invoke reply）
2. 本地开发 profile 固化 debug-endpoints（2 分钟）
3. IotDataSinkServiceImpl @CacheEvict 修复（bug fix）
4. 产品沟通 / CI v2 / 其他延续

以主程综合考量决定候选，直接执行。
```

---

_最后更新：2026-05-06 09:45 +08:00（v22 候选 #1 负路径 + v22 候选 #3 调试端点守卫 双落地 + v22 §6 错误描述澄清）_
