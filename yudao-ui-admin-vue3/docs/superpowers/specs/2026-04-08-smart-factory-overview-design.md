# 智慧工厂总览重构设计

## 1. 目标

- 以 `e:\ch\smart-factory\src` 的新原型为唯一总览参考来源
- 优先重构智慧工厂总览页，不同步扩散到其他子页面
- 前端使用 Vue3 和当前项目规范重建总览页
- 所有展示数据必须来自后端接口，后端数据必须来自真实数据库
- 不使用前端硬编码业务数据，不使用 mock 数据伪造生产效果

## 2. 范围

### 本期包含

- 重构 `src/views/factory/dashboard/Overview/index.vue`
- 新增面向智慧工厂总览原型的后端聚合接口
- 以 `ch_ibms` 真实表数据作为总览数据来源
- 必要时允许调整数据库聚合口径、补统计查询或补充表结构

### 本期不包含

- 生产监控、环境监控、安全监控、能耗管理、视频监控等子页的完整还原
- 用前端临时 mock 或硬编码伪造指标、趋势、告警、视频数据
- 构造假的 3D 场景行为或不存在的实时主视图数据

## 3. 原型来源与页面映射

### 原型来源

- 原型主入口：`e:\ch\smart-factory\src\App.tsx`
- 总览页面：`e:\ch\smart-factory\src\components\pages\Dashboard.tsx`

### 当前项目承接页

- Vue 总览页：`src/views/factory/dashboard/Overview/index.vue`
- 智慧工厂模块目录：`src/views/factory/dashboard`

## 4. 总览页面结构

总览页按照原型拆分为四个区域：

### 4.1 顶部 KPI 区

- 设备在线率
- 告警数
- 环境合格率
- 今日能耗

### 4.2 左侧运营区

- 楼层切换
- 设备状态列表
- 告警事件列表

### 4.3 中部主视图区

- 工厂主视图承接区
- 快捷操作入口：重置视角、设备定位、巡检漫游、视频融合

### 4.4 右侧监控区

- 实时视频卡片
- 能耗趋势
- 环境监测快照

## 5. 数据来源

### 5.1 已确认的真实表

- `ch_ibms.ibms_device`
- `ch_ibms.ibms_device_runtime`
- `ch_ibms.iot_alert_record`
- `ch_ibms.ibms_channel`
- `ch_ibms.ibms_env_sensor`
- `ch_ibms.ibms_env_data_record`
- `ch_ibms.ibms_energy_meter`
- `ch_ibms.ibms_energy_record`
- `ch_ibms.ibms_energy_statistics_daily`
- `ch_ibms.ibms_space`

### 5.2 已确认的数据可用性

- 设备、运行态、环境、能耗、视频通道均有真实记录
- 告警数据量偏少，前端必须接受真实低量态
- 压差、洁净度字段目前未确认有稳定真实来源
- 中部 3D 区域当前无可直接驱动的真实模型数据

## 6. 后端接口设计

### 6.1 新接口

- `GET /iot/factory/overview`

### 6.2 设计原则

- 前端不再拼接多个旧接口
- 后端按页面区块一次性聚合输出
- 真实数据缺失时返回空态或 `null`，不伪造

### 6.3 响应结构

- `kpis`
- `floors`
- `deviceStatusList`
- `latestAlerts`
- `scene`
- `videoSnapshot`
- `energyTrend`
- `environmentSnapshot`

## 7. 字段与表映射

### 7.1 kpis.deviceOnlineRate

- 来源：`ibms_device`、`ibms_device_runtime`
- 输出：`value`、`online`、`offline`、`inactive`、`total`、`trend`
- `trend` 第一阶段返回 `null`

### 7.2 kpis.alarmCount

- 来源：`iot_alert_record`
- 输出：`value`、`unhandled`、`handled`、`trend`

### 7.3 kpis.environmentComplianceRate

- 来源：`ibms_env_sensor`、`ibms_env_data_record`
- 输出：`value`、`qualified`、`total`、`trend`
- 合格规则由后端定义，不写死在前端

### 7.4 kpis.todayEnergy

- 来源：`ibms_energy_statistics_daily`、`ibms_energy_meter`
- 输出：`value`、`unit`、`electricity`、`water`、`gas`、`trend`
- 第一阶段主值建议以电耗为主

### 7.5 floors

- 来源：`ibms_space`
- 第一阶段只取 `type='floor'` 的楼层节点

### 7.6 deviceStatusList

- 来源：`ibms_device`、`ibms_device_runtime`
- 用于左侧设备状态清单

### 7.7 latestAlerts

- 来源：`iot_alert_record`
- 必要时联查 `ibms_device` 补设备名

### 7.8 scene

- 第一阶段返回真实功能入口配置与默认承接信息
- 不提供假 3D 动画数据

### 7.9 videoSnapshot

- 来源：`ibms_channel`
- 只返回视频入口上下文，不直接返回播放地址

### 7.10 energyTrend

- 来源：`ibms_energy_statistics_daily`
- 按天聚合电、水、气趋势

### 7.11 environmentSnapshot

- 来源：`ibms_env_sensor`、`ibms_env_data_record`
- 温度、湿度、PM2.5、CO2 可接真实值
- 压差、洁净度第一阶段允许返回 `null`

## 8. SQL 口径原则

- 在线率：设备总数来自 `ibms_device`，在线/离线/未激活来自 `ibms_device_runtime.state`
- 告警统计：统一来自 `iot_alert_record.process_status`
- 环境合格率：基于最新采样按后端阈值判定
- 今日能耗与趋势：统一来自 `ibms_energy_statistics_daily`
- 视频卡片：优先使用 `ibms_channel.status='online'` 的通道
- 楼层切换：统一来自 `ibms_space`

## 9. 前端改造原则

- `Overview/index.vue` 不再沿用当前简化总览结构
- 直接按原型总览区块重组页面
- 前端只消费新聚合接口
- 当前 `src/api/factory/dashboard.ts` 应改造成新总览接口的调用入口

## 10. 风险与约束

- 告警数据量少，必须真实呈现，不得扩写
- 空间树楼层数据存在混杂项，后端需要过滤
- 环境部分字段存在缺口，允许空态返回
- 主视图区缺乏真实模型数据，第一阶段只做承接壳

## 11. 实施顺序

1. 定义 `FactoryOverviewRespVO`
2. 实现 `FactoryOverviewController`
3. 实现 `FactoryOverviewService`
4. 根据真实库表完成聚合查询
5. 前端改造 `Overview/index.vue`
6. 联调真实数据与空态表现

## 12. 验收标准

- 总览页结构与 `smart-factory` 的 `Dashboard` 原型主布局一致
- 页面数据全部来自后端真实接口
- 后端接口全部来自真实数据库，不使用 mock
- 缺失数据以空态呈现，不用硬编码补位
- Vue 页面符合当前项目的组件、接口、样式与路由规范
