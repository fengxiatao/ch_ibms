# Session Handoff 2026-05-05 v15（候选 #2 完成 · IoT/infra/system `BooleanToIntTypeHandler` × `LambdaUpdateWrapper.set` 全量审计 · 无新增冲突）

> 承接规则：仅读本文件 + `AGENTS.md`，**不要翻历史 vN（含 v1~v14）**（除非用户明确要求）。

---

## 1. 项目骨架

- **后端**：`ruoyi-vue-pro/`（Spring Boot 3，多模块）
  - 关键模块：`yudao-module-iot/yudao-module-iot-biz`、`yudao-module-system`、`yudao-module-infra`、`yudao-server`
- **管理端**：`yudao-ui-admin-vue3/`（Vue3 + Vite + pnpm）
- **DB**：MySQL 8 @ `127.0.0.1:3306/ch_ibms`（`root/123456`）

---

## 2. 本次会话变更（候选 #2 系统审计，无代码变更）

### 2.1 审计目标

依据 v14 §2.4 发现 1（LambdaUpdateWrapper.set 旁路 DO 上的 typeHandler），系统性扫描以下两者的交集：

- DO 字段标注 `@TableField(typeHandler = BooleanToIntTypeHandler.class)`
- Service / Mapper / Job 层使用 `LambdaUpdateWrapper.set(lambda, value)` 或 `Wrappers.<DO>lambdaUpdate().set(lambda, value)` 写入该字段

交集命中即为 v14 H1 同构隐患点，需按「entity+wrapper 分两步 + `@Transactional`」模式修复。

### 2.2 Boolean + typeHandler 字段全量清单（16 DO / 25 字段）

| DO | 字段 | 所在模块 |
|---|---|---|
| `IbmsDeviceDO` | `subsystemOverride`, `menuOverride` | iot |
| `IbmsDiscoveredDeviceDO` | `added`, `activated` | iot |
| `ScheduledTaskConfigDO` | `enabled`, `alertOnFailure`, `fromProduct` | iot |
| `SubsystemDO` | `enabled` | iot |
| `CameraCruiseDO` | `loopEnabled` | iot |
| `VideoViewDO` | `isDefault` | iot |
| `IotVideoPatrolTaskDO` | `autoSnapshot`, `autoRecording`, `aiAnalysis`, `alertOnAbnormal` | iot |
| `IotVideoPatrolRecordDO` | `isAbnormal`, `handled` | iot |
| `OpcAlarmRecordDO` | `handled` | iot |
| `IotCameraSnapshotDO` | `isProcessed` | iot |
| `IotAlarmRuleDO` | `enableSound`, `enablePopup`, `enableRecord` | iot |
| `IotAlarmZoneDO` | `isImportant`, `is24h` | iot |
| `IotAlarmEventDO` | `isNewEvent`, `isHandled` | iot |
| `PostgresBaseDO` | `deleted`（PG 基类） | iot |
| `FileConfigDO` | `master` | infra |

### 2.3 `.set(DO::getField, ...)` 全量交叉反查（iot/infra/system）

扫描范围：

- `yudao-module-iot/**/service/**`、`**/job/**`、`**/mysql/**`
- `yudao-module-infra/**`、`yudao-module-system/**`

用两类 regex 收斂：

1. `\.set\([A-Za-z_]+DO::get[A-Z]...\)` — 所有 LambdaUpdateWrapper 的 set 调用
2. `Wrappers.<...>lambdaUpdate()` / `Wrappers.lambdaUpdate(...)` — 确保不遗漏 `Wrappers` 风格

**命中的 `.set(...)` 目标字段逐项对照 §2.2 清单**：

