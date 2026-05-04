# Session Handoff 2026-05-04 v1（M0 业务关联试点完成）

> 承接规则：仅读本文件 + `AGENTS.md`，**不要翻历史 vN**（除非用户明确要求）。

---

## 1. 项目骨架

- **后端**：`ruoyi-vue-pro/`（Spring Boot 3，多模块）
  - 关键模块：`yudao-module-iot/yudao-module-iot-biz`
- **管理端**：`yudao-ui-admin-vue3/`（Vue3 + Vite + pnpm）
- **DB**：MySQL 8 @ `127.0.0.1:3306/ch_ibms`（`root/123456`，本地）

---

## 2. 本次会话变更（M0 业务关联试点）

**目标**：智慧能源 → IBMS 台账双向关联；通道 business 由 system_type 自动推导。

### DDL（已执行）

- 脚本：`ruoyi-vue-pro/sql/m0_ibms_energy_meter_bind.sql`（幂等）
- `ibms_energy_meter` 加 `ibms_device_id BIGINT NULL` + `idx_ibms_device_id`
- `ibms_channel.idx_business` / `idx_system_type` 已存在（脚本兼容）

### 后端

- 新建 `IbmsBusinessMappingHelper`（`service/ibms/facade/`）：五大类↔子系统码映射，
  权威源 `system_dict_data.ibms_group.remark.systems`；硬编码兜底 + `refresh()` 钩子
- `IbmsChannelServiceImpl.create/updateChannel` 与 NVR 同步路径 `upsertChannelsFromRaw`
  → 统一调用 `deriveBusinessFromSystemType`，不自洽时 WARN 并以推导值覆盖
- `IbmsEnergyMeterDO` + `SaveReqVO` + `RespVO` 增加 `ibmsDeviceId`；
  RespVO 增加 4 个展示字段：`ibmsDeviceName/Code/Ip/Space`
- `IbmsEnergyService.bindDevice(meterId, ibmsDeviceId)` + Impl
  （null 表示解绑；设备 group_code 非 SE 仅 WARN 不阻断）
- `IbmsEnergyController` `get/page/list*` 全部走 `enrichMeterList` 批量联表 `ibms_device`；
  新增 `POST /iot/building/energy/meter/bind-device`，权限 `iot:building-energy:update`

### 前端

- `src/api/iot/building/energy.ts`：
  - `IbmsEnergyMeterVO` 增 5 个 `ibmsDevice*` 字段
  - `IbmsEnergyMeterSaveReqVO` 增 `ibmsDeviceId`
  - 新增 `bindMeterDevice(meterId, ibmsDeviceId)`

### 验证

- `mvn -pl yudao-module-iot/yudao-module-iot-biz compile` → ExitCode=0
- `pnpm ts:check` → 整体 Exit=2 但匹配 `ibmsDevice|bindMeterDevice|energy.ts` 无任何报错
  → 本次未引入新 TS 错误；存量错误在 `views/iot/building/energy/{monitor,remote-reading}` 等无关页面

### 关键技术发现

- `views/energy/DeviceManagement/index.vue` 是**纯 mock**演示页，未走真实 API；
  真实页面在 `views/iot/building/energy/*`（既有 TS 错误待修）
- `DictDataCommonApi` 当前 DTO 不含 `remark`，故 mapping helper 暂用硬编码（与字典等价）
- Powershell 不支持 `<` 重定向，执行 `.sql` 必须 `cmd /c "mysql ... < file.sql"`
- `pnpm ts:check` 会 OOM（heap 4GB），必要时 `$env:NODE_OPTIONS='--max-old-space-size=8192'`

### Commits

> 本次未 commit，工作树包含：
> - `ruoyi-vue-pro/sql/m0_ibms_energy_meter_bind.sql`（新增）
> - `yudao-module-iot-biz` 6 个 .java 文件（5 改 1 增）
> - `yudao-ui-admin-vue3/src/api/iot/building/energy.ts`（改）
> - `server-mvn.log` / `ts-check.log`（构建产物，可 .gitignore）

---

## 3. CI 访问

- Drone：`http://test.sanligz.com.cn`（= `192.168.1.253`）
- 仓库：`fengxiatao/ch_ibms`
- API 基址：`http://test.sanligz.com.cn/api/repos/fengxiatao/ch_ibms`
- Token：与 jingyu 共用 drone 用户 token，**不入库**，存放于环境变量 `DRONE_TOKEN`
  或用户手工粘贴；询问用户后使用
- 读 build：`curl -H "Authorization: Bearer $env:DRONE_TOKEN" http://test.sanligz.com.cn/api/repos/fengxiatao/ch_ibms/builds/<n>`
- AI **不得** 调 Drone API 触发/重试/取消 build，除非用户给出 build 编号与动作

---

## 4. MySQL 连接

| 工具 | 用途 |
|---|---|
| `mcp4_mysql_query`（mysql-ibms） | **唯一允许**的本地 `ch_ibms` 只读查询 |
| run_command + `cmd /c "mysql ... < file.sql"` | 写操作（DDL/DML），需用户明确批准 |

禁用：`mcp5_*`（线上）/ `mcp6_*`（jingyu）/ `mcp7_*`（停车场）

---

## 5. 本机构建硬规则（详见 `.cursor/rules/14-local-build.mdc`）

- 后端模块编译：`mvn -q -pl yudao-module-iot/yudao-module-iot-biz compile`（cwd=`ruoyi-vue-pro/`）
- 前端类型检查：在 `yudao-ui-admin-vue3/` 执行 `pnpm ts:check`，必要时加 NODE_OPTIONS=8192
- 禁止：根目录新建 `.tmp_*` / 解压 zip / 提交 1GB 级 sql 全量备份
- 临时文件统一放 `.tmp_sql/` 或 `tmp-check/`

---

## 6. 下一步候选 + 给下次会话的建议

### 候选（按价值/复杂度排序）

1. **【强烈推荐】views/iot/building/energy 页面接入 bind UI**（DoD：仪表表单加 IBMS 设备选择器；列表展示 `ibmsDeviceName/Code/Ip`；行操作"绑定/解绑设备"按钮调通；点一个仪表能成功绑定且刷新后展示）
2. **存量数据回填**（DoD：编写一次性 SQL 按 `meter_code ⇋ ibms_device.device_code` 模糊匹配输出候选清单，人工 review 后 UPDATE；记录在 `.tmp_sql/`）
3. **business 一致性巡检报表**（DoD：SQL 列出 `ibms_channel` 中 `business` 与 helper 推导值不一致的行，输出 csv 供运营修复）
4. **全量 fat-jar 验证**（DoD：`yudao-server` `mvn package -DskipTests` 通过，本机启动无 NPE，访问 `/iot/building/energy/meter/page` 200）
5. **commit & push 当前改动**（DoD：拆 2 个 commit：DDL+helper+channel 一个，energy meter 关联一个；推送后 Drone build 绿）

### 给下次会话的建议

- **优先 #5**（推送本次改动），再做 #1（前端 UI），最后看时间做 #4
- 不要重新探索 mapping 逻辑：硬编码已与字典等价
- 若需读字典 `remark` 字段，需先扩展 `DictDataCommonApi` DTO，跨 module 改动较大，建议另起会话

---

## 关键访问凭据（敏感）

> 本段需用户在新会话中按需提供，AI 不得在此明文记录：
>
> - DRONE_TOKEN（共用 jingyu）
> - MySQL root 密码（本地 `123456`，已在 `application-local.yaml` 内）

---

_最后更新：2026-05-04 10:51 +08:00（M0 业务关联试点完成版）_
