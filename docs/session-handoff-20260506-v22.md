# Session Handoff 2026-05-06 v22（v21 候选 #1 落地：DataRuleDispatcher 统一切面）

> 承接规则：仅读本文件 + `AGENTS.md`，**不要翻历史 vN（含 v1~v21）**（除非用户明确要求）。

---

## 1. 项目骨架

- **后端**：`ruoyi-vue-pro/`
- **管理端**：`yudao-ui-admin-vue3/`
- **DB**：MySQL 8 @ `127.0.0.1:3306/ch_ibms`（`root/123456`）

---

## 2. 本次会话变更

### 2.1 执行内容

执行 v21 §6 候选 **#1（抽 `IotDataRuleDispatcher` 统一切面）**，AI 全代劳完成（规则 16-2 rev v19 + 规则 19-1 grep 证活 + 规则 20-1 强制 clean package 全程生效）。

### 2.2 commit 列表（snapshot/20260423-full）

| commit | 描述 | 文件数 | 改动行 |
|---|---|---|---|
| `966e9b9` | refactor(iot): 抽 IotDataRuleDispatcher 统一三个 consumer 数据流转触发 (v22 候选 #1) | 4 | +78 / -42 |

已 push 双远端（origin 配置多 pushurl）：
- `ssh://root@192.168.1.253/opt/ci/cache/git/ch_ibms.git` → `e65a9a6..966e9b9`
- `git@github.com:fengxiatao/ch_ibms.git` → `e65a9a6..966e9b9`

### 2.3 落地改动详情

#### 新增 `IotDataRuleDispatcher.java`（`yudao-module-iot/yudao-module-iot-biz/.../service/rule/data/`）

```java
@Component
public class IotDataRuleDispatcher {
    @Resource @Lazy
    private IotDataRuleService dataRuleService;

    public void dispatch(IotDeviceMessage message, String sourceTag) {
        if (message == null || dataRuleService == null) { return; }
        try {
            dataRuleService.executeDataRule(message);
        } catch (Exception e) {
            log.error("[IotDataRuleDispatcher] 触发数据流转规则失败: source={}, deviceId={}, method={}, requestId={}",
                    sourceTag, message.getDeviceId(), message.getMethod(), message.getRequestId(), e);
        }
    }
}
```

设计点：
- **统一 `@Lazy` 持有 `IotDataRuleService`**：消除三个 consumer 各自 `@Lazy` 的样板
- **统一 ERROR 日志格式**：`source` 标识来源 consumer，`deviceId/method/requestId` 都从 `IotDeviceMessage` 直取（不需调用方再传 ctx）
- **吞噬异常**：数据流转规则是旁路消费，绝不允许阻断设备主链路（状态机/事件存储/前端推送）

#### `DeviceEventConsumer.java`

- 删除 `IotDataRuleService dataRuleService` 字段（含 `@Lazy`）+ 删除 `import org.springframework.context.annotation.Lazy`
- 新增 `@Resource IotDataRuleDispatcher dataRuleDispatcher`
- 第 137~150 行的 `try { dataRuleService.executeDataRule(message) } catch` 块 → `dataRuleDispatcher.dispatch(message, "DeviceEventConsumer")` 单行

#### `DeviceServiceResultConsumer.java`

同上模式：字段替换 + 单行 dispatch。错误日志原本含 `requestId`，dispatcher 内部从 message 自动取，等价。

#### `DeviceStateChangeConsumer.java`

- 字段替换 + 删除 `Lazy` import
- `buildStateUpdateMessage(message)` **保留在 consumer 内**（STATE_UPDATE 路径独有的 message 适配，下沉到 dispatcher 反而会污染通用接口）
- 调用：`IotDeviceMessage stateMsg = buildStateUpdateMessage(message); dataRuleDispatcher.dispatch(stateMsg, "DeviceStateChangeConsumer");`

### 2.4 关键技术发现（v22 新增 1 条）

#### 发现 15：dispatcher 内部 `@Lazy` 替代 consumer 各自 `@Lazy` 是安全的

抽切面前担心：消除 consumer 内 `@Lazy` 直接换成 `@Resource IotDataRuleDispatcher`（普通 eager），是否会重新引入构造期循环依赖？

实测：**不会**。因为 `IotDataRuleDispatcher` 自身无业务依赖，仅 `@Lazy` 持有 `IotDataRuleService`。Spring 创建 dispatcher bean 时不需要 IotDataRuleService 实例（@Lazy 改为代理），三个 consumer 拿到的 dispatcher 实例就绪后，第一次 `dispatch()` 调用时才解析真实 service。

启动日志确认：`Started YudaoServerApplication in 32.487 seconds`，无 `BeanCreationException` / `UnsatisfiedDependencyException`。

### 2.5 运行时实测（端到端三次）

webhook sink @ 9999 收到的 POST（`E:\ch\.tmp_sql\webhook-sink.log`）：

```
--- 2026-05-06 09:23:36.798 ---
  method=thing.property.post, params={v22_marker:dispatcher-test, temperature:29.9}
  → 经 DeviceEventConsumer.dispatch path

--- 2026-05-06 09:23:39.919 ---
  method=thing.state.update, state=1, stateName=在线, previousState=0, reason=v22_marker=dispatcher-online
  → 经 DeviceStateChangeConsumer.dispatch path（buildStateUpdateMessage + dispatch）

--- 2026-05-06 09:23:43.059 ---
  method=thing.state.update, state=0, stateName=未激活, previousState=1, reason=v22_marker=dispatcher-offline
  → 经 DeviceStateChangeConsumer.dispatch path（offline）
```

三条都包含 `X-V18-Test: enabled` + `Authorization: Bearer test-token-v18` 自定义 header（来自 sink 10000015 配置），证明从 consumer → dispatcher → executeDataRule → executeDataRuleAction → HTTP sink action 全链路通畅。

`DeviceServiceResultConsumer.dispatch` 路径本次未触发（需要 service.invoke reply），但代码改动等价（同样的字段替换 + 单行 dispatch），承接 v19 实测结论。

### 2.6 未验证项 / 挂账给 v23

1. **v18 修复 2/3 负路径实测**（5 分钟）— 把 sink URL 改成 9998 不通端口，验证 dispatcher 的 ERROR 日志格式 `[IotDataRuleDispatcher] 触发数据流转规则失败: source=...` 是否正确出现且不阻塞下一条
2. **fat-jar 应用日志落盘配置**（10 分钟）— logback file appender，方便从 server 侧抓 INFO/ERROR
3. **调试端点 `/test-publish-state-change` profile 守卫**（5 分钟）— 加 `@ConditionalOnProperty(name="yudao.iot.debug-endpoints.enabled")`
4. **DeviceServiceResultConsumer 路径回归实测**（短，需触发 service.invoke reply 链路）

---

## 3. CI 访问

沿用 v19 §3，无变化。

---

## 4. MySQL 连接

| 工具 | 用途 | 读 | 写 |
|---|---|---|---|
| `mcp4_mysql_query`（mysql-ibms） | 本地 `ch_ibms` | ✓ | ✓（DML 直写；DDL 走 run_command） |

本次会话**未**操作 DB（沿用 v21 已建数据：sink 10000015 / rule 10000006 (property.post) / rule 10000007 (state.update) / device 10000126）。

---

## 5. 本机构建硬规则

- v10~v14、v15、v17 全部条款
- v16 新增 2 条（16-1 mcp4 写权限；16-2 已被 v19 覆盖）
- v18 新增 1 条（18-1 Edit 工具 pattern matching 兼容性）
- v19 新增 2 条（16-2 rev v19 wmic 启停；19-1 grep 证活）
- v20 新增 1 条（20-1 强制 `mvn clean package` 不带 `-q`）
- **v22 无新增硬规则**（本次实测 v20 全部规则均工作正常）

---

## 6. 下一步候选 + 给下次会话的建议

### 当前分支状态

- **分支**：`snapshot/20260423-full` @ `966e9b9`（v22 doc 待 commit）
- **后台进程**（留给 v23 直接复用）：
  - RocketMQ NameServer PID 17048 @ 9876（沿用）
  - RocketMQ Broker PID 19104（沿用）
  - **yudao-server fat-jar PID 1672 @ 48888**（v22 全代码 in 内存，含 IotDataRuleDispatcher）
  - webhook sink @ 9999（沿用 v21 启动的 http.sys 监听）

### 候选（按价值/复杂度排序）

#### 1. v18 修复 2/3 负路径实测（5~10 分钟，验证 dispatcher 错误路径）

把 sink 10000015 的 URL 改成 `http://127.0.0.1:9998/dead-port`（不通），触发 property.post → 期望：
- webhook sink 不收 POST
- yudao-server 日志出现 `[IotDataRuleDispatcher] 触发数据流转规则失败: source=DeviceEventConsumer, deviceId=10000126, method=thing.property.post, requestId=...`
- **下一条**正常消息（恢复 URL 后）能继续闭环

DoD：日志格式正确 + 不阻塞主流程。

#### 2. fat-jar 应用日志落盘（10 分钟，纯运维）

沿用 v21 候选 #3。在 `application-local.yaml` 或 `logback-spring.xml` 加 file appender。
DoD：`E:\ch\.tmp_sql\yudao-server-app.log` 持续滚动写入，能 grep 到 `[IotDataRuleDispatcher]` / `[DeviceStateChangeConsumer] 状态变更处理完成`。

#### 3. 调试端点 profile 守卫（5 分钟）

沿用 v21 候选 #4。给 `/test-publish-state-change` 加 `@ConditionalOnProperty(name = "yudao.iot.debug-endpoints.enabled", havingValue = "true")`。
DoD：默认 profile 下 404；加 `yudao.iot.debug-endpoints.enabled=true` 后可调。

#### 4. DeviceServiceResultConsumer 路径完整回归（10~20 分钟）

通过下行命令请求 → 设备网关 reply → DEVICE_SERVICE_RESULT 主题 → consumer → dispatcher。
需要：定位一个能稳定 reply 的设备 + 一条 service.invoke 命令路径。

DoD：webhook sink 收到 method=`thing.service.invoke_reply`（或 `service.invoke` 视实际 method 命名）的 POST，dispatcher 路径全链路验证完成。

#### 5. 与产品确认规则引擎 dead UI 走向（纯沟通）

沿用 v21 候选 #5。

#### 6. CI v2 启用真实 mvn build

沿用 v21 候选 #6。

#### 7. 其他延续

v19 候选 #5、v18 候选 #4~#7 全部继续挂账。

### 给下次会话的建议

- **首选 #1 + #2 组合**：15 分钟内可同会话完成，给 dispatcher 抽切面收个干净的尾（错误路径 + 落盘日志）
- **#3 单独 5 分钟**：production-ready 评审前必做
- **#4 看时间**：完整覆盖 service.invoke reply 路径，使三个 consumer 都有正向实证
- **规则 19-1 必守**：动手前先 grep 证活调用方
- **规则 20-1 必守**：改 Java 后 `mvn clean package`，不要 `-q`

---

## 7. 关键访问凭据

沿用 v19 §7。本次会话 admin/tenant=1 accessToken：`be1765d6bda94f54ab0680e30cfe769c`（仅参考，已过期周期内）。

---

## 8. 未 push 的本地状态

- **本地 commit**（已 push 双远端 `966e9b9`）：refactor(iot): 抽 IotDataRuleDispatcher 统一三个 consumer
- **本次 doc commit 待加入**：本文件 v22
- **本地 untracked（不入 commit）**：
  - `E:\ch\.tmp_sql\webhook-sink.ps1` / `webhook-sink.log` / `yudao-server-v22.log`
  - 历史 `docs/session-handoff-20260504-v2~v10.md`（曾用名，未追踪）
- **DB 持久变更**：本次会话**未**新增（沿用 v21：sink 10000015 / rule 10000006 / rule 10000007 / device 10000126）
- **运行中后台进程**：见 §6.当前分支状态

---

## 9. 实测命令速查（v23 候选 #1 / #2 用）

```powershell
# 0. 复用 token（如失效则重登录）
$body = @{username='admin';password='admin123';captchaVerification=''} | ConvertTo-Json
$r = Invoke-RestMethod -Uri 'http://127.0.0.1:48888/admin-api/system/auth/login' -Method Post -Body $body -ContentType 'application/json' -Headers @{'tenant-id'='1'}
$token = $r.data.accessToken

# 1. 触发 thing.property.post（DeviceEventConsumer 路径）
$body = '{"deviceId":10000126,"method":"thing.property.post","params":{"v23_marker":"test","temperature":30.0}}'
Invoke-RestMethod -Uri 'http://127.0.0.1:48888/admin-api/iot/device/message/send' -Method Post -Body $body -ContentType 'application/json' -Headers @{'tenant-id'='1'; 'Authorization'="Bearer $token"}

# 2. 触发 thing.state.update（DeviceStateChangeConsumer 路径）
$body = '{"deviceId":10000126,"newState":1,"previousState":0,"reason":"v23_marker=test","deviceType":"CAMERA","tenantId":1}'
Invoke-RestMethod -Uri 'http://127.0.0.1:48888/admin-api/iot/device/message/test-publish-state-change' -Method Post -Body $body -ContentType 'application/json' -Headers @{'tenant-id'='1'; 'Authorization'="Bearer $token"}

# 3. 看 webhook sink 收到的最新 POST
Get-Content E:\ch\.tmp_sql\webhook-sink.log -Tail 25

# 4. 看 yudao-server 日志（注意：fat-jar stdout 仅捕获启动早期，深度日志暂未落盘 — 见 v23 候选 #2）
Get-Content E:\ch\.tmp_sql\yudao-server-v22.log -Tail 30
Select-String -Path E:\ch\.tmp_sql\yudao-server-v22.log -Pattern 'IotDataRuleDispatcher|执行数据流转'

# 5. 改 Java 后强制 clean package（规则 20-1）
cd e:\ch\ruoyi-vue-pro
mvn -pl yudao-module-iot/yudao-module-iot-biz,yudao-server -am clean package -DskipTests -T 1C

# 6. kill + wmic 重启（规则 16-2 rev v19）
netstat -ano | findstr :48888
Stop-Process -Id <pid> -Force
$log = 'E:\ch\.tmp_sql\yudao-server-v23.log'
"=== v23 启动 $(Get-Date) ===" | Out-File -FilePath $log -Encoding utf8
Invoke-CimMethod -ClassName Win32_Process -MethodName Create -Arguments @{
  CommandLine = "cmd /c `"C:\Program Files\Java\jdk-17\bin\java.exe`" -jar E:\ch\ruoyi-vue-pro\yudao-server\target\yudao-server.jar --server.port=48888 >> $log 2>&1"
  CurrentDirectory = 'E:\ch\ruoyi-vue-pro'
}
Start-Sleep -Seconds 50
netstat -ano | findstr :48888

