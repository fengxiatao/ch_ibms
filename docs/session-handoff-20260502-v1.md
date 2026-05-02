# Session Handoff — 2026-05-02 v1（首次建立）

> 本文件由 Windsurf 协助生成。新会话承接时**只读此文件 + `AGENTS.md`**，不翻历史 vN。
> 当前状态：**CI 接入文档化完成，`.drone.yml` 尚未起草，流水线 steps 映射待首次 push 后补充**。

---

## 1. 项目骨架

### 1.1 后端 `e:\ch\ruoyi-vue-pro`
- 框架：Spring Boot 3 + MyBatis Plus，芋道 `ruoyi-vue-pro` 二次开发
- 顶层 `pom.xml`：`e:\ch\ruoyi-vue-pro\pom.xml`
- 核心模块：
  - `yudao-server` — 启动模块（Application 入口）
  - `yudao-framework` — 公共框架
  - `yudao-module-system` — 系统管理（用户、角色、菜单、权限）
  - `yudao-module-infra` — 基础设施（Job、Config、Codegen）
  - `yudao-module-iot` — IoT / 视频 / 设备接入（含 `ZlmStreamServiceImpl` 等）
  - `yudao-dependencies` — BOM 版本管理

### 1.2 管理端 `e:\ch\yudao-ui-admin-vue3`
- 框架：Vue3 + Vite + Element Plus + TypeScript
- 包管理：**pnpm**（脚本见 `package.json`）
- 关键脚本：
  - `pnpm i` — 安装
  - `pnpm dev` — 本地（`--mode env.local`）
  - `pnpm build:prod` — 生产构建
  - `pnpm ts:check` — TS 类型检查
- 构建内存：已设 `--max-old-space-size=8192`

### 1.3 参考项目（不改动）
`dh/`、`anfang/`、`smart-factory/`、`wvp-GB28181-pro/`、`threejs-park-master/`、`parking-miniapp/`、`大华海康代码/`。
未来建议迁移至 `_reference/` 子目录（见"下一步候选"#5）。

---

## 2. 本次会话变更

**本次为 v1 首次建立 handoff，未进行代码修改**。产出物：

- 新增 `AGENTS.md`（项目根，AI Agent 承接入口）
- 新增 `docs/session-handoff-20260502-v1.md`（本文件）
- 新增 `.cursor/rules/14-local-build.mdc`（本机构建硬规则）

**关键技术发现 / 现状摘要**：

- 项目根目录当前混乱（~100 条目，含大量 `.sql` 备份、zip、图片、`.tmp_*` 临时文件），建议后续统一整理
- `.cursor/rules/` 目录存在但为空，此次由 rule 14 填入首条规则
- 仓库内未见 `.drone.yml`，CI 接入尚未开始；需要确认 Drone 侧是否已为 `fengxiatao/ch_ibms` 激活仓库

---

## 3. CI 访问

### 3.1 基本信息

| 项 | 值 |
|---|---|
| Drone Server | `http://test.sanligz.com.cn` |
| 内网地址 | `192.168.1.253`（端口 `:8090` 备用） |
| Repo | `fengxiatao/ch_ibms` |
| Settings | `http://test.sanligz.com.cn/fengxiatao/ch_ibms/settings` |
| API 基址 | `http://test.sanligz.com.cn/api/repos/fengxiatao/ch_ibms` |
| Builds 列表 | `http://test.sanligz.com.cn/api/repos/fengxiatao/ch_ibms/builds` |
| 单次构建日志 | `.../builds/{n}/logs/{stage}/{step}` |

### 3.2 关键访问凭据（**不入 git / 不写代码**）

**token 存放位置**：`e:\ch\.drone-token.local`（已被 `.gitignore` 的 `*.local` 规则覆盖，不会入库）

**使用方式**（PowerShell 读取到环境变量）：

```powershell
Get-Content 'e:\ch\.drone-token.local' | ForEach-Object {
  $k,$v = $_ -split '=',2; [Environment]::SetEnvironmentVariable($k,$v,'Process')
}
# 之后可直接：curl -H "Authorization: Bearer $env:DRONE_TOKEN" http://test.sanligz.com.cn/api/...
```

**文件格式**（单行，KEY=VALUE）：

```text
DRONE_TOKEN=<真实 token>
```

共用 token 说明：

- 当前 CH 与 jingyu 复用同一 drone 用户 token
- 轮换规则：任一项目更换 token 后，**必须同步更新两边 handoff**
- 若后续出现审计或权限隔离需求，再拆分独立 token

