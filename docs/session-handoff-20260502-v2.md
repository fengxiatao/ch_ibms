# Session Handoff — 2026-05-02 v2（仓库瘦身 + CI 跑通）

> 本文件承接 `session-handoff-20260502-v1.md`。新会话只读本 v2 + `AGENTS.md`，**不回翻 v1**（已归档）。
> 核心变更：仓库 filter-repo 瘦身 + chvm1 本地 git mirror + Drone fast-clone 方案已验证。

---

## 1. 项目骨架

与 v1 一致：后端 `e:\ch\ruoyi-vue-pro`（Spring Boot 3 + 芋道 ruoyi-vue-pro），管理端 `e:\ch\yudao-ui-admin-vue3`（Vue3 + Vite + Element Plus + pnpm）。参考项目目录保持不动。

---

## 2. 本次会话变更

### 2.1 仓库 filter-repo 瘦身 ✅

- 工具：`git filter-repo --paths-from-file .tmp_paths_to_keep.txt`
- 保留路径：`ruoyi-vue-pro/`、`yudao-ui-admin-vue3/`、`docs/`、`AGENTS.md`、`.drone.yml`、`.cursor/rules/*`、`.gitignore`、`.gitattributes`、`.cursorrules`、`.windsurfrules`
- 历史重写前后：
  - `size-pack`：**716 MiB → 349 MiB**（-51%）
  - `in-pack`：~20000+ → **15776 对象**
  - 工作树文件数：13216 → **5824**
- 影响：`snapshot/20260423-full` 分支历史全部重写，已 `--force` push 到远端

### 2.2 CI clone 瓶颈根因定位 + 新方案 ✅

**根因**：`chvm1 → github.com` 下行带宽仅 **~20 KiB/s**（SSH 通道握手正常但拉 pack 极慢；HTTPS 完全不通）。默认 clone 即使仓库瘦到 349 MiB 仍需数十分钟。

**新方案**（Git alternates via host mirror）：
1. chvm1 宿主 `/opt/ci/cache/git/ch_ibms.git` 维护一份 bare mirror
2. 开发者本地 `e:\ch` 直接 push 到 chvm1（走内网 SSH，上行 ~3 MiB/s）
3. Drone step 禁用默认 clone，自定义 `fast-clone` step 从宿主 mirror 本地拉取（无网络）

**关键数据**（Build #4 success 验证）：
- `fast-clone` 耗时：**11 秒** ✅（vs 原方案 24 分钟 timeout）
- 总 build 耗时：656 秒（瓶颈转为首次 `alpine:3.19` pull 591s；后续用 `alpine:3.20` 缓存镜像已改至 baebff1）

### 2.3 本地 remote 配置（`e:\ch/.git/config`）

```
[remote "origin"]
    url = git@github.com:fengxiatao/ch_ibms.git
    fetch = +refs/heads/*:refs/remotes/origin/*
    pushurl = ssh://root@192.168.1.253/opt/ci/cache/git/ch_ibms.git
    pushurl = git@github.com:fengxiatao/ch_ibms.git
[remote "chvm1"]
    url = ssh://root@192.168.1.253/opt/ci/cache/git/ch_ibms.git
    fetch = +refs/heads/*:refs/remotes/chvm1/*
```

**效果**：`git push origin <branch>` 自动推 **chvm1 + github** 双处。fetch 只走 github（避免 chvm1 对象陈旧）。

### 2.4 GitHub Deploy Key（新增）

- 仓库：`fengxiatao/ch_ibms` → Settings → Deploy keys
- Title：`chvm1-mirror-readonly`
- 用途：允许 chvm1 root 从 GitHub fetch ch_ibms（read-only）
- chvm1 侧私钥：`/root/.ssh/id_ed25519_ch_ibms`
- SSH config 别名：`github-ch-ibms:fengxiatao/ch_ibms.git`

### 2.5 commit 列表（分支 `snapshot/20260423-full`）

- `bbc20de` — ci: trigger build after repo slim-down (filter-repo done)
- `cccd62d` — ci(.drone.yml): 切换到宿主 mirror 快速 clone
- `baebff1` — ci(.drone.yml): hello/sanity-check 改用已缓存的 alpine:3.20
- `6cb97e0` — ci: 触发 Build #6 验证 fast-clone 稳定性（空 commit）
- `29a7ea5` — ci: 验证 origin 双 pushurl (chvm1 + github)（空 commit）

### 2.6 Build 历史

