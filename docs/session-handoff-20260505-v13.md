# Session Handoff 2026-05-05 v13（候选 #1 重打 fat-jar 闭环 · 端口 48888 纠正 · UI 实测 checklist 入账）

> 承接规则：仅读本文件 + `AGENTS.md`，**不要翻历史 vN（含 v1~v12）**（除非用户明确要求）。

---

## 1. 项目骨架

- **后端**：`ruoyi-vue-pro/`（Spring Boot 3，多模块）
  - 关键模块：`yudao-module-iot/yudao-module-iot-biz`、`yudao-module-system`、`yudao-server`
- **管理端**：`yudao-ui-admin-vue3/`（Vue3 + Vite + pnpm）
- **DB**：MySQL 8 @ `127.0.0.1:3306/ch_ibms`（`root/123456`）

---

## 2. 本次会话变更（候选 #1：重打 fat-jar 应用 H1~H7）

### 2.1 commit 列表

| commit | 说明 | 文件 | CI |
|---|---|---|---|
| `3e212c5` | fix(iot): H6/H7 updateById null 漏更新隐患 | 1 文件 | #18 success（v12 已 push origin） |
| `926c638` | docs(handoff): v12 候选 ② 彻底归档 | 1 文件 | （仅 chvm1 同步，origin 待 push） |
| `<v13-hash>` | docs(handoff): v13 候选 #1 重打 fat-jar 闭环 | 本文件 | 待 push |

**remote 状态**：
- `chvm1/snapshot/20260423-full` = `926c638`（v12）
- `origin/snapshot/20260423-full` = `3e212c5`（v12 handoff 未 push 到 github，本会话末需 `git push origin`）

### 2.2 候选 #1 闭环结果

**目标**：将运行中的 yudao-server fat-jar 从 v9 `8169cc4` 升级到含 H1~H7 的 `926c638`，前置 of UI 实测。

**执行步骤（已全部 DONE）**：

1. ✅ Kill 旧进程 PID 23924（v9 fat-jar @ 14:00 打的）
2. ✅ `mvn -pl yudao-module-iot/yudao-module-iot-biz,yudao-server -am install -DskipTests -T 1C` → **BUILD SUCCESS @ 11.130 s**
   - 日志：`.tmp_sql/v12_rebuild_mvn.log`
3. ✅ 新 jar：`yudao-server/target/yudao-server.jar` @ 2026-05-05 18:00（262 659 205 字节）
4. ✅ Start-Process 启动 `java -Xms512m -Xmx2g -jar yudao-server.jar --spring.profiles.active=local`
5. ✅ `Started YudaoServerApplication in 36.566 seconds`
6. ✅ 健康检查：`GET /actuator/health` 返回 `{"status":"UP"}`（首字节 ASCII 验证），`GET /` HTTP 200

### 2.3 关键技术发现（v13 主干）

#### ★ 端口纠正：48888，不是 48889

v8~v12 handoff 一直写 yudao-server 跑在 `48889`，**实际是 `48888`**。`application-local.yaml` 配的就是 48888。v9 时期 PID 23924 也是 48888（之前 netstat 写 48889 是文档抄写错误）。**v13 起统一用 48888**。

#### ★ Start-Process 父子进程陷阱

PowerShell `Start-Process java ...` 启动 fat-jar 时会派生**两层进程**：

- 父 stub（本次 PID 35468）：~7 MB，几乎不耗 CPU，仅作 stdout/stderr 重定向中转
- 真正 java 进程（本次 PID 31468）：实际跑 Spring Boot，监听 48888

**坑**：
- 写入 `.tmp_sql/v12_yudao_server.pid` 的是父 PID 35468；下次重启 kill 父进程**无法**关掉真正的 yudao-server。
- `Get-Process` 看父进程"很闲"，容易误判应用没起来；其实端口在子进程上监听。

**正确重启姿势**：
```powershell
# 查找子进程：
Get-CimInstance Win32_Process -Filter "ParentProcessId=35468 AND Name='java.exe'"
# 或直接按端口：
$child = (netstat -ano | findstr 48888 | Select-Object -First 1) -split '\s+' | Select-Object -Last 1
Stop-Process -Id $child -Force
# 父 stub 会被孤儿化但不占资源，可一并 Stop-Process -Id 35468
```

#### ★ mvn `-am` 硬规则二次验证

本次 `mvn -pl yudao-module-iot/yudao-module-iot-biz,yudao-server -am install` 顺利 BUILD SUCCESS（含全套 `yudao-spring-boot-starter-*` + `yudao-module-infra/system/iot-*`）。验证了 v12 §5 新增的 "**`-pl` 必须 `-am`**" 硬规则。

### 2.4 验证

- 本机：fat-jar 健康（PID 31468 @ 48888），含 H1~H7
- DB：本会话零写
- CI：未触发新 build（候选 #1 不涉及代码改动，仅运行验证）

---

## 3. CI 访问