# 7. 负路径测试（v23 候选 #1）：mcp4 改 sink URL
# UPDATE iot_data_sink SET config = REPLACE(config, '9999/webhook-test', '9998/dead-port') WHERE id = 10000015;
# 触发 → 看日志 [IotDataRuleDispatcher] 触发数据流转规则失败 → 改回 → 验证恢复
```

关键文件：
- `IotDataRuleDispatcher.java`（v22 新增，统一切面）
- `DeviceEventConsumer.java`（v22 改：单行 dispatch）
- `DeviceServiceResultConsumer.java`（v22 改：单行 dispatch）
- `DeviceStateChangeConsumer.java`（v22 改：buildStateUpdateMessage + dispatch；buildStateUpdateMessage 仍在第 720~735 行）
- `IotDataRuleServiceImpl.java`：第 205 行 `executeDataRule(IotDeviceMessage)` 入口
- `IotDeviceMessageController.java`（v21 新增 `/test-publish-state-change` 端点）

---

## 10. 给新会话 AI 的承接提示词

```text
我是 CH（长辉 IBMS）项目主程，工作区 e:\ch，按 docs/session-handoff-20260506-v22.md 承接。
请先只读该 handoff + AGENTS.md，勿翻历史 vN（v1~v21），然后等我选下一步。
CI：见 handoff §3；MySQL 用 mcp4_mysql_query，DML 可直写（v19 实证）；
本机构建硬规则见 §5（含 16-2 rev v19 wmic + 19-1 grep 证活 + 20-1 强制 clean package）。

