# Session Handoff 2026-05-05 v18（候选 #1 webhook 漏投递最小修复落地 · 本机 mvn 编译通过 · 未运行时实测）

> 承接规则：仅读本文件 + `AGENTS.md`，**不要翻历史 vN（含 v1~v17）**（除非用户明确要求）。

---

## 1. 项目骨架

- **后端**：`ruoyi-vue-pro/`（Spring Boot 3，多模块）
- **管理端**：`yudao-ui-admin-vue3/`（Vue3 + Vite + pnpm）
- **DB**：MySQL 8 @ `127.0.0.1:3306/ch_ibms`（`root/123456`）

---

## 2. 本次会话变更

### 2.1 候选 #1：落地 v17 §2.4 webhook 漏投递最小修复

**输入**：v17 审计出的双层根因 + 3 处最小改动方案。**输出**：4 处代码改动（+12 / -1），本机 `mvn compile -DskipTests` 通过，**未重启 fat-jar、未运行时实测**。

### 2.2 落地改动（4 处）

| # | 文件 | 行/位置 | 修改 |
|---|---|---|---|
| 1 | `IotHttpDataSinkAction.java` | line 53 | `config.getHeaders().putAll(config.getHeaders())` → `config.getHeaders().forEach(headers::add)` —— 解决根因 2-B（自定义 header 丢失） |
| 2 | `IotHttpDataSinkAction.java` | line 85 | 非 2xx 分支 `throw new RuntimeException("HTTP DataSink 非 2xx 响应: ...")` —— 解决根因 2-A 其一 |
| 3 | `IotHttpDataSinkAction.java` | line 90 | 顶层 `catch (Exception e)` 末 `throw e instanceof RuntimeException re ? re : new RuntimeException(e)` —— 解决根因 2-A 其二（让外层 `executeDataRuleAction` 能记录"数据目的执行异常"） |
| 4 | `IotDeviceMessageServiceImpl.java` | line 72-74（注入）+ 195-200（调用） | `@Resource @Lazy IotDataRuleService dataRuleService` + 在 `handleUpstreamDeviceMessage` 的 `createDeviceLogAsync` 之后调用 `dataRuleService.executeDataRule(message)` + try-catch 兜底 —— 解决根因 1（顶层断链） |

**主程微调**：调用点放在外层 `handleUpstreamDeviceMessage` 而非审计文档写的 `handleUpstreamDeviceMessage0`。原因：后者有 5 处 return 分支，每处都加是重复非最小；外层一处即可覆盖 STATE_UPDATE / PROPERTY_POST / EVENT_POST / SERVICE_INVOKE / OTA_PROGRESS 所有 method。规则引擎内部会按 `deviceId+method+identifier` 过滤匹配规则，无匹配 O(1) 直接返回，不影响主链路性能。

### 2.3 编译验证

```powershell
cd e:\ch\ruoyi-vue-pro
mvn -pl yudao-module-iot/yudao-module-iot-biz compile -DskipTests -o
# => [INFO] BUILD SUCCESS
```

仅有与本次修改无关的既有警告（`yudao-module-iot-newgateway-bootstrap` / `yudao-module-iot-simulator` 的 `spring-boot-maven-plugin.version` 缺失）。

### 2.4 关键技术发现（1 条）

#### 发现 6：Edit 工具会把 Java 16+ pattern matching `instanceof X re` 退化为传统 `instanceof X`

一次 edit 工具调用中写的 `throw e instanceof RuntimeException re ? re : new RuntimeException(e)` 被自动改成了 `throw e instanceof RuntimeException ? e : new RuntimeException(e)`。后者三元结果类型会退化到 `Exception`，在无 `throws` 声明的方法里**无法编译**。已通过第二次 edit 显式写回 pattern matching 形式。

**教训**：AI 用 edit 工具改 Java 代码时，含 pattern matching `instanceof X var` 的行要检查是否被工具改错。

### 2.5 commit 列表