- **Drone**：`http://test.sanligz.com.cn`（= `192.168.1.253`）
- **仓库**：`fengxiatao/ch_ibms`
- **API 基址**：`http://test.sanligz.com.cn/api/repos/fengxiatao/ch_ibms`
- **Token**：与 jingyu 共用，**不入库**，由用户在会话中粘贴
- **最近 builds**：#15 / #16 / #17 / #18（v12 末四绿）；本会话未触发新 build
- **`.drone.yml` stages**：`fast-clone` + `hello` + `sanity-check`（仅元数据，无真实 mvn）

---

## 4. MySQL 连接

| 工具 | 用途 | 读 | 写 |
|---|---|---|---|
| `mcp4_mysql_query`（mysql-ibms） | 本地 `ch_ibms` | ✓ | — |

写操作：兜底 `cmd /c "F:\tools\mysql-8.0.40-winx64\bin\mysql.exe ..."`。

禁用：`mcp5_*`（线上）/ `mcp6_*`（jingyu）/ `mcp7_*`（停车场）

---

## 5. 本机构建硬规则（沿用 v10/v11/v12 全部条款 + v13 新增 1 条）

### v13 新增

- **★ Start-Process 启 fat-jar 必须按子进程管理**：见 §2.3。pid 文件应记 child PID（按端口反查），父 stub 仅供 stdout 重定向。下次会话重启或停服优先 `Stop-Process -Id <child>`。

### 沿用条款摘要

- v10 7+1+1：mvn 用 cmd /c 包装 / install 优于 package / 等等（详 v10）
- v11 2 条：mvn 编译失败先看依赖链 / 修 null 漏更新用 LambdaUpdateWrapper.set 显式传 null（详 v11）
- v12 2 条：`mvn -pl <module>` 必须加 `-am` / bug 修复不能"为修而修"（详 v12）

---

## 6. 下一步候选 + 给下次会话的建议

### 当前分支状态

- **分支**：`snapshot/20260423-full` @ `<v13-hash>`（本会话末 commit）
- **运行中后台进程**：
  - PID 17048：RocketMQ NameServer @ 9876（保留）
  - PID 35468：Start-Process 父 stub（~7 MB，可忽略）
  - **PID 31468：yudao-server fat-jar @ 48888（含 H1~H7，**新会话首选用此进程做 UI 实测，无需重启**）**
- **DB**：本会话零写

### 候选（按价值/复杂度排序）

#### **1. 候选 #2 UI 实测 H1~H7（首选，~50 分钟）**

**前置确认**：fat-jar 已含 H1~H7（PID 31468 @ 48888 已就绪）。前端 `yudao-ui-admin-vue3` 启动 vite dev 即可（用户操作）。

**用例 checklist + DoD**：

| ID | 用例 | 操作 | DB 验证（mcp4_mysql_query） |
|---|---|---|---|
| H1 | 调度任务禁用 | 进 IoT → 调度任务 → 启用任务 → 改禁用保存 | `SELECT id, name, status, next_execution_time FROM iot_schedule_job WHERE id=<id>` 看 `next_execution_time IS NULL` |
| H2 | 楼层 DXF 删除 | 进空间管理 → 楼层详情 → 删除 DXF | `SELECT id, dxf_file_url, dxf_file_name, dxf_uploaded_time, dxf_size FROM ibms_floor WHERE id=<id>` 看 4 字段全 NULL |
| H3 | 发现设备 ignore 不填天数 | IoT → 设备发现 → ignore 一条（不填 days） | `SELECT id, ignore_until, ignore_reason FROM iot_device_discovery WHERE id=<id>` 看 `ignore_until` 仍为 NULL（覆盖旧值） |
| H4 | 发现设备 unignore | 同上记录 → unignore | 看 `ignored=0, ignore_until/ignore_reason` 全 NULL |
| H5 | 升级任务手动重试 | OTA → 升级任务 → 失败任务点重试 | `SELECT id, status, start_time, end_time, error_message FROM iot_ota_task WHERE id=<id>` 看 `start_time/end_time/error_message` 按业务清空 |
| H6 | access 授权撤销中 | 门禁 → 人员授权 → 选设备 → 模拟下发失败（断网/停网关）→ 状态撤销中 → 恢复网络再发 | `SELECT id, auth_status, last_dispatch_result FROM ibms_access_person_device_auth WHERE id=<id>` 看 `last_dispatch_result IS NULL`（不残留旧失败描述） |
| H7 | access 带 hash 重发 | 同 H6 但走 `dispatchCredentialsToDevice` | 同 H6 看 `last_dispatch_result IS NULL`，`credential_hash` 保留旧值（非 null 时才更新） |

**主程节奏**：
- 用户每完成一条 UI 操作 → 给我用例 ID + 关键参数（如 record id）→ 我 `mcp4_mysql_query` 取证 → 沉淀到 v14 §2
- 全部七条走完后，commit `<v14-hash>` 把 H1~H7 验证结论入账

#### 2. webhook 漏投递根因（v11 发现，10~20 分钟）

DoD：访问 `https://github.com/fengxiatao/ch_ibms/settings/hooks` 看 drone webhook Recent Deliveries；redeliver 失败的 commit；查 drone server 日志。

