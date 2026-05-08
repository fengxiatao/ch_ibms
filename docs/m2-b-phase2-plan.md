# M2-B-PHASE2 IotDeviceDO 壳类型清理 — 详细计划

> 创建时间：2026-05-08
> 分支：`feature/m2-b-phase2-iot-device-do-cleanup`
> 父规则：`AGENTS.md §1.2 单源数据`、`docs/ibms-unified-data-source-plan.md §1.2/1.3`
> 工量评估：5-10 工作日，分批 commit

---

## 0. 背景

`IotDeviceDO` 表面上仍是设备 DO，但 DB 中 `iot_device` 表已删，`IotDeviceDO` 已无 `@TableName` 注解（@e:\ch\ruoyi-vue-pro\yudao-module-iot\yudao-module-iot-biz\src\main\java\cn\iocoder\yudao\module\iot\dal\dataobject\device\IotDeviceDO.java:24），实际作为 in-memory DTO 壳类型在传递。

虽不直接违反 `§1.2`（它非"业务模块自建表"场景，是 iot 模块内部传递），**但**：

- 名字"DO"误导后续维护者
- 全部字段定义重复 `IbmsDeviceDO` + `IbmsDeviceRuntimeDO`，单源原则要求收敛
- M6-A 在制定时如果习惯使用 `IotDeviceDO`，会延续旧路径

→ 用户选择全量重构，彻底删除 `IotDeviceDO` 类。

---

## 1. 基础设施现状（已确认）

| 资源 | 状态 |
|---|---|
| `ibms_device` 表 DDL | ✅ 完整覆盖 IotDeviceDO 静态字段（device_code/name/nickname/sn/pic_url/device_key/device_secret/auth_type/subsystem_code/subsystem_override/menu_ids/primary_menu_id/menu_override/dxf_entity_id/device_type/group_code/system_code/device_type_code/product_model/brand/access_type/ip/protocol/product_key/ibms_product_id/group_ids/point_count/points_online/points_alarm/space/extra）|
| `ibms_device_runtime` 表 DDL | ✅ 完整覆盖 IotDeviceDO 运行态/位置字段（state/online_time/offline_time/active_time/firmware_id/gateway_id/location_type/latitude/longitude/area_id/address/campus_id/building_id/floor_id/room_id/local_x/y/z/install_location/install_height_type/config/job_config）|
| `IbmsDeviceDO` Java 类 | ✅ 已映射 ibms_device 全部列 |
| `IbmsDeviceRuntimeDO` Java 类 | ✅ 已映射 ibms_device_runtime 全部列 |
| `IbmsDeviceMapper` | ✅ 已含 access/NVR 必需查询 |
| `AccessDeviceView` | ✅ 已作为视图壳模板可参考 |

**结论：DB + Java DO 层面 0 工作量，纯代码层面重构。**

---

## 2. 字段映射表（核心）

