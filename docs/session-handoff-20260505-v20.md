# Session Handoff 2026-05-05 v20（v19 候选 #1+#2 落地 commit · 死代码清理 · webhook 端到端闭环）

> 承接规则：仅读本文件 + `AGENTS.md`，**不要翻历史 vN（含 v1~v19）**（除非用户明确要求）。

---

## 1. 项目骨架

- **后端**：`ruoyi-vue-pro/`
- **管理端**：`yudao-ui-admin-vue3/`
- **DB**：MySQL 8 @ `127.0.0.1:3306/ch_ibms`（`root/123456`）

---

## 2. 本次会话变更

### 2.1 执行内容

执行 v19 §6 候选 **#1 + #2（部分）+ 死代码清理**，全部由 AI 代劳完成（规则 16-2 rev v19 生效）：

1. 修复 v18 修复 4 挂载点错误（迁移到真实消费入口）
2. 顺手挂 `DeviceServiceResultConsumer`（service.invoke reply 路径）
3. 死代码清理（用户决策："删除，尽可能使得项目代码干净无干扰"）
4. 端到端运行时实测两次（清理前 + 清理后）均通过

### 2.2 commit 列表（snapshot/20260423-full）

| commit | 描述 | 文件数 | 改动行 |
|---|---|---|---|
| `c921fdb` | fix(iot): 数据流转规则挂载点迁移到真实消费入口 (v19) | 2 | +44 / -0 |
| `2d256bb` | refactor(iot): 删除 IotDeviceMessageServiceImpl 中的死代码 handleUpstreamDeviceMessage | 2 | +0 / -128 |
| (待) | docs(handoff): v20 - v19 候选 #1+#2 落地 + 死代码清理 + webhook 端到端闭环 | 1 | 本文件 |

### 2.3 落地改动详情

#### `c921fdb` 挂载点迁移

**`DeviceEventConsumer.java`**（`yudao-module-iot/yudao-module-iot-biz/src/main/java/cn/iocoder/yudao/module/iot/mq/consumer/device/`）：

- 新增 `@Resource @Lazy IotDataRuleService dataRuleService` 字段
- `onMessage` 在 `routeToHandler` 后、`pushToFrontend` 前追加：
  ```java
  try {
      if (dataRuleService != null) {
          dataRuleService.executeDataRule(message);
      }
  } catch (Exception e) {
      log.error("[DeviceEventConsumer] 触发数据流转规则失败: ...", e);
  }
  ```

**`DeviceServiceResultConsumer.java`**（同目录）：

- 同样模式注入 + 在 `routeToHandler` 后调 `executeDataRule(message)`

**未挂**：`DeviceStateChangeConsumer.java` 入参为 `DeviceStateChangeMessage`（非 `IotDeviceMessage`），需要构造适配，留待下次会话独立修复（候选 #1）。

#### `2d256bb` 死代码清理

**`IotDeviceMessageService.java`**（接口）：删除 `void handleUpstreamDeviceMessage(IotDeviceMessage, IotDeviceDO)` 方法签名 + javadoc。

**`IotDeviceMessageServiceImpl.java`**（实现）：

- 删除 `public void handleUpstreamDeviceMessage(...)` 方法体
- 删除私有 helper `private Object handleUpstreamDeviceMessage0(...)`
- 删除 **7 个仅死代码使用的 `@Resource` 字段**：
  - `ibmsDeviceMapper`
  - `ibmsDeviceGatewaySupportService`
  - `otaTaskRecordService` (含 `@Lazy`)
  - `dataRuleService` (v18 修复 4 残留，含 `@Lazy`)
  - `deviceServiceInvoker`
  - `devicePropertyProcessor`（完全 unused，原本就没人调用）
  - `deviceEventProcessor`
- 删除 **8 个失去引用的 import**：`Assert` / `ServiceException` / `IotDeviceMessageMethodEnum` / `IbmsDeviceMapper` / `IbmsDeviceGatewaySupportService` / `IotOtaTaskRecordService` / `com.google.common.base.Objects` / `org.springframework.context.annotation.Lazy`
- **保留**字段（`sendDeviceMessage` 活代码路径引用）：`dualTrackDeviceResolver` / `devicePropertyService` / `deviceMessageMapper` / `deviceMessageProducer`

### 2.4 关键技术发现（v20 新增，2 条）

#### 发现 10：`devicePropertyProcessor` 字段历史遗留 unused

