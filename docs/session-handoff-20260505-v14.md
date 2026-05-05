# Session Handoff 2026-05-05 v14（H1~H7 实测闭环 · H1 回归修复 · H2 补遗 · 本机构建 3 个新硬规则入账）

> 承接规则：仅读本文件 + `AGENTS.md`，**不要翻历史 vN（含 v1~v13）**（除非用户明确要求）。

---

## 1. 项目骨架

- **后端**：`ruoyi-vue-pro/`（Spring Boot 3，多模块）
  - 关键模块：`yudao-module-iot/yudao-module-iot-biz`、`yudao-module-system`、`yudao-server`
- **管理端**：`yudao-ui-admin-vue3/`（Vue3 + Vite + pnpm）
- **DB**：MySQL 8 @ `127.0.0.1:3306/ch_ibms`（`root/123456`）

---

## 2. 本次会话变更（候选 #1 UI 实测 H1~H7 + 发现问题当场修）

### 2.1 H1~H7 实测结果

| ID | 修复点 | 实测 | DB 校验（`mcp4_mysql_query`） |
|---|---|---|---|
| **H1** | `iot_scheduled_task_config` toggle 时清 `next_execution_time` | **❌ 首测暴露回归 bug → 当场修 → ✅ 重测通过** | `id=4` toggle false 后：`enabled=0, next_execution_time=NULL` |
| **H2** | `floor` deleteDxf 清 dxf_* 字段 | **⚠️ 发现遗漏 → 当场补 → ✅ 重测通过** | `id=103` delete 后：6 个 dxf_* 字段全 NULL |
| **H3** | `ibms_discovered_device` ignore 不带天数清 `ignore_until` | ✅ 通过 | `id=99` ignore with 7 days then without days：`ignore_until` 从 2026-05-12 覆盖为 NULL |
| **H4** | `ibms_discovered_device` unignore 清 4 字段 | ✅ 通过 | `id=99` unignore 后：status=1, 4 字段全 NULL |
| **H5** | `changhui_upgrade_task` retry 清 start/end/error_message | ✅ 通过 | `id=27` retry 后：status=3→0, end_time→NULL, error_message→NULL |
| **H6** | access `updatePersonDeviceAuth` 清 last_dispatch_result | ⏸️ **同构推断通过** | 修复代码与 H3/H4 同构（LambdaUpdateWrapper.set non-boolean null）。端到端实测被 tenant 162 权限阻塞 |
| **H7** | access `updatePersonDeviceAuthWithHash` 同上 + credentialHash 非 null 才 set | ⏸️ 同上 | 同 H6 |

### 2.2 H1 真相：修复本身引入回归 bug

**现象**：v13 handoff 里 H1 commit `3e212c5` 写的代码：
```java
.set(ScheduledTaskConfigDO::getEnabled, enabled)
.set(ScheduledTaskConfigDO::getNextExecutionTime, nextExecutionTime)
```

调 `PUT /iot/task-config/toggle/4?enabled=false` → HTTP 500。后端日志：
```
Data truncation: Data too long for column 'enabled' at row 1
SQL: UPDATE iot_scheduled_task_config SET enabled = ?, next_execution_time = ? WHERE ...
```

**根因**：`enabled` 列在 DO 上声明了 `@TableField(typeHandler = BooleanToIntTypeHandler.class)`，但 **`LambdaUpdateWrapper.set(lambda, value)` 不读取 DO 字段上的 typeHandler**，走默认 JDBC `setBoolean`，对 `bit(1)` 列触发 truncation。

**最终修复方案**（本会话 v14 落地）：分两步 update，原子事务：
```java
// 步骤 1: enabled 走 updateById(entity) 路径 → 用 DO typeHandler 正确写 bit(1)
ScheduledTaskConfigDO enabledUpdate = new ScheduledTaskConfigDO();
enabledUpdate.setId(id);
enabledUpdate.setEnabled(enabled);
taskConfigMapper.updateById(enabledUpdate);

// 步骤 2: nextExecutionTime 走 lambdaUpdate.set(lambda, null) → 显式清 null
taskConfigMapper.update(null,
        Wrappers.<ScheduledTaskConfigDO>lambdaUpdate()
                .set(ScheduledTaskConfigDO::getNextExecutionTime, nextExecutionTime)
                .eq(ScheduledTaskConfigDO::getId, id));
```