| # | Status | 耗时 | 说明 |
|---|---|---|---|
| 1 | killed | 24min | 原版 .drone.yml，clone timeout |
| 2 | killed | 36s | 人工 kill |
| 3 | killed | 12min+ | 人工 kill |
| **4** | **success ✅** | 11min | 首次验证 alternates，fast-clone 11s，alpine:3.19 首 pull 591s |
| 5 | error | 39s | fast-clone 偶发失败，log 为空（疑似 runner 资源竞争） |
| 6 | pending | - | 被 jingyu 并发 build 占槽位（capacity=2） |

### 2.7 备份 Tag

- `backup/pre-cleanup-20260502` →（旧 SHA，filter-repo 前）
- `backup/pre-filter-repo-20260502` → `8f0628e`（filter-repo 前最后状态）
- 两个 tag 均**未 push** 到远端（local-only），如需异地回滚需 `git push origin backup/*`

### 2.8 本地辅助产物（均 gitignored）

- `.tmp_paths_to_keep.txt` — filter-repo 路径清单（5824 个路径）
- `e:\ch_mirror_20260502.git` — filter-repo 前的完整镜像备份（636 MB，可删除）

---

## 3. CI 访问

### 3.1 基本信息（与 v1 一致）

| 项 | 值 |
|---|---|
| Drone Server | `http://test.sanligz.com.cn` |
| 内网地址 | `192.168.1.253`（drone server + runner 同机） |
| Runner 机器 | `chvm1` (Ubuntu 22.04)，`DRONE_RUNNER_CAPACITY=2` |
| Repo | `fengxiatao/ch_ibms` (trusted: `true`) |
| Settings | `http://test.sanligz.com.cn/fengxiatao/ch_ibms/settings` |
| API 基址 | `http://test.sanligz.com.cn/api/repos/fengxiatao/ch_ibms` |

### 3.2 Token 访问（不入 git）

`.drone-token.local`（`KEY=VAL` 单行）→ `Get-Content` 加载到环境变量 `$env:DRONE_TOKEN`。详见 v1 §3.2。

### 3.3 Pipeline Steps 映射（Build #4 实测）

```
builds/{n}/logs/{stage}/{step}
  stage=1 (ci-smoke):
    step=1  fast-clone      # 从宿主 /opt/ci/cache/git/ch_ibms.git 本地 clone (~11s)
    step=2  hello           # 环境变量打印 (alpine:3.20)
    step=3  sanity-check    # 文件存在性检查 (alpine:3.20)
```

**宿主 volume 挂载**（需 repo trusted）：
```yaml
volumes:
  - name: git-mirror
    host: { path: /opt/ci/cache/git/ch_ibms.git }
steps:
  - name: fast-clone
    image: alpine/git:latest
    volumes: [{ name: git-mirror, path: /mirror }]
    commands:
      - git init -q .
      - git remote add origin /mirror
      - git fetch --depth 1 -q origin "$DRONE_COMMIT"
      - git checkout -q FETCH_HEAD
```

### 3.4 开发者日常工作流（重要！）

```powershell
# 编辑代码后，正常提交
git add <path>
git commit -m "..."

# push 用 origin（已配双 pushurl，会自动推 chvm1 + github）
git push origin snapshot/20260423-full

# 触发顺序：
#   1. push 到 chvm1 mirror （秒级增量）
#   2. push 到 github        （上行 3 MB/s）
#   3. github webhook 到 Drone （可能偶发丢失）
#   4. Drone fast-clone 从 chvm1 mirror 拉最新 SHA
```

**如 webhook 丢失**：用 Drone API `POST /api/repos/fengxiatao/ch_ibms/builds/{n}`（restart）基于同 SHA 重跑。

---

## 4. MySQL 连接（与 v1 完全一致）

| 项 | 值 |
|---|---|
| 允许工具 | `mcp4_mysql_query`（唯一） |
| MCP 连接名 | `mysql-ibms` |
| 目标库 | `ch_ibms` @ `127.0.0.1` |
| 只读 | 是（写操作走 `run_command` + 用户批准） |

禁用：`mcp5`（线上）、`mcp6`（jingyu，**严禁跨项目**）、`mcp7`（parking）。

---

## 5. 本机构建硬规则（与 v1 一致）

见 `.cursor/rules/14-local-build.mdc`。要点：mvn/pnpm 目录统一、`-q` 静默、`git add` 精确路径、不自动启服务。

**新增约束**：
- **禁止**在 `e:\ch` 执行 `git lfs migrate` 或 `git filter-repo`（历史已瘦身完毕，二次操作风险大）
- **必要性**：本地 push 必须保证先推 chvm1 再推 github（已通过双 pushurl 保证）；若手工单独 `git push chvm1` 或单独 `git push github` 请自己处理顺序

---

## 6. 下一步候选（按优先级）

### P0：验证 fast-clone 稳定性（当前未完成）