清理时 grep 发现 `devicePropertyProcessor` 字段从未被任何方法引用过（仅在字段声明那一行出现）。说明它是更早期重构遗留，连死代码 `handleUpstreamDeviceMessage0` 都没有调用它（用的是 `devicePropertyService.saveDeviceProperty`）。一并清理。

#### 发现 11：`mvn package -q` 静默模式可能跳过 Spring Boot repackage

实测 `mvn -pl yudao-server -am package -DskipTests -T 1C -q` 在源码改动后 6 秒返回 BUILD SUCCESS，但 fat-jar `LastWriteTime` 不更新。去掉 `-q` 后才看到 incremental 跳过警告。**建议**：本机改 Java 后**必须 `mvn clean package` 强制重打**，避免 fat-jar 仍是旧版（v19 实测踩坑 1 次）。

### 2.5 运行时实测（两次端到端）

#### 第一次：候选 #1+#2 落地后（含死代码 + 修复 4 残留）

```
23:26:46.498  HTTP /admin-api/iot/device/message/send
23:26:46.659  DeviceEventConsumer.onMessage 收到事件 (deviceType=UNKNOWN, deviceId=10000126, eventType=thing.property.post)
23:26:46.692  [executeDataRule] 设备(10000126) 方法(thing.property.post) 匹配到 1 条规则
23:26:52.573  [IotHttpDataSinkAction.execute] HTTP POST /webhook-test 200 OK {"ok":true}
23:26:52.581  [executeDataRuleAction] 消息(...) 数据目的(10000015) 执行成功
```

#### 第二次：死代码清理后回归（v19_marker=post-cleanup）

```
23:45:38.604  HTTP send
23:45:38.752  DeviceEventConsumer 收到事件
23:45:38.761  executeDataRule 匹配 1 条规则
23:45:38.830  HTTP DataSink 200 OK
23:45:38.834  executeDataRuleAction 执行成功（< 230ms 完整闭环，连接复用后明显加速）
```

webhook sink 两次都正确收到 `X-V18-Test: enabled` + `Authorization: Bearer test-token-v18`，body 含对应 `v19_marker`。

### 2.6 未验证项（挂账给 v20 后）

1. `DeviceStateChangeConsumer` 是否要挂数据流转规则（state.update 路径）→ 需要适配 `DeviceStateChangeMessage` → `IotDeviceMessage` 构造
2. v18 修复 2/3 的负路径（URL 配错 → `[executeDataRuleAction] 执行异常` 日志）—— 本次实测仅 200 OK 路径触发，错误路径代码已就位但未实测
3. `devicePropertyProcessor` 删除后是否真无任何 Spring 装配影响（启动正常，但只压测了 thing.property.post 一种 method）

---

## 3. CI 访问

沿用 v19 §3，无变化。

---

## 4. MySQL 连接

| 工具 | 用途 | 读 | 写 |
|---|---|---|---|
| `mcp4_mysql_query`（mysql-ibms） | 本地 `ch_ibms` | ✓ | ✓（DML 直写；DDL 走 run_command） |

本次会话仅读，未触发写操作。

---

## 5. 本机构建硬规则

- v10~v14、v15、v17 全部条款
- v16 新增 2 条（16-1 mcp4 写权限；16-2 已被 v19 覆盖）
- v18 新增 1 条（18-1 Edit 工具 pattern matching 兼容性）
- v19 新增 2 条：
  - **规则 16-2 rev v19**：AI 可代劳启停 fat-jar，启动用 `Invoke-CimMethod Win32_Process Create` 或 `schtasks`，禁止 `Start-Process` / `Start-Job` / `cmd /c start` / `&` 后台符
  - **规则 19-1**：审计类"漏投递/断链"结论修复前必须 grep 证活调用方
- **v20 新增 1 条**：
  - **规则 20-1（强制 clean package）**：本机改 Java 源码后，**必须**用 `mvn clean package`（不带 `-q`）触发 Spring Boot fat-jar repackage。`mvn package -q` 在 incremental 模式下可能跳过 jar 重打，导致内存中仍是旧代码（v19 实测踩坑 + v20 §2.4 发现 11）。

---

## 6. 下一步候选 + 给下次会话的建议

### 当前分支状态