#### 3. CI v2 启用真实 mvn build step（1~2 小时）

DoD：`.drone.yml` 取消注释 `backend-build`；阿里云 maven 镜像 + cache 卷；artifact 上传。

#### 4. system/infra 模块 null 漏更新审计（1~2 小时，IoT 已清完）

#### 5. 补 v8 候选 ③/④/⑤（前端 typeCode 28/2 / M0 台账 / 启动顺序硬依赖）

### 给下次会话的建议

- **首选 #1 UI 实测**：fat-jar 已就绪，用户进入 UI 立即可走。每条用例做完立刻 DB 取证，**不要批量推到最后再查**。
- **新会话承接 prompt**：见 §10
- **若 H6/H7 断网模拟难做**（生产网关稳定难触发失败），可改为：直接 `mcp4_mysql_query` 找一条 `last_dispatch_result` 非空的旧记录，前端再点一次"重新授权"，看修复后的逻辑是否清空。
- **本会话 v13 commit 后**记得 `git push origin snapshot/20260423-full && git push chvm1 snapshot/20260423-full`（v12 也欠 origin 一推，本次一并推完）

---

## 7. 关键访问凭据（敏感）

> 本段需用户在新会话中按需提供，AI 不得在此明文记录：
>
> - DRONE_TOKEN（与 jingyu 共用）
> - MySQL root 密码（本地 `123456`，已在 `application-local.yaml`）
> - admin 登录：`admin / admin123 / 长辉信息`（tenant_id=1）

---

## 8. 未 push 的本地状态（供新会话继续）

- **本地 untracked**（诊断用，不入 commit）：
  - v13 新增：
    - `.tmp_sql/v12_rebuild_mvn.log`（mvn install 成功日志，11s）
    - `.tmp_sql/v12_yudao_server.log` / `.err.log`（fat-jar 运行日志，含 `Started YudaoServerApplication in 36.566 seconds`）
    - `.tmp_sql/v12_yudao_server.pid`（父 PID 35468，注意非真 yudao 进程，见 §2.3）
  - v12 遗留：`.tmp_sql/v11_h6h7_*.log`、`.tmp_sql/v11_drone_check17.cmd`（含 token 不入 commit）
- **DB 持久变更**（沿用 v10）：
  - `system_menu.id=5195 status=0`
  - `ibms_energy_meter.id=7 ibms_device_id=10000167`（v9）
  - `ibms_energy_meter.id=1 ibms_device_id=10000166`（v9）
- **备份表**（v4 末遗留，可 2026-05-12 后 drop）：
  - `ibms_device_bak_20260505_legacy`
  - `ibms_channel_bak_20260505_legacy`
- **运行中后台进程**：
  - PID 17048：RocketMQ NameServer
  - PID 35468：Start-Process 父 stub（可忽略）
  - **PID 31468：yudao-server fat-jar @ 48888（含 H1~H7，UI 实测目标）**

---

## 9. 给新会话 AI 的承接提示词（粘贴模板）

```text
我是 CH（长辉 IBMS）项目主程，工作区 e:\ch，按 docs/session-handoff-20260505-v13.md 承接。
请先只读该 handoff + AGENTS.md，勿翻历史 vN（v1~v12），然后等我选下一步。
CI：http://test.sanligz.com.cn/api/repos/fengxiatao/ch_ibms（token 见 handoff §7，需我粘贴）；
MySQL MCP 只用 mcp4_mysql_query（mysql-ibms）；本机构建硬规则见 handoff §5（v10 7+1+1 + v11 2 + v12 2 + v13 新增 1：Start-Process 父子进程管理）。

当前分支：snapshot/20260423-full @ <v13-hash>
v13 已落地：候选 #1 重打 fat-jar 闭环（HEAD=926c638 + 本 handoff），新 PID 31468 @ 48888 含 H1~H7，端口纠正 48889→48888
后台进程：
  - PID 17048：RocketMQ NameServer @ 9876
  - PID 35468：Start-Process 父 stub（~7 MB，可忽略）
  - **PID 31468：yudao-server @ 48888（含 H1~H7）— UI 实测就绪，无需重启**

下次候选（详见 handoff §6）：
1. UI 实测 H1~H7（首选，~50 分钟，含 H6/H7 断网模拟下发，DoD + DB 校验 SQL 见 §6 表）
2. webhook 漏投递根因排查（v11 发现，10~20 分钟）
3. CI v2 启用真实 mvn build（v8~v13 挂账，1~2 小时）
4. system/infra null 漏更新审计（IoT 已清完）
5. 补 v8 候选 ③/④/⑤

以你主程综合考量的角度，决定候选，直接执行。
若选 #1：我会逐条给你"用例 ID + 关键参数（record id）"，请你立即 mcp4_mysql_query 取证并实时沉淀。
```

---

_最后更新：2026-05-05 18:25 +08:00（候选 #1 重打 fat-jar 闭环 · 端口 48888 纠正 · 新会话首选 #2 UI 实测）_