| 文件 | 目标字段 | 是否 Boolean+typeHandler |
|---|---|---|
| `IotDeviceChannelServiceImpl#updateDevicePointCount` | `IbmsDeviceDO::getPointCount` | ❌ 非 Boolean |
| `ChanghuiUpgradeServiceImpl#retryTask` | `status/progress/startTime/endTime/errorMessage/retryCount`（`ChanghuiUpgradeTaskDO`） | ❌ 该 DO 无 typeHandler |
| `IbmsEnergyServiceImpl#bind/unbind` | `IbmsEnergyMeterDO::getIbmsDeviceId` | ❌ 非 Boolean |
| `AccessDeviceCapabilityRefreshJob` | `IbmsDeviceDO::getExtra` | ❌ String |
| `IotAccessAuthDispatchServiceImpl`（两处） | `authStatus/lastDispatchTime/lastDispatchResult/credentialHash`（`IotAccessPersonDeviceAuthDO`） | ❌ 均非 Boolean typeHandler |
| `VisitorAppointmentServiceImpl` | `status/approvalComment/approvalTime/approverId/signOutTime`（`VisitorAppointmentDO`） | ❌ 非 Boolean |
| `ScheduledTaskConfigServiceImpl#toggleTask` 步骤 2 | `ScheduledTaskConfigDO::getNextExecutionTime` | ❌ `LocalDateTime`（enabled 已经在步骤 1 走 entity updateById，即 v14 H1 修复） |
| `IbmsChannelServiceImpl`（4 处） | `name/status/currentValue/space/spaceId/extra`（`IbmsChannelDO`） / `pointCount`（`IbmsDeviceDO`） | ❌ 均非 Boolean |
| `FloorDxfServiceImpl#deleteDxf` | `dxfFilePath/FileName/FileSize/UploadTime/Layer0Json/Layer0Svg`（`FloorDO`） | ❌ 均非 Boolean |
| `IbmsDeviceGatewaySupportServiceImpl` | `IbmsDeviceDO::getExtra` | ❌ String |
| `DiscoveredDeviceServiceImpl#ignore/unignore` | `status/ignoredBy/ignoredTime/ignoreReason/ignoreUntil`（`IbmsDiscoveredDeviceDO`） | ❌ **未触及** `added`/`activated`（真正的 Boolean typeHandler 字段） |
| `IotMonitorWallMapper#clearDefault` | `IotMonitorWallDO::getIsDefault` | ❌ **该 DO 未声明 typeHandler**，默认 JDBC `setBoolean` + tinyint 兼容，无 bit(1) 截断风险 |
| `IotOtaTaskRecordMapper` / `IotOtaTaskMapper` / `IotAlertRecordMapper` | 均走 `update(entity, wrapper)` entity 路径，`.set(...)` 未出现 | ✅ 安全（entity 路径 typeHandler 生效） |
| `UserPostMapper#deleteByUserId`（system 唯一命中） | 仅 `.eq(...)` 作为 delete 条件 | ✅ 安全 |

### 2.4 审计结论

- **全量扫描 iot/infra/system 三大模块，v14 H1 `ScheduledTaskConfigDO::getEnabled` 是当前唯一的 `BooleanToIntTypeHandler × LambdaUpdateWrapper.set` 冲突点，已在 v14 commit `d6f6af7` 修复闭环。**
- **本次审计未发现新增隐患，无代码变更。**
- **纵深防御建议**（挂账，非本次实施）：
  - 在 `mybatis-plus` 全局配置增加 `Boolean → TINYINT` 兜底（需全项目回归），可从根源消除风险
  - 或在 `.editorconfig` / Checkstyle 加规则：`.set(*DO::get{BoolField}, *)` 模式审计告警
  - 任何新增 DO Boolean 字段 `@TableField(typeHandler = BooleanToIntTypeHandler)` 时，必须同步检查是否有 service 层 LambdaUpdateWrapper.set 调用，若有则按 v14 H1 分两步 + @Transactional

### 2.5 commit 列表

| commit | 说明 | 文件 | CI |
|---|---|---|---|
| `<v15-doc-hash>` | docs(handoff): v15 BooleanToIntTypeHandler × LambdaUpdateWrapper.set 全量审计无新冲突 | 1 文件（本文件） | 待 push |

**欠账 push**（v14 未闭合，需本会话 push 时一并推）：

- v12 handoff `926c638` → 已推 chvm1，**未推 origin**
- v13 handoff → 已 commit，**未推双 remote**
- v14 代码 `d6f6af7` + v14 doc `341a26e` → **未推双 remote**
- v15 doc → **未 commit**

### 2.6 验证

- 本机：fat-jar PID 26044 @ 48888 仍在运行（含 v14 全部修复），**本次无代码变更无需重启**
- DB：本次会话**未写入任何数据**（纯审计）
- CI：本会话无代码 commit，不触发 build

---

## 3. CI 访问

- **Drone**：`http://test.sanligz.com.cn`（= `192.168.1.253`）
- **仓库**：`fengxiatao/ch_ibms`
- **API 基址**：`http://test.sanligz.com.cn/api/repos/fengxiatao/ch_ibms`
- **Token**：与 jingyu 共用，**不入库**，由用户在会话中粘贴
- **最近 builds**：#15~#18（v12 末四绿）；v14 代码 push 后将触发 #19；v15 纯文档 push 触发 #20
- **`.drone.yml` stages**：`fast-clone` + `hello` + `sanity-check`（仅元数据，无真实 mvn）

---

## 4. MySQL 连接

