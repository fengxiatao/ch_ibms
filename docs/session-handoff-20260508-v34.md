# Session Handoff v34 — 立硬规则 §1.2 + 启动 M2-B-PHASE2

> 日期：2026-05-08
> 上一份：`docs/session-handoff-20260507-v33.md`
> 当前分支：`feature/m2-b-phase2-iot-device-do-cleanup`（基于 `feature/cleanup-orphan-erp`）
> 主计划：`docs/ibms-unified-data-source-plan.md`、`docs/m2-b-phase2-plan.md`

---

## 1. 项目骨架（与 v33 同）

- 后端：`ruoyi-vue-pro/`（Spring Boot 3 + MyBatis Plus + Maven 多模块）
- 管理端：`yudao-ui-admin-vue3/`（Vue3 + Vite + ElementPlus）
- 本会话主要触动 `yudao-module-iot/yudao-module-iot-biz`

---

## 2. 本会话变更

### 2.1 commits（按时间正序）

1. `231374f` `docs(rules): 立第二条全局硬规则 §1.2 单源数据 + §1.3 优先级`
   - 分支：`feature/cleanup-orphan-erp`
   - 已 push 内网 + GitHub
2. **本会话末次 commit（待执行）**：`feat(m2bp2): batch1 partial - 5 files JavaDoc/import 切换到 IbmsDeviceDO`
   - 分支：`feature/m2-b-phase2-iot-device-do-cleanup`

### 2.2 新增/修改文件清单

**新增规则文档**（`231374f`）：

- `AGENTS.md` 新增 §1.2 单源数据 + §1.3 本地端口（重排）
- `docs/ibms-unified-data-source-plan.md` 新增 §1.2 + §1.3

**M2-B-PHASE2 启动**（本批 commit）：

- `docs/m2-b-phase2-plan.md`（新建）：详细计划 + 字段映射表 + 9 批次顺序 + 进度跟踪
- 新建 `IbmsDeviceConstants.java`：收敛 `IotDeviceDO.DEVICE_ID_ALL` 常量
- Batch 1 已改 4 个 DO 文件（JavaDoc-only）：
  - `IotDeviceMessageDO.java`：1 处 `@link IotDeviceDO#getId()` → `@link IbmsDeviceDO#getId()`
  - `IotAlertRecordDO.java`：1 处 import + 1 处 @link
  - `IotOtaTaskRecordDO.java`：1 处 import（含新增 IbmsDeviceRuntimeDO）+ 2 处 @link
  - `IotDataRuleDO.java`：1 处 import + DEVICE_ID_ALL 引用切到 IbmsDeviceConstants

### 2.3 关键技术发现

#### Finding A：M2-B 实际已完成（GAP-011 已闭）

调研发现 `access` 业务模块**已完成单源化**：

- DB：`iot_device` 表已删，仅剩 `ibms_device`(49) / `ibms_device_runtime`(31)
- Mapper：全项目 0 处 import `IotDeviceMapper`
- access 子模块（`controller/admin/access/`、`service/access/`）**0 处** import `IotDeviceDO`（仅 `AccessDeviceView.java` JavaDoc 中提到历史名称）
- `AccessDeviceView.java` 类头注释明示 "M2-B GAP-011 单源化"

→ 之前文档说 "M2-B 清理中" 是状态滞后。

#### Finding B：M2-B-PHASE2 = 清理 IotDeviceDO 壳类型（用户主动选 4: 全量重构）

剩余 39 个文件 ~154 处 `IotDeviceDO` 引用，分布：

- A 类（View 壳，已是 IBMS 包装）：4 文件
- B 类（DO 字段类型签名）：8 文件
- C 类（Controller）：2 文件
- D 类（Service 核心）：25 文件，含硬骨头 `IotDeviceService.java`(24) + `ChanghuiDeviceServiceImpl.java`(16)

修正工量：5-10 工作日，分 9 批 commit。

#### Finding C：DB + IBMS DO Java 类已 100% 覆盖 IotDeviceDO 全部字段

字段映射结论（详见 `docs/m2-b-phase2-plan.md §2`）：

- 静态字段（30+）→ `ibms_device`（已建表）→ `IbmsDeviceDO`（已映射）
- 运行态/位置字段（20+）→ `ibms_device_runtime`（已建表）→ `IbmsDeviceRuntimeDO`（已映射）
- 仅 1 个常量 `DEVICE_ID_ALL` 需要新建落点 → 已通过 `IbmsDeviceConstants` 解决

→ Step A（调研 DB）+ Step B（扩展 Java DO）实际 0 工作量，纯代码层面重构。

