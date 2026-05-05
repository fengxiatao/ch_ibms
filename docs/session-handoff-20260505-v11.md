# Session Handoff 2026-05-05 v11（候选 ② 全闭环：H1~H5 null 漏更新批量修复 + CI 三绿）

> 承接规则：仅读本文件 + `AGENTS.md`，**不要翻历史 vN（含 v1~v10）**（除非用户明确要求）。

---

## 1. 项目骨架

- **后端**：`ruoyi-vue-pro/`（Spring Boot 3，多模块）
  - 关键模块：`yudao-module-iot/yudao-module-iot-biz`、`yudao-module-system`、`yudao-server`
- **管理端**：`yudao-ui-admin-vue3/`（Vue3 + Vite + pnpm）
- **DB**：MySQL 8 @ `127.0.0.1:3306/ch_ibms`（`root/123456`）

---

## 2. 本次会话变更（候选 ② 收尾：null 漏更新批量修复）

### 2.1 commit 列表

| commit | 说明 | 文件 | CI |
|---|---|---|---|
| `afe4ac3` | fix(iot): 修复 3 处 updateById null 漏更新隐患（H1/H2/H4） | 3 文件 32+/30- | #15 success |
| `23fb5a5` | fix(iot): 修复 H3/H5 updateById null 漏更新隐患（续 afe4ac3） | 2 文件 31+/24- | webhook 漏 |
| `9f7663c` | ci: trigger CI for H3/H5 fix (webhook missed 23fb5a5) | 空 commit | #16 success |

**双 remote 已同步**至 `9f7663c`（`origin` = chvm1+github 双 push URL）。

### 2.2 修复的 5 处隐患（全在 `yudao-module-iot/yudao-module-iot-biz`）

模式：`setXxx(null) + updateById(do)` → MyBatis-Plus 默认 `FieldStrategy.NOT_NULL` 把 null 字段忽略，导致 UPDATE SQL 中没出现该列，业务"清空字段"语义失效。

修复模板（参照 v9 `8169cc4` `IbmsEnergyServiceImpl.bindDevice`）：

```java
mapper.update(null,
    Wrappers.<XxxDO>lambdaUpdate()
        .set(XxxDO::getFieldA, valueOrNull)
        .set(XxxDO::getFieldB, null)  // 显式 set null
        .eq(XxxDO::getId, id));
```

| ID | 文件 | 函数 | bug |
|---|---|---|---|
| H1 | `service/task/ScheduledTaskConfigServiceImpl.java` | `toggleTask` | 禁用任务时 `nextExecutionTime=null` 漏更新，残留旧执行时间 |
| H2 | `service/gis/FloorDxfServiceImpl.java` | `deleteDxfForFloor` | 删 DXF 时 4 字段（`dxfFilePath/Name/Size/UploadTime`）null 漏更新 |
| H3 | `service/device/discovery/DiscoveredDeviceServiceImpl.java` | `ignoreDevice` | 永久忽略（ignoreDays=null/0）时 `ignoreUntil=null` 漏更新 |
| H4 | 同上 | `unignoreDevice` | 取消忽略时 4 字段（`ignoredBy/Time/Reason/Until`）null 漏更新 |
| H5 | `service/changhui/upgrade/ChanghuiUpgradeServiceImpl.java` | `retryUpgradeTask` | 重试时 `startTime/endTime/errorMessage=null` 漏更新 |

### 2.3 验证

- **本地 mvn**：`mvn -pl yudao-module-iot/yudao-module-iot-biz compile` = `BUILD SUCCESS`（H1/H2/H4 一次、H3/H5 一次）
- **CI**：#14（v9 8169cc4）→ #15（afe4ac3）→ #16（9f7663c）连续三绿
- **运行验证**：⚠ **未做**。运行中的 fat-jar (PID 23924) 仍是 v9 8169cc4 的版本，未含 H1~H5 修复。下次会话需重打 jar 重启才能 UI 实测

### 2.4 关键发现

#### 发现 1：webhook 偶发漏投递

`23fb5a5` push 后 5 分钟内 Drone 未注册新 build（`/builds/16` 返回 `sql: no rows in result set`，分支页 HEAD 仍显示 `afe4ac3`）。

**根因猜测**：github webhook 投递抖动，drone server 未收到 push 事件。

**绕过方案**（已用，验证有效）：

```bash
git commit --allow-empty -m "ci: trigger CI for ... (webhook missed <sha>)"
git push origin <branch>
```

新 sha 触发新 webhook，drone 这次正常接收。

**根本修复**（未做，待下次会话）：在 `https://github.com/fengxiatao/ch_ibms/settings/hooks` 找到 drone webhook，看 Recent Deliveries 失败投递，做 redelivery；或检查 drone server `/var/log/drone-server.log`。

#### 发现 2：MyBatis-Plus FieldStrategy 是项目级隐患

本次仅修了 IoT 模块 5 处。`grep -rn "setId(null)\|setNull\|set.*\(null\)" yudao-module-iot/yudao-module-iot-biz` 后，仍存在以下中危文件需精筛：

