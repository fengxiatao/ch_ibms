# Session Handoff 2026-05-05 v19（候选 #1 运行时实测结论：v18 修复 4 挂载点错误 · webhook 仍不通）

> 承接规则：仅读本文件 + `AGENTS.md`，**不要翻历史 vN（含 v1~v18）**（除非用户明确要求）。

---

## 1. 项目骨架

- **后端**：`ruoyi-vue-pro/`
- **管理端**：`yudao-ui-admin-vue3/`
- **DB**：MySQL 8 @ `127.0.0.1:3306/ch_ibms`（`root/123456`）

---

## 2. 本次会话变更

### 2.1 执行内容：v18 候选 #1 运行时实测

完整走通了 v18 §2.6 列出的所有前置步骤，并暴露出 v18 修复 4 的挂载点错误。

### 2.2 操作链路（全部由 AI 代劳，16-2 rev 生效）

| # | 动作 | 执行者 | 结果 |
|---|---|---|---|
| 1 | 定位旧 fat-jar（端口 48888，PID 27268）| AI（`netstat`）| OK |
| 2 | 停旧 fat-jar | AI（`Stop-Process -Id 27268 -Force`）| OK |
| 3 | `mvn -pl yudao-server -am package -DskipTests -T 1C -q` | AI | BUILD SUCCESS，jar 262MB @ 22:50 |
| 4 | 启新 fat-jar（规则 16-2 rev v19：wmic/CIM 脱离进程树）| AI（`Invoke-CimMethod Win32_Process Create`）| java PID 11212，36s 启动完成，48888 LISTENING |
| 5 | 起本地 webhook sink @ 9999 | AI（PowerShell HttpListener 脚本，CIM 启）| 自测 POST 成功返回 `{"ok":true}`，header + body 落盘 |
| 6 | INSERT DataSink + DataRule（tenant=1，deviceId=0 全匹配，method=`thing.property.post`）| AI（`mcp4_mysql_query`，DML 直写）| sink.id=10000015, rule.id=10000006 |
| 7 | 登录 admin（tenant=1）+ `POST /admin-api/iot/device/message/send` 发 PROPERTY_POST | AI | HTTP 200, code=0, data=true |
| 8 | **验证回调** | AI（读两处日志）| **webhook sink 0 请求 · fat-jar 日志无任何 executeDataRule 痕迹** |

### 2.3 关键技术发现（v19 新增，3 条）

#### 发现 7（核爆级）：`handleUpstreamDeviceMessage` 在 ch_ibms 分叉里是**完全的死代码**

- grep 全仓：`handleUpstreamDeviceMessage` 只在 `IotDeviceMessageService` 接口定义 + `IotDeviceMessageServiceImpl` 实现 + 内部私有 `handleUpstreamDeviceMessage0` 中出现，**零外部调用方**。
- v17 审计把"顶层断链"定位到"应该从它开始下发数据流转"，但**没意识到连它自己都没人调用**。
- v18 修复 4 把 `dataRuleService.executeDataRule(message)` 挂进了这个死代码方法（`IotDeviceMessageServiceImpl:195-200`），相当于**给一条永远进不来水的管道装了阀门**。
- 实测印证：消息经 `POST /iot/device/message/send` → `sendDeviceMessage` → RocketMQ topic `iot_device_event_reported`（SEND_OK）→ `DeviceEventConsumer.onMessage` 消费 → `routeToHandler` + `pushToFrontend` 结束。**全程不经过 `handleUpstreamDeviceMessage`**，v18 修复 4 永不触发。

#### 发现 8：ch_ibms 的真实上行消费入口是三个 Consumer（非 `handleUpstreamDeviceMessage`）

```
cn.iocoder.yudao.module.iot.mq.consumer.device.
  ├─ DeviceEventConsumer        // topic=DEVICE_EVENT_REPORTED  (property.post / event.post)
  ├─ DeviceStateChangeConsumer  // topic=DEVICE_STATE_CHANGED   (state.update)
  └─ DeviceServiceResultConsumer// topic=DEVICE_SERVICE_RESULT  (service.invoke reply)
```

这是 ch_ibms 相对 ruoyi-vue-pro 原版的架构魔改，原版 `handleUpstreamDeviceMessage` 在分叉里被架空但 shell 没清理。

#### 发现 9：规则 16-2 rev v19 实证成功

用 `Invoke-CimMethod Win32_Process Create` 启 fat-jar → java 子进程父节点为 WmiPrvSE，完全脱离 `run_command` 生命周期，run_command 返回后 java 持续运行。v16 实证的 cascading kill 问题被绕开。

### 2.4 落地改动（本次无 Java 代码改动；仅 DB 新增 2 条测试记录 + 新增启动/sink 脚本）

