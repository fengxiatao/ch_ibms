# Session Handoff v36 — M6-A 启动：GAP-016/015 切换 IBMS 单源 + 内网 Drone 验证渠道打通

> 日期：2026-05-09
> 上一份：`docs/session-handoff-20260508-v35.md`
> 当前分支：`feature/m2-b-phase2-iot-device-do-cleanup`
> HEAD：`d3036b9`（已 push 内网 + GitHub）
> 主计划：`docs/ibms-unified-data-source-plan.md`、`docs/ibms-bidirectional-gap.md`

---

## 1. 项目骨架（与 v35 同）

- 后端：`ruoyi-vue-pro/`（Spring Boot 3 + MyBatis Plus + Maven 多模块）
- 管理端：`yudao-ui-admin-vue3/`（Vue3 + Vite + ElementPlus + pnpm）
- 本会话主要触动 `yudao-ui-admin-vue3/src/views/security/`（前端）

**端口**：后端 `48888`、前端 `3000`（与 v35 一致，AGENTS.md §1.3 锁定）。

---

## 2. 本会话变更

### 2.1 commits（按时间正序）

| commit | 标题 | 文件 | +/- |
|---|---|---|---|
| `17b2f04` | feat(m6a): GAP-016 MultiScreenPreview 切到 IBMS 单源 API | 1 | (v35 末未 push) |
| `d3036b9` | feat(m6a): GAP-015 views/security/index.vue 切到 IBMS 单源安防概览端点 | 1 | +42 / -83 |

两 commit 已在同一次 push 中推送到 `origin/feature/m2-b-phase2-iot-device-do-cleanup`（内网 192.168.1.253 + GitHub）。

### 2.2 GAP-016 MultiScreenPreview（v35 末沉淀，v36 push）

- import 切换：
  - `@/api/iot/spatial/{building,floor,area}` → `@/api/iot/ibms/space.getSpaceTree`
  - `@/api/iot/channel.getChannelPage` → `@/api/iot/ibms/channel.getChannelPage`
- 树模型对齐：`building/floor/area/channels/channel` → `space/channel`（与 RealTimePreview / DeviceTreePanel 同构）
- 通道分页累计 + `business=sa typeCode=VT`
- vue-tsc：0 新增 TS error

> 备注：GAP-016 核心 3 页（RealTimePreview / VideoPlayback / VisualBoard）早先已切完；本次仅 MultiScreenPreview 1 文件。CameraPlayer.vue / MultiScreenPlayer.vue 在 components/ 下为孤儿组件（无 .vue 引用），跳过。

### 2.3 GAP-015 views/security/index.vue（本会话主产物）

| 维度 | v35 旧实现 | v36 新实现 |
|---|---|---|
| import | `@/api/iot/device`（legacy 老 wrapper） | `@/api/iot/security-overview` |
| 列表 API | `getDeviceList` → `/iot/ibms/device/page`，再客户端 `device.config.features?.includes('安防概览')` 过滤 | `getSecurityOverviewCameras` → `/iot/security-overview/cameras`，服务端按"安防概览"菜单过滤 |
| 字段映射 | `deviceName / state / address / config` | `nickname / online / location`（IbmsDeviceService 单源 VO `SecurityOverviewCameraRespVO`） |
| 抓图 | `<img :src="/admin-api/iot/device/snapshot/{id}" />` 直 URL | `getDeviceSnapshot(id)` 异步拉 base64，写入 `camera.snapshotUrl` |
| 首屏体验 | snapshotUrl 同步赋值 | `onMounted` 后立即 `refreshSnapshots()`，避免空白等待 10s |

**单源数据合规性（AGENTS.md §1.2）**：
- 后端 `IotSecurityOverviewServiceImpl` 22 处 `IbmsDevice` / **0 处 `IotDeviceDO`** —— 已合规
- 前端切到专用 controller，不再绕老 wrapper

**vue-tsc 验证**：项目基线 697 errors，`security/index.vue` 0 命中（0 新增）。

### 2.4 关键技术发现 / 修订

#### Finding A：法定的"安防概览"端点本就存在

`IotSecurityOverviewController` (`/iot/security-overview/cameras|snapshot|play-url`) 与前端 wrapper `@/api/iot/security-overview` 早已建好且 `SecurityOverview/index.vue` 在用。GAP-015 不是缺端点，而是 `views/security/index.vue` 这个老入口忘记切。

#### Finding B：v35 末口算"GAP-015 P1 M6"工作量正确，仅 1 文件 / 1.5h

实际改动仅 import + 1 函数 + 1 函数，没有后端改动。

#### Finding C：内网 Drone 验证渠道打通（关键基础设施修复）