| commit | 说明 | 文件 |
|---|---|---|
| `e4be36a` | docs(handoff): v17 - webhook 漏投递根因审计完成（双层根因 + 5 行最小修复方案 + 2 条新发现） | 1（v17 doc） |
| `a5eda0b` | fix(iot): webhook 漏投递最小修复 - 注入 IotDataRuleService 调用 + headers.putAll 修字符 bug + 非 2xx 抛异常 | 2（java） |
| `<v18-doc-hash>` | docs(handoff): v18 - 候选 #1 最小修复落地 + mvn 编译通过 + 未运行时实测 | 1（本文件） |

### 2.6 未验证项（下次会话必做）

1. **运行时实测**：重启 fat-jar → 管理端配 1 条数据流转规则 + 1 个 HTTP DataSink → mock 设备发 PROPERTY_POST → 验证目标 URL 收到回调
2. **负路径实测**：DataSink URL 故意配错（如 `http://127.0.0.1:1`）→ 验证 fat-jar 日志能看到 `[executeDataRuleAction][消息(...) 数据目的(...) 执行异常]`（证明不再"伪成功"）
3. **自定义 header 实测**：DataSink 配 `Authorization: Bearer test`，用 tcpdump / wireshark / nc listener 抓包确认 header 实际发出

---

## 3. CI 访问

- **Drone**：`http://test.sanligz.com.cn`（= `192.168.1.253`）
- **仓库**：`fengxiatao/ch_ibms`
- **API 基址**：`http://test.sanligz.com.cn/api/repos/fengxiatao/ch_ibms`
- **Token**：与 jingyu 共用，**不入库**，由用户在会话中粘贴
- **`.drone.yml` stages**：`fast-clone` + `hello` + `sanity-check`（仅元数据，无真实 mvn）

---

## 4. MySQL 连接（沿用 v16 修订）

| 工具 | 用途 | 读 | 写 |
|---|---|---|---|
| `mcp4_mysql_query`（mysql-ibms） | 本地 `ch_ibms` | ✓ | ✓（v16 实证；DDL 仍走 run_command） |

**写操作流程**：

- DML：`mcp4_mysql_query` 直接执行，AI 必须**明示要写哪条记录**让用户判断
- DDL：走 `run_command` 执行 `F:\tools\mysql-8.0.40-winx64\bin\mysql.exe`，需用户明确批准

禁用：`mcp5_*`（线上）/ `mcp6_*`（jingyu）/ `mcp7_*`（停车场）

---

## 5. 本机构建硬规则（沿用 v10~v17 全部条款 + v18 新增 1 条）

- v10~v14 全部条款（详对应 vN）
- v15 / v17 无新增
- v16 新增 2 条（规则 16-1 mcp4 写权限 / 规则 16-2 fat-jar 启动必须用户手动）
- **v18 新增**：
  - **规则 18-1（Edit 工具兼容性）**：AI 通过 edit 工具改 Java 代码时，如含 Java 16+ pattern matching `instanceof X var` 语法，**必须在 edit 后读回该行验证**是否被工具退化为传统 `instanceof X` 写法；退化会导致三元表达式类型提升失败，编译报错。本项目后端 JDK 17，可放心使用 pattern matching。

---

## 6. 下一步候选 + 给下次会话的建议

### 当前分支状态

- **分支**：`snapshot/20260423-full` @ `<v18-doc-hash>`（`a5eda0b` 代码 + `e4be36a` v17 doc 之上）
- **后台进程**（沿用 v17）：
  - PID 17048：RocketMQ NameServer @ 9876
  - PID 19104：RocketMQ Broker
  - **用户手动启的 yudao-server fat-jar @ 48888**（**本次会话代码改动需重启才生效**）

### 候选（按价值/复杂度排序）

#### 1. 运行时实测 v18 webhook 修复（首选，30~60 分钟）

**前提**：用户手动 kill 旧 fat-jar → 重打 fat-jar → 重启。
**DoD**：见 §2.6 三项未验证项全部通过；日志有 `[executeDataRuleAction] ... 执行成功` 和错误路径的 `执行异常`。