**背景**：Build #5 fast-clone 偶发 error（38s exit 255，log 空）。Build #6 已 restart 但被 jingyu 占槽。

**DoD**：
- 连续 3 次 build 都 fast-clone success，耗时 < 30s
- 若再出现 error，debug 方法：
  - 检查 drone-runner 日志：`docker logs drone-runner --since 10m`
  - 查 sqlite log：`docker exec drone-server sqlite3 /data/database.sqlite "SELECT log_data FROM logs WHERE log_id=<step_id>;"`
  - 手动复现：`docker run --rm --entrypoint=sh -v /opt/ci/cache/git/ch_ibms.git:/mirror:ro -w /work alpine/git:latest -c '<commands>'`

### P1：`.drone.yml` 增加 backend-build / frontend-build

仓库已精简且 clone 稳定后可做。初步指令见 `.drone.yml` 末尾注释区。

**DoD**：单次 push 能跑完 `mvn -q -T 1C clean package -DskipTests`（后端）和 `pnpm i --frozen-lockfile && pnpm build:prod`（前端），两 step 绿灯。

**预估耗时**：后端 mvn 首次 3-5 min（依赖拉取），前端 pnpm 2-3 min。Maven 依赖可用 `/opt/ci/cache/m2` 宿主挂载复用（已预留目录）。

### P2：Push 备份 tag 到远端

```powershell
git push origin backup/pre-filter-repo-20260502
git push origin backup/pre-cleanup-20260502
```

**DoD**：GitHub 上能看到两个 tag，作为异地回滚锚点。

### P3：webhook 可靠性（观察中）

部分 push 没有自动触发 build（如 6cb97e0 commit 未触发 Build #6）。需要持续观察，必要时对比 GitHub Webhook Deliveries 页面查 failure 记录。Drone restart API 可作应急手段。

### P4：归档 v1 handoff

- 建议：v1 移至 `docs/archive/session-handoff-20260502-v1.md`，避免下次 AI 误读
- 或加前缀 `_session-handoff-20260502-v1.md` 弱化命中

---

## 7. 给下次会话的建议

### 7.1 承接时首先做

```text
我是 CH 项目主程，按 docs/session-handoff-20260502-v2.md 承接。
请先只读该 handoff + AGENTS.md，不翻 v1 / 历史 vN。
本地 push 用 git push origin (已配双 pushurl)；
CI: http://test.sanligz.com.cn/api/repos/fengxiatao/ch_ibms (token 见 .drone-token.local)；
MySQL 只用 mcp4_mysql_query (mysql-ibms); 禁 mcp5/6/7。
```

### 7.2 常用命令速查

```powershell
# 加载 drone token
Get-Content 'e:\ch\.drone-token.local' | ForEach-Object { $k,$v = $_ -split '=',2; [Environment]::SetEnvironmentVariable($k,$v,'Process') }
$h=@{Authorization="Bearer $env:DRONE_TOKEN"}

# 查最近 build
Invoke-RestMethod -Headers $h -Uri 'http://test.sanligz.com.cn/api/repos/fengxiatao/ch_ibms/builds?per_page=5' | Select number,status,after

# 查单次 build 详情
Invoke-RestMethod -Headers $h -Uri 'http://test.sanligz.com.cn/api/repos/fengxiatao/ch_ibms/builds/<n>'

# restart build（webhook 丢失时）
Invoke-RestMethod -Headers $h -Method Post -Uri 'http://test.sanligz.com.cn/api/repos/fengxiatao/ch_ibms/builds/<n>'

# 查 chvm1 mirror 状态（SSH MCP）
mcp9_ssh_execute 253-server "cd /opt/ci/cache/git/ch_ibms.git && git rev-parse HEAD && git count-objects -vH"
```

### 7.3 避坑清单

- ❌ 不要 `git push --force` 到 origin（会破坏 chvm1 mirror 同步）
- ❌ 不要在 `.drone.yml` 里用未缓存镜像（会触发 Docker Hub 下载极慢）；**已缓存列表**：`alpine:3.20`、`alpine/git:latest`、`nginx:alpine`、`python:3.11-alpine`、`node:20-alpine`、`redis:6-alpine`
- ❌ 不要修改 `/opt/ci/cache/git/ch_ibms.git` 里的对象（仅通过 `git push chvm1` 写入）
- ✅ 新增 step 尽量重用已缓存镜像；必要时让 chvm1 预 pull：`docker pull <image>`
- ✅ jingyu 并发 build 会占用 runner capacity 2 个槽位中的 1-2 个，ch_ibms build pending > 5min 时可检查 `docker ps --filter 'name=drone-'` 观察并发情况（不干预）
