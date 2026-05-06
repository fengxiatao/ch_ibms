# Session Handoff 2026-05-06 v21（v20 候选 #1 落地：STATE_UPDATE 路径数据流转规则全覆盖）

> 承接规则：仅读本文件 + `AGENTS.md`，**不要翻历史 vN（含 v1~v20）**（除非用户明确要求）。

---

## 1. 项目骨架

- **后端**：`ruoyi-vue-pro/`
- **管理端**：`yudao-ui-admin-vue3/`
- **DB**：MySQL 8 @ `127.0.0.1:3306/ch_ibms`（`root/123456`）

---

## 2. 本次会话变更

### 2.1 执行内容

执行 v20 §6 候选 **#1（STATE_UPDATE 路径挂数据流转规则）**，AI 全代劳完成（规则 16-2 rev v19 + 规则 20-1 全程生效）。

### 2.2 commit 列表（snapshot/20260423-full）

| commit | 描述 | 文件数 | 改动行 |
|---|---|---|---|
| `b6eab53` | feat(iot): DeviceStateChangeConsumer 挂数据流转规则 + 调试端点 (v21 候选 #1) | 2 | +95 / -0 |

已 push 双远端：
- `ssh://192.168.1.253/opt/ci/cache/git/ch_ibms.git` → `04b35b1..b6eab53`
- `github.com:fengxiatao/ch_ibms.git` → `04b35b1..b6eab53`

### 2.3 落地改动详情

#### `DeviceStateChangeConsumer.java`（`yudao-module-iot/yudao-module-iot-biz/src/main/java/cn/iocoder/yudao/module/iot/mq/consumer/device/`）

- 新增 `@Resource @Lazy IotDataRuleService dataRuleService` 字段
- `processStateChange` 末尾追加（步骤 9）：
  ```java
  try {
      if (dataRuleService != null) {
          IotDeviceMessage stateMsg = buildStateUpdateMessage(message);
          dataRuleService.executeDataRule(stateMsg);
      }
  } catch (Exception e) {
      log.error("[DeviceStateChangeConsumer] 触发数据流转规则失败: deviceId={}, newState={}", deviceId, newState, e);
  }
  ```
- 新增 `buildStateUpdateMessage(DeviceStateChangeMessage)` 适配方法：
  - method = `thing.state.update`（`IotDeviceMessageMethodEnum.STATE_UPDATE`）
  - params = `{state, stateName, previousState, reason}`
  - 透传 deviceId / deviceName / productId / tenantId / requestId

#### `IotDeviceMessageController.java`

- 新增 `@Qualifier("iotRocketMQMessageBus") IotMessageBus iotMessageBus` 字段
- 新增调试端点 `POST /admin-api/iot/device/message/test-publish-state-change`：
  - 接收 `{deviceId, newState?, previousState?, reason?, deviceType?, deviceName?, productId?, tenantId?}`
  - 直接 `iotMessageBus.post(IotMessageTopics.DEVICE_STATE_CHANGED, DeviceStateChangeMessage.of(...))`
  - 仅用于本机/集成测试，不接入业务流程
  - 复用 `iot:device:message-end` 权限

### 2.4 关键技术发现（v21 新增，3 条）

#### 发现 12：`/admin-api/iot/device/message/send` 不能用于测 STATE_UPDATE

`IotDeviceMessageProducer.sendDeviceMessage` **无视 method**，统一发到 `DEVICE_EVENT_REPORTED` 主题，由 `DeviceEventConsumer` 消费，**不会触发 `DeviceStateChangeConsumer`**。
要测 STATE_UPDATE 路径必须直接 post 到 `DEVICE_STATE_CHANGED` 主题。本次因此引入测试端点 `/test-publish-state-change`。

#### 发现 13：`ConnectionMode` 在 `core.enums`，不在 `core.gateway.dto`

`DeviceStateChangeMessage.of(...)` 签名要求 `ConnectionMode`，需 import `cn.iocoder.yudao.module.iot.core.enums.ConnectionMode`。
误以为同包会导致编译错误（IDE lint 提示包路径解析失败）。

#### 发现 14：fat-jar stdout 重定向到外部 `>> log` 文件，应用日志要找 logback 配置

通过 `Invoke-CimMethod ... cmd /c java -jar ... >> log` 启动时，stdout 仅捕获了启动早期日志（约 36KB 后停止写入）。Spring Boot logback 自身的 logfile 没在仓库 root 默认目录下。
**结论**：本机端到端验证以 webhook sink log 为主证据；server 侧调试日志需要在 logback 配置里加显式 file appender 才靠谱。本次未深挖。

### 2.5 运行时实测（端到端两次：上线 + 离线）

webhook sink 收到的 POST：

```
=== webhook sink started 2026-05-06 09:03:09 ===

--- 2026-05-06 09:03:37.348 ---
POST /webhook-test
  method=thing.state.update, state=1, stateName=在线, previousState=0,
  reason=v21_marker=state-update-first-test, deviceId=10000126, tenantId=1

--- 2026-05-06 09:04:40.222 ---
POST /webhook-test
  method=thing.state.update, state=0, stateName=未激活, previousState=1,
  reason=v21_marker=offline-test, deviceId=10000126, tenantId=1
```

两次都包含 `X-V18-Test: enabled` + `Authorization: Bearer test-token-v18` 自定义 header（来自 sink 10000015 配置）。

### 2.6 未验证项（挂账给 v22）

1. 抽 `DataRuleDispatcher` 统一切面 — 三处重复（DeviceEventConsumer / DeviceServiceResultConsumer / DeviceStateChangeConsumer）尚未抽取
2. v18 修复 2/3 负路径实测（sink URL 改不通端口 → 看 `[executeDataRuleAction] 执行异常` 日志）
3. fat-jar 应用日志落盘配置（logback 配置 file appender，方便后续会话从 server 侧看 executeDataRule INFO 日志）
4. 调试端点 `/test-publish-state-change` 是否长期保留 — 当前留在源码里，可作为 QA 工具；如要 production-ready 应加 `@Profile("!prod")` 或 `@ConditionalOnProperty`

---

## 3. CI 访问

沿用 v19 §3，无变化。

---

## 4. MySQL 连接

| 工具 | 用途 | 读 | 写 |
|---|---|---|---|
| `mcp4_mysql_query`（mysql-ibms） | 本地 `ch_ibms` | ✓ | ✓（DML 直写；DDL 走 run_command） |

本次会话使用 mcp4 INSERT 了一条规则（详见 §8）。

---

## 5. 本机构建硬规则

- v10~v14、v15、v17 全部条款
- v16 新增 2 条（16-1 mcp4 写权限；16-2 已被 v19 覆盖）
- v18 新增 1 条（18-1 Edit 工具 pattern matching 兼容性）
- v19 新增 2 条（16-2 rev v19 wmic 启停；19-1 grep 证活）
- v20 新增 1 条（20-1 强制 `mvn clean package` 不带 `-q`）
- **v21 无新增硬规则**（本次实测 v20 全部规则均工作正常）

---

## 6. 下一步候选 + 给下次会话的建议

### 当前分支状态

- **分支**：`snapshot/20260423-full` @ `b6eab53`（v21 doc 待 commit）
- **后台进程**（留给 v22 直接复用）：
  - RocketMQ NameServer PID 17048 @ 9876（沿用）
  - RocketMQ Broker PID 19104（沿用）
  - **yudao-server fat-jar PID 16812 @ 48888**（v21 全代码 in 内存，含 STATE_UPDATE 挂载）
  - **webhook sink @ 9999**（v21 重启了一次，新启动时间 2026-05-06 09:03:09）

### 候选（按价值/复杂度排序）

#### 1. 抽 `DataRuleDispatcher` 统一切面（独立会话，中等复杂度）

`DeviceEventConsumer` / `DeviceServiceResultConsumer` / `DeviceStateChangeConsumer` 三处重复 try-catch + adapter + 调用，可用 AOP 或公共基类抽取。
DoD：三个 consumer 调用统一 `dispatcher.dispatch(message)`，错误日志格式一致。

#### 2. v18 修复 2/3 负路径实测（5 分钟）

故意把 sink URL 改成不通的端口 → 观察 fat-jar 日志中 `[executeDataRuleAction][... 执行异常]` 是否完整记录 + 不阻塞主流程。
DoD：webhook sink 不收到 POST，server 日志有 ERROR 但下一条正常消息能继续闭环。

#### 3. fat-jar 应用日志落盘配置（10 分钟，纯运维）

在 `application.yaml` 或 `logback-spring.xml` 加 file appender，方便后续会话从 server 侧抓 executeDataRule INFO 日志。
DoD：`E:\ch\.tmp_sql\yudao-server-v22.log` 持续滚动写入，能 grep 到 `[DeviceStateChangeConsumer] 状态变更处理完成`。

#### 4. 调试端点收尾（5 分钟）

`/test-publish-state-change` 加 `@ConditionalOnProperty(name = "yudao.iot.debug-endpoints.enabled", havingValue = "true")` 或 `@Profile("!prod")`。
DoD：生产 profile 下 404；测试 profile 下可调。

#### 5. 与产品确认规则引擎 dead UI 走向（纯沟通）

沿用 v18 候选 #2。

#### 6. CI v2 启用真实 mvn build

沿用 v18 候选 #3。

#### 7. 其他延续

v18 候选 #4~#7、v19 候选 #5 全部继续挂账。

### 给下次会话的建议

- **首选 #1**：三处重复抽切面，是收尾整个数据流转规则触发链路的最后一步
- **#2 + #3 适合开胃**：5~10 分钟小任务可以同会话叠加
- **#4 看团队态度**：调试端点保留与否影响生产部署评审
- **规则 19-1 必守**：动手前先 grep 证活调用方
- **规则 20-1 必守**：改 Java 后 `mvn clean package`，不要 `-q`

---

## 7. 关键访问凭据

沿用 v19 §7。本次会话 admin/tenant=1 accessToken：`e4615b100f024ba894d0ce94668e13aa`（仅参考，已过期周期内）。

---

## 8. 未 push 的本地状态

- **本地 commit**（已 push 双远端 b6eab53）：feat(iot): STATE_UPDATE 路径挂载 + 调试端点
- **本次 doc commit 待加入**：本文件 v21
- **本地 untracked（不入 commit）**：
  - `E:\ch\.tmp_sql\webhook-sink.ps1` / `webhook-sink.log` / `yudao-server-v21.log`
  - 历史 `docs/session-handoff-20260504-v2~v10.md`（曾用名，未追踪）
  - 各种诊断产物 `_v21_*.txt`
- **DB 持久变更（v21 新增）**：
  - **`iot_data_rule.id=10000007`**（tenant=1，name=`v21-state-update-rule`，deviceId=0 全匹配，method=`thing.state.update`，sink_ids=10000015，status=0 ENABLE）
  - 沿用 v20：sink 10000015、rule 10000006（thing.property.post）、device 10000126
- **备份表**：沿用 v4 末两张，可 2026-05-12 后 drop
- **运行中后台进程**：见 §6.当前分支状态

---

## 9. 实测命令速查（v22 候选 #1 / #2 用）

```powershell
# 0. 复用 token（如失效则重登录）
$body = @{username='admin';password='admin123';captchaVerification=''} | ConvertTo-Json
$r = Invoke-RestMethod -Uri 'http://127.0.0.1:48888/admin-api/system/auth/login' -Method Post -Body $body -ContentType 'application/json' -Headers @{'tenant-id'='1'}
$token = $r.data.accessToken

# 1. 触发 thing.state.update（在线）
$body = '{"deviceId":10000126,"newState":1,"previousState":0,"reason":"v22_marker=test","deviceType":"CAMERA","tenantId":1}'
Invoke-RestMethod -Uri 'http://127.0.0.1:48888/admin-api/iot/device/message/test-publish-state-change' -Method Post -Body $body -ContentType 'application/json' -Headers @{'tenant-id'='1'; 'Authorization'="Bearer $token"}

# 2. 触发 thing.property.post（沿用 v18~v20）
$body = '{"deviceId":10000126,"method":"thing.property.post","params":{"v22_marker":"test","temperature":28.8}}'
Invoke-RestMethod -Uri 'http://127.0.0.1:48888/admin-api/iot/device/message/send' -Method Post -Body $body -ContentType 'application/json' -Headers @{'tenant-id'='1'; 'Authorization'="Bearer $token"}

# 3. 看 webhook sink 收到的最新 POST
Get-Content E:\ch\.tmp_sql\webhook-sink.log -Tail 15

# 4. 改 Java 后强制 clean package（规则 20-1）
cd e:\ch\ruoyi-vue-pro
mvn -pl yudao-module-iot/yudao-module-iot-biz,yudao-server -am clean package -DskipTests -T 1C

# 5. kill + wmic 重启（规则 16-2 rev v19）
netstat -ano | findstr :48888
Stop-Process -Id <pid> -Force
$log = 'E:\ch\.tmp_sql\yudao-server-v22.log'
"=== v22 启动 $(Get-Date) ===" | Out-File -FilePath $log -Encoding utf8
Invoke-CimMethod -ClassName Win32_Process -MethodName Create -Arguments @{
  CommandLine = "cmd /c `"C:\Program Files\Java\jdk-17\bin\java.exe`" -jar E:\ch\ruoyi-vue-pro\yudao-server\target\yudao-server.jar --server.port=48888 >> $log 2>&1"
  CurrentDirectory = 'E:\ch\ruoyi-vue-pro'
}
Start-Sleep -Seconds 50
netstat -ano | findstr :48888
```

关键文件：
- `DeviceStateChangeConsumer.java`（v21 已挂数据流转规则，第 271~281 行调用、第 720~735 行 buildStateUpdateMessage）
- `DeviceEventConsumer.java`（v19 已挂，第 143~150 行）
- `DeviceServiceResultConsumer.java`（v19 已挂）
- `IotDataRuleServiceImpl.java`：第 205 行 `executeDataRule(IotDeviceMessage)` 入口
- `IotDeviceMessageController.java`（v21 新增 `/test-publish-state-change` 端点）

---

## 10. 给新会话 AI 的承接提示词

```text
我是 CH（长辉 IBMS）项目主程，工作区 e:\ch，按 docs/session-handoff-20260506-v21.md 承接。
请先只读该 handoff + AGENTS.md，勿翻历史 vN（v1~v20），然后等我选下一步。
CI：见 handoff §3；MySQL 用 mcp4_mysql_query，DML 可直写（v19 实证）；
本机构建硬规则见 §5（含 16-2 rev v19 wmic + 19-1 grep 证活 + 20-1 强制 clean package）。

