# 租户套餐 × 租户管理员 回归测试清单

> 对应计划：`tenant-package-permission-alignment_685877b3.plan.md`
> 本文只是回归**步骤清单**，真实执行请在测试环境逐条勾选。

## 0. 前置准备

- [x] 合并本次 `docs/permission-audit/mismatch-report.md` 中列出的全部修复（前端 `v-hasPermi`、SQL 种子、DB patch）
- [x] 新库 / 重置环境执行一次 `sql/mysql/ruoyi-vue-pro.sql` 全量 + 新版 `iot_access_menu.sql` / `iot_patrol_perimeter_video_menu.sql` 等模块菜单脚本，确认**新环境部署即对齐**（`iot_patrol_perimeter_video_menu.sql` 已清掉 `iot:patrol-plan:export`/`iot:patrol-task:export`/`iot:patrol-record:export`，并补齐 start/pause/stop、create/update/delete 按钮）
- [x] 现网环境通过 MCP `mysql-ibms` 执行 `sql/mysql/patch/20260423-permission-align.sql` 前的 `SELECT` 段，核对命中行数后再跑 `UPDATE` 段（2026-04-23 实际跑：仅命中 1 行 `iot:ota-firmware:delete`，其余语句 0 行影响；REMAINING_BAD 复检为 0 行）
- [x] 后端 `mvn clean install -DskipTests` 后重启（依据规则 `20-build-version-consistency`），前端 `pnpm build:prod`（注意 `package.json` 没有通用 `build` 脚本，使用 `build:prod`）

## 1. 权限串三方对齐（抽检）

在 `ch_ibms` 上对以下关键权限做三方抽检：

| 权限 | 后端 `@PreAuthorize` 文件 | 前端 `v-hasPermi` | DB `system_menu.permission` |
| --- | --- | --- | --- |
| `iot:access-alarm:update` | `AccessAlarmController.java` | `views/iot/access/alarm/index.vue` | system_menu 行 |
| `iot:epatrol-point:*` | `EpatrolPointController.java` | `views/iot/patrol/point/index.vue` | system_menu 行 |
| `iot:epatrol-route:*` | `EpatrolRouteController.java` | `views/iot/patrol/route/index.vue` | system_menu 行 |
| `iot:patrol-plan:start` | `PatrolPlanController.java` | `views/iot/patrol/plan/index.vue` | system_menu 行 |
| `iot:patrol-task:update` | `PatrolTaskController.java` | `views/iot/patrol/task/index.vue` | system_menu 行 |
| `iot:patrol-record:query` | `PatrolRecordController.java` | `views/iot/patrol/record/index.vue` | system_menu 行 |
| `iot:ota-firmware:update` | `OtaFirmwareController.java` | `views/iot/ota/firmware/index.vue` | system_menu 行 |

- [x] 三方字面量完全一致，且**不再**出现 `iot:access-alarm:handle` / `iot:patrol-point:*` / `iot:patrol-route:*` / `iot:patrol-plan:trigger` / `iot:patrol-task:complete` / `iot:*:export` / `iot:*:generate-qrcode`（REMAINING_BAD 复检为 0 行）
- [ ] 其它 `security:*` vs `iot:camera:*` 类差异按 `mismatch-report.md` 决议执行完毕（仍待人工走查）

## 2. 多套餐 × 多租户权限矩阵

准备三类套餐（至少）：

1. **超管 / 全功能套餐**（含所有菜单）
2. **智慧安防套餐**（门禁/摄像机/告警 + 基础字典）
3. **受限套餐**（例如仅 `iot:parking:*` 停车场）

对每个套餐分别创建 1 个租户，并用该租户的 tenant_admin 登录：

- [ ] **首页 `Index.vue`**
  - [ ] 顶部 4 个主模块卡片：`security / access / energy / building` 的可点击/置灰状态完全跟随 `permission` + 真实注册路由
  - [ ] 点击有权限的卡片，正确落在对应可访问页面；点击置灰卡片**不**会弹 404 或跳到别家租户页面
  - [ ] 子入口（告警类型 / 电子巡更 / 门禁 / 访客 / 能耗）按权限显示/置灰，无权限时点击给 `ElMessage.warning('暂无访问权限…')` 而不是 404
  - [ ] 页面 `router.resolve` 检查通过：有 `meta.permission` 的路由在 `resolveAndPush` 中也会被预校验

