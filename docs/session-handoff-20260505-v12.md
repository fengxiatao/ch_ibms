# Session Handoff 2026-05-05 v12（候选 ② 彻底归档：H6/H7 access 授权下发 result 残留 + CI 四绿）

> 承接规则：仅读本文件 + `AGENTS.md`，**不要翻历史 vN（含 v1~v11）**（除非用户明确要求）。

---

## 1. 项目骨架

- **后端**：`ruoyi-vue-pro/`（Spring Boot 3，多模块）
  - 关键模块：`yudao-module-iot/yudao-module-iot-biz`、`yudao-module-system`、`yudao-server`
- **管理端**：`yudao-ui-admin-vue3/`（Vue3 + Vite + pnpm）
- **DB**：MySQL 8 @ `127.0.0.1:3306/ch_ibms`（`root/123456`）

---

## 2. 本次会话变更（候选 ② 收尾 · 精筛 + 仅修真 bug）

### 2.1 commit 列表

| commit | 说明 | 文件 | CI |
|---|---|---|---|
| `724a377` | docs(handoff): v11 - 候选 ② null 漏更新批量修复 (H1~H5) + CI 三绿 | 1 文件 232+ | #17 success |
| `3e212c5` | fix(iot): 修复 H6/H7 updateById null 漏更新隐患（access 授权下发 result 残留） | 1 文件 18+/9- | #18 success |

**双 remote 已同步**至 `3e212c5`（`chvm1` + `github`）。

### 2.2 H6/H7：access 授权下发 result 残留（本会话唯一修复）

文件：`yudao-module-iot-biz/service/access/IotAccessAuthDispatchServiceImpl.java`

| ID | 方法 | 行号 | bug | 触发点 |
|---|---|---|---|---|
| H6 | `updatePersonDeviceAuth` | 1726~1751 | `result=null` 时 `setLastDispatchResult(null) + updateById` 被 `FieldStrategy.NOT_NULL` 忽略，前端"授权中/撤销中"状态残留上次失败描述 | line 596 撤销中（`AUTH_STATUS_REVOKING`, null）、line 1049 授权中（`AUTH_STATUS_AUTHORIZING`, null） |
| H7 | `updatePersonDeviceAuthWithHash` | 2039~2073 | 同 H6 | line 416 dispatchCredentialsToDevice 成功/失败/跳过路径 |

**修复模板（沿用 v11 H1~H5）**：

```java
mapper.update(null, Wrappers.<XxxDO>lambdaUpdate()
    .set(XxxDO::getFieldA, valueOrNull)
    .set(XxxDO::getFieldB, null)   // 显式 set null
    .eq(XxxDO::getId, id));
```

H7 特别处理：`credentialHash` 仍保留"非 null 才更新"语义（null 表示本次未重算，应保留旧 hash），用条件 `if (credentialHash != null) lambdaUpdate.set(...)` 实现。

### 2.3 精筛结论（关键技术发现，v12 主干）

v11 §2.4 列出的 6 个候选文件，精筛后**仅 `IotAccessAuthDispatch` 构成 H 系列同款 bug**。其余 5 个文件经 caller/语义分析后确认：

| 文件 | 疑似点 | 精筛结论 |
|---|---|---|
| `video/CameraPresetServiceImpl.updatePreset` | line 79 `BeanUtils.toBean(VO, DO) + updateById` | **VO 全量更新是 ruoyi 项目范式**，VO 字段 null 的清空语义由业务约定，主程不擅改 |
| `video/CameraCruiseServiceImpl.updateCruise/updateCruisePoint` | 同上 line 132/683 | 同上 |
| `video/CameraCruiseServiceImpl` 其余 6 处 | line 233/250/366/376/598/639 | 全是 `setId + setStatus` 状态切换，无 null 字段 |
| `alarm/IotAlarmHostServiceImpl.updateAlarmHost` | line 178 VO→DO | VO 全量更新范式 |
| `alarm/IotAlarmHostServiceImpl.updateHostStatus` | line 453 `setArmStatus(armStatus)` 可能 null | **armStatus=null 保留旧值是刻意设计**（未知状态不覆盖，见 line 683 注释） |
| `alarm/IotAlarmHostServiceImpl.updateHostArmStatusByDeviceId` | line 670 switch | `default` 分支有注释"未知状态不覆盖 systemStatus 避免错误回写" |
| `ota/IotOtaTaskRecordServiceImpl` | line 154 `builder().id().description()` | 局部 builder，无 null 字段 |
| `ota/IotOtaTaskServiceImpl` | line 96 `builder().id().status()` | 同上 |
| `access/IotAccessCardServiceImpl` | — | 未匹配 `setXxx(null)+updateById` 模式 |

### 2.4 新增主程纪律（进入 §5 本机构建硬规则）