### 3.3 Pipeline Steps 映射

**当前尚未建立**，等首次 `.drone.yml` 合入后补充，格式样例：

```text
CI steps 映射（builds/{n}/logs/{stage}/{step}）：
  stage=1:
    step=1  clone
    step=2  backend-build      # mvn -q -T 1C clean package -DskipTests
    step=3  frontend-build     # pnpm i && pnpm build:prod
    step=4  backend-test       # mvn -q test
    step=5  package            # 打 zip / docker image
    step=6  deploy             # 视策略而定
```

**首次建立 `.drone.yml` 的建议 stage 划分**（待下次会话确认）：

- Stage 1 Build：`clone` → `backend-build` → `frontend-build`
- Stage 2 Test：`backend-test`（可并行 `frontend-lint`）
- Stage 3 Release：`package` → `deploy`（仅 `main` 分支触发）

---

## 4. MySQL 连接

| 项 | 值 |
|---|---|
| 允许的 MCP 工具 | `mcp4_mysql_query`（唯一） |
| MCP 连接名 | `mysql-ibms` |
| 目标库 | `ch_ibms` @ `127.0.0.1` |
| 只读 | 是（写操作须 `run_command` + 用户批准） |

**禁用连接**（硬规则）：

- `mcp5_mysql_query`（`mysql-ibms-online`）— 线上库禁止 AI 直连
- `mcp6_mysql_query`（`mysql-jingyudp`）— **jingyu 库，严禁跨项目访问**
- `mcp7_mysql_query`（`mysql-parkingspace`）— 停车场库与 CH 主项目无关

---

## 5. 本机构建硬规则（摘要，完整版见 `.cursor/rules/14-local-build.mdc`）

- **Maven**：统一在 `e:\ch\ruoyi-vue-pro` 下执行；必须加 `-q` 静默；长输出重定向到 `server-mvn.log`
- **单模块验证**：`mvn -q -pl yudao-module-iot verify`，**不要带 `-am`**（会拖垮整库）
- **前端**：统一在 `e:\ch\yudao-ui-admin-vue3` 下用 **pnpm**，禁止 npm / yarn 混用
- **Git 提交**：`git add` 必须写精确路径，禁止 `git add .` / `git add -A`
- **禁止行为**：不自动启动 `yudao-server`、不自动执行 `pnpm dev`、不触碰根目录下 `.sql` 大文件

---

## 6. 下一步候选（3~5 条带 DoD）

1. **起草 `.drone.yml` 初版**
   - DoD：文件合入 `main`，Drone 上成功触发首次 build（即使只有 clone + hello step），并把 steps 映射回填到本文件 §3.3
2. **确认 Drone 仓库是否已激活**
   - DoD：在 `http://test.sanligz.com.cn/fengxiatao/ch_ibms/settings` 看到 `Active = true`，webhook 已注册；未激活则协调管理员启用
3. **`.drone.yml` 增加 backend-build + frontend-build 双 step**
   - DoD：一次 push 能同时跑完后端 `mvn package -DskipTests` 与前端 `pnpm build:prod`，两 step 绿灯，产物 artifact（可选）
4. **给下次会话的建议：补充 pipeline steps 映射**
   - DoD：首次 CI 跑通后，用 `curl $API/builds/1` 拉取实际 step 列表，替换 §3.3 的占位表格，发 v2 handoff
5. **（可选）根目录整理**
   - DoD：创建 `_reference/` 与 `_archive/`，把 `dh/`、`anfang/`、`smart-factory/`、`wvp-GB28181-pro/`、`threejs-park-master/`、`parking-miniapp/`、根目录 `*.sql`/`*.zip`/`*.rar` 分类迁入；先 grep 所有引用（脚本、IDE 配置）再移动，更新 `.gitignore`

---

## 7. 给下次会话的建议

- **先跑 CI 再动代码**：CI 未绿之前别做业务变更，避免引入问题难定位
- **每次交接产出新 vN**：文件名 `session-handoff-YYYYMMDD-vN.md`，N 从当日 1 开始递增
- **vN 只保留 1~2 份活跃**：旧 vN 可移入 `docs/archive/` 或加前缀 `_`，避免 AI 误读
- **commit 前自查**：`git status` 确认只改了主项目目录，参考项目目录 `dh/ anfang/ smart-factory/` 等必须保持干净