---

## 3. CI 访问

- Drone Server：`http://test.sanligz.com.cn`
- CH 仓库：`fengxiatao/ch_ibms`
- API 基址：`http://test.sanligz.com.cn/api/repos/fengxiatao/ch_ibms`
- token：与 jingyu 共用，存放位置见 v33

---

## 4. MySQL 连接

- 工具：`mcp4_mysql_query`（`mysql-ibms` 只读）
- 库：`ch_ibms` @ 127.0.0.1
- 写操作：通过 `run_command` + `mysql` 命令行（需用户批准）
- 禁用：`mcp5/6/7`

---

## 5. 本机构建硬规则

参见 `.cursor/rules/14-local-build.mdc`。本会话验证：

```pwsh
cd ruoyi-vue-pro
mvn -pl yudao-module-iot/yudao-module-iot-biz -am -DskipTests compile -B
# → BUILD SUCCESS
```

---

## 6. M2-B-PHASE2 进度

| Batch | 文件数 | 状态 | commit |
|---|---|---|---|
| **1 - JavaDoc-only** | **11** | **🔄 5/11（45%）** | **本会话** |
| 2 - Controller | 2 | ⏳ 待开始 | - |
| 3 - 轻量 Service | 6 | ⏳ 待开始 | - |
| 4 - handler/property | 6 | ⏳ 待开始 | - |
| 5 - rule | 4 | ⏳ 待开始 | - |
| 6 - device/property+support | 5 | ⏳ 待开始 | - |
| 7 - channel+changhui | 2 | ⏳ 待开始 | - |
| 8 - IotDeviceService 核心 | 1 | ⏳ 待开始 | - |
| 9 - 删除 IotDeviceDO.java | 1 | ⏳ 待开始 | - |

### 已完成（5 文件）

1. `IbmsDeviceConstants.java`（新建）
2. `IotDeviceMessageDO.java`
3. `IotAlertRecordDO.java`
4. `IotOtaTaskRecordDO.java`
5. `IotDataRuleDO.java`

### Batch 1 剩余（6 文件）

- `dal/dataobject/rule/IotSceneRuleDO.java` (5)
- `dal/dataobject/device/config/DeviceConfigHelper.java` (6)
- `dal/tdengine/IotDevicePropertyMapper.java` (2)
- `enums/device/AccessDeviceTypeConstants.java` (3)
- `service/access/dto/AccessDeviceView.java` (7) — JavaDoc 改注释
- `service/camera/dto/CameraDeviceView.java` (6)
- `service/ibms/device/support/OtaDeviceView.java` (6)

---

## 7. 下一步候选 + DoD

### 候选 A：继续 Batch 1 剩余（推荐，1-2h）

完成 Batch 1 全部 11 文件，commit + push。

**DoD**：
- [ ] 11 文件全部 grep `IotDeviceDO` = 0
- [ ] `mvn compile` BUILD SUCCESS
- [ ] commit + push

### 候选 B：Batch 2 Controller（30min）

- `IotOtaTaskRecordController.java`（6）
- `NvrController.java`（3）

**DoD**：接口契约不变 + mvn compile + 启 yudao-server 抽样 GET 接口 200

### 候选 C：Batch 3 轻量 Service（1h）

- 6 个轻量 Service，方法签名/getter 改写

**DoD**：mvn compile + 单测保持通过（如有）

### 候选 D：跳过 PHASE2，转做其他

- M2-D 收尾（visual-dashboard 3 空图后端 1-2h）
- M6-A 启动（安防业务补齐 23-30h）
- 用户原始需求：6 个安防 mock 页前后端同步实现

---

## 8. 给下个会话的建议

1. **优先推完 Batch 1**（剩 6 文件，1-2h），保持节奏
2. Batch 1 全完后 commit + push，会话仍有余可启 Batch 2-3
3. **重点**：Batch 7-8（channel/changhui/IotDeviceService）是硬骨头，建议留独立完整会话
4. **Batch 9（删除 IotDeviceDO.java）必须在最后**，且需要全项目 grep 0 验证 + 启动 yudao-server 抽样验证
5. 每批 commit message 格式：`feat(m2bp2): batchN - <描述>`
6. push 频次：每批一 push，避免大累积

---

## 9. 关键引用文档

- `docs/ibms-unified-data-source-plan.md` 主计划（含 §1.1 零 Mock + §1.2 单源数据）
- `docs/m2-b-phase2-plan.md` 本阶段详细计划
- `AGENTS.md` AI Agent 入口规范
- `.cursor/rules/14-local-build.mdc` 本机构建规范