| 工具 | 用途 | 读 | 写 |
|---|---|---|---|
| `mcp4_mysql_query`（mysql-ibms） | 本地 `ch_ibms` | ✓ | — |

写操作：兜底 `cmd /c "F:\tools\mysql-8.0.40-winx64\bin\mysql.exe ..."`。

禁用：`mcp5_*`（线上）/ `mcp6_*`（jingyu）/ `mcp7_*`（停车场）

---

## 5. 本机构建硬规则（沿用 v10~v14 全部条款，v15 无新增）

- v10 7+1+1：mvn 用 `cmd /c` 包装 / install 优于 package / 等等（详 v10）
- v11 2 条：mvn 编译失败先看依赖链 / 修 null 漏更新用 `LambdaUpdateWrapper.set` 显式传 null（详 v11）
- v12 2 条：`mvn -pl <module>` 必须加 `-am` / bug 修复不能"为修而修"（详 v12）
- v13 1 条：`Start-Process` 启 fat-jar 必须按子进程管理（详 v13）
- v14 3 条：LambdaUpdateWrapper.set 旁路 DO typeHandler / IDE (ecj) 污染 target/classes / fat-jar repackage 强制姿势（详 v14 §2.4，**本 v15 审计进一步验证规则 1 当前仅 H1 案例**）

---

## 6. 下一步候选 + 给下次会话的建议

### 当前分支状态

- **分支**：`snapshot/20260423-full` @ `341a26e`（v14 doc commit hash；v15 doc 本会话需新增 commit）
- **运行中后台进程**：
  - PID 17048：RocketMQ NameServer @ 9876（保留）
  - PID 19104：RocketMQ Broker（保留）
  - PID 1312：Start-Process 父 stub（可忽略）
  - **PID 26044：yudao-server fat-jar @ 48888（含 v14 全部修复）—— UI 续测/复测就绪，无需重启**
- **DB**：本会话无写入（见 §8 继承 v14 末状态）

### 候选（按价值/复杂度排序）

#### 1. H6/H7 严格实测（+10 分钟，v14 未闭合）

**前置**：tenant 162 给 user 143 加 `iot:access-management:control` 权限（或改 `id=10000048` tenant 临时到 1，测完改回）。

DoD：

```sql
SELECT id, auth_status, last_dispatch_result, credential_hash, last_dispatch_time
FROM iot_access_person_device_auth WHERE id=10000048;
```

重试前 `last_dispatch_result="部分凭证下发失败"`，重试后应被覆盖（走成功分支 "重试成功"，或失败分支 "重试失败: xxx"），**不残留**原值。

#### 2. webhook 漏投递根因排查（v11 发现，10~20 分钟）

#### 3. CI v2 启用真实 mvn build（v8~v14 挂账，1~2 小时）

#### 4. system/infra 模块 null 漏更新审计（IoT 已由 v10~v14 + v15 清完）

**注**：本 v15 扫描时已验证 infra 只有 `FileConfigDO.master`（Boolean+typeHandler）一个字段，且**无 `.set(FileConfigDO::getMaster, ...)` 调用**，故 infra 该维度干净。剩余候选为：在 system/infra 中查是否有别的 "updateById + Boolean 字段 null 被 FieldStrategy.NOT_NULL 忽略" 案例（非本次审计维度）。

#### 5. 纵深防御：MP 全局 Boolean↔TINYINT 兜底（1~2 小时 + 回归风险）

从根源移除 `BooleanToIntTypeHandler` 手动声明需求，替换为全局 `GenericTypeHandler` + `Boolean → TINYINT` 自动映射。收益大但回归面广，需独立会话处理。

### 给下次会话的建议

- **首选 #1**：闭合 v14 H6/H7 的实测空洞，最小成本
- **v14 代码 + v14 doc + v15 doc 三个 commit 待 push 双 remote**，本会话末尾必须完成
- **新会话承接 prompt**：见 §10

---

## 7. 关键访问凭据（敏感）

> 本段需用户在新会话中按需提供，AI 不得在此明文记录：
>
> - DRONE_TOKEN（与 jingyu 共用）
> - MySQL root 密码（本地 `123456`，已在 `application-local.yaml`）
> - admin 登录：
>   - tenant=1 "长辉信息"：`admin / admin123`（user_id=1）
>   - tenant=162 "长辉IBMS"：`admin / admin123`（user_id=143，**缺 `iot:access-management:control` 权限**，H6/H7 实测受阻）

---

## 8. 未 push 的本地状态（沿用 v14 §8，本会话无 DB 写入）