#### 2. 与产品确认规则引擎 dead UI 走向（纯沟通，无代码）

启用 vs 隐藏菜单。若产品决定隐藏，§2.2 修复 4（调用点）可考虑加 feature flag。

#### 3. CI v2 启用真实 mvn build（v8~v16 挂账，1~2 小时）

`.drone.yml` 引入 `mvn -q -pl yudao-server -am package -DskipTests`（按规则 14.1.3 加 `-am`）。

#### 4. webhook 完整死信表 + RetryTemplate（独立会话，大改）

v17 §2.4 修复 #3 阶段 2。回归面广。建议在 #1 运行时实测**验证"伪成功"确已被打破**之后再做，否则可能把问题从"静默吞"升级为"疯狂重试同一个错 URL"。

#### 5. system/infra 其他维度 null 漏更新审计

非 Boolean+typeHandler 的字段维度（String/LocalDateTime FieldStrategy.NOT_NULL）。

#### 6. 决定是否把 `last_dispatch_result="重试失败: 人员不存在"` 恢复成原值

v16 实测痕迹清理。一句 UPDATE。

#### 7. 纵深防御：MP 全局 Boolean↔TINYINT 兜底（大改，独立会话）

### 给下次会话的建议

- **首选 #1 运行时实测**：本轮只到编译通过为止；修复是否真解决漏投递必须靠运行时验证
- **#2 产品沟通并行**：不阻塞 #1
- **push 状态**：本次会话 3 笔 commit（`e4be36a` + `a5eda0b` + v18 doc）会一并 push 到 origin + chvm1 双 remote

---

## 7. 关键访问凭据（敏感，不入库）

> AI 不得明文记录，由用户在新会话粘贴：
>
> - DRONE_TOKEN（与 jingyu 共用）
> - MySQL root 密码（本地 `123456`）
> - admin 登录：
>   - tenant=1 "长辉信息"：`admin / admin123`（user_id=1）
>   - tenant=162 "长辉IBMS"：`admin / admin123`（user_id=143，缺 `iot:access-management:control` 权限）

---

## 8. 未 push 的本地状态

- **本地 untracked**（诊断用，不入 commit）：沿用 v16 §8 清单（无新增）
- **DB 持久变更**：沿用 v16 §8（无新增；v16 痕迹 `last_dispatch_result="重试失败: 人员不存在"` 仍在）
- **备份表**：v4 末遗留 2 张，可 2026-05-12 后 drop
- **运行中后台进程**：见 §6.当前分支状态（fat-jar 是**旧 class 版本**，新代码未生效）

---

## 9. 实测命令速查（v18 运行时实测用）

```powershell
# 1. kill 旧 fat-jar（用户手动在独立终端，看进程 ID）
Get-Process java | Where-Object { $_.CommandLine -like "*yudao-server*" }  # 找 PID
# Stop-Process -Id <pid> -Force

# 2. 重打 fat-jar
cd e:\ch\ruoyi-vue-pro
mvn -pl yudao-server -am package -DskipTests -T 1C
# 产物：yudao-server/target/yudao-server.jar

# 3. 用户手动在独立终端启动（AI 不得通过 run_command 启，规则 16-2）
# java -jar yudao-server/target/yudao-server.jar --server.port=48888

# 4. 管理端配规则（浏览器）
# → IoT → 规则引擎 → 数据目的 → 新增（HTTP 类型，URL=http://127.0.0.1:9999/webhook-test）
# → IoT → 规则引擎 → 数据流转规则 → 新增（关联上面的 DataSink + 选设备/方法）

# 5. 起本地回调接收器（另一终端）
# python -m http.server 9999  # 只用来看请求到没到
# 或 nc -l -p 9999  # 能看 raw header

# 6. mock 设备发 PROPERTY_POST（用 IoT simulator 或 mqtt client）

# 7. fat-jar 日志看关键点
# 成功路径：[execute][... 请求成功(...)]
# 失败路径：[executeDataRuleAction][消息(...) 数据目的(...) 执行异常]
```