当前分支：snapshot/20260423-full @ b6eab53（v21 doc 待 commit，已 push 双远端）

v21 核心结论（已 commit + push）：
  - b6eab53 feat(iot): DeviceStateChangeConsumer 挂数据流转规则 + 调试端点
  - 端到端两次实测均通过：online (state=1) + offline (state=0) → webhook 09:03:37 / 09:04:40 收到
  - 上行三大消息类型全覆盖：property.post / event.post / state.update / service.invoke reply
  - 新发现 12：/message/send 无视 method 统一发 DEVICE_EVENT_REPORTED，不能用于测 STATE_UPDATE

后台进程（留给 v22 直接复用）：
  - RocketMQ NameServer PID 17048 / Broker PID 19104
  - yudao-server fat-jar PID 16812 @ 48888（v21 代码 in 内存）
  - webhook sink @ 9999（http.sys PID 4，新启动 09:03:09）

DB 测试记录：
  - 沿用 v20：sink 10000015 / rule 10000006（property.post）/ device 10000126
  - v21 新增：rule 10000007（state.update）

下次候选（详见 §6）：
1. 抽 DataRuleDispatcher 统一切面（三处重复 → AOP/基类）
2. v18 修复 2/3 负路径实测（5 分钟）
3. fat-jar 应用日志落盘配置（logback file appender）
4. 调试端点 /test-publish-state-change 加 profile 守卫
5. 产品沟通 / CI v2 / 其他延续

以主程综合考量决定候选，直接执行。
```

---

_最后更新：2026-05-06 09:06 +08:00（v20 候选 #1 落地 commit + 端到端 STATE_UPDATE 双路径闭环 + 新增调试端点）_