- `service/changhui/CameraPresetServiceImpl.java` / `CameraCruiseServiceImpl.java`
- `service/access/IotAccessAuthDispatchServiceImpl.java`
- `service/changhui/alarm/IotAlarmHostServiceImpl.java`
- `service/changhui/ota/IotOtaTaskServiceImpl.java` / `IotOtaTaskDeviceServiceImpl.java`
- `service/access/AccessCardServiceImpl.java`

这些文件中的 `setXxx(null)` 大多是 `setId(null)`（手动让 MyBatis-Plus 重生成），但夹杂少量真业务字段 null。**下次会话前 30 分钟内可逐文件精筛 + 一次性 commit**。

---

## 3. CI 访问

- **Drone**：`http://test.sanligz.com.cn`（= `192.168.1.253`）
- **仓库**：`fengxiatao/ch_ibms`
- **API 基址**：`http://test.sanligz.com.cn/api/repos/fengxiatao/ch_ibms`
- **Token**：与 jingyu 共用，**不入库**，环境变量 `DRONE_TOKEN` 或用户手工粘贴
- **本会话已确认**：
  - Build #14 `8169cc4` (v9 fix energy bind)：success
  - Build #15 `afe4ac3` (H1/H2/H4)：success
  - Build #16 `9f7663c` (空 commit 触发 H3/H5 工作树)：success
- **`.drone.yml` 当前 stages**：fast-clone（从 chvm1 mirror）+ hello + sanity-check（仅元数据，无真实 mvn build）；TODO v2 启用 `backend-build` 时才会跑真实 maven

### 3.1 Drone API 速查

```bash
# 列最近 builds
curl -s -H "Authorization: Bearer $DRONE_TOKEN" \
     "http://test.sanligz.com.cn/api/repos/fengxiatao/ch_ibms/builds"

# 查特定 build
curl -s -H "Authorization: Bearer $DRONE_TOKEN" \
     "http://test.sanligz.com.cn/api/repos/fengxiatao/ch_ibms/builds/<NUM>"
```

PowerShell 转义注意：URL 含 `&` 必须放进 `.cmd` 批处理文件或用 `^&` 转义。本会话写了 `.tmp_sql/v10_drone_poll.cmd` 模板可复用。

---

## 4. MySQL 连接

| 工具 | 用途 | 读 | 写 |
|---|---|---|---|
| `mcp4_mysql_query`（mysql-ibms） | 本地 `ch_ibms` | ✓ | **v10 实测可用，本会话零写未复测** |

**本会话 DB 零写**（候选 ② 全是代码改动 + CI 验证）。`ibms_energy_meter` / `system_menu` 等表保持 v10 末状态。

禁用：`mcp5_*`（线上）/ `mcp6_*`（jingyu）/ `mcp7_*`（停车场）

写操作仍以 v10 §5 修订为准：**优先 `mcp4_mysql_query`**，兜底 `cmd /c "F:\tools\mysql-8.0.40-winx64\bin\mysql.exe ..."`。

---

## 5. 本机构建硬规则（v10 7+1+1 条 + v11 新增 1 条）

> 沿用 v10 §5 全部条款（mvn cwd、fat-jar 启动、中文 commit、`cmd /c` 包装 mvn、PowerShell mysql 占位文件冲突等）。

### v11 新增

- **★ Webhook 漏投递绕过**：当 `git push` 后 3 分钟 Drone 仍未注册新 build（`/builds/<N>` 返回 `sql: no rows in result set`）时，**不要无限等**：

  ```bash
  git commit --allow-empty -m "ci: trigger CI for <reason> (webhook missed <sha>)"
  git push origin <branch>
  ```

  这条 noop commit 不污染历史（可被 reword/squash 进真 fix），但能可靠触发 webhook。

- **★ PowerShell 的 `&` 字符**：URL/命令中含 `&` 必须用 `.cmd` 批处理文件，不要尝试在 PS 命令行内 inline。本会话已沉淀 `.tmp_sql/v10_drone_poll.cmd` / `v10_drone_check16.cmd` / `v10_check.cmd` 模板。

---

## 6. 下一步候选 + 给下次会话的建议

### 当前分支状态

- **分支**：`snapshot/20260423-full` @ `9f7663c`（双 remote 同步）
- **运行中后台进程**：
  - PID 17048：RocketMQ NameServer @ 9876（保留）
  - PID 23924：yudao-server fat-jar @ 48889（**仍是 v9 版本，未含 H1~H5 修复**）
- **DB**：本会话零写，沿用 v10 末状态

### 候选（按价值/复杂度排序）

1. **重打 fat-jar 应用 H1~H5**（前置 of 任何 UI 实测）
   - DoD：kill PID 23924 → `mvn -pl yudao-module-iot/yudao-module-iot-biz,yudao-server -am install -DskipTests -T 1C` → `Remove-Item target\yudao-server.jar*` → `java -jar` 启 48889 → curl `/admin-api/iot/test...` 健康检查
   - 估时：5~10 分钟（mvn install 含 RocketMQ 依赖编译，有过长记录）