| IotDeviceDO 字段 | IBMS 落点 | getter 改写示例 |
|---|---|---|
| `id` | `IbmsDeviceDO.id` | `device.getId()` 不变 |
| `deviceName` | `IbmsDeviceDO.name` | `device.getDeviceName()` → `device.getName()` |
| `nickname` | `IbmsDeviceDO.nickname` | 不变 |
| `serialNumber` | `IbmsDeviceDO.sn` | `getSerialNumber()` → `getSn()` |
| `picUrl` | `IbmsDeviceDO.picUrl` | 不变 |
| `groupIds` | `IbmsDeviceDO.groupIds` | 不变 |
| `productId` | `IbmsDeviceDO.ibmsProductId` | `getProductId()` → `getIbmsProductId()` |
| `productKey` | `IbmsDeviceDO.productKey` | 不变 |
| `deviceKey` | `IbmsDeviceDO.deviceKey` | 不变 |
| `dxfEntityId` | `IbmsDeviceDO.dxfEntityId` | 不变 |
| `deviceType` | `IbmsDeviceDO.deviceType` | 不变 |
| `subsystemCode` | `IbmsDeviceDO.subsystemCode` | 不变 |
| `subsystemOverride` | `IbmsDeviceDO.subsystemOverride` | 不变 |
| `menuIds` | `IbmsDeviceDO.menuIds` | 不变 |
| `primaryMenuId` | `IbmsDeviceDO.primaryMenuId` | 不变 |
| `menuOverride` | `IbmsDeviceDO.menuOverride` | 不变 |
| `deviceSecret` | `IbmsDeviceDO.deviceSecret` | 不变 |
| `authType` | `IbmsDeviceDO.authType` | 不变 |
| `gatewayId` | `IbmsDeviceRuntimeDO.gatewayId` | `device.getGatewayId()` → `runtime.getGatewayId()` |
| `state` | `IbmsDeviceRuntimeDO.state` | `device.getState()` → `runtime.getState()` |
| `onlineTime` | `IbmsDeviceRuntimeDO.onlineTime` | runtime 取 |
| `offlineTime` | `IbmsDeviceRuntimeDO.offlineTime` | runtime 取 |
| `activeTime` | `IbmsDeviceRuntimeDO.activeTime` | runtime 取 |
| `firmwareId` | `IbmsDeviceRuntimeDO.firmwareId` | runtime 取 |
| `locationType` | `IbmsDeviceRuntimeDO.locationType` | runtime 取 |
| `latitude` | `IbmsDeviceRuntimeDO.latitude` | runtime 取 |
| `longitude` | `IbmsDeviceRuntimeDO.longitude` | runtime 取 |
| `areaId` | `IbmsDeviceRuntimeDO.areaId` | runtime 取 |
| `address` | `IbmsDeviceRuntimeDO.address` | runtime 取 |
| `campusId` | `IbmsDeviceRuntimeDO.campusId` | runtime 取 |
| `buildingId` | `IbmsDeviceRuntimeDO.buildingId` | runtime 取 |
| `floorId` | `IbmsDeviceRuntimeDO.floorId` | runtime 取 |
| `roomId` | `IbmsDeviceRuntimeDO.roomId` | runtime 取 |
| `localX/Y/Z` | `IbmsDeviceRuntimeDO.localX/Y/Z` | runtime 取 |
| `installLocation` | `IbmsDeviceRuntimeDO.installLocation` | runtime 取 |
| `installHeightType` | `IbmsDeviceRuntimeDO.installHeightType` | runtime 取 |
| `config` | `IbmsDeviceRuntimeDO.config` | runtime 取 |
| `jobConfig` | `IbmsDeviceRuntimeDO.jobConfig` | runtime 取 |
| `DEVICE_ID_ALL` 常量 | **TODO**：迁到新建 `IbmsDeviceConstants.java` 或保留 0L 字面量 + 注释 | 见 §3 |

---

## 3. 特殊场景处理

### 3.1 `IotDeviceDO.DEVICE_ID_ALL = 0L` 常量

**唯一引用方**：`IotDataRuleDO.java:95` JavaDoc。

**方案**：新建 `cn.iocoder.yudao.module.iot.dal.dataobject.ibms.IbmsDeviceConstants`：

```java
public final class IbmsDeviceConstants {
    /** 设备编号 - 全部设备（用于规则、消息广播等场景）*/
    public static final Long DEVICE_ID_ALL = 0L;
    private IbmsDeviceConstants() {}
}
```

### 3.2 同时需要 ledger + runtime 的场景（如 NVR 自动登录、设备状态推送）

**两种写法**：

- **A. 双对象传参**：方法签名改为 `foo(IbmsDeviceDO ledger, IbmsDeviceRuntimeDO runtime)`，Service 层显式注入两个 Mapper
- **B. 视图壳**（仿 `AccessDeviceView`）：在该业务模块创建 `XxxDeviceView`，工厂方法 `of(ledger, runtime)`，对外暴露 IotDeviceDO 同名 getter

