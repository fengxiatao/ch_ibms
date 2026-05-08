# AGENTS.md — 长辉 IBMS（CH）项目 AI Agent 承接规范

> 本文件是所有 AI 编码助手（Windsurf / Cursor / Trae / Copilot 等）在 CH 项目工作时的**只读入口**。
> 每次新会话承接必须先读本文件 + 最新一份 `docs/session-handoff-<日期>-vN.md`，**不要翻阅历史 vN**（除非用户明确要求）。

---

## 1. 项目定位

长辉 IBMS 智慧楼宇管理系统，基于芋道 `ruoyi-vue-pro` 框架二次开发。

**主项目两端（只有这两个需要开发）**：

- **后端**：`ruoyi-vue-pro/` — Spring Boot 3 + MyBatis Plus + 多模块 Maven（`yudao-server`、`yudao-module-system`、`yudao-module-infra`、`yudao-module-iot`）
- **管理端**：`yudao-ui-admin-vue3/` — Vue3 + Vite + Element Plus + pnpm

**非主项目（同目录下仅作参考，禁止改动）**：
`dh/`、`anfang/`、`smart-factory/`、`wvp-GB28181-pro/`、`threejs-park-master/`、`parking-miniapp/`、`大华海康代码/` 等。

### 1.1 全局硬规则：零 Mock（2026-05-08）

- **所有前端页面 / 大屏 / API 必须走真实数据**
- **禁用** `builtinData` / `Math.random()` 假数据 / 硬编码假数组 / fallback mock
- **后端 stub 必须填实**，无数据返回空集合而非假数据
- **测试数据用 SQL 种子文件补**（参考 `.tmp_sql/m2d_demo_seed.sql`），**禁止前端 mock**
- 每个 M 阶段 DoD 必须包含「该阶段 0 mock 残留」一项
- 详见主计划 `docs/ibms-unified-data-source-plan.md` §1.1

### 1.2 全局硬规则：单源数据（2026-05-08）

**业务模块禁止再定义自己的设备/空间/通道数据体系，必须统一使用 IBMS 设备中台数据**。

- **禁新建业务专属设备/通道/空间表**：禁 SecurityDeviceDO / AccessDeviceDO / EnergyDeviceDO 等
- **业务表引用设备/空间字段必须用 `ibms_device.id` / `ibms_channel.id` / `ibms_space.id`**（Long 或 JSON ID 数组）
- **业务 Service 只可注入 `Ibms*Mapper`**；禁 `IotDeviceMapper` / `IotAccessDeviceMapper` / `SpatialXxxMapper` 老体系
- **业务 RespVO 设备信息字段只用 `IbmsDeviceRespVO` / `IbmsDeviceRespVO.Simple`**
- **新代码 grep self-check**：提交前 grep `iot_device` / `IotDeviceDO` / `spatial_` 新增 0 命中方可提交
- 历史违规（如 `IotAccessDeviceServiceImpl` 混用 `IotDeviceDO`）→ GAP-011 / M2-B 清理中
- 详见主计划 `docs/ibms-unified-data-source-plan.md` §1.2

### 1.3 本地端口（2026-05-08）

- **后端 yudao-server**：`http://127.0.0.1:48888`（非默认 48080）
- **前端 yudao-ui-admin-vue3**：`http://127.0.0.1:3000`（非 Vite 默认 5173）
- 前端 `.env.dev` 中 `VITE_BASE_URL='http://127.0.0.1:48888'` 已对齐

---

## 2. 承接提示词（新会话三句话模板）

用户启动新会话时，建议粘贴：

```text
我是 CH（长辉 IBMS）项目主程，工作区 e:\ch，按 docs/session-handoff-<日期>-vN.md 承接。
请先只读该 handoff + AGENTS.md，勿翻历史 vN（除非我明确要求），然后等我选下一步。
CI：http://test.sanligz.com.cn/api/repos/fengxiatao/ch_ibms（token 见 handoff）；
MySQL MCP 只用 mcp4_mysql_query（mysql-ibms）；本机构建硬规则见 .cursor/rules/14-local-build.mdc。
```

---

## 3. 六大锚点（handoff 必含，对齐 jingyu v10）

每份 `docs/session-handoff-<日期>-vN.md` 必须包含：

1. **项目骨架**：后端 / 管理端 模块路径
2. **本次会话变更**：commit 列表 + 关键技术发现
3. **CI 访问**：Drone URL + 仓库路径 + token 存放位置 + 各 step 名称
4. **MySQL 连接**：`mcp4_mysql_query`（`mysql-ibms`）+ 目标库 `ch_ibms` + 禁用其他连接
5. **本机构建硬规则**：见 `.cursor/rules/14-local-build.mdc`
6. **下一步候选 + 给下次会话的建议**：3~5 条带 DoD（Definition of Done）

---

## 4. MCP 数据库连接硬性规则

**CH 项目唯一允许的 MySQL MCP 连接：**

| 工具名 | 连接标识 | 库 | 用途 |
|---|---|---|---|
| `mcp4_mysql_query` | `mysql-ibms` | `ch_ibms` @ 127.0.0.1 | CH 本地开发/查询（**只读**） |

**禁用名单**（即使工具列表里出现也不得调用）：

- `mcp5_mysql_query`（`mysql-ibms-online`）— 线上库，AI 不得直接访问
- `mcp6_mysql_query`（`mysql-jingyudp`）— **jingyu 项目库，严禁跨项目写入**
- `mcp7_mysql_query`（`mysql-parkingspace`）— 停车场专用库

**写操作**（INSERT / UPDATE / DELETE / DDL）：`mcp4_mysql_query` 为只读，必须通过 `run_command` 执行 `mysql` 命令行，且需用户明确批准。

---

## 5. CI 环境（与 jingyu 共享同一 Drone 服务器）

- **Drone Server**：`http://test.sanligz.com.cn`（= `192.168.1.253`，CI 内网地址）
- **CH 仓库**：`fengxiatao/ch_ibms`
- **Settings URL**：`http://test.sanligz.com.cn/fengxiatao/ch_ibms/settings`
- **API 基址**：`http://test.sanligz.com.cn/api/repos/fengxiatao/ch_ibms`
- **Token**：与 jingyu 共用同一 drone 用户 token，**仅存放于 handoff 的"关键访问凭据"段**，禁止写入代码、禁止 commit

**共用 token 注意事项**：

- 审计时两项目操作归同一用户，问题排查需配合 commit log
- token 轮换需同步通知 jingyu 项目负责人
- 本项目 AI 不得调用任何涉及 jingyu 仓库的 Drone API

---

## 6. 回复语言

- 所有 AI 交互使用**简体中文**回复
- 代码注释使用中文
- commit message 中文简述 + 英文关键词混合可接受

---

## 7. 相关文件索引

- `.cursorrules` — 前端布局规范（Flexbox、`ibms-fullpage` 等）
- `.windsurfrules` — Windsurf 专用 MCP 规则摘要
- `.cursor/rules/14-local-build.mdc` — 本机构建硬规则（mvn / pnpm / git）
- `docs/session-handoff-<日期>-vN.md` — 每次会话交接
- `docs/permission-audit/` — 权限审计产物（历史资料）