- **分支**：`snapshot/20260423-full` @ `2d256bb`（v20 doc 待 commit）
- **后台进程**（留给 v20 直接复用）：
  - RocketMQ NameServer PID 17048 @ 9876（沿用）
  - RocketMQ Broker PID 19104（沿用）
  - **yudao-server fat-jar PID 19608 @ 48888**（v19 全部代码 + 死代码清理后 in 内存，运行正常）
  - **webhook sink @ 9999**（PowerShell HttpListener 通过 http.sys 内核驱动，PID=4）

### 候选（按价值/复杂度排序）

#### 1. 把 STATE_UPDATE 路径也挂上（30 分钟，低风险）

`DeviceStateChangeConsumer.onMessage(DeviceStateChangeMessage)` 入参类型不同，需要先构造 `IotDeviceMessage`：
```java
IotDeviceMessage msg = IotDeviceMessage.requestOf(IotDeviceMessageMethodEnum.STATE_UPDATE.getMethod(), Map.of("state", message.getNewState(), "stateName", message.getNewStateName()));
msg.setDeviceId(message.getDeviceId());
msg.setTenantId(message.getTenantId());
dataRuleService.executeDataRule(msg);
```
DoD：构造 STATE_UPDATE 触发 + 配置 method=`thing.state.update` 的 rule + webhook sink 收到。

#### 2. 抽出 `DataRuleDispatcher` 统一切面（独立会话，中等复杂度）

`DeviceEventConsumer` / `DeviceServiceResultConsumer` (+ #1 的 `DeviceStateChangeConsumer`) 三处重复 try-catch + 调用，可用 AOP 或公共基类抽取。

#### 3. v18 修复 2/3 的负路径实测（30 分钟）

故意把 sink URL 改成不通的端口 → 观察 fat-jar 日志中 `[executeDataRuleAction][... 执行异常]` 日志是否完整记录 + 不阻塞主流程。

#### 4. 与产品确认规则引擎 dead UI 走向（纯沟通）

沿用 v18 候选 #2。

#### 5. CI v2 启用真实 mvn build

沿用 v18 候选 #3。

#### 6. 其他延续

v18 候选 #4~#7、v19 候选 #5 全部继续挂账。

### 给下次会话的建议

- **首选 #1**：补齐 STATE_UPDATE 路径，让上行三大消息类型（property.post/event.post/state.update/service.invoke reply）全覆盖
- **#3 适合开胃**：负路径实测能验证 v18 修复 3 的错误日志是否完整，5 分钟改 sink URL，等同最小验证
- **规则 19-1 必守**：动手前先 grep 证活调用方
- **规则 20-1 必守**：改 Java 后 `mvn clean package`，不要 `-q`

---

## 7. 关键访问凭据

沿用 v19 §7。本次会话获得的 admin/tenant=1 accessToken（仅参考，已过期周期内）：`7d8e9ffa51ad4d8daccacc7dd4f6f255`（v19_marker=fix-relocated 测试时使用）。

---

## 8. 未 push 的本地状态

- **本地 commit**（待 push）：`c921fdb` (fix) + `2d256bb` (refactor) + 待加入的 v20 doc commit
- **本地 untracked（不入 commit）**：
  - `E:\ch\.tmp_sql\webhook-sink.ps1` / `webhook-sink.log` / `yudao-server-v19.log` / `yudao-server-v19-clean.log`
  - 历史 `docs/session-handoff-20260504-v2~v10.md`（曾用名，未追踪）
  - 各种诊断产物 `_v19_*.txt` / `_v20_*.txt`
- **DB 持久变更（沿用 v19）**：
  - `iot_data_sink.id=10000015`（tenant=1，HTTP → http://127.0.0.1:9999/webhook-test）
  - `iot_data_rule.id=10000006`（tenant=1，deviceId=0 全匹配，method=`thing.property.post`，sink_ids=10000015）
  - 沿用 v16 的 `last_dispatch_result="重试失败: 人员不存在"` 痕迹未处理
- **备份表**：沿用 v4 末两张，可 2026-05-12 后 drop
- **运行中后台进程**：见 §6.当前分支状态

---

## 9. 实测命令速查（v20 候选 #1 用）

```powershell
# 1. 查 fat-jar PID（当前 java PID 19608）
netstat -ano | findstr :48888

# 2. kill（AI 可代劳）
Stop-Process -Id 19608 -Force

# 3. 改完 DeviceStateChangeConsumer 后强制 clean package（规则 20-1）
cd e:\ch\ruoyi-vue-pro
mvn -pl yudao-module-iot/yudao-module-iot-biz,yudao-server -am clean package -DskipTests
# 注意：不要加 -q，避免 incremental 跳过 fat-jar repackage

# 4. wmic 启动（规则 16-2 rev v19）
$log = 'E:\ch\.tmp_sql\yudao-server-v20.log'
"=== v20 启动 $(Get-Date) ===" | Out-File -FilePath $log -Encoding utf8
Invoke-CimMethod -ClassName Win32_Process -MethodName Create -Arguments @{
  CommandLine = "cmd /c `"C:\Program Files\Java\jdk-17\bin\java.exe`" -jar E:\ch\ruoyi-vue-pro\yudao-server\target\yudao-server.jar --server.port=48888 >> $log 2>&1"
  CurrentDirectory = 'E:\ch\ruoyi-vue-pro'
}
Start-Sleep -Seconds 50
netstat -ano | findstr :48888