**优选 A**：更干净；只有方法签名复杂度高时（>3 个调用方）才用 B。

### 3.3 `@Builder` / `IotDeviceDO.builder().xxx()` 调用

`IotDeviceDO` 上有 `@Builder`。如果业务代码使用了 builder 创建实例（in-memory 临时对象），改造时需：

- 若仅用于查询返回值的临时拼装 → 改为 `new IbmsDeviceDO()` + setter
- 若用于持久化（已不存在，因为 `@TableName` 已删）→ 直接 `IbmsDeviceMapper.insert(do)`

**预扫描**：grep `IotDeviceDO.builder` 命中点（在 step C 前先做）。

---

## 4. 39 文件分批迁移顺序

### Batch 1：JavaDoc-only（B 类 + A 类，~10 个文件，1-2h）

只改 `import` + `{@link IotDeviceDO#xxx}` → `{@link IbmsDeviceDO#xxx}`。0 逻辑改动。

- `dal/dataobject/device/IotDeviceMessageDO.java` (1)
- `dal/dataobject/alert/IotAlertRecordDO.java` (2)
- `dal/dataobject/ota/IotOtaTaskRecordDO.java` (3)
- `dal/dataobject/rule/IotDataRuleDO.java` (3, 含 DEVICE_ID_ALL 常量)
- `dal/dataobject/rule/IotSceneRuleDO.java` (5)
- `dal/dataobject/device/config/DeviceConfigHelper.java` (6)
- `dal/tdengine/IotDevicePropertyMapper.java` (2)
- `enums/device/AccessDeviceTypeConstants.java` (3)
- `service/access/dto/AccessDeviceView.java` (7) — JavaDoc 改注释
- `service/camera/dto/CameraDeviceView.java` (6) — 内部已是 IBMS 包装
- `service/ibms/device/support/OtaDeviceView.java` (6) — 内部已是 IBMS 包装

**DoD**：
- [ ] 11 个文件 import 全部切到 `IbmsDeviceDO`
- [ ] grep 这 11 个文件 0 IotDeviceDO 命中
- [ ] 新建 `IbmsDeviceConstants.DEVICE_ID_ALL`
- [ ] mvn compile 通过

### Batch 2：Controller 表面引用（C 类，2 个文件，30min）

- `controller/admin/ota/IotOtaTaskRecordController.java` (6)
- `controller/admin/video/NvrController.java` (3)

**预期**：多为 ApiOperation 注释、@link 引用、PathVariable 类型注解。**不动接口契约**（保持前端调用兼容）。

**DoD**：
- [ ] 2 个 Controller 0 IotDeviceDO import
- [ ] 接口契约（路径/HTTP method/参数名）不变
- [ ] mvn compile 通过

### Batch 3：DO 字段类型签名（部分 D 类轻量文件，~6 文件 1h）

- `service/alert/IotAlertRecordServiceImpl.java` (1)
- `service/subsystem/SubsystemServiceImpl.java` (2)
- `service/device/event/IotDeviceEventService.java` (2) + Impl (2)
- `service/device/handler/event/IotDeviceEventHandler.java` (2)
- `service/device/handler/property/DevicePropertyProcessor.java` (2)

**预期**：方法签名 `foo(IotDeviceDO device)` → `foo(IbmsDeviceDO device)`，getter 调用按映射表替换。

**DoD**：每个文件 0 IotDeviceDO + mvn compile 通过 + 调用方修复（若方法签名变化）。

### Batch 4：handler/property 子树（4 个文件，1.5h）

- `service/device/handler/property/IotDevicePropertyHandler.java` (4)
- `service/device/handler/property/impl/TemperatureAlarmPropertyHandler.java` (4)
- `service/device/handler/IotDeviceServiceHandler.java` (2)
- `service/device/handler/DynamicDeviceServiceInvoker.java` (2)
- `service/device/handler/event/DeviceEventProcessor.java` (2)
- `service/device/handler/event/impl/FaceRecognitionEventHandler.java` (4)

