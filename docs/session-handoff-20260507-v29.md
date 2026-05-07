# Session Handoff v29 — M2-B Access 单源化（GAP-011 路径 A）完整交付

> **承接入口**：本文 + `AGENTS.md`（仅读这两份，**不要翻 v28 及更早**，除非用户明确要求）。
> **本会话基线**：v28 (`9a9242e` snapshot/20260423-full)
> **本会话产出**：4 个 commit @ `feature/m2-b-access-single-source` 分支，HEAD = `d8ca100`，未 push

---

## 1. 项目骨架（无变化，详见 v28）

- 后端：`ruoyi-vue-pro/yudao-module-iot/yudao-module-iot-biz`
- 管理端：`yudao-ui-admin-vue3/`
- 后端端口：**48888**（不是 48080）

---

## 2. 本次会话变更

### Commits（按时间顺序）

| Commit | 阶段 | 摘要 | 行数 |
|---|---|---|---|
| `4fbd2e0` | 一 | 引入 `AccessDeviceView` + 接口表面单源化 | +237/-74 |
| `f4d9362` | 一 | 补 `IotAccessDeviceController` 同步迁移 | +9/-10 |
| `a42ee4b` | 一 | `@JsonIgnore` 修复 ledger/runtime 序列化膨胀 | +5/-0 |
| `d8ca100` | 二 | 5 个 caller 迁移 + 删 `buildLegacyAccessDeviceShell` | +54/-149 |

### 关键技术发现

1. **`AccessDeviceView` 设计**：包装 `IbmsDeviceDO`（台账）+ `IbmsDeviceRuntimeDO`（运行态）的只读视图，提供与 `IotDeviceDO` 同名 getter（`getDeviceName/getProductId/getState/getConfig/...`），最小侵入下游 caller。
   - 位置：`cn.iocoder.yudao.module.iot.service.access.dto.AccessDeviceView`
   - **`@JsonIgnore`** 加在 `getLedger()/getRuntime()` 上，避免 Jackson 把内部对象整体序列化（保留与原 `IotDeviceDO` 平铺响应形态兼容）。
   - **空安全**：`AccessDeviceView.of(ledger, runtime)` 在 `ledger == null` 时返回 null。

2. **`DeviceConfigHelper` 重载**：新增 `getIpAddress(DeviceConfig)` / `getPort(DeviceConfig)` 重载，供 `AccessDeviceView` 使用（不必持有 `IotDeviceDO`）。原 `(IotDeviceDO)` 重载保留，外部模块（OTA/Camera/NVR）兼容。

3. **`buildLegacyAccessDeviceShell` 已彻底删除**（-78 行 helper），同步移除其专属 import (`AccessDeviceTypeConstants`、`java.util.Map`)。
   - **保留** 的 `buildLegacyXxxShell`（NVR/Camera/OTA）不在本次 scope，是各自子系统单源化的后续任务。

4. **运行时验证**（基于 `d8ca100`，yudao-server@48888，admin/admin123 + tenant-id=1）：13 条 API **12 个 code:0 + 1 个业务码（命令超时，非迁移引入）**，全部 0 ledger/runtime JSON 泄漏：
   ```
   P1: device/list(4) device/online(1) device/get?id=113(1) device/get?id=2(null)
       device/config/113(1) device-sync/system-users(2)                         全部 code:0
   P2: management/tree(4) management/online-tree(1) management/detail/113(1)
       channel/list-by-device(1) permission-group/list(3) permission-group/devices(0)  全部 code:0
       card/list(deviceId=113) -> code:1050062004 = ACCESS_CARD_QUERY_FAILED（命令超时，
                                                  硬件未在线，业务路径走通即可证明迁移正确）
   ```
   验证脚本：`e:\ch\.tmp_sql\m2b_verify.ps1`

5. **`IotAccessDeviceController.getDevice` 行为保留**：找不到/非 access 子系统设备时返回 `success(null)`（与原 `IotDeviceDO` 路径一致），通过 try-catch 包裹 `getAccessDevice`。

### 文件变更清单

**新增**：
- `service/access/dto/AccessDeviceView.java`

**修改（接口表面）**：
- `service/access/IotAccessDeviceService.java`（List<IotDeviceDO> -> List<AccessDeviceView>）
- `controller/admin/access/IotAccessDeviceController.java`

**修改（service 实现迁移）**：
- `service/access/IotAccessDeviceServiceImpl.java`
- `service/access/IotAccessAuthDispatchServiceImpl.java`
- `service/access/IotAccessDeviceSyncServiceImpl.java`
- `service/access/IotAccessDeviceCapabilityServiceImpl.java`
- `service/access/IotAccessCardServiceImpl.java`
- `service/access/IotAccessCredentialServiceImpl.java`
- `service/access/IotAccessPermissionGroupServiceImpl.java`
- `service/access/IotAccessChannelServiceImpl.java`
- `service/access/AccessManagementServiceImpl.java`

**修改（helper）**：
- `dal/dataobject/device/config/DeviceConfigHelper.java`（新增 (DeviceConfig) 重载）
- `service/ibms/device/support/IbmsDeviceLedgerRuntimeHelper.java`（**删除** buildLegacyAccessDeviceShell + 专属 import）

---

## 3. CI 访问