- **★ bug 修复不能"为修而修"**：收到"某模式在 N 个文件中存在"的输入时，必须逐文件看 caller 语义；若字段 `null` 是业务刻意保留（"未知状态不覆盖"、"未重算不更新"等），不能套用模板机械修复。H1~H5 能套模板是因为 caller 明确期望"清空"；H6/H7 同理。其余 5 文件的 VO 全量更新和 default 分支"有意保留"均不应动。

### 2.5 验证

- **本地 mvn**：`mvn -pl yudao-module-iot/yudao-module-iot-biz -am compile` = **BUILD SUCCESS**（1453 src 全编）
  - 日志：`.tmp_sql/v11_h6h7_compile3.log`
  - ⚠ **陷阱记录**：`mvn -pl ... clean compile`（无 `-am`）会因 `BaseMapperX`/`BooleanToIntTypeHandler` 等 framework 依赖未 install 而 fail；**必须用 `-am`**（also-make）从源码编译依赖链。v12 主程纪律新增此条。
- **CI**：#15（afe4ac3）→ #16（9f7663c）→ #17（724a377 handoff）→ **#18（3e212c5 H6/H7）全 success**，累计四绿
- **运行验证**：⚠ **未做**。运行中 fat-jar (PID 23924) 仍 v9 8169cc4，未含 H1~H7。下次会话需重打

---

## 3. CI 访问

- **Drone**：`http://test.sanligz.com.cn`（= `192.168.1.253`）
- **仓库**：`fengxiatao/ch_ibms`
- **API 基址**：`http://test.sanligz.com.cn/api/repos/fengxiatao/ch_ibms`
- **Token**：与 jingyu 共用，**不入库**，由用户在会话中粘贴（v12 本会话已验证可用）
- **本会话确认 builds**：
  - #14 `8169cc4`（v9）success
  - #15 `afe4ac3`（H1/H2/H4）success
  - #16 `9f7663c`（空 commit 触发 H3/H5）success
  - #17 `724a377`（v11 handoff）success
  - **#18 `3e212c5`（H6/H7）success**
- **`.drone.yml` stages**：`fast-clone` + `hello` + `sanity-check`（仅元数据，无真实 mvn）；`backend-build` 仍未启用

### 3.1 Drone API 速查（PowerShell 安全写法）

PowerShell 中含 `&` 的 URL 必须用 `.cmd` 批处理包装。本会话沉淀：

- `.tmp_sql/v10_drone_check16.cmd`（v10 沉淀）
- `.tmp_sql/v10_drone_poll.cmd`（v10 沉淀）
- **`.tmp_sql/v11_drone_check17.cmd`（本会话，含 #17/#18 查询，token 内嵌需注意不可 commit）**

---

## 4. MySQL 连接

| 工具 | 用途 | 读 | 写 |
|---|---|---|---|
| `mcp4_mysql_query`（mysql-ibms） | 本地 `ch_ibms` | ✓ | — |

**本会话 DB 零写零读**（H6/H7 是纯代码修复 + CI 验证）。

禁用：`mcp5_*`（线上）/ `mcp6_*`（jingyu）/ `mcp7_*`（停车场）

写操作沿用 v10 §5：优先 `mcp4_mysql_query`，兜底 `cmd /c "F:\tools\mysql-8.0.40-winx64\bin\mysql.exe ..."`。

---

## 5. 本机构建硬规则（v10 7+1+1 + v11 2 条 + v12 新增 2 条）

> 沿用 v10 §5 + v11 §5 全部条款。

### v12 新增

- **★ `mvn -pl <module> compile` 必须加 `-am`**（also-make，编译依赖链）。否则 framework 模块 API 变动后，biz 模块会报 `找不到符号：BaseMapperX/BooleanToIntTypeHandler` 等。增量构建（无 `clean`）有时能蒙过，但不可靠。正确命令：

  ```bash
  cmd /c "mvn -pl yudao-module-iot/yudao-module-iot-biz -am compile -DskipTests > <log> 2>&1 & echo EXIT=%ERRORLEVEL%"
  ```

- **★ bug 修复纪律**：见 §2.4。收到"某模式在 N 个文件中存在"的批量修复任务，必须逐文件看 caller 语义，拒绝机械套模板。

---

## 6. 下一步候选 + 给下次会话的建议

### 当前分支状态

- **分支**：`snapshot/20260423-full` @ `3e212c5`（双 remote 同步）
- **运行中后台进程**：
  - PID 17048：RocketMQ NameServer @ 9876（保留）
  - PID 23924：yudao-server fat-jar @ 48889（**仍是 v9 版本，未含 H1~H7 修复**）
- **DB**：本会话零写，沿用 v10 末状态

### 候选（按价值/复杂度排序）

1. **重打 fat-jar 应用 H1~H7**（前置 of 任何 UI 实测）
   - DoD：kill PID 23924 → `cmd /c "mvn -pl yudao-module-iot/yudao-module-iot-biz,yudao-server -am install -DskipTests -T 1C"` → `Remove-Item target\yudao-server.jar*` → `java -jar` 启 48889 → curl `/admin-api/iot/test...` 健康检查
   - 估时：5~10 分钟