公网反代 `test.sanligz.com.cn:80/api/*` 仍卡（运维问题）。本会话验证：

- **本机可直连** `192.168.1.253:8090`（同 LAN，无需 SSH 跳板）
- token 有效，REST API 返回 HTTP 200 + JSON
- `192.168.1.253` 既是 drone-server 也是 git remote（`ssh://192.168.1.253/opt/ci/cache/git/ch_ibms.git`），`git push` 同时推内网 + GitHub

**新建标准化验证脚本模板**（见 §6）。

---

## 3. CI 访问

- Drone Server：`http://test.sanligz.com.cn`（公网反代异常，运维待修）
- **内网直连**：`http://192.168.1.253:8090` ✅ 工作机可直连
- CH 仓库：`fengxiatao/ch_ibms`
- API 基址（公网）：`http://test.sanligz.com.cn/api/repos/fengxiatao/ch_ibms`
- API 基址（内网）：`http://192.168.1.253:8090/api/repos/fengxiatao/ch_ibms`
- token：与 jingyu 共用，存放位置见 v33（仍是暴露版，建议 reset）

### Build #46 验证（GAP-015/016）

```
number   : 46
status   : success
dur      : 88s
after    : d3036b9 (GAP-015)
event    : push
```

> 同次 push 含 `17b2f04 + d3036b9`，Drone 仅对 head 建 build #46（中间 commit 不单独 build，是 Drone 默认行为）。

---

## 4. MySQL 连接

- 工具：`mcp4_mysql_query`（`mysql-ibms`，本地库 `ch_ibms` @ 127.0.0.1）
- **写权限**：`mcp4_mysql_query` 允许直接写入；破坏性操作需用户批准
- 禁用：`mcp5/6/7`

---

## 5. 本机构建硬规则

参见 `.cursor/rules/14-local-build.mdc`。本会话验证：

```pwsh
cd yudao-ui-admin-vue3
$env:NODE_OPTIONS="--max-old-space-size=8192"
pnpm vue-tsc --noEmit > vtsc.log 2>&1
# → 项目基线 697 errors，security/index.vue 0 命中
```

> vue-tsc 默认 4G 堆易 OOM，需 `NODE_OPTIONS=--max-old-space-size=8192`。

---

## 6. Drone 内网验证脚本模板（v36 新增）

push 后 ~15-30s 等 webhook 拉取，然后：

```pwsh
# 看 build 队列前 4 条
(curl.exe -s -H "Authorization: Bearer el42KeFkkvrOi0vQAj2TKAbxu25VgnDN" `
  http://192.168.1.253:8090/api/repos/fengxiatao/ch_ibms/builds `
  | ConvertFrom-Json) `
  | Select-Object number,status,event,after,@{n='msg';e={$_.message.Split("`n")[0].Substring(0,[Math]::Min(60,$_.message.Split("`n")[0].Length))}} -First 4 `
  | Format-Table -AutoSize | Out-File -Encoding utf8 .tmp_sql\drone_top.txt
Get-Content .tmp_sql\drone_top.txt
```

```pwsh
# 看具体一条 build 详情（含耗时）
(curl.exe -s -H "Authorization: Bearer <TOKEN>" `
  http://192.168.1.253:8090/api/repos/fengxiatao/ch_ibms/builds/<N> `
  | ConvertFrom-Json) `
  | Select-Object number,status,@{n='dur';e={if($_.finished-gt 0){"$($_.finished-$_.started)s"}else{"running"}}}, after `
  | Format-List