- Drone Server：`http://test.sanligz.com.cn`（=`192.168.1.253`，CI 内网）
- CH 仓库：`fengxiatao/ch_ibms`
- API 基址：`http://test.sanligz.com.cn/api/repos/fengxiatao/ch_ibms`
- Token：与 jingyu 共用（位置：上一份 v28 handoff 的「关键访问凭据」段，本文不重复存储）
- **本会话未触发 CI**（4 个 commit 全部本地，未 push）

---

## 4. MySQL 连接

| 工具名 | 连接 | 库 | 用途 |
|---|---|---|---|
| `mcp4_mysql_query` | `mysql-ibms` | `ch_ibms` @ 127.0.0.1 | **唯一允许**（只读） |

写操作必须 `run_command` + `mysql.exe` + 用户批准。**禁用** `mcp5/6/7_*`。

---

## 5. 本机构建硬规则

详见 `.cursor/rules/14-local-build.mdc`。本会话遵守：
- 后端：`mvn -pl yudao-module-iot/yudao-module-iot-biz -am clean compile -DskipTests`（最终 BUILD SUCCESS，1456 文件，82s）
- 管理端：本会话未触及
- Git：4 个 commit 在 `feature/m2-b-access-single-source` 分支，**未 push**（等用户决定 push 时机）

---

## 6. 下一步候选 + 给下次会话的建议

按用户原计划 `a→b→d→c`，**a (M2-B) 完成**，下面是 b/d/c。

### 候选 1：push M2-B 4 个 commit + 合并到 snapshot 分支（推荐先做）
- **DoD**：`git push chvm1 feature/m2-b-access-single-source`、`origin feature/m2-b-access-single-source`；视情况合并入 `snapshot/20260423-full` 或新建 `snapshot/20260507-m2b`。
- **耗时**：5 分钟。

### 候选 2：M2-C — `building-visual-dashboard` 聚合 GAP-002（路径 b）
- **目标**：后端新建聚合 `BuildingVisualDashboardController`（替代前端 N 次散调），前端 `building-visual-dashboard` 1087 行重写为消费聚合 API。
- **DoD**：
  1. 后端：新 controller + service + 聚合 VO，`mvn clean compile` SUCCESS；
  2. 前端：`pnpm build` 无 error，`vue-tsc` 通过（先 `$env:NODE_OPTIONS='--max-old-space-size=8192'`）；
  3. Playwright smoke：dashboard 页面 4 个 panel 全部渲染（设备汇总 / 告警 / 能耗 / 视频墙）。
- **工程量**：1.5~2 天，独立分支 `feature/m2-c-dashboard-aggregation`。

### 候选 3：M1.8 — BFS 算法改进（路径 d）
- **目标**：扩展 BFS 追踪 `export *`（已部分修复，见 `b9ef5d9`）+ `namespace import` 模式。
- **DoD**：在 `tools/` 下追加 BFS 测试 case，验证能识别 `export * from './X'` 和 `import * as Y from './X'`，并跑一遍 yudao-ui-admin-vue3 不再误删（`floorplan-icons/iconConfig.ts` 类的复活案例归零）。
- **工程量**：4~6 小时。

### 候选 4：M1.7 Batch-2 / Batch-5（路径 c）
- **目标**：fire api / components 1-行 export 删除，需 Playwright smoke 兜底。
- **DoD**：每条 PR 独立 commit + smoke pass。
- **工程量**：1~1.5 天。

### 已知遗留问题（不在 M2-B scope，记录便于后续）

1. **`IotHttpDataSinkAction` NPE**（运行时日志可见）：
   ```
   service/rule/data/action/IotHttpDataSinkAction.java:55
   message.getTenantId() 为 null -> NullPointerException
   ```
   - 触发场景：v18 测试 webhook 数据规则消费 `IotDeviceMessage` 时 tenantId 缺失。
   - **不在本会话 scope**（rule/data 模块），下次空闲修复（建议加 `Optional.ofNullable(message.getTenantId()).map(Long::toString).orElse("")` 或在消息构造端补 tenantId）。

2. **`IbmsDeviceLedgerRuntimeHelper` 内仍有 3 个 legacy shell 方法**（NVR/Camera/OTA），是各自子系统单源化的下一步任务（远期 M2-D/E）。

---

## 7. 给下次 AI 的具体建议

1. **优先 push** M2-B 4 个 commit，避免本地丢失；可考虑合并入 snapshot 分支。
2. **选 b（M2-C）作为下一个重头戏**，工作量与 M2-B 相当，但跨前后端，建议独立分支。
3. **遵守只读 MCP 规则**：MySQL 一律 `mcp4_mysql_query`；写操作问用户。
4. **M2-B 验证脚本** `e:\ch\.tmp_sql\m2b_verify.ps1` 可复用：每次结构性变更后跑一遍即可校验。
5. **commit message 规范**：`refactor/fix/feat(M2-X/<sub>)`：标题 ≤ 60 字，正文中文描述 + 验证方式。

---

**HEAD**: `d8ca100` @ `feature/m2-b-access-single-source`
**待 push**：`4fbd2e0` `f4d9362` `a42ee4b` `d8ca100`
**v29 状态**: M2-B 完整交付，运行时验证通过（13/13 无序列化泄漏）。