### Batch 5：rule 子树（4 个文件，1-2h）

- `service/rule/scene/IotSceneRuleServiceImpl.java` (2)
- `service/rule/scene/action/IotDeviceServiceInvokeSceneRuleAction.java` (2)
- `service/rule/scene/action/IotDeviceControlSceneRuleAction.java` (2)
- `service/rule/data/IotDataRuleServiceImpl.java` (3)

### Batch 6：device/property 与 support（5 个文件，1.5h）

- `service/device/property/IotDevicePropertyService.java` (3) + Impl (3)
- `service/device/support/IotDeviceSpringCacheEvictor.java` (2)
- `service/device/support/IotLegacyIotDeviceSideEffects.java` (3)
- `service/device/DeviceCoordinateSyncService.java` (5)

### Batch 7：channel + changhui（2 个文件，4-6h，硬骨头）

- `service/channel/IotDeviceChannelServiceImpl.java` (8)
- `service/changhui/device/ChanghuiDeviceServiceImpl.java` (16)

**预期**：业务逻辑密集，需仔细评估每个 getter 调用，可能需要查询 `IbmsDeviceRuntimeDO` 数据。

### Batch 8：核心 Service（最难，1 文件，1-2 天）

- `service/device/IotDeviceService.java` (24)

**预期**：方法签名是公共 API，多被其他 Service 调用。需：
- 保持公共方法名/参数名（避免连锁修改）
- 仅改返回类型 `IotDeviceDO` → `IbmsDeviceDO`（或返回 ledger+runtime 双对象）
- 评估每个调用方是否需要 runtime 字段，决定是否需要新建视图壳

**DoD**：mvn compile 全模块通过 + yudao-server 启动验证 + 抽样 4 个核心 API 通过 Postman 验证。

### Batch 9：删除 IotDeviceDO.java（最后清理）

- 删除 `dal/dataobject/device/IotDeviceDO.java`
- grep 全项目 0 命中验证
- mvn compile + 启动 + Postman 抽样验证

---

## 5. 每批通用 DoD 模板

每批 commit 前必须满足：

1. ✅ 该批文件 grep `\bIotDeviceDO\b` 0 命中
2. ✅ `mvn -pl yudao-module-iot/yudao-module-iot-biz -am compile` 通过
3. ✅ 新增/改动 import 中无 `iot_device` / `IotDeviceMapper` 痕迹
4. ✅ 接口签名改动同步 update 调用方
5. ✅ commit message 列出本批所有文件
6. ✅ push 到内网 + GitHub

最后整体 DoD：

- ✅ `IotDeviceDO.java` 已删除
- ✅ 全项目 grep `IotDeviceDO` = 0
- ✅ `mvn clean package` 整工程通过
- ✅ `yudao-server` 启动成功（48888 端口）
- ✅ 抽样 access/changhui/scene-rule API 200 OK
- ✅ 主计划 `docs/ibms-unified-progress.md` 同步标记 M2-B-PHASE2 完成
- ✅ Drone CI 绿灯

---

## 6. 风险点

1. **`IotDeviceDO.builder()` 链式调用**：如果业务代码用 builder 临时拼装对象，改 IbmsDeviceDO 需要保留 `@Builder` 或重写 setter
2. **`@Data` 注解默认 setter**：业务代码可能 `device.setDeviceName(x)` 写入临时字段，IbmsDeviceDO 字段名不同会编译失败 → 必须按字段映射表改写
3. **方法返回类型变化**：`IotDeviceService.getDevice(Long)` 返回类型从 `IotDeviceDO` 改为 `IbmsDeviceDO` 后，**所有 24 个调用点**都要核查 getter 名
4. **跨模块**：检查 `yudao-module-system`、`yudao-server` 是否也有引用（已确认 grep 范围内仅 `yudao-module-iot-biz` 命中）
5. **CI 风险**：每批 push 前本机 mvn compile 通过，规避 CI 红灯