2. **UI 实测 H1~H7（需 #1）**
   - H1：调度任务禁用 → DB 看 `next_execution_time` null
   - H2：楼层 DXF 删除 → DB 看 `floor.dxf_*` 4 字段 null
   - H3/H4：发现设备 ignore（不填天数）→ unignore → DB 看字段
   - H5：升级任务手动重试 → DB 看 `start_time/end_time/error_message`
   - **H6**：授权人员到设备 → 断网模拟下发失败 → 恢复后再发（`AUTH_STATUS_AUTHORIZING, null`）→ DB 看 `ibms_access_person_device_auth.last_dispatch_result` 是否为 null（预期：是）
   - **H7**：同 H6 但走带 hash 的 `dispatchCredentialsToDevice`
   - 估时：每条 5~10 分钟，7 条 ~50 分钟

3. **webhook 漏投递根因排查**（v11 发现，v12 未做）
   - DoD：`https://github.com/fengxiatao/ch_ibms/settings/hooks` 看 drone webhook Recent Deliveries 失败记录；redeliver 或查 drone server 日志
   - 估时：10~20 分钟

4. **CI v2：启用真实 mvn build step**（v8~v12 一直挂账）
   - DoD：`.drone.yml` 取消注释 `backend-build`；maven 国内镜像 + cache 卷 + artifact 上传
   - 估时：1~2 小时
   - **收益大**：CI 绿从"元数据绿"升级到"mvn 编译绿"

5. **其它 null 漏更新领域**（本会话精筛结论：IoT 模块 Service 层已基本清完）
   - 可选扩展：system/infra 模块同类审计（grep `\.set[A-Z]\w*\(null\)|updateById` + caller 分析）
   - 估时：1~2 小时（若存在则再修）

6. **补 v8 候选 ③/④/⑤**（前端 typeCode 28/2 / M0 台账 / 启动顺序硬依赖）
   - 与主线无强依赖，按需挑

### 给下次会话的建议

- **首选 #1+#2**：候选 ② 已彻底归档（H1~H7 代码 + CI 全绿），**下一步应推向生产验证**，避免"代码修了没人验过"的挂账
- **次选 #4（CI v2）**：当 fat-jar 打包工作流稳定后启用，收益最大
- **记住 §2.4 精筛纪律**：下次再遇"批量修 null 漏更新"类任务，先逐文件 caller 语义分析，不要盲扫

---

## 7. 关键访问凭据（敏感）

> 本段需用户在新会话中按需提供，AI 不得在此明文记录：
>
> - DRONE_TOKEN（与 jingyu 共用；本会话 v12 用户已提供并验证可用）
> - MySQL root 密码（本地 `123456`，已在 `application-local.yaml`）
> - admin 登录：`admin / admin123 / 长辉信息`（tenant_id=1）

**注意**：`.tmp_sql/v11_drone_check17.cmd` 内嵌了 token，属于诊断用一次性脚本，**不得 commit**（项目 `.gitignore` 应覆盖 `.tmp_sql/`，v12 已确认不在 commit diff 中）。

---

## 8. 未 push 的本地状态（供新会话继续）

- **分支**：`snapshot/20260423-full` @ `3e212c5`（**双 remote 已同步**，无未 push commit）
- **本地 untracked**（诊断用，不入 commit）：
  - v12 新增：
    - `.tmp_sql/v11_h6h7_compile.log` / `v11_h6h7_compile2.log` / `v11_h6h7_compile3.log`（mvn 编译日志，其中 compile2 为 clean compile 失败日志作为反例）
    - `.tmp_sql/v11_h6h7_commit_msg.txt`（commit 信息原文）
    - `.tmp_sql/v11_drone_check17.cmd`（含 token，一次性）
  - v11 遗留：`.tmp_sql/v10_*.log/cmd/ps1/json`、`docs/session-handoff-20260504-v10.md`、`docs/session-handoff-20260505-v11.md`
- **DB 持久变更**（沿用 v10）：
  - `system_menu.id=5195 status=0`
  - `ibms_energy_meter.id=7 ibms_device_id=10000167`（v9）
  - `ibms_energy_meter.id=1 ibms_device_id=10000166`（v9）
- **备份表**（v4 末遗留，可 2026-05-12 后 drop）：
  - `ibms_device_bak_20260505_legacy`
  - `ibms_channel_bak_20260505_legacy`
- **运行中后台进程**：
  - PID 17048：RocketMQ NameServer（建议保留）
  - PID 23924：yudao-server fat-jar @ 48889（**v9，需重打才含 H1~H7**）

---

_最后更新：2026-05-05 17:40 +08:00（候选 ② 彻底归档 · H6/H7 + CI 四绿 · 精筛主程纪律入账）_