```

> ⚠️ 终端 scrollback 容易被前次 raw JSON 污染；务必走 `Out-File` + `Get-Content` 而不是直接 `Format-Table` 到 stdout。

---

## 7. M2-B-PHASE2 进度（沿袭 v35）

**44 / 44 操作完成（100%）**，B7 删除 `IotDeviceDO.java` 已落地（`a4ca55a`）。

| Batch | 状态 | commit |
|---|---|---|
| 1 - JavaDoc-only | ✅ | `6e59399` |
| 2 - Controller | ✅ | `6e59399` |
| 3 - 轻量 Service | ✅ | `6e59399` |
| 4 - handler/property + Mapper | ✅ | `6e59399` |
| 5 - rule scene action | ✅ | `6e59399` |
| 6a/6b - 死代码归零 | ✅ | `4345ac7` |
| 6c-1 - channel 链单源化 | ✅ | `2181b1a` |
| 6c-2 - DeviceCoordinateSyncService | ✅ | `e0a9985` |
| 6c-3 - ChanghuiDeviceServiceImpl | ✅ | `5708910` |
| **7 - 删 IotDeviceDO.java** | ✅ | `a4ca55a` |

PHASE2 闭环 ✅。已切到 M6-A 主线。

---

## 8. M6-A 安防补齐进度（v36 新启动）

主计划 `docs/ibms-bidirectional-gap.md` 9 项安防 GAP（M6 范围）：

| GAP | 描述 | 状态 | commit |
|---|---|---|---|
| GAP-005 ~ GAP-010 | 安防各模块（早先已批量切完）| ✅ | (历史) |
| **GAP-015** | `views/security/index.vue` 切 IBMS 单源安防概览 | **✅** | **`d3036b9`** |
| **GAP-016** | VideoSurveillance 4 子页切 IBMS（核心 3 页早完，本次 MultiScreenPreview） | **✅** | **`17b2f04`** |
| GAP-013 | 新照明前端字段映射 + 大屏聚合端点 | ⏳ P0/P1 | - |
| GAP-014 | env/bac alarm 前端列表+详情对齐 | ⏳ P2 | - |
| GAP-017 | hvac/water 设备外键 DDL 校验 | ⏳ P1 | - |

剩余 3 项预估 14-21h。

---

## 9. 下一步候选 + DoD

### 候选 D-3：GAP-017（推荐先做，2-3h）

**任务**：校验 `ibms_hvac_device` / `ibms_water_device` 是否含 `ibms_device_id` 外键，缺则补 DDL 迁移。

**DoD**：
- [ ] `mcp4_mysql_query` 跑 `SHOW CREATE TABLE ibms_hvac_device / ibms_water_device`
- [ ] 缺字段 → 写迁移 SQL（含 `ALTER TABLE ... ADD COLUMN ibms_device_id BIGINT` + 反查老业务设备 ID 回填）
- [ ] 反查回填查询能 100% 对齐到 `ibms_device.id`，无孤儿
- [ ] 迁移 SQL 提交到 `ruoyi-vue-pro/sql/iot/`
- [ ] commit + push + Drone success（仅 SQL 不影响 build）

### 候选 D-2：GAP-014 env/bac alarm（4-6h）

**任务**：env/bac alarm 子模块前端列表+详情对齐 IBMS 单源（已有表 `ibms_env_alarm` / `ibms_bac_alarm` + 部分 controller，缺前端字段映射 + 详情接口）。

### 候选 D-1：GAP-013 新照明（8-12h，跨前后端）

**任务**：新照明 7 子页（`iot/building/newlight/*`）字段映射 + 大屏聚合端点。`IbmsLightingController` 已有底座但前端字段映射缺。

### 候选 E：v34 能源仪表板瑕疵 backlog

- 浮点数显示精度（19,656.095 kWh 应整化）
- 日期格式 ISO 8601 → 本地化
- 工作量 1-2h，低优先级

---

## 10. 给下个会话的建议

1. **承接提示词**（粘贴启动）：

   ```text
   我是 CH（长辉 IBMS）项目主程，工作区 e:\ch，按 docs/session-handoff-20260509-v36.md 承接。
   请先只读该 handoff + AGENTS.md，勿翻历史 vN（除非我明确要求），然后等我选下一步。
   CI：内网 http://192.168.1.253:8090（公网反代异常未修复），token 见 handoff §3；
   MySQL MCP 只用 mcp4_mysql_query（mysql-ibms）；本机构建硬规则见 .cursor/rules/14-local-build.mdc。
   ```

2. **优先 GAP-017** —— 工作量小、纯后端 DDL、低风险，能给 M2 真正画句号。

3. **公网 Drone 反代**仍未修，运维问题；本会话已建立内网验证 SOP，无需再尝试公网。

4. **vue-tsc 项目基线**当前 697 errors（v36 量），新工作必须 0 新增；OOM 时记得 `NODE_OPTIONS=--max-old-space-size=8192`。

5. **GAP-013 启动前**：先 grep `IbmsLightingController` 看已有端点 → 列出前端 7 子页缺字段清单 → 评估是否需要后端补 RespVO 子字段。可能不止 8h。

6. **token 仍是 v33 暴露版**，建议尽早 reset；reset 后通知 jingyu 项目负责人（共用账号）。

---

## 11. 关键引用文档

- `docs/ibms-unified-data-source-plan.md` 主计划（§1.1 零 Mock + §1.2 单源数据 + §1.3 端口）
- `docs/ibms-bidirectional-gap.md` 17 项 GAP 清单（M6-A 输入）
- `docs/m2-b-phase2-plan.md` PHASE2 详细计划（已 100% 闭环）
- `AGENTS.md` AI Agent 入口规范
- `.cursor/rules/14-local-build.mdc` 本机构建规范
- `docs/session-handoff-20260508-v35.md` 上一份（M2-B 收尾）