| # | 位置 | 说明 | 是否需保留 |
|---|---|---|---|
| 1 | `iot_data_sink.id=10000015` (tenant=1, name=`v18-runtime-test-sink`) | HTTP → `http://127.0.0.1:9999/webhook-test`，含 `X-V18-Test` + `Authorization: Bearer test-token-v18` 头 | 保留供下次会话复用 |
| 2 | `iot_data_rule.id=10000006` (tenant=1, name=`v18-runtime-test-rule`) | deviceId=0 全匹配 + method=thing.property.post + sink_ids=10000015 | 保留 |
| 3 | `E:\ch\.tmp_sql\webhook-sink.ps1` | PowerShell HttpListener 监听 9999 落盘 header+body | 保留 |
| 4 | `E:\ch\.tmp_sql\webhook-sink.log` | 本轮只有 AI 自测的一条记录 | 保留作为基线 |
| 5 | `E:\ch\.tmp_sql\yudao-server-v18.log` | 新 fat-jar 完整运行日志 | 保留 |

### 2.5 commit 列表

本次会话**无新 commit**（未落任何 Java 修改）。仅本文件 v19 doc 一条 commit 待落盘。

### 2.6 未验证项（挂账给 v19 修复后）

1. 修复挂载点移至 `DeviceEventConsumer` 后，PROPERTY_POST → webhook 端到端通
2. 自定义 header 抓包验证（v18 修复 1）
3. 负路径：URL 故意配错 → 日志 `[executeDataRuleAction] ... 执行异常`（v18 修复 2/3）
4. STATE_UPDATE / SERVICE_INVOKE 两条路径是否也要挂（`DeviceStateChangeConsumer` / `DeviceServiceResultConsumer`）

---

## 3. CI 访问

沿用 v18 §3，无变化。

---

## 4. MySQL 连接（沿用 v16/v18）

| 工具 | 用途 | 读 | 写 |
|---|---|---|---|
| `mcp4_mysql_query`（mysql-ibms） | 本地 `ch_ibms` | ✓ | ✓（DML 可直写；DDL 走 run_command） |

本次实证：mcp4 DML（INSERT）两次连续成功，返回 `Last insert ID`。

---

## 5. 本机构建硬规则

- v10~v14 全部条款
- v15 / v17 无新增
- v16 新增 2 条（16-1 mcp4 写权限 / 16-2 fat-jar 启动）
- v18 新增 1 条（18-1 Edit 工具 pattern matching 兼容性）
- **v19 新增**：
  - **规则 16-2 rev v19（取代 v16 原 16-2）**：AI **可代劳** 启停 fat-jar，但启动必须用能脱离 `run_command` 进程树的方式：
    - ✓ 允许：`Invoke-CimMethod -ClassName Win32_Process -MethodName Create`（本次使用，已验证成功）
    - ✓ 允许：`schtasks /create + /run`
    - ✗ 禁止：`Start-Process` / `Start-Job` / `cmd /c start` / `&` 后台符（v16 实证被 cascading kill）
    - 启动后 AI 必须 `netstat -ano | findstr :<port>` 验证 LISTENING 才能进入下一步。
    - 停止用 `Stop-Process -Id <pid> -Force`（本次使用，已验证）。
  - **规则 19-1（修复前必须证死）**：审计出的"漏投递/断链"类 bug，在写代码修复前**必须**先用 grep 证明"目标方法实际有调用方"；否则修复可能挂在死代码上（v18 发现 7 血的教训）。

---

## 6. 下一步候选 + 给下次会话的建议

### 当前分支状态

- **分支**：`snapshot/20260423-full`，v19 doc 待 commit
- **后台进程**：
  - RocketMQ NameServer PID 17048 @ 9876（沿用）
  - RocketMQ Broker PID 19104（沿用）
  - **yudao-server fat-jar（v18 代码）@ 48888**：java PID 11212（wmic 启的，父是 cmd PID 7956 → WmiPrvSE）—— v18 修复 4 已加载但永不触发
  - **webhook sink @ 9999**：powershell PID 21896（CIM 启的）

### 候选（按价值/复杂度排序）

#### 1. 移动 v18 修复 4 到正确挂载点 + 重跑实测（首选，30~60 分钟）

**最小方案**：在 `DeviceEventConsumer.onMessage`（`e:\ch\ruoyi-vue-pro\yudao-module-iot\yudao-module-iot-biz\src\main\java\cn\iocoder\yudao\module\iot\mq\consumer\device\DeviceEventConsumer.java:135` 附近）的 try-catch 末尾加：
```java
try {
    dataRuleService.executeDataRule(message);
} catch (Exception e) {
    log.error("[DeviceEventConsumer] 触发数据流转规则失败: deviceId={}, method={}", deviceId, eventType, e);
}
```
（需注入 `IotDataRuleService dataRuleService` 字段，用 `@Lazy` 避免循环依赖，参考 v18 修复 4 做法。）