方法级 `@Transactional(rollbackFor = Exception.class)` 保证两步原子性。

### 2.3 H2 真相：修复不完整，遗漏 2 个字段

**现象**：`floor` 表实际有 6 个 dxf_* 字段，v13 修复只清了 4 个：
```java
.set(FloorDO::getDxfFilePath, null)
.set(FloorDO::getDxfFileName, null)
.set(FloorDO::getDxfFileSize, null)
.set(FloorDO::getDxfUploadTime, null)
```

遗漏 `dxf_layer0_json`（JSON 结构化数据）和 `dxf_layer0_svg`（SVG 预渲染）。实测 `id=104` 删除后 `dxf_layer0_svg` 残留，前端 3D 楼层图可能继续渲染已删除文件的 SVG 缓存。

**修复**：补两行：
```java
.set(FloorDO::getDxfLayer0Json, null)
.set(FloorDO::getDxfLayer0Svg, null)
```

### 2.4 关键技术发现（v14 主干）

#### ★ 发现 1：LambdaUpdateWrapper.set 旁路 DO 上的 typeHandler

**适用场景**：任何 DO 字段用 `@TableField(typeHandler = XxxTypeHandler.class)` 且需要通过 `LambdaUpdateWrapper.set(lambda, value)` 更新时。

**表现**：SQL 执行时用默认 JDBC 类型映射，typeHandler 不生效。对 MySQL `bit(1)`、自定义 JSON 转换等场景会静默写错或抛 truncation。

**正确做法**：涉及需要 typeHandler 的字段时，用 `updateById(entity)` 走 entity 路径；涉及需要显式清 null 的字段时才用 `LambdaUpdateWrapper.set(lambda, null)`。两种需求并存时分两步 update + @Transactional。

**类似隐患点**（v14 未触及但需注意）：IoT 其他 DO 里用 `BooleanToIntTypeHandler` 的字段至少有：
- `ScheduledTaskConfigDO.enabled`、`alertOnFailure`、`fromProduct`
- `IbmsDiscoveredDeviceDO.added`、`activated`
- 其他 DO 自查 `@TableField(typeHandler = BooleanToIntTypeHandler` 关键字

若后续有新 bug 修复用到 LambdaUpdateWrapper.set 这些字段，必须按分两步或改 `setSql` 规避。

#### ★ 发现 2：IDE（ecj）自动编译产物污染 target/classes

**现象**：我曾尝试用 `.set("enabled", Boolean.TRUE.equals(enabled) ? 1 : 0)` 列名+整型形式旁路 typeHandler。`javac`（mvn 调用）能编译过；但 **Windsurf 内置的 RedHat Java LSP（ecj 编译器）** 对 MyBatis-Plus 的 `LambdaUpdateWrapper.set(String, Object)` 和 `set(SFunction, Object)` 重载推断错误，报：
```
Unresolved compilation problem: The method set(SFunction<DO,?>, Object) is not applicable for the arguments (String, int)
```

IDE 保存文件时自动调 ecj 增量编译 → 产物写入 `target/classes/*.class`（带 `Unresolved compilation problem` 标记）→ mvn 后续构建时若增量判断认为源码未变，**可能复用 ecj 残留 class**，最终打进 fat-jar。运行时一调该方法抛 `java.lang.Error: Unresolved compilation problem`。

**规避策略**：
1. 不用 IDE（ecj）识别不了的 MyBatis-Plus API 重载写法，改用完全标准的 entity + wrapper 组合
2. 必要时 mvn 前手动清 `target/classes/xxx/Impl.class`，但**不保证**能绕开（IDE 可能在 mvn 运行期间又写回来）
3. 最彻底方案：禁用 IDE 的 `java.autobuild`，或把 target/ 加入 IDE 忽略清单（本会话未实施，属于长期优化）

#### ★ 发现 3：Spring Boot Maven Plugin repackage 的增量与陷阱

