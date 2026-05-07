# Session Handoff v30 — 2026-05-07

> 承接者只读本文件 + `AGENTS.md`，**不要翻 v29 及更早**（除非用户明确要求）。

---

## 1. 项目骨架

- **后端**：`ruoyi-vue-pro/`（Spring Boot 3 + MP；`yudao-server`、`yudao-module-iot`、`yudao-module-system`、`yudao-module-infra`）
- **管理端**：`yudao-ui-admin-vue3/`（Vue3 + Vite + Element Plus + pnpm）
- 当前分支：`feature/m2-d-nvr-single-source` HEAD = `7fa7365`
- 上游基线：`feature/m2-b-access-single-source` (`4e961a0`) — M2-B 已 push 完成

---

## 2. 本次会话变更

### 提交链（基于 `4e961a0`，1 个新 commit，**已 push** ✓）

| Commit | 说明 |
|---|---|
| `7fa7365` | refactor(M2-D/nvr): NVR 通道同步单源化 - 删 buildLegacyNvrDeviceShell（GAP-011 路径 D 阶段一） |

**改动文件**：
- `ruoyi-vue-pro/yudao-module-iot/yudao-module-iot-biz/src/main/java/cn/iocoder/yudao/module/iot/service/channel/IotDeviceChannelServiceImpl.java`：
  - 加 import `IbmsDeviceVideoNetworkResolver`
  - `syncNvrChannelToIbms` 签名 `IotDeviceDO nvrDevice` → `NetworkParams nvrNet`
  - 内部去掉 `nvrDevice.getConfig().toMap()` 解包逻辑，直接从 `nvrNet.ip/username/password/httpPort/rtspPort` 取值
  - 两个 caller（`syncDeviceChannels` line 301、`syncNvrChannels` line 1749）改为调用 `IbmsDeviceVideoNetworkResolver.resolve(ibms, runtime)` 直接得到 NetworkParams
- `ruoyi-vue-pro/yudao-module-iot/yudao-module-iot-biz/src/main/java/cn/iocoder/yudao/module/iot/service/ibms/device/support/IbmsDeviceLedgerRuntimeHelper.java`：
  - 删除 `buildLegacyNvrDeviceShell(IbmsDeviceDO, IbmsDeviceRuntimeDO)`（-27 行）

**净 diff**：+17 / -56（≈ -40 行）

### 关键技术发现

1. **`buildLegacyNvrDeviceShell` 是冗余 round-trip**：`resolve() → NetworkParams` → 打包 `GenericDeviceConfig.set("ipAddress",...)` → 下游 `toMap()` 解包 → `Map.get("ipAddress")` —— 完全是同源同值的反复包装。M2-D 比 M2-B 更进一步：不引入 View，**直接消除 round-trip**。这是更优的"单源化"实现（M2-B 的 AccessDeviceView 是包装视图，M2-D 直接传 NetworkParams 值对象）。

2. **运行时反向验证**：本会话期间 server 处于运行态，NVR id=12 (ip=192.168.1.200) 触发了状态变更 → 通道同步流程，写入 16 个 VT 通道。DB 实测：
   - `stream_main = rtsp://admin:admin123@192.168.1.200:80/cam/realmonitor?channel=N&subtype=0` ✓
   - `username/password = admin/admin123`（fallback 默认值，因 `ibms_device_runtime.config` 为 null）✓
   - `last_sync = 2026-05-07T15:36:22` ←→ 日志状态变更时间戳一致

3. **`IbmsDeviceVideoNetworkResolver.resolve()` 是单一真理源**：旧路径和新路径都从此函数取参数，行为等价由构造保证。

4. **`syncNvrChannel(Long, IotDeviceDO, IotDeviceDO)` 是 0-caller 死代码**（207 行私有方法，G4 清理 `iot_device_channel` 持久层后弃用）。保留待独立 cleanup commit 处理，**未在本次 M2-D scope 内删除**以保持 commit 紧凑。

### 已知遗留 bug（非本次 scope）

- **`IotHttpDataSinkAction:55` NPE**：`IotDeviceMessage.getTenantId()` 为 null 时 `.toString()` 崩溃。会话期间 NVR 12 上线触发 device-state MQ → DataRule 时复现。修复方案：单行 null check 或默认 `0L`。建议下个会话 1 行修复。

---

## 3. CI 访问

- **Drone Server**：`http://test.sanligz.com.cn`（= `192.168.1.253`）
- **CH 仓库**：`fengxiatao/ch_ibms`
- **API 基址**：`http://test.sanligz.com.cn/api/repos/fengxiatao/ch_ibms`
- **Token**：与 jingyu 项目共用 drone 用户 token，**仅存放于本 handoff "关键访问凭据"段**（如需新建放此处，禁止 commit）
- **Drone Step**：默认 pipeline `default` 含 `mvn package` + `pnpm build` + 静态扫描