2. **修剩余中危 null 漏更新（6 文件，同 H1~H5 模式）**
   - DoD：精筛 `service/changhui/CameraPreset|CameraCruise|alarm/IotAlarmHost|ota/IotOtaTask*|access/IotAccessAuthDispatch|AccessCard` 中的真业务 null（排除 `setId(null)` 主键重生成）→ 一次性 commit + push + CI 验
   - 估时：30 分钟

3. **UI 实测 H1~H5（需 #1 完成）**
   - H1：调度任务页禁用一个任务 → DB 看 `next_execution_time` 是否真的 null
   - H2：楼层 DXF 上传后删除 → DB 看 `floor.dxf_*` 4 字段是否真的 null
   - H3/H4：发现设备页 ignore（不填天数）→ unignore → DB 看字段
   - H5：升级任务失败/手动重试 → DB 看 `start_time/end_time/error_message`
   - 估时：每条 ~5 分钟，5 条 ~30 分钟

4. **webhook 漏投递根因排查**（v11 新发现）
   - DoD：访问 `https://github.com/fengxiatao/ch_ibms/settings/hooks` 看 drone webhook Recent Deliveries 是否有失败投递；如有，redeliver；如无，查 drone server 日志
   - 估时：10~20 分钟（取决于权限）

5. **CI v2：启用真实 mvn build step**（v8/v9/v10 一直挂账）
   - DoD：`.drone.yml` 取消注释 `backend-build` 步骤；首次构建需准备 maven 镜像/缓存目录；目标：commit → push → CI 实跑 mvn package → 生成 fat-jar artifact
   - 估时：1~2 小时（含调试 maven 国内镜像、cache 卷、artifact 上传策略）

6. **补 v8 候选 ③/④/⑤（前端 typeCode 28/2 数字溯源 / M0 台账 / 启动顺序硬依赖）**
   - 与本主线无强依赖，按需挑

### 给下次会话的建议

- **首选 #2**：剩余 6 文件 null 漏更新一次清完，候选 ② 可彻底归档
- **首选 #1+#3**：如果产品/客户在意"修复落地有效"，立刻重启 jar + UI 实测 5 处
- **不要重新探索**：
  - H1~H5 模式已固化，下次类似 bug 直接套 `Wrappers.lambdaUpdate().set(field, null).eq(id, val)` 写法
  - webhook 漏投递有空 commit 兜底方案，别绠结
  - CI 当前只跑 fast-clone+sanity-check，**绿灯不等于 mvn 编译过**——必须本地 `mvn compile` 验证

---

## 7. 关键访问凭据（敏感）

> 本段需用户在新会话中按需提供，AI 不得在此明文记录：
>
> - DRONE_TOKEN（与 jingyu 共用）
> - MySQL root 密码（本地 `123456`，已在 `application-local.yaml` 内）
> - admin 登录：`admin / admin123 / 长辉信息`（tenant_id=1）

---

## 8. 未 push 的本地状态（供新会话继续）

- **分支**：`snapshot/20260423-full` @ `9f7663c`（**双 remote 已同步**，无未 push commit）
- **本地 untracked**（诊断用，不入 commit）：
  - `.tmp_sql/v10_audit_report.md`（本会话 ② 候选审计报告）
  - `.tmp_sql/v10_h1h2h4_compile*.log` / `v10_h3h5_compile.log`（mvn 编译日志）
  - `.tmp_sql/v10_h1h2h4_commit_msg.txt` / `v10_h3h5_commit_msg.txt`（commit 信息原文）
  - `.tmp_sql/v10_drone_*.json` / `v10_drone_*.cmd` / `v10_drone_*.ps1`（CI 查询脚本沉淀）
  - `.tmp_sql/v10_check.cmd` / `v10_check2.cmd`（编译日志检查脚本）
  - v10 遗留：`.tmp_sql/v10_vite*.log`、`docs/session-handoff-20260504-v10.md`
- **DB 持久变更**（沿用 v10）：
  - `system_menu.id=5195 status=0`（保留，修复前端路由 404）
  - `ibms_energy_meter.id=7 ibms_device_id=10000167`（v9 基线）
  - `ibms_energy_meter.id=1 ibms_device_id=10000166`（v9 基线）
- **备份表**（v4 末遗留，可在 2026-05-12 后 drop）：
  - `ibms_device_bak_20260505_legacy`
  - `ibms_channel_bak_20260505_legacy`
- **运行中后台进程**（接管时按需保留/kill）：
  - PID 17048：RocketMQ NameServer（建议保留）
  - PID 23924：yudao-server fat-jar @ 48889（**v9 版本，需重打才含 H1~H5**）

---

_最后更新：2026-05-05 17:10 +08:00（候选 ② null 漏更新批量修复 + CI 三绿版）_