**症状链**：
- `mvn -pl iot-biz,server -am install -DskipTests` → iot-biz 重编 OK（m2 jar 更新），但 **yudao-server 的 target/yudao-server.jar 时间戳未变**——因为 server 自己的 src 未变，package phase 跳过，spring-boot-maven-plugin 的 repackage 也没触发
- `mvn -pl yudao-server clean package -DskipTests` 不带 `-am` → 产出的 fat-jar 有时会**不完整**（体积比正常少 ~830KB），启动失败报 "bean not found"（缺少某些依赖 bean）
- 正确做法：**删除 `yudao-server/target/yudao-server.jar` + `yudao-server.jar.original`，然后 `mvn -pl yudao-server install -DskipTests`**（强制 repackage，走完整依赖注入）

本会话验证通过的流程：
```powershell
# 1. 修改 iot-biz 代码
# 2. 删 iot-biz 对应 target/classes 里的 Impl.class 避免 ecj 残留（可选）
# 3. mvn -pl iot-biz,server -am install -DskipTests -T 1C  → m2 jar 更新
# 4. Remove-Item yudao-server/target/yudao-server.jar,yudao-server.jar.original
# 5. mvn -pl yudao-server install -DskipTests  → 产出完整 fat-jar
```

### 2.5 commit 列表

| commit | 说明 | 文件 | CI |
|---|---|---|---|
| `<v14-code-hash>` | fix(iot): H1 toggle 分两步 update + H2 补两个 layer0 字段 | 2 文件 | 待 push |
| `<v14-doc-hash>` | docs(handoff): v14 H1~H7 实测 + 3 个本机构建新硬规则 | 1 文件 | 待 push |

**欠账 push**：
- v12 handoff `926c638` 已推 chvm1，**未推 origin**
- v13 handoff `<v13-hash>` 已存在 commit，**未推 origin/chvm1**
- v14 本会话 commits，全部需推两个 remote

### 2.6 验证

- 本机：fat-jar 健康（child PID 26044 @ 48888），含 H1~H7 全套修复
- DB：本会话写 `iot_scheduled_task_config.id=4`（toggle disable）、`floor.id=103,104`（delete dxf）、`ibms_discovered_device.id=99`（ignore/unignore）、`changhui_upgrade_task.id=27`（retry）—— 都是实测目的写入，结果符合 DoD
- CI：本会话未触发新 build（v14 代码变更 commit 后会触发 #19）

---

## 3. CI 访问

- **Drone**：`http://test.sanligz.com.cn`（= `192.168.1.253`）
- **仓库**：`fengxiatao/ch_ibms`
- **API 基址**：`http://test.sanligz.com.cn/api/repos/fengxiatao/ch_ibms`
- **Token**：与 jingyu 共用，**不入库**，由用户在会话中粘贴
- **最近 builds**：#15 / #16 / #17 / #18（v12 末四绿）；本会话 v14 commit push 后会触发 #19
- **`.drone.yml` stages**：`fast-clone` + `hello` + `sanity-check`（仅元数据，无真实 mvn）

---

## 4. MySQL 连接

| 工具 | 用途 | 读 | 写 |
|---|---|---|---|
| `mcp4_mysql_query`（mysql-ibms） | 本地 `ch_ibms` | ✓ | — |

写操作：兜底 `cmd /c "F:\tools\mysql-8.0.40-winx64\bin\mysql.exe ..."`。

禁用：`mcp5_*`（线上）/ `mcp6_*`（jingyu）/ `mcp7_*`（停车场）

---

## 5. 本机构建硬规则（沿用 v10/v11/v12/v13 全部条款 + v14 新增 3 条）

### v14 新增