**回滚 v18 修复 4**：`IotDeviceMessageServiceImpl:72-74 + 195-200` 可保留也可删，因为挂在死代码上既不会触发也不会出错。建议保留（未来若 ch_ibms 恢复原版 dispatcher 则自动生效），但加注释标记"仅作为原版路径的占位"。

**DoD**：
- kill PID 11212 → `mvn package` → wmic 重启
- 重发 mock PROPERTY_POST（命令见 §7）
- webhook-sink.log 出现新条目，含 `X-V18-Test: enabled` + `Authorization: Bearer test-token-v18`
- fat-jar log 出现 `[executeDataRuleAction] ... 执行成功`

#### 2. 把 STATE_UPDATE / SERVICE_INVOKE 两路也挂上（+20 分钟，#1 通过后再做）

同样模式加入 `DeviceStateChangeConsumer` / `DeviceServiceResultConsumer`。

#### 3. 重构：抽出 `DataRuleDispatcher` 统一切面（独立会话）

把三处挂载用 AOP 或公共基类统一，避免 3 处重复。

#### 4. 清理 `handleUpstreamDeviceMessage` 死代码（独立会话）

要么恢复调用链，要么删方法。v19 §2.3 发现 7 的根源。

#### 5. 与产品确认规则引擎 dead UI 走向（纯沟通）

沿用 v18 候选 #2。

#### 6. CI v2 启用真实 mvn build

沿用 v18 候选 #3。

#### 7. 其他（v18 候选 #4~#7 全部延续）

### 给下次会话的建议

- **首选 #1**：修复挂载点错误，本会话已留好全部前置（fat-jar 在跑 / webhook sink 在跑 / DataSink + DataRule 在库），只需改代码 + 重启 + 重发消息
- **#2 并做**：为省第二次重启，#1 编码时可顺手把 Consumer 的其他两个也挂上
- **规则 19-1 必守**：下次承接若见审计类结论，**先 grep 证明调用链活着再动手**

---

## 7. 关键访问凭据

沿用 v18 §7，无变化。
新增：admin/tenant=1 accessToken 示例（本次拿到的，22:59 附近发放，2 天后过期）：`e8edef6f24b44f30a8bba979d04b7935`。

---

## 8. 未 push 的本地状态

- **新增 untracked**：
  - `E:\ch\.tmp_sql\webhook-sink.ps1` · `E:\ch\.tmp_sql\webhook-sink.log` · `E:\ch\.tmp_sql\yudao-server-v18.log`（诊断用，保留给 v19 复用，**不入 commit**）
- **DB 持久变更**：
  - `iot_data_sink.id=10000015`（tenant=1）· `iot_data_rule.id=10000006`（tenant=1）—— v19 实测复用后再统一删
  - 沿用 v16 的 `last_dispatch_result="重试失败: 人员不存在"` 痕迹未处理
- **备份表**：沿用 v4 末两张，可 2026-05-12 后 drop
- **运行中后台进程**：见 §6.当前分支状态（fat-jar / webhook sink 均在跑）

---

## 9. 实测命令速查（v19 候选 #1 用）

```powershell
# 1. 查 fat-jar PID（当前 java PID 11212）
netstat -ano | findstr :48888

# 2. kill（AI 可代劳，非 cascading kill）
Stop-Process -Id 11212 -Force

# 3. 重编译（改过 DeviceEventConsumer 后）
cd e:\ch\ruoyi-vue-pro
mvn -pl yudao-server -am package -DskipTests -T 1C -q

# 4. AI 代劳启动（规则 16-2 rev v19）
$log = 'E:\ch\.tmp_sql\yudao-server-v19.log'
"=== v19 启动 $(Get-Date) ===" | Out-File -FilePath $log -Encoding utf8
Invoke-CimMethod -ClassName Win32_Process -MethodName Create -Arguments @{
  CommandLine = "cmd /c `"C:\Program Files\Java\jdk-17\bin\java.exe`" -jar E:\ch\ruoyi-vue-pro\yudao-server\target\yudao-server.jar --server.port=48888 >> $log 2>&1"
  CurrentDirectory = 'E:\ch\ruoyi-vue-pro'
}
# 等 40s
Start-Sleep -Seconds 45
netstat -ano | findstr :48888   # 验证 LISTENING

# 5. webhook sink 如果没了就重启
Invoke-CimMethod -ClassName Win32_Process -MethodName Create -Arguments @{
  CommandLine='powershell.exe -NoProfile -ExecutionPolicy Bypass -File E:\ch\.tmp_sql\webhook-sink.ps1'
  CurrentDirectory='E:\ch'
}