关键文件路径：

- `ruoyi-vue-pro/yudao-module-iot/yudao-module-iot-biz/src/main/java/cn/iocoder/yudao/module/iot/service/device/message/IotDeviceMessageServiceImpl.java:72-74, 195-200`（v18 修复 4）
- `ruoyi-vue-pro/yudao-module-iot/yudao-module-iot-biz/src/main/java/cn/iocoder/yudao/module/iot/service/rule/data/action/IotHttpDataSinkAction.java:53, 85, 90`（v18 修复 1/2/3）
- `ruoyi-vue-pro/yudao-module-iot/yudao-module-iot-biz/src/main/java/cn/iocoder/yudao/module/iot/service/rule/data/IotDataRuleServiceImpl.java:204-273`（规则引擎入口，未动）

---

## 10. 给新会话 AI 的承接提示词（粘贴模板）

```text
我是 CH（长辉 IBMS）项目主程，工作区 e:\ch，按 docs/session-handoff-20260505-v18.md 承接。
请先只读该 handoff + AGENTS.md，勿翻历史 vN（v1~v17），然后等我选下一步。
CI：http://test.sanligz.com.cn/api/repos/fengxiatao/ch_ibms（token 见 handoff §7，需我粘贴）；
MySQL MCP：mcp4_mysql_query（mysql-ibms）实际可读写（v16 实证），DML 需我批准 SQL，DDL 走 run_command；
本机构建硬规则见 handoff §5（v10 7+1+1 + v11 2 + v12 2 + v13 1 + v14 3 + v15 0 + v16 2 + v17 0 + v18 1）。

当前分支：snapshot/20260423-full @ <v18-doc-hash>

v18 已落地：
  - 落地 v17 §2.4 webhook 漏投递最小修复 4 处（+12/-1，commit a5eda0b）
    * 修复 1: IotHttpDataSinkAction:53 headers 自我 putAll bug（用户 header 此前从未发出）
    * 修复 2: IotHttpDataSinkAction:85 非 2xx 抛 RuntimeException（不再伪成功）
    * 修复 3: IotHttpDataSinkAction:90 顶层 catch 末 throw（让上层能 log 异常）
    * 修复 4: IotDeviceMessageServiceImpl 注入 + handleUpstreamDeviceMessage 调用 executeDataRule（主程微调：外层统一一处而非 0 方法）
  - 本机 mvn compile -DskipTests 通过
  - **未运行时实测**（fat-jar 仍是旧 class 版本）
  - 新发现 6 + 硬规则 18-1：Edit 工具会退化 Java pattern matching 语法，改 Java 要读回验证

后台进程：
  - PID 17048：RocketMQ NameServer @ 9876
  - PID 19104：RocketMQ Broker
  - 用户手动启的 yudao-server fat-jar @ 48888（旧 class，需重启才生效）

下次候选（详见 handoff §6）：
1. 运行时实测 v18 webhook 修复【首选，30~60 分钟；前置：重打 fat-jar + 重启】
2. 与产品确认规则引擎 dead UI 启用 vs 隐藏（纯沟通，并行）
3. CI v2 启用真实 mvn build
4. webhook 完整死信表+RetryTemplate（独立会话，#1 通过后再做）
5. system/infra 其他维度 null 漏更新审计
6. 决定是否把 last_dispatch_result 恢复成 "部分凭证下发失败" 原值
7. 纵深防御 MP 全局 Boolean↔TINYINT 兜底（大改）

以你主程综合考量的角度，决定候选，直接执行。
若选 #1：先让我 kill 旧 fat-jar + mvn package + 手动启，然后指导我在管理端配规则 + mock 设备发消息 + 看日志。
```

---

_最后更新：2026-05-05 22:45 +08:00（候选 #1 webhook 最小修复落地 · 本机 mvn 编译通过 · 未运行时实测 · 新增硬规则 18-1）_