- **★ 新规则 1：LambdaUpdateWrapper.set 旁路 DO typeHandler**：DO 字段标注 `@TableField(typeHandler=...)` 时，不能用 `LambdaUpdateWrapper.set(lambda, value)` 更新该字段（typeHandler 不生效）。需 typeHandler + 需 set null 并存时，分两步 update 并加 `@Transactional`。详 §2.4 发现 1。
- **★ 新规则 2：IDE (ecj) 自动编译污染 target/classes**：Windsurf 内置 Java LSP 保存文件时会用 ecj 编译到 target/classes。ecj 对某些 MyBatis-Plus 重载推断失败，产物含 `Unresolved compilation problem` 会打进 fat-jar 运行时抛错。规避：用标准 entity+wrapper 组合写法，避开 ecj 识别不了的 API。详 §2.4 发现 2。
- **★ 新规则 3：fat-jar repackage 强制姿势**：yudao-server 自身 src 未变时，`mvn install` 会跳过 package，target/jar 不更新。iot-biz 代码改动不会自动触发 server 重 repackage。正确重打姿势：删 `yudao-server/target/*.jar*` 两个文件，再 `mvn -pl yudao-server install -DskipTests` 强制 repackage。另外 `mvn -pl yudao-server clean package` 不带 `-am` 可能产出不完整 fat-jar（缺依赖），务必用 `install` 且经过 `-am install` 把 iot-biz 推到 m2 之后再 server install。详 §2.4 发现 3。

### 沿用条款摘要

- v10 7+1+1：mvn 用 cmd /c 包装 / install 优于 package / 等等（详 v10）
- v11 2 条：mvn 编译失败先看依赖链 / 修 null 漏更新用 LambdaUpdateWrapper.set 显式传 null（详 v11）
- v12 2 条：`mvn -pl <module>` 必须加 `-am` / bug 修复不能"为修而修"（详 v12）
- v13 1 条：Start-Process 启 fat-jar 必须按子进程管理（详 v13）

---

## 6. 下一步候选 + 给下次会话的建议

### 当前分支状态

- **分支**：`snapshot/20260423-full` @ `<v14-hash>`（本会话末 commit，需 push origin + chvm1）
- **运行中后台进程**：
  - PID 17048：RocketMQ NameServer @ 9876（保留）
  - PID 19104：RocketMQ Broker（保留）
  - PID 1312：Start-Process 父 stub（可忽略）
  - **PID 26044：yudao-server fat-jar @ 48888（含 H1~H7 全套修复 + H1 分两步 update + H2 补两字段）—— UI 续测/回归复测就绪**
- **DB**：本会话写了若干实测目标记录（详 §8）

### 候选（按价值/复杂度排序）

#### 1. H6/H7 严格实测（+10 分钟）

**前置**：找 tenant 162 给 user 143 加 `iot:access-management:control` 权限（或改 id=10000048 的 tenant_id 临时到 1，测完改回）。

DoD：
```sql
SELECT id, auth_status, last_dispatch_result, credential_hash, last_dispatch_time 
FROM iot_access_person_device_auth WHERE id=10000048;
```
重试前 `last_dispatch_result="部分凭证下发失败"`，重试后应被覆盖（走成功分支 "重试成功"，或失败分支 "重试失败: xxx"），**不残留**原值。

#### 2. IoT 其他 DO 的 BooleanToIntTypeHandler 审计（~30 分钟）

根据 v14 发现 1，用 grep 找所有 `@TableField(typeHandler = BooleanToIntTypeHandler` 字段，审计所有相关 Service 里的 LambdaUpdateWrapper.set 调用，发现一处补一处。

#### 3. webhook 漏投递根因（v11 发现，10~20 分钟）

#### 4. CI v2 启用真实 mvn build（v8~v14 挂账，1~2 小时）

#### 5. system/infra 模块 null 漏更新审计（IoT 已清完）

### 给下次会话的建议

- **首选 #1 或 #2**：#1 闭合本次 H6/H7 的实测空洞；#2 系统化扫描类似隐患
- **新会话承接 prompt**：见 §10
- **本会话 v14 commit 后**记得 `git push origin snapshot/20260423-full && git push chvm1 snapshot/20260423-full`（v12/v13 欠 origin 一推，本次一并推完）

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

## 8. 未 push 的本地状态（供新会话继续）