---

## 7. 承接信息（给下一会话）

下一会话承接时：

1. 看本文件 + `AGENTS.md §1.2` + `docs/session-handoff-20260508-v34.md`
2. 当前分支：`feature/m2-b-phase2-iot-device-do-cleanup`
3. 已完成：Batch 1（部分）— 见 v34 handoff
4. 下一步：继续 Batch 中未完成项 → push → handoff vN+1

---

## 8. 进度跟踪

| Batch | 文件数 | 状态 | commit |
|---|---|---|---|
| 1 - JavaDoc-only | 11 | ✅ 10/11（`DeviceConfigHelper`/`IotDevicePropertyMapper` 推迟到 6/4）| `本批` |
| 2 - Controller | 2 | ✅ 完成（NvrController 删除 0 调用方 `convertChannelsToDevices` 死代码） | `本批` |
| 3 - 轻量 Service | 7 | ✅ 完成（**event 链整体闭环死代码**：`DeviceEventProcessor.processEvent` 0 调用方 + 注释清理 2 文件） | `本批` |
| 4 - handler/property + Mapper | 6+1xml | ✅ 完成（**property 链整体闭环死代码**：`saveDeviceProperty` 0 外部调用，含 `IotDevicePropertyMapper.insert` 签名 + XML OGNL `device.productId`→`device.ibmsProductId`） | `本批` |
| 5 - rule scene action（端点） | 3 | ✅ 完成（仅 `DEVICE_ID_ALL` 切到 `IbmsDeviceConstants` + `IbmsDeviceConstants` 自身 JavaDoc）；rule 子树剩 `IotSceneRuleServiceImpl`/`IotDataRuleServiceImpl` 留 Batch 6 | `本批` |
| 6a - device 死接口 + rule 常量切换 | 1 删 + 2 改 | ✅ 完成（`IotDeviceService` 24 处闭环死接口直接删；`IotSceneRuleServiceImpl`/`IotDataRuleServiceImpl` 切 `IbmsDeviceConstants.DEVICE_ID_ALL`） | `4345ac7` |
| 6b - handler/support 整链死代码清理 | 6 删 | ✅ 完成（`IotDeviceServiceHandler` + `AbstractDeviceServiceHandler` + `DynamicDeviceServiceInvoker` + `DeviceServiceRegistry` + `IotDeviceSpringCacheEvictor` + `IotLegacyIotDeviceSideEffects` 整链 0 调用方）-938 行 | `4345ac7` |
| 6c-1 - channel 链单源化 + 死代码 | 3 改 | ✅ 完成（**channel 链 5 个 private 方法 0 调用方死代码删除**：`syncIpcChannelsViaOnvifToIbms`/`syncIpcChannelsViaOnvif`/`createDefaultChannelInfo`/`createDefaultChannel`/`convertDeviceType`，-180 行；`syncNvrChannelToIbms` 重构为接受 `NvrScannedChannelRow` 去除 IotDeviceDO 入参 + JSON round-trip；`NvrScannedChannelRow` 删 `toLegacyChannelDeviceForSync` 增 `deviceType` 字段；`DeviceConfigHelper` 删 5 个 IotDeviceDO 重载（`hasIpAddress`/`hasPort`/`getNetworkAddress` 全死）） | 本批 |
| 6c-2 - 核心硬骨头剩余 | 2 改 | ⏳ 待开始（`ChanghuiDeviceServiceImpl` 16 + `DeviceCoordinateSyncService` 5） | - |
| 7 - 删除 `IotDeviceDO.java` | 1 | ⏳ 待开始（最后一步，所有调用方清完后；含 `IotDeviceDO` 自身 2 处自引用） | - |
| **总计** | **44** | **33 操作完成 / 44（75%），mvn compile 全通过；剩余 23 处 IotDeviceDO 命中（含自身 2）** | - |