- **本地 untracked**（诊断用，不入 commit）：沿用 v14 §8 清单（v14_* 日志、v12_* 日志、admin_token.txt 等）
- **DB 持久变更**（沿用 v14 末状态，本会话无新增）：
  - `iot_scheduled_task_config.id=4` enabled=0, next_execution_time=NULL
  - `floor.id=103,104` 全 dxf 字段 NULL
  - `ibms_discovered_device.id=99` status=1, 4 字段 NULL
  - `changhui_upgrade_task.id=27` status=0, end_time/error_message NULL
  - v9/v10 历史变更：`system_menu.id=5195 status=0`、`ibms_energy_meter.id=7 ibms_device_id=10000167`、`ibms_energy_meter.id=1 ibms_device_id=10000166`
- **备份表**（v4 末遗留，可 2026-05-12 后 drop）：
  - `ibms_device_bak_20260505_legacy`
  - `ibms_channel_bak_20260505_legacy`
- **运行中后台进程**：
  - PID 17048：RocketMQ NameServer
  - PID 19104：RocketMQ Broker
  - PID 1312：Start-Process 父 stub（可忽略）
  - **PID 26044：yudao-server fat-jar @ 48888（含 v14 全部修复，UI 复测/续测目标）**

---

## 9. 审计证据索引（供快速回溯）

以下 grep 查询可精确重现本次审计，后续新增字段或新增 service 层 `.set(...)` 时只需重跑：

```powershell
# 1. 全量 Boolean+typeHandler 字段
grep -r "typeHandler = BooleanToIntTypeHandler" ruoyi-vue-pro/**/*.java
grep -r "BooleanToIntTypeHandler" ruoyi-vue-pro/**/dataobject/**/*.java

# 2. 全量 LambdaUpdateWrapper.set 调用（iot/infra/system）
# regex: \.set\([A-Za-z_]+DO::get[A-Z]
# 扫描 service/job/mysql 目录

# 3. Wrappers.<...>lambdaUpdate() / Wrappers.lambdaUpdate(...) 风格
grep -r "Wrappers.<" ruoyi-vue-pro/**/iot/**/*.java
grep -r "Wrappers.lambdaUpdate" ruoyi-vue-pro/**/*.java

# 4. 交叉比对：第 2/3 条命中的 .set 目标字段 ∩ 第 1 条字段清单
```

**本次结果快照**：

- 16 DO / 25 字段有 Boolean+typeHandler
- 46 条 lambdaUpdate 调用（iot/infra/system 合计），其中 49 条 `.set(DO::getXxx, ...)` 命中
- **交集 = 1**（v14 H1 `ScheduledTaskConfigDO::getEnabled`，已修复）

---

## 10. 给新会话 AI 的承接提示词（粘贴模板）

```text
我是 CH（长辉 IBMS）项目主程，工作区 e:\ch，按 docs/session-handoff-20260505-v15.md 承接。
请先只读该 handoff + AGENTS.md，勿翻历史 vN（v1~v14），然后等我选下一步。
CI：http://test.sanligz.com.cn/api/repos/fengxiatao/ch_ibms（token 见 handoff §7，需我粘贴）；
MySQL MCP 只用 mcp4_mysql_query（mysql-ibms）；本机构建硬规则见 handoff §5（v10 7+1+1 + v11 2 + v12 2 + v13 1 + v14 3；v15 无新增）。

当前分支：snapshot/20260423-full @ <v15-doc-hash>
v15 已落地：
  - 纯文档：BooleanToIntTypeHandler × LambdaUpdateWrapper.set 全量审计（iot/infra/system）
  - 结论：v14 H1 是唯一冲突点（已修复），无新增隐患
  - 16 DO / 25 Boolean+typeHandler 字段清单 + 交叉证据沉淀在 v15 §2~§9
后台进程：
  - PID 17048：RocketMQ NameServer @ 9876
  - PID 19104：RocketMQ Broker
  - PID 1312：Start-Process 父 stub（~7 MB，可忽略）
  - **PID 26044：yudao-server @ 48888（含 v14 全部修复）— 复测就绪，无需重启**

下次候选（详见 handoff §6）：
1. H6/H7 严格实测（补 tenant 162 user 143 权限 或 改 id=10000048 tenant 临时到 1）【首选】
2. webhook 漏投递根因排查
3. CI v2 启用真实 mvn build
4. system/infra 其他维度 null 漏更新审计
5. 纵深防御：MP 全局 Boolean↔TINYINT 兜底（大改，独立会话）

以你主程综合考量的角度，决定候选，直接执行。
```

---

_最后更新：2026-05-05 19:55 +08:00（候选 #2 完成 · BooleanToIntTypeHandler × LambdaUpdateWrapper.set 全量审计 · 无新增冲突 · v14 H1 经确认为唯一案例）_