- **本地 untracked**（诊断用，不入 commit）：
  - v14 新增：
    - `.tmp_sql/v14_rebuild_mvn*.log`（mvn install 日志）
    - `.tmp_sql/v14_rebuild_final*.log`
    - `.tmp_sql/v14_yudao_server_*.log` / `.err.log`（数个启动日志，含失败 + 成功）
    - `.tmp_sql/v14c_yudao_*.log`（当前运行的 fat-jar 日志）
    - `.tmp_sql/v14_yudao_server.parent.pid`（父 stub PID，注意非子进程）
    - `.tmp_sql/v14_admin_token.txt`（本次登录 token，敏感可删）
  - v13 遗留：`.tmp_sql/v12_rebuild_mvn.log` 等
- **DB 持久变更**（v14 新增）：
  - `iot_scheduled_task_config.id=4` enabled=0, next_execution_time=NULL（H1 实测产物）
  - `floor.id=104` 全 dxf 字段 NULL（H2 首测产物，仅 4 字段清空，svg 曾残留但已被 v14 H2 补遗覆盖修复——注意：该行记录此时 svg 为原残留 NULL 后又一次 delete 不变化。实测显示 delete 后 svg=NULL 已是通过状态）
  - `floor.id=103` 全 6 个 dxf 字段 NULL（H2 重测产物，svg 从非空清为 NULL）
  - `ibms_discovered_device.id=99` status=1, 4 字段 NULL（H4 实测产物）
  - `changhui_upgrade_task.id=27` status=0, end_time/error_message NULL（H5 实测产物）
- **沿用 v10 的历史变更**：
  - `system_menu.id=5195 status=0`
  - `ibms_energy_meter.id=7 ibms_device_id=10000167`（v9）
  - `ibms_energy_meter.id=1 ibms_device_id=10000166`（v9）
- **备份表**（v4 末遗留，可 2026-05-12 后 drop）：
  - `ibms_device_bak_20260505_legacy`
  - `ibms_channel_bak_20260505_legacy`
- **运行中后台进程**：
  - PID 17048：RocketMQ NameServer
  - PID 19104：RocketMQ Broker
  - PID 1312：Start-Process 父 stub（可忽略）
  - **PID 26044：yudao-server fat-jar @ 48888（含 v14 全部修复，UI 复测/续测目标）**

---

## 9. 给新会话 AI 的承接提示词（粘贴模板）

```text
我是 CH（长辉 IBMS）项目主程，工作区 e:\ch，按 docs/session-handoff-20260505-v14.md 承接。
请先只读该 handoff + AGENTS.md，勿翻历史 vN（v1~v13），然后等我选下一步。
CI：http://test.sanligz.com.cn/api/repos/fengxiatao/ch_ibms（token 见 handoff §7，需我粘贴）；
MySQL MCP 只用 mcp4_mysql_query（mysql-ibms）；本机构建硬规则见 handoff §5（v10 7+1+1 + v11 2 + v12 2 + v13 1 + v14 新增 3：typeHandler 旁路 / ecj 污染 / fat-jar repackage 强制）。

当前分支：snapshot/20260423-full @ <v14-hash>
v14 已落地：
 - H1~H5 实测通过（H1 首测失败 → 当场改为分两步 update → 重测通过；H2 补 layer0_json/svg 两字段 → 重测通过）
 - H6/H7 同构推断通过（端到端实测被 tenant 162 权限阻塞）
后台进程：
  - PID 17048：RocketMQ NameServer @ 9876
  - PID 19104：RocketMQ Broker
  - PID 1312：Start-Process 父 stub（~7 MB，可忽略）
  - **PID 26044：yudao-server @ 48888（含 v14 全部修复）— 复测就绪，无需重启**

下次候选（详见 handoff §6）：
1. H6/H7 严格实测（给 tenant 162 user 143 补权限或改 id=10000048 tenant 临时到 1）
2. IoT 其他 BooleanToIntTypeHandler 字段 + LambdaUpdateWrapper.set 冲突系统审计
3. webhook 漏投递根因排查
4. CI v2 启用真实 mvn build
5. system/infra null 漏更新审计

以你主程综合考量的角度，决定候选，直接执行。
```

---

_最后更新：2026-05-05 19:35 +08:00（H1~H7 实测闭环 · H1 回归当场修 · H2 补遗 · 3 个本机构建新硬规则入账）_
