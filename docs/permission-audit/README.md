# 权限三方对照与修复（Tenant Package × Role × Permission）

> 生成时间：2026-04-23
>
> 扫描范围：
> - 后端 `@PreAuthorize('xx')` 共 **441** 条唯一权限串（来自 `ruoyi-vue-pro/yudao-module-*`）
> - 前端 `v-hasPermi` / `checkPermi` 共 **534** 条唯一权限串（来自 `yudao-ui-admin-vue3/src`）
> - 数据库 `ch_ibms.system_menu.permission` 共 774 行（17.6% 为空）

---

## 1. 问题分类总览

| 类别 | 说明 | 影响 | 修复方向 |
|------|------|------|----------|
| A. 前端用了后端没有 | 前端 `v-hasPermi='iot:patrol-point:*'`，后端只有 `iot:epatrol-point:*` | 按钮会 403 | 改前端 |
| B. 数据库有后端没有 | 菜单/按钮 `permission` 在 `system_menu` 但没 controller | 用户能看到/点到但接口 403 | 清理菜单或补后端 |
| C. 后端有数据库没有 | 孤儿 `@PreAuthorize`（菜单未初始化按钮） | 租户管理员没被分配 | 补菜单 SQL |
| D. 安防 security:* | 菜单权限，与 `iot:camera:*` 后端接口不是一对一 | 页面能进，某些接口仍走 `iot:camera:*` | 套餐勾选时两边同勾，或统一前缀 |

---

## 2. 阻塞 tenant_admin 的关键前端错配（类别 A，本次必修）

以下权限串前端在用，但后端 `@PreAuthorize` 和数据库 `system_menu` 都不存在。租户管理员点这些按钮**永远** 403：

### 2.1 巡更点位 / 巡更路线

| 前端错误 | 后端真实 | 命中文件（部分） |
|----------|----------|------------------|
| `iot:patrol-point:delete` | `iot:epatrol-point:delete` | `yudao-ui-admin-vue3/src/views/iot/patrol-plan/point/*` |
| `iot:patrol-point:update` | `iot:epatrol-point:update` | 同上 |
| `iot:patrol-point:generate-qrcode` | *(后端无对应)* | 同上（生成二维码 API 不存在） |
| `iot:patrol-route:create` | `iot:epatrol-route:create` | `yudao-ui-admin-vue3/src/views/iot/patrol-plan/route/*` |
| `iot:patrol-route:update` | `iot:epatrol-route:update` | 同上 |
| `iot:patrol-route:delete` | `iot:epatrol-route:delete` | 同上 |
| `iot:patrol-record:export` | *(后端无对应，应改查询)* | 巡更记录 export 按钮 |

### 2.2 门禁告警 handle/batch-handle/export

| 前端错误 | 后端真实 | 说明 |
|----------|----------|------|
| `iot:access-alarm:handle` | `iot:access-alarm:update` | `AccessAlarmController.java:52` 的处置接口是 `update` |
| `iot:access-alarm:batch-handle` | `iot:access-alarm:update` | 批量处置沿用 update |
| `iot:access-alarm:export` | *(后端无 export 接口)* | 无对应，应去按钮或补后端 |

### 2.3 门禁派发 / 授权记录

| 前端错误 | 后端真实 |
|----------|----------|
| `iot:access-dispatch:redispatch` | *(后端无)* 应走 `iot:access-dispatch:query` + 业务内部 |
| `iot:access-dispatch:export` | *(后端无)* |
| `iot:access-authorization:export` | *(后端无)* |
| `iot:access-record:export` | *(后端无)* |

### 2.4 其他零散错配

| 前端错误 | 后端真实 |
|----------|----------|
| `iot:patrol-task:complete` | `iot:patrol-task:update` 或 `iot:patrol-task:stop` |
| `iot:ota-firmware:delete` | *(后端无)* `IotOtaFirmwareController` 仅有 create/update/query |

---

## 3. 仅影响菜单可见性的漂移（类别 B/C，按优先级后处理）

### 3.1 车位停车 query-btn 命名非标

后端规范后缀应为 `:query`，但 `iot:parking:*:query-btn` 大量使用。**不改后端**（影响面太大），已在 `system_menu` 补齐 query-btn 版本。已是正常工作状态。

### 3.2 数据库冗余/过时权限

以下数据库存在但无后端接口的权限串建议**清理或忽略**，不紧迫：
- `iot:access-test:query`
- `iot:alarm-operation-log:*`
- `iot:channel:*` (旧命名，新命名是 `iot:ibms-channel:*` / `iot:access-channel:*`)
- `iot:data-bridge:export`
- `iot:detong-*` / `iot:keding-*`（厂商设备试点）
- `iot:device-group:*`
- `iot:rule-scene:*` 和 `iot:scene-rule:*`（数据库 rule-scene / 后端 scene-rule 反转）
- `iot:thingmodel:query` （应为 `iot:thing-model:query`）
- `iot:video-patrol-point/record/task-mgmt:*` （重复）

---

## 4. 本次修复动作清单

### 4.1 前端 v-hasPermi 批量替换（阶段 6）

- [x] `iot:patrol-point:*` → `iot:epatrol-point:*`
- [x] `iot:patrol-route:*` → `iot:epatrol-route:*`
- [x] `iot:access-alarm:handle` → `iot:access-alarm:update`
- [x] `iot:access-alarm:batch-handle` → `iot:access-alarm:update`
- [x] 其余无后端对应的按钮（`:export` / `:redispatch` 等）暂保留（前端无权限时自动隐藏即可），待后端补接口

### 4.2 DB patch（阶段 5）

- 检测当前库 `system_menu` 中若存在 `iot:patrol-point:*` / `iot:patrol-route:*` / `iot:access-alarm:handle|batch-handle|export` 按钮条目，统一 `UPDATE` 到正确权限；附 SELECT 预检。

### 4.3 前端首页 / 菜单入口动态化（阶段 7-10）

- 新增 `src/utils/menuResolver.ts`，按 permission / name 反查用户可用路径
- `Index.vue` 卡片跳转改按 permission，无权限则降级/禁用
- `useRenderMenuItem.tsx` 删除 `directoryTitleNavigateTarget` 硬编码
- `resolveAndPush` 增加 permission 预检（已间接通过"只有菜单存在才匹配"实现）

### 4.4 后端运维接口（阶段 11）

已有：`POST /admin-api/system/tenant-package/refresh-all-tenant-role-menu` 全量刷新。
新增：`POST /admin-api/system/tenant-package/refresh-tenant-role-menu?id={id}` 按单个套餐刷新，便于批量修完后只刷动过的。

---

## 5. 清单文件

- `.tmp_backend_permissions.txt`（441 行，已 gitignore）
- `.tmp_frontend_permissions.txt`（534 行，已 gitignore）
- 本文件：`docs/permission-audit/README.md`
- DB patch：`ruoyi-vue-pro/sql/mysql/patch/20260423-permission-align.sql`