---

## 4. MySQL 连接（硬性规则）

- **唯一允许**：`mcp4_mysql_query`（`mysql-ibms`）→ 库 `ch_ibms` @ 127.0.0.1（**只读**）
- **禁用**：`mcp5_mysql_query`（线上）、`mcp6_mysql_query`（jingyu）、`mcp7_mysql_query`（停车场）
- **写操作**：必须通过 `run_command` 执行 `mysql` CLI，且需用户明确批准

**本次会话使用**：3 次 SELECT 查询（`ibms_device:12`、`ibms_channel:device_id=12`、`ibms_device_runtime:device_id=12`），无写入。

---

## 5. 本机构建硬规则

详见 `.cursor/rules/14-local-build.mdc`。

**本次会话验证命令**：
```powershell
cd e:\ch\ruoyi-vue-pro
mvn -pl yudao-module-iot/yudao-module-iot-biz -am compile -DskipTests
```
结果：BUILD SUCCESS（22 个 reactor module，0 error）。

---

## 6. 下一步候选

### 候选 a：M2-D 阶段二 — OTA 单源化（推荐，趁 NVR 经验新鲜）

- **目标**：删 `IbmsDeviceLedgerRuntimeHelper.buildLegacyOtaDeviceShell`
- **scope**：7 个 caller（`IotOtaTaskServiceImpl` ×2、`IbmsIotDualTrackDeviceResolver` ×3、`IotOtaUpgradeJob` ×1、`IotDeviceChannelServiceImpl` ×1）
- **方法**：参考 NVR 经验，但 OTA shell 字段更少（id/tenantId/deviceName/productId/productKey/state/firmwareId），可能直接传 `IbmsDeviceDO + IbmsDeviceRuntimeDO` 二元组，或建一个轻量 `OtaDeviceContext` 值对象
- **DoD**：mvn compile SUCCESS、grep 无残留引用、commit + push
- **耗时**：1~1.5 天

### 候选 b：M2-E — Camera 单源化（最复杂）

- **scope**：9+ 个 caller（含 `UniversalCameraCollector` ONVIF 轮询、`IotSecurityOverviewServiceImpl` 安防概览、`IotDeviceConfigServiceImpl` ONVIF 配置同步）
- **风险**：camera shell 字段最丰富（含 vendor 解析、tenantId、firmwareId、活跃时间等），可能需要类似 `AccessDeviceView` 的 View 包装
- **耗时**：1.5~2 天，建议 OTA 完成后再动

### 候选 c：清理死代码 `syncNvrChannel`（207 行）

- **scope**：删除 `IotDeviceChannelServiceImpl` 中 0-caller 私有方法 `syncNvrChannel(Long, IotDeviceDO, IotDeviceDO)`
- **DoD**：mvn compile SUCCESS、文件 -207 行
- **耗时**：15 分钟，纯删减

### 候选 d：修复 `IotHttpDataSinkAction` NPE

- **scope**：`IotHttpDataSinkAction.java:55` 加 `Objects.requireNonNullElse(msg.getTenantId(), 0L)` 或 null guard
- **DoD**：单测 + 重现验证（NVR 上线再触发一次，无 NPE）
- **耗时**：15~30 分钟

### 候选 e：合并 M2-B + M2-D 入 snapshot 标签

- 新建 `snapshot/20260507-m2bd`，fast-forward 到 `7fa7365`，作为下一阶段开发起点
- **耗时**：5 分钟

---

## 7. 给下次会话的建议

1. **先承接**：读本文件 + `AGENTS.md`，跳过 v29 及更早。
2. **快速健康检查**：
   ```powershell
   git -C e:\ch log --oneline -5
   git -C e:\ch status -sb
   ```
   预期 HEAD = `7fa7365`、分支 `feature/m2-d-nvr-single-source`、远端同步。
3. **优先选 a（OTA）**：技术路径已成熟（参考本会话 NVR 经验），收益高（删一个 helper、释放 7 个 caller）。
4. **如果用户没决定**：先做 c（清理死代码）作为热身，~15 分钟低风险，巩固 M2-D 完整性。

---

## 8. 关键访问凭据

> 仅本文件持有，禁止写入代码 / commit / 文档外。

- Drone token：见 v29 同位置（共用 jingyu 用户 token，未轮换）
- 本机 admin token：v14 之后未变化，参考 `.tmp_sql/v14_admin_token.txt`（gitignored）