当前分支：snapshot/20260423-full @ 966e9b9（v22 doc 待 commit，已 push 双远端）

v22 核心结论（已 commit + push）：
  - 966e9b9 refactor(iot): 抽 IotDataRuleDispatcher 统一三个 consumer 数据流转触发
  - 端到端三次实测全通：property.post + state.update online + state.update offline
  - 三个 consumer (DeviceEvent / DeviceServiceResult / DeviceStateChange) 现在统一调用
    dataRuleDispatcher.dispatch(msg, sourceTag) 单行；dispatcher 内部 @Lazy 持有 IotDataRuleService

后台进程（留给 v23 直接复用）：
  - RocketMQ NameServer PID 17048 / Broker PID 19104
  - yudao-server fat-jar PID 1672 @ 48888（v22 代码 in 内存）
  - webhook sink @ 9999（沿用 v21 启动）

DB 测试记录（沿用 v21，未变化）：
  - sink 10000015 / rule 10000006 (property.post) / rule 10000007 (state.update) / device 10000126

下次候选（详见 §6）：
1. v18 修复 2/3 负路径实测（验证 dispatcher 错误路径）
2. fat-jar 应用日志落盘配置（logback file appender）
3. 调试端点 profile 守卫
4. DeviceServiceResultConsumer 完整回归（service.invoke reply）
5. 产品沟通 / CI v2 / 其他延续

以主程综合考量决定候选，直接执行。
```

---

_最后更新：2026-05-06 09:25 +08:00（v21 候选 #1 落地 commit + 端到端三路径回归 + DataRuleDispatcher 统一切面收尾）_