- [ ] **侧栏目录点击**（`useRenderMenuItem.tsx`）
  - [ ] 已挂 `meta.directoryPermission` 的目录跳转到**按权限反查**得到的页面
  - [ ] `meta.directoryLanding` 的目录按指定子菜单跳转
  - [ ] 只有 `legacyClickableDirectoryTitles` 兜底的目录（门禁管理 / 智慧通行 / 智慧建筑 / 智慧楼宇 / 建筑设备监控 / 智慧能源 / 环境监测）点击均落到该租户**可见的第一个叶子**
  - [ ] 子菜单完全不存在时，点击目录标题**不报错、不跳 404**

- [ ] **全局路由守卫**（`permission.ts`）
  - [ ] 手工在地址栏输入**不属于当前租户**的路径：应被守卫重定向到 `/` 并在 console 打 `[route-guard] 目标路由缺少所需权限`
  - [ ] 目标路由没有 `meta.permission` 或 `meta.noPermCheck=true` 时按原逻辑放行

## 3. 按钮/操作列权限显隐

每套餐登录后，逐页核对：

- [x] `iot/access/alarm/index.vue`：只有 `iot:access-alarm:update` 时显示"批量处置"、行内"处置"（源码 L98/L179 核对通过；导出用 `iot:access-alarm:query` L107）
- [x] `iot/patrol/point/index.vue`：编辑/删除按钮权限串为 `iot:epatrol-point:update / delete`；生成二维码按钮挂 `iot:epatrol-point:update`（源码 L39/L47/L56 核对通过。注意前端 `generateQrCode` 调用的 `/iot/patrol-point/generate-qrcode` 后端无实现，属遗留 API 问题，不在本次权限对齐范围）
- [x] `iot/patrol/route/index.vue`：增/改/删按钮按 `iot:epatrol-route:create / update / delete` 显隐（L31/L75/L91）
- [x] `iot/patrol/plan/index.vue`：触发按钮按 `iot:patrol-plan:start`（L114）
- [x] `iot/patrol/task/index.vue`：完成按钮按 `iot:patrol-task:update`（L126）
- [x] `iot/patrol/record/index.vue`：后端无 export 接口，导出按钮已移除（原挂 `iot:patrol-record:query`），避免运行期 404
- [x] `iot/ota/firmware/index.vue`：删除按钮挂 `iot:ota-firmware:update`（后端无 delete，L122 核对通过）

## 4. 租户套餐数据修复（resync）

- [ ] 管理员登录，调用 `POST /admin-api/system/tenant-package/resync-role-menu?id={packageId}`，按单个套餐重推
- [ ] 批量修复：`POST /admin-api/system/tenant-package/refresh-all-tenant-role-menu`
- [ ] 调用后 `select count(*) from system_role_menu where role_id in (select id from system_role where code='tenant_admin' and tenant_id=?)` 的数量与套餐 `menu_ids` 一致
- [ ] 对漂移租户重新登录，**首页/侧栏/按钮/接口** 无 403，无"页面有权限但按钮没"或反过来

## 5. 构建一致性 & 运行环境验证

- [x] 后端侧：`mvn clean install -DskipTests` BUILD SUCCESS（2026-04-23，1m30s，共 29 个子模块全绿），`yudao-server.jar` 重新生成；`TenantPackageController#resyncTenantRoleMenu / refreshAllTenantRoleMenu` 存在
- [x] 前端侧：`pnpm build:prod` 完成（输出 `dist-prod`，exit 0）。`src/utils/menuResolver.ts` / `src/permission.ts` 新增守卫逻辑均纳入构建产物
- [x] 验证没有出现 `NoClassDefFoundError` / 旧 jar 混用（本次全量 `clean install` 已满足规则 `20-build-version-consistency`）
- [ ] 前端构建时出现 5 条 **missing export** 提示（Rollup 不中断）：`getDevice`×3、`getSimpleProductList`、`createDevice`。**非本次权限对齐范围**，但会在运行期命中功能按钮时报错，建议单独起修复 issue

## 6. 回滚预案

- [x] 保留本次 DB patch 之前的 `system_menu`、`system_role_menu` 快照（2026-04-23 已建：`system_menu_bak_20260423` 1600 行 / `system_role_menu_bak_20260423` 3962 行）
- [x] 前端如需回滚，直接 revert 提交即可；`legacyClickableDirectoryTitles` 仍保留，不影响老行为
- [x] 后端新增接口为纯增量，无需回滚