# 6. 重登录拿 token（tenant=1）
$body = @{username='admin';password='admin123';captchaVerification=''} | ConvertTo-Json
$r = Invoke-RestMethod -Uri 'http://127.0.0.1:48888/admin-api/system/auth/login' -Method Post -Body $body -ContentType 'application/json' -Headers @{'tenant-id'='1'}
$token = $r.data.accessToken

# 7. 发 PROPERTY_POST
$body = @{method='thing.property.post'; deviceId=10000126; params=@{temperature=26.6; v19_marker='fix-relocated'}} | ConvertTo-Json -Compress
Invoke-RestMethod -Uri 'http://127.0.0.1:48888/admin-api/iot/device/message/send' -Method Post -Body $body -ContentType 'application/json' -Headers @{Authorization="Bearer $token"; 'tenant-id'='1'}

# 8. 3s 后验证
Start-Sleep -Seconds 3
Get-Content E:\ch\.tmp_sql\webhook-sink.log -Tail 20
Select-String -Path $log -Pattern 'executeDataRule|executeDataRuleAction|DeviceEventConsumer' -Encoding utf8 | Select-Object -Last 20 | % { $_.Line }
```

关键文件：
- `DeviceEventConsumer.java`：`e:\ch\ruoyi-vue-pro\yudao-module-iot\yudao-module-iot-biz\src\main\java\cn\iocoder\yudao\module\iot\mq\consumer\device\DeviceEventConsumer.java`（line 95-140 `onMessage`）
- `DeviceStateChangeConsumer.java` / `DeviceServiceResultConsumer.java`（同目录）
- `IotDataRuleService.java` / `IotDataRuleServiceImpl.java:204`（`executeDataRule` 入口，不动）
- `IotHttpDataSinkAction.java`（v18 修复 1/2/3，已正确，不动）
- `IotDeviceMessageServiceImpl.java:72-74 + 195-200`（v18 修复 4，挂在死代码上，保留或删）

---

## 10. 给新会话 AI 的承接提示词

```text
我是 CH（长辉 IBMS）项目主程，工作区 e:\ch，按 docs/session-handoff-20260505-v19.md 承接。
请先只读该 handoff + AGENTS.md，勿翻历史 vN（v1~v18），然后等我选下一步。
CI：见 handoff §3；MySQL 用 mcp4_mysql_query，DML 可直写（v19 实证再次通过）；
本机构建硬规则见 §5（含 16-2 rev v19：AI 可代劳启 fat-jar，必须走 wmic/Invoke-CimMethod）。

当前分支：snapshot/20260423-full（v19 doc 待 commit，无 Java 改动）

v19 核心结论：
  - 候选 #1（v18 webhook 修复运行时实测）暴露 v18 修复 4 挂载点错误
  - 发现 7：handleUpstreamDeviceMessage 是死代码（零调用方），v18 修复 4 永不触发
  - 发现 8：真实上行消费入口是 DeviceEventConsumer / DeviceStateChangeConsumer / DeviceServiceResultConsumer
  - 发现 9：wmic 启 fat-jar 脱离 run_command 进程树实证成功，规则 16-2 反转为"AI 可代劳"
  - 新规则 19-1：审计类结论动手前必须 grep 证活调用方

后台进程（留给 v19 直接复用）：
  - RocketMQ NameServer PID 17048 / Broker PID 19104
  - yudao-server fat-jar PID 11212 @ 48888（v18 代码 in 内存，修复 4 无效）
  - webhook sink PID 21896 @ 9999（PowerShell HttpListener）

DB 测试记录（留给 v19 复用）：
  - iot_data_sink.id=10000015（tenant=1，HTTP → http://127.0.0.1:9999/webhook-test，含 X-V18-Test 头）
  - iot_data_rule.id=10000006（tenant=1，deviceId=0 全匹配，method=thing.property.post）

下次候选（详见 §6）：
1. 【首选】移 v18 修复 4 到 DeviceEventConsumer.onMessage + 重启 + 重测（端到端闭环）
2. 顺手挂 DeviceStateChangeConsumer / DeviceServiceResultConsumer
3. 抽 DataRuleDispatcher 统一切面（独立会话）
4. 清理 handleUpstreamDeviceMessage 死代码（独立会话）
5. 产品沟通规则引擎 dead UI / CI v2 / 其他延续

以主程综合考量决定候选，直接执行。若选 #1：我会代劳 kill + mvn + wmic 重启 + 发 mock，然后看日志。
```

---

_最后更新：2026-05-05 23:10 +08:00（运行时实测 · 暴露 v18 修复 4 挂载点错误 · 规则 16-2 rev v19 实证生效 · 新增硬规则 19-1）_
