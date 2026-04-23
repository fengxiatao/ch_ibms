# 新设备类型接入清单（Playbook）（摘录）

> **主文档**：[`iot-newgateway-biz-mq-landing-plan.md`](./iot-newgateway-biz-mq-landing-plan.md) **附录 A**。

用于竖切增量，避免动全局路由策略或第二套状态机。

## 1. 字典与编码

- [ ] 是否需要新增 `ibms_device_type` / `ibms_device_model` / `ibms_point_type`
- [ ] 品牌是否在 `ibms_brand`
- [ ] 区域/系统段是否与现有编码规则一致

## 2. 产品模板

- [ ] `ibms_product` 点位与 `properties` / 表单字段一致
- [ ] 接入参数字段与 `ibms_device.extra` 约定一致

## 3. 台账

- [ ] `extra` JSON 约定（IP、端口、协议、厂家参数）
- [ ] Biz 在创建/更新/删除后发送 `DeviceProfileChanged`（网关不写库）

## 4. deviceType 与契约

- [ ] `iot-core` Topic / DTO 是否需要扩展（优先只增加可选字段）
- [ ] 网关 `PluginConstants` 与 Biz `DeviceCommandPublisher` 使用同一 `deviceType` 字符串
- [ ] 命令是否携带 `brand` / `vendorKey`（多厂家同类型）

## 5. 网关

- [ ] 连接模式与已有插件一致 → 仅增加 **VendorAdapter** 或命令分支
- [ ] 新协议族 → 新 `@DevicePlugin` + `iot.newgateway.plugins.enabled.*`
- [ ] 登录/断线走 **统一生命周期门面**；与 Biz 仅 RocketMQ
- [ ] 禁止业务库访问；禁止新增 Biz HTTP 客户端

## 6. 前端

- [ ] `DICT_TYPE` 与设备表单字段
- [ ] `src/api` 与路由、权限点

## 7. 回归

- [ ] 单设备命令、并发双机、断线重连
- [ ] 字典缓存刷新、网关缓存与 Request-Reply 超时

## 8. 轻量检查

对照主文档 **步骤 0**、**步骤 2**（或摘录文件 `iot-gateway-lightweight-redlines.md`、`iot-biz-gateway-contract.md`）。