# 5. 重登录拿 token
$body = @{username='admin';password='admin123';captchaVerification=''} | ConvertTo-Json
$r = Invoke-RestMethod -Uri 'http://127.0.0.1:48888/admin-api/system/auth/login' -Method Post -Body $body -ContentType 'application/json' -Headers @{'tenant-id'='1'}
$token = $r.data.accessToken

# 6. 触发 STATE_UPDATE（如果走 /message/send 不通，可能要直接发 RocketMQ 或在 DB 里 mock）
# 略：等候选 #1 实施时再补
```

关键文件：
- `DeviceStateChangeConsumer.java`：`e:\ch\ruoyi-vue-pro\yudao-module-iot\yudao-module-iot-biz\src\main\java\cn\iocoder\yudao\module\iot\mq\consumer\device\DeviceStateChangeConsumer.java`（line 173 `onMessage`，需要在 `processStateChange` 里挂）
- `IotDataRuleServiceImpl.java`：`...\service\rule\data\IotDataRuleServiceImpl.java:205` `executeDataRule(IotDeviceMessage)` 入口
- `IotDeviceMessageMethodEnum`：`yudao-module-iot-core` 内，含 `STATE_UPDATE`

---

## 10. 给新会话 AI 的承接提示词

```text
我是 CH（长辉 IBMS）项目主程，工作区 e:\ch，按 docs/session-handoff-20260505-v20.md 承接。
请先只读该 handoff + AGENTS.md，勿翻历史 vN（v1~v19），然后等我选下一步。
CI：见 handoff §3；MySQL 用 mcp4_mysql_query，DML 可直写（v19 实证）；
本机构建硬规则见 §5（含 16-2 rev v19 wmic + 19-1 grep 证活 + 20-1 强制 clean package）。

当前分支：snapshot/20260423-full @ 2d256bb（v20 doc 待 commit）

v20 核心结论：
  - v19 候选 #1+#2 已 commit (c921fdb)：DeviceEventConsumer / DeviceServiceResultConsumer 挂上 dataRuleService.executeDataRule
  - 死代码清理已 commit (2d256bb)：删除 IotDeviceMessageServiceImpl.handleUpstreamDeviceMessage + 7 字段 + 8 imports，128 行 deletions
  - 端到端实测 < 230ms 闭环（thing.property.post → webhook sink 收到含自定义 header 的 POST）
  - 新规则 20-1：mvn package 不要带 -q，否则 fat-jar 可能不重打

后台进程（留给 v20 直接复用）：
  - RocketMQ NameServer PID 17048 / Broker PID 19104
  - yudao-server fat-jar PID 19608 @ 48888（v20 代码 in 内存，含死代码清理）
  - webhook sink @ 9999（http.sys PID 4）

DB 测试记录（留给 v20 复用）：
  - iot_data_sink.id=10000015 / iot_data_rule.id=10000006（同 v19）

下次候选（详见 §6）：
1. 【首选】挂 DeviceStateChangeConsumer（state.update 路径）
2. 抽 DataRuleDispatcher 统一切面（独立会话）
3. v18 修复 2/3 负路径实测（5 分钟改 sink URL）
4. 产品沟通 / CI v2 / 其他延续

以主程综合考量决定候选，直接执行。
```

---

_最后更新：2026-05-05 23:50 +08:00（v19 候选 #1+#2 落地 commit + 死代码清理 commit + 端到端 webhook 闭环 + 新增硬规则 20-1）_
